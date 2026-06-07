package com.syndicati;

import com.syndicati.utils.config.EnvConfig;

/**
 * Simple launcher class for IntelliJ IDEA
 * This class can be easily run as a Java application in IntelliJ
 */
public class Launcher {
    
    public static void main(String[] args) {
        System.out.println("[START] Starting Syndicati Desktop...");
        System.out.println("[INFO] JavaFX Application Launcher");
        EnvConfig.bootstrapSystemProperties();
        
        // Launch the main JavaFX application
        MainApplication.main(args);
    }
}



