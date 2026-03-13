package com.pidev.utils.theme;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Theme Manager - Handles dark and light theme switching
 */
public class ThemeManager {
    
    private static ThemeManager instance;
    private boolean isDarkMode = true; // Start in dark mode
    private Scene currentScene;
    private BooleanProperty isDarkModeProperty = new SimpleBooleanProperty(isDarkMode);
    
    private ThemeManager() {}
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    public boolean isDarkMode() {
        return isDarkMode;
    }
    
    public void setDarkMode(boolean darkMode) {
        this.isDarkMode = darkMode;
        this.isDarkModeProperty.set(darkMode);
        applyTheme();
    }
    
    public void toggleTheme() {
        this.isDarkMode = !this.isDarkMode;
        this.isDarkModeProperty.set(this.isDarkMode);
        System.out.println("🔄 ThemeManager: Theme toggled to " + (isDarkMode ? "Dark" : "Light") + " mode");
        applyTheme();
    }
    
    public BooleanProperty isDarkModeProperty() {
        return isDarkModeProperty;
    }
    
    public void setScene(Scene scene) {
        this.currentScene = scene;
        applyTheme();
    }
    
    private void applyTheme() {
        if (currentScene == null) return;
        
        if (isDarkMode) {
            applyDarkTheme();
        } else {
            applyLightTheme();
        }
    }
    
    private void applyDarkTheme() {
        // Window uses custom rounded chrome; keep scene transparent so corners show through
        // Previously: currentScene.setFill(Color.web("#000000"));
        currentScene.setFill(Color.TRANSPARENT);
        
        // Don't apply background color to root pane - let ImageBackground handle it
        // Pane root = (Pane) currentScene.getRoot();
        // root.setStyle("-fx-background-color: #0a0b0f;");
        
        System.out.println("🌙 Dark theme applied (transparent scene; backgrounds handled by views)");
    }
    
    private void applyLightTheme() {
        // Keep scene transparent in light mode as well to preserve rounded window
        // Previously: currentScene.setFill(Color.web("#f8fafc"));
        currentScene.setFill(Color.TRANSPARENT);
        
        // Don't apply background color to root pane - let ImageBackground handle it
        // Pane root = (Pane) currentScene.getRoot();
        // root.setStyle(
        //     "-fx-background-color: linear-gradient(to bottom, #ffffff 0%, #f8fafc 100%);"
        // );
        
        System.out.println("☀️ Modern light theme applied (transparent scene; backgrounds handled by views)");
    }
    
    public String getBackgroundColor() {
        // Dark = black page; Light = bright page background
        return isDarkMode ? "#000000" : "linear-gradient(to bottom, #ffffff 0%, #f8fafc 100%)";
    }
    
    public String getTextColor() {
        // Dark: white text; Light: slate/dark text for readability on white
        return isDarkMode ? "#f8fafc" : "#1e293b";
    }
    
    public String getSecondaryTextColor() {
        return isDarkMode ? "#cbd5e1" : "#64748b";
    }
    
    public String getDynamicIslandBackground() {
        // Dynamic island surface: dark = glassy black; light = solid/translucent black for "black suit" look
        return isDarkMode ? "rgba(0, 0, 0, 0.55)" : "rgba(0, 0, 0, 0.85)";
    }
    
    public String getDynamicIslandBorder() {
        // TRON ARES: red trim in dark, subtle slate/white in light
        return isDarkMode ? "rgba(255, 46, 46, 0.45)" : "rgba(255, 255, 255, 0.28)";
    }
    
    public String getTabHoverColor() {
        // Subtle red hover on dark, white LED hover on light
        return isDarkMode ? "rgba(255, 46, 46, 0.18)" : "rgba(255, 255, 255, 0.18)";
    }
    
    public String getActiveTabColor() {
        // Red glow in dark, white LED in light
        return isDarkMode ? "rgba(255, 46, 46, 0.28)" : "rgba(255, 255, 255, 0.22)";
    }
    
    public String getToggleTrackBackground() {
        return isDarkMode ? "rgba(0, 0, 0, 0.55)" : "rgba(241, 245, 249, 0.65)";
    }
    
    public String getToggleTrackBorder() {
        return isDarkMode ? "rgba(255, 46, 46, 0.45)" : "rgba(255, 255, 255, 0.35)";
    }
    
    public String getToggleTrackHover() {
        return isDarkMode ? "rgba(255, 46, 46, 0.25)" : "rgba(255, 255, 255, 0.25)";
    }
    
    // Dark liquid glass colors - blackish with transparency
    public String getLiquidGlassBackground() {
        // Dark glass base vs light glass base
        return isDarkMode ? "rgba(0, 0, 0, 0.35)" : "rgba(240, 244, 250, 0.55)";
    }
    
    public String getLiquidGlassBorder() {
        return isDarkMode ? "rgba(255, 46, 46, 0.45)" : "rgba(255, 255, 255, 0.3)";
    }
    
    public String getLiquidGlassHover() {
        return isDarkMode ? "rgba(255, 46, 46, 0.20)" : "rgba(255, 255, 255, 0.20)";
    }
    
    public String getLiquidGlassFocus() {
        return isDarkMode ? "rgba(255, 46, 46, 0.32)" : "rgba(255, 255, 255, 0.30)";
    }
    
    // Dark red liquid glass colors for sign up button
    public String getDarkRedLiquidGlassBackground() {
        return "rgba(139, 0, 0, 0.3)"; // Dark red with same opacity as black
    }
    
    public String getDarkRedLiquidGlassBorder() {
        return "rgba(255, 255, 255, 0.2)"; // Same white border
    }
    
    public String getDarkRedLiquidGlassHover() {
        return "rgba(139, 0, 0, 0.4)"; // Darker red on hover
    }
    
    public String getDarkRedLiquidGlassFocus() {
        return "rgba(139, 0, 0, 0.5)"; // Darkest red on focus
    }
    
    public String getModernAccentColor() {
        // TRON ARES: red in dark, white LED in light
        return isDarkMode ? "#FF2E2E" : "#FFFFFF";
    }
    
    public String getModernSecondaryColor() {
        // Secondary accent: deeper red in dark, cool white in light
        return isDarkMode ? "#CC1F1F" : "#E5E7EB";
    }

    // TRON glow colors for effects
    public Color getNeonGlowColor() {
        return isDarkMode ? Color.web("#FF2E2E") : Color.web("#FFFFFF");
    }

    public Color getAmberGlowColor() {
        return isDarkMode ? Color.web("#CC1F1F") : Color.web("#E5E7EB");
    }

    // Island-specific text colors (header/footer) to ensure contrast on black island in light mode
    public String getIslandTextColor() {
        return "#FFFFFF";
    }

    public String getIslandSecondaryTextColor() {
        return "rgba(255, 255, 255, 0.75)";
    }
}