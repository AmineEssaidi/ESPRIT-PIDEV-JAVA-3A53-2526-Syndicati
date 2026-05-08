package com.syndicati.utils.media;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Lightweight on-disk cache for the startup cinematic MP4.
 *
 * Streaming MP4 over HTTPS can stutter on some JavaFX/Windows setups during startup.
 * Caching once to disk makes playback much smoother for subsequent launches.
 */
public final class CinematicVideoCache {
    private CinematicVideoCache() {}

    public static final String REMOTE_STARTUP_VIDEO_URL =
        "https://ik.imagekit.io/b0dxqylai/syndicati.mp4?updatedAt=1778146299878";

    private static volatile boolean warmupStarted = false;

    public static String getStartupVideoSource() {
        Path p = cachePath();
        if (p != null && Files.exists(p)) {
            return p.toUri().toString();
        }
        return REMOTE_STARTUP_VIDEO_URL;
    }

    public static void warmupAsync() {
        if (warmupStarted) return;
        warmupStarted = true;

        Thread.startVirtualThread(() -> {
            Path cache = cachePath();
            if (cache == null) return;
            if (Files.exists(cache)) return;

            try {
                Files.createDirectories(cache.getParent());
            } catch (IOException ignored) {
                return;
            }

            Path tmp = cache.resolveSibling(cache.getFileName().toString() + ".part");
            try {
                URL url = new URL(REMOTE_STARTUP_VIDEO_URL);
                try (InputStream in = url.openStream()) {
                    Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
                }
                Files.move(tmp, cache, StandardCopyOption.REPLACE_EXISTING);
            } catch (Throwable e) {
                try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
            }
        });
    }

    private static Path cachePath() {
        try {
            String base = System.getProperty("user.dir");
            if (base == null || base.isBlank()) return null;
            return Path.of(base, "cache", "startup-cinematic.mp4");
        } catch (Exception e) {
            return null;
        }
    }
}

