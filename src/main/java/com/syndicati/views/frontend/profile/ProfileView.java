package com.syndicati.views.frontend.profile;

import com.syndicati.MainApplication;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.models.entities.Profile;
import com.syndicati.models.entities.User;
import com.syndicati.models.services.ProfileService;
import com.syndicati.models.services.UserService;
import com.syndicati.services.ProfileImageService;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.image.ImageLoaderUtil;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Files;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Profile page replica based on templates/frontend/profile/profile.html.twig.
 * This is a UI-only duplication without backend behavior.
 */
public class ProfileView implements ViewInterface {

    private static final DateTimeFormatter PROFILE_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VBox root;
    private final ThemeManager tm;
    private final SessionManager sessionManager;
    private final UserService userService;
    private final ProfileService profileService;

    private User currentUser;
    private Profile currentProfile;

    private final Map<String, VBox> mainPages = new LinkedHashMap<>();
    private final Map<String, Button> mainNavButtons = new LinkedHashMap<>();

    private final Map<String, VBox> detailTabs = new LinkedHashMap<>();
    private final Map<String, Button> detailTabButtons = new LinkedHashMap<>();

    public ProfileView() {
        this.tm = ThemeManager.getInstance();
        this.sessionManager = SessionManager.getInstance();
        this.userService = new UserService();
        this.profileService = new ProfileService();
        this.root = new VBox();
        build();
    }

    private void build() {
        hydrateSessionData();

        VBox content = new VBox(26);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(24, 0, 42, 0));
        content.setFillWidth(true);

        content.getChildren().add(createMainNavigation());

        VBox page1 = createOverviewPage();
        VBox page2 = createActivityPage();
        mainPages.put("overview", page1);
        mainPages.put("activity", page2);
        content.getChildren().addAll(page1, page2);

