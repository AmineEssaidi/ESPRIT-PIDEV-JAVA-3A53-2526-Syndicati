package com.syndicati.components.security;

import com.syndicati.services.observability.HCaptchaService;
import com.syndicati.services.security.HCaptchaLocalServer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Premium hCaptcha Component using a Modal Popup for the challenge.
 */
public class HCaptchaComponent {
    private final HCaptchaService hcaptchaService;
    private StackPane container;
    private volatile String captchaToken = "";
    private HCaptchaLocalServer localServer;
    private volatile boolean verified = false;
    private volatile boolean loadFailed = false;
    private Runnable onLoadFail;
    private Runnable onVerified;
    private Stage currentModal;
    private double xOffset = 0;
    private double yOffset = 0;

    public HCaptchaComponent() {
        this.hcaptchaService = new HCaptchaService();
        initializeComponent();
    }

    private void initializeComponent() {
        container = new StackPane();
        container.setAlignment(Pos.CENTER);
        container.setPrefHeight(60);
        container.setMaxWidth(350);

        Button verifyButton = new Button("Verify I am Human");
        verifyButton.setPrefWidth(220);
        verifyButton.setPrefHeight(45);
        verifyButton.setCursor(javafx.scene.Cursor.HAND);
        verifyButton.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.05);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(255, 255, 255, 0.1);" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );

        verifyButton.setOnMouseEntered(e -> verifyButton.setStyle(verifyButton.getStyle() + "-fx-background-color: rgba(255, 255, 255, 0.1);"));
        verifyButton.setOnMouseExited(e -> verifyButton.setStyle(verifyButton.getStyle().replace("-fx-background-color: rgba(255, 255, 255, 0.1);", "-fx-background-color: rgba(255, 255, 255, 0.05);")));

        verifyButton.setOnAction(e -> openCaptchaModal());
        
        container.getChildren().add(verifyButton);
    }

    private void openCaptchaModal() {
        if (verified) return;

        currentModal = new Stage();
        currentModal.initModality(Modality.APPLICATION_MODAL);
        currentModal.initStyle(StageStyle.TRANSPARENT);
        currentModal.setTitle("Security Verification");

        WebView modalWebView = new WebView();
        modalWebView.setPrefSize(420, 520);
        modalWebView.setPageFill(Color.TRANSPARENT);
        WebEngine modalEngine = modalWebView.getEngine();
        
        // Unique user data dir for this instance
        try {
            Path tempDir = Files.createTempDirectory("syndicati-captcha-");
            modalEngine.setUserDataDirectory(tempDir.toFile());
        } catch (Exception e) {}

        // Setup bridge for the modal
        modalEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) modalEngine.executeScript("window");
                window.setMember("java", this); // Use 'this' directly for reliability
            }
        });

        try {
            if (localServer == null) {
                localServer = new HCaptchaLocalServer(buildHCaptchaHtml());
                localServer.start();
            }
            modalEngine.load(localServer.getUrl());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Header with close button
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setPadding(new Insets(10, 10, 0, 10));
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> currentModal.close());
        header.getChildren().add(closeBtn);

        VBox root = new VBox(header, modalWebView);
        root.setStyle("-fx-background-color: transparent;"); 
        
        // Use a StackPane to center the hCaptcha perfectly
        StackPane rootWrapper = new StackPane(root);
        rootWrapper.setStyle("-fx-background-color: transparent;"); // Fully invisible
        rootWrapper.setPadding(new Insets(10));
        
        // Make draggable
        rootWrapper.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        rootWrapper.setOnMouseDragged(event -> {
            currentModal.setX(event.getScreenX() - xOffset);
            currentModal.setY(event.getScreenY() - yOffset);
        });

        Scene scene = new Scene(rootWrapper);
        scene.setFill(Color.TRANSPARENT);
        currentModal.setScene(scene);
        currentModal.show();
    }

    private void showVerifiedStatus() {
        container.getChildren().clear();
        Label verifiedLabel = new Label("✓ Security Verified");
        verifiedLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 16px;");
        container.getChildren().add(verifiedLabel);
    }

    public boolean hasLoadFailed() {
        return loadFailed;
    }

    public void setOnLoadFail(Runnable callback) {
        this.onLoadFail = callback;
    }

    public void setOnVerified(Runnable callback) {
        this.onVerified = callback;
    }

    public void setCaptchaToken(String token) {
        this.captchaToken = token;
    }

    public void setVerified(boolean v) {
        if (v) {
            this.verified = true;
            Platform.runLater(() -> {
                if (currentModal != null) {
                    currentModal.close();
                }
                showVerifiedStatus();
                if (onVerified != null) {
                    onVerified.run();
                }
            });
        }
    }

    public void log(String msg) {
        System.err.println("[HCaptcha-JS] " + msg);
    }

    private String buildHCaptchaHtml() {
        String siteKey = hcaptchaService.getSiteKey();
        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        * { background: transparent !important; }
        html, body {
            margin: 0;
            padding: 0;
            background: transparent !important;
            overflow: hidden;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }
        #captcha-target {
            background: transparent !important;
        }
    </style>
</head>
<body>
    <div id="captcha-target"></div>
    <script src="https://js.hcaptcha.com/1/api.js" async defer></script>
    <script>
        function onCaptchaSolved(token) {
            if (window.java) {
                window.java.setCaptchaToken(token);
                window.java.setVerified(true);
            }
        }
        function onCaptchaError() {
            if (window.java) window.java.log('hCaptcha error');
        }
        function render() {
            if (!window.hcaptcha) { setTimeout(render, 500); return; }
            window.hcaptcha.render("captcha-target", {
                sitekey: "HCAPTCHA_SITE_KEY",
                theme: "dark",
                callback: onCaptchaSolved,
                "error-callback": onCaptchaError
            });
        }
        window.onload = render;
    </script>
</body>
</html>
""".replace("HCAPTCHA_SITE_KEY", siteKey);
    }

    public StackPane getContainer() {
        return container;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public boolean isVerified() {
        return verified;
    }

    public void cleanup() {
        if (localServer != null) {
            localServer.stop();
            localServer = null;
        }
    }
}
