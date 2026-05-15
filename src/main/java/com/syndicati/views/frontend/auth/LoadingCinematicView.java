package com.syndicati.views.frontend.auth;

import com.syndicati.utils.theme.ThemeManager;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.Random;
import com.syndicati.utils.media.CinematicVideoCache;

/**
 * LoadingCinematicView - A premium, localized loading transition.
 * Features a volumetric background and staggered typography reveal.
 */
public class LoadingCinematicView {

    private final StackPane root;
    private final ThemeManager tm = ThemeManager.getInstance();
    private final String accentHex = tm.getAccentHex();
    private final Random rand = new Random();

    private MediaPlayer videoPlayer;
    private MediaView mediaView;
    private SequentialTransition activeSequence;
    private final java.util.List<Animation> ambientAnimations = new java.util.ArrayList<>();
    private boolean useVideoBackground = true;
    
    private Pane backgroundLayer;
    private VBox content;
    private Label loadingLabel;
    private Label viewLabel;

    public LoadingCinematicView(String targetView) {
        this.root = new StackPane();
        this.root.setStyle("-fx-background-color: #010103;");
        
        setupAtmosphere();
        setupContent(getHumanReadableName(targetView));
    }

    public StackPane getRoot() {
        return root;
    }

    private void setupAtmosphere() {
        if (useVideoBackground && setupVideoBackground()) {
            return;
        }
        
        backgroundLayer = new Pane();
        backgroundLayer.setMouseTransparent(true);
        
        // Background Glow
        Circle glow = new Circle(600, new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(accentHex, 0.15)),
            new Stop(1, Color.TRANSPARENT)
        ));
        glow.setEffect(new GaussianBlur(150));
        backgroundLayer.getChildren().add(glow);
        
        // Volumetric Particles
        for (int i = 0; i < 25; i++) {
            double size = 1.5 + rand.nextDouble() * 3;
            Circle p = new Circle(size, Color.web(accentHex, 0.25));
            p.setTranslateX(rand.nextDouble() * 1400 - 100);
            p.setTranslateY(rand.nextDouble() * 900 - 50);
            p.setEffect(new GaussianBlur(rand.nextDouble() * 5));
            backgroundLayer.getChildren().add(p);
            
            // Floating up animation
            TranslateTransition tt = new TranslateTransition(Duration.seconds(3 + rand.nextDouble() * 4), p);
            tt.setByY(-200 - rand.nextDouble() * 200);
            tt.setCycleCount(Timeline.INDEFINITE);
            tt.setAutoReverse(true);
            tt.setInterpolator(Interpolator.EASE_BOTH);
            tt.play();
            ambientAnimations.add(tt);
            
            // Subtle pulse
            ScaleTransition st = new ScaleTransition(Duration.seconds(2 + rand.nextDouble() * 2), p);
            st.setToX(1.5); st.setToY(1.5);
            st.setCycleCount(Timeline.INDEFINITE);
            st.setAutoReverse(true);
            st.play();
            ambientAnimations.add(st);
        }
        
        root.getChildren().add(backgroundLayer);
    }

    private boolean setupVideoBackground() {
        try {
            try { CinematicVideoCache.warmupAsync(); } catch (Throwable ignored) {}
            String src;
            try {
                src = CinematicVideoCache.getStartupVideoSource();
            } catch (Throwable t) {
                src = CinematicVideoCache.REMOTE_STARTUP_VIDEO_URL;
            }
            Media media = new Media(src);
            videoPlayer = new MediaPlayer(media);
            videoPlayer.setVolume(0.0);
            videoPlayer.setAutoPlay(false);
            videoPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            mediaView = new MediaView(videoPlayer);
            mediaView.setPreserveRatio(false);
            mediaView.fitWidthProperty().bind(root.widthProperty());
            mediaView.fitHeightProperty().bind(root.heightProperty());
            mediaView.setSmooth(true);

            root.getChildren().add(0, mediaView);
            return true;
        } catch (Exception e) {
            useVideoBackground = false;
            System.err.println("[LoadingCinematic] Video background failed: " + e.getMessage());
            return false;
        }
    }

    private void setupContent(String targetView) {
        content = new VBox(-5);
        content.setAlignment(Pos.CENTER);
        
        loadingLabel = new Label("LOADING");
        loadingLabel.setStyle("-fx-font-family: 'Clash Grotesk Bold'; -fx-font-size: 56px; -fx-text-fill: white; -fx-letter-spacing: 14px; -fx-opacity: 0;");
        loadingLabel.setTranslateY(20);
        
        viewLabel = new Label(targetView.toUpperCase());
        viewLabel.setStyle("-fx-font-family: 'Archivo'; -fx-font-size: 15px; -fx-text-fill: " + accentHex + "; -fx-letter-spacing: 8px; -fx-opacity: 0;");
        viewLabel.setTranslateY(30);
        
        content.getChildren().addAll(loadingLabel, viewLabel);
        root.getChildren().add(content);
    }

    public void play(Runnable onFinished) {
        if (videoPlayer != null) {
            if (videoPlayer.getStatus() == MediaPlayer.Status.READY) {
                videoPlayer.seek(Duration.ZERO);
                videoPlayer.play();
            } else {
                videoPlayer.setOnReady(() -> {
                    try {
                        videoPlayer.seek(Duration.ZERO);
                        videoPlayer.play();
                    } catch (Exception ignored) {}
                });
            }
        }

        // Staggered Entrance
        FadeTransition f1 = new FadeTransition(Duration.millis(800), loadingLabel);
        f1.setToValue(1.0);
        TranslateTransition t1 = new TranslateTransition(Duration.millis(1200), loadingLabel);
        t1.setToY(0);
        t1.setInterpolator(Interpolator.SPLINE(0.1, 0.8, 0.2, 1.0));
        
        FadeTransition f2 = new FadeTransition(Duration.millis(800), viewLabel);
        f2.setToValue(0.7);
        f2.setDelay(Duration.millis(300));
        TranslateTransition t2 = new TranslateTransition(Duration.millis(1200), viewLabel);
        t2.setToY(0);
        t2.setDelay(Duration.millis(300));
        t2.setInterpolator(Interpolator.SPLINE(0.1, 0.8, 0.2, 1.0));
        
        PauseTransition pause = new PauseTransition(Duration.millis(1500));
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), root);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            if (videoPlayer != null) videoPlayer.stop();
            onFinished.run();
        });
        
        activeSequence = new SequentialTransition(
            new ParallelTransition(f1, t1, f2, t2),
            pause,
            fadeOut
        );
        activeSequence.play();
    }

    public void cleanup() {
        try {
            if (activeSequence != null) {
                activeSequence.stop();
                activeSequence = null;
            }
        } catch (Exception ignored) {}
        for (Animation animation : java.util.List.copyOf(ambientAnimations)) {
            try { animation.stop(); } catch (Exception ignored) {}
        }
        ambientAnimations.clear();
        try {
            if (videoPlayer != null) {
                videoPlayer.stop();
                videoPlayer.dispose();
            }
        } catch (Exception ignored) {}
        videoPlayer = null;
        if (mediaView != null) {
            try { mediaView.setMediaPlayer(null); } catch (Exception ignored) {}
        }
        mediaView = null;
        try { root.getChildren().clear(); } catch (Exception ignored) {}
    }

    private String getHumanReadableName(String pageName) {
        if (pageName == null) return "Unknown";
        String normalized = pageName.toLowerCase().trim();
        switch (normalized) {
            case "home": return "Nexus Central";
            case "services": return "Available Modules";
            case "profile": return "Identity Hub";
            case "dashboard": return "Command Center";
            case "settings": return "System Config";
            case "residence": 
            case "services/residence": return "Residential Node";
            case "forum": 
            case "services/forum": return "Community Grid";
            case "syndicat": 
            case "services/syndicat": return "Syndic Matrix";
            case "evenement": 
            case "services/evenement": return "Syndicati Events";
            case "about": return "The Architecture";
            default: return pageName;
        }
    }
}
