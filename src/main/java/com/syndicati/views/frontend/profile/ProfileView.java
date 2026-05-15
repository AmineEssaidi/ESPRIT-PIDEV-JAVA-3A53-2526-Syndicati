package com.syndicati.views.frontend.profile;

import com.syndicati.MainApplication;
import com.syndicati.controllers.user.profile.ProfileController;
import com.syndicati.controllers.user.relationship.UserRelationshipController;
import com.syndicati.controllers.user.standing.UserStandingController;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.controllers.user.profile.ProfileAvatarController;
import com.syndicati.controllers.user.user.UserController;
import com.syndicati.controllers.biometric.CameraController;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import com.syndicati.models.user.UserRelationship;
import com.syndicati.models.user.UserStanding;
import com.syndicati.models.user.data.ProfileRepository;
import com.syndicati.models.user.data.UserRelationshipRepository;
import com.syndicati.models.user.data.UserRepository;
import com.syndicati.utils.navigation.NavigationManager;
import com.syndicati.services.user.profile.ProfileService;
import com.syndicati.services.security.TwoFactorService;
import com.syndicati.services.security.BiometricsService;
import com.syndicati.services.security.FaceIDService;
import com.syndicati.services.biometric.RealCameraService;
import com.syndicati.services.InsightFaceService;
import com.syndicati.utils.image.ImageLoaderUtil;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.ui.HorizonDesignSystem;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import java.util.Arrays;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
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
import javafx.scene.control.TextArea;
import org.mindrot.jbcrypt.BCrypt;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import java.util.concurrent.atomic.AtomicReference;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Profile page replica based on templates/frontend/profile/profile.html.twig.
 * This is a UI-only duplication without backend behavior.
 */
public class ProfileView implements ViewInterface {

    private enum QuickActionsMode {
        DEFAULT,
        TILES,
        DETAIL
    }

    private final VBox root;
    private final ThemeManager tm;

    private final Map<String, VBox> mainPages = new LinkedHashMap<>();
    private final Map<String, Button> mainNavButtons = new LinkedHashMap<>();

    private final Map<String, VBox> detailTabs = new LinkedHashMap<>();
    private final Map<String, Button> detailTabButtons = new LinkedHashMap<>();
    private final List<javafx.animation.Animation> ownedAnimations = new java.util.ArrayList<>();
    private long lastXpInteractionAt = 0L;
    private int lastRenderedStandingPoints = -1;
    private int lastRenderedStandingLevel = -1;
    private volatile boolean avatarUpdateInProgress = false;
    private VBox contentHost;

    private StackPane quickActionsContainer;
    private VBox quickActionsDefaultView;
    private VBox quickActionsSwitcherView;
    private VBox quickActionsDetailView;
    private Button quickActionsButton;
    private final InsightFaceService insightFaceService;
    /** Prevents duplicate concurrent loadDataAsync() runs. */
    private final AtomicBoolean isLoading = new AtomicBoolean(false);
    private volatile boolean disposed = false;

    public ProfileView() {
        this.tm = ThemeManager.getInstance();
        this.root = new VBox();
        this.root.setStyle("-fx-background-color: transparent;");
        this.root.setAlignment(Pos.TOP_CENTER);
        this.root.setFillWidth(true);
        this.insightFaceService = InsightFaceService.getInstance();

        // 1. Show skeleton instantly (Zero freeze)
        this.root.getChildren().setAll(buildSkeleton());

        // 2. Build heavy content progressively in the background
        javafx.application.Platform.runLater(() -> {
            Thread.startVirtualThread(this::loadDataAsync);
        });
    }

    private void buildContentAsync() {
        // Build the structure in slices to keep the UI thread breathing
        javafx.application.Platform.runLater(() -> {
            if (disposed) return;
            VBox container = new VBox();
            container.setAlignment(Pos.TOP_CENTER);
            container.setFillWidth(true);
            container.setStyle("-fx-background-color: transparent;");
            
            mainPages.clear();
            mainNavButtons.clear();
            
            VBox content = new VBox(26);
            content.setAlignment(Pos.TOP_CENTER);
            content.setPadding(new Insets(24, 0, 42, 0));
            content.setFillWidth(true);
            contentHost = content;

            // Slice 1: Navigation
            content.getChildren().add(createMainNavigation());
            
            // Slice 2: Heavy Pages
            javafx.application.Platform.runLater(() -> {
                if (disposed) return;
                VBox page1 = createOverviewPage();
                mainPages.put("overview", page1);
                content.getChildren().add(page1);
                setMainPage("overview");

                ScrollPane scroll = new ScrollPane(content);
                scroll.setFitToWidth(true);
                scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
                container.getChildren().add(scroll);

                // Final Swap
                stopOwnedAnimations();
                this.root.getChildren().setAll(container);
            });
        });
    }

    /** Lightweight skeleton shown while data loads. Instant to build. */
    private VBox buildSkeleton() {
        VBox skeleton = new VBox(20);
        skeleton.setAlignment(Pos.TOP_CENTER);
        skeleton.setPadding(new Insets(40, 24, 40, 24));
        skeleton.setStyle("-fx-background-color: transparent;");

        // Shimmer header bar
        for (int i = 0; i < 4; i++) {
            javafx.scene.layout.Region bar = new javafx.scene.layout.Region();
            bar.setPrefHeight(i == 0 ? 180 : 48);
            bar.setMaxWidth(i == 0 ? 1600 : (i == 1 ? 420 : 800));
            bar.setStyle(
                "-fx-background-color: " + (tm.isDarkMode() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.06)") + ";" +
                "-fx-background-radius: " + (i == 0 ? 34 : 12) + "px;"
            );
            // Shimmer animation
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(900), bar);
            ft.setFromValue(0.4);
            ft.setToValue(1.0);
            ft.setAutoReverse(true);
            ft.setCycleCount(javafx.animation.Animation.INDEFINITE);
            ft.setDelay(javafx.util.Duration.millis(i * 120));
            ft.play();
            ownedAnimations.add(ft);
            skeleton.getChildren().add(bar);
        }

