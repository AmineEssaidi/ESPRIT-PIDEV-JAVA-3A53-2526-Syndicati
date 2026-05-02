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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
// Removed netscape.javascript.JSObject to avoid deprecation in Java 24+

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Premium hCaptcha Component using a Modal Popup for the challenge.
 */
@SuppressWarnings("all")
public class HCaptchaComponent {
    private final HCaptchaService hcaptchaService;
    private VBox container;
    private volatile String captchaToken = "";
    private HCaptchaLocalServer localServer;
    private volatile boolean verified = false;
    private volatile boolean loadFailed = false;
    private Runnable onLoadFail;
    private Runnable onVerified;
    private Stage currentModal;
    private double xOffset = 0;
    private double yOffset = 0;

    private WebView modalWebView;
    private WebEngine modalEngine;
    private boolean modalInitialized = false;

    public HCaptchaComponent() {
        this.hcaptchaService = new HCaptchaService();
        initializeComponent();
        // Proactively initialize the WebView and local server on a background thread
        Platform.runLater(this::preInitializeModal);
    }

    private void preInitializeModal() {
        if (modalInitialized) return;

        modalWebView = new WebView();
        modalWebView.setPrefSize(500, 680);
        modalWebView.setPageFill(Color.TRANSPARENT);
        VBox.setVgrow(modalWebView, Priority.ALWAYS);
        modalEngine = modalWebView.getEngine();

        try {
            Path tempDir = Files.createTempDirectory("syndicati-captcha-");
            modalEngine.setUserDataDirectory(tempDir.toFile());
        } catch (Exception e) {}

        // Use a future-proof Alert-based bridge instead of the deprecated JSObject
        modalEngine.setOnAlert(event -> {
            String data = event.getData();
            if (data == null) return;

            if (data.startsWith("captcha:verified:")) {
                setCaptchaToken(data.substring(17));
                setVerified(true);
            } else if (data.startsWith("captcha:log:")) {
                log(data.substring(12));
            }
        });

        try {
            if (localServer == null) {
                localServer = new HCaptchaLocalServer(buildHCaptchaHtml());
                localServer.start();
            }
            modalEngine.load(localServer.getUrl());
            modalInitialized = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initializeComponent() {
        container = new VBox();
        container.setSpacing(0);
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
        if (verified || modalWebView == null) return;
        
        Platform.runLater(() -> {
            // Swap the button for the actual hCaptcha WebView
            container.getChildren().clear();
            
            // Remove constraints that were meant for the tiny "Verify" button
            container.setPrefHeight(Region.USE_COMPUTED_SIZE);
            container.setMaxHeight(Double.MAX_VALUE);
            container.setPrefWidth(Region.USE_COMPUTED_SIZE);
            container.setMaxWidth(Double.MAX_VALUE);
            
            container.getChildren().add(modalWebView);
            
            // Ensure the panel is open in the main view
            com.syndicati.views.frontend.login.LoginView.getInstance().openCaptchaPanel();
        });
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
                com.syndicati.views.frontend.login.LoginView.getInstance().closeCaptchaPanel();
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
            min-height: 600px;
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
            // Use Alert-based messaging bridge (future-proof replacement for window.java)
            alert("captcha:verified:" + token);
        }
        function onCaptchaError() {
            alert("captcha:log:hCaptcha error");
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

    public VBox getContainer() {
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
