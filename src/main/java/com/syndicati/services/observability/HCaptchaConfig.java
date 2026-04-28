package com.syndicati.services.observability;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Configuration for hCaptcha integration.
 * Uses the same key pair as the Horizon web project.
 */
public class HCaptchaConfig {

    private static final String DEFAULT_SITE_KEY = "";
    private static final String DEFAULT_SECRET_KEY = "";

    private boolean enabled;
    private String siteKey;
    private String secretKey;

    public HCaptchaConfig() {
        loadFromProperties();
    }

    private void loadFromProperties() {
        Properties props = new Properties();
        Properties dotenvProps = new Properties();

        try (InputStream is = getClass().getResourceAsStream("/application.local.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            System.out.println("[HCaptchaConfig] Could not load classpath properties: " + e.getMessage());
        }

        File fsProps = new File("config/application.local.properties");
        if (fsProps.exists()) {
            try (InputStream is = new FileInputStream(fsProps)) {
                props.load(is);
            } catch (IOException e) {
                System.out.println("[HCaptchaConfig] Could not load filesystem properties: " + e.getMessage());
            }
        }

        loadDotenvFile(dotenvProps, new File(".env.local"));
        loadDotenvFile(dotenvProps, new File(".env"));

        siteKey = getStringProperty(props, dotenvProps, "hcaptcha.site_key", DEFAULT_SITE_KEY);
        secretKey = getStringProperty(props, dotenvProps, "hcaptcha.secret_key", DEFAULT_SECRET_KEY);
        enabled = !siteKey.isBlank() && !secretKey.isBlank();

        if (enabled) {
            System.err.println("[HCaptchaConfig] hCaptcha enabled. Site key present: yes");
        }
    }

    private void loadDotenvFile(Properties target, File file) {
        if (file == null || !file.exists()) {
            return;
        }

        try {
            for (String rawLine : Files.readAllLines(Path.of(file.getAbsolutePath()), StandardCharsets.UTF_8)) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("###")) {
                    continue;
                }

                if (line.startsWith("export ")) {
                    line = line.substring(7).trim();
                }

                int equalsIndex = line.indexOf('=');
                if (equalsIndex <= 0) {
                    continue;
                }

                String key = line.substring(0, equalsIndex).trim();
                String value = line.substring(equalsIndex + 1).trim();

                if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                if (!key.isBlank() && !value.isBlank()) {
                    target.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            System.out.println("[HCaptchaConfig] Could not load dotenv file " + file.getName() + ": " + e.getMessage());
        }
    }

    private String getStringProperty(Properties props, Properties dotenvProps, String key, String defaultValue) {
        String env = System.getenv(key.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) {
            return env;
        }

        String prop = props.getProperty(key);
        if (prop == null || prop.isBlank()) {
            // Also try uppercase-underscore format in properties file
            prop = props.getProperty(key.toUpperCase().replace('.', '_'));
        }
        
        if (prop != null && !prop.isBlank()) {
            return prop;
        }

        String envStyleKey = key.toUpperCase().replace('.', '_');
        String dotenvValue = dotenvProps.getProperty(envStyleKey);
        if (dotenvValue != null && !dotenvValue.isBlank()) {
            return dotenvValue;
        }

        String directDotenvValue = dotenvProps.getProperty(key);
        if (directDotenvValue != null && !directDotenvValue.isBlank()) {
            return directDotenvValue;
        }

        return defaultValue;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
