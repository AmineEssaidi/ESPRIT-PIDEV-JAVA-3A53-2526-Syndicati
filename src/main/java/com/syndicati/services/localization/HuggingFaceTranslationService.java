package com.syndicati.services.localization;

import com.syndicati.utils.config.EnvConfig;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Service for translating text using Hugging Face Inference API (MarianMT models).
 */
public class HuggingFaceTranslationService {

    private static final String API_URL_BASE = "https://api-inference.huggingface.co/models/Helsinki-NLP/opus-mt-en-";
    private final String apiKey;
    private final OkHttpClient client;

    public HuggingFaceTranslationService() {
        this.apiKey = EnvConfig.get("HUGGING_FACE_API_KEY");
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
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

        String modelId = "Helsinki-NLP/opus-mt-en-" + targetLanguageCode.toLowerCase();
        String url = "https://api-inference.huggingface.co/models/" + modelId;

        JSONObject payload = new JSONObject();
        payload.put("inputs", text);
        
        // Options to wait for the model to load if it's in a cold state
        JSONObject options = new JSONObject();
        options.put("wait_for_model", true);
        payload.put("options", options);

        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JSONArray jsonResponse = new JSONArray(responseBody);
                if (!jsonResponse.isEmpty()) {
                    return jsonResponse.getJSONObject(0).getString("translation_text");
                }
            } else {
                System.err.println("HF Translation Error (" + targetLanguageCode + "): " + response.code() + " - " + response.message());
                if (response.body() != null) {
                    System.err.println("Response: " + response.body().string());
                }
            }
        } catch (IOException e) {
            System.err.println("HF Translation Exception: " + e.getMessage());
        }

        return text; // Fallback to original text
    }
}
