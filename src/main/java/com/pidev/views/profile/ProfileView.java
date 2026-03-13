package com.pidev.views.profile;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import com.pidev.interfaces.ViewInterface;
import com.pidev.utils.navigation.NavigationManager;
import com.pidev.utils.theme.ThemeManager;

/**
 * Profile View - Modern profile page with dynamic island design
 */
public class ProfileView implements ViewInterface {
    
    private final VBox root;
    
    public ProfileView() {
        this.root = new VBox();
        setupLayout();
    }
    
    private void setupLayout() {
        // Create the main content container
        VBox contentContainer = new VBox();
        contentContainer.setSpacing(30);
        contentContainer.setAlignment(Pos.CENTER);
        contentContainer.setPadding(new Insets(60, 40, 60, 40));
        
        // Profile header section
        VBox profileHeader = createProfileHeader();
        
        // Profile stats section
        HBox profileStats = createProfileStats();
        
        // Profile actions section
        HBox profileActions = createProfileActions();
        
        // Profile info section
        VBox profileInfo = createProfileInfo();
        
        contentContainer.getChildren().addAll(profileHeader, profileStats, profileActions, profileInfo);
        
        // Wrap content in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(contentContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        // Set the scroll pane as the root
        root.getChildren().add(scrollPane);
        
        applyThemeStyling();
    }
    
