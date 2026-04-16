package com.syndicati.services.dashboard;

import com.syndicati.controllers.syndicat.ReclamationController;
import com.syndicati.models.syndicat.Reclamation;
import com.syndicati.models.syndicat.Reponse;
import com.syndicati.controllers.user.onboarding.OnboardingController;
import com.syndicati.controllers.user.profile.ProfileController;
import com.syndicati.controllers.user.user.UserController;
import com.syndicati.models.user.Onboarding;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DashboardAdminService {

    private final UserController userController;
    private final ProfileController profileController;
    private final OnboardingController onboardingController;
    private final ReclamationController reclamationController;

    public DashboardAdminService() {
        this.userController = new UserController();
        this.profileController = new ProfileController();
        this.onboardingController = new OnboardingController();
        this.reclamationController = new ReclamationController();
    }

    public List<User> users() {
        return userController.users();
    }

    public List<Profile> profiles() {
        return profileController.profiles();
    }

    public List<Onboarding> onboardings() {
        return onboardingController.onboardings();
    }

    public List<Reclamation> reclamations() {
        return reclamationController.reclamations();
    }

    public List<Reponse> reponses() {
        return reclamationController.reponses();
    }

    public boolean saveEntity(String entityLabel, String mode, String[] originalRowData, VBox fields) {
        if ("User".equalsIgnoreCase(entityLabel)) {
            return saveUser(mode, originalRowData, fields);
        }
        if ("Profile".equalsIgnoreCase(entityLabel)) {
            return saveProfile(mode, originalRowData, fields);
        }
        if ("Onboarding".equalsIgnoreCase(entityLabel)) {
            return saveOnboarding(mode, originalRowData, fields);
        }
        if ("Reclamation".equalsIgnoreCase(entityLabel)) {
            return saveReclamation(mode, originalRowData, fields);
        }
        if ("Reponse".equalsIgnoreCase(entityLabel)) {
            return saveReponse(mode, originalRowData, fields);
        }
        return false;
    }

    public boolean deleteEntity(String entityLabel, String[] rowData) {
        if ("User".equalsIgnoreCase(entityLabel)) {
            return deleteUser(rowData);
        }
        if ("Profile".equalsIgnoreCase(entityLabel)) {
            return deleteProfile(rowData);
        }
        if ("Reclamation".equalsIgnoreCase(entityLabel)) {
            return deleteReclamation(rowData);
        }
        if ("Reponse".equalsIgnoreCase(entityLabel)) {
            return deleteReponse(rowData);
        }
        return false;
    }

    private boolean saveUser(String mode, String[] originalRowData, VBox fields) {
        Map<String, String> values = readEditableFieldValues(fields);
        String name = safe(values.get("Name"));
        String email = safe(values.get("Email"));
        String role = safe(values.get("Role")).toUpperCase();
        String verifiedText = safe(values.get("Verified"));
        String statusText = safe(values.get("Status"));

        if ("-".equals(name) || "-".equals(email) || "-".equals(role)) {
            return false;
        }

        String[] splitName = splitName(name);
        boolean verified = isTruthy(verifiedText);
        boolean disabled = "DISABLED".equalsIgnoreCase(statusText);

        if ("add".equals(mode)) {
            User user = new User();
            user.setFirstName(splitName[0]);
            user.setLastName(splitName[1]);
            user.setEmailUser(email);
            user.setRoleUser(role);
            user.setVerified(verified);
            user.setDisabled(disabled);
            user.setPasswordUser("ChangeMe#2026");
            return userController.userAdd(user) > 0;
        }

        if ("edit".equals(mode)) {
            String oldEmail = (originalRowData != null && originalRowData.length > 1) ? originalRowData[1] : email;
            Optional<User> existingOpt = userController.userByEmail(oldEmail);
            if (existingOpt.isEmpty()) {
                existingOpt = userController.userByEmail(email);
            }
            if (existingOpt.isEmpty()) {
                return false;
            }

            User existing = existingOpt.get();
            existing.setFirstName(splitName[0]);
            existing.setLastName(splitName[1]);
            existing.setEmailUser(email);
            existing.setRoleUser(role);
            existing.setVerified(verified);
            existing.setDisabled(disabled);
            if (existing.getPasswordUser() == null || existing.getPasswordUser().isBlank()) {
                existing.setPasswordUser("ChangeMe#2026");
            }
            return userController.userEdit(existing);
        }

        return false;
    }

    private boolean saveProfile(String mode, String[] originalRowData, VBox fields) {
        Map<String, String> values = readEditableFieldValues(fields);

        if ("add".equals(mode)) {
            String userIdStr = safe(values.get("User ID"));
            if ("-".equals(userIdStr)) {
                return false;
            }

            try {
                int userId = Integer.parseInt(userIdStr);
                Profile profile = new Profile();
                profile.setUserId(userId);
                profile.setLocale(safe(values.get("Locale")));
                String themeStr = safe(values.get("Theme"));
                profile.setTheme("-".equals(themeStr) ? null : Integer.parseInt(themeStr));
                String tzStr = safe(values.get("Timezone"));
                profile.setTimezone("-".equals(tzStr) ? null : Integer.parseInt(tzStr));
                profile.setAvatar(null);
                profile.setDescriptionProfile(safe(values.get("Bio")));

                Optional<Integer> createdIdOpt = profileController.profileCreate(profile);
                return createdIdOpt.isPresent();
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if ("edit".equals(mode)) {
            String profileIdStr = (originalRowData != null && originalRowData.length > 0) ? originalRowData[0] : "-";
            if ("-".equals(profileIdStr)) {
                return false;
            }

            try {
                int profileId = Integer.parseInt(profileIdStr);
                Optional<Profile> existingOpt = profileController.profileById(profileId);
                if (existingOpt.isEmpty()) {
                    return false;
                }

                Profile existing = existingOpt.get();
                existing.setLocale(safe(values.get("Locale")));
                String themeStr = safe(values.get("Theme"));
                existing.setTheme("-".equals(themeStr) ? null : Integer.parseInt(themeStr));
                String tzStr = safe(values.get("Timezone"));
                existing.setTimezone("-".equals(tzStr) ? null : Integer.parseInt(tzStr));
                existing.setDescriptionProfile(safe(values.get("Bio")));

                return profileController.profileUpdate(existing);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }

    private boolean deleteUser(String[] rowData) {
        if (rowData == null || rowData.length < 2) {
            return false;
        }

        String email = rowData[1];
        Optional<User> existing = userController.userByEmail(email);
        return existing.filter(user -> user.getIdUser() != null)
            .map(user -> userController.userDelete(user.getIdUser()))
            .orElse(false);
    }

    private boolean deleteProfile(String[] rowData) {
        if (rowData == null || rowData.length < 1) {
            return false;
        }

        try {
            int profileId = Integer.parseInt(rowData[0]);
            return profileController.profileDelete(profileId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean saveOnboarding(String mode, String[] originalRowData, VBox fields) {
        Map<String, String> values = readEditableFieldValues(fields);

        if ("add".equals(mode)) {
            String userIdStr = safe(values.get("User ID"));
            if ("-".equals(userIdStr)) {
                return false;
            }

            try {
                Onboarding onboarding = new Onboarding();
                onboarding.setUserId(Integer.parseInt(userIdStr));
                onboarding.setStep(parseIntOrDefault(values.get("Step"), 1));
                onboarding.setCompleted(isTruthy(values.get("Completed")));
                onboarding.setSelectedLocale(safe(values.get("Locale")));
                onboarding.setSelectedTheme(safe(values.get("Theme")));
                onboarding.setSuggestions(normalizeOptional(values.get("Suggestions")));
                onboarding.setSelectedPreferencesJson(normalizeOptional(values.get("Preferences")));

                return onboardingController.saveOnboarding(onboarding);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if ("edit".equals(mode)) {
            String onboardingIdStr = (originalRowData != null && originalRowData.length > 0) ? originalRowData[0] : "-";
            if ("-".equals(onboardingIdStr)) {
                return false;
            }

            try {
                int onboardingId = Integer.parseInt(onboardingIdStr);
                Optional<Onboarding> existingOpt = onboardingController.onboardingById(onboardingId);
                if (existingOpt.isEmpty()) {
                    return false;
                }

                Onboarding existing = existingOpt.get();
                existing.setUserId(parseIntOrNull(values.get("User ID"), existing.getUserId()));
                existing.setStep(parseIntOrDefault(values.get("Step"), existing.getStep()));
                existing.setCompleted(isTruthy(values.get("Completed")));
                existing.setSelectedLocale(safe(values.get("Locale")));
                existing.setSelectedTheme(safe(values.get("Theme")));
                existing.setSuggestions(normalizeOptional(values.get("Suggestions")));
                existing.setSelectedPreferencesJson(normalizeOptional(values.get("Preferences")));

                return onboardingController.saveOnboarding(existing);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }

    private boolean saveReclamation(String mode, String[] originalRowData, VBox fields) {
        Map<String, String> values = readEditableFieldValues(fields);

        if ("add".equals(mode)) {
            String title = safe(values.get("Title"));
            String userDisplayName = safe(values.get("User"));
            String statut = normalizeReclamationStatus(values.get("Status"));
            LocalDateTime date = parseDate(values.get("Date"));

            if ("-".equals(title) || "-".equals(userDisplayName)) {
                return false;
            }

            Optional<User> userOpt = findUserByDisplayName(userDisplayName);
            if (userOpt.isEmpty()) {
                return false;
            }

            String generatedDescription = "Created from dashboard: " + title;
            Integer createdId = reclamationController.reclamationCreate(
                title,
                generatedDescription,
                date,
                null,
                userOpt.get()
            );

            if (createdId == null || createdId <= 0) {
                return false;
            }

            if (!"-".equals(statut) && !"en_attente".equals(statut)) {
                return reclamationController.reclamationUpdateStatut(createdId, statut);
            }
            return true;
        }

        if ("edit".equals(mode)) {
            String statut = normalizeReclamationStatus(values.get("Status"));
            if ("-".equals(statut)) {
                return false;
            }

            Optional<Reclamation> targetOpt = findReclamationByRow(originalRowData);
            if (targetOpt.isEmpty()) {
                return false;
            }

            Reclamation target = targetOpt.get();
            if (target.getIdReclamations() == null || target.getIdReclamations() <= 0) {
                return false;
            }

            return reclamationController.reclamationUpdateStatut(target.getIdReclamations(), statut);
        }

        return false;
    }

    private boolean saveReponse(String mode, String[] originalRowData, VBox fields) {
        Map<String, String> values = readEditableFieldValues(fields);

        if ("add".equals(mode)) {
            String message = safe(values.get("Message"));
            String userDisplayName = safe(values.get("User"));
            String reclamationTitle = safe(values.get("Reclamation"));

            if ("-".equals(message) || "-".equals(userDisplayName) || "-".equals(reclamationTitle)) {
                return false;
            }

            Optional<User> userOpt = findUserByDisplayName(userDisplayName);
            Optional<Reclamation> reclamationOpt = findReclamationByTitle(reclamationTitle);
            if (userOpt.isEmpty() || reclamationOpt.isEmpty()) {
                return false;
            }

            Integer createdId = reclamationController.reponseCreate(
                null,
                message,
                null,
                reclamationOpt.get(),
                userOpt.get()
            );
            return createdId != null && createdId > 0;
        }

        if ("edit".equals(mode)) {
            Optional<Reponse> targetOpt = findReponseByRow(originalRowData);
            if (targetOpt.isEmpty()) {
                return false;
            }

            Reponse target = targetOpt.get();
            if (target.getIdReponses() == null || target.getIdReponses() <= 0) {
                return false;
            }

            String updatedMessage = safe(values.get("Message"));
            if ("-".equals(updatedMessage)) {
                return false;
            }

            return reclamationController.reponseUpdate(
                target.getIdReponses(),
                target.getTitreReponse(),
                updatedMessage
            );
        }

        return false;
    }

    private boolean deleteReclamation(String[] rowData) {
        Optional<Reclamation> targetOpt = findReclamationByRow(rowData);
        if (targetOpt.isEmpty() || targetOpt.get().getIdReclamations() == null) {
            return false;
        }
        return reclamationController.reclamationDelete(targetOpt.get().getIdReclamations());
    }

    private boolean deleteReponse(String[] rowData) {
        Optional<Reponse> targetOpt = findReponseByRow(rowData);
        if (targetOpt.isEmpty() || targetOpt.get().getIdReponses() == null) {
            return false;
        }
        return reclamationController.reponseDelete(targetOpt.get().getIdReponses());
    }

    private Optional<Reponse> findReponseByRow(String[] originalRowData) {
        if (originalRowData == null || originalRowData.length < 4) {
            return Optional.empty();
        }

        String rowMessage = safe(originalRowData[0]);
        String rowUser = safe(originalRowData[1]);
        String rowReclamation = safe(originalRowData[2]);
        String rowDate = safe(originalRowData[3]);

        for (Reponse rep : reclamationController.reponses()) {
            String repMessage = toReponseTableMessage(rep.getMessageReponse());
            String repUser = safe(reclamationUserName(rep.getUser()));
            String repReclamation = rep.getReclamation() != null ? safe(rep.getReclamation().getTitreReclamations()) : "-";
            String repDate = rep.getCreatedAt() != null ? rep.getCreatedAt().toString().substring(0, 10) : "-";

            if (repMessage.equals(rowMessage)
                && repUser.equals(rowUser)
                && repReclamation.equals(rowReclamation)
                && repDate.equals(rowDate)) {
                return Optional.of(rep);
            }
        }

        return Optional.empty();
    }

    private String toReponseTableMessage(String message) {
        String normalized = safe(message);
        if (normalized.length() > 50) {
            return normalized.substring(0, 47) + "...";
        }
        return normalized;
    }

    private Optional<Reclamation> findReclamationByTitle(String title) {
        String normalized = safe(title);
        if ("-".equals(normalized)) {
            return Optional.empty();
        }

        for (Reclamation rec : reclamationController.reclamations()) {
            if (safe(rec.getTitreReclamations()).equals(normalized)) {
                return Optional.of(rec);
            }
        }

        return Optional.empty();
    }

    private Optional<User> findUserByDisplayName(String displayName) {
        String normalized = safe(displayName);
        if ("-".equals(normalized)) {
            return Optional.empty();
        }

        for (User user : userController.users()) {
            String full = reclamationUserName(user);
            if (full.equals(normalized)) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    private LocalDateTime parseDate(String dateValue) {
        String normalized = safe(dateValue);
        if ("-".equals(normalized)) {
            return LocalDateTime.now();
        }

        try {
            return LocalDate.parse(normalized).atStartOfDay();
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }

    private String reclamationUserName(User user) {
        if (user == null) {
            return "Unknown";
        }
        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "Unknown" : full;
    }

    private Optional<Reclamation> findReclamationByRow(String[] originalRowData) {
        if (originalRowData == null || originalRowData.length < 5) {
            return Optional.empty();
        }

        String rowTitle = safe(originalRowData[0]);
        String rowUser = safe(originalRowData[1]);
        String rowDate = safe(originalRowData[3]);
        String rowReplies = safe(originalRowData[4]);

        for (Reclamation rec : reclamationController.reclamations()) {
            String recTitle = safe(rec.getTitreReclamations());
            String recUser = safe(reclamationUserName(rec));
            String recDate = rec.getCreatedAt() != null ? rec.getCreatedAt().toString().substring(0, 10) : "-";
            String recReplies = String.valueOf(rec.getReponses() != null ? rec.getReponses().size() : 0);

            if (recTitle.equals(rowTitle)
                && recUser.equals(rowUser)
                && recDate.equals(rowDate)
                && recReplies.equals(rowReplies)) {
                return Optional.of(rec);
            }
        }

        return Optional.empty();
    }

    private String reclamationUserName(Reclamation rec) {
        if (rec == null || rec.getUser() == null) {
            return "Unknown";
        }
        String first = rec.getUser().getFirstName() == null ? "" : rec.getUser().getFirstName().trim();
        String last = rec.getUser().getLastName() == null ? "" : rec.getUser().getLastName().trim();
        String full = (first + " " + last).trim();
        return full.isEmpty() ? "Unknown" : full;
    }

    private String normalizeReclamationStatus(String value) {
        String normalized = safe(value);
        if ("-".equals(normalized)) {
            return "-";
        }

        String token = normalized.trim().toLowerCase().replace(' ', '_');
        return switch (token) {
            case "active", "en_attente", "refuse", "termine" -> token;
            case "pending" -> "en_attente";
            case "rejected" -> "refuse";
            case "completed" -> "termine";
            default -> "-";
        };
    }

    private Map<String, String> readEditableFieldValues(VBox fields) {
        Map<String, String> values = new LinkedHashMap<>();
        for (Node node : fields.getChildren()) {
            if (!(node instanceof VBox)) {
                continue;
            }
            VBox row = (VBox) node;
            if (row.getChildren().size() < 2) {
                continue;
            }
            Node labelNode = row.getChildren().get(0);
            Node inputNode = row.getChildren().get(1);
            if (labelNode instanceof Text && inputNode instanceof TextField) {
                String label = ((Text) labelNode).getText();
                String value = ((TextField) inputNode).getText();
                values.put(label, value);
                continue;
            }
            if (labelNode instanceof Text && inputNode instanceof ComboBox) {
                String label = ((Text) labelNode).getText();
                Object selected = ((ComboBox<?>) inputNode).getValue();
                values.put(label, selected == null ? "" : selected.toString());
            }
        }
        return values;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String[] splitName(String fullName) {
        String normalized = fullName == null ? "" : fullName.trim();
        if (normalized.isEmpty()) {
            return new String[]{"Unknown", "User"};
        }

        String[] parts = normalized.split("\\s+", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], parts[0]};
        }
        return new String[]{parts[0], parts[1]};
    }

    private boolean isTruthy(String value) {
        String normalized = value == null ? "" : value.trim();
        return "yes".equalsIgnoreCase(normalized)
            || "true".equalsIgnoreCase(normalized)
            || "1".equalsIgnoreCase(normalized)
            || "verified".equalsIgnoreCase(normalized);
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            String normalized = safe(value);
            return "-".equals(normalized) ? defaultValue : Integer.parseInt(normalized);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer parseIntOrNull(String value, Integer fallback) {
        try {
            String normalized = safe(value);
            return "-".equals(normalized) ? fallback : Integer.parseInt(normalized);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String normalizeOptional(String value) {
        String normalized = safe(value);
        return "-".equals(normalized) ? null : normalized;
    }
}