        // Loading label
        Label lbl = new Label("Loading profile...");
        lbl.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13));
        lbl.setTextFill(Color.web(tm.isDarkMode() ? "rgba(255,255,255,0.35)" : "rgba(15,23,42,0.35)"));
        skeleton.getChildren().add(lbl);
        return skeleton;
    }

    @Override
    public void loadDataAsync() {
        // Prevent overlapping concurrent loads
        if (!isLoading.compareAndSet(false, true)) return;

        Thread.startVirtualThread(() -> {
            try {
                User currentUser = SessionManager.getInstance().getCurrentUser();
                if (currentUser == null) {
                    isLoading.set(false);
                    return;
                }

                com.syndicati.utils.session.SessionManager sm = com.syndicati.utils.session.SessionManager.getInstance();

                // --- PHASE 1: Fetch all data on background thread (Cache Aware) ---
                if (!sm.isProfileFresh()) {
                    Profile profile = new ProfileController()
                        .findOneByUserId(currentUser.getIdUser()).orElse(null);
                    if (profile != null) sm.setCurrentProfile(profile);
                }

                // Standing changes from the web/live polling side; always force a DB read for profile.
                UserStanding standing = new UserStandingController()
                    .findOrCreateByUserId(currentUser.getIdUser(), false);
                sm.setCurrentStanding(standing);

                if (!sm.isCircleCacheFresh()) {
                    UserRelationshipController rc = new UserRelationshipController();
                    
                    // Fetch sequentially to respect the pool size of 2 and avoid 10s timeouts
                    int friendCount = rc.countFriends(currentUser);
                    int pendingCount = rc.countPendingRequests(currentUser);
                    List<User> friendList = rc.findFriends(currentUser, 24);
                    List<UserRelationship> pendingList = rc.findPendingRequestsFor(currentUser);

                    sm.setCircleData(friendList, pendingList, friendCount, pendingCount);
                }

                Platform.runLater(this::buildContentAsync);
            } catch (Exception ex) {
                System.err.println("[ProfileView] loadDataAsync failed: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                isLoading.set(false);
            }
        });
    }

    private VBox buildContent() {
        VBox container = new VBox();
        container.setAlignment(Pos.TOP_CENTER);
        container.setFillWidth(true);
        container.setStyle("-fx-background-color: transparent;");
        
        mainPages.clear();
        mainNavButtons.clear();
        
        VBox content = new VBox(26);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(24, 0, 42, 0));
        content.setFillWidth(true);
        contentHost = content;

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

        container.getChildren().add(scroll);
        return container;
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
        HorizonDesignSystem.installButtonMotion(btn);
        return btn;
    }

    private void setMainPage(String key) {
        if ("activity".equals(key) && !mainPages.containsKey("activity") && contentHost != null) {
            VBox page = createActivityPageShell();
            mainPages.put("activity", page);
            contentHost.getChildren().add(page);
        }
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
        row.setMaxWidth(1600);
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
        User currentUser = SessionManager.getInstance().getCurrentUser();
        Profile currentProfile = SessionManager.getInstance().getCurrentProfile();
        
        VBox card = new VBox(0);
        card.setMaxWidth(1600);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(HorizonDesignSystem.webSectionCard(34, false));
        HorizonDesignSystem.popIn(card);

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

        StackPane avatarWrap = new StackPane();
        avatarWrap.setCursor(javafx.scene.Cursor.HAND);
        Circle avatar = new Circle(56);
        avatar.setFill(Color.web(tm.getAccentHex()));
        avatar.setStroke(Color.web(tm.toRgba(tm.getAccentHex(), 0.35)));
        avatar.setStrokeWidth(2);

        String userInitial = currentInitial(currentUser);
        Text avatarText = text(userInitial, 38, true, "#ffffff");
        boolean hasAvatar = applyAvatarFill(avatar);
        avatarText.setVisible(!hasAvatar);
        avatarText.setManaged(!hasAvatar);
        avatarWrap.getChildren().addAll(avatar, avatarText);
        avatarWrap.setOnMouseClicked(e -> handleProfileAvatarUpload(currentUser, avatar, avatarText));

        VBox identity = new VBox(10);
        identity.setAlignment(Pos.TOP_LEFT);
        String displayName = currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "Syndicati Member";
        String displayRole = currentUser != null ? currentUser.getRoleUser() : "Resident";
        String displayEmail = currentUser != null ? currentUser.getEmailUser() : "contact@syndicati.tn";
        String displayBio = currentProfile != null && currentProfile.getDescriptionProfile() != null
            ? currentProfile.getDescriptionProfile().trim()
            : "";
        String createdDate = currentUser != null && currentUser.getCreatedAt() != null 
            ? currentUser.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            : "12/03/2026";
        String verifiedStatus = currentUser != null && currentUser.isVerified() ? "Verified" : "Pending";
        
        Text name = text(displayName, 34, true, "#ffffff");
        Text role = text(displayRole, 16, false, textSoft());
        Text email = text(displayEmail, 14, false, textMuted());
        Text bio = null;
        if (!displayBio.isBlank()) {
            bio = text(displayBio, 13, false, "rgba(255,255,255,0.72)");
            bio.setWrappingWidth(520);
            bio.setLineSpacing(3);
        }

        HBox stats = new HBox(12,
            statPill("Account created", createdDate),
            statPill("Status", verifiedStatus)
        );

        VBox defaultInfo = bio == null
            ? new VBox(10, name, role, email, stats)
            : new VBox(10, name, role, email, bio, stats);
        defaultInfo.setOpacity(1);
        defaultInfo.setTranslateX(0);

        HBox quickBtnWrap = new HBox();
        quickBtnWrap.setAlignment(Pos.CENTER_RIGHT);
        quickBtnWrap.setPadding(new Insets(4, 0, 0, 0));

        Button showActionsBtn = new Button("ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬Å“Ãƒâ€šÃ‚Â¦  QUICK ACTIONS  ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Âº");
        showActionsBtn.setText("\u22EF  QUICK ACTIONS  \u203A");
        styleQuickActionsButton(showActionsBtn, false);
        HorizonDesignSystem.installButtonMotion(showActionsBtn);
        quickBtnWrap.getChildren().add(showActionsBtn);
        defaultInfo.getChildren().add(quickBtnWrap);

        StackPane actionsSwitcher = new StackPane();
        actionsSwitcher.setStyle("-fx-background-color: transparent;");
        actionsSwitcher.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        actionsSwitcher.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        actionsSwitcher.setMaxHeight(Double.MAX_VALUE);

        VBox switcherView = new VBox(12);
        switcherView.setId("actions-switcher-view");
        switcherView.setStyle("-fx-background-color: transparent;");
        switcherView.setVisible(false);
        switcherView.setManaged(false);
        switcherView.setMaxWidth(Double.MAX_VALUE);
        switcherView.setOpacity(1);
        switcherView.setTranslateX(0);
        switcherView.setMouseTransparent(true);

        HBox switcherTopBar = new HBox(10);
        switcherTopBar.setAlignment(Pos.CENTER_LEFT);

        Button switcherBackBtn = new Button("ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â Ãƒâ€šÃ‚Â");
        switcherBackBtn.setText("\u2190");
        switcherBackBtn.setStyle(
            "-fx-min-width: 30px; -fx-min-height: 30px;" +
            "-fx-max-width: 30px; -fx-max-height: 30px;" +
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-border-color: rgba(255,255,255,0.15);" +
            "-fx-border-width: 1px; -fx-border-radius: 999px; -fx-background-radius: 999px;" +
            "-fx-text-fill: rgba(255,255,255,0.75); -fx-font-weight: 700; -fx-cursor: hand;"
        );
        HorizonDesignSystem.installButtonMotion(switcherBackBtn);
        Text switcherTitle = text("QUICK ACTIONS", 12, true, "rgba(255,255,255,0.55)");
        switcherTopBar.getChildren().addAll(switcherBackBtn, switcherTitle);

        GridPane actions = new GridPane();
        actions.setId("quick-actions-grid");
        actions.setHgap(10);
        actions.setVgap(10);
        ColumnConstraints c = new ColumnConstraints();
        c.setPercentWidth(33.33);
        c.setFillWidth(true);
        actions.getColumnConstraints().addAll(c, c, c);
        actions.add(createActionTile("ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸Ãƒâ€¦Ã‚Â½Ãƒâ€šÃ‚Â¥ Host Spotlight", "host", switcherView, showActionsBtn), 0, 0);
        actions.add(createActionTile("ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ Join by Code", "join", switcherView, showActionsBtn), 1, 0);
        actions.add(createActionTile("ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒâ€šÃ‚Â 2FA", "2fa", switcherView, showActionsBtn), 2, 0);
        actions.add(createActionTile("ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â  Biometrics", "biometrics", switcherView, showActionsBtn), 0, 1);
        actions.add(createActionTile("ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“Ãƒâ€šÃ‚Â¤ Face ID", "faceid", switcherView, showActionsBtn), 1, 1);
        actions.add(createActionTile("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã‚Â¡ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã‚Â¯Ãƒâ€šÃ‚Â¸Ãƒâ€šÃ‚Â Settings", "settings", switcherView, showActionsBtn), 2, 1);

        switcherView.getChildren().addAll(switcherTopBar, actions);

        VBox detailView = new VBox(12);
        detailView.setId("actions-detail-view");
        detailView.setStyle("-fx-background-color: transparent;");
        detailView.setVisible(false);
        detailView.setManaged(false);
        detailView.setMaxWidth(Double.MAX_VALUE);
        detailView.setOpacity(1);
        detailView.setTranslateX(0);
        detailView.setMouseTransparent(true);

        showActionsBtn.setOnAction(e -> showActionsTiles(defaultInfo, switcherView, detailView, showActionsBtn));
        switcherBackBtn.setOnAction(e -> showDefaultHeroInfo(defaultInfo, switcherView, detailView, showActionsBtn));

        quickActionsContainer = actionsSwitcher;
        quickActionsDefaultView = defaultInfo;
        quickActionsSwitcherView = switcherView;
        quickActionsDetailView = detailView;
        quickActionsButton = showActionsBtn;

        actionsSwitcher.getChildren().setAll(defaultInfo);
        defaultInfo.setVisible(true);
        defaultInfo.setManaged(true);
        defaultInfo.setMouseTransparent(false);
        switcherView.setVisible(false);
        switcherView.setManaged(false);
        switcherView.setMouseTransparent(true);
        detailView.setVisible(false);
        detailView.setManaged(false);
        detailView.setMouseTransparent(true);

        identity.getChildren().add(actionsSwitcher);
        HBox.setHgrow(identity, Priority.ALWAYS);

        body.getChildren().addAll(avatarWrap, identity);
        card.getChildren().addAll(banner, body);

        return card;
    }
    
    private void showActionsTiles(VBox defaultView, VBox switcherView, VBox detailView, Button showBtn) {
        styleQuickActionsButton(showBtn, true);
        StackPane actionsSwitcher = quickActionsContainer != null ? quickActionsContainer : (StackPane) defaultView.getParent();
        if (actionsSwitcher == null) {
            return;
        }
        actionsSwitcher.getChildren().setAll(switcherView);
        switcherView.setVisible(true);
        switcherView.setManaged(true);
        switcherView.setMouseTransparent(false);
        switcherView.setOpacity(1);
        switcherView.setTranslateX(0);
        switcherView.toFront();
        HorizonDesignSystem.dropdownIn(switcherView);
    }

    private void showDefaultHeroInfo(VBox defaultView, VBox switcherView, VBox detailView, Button showBtn) {
        styleQuickActionsButton(showBtn, false);
        StackPane actionsSwitcher = quickActionsContainer != null ? quickActionsContainer : (StackPane) switcherView.getParent();
        if (actionsSwitcher == null) {
            return;
        }
        actionsSwitcher.getChildren().setAll(defaultView);
        defaultView.setVisible(true);
        defaultView.setManaged(true);
        defaultView.setMouseTransparent(false);
        defaultView.setOpacity(1);
        defaultView.setTranslateX(0);
        defaultView.toFront();
        HorizonDesignSystem.dropdownIn(defaultView);
    }

    private void styleQuickActionsButton(Button button, boolean active) {
        if (active) {
            button.setStyle(
                "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
                "-fx-text-fill: #ffffff; -fx-border-color: transparent; -fx-border-width: 1px;" +
                "-fx-background-radius: 999px; -fx-border-radius: 999px;" +
                "-fx-padding: 9 16 9 16; -fx-font-weight: 800; -fx-font-size: 11px;"
            );
        } else {
            button.setStyle(
                "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.15) + ";" +
                "-fx-text-fill: " + tm.getAccentHex() + ";" +
                "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.35) + "; -fx-border-width: 1px;" +
                "-fx-background-radius: 999px; -fx-border-radius: 999px;" +
                "-fx-padding: 9 16 9 16; -fx-font-weight: 800; -fx-font-size: 11px;"
            );
        }
    }

    private void animateHeroTransition(VBox from, VBox to) {
        if (from == to) {
            return;
        }

        to.toFront();
        to.setOpacity(0);
        to.setTranslateX(18);

        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(220), from);
        fadeOut.setFromValue(from.getOpacity());
        fadeOut.setToValue(0);

        javafx.animation.TranslateTransition slideOut = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(220), from);
        slideOut.setFromX(0);
        slideOut.setToX(-18);

        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(260), to);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        javafx.animation.TranslateTransition slideIn = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(260), to);
        slideIn.setFromX(18);
        slideIn.setToX(0);

        fadeOut.setOnFinished(e -> {
            from.setTranslateX(0);
            from.setOpacity(0);
        });
        
        fadeIn.setOnFinished(e -> {
            to.setTranslateX(0);
            to.setOpacity(1);
        });

        fadeOut.play();
        slideOut.play();
        fadeIn.play();
        slideIn.play();
    }

    private boolean isDescendantOf(javafx.scene.Node node, javafx.scene.Node ancestor) {
        javafx.scene.Node current = node;
        while (current != null) {
            if (current == ancestor) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    private VBox createStandingCard() {
        VBox card = cardShell();
        card.setPadding(new Insets(22));

        User currentUser = SessionManager.getInstance().getCurrentUser();
        UserStanding standing = SessionManager.getInstance().getCurrentStanding();

        int levelValue = standing != null ? standing.getLevel() : 1;
        int pointsValue = standing != null ? standing.getPoints() : 0;
        String standingLabel = standing != null ? standing.getStandingLabel() : "NORMAL";

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        Text levelNumber = text(String.valueOf(levelValue), 28, true, "#ffffff");
        VBox level = new VBox(0, levelNumber, text("LVL", 10, true, tm.getAccentHex()));
        level.setAlignment(Pos.CENTER);
        level.setMinSize(72, 72);
        level.setStyle(standingLevelStyle(false));
        installBreathingGlow(level, 0.22);

        Text liveBadge = text("LIVE XP", 10, true, tm.getAccentHex());
        HBox titleLine = new HBox(10, text("RESIDENT STANDING", 18, true, "#ffffff"), liveBadge);
        titleLine.setAlignment(Pos.CENTER_LEFT);
        VBox title = new VBox(4, titleLine, text("Your status within the Horizon community", 12, false, textMuted()));
        HBox.setHgrow(title, Priority.ALWAYS);

        Text pointsNumber = text(String.valueOf(pointsValue), 24, true, "#ffffff");
        VBox points = new VBox(2, pointsNumber, text("SYNDIC PTS", 10, true, "rgba(255,255,255,0.50)"));
        points.setAlignment(Pos.CENTER_RIGHT);
        installBreathingGlow(points, 0.14);

        header.getChildren().addAll(level, title, points);

        boolean isAllGood = "NORMAL".equals(standingLabel) || "GOOD".equals(standingLabel);
        boolean isWarned = "WARNED".equals(standingLabel);
        boolean isSuspended = "SUSPENDED".equals(standingLabel);
        boolean isBanned = "BANNED".equals(standingLabel);

        HBox standingBar = new HBox(12,
            standingPoint("All good", isAllGood),
            standingPoint("Warned", isWarned),
            standingPoint("Suspended", isSuspended),
            standingPoint("Banned", isBanned)
        );
        standingBar.setAlignment(Pos.CENTER_LEFT);

        int currentXp = pointsValue % 100;
        int previousPoints = lastRenderedStandingPoints >= 0 ? lastRenderedStandingPoints : pointsValue;
        int previousLevel = lastRenderedStandingLevel >= 0 ? lastRenderedStandingLevel : levelValue;
        int previousXp = Math.max(0, previousPoints % 100);
        int gainedXp = lastRenderedStandingPoints >= 0 ? Math.max(0, pointsValue - lastRenderedStandingPoints) : 0;

        if (previousLevel != levelValue) {
            animateIntegerText(levelNumber, previousLevel, levelValue, 650);
        }
        if (previousPoints != pointsValue) {
            animateIntegerText(pointsNumber, previousPoints, pointsValue, 760);
            playXpPulse(points);
            playXpPulse(level);
        }

        VBox xp = new VBox(8);
        HBox xpTop = new HBox();
        xpTop.setAlignment(Pos.CENTER_LEFT);
        Text left = text("XP TOWARDS LEVEL " + (levelValue + 1), 11, true, textMuted());
        Text gain = text(gainedXp > 0 ? "+" + gainedXp + " XP" : "SYNCED", 11, true, gainedXp > 0 ? tm.getAccentHex() : "rgba(255,255,255,0.42)");
        Text right = text(currentXp + " / 100", 11, true, tm.getTextColor());
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        xpTop.getChildren().addAll(left, gain, spacer, right);

        StackPane progressTrack = new StackPane();
        progressTrack.setAlignment(Pos.CENTER_LEFT);
        progressTrack.setMinHeight(10);
        progressTrack.setStyle("-fx-background-color: " + surfaceSoft() + "; -fx-background-radius: 999px; -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.18) + "; -fx-border-radius: 999px;");
        StackPane fill = new StackPane();
        fill.setMinHeight(10);
        fill.setStyle("-fx-background-color: " + tm.getEffectiveAccentGradient() + "; -fx-background-radius: 999px; -fx-effect: dropshadow(gaussian, " + tm.toRgba(tm.getAccentHex(), 0.55) + ", 18, 0.22, 0, 0);");
        StackPane shine = new StackPane();
        shine.setMouseTransparent(true);
        shine.setMinSize(58, 10);
        shine.setMaxSize(58, 10);
        shine.setOpacity(0.0);
        shine.setStyle("-fx-background-color: linear-gradient(to right, transparent, rgba(255,255,255,0.55), transparent); -fx-background-radius: 999px;");
        
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        StackPane.setAlignment(shine, Pos.CENTER_LEFT);
        double clampedXp = Math.min(Math.max(currentXp, 0.0), 100.0);
        double previousClampedXp = Math.min(Math.max(previousXp, 0.0), 100.0);
        fill.setMinWidth(0);
        javafx.beans.property.DoubleProperty progressValue = new javafx.beans.property.SimpleDoubleProperty(previousClampedXp / 100.0);
        fill.prefWidthProperty().bind(progressTrack.widthProperty().multiply(progressValue));
        fill.maxWidthProperty().bind(progressTrack.widthProperty().multiply(progressValue));
        animateProgress(progressValue, clampedXp / 100.0);
        animateProgressShine(shine, progressTrack);
        
        progressTrack.getChildren().addAll(fill, shine);

        HBox xpMeta = new HBox(10,
            standingMetricChip("TOTAL", String.valueOf(pointsValue)),
            standingMetricChip("NEXT", Math.max(0, 100 - currentXp) + " XP"),
            standingMetricChip("LEVEL", String.valueOf(levelValue))
        );
        xpMeta.setAlignment(Pos.CENTER_LEFT);

        HBox tickRow = new HBox();
        tickRow.setAlignment(Pos.CENTER_LEFT);
        tickRow.setSpacing(0);
        tickRow.getChildren().addAll(
            xpTick("0"), flexibleTickSpace(), xpTick("25"), flexibleTickSpace(), xpTick("50"), flexibleTickSpace(), xpTick("75"), flexibleTickSpace(), xpTick("100")
        );

        xp.getChildren().addAll(xpTop, progressTrack, tickRow, xpMeta);

        card.getChildren().addAll(header, standingBar, xp);
        lastRenderedStandingPoints = pointsValue;
        lastRenderedStandingLevel = levelValue;
        return card;
    }

    private VBox createCircleCard() {
        VBox card = cardShell();
        card.setPadding(new Insets(22));

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getIdUser() == null) {
            card.getChildren().addAll(
                text("CIRCLE", 18, true, "#ffffff"),
                text("Sign in to manage your circle.", 12, false, textMuted())
            );
            return card;
        }

        UserRelationshipController relationshipController = new UserRelationshipController();
        UserController userController = new UserController();
        // Removed blocking DB calls (refreshCircle[0].run() call at bottom)

        Text friendsBadge = badge("0 FRIENDS");
        Text pendingBadge = badge("0 PENDING");
        HBox top = new HBox(10, text("CIRCLE", 18, true, "#ffffff"), friendsBadge, pendingBadge);
        top.setAlignment(Pos.CENTER_LEFT);

        Button friendsTab = tabChip("Friends", true);
        Button pendingTab = tabChip("Pending", false);
        HBox switcher = new HBox(8, friendsTab, pendingTab);

        GridPane friendsGrid = new GridPane();
        friendsGrid.setHgap(12);
        friendsGrid.setVgap(12);

        VBox pendingList = new VBox(10);

        StackPane listFace = new StackPane(friendsGrid, pendingList);
        pendingList.setVisible(false);
        pendingList.setManaged(false);

        VBox search = new VBox(10);
        search.setPadding(new Insets(14));
        search.setStyle(shell(18, "rgba(10,10,15,0.80)", 0.16));
        Text searchTitle = text("Explore Community", 13, true, "#ffffff");
        Text searchSub = text("Search residents and send connection requests.", 12, false, textMuted());

        javafx.scene.control.TextField searchField = new javafx.scene.control.TextField();
        searchField.setPromptText("Search by name...");
        searchField.setStyle(
            "-fx-padding: 9 12 9 12;" +
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-text-fill: white;" +
            "-fx-border-color: rgba(255,255,255,0.12);" +
            "-fx-border-width: 1;" +
            "-fx-background-radius: 12; -fx-border-radius: 12;"
        );

        VBox searchResults = new VBox(8);

        Runnable[] refreshCircle = new Runnable[1];
        Runnable[] refreshSearch = new Runnable[1];
        int[] previousFriendCount = new int[] { -1 };
        int[] previousPendingCount = new int[] { -1 };

        refreshCircle[0] = () -> {
            friendsGrid.getChildren().setAll(text("Syncing circle...", 12, false, textMuted()));
            pendingList.getChildren().setAll(text("Syncing requests...", 12, false, textMuted()));

            Thread.startVirtualThread(() -> {
                try {
                    int friendCount = relationshipController.countFriends(currentUser);
                    int pendingCount = relationshipController.countPendingRequests(currentUser);
                    List<User> friends = relationshipController.findFriends(currentUser, 24);
                    List<UserRelationship> pending = relationshipController.findPendingRequestsFor(currentUser);

                    List<Integer> otherIds = new java.util.ArrayList<>();
                    for (UserRelationship rel : pending) {
                        Integer otherId = (rel.getUserSecondId() != null && rel.getUserSecondId().equals(currentUser.getIdUser()))
                            ? rel.getUserFirstId() : rel.getUserSecondId();
                        if (otherId != null && otherId > 0) otherIds.add(otherId);
                    }
                    java.util.Map<Integer, User> userMap = userController.findAllByIds(otherIds).stream()
                        .collect(java.util.stream.Collectors.toMap(User::getIdUser, u -> u, (a, b) -> a));

                    SessionManager.getInstance().setCircleData(friends, pending, friendCount, pendingCount);

                    Platform.runLater(() -> {
                        if (disposed) return;
                        friendsBadge.setText(friendCount + " FRIENDS");
                        pendingBadge.setText(pendingCount + " PENDING");
                        if (previousFriendCount[0] != -1 && previousFriendCount[0] != friendCount) {
                            animateBadgePulse(friendsBadge);
                        }
                        if (previousPendingCount[0] != -1 && previousPendingCount[0] != pendingCount) {
                            animateBadgePulse(pendingBadge);
                        }
                        previousFriendCount[0] = friendCount;
                        previousPendingCount[0] = pendingCount;

                        friendsGrid.getChildren().clear();
                        int col = 0;
                        int row = 0;
                        for (User friend : friends) {
                            VBox friendItem = circleFriendCard(friend, () -> {
                                relationshipController.removeConnection(currentUser.getIdUser(), friend.getIdUser());
                                SessionManager.getInstance().invalidateCircleCache();
                                refreshCircle[0].run();
                                refreshSearch[0].run();
                            });
                            friendsGrid.add(friendItem, col, row);
                            col++;
                            if (col >= 4) {
                                col = 0;
                                row++;
                            }
                        }
                        if (friends.isEmpty()) {
                            friendsGrid.add(text("No friends yet.", 12, false, textMuted()), 0, 0);
                        }

                        pendingList.getChildren().clear();
                        for (UserRelationship relationship : pending) {
                            Integer firstId = relationship.getUserFirstId();
                            Integer secondId = relationship.getUserSecondId();
                            if (firstId == null || secondId == null) continue;

                            boolean incoming = secondId.equals(currentUser.getIdUser());
                            int otherId = incoming ? firstId : secondId;
                            User otherUser = userMap.get(otherId);
                            if (otherUser == null) continue;

                            pendingList.getChildren().add(circlePendingRow(
                                otherUser,
                                incoming,
                                () -> {
                                    if (relationship.getId() != null) {
                                        relationshipController.acceptRequest(relationship.getId(), currentUser.getIdUser());
                                        SessionManager.getInstance().invalidateCircleCache();
                                        refreshCircle[0].run();
                                        refreshSearch[0].run();
                                    }
                                },
                                () -> {
                                    relationshipController.removeConnection(currentUser.getIdUser(), otherUser.getIdUser());
                                    SessionManager.getInstance().invalidateCircleCache();
                                    refreshCircle[0].run();
                                    refreshSearch[0].run();
                                }
                            ));
                        }
                        if (pendingList.getChildren().isEmpty()) {
                            pendingList.getChildren().add(text("No pending requests.", 12, false, textMuted()));
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        friendsGrid.getChildren().setAll(text("Circle could not sync right now.", 12, false, textMuted()));
                        pendingList.getChildren().setAll(text("Requests could not sync right now.", 12, false, textMuted()));
                    });
                }
            });
        };

        refreshSearch[0] = () -> {
            searchResults.getChildren().clear();
            String query = searchField.getText() == null ? "" : searchField.getText().trim();
            if (query.isEmpty()) {
                searchResults.getChildren().add(text("Start typing to discover residents.", 11, false, textMuted()));
                return;
            }

            List<User> users = userController.searchByName(query, currentUser.getIdUser(), 8);
            if (users.isEmpty()) {
                searchResults.getChildren().add(text("No users found.", 11, false, textMuted()));
                return;
            }

            for (User candidate : users) {
                if (candidate.getIdUser() == null || candidate.getIdUser().equals(currentUser.getIdUser())) {
                    continue;
                }

                Optional<UserRelationship> relationshipOpt = relationshipController.findRelationship(currentUser.getIdUser(), candidate.getIdUser());
                String actionLabel = relationshipController.getRelationshipLabel(currentUser.getIdUser(), candidate.getIdUser());
                Button actionBtn = new Button(actionLabel);
                actionBtn.setStyle(
                    "-fx-padding: 7 12 7 12;" +
                    "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
                    "-fx-text-fill: white; -fx-font-weight: 800;" +
                    "-fx-background-radius: 12; -fx-cursor: hand;"
                );

                actionBtn.setOnAction(evt -> {
                    String state = relationshipController.getRelationshipState(currentUser.getIdUser(), candidate.getIdUser());
                    boolean done;
                    switch (state) {
                        case "NONE" -> done = relationshipController.sendFriendRequest(currentUser.getIdUser(), candidate.getIdUser());
                        case "FRIENDS" -> done = relationshipController.removeConnection(currentUser.getIdUser(), candidate.getIdUser());
                        case "PENDING" -> done = relationshipController.removeConnection(currentUser.getIdUser(), candidate.getIdUser());
                        default -> done = false;
                    }

                    if (!done) {
                        showAvatarAlert(Alert.AlertType.ERROR, "Circle", "Could not update relationship.");
                        return;
                    }
                    refreshCircle[0].run();
                    refreshSearch[0].run();
                });

                if ("BLOCKED".equals(relationshipController.getRelationshipState(currentUser.getIdUser(), candidate.getIdUser()))) {
                    actionBtn.setDisable(true);
                }

                searchResults.getChildren().add(circleSearchRow(candidate, relationshipOpt, actionBtn, currentUser, () -> {
                    refreshCircle[0].run();
                    refreshSearch[0].run();
                }));
            }
        };

        javafx.animation.PauseTransition searchDebounce = new javafx.animation.PauseTransition(javafx.util.Duration.millis(220));
        searchDebounce.setOnFinished(e -> refreshSearch[0].run());
        searchField.textProperty().addListener((obs, oldValue, newValue) -> searchDebounce.playFromStart());

        friendsTab.setOnAction(e -> {
            styleCircleTabButton(friendsTab, true);
            styleCircleTabButton(pendingTab, false);
            animateCircleFace(pendingList, friendsGrid);
        });

        pendingTab.setOnAction(e -> {
            styleCircleTabButton(friendsTab, false);
            styleCircleTabButton(pendingTab, true);
            animateCircleFace(friendsGrid, pendingList);
        });

        SessionManager sm = SessionManager.getInstance();
        if (sm.isCircleCacheFresh() && sm.getCachedFriends() != null) {
            friendsBadge.setText(Math.max(sm.getCachedFriendCount(), 0) + " FRIENDS");
            pendingBadge.setText(Math.max(sm.getCachedPendingCount(), 0) + " PENDING");
            friendsGrid.getChildren().clear();
            int col = 0;
            int row = 0;
            for (User friend : sm.getCachedFriends()) {
                friendsGrid.add(circleFriendCard(friend, () -> {
                    relationshipController.removeConnection(currentUser.getIdUser(), friend.getIdUser());
                    SessionManager.getInstance().invalidateCircleCache();
                    refreshCircle[0].run();
                    refreshSearch[0].run();
                }), col, row);
                col++;
                if (col >= 4) {
                    col = 0;
                    row++;
                }
            }
            if (sm.getCachedFriends().isEmpty()) {
                friendsGrid.add(text("No friends yet.", 12, false, textMuted()), 0, 0);
            }
            pendingList.getChildren().setAll(text("Open Pending to sync requests.", 12, false, textMuted()));
        } else {
            friendsGrid.getChildren().setAll(text("Circle is loading quietly...", 12, false, textMuted()));
            pendingList.getChildren().setAll(text("Requests are loading quietly...", 12, false, textMuted()));
            refreshCircle[0].run();
        }
        refreshSearch[0].run();

        search.getChildren().addAll(searchTitle, searchSub, searchField, searchResults);

        card.getChildren().addAll(top, switcher, listFace, search);
        return card;
    }

    private void styleCircleTabButton(Button btn, boolean active) {
        String background = active ? tm.getEffectiveAccentGradient() : surfaceSoft();
        String textFill = active ? "white" : textMuted();
        btn.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + textFill + ";" +
            "-fx-font-weight: 800; -fx-background-radius: 12px; -fx-padding: 8 12 8 12;"
        );
    }

    private VBox circleFriendCard(User friend, Runnable onRemove) {
        String fullName = ((friend.getFirstName() == null ? "" : friend.getFirstName()) + " " + (friend.getLastName() == null ? "" : friend.getLastName())).trim();
        String display = fullName.isBlank() ? "User" : fullName;

        VBox c = new VBox(8);
        c.setPadding(new Insets(10));
        c.setAlignment(Pos.CENTER_LEFT);
        c.setStyle(shell(14, "rgba(10,10,15,0.80)", 0.10));

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = circleAvatarNode(friend, display, 38, 14);

        VBox meta = new VBox(2);
        meta.getChildren().addAll(
            text(display, 11, true, "#ffffff"),
            text(friend.getEmailUser() == null ? "" : friend.getEmailUser(), 10, false, textMuted())
        );
        row.getChildren().addAll(avatar, meta);

        Button removeBtn = new Button("Remove");
        removeBtn.setStyle(
            "-fx-padding: 6 10 6 10;" +
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-text-fill: white; -fx-font-weight: 800;" +
            "-fx-background-radius: 10; -fx-cursor: hand;"
        );
        removeBtn.setOnAction(e -> onRemove.run());

        c.getChildren().addAll(row, removeBtn);
        return c;
    }

    private HBox circlePendingRow(User otherUser, boolean incoming, Runnable onAccept, Runnable onDeclineOrCancel) {
        String fullName = ((otherUser.getFirstName() == null ? "" : otherUser.getFirstName()) + " " + (otherUser.getLastName() == null ? "" : otherUser.getLastName())).trim();
        String display = fullName.isBlank() ? "User" : fullName;

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle(shell(12, "rgba(10,10,15,0.80)", 0.10));

        StackPane avatar = circleAvatarNode(otherUser, display, 34, 12);

        VBox meta = new VBox(2);
        meta.getChildren().addAll(
            text(display, 12, true, "#ffffff"),
            text(incoming ? "Incoming request" : "Sent request", 10, false, textMuted())
        );
        HBox.setHgrow(meta, Priority.ALWAYS);

        HBox actions = new HBox(8);
        if (incoming) {
            Button accept = new Button("Accept");
            accept.setStyle(
                "-fx-padding: 6 10 6 10;" +
                "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
                "-fx-text-fill: white; -fx-font-weight: 800;" +
                "-fx-background-radius: 10; -fx-cursor: hand;"
            );
            accept.setOnAction(e -> onAccept.run());
            actions.getChildren().add(accept);
        }

        Button decline = new Button(incoming ? "Decline" : "Cancel");
        decline.setStyle(
            "-fx-padding: 6 10 6 10;" +
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-text-fill: white; -fx-font-weight: 800;" +
            "-fx-background-radius: 10; -fx-cursor: hand;"
        );
        decline.setOnAction(e -> onDeclineOrCancel.run());
        actions.getChildren().add(decline);

        row.getChildren().addAll(avatar, meta, actions);
        return row;
    }

    private HBox circleSearchRow(User candidate, Optional<UserRelationship> relationshipOpt, Button actionBtn, User currentUser, Runnable onRefresh) {
        String fullName = ((candidate.getFirstName() == null ? "" : candidate.getFirstName()) + " " + (candidate.getLastName() == null ? "" : candidate.getLastName())).trim();
        String display = fullName.isBlank() ? "User" : fullName;

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle(shell(10, "rgba(10,10,15,0.80)", 0.08));

        StackPane avatar = circleAvatarNode(candidate, display, 32, 12);

        VBox meta = new VBox(1);
        meta.getChildren().addAll(
            text(display, 11, true, "#ffffff"),
            text(candidate.getEmailUser() == null ? "" : candidate.getEmailUser(), 10, false, textMuted())
        );
        HBox.setHgrow(meta, Priority.ALWAYS);

        if (relationshipOpt.isPresent()) {
            UserRelationship r = relationshipOpt.get();
            boolean incoming = r.getUserSecondId() != null && r.getUserSecondId().equals(currentUser.getIdUser())
                && "PENDING_FIRST_SECOND".equalsIgnoreCase(r.getStatus());
            if (incoming && r.getId() != null) {
                Button acceptBtn = new Button("Accept");
                acceptBtn.setStyle(
                    "-fx-padding: 6 10 6 10;" +
                    "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
                    "-fx-text-fill: white; -fx-font-weight: 800;" +
                    "-fx-background-radius: 10; -fx-cursor: hand;"
                );
                UserRelationshipController relationshipController = new UserRelationshipController();
                acceptBtn.setOnAction(e -> {
                    relationshipController.acceptRequest(r.getId(), currentUser.getIdUser());
                    onRefresh.run();
                });
                row.getChildren().addAll(avatar, meta, acceptBtn, actionBtn);
                return row;
            }
        }

        row.getChildren().addAll(avatar, meta, actionBtn);
        return row;
    }

    private VBox createActivityPage() {
        VBox page = new VBox(20);
        page.setAlignment(Pos.TOP_CENTER);

        HBox tabNav = new HBox(8);
        tabNav.setAlignment(Pos.CENTER);
        tabNav.setPadding(new Insets(8));
        tabNav.setMaxWidth(1120);
        tabNav.setStyle(shell(999, "rgba(10,10,15,0.75)", 0.1));

        addDetailTab(tabNav, "Account", "account", new ProfileAccountSection().getRoot());
        addDetailTab(tabNav, "Forum", "forum", new ProfileForumSectionEnhanced().getRoot());
        addDetailTab(tabNav, "Events", "events", new ProfileEventsSectionEnhanced().getRoot());
        addDetailTab(tabNav, "Reclamation", "reclamation", new ProfileReclamationSectionEnhanced().getRoot());
        addDetailTab(tabNav, "Residence", "residence", createResidenceTab());

        VBox wrapper = new VBox(0);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setMaxWidth(1600);
        wrapper.getChildren().addAll(tabNav);
        wrapper.getChildren().addAll(detailTabs.values());

        setDetailTab("account");
        page.getChildren().add(wrapper);
        return page;
    }

    private VBox createActivityPageShell() {
        VBox page = new VBox(20);
        page.setAlignment(Pos.TOP_CENTER);

        HBox tabNav = new HBox(8);
        tabNav.setAlignment(Pos.CENTER);
        tabNav.setPadding(new Insets(8));
        tabNav.setMaxWidth(1120);
        tabNav.setStyle(shell(999, "rgba(10,10,15,0.75)", 0.1));

        VBox wrapper = new VBox(0);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setMaxWidth(1600);
        wrapper.getChildren().add(tabNav);
        page.getChildren().add(wrapper);

        detailTabs.clear();
        detailTabButtons.clear();
        addLazyDetailTab(tabNav, wrapper, "Account", "account", () -> new ProfileAccountSection().getRoot());
        addLazyDetailTab(tabNav, wrapper, "Forum", "forum", () -> new ProfileForumSectionEnhanced().getRoot());
        addLazyDetailTab(tabNav, wrapper, "Events", "events", () -> new ProfileEventsSectionEnhanced().getRoot());
        addLazyDetailTab(tabNav, wrapper, "Reclamation", "reclamation", () -> new ProfileReclamationSectionEnhanced().getRoot());
        addLazyDetailTab(tabNav, wrapper, "Residence", "residence", this::createResidenceTab);
        setDetailTab("account");
        return page;
    }

    private void addLazyDetailTab(HBox nav, VBox wrapper, String label, String key, java.util.function.Supplier<VBox> factory) {
        VBox placeholder = new VBox(10, text("Loading " + label.toLowerCase() + "...", 12, false, textMuted()));
        placeholder.setAlignment(Pos.TOP_CENTER);
        placeholder.setPadding(new Insets(16, 0, 0, 0));
        detailTabs.put(key, placeholder);
        wrapper.getChildren().add(placeholder);

        Button btn = new Button(label);
        btn.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 12));
        btn.setOnAction(e -> {
            VBox current = detailTabs.get(key);
            if (current == placeholder) {
                Thread.startVirtualThread(() -> {
                    try {
                        Platform.runLater(() -> {
                            if (disposed || detailTabs.get(key) != placeholder) return;
                            VBox built = factory.get();
                            int index = wrapper.getChildren().indexOf(placeholder);
                            if (index >= 0) {
                                wrapper.getChildren().set(index, built);
                            }
                            detailTabs.put(key, built);
                            setDetailTab(key);
                        });
                    } catch (Exception ignored) {
                    }
                });
            }
            setDetailTab(key);
        });
        styleDetailTabButton(btn, false);
        detailTabButtons.put(key, btn);
        nav.getChildren().add(btn);

        if ("account".equals(key)) {
            Platform.runLater(btn::fire);
        }
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
        String background = active ? tm.getEffectiveAccentGradient() : "transparent";
        String textFill = active ? "white" : textMuted();
        btn.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + textFill + ";" +
            "-fx-font-weight: 800; -fx-background-radius: 999px; -fx-padding: 11 18 11 18;"
        );
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
        row.setStyle(shell(16, "rgba(10,10,15,0.80)", 0.08));

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
        row.setStyle(shell(16, "rgba(10,10,15,0.80)", 0.08));

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
        row.setStyle(shell(16, "rgba(255, 255, 255, 0.03)", 0.06));

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
        pill.setStyle(shell(12, "rgba(10,10,15,0.75)", 0.09));
        return pill;
    }

    private Button smallPill(String text, boolean active) {
        Button b = new Button(text);
        String background = active ? "rgba(255,255,255,0.15)" : "transparent";
        String textFill = active ? "#ffffff" : textMuted();
        b.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + textFill + ";" +
            "-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-radius: 999px; -fx-padding: 7 12 7 12;"
        );
        return b;
    }

    private Button forumTabPill(String text, boolean active) {
        Button b = new Button(text);
        String background = active ? "rgba(255,255,255,0.10)" : "transparent";
        String textFill = active ? "#ffffff" : textMuted();
        b.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + textFill + ";" +
            "-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-radius: 999px; -fx-padding: 9 12 9 12;"
        );
        return b;
    }

    private Button pagePill(String text, boolean active) {
        Button b = new Button(text);
        b.setMinSize(34, 34);
        String background = active ? tm.getEffectiveAccentGradient() : "transparent";
        String textFill = active ? "#ffffff" : textMuted();
        b.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + textFill + ";" +
            "-fx-font-size: 11px; -fx-font-weight: 800; -fx-background-radius: 999px;"
        );
        return b;
    }

    private VBox createActionTile(String label, String actionId, VBox switcherView, Button showActionsBtn) {
        VBox tile = new VBox(6);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(13, 8, 13, 8));
        tile.setStyle(HorizonDesignSystem.webSectionCard(16, false));
        tile.setCursor(javafx.scene.Cursor.HAND);
        tile.setMinHeight(78);
        
        String cleanIcon = quickActionIcon(actionId);
        String cleanTitle = quickActionTitle(actionId);
        javafx.scene.text.Text icon = text(cleanIcon, 18, true, tm.getAccentHex());
        javafx.scene.text.Text title = text(cleanTitle.toUpperCase(), 10, true, "rgba(255,255,255,0.70)");
        
        tile.getChildren().addAll(icon, title);
        
        tile.setOnMouseEntered(e -> {
            tile.setStyle(HorizonDesignSystem.webSectionCard(16, true));
            javafx.animation.TranslateTransition lift = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(180), tile);
            lift.setToY(-5);
            lift.setInterpolator(HorizonDesignSystem.WEB_EASE);
            lift.play();
            title.setFill(Color.WHITE);
        });
        tile.setOnMouseExited(e -> {
            tile.setStyle(HorizonDesignSystem.webSectionCard(16, false));
            javafx.animation.TranslateTransition settle = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(170), tile);
            settle.setToY(0);
            settle.setInterpolator(HorizonDesignSystem.WEB_EASE);
            settle.play();
            title.setFill(Color.web("rgba(255,255,255,0.70)"));
        });
        tile.setOnMouseClicked(e -> {
            awardInteractionXp(1);
            showActionPanel(actionId, switcherView, showActionsBtn);
        });
        
        return tile;
    }

    private String quickActionIcon(String actionId) {
        return switch (actionId) {
            case "host" -> "\u2605";
            case "join" -> "+";
            case "2fa" -> "\u25C9";
            case "biometrics" -> "\u25C7";
            case "faceid" -> "\u25C9";
            case "settings" -> "\u2699";
            default -> "\u22EF";
        };
    }

    private String quickActionTitle(String actionId) {
        return switch (actionId) {
            case "host" -> "Host Spotlight";
            case "join" -> "Join by Code";
            case "2fa" -> "2FA";
            case "biometrics" -> "Biometrics";
            case "faceid" -> "Face ID";
            case "settings" -> "Settings";
            default -> actionId == null ? "Action" : actionId;
        };
    }
    
    private void showActionPanel(String actionId, VBox switcherView, Button showActionsBtn) {
        StackPane parent = quickActionsContainer != null ? quickActionsContainer : (StackPane) switcherView.getParent();
        VBox defaultView = quickActionsDefaultView;
        VBox detailView = quickActionsDetailView;
        if (parent == null || defaultView == null || detailView == null) {
            return;
        }

        parent.getChildren().setAll(detailView);
        detailView.getChildren().clear();
        detailView.setVisible(true);
        detailView.setManaged(true);
        detailView.setMouseTransparent(false);
        detailView.setOpacity(1);
        detailView.setTranslateX(0);
        detailView.toFront();
        
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 12, 0));
        header.setStyle("-fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 0 0 1 0;");
        
        javafx.scene.control.Button backBtn = new javafx.scene.control.Button("ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â Ãƒâ€šÃ‚Â");
        backBtn.setText("\u2190");
        backBtn.setStyle(
            "-fx-min-width: 30px; -fx-min-height: 30px;" +
            "-fx-max-width: 30px; -fx-max-height: 30px;" +
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-border-color: rgba(255,255,255,0.15); -fx-border-width: 1px;" +
            "-fx-border-radius: 999px; -fx-background-radius: 999px;" +
            "-fx-text-fill: rgba(255,255,255,0.75); -fx-font-weight: 700; -fx-cursor: hand;"
        );
        HorizonDesignSystem.installButtonMotion(backBtn);
        backBtn.setOnAction(e -> hideActionPanel(switcherView, detailView, showActionsBtn));
        
        javafx.scene.text.Text title = text(getTitleForAction(actionId), 14, true, "rgba(255,255,255,0.88)");
        
        header.getChildren().addAll(backBtn, title);
        detailView.getChildren().add(header);
        HorizonDesignSystem.dropdownIn(detailView);
        
        // Add content based on action
        VBox content = new VBox(12);
        content.setPadding(new Insets(12, 0, 0, 0));
        content.setStyle("-fx-background-color: transparent;");
        
        ScrollPane scrollPane = null;
        if ("settings".equals(actionId)) {
            scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setMaxHeight(190);
            scrollPane.setPrefHeight(190);
            scrollPane.setStyle("-fx-control-inner-background: transparent; -fx-padding: 0; -fx-border-color: transparent;");
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        }
        
        switch(actionId) {
            case "2fa" -> buildTwoFactorPanel(content);
            case "biometrics" -> buildBiometricsPanel(content);
            case "faceid" -> buildFaceIDPanel(content);
            case "settings" -> {
                User user = SessionManager.getInstance().getCurrentUser();
                Profile profile = currentProfile();
                content.getChildren().add(text("Profile Settings", 13, true, textSoft()));
                
                // Bio field
                content.getChildren().add(text("Bio", 11, true, "rgba(255,255,255,0.70)"));
                javafx.scene.control.TextArea bioArea = new javafx.scene.control.TextArea();
                bioArea.setPromptText("Tell us a bit about yourself...");
                bioArea.setWrapText(true);
                bioArea.setStyle(profileInputStyle() + "-fx-control-inner-background: " + surfaceCard() + ";");
                bioArea.setPrefHeight(50);
                bioArea.setText(profile != null && profile.getDescriptionProfile() != null ? profile.getDescriptionProfile() : "");
                content.getChildren().add(bioArea);
                content.getChildren().add(text(" ", 8, false, "transparent"));
                
                // Phone field
                content.getChildren().add(text("Phone", 11, true, "rgba(255,255,255,0.70)"));
                javafx.scene.control.TextField phoneField = new javafx.scene.control.TextField();
                phoneField.setPromptText("Phone number");
                phoneField.setStyle(profileInputStyle());
                phoneField.setText(user != null && user.getPhone() != null ? user.getPhone() : "");
                content.getChildren().add(phoneField);
                content.getChildren().add(text(" ", 8, false, "transparent"));
                
                // Theme, Language, Timezone row
                content.getChildren().add(text("Preferences", 11, true, "rgba(255,255,255,0.70)"));
                HBox prefRow = new HBox(10);
                prefRow.setStyle("-fx-fill-height: true;");
                
                javafx.scene.control.ComboBox<String> themeCombo = new javafx.scene.control.ComboBox<>();
                themeCombo.getItems().addAll("Dark", "Light", "Auto");
                themeCombo.setValue(themeFromCode(profile == null ? null : profile.getTheme()));
                themeCombo.setStyle(profileInputStyle());
                HBox.setHgrow(themeCombo, Priority.ALWAYS);
                
                javafx.scene.control.ComboBox<String> langCombo = new javafx.scene.control.ComboBox<>();
                langCombo.getItems().addAll("English", "French", "Arabic", "Spanish");
                langCombo.setValue(languageFromLocale(profile == null ? null : profile.getLocale()));
                langCombo.setStyle(profileInputStyle());
                HBox.setHgrow(langCombo, Priority.ALWAYS);
                
                javafx.scene.control.TextField tzField = new javafx.scene.control.TextField();
                tzField.setPromptText("UTC+1");
                tzField.setStyle(profileInputStyle());
                tzField.setText(timezoneText(profile == null ? null : profile.getTimezone()));
                HBox.setHgrow(tzField, Priority.ALWAYS);
                
                prefRow.getChildren().addAll(themeCombo, langCombo, tzField);
                content.getChildren().add(prefRow);
                content.getChildren().add(text(" ", 8, false, "transparent"));
                
                // Security section
                content.getChildren().add(text("SECURITY", 10, true, "rgba(255,255,255,0.50)"));
                content.getChildren().add(text("Current Password", 11, true, "rgba(255,255,255,0.70)"));
                javafx.scene.control.PasswordField currentPwdField = new javafx.scene.control.PasswordField();
                currentPwdField.setPromptText("Required to change password");
                currentPwdField.setStyle(profileInputStyle());
                content.getChildren().add(currentPwdField);
                content.getChildren().add(text(" ", 8, false, "transparent"));
                
                content.getChildren().add(text("New Password", 11, true, "rgba(255,255,255,0.70)"));
                javafx.scene.control.PasswordField newPwdField = new javafx.scene.control.PasswordField();
                newPwdField.setPromptText("Min 6 characters");
                newPwdField.setStyle(profileInputStyle());
                content.getChildren().add(newPwdField);
                content.getChildren().add(text(" ", 8, false, "transparent"));
                
                content.getChildren().add(text("Confirm Password", 11, true, "rgba(255,255,255,0.70)"));
                javafx.scene.control.PasswordField confirmPwdField = new javafx.scene.control.PasswordField();
                confirmPwdField.setPromptText("Confirm new password");
                confirmPwdField.setStyle(profileInputStyle());
                content.getChildren().add(confirmPwdField);
                content.getChildren().add(text(" ", 4, false, "transparent"));
                
                javafx.scene.control.Button saveBtn = new javafx.scene.control.Button("Save Changes");
                saveBtn.setStyle(profilePrimaryButtonStyle(14));
                HorizonDesignSystem.installButtonMotion(saveBtn);
                saveBtn.setOnAction(e -> {
                    User sessionUser = SessionManager.getInstance().getCurrentUser();
                    if (sessionUser == null || sessionUser.getIdUser() == null) {
                        showAvatarAlert(Alert.AlertType.ERROR, "Settings", "No active user session.");
                        return;
                    }

                    UserController userController = new UserController();
                    ProfileController profileController = new ProfileController();

                    String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
                    String currentPwd = currentPwdField.getText() == null ? "" : currentPwdField.getText().trim();
                    String newPwd = newPwdField.getText() == null ? "" : newPwdField.getText().trim();
                    String confirmPwd = confirmPwdField.getText() == null ? "" : confirmPwdField.getText().trim();

                    boolean wantsPasswordChange = !currentPwd.isEmpty() || !newPwd.isEmpty() || !confirmPwd.isEmpty();
                    if (wantsPasswordChange) {
                        if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                            showAvatarAlert(Alert.AlertType.ERROR, "Settings", "Fill current, new and confirm password fields.");
                            return;
                        }
                        if (!matchesPassword(currentPwd, sessionUser.getPasswordUser())) {
                            showAvatarAlert(Alert.AlertType.ERROR, "Settings", "Current password is incorrect.");
                            return;
                        }
                        if (newPwd.length() < 6) {
                            showAvatarAlert(Alert.AlertType.ERROR, "Settings", "New password must be at least 6 characters.");
                            return;
                        }
                        if (!newPwd.equals(confirmPwd)) {
                            showAvatarAlert(Alert.AlertType.ERROR, "Settings", "New password and confirmation do not match.");
                            return;
                        }
                    }

                    User userToSave = sessionUser;
                    userToSave.setPhone(phone.isEmpty() ? null : phone);
                    if (wantsPasswordChange) {
                        userToSave.setPasswordUser(BCrypt.hashpw(newPwd, BCrypt.gensalt(13)));
                    }

                    boolean userUpdated = userController.userEdit(userToSave);
                    if (!userUpdated) {
                        showAvatarAlert(Alert.AlertType.ERROR, "Settings", "Failed to update user account settings.");
                        return;
                    }

                    Profile profileToSave = getOrCreateCurrentProfile(sessionUser);
                    if (profileToSave == null) {
                        showAvatarAlert(Alert.AlertType.ERROR, "Settings", "Failed to load profile settings.");
                        return;
                    }

                    profileToSave.setDescriptionProfile(bioArea.getText() == null ? "" : bioArea.getText().trim());
                    profileToSave.setTheme(themeToCode(themeCombo.getValue()));
                    profileToSave.setLocale(languageToLocale(langCombo.getValue()));
                    profileToSave.setTimezone(parseTimezone(tzField.getText()));
                    if (profileToSave.getSettingsJson() == null || profileToSave.getSettingsJson().isBlank()) {
                        profileToSave.setSettingsJson("{}");
                    }

                    boolean profileUpdated = profileController.updateProfile(profileToSave);
                    if (!profileUpdated) {
                        showAvatarAlert(Alert.AlertType.ERROR, "Settings", "Failed to update profile preferences.");
                        return;
                    }

                    SessionManager.getInstance().setCurrentUser(userToSave);
                    SessionManager.getInstance().setCurrentProfile(profileToSave);
                    currentPwdField.clear();
                    newPwdField.clear();
                    confirmPwdField.clear();
                    showAvatarAlert(Alert.AlertType.INFORMATION, "Settings", "Settings saved successfully.");
                });
                content.getChildren().add(saveBtn);
            }
            case "host" -> {
                content.getChildren().add(text("Host Spotlight", 13, true, textSoft()));
                content.getChildren().add(text("Start a video spotlight session and invite residents to join.", 11, false, textMuted()));
                javafx.scene.control.Button startBtn = new javafx.scene.control.Button("Start Spotlight");
                startBtn.setStyle(profilePrimaryButtonStyle(14));
                HorizonDesignSystem.installButtonMotion(startBtn);
                content.getChildren().add(startBtn);
            }
            case "join" -> {
                content.getChildren().add(text("Join by Invitation Code", 13, true, textSoft()));
                content.getChildren().add(text("Enter a room code to join an existing spotlight session.", 11, false, textMuted()));
                javafx.scene.control.Button joinBtn = new javafx.scene.control.Button("Join Session");
                joinBtn.setStyle(profilePrimaryButtonStyle(14));
                HorizonDesignSystem.installButtonMotion(joinBtn);
                content.getChildren().add(joinBtn);
            }
        }
        
        if (scrollPane != null) {
            detailView.getChildren().add(scrollPane);
        } else {
            detailView.getChildren().add(content);
        }
        parent.getChildren().setAll(detailView);
        detailView.setVisible(true);
        detailView.setManaged(true);
        detailView.setMouseTransparent(false);
        detailView.setOpacity(1);
        detailView.setTranslateX(0);
        detailView.toFront();
    }
    
    private void hideActionPanel(VBox switcherView, VBox detailView, Button showActionsBtn) {
        styleQuickActionsButton(showActionsBtn, true);
        StackPane parent = quickActionsContainer != null ? quickActionsContainer : (StackPane) detailView.getParent();
        if (parent == null) {
            return;
        }
        parent.getChildren().setAll(switcherView);
        switcherView.setVisible(true);
        switcherView.setManaged(true);
        switcherView.setMouseTransparent(false);
        switcherView.setOpacity(1);
        switcherView.setTranslateX(0);
        switcherView.toFront();
    }
    
    private String getTitleForAction(String actionId) {
        return switch(actionId) {
            case "2fa" -> "Two-Factor Auth";
            case "biometrics" -> "Biometrics";
            case "faceid" -> "Face ID";
            case "settings" -> "Settings";
            case "host" -> "Host Spotlight";
            case "join" -> "Join by Code";
            default -> "Action";
        };
    }
    
    private String formatRole(String role) {
        if (role == null) {
            return "Resident";
        }
        if ("ADMIN".equals(role)) {
            return "\u25C9 Administrator";
        }
        if ("OWNER".equals(role)) {
            return "\u25C7 Property Owner";
        }
        if ("RESIDENT".equals(role)) {
            return "\u25CF Resident";
        }
        return switch(role) {
            case "ADMIN" -> "ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã‚ÂÃƒâ€šÃ‚Â Administrator";
            case "OWNER" -> "ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“ Property Owner";
            case "RESIDENT" -> "ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã‹Å“Ãƒâ€šÃ‚Â¤ Resident";
            default -> role;
        };
    }

    private void handleProfileAvatarUpload(User user, Circle avatarCircle, Text avatarText) {
        if (user == null || user.getIdUser() == null || root.getScene() == null) {
            return;
        }

        if (avatarUpdateInProgress) {
            showAvatarAlert(Alert.AlertType.INFORMATION, "Avatar", "An avatar update is already running.");
            return;
        }

        Optional<String> selectedChoice = showAvatarSourceDialog();
        if (selectedChoice.isEmpty()) {
            return;
        }

        if ("generate".equals(selectedChoice.get())) {
            handleProfileAvatarGeneration(user, avatarCircle, avatarText);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Profile Picture");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selected = chooser.showOpenDialog(root.getScene().getWindow());
        if (selected == null) {
            return;
        }

        try {
            Profile profile = getOrCreateCurrentProfile(user);
            if (profile == null || profile.getIdProfile() == null) {
                showAvatarAlert(Alert.AlertType.ERROR, "Avatar", "Could not load your profile record.");
                return;
            }

            byte[] data = Files.readAllBytes(selected.toPath());
            ProfileAvatarController controller = new ProfileAvatarController();
            ProfileAvatarController.AvatarUpdateResult result = controller.updateAvatar(profile, data, selected.getName());
            if (!result.isSuccess()) {
                showAvatarAlert(Alert.AlertType.ERROR, "Avatar", result.getMessage());
                return;
            }

            profile.setAvatar(result.getImagePath());
            SessionManager.getInstance().setCurrentProfile(profile);

            boolean hasAvatar = applyAvatarFill(avatarCircle);
            avatarText.setText(currentInitial(user));
            avatarText.setVisible(!hasAvatar);
            avatarText.setManaged(!hasAvatar);
            showAvatarAlert(Alert.AlertType.INFORMATION, "Avatar", "Profile picture updated.");
        } catch (Exception ex) {
            showAvatarAlert(Alert.AlertType.ERROR, "Avatar", "Failed to upload image.");
        }
    }

    private void handleProfileAvatarGeneration(User user, Circle avatarCircle, Text avatarText) {
        if (avatarUpdateInProgress) {
            showAvatarAlert(Alert.AlertType.INFORMATION, "Avatar", "An avatar update is already running.");
            return;
        }

        Optional<String> promptResult = showAvatarPromptDialog();
        if (promptResult.isEmpty()) {
            return;
        }

        String prompt = promptResult.get().trim();
        if (prompt.isBlank()) {
            showAvatarAlert(Alert.AlertType.ERROR, "Avatar", "Please enter a prompt for the generated avatar.");
            return;
        }

        avatarUpdateInProgress = true;

        Task<ProfileAvatarController.AvatarUpdateResult> task = new Task<>() {
            @Override
            protected ProfileAvatarController.AvatarUpdateResult call() {
                Profile profile = getOrCreateCurrentProfile(user);
                if (profile == null || profile.getIdProfile() == null) {
                    return ProfileAvatarController.AvatarUpdateResult.failure("Could not load your profile record.");
                }

                ProfileAvatarController controller = new ProfileAvatarController();
                return controller.generateAvatar(profile, prompt);
            }
        };

        task.setOnSucceeded(event -> {
            avatarUpdateInProgress = false;
            ProfileAvatarController.AvatarUpdateResult result = task.getValue();
            if (!result.isSuccess()) {
                showAvatarAlert(Alert.AlertType.ERROR, "Avatar", result.getMessage());
                return;
            }

            Profile profile = getOrCreateCurrentProfile(user);
            if (profile != null) {
                profile.setAvatar(result.getImagePath());
                SessionManager.getInstance().setCurrentProfile(profile);
            }

            boolean hasAvatar = applyAvatarFill(avatarCircle);
            avatarText.setText(currentInitial(user));
            avatarText.setVisible(!hasAvatar);
            avatarText.setManaged(!hasAvatar);
            showAvatarAlert(Alert.AlertType.INFORMATION, "Avatar", "Generated profile picture updated.");
        });

        task.setOnFailed(event -> {
            avatarUpdateInProgress = false;
            Throwable t = task.getException();
            if (t != null) {
                System.out.println("ProfileView: Avatar generation task failed: " + t.getMessage());
                t.printStackTrace();
                String msg = t.getMessage() == null ? "Failed to generate image." : t.getMessage();
                showAvatarAlert(Alert.AlertType.ERROR, "Avatar", msg);
            } else {
                showAvatarAlert(Alert.AlertType.ERROR, "Avatar", "Failed to generate image.");
            }
        });

        Thread worker = new Thread(task, "profile-avatar-generation");
        worker.setDaemon(true);
        worker.start();
    }

    private Optional<String> showAvatarSourceDialog() {
        if (root.getScene() == null || root.getScene().getWindow() == null) {
            return Optional.empty();
        }

        Stage stage = new Stage();
        stage.initOwner(root.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        Label title = new Label("Update your profile picture");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: 800;");
        Label subtitle = new Label("Choose a local image file or generate one with Pollinations AI.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.72); -fx-font-size: 13px;");

        Button generateBtn = new Button("Generate with AI");
        generateBtn.setStyle(fancyAvatarSecondaryButtonStyle());
        HorizonDesignSystem.installButtonMotion(generateBtn);
        Button uploadBtn = new Button("Upload Image");
        uploadBtn.setStyle(fancyAvatarPrimaryButtonStyle());
        HorizonDesignSystem.installButtonMotion(uploadBtn);
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(fancyAvatarGhostButtonStyle());
        HorizonDesignSystem.installButtonMotion(cancelBtn);

        HBox actions = new HBox(8, generateBtn, uploadBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(12, title, subtitle, actions);
        content.setStyle(HorizonDesignSystem.webSectionCard(18, false) + "-fx-padding: 14;");
        HorizonDesignSystem.popIn(content);

        AtomicReference<String> resultRef = new AtomicReference<>(null);
        generateBtn.setOnAction(e -> { resultRef.set("generate"); stage.close(); });
        uploadBtn.setOnAction(e -> { resultRef.set("upload"); stage.close(); });
        cancelBtn.setOnAction(e -> { resultRef.set(null); stage.close(); });

        Scene scene = new Scene(content);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.showAndWait();

        String res = resultRef.get();
        return res == null ? Optional.empty() : Optional.of(res);
    }

    private Optional<String> showAvatarPromptDialog() {
        if (root.getScene() == null || root.getScene().getWindow() == null) {
            return Optional.empty();
        }

        Stage stage = new Stage();
        stage.initOwner(root.getScene().getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        Label title = new Label("Describe the avatar you want");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: 800;");
        Label subtitle = new Label("Pollinations AI will use your prompt and the result will be saved like a normal profile image.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.72); -fx-font-size: 13px;");

        TextArea area = new TextArea("professional portrait, clean background, soft lighting");
        area.setWrapText(true);
        area.setPrefRowCount(5);
        area.setStyle(
            HorizonDesignSystem.input() +
            "-fx-control-inner-background: " + surfaceCard() + ";"
        );

        Button generateBtn = new Button("Generate");
        generateBtn.setStyle(fancyAvatarPrimaryButtonStyle());
        HorizonDesignSystem.installButtonMotion(generateBtn);
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(fancyAvatarGhostButtonStyle());
        HorizonDesignSystem.installButtonMotion(cancelBtn);

        HBox actions = new HBox(8, generateBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(12, title, subtitle, area, actions);
        content.setStyle(HorizonDesignSystem.webSectionCard(18, false) + "-fx-padding: 14;");
        HorizonDesignSystem.popIn(content);

        AtomicReference<String> resultRef = new AtomicReference<>(null);
        generateBtn.setOnAction(e -> { resultRef.set(safePrompt(area.getText())); stage.close(); });
        cancelBtn.setOnAction(e -> { resultRef.set(null); stage.close(); });

        Scene scene = new Scene(content);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.showAndWait();

        String out = resultRef.get();
        if (out == null) return Optional.empty();
        if (out.isBlank()) return Optional.of("");
        return Optional.of(out);
    }

    private boolean applyAvatarFill(Circle avatarCircle) {
        Profile profile = currentProfile();
        String avatarPath = profile == null ? null : profile.getAvatar();
        if (avatarPath != null && !avatarPath.isBlank()) {
            Image img = ImageLoaderUtil.loadProfileAvatar(avatarPath, false);
            if (img != null && !img.isError()) {
                // Ensure image is actually loaded to prevent IllegalArgumentException in ImagePattern
                if (img.getProgress() == 1.0) {
                    avatarCircle.setFill(new ImagePattern(img));
                    return true;
                } else {
                    // If not loaded yet (despite sync flag), use a listener to apply it once ready
                    img.progressProperty().addListener((obs, old, progress) -> {
                        if (progress.doubleValue() == 1.0) {
                            javafx.application.Platform.runLater(() -> avatarCircle.setFill(new ImagePattern(img)));
                        }
                    });
                }
            }
        }
        avatarCircle.setFill(Color.web(tm.getAccentHex()));
        return false;
    }

    private Profile currentProfile() {
        SessionManager session = SessionManager.getInstance();
        Profile profile = session.getCurrentProfile();
        if (profile != null) {
            return profile;
        }

        User user = session.getCurrentUser();
        if (user == null || user.getIdUser() == null) {
            return null;
        }

        ProfileService profileService = new ProfileService();
        profile = profileService.findOneByUserId(user.getIdUser()).orElse(null);
        if (profile != null) {
            session.setCurrentProfile(profile);
        }
        return profile;
    }

    private Profile getOrCreateCurrentProfile(User user) {
        Profile existing = currentProfile();
        if (existing != null) {
            return existing;
        }

        ProfileService profileService = new ProfileService();
        Profile created = new Profile();
        created.setUserId(user.getIdUser());
        created.setAvatar(null);
        created.setLocale("en");
        created.setTheme(0);
        created.setTimezone(1);
        created.setDescriptionProfile("");
        created.setSettingsJson("{}");

        Optional<Integer> id = profileService.createProfile(created);
        if (id.isEmpty()) {
            return null;
        }

        Profile loaded = profileService.findById(id.get()).orElse(null);
        if (loaded != null) {
            SessionManager.getInstance().setCurrentProfile(loaded);
        }
        return loaded;
    }

    private String currentInitial(User user) {
        if (user != null) {
            String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
            String last = user.getLastName() == null ? "" : user.getLastName().trim();
            String fullName = (first + " " + last).trim();
            if (!fullName.isBlank()) {
                return fullName.substring(0, 1).toUpperCase();
            }
            String email = user.getEmailUser() == null ? "" : user.getEmailUser().trim();
            if (!email.isBlank()) {
                return email.substring(0, 1).toUpperCase();
            }
        }
        return "U";
    }

    private void showAvatarAlert(Alert.AlertType type, String title, String message) {
        if (type == Alert.AlertType.ERROR) {
            GlobalNotificationPillManager.expandedError(
                title,
                message,
                "Review the fields you just changed.",
                "Retry the action after correcting the input.",
                "If the problem persists, reopen this page and try again."
            );
        } else if (type == Alert.AlertType.WARNING) {
            GlobalNotificationPillManager.validationIssue(title, message, "Double-check the current input before continuing.");
        } else {
            GlobalNotificationPillManager.success(title, message);
        }
    }

    private Dialog<ButtonType> styledAvatarDialog(String title) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            dialog.initOwner(root.getScene().getWindow());
        }

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle(HorizonDesignSystem.webSectionCard(22, false) + "-fx-padding: 18;");
        return dialog;
    }

    private String fancyAvatarPrimaryButtonStyle() {
        return HorizonDesignSystem.buttonPrimary() + "-fx-background-radius:999px;-fx-border-radius:999px;-fx-padding: 10 18 10 18;";
    }

    private String fancyAvatarSecondaryButtonStyle() {
        return HorizonDesignSystem.buttonGhost() + "-fx-background-radius:999px;-fx-border-radius:999px;-fx-padding: 10 18 10 18;";
    }

    private String fancyAvatarGhostButtonStyle() {
        return HorizonDesignSystem.buttonGhost() + "-fx-background-color:transparent;-fx-background-radius:999px;-fx-border-radius:999px;-fx-padding: 10 18 10 18;";
    }

    private String safePrompt(String text) {
        return text == null ? "" : text.trim();
    }

    private boolean matchesPassword(String raw, String stored) {
        if (stored == null || stored.isBlank()) {
            return false;
        }

        try {
            if (stored.startsWith("$2y$")) {
                return BCrypt.checkpw(raw, "$2a$" + stored.substring(4));
            }
            if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
                return BCrypt.checkpw(raw, stored);
            }
        } catch (IllegalArgumentException ignored) {
            return false;
        }

        return raw.equals(stored);
    }

    private String themeFromCode(Integer themeCode) {
        if (themeCode == null) {
            return "Dark";
        }
        return switch (themeCode) {
            case 1 -> "Dark";
            case 2 -> "Auto";
            default -> "Light";
        };
    }

    private Integer themeToCode(String themeValue) {
        if (themeValue == null) {
            return 1;
        }
        return switch (themeValue) {
            case "Light" -> 0;
            case "Auto" -> 2;
            default -> 1;
        };
    }

    private String languageFromLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "English";
        }
        String normalized = locale.toLowerCase();
        return switch (normalized) {
            case "fr", "fr_fr" -> "French";
            case "ar", "ar_tn" -> "Arabic";
            case "es", "es_es" -> "Spanish";
            default -> "English";
        };
    }

    private String languageToLocale(String language) {
        if (language == null) {
            return "en";
        }
        return switch (language) {
            case "French" -> "fr";
            case "Arabic" -> "ar";
            case "Spanish" -> "es";
            default -> "en";
        };
    }

    private Integer parseTimezone(String input) {
        if (input == null || input.isBlank()) {
            return 1;
        }
        String normalized = input.trim().toUpperCase().replace("UTC", "").replace("GMT", "").trim();
        if (normalized.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    private String timezoneText(Integer tz) {
        if (tz == null) {
            return "UTC+1";
        }
        if (tz == 0) {
            return "UTC";
        }
        return "UTC" + (tz > 0 ? "+" : "") + tz;
    }

    private void installXpActionTracking(javafx.scene.Parent parent) {
        parent.addEventFilter(ActionEvent.ACTION, event -> {
            Object source = event.getSource();
            if (source instanceof Button) {
                awardInteractionXp(1);
            }
        });
    }

    private void awardInteractionXp(int xp) {
        NavigationManager.getInstance().awardInteractionXp(Math.max(0, xp));
    }

    private void animateCircleFace(javafx.scene.Node from, javafx.scene.Node to) {
        if (from == to || to.isVisible()) {
            return;
        }

        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(150), from);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            from.setVisible(false);
            from.setManaged(false);

            to.setVisible(true);
            to.setManaged(true);
            to.setOpacity(0);

            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(180), to);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void animateBadgePulse(Text badge) {
        javafx.animation.ScaleTransition pulse = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(180), badge);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private StackPane circleAvatarNode(User user, String fallbackDisplay, double size, int fontSize) {
        Circle circle = new Circle(size / 2.0);
        circle.setFill(Color.web(tm.toRgba(tm.getAccentHex(), 0.20)));
        circle.setStroke(Color.web(tm.toRgba(tm.getAccentHex(), 0.34)));
        circle.setStrokeWidth(1.4);

        String initial = fallbackDisplay == null || fallbackDisplay.isBlank()
            ? "U"
            : fallbackDisplay.substring(0, 1).toUpperCase();
        Text initialText = text(initial, fontSize, true, "#ffffff");

        boolean hasAvatar = applyAvatarFillForUser(circle, user);
        initialText.setVisible(!hasAvatar);
        initialText.setManaged(!hasAvatar);

        StackPane avatar = new StackPane(circle, initialText);
        avatar.setMinSize(size, size);
        avatar.setPrefSize(size, size);
        return avatar;
    }

    private boolean applyAvatarFillForUser(Circle avatarCircle, User user) {
        if (user == null || user.getIdUser() == null) {
            return false;
        }

        ProfileService profileService = new ProfileService();
        Profile userProfile = profileService.findOneByUserId(user.getIdUser()).orElse(null);
        String avatarPath = userProfile == null ? null : userProfile.getAvatar();
        if (avatarPath == null || avatarPath.isBlank()) {
            return false;
        }

        Image img = ImageLoaderUtil.loadProfileAvatar(avatarPath, false);
        if (img == null || img.isError()) {
            return false;
        }
        avatarCircle.setFill(new ImagePattern(img));
        return true;
    }

    private VBox standingMetricChip(String label, String value) {
        VBox chip = new VBox(2, text(label, 9, true, "rgba(255,255,255,0.42)"), text(value, 12, true, "#ffffff"));
        chip.setPadding(new Insets(8, 12, 8, 12));
        chip.setMinWidth(74);
        chip.setStyle("-fx-background-color: rgba(255,255,255,0.055);" +
            "-fx-background-radius: 14px;" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.18) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 14px;");
        return chip;
    }

    private Text xpTick(String label) {
        return text(label, 9, true, "rgba(255,255,255,0.38)");
    }

    private javafx.scene.layout.Region flexibleTickSpace() {
        javafx.scene.layout.Region region = new javafx.scene.layout.Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }
    private VBox standingPoint(String label, boolean active) {
        VBox point = new VBox(6);
        point.setAlignment(Pos.CENTER);
        Circle dot = new Circle(10);
        dot.setFill(active ? Color.web(tm.getAccentHex()) : Color.web("#2f3136"));
        if (active) {
            dot.setEffect(new javafx.scene.effect.DropShadow(18, Color.web(tm.toRgba(tm.getAccentHex(), 0.72))));
            installBreathingGlow(dot, 0.18);
        }
        Text txt = text(label, 11, true, active ? "#ffffff" : textMuted());
        point.getChildren().addAll(dot, txt);
        return point;
    }

    private VBox friendCard(String name) {
        VBox c = new VBox(8);
        c.setAlignment(Pos.CENTER);
        StackPane avatar = new StackPane();
        avatar.setMinSize(60, 60);
        avatar.setStyle(shell(16, "rgba(10,10,15,0.80)", 0.10));
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
        String background = active ? tm.getEffectiveAccentGradient() : surfaceSoft();
        String textFill = active ? "white" : textMuted();
        b.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-text-fill: " + textFill + ";" +
            "-fx-font-weight: 800; -fx-background-radius: 12px; -fx-padding: 8 12 8 12;"
        );
        return b;
    }

    private Text badge(String label) {
        return text(label, 10, true, "#ffffff");
    }

    private void buildTwoFactorPanel(VBox content) {
        User user = SessionManager.getInstance().getCurrentUser();
        TwoFactorService twoFactorService = new TwoFactorService();
        
        content.getChildren().add(text("Two-Factor Authentication", 13, true, textSoft()));
        content.getChildren().add(text("Secure your account with time-based one-time passwords.", 11, false, textMuted()));
        content.getChildren().add(text(" ", 4, false, "transparent"));
        
        // Check if TOTP is already configured
        boolean totpConfigured = twoFactorService.isTotpConfigured();
        
        if (totpConfigured) {
            // Show configured status
            HBox statusBox = new HBox(10);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            statusBox.setPadding(new Insets(12));
            statusBox.setStyle(shell(12, tm.toRgba(tm.getAccentHex(), 0.10), 0.15));
            
            Text statusIcon = text("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œ", 18, true, tm.getAccentHex());
            VBox statusInfo = new VBox(3,
                text("Authenticator App Configured", 12, true, "#ffffff"),
                text("Your account is protected with TOTP", 11, false, textMuted())
            );
            HBox.setHgrow(statusInfo, Priority.ALWAYS);
            
            Button removeBtn = new Button("Remove TOTP");
            removeBtn.setStyle(
                "-fx-padding: 8 12 8 12;" +
                "-fx-background-color: rgba(255,0,0,0.15);" +
                "-fx-text-fill: #ff6b6b; -fx-font-weight: 800;" +
                "-fx-background-radius: 10; -fx-cursor: hand;"
            );
            removeBtn.setOnAction(e -> {
                try {
                    twoFactorService.disableTotpForUser();
                    showAvatarAlert(Alert.AlertType.INFORMATION, "2FA", "TOTP disabled successfully.");
                    // Refresh the panel to show updated state
                    content.getChildren().clear();
                    buildTwoFactorPanel(content);
                } catch (Exception ex) {
                    System.out.println("Error disabling TOTP: " + ex.getMessage());
                    ex.printStackTrace();
                    showAvatarAlert(Alert.AlertType.ERROR, "2FA", "Failed to disable TOTP: " + ex.getMessage());
                }
            });
            
            statusBox.getChildren().addAll(statusIcon, statusInfo, removeBtn);
            content.getChildren().add(statusBox);
        } else {
            // Show setup flow
            HBox statusBox = new HBox(10);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            statusBox.setPadding(new Insets(12));
            statusBox.setStyle(shell(12, "rgba(255,255,255,0.05)", 0.12));
            
            Text statusIcon = text("ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬ÂÃƒâ€šÃ‚Â¯", 18, true, "rgba(255,255,255,0.50)");
            VBox statusInfo = new VBox(3,
                text("Authenticator App Setup", 12, true, "#ffffff"),
                text("Set up time-based one-time passwords", 11, false, textMuted())
            );
            HBox.setHgrow(statusInfo, Priority.ALWAYS);
            
            statusBox.getChildren().addAll(statusIcon, statusInfo);
            content.getChildren().add(statusBox);
            content.getChildren().add(text(" ", 8, false, "transparent"));
            
            // Setup instructions
            content.getChildren().add(text("Step 1: Generate Secret", 12, true, "rgba(255,255,255,0.88)"));
            content.getChildren().add(text("Click the button below to generate a secret code. Save it in a secure location.", 11, false, textMuted()));
            
            javafx.scene.control.Button generateBtn = new javafx.scene.control.Button("Generate Secret");
            generateBtn.setStyle(profilePrimaryButtonStyle(10));
            HorizonDesignSystem.installButtonMotion(generateBtn);
            content.getChildren().add(generateBtn);
            content.getChildren().add(text(" ", 8, false, "transparent"));
            
            // QR and secret display (initially hidden)
            VBox setupBox = new VBox(12);
            setupBox.setPadding(new Insets(14));
            setupBox.setStyle(shell(14, "rgba(255,255,255,0.04)", 0.11));
            setupBox.setVisible(false);
            setupBox.setManaged(false);
            
            StackPane qrPane = new StackPane();
            qrPane.setMinSize(140, 140);
            qrPane.setMaxSize(140, 140);
            qrPane.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
            qrPane.getChildren().add(text("QR Code", 10, false, "black"));
            
            javafx.scene.control.PasswordField maskedSecretField = new javafx.scene.control.PasswordField();
            maskedSecretField.setEditable(false);
            maskedSecretField.setPromptText("Secret key will appear here");
            maskedSecretField.setStyle(profileInputStyle());

            javafx.scene.control.TextField visibleSecretField = new javafx.scene.control.TextField();
            visibleSecretField.setEditable(false);
            visibleSecretField.setPromptText("Secret key will appear here");
            visibleSecretField.setStyle(profileInputStyle());
            visibleSecretField.setVisible(false);
            visibleSecretField.setManaged(false);

            javafx.scene.control.Button toggleSecretBtn = new javafx.scene.control.Button("Show");
            toggleSecretBtn.setStyle(profileGhostButtonStyle(8) + "-fx-padding: 6 10 6 10;");
            HorizonDesignSystem.installButtonMotion(toggleSecretBtn);
            HBox secretRow = new HBox(8, maskedSecretField, visibleSecretField, toggleSecretBtn);
            secretRow.setAlignment(Pos.CENTER);
            HBox.setHgrow(maskedSecretField, Priority.ALWAYS);
            HBox.setHgrow(visibleSecretField, Priority.ALWAYS);

            toggleSecretBtn.setOnAction(evt -> {
                boolean showing = visibleSecretField.isVisible();
                if (showing) {
                    // switch to masked
                    visibleSecretField.setVisible(false);
                    visibleSecretField.setManaged(false);
                    maskedSecretField.setVisible(true);
                    maskedSecretField.setManaged(true);
                    maskedSecretField.setText(visibleSecretField.getText());
                    toggleSecretBtn.setText("Show");
                } else {
                    visibleSecretField.setText(maskedSecretField.getText());
                    visibleSecretField.setVisible(true);
                    visibleSecretField.setManaged(true);
                    maskedSecretField.setVisible(false);
                    maskedSecretField.setManaged(false);
                    toggleSecretBtn.setText("Hide");
                }
            });

            content.getChildren().add(setupBox);
            VBox qrSection = new VBox(8);
            qrSection.setAlignment(Pos.CENTER);
            qrSection.getChildren().add(text("Step 2: Scan QR Code", 12, true, "rgba(255,255,255,0.88)"));
            qrSection.getChildren().add(qrPane);
            qrSection.getChildren().add(text("Or enter this code manually:", 11, false, textMuted()));
            qrSection.getChildren().add(secretRow);
            setupBox.getChildren().add(qrSection);
            
            setupBox.getChildren().add(text(" ", 8, false, "transparent"));
            setupBox.getChildren().add(text("Step 3: Verify Code", 12, true, "rgba(255,255,255,0.88)"));
            setupBox.getChildren().add(text("Enter a 6-digit code from your authenticator app:", 11, false, textMuted()));
            
            javafx.scene.control.TextField codeField = new javafx.scene.control.TextField();
            codeField.setPromptText("000000");
            codeField.setMaxWidth(120);
            codeField.setStyle(profileInputStyle() + "-fx-font-size: 13px;");
            
            javafx.scene.control.Button verifyBtn = new javafx.scene.control.Button("Verify & Activate");
            verifyBtn.setStyle(profilePrimaryButtonStyle(10));
            HorizonDesignSystem.installButtonMotion(verifyBtn);
            
            HBox codeRow = new HBox(10);
            codeRow.setAlignment(Pos.CENTER_LEFT);
            codeRow.getChildren().addAll(codeField, verifyBtn);
            setupBox.getChildren().add(codeRow);
            
            // Generate button action
            generateBtn.setOnAction(e -> {
                String secret = twoFactorService.generateTotpSecret();
                String email = user != null ? user.getEmailUser() : "user@syndicati.tn";
                String qrDataUrl = twoFactorService.generateQRCodeDataUrl(secret, email, "Syndicati");

                // set secret into both masked and visible fields
                maskedSecretField.setText(secret);
                visibleSecretField.setText(secret);
                visibleSecretField.setVisible(false);
                visibleSecretField.setManaged(false);
                maskedSecretField.setVisible(true);
                maskedSecretField.setManaged(true);

                qrPane.getChildren().clear();
                if (qrDataUrl != null && qrDataUrl.startsWith("data:image/png;base64,")) {
                    javafx.scene.image.Image qrImage = new javafx.scene.image.Image(qrDataUrl);
                    javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(qrImage);
                    iv.setFitWidth(140);
                    iv.setFitHeight(140);
                    iv.setPreserveRatio(true);
                    qrPane.getChildren().add(iv);
                } else {
                    qrPane.getChildren().add(text("QR Generated", 10, true, "black"));
                }
                
                setupBox.setVisible(true);
                setupBox.setManaged(true);
                generateBtn.setDisable(true);
            });
            
            // Verify button action
            verifyBtn.setOnAction(e -> {
                String code = codeField.getText();
                if (code == null || code.trim().isEmpty() || code.length() != 6) {
                    showAvatarAlert(Alert.AlertType.ERROR, "2FA", "Please enter a valid 6-digit code.");
                    return;
                }
                
                String secret = maskedSecretField.isVisible() ? maskedSecretField.getText() : visibleSecretField.getText();
                if (secret == null || secret.isEmpty()) {
                    showAvatarAlert(Alert.AlertType.ERROR, "2FA", "Please generate a secret first.");
                    return;
                }
                
                boolean valid = twoFactorService.verifyTotpCode(secret, code);
                if (!valid) {
                    showAvatarAlert(Alert.AlertType.ERROR, "2FA", "Invalid code. Please try again.");
                    return;
                }
                
                try {
                    twoFactorService.enableTotpForUser(secret);
                    showAvatarAlert(Alert.AlertType.INFORMATION, "2FA", "TOTP activated successfully! Your account is now protected.");
                    // Refresh the panel to show updated state
                    content.getChildren().clear();
                    buildTwoFactorPanel(content);
                } catch (Exception ex) {
                    System.out.println("Error enabling TOTP: " + ex.getMessage());
                    ex.printStackTrace();
                    showAvatarAlert(Alert.AlertType.ERROR, "2FA", "Failed to enable TOTP: " + ex.getMessage());
                }
            });
        }
    }
    
    private void buildBiometricsPanel(VBox content) {
        User user = SessionManager.getInstance().getCurrentUser();
        BiometricsService biometricsService = new BiometricsService();
        
        content.getChildren().add(text("Biometric Authentication", 13, true, textSoft()));
        content.getChildren().add(text("Use your device's biometrics to log in faster and more securely.", 11, false, textMuted()));
        content.getChildren().add(text(" ", 8, false, "transparent"));
        
        // Check Windows Hello availability
        boolean windowsHelloAvailable = biometricsService.isWindowsHelloAvailable();
        
        if (!windowsHelloAvailable) {
            HBox warningBox = new HBox(10);
            warningBox.setAlignment(Pos.CENTER_LEFT);
            warningBox.setPadding(new Insets(12));
            warningBox.setStyle(shell(12, "rgba(255,150,0,0.10)", 0.18));
            
            Text warningIcon = text("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã‚Â¡Ãƒâ€šÃ‚Â ", 16, true, "#ffa600");
            Text warningText = text("Windows Hello is not available on this device.", 11, false, "#ffffff");
            warningBox.getChildren().addAll(warningIcon, warningText);
            content.getChildren().add(warningBox);
            return;
        }
        
        // Registered credentials list
        content.getChildren().add(text("Registered Devices", 12, true, "rgba(255,255,255,0.88)"));
        
        VBox credentialsList = new VBox(10);
        credentialsList.setStyle("-fx-background-color: transparent;");
        
        List<BiometricsService.BiometricCredential> credentials = biometricsService.listCredentials();
        if (credentials.isEmpty()) {
            credentialsList.getChildren().add(text("No devices registered yet.", 11, false, textMuted()));
        } else {
            for (BiometricsService.BiometricCredential cred : credentials) {
                HBox credRow = new HBox(10);
                credRow.setAlignment(Pos.CENTER_LEFT);
                credRow.setPadding(new Insets(10));
                credRow.setStyle(shell(10, "rgba(255,255,255,0.04)", 0.10));
                
                VBox credInfo = new VBox(2);
                credInfo.getChildren().addAll(
                    text(cred.getDeviceName(), 11, true, "#ffffff"),
                    text(cred.getDeviceType() + " | Created: " + cred.getCreatedAt(), 10, false, textMuted())
                );
                HBox.setHgrow(credInfo, Priority.ALWAYS);
                
                javafx.scene.control.Button removeBtn = new javafx.scene.control.Button("Remove");
                removeBtn.setStyle(
                    "-fx-padding: 6 10 6 10;" +
                    "-fx-background-color: rgba(255,100,100,0.15);" +
                    "-fx-text-fill: #ff6464; -fx-font-weight: 800;" +
                    "-fx-background-radius: 8; -fx-cursor: hand;"
                );
                
                BiometricsService.BiometricCredential finalCred = cred;
                removeBtn.setOnAction(e -> {
                    biometricsService.removeCredential(finalCred.getId());
                    credentialsList.getChildren().remove(credRow);
                    showAvatarAlert(Alert.AlertType.INFORMATION, "Biometrics", "Device removed successfully.");
                });
                
                credRow.getChildren().addAll(credInfo, removeBtn);
                credentialsList.getChildren().add(credRow);
            }
        }
        
        content.getChildren().add(credentialsList);
        content.getChildren().add(text(" ", 8, false, "transparent"));
        
        // Register new device
        content.getChildren().add(text("Register New Device", 12, true, "rgba(255,255,255,0.88)"));
        
        javafx.scene.control.TextField deviceNameField = new javafx.scene.control.TextField();
        deviceNameField.setPromptText("e.g., My Laptop, Windows PC");
        deviceNameField.setStyle(profileInputStyle());
        content.getChildren().add(deviceNameField);
        
        javafx.scene.control.Button registerBtn = new javafx.scene.control.Button("Register Device");
        registerBtn.setStyle(profilePrimaryButtonStyle(10));
        HorizonDesignSystem.installButtonMotion(registerBtn);
        registerBtn.setOnAction(e -> {
            String deviceName = deviceNameField.getText();
            if (deviceName == null || deviceName.trim().isEmpty()) {
                showAvatarAlert(Alert.AlertType.ERROR, "Biometrics", "Please enter a device name.");
                return;
            }
            
            biometricsService.registerWindowsHello(deviceName);
            showAvatarAlert(Alert.AlertType.INFORMATION, "Biometrics", "Device registered successfully. You can now use Windows Hello to log in.");
            deviceNameField.clear();
            // Refresh credentials list
            content.getChildren().clear();
            buildBiometricsPanel(content);
        });
        content.getChildren().add(registerBtn);
    }
    
    private void buildFaceIDPanel(VBox content) {
        User user = SessionManager.getInstance().getCurrentUser();
        FaceIDService faceIDService = new FaceIDService();
        CameraController cameraController = new CameraController();
        
        content.getChildren().add(text("Face ID Authentication", 13, true, textSoft()));
        content.getChildren().add(text("Enroll your face for secure, local biometric authentication.", 11, false, textMuted()));
        content.getChildren().add(text(" ", 8, false, "transparent"));
        
        boolean faceIDEnrolled = faceIDService.isFaceIDEnrolled();
        
        if (faceIDEnrolled) {
            // Show enrolled status
            HBox statusBox = new HBox(10);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            statusBox.setPadding(new Insets(12));
            statusBox.setStyle(shell(12, tm.toRgba(tm.getAccentHex(), 0.10), 0.15));
            
            Text statusIcon = text("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œ", 18, true, tm.getAccentHex());
            VBox statusInfo = new VBox(3,
                text("Face ID Enrolled", 12, true, "#ffffff"),
                text("Your face is securely enrolled and encrypted locally", 11, false, textMuted())
            );
            HBox.setHgrow(statusInfo, Priority.ALWAYS);
            
            javafx.scene.control.Button removeBtn = new javafx.scene.control.Button("Remove Face ID");
            removeBtn.setStyle(
                "-fx-padding: 8 12 8 12;" +
                "-fx-background-color: rgba(255,0,0,0.15);" +
                "-fx-text-fill: #ff6b6b; -fx-font-weight: 800;" +
                "-fx-background-radius: 10; -fx-cursor: hand;"
            );
            removeBtn.setOnAction(e -> {
                faceIDService.removeFaceIDEnrollment();
                showAvatarAlert(Alert.AlertType.INFORMATION, "Face ID", "Face ID enrollment removed.");
                content.getChildren().clear();
                buildFaceIDPanel(content);
            });
            
            statusBox.getChildren().addAll(statusIcon, statusInfo, removeBtn);
            content.getChildren().add(statusBox);
        } else {
            // Show enrollment setup
            HBox statusBox = new HBox(10);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            statusBox.setPadding(new Insets(12));
            statusBox.setStyle(shell(12, "rgba(255,255,255,0.05)", 0.12));
            
            Text statusIcon = text("ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã¢â‚¬ÂÃƒâ€šÃ‚Â¯", 18, true, "rgba(255,255,255,0.50)");
            VBox statusInfo = new VBox(3,
                text("Face ID Setup", 12, true, "#ffffff"),
                text("Enroll your face for authentication", 11, false, textMuted())
            );
            HBox.setHgrow(statusInfo, Priority.ALWAYS);
            
            statusBox.getChildren().addAll(statusIcon, statusInfo);
            content.getChildren().add(statusBox);
            content.getChildren().add(text(" ", 8, false, "transparent"));
            
            // PIN setup
            content.getChildren().add(text("Step 1: Set PIN", 12, true, "rgba(255,255,255,0.88)"));
            content.getChildren().add(text("Your face encoding will be encrypted with this PIN. This PIN cannot be recovered.", 11, false, textMuted()));
            
            javafx.scene.control.PasswordField pinField = new javafx.scene.control.PasswordField();
            pinField.setPromptText("4-8 digit PIN");
            pinField.setMaxWidth(150);
            pinField.setStyle(profileInputStyle());
            content.getChildren().add(pinField);
            content.getChildren().add(text(" ", 8, false, "transparent"));
            
            // Enrollment button
            javafx.scene.control.Button startEnrollBtn = new javafx.scene.control.Button("Start Enrollment");
            startEnrollBtn.setStyle(profilePrimaryButtonStyle(10));
            HorizonDesignSystem.installButtonMotion(startEnrollBtn);
            
            VBox enrollmentBox = new VBox(12);
            enrollmentBox.setPadding(new Insets(14));
            enrollmentBox.setStyle(shell(14, "rgba(255,255,255,0.04)", 0.11));
            enrollmentBox.setVisible(false);
            enrollmentBox.setManaged(false);
            
            // Enrollment video preview area
            javafx.scene.image.ImageView videoPreview = new javafx.scene.image.ImageView();
            videoPreview.setFitWidth(280);
            videoPreview.setFitHeight(210);
            videoPreview.setPreserveRatio(true);
            StackPane videoPreviewPane = new StackPane(videoPreview);
            videoPreviewPane.setMinSize(280, 210);
            videoPreviewPane.setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 12;");
            
            // Progress bar
            javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar(0);
            progressBar.setStyle("-fx-padding: 0;");
            
            // Enrollment hint
            Text enrollmentHint = text("Position your face in the frame and look directly at the camera.", 11, false, textMuted());
            
            // Start camera capture button
            javafx.scene.control.Button startCaptureBtn = new javafx.scene.control.Button("Start Camera");
            startCaptureBtn.setStyle(profileGhostButtonStyle(10));
            HorizonDesignSystem.installButtonMotion(startCaptureBtn);
            
            javafx.scene.control.Button completeEnrollBtn = new javafx.scene.control.Button("Complete Enrollment");
            completeEnrollBtn.setStyle(profilePrimaryButtonStyle(10));
            HorizonDesignSystem.installButtonMotion(completeEnrollBtn);
            completeEnrollBtn.setDisable(true);
            
            enrollmentBox.getChildren().addAll(
                text("Step 2: Capture Face Frames", 12, true, "rgba(255,255,255,0.88)"),
                videoPreviewPane,
                enrollmentHint,
                progressBar,
                startCaptureBtn
            );
            
            content.getChildren().add(startEnrollBtn);
            content.getChildren().add(enrollmentBox);
            
            // Start enrollment button action
            startEnrollBtn.setOnAction(e -> {
                String pin = pinField.getText();
                if (pin == null || pin.isEmpty() || pin.length() < 4 || pin.length() > 8) {
                    showAvatarAlert(Alert.AlertType.ERROR, "Face ID", "PIN must be 4-8 digits.");
                    return;
                }
                
                faceIDService.startEnrollment();
                enrollmentBox.setVisible(true);
                enrollmentBox.setManaged(true);
                startEnrollBtn.setDisable(true);
                pinField.setDisable(true);
            });
            
            // Start camera capture
            startCaptureBtn.setOnAction(e -> {
                startCaptureBtn.setDisable(true);
                startCaptureBtn.setText("ÃƒÆ’Ã‚Â°Ãƒâ€¦Ã‚Â¸ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒâ€šÃ‚Â· Capturing...");
                startCaptureBtn.setText("\u25CF Capturing...");
                
                Thread.ofVirtual().name("ProfileFaceID-Camera").start(() -> {
                    try {
                        RealCameraService cameraService = cameraController.getOrCreate(null);
                        if (!cameraController.initializeDefaultCamera(cameraService)) {
                            Platform.runLater(() -> {
                                showAvatarAlert(Alert.AlertType.ERROR, "Face ID", "Failed to access camera. Check console for details.");
                                startCaptureBtn.setDisable(false);
                                startCaptureBtn.setText("Start Camera");
                            });
                            return;
                        }
                        
                        cameraService.startCapture();
                        Thread.sleep(1500);
                        
                        // Verify camera is working
                        javafx.scene.image.Image testFrame = cameraService.getCurrentFrameWithDetection();
                        if (testFrame == null) {
                            Platform.runLater(() -> {
                                showAvatarAlert(Alert.AlertType.ERROR, "Face ID", "Camera not responding. Try reconnecting.");
                                startCaptureBtn.setDisable(false);
                                startCaptureBtn.setText("Start Camera");
                            });
                            return;
                        }
                        
                        // Start preview animation
                        javafx.animation.AnimationTimer previewTimer = new javafx.animation.AnimationTimer() {
                            @Override
                            public void handle(long now) {
                                javafx.scene.image.Image frame = cameraService.getCurrentFrameWithDetection();
                                if (frame != null) {
                                    videoPreview.setImage(frame);
                                }
                            }
                        };
                        previewTimer.start();
                        
                        Platform.runLater(() -> {
                            enrollmentHint.setText("Position your face in the frame...");
                        });
                        
                        // Capture frames
                        final int[] framesCaptured = {0};
                        final int targetFrames = 20;
                        long startTime = System.currentTimeMillis();
                        long timeout = 20000;
                        
                        faceIDService.clearFrameHistory();
                        
                        while (framesCaptured[0] < targetFrames && (System.currentTimeMillis() - startTime) < timeout) {
                            RealCameraService.FaceQuality quality = cameraService.assessFrameQuality();
                            
                            final int currentCount = framesCaptured[0];
                            Platform.runLater(() -> {
                                enrollmentHint.setText(quality.message + " (" + currentCount + "/" + targetFrames + ")");
                                progressBar.setProgress((double) currentCount / targetFrames);
                            });
                            
                            if (quality.isGood) {
                                RealCameraService.FaceFrameData frameData = cameraService.captureFrame();
                                if (frameData != null) {
                                    // Get real embedding from InsightFace via FaceIDService
                                    InsightFaceService.FaceMesh mesh = insightFaceService.processFaceFrame(frameData.frameImage, true);
                                    if (mesh != null && mesh.detected && mesh.embedding != null) {
                                        faceIDService.addFrameEmbedding(mesh.embedding);
                                        framesCaptured[0]++;
                                    } else {
                                        // If InsightFace fails, we can't enroll a valid face
                                        // But we'll try again in the next frame
                                    }
                                }
                            }
                            
                            Thread.sleep(150);
                        }
                        
                        cameraService.stopCapture();
                        previewTimer.stop();
                        
                        if (framesCaptured[0] < targetFrames) {
                            Platform.runLater(() -> {
                                showAvatarAlert(Alert.AlertType.ERROR, "Face ID", "Not enough frames captured. Try again.");
                                startCaptureBtn.setDisable(false);
                                startCaptureBtn.setText("Start Camera");
                            });
                            return;
                        }
                        
                        Platform.runLater(() -> {
                            enrollmentHint.setText("Face capture complete! " + framesCaptured[0] + " frames captured.");
                            progressBar.setProgress(1.0);
                            startCaptureBtn.setDisable(true);
                            startCaptureBtn.setText("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œ Frames Captured");
                            startCaptureBtn.setText("\u2713 Frames Captured");
                            
                            if (!enrollmentBox.getChildren().contains(completeEnrollBtn)) {
                                enrollmentBox.getChildren().add(completeEnrollBtn);
                            }
                            completeEnrollBtn.setDisable(false);
                        });
                        
                    } catch (Exception ex) {
                        System.err.println("Face ID enrollment camera error: " + ex.getMessage());
                        ex.printStackTrace();
                        Platform.runLater(() -> {
                            showAvatarAlert(Alert.AlertType.ERROR, "Face ID", "Camera error: " + ex.getMessage());
                            startCaptureBtn.setDisable(false);
                            startCaptureBtn.setText("Start Camera");
                        });
                    }
                });
            });
            
            // Complete enrollment action
            completeEnrollBtn.setOnAction(e -> {
                String pin = pinField.getText();
                boolean success = faceIDService.completeEnrollment(pin);
                if (!success) {
                    showAvatarAlert(Alert.AlertType.ERROR, "Face ID", "Failed to complete enrollment.");
                    return;
                }
                
                showAvatarAlert(Alert.AlertType.INFORMATION, "Face ID", "Face ID enrollment completed successfully!");
                content.getChildren().clear();
                buildFaceIDPanel(content);
            });
        }
    }

    private VBox cardShell() {
        VBox c = new VBox(16);
        c.setStyle(HorizonDesignSystem.webProfileCard());
        HorizonDesignSystem.installLift(c, 1.006, -2);
        return c;
    }

    private String shell(double radius, String bg, double borderOpacity) {
        if (radius >= 100) {
            return "-fx-background-color:" + HorizonDesignSystem.surfaceSoft() + ";" +
                "-fx-background-radius:999px;" +
                "-fx-border-color:" + HorizonDesignSystem.borderStrong() + ";" +
                "-fx-border-width:1;" +
                "-fx-border-radius:999px;";
        }
        return HorizonDesignSystem.webSectionCard((int) Math.round(radius), false);
    }

    private String profileInputStyle() {
        return HorizonDesignSystem.input() +
            "-fx-padding: 9 12 9 12;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-radius: 12px;";
    }

    private String profilePrimaryButtonStyle(int radius) {
        return HorizonDesignSystem.buttonPrimary() +
            "-fx-padding: 11 16 11 16;" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-radius: " + radius + "px;" +
            "-fx-font-weight: 800;";
    }

    private String profileGhostButtonStyle(int radius) {
        return HorizonDesignSystem.buttonGhost() +
            "-fx-padding: 9 14 9 14;" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-radius: " + radius + "px;" +
            "-fx-font-weight: 800;";
    }

    private String surfaceCard() {
        return tm.isDarkMode() ? "rgba(0,0,0,0.86)" : "rgba(255,255,255,0.96)";
    }

    private String surfaceSoft() {
        return HorizonDesignSystem.surfaceSoft();
    }

    private String borderSoft() {
        return HorizonDesignSystem.borderStrong();
    }

    private String textSoft() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.93)" : "rgba(15,23,42,0.90)";
    }

    private String textMuted() {
        return HorizonDesignSystem.mutedText();
    }

    private Pane spacer(double width) {
        Pane p = new Pane();
        p.setMinWidth(width);
        return p;
    }


    private String standingLevelStyle(boolean hot) {
        double alpha = hot ? 0.42 : 0.24;
        return "-fx-background-color: radial-gradient(focus-angle 35deg, focus-distance 20%, center 30% 18%, radius 120%, " + tm.toRgba(tm.getAccentHex(), 0.28) + " 0%, rgba(10,10,15,0.82) 62%, rgba(0,0,0,0.94) 100%);" +
            "-fx-background-radius: 18px;" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), alpha) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 18px;" +
            "-fx-effect: dropshadow(gaussian, " + tm.toRgba(tm.getAccentHex(), hot ? 0.42 : 0.24) + ", " + (hot ? 34 : 22) + ", 0.20, 0, 0);";
    }

    private void animateIntegerText(Text target, int from, int to, int millis) {
        if (target == null || from == to) return;
        javafx.beans.property.IntegerProperty value = new javafx.beans.property.SimpleIntegerProperty(from);
        value.addListener((obs, oldValue, newValue) -> target.setText(String.valueOf(newValue.intValue())));
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(millis),
                new javafx.animation.KeyValue(value, to, HorizonDesignSystem.WEB_EASE)
            )
        );
        ownedAnimations.add(timeline);
        timeline.play();
    }

    private void animateProgress(javafx.beans.property.DoubleProperty progressValue, double targetValue) {
        double target = Math.max(0.0, Math.min(1.0, targetValue));
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(820),
                new javafx.animation.KeyValue(progressValue, target, HorizonDesignSystem.WEB_EASE)
            )
        );
        ownedAnimations.add(timeline);
        timeline.play();
    }

    private void animateProgressShine(StackPane shine, StackPane track) {
        Platform.runLater(() -> {
            double travel = Math.max(0, track.getWidth() - 58);
            shine.setTranslateX(0);
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                    new javafx.animation.KeyValue(shine.opacityProperty(), 0.0),
                    new javafx.animation.KeyValue(shine.translateXProperty(), 0.0)
                ),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(180),
                    new javafx.animation.KeyValue(shine.opacityProperty(), 0.75, HorizonDesignSystem.WEB_EASE)
                ),
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(880),
                    new javafx.animation.KeyValue(shine.translateXProperty(), travel, HorizonDesignSystem.WEB_EASE),
                    new javafx.animation.KeyValue(shine.opacityProperty(), 0.0, HorizonDesignSystem.WEB_EASE)
                )
            );
            ownedAnimations.add(timeline);
            timeline.play();
        });
    }

    private void playXpPulse(javafx.scene.Node node) {
        javafx.animation.ScaleTransition pulse = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(260), node);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.045);
        pulse.setToY(1.045);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.setInterpolator(HorizonDesignSystem.WEB_POP);
        ownedAnimations.add(pulse);
        pulse.play();
    }

    private void installBreathingGlow(javafx.scene.Node node, double glowAlpha) {
        node.setEffect(new javafx.scene.effect.DropShadow(22, Color.web(tm.toRgba(tm.getAccentHex(), glowAlpha))));
        javafx.animation.ScaleTransition breath = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(1500), node);
        breath.setFromX(1.0);
        breath.setFromY(1.0);
        breath.setToX(1.018);
        breath.setToY(1.018);
        breath.setAutoReverse(true);
        breath.setCycleCount(javafx.animation.Animation.INDEFINITE);
        breath.setInterpolator(HorizonDesignSystem.WEB_EASE);
        ownedAnimations.add(breath);
        breath.play();
    }
    private Text text(String value, int size, boolean bold, String color) {
        Text t = new Text(cleanUiGlyphText(value));
        t.setFont(Font.font(
            bold ? MainApplication.getInstance().getBoldFontFamily() : MainApplication.getInstance().getLightFontFamily(),
            bold ? FontWeight.BOLD : FontWeight.NORMAL,
            size
        ));
        t.setFill(Color.web(color));
        return t;
    }

    private String cleanUiGlyphText(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains("\u00C3") || value.contains("\u00E2")) {
            return "\u25CF";
        }
        return value;
    }

    private void stopOwnedAnimations() {
        for (javafx.animation.Animation animation : ownedAnimations) {
            try {
                animation.stop();
            } catch (Exception ignored) {
            }
        }
        ownedAnimations.clear();
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public void cleanup() {
        disposed = true;
        try {
            stopOwnedAnimations();
            contentHost = null;
            mainPages.clear();
            mainNavButtons.clear();
            detailTabs.clear();
            detailTabButtons.clear();
            root.getChildren().clear();
        } catch (Exception ignored) {}
        try { ImageLoaderUtil.trimCache(4); } catch (Exception ignored) {}
    }
}


