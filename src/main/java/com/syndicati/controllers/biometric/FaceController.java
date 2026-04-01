package com.syndicati.controllers.biometric;

import com.syndicati.models.entities.biometric.FaceCredential;
import com.syndicati.models.entities.User;
import com.syndicati.services.security.FaceEncryptionService;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.models.services.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * FaceID Authentication Controller
 * Handles face enrollment and authentication
 * Matches web version: App\Controller\FaceCred\FaceController
 */
public class FaceController {

    private static final double DISTANCE_THRESHOLD = 0.5;
    private final FaceEncryptionService encryptionService;
    private final UserService userService;
    private final List<FaceCredential> faceCredentialStorage; // In-memory storage (replace with DB)

    public FaceController() {
        this.encryptionService = new FaceEncryptionService();
        this.userService = new UserService();
        this.faceCredentialStorage = new ArrayList<>();
    }

    /**
     * Enroll a user's face for a specific device
     * 
     * @param embedding Array of 384 face descriptor values
     * @param pin User's numeric PIN
     * @param deviceId Device identifier
     * @return Response map with status or error
     */
    public Map<String, Object> enrollFace(double[] embedding, String pin, String deviceId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate request
            if (embedding == null || pin == null || deviceId == null) {
                response.put("error", "Missing required data: embedding, pin, deviceId");
                return response;
            }

            // Get current user from session
            SessionManager sessionManager = SessionManager.getInstance();
            User currentUser = sessionManager.getCurrentUser();

            if (currentUser == null) {
                response.put("error", "User not logged in");
                return response;
            }

            if (embedding.length != 384) {
                response.put("error", "Invalid embedding: must be 384-dimensional");
                return response;
            }

            // Derive encryption key from PIN and email
            byte[] key = encryptionService.deriveKey(pin, currentUser.getEmailUser());

            // Serialize embedding to JSON and encrypt
            String embeddingJson = serializeEmbedding(embedding);
            byte[] encryptedData = encryptionService.encrypt(embeddingJson, key);

            // Check if device already has a credential
            FaceCredential credential = findActiveForUserAndDevice(currentUser.getIdUser(), deviceId);

            if (credential == null) {
                credential = new FaceCredential();
                credential.setUserId(currentUser.getIdUser());
                credential.setDeviceId(deviceId);
                faceCredentialStorage.add(credential);
            }

            credential.setEncryptedFaceid(encryptedData);
            credential.setUpdatedAt(LocalDateTime.now());
            credential.setFlag("active");

            response.put("status", "ok");
            response.put("message", "Face enrolled successfully");
            return response;

        } catch (Exception e) {
            response.put("error", "Enrollment failed: " + e.getMessage());
            return response;
        }
    }

    /**
     * Authenticate user with face recognition
     * 
     * @param email User's email
     * @param embedding Current detected face embedding
     * @param pin User's numeric PIN
     * @param deviceId Device identifier
     * @return Response map with user data or error
     */
    public Map<String, Object> authenticateWithFace(String email, double[] embedding, String pin, String deviceId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate request
            if (email == null || embedding == null || pin == null || deviceId == null) {
                response.put("error", "Missing required data: email, embedding, pin, deviceId");
                return response;
            }

            // Find user by email
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                response.put("error", "User not found");
                return response;
            }

            User user = userOpt.get();
            // Check if user is verified and not disabled
            if (!user.isVerified()) {
                response.put("error", "Account not verified by administrator");
                return response;
            }

            if (user.isDisabled()) {
                response.put("error", "Account is disabled");
                return response;
            }

            // Find enrolled credential for this device
            FaceCredential credential = findActiveForUserAndDevice(user.getIdUser(), deviceId);
            if (credential == null || credential.getEncryptedFaceid() == null) {
                response.put("error", "No FaceID enrolled for this device");
                return response;
            }

            // Derive key and decrypt stored embedding
            byte[] key = encryptionService.deriveKey(pin, user.getEmailUser());
            String decryptedEmbeddingJson = encryptionService.decrypt(credential.getEncryptedFaceid(), key);

            if (decryptedEmbeddingJson == null) {
                response.put("error", "Invalid PIN or corrupted data");
                return response;
            }

            // Parse stored embedding
            double[] storedEmbedding = parseEmbeddingFromString(decryptedEmbeddingJson);

            // Calculate distance
            double distance = encryptionService.calculateDistance(embedding, storedEmbedding);

            // Verify face match
            if (distance < DISTANCE_THRESHOLD) {
                // Update last used timestamp
                credential.setLastUsedAt(LocalDateTime.now());

                // Create session
                SessionManager sessionManager = SessionManager.getInstance();
                sessionManager.setCurrentUser(user);

                response.put("status", "ok");
                response.put("message", "Face verified successfully");
                response.put("distance", distance);
                response.put("user", user.getFirstName() + " " + user.getLastName());
                return response;
            }

            response.put("error", "Face mismatch");
            response.put("distance", distance);
            response.put("threshold", DISTANCE_THRESHOLD);
            return response;

        } catch (Exception e) {
            response.put("error", "Authentication failed: " + e.getMessage());
            return response;
        }
    }

    /**
     * Find active face credential for user and device
     * In production, this would query the database
     */
    private FaceCredential findActiveForUserAndDevice(Integer userId, String deviceId) {
        return faceCredentialStorage.stream()
            .filter(cred -> cred.getUserId().equals(userId) 
                && cred.getDeviceId().equals(deviceId) 
                && cred.isActive())
            .findFirst()
            .orElse(null);
    }

    /**
     * Delete/disable face credential for a device
     */
    public Map<String, Object> disableFace(Integer userId, String deviceId) {
        Map<String, Object> response = new HashMap<>();

        FaceCredential credential = findActiveForUserAndDevice(userId, deviceId);
        if (credential == null) {
            response.put("error", "Credential not found");
            return response;
        }

        credential.setFlag("inactive");
        response.put("status", "ok");
        response.put("message", "FaceID disabled successfully");
        return response;
    }

    /**
     * List all enrolled face credentials for a user
     */
    public List<Map<String, Object>> listUserFaceCredentials(Integer userId) {
        List<Map<String, Object>> list = new ArrayList<>();

        for (FaceCredential cred : faceCredentialStorage) {
            if (cred.getUserId().equals(userId)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", cred.getIdFacecred());
                item.put("deviceId", cred.getDeviceId());
                item.put("flag", cred.getFlag());
                item.put("createdAt", cred.getCreatedAt());
                item.put("lastUsedAt", cred.getLastUsedAt());
                list.add(item);
            }
        }

        return list;
    }

    /**
     * Serialize embedding array to string
     */
    private String serializeEmbedding(double[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Parse embedding array from string representation
     */
    private double[] parseEmbeddingFromString(String str) {
        String cleaned = str.replace("[", "").replace("]", "").trim();
        String[] parts = cleaned.split(",");
        double[] result = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Double.parseDouble(parts[i].trim());
        }
        return result;
    }
}
