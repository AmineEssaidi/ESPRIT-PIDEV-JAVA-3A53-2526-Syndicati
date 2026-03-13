package com.pidev.components.shared;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlurType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import com.pidev.utils.theme.ThemeManager;
import com.pidev.utils.navigation.NavigationManager;
import com.pidev.components.shared.ConnectionStatusPill;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Dynamic Header Component - Main header with improved island hosting tabs
 */
public class DynamicHeader {
    
    private final StackPane root;
    private final ThemeManager themeManager;
    private HBox navSection;
    private HBox mainTabs;
    private boolean isExpanded = false;
    private String currentExpandedTab = null;
    private Runnable backgroundUpdateCallback;
    
    // Enhanced state management
    private Map<String, Button> mainTabButtons = new HashMap<>();
    private Map<String, Button> submenuButtons = new HashMap<>();
    private String activeTab = "Home";
    
    // Animation and layout properties
    private double originalNavSectionWidth;
    private double originalNavSectionHeight;
    private HBox subMenuContainer;
    private StackPane islandContainer;
    
    // Profile dropdown state
    private boolean isProfileDropdownOpen = false;
    private boolean isTabDropdownOpen = false;
    
    // Navigation island expansion
    private HBox navPillIsland;
    private HBox submenuContainer;
    
    // Profile pill expansion
    private StackPane profileContainer;
    private Circle profileIconRef;
    
    // Search pill expansion
    private StackPane searchContainer;
    private boolean isSearchExpanded = false;
    
    public DynamicHeader() {
        this.root = new StackPane();
        this.themeManager = ThemeManager.getInstance();
        
        setupLayout();
        applyThemeStyling();
    }
    
    private void setupLayout() {
        // Main header container - now transparent, only child elements are visible
        root.setPadding(new Insets(12, 16, 12, 16));
        
        // Transparent background - no visible big island
        root.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: transparent;" +
            "-fx-border-width: 0;"
        );
        
        // No shadow on the root container
        root.setEffect(null);
        
        // Create main layout with centered navigation pill
        HBox mainLayout = new HBox();
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setSpacing(0);
        
        // Left growing spacer
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        
        // Center section: Navigation pill island with tabs
        HBox navPillIsland = createNavigationPillIsland();
        
        // Right growing spacer (equal to left)
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        // Right section (connection pill, theme toggle, logout, profile)
        HBox rightSection = createProfileSection();
        
