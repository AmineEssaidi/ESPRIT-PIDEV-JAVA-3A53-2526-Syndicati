package com.syndicati.components.shared;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Cinematic Parallax Background Component.
 * Completely replaces the buggy JavaFX MediaView with a smooth, 
 * zero-lag Ken Burns image animation effect to eliminate UI freezing.
 */
public class ImageBackground {
    private final StackPane root;
    private final List<ImageView> imageViews = new ArrayList<>();
    private SequentialTransition animationSequence;

    // Optional video background support (login view).
    private MediaPlayer videoPlayer;
    private MediaView mediaView;
    private boolean videoMode = false;
    private ChangeListener<javafx.scene.Scene> sceneListener;

    private static final String STARTUP_CINEMATIC_VIDEO_URL =
        "https://ik.imagekit.io/b0dxqylai/syndicati.mp4?updatedAt=1778146299878";

    public ImageBackground() {
        this.root = new StackPane();
        setupLayout();
        // Load images on a virtual thread so it doesn't block UI initialization
        Thread.startVirtualThread(this::loadImagesAndAnimate);
    }

    private void setupLayout() {
        root.setMouseTransparent(true);
        root.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        root.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        root.setStyle("-fx-background-color: #000000;"); 

        // Ensure background elements size against the actual Scene (matches cinematics),
        // not against transient computed sizes during view construction.
        sceneListener = (obs, oldScene, newScene) -> {
            if (mediaView != null) {
                rebindMediaViewSize();
            }
            if (!imageViews.isEmpty()) {
                for (ImageView iv : imageViews) {
                    rebindImageViewSize(iv);
                }
            }
        };
        root.sceneProperty().addListener(sceneListener);
    }

    private void rebindMediaViewSize() {
        if (mediaView == null) return;
        try {
            mediaView.fitWidthProperty().unbind();
            mediaView.fitHeightProperty().unbind();
        } catch (Exception ignored) { }

        javafx.scene.Scene s = root.getScene();
        if (s != null) {
            mediaView.fitWidthProperty().bind(s.widthProperty());
            mediaView.fitHeightProperty().bind(s.heightProperty());
        } else {
            mediaView.fitWidthProperty().bind(root.widthProperty());
            mediaView.fitHeightProperty().bind(root.heightProperty());
        }
    }

    private void rebindImageViewSize(ImageView iv) {
        if (iv == null) return;
        try {
            iv.fitWidthProperty().unbind();
            iv.fitHeightProperty().unbind();
        } catch (Exception ignored) { }

        javafx.scene.Scene s = root.getScene();
        if (s != null) {
            iv.fitWidthProperty().bind(s.widthProperty());
            iv.fitHeightProperty().bind(s.heightProperty());
        } else {
            iv.fitWidthProperty().bind(root.widthProperty());
            iv.fitHeightProperty().bind(root.heightProperty());
        }
    }

