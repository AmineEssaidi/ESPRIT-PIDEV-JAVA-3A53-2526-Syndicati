package com.syndicati.utils.image;

import javafx.scene.image.Image;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for loading images from various sources including the uploads folder.
 * Centralizes image loading logic for profile avatars and other images.
 */
public class ImageLoaderUtil {

    /**
     * Load an image from a relative or absolute path, URL, or resource.
     * Supports:
     * - Relative paths (resolved from project root): uploads/profile_images/avatar.jpg
     * - Absolute paths: C:\path\to\image.jpg
     * - URLs: http://example.com/image.jpg, https://example.com/image.jpg
     * - Resources: /images/default.jpg
     *
     * @param imagePath Path to the image
     * @param async Whether to load asynchronously
     * @return Image object or null if image cannot be loaded
     */
    public static Image loadImage(String imagePath, boolean async) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        try {
            String candidate = imagePath.trim();

            // Check if it's a URL
            if (isUrl(candidate)) {
                return new Image(candidate, async);
            }

            // Try loading from file system (relative or absolute path)
            Path path = Paths.get(candidate);
            if (!path.isAbsolute()) {
                path = Paths.get(System.getProperty("user.dir")).resolve(candidate);
            }

            if (Files.exists(path) && Files.isRegularFile(path)) {
                return new Image(path.toUri().toString(), async);
            }

            // Try loading as resource
            String resourcePath = candidate.startsWith("/") ? candidate : "/" + candidate;
            java.net.URL resource = ImageLoaderUtil.class.getResource(resourcePath);
            if (resource != null) {
                return new Image(resource.toExternalForm(), async);
            }

        } catch (Exception e) {
            System.err.println("ImageLoaderUtil: Failed to load image from " + imagePath + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Load an image synchronously (blocking).
     */
    public static Image loadImage(String imagePath) {
        return loadImage(imagePath, false);
    }

    /**
     * Load a profile avatar image from the uploads/profile_images folder.
     * Handles both existing paths and new uploads folder structure.
     *
     * @param imagePath Path stored in database (e.g., "uploads/profile_images/avatar_123.jpg" or "profile_images/avatar_123.jpg")
     * @param async Whether to load asynchronously
     * @return Image object or null if image cannot be loaded
     */
    public static Image loadProfileAvatar(String imagePath, boolean async) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        String path = imagePath.trim();

        // Handle old paths that don't have 'uploads/' prefix
        if (!path.startsWith("uploads/") && !path.startsWith("uploads\\") && !isUrl(path)) {
            path = "uploads/" + path;
        }

        return loadImage(path, async);
    }

    /**
     * Load a profile avatar image synchronously.
     */
    public static Image loadProfileAvatar(String imagePath) {
        return loadProfileAvatar(imagePath, false);
    }

    /**
     * Check if a path is a valid URL.
     */
    private static boolean isUrl(String value) {
        if (value == null) {
            return false;
        }
        String v = value.toLowerCase();
        return v.startsWith("http://")
            || v.startsWith("https://")
            || v.startsWith("file:")
            || v.startsWith("data:")
            || v.startsWith("jar:");
    }
}

