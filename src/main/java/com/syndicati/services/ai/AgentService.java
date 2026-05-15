package com.syndicati.services.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.concurrent.CompletableFuture;

/**
 * AgentService
 * Direct Java implementation of the Syndicati assistant.
 *
 * No local Python, LangChain, LangGraph, FastAPI, or Playwright worker is started.
 * Gemini is used first. Groq is used as a fallback when configured.
 */
public class AgentService {

    private static AgentService instance;

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

    private AgentService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .build();
        this.geminiApiKey = EnvConfig.getOrDefault("GEMINI_API_KEY", EnvConfig.getOrDefault("GOOGLE_API_KEY", ""));
        this.geminiModel = EnvConfig.getOrDefault("GEMINI_MODEL", "gemini-2.0-flash");
        this.groqApiKey = EnvConfig.getOrDefault("GROQ_API_KEY", "");
        this.groqModel = EnvConfig.getOrDefault("GROQ_MODEL", "llama-3.3-70b-versatile");
    }

    public static synchronized AgentService getInstance() {
        if (instance == null) {
            instance = new AgentService();
        }
        return instance;
    }

    public static synchronized void shutdown() {
        instance = null;
    }

    public void stopPythonWorker() {
        // Compatibility no-op: the agent no longer starts a local Python worker.
    }

    public CompletableFuture<String> chatAsync(String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject result = runAgent(message, null, "chat");
                return cleanReply(result);
            } catch (Exception e) {
                e.printStackTrace();
                return "Agent Error: " + e.getMessage();
            }
        });
    }

    public CompletableFuture<String> takeoverAsync(String userRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject uiState = AppInspector.inspectCurrentScene();
                JsonObject result = runAgent(userRequest, uiState, "takeover");
                executeActions(result);
                return cleanReply(result);
            } catch (Exception e) {
                e.printStackTrace();
                return "Takeover Error: " + e.getMessage();
            }
        });
    }

    private JsonObject runAgent(String message, JsonObject uiState, String mode) throws Exception {
        Exception geminiError = null;
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                return callGemini(message, uiState, mode);
            } catch (Exception e) {
                geminiError = e;
                System.err.println("[AgentService] Gemini failed, trying Groq fallback: " + e.getMessage());
            }
        }

        if (groqApiKey != null && !groqApiKey.isBlank()) {
            return callGroq(message, uiState, mode);
        }

        if (geminiError != null) {
            throw geminiError;
        }
        throw new IllegalStateException("No agent API key configured. Set GEMINI_API_KEY or GROQ_API_KEY.");
    }

    private JsonObject callGemini(String message, JsonObject uiState, String mode) throws Exception {
        JsonObject body = new JsonObject();

        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", systemPrompt(mode));
        systemParts.add(systemPart);
        systemInstruction.add("parts", systemParts);
        body.add("systemInstruction", systemInstruction);

        JsonArray contents = new JsonArray();
        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray userParts = new JsonArray();
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", userPrompt(message, uiState, mode));
        userParts.add(userPart);
        userContent.add("parts", userParts);
        contents.add(userContent);
        body.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.2);
        generationConfig.addProperty("responseMimeType", "application/json");
        generationConfig.add("responseSchema", actionResponseSchema());
        body.add("generationConfig", generationConfig);

        String encodedModel = URLEncoder.encode(geminiModel, StandardCharsets.UTF_8).replace("+", "%20");
        String encodedKey = URLEncoder.encode(geminiApiKey, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(GEMINI_ENDPOINT, encodedModel, encodedKey)))
                .timeout(Duration.ofSeconds(70))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Gemini HTTP " + response.statusCode() + ": " + response.body());
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
        return normalizeAgentResult(jsonText);
    }

    private JsonObject callGroq(String message, JsonObject uiState, String mode) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("model", groqModel);
        body.addProperty("temperature", 0.2);

        JsonArray messages = new JsonArray();
        messages.add(chatMessage("system", systemPrompt(mode)));
        messages.add(chatMessage("user", userPrompt(message, uiState, mode)));
        body.add("messages", messages);

        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "json_object");
        body.add("response_format", responseFormat);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_ENDPOINT))
                .timeout(Duration.ofSeconds(70))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + groqApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("Groq HTTP " + response.statusCode() + ": " + response.body());
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
        return normalizeAgentResult(jsonText);
    }

    private JsonObject chatMessage(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    private String systemPrompt(String mode) {
        return """
            You are the Syndicati desktop assistant.
            You help users navigate and interact with the Syndicati property management JavaFX app.

            Return ONLY valid JSON. No markdown. No code fences.

            Required JSON shape:
            {
              "success": true,
              "reply": "short user-facing sentence",
              "actions": [
                {"type": "NAVIGATE", "target": "profile"},
                {"type": "CLICK", "target": "Save"},
                {"type": "FILL", "target": "Email", "value": "user@example.com"}
              ]
            }

            Valid action types:
            - NAVIGATE: target must be one of home, services, about, profile, settings, dashboard,
              services/syndicat, services/forum, services/residence, services/evenement.
            - CLICK: target is a visible button/label/id from CURRENT UI ELEMENTS.
            - FILL: target is a visible input id/label/prompt from CURRENT UI ELEMENTS and value is required.

            Rules:
            - For page navigation requests, prefer NAVIGATE over CLICK.
            - In chat mode, normally return no actions.
            - In takeover mode, return actions only when the request needs app interaction.
            - If unsure, ask a short clarification in reply and return an empty actions array.
            - Never invent database data.
            MODE: """ + mode;
    }

    private String userPrompt(String message, JsonObject uiState, String mode) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("User request:\n").append(message == null ? "" : message).append("\n\n");
        prompt.append("Mode: ").append(mode).append("\n\n");
        if ("takeover".equalsIgnoreCase(mode) && uiState != null) {
            prompt.append("CURRENT UI ELEMENTS JSON:\n");
            JsonElement elements = uiState.get("elements");
            prompt.append(elements == null ? gson.toJson(uiState) : gson.toJson(elements));
            prompt.append("\n\n");
        }
        prompt.append("Return the strict JSON object now.");
        return prompt.toString();
    }

    private JsonObject actionResponseSchema() {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "OBJECT");

        JsonObject properties = new JsonObject();
        properties.add("success", typed("BOOLEAN"));
        properties.add("reply", typed("STRING"));

        JsonObject actions = new JsonObject();
        actions.addProperty("type", "ARRAY");
        JsonObject actionItem = new JsonObject();
        actionItem.addProperty("type", "OBJECT");
        JsonObject actionProps = new JsonObject();
        JsonObject type = typed("STRING");
        JsonArray enumValues = new JsonArray();
        enumValues.add("NAVIGATE");
        enumValues.add("CLICK");
        enumValues.add("FILL");
        type.add("enum", enumValues);
        actionProps.add("type", type);
        actionProps.add("target", typed("STRING"));
        actionProps.add("value", typed("STRING"));
        actionItem.add("properties", actionProps);
        actionItem.add("required", stringArray("type", "target"));
        actionItem.add("propertyOrdering", stringArray("type", "target", "value"));
        actions.add("items", actionItem);
        properties.add("actions", actions);

        schema.add("properties", properties);
        schema.add("required", stringArray("success", "reply", "actions"));
        schema.add("propertyOrdering", stringArray("success", "reply", "actions"));
        return schema;
    }

    private JsonObject typed(String type) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type);
        return object;
    }

    private JsonArray stringArray(String... values) {
        JsonArray array = new JsonArray();
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private JsonObject normalizeAgentResult(String jsonText) {
        if (jsonText == null || jsonText.isBlank()) {
            return fallbackResult("The agent returned an empty response.");
        }

        String clean = stripCodeFence(jsonText.trim());
        JsonElement parsed = JsonParser.parseString(clean);
        if (!parsed.isJsonObject()) {
            return fallbackResult(clean);
        }

        JsonObject object = parsed.getAsJsonObject();
        if (!object.has("success")) {
            object.addProperty("success", true);
        }
        if (!object.has("reply") || object.get("reply").isJsonNull()) {
            object.addProperty("reply", "Done.");
        }
        if (!object.has("actions") || !object.get("actions").isJsonArray()) {
            object.add("actions", new JsonArray());
        }
        return object;
    }

    private String stripCodeFence(String text) {
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            int lastFence = text.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline) {
                return text.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return text;
    }

    private JsonObject fallbackResult(String reply) {
        JsonObject result = new JsonObject();
        result.addProperty("success", false);
        result.addProperty("reply", reply == null || reply.isBlank() ? "Agent failed to respond." : reply);
        result.add("actions", new JsonArray());
        return result;
    }

    private void executeActions(JsonObject result) {
        JsonArray actions = result.getAsJsonArray("actions");
        if (actions == null || actions.isEmpty()) {
            return;
        }

        for (JsonElement actionEl : actions) {
            if (!actionEl.isJsonObject()) {
                continue;
            }
            JsonObject action = actionEl.getAsJsonObject();
            String type = getString(action, "type").toUpperCase();
            String target = getString(action, "target");
            String value = getString(action, "value");
            if (type.isBlank() || target.isBlank()) {
                continue;
            }
            AppActionBridge.executeAction(type, target, value);
        }
    }

    private String cleanReply(JsonObject result) {
        String reply = getString(result, "reply");
        return reply.isBlank() ? "Done." : reply.replaceAll("\\*\\*", "");
    }

    private String getString(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return "";
        }
        return object.get(key).getAsString();
    }
}
