package com.pidev.views.services;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import com.pidev.interfaces.ViewInterface;
import com.pidev.utils.theme.ThemeManager;

/**
 * Service Detail View - Detailed service information page
 */
public class ServiceDetailView implements ViewInterface {
    
    private final VBox root;
    
    public ServiceDetailView() {
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
        Button backButton = new Button("← Back to Services");
        backButton.setStyle(
            "-fx-background-color: " + ThemeManager.getInstance().getModernAccentColor() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-font-size: 14px;"
        );
        backButton.setOnAction(e -> {
            // This will be handled by the navigation manager
            System.out.println("Back to Services clicked");
        });
        
        // Title
        Text title = new Text("Service Details");
        title.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 36));
        title.setFill(Color.web(ThemeManager.getInstance().getTextColor()));
        
        // Content
        VBox content = new VBox();
        content.setSpacing(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600);
        
        Text description = new Text("This is a detailed view of our service offerings. Here you would find comprehensive information about our development services, pricing, and process.");
        description.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 16));
        description.setFill(Color.web(ThemeManager.getInstance().getSecondaryTextColor()));
        description.setWrappingWidth(600);
        
        Text features = new Text("• Modern web applications\n• Responsive design\n• Cross-platform compatibility\n• 24/7 support");
        features.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        features.setFill(Color.web(ThemeManager.getInstance().getTextColor()));        content.getChildren().addAll(description, features);
        
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
