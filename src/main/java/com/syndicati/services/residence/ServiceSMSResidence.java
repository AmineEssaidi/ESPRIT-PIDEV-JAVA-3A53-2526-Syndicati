package com.syndicati.services.residence;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ServiceSMSResidence {

    private static final String BASE_URL = "https://api.textbee.dev/api/v1";
    private static final String API_KEY = "1b234a03-e935-4bad-8aa0-a0a3b74ef9c2";
    private static final String DEVICE_ID = "69e7d938b5cd3ce4c7edbce4";

    public String sendSMS(String recipient, String message) throws Exception {
        String body = String.format(
                "{\"recipients\":[\"%s\"],\"message\":\"%s\"}",
                recipient, message.replace("\"", "\\\"")
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/gateway/devices/" + DEVICE_ID + "/send-sms"))
                .header("Content-Type", "application/json")
                .header("x-api-key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

}
