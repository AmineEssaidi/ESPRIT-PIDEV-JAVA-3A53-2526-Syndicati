package com.syndicati.components.shared;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

/**
 * Video Background Component - Login video only with image fallback
 */
public class ImageBackground {
    
    private final StackPane root;
    private MediaView backgroundMediaView;
    private ImageView backgroundImageView;
    private MediaPlayer loginPlayer;
    private Image fallbackImage;
    private boolean isVideoMode = true;
    
    public ImageBackground() {
        this.root = new StackPane();
        setupLayout();
        loadMedia();
        updateBackground();
    }
    
    private void setupLayout() {
        backgroundMediaView = new MediaView();
        backgroundMediaView.setPreserveRatio(false);
        backgroundMediaView.setSmooth(true);
        
        backgroundImageView = new ImageView();
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setSmooth(true);
        
        root.setMouseTransparent(true);
        root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        root.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        
        StackPane.setAlignment(backgroundMediaView, Pos.CENTER);
        StackPane.setAlignment(backgroundImageView, Pos.CENTER);
        
        backgroundMediaView.fitWidthProperty().bind(root.widthProperty());
        backgroundMediaView.fitHeightProperty().bind(root.heightProperty());
        backgroundImageView.fitWidthProperty().bind(root.widthProperty());
        backgroundImageView.fitHeightProperty().bind(root.heightProperty());
        
        root.getChildren().addAll(backgroundImageView, backgroundMediaView);
    }
    
    private void loadMedia() {
        try {
            System.out.println("Loading login video...");
            loadLoginVideo();
            System.out.println("Loading fallback image...");
            loadFallbackImage();
            System.out.println("Media loading completed!");
        } catch (Exception e) {
            System.err.println("Error loading media: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadLoginVideo() {
        try {
            System.out.println("Loading login.mp4...");
            Media loginMedia = new Media(getClass().getResource("/login.mp4").toString());
            loginPlayer = new MediaPlayer(loginMedia);
            loginPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            loginPlayer.setMute(true);
            loginPlayer.setAutoPlay(false);
            System.out.println("Login video loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load login.mp4: " + e.getMessage());
            loginPlayer = null;
            isVideoMode = false;
        }
    }
    
    private void loadFallbackImage() {
        try {
            System.out.println("Loading fallback image...");
            fallbackImage = new Image(getClass().getResourceAsStream("/darkmode.jpg"));
            if (fallbackImage != null) {
                System.out.println("Fallback image loaded successfully");
            } else {
                System.out.println("Fallback image failed to load");
            }
        } catch (Exception e) {
            System.err.println("Error loading fallback image: " + e.getMessage());
        }
    }
    
    private void updateBackground() {
        if (backgroundMediaView == null && backgroundImageView == null) {
            System.out.println("Both media and image views are null");
            return;
        }
        updateImageBackground();
    }
    
    private void updateImageBackground() {
        if (fallbackImage != null) {
            System.out.println("Setting fallback image background");
            backgroundImageView.setImage(fallbackImage);
            backgroundImageView.setVisible(true);
            backgroundMediaView.setVisible(false);
        } else {
            System.out.println("Fallback image is null");
            backgroundImageView.setVisible(true);
            backgroundMediaView.setVisible(false);
        }
    }
    
    public void updateTheme() {
        System.out.println("updateTheme() called - login video only");
    }
    
    public void setLoginMode() {
        System.out.println("Setting login mode");
        if (isVideoMode && loginPlayer != null) {
            try {
                updateImageBackground();
                System.out.println("Preparing login video");
                playWhenReady(loginPlayer);
            } catch (Exception e) {
                System.err.println("Error playing login video: " + e.getMessage());
                updateImageBackground();
            }
        } else {
            System.out.println("Login video not available, using fallback");
            updateImageBackground();
        }
    }

    private void playWhenReady(MediaPlayer player) {
        if (player == null) {
            updateImageBackground();
            return;
        }

        try {
            player.seek(Duration.ZERO);
            player.stop();
        } catch (Exception e) {
            System.err.println("Error resetting player: " + e.getMessage());
        }

        Runnable startPlayback = () -> {
            try {
                backgroundMediaView.setMediaPlayer(player);
                backgroundMediaView.setVisible(true);
                backgroundImageView.setVisible(false);
                player.play();
                System.out.println("Video playback started successfully");
            } catch (Exception ex) {
                System.err.println("Failed to start playback: " + ex.getMessage());
                updateImageBackground();
            }
        };

        player.setOnError(() -> {
            System.err.println("Media error: " + player.getError());
            updateImageBackground();
        });
        player.setOnReady(startPlayback);

        MediaPlayer.Status status = player.getStatus();
        if (status == MediaPlayer.Status.READY
            || status == MediaPlayer.Status.PAUSED
            || status == MediaPlayer.Status.STOPPED) {
            startPlayback.run();
        }
    }
    
    public StackPane getRoot() {
        return root;
    }
    
    public void cleanup() {
        System.out.println("Cleaning up video resources");
        try {
            if (loginPlayer != null) {
                loginPlayer.stop();
                loginPlayer.dispose();
                loginPlayer = null;
            }
            if (backgroundMediaView != null) {
                backgroundMediaView.setMediaPlayer(null);
                backgroundMediaView.setVisible(false);
            }
            if (backgroundImageView != null) {
                backgroundImageView.setImage(null);
                backgroundImageView.setVisible(false);
            }
            System.out.println("Video resources cleaned up successfully");
        } catch (Exception e) {
            System.err.println("Error during media cleanup: " + e.getMessage());
        }
    }
}