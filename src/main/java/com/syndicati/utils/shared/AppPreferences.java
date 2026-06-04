package com.syndicati.utils.shared;

import java.util.prefs.Preferences;

/**
 * AppPreferences - Persistent key-value storage equivalent to localStorage.
 * Backed by java.util.prefs.Preferences (OS keystore / registry on Windows).
 */
public class AppPreferences {

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(AppPreferences.class);
    public static final String KEY_THEME            = "theme";           // "dark" | "light"
    public static final String KEY_ACCENT_COLOR     = "accent-color";    // hex e.g. "#10b981"
    public static final String KEY_ACCENT_GRADIENT  = "accent-gradient"; // JavaFX linear-gradient(...)
    public static final String KEY_ACCENT_NAME      = "accent-name";     // e.g. "Syndicati"
    public static final String KEY_LANGUAGE         = "lang";            // "en" | "fr" | "ar"
    public static final String KEY_ANIM_ACCENTS     = "animated-accents";// "true"/"false"
    public static final String DEFAULT_THEME        = "dark";
    public static final String DEFAULT_ACCENT_COLOR = "#10b981";
    public static final String DEFAULT_ACCENT_GRADIENT = "linear-gradient(from 0% 0% to 100% 100%, #04130f 0%, #0a4f35 24%, #10b981 48%, #25f2a3 68%, #d6fff1 82%, #063b2d 100%)";
    public static final String DEFAULT_ACCENT_NAME  = "Syndicati";
    public static final String DEFAULT_LANGUAGE     = "en";
    public static final boolean DEFAULT_ANIM_ACCENTS = true;

    private AppPreferences() {}

    public static void set(String key, String value) {
        PREFS.put(key, value);
        syncToDatabase(key, value);
    }

    public static String get(String key, String defaultValue) {
        return PREFS.get(key, defaultValue);
    }

    public static void setLocalOnly(String key, String value) {
        PREFS.put(key, value);
    }

    public static String getLocal(String key, String defaultValue) {
        return PREFS.get(key, defaultValue);
    }

    public static void removeLocal(String key) {
        PREFS.remove(key);
    }

    public static void setBoolean(String key, boolean value) {
        PREFS.putBoolean(key, value);
        syncToDatabase(key, value);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return PREFS.getBoolean(key, defaultValue);
    }

    private static void syncToDatabase(String key, Object value) {
        com.syndicati.utils.session.SessionManager session = com.syndicati.utils.session.SessionManager.getInstance();
        if (session.isLoggedIn() && session.getCurrentProfile() != null) {
            com.syndicati.models.user.Profile profile = session.getCurrentProfile();
            String json = profile.getSettingsJson();
            org.json.JSONObject obj;
            try {
                obj = (json == null || json.trim().isEmpty()) ? new org.json.JSONObject() : new org.json.JSONObject(json);
            } catch (Exception e) {
                obj = new org.json.JSONObject();
            }
            obj.put(key, value);
            profile.setSettingsJson(obj.toString());
            new com.syndicati.models.user.data.ProfileRepository().update(profile);
        }
    }

    public static void syncFromProfile(com.syndicati.models.user.Profile profile) {
        if (profile == null) return;
        String json = profile.getSettingsJson();
        if (json == null || json.trim().isEmpty()) return;

        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            for (String key : obj.keySet()) {
                Object value = obj.get(key);
                if (value instanceof Boolean) {
                    PREFS.putBoolean(key, (Boolean) value);
                } else {
                    PREFS.put(key, value.toString());
                }
            }
            // Trigger theme update
            com.syndicati.utils.theme.ThemeManager.getInstance().reloadFromPreferences();
        } catch (Exception e) {
            System.err.println("Failed to sync settings from profile: " + e.getMessage());
        }
    }
}



