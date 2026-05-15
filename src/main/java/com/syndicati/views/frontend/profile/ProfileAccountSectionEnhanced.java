package com.syndicati.views.frontend.profile;

import com.syndicati.controllers.user.user.UserController;
import com.syndicati.models.user.User;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.ui.HorizonDesignSystem;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;

/**
 * Enhanced Account section with glass-switcher, settings modal, form controls.
 * Matches Horizon profile 1:1 with smooth animations and interactive settings.
 */
public class ProfileAccountSectionEnhanced {
    
    private final VBox root = new VBox(16);
    private final UserController userController;
    private final StackPane switcherContainer = new StackPane();
    
    // Switcher faces
    private final VBox faceViewMode = new VBox(16);
    private final VBox faceEditMode = new VBox(16);
    
    private User currentUser;

    public ProfileAccountSectionEnhanced() {
        this.userController = new UserController();
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        buildLayout();
    }

    private void buildLayout() {
        root.setPadding(new Insets(16, 0, 0, 0));
        
        VBox card = new VBox(0);
        card.setStyle(HorizonDesignSystem.webObsidianPanel(24));
        card.setPrefHeight(600);
        HorizonDesignSystem.popIn(card);
        
        // Switcher container
        switcherContainer.setPrefHeight(500);
        VBox.setVgrow(switcherContainer, Priority.ALWAYS);
        
        buildViewMode();
        buildEditMode();
        
        // Add edit view first (back), view mode on top (front)
        switcherContainer.getChildren().addAll(faceEditMode, faceViewMode);
        faceEditMode.setVisible(false);
        faceEditMode.setManaged(false);
        
        card.getChildren().add(switcherContainer);
        root.getChildren().add(card);
    }

    private void buildViewMode() {
        faceViewMode.setStyle("-fx-background-color: transparent;");
        faceViewMode.setPadding(new Insets(20));
        
        // Header with title and settings button
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("👤 Account");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        HBox.setHgrow(title, Priority.ALWAYS);
        
        Button settingsBtn = new Button("⚙️ Settings");
        settingsBtn.setStyle(HorizonDesignSystem.buttonPrimary() + "-fx-padding: 10 18 10 18; -fx-background-radius: 999px; -fx-border-radius: 999px; -fx-font-size: 11;");
        HorizonDesignSystem.installButtonMotion(settingsBtn);
        settingsBtn.setOnAction(e -> showEditMode());
        
        header.getChildren().addAll(title, settingsBtn);
        faceViewMode.getChildren().add(header);
        
        // Scroll pane for content
        ScrollPane viewScroll = new ScrollPane();
        HorizonDesignSystem.styleScrollPane(viewScroll);
        
        VBox viewContent = new VBox(16);
        viewContent.setPadding(new Insets(12, 0, 0, 0));
        
        // Account info card
        VBox accountCard = createAccountInfoCard();
        viewContent.getChildren().add(accountCard);
        
        // Onboarding choices card
        VBox onboardingCard = createOnboardingCard();
        viewContent.getChildren().add(onboardingCard);
        
        viewScroll.setContent(viewContent);
        VBox.setVgrow(viewScroll, Priority.ALWAYS);
        faceViewMode.getChildren().add(viewScroll);
    }

    private VBox createAccountInfoCard() {
        VBox card = new VBox(12);
        card.setStyle(profileInnerCardStyle(16));
        HorizonDesignSystem.installWebLift(card);
        
        Label cardTitle = new Label("Account Information");
        cardTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        cardTitle.setTextFill(Color.WHITE);
        card.getChildren().add(cardTitle);
        
        if (currentUser != null) {
            card.getChildren().addAll(
                createInfoLine("First Name", currentUser.getFirstName()),
                createInfoLine("Last Name", currentUser.getLastName()),
                createInfoLine("Email", currentUser.getEmailUser()),
                createInfoLine("Role", formatRole(currentUser.getRoleUser())),
                createInfoLine("Verified", currentUser.isVerified() ? "✓ Yes" : "✗ Pending admin verification"),
                createInfoLine("Account Created", currentUser.getCreatedAt() != null 
                    ? currentUser.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "—"),
                createInfoLine("Phone", currentUser.getPhone() != null ? currentUser.getPhone() : "—")
            );
        }
        
        return card;
    }

