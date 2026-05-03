package com.syndicati.services.security;

import com.syndicati.utils.config.EnvConfig;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.logging.Logger;

public class GoogleOAuthService {
    private static final Logger LOGGER = Logger.getLogger(GoogleOAuthService.class.getName());

    /** Ports to try in order. If 8888 is stuck, we fall back to the next one. */
    private static final int[] CANDIDATE_PORTS = {8888, 8889, 8890, 8891, 8892};

    /**
     * Static reference to the currently running callback server.
     * Shared across all instances so clicking the button twice always stops the first server.
     */
    private static com.sun.net.httpserver.HttpServer activeServer = null;
    private static int activePort = -1;

    private final String clientId;
    private final String clientSecret;
    private final OkHttpClient httpClient;

    public GoogleOAuthService() {
        this.clientId = EnvConfig.get("GOOGLE_OAUTH_CLIENT_ID");
        this.clientSecret = EnvConfig.get("GOOGLE_OAUTH_CLIENT_SECRET");
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build();

        if (this.clientId == null || this.clientId.isEmpty()) {
            LOGGER.warning("GOOGLE_OAUTH_CLIENT_ID is not configured in .env");
        }
    }

    /**
     * Stops any currently running OAuth callback server.
     * Safe to call even if no server is running.
     */
    public static synchronized void stopActiveServer() {
        if (activeServer != null) {
            try {
                activeServer.stop(0);
                LOGGER.info("Stopped previous OAuth callback server on port " + activePort);
            } catch (Exception e) {
                LOGGER.warning("Error stopping previous server: " + e.getMessage());
            }
            activeServer = null;
            activePort = -1;
        }
    }

    /**
     * Generates the Google OAuth authorization URL.
     */
    public String getAuthorizationUrl(String redirectUri) {
        String encodedUri = redirectUri;
        try {
            encodedUri = java.net.URLEncoder.encode(redirectUri, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {}

        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + clientId +
                "&redirect_uri=" + encodedUri +
                "&response_type=code" +
                "&scope=email%20profile" +
                "&access_type=online" +
                "&prompt=consent";
    }

    /**
     * Exchanges the authorization code for an access token and fetches user info.
     * Must be called from a background thread — makes network calls.
     */
    public GoogleUserInfo exchangeCodeAndGetUserInfo(String code, String redirectUri) throws Exception {
        LOGGER.info("Exchanging Google OAuth code for token...");

        RequestBody formBody = new FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("code", code)
                .add("redirect_uri", redirectUri)
                .add("grant_type", "authorization_code")
                .build();

        Request tokenRequest = new Request.Builder()
                .url("https://oauth2.googleapis.com/token")
                .post(formBody)
                .build();

        String accessToken;
        try (Response response = httpClient.newCall(tokenRequest).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                String error = response.body() != null ? response.body().string() : "No response body";
                LOGGER.severe("Token exchange failed: " + response.code() + " - " + error);
                throw new Exception("Failed to exchange code for token: " + response.code());
            }
            JSONObject json = new JSONObject(response.body().string());
            accessToken = json.getString("access_token");
        }

        LOGGER.info("Token obtained. Fetching user info...");

        Request userInfoRequest = new Request.Builder()
                .url("https://www.googleapis.com/oauth2/v2/userinfo")
                .header("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        try (Response response = httpClient.newCall(userInfoRequest).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new Exception("Failed to fetch user info: " + response.code());
            }
            JSONObject json = new JSONObject(response.body().string());

            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.setId(json.optString("id"));
            userInfo.setEmail(json.optString("email"));
            userInfo.setFirstName(json.optString("given_name", ""));
            userInfo.setLastName(json.optString("family_name", ""));
            userInfo.setPicture(json.optString("picture", ""));

            LOGGER.info("User info fetched successfully for: " + userInfo.getEmail());
            return userInfo;
        }
    }

    /**
     * Starts a local HTTP server to listen for the Google OAuth callback.
     * Automatically stops any previously running server first.
     * Tries ports 8888-8892 in sequence if earlier ports are taken.
     *
     * @param onCodeReceived Callback invoked with the authorization code when it arrives.
     * @return The redirect URI that was successfully bound (e.g. http://localhost:8888/callback).
     */
    public synchronized String startCallbackServer(java.util.function.Consumer<String> onCodeReceived) throws IOException {
        // Always stop any previous server first — handles double-click and crashed auth flows.
        stopActiveServer();

        com.sun.net.httpserver.HttpServer server = null;
        int boundPort = -1;

        for (int port : CANDIDATE_PORTS) {
            try {
                server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(port), 0);
                boundPort = port;
                LOGGER.info("OAuth callback server bound to port " + port);
                break;
            } catch (java.net.BindException e) {
                LOGGER.warning("Port " + port + " busy, trying next...");
            }
        }

        if (server == null) {
            throw new IOException("All OAuth callback ports are in use. Please restart the application.");
        }

        // Store as active so subsequent clicks can stop it cleanly.
        activeServer = server;
        activePort = boundPort;

        final String redirectUri = "http://localhost:" + boundPort + "/callback";
        final com.sun.net.httpserver.HttpServer finalServer = server;

        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String code = null;
            if (query != null && query.contains("code=")) {
                for (String param : query.split("&")) {
                    if (param.startsWith("code=")) {
                        code = param.substring(5);
                        break;
                    }
                }
            }

            String responseHtml;
            if (code != null) {
                responseHtml = "<html><head><title>Syndicati</title></head><body style='font-family:sans-serif;text-align:center;padding-top:80px'>" +
                    "<h2 style='color:#22c55e'>&#10003; Authentication successful!</h2>" +
                    "<p>You can close this window and return to Syndicati.</p>" +
                    "<script>setTimeout(window.close,2500);</script></body></html>";
                exchange.sendResponseHeaders(200, responseHtml.getBytes().length);
            } else {
                responseHtml = "<html><body><h2>Authentication failed — no code received.</h2></body></html>";
                exchange.sendResponseHeaders(400, responseHtml.getBytes().length);
            }

            try (java.io.OutputStream os = exchange.getResponseBody()) {
                os.write(responseHtml.getBytes());
            }

            if (code != null) {
                final String finalCode = code;
                // Wait briefly so the browser receives the response, then shut down.
                Thread.startVirtualThread(() -> {
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException ignored) {}
                    stopActiveServer();
                    onCodeReceived.accept(finalCode);
                });
            }
        });

        server.setExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        LOGGER.info("OAuth callback server listening on " + redirectUri);
        return redirectUri;
    }

    // ── Shutdown hook — cleans up if app is killed mid-auth ───────────────────
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(GoogleOAuthService::stopActiveServer));
    }

    // ── User info model ────────────────────────────────────────────────────────
    public static class GoogleUserInfo {
        private String id, email, firstName, lastName, picture;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPicture() { return picture; }
        public void setPicture(String picture) { this.picture = picture; }
    }
}
