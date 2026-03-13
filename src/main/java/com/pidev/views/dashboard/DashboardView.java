package com.pidev.views.dashboard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import com.pidev.interfaces.ViewInterface;
import com.pidev.utils.navigation.NavigationManager;
import com.pidev.utils.theme.ThemeManager;

/**
 * Dashboard View - Main dashboard with stats and quick actions
 */
public class DashboardView implements ViewInterface {
    
    private final VBox root;
    
    public DashboardView() {
        this.root = new VBox();
        setupLayout();
    }
    
    private void setupLayout() {
        root.setSpacing(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(60, 40, 60, 40));
        
        applyThemeStyling();
        
        // Dashboard header
        VBox dashboardHeader = createDashboardHeader();
        
        // Stats grid
        GridPane statsGrid = createStatsGrid();
        
        // Quick actions
        HBox quickActions = createQuickActions();
        
        // Recent activity
        VBox recentActivity = createRecentActivity();
        
        root.getChildren().addAll(dashboardHeader, statsGrid, quickActions, recentActivity);
    }
    
    private VBox createDashboardHeader() {
        VBox header = new VBox();
        header.setSpacing(15);
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
        
        // Welcome text
        Text welcomeText = new Text("Welcome to Dashboard");
        welcomeText.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 32));
        welcomeText.setFill(Color.web(themeManager.getTextColor()));
        
        // Subtitle
        Text subtitle = new Text("Here's what's happening with your account");
        subtitle.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 18));
        subtitle.setFill(Color.web(themeManager.getSecondaryTextColor()));        header.getChildren().addAll(welcomeText, subtitle);
        
        return header;
    }
    
    private GridPane createStatsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);
        
        ThemeManager themeManager = ThemeManager.getInstance();
        
        // Create stat cards
        VBox totalUsers = createStatCard("👥", "Total Users", "1,234", themeManager);
        VBox activeProjects = createStatCard("🚀", "Active Projects", "12", themeManager);
        VBox completedTasks = createStatCard("✅", "Completed Tasks", "89", themeManager);
        VBox revenue = createStatCard("💰", "Revenue", "$45,678", themeManager);
        
        // Add to grid
        grid.add(totalUsers, 0, 0);
        grid.add(activeProjects, 1, 0);
        grid.add(completedTasks, 0, 1);
        grid.add(revenue, 1, 1);
        
        return grid;
    }
    
    private VBox createStatCard(String icon, String label, String value, ThemeManager themeManager) {
        VBox statCard = new VBox();
        statCard.setSpacing(10);
        statCard.setAlignment(Pos.CENTER);
        statCard.setPadding(new Insets(25, 30, 25, 30));
        statCard.setMinWidth(200);
        statCard.setMinHeight(150);
        
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
        statShadow.setRadius(10);
        statShadow.setOffsetX(0);
        statShadow.setOffsetY(5);
        statCard.setEffect(statShadow);
        
        // Icon
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(32));
        
        // Value
        Text valueText = new Text(value);
        valueText.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 24));
        valueText.setFill(Color.web(themeManager.getTextColor()));
        
        // Label
        Text labelText = new Text(label);
        labelText.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
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
    
    private HBox createQuickActions() {
        HBox actionsContainer = new HBox();
        actionsContainer.setSpacing(20);
        actionsContainer.setAlignment(Pos.CENTER);
        
        ThemeManager themeManager = ThemeManager.getInstance();
        
        // Create action buttons
        Button newProject = createActionButton("➕ New Project", themeManager);
        newProject.setOnAction(e -> System.out.println("New Project clicked"));
        
        Button viewReports = createActionButton("📊 View Reports", themeManager);
        viewReports.setOnAction(e -> System.out.println("View Reports clicked"));
        
        Button manageUsers = createActionButton("👥 Manage Users", themeManager);
        manageUsers.setOnAction(e -> System.out.println("Manage Users clicked"));
        
        Button settings = createActionButton("⚙️ Settings", themeManager);
        settings.setOnAction(e -> System.out.println("Settings clicked"));
        
        actionsContainer.getChildren().addAll(newProject, viewReports, manageUsers, settings);
        
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
    
    private VBox createRecentActivity() {
        VBox activityContainer = new VBox();
        activityContainer.setSpacing(15);
        activityContainer.setAlignment(Pos.CENTER);
        activityContainer.setPadding(new Insets(25, 40, 25, 40));
        
        ThemeManager themeManager = ThemeManager.getInstance();
        activityContainer.setStyle(
            "-fx-background-color: " + themeManager.getDynamicIslandBackground() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;"
        );
        
        // Add shadow effect
        DropShadow activityShadow = new DropShadow();
        activityShadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        if (themeManager.isDarkMode()) {
            activityShadow.setColor(Color.color(0, 0, 0, 0.3));
        } else {
            activityShadow.setColor(Color.color(0, 0, 0, 0.1));
        }
        activityShadow.setRadius(10);
        activityShadow.setOffsetX(0);
        activityShadow.setOffsetY(5);
        activityContainer.setEffect(activityShadow);
        
        // Activity title
        Text activityTitle = new Text("Recent Activity");
        activityTitle.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 20));
        activityTitle.setFill(Color.web(themeManager.getTextColor()));        // Activity items
        VBox activityItems = new VBox();
        activityItems.setSpacing(10);
        activityItems.setAlignment(Pos.CENTER_LEFT);
        
        String[] activities = {
            "🎯 New project 'Mobile App' created",
            "👤 User 'John Doe' registered",
            "✅ Task 'UI Design' completed",
            "📊 Monthly report generated",
            "🔧 System maintenance completed"
        };
        
        for (String activity : activities) {
            Text activityItem = new Text(activity);
            activityItem.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
            activityItem.setFill(Color.web(themeManager.getSecondaryTextColor()));
            activityItems.getChildren().add(activityItem);
        }
        
        activityContainer.getChildren().addAll(activityTitle, activityItems);
        
        return activityContainer;
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
