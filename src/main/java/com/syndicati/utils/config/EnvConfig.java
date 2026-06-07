package com.syndicati.utils.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Native Java config loader with precedence:
 * 1) JVM system properties (-DKEY=value)
 * 2) OS environment variables
 * 3) local properties/dotenv files for development compatibility
 * 4) Infisical machine-identity secrets as the shared project fallback
 */
public final class EnvConfig {

    private static final Duration INFISICAL_TIMEOUT = Duration.ofSeconds(8);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"(?:accessToken|token)\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern SECRET_OBJECT_PATTERN = Pattern.compile(
            "\\{[^{}]*\"secretKey\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"[^{}]*\"secretValue\"\\s*:\\s*(null|\"(?:\\\\.|[^\"])*\")",
            Pattern.DOTALL
    );
    private static final Map<String, String> LOADED = load();

    private EnvConfig() {
    }

    public static String get(String key) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String real = System.getenv(key);
        if (real != null && !real.isBlank()) {
            return real;
        }
        return LOADED.get(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static void bootstrapSystemProperties() {
        for (Map.Entry<String, String> entry : LOADED.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || key.isBlank() || value == null || value.isBlank()) {
                continue;
            }

            String existing = System.getProperty(key);
            if (existing == null || existing.isBlank()) {
                System.setProperty(key, value);
            }
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return "1".equals(value) || "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
    }

    private static Map<String, String> load() {
        Map<String, String> env = new HashMap<>();

        loadPropertiesFile(Path.of("config", "application.properties"), env);
        loadPropertiesFile(Path.of("config", "application.local.properties"), env);

        loadDotenvFile(Path.of(".env"), env);
        loadDotenvFile(Path.of(".env.local"), env);

        String appEnv = env.getOrDefault("APP_ENV", "dev");
        loadPropertiesFile(Path.of("config", "application." + appEnv + ".properties"), env);
        loadPropertiesFile(Path.of("config", "application." + appEnv + ".local.properties"), env);
        loadDotenvFile(Path.of(".env." + appEnv), env);
        loadDotenvFile(Path.of(".env." + appEnv + ".local"), env);

        loadInfisicalSecrets(env);

        return env;
    }

    private static void loadInfisicalSecrets(Map<String, String> env) {
        if ("0".equals(value(env, "INFISICAL_BOOTSTRAP")) || getDirectEnv("SYNDICATI_INFISICAL_DISABLED") != null) {
            return;
        }

        String clientId = value(env, "INFISICAL_UNIVERSAL_AUTH_CLIENT_ID");
        String clientSecret = value(env, "INFISICAL_UNIVERSAL_AUTH_CLIENT_SECRET");
        String projectId = value(env, "INFISICAL_PROJECT_ID");
        if (isBlank(clientId) || isBlank(clientSecret) || isBlank(projectId)) {
            debugInfisical("Infisical machine identity is not configured");
            return;
        }

        String host = trimTrailingSlash(defaultIfBlank(value(env, "INFISICAL_HOST"), "https://app.infisical.com"));
        String environment = normalizeEnvironment(defaultIfBlank(value(env, "INFISICAL_ENV"), "prod"));
        String secretPath = defaultIfBlank(value(env, "INFISICAL_SECRET_PATH"), "/");

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(INFISICAL_TIMEOUT)
                    .build();

            String token = login(client, host, clientId, clientSecret);
            if (isBlank(token)) {
                debugInfisical("Infisical login returned no token");
                return;
            }

            Map<String, String> secrets = fetchSecrets(client, host, token, projectId, environment, secretPath, "workspaceId");
            if (secrets.isEmpty()) {
                secrets = fetchSecrets(client, host, token, projectId, environment, secretPath, "projectId");
            }

            int merged = 0;
            for (Map.Entry<String, String> entry : secrets.entrySet()) {
                String key = entry.getKey();
                String secretValue = entry.getValue();
                if (!isBlank(key) && !isBlank(secretValue) && isBlank(env.get(key))) {
                    env.put(key, secretValue);
                    merged++;
                }
            }
            debugInfisical("Infisical secrets loaded: " + merged);
        } catch (Exception ex) {
            debugInfisical("Infisical secrets unavailable: " + ex.getClass().getSimpleName()
                    + (ex.getMessage() == null ? "" : " - " + ex.getMessage()));
        }
    }

    private static String login(HttpClient client, String host, String clientId, String clientSecret)
            throws IOException, InterruptedException {
        String body = "{\"clientId\":\"" + json(clientId) + "\",\"clientSecret\":\"" + json(clientSecret) + "\"}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(host + "/api/v1/auth/universal-auth/login"))
                .timeout(INFISICAL_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            debugInfisical("Infisical login failed with HTTP " + response.statusCode());
            return null;
        }

        Matcher matcher = TOKEN_PATTERN.matcher(response.body());
        return matcher.find() ? unescapeJson(matcher.group(1)) : null;
    }

    private static Map<String, String> fetchSecrets(
            HttpClient client,
            String host,
            String token,
            String projectId,
            String environment,
            String secretPath,
            String projectParam
    ) throws IOException, InterruptedException {
        String url = host + "/api/v3/secrets/raw"
                + "?environment=" + url(environment)
                + "&secretPath=" + url(secretPath)
                + "&include_imports=true"
                + "&expandSecretReferences=true"
                + "&" + projectParam + "=" + url(projectId);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(INFISICAL_TIMEOUT)
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            debugInfisical("Infisical secret fetch failed with HTTP " + response.statusCode() + " using " + projectParam);
            return Map.of();
        }

        return parseSecrets(response.body());
    }

    private static Map<String, String> parseSecrets(String body) {
        Map<String, String> secrets = new HashMap<>();
        Matcher matcher = SECRET_OBJECT_PATTERN.matcher(body == null ? "" : body);
        while (matcher.find()) {
            String key = unescapeJson(matcher.group(1));
            String rawValue = matcher.group(2);
            if (!"null".equals(rawValue)) {
                String secretValue = rawValue.substring(1, rawValue.length() - 1);
                secrets.put(key, unescapeJson(secretValue));
            }
        }
        return secrets;
    }

    private static String value(Map<String, String> env, String key) {
        String systemProperty = System.getProperty(key);
        if (!isBlank(systemProperty)) {
            return systemProperty;
        }

        String real = System.getenv(key);
        if (!isBlank(real)) {
            return real;
        }

        return env.get(key);
    }

    private static String getDirectEnv(String key) {
        String value = System.getenv(key);
        return isBlank(value) ? null : value;
    }

    private static void debugInfisical(String message) {
        if (getDirectEnv("SYNDICATI_INFISICAL_DEBUG") != null || getDirectEnv("INFISICAL_DEBUG") != null) {
            System.out.println("[EnvConfig] " + message);
        }
    }

    private static void loadPropertiesFile(Path path, Map<String, String> env) {
        if (!Files.exists(path)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException ignored) {
            return;
        }

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (!isBlank(value)) {
                env.put(key, value.trim());
            }
        }
    }

    private static void loadDotenvFile(Path path, Map<String, String> env) {
        if (!Files.exists(path)) {
            return;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            return;
        }

        for (String rawLine : lines) {
            String line = rawLine == null ? "" : rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.startsWith("export ")) {
                line = line.substring("export ".length()).trim();
            }

            int idx = line.indexOf('=');
            if (idx <= 0) {
                continue;
            }

            String key = line.substring(0, idx).trim();
            String envValue = stripQuotes(line.substring(idx + 1).trim());
            if (!envValue.isBlank()) {
                env.put(key, envValue);
            }
        }
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    private static String normalizeEnvironment(String environment) {
        if ("production".equalsIgnoreCase(environment)) {
            return "prod";
        }
        if ("development".equalsIgnoreCase(environment)) {
            return "dev";
        }
        return environment;
    }

    private static String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String defaultIfBlank(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String json(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String value) {
        StringBuilder out = new StringBuilder(value.length());
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (escaping) {
                switch (ch) {
                    case '"' -> out.append('"');
                    case '\\' -> out.append('\\');
                    case '/' -> out.append('/');
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    default -> out.append(ch);
                }
                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            } else {
                out.append(ch);
            }
        }
        if (escaping) {
            out.append('\\');
        }
        return out.toString();
    }
}
