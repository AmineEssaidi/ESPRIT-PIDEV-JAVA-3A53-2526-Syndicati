package com.syndicati.views.frontend.auth;

import com.syndicati.MainApplication;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.ui.HorizonDesignSystem;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.syndicati.utils.media.CinematicVideoCache;

/**
 * SessionRecoveryView - A cinematic, high-end animation view shown during auto-login.
 * Now merged with destination choice logic for a seamless entry experience.
 */
public class SessionRecoveryView {

    private final StackPane root;
    private final ThemeManager tm = ThemeManager.getInstance();
    private final String accentHex = tm.getAccentHex();
    
    private ProgressBar customProgress;
    private Label statusLabel;
    private final List<Particle> particles = new ArrayList<>();
    private Timeline particleTimeline;
    private final java.util.List<Animation> activeAnimations = new java.util.ArrayList<>();
    
    private VBox statusBox;
    private HBox actionsBox;
    private VBox userCard;
    private ImageView avatarView;
    private Label nameLabel;

    private MediaPlayer videoPlayer;
    private MediaView mediaView;
    private boolean useVideoBackground = true;
    
    private Runnable onGoHome;
    private Runnable onGoDashboard;

    public SessionRecoveryView() {
        this.root = new StackPane();
        this.root.setStyle("-fx-background-color: #050508;");
        
        setupContent();
        startAmbientAnimations();
    }

    public StackPane getRoot() {
        return root;
    }

    public void setOnGoHome(Runnable r) { this.onGoHome = r; }
    public void setOnGoDashboard(Runnable r) { this.onGoDashboard = r; }

