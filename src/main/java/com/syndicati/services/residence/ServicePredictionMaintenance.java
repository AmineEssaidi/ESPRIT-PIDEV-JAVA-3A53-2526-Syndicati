package com.syndicati.services.residence;

import com.syndicati.models.residence.Appartement;
import com.syndicati.models.residence.Maintenance;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

public class ServicePredictionMaintenance {
    private static final String MISTRAL_API_KEY = "mOKDdpTENecfWkN3YslM0pWn8wuwXikA";
    private static final String MISTRAL_URL = "https://api.mistral.ai/v1/chat/completions";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String predict(Appartement appartement, Maintenance maintenance) {
        String today   = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String minDate = LocalDate.now().plusWeeks(2).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        String prompt = String.format("""
            Tu es un expert en maintenance de bâtiments. Analyse cet appartement et fournis une prédiction de maintenance courte et concise.
            Aujourd'hui nous sommes le %s. La date de maintenance recommandée doit être APRÈS le %s (minimum 2 semaines à partir d'aujourd'hui).

            Données de l'appartement:
            - Type: %s
            - Superficie: %s m²

            Fiche maintenance:
            - État général: %s
            - État plomberie: %s
            - État électricité: %s
            - État chauffage: %s
            - Dernière maintenance: %s
            - Notes: %s

            Réponds UNIQUEMENT dans ce format exact, sans astérisques, sans texte supplémentaire:
            Etat: [Faible / Modéré / Critique]
            Actions: [actions recommandées dans l'appartement lui même, et équipements à réparer, max 3 phrases, retourne à la ligne pour chaque action]
            Date de maintenance Recommendée: [YYYY-MM-DD]
            """,
                today,
                minDate,
                appartement.getType_a(),
                appartement.getSuperficie(),
                maintenance != null ? maintenance.getEtat_app()          : "N/A",
                maintenance != null ? maintenance.getEtat_plomberie()    : "N/A",
                maintenance != null ? maintenance.getEtat_electricite()  : "N/A",
                maintenance != null ? maintenance.getEtat_chauffage()    : "N/A",
                maintenance != null ? maintenance.getDate_derniere_maintenance() : "N/A",
                maintenance != null ? maintenance.getdescription_maint() : "N/A"
        );

        JSONObject body = new JSONObject();
        body.put("model", "mistral-small-latest");
        body.put("max_tokens", 600);
        body.put("temperature", 0.4);

        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.put(userMessage);
        body.put("messages", messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MISTRAL_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + MISTRAL_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("[Mistral] Error " + response.statusCode() + ": " + response.body());
                return "Impossible de générer une recommandation.";
            }

            JSONObject json = new JSONObject(response.body());
            return json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (IOException | InterruptedException e) {
            System.err.println("[Mistral] Request failed: " + e.getMessage());
            return "Impossible de générer une recommandation.";
        }
    }
}
