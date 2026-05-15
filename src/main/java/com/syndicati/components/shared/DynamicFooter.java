package com.syndicati.components.shared;

import javafx.beans.value.ChangeListener;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlurType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.*;
import javafx.util.Duration;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.ui.HorizonDesignSystem;

/**
 * Dynamic Footer Component - Dynamic island design footer
 */
public class DynamicFooter {
    
    private final HBox root;
    private final StackPane wrapper;
    private final Region glowLayer;
    private ChangeListener<Number> gradientPhaseListener;
    
    public DynamicFooter() {
        this.wrapper = new StackPane();
        this.glowLayer = new Region();
        this.root = new HBox();
        setupLayout();
        startAnimations();
        startBorderAnimation();
    }

    private void startBorderAnimation() {
        gradientPhaseListener = (obs, oldVal, newVal) -> applyThemeStyling();
        ThemeManager.getInstance().gradientPhaseProperty().addListener(gradientPhaseListener);
    }
    
    private void setupLayout() {
        root.getChildren().clear();

        // Main footer container with dynamic island styling
        root.setSpacing(0);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(18, 28, 18, 28));
        root.setMinHeight(72);
        root.setPrefHeight(72);
        root.setMaxWidth(Double.MAX_VALUE);
        
        // Apply dynamic island background with theme-aware colors
        applyThemeStyling();
        
        // Add root to wrapper (guard against duplicate on refreshTheme calls)
        glowLayer.setMouseTransparent(true);
        glowLayer.setOpacity(0);
        glowLayer.prefWidthProperty().bind(root.widthProperty());
        glowLayer.prefHeightProperty().bind(root.heightProperty());
        glowLayer.setStyle(glowStyle());
        if (!wrapper.getChildren().contains(glowLayer)) {
            wrapper.getChildren().add(glowLayer);
        }
        if (!wrapper.getChildren().contains(root)) {
            wrapper.getChildren().add(root);
        }
        wrapper.setPadding(new Insets(22, 30, 10, 30));
        
        // Footer content container
        HBox footerContent = new HBox();
        footerContent.setSpacing(20);
        footerContent.setAlignment(Pos.CENTER);
        footerContent.setMaxWidth(Double.MAX_VALUE);
        
        // Left side - web footer copy
        HBox leftSection = new HBox(8);
        leftSection.setAlignment(Pos.CENTER_LEFT);
        
