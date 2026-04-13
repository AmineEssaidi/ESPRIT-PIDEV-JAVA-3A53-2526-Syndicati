package com.syndicati.services.dashboard;

import com.syndicati.controllers.user.onboarding.OnboardingController;
import com.syndicati.controllers.user.profile.ProfileController;
import com.syndicati.controllers.user.user.UserController;
import com.syndicati.models.user.Onboarding;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DashboardAdminService {

    private final UserController userController;
    private final ProfileController profileController;
    private final OnboardingController onboardingController;

    public DashboardAdminService() {
        this.userController = new UserController();
        this.profileController = new ProfileController();
        this.onboardingController = new OnboardingController();
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
        return false;
    }

    public boolean deleteEntity(String entityLabel, String[] rowData) {
        if ("User".equalsIgnoreCase(entityLabel)) {
            return deleteUser(rowData);
        }
        if ("Profile".equalsIgnoreCase(entityLabel)) {
            return deleteProfile(rowData);
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