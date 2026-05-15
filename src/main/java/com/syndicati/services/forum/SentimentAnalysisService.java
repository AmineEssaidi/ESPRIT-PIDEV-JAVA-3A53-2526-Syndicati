package com.syndicati.services.forum;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.syndicati.utils.config.EnvConfig;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Direct Java sentiment analysis for Forum "Feeling".
 *
 * No local Python, Flask, TextBlob, LangChain, or LangGraph worker is required.
 * Gemini is used first, then Groq as fallback when configured.
 */
public class SentimentAnalysisService {

    private static final String GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";
    private static final String GROQ_ENDPOINT =
            "https://api.groq.com/openai/v1/chat/completions";

    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    private final String geminiApiKey;
    private final String geminiModel;
    private final String groqApiKey;
    private final String groqModel;

    public SentimentAnalysisService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .build();
        this.geminiApiKey = EnvConfig.getOrDefault("GEMINI_API_KEY", EnvConfig.getOrDefault("GOOGLE_API_KEY", ""));
        this.geminiModel = EnvConfig.getOrDefault("GEMINI_SENTIMENT_MODEL",
                EnvConfig.getOrDefault("GEMINI_MODEL", "gemini-2.0-flash"));
        this.groqApiKey = EnvConfig.getOrDefault("GROQ_API_KEY", "");
        this.groqModel = EnvConfig.getOrDefault("GROQ_SENTIMENT_MODEL",
                EnvConfig.getOrDefault("GROQ_MODEL", "llama-3.3-70b-versatile"));
    }

    public static void startMicroservice() {
        System.out.println("Sentiment analysis uses direct Gemini/Groq APIs; no local Python service is started.");
    }

    public static void stopMicroservice() {
        // Compatibility no-op: there is no local sentiment process anymore.
    }

    public CompletableFuture<SentimentResult> analyzeContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    new SentimentResult("Neutral", "100", "No content to analyze.", "Neutral", true)
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            Exception geminiError = null;
            if (geminiApiKey != null && !geminiApiKey.isBlank()) {
                try {
                    return callGemini(text);
                } catch (Exception e) {
                    geminiError = e;
                    System.err.println("[SentimentAnalysisService] Gemini failed, trying Groq fallback: " + e.getMessage());
                }
            }

            if (groqApiKey != null && !groqApiKey.isBlank()) {
                try {
                    return callGroq(text);
                } catch (Exception e) {
                    System.err.println("[SentimentAnalysisService] Groq failed: " + e.getMessage());
                    return localHeuristic(text, "API fallback failed: " + e.getMessage());
                }
            }

            if (geminiError != null) {
                return localHeuristic(text, "Gemini unavailable: " + geminiError.getMessage());
            }
            return localHeuristic(text, "No sentiment API key configured.");
        });
    }

    public SentimentResult analyze(String text) {
        try {
            return analyzeContent(text).get();
        } catch (Exception e) {
            return localHeuristic(text, e.getMessage());
        }
    }

    private SentimentResult callGemini(String text) throws Exception {
        JsonObject body = new JsonObject();

        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", systemPrompt());
        systemParts.add(systemPart);
        systemInstruction.add("parts", systemParts);
        body.add("systemInstruction", systemInstruction);

        JsonArray contents = new JsonArray();
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray userParts = new JsonArray();
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", "Analyze this forum text:\n" + text);
        userParts.add(userPart);
        userContent.add("parts", userParts);
        contents.add(userContent);
        body.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.1);
        generationConfig.addProperty("responseMimeType", "application/json");
        generationConfig.add("responseSchema", sentimentResponseSchema());
        body.add("generationConfig", generationConfig);

        String encodedModel = URLEncoder.encode(geminiModel, StandardCharsets.UTF_8).replace("+", "%20");
        String encodedKey = URLEncoder.encode(geminiApiKey, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(GEMINI_ENDPOINT, encodedModel, encodedKey)))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Gemini HTTP " + response.statusCode());
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Gemini returned no candidates.");
        }

        JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
        JsonArray parts = content == null ? null : content.getAsJsonArray("parts");
        if (parts == null || parts.isEmpty()) {
            throw new RuntimeException("Gemini returned no content parts.");
        }

        String jsonText = parts.get(0).getAsJsonObject().get("text").getAsString();
        return parseApiResult(jsonText);
    }

    private SentimentResult callGroq(String text) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("model", groqModel);
        body.addProperty("temperature", 0.1);

        JsonArray messages = new JsonArray();
        messages.add(chatMessage("system", systemPrompt()));
        messages.add(chatMessage("user", "Analyze this forum text:\n" + text));
        body.add("messages", messages);

        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "json_object");
        body.add("response_format", responseFormat);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_ENDPOINT))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + groqApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Groq HTTP " + response.statusCode());
        }

        JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("Groq returned no choices.");
        }

        JsonObject choiceMessage = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        String jsonText = choiceMessage == null || !choiceMessage.has("content")
                ? ""
                : choiceMessage.get("content").getAsString();
        return parseApiResult(jsonText);
    }

    private JsonObject chatMessage(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    private String systemPrompt() {
        return """
            You are a sentiment and emotion classifier for a residential community forum.
            Return ONLY valid JSON. No markdown. No code fences.

            Required JSON shape:
            {
              "sentiment": "Positive|Negative|Neutral",
              "confidence": 0,
              "primaryEmotion": "Joy|Trust|Anticipation|Calm|Neutral|Surprise|Sadness|Anger|Fear|Disgust",
              "explanation": "one short sentence explaining the linguistic signal"
            }

            Rules:
            - confidence must be an integer from 0 to 100.
            - Keep the explanation under 160 characters.
            - Do not add extra keys.
            """;
    }

    private JsonObject sentimentResponseSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "OBJECT");

        JsonObject properties = new JsonObject();
        properties.add("sentiment", typed("STRING"));
        properties.add("confidence", typed("INTEGER"));
        properties.add("primaryEmotion", typed("STRING"));
        properties.add("explanation", typed("STRING"));
        schema.add("properties", properties);

        JsonArray required = new JsonArray();
        required.add("sentiment");
        required.add("confidence");
        required.add("primaryEmotion");
        required.add("explanation");
        schema.add("required", required);

        return schema;
    }

    private JsonObject typed(String type) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type);
        return obj;
    }

    private SentimentResult parseApiResult(String jsonText) {
        if (jsonText == null || jsonText.isBlank()) {
            throw new IllegalArgumentException("Empty sentiment response.");
        }

        String cleaned = jsonText.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }

        JsonObject object = JsonParser.parseString(cleaned).getAsJsonObject();
        String sentiment = normalizeSentiment(getString(object, "sentiment", "Neutral"));
        int confidence = clamp(getInt(object, "confidence", 50), 0, 100);
        String primaryEmotion = normalizeEmotion(getString(object, "primaryEmotion", "Neutral"));
        String explanation = getString(object, "explanation", "Analysis completed.");

        return new SentimentResult(sentiment, String.valueOf(confidence), explanation, primaryEmotion, true);
    }

    private String getString(JsonObject object, String key, String fallback) {
        return object != null && object.has(key) && !object.get(key).isJsonNull()
                ? object.get(key).getAsString()
                : fallback;
    }

    private int getInt(JsonObject object, String key, int fallback) {
        try {
            return object != null && object.has(key) && !object.get(key).isJsonNull()
                    ? object.get(key).getAsInt()
                    : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private String normalizeSentiment(String sentiment) {
        String s = sentiment == null ? "" : sentiment.toLowerCase(Locale.ROOT);
        if (s.contains("pos") || s.contains("happy") || s.contains("joy")) {
            return "Positive";
        }
        if (s.contains("neg") || s.contains("sad") || s.contains("anger") || s.contains("fear")) {
            return "Negative";
        }
        return "Neutral";
    }

    private String normalizeEmotion(String emotion) {
        if (emotion == null || emotion.isBlank()) {
            return "Neutral";
        }
        String e = emotion.trim();
        return e.substring(0, 1).toUpperCase(Locale.ROOT) + e.substring(1).toLowerCase(Locale.ROOT);
    }

    private SentimentResult localHeuristic(String text, String reason) {
        String value = text == null ? "" : text.toLowerCase(Locale.ROOT);
        int positive = score(value, "good", "great", "love", "thanks", "happy", "excellent", "amazing", "nice", "perfect", "helpful");
        int negative = score(value, "bad", "angry", "hate", "sad", "terrible", "awful", "broken", "problem", "issue", "slow", "annoying");

        String sentiment = "Neutral";
        String emotion = "Neutral";
        int confidence = 55;
        if (positive > negative) {
            sentiment = "Positive";
            emotion = "Joy";
            confidence = Math.min(88, 60 + positive * 8);
        } else if (negative > positive) {
            sentiment = "Negative";
            emotion = value.contains("angry") || value.contains("hate") ? "Anger" : "Sadness";
            confidence = Math.min(88, 60 + negative * 8);
        }

        String explanation = reason == null || reason.isBlank()
                ? "Local keyword fallback was used."
                : reason;
        return new SentimentResult(sentiment, String.valueOf(confidence), explanation, emotion, true);
    }

    private int score(String text, String... words) {
        int count = 0;
        for (String word : words) {
            if (text.contains(word)) {
                count++;
            }
        }
        return count;
    }

    public static class SentimentResult {
        public final String sentiment;
        public final String confidence;
        public final String explanation;
        public final String primaryEmotion;
        public final boolean success;

        public SentimentResult(String sentiment, String confidence, String explanation, String primaryEmotion, boolean success) {
            this.sentiment = sentiment;
            this.confidence = confidence;
            this.explanation = explanation;
            this.primaryEmotion = primaryEmotion;
            this.success = success;
        }

        public String getLabel() {
            return sentiment;
        }

        public double getScore() {
            try {
                return Double.parseDouble(confidence) / 100.0;
            } catch (Exception e) {
                return 0.0;
            }
        }
    }
}
