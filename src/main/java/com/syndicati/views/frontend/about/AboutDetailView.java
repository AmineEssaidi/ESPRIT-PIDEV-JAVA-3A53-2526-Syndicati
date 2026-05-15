package com.syndicati.views.frontend.about;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.navigation.NavigationManager;
import com.syndicati.utils.ui.HorizonDesignSystem;

/**
 * About Detail View - Detailed about information page
 */
public class AboutDetailView implements ViewInterface {
    
    private final VBox root;
    
    public AboutDetailView() {
        this.root = new VBox();
        setupLayout();
    }
    
    private void setupLayout() {
        root.setSpacing(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(80, 40, 80, 40));
        
        // Apply theme styling
        applyThemeStyling();
        
        // Back button
        Button backButton = new Button("< Back to About");
        backButton.setStyle(HorizonDesignSystem.buttonPrimary() + "-fx-padding: 10 18 10 18;-fx-font-size: 14px;");
        HorizonDesignSystem.installButtonMotion(backButton);
        backButton.setOnAction(e -> {
            NavigationManager.getInstance().navigateTo("about");
        });
        
        // Title
        Text title = new Text("About Details");
        title.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 36));
        title.setFill(ThemeManager.getInstance().getAccentGradientPaint());
        
        // Content
        VBox content = new VBox();
        content.setSpacing(20);
        content.setAlignment(Pos.TOP_LEFT);
        content.setMaxWidth(600);
        ThemeManager tm = ThemeManager.getInstance();
        content.setStyle(HorizonDesignSystem.webSectionCard(24, false) + "-fx-padding: 26px;");
        HorizonDesignSystem.installWebLift(content);
        
        Text description = new Text("This is a detailed view about our company. Here you would find comprehensive information about our story, team, values, and contact information.");
        description.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 16));
        description.setFill(Color.web(ThemeManager.getInstance().getSecondaryTextColor()));
        description.setWrappingWidth(600);
        
        Text details = new Text("* Founded in 2024\n* Passionate about technology\n* Committed to excellence\n* Always learning and growing");
        details.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        details.setFill(Color.web(ThemeManager.getInstance().getTextColor()));
        content.getChildren().addAll(description, details);
        
        root.getChildren().addAll(backButton, title, content);
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