    private VBox createProfileHeader() {
        VBox header = new VBox();
        header.setSpacing(20);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 40, 30, 40));
        
        ThemeManager themeManager = ThemeManager.getInstance();
        header.setStyle(
            "-fx-background-color: " + themeManager.getDynamicIslandBackground() + ";" +
            "-fx-background-radius: 24px;" +
            "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 24px;"
        );
        
        // Add shadow effect
        DropShadow headerShadow = new DropShadow();
        headerShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        if (themeManager.isDarkMode()) {
            headerShadow.setColor(Color.color(0, 0, 0, 0.3));
        } else {
            headerShadow.setColor(Color.color(0, 0, 0, 0.1));
        }
        headerShadow.setRadius(15);
        headerShadow.setOffsetX(0);
        headerShadow.setOffsetY(8);
        header.setEffect(headerShadow);
        
        // Profile avatar
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPadding(new Insets(10));
        
        Circle avatar = new Circle(60);
        avatar.setFill(Color.web(themeManager.getModernAccentColor()));
        
        // Add glow effect to avatar
        DropShadow avatarGlow = new DropShadow();
        avatarGlow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        avatarGlow.setColor(Color.color(0.424, 0.361, 0.906, 0.4));
        avatarGlow.setRadius(20);
        avatarGlow.setOffsetX(0);
        avatarGlow.setOffsetY(0);
        avatar.setEffect(avatarGlow);
        
        // Avatar icon
        Text avatarIcon = new Text("👤");
        avatarIcon.setFont(Font.font(40));
        avatarIcon.setFill(Color.web(com.pidev.utils.theme.ThemeManager.getInstance().getTextColor()));
        
        avatarContainer.getChildren().addAll(avatar, avatarIcon);
        
        // Profile name
        Text profileName = new Text("Amine");
        profileName.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 32));
        profileName.setFill(Color.web(themeManager.getTextColor()));
        
        // Profile title
        Text profileTitle = new Text("Full Stack Developer");
        profileTitle.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 18));
        profileTitle.setFill(Color.web(themeManager.getSecondaryTextColor()));
        
        // Profile bio
        Text profileBio = new Text("Passionate about creating beautiful and functional applications.\nBuilding the future, one line of code at a time.");
        profileBio.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        profileBio.setFill(Color.web(themeManager.getSecondaryTextColor()));
        profileBio.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);        header.getChildren().addAll(avatarContainer, profileName, profileTitle, profileBio);
        
        return header;
    }
    
    private HBox createProfileStats() {
        HBox statsContainer = new HBox();
        statsContainer.setSpacing(20);
        statsContainer.setAlignment(Pos.CENTER);
        
        ThemeManager themeManager = ThemeManager.getInstance();
        
        // Projects stat
        VBox projectsStat = createStatCard("🚀", "Projects", "24", themeManager);
        
        // Experience stat
        VBox experienceStat = createStatCard("💼", "Experience", "3+ Years", themeManager);
        
        // Skills stat
        VBox skillsStat = createStatCard("⚡", "Skills", "15+", themeManager);
        
        statsContainer.getChildren().addAll(projectsStat, experienceStat, skillsStat);
        
        return statsContainer;
    }
    
    private VBox createStatCard(String icon, String label, String value, ThemeManager themeManager) {
        VBox statCard = new VBox();
        statCard.setSpacing(8);
        statCard.setAlignment(Pos.CENTER);
        statCard.setPadding(new Insets(20, 25, 20, 25));
        statCard.setMinWidth(120);
        
        statCard.setStyle(
            "-fx-background-color: " + themeManager.getDynamicIslandBackground() + ";" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 16px;"
        );
        
        // Add shadow effect
        DropShadow statShadow = new DropShadow();
        statShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        if (themeManager.isDarkMode()) {
            statShadow.setColor(Color.color(0, 0, 0, 0.3));
        } else {
            statShadow.setColor(Color.color(0, 0, 0, 0.1));
        }
        statShadow.setRadius(8);
        statShadow.setOffsetX(0);
        statShadow.setOffsetY(4);
        statCard.setEffect(statShadow);
        
        // Icon
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(24));
        
        // Value
        Text valueText = new Text(value);
        valueText.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 20));
        valueText.setFill(Color.web(themeManager.getTextColor()));
        
        // Label
        Text labelText = new Text(label);
        labelText.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12));
        labelText.setFill(Color.web(themeManager.getSecondaryTextColor()));        statCard.getChildren().addAll(iconText, valueText, labelText);
        
        // Add hover effect
        statCard.setOnMouseEntered(e -> {
            statCard.setStyle(
                "-fx-background-color: " + themeManager.getTabHoverColor() + ";" +
                "-fx-background-radius: 16px;" +
                "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 16px;"
            );
        });
        
        statCard.setOnMouseExited(e -> {
            statCard.setStyle(
                "-fx-background-color: " + themeManager.getDynamicIslandBackground() + ";" +
                "-fx-background-radius: 16px;" +
                "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 16px;"
            );
        });
        
        return statCard;
    }
    
    private HBox createProfileActions() {
        HBox actionsContainer = new HBox();
        actionsContainer.setSpacing(15);
        actionsContainer.setAlignment(Pos.CENTER);
        
        ThemeManager themeManager = ThemeManager.getInstance();
        
        // Edit Profile button
        Button editButton = createActionButton("✏️ Edit Profile", themeManager);
        editButton.setOnAction(e -> System.out.println("Edit Profile clicked"));
        
        // Settings button
        Button settingsButton = createActionButton("⚙️ Settings", themeManager);
        settingsButton.setOnAction(e -> System.out.println("Settings clicked"));
        
        // Back to Home button
        Button homeButton = createActionButton("🏠 Back to Home", themeManager);
        homeButton.setOnAction(e -> NavigationManager.getInstance().navigateTo("home"));
        
        actionsContainer.getChildren().addAll(editButton, settingsButton, homeButton);
        
        return actionsContainer;
    }
    
    private Button createActionButton(String text, ThemeManager themeManager) {
        Button button = new Button(text);
        button.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 14));
        button.setPadding(new Insets(12, 20, 12, 20));
        button.setMinWidth(140);        button.setStyle(
            "-fx-background-color: " + themeManager.getModernAccentColor() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-radius: 12px;"
        );
        
        // Add shadow effect
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        if (themeManager.isDarkMode()) {
            buttonShadow.setColor(Color.color(0, 0, 0, 0.3));
        } else {
            buttonShadow.setColor(Color.color(0, 0, 0, 0.2));
        }
        buttonShadow.setRadius(8);
        buttonShadow.setOffsetX(0);
        buttonShadow.setOffsetY(4);
        button.setEffect(buttonShadow);
        
        // Add hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getModernSecondaryColor() + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12px;" +
                "-fx-border-radius: 12px;"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getModernAccentColor() + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12px;" +
                "-fx-border-radius: 12px;"
            );
        });
        
        return button;
    }
    
    private VBox createProfileInfo() {
        VBox infoContainer = new VBox();
        infoContainer.setSpacing(15);
        infoContainer.setAlignment(Pos.CENTER);
        infoContainer.setPadding(new Insets(25, 40, 25, 40));
        
        ThemeManager themeManager = ThemeManager.getInstance();
        infoContainer.setStyle(
            "-fx-background-color: " + themeManager.getDynamicIslandBackground() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;"
        );
        
        // Add shadow effect
        DropShadow infoShadow = new DropShadow();
        infoShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        if (themeManager.isDarkMode()) {
            infoShadow.setColor(Color.color(0, 0, 0, 0.3));
        } else {
            infoShadow.setColor(Color.color(0, 0, 0, 0.1));
        }
        infoShadow.setRadius(10);
        infoShadow.setOffsetX(0);
        infoShadow.setOffsetY(5);
        infoContainer.setEffect(infoShadow);
        
        // Info title
        Text infoTitle = new Text("About Me");
        infoTitle.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 20));
        infoTitle.setFill(Color.web(themeManager.getTextColor()));        // Info content
        VBox infoContent = new VBox();
        infoContent.setSpacing(8);
        infoContent.setAlignment(Pos.CENTER_LEFT);
        
        String[] infoItems = {
            "📍 Location: Morocco",
            "🎓 Education: Computer Science",
            "💻 Languages: Java, JavaScript, Python",
            "🛠️ Tools: IntelliJ, VS Code, Git",
            "🌟 Interests: AI, Web Development, UI/UX"
        };
        
        for (String item : infoItems) {
            Text infoItem = new Text(item);
            infoItem.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
            infoItem.setFill(Color.web(themeManager.getSecondaryTextColor()));
            infoContent.getChildren().add(infoItem);
        }
        
        infoContainer.getChildren().addAll(infoTitle, infoContent);
        
        return infoContainer;
    }
    
    private void applyThemeStyling() {
        // Don't apply background color - let ImageBackground handle it
        root.setStyle("-fx-background-color: transparent;");
    }
    
    @Override
    public Pane getRoot() {
        return root;
    }
    
    @Override
    public void cleanup() {
        // Cleanup resources if needed
    }
}
