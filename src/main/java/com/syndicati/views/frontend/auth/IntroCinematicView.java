package com.syndicati.views.frontend.auth;

import com.syndicati.utils.theme.ThemeManager;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * IntroCinematicView - The "Volumetric Core" Edition.
 * Transforms 2D dots into 3D energy spheres using spherical shading and deep perspective.
 * Features a high-fidelity bokeh engine and real-time volumetric light integration.
 */
public class IntroCinematicView {

    private final StackPane root;
    private final ThemeManager tm = ThemeManager.getInstance();
    private final String accentHex = tm.getAccentHex();
    private MediaPlayer mediaPlayer;
    private Runnable onFinished;
    
    private Pane fogLayer;
    private Pane networkLayer;
    private Pane signalLayer;
    private VBox centerContent;
    private HBox lettersBox;
    private Label tagline;
    
    private final List<NetworkNode> nodes = new ArrayList<>();
    private final List<Line> links = new ArrayList<>();
    private Timeline networkTimeline;
    private final Random rand = new Random();

    public IntroCinematicView() {
        this.root = new StackPane();
        this.root.setStyle("-fx-background-color: #010103;");
        
        setupAtmosphere();
        setupNetwork();
        setupContent();
        setupAudio();
    }

    public StackPane getRoot() {
        return root;
    }

    public void setOnFinished(Runnable onFinished) {
        this.onFinished = onFinished;
    }

    private void setupAudio() {
        try {
            URL resource = getClass().getResource("/audio/intro.mp3");
            if (resource != null) {
                Media media = new Media(resource.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setVolume(0.5);
            }
        } catch (Exception e) {
            System.err.println("[Intro] Audio setup failed: " + e.getMessage());
        }
    }

    private void setupAtmosphere() {
        Circle coreGlow = new Circle(800, new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(accentHex, 0.08)),
            new Stop(1, Color.TRANSPARENT)
        ));
        coreGlow.setEffect(new GaussianBlur(150));
        
        fogLayer = new Pane();
        fogLayer.setOpacity(0);
        
        String[] fogColors = {accentHex, "#0EA5E9", "#4338CA"};
        for (String color : fogColors) {
            Circle fog = new Circle(600 + rand.nextDouble() * 200, Color.web(color, 0.03));
            fog.setTranslateX(rand.nextDouble() * 1200 - 600);
            fog.setTranslateY(rand.nextDouble() * 800 - 400);
            fog.setEffect(new GaussianBlur(180));
            fogLayer.getChildren().add(fog);
            animateFog(fog, 20 + rand.nextDouble() * 10);
        }

