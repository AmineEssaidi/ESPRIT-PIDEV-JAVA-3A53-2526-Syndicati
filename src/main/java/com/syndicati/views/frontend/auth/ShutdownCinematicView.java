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
 * ShutdownCinematicView - Reverses the intro cinematic for a premium exit experience.
 */
public class ShutdownCinematicView {

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

    public ShutdownCinematicView() {
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
                mediaPlayer.setVolume(0.4);
            }
        } catch (Exception e) {}
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
        tt.play();
    }

    private void setupNetwork() {
        networkLayer = new Pane();
        networkLayer.setOpacity(0);
        signalLayer = new Pane();
        
        int nodeCount = 30;
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
                double dx = n1.circle.getTranslateX() - n2.circle.getTranslateX();
                double dy = n1.circle.getTranslateY() - n2.circle.getTranslateY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < threshold) {
                    line.setStartX(n1.circle.getTranslateX());
                    line.setStartY(n1.circle.getTranslateY());
                    line.setEndX(n2.circle.getTranslateX());
                    line.setEndY(n2.circle.getTranslateY());
                    line.setOpacity((1.0 - (dist / threshold)) * 0.3);
                    line.setVisible(true);
                } else line.setVisible(false);
            }
        }
    }

    private void setupContent() {
        lettersBox = new HBox(10);
        lettersBox.setAlignment(Pos.CENTER);
        
        String brand = "SHUTTING DOWN";
        for (char c : brand.toCharArray()) {
            Label l = new Label(String.valueOf(c));
            l.setStyle("-fx-font-family: 'Clash Grotesk Bold'; -fx-font-size: 64px; -fx-text-fill: white; -fx-opacity: 0;");
            l.setRotationAxis(Rotate.X_AXIS);
            l.setRotate(-45);
            l.setTranslateY(-50);
            l.setEffect(new GaussianBlur(20));
            lettersBox.getChildren().add(l);
        }

        tagline = new Label("ALL SYSTEMS SECURED.");
        tagline.setStyle("-fx-font-family: 'Archivo'; -fx-font-size: 13px; -fx-text-fill: white; -fx-opacity: 0; -fx-letter-spacing: 12px;");
        tagline.setTranslateY(100);

        centerContent = new VBox(0);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(lettersBox, tagline);
        root.getChildren().add(centerContent);
    }

    public void play() {
        if (mediaPlayer != null) mediaPlayer.play();
        networkTimeline.play();
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), fogLayer);
        fadeIn.setToValue(1.0);
        FadeTransition netIn = new FadeTransition(Duration.millis(1000), networkLayer);
        netIn.setToValue(0.6);
        fadeIn.play(); netIn.play();

        ParallelTransition lettersAnim = new ParallelTransition();
        for (int i = 0; i < lettersBox.getChildren().size(); i++) {
            Label l = (Label) lettersBox.getChildren().get(i);
            Duration delay = Duration.millis(200 + (i * 80));
            FadeTransition ft = new FadeTransition(Duration.millis(800), l);
            ft.setToValue(1.0); ft.setDelay(delay);
            TranslateTransition tt = new TranslateTransition(Duration.millis(1200), l);
            tt.setToY(0); tt.setDelay(delay);
            tt.setInterpolator(Interpolator.SPLINE(0.1, 0.8, 0.2, 1.0));
            RotateTransition rt = new RotateTransition(Duration.millis(1200), l);
            rt.setToAngle(0); rt.setDelay(delay);
            Timeline blur = new Timeline(new KeyFrame(delay.add(Duration.millis(1200)), new KeyValue(((GaussianBlur)l.getEffect()).radiusProperty(), 0)));
            lettersAnim.getChildren().addAll(ft, tt, rt, blur);
        }
        
        FadeTransition tagFade = new FadeTransition(Duration.millis(1500), tagline);
        tagFade.setToValue(0.7); tagFade.setDelay(Duration.millis(800));

        PauseTransition hold = new PauseTransition(Duration.seconds(2.5));
        FadeTransition finalOut = new FadeTransition(Duration.seconds(1.2), root);
        finalOut.setToValue(0.0);
        finalOut.setOnFinished(e -> {
            if (mediaPlayer != null) mediaPlayer.stop();
            if (onFinished != null) onFinished.run();
        });

        new SequentialTransition(lettersAnim, tagFade, hold, finalOut).play();
    }

    private class NetworkNode {
        final Circle circle;
        double vx, vy, vz;
        double z; 
        NetworkNode(Random r, String accent) {
            this.z = r.nextDouble() * 1000 - 500;
            this.circle = new Circle(2 + r.nextDouble() * 3, Color.web(accent, 0.4));
            this.circle.setTranslateX(r.nextDouble() * 1500);
            this.circle.setTranslateY(r.nextDouble() * 1000);
            this.vx = (r.nextDouble() - 0.5) * 2;
            this.vy = (r.nextDouble() - 0.5) * 2;
        }
        void update() {
            circle.setTranslateX(circle.getTranslateX() + vx);
            circle.setTranslateY(circle.getTranslateY() + vy);
            if (circle.getTranslateX() < 0 || circle.getTranslateX() > 1500) vx *= -1;
            if (circle.getTranslateY() < 0 || circle.getTranslateY() > 1000) vy *= -1;
        }
    }
}
