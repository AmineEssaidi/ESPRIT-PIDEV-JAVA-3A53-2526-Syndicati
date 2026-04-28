package com.syndicati.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * Service to handle profile avatar image uploads and storage.
 * Uses unified uploads folder structure: uploads/profile_images/
 */
public class ProfileImageService {

    private static final String PROFILE_IMAGES_DIR = "uploads" + File.separator + "profile_images";
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(30))
        .build();
    // Last error message for the most recent generation/save operation (helpful for user feedback)
    private static volatile String lastErrorMessage = null;

    static {
        // Ensure profile_images directory exists on startup
        ensureDirectoryExists();
    }

    /**
     * Ensure the profile_images directory exists.
     */
    private static void ensureDirectoryExists() {
        File dir = new File(PROFILE_IMAGES_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("ProfileImageService: Created directory " + PROFILE_IMAGES_DIR);
            } else {
                System.out.println("ProfileImageService: Failed to create directory " + PROFILE_IMAGES_DIR);
            }
        }
    }

    /**
     * Save an avatar image file and return the relative path for storage in the database.
     * Uses collision-resistant naming: avatar_{profileId}_{uniqueId}.{ext}
     *
     * @param fileData Raw file bytes
     * @param originalFileName Original file name (used to extract extension)
     * @param profileId Profile ID for naming
     * @return Relative path for database storage (e.g., "profile_images/avatar_123_abc123.jpg"), or null on error
     */
    public static String saveAvatarImage(byte[] fileData, String originalFileName, int profileId) {
        return saveAvatarImageInternal(fileData, originalFileName, profileId);
    }

    /**
     * Generate an avatar with Pollinations AI, then store it with the same upload path format.
     *
     * @param prompt User prompt for image generation
     * @param profileId Profile ID for naming
     * @return Relative path for database storage, or null on error
     */
    public static String saveGeneratedAvatarImage(String prompt, int profileId) {
        lastErrorMessage = null;
        String normalizedPrompt = normalizePrompt(prompt);
        if (normalizedPrompt.isEmpty()) {
            lastErrorMessage = "Empty prompt provided";
            System.out.println("ProfileImageService.saveGeneratedAvatarImage: Empty prompt");
            return null;
        }

        String encodedPrompt = URLEncoder.encode(normalizedPrompt, StandardCharsets.UTF_8);
        URI uri = URI.create("https://image.pollinations.ai/prompt/" + encodedPrompt + "?width=512&height=512&nologo=true&enhance=true");
        HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(60))
            .GET()
            .build();

        try {
            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());
            System.out.println("ProfileImageService.saveGeneratedAvatarImage: Pollinations status " + response.statusCode());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                lastErrorMessage = "Pollinations returned status " + response.statusCode();
                return null;
            }

            byte[] body = response.body();
            int size = body == null ? 0 : body.length;
            String contentType = response.headers().firstValue("content-type").orElse("").toLowerCase();
            System.out.println("ProfileImageService.saveGeneratedAvatarImage: content-type=" + contentType + " size=" + size);

            if (!contentType.startsWith("image/")) {
                lastErrorMessage = "Unexpected content-type from generator: " + contentType;
                System.out.println("ProfileImageService.saveGeneratedAvatarImage: Unexpected content-type " + contentType);
                return null;
            }

            if (size > MAX_FILE_SIZE) {
                lastErrorMessage = "Generated image too large (" + size + " bytes).";
                System.out.println("ProfileImageService.saveGeneratedAvatarImage: Generated image exceeds max size " + size);
                return null;
            }

            String extension = guessExtensionFromContentType(contentType);
            return saveAvatarImageInternal(body, "generated." + extension, profileId);
        } catch (Exception e) {
            lastErrorMessage = "Exception during generation: " + e.getMessage();
            System.out.println("ProfileImageService.saveGeneratedAvatarImage error:");
            e.printStackTrace();
            return null;
        }
    }

    private static String saveAvatarImageInternal(byte[] fileData, String originalFileName, int profileId) {
        if (fileData == null || fileData.length == 0) {
            lastErrorMessage = "Empty file data";
            System.out.println("ProfileImageService.saveAvatarImage: Empty file data");
            return null;
        }

        if (fileData.length > MAX_FILE_SIZE) {
            lastErrorMessage = "File exceeds max size of " + MAX_FILE_SIZE;
            System.out.println("ProfileImageService.saveAvatarImage: File exceeds max size of " + MAX_FILE_SIZE);
            return null;
        }

        // Extract file extension
        String extension = extractExtension(originalFileName);
        if (!isExtensionAllowed(extension)) {
            lastErrorMessage = "Invalid file extension: " + extension;
            System.out.println("ProfileImageService.saveAvatarImage: Invalid file extension: " + extension);
            return null;
        }

        // Generate unique filename: avatar_{profileId}_{uuid}.{ext}
        String uniqueId = UUID.randomUUID().toString().substring(0, 8); // Short UUID for readability
        String fileName = String.format("avatar_%d_%s.%s", profileId, uniqueId, extension);
        String filePath = PROFILE_IMAGES_DIR + File.separator + fileName;

        try {
            Files.write(Paths.get(filePath), fileData);
            System.out.println("ProfileImageService.saveAvatarImage: Saved to " + filePath);
            // Return database path (forward slashes for consistency)
            return "uploads/profile_images/" + fileName;
        } catch (IOException e) {
            lastErrorMessage = "Failed to write file: " + e.getMessage();
            System.out.println("ProfileImageService.saveAvatarImage error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    private static String normalizePrompt(String prompt) {
        if (prompt == null) {
            return "";
        }
        return prompt.trim().replaceAll("\\s+", " ");
    }

    private static String guessExtensionFromContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "png";
        }
        if (contentType.contains("jpeg") || contentType.contains("jpg")) {
            return "jpg";
        }
        if (contentType.contains("webp")) {
            return "webp";
        }
        if (contentType.contains("gif")) {
            return "gif";
        }
        return "png";
    }

    /**
     * Delete an avatar image file by relative path.
     *
     * @param imagePath Relative path (e.g., "profile_images/avatar_123_abc123.jpg")
     * @return true if deleted successfully or file didn't exist, false on error
     */
    public static boolean deleteAvatarImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return true; // No image to delete
        }

        try {
            File file = new File(imagePath);
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("ProfileImageService.deleteAvatarImage: Deleted " + imagePath);
                    return true;
                } else {
                    System.out.println("ProfileImageService.deleteAvatarImage: Failed to delete " + imagePath);
                    return false;
                }
            }
            return true; // File already doesn't exist
        } catch (Exception e) {
            System.out.println("ProfileImageService.deleteAvatarImage error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if file extension is allowed.
     */
    private static boolean isExtensionAllowed(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        String ext = extension.toLowerCase();
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract file extension from filename.
     */
    private static String extractExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * Get the full file path for an image stored in the database path format.
     * Useful for serving images later.
     *
     * @param databasePath Path stored in database (e.g., "profile_images/avatar_123_abc123.jpg")
     * @return Full file path
     */
    public static String getFullPath(String databasePath) {
        if (databasePath == null || databasePath.isEmpty()) {
            return "";
        }
        return databasePath;
    }
}

