package com.pidev.utils.theme;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.layout.Pane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Duration;
import com.pidev.utils.shared.AppPreferences;
import java.util.ArrayList;
import java.util.List;

/**
 * Theme Manager - Handles dark and light theme switching
 */
public class ThemeManager {
    
    private static ThemeManager instance;
    private boolean isDarkMode = true; // Start in dark mode
    private Scene currentScene;
    private BooleanProperty isDarkModeProperty = new SimpleBooleanProperty(isDarkMode);

    // Accent color – loaded from persistent prefs at startup
    private String accentColor = AppPreferences.DEFAULT_ACCENT_COLOR;
    private String accentGradient = AppPreferences.DEFAULT_ACCENT_GRADIENT;
    private StringProperty accentColorProperty = new SimpleStringProperty(accentColor);
    private boolean animatedAccents = AppPreferences.DEFAULT_ANIM_ACCENTS;

    // Listeners notified when accent or theme changes (used by SettingsView to refresh UI)
    private final List<Runnable> accentChangeListeners = new ArrayList<>();

    // Animated gradient – phase cycles 0→1 continuously when animatedAccents=true
    private final DoubleProperty gradientPhase = new SimpleDoubleProperty(0.0);
    private Timeline gradientTimeline;
    
    private ThemeManager() {
        // Load persisted preferences
        String savedTheme = AppPreferences.get(AppPreferences.KEY_THEME, AppPreferences.DEFAULT_THEME);
        this.isDarkMode = "dark".equals(savedTheme);
        this.isDarkModeProperty.set(isDarkMode);

        String savedAccent = AppPreferences.get(AppPreferences.KEY_ACCENT_COLOR, AppPreferences.DEFAULT_ACCENT_COLOR);
        this.accentColor = savedAccent;
        this.accentColorProperty.set(accentColor);

        String savedGradient = AppPreferences.get(
            AppPreferences.KEY_ACCENT_GRADIENT,
            buildGradientFromAccent(savedAccent)
        );
        this.accentGradient = savedGradient;

        this.animatedAccents = AppPreferences.getBoolean(AppPreferences.KEY_ANIM_ACCENTS, AppPreferences.DEFAULT_ANIM_ACCENTS);
        initGradientTimeline();
    }

