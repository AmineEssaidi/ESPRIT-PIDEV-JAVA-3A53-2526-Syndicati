package com.pidev.components.shared;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import com.pidev.utils.theme.ThemeManager;

/**
 * Video Background Component - Shows different videos based on theme, with image fallback
 */
public class ImageBackground {
    
    private final StackPane root;
    private MediaView backgroundMediaView;
    private ImageView backgroundImageView; // Fallback
    private MediaPlayer darkModePlayer;
    private MediaPlayer lightModePlayer;
    private MediaPlayer loginPlayer;
    private Image darkModeImage; // Fallback
    private Image lightModeImage; // Fallback
    private boolean isVideoMode = true;
    
    public ImageBackground() {
        this.root = new StackPane();
        setupLayout();
        loadImages();
        updateBackground();
    }
    
    private void setupLayout() {
        // Create media view for video background
        backgroundMediaView = new MediaView();
        backgroundMediaView.setPreserveRatio(false);
        backgroundMediaView.setSmooth(true);
        
        // Create image view for fallback
        backgroundImageView = new ImageView();
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setSmooth(true);
        
        // Make sure the background doesn't interfere with mouse events
        root.setMouseTransparent(true);
        
        // Ensure the root StackPane fills the entire area
        root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        root.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        
        // Position both views to cover the entire area
        StackPane.setAlignment(backgroundMediaView, Pos.CENTER);
        StackPane.setAlignment(backgroundImageView, Pos.CENTER);
        
        // Make sure both views fill the entire StackPane
        backgroundMediaView.fitWidthProperty().bind(root.widthProperty());
        backgroundMediaView.fitHeightProperty().bind(root.heightProperty());
        backgroundImageView.fitWidthProperty().bind(root.widthProperty());
        backgroundImageView.fitHeightProperty().bind(root.heightProperty());
        
        // Add both views to root (video on top, image as fallback)
        root.getChildren().addAll(backgroundImageView, backgroundMediaView);
    }
    
