package com.syndicati.services.security;

import com.syndicati.utils.config.EnvConfig;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.logging.Logger;

public class GoogleOAuthService {
    private static final Logger LOGGER = Logger.getLogger(GoogleOAuthService.class.getName());
    
    private final String clientId;
    private final String clientSecret;
    private final OkHttpClient httpClient;

    public GoogleOAuthService() {
        this.clientId = EnvConfig.get("GOOGLE_OAUTH_CLIENT_ID");
        this.clientSecret = EnvConfig.get("GOOGLE_OAUTH_CLIENT_SECRET");
        this.httpClient = new OkHttpClient();
        
        if (this.clientId == null || this.clientId.isEmpty()) {
            LOGGER.warning("GOOGLE_OAUTH_CLIENT_ID is not configured in .env");
        }
    }

    /**
     * Generates the Google OAuth authorization URL.
     * @param redirectUri The local redirect URI (e.g., http://localhost:12345/callback)
     * @return The authorization URL to open in the browser
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
     * @param code The authorization code received in the callback
     * @param redirectUri The redirect URI used in the initial request
     * @return A GoogleUserInfo object containing user details
     * @throws Exception If token exchange or user info fetch fails
     */
    public GoogleUserInfo exchangeCodeAndGetUserInfo(String code, String redirectUri) throws Exception {
        LOGGER.info("Exchanging Google OAuth code for token...");
        
        // 1. Exchange code for token
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
        
        // 2. Fetch user info
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

    public static class GoogleUserInfo {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String picture;

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

    /**
     * Starts a local HTTP server to listen for the Google OAuth callback.
     * @param onCodeReceived Callback invoked when the authorization code is received.
     * @return The redirect URI to use for the OAuth request.
     */
    public String startCallbackServer(java.util.function.Consumer<String> onCodeReceived) throws IOException {
        com.sun.net.httpserver.HttpServer server = null;
        int port = 8888; // Fixed port so it can be added to Google Console
        try {
            server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(port), 0);
        } catch (java.net.BindException e) {
            LOGGER.severe("Port " + port + " is already in use. Please close any applications using this port.");
            throw e;
        }
        
        String redirectUri = "http://localhost:" + port + "/callback";

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

            String responseMessage;
            if (code != null) {
                responseMessage = "<html><body><h2>Authentication successful!</h2><p>You can close this window and return to Syndicati.</p><script>setTimeout(window.close, 3000);</script></body></html>";
                exchange.sendResponseHeaders(200, responseMessage.length());
            } else {
                responseMessage = "<html><body><h2>Authentication failed!</h2><p>No authorization code found.</p></body></html>";
                exchange.sendResponseHeaders(400, responseMessage.length());
            }
            
            java.io.OutputStream os = exchange.getResponseBody();
            os.write(responseMessage.getBytes());
            os.close();

            if (code != null) {
                final String finalCode = code;
                // Run on a separate thread to allow server to send response before stopping
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        finalServer.stop(0);
                        onCodeReceived.accept(finalCode);
                    } catch (Exception e) {
                        LOGGER.severe("Error handling callback: " + e.getMessage());
                    }
                }).start();
            }
        });

        server.setExecutor(null);
        server.start();
        LOGGER.info("OAuth Callback server listening on " + redirectUri);
        return redirectUri;
    }
}