    private VBox createOnboardingCard() {
        VBox card = new VBox(12);
        card.setStyle(profileInnerCardStyle(16));
        HorizonDesignSystem.installWebLift(card);
        
        Label cardTitle = new Label("✓ Onboarding Choices");
        cardTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        cardTitle.setTextFill(Color.WHITE);
        card.getChildren().add(cardTitle);
        
        HBox preferencesRow = new HBox(8);
        preferencesRow.setAlignment(Pos.CENTER_LEFT);
        
        preferencesRow.getChildren().addAll(
            createPrefPill("Language", "English"),
            createPrefPill("Theme", "Dark"),
            createPrefPill("Status", "Completed")
        );
        
        card.getChildren().add(preferencesRow);
        return card;
    }

    private VBox createInfoLine(String label, String value) {
        VBox line = new VBox(4);
        
        Label labelText = new Label(label);
        labelText.setFont(Font.font("Segoe UI", 10));
        labelText.setTextFill(Color.color(1, 1, 1, 0.6));
        
        Label valueText = new Label(value);
        valueText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        valueText.setTextFill(Color.WHITE);
        valueText.setWrapText(true);
        
        line.getChildren().addAll(labelText, valueText);
        return line;
    }

    private VBox createPrefPill(String key, String value) {
        VBox pill = new VBox(2);
        pill.setStyle(HorizonDesignSystem.webAccentBadge() + "-fx-padding: 10 14 10 14; -fx-background-radius: 12px; -fx-border-radius: 12px;");
        HorizonDesignSystem.installLift(pill, 1.02, -2);
        
        Label keyLabel = new Label(key);
        keyLabel.setFont(Font.font("Segoe UI", 9));
        keyLabel.setTextFill(Color.color(1, 1, 1, 0.6));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        valueLabel.setTextFill(Color.color(99/255.0, 102/255.0, 241/255.0));
        
        pill.getChildren().addAll(keyLabel, valueLabel);
        return pill;
    }

    private void buildEditMode() {
        faceEditMode.setStyle("-fx-background-color: rgba(5, 5, 10, 0.96); -fx-background-radius: 24px;");
        faceEditMode.setPadding(new Insets(20));
        
        // Back button and title
        HBox backHeader = new HBox(12);
        backHeader.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = new Button("←");
        backBtn.setStyle(HorizonDesignSystem.buttonGhost() + "-fx-min-width: 34px; -fx-min-height: 34px; -fx-max-width: 34px; -fx-max-height: 34px; -fx-background-radius: 999px; -fx-border-radius: 999px;");
        HorizonDesignSystem.installButtonMotion(backBtn);
        backBtn.setOnAction(e -> showViewMode());
        
        Label editTitle = new Label("Account Settings");
        editTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        editTitle.setTextFill(Color.WHITE);
        
        backHeader.getChildren().addAll(backBtn, editTitle);
        faceEditMode.getChildren().add(backHeader);
        
        // Settings form
        ScrollPane editScroll = new ScrollPane();
        HorizonDesignSystem.styleScrollPane(editScroll);
        
        VBox formContent = new VBox(16);
        formContent.setPadding(new Insets(16, 0, 0, 0));
        
        // Profile section
        VBox profileSection = createProfileSettingsSection();
        formContent.getChildren().add(profileSection);
        
        // Preferences section
        VBox preferencesSection = createPreferencesSettingsSection();
        formContent.getChildren().add(preferencesSection);
        
        // Security section
        VBox securitySection = createSecuritySettingsSection();
        formContent.getChildren().add(securitySection);
        
        // Save button
        Button saveBtn = new Button("💾 Save Changes");
        saveBtn.setStyle(HorizonDesignSystem.buttonPrimary() + "-fx-padding: 12 24 12 24; -fx-background-radius: 999px; -fx-border-radius: 999px; -fx-font-size: 12;");
        saveBtn.setPrefWidth(150);
        HorizonDesignSystem.installButtonMotion(saveBtn);
        saveBtn.setOnAction(e -> handleSaveSettings());
        formContent.getChildren().add(saveBtn);
        
        editScroll.setContent(formContent);
        VBox.setVgrow(editScroll, Priority.ALWAYS);
        faceEditMode.getChildren().add(editScroll);
    }