    private void loadImages() {
        try {
            // Try loading videos first (primary)
            System.out.println("🔄 VideoBackground: Loading videos...");
            loadVideos();
            
            // Try loading images as fallback
            System.out.println("🔄 VideoBackground: Loading fallback images...");
            loadFallbackImages();
            
            System.out.println("✅ VideoBackground: Loading process completed!");
            
        } catch (Exception e) {
            System.err.println("❌ VideoBackground: Error loading media: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadVideos() {
        try {
            // Try loading dark mode video
            System.out.println("🔄 VideoBackground: Loading darkmode.mp4...");
            try {
                Media darkModeMedia = new Media(getClass().getResource("/darkmode.mp4").toString());
                darkModePlayer = new MediaPlayer(darkModeMedia);
                darkModePlayer.setAutoPlay(true);
                darkModePlayer.setCycleCount(MediaPlayer.INDEFINITE);
                darkModePlayer.setMute(true); // Mute by default
                System.out.println("🎥 darkModePlayer: ✅ Loaded successfully");
            } catch (Exception e) {
                System.err.println("❌ VideoBackground: Failed to load darkmode.mp4: " + e.getMessage());
                darkModePlayer = null;
            }
            
            // Try loading light mode video
            System.out.println("🔄 VideoBackground: Loading lightmode.mp4...");
            try {
                Media lightModeMedia = new Media(getClass().getResource("/lightmode.mp4").toString());
                lightModePlayer = new MediaPlayer(lightModeMedia);
                lightModePlayer.setAutoPlay(true);
                lightModePlayer.setCycleCount(MediaPlayer.INDEFINITE);
                lightModePlayer.setMute(true); // Mute by default
                System.out.println("🎥 lightModePlayer: ✅ Loaded successfully");
            } catch (Exception e) {
                System.err.println("❌ VideoBackground: Failed to load lightmode.mp4: " + e.getMessage());
                lightModePlayer = null;
            }
            
            // Try loading login video
            System.out.println("🔄 VideoBackground: Loading login.mp4...");
            try {
                Media loginMedia = new Media(getClass().getResource("/login.mp4").toString());
                loginPlayer = new MediaPlayer(loginMedia);
                loginPlayer.setAutoPlay(true);
                loginPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                loginPlayer.setMute(true); // Mute by default
                System.out.println("🎥 loginPlayer: ✅ Loaded successfully");
            } catch (Exception e) {
                System.err.println("❌ VideoBackground: Failed to load login.mp4: " + e.getMessage());
                loginPlayer = null;
            }
            
            // Check if any videos loaded successfully
            if (darkModePlayer == null && lightModePlayer == null && loginPlayer == null) {
                System.err.println("❌ VideoBackground: No videos loaded successfully, falling back to images");
                isVideoMode = false;
            } else {
                System.out.println("✅ VideoBackground: At least one video loaded successfully");
            }
            
        } catch (Exception e) {
            System.err.println("❌ VideoBackground: General video loading error, falling back to images: " + e.getMessage());
            e.printStackTrace();
            isVideoMode = false;
        }
    }
    
    private void loadFallbackImages() {
        try {
            // Try loading dark mode image
            System.out.println("🔄 VideoBackground: Loading darkmode.jpg fallback...");
            darkModeImage = new Image(getClass().getResourceAsStream("/darkmode.jpg"));
            System.out.println("🖼️ darkModeImage: " + (darkModeImage != null ? "✅ Loaded" : "❌ Failed"));
            
            // Try loading light mode image
            System.out.println("🔄 VideoBackground: Loading lightmode.jpeg fallback...");
            lightModeImage = new Image(getClass().getResourceAsStream("/lightmode.jpeg"));
            System.out.println("🖼️ lightModeImage: " + (lightModeImage != null ? "✅ Loaded" : "❌ Failed"));
            
            // If classpath loading fails, try alternative methods
            if (darkModeImage == null || lightModeImage == null) {
                System.out.println("🔄 VideoBackground: Trying alternative image loading methods...");
                
                // Try with different path formats
                try {
                    if (darkModeImage == null) {
                        darkModeImage = new Image("file:src/main/resources/darkmode.jpg");
                        System.out.println("🖼️ darkModeImage (file): " + (darkModeImage != null ? "✅ Loaded" : "❌ Failed"));
                    }
                    if (lightModeImage == null) {
                        lightModeImage = new Image("file:src/main/resources/lightmode.jpeg");
                        System.out.println("🖼️ lightModeImage (file): " + (lightModeImage != null ? "✅ Loaded" : "❌ Failed"));
                    }
                } catch (Exception e2) {
                    System.err.println("❌ VideoBackground: File loading also failed: " + e2.getMessage());
                }
                
                // Try absolute path as last resort
                try {
                    if (darkModeImage == null) {
                        darkModeImage = new Image("file:C:/Users/amine/Desktop/2024 backup/ami_pidev_java/src/main/resources/darkmode.jpg");
                        System.out.println("🖼️ darkModeImage (absolute): " + (darkModeImage != null ? "✅ Loaded" : "❌ Failed"));
                    }
                    if (lightModeImage == null) {
                        lightModeImage = new Image("file:C:/Users/amine/Desktop/2024 backup/ami_pidev_java/src/main/resources/lightmode.jpeg");
                        System.out.println("🖼️ lightModeImage (absolute): " + (lightModeImage != null ? "✅ Loaded" : "❌ Failed"));
                    }
                } catch (Exception e3) {
                    System.err.println("❌ VideoBackground: Absolute path loading also failed: " + e3.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ VideoBackground: Error loading fallback images: " + e.getMessage());
        }
    }
    
    private void updateBackground() {
        if (backgroundMediaView == null && backgroundImageView == null) {
            System.out.println("⚠️ VideoBackground: Both media and image views are null");
            return;
        }
        
        ThemeManager themeManager = ThemeManager.getInstance();
        boolean isDark = themeManager.isDarkMode();
        
        System.out.println("🎥 VideoBackground: Setting theme to " + (isDark ? "Dark" : "Light"));
        
        if (isVideoMode) {
            // Try video mode first
            updateVideoBackground(isDark);
        } else {
            // Fall back to image mode
            updateImageBackground(isDark);
        }
    }
    
    private void updateVideoBackground(boolean isDark) {
        try {
            // Stop current video
            if (darkModePlayer != null) darkModePlayer.stop();
            if (lightModePlayer != null) lightModePlayer.stop();
            if (loginPlayer != null) loginPlayer.stop();
            
            MediaPlayer currentPlayer = isDark ? darkModePlayer : lightModePlayer;
            
            System.out.println("🎥 VideoBackground: Debug - isDark: " + isDark);
            System.out.println("🎥 VideoBackground: Debug - darkModePlayer: " + (darkModePlayer != null ? "available" : "null"));
            System.out.println("🎥 VideoBackground: Debug - lightModePlayer: " + (lightModePlayer != null ? "available" : "null"));
            System.out.println("🎥 VideoBackground: Debug - currentPlayer: " + (currentPlayer != null ? "available" : "null"));
            
            if (currentPlayer != null) {
                System.out.println("🎥 VideoBackground: Playing " + (isDark ? "dark" : "light") + " mode video");
                backgroundMediaView.setMediaPlayer(currentPlayer);
                backgroundMediaView.setVisible(true);
                backgroundImageView.setVisible(false);
                currentPlayer.play();
                System.out.println("✅ VideoBackground: Video background updated successfully!");
            } else {
                System.out.println("❌ VideoBackground: Video player is null, falling back to images");
                System.out.println("❌ VideoBackground: Specifically, " + (isDark ? "darkModePlayer" : "lightModePlayer") + " is null");
                updateImageBackground(isDark);
            }
        } catch (Exception e) {
            System.err.println("❌ VideoBackground: Error playing video, falling back to images: " + e.getMessage());
            e.printStackTrace();
            updateImageBackground(isDark);
        }
    }
    
    private void updateImageBackground(boolean isDark) {
        Image currentImage = isDark ? darkModeImage : lightModeImage;
        
        System.out.println("🖼️ VideoBackground: Using " + (isDark ? "darkModeImage" : "lightModeImage") + " fallback");
        System.out.println("🖼️ VideoBackground: darkModeImage is " + (darkModeImage != null ? "available" : "null"));
        System.out.println("🖼️ VideoBackground: lightModeImage is " + (lightModeImage != null ? "available" : "null"));
        System.out.println("🖼️ VideoBackground: currentImage is " + (currentImage != null ? "available" : "null"));
        
        if (currentImage != null) {
            System.out.println("🖼️ VideoBackground: Setting fallback image with dimensions: " + 
                currentImage.getWidth() + "x" + currentImage.getHeight());
            
            backgroundImageView.setImage(currentImage);
            backgroundImageView.setVisible(true);
            backgroundMediaView.setVisible(false);
            
            System.out.println("✅ VideoBackground: Fallback image background updated successfully!");
        } else {
            System.out.println("❌ VideoBackground: Current fallback image is null!");
            if (isDark) {
                System.out.println("❌ VideoBackground: darkModeImage fallback failed to load");
            } else {
                System.out.println("❌ VideoBackground: lightModeImage fallback failed to load");
            }
        }
    }
    
    public void updateTheme() {
        System.out.println("🔄 VideoBackground: updateTheme() called");
        updateBackground();
    }
    
    public void setLoginMode() {
        System.out.println("🔄 VideoBackground: Setting login mode");
        if (isVideoMode && loginPlayer != null) {
            try {
                // Stop current videos
                if (darkModePlayer != null) darkModePlayer.stop();
                if (lightModePlayer != null) lightModePlayer.stop();
                
                System.out.println("🎥 VideoBackground: Playing login video");
                backgroundMediaView.setMediaPlayer(loginPlayer);
                backgroundMediaView.setVisible(true);
                backgroundImageView.setVisible(false);
                loginPlayer.play();
                System.out.println("✅ VideoBackground: Login video background set successfully!");
            } catch (Exception e) {
                System.err.println("❌ VideoBackground: Error playing login video, falling back to images: " + e.getMessage());
                updateImageBackground(ThemeManager.getInstance().isDarkMode());
            }
        } else {
            System.out.println("❌ VideoBackground: Login video not available, using fallback");
            updateImageBackground(ThemeManager.getInstance().isDarkMode());
        }
    }
    
    public StackPane getRoot() {
        return root;
    }
    
    public void cleanup() {
        // Clean up video resources
        System.out.println("🧹 VideoBackground: Cleaning up video resources");
        if (darkModePlayer != null) {
            darkModePlayer.stop();
            darkModePlayer.dispose();
        }
        if (lightModePlayer != null) {
            lightModePlayer.stop();
            lightModePlayer.dispose();
        }
        if (loginPlayer != null) {
            loginPlayer.stop();
            loginPlayer.dispose();
        }
    }
}