    private void setupContent() {
        // Background video replaces dot/particle visuals.
        setupVideoBackground();

        // 1. Cinematic Background
        Circle topGlow = new Circle(400, Color.web(accentHex, 0.08));
        topGlow.setTranslateY(-300);
        topGlow.setEffect(new GaussianBlur(120));
        
        Circle bottomGlow = new Circle(300, Color.web(accentHex, 0.05));
        bottomGlow.setTranslateY(400);
        bottomGlow.setEffect(new GaussianBlur(100));

        VBox centerBox = new VBox(40);
        centerBox.setAlignment(Pos.CENTER);

        // 2. User Card (Avatar + Name)
        userCard = new VBox(20);
        userCard.setAlignment(Pos.CENTER);
        userCard.setOpacity(0);
        userCard.setTranslateY(20);

        StackPane avatarFrame = new StackPane();
        Circle avatarClip = new Circle(50, 50, 50);
        
        avatarView = new ImageView();
        avatarView.setFitWidth(100);
        avatarView.setFitHeight(100);
        avatarView.setClip(avatarClip);
        
        // Initial state
        setDefaultAvatar(avatarView);

        Circle border = new Circle(54);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(accentHex)),
            new Stop(1, Color.web(accentHex, 0.3))
        ));
        border.setStrokeWidth(2);
        
        avatarFrame.getChildren().addAll(border, avatarView);

        nameLabel = new Label(com.syndicati.utils.localization.LocalizationManager.getInstance().get("recovering").toUpperCase());
        nameLabel.setStyle("-fx-font-family: 'Clash Grotesk Bold'; -fx-font-size: 24px; -fx-text-fill: white; -fx-letter-spacing: 2px;");
        
        userCard.getChildren().addAll(avatarFrame, nameLabel);
        
        // Try to update immediately if user already in session
        updateUser(SessionManager.getInstance().getCurrentUser());

        // 3. Status Box
        statusBox = new VBox(15);
        statusBox.setAlignment(Pos.CENTER);
        
        statusLabel = new Label(com.syndicati.utils.localization.LocalizationManager.getInstance().get("warming_workspace").toUpperCase());
        statusLabel.setStyle("-fx-font-family: 'Archivo'; -fx-font-size: 12px; -fx-text-fill: white; -fx-opacity: 0.5; -fx-letter-spacing: 4px;");
        
        customProgress = new ProgressBar();
        statusBox.getChildren().addAll(statusLabel, customProgress);

        // 4. Actions Box (Initially hidden)
        actionsBox = new HBox(15);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setOpacity(0);
        actionsBox.setManaged(false);

        centerBox.getChildren().addAll(userCard, statusBox, actionsBox);

        // 5. Particle Layer (only if video fails)
        if (!useVideoBackground) {
            Pane particleLayer = new Pane();
            particleLayer.setMouseTransparent(true);
            createParticles(particleLayer);
            root.getChildren().addAll(topGlow, bottomGlow, particleLayer, centerBox);
        } else {
            root.getChildren().addAll(topGlow, bottomGlow, centerBox);
        }
    }

    private void setupVideoBackground() {
        if (!useVideoBackground) return;
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
        } catch (Exception e) {
            useVideoBackground = false;
            System.err.println("[SessionRecovery] Video background failed: " + e.getMessage());
        }
    }

    private void setDefaultAvatar(ImageView iv) {
        iv.setImage(new Image("https://api.dicebear.com/7.x/avataaars/png?seed=Syndicati", true));
    }

    private void createParticles(Pane layer) {
        Random rand = new Random();
        for (int i = 0; i < 40; i++) {
            Particle p = new Particle(rand, accentHex);
            particles.add(p);
            layer.getChildren().add(p.node);
        }
    }

    private void startAmbientAnimations() {
        if (!useVideoBackground && !particles.isEmpty()) {
            particleTimeline = new Timeline(new KeyFrame(Duration.millis(30), e -> {
                for (Particle p : particles) p.update();
            }));
            particleTimeline.setCycleCount(Timeline.INDEFINITE);
            particleTimeline.play();
        }
        
        // Fade in user card
        FadeTransition ft = new FadeTransition(Duration.seconds(1), userCard);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.seconds(1), userCard);
        tt.setToY(0);
        ParallelTransition intro = new ParallelTransition(ft, tt);
        activeAnimations.add(intro);
        intro.play();

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
    }

    public void setProgress(double p, String status) {
        javafx.application.Platform.runLater(() -> {
            customProgress.setProgress(p);
            statusLabel.setText(status.toUpperCase());
            
            if (p >= 1.0) {
                showDestinationButtons();
            }
        });
    }

    public void updateUser(User user) {
        if (user == null) return;
        javafx.application.Platform.runLater(() -> {
            nameLabel.setText((user.getFirstName() + " " + user.getLastName()).toUpperCase());
            Profile profile = SessionManager.getInstance().getCurrentProfile();
            if (profile != null && profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
                Image img = com.syndicati.utils.image.ImageLoaderUtil.loadProfileAvatar(profile.getAvatar());
                if (img != null) {
                    avatarView.setImage(img);
                } else {
                    setDefaultAvatar(avatarView);
                }
            } else {
                setDefaultAvatar(avatarView);
            }
        });
    }

    private void showDestinationButtons() {
        if (actionsBox.isManaged()) {
            return;
        }
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            // Fallback: if user is still null in session manager, try to use the one we might have cached or wait
            return;
        }

        // 1. Create Buttons
        Button homeBtn = createStyledButton(com.syndicati.utils.localization.LocalizationManager.getInstance().get("go_to_community").toUpperCase(), false);
        homeBtn.setOnAction(e -> { if(onGoHome != null) onGoHome.run(); });

        actionsBox.getChildren().add(homeBtn);

        // Add Dashboard if Admin/Syndic
        String role = user.getRoleUser();
        if ("ADMIN".equals(role) || "SUPERADMIN".equals(role) || "SYNDIC".equals(role)) {
            Button dashBtn = createStyledButton(com.syndicati.utils.localization.LocalizationManager.getInstance().get("go_to_dashboard").toUpperCase(), true);
            dashBtn.setOnAction(e -> { if(onGoDashboard != null) onGoDashboard.run(); });
            actionsBox.getChildren().add(dashBtn);
        }

        // 2. Transition
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), statusBox);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            statusBox.setManaged(false);
            actionsBox.setManaged(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), actionsBox);
            fadeIn.setToValue(1);
            fadeIn.play();
            
            TranslateTransition slideUp = new TranslateTransition(Duration.millis(800), actionsBox);
            slideUp.setFromY(20); slideUp.setToY(0);
            slideUp.play();
        });
        activeAnimations.add(fadeOut);
        fadeOut.play();
    }

    private Button createStyledButton(String text, boolean primary) {
        Button btn = new Button(text);
        if (primary) {
            btn.setStyle(HorizonDesignSystem.buttonPrimary() +
                "-fx-font-family: 'Clash Grotesk Bold';-fx-font-size: 13px;-fx-padding: 12 25 12 25;");
            btn.setEffect(new DropShadow(15, Color.web(accentHex, 0.4)));
        } else {
            btn.setStyle(HorizonDesignSystem.buttonGhost() +
                "-fx-font-family: 'Clash Grotesk Bold';-fx-font-size: 13px;-fx-padding: 12 25 12 25;");
        }
        HorizonDesignSystem.installButtonMotion(btn);
        
        return btn;
    }

    public void cleanup() {
        try {
            if (particleTimeline != null) {
                particleTimeline.stop();
                particleTimeline = null;
            }
        } catch (Exception ignored) {}
        for (Animation animation : java.util.List.copyOf(activeAnimations)) {
            try { animation.stop(); } catch (Exception ignored) {}
        }
        activeAnimations.clear();
        try {
            if (videoPlayer != null) {
                videoPlayer.stop();
                videoPlayer.dispose();
            }
        } catch (Exception ignored) {}
        videoPlayer = null;
        if (mediaView != null) {
            try { mediaView.setMediaPlayer(null); } catch (Exception ignored) {}
            mediaView = null;
        }
        try { avatarView.setImage(null); } catch (Exception ignored) {}
        try { root.getChildren().clear(); } catch (Exception ignored) {}
    }

    // --- INNER CLASSES ---

    private static class ProgressBar extends StackPane {
        private final Rectangle fill;
        public ProgressBar() {
            setMaxWidth(220);
            setPrefHeight(4);
            setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 2;");
            
            fill = new Rectangle(0, 4);
            fill.setArcWidth(4); fill.setArcHeight(4);
            fill.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web(ThemeManager.getInstance().getAccentHex())),
                new Stop(1, Color.web(ThemeManager.getInstance().getAccentHex()).deriveColor(0, 1, 1.4, 1))
            ));
            
            setAlignment(Pos.CENTER_LEFT);
            getChildren().add(fill);
        }
        public void setProgress(double p) {
            Timeline tl = new Timeline(new KeyFrame(Duration.millis(400), 
                new KeyValue(fill.widthProperty(), getMaxWidth() * p)
            ));
            tl.play();
        }
    }

    private static class Particle {
        final Node node;
        double x, y, vx, vy, alpha;
        final Random rand;
        
        Particle(Random rand, String color) {
            this.rand = rand;
            Circle c = new Circle(rand.nextDouble() * 2 + 1, Color.web(color, 0.4));
            c.setEffect(new DropShadow(5, Color.web(color)));
            this.node = c;
            reset();
        }
        
        void reset() {
            x = rand.nextDouble() * 1200;
            y = rand.nextDouble() * 800;
            vx = (rand.nextDouble() - 0.5) * 1.2;
            vy = (rand.nextDouble() - 0.5) * 1.2;
            node.setOpacity(0);
        }
        
        void update() {
            x += vx; y += vy;
            node.setTranslateX(x);
            node.setTranslateY(y);
            alpha += 0.02;
            node.setOpacity(Math.sin(alpha) * 0.3 + 0.3);
            if (x < 0 || x > 1500 || y < 0 || y > 1000) reset();
        }
    }
}
