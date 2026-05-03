package com.syndicati.utils.localization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.syndicati.services.localization.MyMemoryTranslationService;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for application localization and multi-language support.
 * Handles JSON-based translation dictionaries and RTL orientation for Arabic.
 */
public class LocalizationManager {

    private static LocalizationManager instance;
    private static final String LANG_DIR = "src/main/resources/lang/";
    
    private String currentLanguage = "en";
    private final Map<String, String> translations = new ConcurrentHashMap<>();
    private final Map<String, String> fallbackEnglish = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final MyMemoryTranslationService translationService;

    private LocalizationManager() {
        this.translationService = new MyMemoryTranslationService();
        ensureLangDirectory();
        loadFallbackEnglish();
        loadLanguage(currentLanguage);
    }

    private void loadFallbackEnglish() {
        File file = new File(LANG_DIR + "en.json");
        if (file.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> loaded = gson.fromJson(reader, type);
                if (loaded != null) {
                    fallbackEnglish.putAll(loaded);
                }
            } catch (IOException e) {
                System.err.println("Error loading fallback English: " + e.getMessage());
            }
        }
    }

    public static synchronized LocalizationManager getInstance() {
        if (instance == null) {
            instance = new LocalizationManager();
        }
        return instance;
    }

    private void ensureLangDirectory() {
        File dir = new File(LANG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Loads a language dictionary from a JSON file.
     * 
     * @param langCode The language code (en, fr, ar).
     */
    public void loadLanguage(String langCode) {
        this.currentLanguage = langCode.toLowerCase();
        String filePath = LANG_DIR + currentLanguage + ".json";
        File file = new File(filePath);

        if (file.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> loaded = gson.fromJson(reader, type);
                if (loaded != null) {
                    translations.clear();
                    translations.putAll(loaded);
                }
            } catch (IOException e) {
                System.err.println("Error loading language file: " + e.getMessage());
            }
        } else {
            // Fallback: Clear current translations so we don't keep the previous language's text
            translations.clear();
            if ("en".equals(currentLanguage)) {
                saveLanguage();
            }
        }
    }

    /**
     * Saves the current translations to the JSON file.
     */
    public void saveLanguage() {
        String filePath = LANG_DIR + currentLanguage + ".json";
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            gson.toJson(translations, writer);
        } catch (IOException e) {
            System.err.println("Error saving language file: " + e.getMessage());
        }
    }

    /**
     * Gets a translated string for a given key.
     * 
     * @param key The translation key.
     * @return The translated value, or the key itself if not found.
     */
    public String get(String key) {
        return translations.getOrDefault(key, fallbackEnglish.getOrDefault(key, key));
    }

    /**
     * Sets a translation value and saves it.
     */
    public void set(String key, String value) {
        translations.put(key, value);
        saveLanguage();
    }

    /**
     * Automatically translates all keys from the English dictionary to target languages.
     * Uses the Hugging Face service.
     */
    /**
     * Automatically translates all keys from the English dictionary to target languages.
     * Processes in parallel for high performance without affecting the current UI state.
     */
    public void syncTranslations(String... targetLanguages) {
        // 1. Load English source dictionary independently
        Map<String, String> englishSource = loadDictionaryDirectly("en");
        if (englishSource.isEmpty()) return;

        for (String target : targetLanguages) {
            if ("en".equalsIgnoreCase(target)) continue;

            System.out.println("[INFO] Async Syncing: " + target);
            Map<String, String> targetMap = loadDictionaryDirectly(target);
            
            java.util.concurrent.atomic.AtomicBoolean changed = new java.util.concurrent.atomic.AtomicBoolean(false);
            
            // Use parallel streams for high-speed translation across multiple virtual threads
            englishSource.entrySet().parallelStream().forEach(entry -> {
                String currentVal = targetMap.get(entry.getKey());
                // Only translate if missing or untranslated (matches key/source)
                if (currentVal == null || currentVal.equals(entry.getKey()) || currentVal.equals(entry.getValue())) {
                    try {
                        String translated = translationService.translate(entry.getValue(), target);
                        if (translated != null && !translated.equals(entry.getValue())) {
                            targetMap.put(entry.getKey(), translated);
                            changed.set(true);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to translate key " + entry.getKey() + ": " + e.getMessage());
                    }
                }
            });

            if (changed.get()) {
                saveDictionaryDirectly(target, targetMap);
                // If the user IS currently on this language, reload it silently
                if (target.equalsIgnoreCase(currentLanguage)) {
                    javafx.application.Platform.runLater(() -> {
                        translations.putAll(targetMap);
                        // Optional: trigger UI component refresh if needed
                    });
                }
            }
        }
    }

    private Map<String, String> loadDictionaryDirectly(String langCode) {
        String filePath = LANG_DIR + langCode.toLowerCase() + ".json";
        File file = new File(filePath);
        if (file.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                Type type = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> loaded = gson.fromJson(reader, type);
                return loaded != null ? new ConcurrentHashMap<>(loaded) : new ConcurrentHashMap<>();
            } catch (IOException e) {
                System.err.println("Error loading " + langCode + ": " + e.getMessage());
            }
        }
        return new ConcurrentHashMap<>();
    }

    private void saveDictionaryDirectly(String langCode, Map<String, String> map) {
        String filePath = LANG_DIR + langCode.toLowerCase() + ".json";
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8)) {
            gson.toJson(map, writer);
            System.out.println("[SUCCESS] Saved " + langCode + " dictionary.");
        } catch (IOException e) {
            System.err.println("Error saving " + langCode + ": " + e.getMessage());
        }
    }

    /**
     * Applies the correct orientation to a UI node based on the current language.
     * 
     * @param node The node to orient.
     */
    public void applyOrientation(Node node) {
        if (node == null) return;
        if ("ar".equalsIgnoreCase(currentLanguage)) {
            node.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        } else {
            node.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public boolean isRTL() {
        return "ar".equalsIgnoreCase(currentLanguage);
    }
}