        Text copyrightText = new Text("Copyright " + java.time.Year.now().getValue());
        copyrightText.setFont(javafx.scene.text.Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), javafx.scene.text.FontWeight.NORMAL, 13));
        copyrightText.setFill(Color.web(ThemeManager.getInstance().getIslandSecondaryTextColor()));

        Text loveText1 = new Text("Made with");
        loveText1.setFont(javafx.scene.text.Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), javafx.scene.text.FontWeight.NORMAL, 13));
        loveText1.setFill(Color.web(ThemeManager.getInstance().getIslandSecondaryTextColor()));
        
        Text heart = new Text("+");
        heart.setFont(javafx.scene.text.Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), javafx.scene.text.FontWeight.BOLD, 16));
        heart.setFill(Color.web("#ff3b30"));
        
        Text byText = new Text("by");
        byText.setFont(javafx.scene.text.Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), javafx.scene.text.FontWeight.NORMAL, 13));
        byText.setFill(Color.web(ThemeManager.getInstance().getIslandSecondaryTextColor()));

        Text loveText2 = new Text("Mohamed Amine Essaidi");
        loveText2.setFont(javafx.scene.text.Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), javafx.scene.text.FontWeight.SEMI_BOLD, 13));
        loveText2.setFill(Color.web(ThemeManager.getInstance().getModernAccentColor()));
        
        leftSection.getChildren().addAll(copyrightText, loveText1, heart, byText, loveText2);
        
        // Right side - Quick links
        HBox rightSection = new HBox(16);
        rightSection.setAlignment(Pos.CENTER_RIGHT);
        rightSection.getChildren().addAll(
            createFooterLink("Docs", "Documentation"),
            createFooterLink("Help", "Support"),
            createFooterLink("Code", "GitHub"),
            createFooterLink("Legal", "License")
        );
        
        // Add sections to footer content
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footerContent.getChildren().addAll(leftSection, spacer, rightSection);
        
        // Set grow priorities to push content to edges
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        HBox.setHgrow(rightSection, Priority.ALWAYS);
        
        // Force alignment to edges
        leftSection.setAlignment(Pos.CENTER_LEFT);
        rightSection.setAlignment(Pos.CENTER_RIGHT);
        
        root.getChildren().add(footerContent);
        installIslandHover();
    }
    
    private HBox createFooterLink(String icon, String text) {
        HBox link = new HBox(8);
        link.setAlignment(Pos.CENTER);
        link.setPadding(new Insets(7, 12, 7, 12));
        link.setCursor(javafx.scene.Cursor.HAND);
        ThemeManager themeManager = ThemeManager.getInstance();
        boolean dark = themeManager.isDarkMode();
        String baseText = dark ? themeManager.getIslandSecondaryTextColor() : "#334155";
        String hoverText = "#ffffff";
        String hoverBg = themeManager.getEffectiveAccentGradient();
        
        // Link icon with theme-aware styling
        Text linkIcon = new Text(icon);
        linkIcon.setFont(javafx.scene.text.Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), javafx.scene.text.FontWeight.BOLD, 10));
        linkIcon.setFill(Color.web(baseText));
        
        // Link text with improved typography
        Text linkText = new Text(text);
        linkText.setFont(javafx.scene.text.Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), javafx.scene.text.FontWeight.MEDIUM, 12));
        linkText.setFill(Color.web(baseText));
        link.getChildren().addAll(linkIcon, linkText);
        
        // Enhanced hover effects with smooth transitions
        link.setOnMouseEntered(e -> {
            link.setStyle(
                "-fx-background-color: " + hoverBg + ";" +
                "-fx-background-radius: 999px;" +
                "-fx-effect: dropshadow(gaussian, " + themeManager.toRgba(themeManager.getAccentHex(), 0.26) + ", 14, 0.24, 0, 4);"
            );

            ScaleTransition linkScale = new ScaleTransition(Duration.millis(180), link);
            linkScale.setToX(1.05);
            linkScale.setToY(1.05);
            linkScale.setInterpolator(HorizonDesignSystem.WEB_POP);
            linkScale.play();
            
            // Animate icon
            ScaleTransition iconScale = new ScaleTransition(Duration.millis(150), linkIcon);
            iconScale.setToX(1.2);
            iconScale.setToY(1.2);
            iconScale.setInterpolator(HorizonDesignSystem.WEB_POP);
            iconScale.play();
            
            // Change text and icon color on hover
            linkText.setFill(Color.web(hoverText));
            linkIcon.setFill(Color.web(hoverText));
        });
        
        link.setOnMouseExited(e -> {
            link.setStyle("-fx-background-color: transparent; -fx-background-radius: 999px;");

            ScaleTransition linkScale = new ScaleTransition(Duration.millis(170), link);
            linkScale.setToX(1.0);
            linkScale.setToY(1.0);
            linkScale.setInterpolator(HorizonDesignSystem.WEB_EASE);
            linkScale.play();
            
            // Reset icon
            ScaleTransition iconScale = new ScaleTransition(Duration.millis(150), linkIcon);
            iconScale.setToX(1.0);
            iconScale.setToY(1.0);
            iconScale.setInterpolator(HorizonDesignSystem.WEB_EASE);
            iconScale.play();
            
            // Reset text and icon color
            linkText.setFill(Color.web(baseText));
            linkIcon.setFill(Color.web(baseText));
        });
        
        // Add click effect
        link.setOnMousePressed(e -> {
            link.setStyle(
                "-fx-background-color: " + hoverBg + ";" +
                "-fx-background-radius: 999px;"
            );
            link.setScaleX(0.96);
            link.setScaleY(0.96);
        });
        
        link.setOnMouseReleased(e -> {
            link.setStyle(
                "-fx-background-color: " + hoverBg + ";" +
                "-fx-background-radius: 999px;"
            );
            ScaleTransition linkScale = new ScaleTransition(Duration.millis(160), link);
            linkScale.setToX(1.05);
            linkScale.setToY(1.05);
            linkScale.setInterpolator(HorizonDesignSystem.WEB_POP);
            linkScale.play();
        });
        
        return link;
    }
    
    private void applyThemeStyling() {
        ThemeManager tm = ThemeManager.getInstance();
        glowLayer.setStyle(glowStyle());
        if (tm.isDarkMode()) {
            double pulse = 0.32 + (0.12 * Math.sin(tm.gradientPhaseProperty().get() * Math.PI));
            double radius = 22 + (6 * Math.sin(tm.gradientPhaseProperty().get() * Math.PI));

            root.setStyle(HorizonDesignSystem.webFloatingIsland(999, false));

            DropShadow footerShadow = new DropShadow();
            footerShadow.setBlurType(BlurType.GAUSSIAN);
            footerShadow.setColor(tm.getNeonGlowColor().deriveColor(0, 1, 1, pulse));
            footerShadow.setRadius(radius);
            footerShadow.setOffsetX(0);
            footerShadow.setOffsetY(0);
            root.setEffect(footerShadow);
            return;
        }

        root.setStyle(HorizonDesignSystem.webFloatingIsland(999, false));

        DropShadow footerShadow = new DropShadow();
        footerShadow.setBlurType(BlurType.GAUSSIAN);
        footerShadow.setColor(Color.web("rgba(15,23,42,0.16)"));
        footerShadow.setRadius(20);
        footerShadow.setOffsetX(0);
        footerShadow.setOffsetY(4);
        root.setEffect(footerShadow);
    }
    
    private void startAnimations() {
        // Create subtle floating animation for the dynamic island
        TranslateTransition floatAnimation = new TranslateTransition(Duration.seconds(4), root);
        floatAnimation.setFromY(0);
        floatAnimation.setToY(2);
        floatAnimation.setAutoReverse(true);
        floatAnimation.setCycleCount(Animation.INDEFINITE);
        floatAnimation.setInterpolator(HorizonDesignSystem.WEB_EASE);
        floatAnimation.play();
    }

    private String glowStyle() {
        ThemeManager tm = ThemeManager.getInstance();
        return "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
            "-fx-background-radius: 52px;" +
            "-fx-effect: dropshadow(gaussian, " + tm.toRgba(tm.getAccentHex(), 0.34) + ", 38, 0.28, 0, 0);";
    }

    private void installIslandHover() {
        root.setOnMouseEntered(e -> {
            TranslateTransition lift = new TranslateTransition(Duration.millis(260), root);
            lift.setToY(-2);
            lift.setInterpolator(HorizonDesignSystem.WEB_EASE);
            FadeTransition glow = new FadeTransition(Duration.millis(300), glowLayer);
            glow.setToValue(0.86);
            glow.setInterpolator(HorizonDesignSystem.WEB_EASE);
            new ParallelTransition(lift, glow).play();
        });

        root.setOnMouseExited(e -> {
            TranslateTransition lift = new TranslateTransition(Duration.millis(260), root);
            lift.setToY(0);
            lift.setInterpolator(HorizonDesignSystem.WEB_EASE);
            FadeTransition glow = new FadeTransition(Duration.millis(300), glowLayer);
            glow.setToValue(0);
            glow.setInterpolator(HorizonDesignSystem.WEB_EASE);
            new ParallelTransition(lift, glow).play();
        });
    }
    
    public StackPane getRoot() {
        return wrapper;
    }
    
    public void cleanup() {
        if (gradientPhaseListener != null) {
            ThemeManager.getInstance().gradientPhaseProperty().removeListener(gradientPhaseListener);
            gradientPhaseListener = null;
        }
    }
    
    // Public method to refresh theme-dependent styles
    public void refreshTheme() {
        setupLayout();
    }
}


