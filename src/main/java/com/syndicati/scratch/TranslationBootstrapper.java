package com.syndicati.scratch;

import com.syndicati.utils.localization.LocalizationManager;

/**
 * Scratch script to trigger the AI translation sync for the app UI.
 * This uses the Hugging Face API key to generate fr.json and ar.json from en.json.
 */
public class TranslationBootstrapper {
    public static void main(String[] args) {
        System.out.println("=== SYNDICATI TRANSLATION BOOTSTRAPPER ===");
        
        LocalizationManager manager = LocalizationManager.getInstance();
        
        System.out.println("[STEP 1] Starting Sync with Hugging Face (MarianMT)...");
        // This will create fr.json and ar.json based on en.json
        manager.syncTranslations("fr", "ar");
        
        System.out.println("[STEP 2] Sync Complete!");
        System.out.println("Check src/main/resources/lang/ for the new files.");
        System.exit(0);
    }
}
