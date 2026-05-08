package com.syndicati.utils.image.imagekit;

import com.syndicati.utils.config.EnvConfig;

/**
 * ImageKit configuration loaded from environment variables.
 *
 * Required for server-side uploads:
 * - IMAGEKIT_PRIVATE_KEY (value like "private_....=")
 *
 * Optional:
 * - IMAGEKIT_PUBLIC_KEY
 * - IMAGEKIT_URL_ENDPOINT (e.g. https://ik.imagekit.io/<id>)
 * - IMAGEKIT_ENABLED=true/false (default: true if private key is present)
 */
public final class ImageKitConfig {

    private final String privateKey;
    private final String publicKey;
    private final String urlEndpoint;
    private final boolean enabled;

    private ImageKitConfig(String privateKey, String publicKey, String urlEndpoint, boolean enabled) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.urlEndpoint = urlEndpoint;
        this.enabled = enabled;
    }

    public static ImageKitConfig fromEnv() {
        // Prefer our EnvConfig loader so local .env/.env.local values work.
        String privateKey = trimToNull(EnvConfig.get("IMAGEKIT_PRIVATE_KEY"));
        String publicKey = trimToNull(EnvConfig.get("IMAGEKIT_PUBLIC_KEY"));
        String urlEndpoint = trimToNull(EnvConfig.get("IMAGEKIT_URL_ENDPOINT"));

        String enabledRaw = trimToNull(EnvConfig.get("IMAGEKIT_ENABLED"));
        boolean enabled;
        if (enabledRaw != null) {
            enabled = Boolean.parseBoolean(enabledRaw);
        } else {
            enabled = privateKey != null;
        }

        return new ImageKitConfig(privateKey, publicKey, urlEndpoint, enabled);
    }

    private static String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getUrlEndpoint() {
        return urlEndpoint;
    }

    public boolean isEnabled() {
        return enabled;
    }
}

