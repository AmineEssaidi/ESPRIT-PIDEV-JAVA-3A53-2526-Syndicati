package com.syndicati.services.security;

import com.syndicati.models.user.User;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.models.user.data.UserRepository;
import com.syndicati.services.InsightFaceService;
import com.syndicati.services.DatabaseService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.awt.image.BufferedImage;

/**
 * Handles face recognition enrollment and authentication
 */
public class FaceIDService {
    private static final Logger LOGGER = Logger.getLogger(FaceIDService.class.getName());
    private static final int MIN_GOOD_FRAMES = 20;
    private static final double SPOOFING_THRESHOLD = 0.5; // 0.5+ score = spoof, <0.5 = real
    
    private final UserRepository userRepository;
    private final InsightFaceService insightFaceService;
    private final DatabaseService databaseService;
    private final FaceEncryptionService encryptionService;
    
    private List<double[]> currentEmbeddings;
    private List<InsightFaceService.FaceMesh> frameMeshes; // For liveness analysis via InsightFace

    
    public FaceIDService() {
        this.userRepository = new UserRepository();
        this.insightFaceService = InsightFaceService.getInstance();
        this.insightFaceService.initialize();
        this.databaseService = DatabaseService.getInstance();
        this.encryptionService = new FaceEncryptionService();
        this.currentEmbeddings = new ArrayList<>();
        this.frameMeshes = new ArrayList<>();
    }
    
    /**
     * Start face enrollment process
     */
    public void startEnrollment() {
        currentEmbeddings.clear();
        LOGGER.info("Face ID enrollment started");
    }
    
    /**
     * Add face frame embedding during enrollment
     */
    public void addFrameEmbedding(double[] embedding) {
        if (embedding != null && embedding.length > 0) {
            currentEmbeddings.add(embedding);
            LOGGER.info("Frame added (" + currentEmbeddings.size() + "/" + MIN_GOOD_FRAMES + ")");
        }
    }
    
