package com.pidev.views.about;

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
        Button backButton = new Button("← Back to About");
        backButton.setStyle(
            "-fx-background-color: " + ThemeManager.getInstance().getModernAccentColor() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-padding: 8 16 8 16;" +
            "-fx-font-size: 14px;"
        );
        backButton.setOnAction(e -> {
            // This will be handled by the navigation manager
            System.out.println("Back to About clicked");
        });
        
        // Title
        Text title = new Text("About Details");
        title.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 36));
        title.setFill(Color.web(ThemeManager.getInstance().getTextColor()));
        
        // Content
        VBox content = new VBox();
        content.setSpacing(20);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(600);
        
        Text description = new Text("This is a detailed view about our company. Here you would find comprehensive information about our story, team, values, and contact information.");
        description.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 16));
        description.setFill(Color.web(ThemeManager.getInstance().getSecondaryTextColor()));
        description.setWrappingWidth(600);
        
        Text details = new Text("• Founded in 2024\n• Passionate about technology\n• Committed to excellence\n• Always learning and growing");
        details.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        details.setFill(Color.web(ThemeManager.getInstance().getTextColor()));        content.getChildren().addAll(description, details);
        
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
