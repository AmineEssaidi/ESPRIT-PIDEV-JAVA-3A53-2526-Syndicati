package com.pidev.views.settings;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.pidev.interfaces.ViewInterface;
import com.pidev.utils.navigation.NavigationManager;
import com.pidev.utils.theme.ThemeManager;

/**
 * Settings View - Application settings page with modern design
 */
public class SettingsView implements ViewInterface {
    
    private final VBox root;
    private final ThemeManager themeManager;
    
    public SettingsView() {
        this.root = new VBox();
        this.themeManager = ThemeManager.getInstance();
        setupLayout();
    }
    
    private void setupLayout() {
        VBox contentContainer = new VBox();
        contentContainer.setSpacing(25);
        contentContainer.setAlignment(Pos.TOP_CENTER);
        contentContainer.setPadding(new Insets(40, 40, 40, 40));
        contentContainer.setMaxWidth(700);
        
        // Title
        Label titleLabel = new Label("⚙ Settings");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(themeManager.isDarkMode() ? Color.WHITE : Color.web("#1a1a1e"));
        
        // Settings sections
        VBox appearanceSection = createSettingsSection("Appearance", createAppearanceSettings());
        VBox notificationSection = createSettingsSection("Notifications", createNotificationSettings());
        VBox privacySection = createSettingsSection("Privacy", createPrivacySettings());
        VBox generalSection = createSettingsSection("General", createGeneralSettings());
        
        // Back button
        Button backButton = createStyledButton("← Back to Home");
        backButton.setOnAction(e -> NavigationManager.getInstance().navigateTo("home"));
        
        contentContainer.getChildren().addAll(
            titleLabel,
            appearanceSection,
            notificationSection,
            privacySection,
            generalSection,
            backButton
        );
        
        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        // Center the content
        HBox centerWrapper = new HBox(scrollPane);
        centerWrapper.setAlignment(Pos.CENTER);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        
        root.getChildren().add(centerWrapper);
        VBox.setVgrow(centerWrapper, Priority.ALWAYS);
        
        applyThemeStyling();
    }
    
    private VBox createSettingsSection(String title, VBox content) {
        VBox section = new VBox();
        section.setSpacing(15);
        section.setPadding(new Insets(20));
        
        section.setStyle(
            "-fx-background-color: " + themeManager.getDynamicIslandBackground() + ";" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 16px;"
        );
        
        // Add shadow
        DropShadow shadow = new DropShadow();
        shadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        shadow.setColor(Color.color(0, 0, 0, themeManager.isDarkMode() ? 0.3 : 0.1));
        shadow.setRadius(10);
        shadow.setOffsetY(4);
        section.setEffect(shadow);
        
        // Section title
        Label sectionTitle = new Label(title);
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setTextFill(themeManager.isDarkMode() ? Color.WHITE : Color.web("#1a1a1e"));
        
        section.getChildren().addAll(sectionTitle, content);
        
        return section;
    }
    
    private VBox createAppearanceSettings() {
        VBox settings = new VBox();
        settings.setSpacing(12);
        
        // Dark mode toggle
        HBox darkModeRow = createSettingRow("Dark Mode", "Switch between light and dark themes");
        ToggleButton darkModeToggle = new ToggleButton(themeManager.isDarkMode() ? "ON" : "OFF");
        darkModeToggle.setSelected(themeManager.isDarkMode());
        styleToggleButton(darkModeToggle);
        darkModeToggle.setOnAction(e -> {
            themeManager.toggleTheme();
            darkModeToggle.setText(themeManager.isDarkMode() ? "ON" : "OFF");
            // Refresh the view
            root.getChildren().clear();
            setupLayout();
        });
        ((HBox) darkModeRow).getChildren().add(darkModeToggle);
        
        // Accent color (placeholder)
        HBox accentColorRow = createSettingRow("Accent Color", "Choose your preferred accent color");
        ComboBox<String> accentCombo = new ComboBox<>();
        accentCombo.getItems().addAll("Red", "Blue", "Green", "Purple", "Orange");
        accentCombo.setValue("Red");
        styleComboBox(accentCombo);
        ((HBox) accentColorRow).getChildren().add(accentCombo);
        
        // Font size
        HBox fontSizeRow = createSettingRow("Font Size", "Adjust the application font size");
        Slider fontSlider = new Slider(12, 20, 14);
        fontSlider.setShowTickLabels(true);
        fontSlider.setShowTickMarks(true);
        fontSlider.setMajorTickUnit(2);
        fontSlider.setPrefWidth(150);
        ((HBox) fontSizeRow).getChildren().add(fontSlider);
        
        settings.getChildren().addAll(darkModeRow, accentColorRow, fontSizeRow);
        return settings;
    }
    
    private VBox createNotificationSettings() {
        VBox settings = new VBox();
        settings.setSpacing(12);
        
        // Push notifications
        HBox pushRow = createSettingRow("Push Notifications", "Receive push notifications");
        ToggleButton pushToggle = new ToggleButton("ON");
        pushToggle.setSelected(true);
        styleToggleButton(pushToggle);
        pushToggle.setOnAction(e -> pushToggle.setText(pushToggle.isSelected() ? "ON" : "OFF"));
        ((HBox) pushRow).getChildren().add(pushToggle);
        
        // Email notifications
        HBox emailRow = createSettingRow("Email Notifications", "Receive email updates");
        ToggleButton emailToggle = new ToggleButton("ON");
        emailToggle.setSelected(true);
        styleToggleButton(emailToggle);
        emailToggle.setOnAction(e -> emailToggle.setText(emailToggle.isSelected() ? "ON" : "OFF"));
        ((HBox) emailRow).getChildren().add(emailToggle);
        
        // Sound
        HBox soundRow = createSettingRow("Sound Effects", "Play sounds for notifications");
        ToggleButton soundToggle = new ToggleButton("OFF");
        soundToggle.setSelected(false);
        styleToggleButton(soundToggle);
        soundToggle.setOnAction(e -> soundToggle.setText(soundToggle.isSelected() ? "ON" : "OFF"));
        ((HBox) soundRow).getChildren().add(soundToggle);
        
        settings.getChildren().addAll(pushRow, emailRow, soundRow);
        return settings;
    }
    
