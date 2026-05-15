package com.syndicati.utils.ui;

import com.syndicati.utils.theme.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public final class HorizonDesignSystem {
    private static final ThemeManager TM = ThemeManager.getInstance();
    public static final Interpolator WEB_EASE = Interpolator.SPLINE(0.4, 0.0, 0.2, 1.0);
    public static final Interpolator WEB_POP = Interpolator.SPLINE(0.16, 1.0, 0.3, 1.0);

    private HorizonDesignSystem() {}

    public static String pageOverlay() {
        return TM.isDarkMode()
            ? "rgba(5, 7, 12, 0.78)"
            : "rgba(248, 250, 252, 0.82)";
    }

    public static String surface() {
        return TM.isDarkMode()
            ? "rgba(0, 0, 0, 0.88)"
            : "rgba(255, 255, 255, 0.96)";
    }

    public static String surfaceSoft() {
        return TM.isDarkMode()
            ? "rgba(255, 255, 255, 0.045)"
            : "rgba(15, 23, 42, 0.055)";
    }

    public static String border() {
        return TM.isDarkMode()
            ? "rgba(255, 255, 255, 0.12)"
            : "rgba(15, 23, 42, 0.12)";
    }

    public static String borderStrong() {
        return TM.isDarkMode()
            ? TM.toRgba(TM.getAccentHex(), 0.34)
            : "rgba(15, 23, 42, 0.18)";
    }

    public static String text() {
        return TM.isDarkMode() ? "#ffffff" : "#0f172a";
    }

    public static String mutedText() {
        return TM.isDarkMode() ? "rgba(255, 255, 255, 0.64)" : "rgba(15, 23, 42, 0.62)";
    }

    public static String card(int radius) {
        return webSectionCard(radius, false);
    }

    public static String cardAccent(int radius) {
        return "-fx-background-color: " + TM.getEffectiveAccentGradient() + ", " + surface() + ";" +
            "-fx-background-insets: 0, 1px;" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-color: " + borderStrong() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: " + radius + "px;" +
            "-fx-effect: dropshadow(gaussian, " + shadowColor(0.38) + ", 32, 0.20, 0, 12);";
    }

    public static String webHeroPanel() {
        return "-fx-background-color: #000000;" +
            "-fx-background-radius: 48px;" +
            "-fx-border-color: " + (TM.isDarkMode() ? "rgba(255, 255, 255, 0.08)" : "rgba(15, 23, 42, 0.10)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 48px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.80), 86, 0.22, 0, 34);";
    }

    public static String webServiceCard(int radius, boolean hover) {
        String bg = TM.isDarkMode() ? (hover ? "rgba(5,5,10,0.75)" : "rgba(0,0,0,0.70)") : "#ffffff";
        String border = hover ? TM.toRgba(TM.getAccentHex(), 0.40) : (TM.isDarkMode() ? "rgba(255,255,255,0.10)" : "rgba(226,232,240,1.0)");
        String shadow = TM.isDarkMode()
            ? (hover ? "rgba(0,0,0,0.60)" : "rgba(0,0,0,0.50)")
            : "rgba(15,23,42,0.08)";
        return "-fx-background-color: " + bg + ";" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-color: " + border + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: " + radius + "px;" +
            "-fx-effect: dropshadow(gaussian, " + shadow + ", " + (hover ? 60 : 35) + ", 0.18, 0, " + (hover ? 24 : 14) + ");";
    }

    public static String webObsidianPanel(int radius) {
        return "-fx-background-color: " + (TM.isDarkMode() ? "#0a0a0c" : "#ffffff") + ";" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-color: " + (TM.isDarkMode() ? "rgba(255,255,255,0.08)" : "rgba(226,232,240,1.0)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: " + radius + "px;" +
            "-fx-effect: dropshadow(gaussian, " + (TM.isDarkMode() ? "rgba(0,0,0,0.60)" : "rgba(15,23,42,0.08)") + ", 60, 0.20, 0, 20);";
    }

    public static String webServiceInput(int radius, boolean focused) {
        return "-fx-background-color: " + (TM.isDarkMode() ? (focused ? "rgba(0,0,0,0.40)" : "rgba(0,0,0,0.20)") : "rgba(255,255,255,0.92)") + ";" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-color: " + (focused ? TM.getAccentHex() : (TM.isDarkMode() ? "rgba(255,255,255,0.10)" : "rgba(15,23,42,0.14)")) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: " + radius + "px;" +
            "-fx-text-fill: " + text() + ";" +
            "-fx-prompt-text-fill: " + (TM.isDarkMode() ? "rgba(255,255,255,0.34)" : "rgba(15,23,42,0.42)") + ";" +
            "-fx-padding: 14 22 14 22;" +
            (focused ? "-fx-effect: dropshadow(gaussian, " + TM.toRgba(TM.getAccentHex(), 0.18) + ", 16, 0.20, 0, 0);" : "");
    }

    public static String webAccentBadge() {
        return "-fx-background-color: " + TM.toRgba(TM.getAccentHex(), 0.13) + ";" +
            "-fx-background-radius: 999px;" +
            "-fx-border-color: " + TM.toRgba(TM.getAccentHex(), 0.34) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 999px;" +
            "-fx-effect: dropshadow(gaussian, " + TM.toRgba(TM.getAccentHex(), 0.16) + ", 18, 0.18, 0, 0);";
    }

    public static String accentRgba(double alpha) {
        return TM.toRgba(TM.getAccentHex(), alpha);
    }

    public static String buttonPrimaryBackground() {
        return TM.getEffectiveAccentGradient();
    }

    public static String webFeatureCard(int radius) {
        return "-fx-background-color: " + (TM.isDarkMode() ? "rgba(0, 0, 0, 0.78)" : "rgba(245, 245, 245, 0.86)") + ";" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-color: " + TM.toRgba(TM.getAccentHex(), TM.isDarkMode() ? 0.30 : 0.18) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: " + radius + "px;" +
            "-fx-effect: dropshadow(gaussian, " + (TM.isDarkMode() ? TM.toRgba(TM.getAccentHex(), 0.22) : "rgba(15, 23, 42, 0.12)") + ", 28, 0.18, 0, 10);";
    }

    public static String webDashboardPanel() {
        return "-fx-background-color: " + (TM.isDarkMode() ? "rgba(5, 5, 10, 0.86)" : "rgba(255, 255, 255, 0.92)") + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + (TM.isDarkMode() ? "rgba(255,255,255,0.07)" : "rgba(15, 23, 42, 0.12)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;" +
            "-fx-effect: dropshadow(gaussian, " + (TM.isDarkMode() ? "rgba(0, 0, 0, 0.72)" : "rgba(15, 23, 42, 0.12)") + ", 72, 0.22, 0, 26);";
    }

    public static String webAuthCard(boolean hover) {
        ThemeManager tm = TM;
        double borderAlpha = hover ? 0.35 : 0.18;
        double shadowAlpha = hover ? 0.86 : 0.78;
        return "-fx-background-color: " + (tm.isDarkMode()
                ? "rgba(10, 10, 10, 0.85)"
                : "rgba(255, 255, 255, 0.94)") + ";" +
            "-fx-background-radius: 28px;" +
            "-fx-border-color: " + (tm.isDarkMode()
                ? "rgba(255, 255, 255, " + borderAlpha + ")"
                : "rgba(15, 23, 42, 0.12)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 28px;" +
            "-fx-effect: dropshadow(gaussian, " + (tm.isDarkMode()
                ? "rgba(0, 0, 0, " + shadowAlpha + ")"
                : "rgba(15, 23, 42, 0.16)") + ", " + (hover ? 100 : 82) + ", 0.22, 0, " + (hover ? 34 : 28) + ");";
    }

    public static String webFloatingIsland(int radius, boolean hover) {
        double borderAlpha = hover ? 0.20 : 0.10;
        return "-fx-background-color: " + (TM.isDarkMode() ? "rgba(0, 0, 0, 0.82)" : "rgba(255, 255, 255, 0.94)") + ";" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-color: " + (TM.isDarkMode() ? "rgba(255, 255, 255, " + borderAlpha + ")" : "rgba(15, 23, 42, 0.10)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: " + radius + "px;" +
            "-fx-effect: dropshadow(gaussian, " + (TM.isDarkMode() ? "rgba(0,0,0,0.72)" : "rgba(15,23,42,0.12)") + ", " + (hover ? 72 : 44) + ", 0.22, 0, " + (hover ? 22 : 14) + ");";
    }

    public static String webDashboardGlass(int radius, boolean hover) {
        return "-fx-background-color: " + (TM.isDarkMode() ? "rgba(5, 5, 10, 0.86)" : "rgba(255,255,255,0.92)") + ";" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-color: " + (TM.isDarkMode() ? "rgba(255,255,255," + (hover ? "0.11" : "0.07") + ")" : "rgba(15,23,42,0.12)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: " + radius + "px;" +
            "-fx-effect: dropshadow(gaussian, " + (TM.isDarkMode() ? "rgba(0,0,0,0.70)" : "rgba(15,23,42,0.12)") + ", " + (hover ? 82 : 58) + ", 0.20, 0, " + (hover ? 28 : 20) + ");";
    }

    public static String webProfileCard() {
        return webSectionCard(18, false);
    }

    public static String webSectionCard(int radius, boolean hover) {
        double accentAlpha = hover ? 0.20 : 0.13;
        double borderAlpha = hover ? 0.46 : 0.28;
        String bg = TM.isDarkMode()
            ? "radial-gradient(focus-angle 28deg, focus-distance 24%, center 14% 10%, radius 132%, " + TM.toRgba(TM.getAccentHex(), accentAlpha) + " 0%, rgba(10, 10, 15, 0.88) 60%, rgba(0, 0, 0, 0.94) 100%), " +
                "linear-gradient(to bottom right, rgba(255,255,255,0.055), rgba(255,255,255,0.014) 48%, " + TM.toRgba(TM.getAccentHex(), hover ? 0.09 : 0.05) + " 100%)"
            : "linear-gradient(to bottom right, rgba(255,255,255,0.98), rgba(255,255,255,0.90) 54%, rgba(243,247,255,0.96) 100%)";

        return "-fx-background-color: " + bg + ";" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-color: " + TM.toRgba(TM.getAccentHex(), borderAlpha) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: " + radius + "px;" +
            "-fx-effect: dropshadow(gaussian, " + (TM.isDarkMode() ? "rgba(0,0,0,0.55)" : "rgba(15,23,42,0.12)") + ", " + (hover ? 38 : 28) + ", 0.18, 0, " + (hover ? 16 : 10) + ");";
    }

    public static String webIconTile(int radius) {
        return "-fx-background-color: " + TM.toRgba(TM.getAccentHex(), TM.isDarkMode() ? 0.18 : 0.10) + ";" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-color: " + TM.toRgba(TM.getAccentHex(), 0.32) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: " + radius + "px;";
    }

    public static String pill(boolean active) {
        String bg = active ? TM.getEffectiveAccentGradient() : surfaceSoft();
        String fg = active ? "#ffffff" : text();
        String b = active ? "transparent" : border();
        return "-fx-background-color: " + bg + ";" +
            "-fx-background-radius: 999px;" +
            "-fx-border-color: " + b + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 999px;" +
            "-fx-text-fill: " + fg + ";" +
            "-fx-font-weight: 800;" +
            "-fx-cursor: hand;";
    }

    public static String buttonPrimary() {
        return "-fx-background-color: " + TM.getEffectiveAccentGradient() + ";" +
            "-fx-background-radius: 14px;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-weight: 800;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, " + TM.toRgba(TM.getAccentHex(), 0.38) + ", 18, 0.22, 0, 6);";
    }

    public static String buttonGhost() {
        return "-fx-background-color: " + (TM.isDarkMode() ? "rgba(255,255,255,0.065)" : "rgba(15,23,42,0.055)") + ";" +
            "-fx-background-radius: 14px;" +
            "-fx-border-color: " + borderStrong() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 14px;" +
            "-fx-text-fill: " + text() + ";" +
            "-fx-font-weight: 700;" +
            "-fx-cursor: hand;";
    }

    public static String input() {
        return "-fx-background-color: " + (TM.isDarkMode() ? "rgba(0,0,0,0.62)" : "rgba(255,255,255,0.92)") + ";" +
            "-fx-background-radius: 14px;" +
            "-fx-border-color: " + borderStrong() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 14px;" +
            "-fx-text-fill: " + text() + ";" +
            "-fx-prompt-text-fill: " + mutedText() + ";" +
            "-fx-padding: 10 13 10 13;";
    }

    public static void styleScrollPane(ScrollPane scrollPane) {
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;" + TM.getScrollbarVariableStyle());
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    public static void installLift(Node node) {
        installLift(node, 1.012, -3);
    }

    public static void installLift(Node node, double scale, double y) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(140), node);
            st.setToX(scale);
            st.setToY(scale);
            st.setInterpolator(WEB_EASE);
            TranslateTransition tt = new TranslateTransition(Duration.millis(140), node);
            tt.setToY(y);
            tt.setInterpolator(WEB_EASE);
            st.play();
            tt.play();
        });
        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
            st.setToX(1);
            st.setToY(1);
            st.setInterpolator(WEB_EASE);
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), node);
            tt.setToY(0);
            tt.setInterpolator(WEB_EASE);
            st.play();
            tt.play();
        });
    }

    public static void installButtonMotion(ButtonBase button) {
        button.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(70), button);
            st.setToX(0.985);
            st.setToY(0.985);
            st.setInterpolator(WEB_EASE);
            st.play();
        });
        button.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(110), button);
            st.setToX(1);
            st.setToY(1);
            st.setInterpolator(WEB_EASE);
            st.play();
        });
    }

    public static void installWebLift(Node node) {
        installLift(node, 1.01, -8);
    }

    public static void installIslandMotion(Node node) {
        node.setOnMouseEntered(e -> animateNode(node, 1.0, 1.0, -2, 1, 260, WEB_EASE));
        node.setOnMouseExited(e -> animateNode(node, 1.0, 1.0, 0, 1, 260, WEB_EASE));
    }

    public static void popIn(Node node) {
        node.setOpacity(0);
        node.setScaleX(0.96);
        node.setScaleY(0.96);
        node.setTranslateY(-10);
        animateNode(node, 1.0, 1.0, 0, 1, 300, WEB_POP);
    }

    public static void dropdownIn(Node node) {
        node.setOpacity(0);
        node.setScaleX(0.98);
        node.setScaleY(0.98);
        node.setTranslateY(-10);
        animateNode(node, 1.0, 1.0, 0, 1, 260, WEB_EASE);
    }

    public static void dropdownOut(Node node, Runnable after) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(160),
                new KeyValue(node.scaleXProperty(), 0.98, WEB_EASE),
                new KeyValue(node.scaleYProperty(), 0.98, WEB_EASE),
                new KeyValue(node.translateYProperty(), -8, WEB_EASE),
                new KeyValue(node.opacityProperty(), 0, WEB_EASE)
            )
        );
        timeline.setOnFinished(e -> {
            if (after != null) {
                after.run();
            }
            node.setScaleX(1);
            node.setScaleY(1);
            node.setTranslateY(0);
        });
        timeline.play();
    }

    public static void activePulse(Node node) {
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.scaleXProperty(), 1.0),
                new KeyValue(node.scaleYProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(170),
                new KeyValue(node.scaleXProperty(), 1.025, WEB_POP),
                new KeyValue(node.scaleYProperty(), 1.025, WEB_POP)
            ),
            new KeyFrame(Duration.millis(320),
                new KeyValue(node.scaleXProperty(), 1.0, WEB_EASE),
                new KeyValue(node.scaleYProperty(), 1.0, WEB_EASE)
            )
        );
        pulse.play();
    }

    private static void animateNode(Node node, double scaleX, double scaleY, double translateY, double opacity, int millis, Interpolator interpolator) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(millis),
                new KeyValue(node.scaleXProperty(), scaleX, interpolator),
                new KeyValue(node.scaleYProperty(), scaleY, interpolator),
                new KeyValue(node.translateYProperty(), translateY, interpolator),
                new KeyValue(node.opacityProperty(), opacity, interpolator)
            )
        );
        timeline.play();
    }

    public static void installFocusGlow(Control control) {
        control.focusedProperty().addListener((obs, oldValue, focused) -> {
            if (focused) {
                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.web(TM.toRgba(TM.getAccentHex(), 0.40)));
                shadow.setRadius(18);
                shadow.setSpread(0.05);
                control.setEffect(shadow);
            } else {
                control.setEffect(null);
            }
        });
    }

    public static void fadeIn(Node node) {
        node.setOpacity(0);
        node.setTranslateY(8);
        FadeTransition fade = new FadeTransition(Duration.millis(260), node);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(260), node);
        slide.setToY(0);
        fade.play();
        slide.play();
    }

    private static String shadowColor(double alpha) {
        return TM.isDarkMode()
            ? "rgba(0, 0, 0, " + alpha + ")"
            : "rgba(15, 23, 42, " + Math.min(alpha, 0.16) + ")";
    }
}
