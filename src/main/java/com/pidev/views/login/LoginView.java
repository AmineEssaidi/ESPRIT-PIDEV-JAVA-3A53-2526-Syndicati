package com.pidev.views.login;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import com.pidev.interfaces.ViewInterface;
import com.pidev.utils.theme.ThemeManager;
import com.pidev.components.shared.ImageBackground;
import com.pidev.components.shared.ConnectionStatusPill;

/**
 * Login View - Beautiful login page with liquid glass design and background video
 */
public class LoginView implements ViewInterface {
    
    private final StackPane root;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button signUpButton;
    private MediaPlayer mediaPlayer;
    private ImageBackground imageBackground;
    private VBox loginContainer;
    private VBox signUpContainer;
    private VBox forgotPasswordContainer;
    private boolean isSignUpMode = false;
    private boolean isForgotPasswordMode = false;
    
    public LoginView() {
        this.root = new StackPane();
        setupLayout();
    }
    
    private void setupLayout() {
        // Image background
        imageBackground = new ImageBackground();
        root.getChildren().add(imageBackground.getRoot());
        
        // Set login video background
        imageBackground.setLoginMode();
        
        // Create the main login container
        loginContainer = createLoginContainer();
        
        // Create the sign up container
        signUpContainer = createSignUpContainer();
        
        // Create the forgot password container
        forgotPasswordContainer = createForgotPasswordContainer();
        
        // Add login container to root (initially visible)
        root.getChildren().add(loginContainer);
        loginContainer.setVisible(true);
        loginContainer.setManaged(true);
        
        // Add sign up container to root (initially hidden)
        root.getChildren().add(signUpContainer);
        signUpContainer.setVisible(false);
        signUpContainer.setManaged(false);
        
        // Add forgot password container to root (initially hidden)
        root.getChildren().add(forgotPasswordContainer);
        forgotPasswordContainer.setVisible(false);
        forgotPasswordContainer.setManaged(false);
        
        // Create window bar (like landing page)
        HBox windowBar = createWindowBar();
        
        // Create horizontal container for both controls (ensure it doesn't block clicks under empty bounds)
        HBox topRightControls = new HBox();
        topRightControls.setSpacing(12);
        topRightControls.setAlignment(Pos.TOP_RIGHT);
        topRightControls.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        topRightControls.setPickOnBounds(false);
        
        // Create connection status pill
        ConnectionStatusPill connectionPill = new ConnectionStatusPill();
        
        // Create theme toggle
        StackPane themeToggle = createThemeToggle();
        
        // Add both to horizontal container
        topRightControls.getChildren().addAll(connectionPill.getPillContainer(), themeToggle);
        
        // Add window bar to root as a separate layer on top
        root.getChildren().add(windowBar);
        StackPane.setAlignment(windowBar, Pos.TOP_LEFT);
        
        // Add the horizontal container to root (positioned below window bar)
        root.getChildren().add(topRightControls);
        StackPane.setAlignment(topRightControls, Pos.TOP_RIGHT);
        StackPane.setMargin(topRightControls, new Insets(52, 20, 0, 0)); // Top margin 52px to go below window bar
        // Keep login UI on top of overlays
        loginContainer.toFront();
        
        // Apply theme styling
        applyThemeStyling();
    }
    
    
    private VBox createLoginContainer() {
        VBox container = new VBox();
        container.setSpacing(30);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(60, 80, 60, 80));
        container.setMaxWidth(400);
        container.setMaxHeight(500);
        