    private void initGradientTimeline() {
        gradientTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(gradientPhase, 0.0, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(7.5), new KeyValue(gradientPhase, 1.0, Interpolator.EASE_BOTH))
        );
        gradientTimeline.setAutoReverse(true);
        gradientTimeline.setCycleCount(Animation.INDEFINITE);
        if (animatedAccents) gradientTimeline.play();
    }
    
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
        AppPreferences.set(AppPreferences.KEY_THEME, darkMode ? "dark" : "light");
        applyTheme();
        notifyAccentListeners();
    }
    
    public void toggleTheme() {
        this.isDarkMode = !this.isDarkMode;
        this.isDarkModeProperty.set(this.isDarkMode);
        AppPreferences.set(AppPreferences.KEY_THEME, isDarkMode ? "dark" : "light");
        System.out.println("🔄 ThemeManager: Theme toggled to " + (isDarkMode ? "Dark" : "Light") + " mode");
        applyTheme();
        notifyAccentListeners();
    }

    public void setDarkModePreference(boolean dark) {
        this.isDarkMode = dark;
        this.isDarkModeProperty.set(isDarkMode);
        AppPreferences.set(AppPreferences.KEY_THEME, isDarkMode ? "dark" : "light");
        applyTheme();
        notifyAccentListeners();
    }

    // ── Accent color ─────────────────────────────────────────────────────────
    public String getAccentHex() {
        return accentColor;
    }

    public StringProperty accentColorProperty() {
        return accentColorProperty;
    }

    public String getAccentGradient() {
        return accentGradient;
    }

    /**
     * Returns a Paint for filling Text nodes using the exact chosen swatch gradient.
     */
    public Paint getAccentGradientPaint() {
        try {
            return Paint.valueOf(accentGradient);
        } catch (Exception e) {
            try { return Color.web(accentColor); } catch (Exception ex) { return Color.PURPLE; }
        }
    }

    /**
     * Kept for compatibility – delegates to getAccentGradientPaint().
     */
    public Paint buildAccentLinearGradient(String ignoredHex) {
        return getAccentGradientPaint();
    }

    /**
     * Returns an animated CSS gradient string for -fx-background-color.
     * Keeps the exact chosen accent colors and gently drifts the gradient along
     * its diagonal instead of rotating it, which feels calmer and less chaotic.
     */
    public String getAnimatedAccentGradient() {
        double phase = gradientPhase.get();
        double drift = -16.0 + (phase * 32.0);
        double x1 = drift;
        double y1 = drift;
        double x2 = 100.0 + drift;
        double y2 = 100.0 + drift;

        // Preserve the original chosen color stops exactly; only the direction window moves.
        try {
            int firstComma = accentGradient.indexOf(",");
            if (firstComma < 0) return accentGradient;
            String stops = accentGradient.substring(firstComma);
            return String.format("linear-gradient(from %.1f%% %.1f%% to %.1f%% %.1f%%%s",
                x1, y1, x2, y2, stops);
        } catch (Exception e) {
            return accentGradient;
        }
    }

    /** Returns the gradient to use for CSS borders/backgrounds – animated if enabled */
    public String getEffectiveAccentGradient() {
        return animatedAccents ? getAnimatedAccentGradient() : accentGradient;
    }

    public void setAccentColor(String hex) {
        setAccentTheme(hex, buildGradientFromAccent(hex));
    }

    public void setAccentTheme(String hex, String gradient) {
        this.accentColor = hex;
        this.accentGradient = gradient;
        this.accentColorProperty.set(hex);
        AppPreferences.set(AppPreferences.KEY_ACCENT_COLOR, hex);
        AppPreferences.set(AppPreferences.KEY_ACCENT_GRADIENT, gradient);
        notifyAccentListeners();
    }

    // ── Animated accents ─────────────────────────────────────────────────────
    public boolean isAnimatedAccents() { return animatedAccents; }

    public void setAnimatedAccents(boolean value) {
        this.animatedAccents = value;
        AppPreferences.setBoolean(AppPreferences.KEY_ANIM_ACCENTS, value);
        if (gradientTimeline != null) {
            if (value) gradientTimeline.play();
            else { gradientTimeline.stop(); gradientPhase.set(0.0); }
        }
        notifyAccentListeners();
    }

    /** Phase property – bind a node's style to this to react to animation ticks */
    public DoubleProperty gradientPhaseProperty() { return gradientPhase; }

    // ── Change listeners ──────────────────────────────────────────────────────
    public void addAccentChangeListener(Runnable listener) {
        accentChangeListeners.add(listener);
    }

    public void removeAccentChangeListener(Runnable listener) {
        accentChangeListeners.remove(listener);
    }

    private void notifyAccentListeners() {
        accentChangeListeners.forEach(Runnable::run);
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
        return isDarkMode
            ? toRgba(accentColor, 0.45)
            : "rgba(255, 255, 255, 0.28)";
    }
    
    public String getTabHoverColor() {
        return isDarkMode ? toRgba(accentColor, 0.18) : "rgba(255, 255, 255, 0.18)";
    }
    
    public String getActiveTabColor() {
        return accentGradient;
    }
    
    public String getToggleTrackBackground() {
        return isDarkMode ? "rgba(0, 0, 0, 0.55)" : "rgba(241, 245, 249, 0.65)";
    }
    
    public String getToggleTrackBorder() {
        return isDarkMode ? toRgba(accentColor, 0.45) : "rgba(255, 255, 255, 0.35)";
    }
    
    public String getToggleTrackHover() {
        return isDarkMode ? toRgba(accentColor, 0.25) : "rgba(255, 255, 255, 0.25)";
    }
    
    // Dark liquid glass colors - blackish with transparency
    public String getLiquidGlassBackground() {
        // Dark glass base vs light glass base
        return isDarkMode ? "rgba(0, 0, 0, 0.35)" : "rgba(240, 244, 250, 0.55)";
    }
    
    public String getLiquidGlassBorder() {
        return isDarkMode ? toRgba(accentColor, 0.45) : "rgba(255, 255, 255, 0.3)";
    }
    
    public String getLiquidGlassHover() {
        return isDarkMode ? toRgba(accentColor, 0.20) : "rgba(255, 255, 255, 0.20)";
    }
    
    public String getLiquidGlassFocus() {
        return isDarkMode ? toRgba(accentColor, 0.32) : "rgba(255, 255, 255, 0.30)";
    }

    /** Convert a hex color to rgba(r, g, b, alpha) string */
    public String toRgba(String hex, double alpha) {
        try {
            Color c = Color.web(hex);
            return String.format("rgba(%d, %d, %d, %.2f)",
                    (int)(c.getRed() * 255),
                    (int)(c.getGreen() * 255),
                    (int)(c.getBlue() * 255),
                    alpha);
        } catch (Exception e) {
            return "rgba(255, 46, 46, " + alpha + ")";
        }
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
        // Use persisted accent; in light mode keep accent visible (not white)
        return accentColor;
    }

    public String getModernSecondaryColor() {
        // Derived darker shade – keep simple
        return isDarkMode ? deriveColor(accentColor, 0.8) : deriveColor(accentColor, 0.9);
    }

    /** Naively darken a hex color by multiplying RGB channels by factor (0..1) */
    private String deriveColor(String hex, double factor) {
        try {
            Color c = Color.web(hex);
            int r = (int)(c.getRed()   * factor * 255);
            int g = (int)(c.getGreen() * factor * 255);
            int b = (int)(c.getBlue()  * factor * 255);
            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) {
            return hex;
        }
    }

    private String buildGradientFromAccent(String hex) {
        try {
            Color base = Color.web(hex);
            Color bright = base.deriveColor(0, 1.0, 1.28, 1.0);
            Color deep = base.deriveColor(0, 1.0, 0.78, 1.0);
            return String.format(
                "linear-gradient(from 0%% 0%% to 100%% 100%%, %s 0%%, %s 52%%, %s 100%%)",
                toHex(bright),
                toHex(base),
                toHex(deep)
            );
        } catch (Exception e) {
            return AppPreferences.DEFAULT_ACCENT_GRADIENT;
        }
    }

    private String toHex(Color color) {
        return String.format(
            "#%02x%02x%02x",
            (int) Math.round(color.getRed() * 255),
            (int) Math.round(color.getGreen() * 255),
            (int) Math.round(color.getBlue() * 255)
        );
    }

    // Glow colors derived from current accent
    public Color getNeonGlowColor() {
        try { return Color.web(accentColor); } catch (Exception e) { return Color.web("#FF2E2E"); }
    }

    public Color getAmberGlowColor() {
        try { return Color.web(deriveColor(accentColor, 0.8)); } catch (Exception e) { return Color.web("#CC1F1F"); }
    }

    // Island-specific text colors (header/footer) to ensure contrast on black island in light mode
    public String getIslandTextColor() {
        return "#FFFFFF";
    }

    public String getIslandSecondaryTextColor() {
        return "rgba(255, 255, 255, 0.75)";
    }
}