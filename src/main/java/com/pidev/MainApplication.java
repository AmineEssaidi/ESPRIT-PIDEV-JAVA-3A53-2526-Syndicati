package com.pidev;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.text.Font;
import com.pidev.views.home.LandingPageView;
import com.pidev.views.login.LoginView;
import com.pidev.utils.theme.ThemeManager;
import com.pidev.utils.navigation.NavigationManager;

/**
 * Main JavaFX Application - Dynamic Island Design Landing Page
 */
public class MainApplication extends Application {
    
    private static MainApplication instance;
    private Stage primaryStage;
    private LandingPageView landingPageView;
    private LoginView loginView;
    private boolean isLoggedIn = false;
    private javafx.animation.Timeline loginChecker; // Keep reference to stop it later
    private String boldFontFamily = "Clash Grotesk"; // default name in case load resolves differently
    private String lightFontFamily = "Clash Grotesk"; // default name in case load resolves differently
    
    @Override
    public void start(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;
        
        // Load custom fonts once (will be cached by JavaFX)
        loadCustomFonts();
        
        // Create the login view first
        loginView = new LoginView();
        
        // Set up the scene with login view - dynamic sizing with min constraints
        Scene scene = new Scene(loginView.getRoot(), 1200, 800);
        // Apply global font family to entire scene (use Light as default body font)
        if (scene.getRoot() != null) {
            scene.getRoot().setStyle("-fx-font-family: '" + lightFontFamily + "';");
        }
        
        // Set up theme manager
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.setScene(scene);
        
        // Start database connection monitoring
        com.pidev.utils.database.ConnectionManager connectionManager = com.pidev.utils.database.ConnectionManager.getInstance();
        connectionManager.startMonitoring();
        
        // Add JVM shutdown hook as backup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🔄 JVM Shutdown - Stopping database monitoring...");
            connectionManager.shutdown();
        }));
        
        // Configure the stage - transparent for rounded corners and shadow
        primaryStage.setTitle("Dynamic Island App - Login");
        scene.setFill(Color.TRANSPARENT);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        
        // Set minimum window size
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Show the stage first (needed for width/height to be set)
        primaryStage.show();
        
        // Center the window on screen after showing
        centerStageOnScreen(primaryStage);
        
        // Add corner resize functionality
        addResizeHandlers(primaryStage, scene);
        
        // Force rounded corners by applying shape to scene root after showing
        applyRoundedShape(scene);
        
        // Add shutdown hook to properly close database monitoring
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("🔄 Shutting down application...");
            connectionManager.shutdown();
            System.out.println("✅ Database monitoring stopped");
        });
        
        System.out.println("✅ Dynamic Island App started!");
        System.out.println("🔐 Login page loaded - use admin/admin to login");
        
        // Set up login monitoring
        setupLoginMonitoring();
    }
    
    private void setupLoginMonitoring() {
        // Stop any existing login checker first
        if (loginChecker != null) {
            loginChecker.stop();
        }
        
        // Create a timeline to check for successful login
        loginChecker = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), e -> {
                if (loginView != null && loginView.isLoginSuccessful()) {
                    navigateToLandingPage();
                }
            })
        );
        loginChecker.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        loginChecker.play();
    }
    
    private void navigateToLandingPage() {
        if (isLoggedIn) return; // Prevent multiple navigations
        
        isLoggedIn = true;
        System.out.println("🔓 Login successful! Navigating to landing page...");
        
        // Store current window size and position before switching
        double currentWidth = primaryStage.getWidth();
        double currentHeight = primaryStage.getHeight();
        double currentX = primaryStage.getX();
        double currentY = primaryStage.getY();
        boolean wasMaximized = primaryStage.isMaximized();
        
        // STOP the login monitoring animation FIRST
        if (loginChecker != null) {
            loginChecker.stop();
            loginChecker = null;
        }
        
        // Clean up login view FIRST to remove all backgrounds
        loginView.cleanup();
        loginView = null; // Clear reference to ensure garbage collection
        
        // Create the landing page view
        landingPageView = new LandingPageView();
        
        // Set up navigation manager
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setViews(landingPageView);
        
        // Create completely new scene - don't set initial size, let it adapt to stage
        Scene scene = new Scene(landingPageView.getRoot());
        scene.setFill(Color.TRANSPARENT); // Make scene transparent to show rounded corners
        scene.getStylesheets().clear(); // Clear any inherited styles
        // Apply global font family to entire scene (use Light as default body font)
        if (scene.getRoot() != null) {
            scene.getRoot().setStyle("-fx-font-family: '" + lightFontFamily + "';");
        }
        
        // Set minimum window size
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        ThemeManager.getInstance().setScene(scene);
        
        // Set scene first, THEN restore size
        primaryStage.setScene(scene);
        
        // Restore window size and position before showing
        if (wasMaximized) {
            primaryStage.setMaximized(true);
        } else {
            primaryStage.setWidth(currentWidth);
            primaryStage.setHeight(currentHeight);
            primaryStage.setX(currentX);
            primaryStage.setY(currentY);
        }
        
        primaryStage.setTitle("Dynamic Island App - Dashboard");
        
        // Add corner resize functionality for landing page
        addResizeHandlers(primaryStage, scene);
        
        // Apply rounded shape to new scene
        applyRoundedShape(scene);
        
        // Force refresh
        primaryStage.show();
        
        // Force multiple layout passes to ensure content is properly sized
        javafx.application.Platform.runLater(() -> {
            landingPageView.getRoot().layout();
            primaryStage.sizeToScene();
            javafx.application.Platform.runLater(() -> {
                landingPageView.getRoot().layout();
                primaryStage.sizeToScene();
            });
        });
        
        System.out.println("✅ Successfully navigated to landing page!");
    }

    private void loadCustomFonts() {
        try {
            // Load ClashGrotesk-Bold for titles
            java.io.InputStream boldStream = MainApplication.class.getResourceAsStream("/ClashGrotesk-Bold.otf");
            if (boldStream != null) {
                Font boldFont = Font.loadFont(boldStream, 14);
                if (boldFont != null) {
                    boldFontFamily = boldFont.getFamily();
                    System.out.println("📦 Loaded bold font: " + boldFont.getName() + " (family: " + boldFontFamily + ")");
                } else {
                    System.out.println("⚠️ Failed to load ClashGrotesk-Bold.otf font – using default family name.");
                }
                boldStream.close();
            } else {
                System.out.println("⚠️ ClashGrotesk-Bold.otf not found on classpath.");
            }
            
            // Load ClashGrotesk-Light for body text
            java.io.InputStream lightStream = MainApplication.class.getResourceAsStream("/ClashGrotesk-Light.otf");
            if (lightStream != null) {
                Font lightFont = Font.loadFont(lightStream, 14);
                if (lightFont != null) {
                    lightFontFamily = lightFont.getFamily();
                    System.out.println("📦 Loaded light font: " + lightFont.getName() + " (family: " + lightFontFamily + ")");
                } else {
                    System.out.println("⚠️ Failed to load ClashGrotesk-Light.otf font – using default family name.");
                }
                lightStream.close();
            } else {
                System.out.println("⚠️ ClashGrotesk-Light.otf not found on classpath.");
            }
        } catch (Exception ex) {
            System.out.println("⚠️ Error loading custom fonts: " + ex.getMessage());
        }
    }
    
    public String getBoldFontFamily() {
        return boldFontFamily;
    }
    
    public String getLightFontFamily() {
        return lightFontFamily;
    }
    
    public void logout() {
        System.out.println("🔓 Logging out - returning to login page...");
        
        // Store current window size and position before switching
        double currentWidth = primaryStage.getWidth();
        double currentHeight = primaryStage.getHeight();
        double currentX = primaryStage.getX();
        double currentY = primaryStage.getY();
        boolean wasMaximized = primaryStage.isMaximized();
        
        // Reset login state
        isLoggedIn = false;
        
        // Create new login view
        loginView = new LoginView();
        
        // Update the scene with transparent fill - don't set initial size
        Scene scene = new Scene(loginView.getRoot());
        scene.setFill(Color.TRANSPARENT); // Keep transparency for login view too
        scene.getStylesheets().clear(); // Avoid inherited styles that could reintroduce backgrounds
        
        // Set minimum window size
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        ThemeManager.getInstance().setScene(scene);
        
        // Set scene first, THEN restore size
        primaryStage.setScene(scene);
        
        // Restore window size and position before showing
        if (wasMaximized) {
            primaryStage.setMaximized(true);
        } else {
            primaryStage.setWidth(currentWidth);
            primaryStage.setHeight(currentHeight);
            primaryStage.setX(currentX);
            primaryStage.setY(currentY);
        }
        
        primaryStage.setTitle("Dynamic Island App - Login");
        
        // Add corner resize functionality for login page
        addResizeHandlers(primaryStage, scene);
        
        // Reapply rounded window clip for the login scene after logout
        applyRoundedShape(scene);
        
        // Show stage
        primaryStage.show();
        
        // Force multiple layout passes to ensure content is properly sized
        javafx.application.Platform.runLater(() -> {
            loginView.getRoot().layout();
            primaryStage.sizeToScene();
            javafx.application.Platform.runLater(() -> {
                loginView.getRoot().layout();
                primaryStage.sizeToScene();
            });
        });
        
        // Clean up landing page view
        if (landingPageView != null) {
            landingPageView.cleanup();
            landingPageView = null;
        }
        
        // Set up login monitoring again
        setupLoginMonitoring();
        
        System.out.println("✅ Successfully returned to login page!");
    }
    
    public static MainApplication getInstance() {
        return instance;
    }
    
    private void centerStageOnScreen(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }
    
    private void applyRoundedShape(Scene scene) {
        // Create rounded rectangle clip for the scene root
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        clip.widthProperty().bind(scene.widthProperty());
        clip.heightProperty().bind(scene.heightProperty());
        scene.getRoot().setClip(clip);
    }
    
    private void addResizeHandlers(Stage stage, Scene scene) {
        final double RESIZE_MARGIN = 8; // Pixel margin for resize detection
        final double WINDOW_BAR_HEIGHT = 40; // Height of custom window bar to exclude from resize
        final double[] resizeStartX = {0};
        final double[] resizeStartY = {0};
        final double[] resizeStartWidth = {0};
        final double[] resizeStartHeight = {0};
        final String[] resizeDirection = {""};
        
        scene.setOnMouseMoved(e -> {
            if (stage.isMaximized()) return;
            
            double mouseX = e.getSceneX();
            double mouseY = e.getSceneY();
            double width = scene.getWidth();
            double height = scene.getHeight();
            
            // Skip if mouse is over window bar (top 40px center area) to allow dragging
            if (mouseY < WINDOW_BAR_HEIGHT && mouseX > RESIZE_MARGIN && mouseX < width - RESIZE_MARGIN) {
                scene.setCursor(javafx.scene.Cursor.DEFAULT);
                return;
            }
            
            // Detect which edge/corner
            boolean left = mouseX < RESIZE_MARGIN;
            boolean right = mouseX > width - RESIZE_MARGIN;
            boolean top = mouseY < RESIZE_MARGIN;
            boolean bottom = mouseY > height - RESIZE_MARGIN;
            
            // Set cursor based on position
            if (top && left) {
                scene.setCursor(javafx.scene.Cursor.NW_RESIZE);
            } else if (top && right) {
                scene.setCursor(javafx.scene.Cursor.NE_RESIZE);
            } else if (bottom && left) {
                scene.setCursor(javafx.scene.Cursor.SW_RESIZE);
            } else if (bottom && right) {
                scene.setCursor(javafx.scene.Cursor.SE_RESIZE);
            } else if (top) {
                scene.setCursor(javafx.scene.Cursor.N_RESIZE);
            } else if (bottom) {
                scene.setCursor(javafx.scene.Cursor.S_RESIZE);
            } else if (left) {
                scene.setCursor(javafx.scene.Cursor.W_RESIZE);
            } else if (right) {
                scene.setCursor(javafx.scene.Cursor.E_RESIZE);
            } else {
                scene.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });
        
        scene.setOnMousePressed(e -> {
            if (stage.isMaximized()) return;
            
            double mouseX = e.getSceneX();
            double mouseY = e.getSceneY();
            double width = scene.getWidth();
            double height = scene.getHeight();
            
            // Skip if mouse is over window bar (top 40px center area)
            if (mouseY < WINDOW_BAR_HEIGHT && mouseX > RESIZE_MARGIN && mouseX < width - RESIZE_MARGIN) {
                resizeDirection[0] = "";
                return;
            }
            
            resizeStartX[0] = e.getScreenX();
            resizeStartY[0] = e.getScreenY();
            resizeStartWidth[0] = stage.getWidth();
            resizeStartHeight[0] = stage.getHeight();
            
            // Detect which edge/corner
            boolean left = mouseX < RESIZE_MARGIN;
            boolean right = mouseX > width - RESIZE_MARGIN;
            boolean top = mouseY < RESIZE_MARGIN;
            boolean bottom = mouseY > height - RESIZE_MARGIN;
            
            if (top && left) {
                resizeDirection[0] = "NW";
            } else if (top && right) {
                resizeDirection[0] = "NE";
            } else if (bottom && left) {
                resizeDirection[0] = "SW";
            } else if (bottom && right) {
                resizeDirection[0] = "SE";
            } else if (top) {
                resizeDirection[0] = "N";
            } else if (bottom) {
                resizeDirection[0] = "S";
            } else if (left) {
                resizeDirection[0] = "W";
            } else if (right) {
                resizeDirection[0] = "E";
            } else {
                resizeDirection[0] = "";
            }
        });
        
        scene.setOnMouseDragged(e -> {
            if (stage.isMaximized() || resizeDirection[0].isEmpty()) return;
            
            double deltaX = e.getScreenX() - resizeStartX[0];
            double deltaY = e.getScreenY() - resizeStartY[0];
            
            String dir = resizeDirection[0];
            
            // Handle horizontal resizing
            if (dir.contains("W")) {
                double newWidth = resizeStartWidth[0] - deltaX;
                if (newWidth >= stage.getMinWidth()) {
                    stage.setX(e.getScreenX());
                    stage.setWidth(newWidth);
                }
            } else if (dir.contains("E")) {
                double newWidth = resizeStartWidth[0] + deltaX;
                if (newWidth >= stage.getMinWidth()) {
                    stage.setWidth(newWidth);
                }
            }
            
            // Handle vertical resizing
            if (dir.contains("N")) {
                double newHeight = resizeStartHeight[0] - deltaY;
                if (newHeight >= stage.getMinHeight()) {
                    stage.setY(e.getScreenY());
                    stage.setHeight(newHeight);
                }
            } else if (dir.contains("S")) {
                double newHeight = resizeStartHeight[0] + deltaY;
                if (newHeight >= stage.getMinHeight()) {
                    stage.setHeight(newHeight);
                }
            }
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
