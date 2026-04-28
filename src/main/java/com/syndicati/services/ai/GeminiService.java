package com.syndicati.services.ai;

import com.syndicati.utils.config.EnvConfig;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GeminiService {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private final String apiKey;
    private final OkHttpClient client;

    public GeminiService() {
        this.apiKey = EnvConfig.get("GEMINI_API_KEY");
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String generateContent(String prompt) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("Gemini API Key is missing in .env.local");
        }

        JSONObject json = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject parts = new JSONObject();
        parts.put("text", prompt);
        JSONArray partsArray = new JSONArray();
        partsArray.put(parts);
        JSONObject contentObj = new JSONObject();
        contentObj.put("parts", partsArray);
        contents.put(contentObj);
        json.put("contents", contents);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL + "?key=" + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Gemini API Error: " + response.code() + " " + response.body().string());
            }
            
            JSONObject responseJson = new JSONObject(response.body().string());
            return responseJson.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
        }
    }
}