    private VBox createProfileSettingsSection() {
        VBox section = new VBox(12);
        section.setStyle(profileInnerCardStyle(16));
        
        Label sectionTitle = new Label("Profile Settings");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        sectionTitle.setTextFill(Color.WHITE);
        section.getChildren().add(sectionTitle);
        
        // Avatar upload button
        Button avatarBtn = new Button("🖼️ Upload Avatar");
        avatarBtn.setStyle(HorizonDesignSystem.buttonGhost() + "-fx-padding: 10 16 10 16; -fx-background-radius: 14px; -fx-border-radius: 14px;");
        HorizonDesignSystem.installButtonMotion(avatarBtn);
        avatarBtn.setOnAction(e -> handleAvatarUpload());
        section.getChildren().add(avatarBtn);
        
        // Bio text field
        Label bioLabel = new Label("Bio");
        bioLabel.setFont(Font.font("Segoe UI", 10));
        bioLabel.setTextFill(Color.color(1, 1, 1, 0.6));
        
        TextField bioField = new TextField();
        styleProfileField(bioField);
        bioField.setPromptText("Tell us about yourself...");
        bioField.setPrefHeight(80);
        
        section.getChildren().addAll(bioLabel, bioField);
        return section;
    }

    private VBox createPreferencesSettingsSection() {
        VBox section = new VBox(12);
        section.setStyle(profileInnerCardStyle(16));
        
        Label sectionTitle = new Label("Preferences");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        sectionTitle.setTextFill(Color.WHITE);
        section.getChildren().add(sectionTitle);
        
        // Language preference
        Label langLabel = new Label("Language");
        langLabel.setFont(Font.font("Segoe UI", 10));
        langLabel.setTextFill(Color.color(1, 1, 1, 0.6));
        
        TextField langField = new TextField("English");
        styleProfileField(langField);
        
        // Theme preference
        Label themeLabel = new Label("Theme");
        themeLabel.setFont(Font.font("Segoe UI", 10));
        themeLabel.setTextFill(Color.color(1, 1, 1, 0.6));
        
        TextField themeField = new TextField("Dark");
        styleProfileField(themeField);
        
        section.getChildren().addAll(langLabel, langField, themeLabel, themeField);
        return section;
    }

    private VBox createSecuritySettingsSection() {
        VBox section = new VBox(12);
        section.setStyle(profileInnerCardStyle(16));
        
        Label sectionTitle = new Label("🔒 Security");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        sectionTitle.setTextFill(Color.WHITE);
        section.getChildren().add(sectionTitle);
        
        // Change password button
        Button changePasswordBtn = new Button("🔑 Change Password");
        changePasswordBtn.setStyle(HorizonDesignSystem.buttonGhost() + "-fx-padding: 10 16 10 16; -fx-background-radius: 14px; -fx-border-radius: 14px;");
        HorizonDesignSystem.installButtonMotion(changePasswordBtn);
        changePasswordBtn.setOnAction(e -> handleChangePassword());
        section.getChildren().add(changePasswordBtn);
        
        // 2FA toggle
        Button twoFABtn = new Button("📱 Enable 2FA/Biometrics");
        twoFABtn.setStyle(HorizonDesignSystem.buttonGhost() + "-fx-padding: 10 16 10 16; -fx-background-radius: 14px; -fx-border-radius: 14px;");
        HorizonDesignSystem.installButtonMotion(twoFABtn);
        twoFABtn.setOnAction(e -> handleTwoFA());
        section.getChildren().add(twoFABtn);
        
        return section;
    }

    private String formatRole(String role) {
        return switch (role) {
            case "ADMIN" -> "🔐 Administrator";
            case "OWNER" -> "👑 Property Owner";
            case "RESIDENT" -> "👤 Resident";
            default -> role;
        };
    }

    private String profileInnerCardStyle(int radius) {
        return HorizonDesignSystem.webServiceCard(radius, false) + "-fx-padding: 18;";
    }

    private void styleProfileField(TextField field) {
        field.setStyle(HorizonDesignSystem.webServiceInput(12, false));
        field.focusedProperty().addListener((obs, oldValue, focused) ->
            field.setStyle(HorizonDesignSystem.webServiceInput(12, focused))
        );
    }

    private void handleAvatarUpload() {
        System.out.println("Avatar upload clicked");
    }

    private void handleChangePassword() {
        System.out.println("Change password clicked");
    }

    private void handleTwoFA() {
        System.out.println("2FA setup clicked");
    }

    private void handleSaveSettings() {
        System.out.println("Settings saved");
        showViewMode();
    }

    private void showViewMode() {
        animateSwitcherFace(true);
    }

    private void showEditMode() {
        animateSwitcherFace(false);
    }

    private void animateSwitcherFace(boolean showView) {
        VBox toShow = showView ? faceViewMode : faceEditMode;
        VBox toHide = showView ? faceEditMode : faceViewMode;
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toHide);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            toHide.setVisible(false);
            toHide.setManaged(false);
        });
        
        toShow.setVisible(true);
        toShow.setManaged(true);
        toShow.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toShow);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        fadeOut.play();
        fadeIn.play();
    }

    public VBox getRoot() {
        return root;
    }
}