    private void loadImagesAndAnimate() {
        try {
            System.out.println("Loading cinematic background frames...");
            List<Image> frames = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                // Try loading from images folder (extracted by OpenCV)
                java.net.URL res = getClass().getResource("/images/login_bg_" + i + ".jpg");
                if (res != null) {
                    frames.add(new Image(res.toExternalForm()));
                }
            }
            
            if (frames.isEmpty()) {
                System.out.println("Frames not found, loading fallback.");
                java.net.URL res = getClass().getResource("/darkmode.jpg");
                if (res != null) frames.add(new Image(res.toExternalForm()));
            }

            Platform.runLater(() -> {
                // If video mode got enabled after constructor, stop here.
                if (videoMode) return;

                for (int i = 0; i < frames.size(); i++) {
                    ImageView iv = new ImageView(frames.get(i));
                    // Allow the image to stretch to cover the screen
                    iv.setPreserveRatio(false);
                    iv.setSmooth(true);
                    rebindImageViewSize(iv);
                    
                    // First image is fully visible, others are hidden
                    iv.setOpacity(i == 0 ? 1.0 : 0.0);
                    
                    // Set an initial scale so the image can zoom outwards if needed
                    iv.setScaleX(1.05);
                    iv.setScaleY(1.05);
                    
                    imageViews.add(iv);
                    root.getChildren().add(iv);
                    StackPane.setAlignment(iv, Pos.CENTER);
                }
                
                System.out.println("Cinematic background frames loaded. Starting animation.");
                startAnimationSequence();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAnimationSequence() {
        if (imageViews.isEmpty() || imageViews.size() == 1) return;
        
        animationSequence = new SequentialTransition();
        animationSequence.setCycleCount(Timeline.INDEFINITE);
        
        double durationMs = 8000; // 8 seconds total per image cycle
        double fadeMs = 2500; // 2.5 seconds crossfade duration
        
        for (int i = 0; i < imageViews.size(); i++) {
            ImageView current = imageViews.get(i);
            ImageView next = imageViews.get((i + 1) % imageViews.size());
            
            // 1. Current image zooms in very slowly
            ScaleTransition stCurrent = new ScaleTransition(Duration.millis(durationMs), current);
            stCurrent.setFromX(1.05); stCurrent.setFromY(1.05);
            stCurrent.setToX(1.15); stCurrent.setToY(1.15);
            stCurrent.setInterpolator(Interpolator.LINEAR);
            
            // 2. Towards the end of the current image's cycle, the NEXT image fades in
            FadeTransition ftNext = new FadeTransition(Duration.millis(fadeMs), next);
            ftNext.setFromValue(0.0);
            ftNext.setToValue(1.0);
            ftNext.setDelay(Duration.millis(durationMs - fadeMs));
            
            // 3. At the exact same time, the CURRENT image fades out
            FadeTransition ftCurrent = new FadeTransition(Duration.millis(fadeMs), current);
            ftCurrent.setFromValue(1.0);
            ftCurrent.setToValue(0.0);
            ftCurrent.setDelay(Duration.millis(durationMs - fadeMs));

            // Run these three transitions simultaneously for this cycle
            ParallelTransition pt = new ParallelTransition(stCurrent, ftNext, ftCurrent);
            animationSequence.getChildren().add(pt);
        }
        
        animationSequence.play();
    }

    // Public API stubs to maintain compatibility with views that were calling these on the video player
    public void updateTheme() { }

    public void setLoginMode() {
        // Try video background first; fall back to JPG animations (already loaded/possibly still loading).
        // Calling this multiple times should be safe and should not stack layers/timelines.
        if (videoPlayer != null && mediaView != null) {
            return;
        }
        videoMode = true;
        try {
            Media media = new Media(STARTUP_CINEMATIC_VIDEO_URL);
            videoPlayer = new MediaPlayer(media);
            videoPlayer.setVolume(0.0); // mute
            videoPlayer.setAutoPlay(false);
            videoPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            mediaView = new MediaView(videoPlayer);
            // Match the other cinematics: fill the whole background surface.
            // (This is also the closest to what users perceive as "not zoomed/cropped".)
            mediaView.setPreserveRatio(false);
            rebindMediaViewSize();
            mediaView.setSmooth(true);
            // Center the video inside the stage when aspect ratios differ.
            StackPane.setAlignment(mediaView, Pos.CENTER);

            // Stop and remove the frame animation if it started.
            if (animationSequence != null) {
                animationSequence.stop();
                animationSequence = null;
            }
            if (!imageViews.isEmpty()) {
                for (ImageView iv : imageViews) {
                    root.getChildren().remove(iv);
                }
                imageViews.clear();
            }

            // Add the MediaView first (so the first rendered frame doesn't "pop" later).
            if (!root.getChildren().contains(mediaView)) {
                root.getChildren().add(0, mediaView);
            }

            // Avoid starting playback until JavaFX has decoded enough to render smoothly.
            // Starting too early often looks like a slideshow on some Windows setups.
            videoPlayer.setOnReady(() -> Platform.runLater(() -> {
                try {
                    videoPlayer.seek(Duration.ZERO);
                    videoPlayer.play();
                } catch (Exception ignored) {
                    // If play() fails here, fallback will kick in via onError.
                }
            }));

            videoPlayer.setOnError(() -> {
                System.err.println("Login video playback error, falling back to frames. " + videoPlayer.getError());
                Platform.runLater(() -> {
                    // Remove video layer and allow JPG mode again
                    if (mediaView != null) {
                        root.getChildren().remove(mediaView);
                        mediaView = null;
                    }
                    if (videoPlayer != null) {
                        try { videoPlayer.stop(); } catch (Exception ignored) {}
                        videoPlayer = null;
                    }
                    videoMode = false;
                    // If frames were never loaded (because videoMode was true), kick them off now.
                    if (imageViews.isEmpty() && animationSequence == null) {
                        Thread.startVirtualThread(ImageBackground.this::loadImagesAndAnimate);
                    }
                });
            });
        } catch (Exception e) {
            // Keep frame animation as fallback.
            videoMode = false;
            System.err.println("Video background failed in login mode, using frames. " + e.getMessage());
        }
    }
    
    public StackPane getRoot() { 
        return root; 
    }
    
    public void cleanup() {
        System.out.println("Cleaning up cinematic background resources...");
        if (animationSequence != null) {
            animationSequence.stop();
            animationSequence = null;
        }
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer = null;
        }
        for (ImageView iv : imageViews) {
            iv.imageProperty().unbind();
            iv.setImage(null);
        }
        imageViews.clear();
        if (mediaView != null) {
            root.getChildren().remove(mediaView);
            mediaView = null;
        }
        if (sceneListener != null) {
            try { root.sceneProperty().removeListener(sceneListener); } catch (Exception ignored) {}
            sceneListener = null;
        }
        root.getChildren().clear();
    }
}