        mainLayout.getChildren().addAll(leftSpacer, navPillIsland, rightSpacer, rightSection);
        root.getChildren().add(mainLayout);
    }
    
    private HBox createNavigationPillIsland() {
        navPillIsland = new HBox();
        navPillIsland.setSpacing(25);
        navPillIsland.setAlignment(Pos.CENTER);
        navPillIsland.setPadding(new Insets(12, 150, 12, 150));
        
        // Liquid glass styling for the navigation pill
        navPillIsland.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 25px;" +
            "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 25px;"
        );
        
        // Add subtle shadow
        DropShadow pillShadow = new DropShadow();
        pillShadow.setBlurType(BlurType.GAUSSIAN);
        pillShadow.setColor(Color.color(0, 0, 0, 0.3));
        pillShadow.setRadius(12);
        pillShadow.setOffsetX(0);
        pillShadow.setOffsetY(3);
        navPillIsland.setEffect(pillShadow);
        
        // Create search icon pill on the left
        StackPane searchPill = createSearchIconPill();
        
        // Create navigation tabs with text labels
        Button homeTab = createNavPillTab("Home", true);
        Button servicesTab = createNavPillTab("Services", false);
        Button aboutTab = createNavPillTab("About", false);
        Button dashboardTab = createNavPillTab("Dashboard", false);
        
        // Store tab references
        mainTabButtons.put("Home", homeTab);
        mainTabButtons.put("Services", servicesTab);
        mainTabButtons.put("About", aboutTab);
        mainTabButtons.put("Dashboard", dashboardTab);
        
        navPillIsland.getChildren().addAll(searchPill, homeTab, servicesTab, aboutTab, dashboardTab);
        
        return navPillIsland;
    }
    
    private StackPane createSearchIconPill() {
        searchContainer = new StackPane();
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Create search icon (magnifying glass)
        Text searchIcon = new Text("🔍");
        searchIcon.setFont(Font.font(16));
        searchIcon.setFill(Color.WHITE);
        
        // Initial collapsed state - just the icon
        HBox collapsedSearch = new HBox();
        collapsedSearch.setAlignment(Pos.CENTER);
        collapsedSearch.setPadding(new Insets(8, 10, 8, 10));
        collapsedSearch.getChildren().add(searchIcon);
        collapsedSearch.setCursor(javafx.scene.Cursor.HAND);
        
        searchContainer.getChildren().add(collapsedSearch);
        
        // Hover effect - morph into expanded search pill
        searchContainer.setOnMouseEntered(e -> expandSearchPill());
        // Don't set onMouseExited here - let the expanded search handle it
        
        return searchContainer;
    }
    
    private void expandSearchPill() {
        if (isSearchExpanded) return;
        isSearchExpanded = true;
        
        // Create expanded search pill
        HBox expandedSearch = new HBox();
        expandedSearch.setAlignment(Pos.CENTER_LEFT);
        expandedSearch.setSpacing(8);
        expandedSearch.setPadding(new Insets(8, 16, 8, 16));
        expandedSearch.setPrefWidth(250);
        expandedSearch.setMaxWidth(250);
        
        // Liquid glass styling
        expandedSearch.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: black;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;"
        );
        
        // Search icon
        Text searchIcon = new Text("🔍");
        searchIcon.setFont(Font.font(16));
        searchIcon.setFill(Color.WHITE);
        
        // Create TextField for typing
        javafx.scene.control.TextField searchField = new javafx.scene.control.TextField();
        searchField.setPromptText("Search...");
        searchField.setPrefWidth(180);
        searchField.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255, 255, 255, 0.6);" +
            "-fx-font-size: 13px;" +
            "-fx-border-width: 0;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 0;"
        );
        searchField.setFocusTraversable(true);
        
        expandedSearch.getChildren().addAll(searchIcon, searchField);
        
        // Keep expanded when hovering or typing
        expandedSearch.setOnMouseEntered(e -> {
            // Keep expanded
        });
        
        // Prevent collapse on mouse exit if focused
        expandedSearch.setOnMouseExited(e -> {
            if (!searchField.isFocused()) {
                collapseSearchPill();
            }
        });
        
        // Collapse when focus is lost and not hovering
        searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !expandedSearch.isHover()) {
                collapseSearchPill();
            }
        });
        
        // Animate expansion
        expandedSearch.setScaleX(0.8);
        expandedSearch.setOpacity(0);
        
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), expandedSearch);
        scaleTransition.setFromX(0.8);
        scaleTransition.setToX(1.0);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), expandedSearch);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1.0);
        
        searchContainer.getChildren().clear();
        searchContainer.getChildren().add(expandedSearch);
        
        ParallelTransition transition = new ParallelTransition(scaleTransition, fadeTransition);
        transition.play();
    }
    
    private void collapseSearchPill() {
        if (!isSearchExpanded) return;
        isSearchExpanded = false;
        
        // Create collapsed search icon
        Text searchIcon = new Text("🔍");
        searchIcon.setFont(Font.font(16));
        searchIcon.setFill(Color.WHITE);
        
        HBox collapsedSearch = new HBox();
        collapsedSearch.setAlignment(Pos.CENTER);
        collapsedSearch.setPadding(new Insets(8, 10, 8, 10));
        collapsedSearch.getChildren().add(searchIcon);
        collapsedSearch.setCursor(javafx.scene.Cursor.HAND);
        
        // Animate collapse
        collapsedSearch.setScaleX(0.8);
        collapsedSearch.setOpacity(0);
        
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), collapsedSearch);
        scaleTransition.setFromX(0.8);
        scaleTransition.setToX(1.0);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), collapsedSearch);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1.0);
        
        searchContainer.getChildren().clear();
        searchContainer.getChildren().add(collapsedSearch);
        
        ParallelTransition transition = new ParallelTransition(scaleTransition, fadeTransition);
        transition.play();
    }
    
    private Button createNavPillTab(String tabName, boolean isActive) {
        Button tab = new Button(tabName);
        tab.setPadding(new Insets(10, 20, 10, 20));
        tab.setMinWidth(Region.USE_PREF_SIZE);
        tab.setPrefWidth(Region.USE_COMPUTED_SIZE);
        tab.setMaxWidth(Region.USE_PREF_SIZE);
        
        // Apply styling based on active state
        updateNavPillTabStyle(tab, tabName, isActive);
        
        // Hover effects
        tab.setOnMouseEntered(e -> {
            if (!tabName.equals(activeTab)) {
                // Use light font for hover on non-active tabs
                tab.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13));
                tab.setStyle(
                    "-fx-background-color: " + themeManager.getLiquidGlassHover() + ";" +
                    "-fx-background-radius: 18px;" +
                    "-fx-text-fill: rgba(255, 255, 255, 0.85);" +
                    "-fx-cursor: hand;"
                );
            }
        });
        
        tab.setOnMouseExited(e -> {
            updateNavPillTabStyle(tab, tabName, tabName.equals(activeTab));
        });
        
        // Click handler
        tab.setOnAction(e -> handleNavPillTabClick(tabName));
        
        return tab;
    }
    
    private void updateNavPillTabStyle(Button tab, String tabName, boolean isActive) {
        if (isActive || tabName.equals(activeTab)) {
            // Active tab: bold font, white text with glow effect
            tab.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 13));
            tab.setStyle(
                "-fx-background-color: " + themeManager.getLiquidGlassFocus() + ";" +
                "-fx-background-radius: 18px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, " + themeManager.getModernAccentColor() + ", 10, 0.4, 0, 0);"
            );
        } else {
            // Non-active tabs: light font, muted gray text, no glow
            tab.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13));
            tab.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 18px;" +
                "-fx-text-fill: rgba(255, 255, 255, 0.6);" +
                "-fx-cursor: hand;"
            );
        }
    }
    
    private void handleNavPillTabClick(String tabName) {
        // Update active tab state
        String previousTab = activeTab;
        activeTab = tabName;
        
        // Update previous and new tab styles
        if (mainTabButtons.containsKey(previousTab)) {
            updateNavPillTabStyle(mainTabButtons.get(previousTab), previousTab, false);
        }
        if (mainTabButtons.containsKey(activeTab)) {
            updateNavPillTabStyle(mainTabButtons.get(activeTab), activeTab, true);
        }
        
        // Check if tab has submenu, show dropdown
        if (tabName.equals("Services") || tabName.equals("About")) {
            showTabDropdown(mainTabButtons.get(tabName), tabName);
        } else {
            // Navigate directly for tabs without submenu
            switch (tabName) {
                case "Home":
                    NavigationManager.getInstance().navigateTo("home");
                    break;
                case "Dashboard":
                    NavigationManager.getInstance().navigateTo("dashboard");
                    break;
            }
        }
    }
    
    private void showTabDropdown(Button tabButton, String tabName) {
        // If already expanded with this tab, collapse it
        if (isTabDropdownOpen) {
            collapseNavigationIsland();
            return;
        }
        
        isTabDropdownOpen = true;
        
        // Create submenu container
        submenuContainer = new HBox();
        submenuContainer.setSpacing(8);
        submenuContainer.setAlignment(Pos.CENTER);
        
        // Add back arrow
        Button backBtn = new Button("←");
    backBtn.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 14));
        backBtn.setPadding(new Insets(10, 15, 10, 15));
        backBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 15px;" +
            "-fx-text-fill: rgba(255, 255, 255, 0.7);" +
            "-fx-cursor: hand;"
        );
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassHover() + ";" +
            "-fx-background-radius: 15px;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        ));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 15px;" +
            "-fx-text-fill: rgba(255, 255, 255, 0.7);" +
            "-fx-cursor: hand;"
        ));
        backBtn.setOnAction(e -> collapseNavigationIsland());
        
        submenuContainer.getChildren().add(backBtn);
        
        // Add submenu items based on tab
        if (tabName.equals("Services")) {
            Button webDevBtn = createSubmenuButton("Web Development");
            Button mobileAppBtn = createSubmenuButton("Mobile Apps");
            Button cloudBtn = createSubmenuButton("Cloud Solutions");
            submenuContainer.getChildren().addAll(webDevBtn, mobileAppBtn, cloudBtn);
        } else if (tabName.equals("About")) {
            Button companyBtn = createSubmenuButton("Company");
            Button teamBtn = createSubmenuButton("Team");
            Button contactBtn = createSubmenuButton("Contact");
            submenuContainer.getChildren().addAll(companyBtn, teamBtn, contactBtn);
        }
        
        // Animate expansion
        expandNavigationIsland();
    }
    
    private Button createSubmenuButton(String text) {
        Button btn = new Button(text);
    btn.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13));
        btn.setPadding(new Insets(10, 20, 10, 20));
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 15px;" +
            "-fx-text-fill: rgba(255, 255, 255, 0.7);" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassHover() + ";" +
            "-fx-background-radius: 15px;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 15px;" +
            "-fx-text-fill: rgba(255, 255, 255, 0.7);" +
            "-fx-cursor: hand;"
        ));
        
        btn.setOnAction(e -> {
            collapseNavigationIsland();
            NavigationManager.getInstance().navigateTo(activeTab.toLowerCase());
        });
        
        return btn;
    }
    
    private void expandNavigationIsland() {
        // Store original children
        java.util.List<javafx.scene.Node> originalChildren = new java.util.ArrayList<>(navPillIsland.getChildren());
        
        // Fade out main tabs
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), navPillIsland);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            // Replace with submenu
            navPillIsland.getChildren().clear();
            navPillIsland.getChildren().addAll(submenuContainer.getChildren());
            
            // Fade in submenu
            navPillIsland.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), navPillIsland);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    private void collapseNavigationIsland() {
        if (!isTabDropdownOpen) return;
        
        isTabDropdownOpen = false;
        
        // Fade out submenu
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), navPillIsland);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            // Restore main tabs with search pill
            navPillIsland.getChildren().clear();
            navPillIsland.getChildren().addAll(
                searchContainer,
                mainTabButtons.get("Home"),
                mainTabButtons.get("Services"),
                mainTabButtons.get("About"),
                mainTabButtons.get("Dashboard")
            );
            
            // Fade in main tabs
            navPillIsland.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), navPillIsland);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    private StackPane createIslandContainer() {
        // Create the main island container
        StackPane islandContainer = new StackPane();
        islandContainer.setPadding(new Insets(10, 20, 10, 20));
        
        // Apply enhanced island styling
        String islandStyle = "-fx-background-color: " + themeManager.getDynamicIslandBackground() + ";" +
            "-fx-background-radius: 35px;" +
            "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 35px;";
        islandContainer.setStyle(islandStyle);
        
        // TRON glow around island
        DropShadow islandShadow = new DropShadow();
        islandShadow.setBlurType(BlurType.GAUSSIAN);
        islandShadow.setColor(themeManager.getNeonGlowColor().deriveColor(0, 1, 1, 0.45));
        islandShadow.setRadius(24);
        islandShadow.setOffsetX(0);
        islandShadow.setOffsetY(6);
        islandContainer.setEffect(islandShadow);
        
        // Create the navigation section
        navSection = new HBox();
        navSection.setSpacing(10);
        navSection.setAlignment(Pos.CENTER);
        
        // Create main tabs container
        mainTabs = new HBox();
        mainTabs.setSpacing(10);
        mainTabs.setAlignment(Pos.CENTER);
        
        // Create and store main navigation tabs
        Button homeTab = createEnhancedNavTab("Home", true);
        Button servicesTab = createEnhancedNavTab("Services", false);
        Button aboutTab = createEnhancedNavTab("About", false);
        Button dashboardTab = createEnhancedNavTab("Dashboard", false);
        
        // Store tab references
        mainTabButtons.put("Home", homeTab);
        mainTabButtons.put("Services", servicesTab);
        mainTabButtons.put("About", aboutTab);
        mainTabButtons.put("Dashboard", dashboardTab);
        
        mainTabs.getChildren().addAll(homeTab, servicesTab, aboutTab, dashboardTab);
        
        // Create sub-menu container (initially hidden)
        subMenuContainer = new HBox();
        subMenuContainer.setSpacing(10);
        subMenuContainer.setAlignment(Pos.CENTER);
        subMenuContainer.setVisible(false);
        subMenuContainer.setOpacity(0.0);
        
        // Add both containers to the island
        islandContainer.getChildren().addAll(mainTabs, subMenuContainer);
        
        // Store original dimensions for animations
        islandContainer.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (!isExpanded && newBounds.getWidth() > 0) {
                originalNavSectionWidth = newBounds.getWidth();
                originalNavSectionHeight = newBounds.getHeight();
            }
        });
        
        return islandContainer;
    }
    
    private Button createEnhancedNavTab(String text, boolean isActive) {
        Button tab = new Button(text);
    tab.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 15));
        tab.setPadding(new Insets(10, 16, 10, 16));
        
        // Enhanced sizing with better proportions - fixed Dashboard width
        if (text.equals("Dashboard")) {
            tab.setMinWidth(110);
            tab.setMaxWidth(110);
            tab.setPrefWidth(110);
        } else {
            tab.setMinWidth(90);
            tab.setMaxWidth(90);
            tab.setPrefWidth(90);
        }
        
        // Apply enhanced styling based on active state
        updateTabStyle(tab, text, isActive);
        
        // Enhanced hover effects with smooth transitions - FIXED: Proper active state checking
        tab.setOnMouseEntered(e -> {
            // Always check current activeTab state, not the cached value
            if (!text.equals(activeTab)) {
                tab.setStyle(
                    "-fx-background-color: " + themeManager.getTabHoverColor() + ";" +
                    "-fx-background-radius: 15px;" +
                    "-fx-text-fill: white;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, " + themeManager.getModernAccentColor() + ", 12, 0.4, 0, 0);" +
                    "-fx-scale-x: 1.05;" +
                    "-fx-scale-y: 1.05;"
                );
            }
        });
        
        tab.setOnMouseExited(e -> {
            // Always check current activeTab state and apply correct styling
            if (!text.equals(activeTab)) {
                tab.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-background-radius: 15px;" +
                    "-fx-text-fill: white;" +
                    "-fx-cursor: hand;" +
                    "-fx-scale-x: 1.0;" +
                    "-fx-scale-y: 1.0;"
                );
            } else {
                // If this is the active tab, ensure it shows active styling
                tab.setStyle(
                    "-fx-background-color: " + themeManager.getActiveTabColor() + ";" +
                    "-fx-background-radius: 15px;" +
                    "-fx-text-fill: white;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, " + themeManager.getModernAccentColor() + ", 15, 0.45, 0, 0);" +
                    "-fx-scale-x: 1.0;" +
                    "-fx-scale-y: 1.0;"
                );
            }
        });
        
        // Enhanced click handlers with better state management
        tab.setOnAction(e -> {
            System.out.println("Tab clicked: " + text); // Debug output
            handleTabClick(text);
        });
        
        // Ensure button is properly enabled and clickable
        tab.setDisable(false);
        tab.setFocusTraversable(true);
        
        return tab;
    }
    
    private void updateTabStyle(Button tab, String text, boolean isActive) {
        if (isActive || text.equals(activeTab)) {
            tab.setStyle(
                "-fx-background-color: " + themeManager.getActiveTabColor() + ";" +
                "-fx-background-radius: 15px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, " + themeManager.getModernAccentColor() + ", 15, 0.45, 0, 0);" +
                "-fx-scale-x: 1.0;" +
                "-fx-scale-y: 1.0;"
            );
        } else {
            tab.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 15px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.0;" +
                "-fx-scale-y: 1.0;"
            );
        }
    }
    
    private void handleTabClick(String tabName) {
        // Update active tab state
        updateActiveTab(tabName);
        
        switch (tabName) {
            case "Home":
                navigateToHome();
                break;
            case "Services":
                toggleSubmenu("Services");
                break;
            case "About":
                toggleSubmenu("About");
                break;
            case "Dashboard":
                navigateToDashboard();
                break;
        }
    }
    
    private void updateActiveTab(String newActiveTab) {
        // Update previous active tab - ensure it's properly cleared
        if (mainTabButtons.containsKey(activeTab)) {
            Button previousTab = mainTabButtons.get(activeTab);
            updateTabStyle(previousTab, activeTab, false);
            // Force clear any hover effects that might be stuck
            previousTab.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 15px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.0;" +
                "-fx-scale-y: 1.0;"
            );
        }
        
        // Set new active tab
        activeTab = newActiveTab;
        
        // Update new active tab style
        if (mainTabButtons.containsKey(activeTab)) {
            updateTabStyle(mainTabButtons.get(activeTab), activeTab, true);
        }
    }
    
    private void refreshTabStates() {
        // Ensure all tabs are properly styled based on current active tab
        for (Map.Entry<String, Button> entry : mainTabButtons.entrySet()) {
            String tabName = entry.getKey();
            Button tab = entry.getValue();
            boolean isActive = tabName.equals(activeTab);
            
            // Force clear any stuck hover effects and apply correct styling
            if (isActive) {
                tab.setStyle(
                    "-fx-background-color: " + themeManager.getActiveTabColor() + ";" +
                    "-fx-background-radius: 15px;" +
                    "-fx-text-fill: white;" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(gaussian, " + themeManager.getModernAccentColor() + ", 15, 0.45, 0, 0);" +
                    "-fx-scale-x: 1.0;" +
                    "-fx-scale-y: 1.0;"
                );
            } else {
                tab.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-background-radius: 15px;" +
                    "-fx-text-fill: white;" +
                    "-fx-cursor: hand;" +
                    "-fx-scale-x: 1.0;" +
                    "-fx-scale-y: 1.0;"
                );
            }
        }
    }
    
    private HBox createProfileSection() {
        HBox container = new HBox();
        container.setSpacing(12);
        container.setAlignment(Pos.CENTER_RIGHT);
        
        // Connection status pill
        ConnectionStatusPill connectionPill = new ConnectionStatusPill();
        
        // Profile icon with dropdown
        StackPane profileIconContainer = createProfileIconWithDropdown();
        
        container.getChildren().addAll(connectionPill.getPillContainer(), profileIconContainer);
        return container;
    }

    private HBox createWindowControls() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setSpacing(6);

        javafx.scene.control.Button btnMin = flatWinButton("–");
        javafx.scene.control.Button btnMax = flatWinButton("□");
        javafx.scene.control.Button btnClose = flatWinButton("✕");

        // Add tooltips using setTooltip for buttons
        Tooltip minTooltip = new Tooltip("Minimize");
        minTooltip.setShowDelay(Duration.millis(200));
        btnMin.setTooltip(minTooltip);
        
        Tooltip maxTooltip = new Tooltip("Maximize");
        maxTooltip.setShowDelay(Duration.millis(200));
        btnMax.setTooltip(maxTooltip);
        
        Tooltip closeTooltip = new Tooltip("Close");
        closeTooltip.setShowDelay(Duration.millis(200));
        btnClose.setTooltip(closeTooltip);

        btnMin.setOnAction(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            stage.setIconified(true);
        });
        btnMax.setOnAction(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
            // Update tooltip text based on state
            maxTooltip.setText(stage.isMaximized() ? "Restore" : "Maximize");
        });
        btnClose.setOnAction(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            stage.close();
        });

        // Drag window via header
        final double[] dragOffset = new double[2];
        root.setOnMousePressed(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            dragOffset[0] = e.getScreenX() - stage.getX();
            dragOffset[1] = e.getScreenY() - stage.getY();
        });
        root.setOnMouseDragged(e -> {
            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            if (!stage.isMaximized()) {
                stage.setX(e.getScreenX() - dragOffset[0]);
                stage.setY(e.getScreenY() - dragOffset[1]);
            }
        });

        bar.getChildren().addAll(btnMin, btnMax, btnClose);
        return bar;
    }

    private javafx.scene.control.Button flatWinButton(String text) {
        javafx.scene.control.Button b = new javafx.scene.control.Button(text);
        b.setMinSize(28, 24);
        b.setPrefSize(28, 24);
        b.setStyle(
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-background-radius: 8px;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;"
        );
        b.setOnMouseEntered(e -> b.setStyle(
            "-fx-background-color: rgba(255,255,255,0.18);" +
            "-fx-background-radius: 8px;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;"
        ));
        b.setOnMouseExited(e -> b.setStyle(
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-background-radius: 8px;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;"
        ));
        return b;
    }
    
    private StackPane createThemeToggle() {
        StackPane toggleContainer = new StackPane();
        
        // Toggle track (background)
        javafx.scene.shape.Rectangle track = new javafx.scene.shape.Rectangle(65, 35);
        track.setArcWidth(35);
        track.setArcHeight(35);
        track.setFill(Color.web(themeManager.getToggleTrackBackground()));
        track.setStroke(Color.web(themeManager.getToggleTrackBorder()));
        track.setStrokeWidth(1);
        
        // Toggle thumb container
        StackPane thumbContainer = new StackPane();
        thumbContainer.setTranslateX(themeManager.isDarkMode() ? -16 : 16);
        
        // Moon icon (dark mode)
        Text moonIcon = new Text("☾");
        moonIcon.setFont(Font.font(16));
        moonIcon.setFill(Color.WHITE);
        moonIcon.setOpacity(themeManager.isDarkMode() ? 1.0 : 0.0);
        
        // Sun icon (light mode)
        Text sunIcon = new Text("☀");
        sunIcon.setFont(Font.font(16));
        sunIcon.setFill(Color.web("#f59e0b"));
        sunIcon.setOpacity(themeManager.isDarkMode() ? 0.0 : 1.0);
        
        // Add shadow to thumb container
        DropShadow thumbShadow = new DropShadow();
        thumbShadow.setBlurType(BlurType.GAUSSIAN);
        thumbShadow.setColor(Color.color(0, 0, 0, 0.3));
        thumbShadow.setRadius(6);
        thumbShadow.setOffsetX(0);
        thumbShadow.setOffsetY(3);
        thumbContainer.setEffect(thumbShadow);
        
        thumbContainer.getChildren().addAll(moonIcon, sunIcon);
        toggleContainer.getChildren().addAll(track, thumbContainer);
        
        // Add click handler
        toggleContainer.setOnMouseClicked(e -> {
            toggleTheme(thumbContainer, sunIcon, moonIcon, track);
        });
        
        // Add hover effect
        track.setOnMouseEntered(e -> {
            track.setFill(Color.web(themeManager.getToggleTrackHover()));
        });
        track.setOnMouseExited(e -> {
            track.setFill(Color.web(themeManager.getToggleTrackBackground()));
        });
        
        return toggleContainer;
    }
    
    private StackPane createLogoutButton() {
        StackPane buttonContainer = new StackPane();
        
        // Button background
        javafx.scene.shape.Rectangle buttonBg = new javafx.scene.shape.Rectangle(65, 35);
        buttonBg.setArcWidth(35);
        buttonBg.setArcHeight(35);
        buttonBg.setFill(Color.web(themeManager.getLiquidGlassBackground()));
        buttonBg.setStroke(Color.web(themeManager.getLiquidGlassBorder()));
        buttonBg.setStrokeWidth(1);
        
        // Door icon
        Text doorIcon = new Text("🚪");
        doorIcon.setFont(Font.font(16));
        doorIcon.setFill(Color.WHITE);
        
        buttonContainer.getChildren().addAll(buttonBg, doorIcon);
        
        // Add shadow
        DropShadow buttonShadow = new DropShadow();
        buttonShadow.setBlurType(BlurType.GAUSSIAN);
        buttonShadow.setColor(Color.color(0, 0, 0, 0.3));
        buttonShadow.setRadius(6);
        buttonShadow.setOffsetX(0);
        buttonShadow.setOffsetY(3);
        buttonContainer.setEffect(buttonShadow);
        
        // Add hover effects
        buttonContainer.setOnMouseEntered(e -> {
            buttonBg.setFill(Color.web(themeManager.getLiquidGlassHover()));
            buttonContainer.setScaleX(1.05);
            buttonContainer.setScaleY(1.05);
        });
        
        buttonContainer.setOnMouseExited(e -> {
            buttonBg.setFill(Color.web(themeManager.getLiquidGlassBackground()));
            buttonContainer.setScaleX(1.0);
            buttonContainer.setScaleY(1.0);
        });
        
        // Add click handler
        buttonContainer.setOnMouseClicked(e -> handleLogout());
        
        return buttonContainer;
    }
    
    private StackPane createProfileIconWithDropdown() {
        profileContainer = new StackPane();
        profileContainer.setAlignment(Pos.CENTER);
        
        // Profile icon
        profileIconRef = new Circle(20);
        profileIconRef.setFill(Color.web(themeManager.getModernAccentColor()));
        profileIconRef.setStroke(Color.WHITE);
        profileIconRef.setStrokeWidth(2);
        
        // Add shadow
        DropShadow profileShadow = new DropShadow();
        profileShadow.setBlurType(BlurType.GAUSSIAN);
        profileShadow.setColor(themeManager.getNeonGlowColor().deriveColor(0, 1, 1, 0.5));
        profileShadow.setRadius(12);
        profileShadow.setOffsetX(0);
        profileShadow.setOffsetY(4);
        profileIconRef.setEffect(profileShadow);
        
        // Add hover effects - expand on hover
        profileIconRef.setOnMouseEntered(e -> {
            if (!isProfileDropdownOpen) {
                profileIconRef.setScaleX(1.1);
                profileIconRef.setScaleY(1.1);
                expandProfilePill();
            }
        });
        
        profileIconRef.setOnMouseExited(e -> {
            if (!isProfileDropdownOpen) {
                profileIconRef.setScaleX(1.0);
                profileIconRef.setScaleY(1.0);
            }
        });
        
        profileContainer.getChildren().add(profileIconRef);
        return profileContainer;
    }
    
    private void toggleProfilePill() {
        if (isProfileDropdownOpen) {
            collapseProfilePill();
        } else {
            expandProfilePill();
        }
    }
    
    private void expandProfilePill() {
        isProfileDropdownOpen = true;
        
        // Create expanded pill container
        HBox expandedPill = new HBox();
        expandedPill.setSpacing(12);
        expandedPill.setAlignment(Pos.CENTER);
        expandedPill.setPadding(new Insets(8, 16, 8, 16));
        expandedPill.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 25px;" +
            "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 25px;"
        );
        
        // Add shadow
        DropShadow pillShadow = new DropShadow();
        pillShadow.setBlurType(BlurType.GAUSSIAN);
        pillShadow.setColor(Color.color(0, 0, 0, 0.3));
        pillShadow.setRadius(12);
        pillShadow.setOffsetX(0);
        pillShadow.setOffsetY(4);
        expandedPill.setEffect(pillShadow);
        
        // Collapse when mouse exits the expanded pill
        expandedPill.setOnMouseExited(e -> {
            collapseProfilePill();
        });
        
        // Profile icon button (navigates to profile)
        Text profileIcon = new Text("👤");
        profileIcon.setFont(Font.font(18));
        profileIcon.setFill(Color.WHITE);
        profileIcon.setCursor(javafx.scene.Cursor.HAND);
        Tooltip profileTooltip = new Tooltip("Profile");
        profileTooltip.setShowDelay(Duration.millis(200));
        Tooltip.install(profileIcon, profileTooltip);
        profileIcon.setOnMouseClicked(e -> {
            collapseProfilePill();
            NavigationManager.getInstance().navigateTo("profile");
        });
        
        // Settings icon button
        Text settingsIcon = new Text("⚙");
        settingsIcon.setFont(Font.font(18));
        settingsIcon.setFill(Color.WHITE);
        settingsIcon.setCursor(javafx.scene.Cursor.HAND);
        Tooltip settingsTooltip = new Tooltip("Settings");
        settingsTooltip.setShowDelay(Duration.millis(200));
        Tooltip.install(settingsIcon, settingsTooltip);
        settingsIcon.setOnMouseClicked(e -> {
            collapseProfilePill();
            NavigationManager.getInstance().navigateTo("settings");
        });
        
        // Theme toggle icon button
        Text themeIcon = new Text(themeManager.isDarkMode() ? "☀" : "☾");
        themeIcon.setFont(Font.font(18));
        themeIcon.setFill(Color.WHITE);
        themeIcon.setCursor(javafx.scene.Cursor.HAND);
        Tooltip themeTooltip = new Tooltip(themeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        themeTooltip.setShowDelay(Duration.millis(200));
        Tooltip.install(themeIcon, themeTooltip);
        themeIcon.setOnMouseClicked(e -> {
            themeManager.toggleTheme();
            applyThemeStyling();
            if (backgroundUpdateCallback != null) {
                backgroundUpdateCallback.run();
            }
            themeIcon.setText(themeManager.isDarkMode() ? "☀" : "☾");
            themeTooltip.setText(themeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        });
        
        // Logout icon button
        Text logoutIcon = new Text("🚪");
        logoutIcon.setFont(Font.font(18));
        logoutIcon.setFill(Color.WHITE);
        logoutIcon.setCursor(javafx.scene.Cursor.HAND);
        Tooltip logoutTooltip = new Tooltip("Logout");
        logoutTooltip.setShowDelay(Duration.millis(200));
        Tooltip.install(logoutIcon, logoutTooltip);
        logoutIcon.setOnMouseClicked(e -> {
            collapseProfilePill();
            handleLogout();
        });
        
        expandedPill.getChildren().addAll(profileIcon, settingsIcon, themeIcon, logoutIcon);
        
        // Fade out icon
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), profileIconRef);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            profileContainer.getChildren().clear();
            profileContainer.getChildren().add(expandedPill);
            
            // Fade in expanded pill
            expandedPill.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), expandedPill);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    private void collapseProfilePill() {
        isProfileDropdownOpen = false;
        
        javafx.scene.Node currentContent = profileContainer.getChildren().get(0);
        
        // Fade out expanded pill
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentContent);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        
        fadeOut.setOnFinished(e -> {
            profileContainer.getChildren().clear();
            profileContainer.getChildren().add(profileIconRef);
            
            // Fade in icon
            profileIconRef.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), profileIconRef);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        
        fadeOut.play();
    }
    
    private void showProfileDropdown(Circle profileIcon) {
        // Prevent multiple dropdowns from opening
        if (isProfileDropdownOpen) {
            return;
        }
        
        isProfileDropdownOpen = true;
        
        // Get the main container (root of LandingPageView)
        Pane mainContainer = (Pane) root.getScene().getRoot();
        
        // Create dropdown if it doesn't exist or recreate it
        VBox dropdown = createProfileDropdown();
        dropdown.setVisible(false);
        dropdown.setMouseTransparent(true);
        
        // Add to main container as overlay
        mainContainer.getChildren().add(dropdown);
        
        // Position dropdown below the profile icon
        positionDropdown(dropdown, profileIcon);
        
        // Open dropdown
        openDropdown(dropdown);
        
        // Store dropdown reference to allow closing from outside clicks
        dropdown.setOnMouseClicked(e -> e.consume()); // Prevent clicks inside dropdown from closing it
        
        // Close dropdown when clicking outside
        mainContainer.setOnMouseClicked(event -> {
            // Check if click is inside dropdown bounds
            javafx.geometry.Bounds dropdownBounds = dropdown.getBoundsInParent();
            boolean clickedInDropdown = dropdownBounds.contains(event.getX(), event.getY());
            
            if (!clickedInDropdown && dropdown.isVisible()) {
                closeDropdown(dropdown);
                // Remove from scene after closing
                Timeline removeTimeline = new Timeline(new KeyFrame(Duration.millis(250), e -> {
                    mainContainer.getChildren().remove(dropdown);
                    mainContainer.setOnMouseClicked(null); // Clean up handler
                    isProfileDropdownOpen = false; // Reset flag
                }));
                removeTimeline.play();
            }
        });
    }
    
    private void positionDropdown(VBox dropdown, Circle profileIcon) {
        javafx.geometry.Bounds iconBounds = profileIcon.localToScene(profileIcon.getBoundsInLocal());
        
        // Position dropdown: align right edge with icon, directly below it
        dropdown.setLayoutX(iconBounds.getMaxX() - 180); // Right align with icon (180px dropdown width)
        dropdown.setLayoutY(iconBounds.getMaxY() + 10); // 10px below the icon
    }
    
    private VBox createProfileDropdown() {
        VBox dropdown = new VBox();
        dropdown.setSpacing(8);
        dropdown.setPadding(new Insets(12));
        dropdown.setAlignment(Pos.TOP_CENTER);
        dropdown.setMaxWidth(180);
        dropdown.setMinWidth(180);
        dropdown.setPrefWidth(180);
        dropdown.setMaxHeight(Region.USE_PREF_SIZE);
        dropdown.setPrefHeight(Region.USE_COMPUTED_SIZE);
        
        // Liquid glass styling
        dropdown.setStyle(
            "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;"
        );
        
        // Add shadow
        DropShadow dropdownShadow = new DropShadow();
        dropdownShadow.setBlurType(BlurType.GAUSSIAN);
        dropdownShadow.setColor(Color.color(0, 0, 0, 0.4));
        dropdownShadow.setRadius(20);
        dropdownShadow.setOffsetX(0);
        dropdownShadow.setOffsetY(8);
        dropdown.setEffect(dropdownShadow);
        
        // Profile button
        Button profileBtn = createDropdownButton("👤 Profile", () -> {
            NavigationManager.getInstance().navigateTo("profile");
            dropdown.setVisible(false);
            isProfileDropdownOpen = false;
        });
        
        // Theme toggle button
        Button themeBtn = new Button(themeManager.isDarkMode() ? "☀ Light Mode" : "☾ Dark Mode");
    themeBtn.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13));
        themeBtn.setPadding(new Insets(10, 15, 10, 15));
        themeBtn.setMaxWidth(Double.MAX_VALUE);
        themeBtn.setAlignment(Pos.CENTER_LEFT);
        
        themeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 12px;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
        
        themeBtn.setOnMouseEntered(e -> {
            themeBtn.setStyle(
                "-fx-background-color: " + themeManager.getLiquidGlassHover() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
        });
        
        themeBtn.setOnMouseExited(e -> {
            themeBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
        });
        
        themeBtn.setOnAction(e -> {
            themeManager.toggleTheme();
            applyThemeStyling();
            if (backgroundUpdateCallback != null) {
                backgroundUpdateCallback.run();
            }
            // Update button text
            themeBtn.setText(themeManager.isDarkMode() ? "☀ Light Mode" : "☾ Dark Mode");
        });
        
        // Logout button
        Button logoutBtn = createDropdownButton("🚪 Logout", () -> {
            dropdown.setVisible(false);
            isProfileDropdownOpen = false;
            handleLogout();
        });
        
        dropdown.getChildren().addAll(profileBtn, themeBtn, logoutBtn);
        
        return dropdown;
    }
    
    private Button createDropdownButton(String text, Runnable action) {
        Button btn = new Button(text);
    btn.setFont(Font.font(com.pidev.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13));
        btn.setPadding(new Insets(10, 15, 10, 15));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 12px;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: " + themeManager.getLiquidGlassHover() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;"
            );
        });
        
        btn.setOnAction(e -> action.run());
        
        return btn;
    }
    
    private void toggleDropdown(VBox dropdown) {
        if (dropdown.isVisible()) {
            closeDropdown(dropdown);
        } else {
            openDropdown(dropdown);
        }
    }
    
    private void openDropdown(VBox dropdown) {
        dropdown.setVisible(true);
        dropdown.setMouseTransparent(false);
        dropdown.setScaleY(0);
        dropdown.setOpacity(0);
        
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(250), dropdown);
        scaleTransition.setFromY(0);
        scaleTransition.setToY(1);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(250), dropdown);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        
        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();
    }
    
    private void closeDropdown(VBox dropdown) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), dropdown);
        scaleTransition.setFromY(1);
        scaleTransition.setToY(0);
        scaleTransition.setInterpolator(Interpolator.EASE_IN);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), dropdown);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);
        
        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.setOnFinished(e -> {
            dropdown.setVisible(false);
            dropdown.setMouseTransparent(true);
        });
        parallelTransition.play();
    }
    
    private void toggleTheme(StackPane thumbContainer, Text sunIcon, Text moonIcon, javafx.scene.shape.Rectangle track) {
        themeManager.toggleTheme();
        
        // Re-apply header styles immediately
        applyThemeStyling();
        
        // Notify background update callback (LandingPageView will refresh background/footer)
        if (backgroundUpdateCallback != null) {
            backgroundUpdateCallback.run();
        }
        
        // Animate thumb movement
        TranslateTransition thumbAnimation = new TranslateTransition(Duration.millis(300), thumbContainer);
        FadeTransition sunFade = new FadeTransition(Duration.millis(300), sunIcon);
        FadeTransition moonFade = new FadeTransition(Duration.millis(300), moonIcon);
        
        if (themeManager.isDarkMode()) {
            thumbAnimation.setToX(-16);
            sunFade.setToValue(0.0);
            moonFade.setToValue(1.0);
        } else {
            thumbAnimation.setToX(16);
            sunFade.setToValue(1.0);
            moonFade.setToValue(0.0);
        }
        
        thumbAnimation.setInterpolator(Interpolator.EASE_BOTH);
        sunFade.setInterpolator(Interpolator.EASE_BOTH);
        moonFade.setInterpolator(Interpolator.EASE_BOTH);
        
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(thumbAnimation, sunFade, moonFade);
        parallelTransition.play();
        
        parallelTransition.setOnFinished(e -> {
            track.setFill(Color.web(themeManager.getToggleTrackBackground()));
            track.setStroke(Color.web(themeManager.getToggleTrackBorder()));
        });
    }
    
    private void handleLogout() {
        com.pidev.MainApplication.getInstance().logout();
    }
    
    private void navigateToHome() {
        NavigationManager.getInstance().navigateTo("home");
    }
    
    private void navigateToDashboard() {
        NavigationManager.getInstance().navigateTo("dashboard");
    }
    
    public void setBackgroundUpdateCallback(Runnable callback) {
        this.backgroundUpdateCallback = callback;
    }
    
    public void setMainContainer(Pane mainContainer) {
        // This method is kept for compatibility but not used in the new structure
    }
    
    private void toggleSubmenu(String tabName) {
        if (islandContainer == null) return;
        
        if (isExpanded && currentExpandedTab != null && currentExpandedTab.equals(tabName)) {
            collapseIsland();
        } else {
            expandIsland(tabName);
        }
    }
    
    private void expandIsland(String tabName) {
        if (islandContainer == null || mainTabs == null || subMenuContainer == null) return;
        
        isExpanded = true;
        currentExpandedTab = tabName;
        
        // CRITICAL FIX: Set the expanded tab as active when opening submenu
        updateActiveTab(tabName);
        
        // Clear previous submenu content
        subMenuContainer.getChildren().clear();
        submenuButtons.clear();
        
        // Create enhanced back arrow
        Button backArrow = createEnhancedBackArrow();
        subMenuContainer.getChildren().add(backArrow);
        
        // Add sub-menu items based on tab with enhanced styling - DYNAMIC TEXT LENGTHS
        if ("Services".equals(tabName)) {
            Button service1 = createEnhancedSubmenuButton("Web Development", "services/web");
            Button service2 = createEnhancedSubmenuButton("Mobile Applications", "services/mobile");
            Button service3 = createEnhancedSubmenuButton("Technical Consulting", "services/consulting");
            subMenuContainer.getChildren().addAll(service1, service2, service3);
        } else if ("About".equals(tabName)) {
            Button about1 = createEnhancedSubmenuButton("Our Company Story", "about/story");
            Button about2 = createEnhancedSubmenuButton("Meet Our Team", "about/team");
            Button about3 = createEnhancedSubmenuButton("Contact Information", "about/contact");
            subMenuContainer.getChildren().addAll(about1, about2, about3);
        }
        
        // Enhanced animation sequence
        animateToSubmenu();
    }
    
    private void collapseIsland() {
        if (islandContainer == null || mainTabs == null || subMenuContainer == null) return;
        
        isExpanded = false;
        currentExpandedTab = null;
        
        // Enhanced animation sequence
        animateToMainTabs();
    }
    
    private void animateToSubmenu() {
        // Fade out main tabs
        FadeTransition fadeOutMain = new FadeTransition(Duration.millis(200), mainTabs);
        fadeOutMain.setFromValue(1.0);
        fadeOutMain.setToValue(0.0);
        
        // Scale and fade in submenu
        FadeTransition fadeInSubmenu = new FadeTransition(Duration.millis(300), subMenuContainer);
        fadeInSubmenu.setFromValue(0.0);
        fadeInSubmenu.setToValue(1.0);
        
        ScaleTransition scaleSubmenu = new ScaleTransition(Duration.millis(300), subMenuContainer);
        scaleSubmenu.setFromX(0.8);
        scaleSubmenu.setToX(1.0);
        scaleSubmenu.setFromY(0.8);
        scaleSubmenu.setToY(1.0);
        
        // Execute animation sequence
        fadeOutMain.setOnFinished(e -> {
            mainTabs.setVisible(false);
            subMenuContainer.setVisible(true);
            
            ParallelTransition showSubmenu = new ParallelTransition();
            showSubmenu.getChildren().addAll(fadeInSubmenu, scaleSubmenu);
            showSubmenu.play();
        });
        
        fadeOutMain.play();
    }
    
    private void animateToMainTabs() {
        // Fade out submenu
        FadeTransition fadeOutSubmenu = new FadeTransition(Duration.millis(200), subMenuContainer);
        fadeOutSubmenu.setFromValue(1.0);
        fadeOutSubmenu.setToValue(0.0);
        
        // Scale and fade in main tabs
        FadeTransition fadeInMain = new FadeTransition(Duration.millis(300), mainTabs);
        fadeInMain.setFromValue(0.0);
        fadeInMain.setToValue(1.0);
        
        ScaleTransition scaleMain = new ScaleTransition(Duration.millis(300), mainTabs);
        scaleMain.setFromX(0.8);
        scaleMain.setToX(1.0);
        scaleMain.setFromY(0.8);
        scaleMain.setToY(1.0);
        
        // Execute animation sequence
        fadeOutSubmenu.setOnFinished(e -> {
            subMenuContainer.setVisible(false);
            mainTabs.setVisible(true);
            
            // CRITICAL FIX: Ensure only the correct tab shows as selected when returning from submenu
            refreshTabStates();
            
            ParallelTransition showMain = new ParallelTransition();
            showMain.getChildren().addAll(fadeInMain, scaleMain);
            showMain.play();
        });
        
        fadeOutSubmenu.play();
    }
    
    private Button createEnhancedSubmenuButton(String text, String route) {
        Button button = new Button(text);
    button.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 13));
        button.setPadding(new Insets(8, 14, 8, 14)); // Restored original padding
        
        // CONSERVATIVE dynamic submenu button sizing - prevents text cutoff without breaking island
        int calculatedWidth = Math.max(120, text.length() * 6 + 24); // Very conservative calculation
        int maxWidth = Math.min(calculatedWidth, 160); // Lower cap to preserve island design
        
        button.setMinWidth(maxWidth);
        button.setMaxWidth(maxWidth);
        button.setPrefWidth(maxWidth);
        
        // Apply enhanced submenu styling
        button.setStyle(
            "-fx-background-color: " + themeManager.getTabHoverColor() + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;" +
            "-fx-scale-x: 1.0;" +
            "-fx-scale-y: 1.0;"
        );
        
        // Enhanced hover effects with smooth scaling
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getActiveTabColor() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.05;" +
                "-fx-scale-y: 1.05;"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: " + themeManager.getTabHoverColor() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.0;" +
                "-fx-scale-y: 1.0;"
            );
        });
        
        // Enhanced click handler with navigation
        button.setOnAction(e -> {
            System.out.println("Submenu clicked: " + text + " -> " + route);
            NavigationManager.getInstance().navigateTo(route);
            collapseIsland();
        });
        
        // Store button reference
        submenuButtons.put(text, button);
        
        return button;
    }
    
    private Button createEnhancedBackArrow() {
        Button backButton = new Button("←");
    backButton.setFont(Font.font(com.pidev.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 18));
        backButton.setPadding(new Insets(8, 12, 8, 12));
        
        // Enhanced back arrow sizing
        backButton.setMinWidth(45);
        backButton.setMaxWidth(45);
        backButton.setPrefWidth(45);
        
        // Apply enhanced back arrow styling
        backButton.setStyle(
            "-fx-background-color: " + themeManager.getActiveTabColor() + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-text-fill: white;" +
            "-fx-cursor: hand;" +
            "-fx-scale-x: 1.0;" +
            "-fx-scale-y: 1.0;"
        );
        
        // Enhanced hover effects
        backButton.setOnMouseEntered(e -> {
            backButton.setStyle(
                "-fx-background-color: " + themeManager.getTabHoverColor() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.1;" +
                "-fx-scale-y: 1.1;"
            );
        });
        
        backButton.setOnMouseExited(e -> {
            backButton.setStyle(
                "-fx-background-color: " + themeManager.getActiveTabColor() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-text-fill: white;" +
                "-fx-cursor: hand;" +
                "-fx-scale-x: 1.0;" +
                "-fx-scale-y: 1.0;"
            );
        });
        
        // Enhanced click handler with smooth animation
        backButton.setOnAction(e -> {
            collapseIsland();
        });
        
        return backButton;
    }
    
    private void applyThemeStyling() {
        // Keep root transparent - no big island visible
        root.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 0;" +
            "-fx-border-color: transparent;" +
            "-fx-border-width: 0;"
        );
        root.setEffect(null);

        // Update navigation pill island styling
        if (navPillIsland != null) {
            navPillIsland.setStyle(
                "-fx-background-color: " + themeManager.getLiquidGlassBackground() + ";" +
                "-fx-background-radius: 25px;" +
                "-fx-border-color: " + themeManager.getLiquidGlassBorder() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 25px;"
            );
            
            // Update pill shadow
            DropShadow pillShadow = new DropShadow();
            pillShadow.setBlurType(BlurType.GAUSSIAN);
            pillShadow.setColor(Color.color(0, 0, 0, 0.3));
            pillShadow.setRadius(12);
            pillShadow.setOffsetX(0);
            pillShadow.setOffsetY(3);
            navPillIsland.setEffect(pillShadow);
        }

        if (islandContainer != null) {
            String islandStyle = "-fx-background-color: " + themeManager.getDynamicIslandBackground() + ";" +
                "-fx-background-radius: 35px;" +
                "-fx-border-color: " + themeManager.getDynamicIslandBorder() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 35px;";
            islandContainer.setStyle(islandStyle);

            // Update glow
            DropShadow islandShadow = new DropShadow();
            islandShadow.setBlurType(BlurType.GAUSSIAN);
            islandShadow.setColor(themeManager.getNeonGlowColor().deriveColor(0, 1, 1, 0.45));
            islandShadow.setRadius(24);
            islandShadow.setOffsetX(0);
            islandShadow.setOffsetY(6);
            islandContainer.setEffect(islandShadow);
        }

        // Refresh tab styles to pick up new colors
        refreshTabStates();
    }
    
    public Pane getRoot() {
        return root;
    }
    
    public void cleanup() {
        // Cleanup resources if needed
    }
}
