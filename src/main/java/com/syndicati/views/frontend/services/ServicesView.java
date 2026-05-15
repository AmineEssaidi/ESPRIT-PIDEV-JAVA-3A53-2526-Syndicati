package com.syndicati.views.frontend.services;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlurType;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.ui.HorizonDesignSystem;
import com.syndicati.utils.navigation.NavigationManager;

/**
 * Services View - Main services page with sub-menu options
 */
public class ServicesView implements ViewInterface {
    
    private final VBox root;
    
    public ServicesView() {
        this.root = new VBox();
        setupLayout();
    }
    
    private void setupLayout() {
        root.setSpacing(34);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(30, 34, 70, 34));
        
        // Apply theme styling
        applyThemeStyling();

        StackPane hero = buildHero();
        Text title = sectionTitle("Nos Services");
        Text subtitle = new Text("Une suite complete de solutions pour votre copropriete.");
        subtitle.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 16));
        subtitle.setFill(Color.web(HorizonDesignSystem.mutedText()));

        HBox servicesGrid = new HBox(22);
        servicesGrid.setAlignment(Pos.CENTER);
        servicesGrid.setFillHeight(true);

        VBox service1 = createServiceCard("BLDG", "Gestion Residentielle", "Gerez vos residences, appartements et residents en toute simplicite.");
        service1.setOnMouseClicked(e -> {
            NavigationManager.getInstance().awardInteractionXp(1);
            NavigationManager.getInstance().navigateTo("residence");
        });
        
        VBox service2 = createServiceCard("SAV", "Maintenance & SAV", "Suivez les reclamations et planifiez les interventions techniques.");
        service2.setOnMouseClicked(e -> {
            NavigationManager.getInstance().awardInteractionXp(1);
            NavigationManager.getInstance().navigateTo("syndicat");
        });
        
        VBox service3 = createServiceCard("CHAT", "Communication", "Restez connecte avec vos voisins via le forum et la messagerie.");
        service3.setOnMouseClicked(e -> {
            NavigationManager.getInstance().awardInteractionXp(1);
            NavigationManager.getInstance().navigateTo("forum");
        });

        VBox service4 = createServiceCard("PAY", "Paiements Securises", "Reglez vos frais de syndic via une experience rapide et claire.");
        service4.setOnMouseClicked(e -> {
            NavigationManager.getInstance().awardInteractionXp(1);
            NavigationManager.getInstance().navigateTo("profile");
        });
        
        servicesGrid.getChildren().addAll(service1, service2, service3, service4);
        
        root.getChildren().addAll(hero, title, subtitle, servicesGrid);
    }

    private StackPane buildHero() {
        StackPane hero = new StackPane();
        hero.setMaxWidth(1500);
        hero.setMinHeight(360);
        hero.setPadding(new Insets(56, 64, 56, 64));
        hero.setStyle(HorizonDesignSystem.webHeroPanel());

        Region glow = new Region();
        glow.setPrefSize(560, 560);
        glow.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 55%, " +
            ThemeManager.getInstance().toRgba(ThemeManager.getInstance().getAccentHex(), 0.28) + ", transparent);");
        StackPane.setAlignment(glow, Pos.CENTER_RIGHT);
        glow.setTranslateX(150);

        VBox copy = new VBox(22);
        copy.setAlignment(Pos.CENTER_LEFT);
        copy.setMaxWidth(780);

        HBox badge = new HBox(new Text("SERVICES HUB"));
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(10, 20, 10, 20));
        badge.setStyle(HorizonDesignSystem.webAccentBadge());
        ((Text) badge.getChildren().get(0)).setFill(Color.WHITE);
        ((Text) badge.getChildren().get(0)).setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 12));

        Text heading = new Text("Syndicati Services");
        heading.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), FontWeight.EXTRA_BOLD, 64));
        heading.setFill(Color.WHITE);

        Text body = new Text("The same premium web experience, rebuilt inside the Java app for residence, maintenance, forum, events and payments.");
        body.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 18));
        body.setFill(Color.web("rgba(255,255,255,0.56)"));
        body.setWrappingWidth(660);
        body.setLineSpacing(5);

        copy.getChildren().addAll(badge, heading, body);
        hero.getChildren().addAll(glow, copy);
        StackPane.setAlignment(copy, Pos.CENTER_LEFT);
        HorizonDesignSystem.fadeIn(hero);
        return hero;
    }

    private Text sectionTitle(String value) {
        Text title = new Text(value + " .");
        title.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), FontWeight.EXTRA_BOLD, 44));
        title.setFill(Color.web(ThemeManager.getInstance().getTextColor()));
        return title;
    }
    
    private VBox createServiceCard(String icon, String title, String description) {
        VBox card = new VBox();
        card.setSpacing(15);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(30, 26, 30, 26));
        card.setMinWidth(235);
        card.setPrefWidth(280);
        card.setMaxWidth(320);
        
        // Apply card styling
        ThemeManager themeManager = ThemeManager.getInstance();
        card.setStyle(HorizonDesignSystem.webFeatureCard(28));
        
        // Add shadow effect
        DropShadow cardShadow = new DropShadow();
        cardShadow.setBlurType(BlurType.GAUSSIAN);
        if (themeManager.isDarkMode()) {
            cardShadow.setColor(Color.color(0, 0, 0, 0.3));
        } else {
            cardShadow.setColor(Color.color(0, 0, 0, 0.1));
        }
        cardShadow.setRadius(10);
        cardShadow.setOffsetX(0);
        cardShadow.setOffsetY(4);
        card.setEffect(cardShadow);
        
        // Icon
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), FontWeight.EXTRA_BOLD, 28));
        iconText.setFill(Color.web(themeManager.getAccentHex()));
        
        // Title
        Text titleText = new Text(title);
        titleText.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 20));
        titleText.setFill(Color.web(themeManager.getTextColor()));
        
        // Description
        Text descText = new Text(description);
        descText.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        descText.setFill(Color.web(themeManager.getSecondaryTextColor()));
        descText.setWrappingWidth(218);
        Region line = new Region();
        line.setPrefHeight(2);
        line.setMaxWidth(Double.MAX_VALUE);
        line.setStyle("-fx-background-color: linear-gradient(to right, " + themeManager.getAccentHex() + ", transparent); -fx-background-radius: 2px;");
        card.getChildren().addAll(iconText, titleText, descText, line);
        
        HorizonDesignSystem.installLift(card, 1.02, -10);
        
        return card;
    }

    private String cardStyle(ThemeManager tm, boolean hover) {
        if (tm.isDarkMode()) {
            String accentSoft = tm.toRgba(tm.getAccentHex(), hover ? 0.22 : 0.16);
            String accentGlow = tm.toRgba(tm.getAccentHex(), hover ? 0.10 : 0.06);
            String border = tm.toRgba(tm.getAccentHex(), hover ? 0.52 : 0.34);
            return "-fx-background-color: radial-gradient(focus-angle 32deg, focus-distance 25%, center 16% 12%, radius 125%, " + accentSoft + " 0%, rgba(0,0,0,0.88) 62%, rgba(0,0,0,0.96) 100%), " +
                   "linear-gradient(to bottom right, rgba(255,255,255,0.06), rgba(255,255,255,0.015) 46%, " + accentGlow + " 100%);" +
                   "-fx-background-radius: 20px;" +
                   "-fx-border-color: " + border + ";" +
                   "-fx-border-width: 1px;" +
                   "-fx-border-radius: 20px;";
        }
        String border = tm.toRgba(tm.getAccentHex(), hover ? 0.34 : 0.24);
        return "-fx-background-color: linear-gradient(to bottom right, rgba(255,255,255,0.96), rgba(255,255,255,0.86) 54%, rgba(243,247,255,0.92) 100%);" +
               "-fx-background-radius: 20px;" +
               "-fx-border-color: " + border + ";" +
               "-fx-border-width: 1px;" +
               "-fx-border-radius: 20px;";
    }
    
    private void applyThemeStyling() {
        // Don't apply background color - let ImageBackground handle it
        root.setStyle("-fx-background-color: transparent;");
    }
    
    @Override
    public Pane getRoot() {
        return root;
    }
    
    public void cleanup() {
        // Cleanup resources if needed
    }
}


