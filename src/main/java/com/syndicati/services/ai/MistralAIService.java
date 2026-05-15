package com.syndicati.services.ai;

import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.utils.config.EnvConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Service for generating residence maintenance recommendations with Mistral AI.
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

    public String generateRecommendation(Apartment apartment, Maintenance maintenance) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Mistral API key is not configured. Please check your environment settings.";
        }
        if (apartment == null) {
            return "Apartment data is missing, so the AI recommendation cannot be generated.";
        }

        LocalDate now = LocalDate.now();
        String today = now.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String minDate = now.plusWeeks(2).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        String prompt = buildPrompt(apartment, maintenance, today, minDate, now.plusWeeks(2));

        JSONObject payload = new JSONObject();
        payload.put("model", "mistral-small-latest");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", "Tu es un assistant expert en maintenance residentielle. Tu reponds de facon concise et operationnelle."));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", prompt));

        payload.put("messages", messages);
        payload.put("max_tokens", 600);
        payload.put("temperature", 0.35);

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
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray choices = jsonResponse.optJSONArray("choices");
                if (choices != null && choices.length() > 0) {
                    String content = choices.getJSONObject(0)
                            .getJSONObject("message")
                            .optString("content", "");
                    if (!content.isBlank() && !"null".equalsIgnoreCase(content.trim())) {
                        return normalizeRecommendation(content);
                    }
                }
                return "Mistral AI returned an empty recommendation. Please try again.";
            }

            String error = response.body() != null ? response.body().string() : "No response body";
            System.err.println("Mistral API Error: " + response.code() + " - " + error);
            if (response.code() == 401 || response.code() == 403) {
                return "Mistral authentication failed. Verify the API key.";
            }
            if (response.code() == 429) {
                return "Mistral request limit reached. Please retry later.";
            }
        } catch (Exception e) {
            System.err.println("Mistral API Exception: " + e.getMessage());
            e.printStackTrace();
        }

        return "Mistral AI could not generate a recommendation right now. Check the console and retry.";
    }

    private String buildPrompt(Apartment apartment, Maintenance maintenance, String today, String minDate, LocalDate isoMinDate) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Tu es un expert en maintenance de batiments residentiels. Analyse cet appartement et fournis une prediction courte, concrete et exploitable.\n");
        promptBuilder.append("Aujourd'hui nous sommes le ").append(today).append(". La date de maintenance recommandee doit etre APRES le ").append(minDate).append(" (minimum 2 semaines a partir d'aujourd'hui).\n\n");

        promptBuilder.append("Donnees de l'appartement:\n");
        promptBuilder.append("- ID appartement: ").append(value(apartment.getIdApartment())).append("\n");
        promptBuilder.append("- ID residence: ").append(value(apartment.getIdResidence())).append("\n");
        promptBuilder.append("- Type: ").append(value(apartment.getTypeApartment())).append("\n");
        promptBuilder.append("- Superficie: ").append(value(apartment.getArea())).append(" m2\n");
        promptBuilder.append("- Date de construction: ").append(value(apartment.getDateConstructed())).append("\n");
        promptBuilder.append("- Parking: ").append(isYes(apartment.getParking())).append("\n");
        promptBuilder.append("- Statut occupation: ").append(apartment.getAvailable() != null && apartment.getAvailable() == 1 ? "Disponible" : "Occupe").append("\n");
        promptBuilder.append("- Informations structurees: ").append(value(apartment.getApartmentInfo())).append("\n\n");

        promptBuilder.append("Fiche maintenance:\n");
        if (maintenance != null) {
            promptBuilder.append("- Etat general: ").append(value(maintenance.getGeneralCondition())).append("\n");
            promptBuilder.append("- Etat plomberie: ").append(value(maintenance.getPlumbingCondition())).append("\n");
            promptBuilder.append("- Etat electricite: ").append(value(maintenance.getElectricalCondition())).append("\n");
            promptBuilder.append("- Etat chauffage: ").append(value(maintenance.getHeatingCondition())).append("\n");
            promptBuilder.append("- Derniere maintenance: ").append(value(maintenance.getLastMaintenanceDate())).append("\n");
            promptBuilder.append("- Notes: ").append(value(maintenance.getDescription())).append("\n");
        } else {
            promptBuilder.append("- Aucune fiche de maintenance existante.\n");
        }

        promptBuilder.append("\nRegles:\n");
        promptBuilder.append("- Priorise les risques plomberie, electricite et chauffage si leur etat est mauvais ou critique.\n");
        promptBuilder.append("- Si des donnees manquent, recommande un diagnostic cible au lieu d'inventer.\n");
        promptBuilder.append("- La date doit etre au format ISO YYYY-MM-DD et rester apres ").append(isoMinDate).append(".\n");
        promptBuilder.append("- Ne mentionne pas le modele IA, les limites techniques ou les donnees manquantes sauf si cela change l'action de maintenance.\n\n");

        promptBuilder.append("Reponds UNIQUEMENT dans ce format exact, sans markdown, sans asterisques, sans texte supplementaire:\n");
        promptBuilder.append("Etat: [Faible / Modere / Critique]\n");
        promptBuilder.append("Actions:\n");
        promptBuilder.append("- [action 1 concrete]\n");
        promptBuilder.append("- [action 2 concrete]\n");
        promptBuilder.append("- [action 3 concrete]\n");
        promptBuilder.append("Date de maintenance recommandee: [YYYY-MM-DD]");
        return promptBuilder.toString();
    }

    private static String value(Object value) {
        if (value == null) return "Non renseigne";
        String text = String.valueOf(value).trim();
        return text.isBlank() || "null".equalsIgnoreCase(text) ? "Non renseigne" : text;
    }

    private static String isYes(Integer value) {
        return value != null && value == 1 ? "Oui" : "Non";
    }

    private static String normalizeRecommendation(String content) {
        String cleaned = content
                .replace("*", "")
                .replace("Recommendée", "recommandee")
                .replace("Recommandee", "recommandee")
                .replace("Recommandée", "recommandee")
                .trim();
        if (!cleaned.toLowerCase().contains("etat:")) {
            cleaned = "Etat: A verifier\nActions:\n- " + cleaned;
        }
        return cleaned;
    }
}
