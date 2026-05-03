package com.syndicati.services.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import com.syndicati.services.ai.AppInspector;
import com.syndicati.services.ai.AppActionBridge;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * AgentService
 * Connects to the Syndicati LangGraph Python worker.
 */
public class AgentService {

    private static AgentService instance;
    private final HttpClient httpClient;
    private final String workerUrl = "http://127.0.0.1:8002/chat";
    private final Gson gson = new Gson();
    private Process pythonProcess;

    private AgentService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
                
        startPythonWorker();
        
        // Add shutdown hook to kill Python process when app closes
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopPythonWorker));
    }

    private void startPythonWorker() {
        try {
            // Kill any existing process on port 8002 (Windows specific)
            cleanupPort8002();

            String scriptPath = new java.io.File("workers/langgraph_agent_service.py").getAbsolutePath();
            System.out.println("[INFO] Starting LangGraph Agent Worker: " + scriptPath);
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath);
            pb.environment().put("GOOGLE_API_KEY", "AIzaSyDH9UIgLTkT8N8BN9xXBCVlFjw-sGqeGtA");
            pb.environment().put("GROQ_API_KEY", "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");
            pb.redirectErrorStream(true);
            pythonProcess = pb.start();

            new Thread(() -> {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(pythonProcess.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[LangGraph Worker] " + line);
                    }
                } catch (Exception e) {}
            }).start();
            
            Thread.sleep(3000);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to start LangGraph Agent Worker: " + e.getMessage());
        }
    }

    private void cleanupPort8002() {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "for /f \"tokens=5\" %a in ('netstat -aon ^| findstr 8002') do taskkill /f /pid %a");
            pb.start().waitFor();
        } catch (Exception ignored) {}
    }

    public void stopPythonWorker() {
        if (pythonProcess != null && pythonProcess.isAlive()) {
            System.out.println("[INFO] Shutting down AI Agent Worker...");
            pythonProcess.destroyForcibly();
            cleanupPort8002();
        }
    }

    public static synchronized AgentService getInstance() {
        if (instance == null) {
            instance = new AgentService();
        }
        return instance;
    }

    public static synchronized void shutdown() {
        if (instance != null) {
            instance.stopPythonWorker();
        }
    }

    public CompletableFuture<String> chatAsync(String message) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject payload = new JsonObject();
            payload.addProperty("message", message);
            payload.addProperty("mode", "chat");
            payload.addProperty("session_id", "chat_" + System.currentTimeMillis());
            
            try {
                String response = sendRequest(payload);
                System.out.println("[AgentService] Chat Raw Response: " + response);
                
                JsonObject resJson = JsonParser.parseString(response).getAsJsonObject();
                
                // If reply is present, we consider it a success even if the 'success' field is missing
                if (resJson.has("reply") && !resJson.get("reply").isJsonNull()) {
                    String reply = resJson.get("reply").getAsString();
                    // Strip markdown bold markers (**) for clean display in UI
                    return reply.replaceAll("\\*\\*", "");
                } else if (resJson.has("success") && resJson.get("success").getAsBoolean()) {
                    // Fallback to legacy success check
                    return "Done.";
                } else {
                    return "Error: " + (resJson.has("error") ? resJson.get("error").getAsString() : "Unknown");
                }
            } catch (Exception e) {
                e.printStackTrace(); // Print full stack trace for debugging
                return "Worker Error: " + e.getMessage();
            }
        });
    }

    public CompletableFuture<String> takeoverAsync(String userRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject uiState = AppInspector.inspectCurrentScene();
                System.out.println("[AgentService] Requesting Takeover with UI State...");
                
                JsonObject payload = new JsonObject();
                payload.addProperty("message", userRequest);
                payload.addProperty("session_id", "takeover_" + System.currentTimeMillis());
                payload.add("ui_state", uiState);
                payload.addProperty("mode", "takeover");

                String response = sendRequest(payload);
                System.out.println("[AgentService] Raw Response: " + response);
                JsonObject resJson = gson.fromJson(response, JsonObject.class);

                if (resJson.has("actions") && !resJson.get("actions").isJsonNull()) {
                    JsonArray actions = resJson.getAsJsonArray("actions");
                    for (JsonElement actionEl : actions) {
                        if (!actionEl.isJsonObject()) continue;
                        JsonObject action = actionEl.getAsJsonObject();
                        String type = action.get("type").getAsString();
                        String target = action.get("target").getAsString();
                        String value = action.has("value") ? action.get("value").getAsString() : "";
                        
                        AppActionBridge.executeAction(type, target, value);
                    }
                }

                if (resJson.has("reply") && !resJson.get("reply").isJsonNull()) {
                    String reply = resJson.get("reply").getAsString();
                    return reply.replaceAll("\\*\\*", "");
                }
                return "Executing your request...";
            } catch (Exception e) {
                return "Takeover Error: " + e.getMessage();
            }
        });
    }

    private String sendRequest(JsonObject payload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(workerUrl))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        if (body == null || body.isBlank()) {
            throw new RuntimeException("Worker returned an empty response.");
        }
        return body;
    }
}