    /**
     * Add frame for liveness analysis via InsightFace anti-spoofing
     */
    public void addFrameForLivenessCheck(BufferedImage frame) {
        if (frame == null) {
            return;
        }
        
        try {
            // Process frame with InsightFace to get spoofing analysis
            InsightFaceService.FaceMesh mesh = insightFaceService.processFaceFrame(frame, true);
            
            if (mesh != null && mesh.detected) {
                frameMeshes.add(mesh);
                if (frameMeshes.size() <= 10) {
                    LOGGER.info("Frame " + frameMeshes.size() + " analyzed - Spoofing score: " + 
                        String.format("%.4f", mesh.spoofing.spoofingScore) + 
                        " (isSpoof: " + mesh.spoofing.isSpoof + ")");
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error processing frame with InsightFace: " + e.getMessage());
        }
    }

    /**
     * Check liveness using InsightFace anti-spoofing detection
     * Returns true if face passes anti-spoofing checks
     */
    private boolean isLivenessPassedWithInsightFace() {
        try {
            if (frameMeshes.isEmpty()) {
                LOGGER.warning("No frames analyzed for liveness check");
                return false;
            }

            int totalFrames = frameMeshes.size();
            int spoofFrames = 0;
            double avgSpoofingScore = 0.0;
            
            // Analyze all frames collected
            for (InsightFaceService.FaceMesh mesh : frameMeshes) {
                if (mesh.spoofing != null) {
                    avgSpoofingScore += mesh.spoofing.spoofingScore;
                    if (mesh.spoofing.isSpoof) {
                        spoofFrames++;
                    }
                }
            }
            
            avgSpoofingScore /= totalFrames;
            
            LOGGER.info("InsightFace Liveness Analysis:");
            LOGGER.info("  Total frames: " + totalFrames);
            LOGGER.info("  Average spoofing score: " + String.format("%.4f", avgSpoofingScore) + " (threshold: " + SPOOFING_THRESHOLD + ")");
            LOGGER.info("  Frames detected as spoof: " + spoofFrames + "/" + totalFrames);
            
            double passRatio = (double) (totalFrames - spoofFrames) / totalFrames;
            
            // Be strict: Fail if average score is high OR too many individual frames failed
            if (avgSpoofingScore >= SPOOFING_THRESHOLD || passRatio < 0.8) {
                LOGGER.warning("✗ Liveness check FAILED - Spoofing detected by InsightFace");
                LOGGER.warning("  Avg Score: " + String.format("%.4f", avgSpoofingScore) + " (max " + SPOOFING_THRESHOLD + ")");
                LOGGER.warning("  Pass Ratio: " + String.format("%.4f", passRatio) + " (min 0.8)");
                
                if (!frameMeshes.isEmpty() && frameMeshes.get(0).spoofing != null && frameMeshes.get(0).spoofing.indicators != null) {
                    LOGGER.warning("  Spoofing indicators: " + String.join(", ", frameMeshes.get(0).spoofing.indicators));
                }
                return false;
            }
            
            LOGGER.info("✓ Liveness check PASSED - Face verified as live by InsightFace (score: " + 
                String.format("%.4f", avgSpoofingScore) + ")");
            return true;
            
        } catch (Exception e) {
            LOGGER.warning("Liveness check error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Clear frame history after liveness verification
     */
    public void clearFrameHistory() {
        frameMeshes.clear();
    }
    
    /**
     * Get enrollment progress (0-1)
     */
    public double getEnrollmentProgress() {
        return Math.min(1.0, (double) currentEmbeddings.size() / MIN_GOOD_FRAMES);
    }
    
    /**
     * Check if enrollment has enough frames
     */
    public boolean isEnrollmentReady() {
        return currentEmbeddings.size() >= MIN_GOOD_FRAMES;
    }
    
    /**
     * Complete enrollment - saves to facecred table
     */
    public boolean completeEnrollment(String pin) {
        try {
            if (!isEnrollmentReady()) {
                throw new IllegalStateException("Not enough frames captured");
            }
            
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null || currentUser.getIdUser() == null) {
                throw new IllegalStateException("User not logged in");
            }
            
            // Average embeddings
            double[] avgEmbedding = averageEmbeddings(currentEmbeddings);
            
            // Get device ID
            String deviceId = getDeviceId();
            
            // Serialize embedding to JSON
            JSONArray embeddingArray = new JSONArray();
            for (double value : avgEmbedding) {
                embeddingArray.put(value);
            }
            String embeddingJson = embeddingArray.toString();
            
            // Encrypt embedding with PIN-derived key (like Horizon does)
            byte[] encryptedEmbedding = encryptEmbeddingWithPin(embeddingJson.getBytes(), pin, currentUser.getEmailUser());
            
            // Check if enrollment already exists
            FaceCredential existing = retrieveFaceIDEnrollment(currentUser.getIdUser(), deviceId);
            
            LocalDateTime now = LocalDateTime.now();
            
            try (Connection conn = databaseService.getConnection()) {
                if (conn == null) {
                    LOGGER.severe("Cannot connect to database");
                    return false;
                }
                
                if (existing != null) {
                    // Update existing enrollment
                    String sql = "UPDATE facecred SET encrypted_faceid = ?, updated_at = ?, last_used_at = ? WHERE id_facecred = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setBytes(1, encryptedEmbedding);
                        ps.setTimestamp(2, Timestamp.valueOf(now));
                        ps.setTimestamp(3, Timestamp.valueOf(now));
                        ps.setInt(4, existing.getId());
                        ps.executeUpdate();
                    }
                } else {
                    // Insert new enrollment
                    String sql = "INSERT INTO facecred (user_id, device_id, encrypted_faceid, created_at, updated_at, last_used_at, flag) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, currentUser.getIdUser());
                        ps.setString(2, deviceId);
                        ps.setBytes(3, encryptedEmbedding);
                        ps.setTimestamp(4, Timestamp.valueOf(now));
                        ps.setTimestamp(5, Timestamp.valueOf(now));
                        ps.setTimestamp(6, Timestamp.valueOf(now));
                        ps.setString(7, "active");
                        ps.executeUpdate();
                    }
                }
            }
            
            currentEmbeddings.clear();
            LOGGER.info("Face ID enrollment completed for user: " + currentUser.getIdUser());
            return true;
        } catch (Exception e) {
            LOGGER.severe("Face ID enrollment failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Authenticate with face and PIN
     */
    public boolean authenticateWithFaceID(double[] liveEmbedding, String pin) {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null || currentUser.getIdUser() == null) {
                LOGGER.warning("User not logged in");
                return false;
            }
            
            // Verify liveness using InsightFace anti-spoofing
            if (!isLivenessPassedWithInsightFace()) {
                LOGGER.warning("Liveness check failed - possible spoofing attempt detected by InsightFace");
                return false;
            }
            
            String deviceId = getDeviceId();
            
            // Retrieve enrollment from database
            FaceCredential enrollment = retrieveFaceIDEnrollment(currentUser.getIdUser(), deviceId);
            if (enrollment == null) {
                LOGGER.warning("No Face ID enrollment found for user on this device");
                return false;
            }
            
            // Decrypt embedding with PIN-derived key (if PIN is wrong, decryption fails)
            byte[] decryptedEmbeddingBytes = decryptEmbeddingWithPin(enrollment.getEncryptedEmbedding(), pin, currentUser.getEmailUser());
            if (decryptedEmbeddingBytes == null) {
                LOGGER.warning("PIN verification failed - could not decrypt face embedding");
                return false;
            }
            
            // Parse decrypted embedding
            String storedEmbeddingJson = new String(decryptedEmbeddingBytes);
            JSONArray storedArray = new JSONArray(storedEmbeddingJson);
            double[] storedEmbedding = new double[storedArray.length()];
            for (int i = 0; i < storedArray.length(); i++) {
                storedEmbedding[i] = storedArray.getDouble(i);
            }
            
            // Compare embeddings
            double similarity = calculateSimilarity(liveEmbedding, storedEmbedding);
            double threshold = 0.6; // Tunable threshold
            
            if (similarity >= threshold) {
                // Update last used timestamp
                try (Connection conn = databaseService.getConnection()) {
                    if (conn != null) {
                        String sql = "UPDATE facecred SET last_used_at = ? WHERE id_facecred = ?";
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                            ps.setInt(2, enrollment.getId());
                            ps.executeUpdate();
                        }
                    }
                }
                
                LOGGER.info("✓ Face ID authentication successful (InsightFace liveness + embedding verified, similarity: " + String.format("%.4f", similarity) + ")");
                return true;
            } else {
                LOGGER.warning("Face similarity too low: " + String.format("%.4f", similarity) + " (threshold: " + threshold + ")");
                return false;
            }
        } catch (Exception e) {
            LOGGER.severe("Face ID authentication failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if Face ID is enrolled for current user
     */
    public boolean isFaceIDEnrolled() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null || currentUser.getIdUser() == null) {
                return false;
            }
            
            String deviceId = getDeviceId();
            FaceCredential enrollment = retrieveFaceIDEnrollment(currentUser.getIdUser(), deviceId);
            return enrollment != null && enrollment.getFlag().equals("active");
        } catch (Exception e) {
            LOGGER.warning("Error checking Face ID enrollment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove Face ID enrollment
     */
    public boolean removeFaceIDEnrollment() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null || currentUser.getIdUser() == null) {
                return false;
            }
            
            String deviceId = getDeviceId();
            
            try (Connection conn = databaseService.getConnection()) {
                if (conn == null) {
                    LOGGER.severe("Cannot connect to database");
                    return false;
                }
                
                String sql = "DELETE FROM facecred WHERE user_id = ? AND device_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, currentUser.getIdUser());
                    ps.setString(2, deviceId);
                    int rowsDeleted = ps.executeUpdate();
                    
                    if (rowsDeleted > 0) {
                        LOGGER.info("Face ID enrollment removed for device: " + deviceId);
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            LOGGER.warning("Failed to remove Face ID enrollment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // ─── Private Helper Methods ───
    
    private double[] averageEmbeddings(List<double[]> embeddings) {
        if (embeddings.isEmpty()) {
            return new double[128];
        }
        
        double[] avg = new double[embeddings.get(0).length];
        for (double[] embedding : embeddings) {
            for (int i = 0; i < embedding.length; i++) {
                avg[i] += embedding[i];
            }
        }
        for (int i = 0; i < avg.length; i++) {
            avg[i] /= embeddings.size();
        }
        
        return avg;
    }
    
    private double calculateSimilarity(double[] embedding1, double[] embedding2) {
        if (embedding1.length != embedding2.length) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += embedding1[i] * embedding1[i];
            norm2 += embedding2[i] * embedding2[i];
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    private byte[] encryptEmbeddingWithPin(byte[] data, String pin, String email) {
        try {
            byte[] key = encryptionService.deriveKey(pin, email);
            return encryptionService.encrypt(new String(data), key);
        } catch (Exception e) {
            LOGGER.severe("Failed to encrypt embedding: " + e.getMessage());
            return null;
        }
    }

    private byte[] decryptEmbeddingWithPin(byte[] packedData, String pin, String email) {
        try {
            byte[] key = encryptionService.deriveKey(pin, email);
            String decrypted = encryptionService.decrypt(packedData, key);
            return decrypted != null ? decrypted.getBytes() : null;
        } catch (Exception e) {
            LOGGER.warning("Failed to decrypt embedding (invalid PIN?)");
            return null;
        }
    }

    
    private FaceCredential retrieveFaceIDEnrollment(Integer userId, String deviceId) {
        String sql = "SELECT id_facecred, user_id, device_id, encrypted_faceid, created_at, updated_at, last_used_at, flag FROM facecred WHERE user_id = ? AND device_id = ? AND flag = 'active' LIMIT 1";

        
        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                LOGGER.warning("Cannot connect to database");
                return null;
            }
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, deviceId);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        FaceCredential cred = new FaceCredential();
                        cred.setId(rs.getInt("id_facecred"));
                        cred.setUserId(rs.getInt("user_id"));
                        cred.setDeviceId(rs.getString("device_id"));
                        cred.setEncryptedEmbedding(rs.getBytes("encrypted_faceid"));
                        cred.setFlag(rs.getString("flag"));
                        return cred;

                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error retrieving Face ID enrollment: " + e.getMessage());
        }
        
        return null;
    }
    
    private String getDeviceId() {
        try {
            String deviceId = System.getProperty("user.name") + "@" + java.net.InetAddress.getLocalHost().getHostName();
            return deviceId;
        } catch (Exception e) {
            return "unknown-device";
        }
    }
    
    /**
     * FaceCredential Data Class
     */
    private static class FaceCredential {
        private int id;
        private int userId;
        private String deviceId;
        private byte[] encryptedEmbedding;
        private String flag;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public byte[] getEncryptedEmbedding() { return encryptedEmbedding; }
        public void setEncryptedEmbedding(byte[] encryptedEmbedding) { this.encryptedEmbedding = encryptedEmbedding; }
        
        public String getFlag() { return flag; }
        public void setFlag(String flag) { this.flag = flag; }
    }
}