    private VBox createPrivacySettings() {
        VBox settings = new VBox();
        settings.setSpacing(12);
        
        // Profile visibility
        HBox visibilityRow = createSettingRow("Profile Visibility", "Who can see your profile");
        ComboBox<String> visibilityCombo = new ComboBox<>();
        visibilityCombo.getItems().addAll("Everyone", "Friends Only", "Private");
        visibilityCombo.setValue("Everyone");
        styleComboBox(visibilityCombo);
        ((HBox) visibilityRow).getChildren().add(visibilityCombo);
        
        // Activity status
        HBox activityRow = createSettingRow("Show Activity Status", "Let others see when you're online");
        ToggleButton activityToggle = new ToggleButton("ON");
        activityToggle.setSelected(true);
        styleToggleButton(activityToggle);
        activityToggle.setOnAction(e -> activityToggle.setText(activityToggle.isSelected() ? "ON" : "OFF"));
        ((HBox) activityRow).getChildren().add(activityToggle);
        
        // Data collection
        HBox dataRow = createSettingRow("Analytics", "Help improve the app with usage data");
        ToggleButton dataToggle = new ToggleButton("ON");
        dataToggle.setSelected(true);
        styleToggleButton(dataToggle);
        dataToggle.setOnAction(e -> dataToggle.setText(dataToggle.isSelected() ? "ON" : "OFF"));
        ((HBox) dataRow).getChildren().add(dataToggle);
        
        settings.getChildren().addAll(visibilityRow, activityRow, dataRow);
        return settings;
    }
    
    private VBox createGeneralSettings() {
        VBox settings = new VBox();
        settings.setSpacing(12);
        
        // Language
        HBox langRow = createSettingRow("Language", "Choose your preferred language");
        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll("English", "French", "Spanish", "German", "Arabic");
        langCombo.setValue("English");
        styleComboBox(langCombo);
        ((HBox) langRow).getChildren().add(langCombo);
        
        // Auto-update
        HBox updateRow = createSettingRow("Auto-Update", "Automatically download updates");
        ToggleButton updateToggle = new ToggleButton("ON");
        updateToggle.setSelected(true);
        styleToggleButton(updateToggle);
        updateToggle.setOnAction(e -> updateToggle.setText(updateToggle.isSelected() ? "ON" : "OFF"));
        ((HBox) updateRow).getChildren().add(updateToggle);
        
        // Startup
        HBox startupRow = createSettingRow("Launch on Startup", "Open app when computer starts");
        ToggleButton startupToggle = new ToggleButton("OFF");
        startupToggle.setSelected(false);
        styleToggleButton(startupToggle);
        startupToggle.setOnAction(e -> startupToggle.setText(startupToggle.isSelected() ? "ON" : "OFF"));
        ((HBox) startupRow).getChildren().add(startupToggle);
        
        settings.getChildren().addAll(langRow, updateRow, startupRow);
        return settings;
    }
    
    private HBox createSettingRow(String title, String description) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(15);
        
        VBox textBox = new VBox();
        textBox.setSpacing(2);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        titleLabel.setTextFill(themeManager.isDarkMode() ? Color.WHITE : Color.web("#1a1a1e"));
        
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 12));
        descLabel.setTextFill(themeManager.isDarkMode() ? Color.web("#888888") : Color.web("#666666"));
        
        textBox.getChildren().addAll(titleLabel, descLabel);
        row.getChildren().add(textBox);
        
        return row;
    }
    
    private void styleToggleButton(ToggleButton toggle) {
        toggle.setPrefWidth(60);
        toggle.setStyle(
            "-fx-background-color: " + (toggle.isSelected() ? themeManager.getModernAccentColor() : "#555555") + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 15px;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );
        toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            toggle.setStyle(
                "-fx-background-color: " + (newVal ? themeManager.getModernAccentColor() : "#555555") + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 15px;" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: bold;"
            );
        });
    }
    
    private void styleComboBox(ComboBox<String> combo) {
        combo.setPrefWidth(130);
        combo.setStyle(
            "-fx-background-color: " + (themeManager.isDarkMode() ? "#333333" : "#e0e0e0") + ";" +
            "-fx-text-fill: " + (themeManager.isDarkMode() ? "white" : "black") + ";" +
            "-fx-background-radius: 8px;"
        );
    }
    
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        button.setPadding(new Insets(12, 30, 12, 30));
        button.setStyle(
            "-fx-background-color: " + themeManager.getModernAccentColor() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20px;" +
            "-fx-cursor: hand;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: derive(" + themeManager.getModernAccentColor() + ", -10%);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20px;" +
            "-fx-cursor: hand;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: " + themeManager.getModernAccentColor() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20px;" +
            "-fx-cursor: hand;"
        ));
        return button;
    }
    
    private void applyThemeStyling() {
        root.setStyle("-fx-background-color: transparent;");
    }
    
    @Override
    public VBox getRoot() {
        return root;
    }
    
    @Override
    public void cleanup() {
        // Cleanup resources if needed
    }
}
