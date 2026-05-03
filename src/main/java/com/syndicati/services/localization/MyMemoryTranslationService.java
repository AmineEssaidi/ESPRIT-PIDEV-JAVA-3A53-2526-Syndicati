package com.syndicati.services.localization;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Service for translating text using MyMemory API.
 * This serves as a reliable fallback or primary service for UI strings.
 */
public class MyMemoryTranslationService {

    private static final String API_URL = "https://api.mymemory.translated.net/get";
    private final OkHttpClient client;

    public MyMemoryTranslationService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Translates English text to a target language.
     * 
     * @param text The English text to translate.
     * @param targetLanguageCode The target language code (e.g., "fr", "ar").
     * @return The translated text, or original text if translation fails.
     */
    public String translate(String text, String targetLanguageCode) {
        if (text == null || text.isBlank() || "en".equalsIgnoreCase(targetLanguageCode)) {
            return text;
        }

        try {
            String langPair = "en|" + targetLanguageCode.toLowerCase();
            String url = API_URL + "?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8) + "&langpair=" + langPair;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONObject responseData = jsonResponse.optJSONObject("responseData");
                    if (responseData != null) {
                        return responseData.getString("translatedText");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("MyMemory Translation Exception: " + e.getMessage());
        }

        return text;
    }
}
