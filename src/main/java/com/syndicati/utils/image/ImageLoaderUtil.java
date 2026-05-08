package com.syndicati.utils.image;

import javafx.scene.image.Image;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for loading images from various sources including the uploads folder.
 * Centralizes image loading logic with async loading and in-memory cache.
 */
public class ImageLoaderUtil {

    private static final int MAX_CACHED_IMAGES = 24;
    private static final Object CACHE_LOCK = new Object();
    private static final Map<String, Image> imageCache = new LinkedHashMap<String, Image>(16, 0.75f, true) {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_CACHED_IMAGES;
        }
    };

    /**
     * Load an image from a relative or absolute path, URL, or resource.
     * Supports:
     * - Relative paths (resolved from project root): uploads/profile_images/avatar.jpg
     * - Absolute paths: C:\path\to\image.jpg
     * - URLs: http://example.com/image.jpg, https://example.com/image.jpg
     * - Resources: /images/default.jpg
     *
     * Uses in-memory cache to avoid reloading the same image multiple times.
     *
     * @param imagePath Path to the image
     * @param async Whether to load asynchronously (recommended: true for UI thread safety)
     * @return Image object or null if image cannot be loaded
     */
    public static Image loadImage(String imagePath, boolean async) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        
        // Check cache first (thread-safe; LinkedHashMap is not concurrent).
        synchronized (CACHE_LOCK) {
            Image cached = imageCache.get(imagePath);
            if (cached != null) return cached;
        }

        try {
            String candidate = imagePath.trim();
            Image image = null;

            // Check if it's a URL
            if (isUrl(candidate)) {
                // Do NOT cache remote URLs by default: they are often large and cause RAM growth.
                // Callers that need caching should cache thumbnails explicitly.
                return new Image(candidate, async);
            } else {
                // Try loading from file system (relative or absolute path)
                Path path = Paths.get(candidate);
                if (!path.isAbsolute()) {
                    path = Paths.get(System.getProperty("user.dir")).resolve(candidate);
                }

                if (Files.exists(path) && Files.isRegularFile(path)) {
                    image = new Image(path.toUri().toString(), async);
                } else {
                    // Try loading as resource
                    String resourcePath = candidate.startsWith("/") ? candidate : "/" + candidate;
                    java.net.URL resource = ImageLoaderUtil.class.getResource(resourcePath);
                    if (resource != null) {
                        image = new Image(resource.toExternalForm(), async);
                    }
                }
            }
            
            if (image != null && !image.isError()) {
                synchronized (CACHE_LOCK) {
                    imageCache.put(imagePath, image);
                }
                return image;
            }

        } catch (Exception e) {
            System.err.println("ImageLoaderUtil: Failed to load image from " + imagePath + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Load an image asynchronously (non-blocking, recommended for UI).
     */
    public static Image loadImage(String imagePath) {
        return loadImage(imagePath, true);
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

        // If we stored an ImageKit URL in DB, try loading it first.
        // If ImageKit is down, fall back to local uploads/profile_images/<filename>.
        if (isUrl(path)) {
            Image urlImg = loadImage(path, async);
            if (urlImg != null) {
                return urlImg;
            }

            String filename = extractFilenameFromUrl(path);
            if (filename != null && !filename.isBlank()) {
                Image localImg = loadImage("uploads/profile_images/" + filename, async);
                if (localImg != null) return localImg;
                localImg = loadImage("profile_images/" + filename, async);
                if (localImg != null) return localImg;
            }

            return null;
        }

        // Handle old paths that don't have 'uploads/' prefix
        if (!path.startsWith("uploads/") && !path.startsWith("uploads\\") && !isUrl(path)) {
            path = "uploads/" + path;
        }

        return loadImage(path, async);
    }

    /**
     * Load a profile avatar image asynchronously (non-blocking).
     */
    public static Image loadProfileAvatar(String imagePath) {
        return loadProfileAvatar(imagePath, true);
    }
    
    /**
     * Clear the image cache (call sparingly, e.g., on logout).
     */
    public static void clearCache() {
        synchronized (CACHE_LOCK) {
            imageCache.clear();
        }
    }

    /**
     * Trim cache to at most max entries (best-effort).
     */
    public static void trimCache(int max) {
        int target = Math.max(0, max);
        synchronized (CACHE_LOCK) {
            while (imageCache.size() > target) {
                // Remove eldest (iteration order is access-order).
                String eldestKey = imageCache.keySet().iterator().next();
                imageCache.remove(eldestKey);
            }
        }
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

    private static String extractFilenameFromUrl(String url) {
        if (url == null || url.isBlank()) return null;
        String u = url.trim();
        int q = u.indexOf('?');
        if (q >= 0) {
            u = u.substring(0, q);
        }
        int lastSlash = u.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < u.length() - 1) {
            return u.substring(lastSlash + 1);
        }
        return u;
    }
}