        // Apply liquid glass effect with consistent colors
        ThemeManager themeManager = ThemeManager.getInstance();
        container.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;" +
            "-fx-background-insets: 0, 0, 0, 0;"
        );
        
        // Add glassmorphism shadow
        DropShadow glassShadow = new DropShadow();
        glassShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        glassShadow.setColor(Color.color(0, 0, 0, 0.3));
        glassShadow.setRadius(30);
        glassShadow.setOffsetX(0);
        glassShadow.setOffsetY(10);
        container.setEffect(glassShadow);
        
        // App title
    Text title = new Text("Dynamic Island App");
    title.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 32));
        title.setFill(Color.web(ThemeManager.getInstance().getTextColor()));
        title.setTextAlignment(TextAlignment.CENTER);
        
        // Subtitle
    Text subtitle = new Text("Welcome back! Please sign in to continue");
    subtitle.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 16));
        subtitle.setFill(Color.web(ThemeManager.getInstance().getSecondaryTextColor()));
        subtitle.setTextAlignment(TextAlignment.CENTER);
        
        // Username field
        VBox usernameContainer = createInputField("👤", "Username or Email");
        usernameField = (TextField) usernameContainer.getChildren().get(1);
        
        // Password field
        VBox passwordContainer = createInputField("🔒", "Password");
        passwordField = (PasswordField) passwordContainer.getChildren().get(1);
        
        // Create button container for side-by-side buttons
        HBox buttonContainer = createButtonContainer();
        
        // Forgot password link
    Hyperlink forgotPassword = new Hyperlink("Forgot your password?");
    forgotPassword.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        forgotPassword.setTextFill(Color.color(1, 1, 1, 0.7));
        forgotPassword.setOnAction(e -> switchToForgotPassword());
        
        // Add all elements to container
        container.getChildren().addAll(
            title, subtitle, 
            usernameContainer, passwordContainer, 
            buttonContainer, forgotPassword
        );
        
        return container;
    }
    
    private HBox createButtonContainer() {
        HBox buttonContainer = new HBox();
        buttonContainer.setSpacing(8);
        buttonContainer.setAlignment(Pos.CENTER);
        
        // Create sign in button
        loginButton = createLoginButton();
        loginButton.setPrefWidth(146); // Half of original 300px width minus spacing
        loginButton.setMaxWidth(146);
        loginButton.setMinWidth(146);
        
        // Create sign up button
        signUpButton = createSignUpButton();
        signUpButton.setPrefWidth(146); // Half of original 300px width minus spacing
        signUpButton.setMaxWidth(146);
        signUpButton.setMinWidth(146);
        
        buttonContainer.getChildren().addAll(loginButton, signUpButton);
        return buttonContainer;
    }
    
    private VBox createSignUpContainer() {
        VBox container = new VBox();
        container.setSpacing(20); // Reduced from 30
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40, 60, 40, 60)); // Reduced from 60, 80, 60, 80
        container.setMaxWidth(380); // Reduced from 400
        container.setMaxHeight(550); // Reduced from 600
        
        // Apply liquid glass effect with consistent colors
        ThemeManager themeManager = ThemeManager.getInstance();
        container.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;" +
            "-fx-background-insets: 0, 0, 0, 0;"
        );
        
        // Add glassmorphism shadow
        DropShadow glassShadow = new DropShadow();
        glassShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        glassShadow.setColor(Color.color(0, 0, 0, 0.3));
        glassShadow.setRadius(30);
        glassShadow.setOffsetX(0);
        glassShadow.setOffsetY(10);
        container.setEffect(glassShadow);
        
        // App title
    Text title = new Text("Dynamic Island App");
    title.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 26)); // Reduced from 32
        title.setFill(Color.web(ThemeManager.getInstance().getTextColor()));
        title.setTextAlignment(TextAlignment.CENTER);
        
        // Subtitle
    Text subtitle = new Text("Create your account to get started");
    subtitle.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13)); // Reduced from 16
        subtitle.setFill(Color.web(ThemeManager.getInstance().getSecondaryTextColor()));
        subtitle.setTextAlignment(TextAlignment.CENTER);
        
        // Sign up fields
        VBox fullNameContainer = createInputField("👤", "Full Name");
        VBox emailContainer = createInputField("📧", "Email Address");
        VBox usernameSignUpContainer = createInputField("👤", "Username");
        VBox passwordSignUpContainer = createInputField("🔒", "Password");
        VBox confirmPasswordContainer = createInputField("🔒", "Confirm Password");
        
        // Sign up button
        Button signUpSubmitButton = createSignUpSubmitButton();
        
        // Back to login link
    Hyperlink backToLogin = new Hyperlink("Already have an account? Sign in");
    backToLogin.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        backToLogin.setTextFill(Color.color(1, 1, 1, 0.7));
        backToLogin.setOnAction(e -> switchToLogin());
        
        // Add all elements to container
        container.getChildren().addAll(
            title, subtitle, 
            fullNameContainer, emailContainer, usernameSignUpContainer,
            passwordSignUpContainer, confirmPasswordContainer,
            signUpSubmitButton, backToLogin
        );
        
        return container;
    }
    
    private VBox createForgotPasswordContainer() {
        VBox container = new VBox();
        container.setSpacing(20); // Reduced from 30
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40, 60, 40, 60)); // Reduced from 60, 80, 60, 80
        container.setMaxWidth(380); // Reduced from 400
        container.setMaxHeight(450); // Reduced from 500
        
        // Apply liquid glass effect with consistent colors
        ThemeManager themeManager = ThemeManager.getInstance();
        container.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;" +
            "-fx-background-insets: 0, 0, 0, 0;"
        );
        
        // Add glassmorphism shadow
        DropShadow glassShadow = new DropShadow();
        glassShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        glassShadow.setColor(Color.color(0, 0, 0, 0.3));
        glassShadow.setRadius(30);
        glassShadow.setOffsetX(0);
        glassShadow.setOffsetY(10);
        container.setEffect(glassShadow);
        
        // App title
    Text title = new Text("Dynamic Island App");
    title.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 26)); // Reduced from 32
        title.setFill(Color.web(ThemeManager.getInstance().getTextColor()));
        title.setTextAlignment(TextAlignment.CENTER);
        
        // Subtitle
    Text subtitle = new Text("Reset your password");
    subtitle.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13)); // Reduced from 16
        subtitle.setFill(Color.web(ThemeManager.getInstance().getSecondaryTextColor()));
        subtitle.setTextAlignment(TextAlignment.CENTER);
        
        // Description
    Text description = new Text("Enter your username, email, or phone number and we'll send you a reset link or OTP");
    description.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12)); // Reduced from 14
        description.setFill(Color.color(1, 1, 1, 0.7));
        description.setTextAlignment(TextAlignment.CENTER);
        description.setWrappingWidth(300); // Reduced from 350
        
        // Recovery field
        VBox recoveryContainer = createCenteredInputField("🔍", "Username, Email, or Phone Number");
        
        // Send reset button
        Button sendResetButton = createSendResetButton();
        
        // Back to login link
    Hyperlink backToLogin = new Hyperlink("Remember your password? Sign in");
    backToLogin.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        backToLogin.setTextFill(Color.color(1, 1, 1, 0.7));
        backToLogin.setOnAction(e -> switchToLogin());
        
        // Add all elements to container
        container.getChildren().addAll(
            title, subtitle, description,
            recoveryContainer, sendResetButton, backToLogin
        );
        
        return container;
    }
    
    private VBox createCenteredInputField(String icon, String placeholder) {
        VBox container = new VBox();
        container.setSpacing(8);
        container.setAlignment(Pos.CENTER); // Center alignment instead of CENTER_LEFT
        
        // Icon and label
        HBox labelContainer = new HBox();
        labelContainer.setSpacing(8);
        labelContainer.setAlignment(Pos.CENTER); // Center alignment
        
    Text iconText = new Text(icon);
    iconText.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 16));
        iconText.setFill(Color.color(1, 1, 1, 0.8));
        
    Text label = new Text(placeholder);
    label.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        label.setFill(Color.color(1, 1, 1, 0.8));
        
        labelContainer.getChildren().addAll(iconText, label);
        
        // Input field
        TextField inputField = new TextField();
    inputField.setPromptText(placeholder);
    inputField.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        inputField.setPrefHeight(45);
        inputField.setMaxWidth(300);
        
        // Apply glassmorphism styling to input field
        ThemeManager themeManager = ThemeManager.getInstance();
        inputField.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255, 255, 255, 0.6);" +
            "-fx-padding: 12px 16px;"
        );
        
        // Add focus effects
        inputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                inputField.setStyle(
                    "-fx-background-color: " + themeManager.getLiquidGlassHover() + ";" +
                    "-fx-background-radius: 12px;" +
                    "-fx-border-color: " + themeManager.getLiquidGlassFocus() + ";" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 12px;" +
                    "-fx-text-fill: white;" +
                    "-fx-prompt-text-fill: rgba(255, 255, 255, 0.6);" +
                    "-fx-padding: 12px 16px;"
                );
            } else {
                inputField.setStyle(
                    "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
                    "-fx-background-radius: 12px;" +
                    "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 12px;" +
                    "-fx-text-fill: white;" +
                    "-fx-prompt-text-fill: rgba(255, 255, 255, 0.6);" +
                    "-fx-padding: 12px 16px;"
                );
            }
        });
        
        container.getChildren().addAll(labelContainer, inputField);
        
        return container;
    }
    
    private VBox createInputField(String icon, String placeholder) {
        VBox container = new VBox();
        container.setSpacing(8);
        container.setAlignment(Pos.CENTER_LEFT);
        
        // Icon and label
        HBox labelContainer = new HBox();
        labelContainer.setSpacing(8);
        labelContainer.setAlignment(Pos.CENTER_LEFT);
        
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(16));
        iconText.setFill(Color.web(ThemeManager.getInstance().getTextColor()));
        
        Text label = new Text(placeholder);
        label.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        label.setFill(Color.web(ThemeManager.getInstance().getTextColor()));        labelContainer.getChildren().addAll(iconText, label);
        
        // Input field
        TextField inputField;
        if (placeholder.contains("Password")) {
            inputField = new PasswordField();
        } else {
            inputField = new TextField();
        }
        
    inputField.setPromptText(placeholder);
    inputField.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        inputField.setPrefHeight(45);
        inputField.setMaxWidth(300);
        
        // Apply glassmorphism styling to input field
        ThemeManager themeManager = ThemeManager.getInstance();
        inputField.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255, 255, 255, 0.6);" +
            "-fx-padding: 12px 16px;"
        );
        
        // Add focus effects
        inputField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                inputField.setStyle(
                    "-fx-background-color: " + themeManager.getLiquidGlassHover() + ";" +
                    "-fx-background-radius: 12px;" +
                    "-fx-border-color: " + themeManager.getLiquidGlassFocus() + ";" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 12px;" +
                    "-fx-text-fill: white;" +
                    "-fx-prompt-text-fill: rgba(255, 255, 255, 0.6);" +
                    "-fx-padding: 12px 16px;"
                );
            } else {
                inputField.setStyle(
                    "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
                    "-fx-background-radius: 12px;" +
                    "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 12px;" +
                    "-fx-text-fill: white;" +
                    "-fx-prompt-text-fill: rgba(255, 255, 255, 0.6);" +
                    "-fx-padding: 12px 16px;"
                );
            }
        });
        
        container.getChildren().addAll(labelContainer, inputField);
        
        return container;
    }
    
    private Button createLoginButton() {
    Button button = new Button("Sign In");
    button.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 16));
        button.setPrefHeight(50);
        
        // Apply liquid glass styling
        ThemeManager themeManager = ThemeManager.getInstance();
        button.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
        
        // Add shadow effect
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        buttonShadow.setColor(Color.color(0, 0, 0, 0.3));
        buttonShadow.setRadius(15);
        buttonShadow.setOffsetX(0);
        buttonShadow.setOffsetY(5);
        button.setEffect(buttonShadow);
        
        // Add hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getLiquidGlassHover() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + themeManager.getLiquidGlassFocus() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        
        // Add click handler
        button.setOnAction(e -> handleLogin());
        
        return button;
    }
    
    private Button createSignUpButton() {
    Button button = new Button("Sign Up");
    button.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 16));
        button.setPrefHeight(50);
        
        // Apply dark red liquid glass styling
        ThemeManager themeManager = ThemeManager.getInstance();
        button.setStyle(
            "-fx-background-color: " + themeManager.getDarkRedLiquidGlassBackground() + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + themeManager.getDarkRedLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
        
        // Add shadow effect
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        buttonShadow.setColor(Color.color(0, 0, 0, 0.3));
        buttonShadow.setRadius(15);
        buttonShadow.setOffsetX(0);
        buttonShadow.setOffsetY(5);
        button.setEffect(buttonShadow);
        
        // Add hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getDarkRedLiquidGlassHover() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + themeManager.getDarkRedLiquidGlassFocus() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getDarkRedLiquidGlassBackground() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + themeManager.getDarkRedLiquidGlassBorder() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        
        // Add click handler
        button.setOnAction(e -> switchToSignUp());
        
        return button;
    }
    
    private Button createSignUpSubmitButton() {
    Button button = new Button("Create Account");
    button.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 16));
        button.setPrefHeight(50);
        button.setMaxWidth(300);
        button.setMinWidth(300);
        
        // Apply liquid glass styling
        ThemeManager themeManager = ThemeManager.getInstance();
        button.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
        
        // Add shadow effect
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        buttonShadow.setColor(Color.color(0, 0, 0, 0.3));
        buttonShadow.setRadius(15);
        buttonShadow.setOffsetX(0);
        buttonShadow.setOffsetY(5);
        button.setEffect(buttonShadow);
        
        // Add hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getLiquidGlassHover() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + themeManager.getLiquidGlassFocus() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        
        // Add click handler
        button.setOnAction(e -> handleSignUp());
        
        return button;
    }
    
    private Button createSendResetButton() {
    Button button = new Button("Send Reset Link");
    button.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 16));
        button.setPrefHeight(50);
        button.setMaxWidth(300);
        button.setMinWidth(300);
        
        // Apply liquid glass styling
        ThemeManager themeManager = ThemeManager.getInstance();
        button.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
        
        // Add shadow effect
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        buttonShadow.setColor(Color.color(0, 0, 0, 0.3));
        buttonShadow.setRadius(15);
        buttonShadow.setOffsetX(0);
        buttonShadow.setOffsetY(5);
        button.setEffect(buttonShadow);
        
        // Add hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getLiquidGlassHover() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + themeManager.getLiquidGlassFocus() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        
        // Add click handler
        button.setOnAction(e -> handleForgotPassword());
        
        return button;
    }
    
    private void switchToSignUp() {
        isSignUpMode = true;
        isForgotPasswordMode = false;
        loginContainer.setVisible(false);
        loginContainer.setManaged(false);
        signUpContainer.setVisible(true);
        signUpContainer.setManaged(true);
        forgotPasswordContainer.setVisible(false);
        forgotPasswordContainer.setManaged(false);
        signUpContainer.toFront();
    }
    
    private void switchToLogin() {
        isSignUpMode = false;
        isForgotPasswordMode = false;
        signUpContainer.setVisible(false);
        signUpContainer.setManaged(false);
        forgotPasswordContainer.setVisible(false);
        forgotPasswordContainer.setManaged(false);
        loginContainer.setVisible(true);
        loginContainer.setManaged(true);
        loginContainer.toFront();
    }
    
    private void switchToForgotPassword() {
        isSignUpMode = false;
        isForgotPasswordMode = true;
        loginContainer.setVisible(false);
        loginContainer.setManaged(false);
        signUpContainer.setVisible(false);
        signUpContainer.setManaged(false);
        forgotPasswordContainer.setVisible(true);
        forgotPasswordContainer.setManaged(true);
        forgotPasswordContainer.toFront();
    }
    
    private void handleSignUp() {
        // Placeholder for sign up logic
        System.out.println("Sign up clicked - implement sign up logic here");
        // For now, just switch back to login
        switchToLogin();
    }
    
    private void handleForgotPassword() {
        // Placeholder for forgot password logic
        System.out.println("Forgot password clicked - implement password reset logic here");
        // For now, just switch back to login
        switchToLogin();
    }
    
    private StackPane createTopRightControls() {
        // Create horizontal container for connection pill and theme toggle
        HBox controlsHBox = new HBox();
        controlsHBox.setSpacing(12);
        controlsHBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Create connection status pill
        ConnectionStatusPill connectionPill = new ConnectionStatusPill();
        
        // Create theme toggle
        StackPane themeToggle = createThemeToggle();
        
        // Add both to horizontal container
        controlsHBox.getChildren().addAll(connectionPill.getPillContainer(), themeToggle);
        
        // Create container for positioning
        StackPane controlsContainer = new StackPane();
        controlsContainer.getChildren().add(controlsHBox);
        
        // Position in top right corner using StackPane alignment
        StackPane.setAlignment(controlsContainer, Pos.TOP_RIGHT);
        
        // Set margins from the edges
        StackPane.setMargin(controlsContainer, new Insets(20, 20, 0, 0));
        
        return controlsContainer;
    }
    
    private StackPane createThemeToggle() {
        StackPane toggleContainer = new StackPane();
        
        // Set the container size to exactly match the toggle button
        toggleContainer.setPrefSize(65, 35);
        toggleContainer.setMaxSize(65, 35);
        toggleContainer.setMinSize(65, 35);
        
        // Toggle track (background) - more rounded with theme-aware colors
        javafx.scene.shape.Rectangle track = new javafx.scene.shape.Rectangle(65, 35);
        track.setArcWidth(35); // Fully rounded
        track.setArcHeight(35);
        
        final ThemeManager themeManager = ThemeManager.getInstance();
        track.setFill(Color.web(themeManager.getToggleTrackBackground()));
        track.setStroke(Color.web(themeManager.getToggleTrackBorder()));
        track.setStrokeWidth(1);
        
        // Toggle thumb container (will hold the icon)
        StackPane thumbContainer = new StackPane();
        thumbContainer.setTranslateX(themeManager.isDarkMode() ? -16 : 16); // Position based on current theme
        
        // Moon icon (dark mode)
        javafx.scene.text.Text moonIcon = new javafx.scene.text.Text("☾");
        moonIcon.setFont(javafx.scene.text.Font.font(16));
        moonIcon.setFill(Color.WHITE);
        moonIcon.setOpacity(themeManager.isDarkMode() ? 1.0 : 0.0);
        
        // Sun icon (light mode)
        javafx.scene.text.Text sunIcon = new javafx.scene.text.Text("☀");
        sunIcon.setFont(javafx.scene.text.Font.font(16));
        sunIcon.setFill(Color.web("#f59e0b")); // Modern amber color
        sunIcon.setOpacity(themeManager.isDarkMode() ? 0.0 : 1.0);
        
        // Add shadow to thumb container
        javafx.scene.effect.DropShadow thumbShadow = new javafx.scene.effect.DropShadow();
        thumbShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        if (themeManager.isDarkMode()) {
            thumbShadow.setColor(Color.color(0, 0, 0, 0.3));
        } else {
            thumbShadow.setColor(Color.color(0, 0, 0, 0.15));
        }
        thumbShadow.setRadius(6);
        thumbShadow.setOffsetX(0);
        thumbShadow.setOffsetY(3);
        thumbContainer.setEffect(thumbShadow);
        
        // Add icons to thumb container
        thumbContainer.getChildren().addAll(moonIcon, sunIcon);
        
        // Add all elements to container
        toggleContainer.getChildren().addAll(track, thumbContainer);
        
        // Add click handler for toggle
        toggleContainer.setOnMouseClicked(e -> toggleTheme(thumbContainer, sunIcon, moonIcon, track));
        
        // Add hover effect - only on the track
        track.setOnMouseEntered(e -> {
            track.setFill(Color.web(themeManager.getToggleTrackHover()));
        });
        track.setOnMouseExited(e -> {
            track.setFill(Color.web(themeManager.getToggleTrackBackground()));
        });
        
        return toggleContainer;
    }
    
    private void toggleTheme(StackPane thumbContainer, javafx.scene.text.Text sunIcon, 
                           javafx.scene.text.Text moonIcon, javafx.scene.shape.Rectangle track) {
        // Toggle theme in theme manager
        final ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.toggleTheme();
        
        // Keep login video regardless of theme changes
        if (imageBackground != null) {
            imageBackground.setLoginMode();
        }
        
        // Animate thumb movement
        javafx.animation.TranslateTransition thumbAnimation = new javafx.animation.TranslateTransition(
            javafx.util.Duration.millis(300), thumbContainer);
        
        // Animate icon transitions
        javafx.animation.FadeTransition sunFade = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(300), sunIcon);
        javafx.animation.FadeTransition moonFade = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(300), moonIcon);
        
        if (themeManager.isDarkMode()) {
            // Move to dark mode position (left) and show moon
            thumbAnimation.setToX(-16);
            sunFade.setToValue(0.0); // Hide sun
            moonFade.setToValue(1.0); // Show moon
        } else {
            // Move to light mode position (right) and show sun
            thumbAnimation.setToX(16);
            sunFade.setToValue(1.0); // Show sun
            moonFade.setToValue(0.0); // Hide moon
        }
        
        thumbAnimation.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        sunFade.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        moonFade.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        
        // Play all animations together
        javafx.animation.ParallelTransition parallelTransition = new javafx.animation.ParallelTransition();
        parallelTransition.getChildren().addAll(thumbAnimation, sunFade, moonFade);
        parallelTransition.play();
        
        // Update styling after animation
        parallelTransition.setOnFinished(e -> {
            track.setFill(Color.web(themeManager.getToggleTrackBackground()));
            track.setStroke(Color.web(themeManager.getToggleTrackBorder()));
        });
        
        System.out.println("Theme switched to: " + (themeManager.isDarkMode() ? "Dark" : "Light"));
    }
    
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        // Simple validation for admin/admin
        if ("admin".equals(username) && "admin".equals(password)) {
            System.out.println("Login successful! Navigating to landing page...");
            // Navigate to landing page
            navigateToLandingPage();
        } else {
            // Show error message
            showErrorMessage("Invalid credentials. Please use admin/admin for now.");
        }
    }
    
    private void showErrorMessage(String message) {
        // Create a temporary error message
    Label errorLabel = new Label(message);
    errorLabel.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        errorLabel.setTextFill(Color.color(1, 0.3, 0.3, 1)); // Red color
        errorLabel.setStyle(
            "-fx-background-color: rgba(255, 0, 0, 0.1);" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 16px;"
        );
        
        // Add to root temporarily
        root.getChildren().add(errorLabel);
        
        // Remove after 3 seconds
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> {
                root.getChildren().remove(errorLabel);
            })
        );
        timeline.play();
    }
    
    private void navigateToLandingPage() {
        // This will be handled by the main application
        System.out.println("Login successful - ready to navigate to landing page");
        // The main application will handle the navigation
    }
    
    private void applyThemeStyling() {
        // Login page has its own styling, but we can add theme-aware elements if needed
        root.setStyle("-fx-background-color: transparent;");
    }
    
    @Override
    public Pane getRoot() {
        return root;
    }
    
    // Custom window bar with drag/min/restore/close - macOS style (same as LandingPageView)
    private HBox createWindowBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setSpacing(8);
        bar.setPadding(new Insets(4, 16, 4, 16));
        bar.setPrefHeight(40);
        bar.setMinHeight(40);
        bar.setMaxHeight(40);
        bar.setStyle("-fx-background-color: transparent; -fx-background-radius: 0;");
        bar.setPickOnBounds(false);
        bar.setMouseTransparent(false);

        Region dragRegion = new Region();
        HBox.setHgrow(dragRegion, Priority.ALWAYS);
        dragRegion.setMinHeight(40);
        dragRegion.setPrefHeight(40);
        dragRegion.setStyle("-fx-cursor: move;");

        // macOS-style circular window control buttons
        javafx.scene.control.Button btnMin = new javafx.scene.control.Button("");
        javafx.scene.control.Button btnMax = new javafx.scene.control.Button("");
        javafx.scene.control.Button btnClose = new javafx.scene.control.Button("");

        styleMacOSButton(btnMin, "#febc2e", "−");
        styleMacOSButton(btnMax, "#28c840", "☐");
        styleMacOSButton(btnClose, "#ff5f57", "✕");

        HBox buttonContainer = new HBox(8);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        buttonContainer.setPadding(new Insets(0));
        buttonContainer.getChildren().addAll(btnMin, btnMax, btnClose);
        buttonContainer.setPickOnBounds(false);
        buttonContainer.setMouseTransparent(false);

        btnMin.setOnAction(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            stage.setIconified(true);
        });
        btnMax.setOnAction(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        });
        btnClose.setOnAction(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            stage.close();
        });

        final double[] dragOffset = new double[2];
        dragRegion.setMouseTransparent(false);
        buttonContainer.setMouseTransparent(false);
        dragRegion.setPickOnBounds(true);
        buttonContainer.setPickOnBounds(false);

        // Standard drag logic
        dragRegion.setOnMousePressed(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            dragOffset[0] = e.getScreenX() - stage.getX();
            dragOffset[1] = e.getScreenY() - stage.getY();
        });
        dragRegion.setOnMouseDragged(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            if (!stage.isMaximized()) {
                stage.setX(e.getScreenX() - dragOffset[0]);
                stage.setY(e.getScreenY() - dragOffset[1]);
            }
        });

        bar.getChildren().addAll(dragRegion, buttonContainer);
        HBox.setHgrow(dragRegion, Priority.ALWAYS);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        return bar;
    }

    private void styleMacOSButton(javafx.scene.control.Button btn, String color, String symbol) {
        // Create circular buttons like macOS
        btn.setMinSize(12, 12);
        btn.setPrefSize(12, 12);
        btn.setMaxSize(12, 12);
        btn.setFocusTraversable(false);
        btn.setPickOnBounds(true);
        btn.setMouseTransparent(false);
        
        // macOS style: circular with solid color
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 6px;" +
            "-fx-border-color: rgba(0,0,0,0.2);" +
            "-fx-border-width: 0.5px;" +
            "-fx-border-radius: 6px;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 8px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: transparent;" +
            "-fx-padding: 0;"
        );
        
        // Store the symbol for hover effect
        final String btnSymbol = symbol;
        
        // Show symbol and darken on hover
        btn.setOnMouseEntered(e -> {
            btn.setText(btnSymbol);
            String darkerColor = color;
            if (color.equals("#ff5f57")) darkerColor = "#e04b42";
            if (color.equals("#febc2e")) darkerColor = "#d9a21a";
            if (color.equals("#28c840")) darkerColor = "#1fa931";
            
            btn.setStyle(
                "-fx-background-color: " + darkerColor + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-border-color: rgba(0,0,0,0.3);" +
                "-fx-border-width: 0.5px;" +
                "-fx-border-radius: 6px;" +
                "-fx-cursor: hand;" +
                "-fx-font-size: 8px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: rgba(0,0,0,0.7);" +
                "-fx-padding: 0;"
            );
        });
        
        // Hide symbol and restore color on exit
        btn.setOnMouseExited(e -> {
            btn.setText("");
            btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-border-color: rgba(0,0,0,0.2);" +
                "-fx-border-width: 0.5px;" +
                "-fx-border-radius: 6px;" +
                "-fx-cursor: hand;" +
                "-fx-font-size: 8px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: transparent;" +
                "-fx-padding: 0;"
            );
        });
    }
    
    @Override
    public void cleanup() {
        // Clean up media player if it exists
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        // Clean up image background
        if (imageBackground != null) {
            imageBackground.cleanup();
            // Remove image background from root
            root.getChildren().remove(imageBackground.getRoot());
            imageBackground = null;
        }
        // Clear all children from root to ensure no lingering backgrounds
        root.getChildren().clear();
        root.setStyle("-fx-background-color: transparent;");
        root.setBackground(null);
    }
    
    // Public method to check if login was successful
    public boolean isLoginSuccessful() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        return "admin".equals(username) && "admin".equals(password);
    }
}