        setMainPage("overview");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        root.getChildren().add(scroll);
        root.setStyle("-fx-background-color: transparent;");
    }

    private HBox createMainNavigation() {
        HBox nav = new HBox(12);
        nav.setAlignment(Pos.CENTER);
        nav.setPadding(new Insets(8));
        nav.setMaxWidth(560);
        nav.setStyle(
            "-fx-background-color: " + surfaceSoft() + ";" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 999px;" +
            "-fx-border-radius: 999px;"
        );

        Button overview = createMainNavButton("Overview", "overview");
        Button activity = createMainNavButton("Activity & Details", "activity");
        mainNavButtons.put("overview", overview);
        mainNavButtons.put("activity", activity);
        nav.getChildren().addAll(overview, activity);
        return nav;
    }

    private Button createMainNavButton(String label, String key) {
        Button btn = new Button(label);
        btn.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 13));
        btn.setOnAction(e -> setMainPage(key));
        styleMainNavButton(btn, false);
        return btn;
    }

    private void setMainPage(String key) {
        mainPages.forEach((name, pane) -> {
            boolean active = name.equals(key);
            pane.setVisible(active);
            pane.setManaged(active);
        });
        mainNavButtons.forEach((name, btn) -> styleMainNavButton(btn, name.equals(key)));
    }

    private void styleMainNavButton(Button btn, boolean active) {
        if (active) {
            btn.setStyle(
                "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
                "-fx-text-fill: white; -fx-font-weight: 800;" +
                "-fx-background-radius: 999px; -fx-padding: 12 24 12 24;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + textMuted() + ";" +
                "-fx-font-weight: 800;" +
                "-fx-background-radius: 999px; -fx-padding: 12 24 12 24;"
            );
        }
    }

    private VBox createOverviewPage() {
        VBox page = new VBox(24);
        page.setAlignment(Pos.TOP_CENTER);

        page.getChildren().add(createHeroCard());

        HBox row = new HBox(22);
        row.setAlignment(Pos.TOP_CENTER);
        row.setMaxWidth(1800);
        row.prefWidthProperty().bind(root.widthProperty().multiply(0.95));
        VBox standing = createStandingCard();
        VBox circle = createCircleCard();
        HBox.setHgrow(standing, Priority.ALWAYS);
        HBox.setHgrow(circle, Priority.ALWAYS);
        standing.setMaxWidth(Double.MAX_VALUE);
        circle.setMaxWidth(Double.MAX_VALUE);
        row.getChildren().addAll(standing, circle);

        page.getChildren().add(row);
        return page;
    }

    private VBox createHeroCard() {
        VBox card = new VBox(0);
        card.setMaxWidth(1800);
        card.setMaxHeight(Double.MAX_VALUE);
        card.prefWidthProperty().bind(root.widthProperty().multiply(0.95));
        card.setStyle(shell(34, surfaceCard(), 0.16));

        StackPane banner = new StackPane();
        banner.setMinHeight(180);
        banner.setStyle(
            "-fx-background-color: linear-gradient(to right, rgba(43,43,58,0.95), rgba(20,20,28,0.9));" +
            "-fx-background-radius: 34px 34px 0 0;"
        );
        banner.getChildren().add(text("PROFILE", 40, true, "rgba(255,255,255,0.20)"));

        HBox body = new HBox(26);
        body.setPadding(new Insets(24));
        body.setAlignment(Pos.TOP_LEFT);
        body.setMaxHeight(Double.MAX_VALUE);
        body.setMaxWidth(Double.MAX_VALUE);

        StackPane avatarWrap = new StackPane();
        Circle avatar = new Circle(56);
        avatar.setStroke(Color.web(tm.toRgba(tm.getAccentHex(), 0.35)));
        avatar.setStrokeWidth(2);
        Text avatarText = text(displayInitial(), 38, true, "#ffffff");
        
        // Load profile avatar image if available
        Profile currentProfile = sessionManager.getCurrentProfile();
        boolean hasImage = false;
        if (currentProfile != null && currentProfile.getAvatar() != null && !currentProfile.getAvatar().isBlank()) {
            Image img = ImageLoaderUtil.loadProfileAvatar(currentProfile.getAvatar(), false);
            if (img != null && !img.isError()) {
                avatar.setFill(new ImagePattern(img));
                hasImage = true;
                avatarText.setVisible(false);
                avatarText.setMouseTransparent(true);
            } else {
                avatar.setFill(Color.web(tm.toRgba(tm.getAccentHex(), 0.22)));
            }
        } else {
            avatar.setFill(Color.web(tm.toRgba(tm.getAccentHex(), 0.22)));
        }
        
        // Make avatar clickable for image upload
        avatar.setCursor(javafx.scene.Cursor.HAND);
        avatar.setPickOnBounds(true);
        avatar.setOnMouseClicked(e -> handleAvatarClick(avatar, avatarText));
        
        avatarWrap.getChildren().addAll(avatar, avatarText);

        VBox identity = new VBox(10);
        identity.setAlignment(Pos.TOP_LEFT);
        
        // Container for default info and quick actions menu (with animation switching)
        // Use StackPane to layer sections - only one visible at a time
        StackPane heroContent = new StackPane();
        heroContent.setStyle("-fx-alignment: top-left;");
        heroContent.setMaxHeight(Double.MAX_VALUE);
        heroContent.setMaxWidth(Double.MAX_VALUE);
        
        // Default hero info (name, role, email, stats, quick actions button)
        VBox defaultInfo = new VBox(10);
        defaultInfo.setAlignment(Pos.TOP_LEFT);
        defaultInfo.setMaxWidth(Double.MAX_VALUE);
        defaultInfo.setMaxHeight(Double.MAX_VALUE);
        Text name = text(displayName(), 34, true, "#ffffff");
        Text role = text(displayRole(), 16, false, textSoft());
        Text email = text(displayEmail(), 14, false, textMuted());

        HBox stats = new HBox(12,
            statPill("Account created", displayCreatedAt()),
            statPill("Status", displayVerified())
        );

        HBox quickTop = new HBox();
        quickTop.setAlignment(Pos.CENTER_RIGHT);
        Button quickBtn = new Button("Quick Actions");
        quickBtn.setStyle("-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.18) + "; -fx-text-fill: " + tm.getAccentHex() + "; -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.35) + "; -fx-border-width: 1px; -fx-background-radius: 999px; -fx-border-radius: 999px; -fx-padding: 9 16 9 16; -fx-font-weight: 700;");
        quickTop.getChildren().add(quickBtn);
        
        defaultInfo.getChildren().addAll(name, role, email, stats, quickTop);
        
        // Quick actions menu (tiles + back button)
        VBox quickActionMenu = new VBox(12);
        quickActionMenu.setAlignment(Pos.TOP_LEFT);
        quickActionMenu.setMaxWidth(Double.MAX_VALUE);
        quickActionMenu.setMaxHeight(Double.MAX_VALUE);
        quickActionMenu.setVisible(false);
        quickActionMenu.setManaged(false);
        quickActionMenu.setOpacity(0);
        
        HBox menuHeader = new HBox(10);
        menuHeader.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = new Button("←");
        backBtn.setFont(Font.font(18));
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + textMuted() + "; -fx-font-weight: 700; -fx-padding: 0;");
        backBtn.setPrefSize(30, 30);
        Text menuTitle = text("Quick Actions", 16, true, "#ffffff");
        menuHeader.getChildren().addAll(backBtn, menuTitle);
        
        GridPane actionTiles = new GridPane();
        actionTiles.setHgap(10);
        actionTiles.setVgap(10);
        actionTiles.setMaxWidth(Double.MAX_VALUE);
        
        // Column constraints - make columns expand equally
        ColumnConstraints c = new ColumnConstraints();
        c.setPercentWidth(33.33);
        c.setFillWidth(true);
        actionTiles.getColumnConstraints().addAll(c, c, c);
        
        // Row constraints - make rows expand equally
        RowConstraints r = new RowConstraints();
        r.setPercentHeight(50);
        r.setFillHeight(true);
        actionTiles.getRowConstraints().addAll(r, r);
        
        // Action tiles with proper icons (no emojis - text labels instead)
        actionTiles.add(createActionTile("Host Spotlight", "▶"), 0, 0);
        actionTiles.add(createActionTile("Join by Code", "#"), 1, 0);
        actionTiles.add(createActionTile("2FA", "◆"), 2, 0);
        actionTiles.add(createActionTile("Biometrics", "◉"), 0, 1);
        actionTiles.add(createActionTile("Face ID", "◐"), 1, 1);
        actionTiles.add(createActionTile("Settings", "⚙"), 2, 1);
        
        quickActionMenu.getChildren().addAll(menuHeader, actionTiles);
        VBox.setVgrow(actionTiles, Priority.ALWAYS);
        
        // Create panels with back buttons and handlers
        VBox hostPanel = createQuickActionPanel("Host Spotlight", "Start a video spotlight session and invite residents to join.", "Start Spotlight");
        VBox joinPanel = createQuickActionPanel("Join by Code", "Enter a room code to join an existing spotlight session.", "Join Session");
        VBox twoFAPanel = createTwoFAPanel();
        VBox biometricsPanel = createQuickActionPanel("Biometrics", "Use your device's biometrics to log in faster.", "Register Device");
        VBox faceIDPanel = createQuickActionPanel("Face ID", "Protect your account with local Face ID (PC only).", "Start Enrollment");
        VBox settingsPanel = createSettingsPanel();
        
        // Set panels to expand to fill the hero space
        for (VBox panel : new VBox[]{hostPanel, joinPanel, twoFAPanel, biometricsPanel, faceIDPanel, settingsPanel}) {
            panel.setMaxWidth(Double.MAX_VALUE);
            panel.setMaxHeight(Double.MAX_VALUE);
            panel.setVisible(false);
            panel.setManaged(false);
            panel.setOpacity(0);
        }
        
        // Wire back buttons to show quick menu
        wireBackButton(hostPanel, quickActionMenu, defaultInfo);
        wireBackButton(joinPanel, quickActionMenu, defaultInfo);
        wireBackButton(twoFAPanel, quickActionMenu, defaultInfo);
        wireBackButton(biometricsPanel, quickActionMenu, defaultInfo);
        wireBackButton(faceIDPanel, quickActionMenu, defaultInfo);
        wireBackButton(settingsPanel, quickActionMenu, defaultInfo);
        
        // Add tiles to a list for easy iteration
        java.util.List<VBox> tileList = new java.util.ArrayList<>();
        for (javafx.scene.Node node : actionTiles.getChildren()) {
            if (node instanceof VBox) {
                tileList.add((VBox)node);
            }
        }
        
        // Tile mapping to panels
        java.util.Map<Integer, VBox> tilePanelMap = new java.util.HashMap<>();
        tilePanelMap.put(0, hostPanel);       // Host Spotlight
        tilePanelMap.put(1, joinPanel);        // Join by Code
        tilePanelMap.put(2, twoFAPanel);       // 2FA
        tilePanelMap.put(3, biometricsPanel);  // Biometrics
        tilePanelMap.put(4, faceIDPanel);      // Face ID
        tilePanelMap.put(5, settingsPanel);    // Settings
        
        // Set tile click handlers
        int tileIndex = 0;
        VBox[] allPanels = {hostPanel, joinPanel, twoFAPanel, biometricsPanel, faceIDPanel, settingsPanel};
        for (VBox tile : tileList) {
            final int index = tileIndex;
            tile.setOnMouseClicked(e -> {
                // Hide quick menu and show selected panel with animation
                quickActionMenu.setVisible(false);
                quickActionMenu.setManaged(false);
                
                // Hide all panels except the selected one
                for (int i = 0; i < allPanels.length; i++) {
                    allPanels[i].setVisible(false);
                    allPanels[i].setOpacity(0);
                }
                
                // Show selected panel with fade animation
                VBox selectedPanel = allPanels[index];
                selectedPanel.setVisible(true);
                selectedPanel.setManaged(true);
                
                FadeTransition fade = new FadeTransition(Duration.millis(200), selectedPanel);
                fade.setFromValue(0);
                fade.setToValue(1.0);
                fade.play();
            });
            tileIndex++;
        }
        
        // Add all to hero content (StackPane layers them - only one visible at a time)
        heroContent.getChildren().addAll(defaultInfo, quickActionMenu, hostPanel, joinPanel, twoFAPanel, biometricsPanel, faceIDPanel, settingsPanel);
        
        // Quick actions button toggle
        quickBtn.setOnAction(e -> {
            if (quickActionMenu.isVisible()) {
                // Hide menu, show default info
                FadeTransition fade = new FadeTransition(Duration.millis(200), quickActionMenu);
                fade.setFromValue(1);
                fade.setToValue(0);
                
                fade.setOnFinished(ev -> {
                    quickActionMenu.setVisible(false);
                    quickActionMenu.setManaged(false);
                    defaultInfo.setVisible(true);
                    defaultInfo.setOpacity(1.0);
                });
                fade.play();
            } else {
                // Show menu, hide default info
                defaultInfo.setVisible(false);
                quickActionMenu.setVisible(true);
                quickActionMenu.setManaged(true);
                quickActionMenu.setOpacity(0);
                
                FadeTransition fade = new FadeTransition(Duration.millis(200), quickActionMenu);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
            }
        });
        
        backBtn.setOnAction(e -> {
            if (quickActionMenu.isVisible()) {
                // Hide menu, show default info
                FadeTransition fade = new FadeTransition(Duration.millis(200), quickActionMenu);
                fade.setFromValue(1);
                fade.setToValue(0);
                
                fade.setOnFinished(ev -> {
                    quickActionMenu.setVisible(false);
                    quickActionMenu.setManaged(false);
                    defaultInfo.setVisible(true);
                    defaultInfo.setOpacity(1.0);
                });
                fade.play();
            }
        });
        
        identity.getChildren().addAll(heroContent);
        HBox.setHgrow(identity, Priority.ALWAYS);
        identity.setMaxHeight(Double.MAX_VALUE);

        body.getChildren().addAll(avatarWrap, identity);
        VBox.setVgrow(body, Priority.ALWAYS);
        card.getChildren().addAll(banner, body);
        return card;
    }
    
    /**
     * Wire back button to show quick menu again
     */
    private void wireBackButton(VBox panel, VBox quickMenu, VBox defaultInfo) {
        // Find the back button in the panel
        if (panel.getChildren().size() > 0) {
            javafx.scene.Node topBar = panel.getChildren().get(0);
            if (topBar instanceof HBox) {
                HBox bar = (HBox)topBar;
                if (bar.getChildren().size() > 0) {
                    javafx.scene.Node firstChild = bar.getChildren().get(0);
                    if (firstChild instanceof Button) {
                        Button backBtn = (Button)firstChild;
                        backBtn.setOnAction(e -> {
                            // Hide panel, show quick menu with animation
                            panel.setVisible(false);
                            panel.setManaged(false);
                            panel.setOpacity(0);
                            
                            quickMenu.setVisible(true);
                            quickMenu.setManaged(true);
                            quickMenu.setOpacity(0);
                            
                            FadeTransition fade = new FadeTransition(Duration.millis(200), quickMenu);
                            fade.setFromValue(0);
                            fade.setToValue(1);
                            fade.play();
                        });
                    }
                }
            }
        }
    }

    /**
     * Create action tile with proper styling (no emojis)
     */
    private VBox createActionTile(String label, String symbol) {
        VBox tile = new VBox(8);
        tile.setAlignment(Pos.CENTER);
        tile.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 16px;" +
            "-fx-padding: 12 8 12 8;" +
            "-fx-cursor: hand;"
        );
        tile.setMaxWidth(Double.MAX_VALUE);
        tile.setMaxHeight(Double.MAX_VALUE);
        tile.setMinHeight(100);
        
        Text iconText = text(symbol, 28, false, tm.getAccentHex());
        Text labelText = text(label, 11, true, "rgba(255,255,255,0.6)");
        labelText.setWrappingWidth(80);
        labelText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        tile.getChildren().addAll(iconText, labelText);
        
        // Hover animation
        tile.setOnMouseEntered(e -> {
            tile.setStyle(
                "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.12) + ";" +
                "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.3) + ";" +
                "-fx-border-width: 1px;" +
                "-fx-background-radius: 16px;" +
                "-fx-padding: 12 8 12 8;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), tile);
            scale.setByX(0.05);
            scale.setByY(0.05);
            scale.play();
        });
        
        tile.setOnMouseExited(e -> {
            tile.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04);" +
                "-fx-border-color: rgba(255,255,255,0.08);" +
                "-fx-border-width: 1px;" +
                "-fx-background-radius: 16px;" +
                "-fx-padding: 12 8 12 8;" +
                "-fx-cursor: hand;"
            );
            ScaleTransition scale = new ScaleTransition(Duration.millis(150), tile);
            scale.setByX(-0.05);
            scale.setByY(-0.05);
            scale.play();
        });
        
        return tile;
    }

    /**
     * Create generic quick action panel (UI only, no functionality)
     */
    private VBox createQuickActionPanel(String title, String description, String buttonLabel) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 14; -fx-padding: 0;");
        backBtn.setPrefSize(30, 30);
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #ffffff; -fx-font-size: 14; -fx-padding: 0;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 14; -fx-padding: 0;"));
        
        Text titleText = text(title, 14, true, "#ffffff");
        topBar.getChildren().addAll(backBtn, titleText);
        
        Text descText = text(description, 12, false, "rgba(255,255,255,0.6)");
        descText.setWrappingWidth(300);
        
        Button actionBtn = new Button(buttonLabel);
        actionBtn.setStyle(
            "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
            "-fx-text-fill: #ffffff;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-background-radius: 12;" +
            "-fx-font-weight: 700;" +
            "-fx-font-size: 12;" +
            "-fx-cursor: hand;"
        );
        actionBtn.setOnMouseEntered(e -> actionBtn.setScaleX(1.05));
        actionBtn.setOnMouseExited(e -> actionBtn.setScaleX(1.0));
        
        panel.getChildren().addAll(topBar, descText, actionBtn);
        return panel;
    }

    /**
     * Create 2FA settings panel with toggle and options
     */
    private VBox createTwoFAPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 14; -fx-padding: 0;");
        backBtn.setPrefSize(30, 30);
        
        Text titleText = text("Two-Factor Authentication & Biometrics", 14, true, "#ffffff");
        topBar.getChildren().addAll(backBtn, titleText);
        
        // 2FA Toggle
        HBox toggleBox = new HBox(10);
        toggleBox.setAlignment(Pos.CENTER_LEFT);
        toggleBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-background-radius: 12; -fx-padding: 12;");
        
        Text toggleLabel = text("Two-Factor Authentication", 11, true, "rgba(255,255,255,0.9)");
        Text toggleStatus = text("Disabled", 10, false, "rgba(255,255,255,0.6)");
        VBox toggleInfo = new VBox(2, toggleLabel, toggleStatus);
        
        HBox.setHgrow(toggleInfo, Priority.ALWAYS);
        toggleBox.getChildren().addAll(toggleInfo);
        
        // Email OTP Option
        VBox emailOpt = new VBox(8);
        emailOpt.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-background-radius: 12; -fx-padding: 12;");
        Text emailTitle = text("Email OTP", 11, true, "rgba(255,255,255,0.7)");
        Text emailDesc = text("A code is sent to your email at each login.", 10, false, "rgba(255,255,255,0.5)");
        emailOpt.getChildren().addAll(emailTitle, emailDesc);
        
        // TOTP Option
        VBox totpOpt = new VBox(8);
        totpOpt.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-background-radius: 12; -fx-padding: 12;");
        Text totpTitle = text("Authenticator App", 11, true, "rgba(255,255,255,0.7)");
        Text totpDesc = text("Use an authenticator app like Google Authenticator.", 10, false, "rgba(255,255,255,0.5)");
        totpOpt.getChildren().addAll(totpTitle, totpDesc);

        // FaceID Option
        VBox faceIDOpt = new VBox(8);
        faceIDOpt.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-background-radius: 12; -fx-padding: 12;");
        Text faceIDTitle = text("Face ID", 11, true, "rgba(255,255,255,0.7)");
        Text faceIDDesc = text("Login using facial recognition with a PIN-protected enrollment.", 10, false, "rgba(255,255,255,0.5)");
        faceIDOpt.getChildren().addAll(faceIDTitle, faceIDDesc);

        // WebAuthn/Passkey Option
        VBox webauthOpt = new VBox(8);
        webauthOpt.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-background-radius: 12; -fx-padding: 12;");
        Text webauthTitle = text("WebAuthn / Passkey", 11, true, "rgba(255,255,255,0.7)");
        Text webauthDesc = text("Passwordless login with security keys, Windows Hello, or Touch ID.", 10, false, "rgba(255,255,255,0.5)");
        webauthOpt.getChildren().addAll(webauthTitle, webauthDesc);
        
        panel.getChildren().addAll(topBar, new Text(""), toggleBox, new Text(""), 
            text("2FA Methods", 12, true, "rgba(255,255,255,0.7)"),
            emailOpt, totpOpt,
            new Text(""), text("Biometric & Passwordless", 12, true, "rgba(255,255,255,0.7)"),
            faceIDOpt, webauthOpt);
        return panel;
    }

    /**
     * Create comprehensive settings panel
     */
    private VBox createSettingsPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
        
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 14; -fx-padding: 0;");
        backBtn.setPrefSize(30, 30);
        
        Text titleText = text("Profile Settings", 14, true, "#ffffff");
        topBar.getChildren().addAll(backBtn, titleText);
        
        VBox form = new VBox(8);
        form.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-background-radius: 12; -fx-padding: 12; -fx-background-color: rgba(255,255,255,0.02);");
        
        // Bio Field
        form.getChildren().add(createSettingsLabel("Bio"));
        form.getChildren().add(createSettingsInput("Tell us about yourself...", 60));
        
        // Phone Field
        form.getChildren().add(createSettingsLabel("Phone"));
        form.getChildren().add(createSettingsInput("+216 ..."));
        
        // Theme Dropdown
        form.getChildren().add(createSettingsLabel("Theme"));
        form.getChildren().add(createSettingsDropdown(new String[]{"Dark", "Light"}));
        
        // Language Dropdown
        form.getChildren().add(createSettingsLabel("Language"));
        form.getChildren().add(createSettingsDropdown(new String[]{"English", "French", "Arabic"}));
        
        // Timezone Field
        form.getChildren().add(createSettingsLabel("Timezone"));
        form.getChildren().add(createSettingsInput("Africa/Tunis"));
        
        // Security Section
        form.getChildren().add(createSettingsLabel("Security"));
        form.getChildren().add(createSettingsInput("••••••••", 12));
        form.getChildren().add(createSettingsInput("••••••••", 12));
        form.getChildren().add(createSettingsInput("••••••••", 12));
        
        // Save Button
        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle(
            "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
            "-fx-text-fill: #ffffff;" +
            "-fx-padding: 10 20 10 20;" +
            "-fx-background-radius: 12;" +
            "-fx-font-weight: 700;" +
            "-fx-font-size: 12;" +
            "-fx-cursor: hand;"
        );
        saveBtn.setOnAction(e -> handleSettingsSave());
        
        panel.getChildren().addAll(topBar, form, saveBtn);
        return panel;
    }

    private Text createSettingsLabel(String label) {
        return text(label.toUpperCase(), 10, true, "rgba(255,255,255,0.5)");
    }

    private javafx.scene.control.TextField createSettingsInput(String prompt) {
        return createSettingsInput(prompt, 36);
    }

    private javafx.scene.control.TextField createSettingsInput(String prompt, double height) {
        javafx.scene.control.TextField input = new javafx.scene.control.TextField();
        input.setPromptText(prompt);
        input.setPrefHeight(height);
        input.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-border-color: rgba(255,255,255,0.12);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.3);" +
            "-fx-padding: 8 12 8 12;" +
            "-fx-font-size: 11;"
        );
        return input;
    }

    private javafx.scene.control.ComboBox<String> createSettingsDropdown(String[] options) {
        javafx.scene.control.ComboBox<String> combo = new javafx.scene.control.ComboBox<>();
        combo.getItems().addAll(options);
        combo.setPrefHeight(36);
        return combo;
    }

    /**
     * Handle settings save (stub - add database save logic here)
     */
    private void handleSettingsSave() {
        showAlert("Success", "Settings saved successfully!");
    }

    /**
     * Handle action tile click to show panel
     */

    private VBox createStandingCard() {
        VBox card = cardShell();
        card.setPadding(new Insets(22));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox level = new VBox(0, text("7", 28, true, "#ffffff"), text("LVL", 10, true, tm.getAccentHex()));
        level.setAlignment(Pos.CENTER);
        level.setMinSize(72, 72);
        level.setStyle(shell(16, surfaceSoft(), 0.16));

        VBox title = new VBox(4, text("RESIDENT STANDING", 18, true, "#ffffff"), text("Your status within the Horizon community", 12, false, textMuted()));
        HBox.setHgrow(title, Priority.ALWAYS);

        VBox points = new VBox(2, text("420", 24, true, "#ffffff"), text("SYNDIC PTS", 10, true, "rgba(255,255,255,0.50)"));
        points.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(level, title, points);

        HBox standingBar = new HBox(12,
            standingPoint("All good", true),
            standingPoint("Limited", true),
            standingPoint("At risk", false),
            standingPoint("Suspended", false)
        );
        standingBar.setAlignment(Pos.CENTER_LEFT);

        VBox xp = new VBox(8);
        HBox xpTop = new HBox();
        xpTop.setAlignment(Pos.CENTER_LEFT);
        Text left = text("XP TOWARDS LEVEL 8", 11, true, textMuted());
        Text right = text("20/100", 11, true, "#ffffff");
        HBox.setHgrow(left, Priority.ALWAYS);
        xpTop.getChildren().addAll(left, right);

        StackPane progressTrack = new StackPane();
        progressTrack.setAlignment(Pos.CENTER_LEFT);
        progressTrack.setMinHeight(8);
        progressTrack.setStyle("-fx-background-color: " + surfaceSoft() + "; -fx-background-radius: 999px;");
        StackPane fill = new StackPane();
        fill.setPrefWidth(180);
        fill.setMinHeight(8);
        fill.setStyle("-fx-background-color: " + tm.getEffectiveAccentGradient() + "; -fx-background-radius: 999px;");
        progressTrack.getChildren().add(fill);

        xp.getChildren().addAll(xpTop, progressTrack);

        card.getChildren().addAll(header, standingBar, xp);
        return card;
    }

    private VBox createCircleCard() {
        VBox card = cardShell();
        card.setPadding(new Insets(22));

        HBox top = new HBox(10, text("CIRCLE", 18, true, "#ffffff"), badge("8 FRIENDS"), badge("2 PENDING"));
        top.setAlignment(Pos.CENTER_LEFT);

        HBox switcher = new HBox(8, tabChip("Friends", true), tabChip("Pending", false));

        GridPane friends = new GridPane();
        friends.setHgap(12);
        friends.setVgap(12);
        for (int i = 0; i < 8; i++) {
            friends.add(friendCard("Friend " + (i + 1)), i % 4, i / 4);
        }

        VBox search = new VBox(8);
        search.setPadding(new Insets(14));
        search.setStyle(shell(18, surfaceSoft(), 0.16));
        search.getChildren().addAll(text("Explore Community", 13, true, "#ffffff"), text("Search residents and send connection requests.", 12, false, textMuted()));

        card.getChildren().addAll(top, switcher, friends, search);
        return card;
    }

    private VBox createActivityPage() {
        VBox page = new VBox(20);
        page.setAlignment(Pos.TOP_CENTER);

        HBox tabNav = new HBox(8);
        tabNav.setAlignment(Pos.CENTER);
        tabNav.setPadding(new Insets(8));
        tabNav.setMaxWidth(1120);
        tabNav.setStyle(shell(999, "rgba(255,255,255,0.04)", 0.1));

        addDetailTab(tabNav, "Account", "account", createAccountTab());
        addDetailTab(tabNav, "Forum", "forum", createForumTab());
        addDetailTab(tabNav, "Events", "events", createEventsTab());
        addDetailTab(tabNav, "Reclamation", "reclamation", createReclamationTab());
        addDetailTab(tabNav, "Residence", "residence", createResidenceTab());

        VBox wrapper = new VBox(0);
        wrapper.setMaxWidth(1800);
        wrapper.prefWidthProperty().bind(root.widthProperty().multiply(0.95));
        wrapper.getChildren().addAll(tabNav);
        wrapper.getChildren().addAll(detailTabs.values());

        setDetailTab("account");
        page.getChildren().add(wrapper);
        return page;
    }

    private void addDetailTab(HBox nav, String label, String key, VBox page) {
        Button btn = new Button(label);
        btn.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 12));
        btn.setOnAction(e -> setDetailTab(key));
        styleDetailTabButton(btn, false);

        detailTabButtons.put(key, btn);
        detailTabs.put(key, page);
        nav.getChildren().add(btn);
    }

    private void setDetailTab(String key) {
        detailTabs.forEach((name, pane) -> {
            boolean active = name.equals(key);
            pane.setVisible(active);
            pane.setManaged(active);
        });
        detailTabButtons.forEach((name, btn) -> styleDetailTabButton(btn, name.equals(key)));
    }

    private void styleDetailTabButton(Button btn, boolean active) {
        btn.setStyle((active
            ? "-fx-background-color: " + tm.getEffectiveAccentGradient() + "; -fx-text-fill: white;"
            : "-fx-background-color: transparent; -fx-text-fill: " + textMuted() + ";")
            + "-fx-font-weight: 800; -fx-background-radius: 999px; -fx-padding: 11 18 11 18;");
    }

    private VBox createAccountTab() {
        VBox tab = new VBox(16);
        tab.setPadding(new Insets(16, 0, 0, 0));

        VBox account = cardShell();
        account.setPadding(new Insets(22));
        account.getChildren().addAll(
            text("Account", 24, true, "#ffffff"),
            infoLine("First name", displayFirstName()),
            infoLine("Last name", displayLastName()),
            infoLine("Email", displayEmail()),
            infoLine("Role", displayRole()),
            infoLine("Verified", displayVerified()),
            infoLine("Account created", displayCreatedAt()),
            infoLine("Bio", displayBio()),
            infoLine("Timezone", displayTimezone()),
            infoLine("Phone", displayPhone())
        );

        VBox onboarding = cardShell();
        onboarding.setPadding(new Insets(22));
        GridPane prefGrid = new GridPane();
        prefGrid.setHgap(10);
        prefGrid.setVgap(10);
        ColumnConstraints pc = new ColumnConstraints();
        pc.setPercentWidth(33.33);
        prefGrid.getColumnConstraints().addAll(pc, pc, pc);
        prefGrid.add(pillCard("Language", "English"), 0, 0);
        prefGrid.add(pillCard("Theme", "Dark"), 1, 0);
        prefGrid.add(pillCard("Status", "Completed"), 2, 0);
        prefGrid.add(pillCard("Notification", "Instant"), 0, 1);
        prefGrid.add(pillCard("Property Type", "Apartment"), 1, 1);
        prefGrid.add(pillCard("Contact", "Phone"), 2, 1);

        onboarding.getChildren().addAll(
            text("Your onboarding choices", 22, true, "#ffffff"),
            text("Update language, theme and profile preferences.", 13, false, textSoft()),
            prefGrid,
            text("Suggestions: Keep notifications instant for urgent building updates.", 13, false, textMuted())
        );

        tab.getChildren().addAll(account, onboarding);
        return tab;
    }

    private VBox createForumTab() {
        VBox tab = new VBox(16);
        tab.setPadding(new Insets(16, 0, 0, 0));

        VBox card = cardShell();
        card.setPadding(new Insets(22));
        card.getChildren().add(text("Your Forum Activity", 24, true, "#ffffff"));

        HBox categoryFilters = new HBox(8,
            smallPill("Announcements", false),
            smallPill("General", true)
        );

        HBox subTabs = new HBox(8,
            forumTabPill("Publications (2)", true),
            forumTabPill("Comments (1)", false),
            forumTabPill("Reactions (1)", false),
            forumTabPill("Bookmarks (1)", false)
        );
        subTabs.setPadding(new Insets(6));
        subTabs.setStyle(shell(999, surfaceSoft(), 0.16));

        VBox publicationList = new VBox(10,
            forumItem("Publication", "How to improve building communication?", "General", "15 Mar 2026"),
            forumItem("Publication", "Monthly budget summary and maintenance", "Announcement", "09 Mar 2026")
        );

        HBox pagination = new HBox(8,
            pagePill("1", true),
            pagePill("2", false),
            pagePill("3", false)
        );
        pagination.setAlignment(Pos.CENTER);
        pagination.setPadding(new Insets(8));
        pagination.setMaxWidth(220);
        pagination.setStyle(shell(999, surfaceSoft(), 0.16));

        card.getChildren().addAll(categoryFilters, subTabs, publicationList, pagination);

        tab.getChildren().add(card);
        return tab;
    }

    private VBox createEventsTab() {
        VBox tab = new VBox(16);
        tab.setPadding(new Insets(16, 0, 0, 0));

        VBox card = cardShell();
        card.setPadding(new Insets(22));
        card.getChildren().add(text("Your Events", 24, true, "#ffffff"));
        card.getChildren().add(eventItem("Community rooftop meetup", "16 Mar 2026, 19:00", "Rooftop", "Planned", "Social"));
        card.getChildren().add(eventItem("Monthly co-owners assembly", "21 Mar 2026, 10:00", "Main Hall", "In progress", "Meeting"));
        card.getChildren().add(eventItem("Emergency drill", "03 Mar 2026, 08:00", "Block C", "Completed", "Safety"));

        HBox pagination = new HBox(8, pagePill("1", true), pagePill("2", false));
        pagination.setAlignment(Pos.CENTER);
        pagination.setPadding(new Insets(8));
        pagination.setMaxWidth(170);
        pagination.setStyle(shell(999, surfaceSoft(), 0.16));
        card.getChildren().add(pagination);

        tab.getChildren().add(card);
        return tab;
    }

    private VBox createReclamationTab() {
        VBox tab = new VBox(16);
        tab.setPadding(new Insets(16, 0, 0, 0));

        VBox card = cardShell();
        card.setPadding(new Insets(22));
        card.getChildren().add(text("Your Reclamations", 24, true, "#ffffff"));
        card.getChildren().add(reclamationItem("Water leakage in block B corridor", "In progress", "Submitted on 10 Mar 2026"));
        card.getChildren().add(reclamationItem("Parking gate sensor malfunction", "Confirmed", "Submitted on 05 Mar 2026"));
        card.getChildren().add(reclamationItem("Elevator cabin lighting issue", "Active", "Submitted on 01 Mar 2026"));

        HBox pagination = new HBox(8, pagePill("1", true), pagePill("2", false));
        pagination.setAlignment(Pos.CENTER);
        pagination.setPadding(new Insets(8));
        pagination.setMaxWidth(170);
        pagination.setStyle(shell(999, surfaceSoft(), 0.16));
        card.getChildren().add(pagination);

        tab.getChildren().add(card);
        return tab;
    }

    private VBox createResidenceTab() {
        VBox tab = new VBox(16);
        tab.setPadding(new Insets(16, 0, 0, 0));

        VBox card = cardShell();
        card.setPadding(new Insets(22));
        card.getChildren().add(text("Your Residences", 24, true, "#ffffff"));
        card.getChildren().add(residenceItem("Horizon Gardens", "Bloc A - Apt 302", "Owner", "Available", "Parking: Yes"));
        card.getChildren().add(residenceItem("Horizon Park", "Bloc C - Apt 104", "Tenant", "Occupied", "Parking: No"));

        tab.getChildren().add(card);
        return tab;
    }

    private HBox infoLine(String k, String v) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Text key = text(k, 13, true, textMuted());
        Text val = text(v, 13, false, "#ffffff");
        HBox.setHgrow(key, Priority.NEVER);
        HBox.setHgrow(val, Priority.ALWAYS);
        row.getChildren().addAll(key, spacer(26), val);
        return row;
    }

    private VBox listRow(String type, String title, String status) {
        VBox row = new VBox(4);
        row.setPadding(new Insets(14));
        row.setStyle(shell(14, tm.toRgba(tm.getAccentHex(), 0.06), 0.13));
        row.getChildren().addAll(
            text(type + " - " + status, 11, true, tm.getAccentHex()),
            text(title, 14, true, "#ffffff")
        );
        return row;
    }

    private VBox forumItem(String type, String title, String category, String date) {
        VBox row = new VBox(8);
        row.setPadding(new Insets(14));
        row.setStyle(shell(14, tm.toRgba(tm.getAccentHex(), 0.05), 0.10));

        HBox top = new HBox();
        Text left = text(type + " - " + category, 11, true, tm.getAccentHex());
        Text right = text(date, 11, false, textMuted());
        HBox.setHgrow(left, Priority.ALWAYS);
        top.getChildren().addAll(left, right);

        row.getChildren().addAll(top, text(title, 14, true, "#ffffff"));
        return row;
    }

    private VBox eventItem(String title, String date, String location, String status, String type) {
        VBox row = new VBox(10);
        row.setPadding(new Insets(16));
        row.setStyle(shell(16, "rgba(255,255,255,0.03)", 0.08));

        HBox top = new HBox(10);
        VBox iconWrap = new VBox(text("EV", 11, true, tm.getAccentHex()));
        iconWrap.setAlignment(Pos.CENTER);
        iconWrap.setMinSize(42, 42);
        iconWrap.setStyle(shell(10, tm.toRgba(tm.getAccentHex(), 0.10), 0.18));

        VBox titleMeta = new VBox(5,
            text(title, 15, true, "#ffffff"),
            text(date + " | " + location, 12, false, textMuted())
        );
        HBox.setHgrow(titleMeta, Priority.ALWAYS);

        Text statusTag = text(status, 11, true, "#ffffff");
        VBox statusWrap = new VBox(statusTag);
        statusWrap.setPadding(new Insets(6, 10, 6, 10));
        statusWrap.setStyle(shell(999, tm.toRgba(tm.getAccentHex(), 0.18), 0.25));

        top.getChildren().addAll(iconWrap, titleMeta, statusWrap);
        row.getChildren().addAll(top, text("Type: " + type + " | Enrolled: 12/40", 12, false, textMuted()));
        return row;
    }

    private VBox reclamationItem(String title, String status, String submitted) {
        VBox row = new VBox(8);
        row.setPadding(new Insets(16));
        row.setStyle(shell(16, "rgba(255,255,255,0.03)", 0.08));

        HBox top = new HBox();
        Text titleText = text(title, 14, true, "#ffffff");
        Text statusText = text(status, 11, true, tm.getAccentHex());
        HBox.setHgrow(titleText, Priority.ALWAYS);
        top.getChildren().addAll(titleText, statusText);

        row.getChildren().addAll(top, text(submitted, 12, false, textMuted()));
        return row;
    }

    private VBox residenceItem(String residence, String unit, String relation, String availability, String parking) {
        VBox row = new VBox(8);
        row.setPadding(new Insets(16));
        row.setStyle(shell(16, "rgba(255,255,255,0.03)", 0.08));

        HBox top = new HBox();
        VBox info = new VBox(4,
            text(residence, 15, true, "#ffffff"),
            text(unit, 12, false, textMuted())
        );
        HBox.setHgrow(info, Priority.ALWAYS);
        Text relationTag = text(relation, 11, true, tm.getAccentHex());
        top.getChildren().addAll(info, relationTag);

        row.getChildren().addAll(top, text(availability + " | " + parking, 12, false, textMuted()));
        return row;
    }

    private VBox pillCard(String label, String value) {
        VBox pill = new VBox(4,
            text(label, 11, true, textMuted()),
            text(value, 13, true, "#ffffff")
        );
        pill.setPadding(new Insets(10, 12, 10, 12));
        pill.setStyle(shell(12, "rgba(255,255,255,0.04)", 0.09));
        return pill;
    }

    private Button smallPill(String text, boolean active) {
        Button b = new Button(text);
        b.setStyle((active
            ? "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: #ffffff;"
            : "-fx-background-color: transparent; -fx-text-fill: " + textMuted() + ";")
            + "-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-radius: 999px; -fx-padding: 7 12 7 12;"
        );
        return b;
    }

    private Button forumTabPill(String text, boolean active) {
        Button b = new Button(text);
        b.setStyle((active
            ? "-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: #ffffff;"
            : "-fx-background-color: transparent; -fx-text-fill: " + textMuted() + ";")
            + "-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-radius: 999px; -fx-padding: 9 12 9 12;"
        );
        return b;
    }

    private Button pagePill(String text, boolean active) {
        Button b = new Button(text);
        b.setMinSize(34, 34);
        b.setStyle((active
            ? "-fx-background-color: " + tm.getEffectiveAccentGradient() + "; -fx-text-fill: #ffffff;"
            : "-fx-background-color: transparent; -fx-text-fill: " + textMuted() + ";")
            + "-fx-font-size: 11px; -fx-font-weight: 800; -fx-background-radius: 999px;"
        );
        return b;
    }

    private VBox standingPoint(String label, boolean active) {
        VBox point = new VBox(6);
        point.setAlignment(Pos.CENTER);
        Circle dot = new Circle(10);
        dot.setFill(active ? Color.web(tm.getAccentHex()) : Color.web("#2f3136"));
        Text txt = text(label, 11, true, active ? "#ffffff" : textMuted());
        point.getChildren().addAll(dot, txt);
        return point;
    }

    private VBox friendCard(String name) {
        VBox c = new VBox(8);
        c.setAlignment(Pos.CENTER);
        StackPane avatar = new StackPane();
        avatar.setMinSize(60, 60);
        avatar.setStyle(shell(16, "rgba(255,255,255,0.05)", 0.10));
        avatar.getChildren().add(text("U", 18, true, textSoft()));
        c.getChildren().addAll(avatar, text(name, 11, true, textMuted()));
        return c;
    }

    private VBox statPill(String label, String value) {
        VBox pill = new VBox(4, text(label, 11, true, textMuted()), text(value, 13, true, "#ffffff"));
        pill.setPadding(new Insets(10, 14, 10, 14));
        pill.setStyle(shell(12, "rgba(255,255,255,0.04)", 0.09));
        return pill;
    }

    private Button tabChip(String label, boolean active) {
        Button b = new Button(label);
        b.setStyle((active
            ? "-fx-background-color: " + tm.getEffectiveAccentGradient() + "; -fx-text-fill: white;"
            : "-fx-background-color: " + surfaceSoft() + "; -fx-text-fill: " + textMuted() + ";")
            + "-fx-font-weight: 800; -fx-background-radius: 12px; -fx-padding: 8 12 8 12;");
        return b;
    }

    private Text badge(String label) {
        return text(label, 10, true, "#ffffff");
    }

    private VBox cardShell() {
        VBox c = new VBox(16);
        c.setStyle(shell(28, surfaceCard(), 0.16));
        return c;
    }

    private void hydrateSessionData() {
        currentUser = sessionManager.getCurrentUser();
        currentProfile = sessionManager.getCurrentProfile();

        if (currentUser != null && currentUser.getIdUser() != null) {
            currentUser = userService.findById(currentUser.getIdUser()).orElse(currentUser);
            sessionManager.setCurrentUser(currentUser);

            if (currentProfile == null) {
                currentProfile = profileService.findOneByUserId(currentUser.getIdUser()).orElse(null);
                if (currentProfile != null) {
                    sessionManager.setCurrentProfile(currentProfile);
                }
            }
        }
    }

    private String displayFirstName() {
        return valueOr(currentUser != null ? currentUser.getFirstName() : null, "Unknown");
    }

    private String displayLastName() {
        return valueOr(currentUser != null ? currentUser.getLastName() : null, "User");
    }

    private String displayName() {
        String full = (displayFirstName() + " " + displayLastName()).trim();
        return full.isBlank() ? "Syndicati Member" : full;
    }

    private String displayEmail() {
        return valueOr(currentUser != null ? currentUser.getEmailUser() : null, "contact@syndicati.tn");
    }

    private String displayRole() {
        String role = currentUser != null ? currentUser.getRoleUser() : null;
        if (role == null || role.isBlank()) {
            return "Resident";
        }
        if ("ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            return "Admin";
        }
        return role;
    }

    private String displayVerified() {
        return currentUser != null && currentUser.isVerified() ? "Yes" : "No";
    }

    private String displayCreatedAt() {
        if (currentUser == null || currentUser.getCreatedAt() == null) {
            return "-";
        }
        return PROFILE_DATE_FMT.format(currentUser.getCreatedAt());
    }

    private String displayBio() {
        return valueOr(currentProfile != null ? currentProfile.getDescriptionProfile() : null, "No bio added yet.");
    }

    private String displayTimezone() {
        Integer tz = currentProfile != null ? currentProfile.getTimezone() : null;
        return tz == null ? "-" : String.valueOf(tz);
    }

    private String displayPhone() {
        return valueOr(currentUser != null ? currentUser.getPhone() : null, "-");
    }

    private String displayInitial() {
        String name = displayName();
        return name.isBlank() ? "U" : name.substring(0, 1).toUpperCase();
    }

    private String valueOr(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private String shell(double radius, String bg, double borderOpacity) {
        return "-fx-background-color: " + bg + ";"
            + "-fx-border-color: rgba(255,255,255," + borderOpacity + ");"
            + "-fx-border-width: 1px;"
            + "-fx-background-radius: " + radius + "px;"
            + "-fx-border-radius: " + radius + "px;";
    }

    private String surfaceCard() {
        return tm.isDarkMode()
            ? "linear-gradient(from 0% 0% to 100% 100%, rgba(10,10,10,0.94) 0%, rgba(14,14,14,0.94) 62%, " + tm.toRgba(tm.getAccentHex(), 0.10) + " 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, rgba(255,255,255,0.98) 0%, rgba(248,250,252,0.96) 100%)";
    }

    private String surfaceSoft() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.09)" : "rgba(15,23,42,0.08)";
    }

    private String borderSoft() {
        return tm.isDarkMode() ? tm.toRgba(tm.getAccentHex(), 0.34) : "rgba(15,23,42,0.16)";
    }

    private String textSoft() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.93)" : "rgba(15,23,42,0.90)";
    }

    private String textMuted() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.79)" : "rgba(30,41,59,0.82)";
    }

    private Pane spacer(double width) {
        Pane p = new Pane();
        p.setMinWidth(width);
        return p;
    }

    private Text text(String value, int size, boolean bold, String color) {
        Text t = new Text(value);
        t.setFont(Font.font(
            bold ? MainApplication.getInstance().getBoldFontFamily() : MainApplication.getInstance().getLightFontFamily(),
            bold ? FontWeight.BOLD : FontWeight.NORMAL,
            size
        ));
        t.setFill(Color.web(color));
        return t;
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public void cleanup() {
        // No resources to release in this static UI replica.
    }

    /**
     * Handle avatar click to upload a new profile image
     */
    private void handleAvatarClick(Circle avatar, Text avatarText) {
        try {
            Profile profile = sessionManager.getCurrentProfile();
            if (profile == null) {
                showAlert("Error", "No profile loaded");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Profile Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null && selectedFile.exists()) {
                byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                String newImagePath = ProfileImageService.saveAvatarImage(fileData, selectedFile.getName(), profile.getIdProfile());
                
                if (newImagePath != null) {
                    // Update profile in database
                    profile.setAvatar(newImagePath);
                    profileService.updateProfile(profile);
                    
                    // Update avatar display
                    Image img = ImageLoaderUtil.loadProfileAvatar(newImagePath, false);
                    if (img != null && !img.isError()) {
                        avatar.setFill(new ImagePattern(img));
                        avatarText.setVisible(false);
                        avatarText.setMouseTransparent(true);
                        showAlert("Success", "Profile image updated successfully!");
                    } else {
                        showAlert("Error", "Failed to load uploaded image");
                    }
                } else {
                    showAlert("Error", "Failed to save image");
                }
            }
        } catch (Exception ex) {
            System.err.println("ProfileView: Error uploading avatar: " + ex.getMessage());
            ex.printStackTrace();
            showAlert("Error", "Error uploading image: " + ex.getMessage());
        }
    }

    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

