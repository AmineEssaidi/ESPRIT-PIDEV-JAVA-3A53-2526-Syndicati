package com.pidev.views.about;

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
import com.pidev.interfaces.ViewInterface;
import com.pidev.utils.theme.ThemeManager;
import com.pidev.utils.navigation.NavigationManager;

/**
 * About View - Main about page with sub-menu options
 */
public class AboutView implements ViewInterface {
    
    private final VBox root;
    
    public AboutView() {
        this.root = new VBox();
        setupLayout();
    }
    
    private void setupLayout() {
        root.setSpacing(40);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(80, 40, 80, 40));
        
        // Apply theme styling
        applyThemeStyling();
        
        // Title
        Text title = new Text("About Us");
        title.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 48));
        title.setFill(Color.web(ThemeManager.getInstance().getTextColor()));
        
        // Subtitle
        Text subtitle = new Text("Learn more about our company and team");
        subtitle.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 18));
        subtitle.setFill(Color.web(ThemeManager.getInstance().getSecondaryTextColor()));        // About options grid
        HBox aboutGrid = new HBox();
        aboutGrid.setSpacing(30);
        aboutGrid.setAlignment(Pos.CENTER);
        
        // Option 1: Our Story
        VBox option1 = createAboutCard("📖", "Our Story", "Learn about our journey and mission");
        option1.setOnMouseClicked(e -> NavigationManager.getInstance().navigateTo("about-detail"));
        
        // Option 2: Our Team
        VBox option2 = createAboutCard("👥", "Our Team", "Meet the people behind our success");
        option2.setOnMouseClicked(e -> NavigationManager.getInstance().navigateTo("about-detail"));
        
        // Option 3: Contact Us
        VBox option3 = createAboutCard("📞", "Contact Us", "Get in touch with our team");
        option3.setOnMouseClicked(e -> NavigationManager.getInstance().navigateTo("about-detail"));
        
        aboutGrid.getChildren().addAll(option1, option2, option3);
        
        root.getChildren().addAll(title, subtitle, aboutGrid);
    }
    
    private VBox createAboutCard(String icon, String title, String description) {
        VBox card = new VBox();
        card.setSpacing(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 25, 30, 25));
        card.setMinWidth(200);
        card.setMaxWidth(250);
        
        // Apply card styling
        ThemeManager themeManager = ThemeManager.getInstance();
        card.setStyle(
            "-fx-background-color: " + themeManager.getDynamicIslandBackground() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;"
        );
        
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
        iconText.setFont(Font.font(48));
        
        // Title
        Text titleText = new Text(title);
        titleText.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 20));
        titleText.setFill(Color.web(themeManager.getTextColor()));
        
        // Description
        Text descText = new Text(description);
        descText.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        descText.setFill(Color.web(themeManager.getSecondaryTextColor()));
        descText.setWrappingWidth(200);        card.getChildren().addAll(iconText, titleText, descText);
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: " + themeManager.getTabHoverColor() + ";" +
                "-fx-background-radius: 20px;" +
                "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 20px;"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: " + themeManager.getDynamicIslandBackground() + ";" +
                "-fx-background-radius: 20px;" +
                "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 20px;"
            );
        });
        
        return card;
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
