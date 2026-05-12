package com.syndicati.services.ai;

import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.utils.config.EnvConfig;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Service for generating maintenance recommendations using Mistral AI.
 * Ported from Horizon web application.
 */
public class MistralAIService {

    private static final String API_URL = "https://api.mistral.ai/v1/chat/completions";
    private final String apiKey;
    private final OkHttpClient client;

    public MistralAIService() {
        this.apiKey = EnvConfig.get("MISTRAL_API_KEY");
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Generates a maintenance recommendation for a specific apartment and its current maintenance state.
     */
    public String generateRecommendation(Apartment apartment, Maintenance maintenance) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Mistral API Key not configured. Please check your .env or application.local.properties file.";
        }

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String minDate = LocalDate.now().plusWeeks(2).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Tu es un expert en maintenance de bâtiments. Analyse cet appartement et fournis une prédiction de maintenance courte et concise.\n");
        promptBuilder.append("Aujourd'hui nous sommes le ").append(today).append(". La date de maintenance recommandée doit être APRÈS le ").append(minDate).append(" (minimum 2 semaines à partir d'aujourd'hui).\n\n");
        
        promptBuilder.append("Données de l'appartement:\n");
        promptBuilder.append("- Type: ").append(apartment.getTypeApartment()).append("\n");
        promptBuilder.append("- Superficie: ").append(apartment.getArea()).append(" m²\n");
        // Note: Apartment in Java might not have construction date, using a default if missing
        promptBuilder.append("- Informations: ").append(apartment.getApartmentInfo()).append("\n\n");

        promptBuilder.append("Fiche maintenance:\n");
        if (maintenance != null) {
            promptBuilder.append("- État général: ").append(maintenance.getGeneralCondition()).append("\n");
            promptBuilder.append("- État plomberie: ").append(maintenance.getPlumbingCondition()).append("\n");
            promptBuilder.append("- État électricité: ").append(maintenance.getElectricalCondition()).append("\n");
            promptBuilder.append("- État chauffage: ").append(maintenance.getHeatingCondition()).append("\n");
            promptBuilder.append("- Dernière maintenance: ").append(maintenance.getLastMaintenanceDate()).append("\n");
            promptBuilder.append("- Notes: ").append(maintenance.getDescription()).append("\n");
        } else {
            promptBuilder.append("- Aucune fiche de maintenance existante.\n");
        }

        promptBuilder.append("\nRéponds UNIQUEMENT dans ce format exact, sans astérisques, sans texte supplémentaire:\n");
        promptBuilder.append("Etat: [Faible / Modéré / Critique]\n");
        promptBuilder.append("Actions: [actions recommandées dans l'appartement lui même, et équipements à réparer, max 3 phrases, retourne à la ligne pour chaque action]\n");
        promptBuilder.append("Date de maintenance Recommendée: [YYYY-MM-DD]");

        String prompt = promptBuilder.toString();

        JSONObject payload = new JSONObject();
        payload.put("model", "mistral-medium-latest");
        
        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.put(userMessage);
        
        payload.put("messages", messages);
        payload.put("max_tokens", 800);
        payload.put("temperature", 0.4);

        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                // System.out.println("Mistral API Response: " + responseBody);
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    String content = choices.getJSONObject(0).getJSONObject("message").optString("content", null);
                    if (content != null && !content.isBlank() && !content.equalsIgnoreCase("null")) {
                        return content;
                    } else {
                        System.err.println("Mistral API returned empty or 'null' content.");
                    }
                }
            } else {
                String error = response.body() != null ? response.body().string() : "No response body";
                System.err.println("Mistral API Error: " + response.code() + " - " + error);
                
                // If it's a 401/403/429, provide a more helpful message
                if (response.code() == 401) return "Erreur d'authentification Mistral AI. Vérifiez votre clé API.";
                if (response.code() == 429) return "Limite de requêtes Mistral AI atteinte. Réessayez plus tard.";
            }
        } catch (Exception e) {
            System.err.println("Mistral API Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return "Mistral AI n'a pas pu générer de texte pour le moment. Veuillez vérifier la console ou réessayer.";
    }
}
