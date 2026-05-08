package com.syndicati.services.residence;

import com.syndicati.utils.config.EnvConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Service for sending SMS via TextBee (as found in the reference project).
 */
public class SmsService {

    private static final String BASE_URL = "https://api.textbee.dev/api/v1";
    private final String apiKey;
    private final String deviceId;
    private final HttpClient httpClient;

    public SmsService() {
        this.apiKey = EnvConfig.get("TEXTBEE_API_KEY");
        this.deviceId = EnvConfig.get("TEXTBEE_DEVICE_ID");
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Sends an SMS asynchronously.
     *
     * @param recipient The phone number of the recipient.
     * @param message   The message content.
     * @return A CompletableFuture with the API response body.
     */
    public CompletableFuture<String> sendSmsAsync(String recipient, String message) {
        return CompletableFuture.supplyAsync(() -> {
            if (apiKey == null || deviceId == null) {
                return "Error: TextBee credentials not configured in .env";
            }

            try {
                // Escape quotes in message for JSON
                String escapedMsg = message.replace("\"", "\\\"");
                
                String body = String.format(
                    "{\"recipients\":[\"%s\"],\"message\":\"%s\"}",
                    recipient, escapedMsg
                );

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/gateway/devices/" + deviceId + "/send-sms"))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            } catch (Exception e) {
                System.err.println("SMS sending failed: " + e.getMessage());
                return "Error: " + e.getMessage();
            }
        });
    }
}