        root.getChildren().addAll(coreGlow, fogLayer);
    }

    private void animateFog(Circle fog, double duration) {
        TranslateTransition tt = new TranslateTransition(Duration.seconds(duration), fog);
        tt.setByX(rand.nextDouble() * 400 - 200);
        tt.setByY(rand.nextDouble() * 400 - 200);
        tt.setAutoReverse(true);
        tt.setCycleCount(Timeline.INDEFINITE);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.play();
        
        ScaleTransition st = new ScaleTransition(Duration.seconds(duration * 1.5), fog);
        st.setFromX(0.8); st.setFromY(0.8);
        st.setToX(1.3); st.setToY(1.3);
        st.setAutoReverse(true);
        st.setCycleCount(Timeline.INDEFINITE);
        st.play();
    }

    private void setupNetwork() {
        networkLayer = new Pane();
        networkLayer.setOpacity(0);
        
        signalLayer = new Pane();
        signalLayer.setMouseTransparent(true);
        
        int nodeCount = 40;
        for (int i = 0; i < nodeCount; i++) {
            NetworkNode node = new NetworkNode(rand, accentHex);
            nodes.add(node);
            networkLayer.getChildren().add(node.circle);
        }
        
        for (int i = 0; i < nodeCount; i++) {
            for (int j = i + 1; j < nodeCount; j++) {
                Line line = new Line();
                line.setStroke(Color.web(accentHex, 0.08));
                line.setStrokeWidth(0.5);
                line.setVisible(false);
                links.add(line);
                networkLayer.getChildren().add(line);
                line.toBack();
            }
        }
        
        networkTimeline = new Timeline(new KeyFrame(Duration.millis(30), e -> updateNetwork()));
        networkTimeline.setCycleCount(Timeline.INDEFINITE);

        root.getChildren().addAll(networkLayer, signalLayer);
    }

    private void updateNetwork() {
        double threshold = 240;
        int linkIdx = 0;
        
        for (NetworkNode node : nodes) node.update();
        
        for (int i = 0; i < nodes.size(); i++) {
            NetworkNode n1 = nodes.get(i);
            for (int j = i + 1; j < nodes.size(); j++) {
                NetworkNode n2 = nodes.get(j);
                Line line = links.get(linkIdx++);
                
                double zDiff = Math.abs(n1.z - n2.z);
                if (zDiff > 250) {
                    line.setVisible(false);
                    continue;
                }
                
                double dx = n1.circle.getTranslateX() - n2.circle.getTranslateX();
                double dy = n1.circle.getTranslateY() - n2.circle.getTranslateY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                
                if (dist < threshold) {
                    line.setStartX(n1.circle.getTranslateX());
                    line.setStartY(n1.circle.getTranslateY());
                    line.setEndX(n2.circle.getTranslateX());
                    line.setEndY(n2.circle.getTranslateY());
                    
                    double strength = (1.0 - (dist / threshold)) * (1.0 - (zDiff / 250.0));
                    line.setOpacity(strength * 0.4);
                    line.setVisible(true);
                    
                    if (rand.nextDouble() < 0.001 * strength) spawnSignal(n1, n2, strength);
                } else {
                    line.setVisible(false);
                }
            }
        }
    }

    private void spawnSignal(NetworkNode start, NetworkNode end, double strength) {
        Circle signal = new Circle(1.5 * strength, Color.WHITE);
        signal.setEffect(new DropShadow(5, Color.WHITE));
        signalLayer.getChildren().add(signal);
        
        signal.setTranslateX(start.circle.getTranslateX());
        signal.setTranslateY(start.circle.getTranslateY());
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(800 + rand.nextInt(400)), signal);
        tt.setToX(end.circle.getTranslateX());
        tt.setToY(end.circle.getTranslateY());
        tt.setOnFinished(e -> signalLayer.getChildren().remove(signal));
        tt.play();
    }

    private void setupContent() {
        lettersBox = new HBox(10);
        lettersBox.setAlignment(Pos.CENTER);
        
        String brand = "SYNDICATI";
        for (char c : brand.toCharArray()) {
            Label l = new Label(String.valueOf(c));
            l.setStyle("-fx-font-family: 'Clash Grotesk Bold'; -fx-font-size: 82px; -fx-text-fill: white; -fx-opacity: 0;");
            l.setRotationAxis(Rotate.X_AXIS);
            l.setRotate(45);
            l.setTranslateY(50);
            l.setEffect(new GaussianBlur(25));
            lettersBox.getChildren().add(l);
        }

        tagline = new Label("THE ARCHITECTURE OF CONNECTION.");
        tagline.setStyle("-fx-font-family: 'Archivo'; -fx-font-size: 13px; -fx-text-fill: white; -fx-opacity: 0; -fx-letter-spacing: 14px;");
        tagline.setTranslateY(110);

        centerContent = new VBox(0);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(lettersBox, tagline);
        
        root.getChildren().add(centerContent);
    }

    public void play() {
        if (mediaPlayer != null) mediaPlayer.play();
        networkTimeline.play();
        
        SequentialTransition mainSequence = new SequentialTransition();
        
        ParallelTransition reveal = new ParallelTransition();
        FadeTransition fogFade = new FadeTransition(Duration.millis(3000), fogLayer);
        fogFade.setToValue(1.0);
        FadeTransition networkFade = new FadeTransition(Duration.millis(3000), networkLayer);
        networkFade.setToValue(1.0);
        Timeline zoom = new Timeline(new KeyFrame(Duration.millis(5000), 
            new KeyValue(networkLayer.scaleXProperty(), 1.3, Interpolator.EASE_BOTH),
            new KeyValue(networkLayer.scaleYProperty(), 1.3, Interpolator.EASE_BOTH)
        ));
        reveal.getChildren().addAll(fogFade, networkFade, zoom);

        ParallelTransition lettersAnim = new ParallelTransition();
        for (int i = 0; i < lettersBox.getChildren().size(); i++) {
            Label l = (Label) lettersBox.getChildren().get(i);
            Duration delay = Duration.millis(1500 + (i * 120));
            
            FadeTransition ft = new FadeTransition(Duration.millis(1200), l);
            ft.setToValue(1.0);
            ft.setDelay(delay);
            
            TranslateTransition tt = new TranslateTransition(Duration.millis(1800), l);
            tt.setToY(0);
            tt.setDelay(delay);
            tt.setInterpolator(Interpolator.SPLINE(0.1, 0.8, 0.2, 1.0));
            
            RotateTransition rt = new RotateTransition(Duration.millis(1800), l);
            rt.setToAngle(0);
            rt.setDelay(delay);
            
            Timeline blurAnim = new Timeline(new KeyFrame(delay.add(Duration.millis(1800)), 
                new KeyValue(((GaussianBlur)l.getEffect()).radiusProperty(), 0)
            ));

            lettersAnim.getChildren().addAll(ft, tt, rt, blurAnim);
        }
        
        FadeTransition taglineFade = new FadeTransition(Duration.millis(2500), tagline);
        taglineFade.setToValue(0.8);
        taglineFade.setDelay(Duration.millis(1200));

        PauseTransition hold = new PauseTransition(Duration.seconds(3.5));
        FadeTransition rootFadeOut = new FadeTransition(Duration.seconds(1.5), root);
        rootFadeOut.setToValue(0.0);
        rootFadeOut.setOnFinished(e -> {
            if (mediaPlayer != null) mediaPlayer.stop();
            if (networkTimeline != null) networkTimeline.stop();
            if (onFinished != null) onFinished.run();
        });

        mainSequence.getChildren().addAll(reveal, lettersAnim, taglineFade, hold, rootFadeOut);
        mainSequence.play();
    }

    private class NetworkNode {
        final Circle circle;
        final GaussianBlur bokeh;
        double vx, vy, vz, pulseTime;
        double z; 
        final String accent;

        NetworkNode(Random r, String accent) {
            this.accent = accent;
            this.z = r.nextDouble() * 1000 - 500;
            this.circle = new Circle(4); // Larger base for volumetric shading
            this.bokeh = new GaussianBlur(0);
            this.circle.setEffect(bokeh);
            
            // Start in a larger field for parallax
            this.circle.setTranslateX(r.nextDouble() * 3000 - 500);
            this.circle.setTranslateY(r.nextDouble() * 2000 - 500);
            
            this.vx = (r.nextDouble() - 0.5) * 1.5;
            this.vy = (r.nextDouble() - 0.5) * 1.5;
            this.vz = (r.nextDouble() - 0.5) * 2.0; 
            this.pulseTime = r.nextDouble() * 100;
            
            updateVolumetricStyle();
        }

        private void updateVolumetricStyle() {
            double normZ = (z + 500) / 1000.0;
            double scale = 0.3 + normZ * 1.6;
            circle.setScaleX(scale);
            circle.setScaleY(scale);
            
            // Spherical Shading: Radial gradient makes it look like a glowing sphere
            RadialGradient shade = new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE), // Bright core
                new Stop(0.3, Color.web(accent, 0.9)), // Accent mid
                new Stop(1, Color.web(accent, 0.0)) // Fading edge
            );
            circle.setFill(shade);
            circle.setOpacity(0.2 + normZ * 0.8);
            
            // Depth-based Bokeh
            bokeh.setRadius(Math.max(0.1, (1.0 - normZ) * 10.0));
        }

        void update() {
            pulseTime += 0.05;
            z += vz;
            if (z < -500 || z > 500) vz *= -1; 
            
            double normZ = (z + 500) / 1000.0;
            circle.setTranslateX(circle.getTranslateX() + vx * (0.6 + normZ));
            circle.setTranslateY(circle.getTranslateY() + vy * (0.6 + normZ));
            
            updateVolumetricStyle();
            
            double pulse = 1.0 + Math.sin(pulseTime) * 0.25;
            circle.setScaleX(circle.getScaleX() * pulse);
            circle.setScaleY(circle.getScaleY() * pulse);
            
            if (circle.getTranslateX() < -500 || circle.getTranslateX() > 3000) vx *= -1;
            if (circle.getTranslateY() < -500 || circle.getTranslateY() > 2500) vy *= -1;
        }
    }
}
