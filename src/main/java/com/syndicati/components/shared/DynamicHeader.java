package com.syndicati.components.shared;

import com.syndicati.MainApplication;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import com.syndicati.utils.navigation.NavigationManager;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.ui.HorizonDesignSystem;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.security.AccessControlService;
import com.syndicati.utils.image.ImageLoaderUtil;
import com.syndicati.services.user.profile.ProfileService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.syndicati.utils.localization.LocalizationManager;

public class DynamicHeader {
    private static final String LOGO_FOR_DARK_THEME = "/app_logo/syndicati-logo-light.png";
    private static final String LOGO_FOR_LIGHT_THEME = "/app_logo/syndicati-logo-dark.png";

    private final StackPane root;
    private final ThemeManager themeManager;
    private final LocalizationManager lm = LocalizationManager.getInstance();

    private Runnable backgroundUpdateCallback;

    private HBox navbar;
    private StackPane navbarShell;
    private Region navbarGlow;
    private HBox leftSection;
    private HBox rightSection;
    private HBox tabsPill;
    private StackPane tabsStack;
    private Region tabHighlight;

    private final Map<String, Button> tabButtons = new LinkedHashMap<>();
    private final Map<String, VBox> tabDropdownPopups = new HashMap<>();

    private VBox notificationDropdown;
    private VBox profileDropdown;
    private Node profileAnchor;
    private Node notificationAnchor;

    private PauseTransition closeTabDelay;
    private PauseTransition closeProfileDelay;
    private PauseTransition closeNotificationDelay;
    private String activeTab = "home";
    private String lastAnimatedActiveTab = "";

    private Circle profileTriggerAvatarCircle;
    private Label profileTriggerInitialLabel;

    private static final double HEADER_DROPDOWN_GAP = 10;

    public DynamicHeader() {
        this.root = new StackPane();
        this.themeManager = ThemeManager.getInstance();
        buildLayout();
        applyThemeStyling();
    }

    private void buildLayout() {
        root.setPadding(new Insets(22, 30, 0, 30));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: transparent;");
        root.setPickOnBounds(false);
        root.setMinHeight(Region.USE_PREF_SIZE);
        root.setPrefHeight(Region.USE_COMPUTED_SIZE);
        root.setMaxHeight(Region.USE_PREF_SIZE);

        navbar = new HBox();
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setPadding(new Insets(12, 24, 12, 24));
        navbar.setSpacing(16);
        navbar.setMinHeight(76);
        navbar.setPrefHeight(76);
        navbar.setPickOnBounds(true);
        installNavbarIslandHover();

        leftSection = buildLeftSection();
        HBox centerSection = buildCenterSection();
        rightSection = buildRightSection();

        leftSection.setMinWidth(260);
        leftSection.setPrefWidth(260);
        rightSection.setMinWidth(260);
        rightSection.setPrefWidth(260);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        navbar.getChildren().addAll(leftSection, leftSpacer, centerSection, rightSpacer, rightSection);
        navbarShell = new StackPane();
        navbarShell.setPickOnBounds(false);
        navbarGlow = new Region();
        navbarGlow.setMouseTransparent(true);
        navbarGlow.setOpacity(0);
        navbarGlow.prefWidthProperty().bind(navbar.widthProperty());
        navbarGlow.prefHeightProperty().bind(navbar.heightProperty());
        navbarGlow.setStyle(navbarGlowStyle());
        navbarShell.getChildren().addAll(navbarGlow, navbar);
        root.getChildren().add(navbarShell);
        if (notificationDropdown != null) {
            root.getChildren().add(notificationDropdown);
            StackPane.setAlignment(notificationDropdown, Pos.TOP_LEFT);
        }
        if (profileDropdown != null) {
            root.getChildren().add(profileDropdown);
            StackPane.setAlignment(profileDropdown, Pos.TOP_LEFT);
        }
    }

    private HBox buildLeftSection() {
        HBox left = new HBox();
        left.setAlignment(Pos.CENTER_LEFT);

        ImageView logoImage = new ImageView(loadThemedLogoImage());
        logoImage.setFitWidth(205);
        logoImage.setFitHeight(78);
        logoImage.setPreserveRatio(true);
        logoImage.setSmooth(true);

        Button logo = new Button();
        logo.setGraphic(logoImage);
        logo.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        logo.setMinWidth(232);
        logo.setPrefWidth(232);
        logo.setMinHeight(76);
        logo.setPrefHeight(76);
        logo.setAlignment(Pos.CENTER);
        logo.setPadding(new Insets(3, 12, 3, 12));
        styleGhostPill(logo);
        themeManager.isDarkModeProperty().addListener((obs, wasDark, isDark) -> logoImage.setImage(loadThemedLogoImage()));
        logo.setOnAction(e -> {
            activeTab = "home";
            updateTabsState();
            closeAllPopups();
            NavigationManager.getInstance().navigateTo("home");
        });

        left.getChildren().add(logo);
        return left;
    }

    private Image loadThemedLogoImage() {
        String resource = themeManager.isDarkMode() ? LOGO_FOR_DARK_THEME : LOGO_FOR_LIGHT_THEME;
        java.io.InputStream stream = getClass().getResourceAsStream(resource);
        if (stream == null) {
            stream = getClass().getResourceAsStream("/app_logo/syndicati.png");
        }
        return new Image(stream, 180, 82, true, true);
    }

    private HBox buildCenterSection() {
        HBox center = new HBox();
        center.setAlignment(Pos.CENTER);
        center.setPickOnBounds(false);

        tabsStack = new StackPane();
        tabsStack.setAlignment(Pos.TOP_LEFT);
        tabsStack.setPickOnBounds(false);
        tabHighlight = new Region();
        tabHighlight.setManaged(false);
        tabHighlight.setMouseTransparent(true);
        tabHighlight.setOpacity(0);
        tabHighlight.setStyle(tabHighlightStyle(false));
        StackPane.setAlignment(tabHighlight, Pos.TOP_LEFT);

        tabsPill = new HBox();
        tabsPill.setAlignment(Pos.CENTER);
        tabsPill.setSpacing(4);
        tabsPill.setPadding(new Insets(9));
        tabsPill.widthProperty().addListener((obs, oldValue, newValue) -> Platform.runLater(() -> moveTabHighlightToActive(false)));
        tabsPill.heightProperty().addListener((obs, oldValue, newValue) -> Platform.runLater(() -> moveTabHighlightToActive(false)));

        Button home = createTabButton("home", lm.get("home"));
        home.setOnAction(e -> {
            activeTab = "home";
            updateTabsState();
            closeAllPopups();
            NavigationManager.getInstance().navigateTo("home");
        });

        Button services = createTabButton("services", lm.get("services"));
        Map<String, String> servicesItems = new LinkedHashMap<>();
        servicesItems.put(lm.get("residence"), "services/residence");
        servicesItems.put(lm.get("forum"), "services/forum");
        servicesItems.put(lm.get("syndicat"), "services/syndicat");
        servicesItems.put(lm.get("evenement"), "services/evenement");
        attachTabDropdown("services", services, servicesItems);

        Button about = createTabButton("about", lm.get("about"));
        Map<String, String> aboutItems = new LinkedHashMap<>();
        aboutItems.put(lm.get("our_team"), "about");
        aboutItems.put(lm.get("company"), "about");
        aboutItems.put(lm.get("contact"), "about");
        attachTabDropdown("about", about, aboutItems);

        tabsPill.getChildren().addAll(home, services, about);
        tabsStack.getChildren().addAll(tabHighlight, tabsPill);
        center.getChildren().add(tabsStack);
        updateTabsState();
        Platform.runLater(() -> moveTabHighlightToActive(false));
        return center;
    }

    private Button createTabButton(String key, String text) {
        Button tab = new Button(text);
        tabButtons.put(key, tab);
        tab.setPadding(new Insets(11, 24, 11, 24));
        tab.setMinHeight(44);
        tab.setMinWidth(96);
        tab.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13));
        tab.setOnMouseEntered(e -> {
            if (!key.equals(activeTab)) {
                tab.setStyle(navTabTextStyle(false, true));
                moveTabHighlight(tab, true, true);
                HorizonDesignSystem.activePulse(tab);
            }
        });
        tab.setOnMouseExited(e -> {
            updateTabsState();
            moveTabHighlightToActive(true);
        });
        return tab;
    }

    private void attachTabDropdown(String key, Button anchorTab, Map<String, String> items) {
        VBox content = new VBox();
        content.setPadding(new Insets(10));
        content.setSpacing(3);
        content.setPrefWidth(210);
        content.setStyle("-fx-background-color: transparent;"); // Prevent white flash
        content.setVisible(false);
        content.setManaged(false);
        content.setMouseTransparent(true);

        for (Map.Entry<String, String> entry : items.entrySet()) {
            Button row = new Button(entry.getKey());
            row.setMaxWidth(Double.MAX_VALUE);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 12, 10, 12));
            row.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12));
            styleDropdownRow(row);
            String route = entry.getValue();
            row.setOnAction(e -> {
                closeAllPopups();
                activeTab = key;
                updateTabsState();
                NavigationManager.getInstance().navigateTo(route);
            });
            content.getChildren().add(row);
        }

        content.setOnMouseEntered(e -> cancelTabCloseDelay());
        content.setOnMouseExited(e -> scheduleCloseTabPopup(key));
        root.getChildren().add(content);
        StackPane.setAlignment(content, Pos.TOP_LEFT);

        anchorTab.setOnMouseEntered(e -> showTabPopup(key, anchorTab));
        anchorTab.setOnMouseExited(e -> scheduleCloseTabPopup(key));
        anchorTab.setOnAction(e -> {
            if (content.isVisible()) {
                closeTabPopup(content);
            } else {
                showTabPopup(key, anchorTab);
            }
        });

        tabDropdownPopups.put(key, content);
    }

    private void showTabPopup(String key, Button anchor) {
        cancelTabCloseDelay();
        closeNotificationPopup();
        closeProfilePopup();

        VBox content = tabDropdownPopups.get(key);
        if (content == null) {
            return;
        }

        for (Map.Entry<String, VBox> e : tabDropdownPopups.entrySet()) {
            if (!e.getKey().equals(key)) {
                closeTabPopup(e.getValue());
            }
        }

        content.applyCss();
        content.autosize();
        double w = content.prefWidth(-1);

        Bounds b = anchor.localToScreen(anchor.getBoundsInLocal());
        if (b == null) {
            return;
        }

        double x = b.getMinX() + (b.getWidth() - w) / 2.0;
        double y = dropdownTopScreen();
        Point2D local = root.screenToLocal(x, y);
        if (local == null) {
            return;
        }
        content.relocate(local.getX(), local.getY());
        content.setVisible(true);
        content.setMouseTransparent(false);
        content.toFront();
        HorizonDesignSystem.dropdownIn(content);
    }


    private double tabDropdownTopScreen(Node anchor) {
        return dropdownTopScreen();
    }
    private void scheduleCloseTabPopup(String key) {
        cancelTabCloseDelay();
        closeTabDelay = new PauseTransition(Duration.millis(160));
        closeTabDelay.setOnFinished(e -> {
            VBox content = tabDropdownPopups.get(key);
            if (content != null && content.isVisible()) {
                boolean hoveringContent = content.isHover();
                boolean hoveringAnchor = tabButtons.containsKey(key) && tabButtons.get(key).isHover();
                if (!hoveringContent && !hoveringAnchor) {
                    closeTabPopup(content);
                }
            }
        });
        closeTabDelay.play();
    }

    private void closeTabPopup(VBox content) {
        if (content == null) {
            return;
        }
        content.setMouseTransparent(true);
        if (content.isVisible()) {
            HorizonDesignSystem.dropdownOut(content, () -> content.setVisible(false));
        }
    }

    private void cancelTabCloseDelay() {
        if (closeTabDelay != null) {
            closeTabDelay.stop();
        }
    }

    private HBox buildRightSection() {
        HBox right = new HBox();
        right.setAlignment(Pos.CENTER_RIGHT);
        right.setSpacing(10);

        StackPane langSwitcher = buildLanguageSwitcher();
        StackPane themeToggle = buildThemeToggle();
        StackPane bell = buildNotificationTrigger();
        StackPane profile = buildProfileTrigger();

        right.getChildren().addAll(langSwitcher, themeToggle, bell, profile);
        return right;
    }

    private StackPane buildLanguageSwitcher() {
        com.syndicati.utils.localization.LocalizationManager lm = com.syndicati.utils.localization.LocalizationManager.getInstance();
        HBox container = new HBox(0);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(2));
        container.setMinWidth(118);
        container.setPrefWidth(118);
        container.setMaxWidth(118);
        container.setStyle(
            "-fx-background-color: " + (themeManager.isDarkMode() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.05)") + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + (themeManager.isDarkMode() ? "rgba(255,255,255,0.1)" : "rgba(15,23,42,0.1)") + ";" +
            "-fx-border-radius: 20px;"
        );

        String[] langs = {"EN", "FR", "AR"};
        for (String lang : langs) {
            Button btn = new Button(lang);
            btn.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.BOLD, 9));
            btn.setPadding(new Insets(4, 8, 4, 8));
            btn.setMinWidth(36);
            btn.setPrefWidth(36);
            btn.setMaxWidth(36);
            btn.setTextOverrun(OverrunStyle.CLIP);
            styleLangButton(btn, lang.equalsIgnoreCase(lm.getCurrentLanguage()));
            btn.setOnAction(e -> {
                if (!lang.equalsIgnoreCase(lm.getCurrentLanguage())) {
                    lm.loadLanguage(lang.toLowerCase());
                    MainApplication.getInstance().refreshAppUI();
                }
            });
            container.getChildren().add(btn);
        }
        
        return new StackPane(container);
    }

    private void styleLangButton(Button btn, boolean active) {
        String base = "-fx-background-radius: 18px; -fx-cursor: hand; -fx-transition: all 0.2s;";
        if (active) {
            btn.setStyle(base + 
                "-fx-background-color: " + themeManager.getAccentHex() + ";" +
                "-fx-text-fill: #ffffff;"
            );
        } else {
            btn.setStyle(base + 
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + (themeManager.isDarkMode() ? "#94a3b8" : "#64748b") + ";"
            );
        }
    }

    private StackPane buildThemeToggle() {
        StackPane wrap = new StackPane();
        wrap.setAlignment(Pos.CENTER);
        wrap.setMinSize(62, 34);
        wrap.setPrefSize(62, 34);
        wrap.setMaxSize(62, 34);

        Rectangle track = new Rectangle(62, 34);
        track.setArcWidth(34);
        track.setArcHeight(34);

        Circle thumb = new Circle(13);
        Label icon = new Label(themeManager.isDarkMode() ? "\u263e" : "\u2600");
        icon.setFont(Font.font(12));

        updateThemeToggleVisual(track, thumb, icon);

        wrap.getChildren().addAll(track, thumb, icon);
        wrap.setOnMouseClicked(e -> {
            themeManager.toggleTheme();
            updateThemeToggleVisual(track, thumb, icon);
            applyThemeStyling();
            if (backgroundUpdateCallback != null) {
                backgroundUpdateCallback.run();
            }
        });

        return wrap;
    }

    private void updateThemeToggleVisual(Rectangle track, Circle thumb, Label icon) {
        boolean dark = themeManager.isDarkMode();
        track.setFill(Color.web(dark ? "rgba(255,255,255,0.08)" : "rgba(255,255,255,0.75)"));
        track.setStroke(Color.web(dark ? "rgba(255,255,255,0.20)" : "rgba(15,23,42,0.12)"));
        thumb.setFill(Color.web(dark ? "#111827" : "#ffffff"));
        thumb.setTranslateX(dark ? 13 : -13);
        icon.setText(dark ? "\u263e" : "\u2600");
        icon.setTextFill(Color.web(dark ? "#e5e7eb" : "#111827"));
        icon.setTranslateX(dark ? 13 : -13);
    }

    private StackPane buildNotificationTrigger() {
        StackPane wrap = new StackPane();
        wrap.setAlignment(Pos.CENTER);
        notificationAnchor = wrap;

        Button bellButton = new Button("!");
        bellButton.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BLACK, 14));
        bellButton.setPadding(new Insets(8, 12, 8, 12));
        bellButton.setMinSize(42, 42);
        bellButton.setPrefSize(42, 42);
        bellButton.setMaxSize(42, 42);
        bellButton.setTextOverrun(OverrunStyle.CLIP);
        styleGhostPill(bellButton);

        Circle badge = new Circle(4.5, Color.web("#ff3b30"));
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(4, 5, 0, 0));

        notificationDropdown = buildNotificationDropdown();
        notificationDropdown.setVisible(false);
        notificationDropdown.setManaged(false);
        notificationDropdown.setMouseTransparent(true);
        notificationDropdown.toFront();

        notificationDropdown.setOnMouseEntered(e -> cancelNotificationCloseDelay());
        notificationDropdown.setOnMouseExited(e -> scheduleCloseNotificationPopup());

        wrap.setOnMouseEntered(e -> {
            if (profileDropdown != null && profileDropdown.isVisible()) {
                return;
            }
            cancelNotificationCloseDelay();
            showNotificationPopup();
        });
        wrap.setOnMouseExited(e -> scheduleCloseNotificationPopup());

        bellButton.setOnAction(e -> {
            if (notificationDropdown != null && notificationDropdown.isVisible()) {
                closeNotificationPopup();
                return;
            }
            showNotificationPopup();
        });

        wrap.getChildren().addAll(bellButton, badge);
        return wrap;
    }

    private VBox buildNotificationDropdown() {
        VBox box = new VBox();
        box.setPadding(new Insets(0));
        box.setSpacing(0);
        box.setPrefWidth(300);
        box.setMaxWidth(300);
        box.setMinWidth(300);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 14, 20));
        Label title = new Label("Notifications");
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 14));
        title.setTextFill(themeManager.isDarkMode() ? Color.web("#f3f4f6") : Color.web("#111827"));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = new Button("\u2715");
        close.setPadding(new Insets(4, 10, 4, 10));
        styleGhostPill(close);
        close.setOnAction(e -> closeNotificationPopup());
        header.getChildren().addAll(title, spacer, close);

        VBox rows = new VBox();
        rows.setPadding(new Insets(8));
        rows.setSpacing(6);
        rows.getChildren().addAll(
            notifRow("\u2728", "Syndicati", "Your community dashboard is synced", "now"),
            notifRow("\ud83d\udcac", "Forum", "A new reply landed in your discussion", "5 min"),
            notifRow("\ud83c\udfe2", "Residence", "A maintenance update is ready", "1 h")
        );

        box.getChildren().addAll(header, rows);
        return box;
    }

    private VBox notifRow(String icon, String title, String body, String time) {
        VBox row = new VBox();
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setSpacing(2);
        Label t = new Label(icon + "  " + title);
        t.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 12));
        t.setTextFill(themeManager.isDarkMode() ? Color.web("#f3f4f6") : Color.web("#111827"));
        Label b = new Label(body);
        b.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12));
        b.setTextFill(themeManager.isDarkMode() ? Color.web("#cbd5e1") : Color.web("#334155"));
        Label tm = new Label(time);
        tm.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 11));
        tm.setTextFill(themeManager.isDarkMode() ? Color.web("#94a3b8") : Color.web("#64748b"));
        row.getChildren().addAll(t, b, tm);
        row.setStyle("-fx-background-radius: 14px;");
        row.setOnMouseEntered(e -> row.setStyle(
            "-fx-background-color: " + (themeManager.isDarkMode() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.05)") + ";" +
            "-fx-background-radius: 14px;"
        ));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent; -fx-background-radius: 14px;"));
        return row;
    }

    private StackPane buildProfileTrigger() {
        StackPane wrap = new StackPane();
        wrap.setAlignment(Pos.CENTER);
        wrap.setMinSize(48, 48);

        Circle avatar = new Circle(20);
        Label initial = new Label(currentInitial());
        initial.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 11));
        initial.setTextFill(Color.WHITE);

        boolean hasAvatar = applyAvatarFill(avatar);
        initial.setVisible(!hasAvatar);
        initial.setManaged(!hasAvatar);

        profileTriggerAvatarCircle = avatar;
        profileTriggerInitialLabel = initial;

        Circle online = new Circle(5.5, Color.web("#22c55e"));
        StackPane.setAlignment(online, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(online, new Insets(0, 1, 1, 0));

        wrap.getChildren().addAll(avatar, initial, online);
        wrap.setStyle(
            "-fx-cursor: hand;" +
            "-fx-background-color: " + (themeManager.isDarkMode() ? "rgba(255,255,255,0.045)" : "rgba(15,23,42,0.05)") + ";" +
            "-fx-background-radius: 999px;" +
            "-fx-border-color: " + (themeManager.isDarkMode() ? "rgba(255,255,255,0.09)" : "rgba(15,23,42,0.08)") + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 999px;"
        );
        wrap.setMinSize(48, 48);
        wrap.setPrefSize(48, 48);
        wrap.setMaxSize(48, 48);
        profileAnchor = wrap;

        profileDropdown = buildProfileDropdown();
        profileDropdown.setVisible(false);
        profileDropdown.setManaged(false);
        profileDropdown.setMouseTransparent(true);
        profileDropdown.toFront();

        profileDropdown.setOnMouseEntered(e -> cancelProfileCloseDelay());
        profileDropdown.setOnMouseExited(e -> scheduleCloseProfilePopup());

        wrap.setOnMouseEntered(e -> {
            cancelProfileCloseDelay();
            showProfilePopup();
        });
        wrap.setOnMouseExited(e -> scheduleCloseProfilePopup());

        wrap.setOnMouseClicked(e -> {
            if (profileDropdown != null && profileDropdown.isVisible()) {
                closeProfilePopup();
                e.consume();
                return;
            }
            showProfilePopup();
            e.consume();
        });

        return wrap;
    }

    private VBox buildProfileDropdown() {
        VBox box = new VBox();
        box.setPadding(new Insets(0));
        box.setSpacing(0);
        box.setPrefWidth(320);
        box.setMaxWidth(320);
        box.setMinWidth(320);
        box.setStyle(profileDropdownCardStyle());

        HBox head = new HBox();
        head.setAlignment(Pos.CENTER_LEFT);
        head.setSpacing(10);
        head.setPadding(new Insets(20, 22, 16, 22));
        Circle pic = new Circle(20);
        boolean hasAvatar = applyAvatarFill(pic);
        Label picInitial = new Label(currentInitial());
        picInitial.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 14));
        picInitial.setTextFill(Color.WHITE);
        picInitial.setVisible(!hasAvatar);
        picInitial.setManaged(!hasAvatar);
        StackPane picWrap = new StackPane(pic, picInitial);
        
        VBox info = new VBox();
        Label name = new Label(currentDisplayName());
        name.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 13));
        name.setTextFill(themeManager.isDarkMode() ? Color.web("#f3f4f6") : Color.web("#111827"));
        Label mail = new Label(currentEmail());
        mail.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 11));
        mail.setTextFill(themeManager.isDarkMode() ? Color.web("#94a3b8") : Color.web("#64748b"));
        info.getChildren().addAll(name, mail);
        head.getChildren().addAll(picWrap, info);

        Region sep1 = new Region();
        sep1.setPrefHeight(1);
        sep1.setStyle("-fx-background-color: " + (themeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.10)") + ";");

        Button profile = profileRow("\ud83d\udc64  " + lm.get("profile"), "profile");
        Button settings = profileRow("\u2699  " + lm.get("settings"), "settings");

        VBox rows = new VBox();
        rows.setPadding(new Insets(8));
        rows.setSpacing(6);
        rows.getChildren().add(profile);
        if (AccessControlService.canAccessAdminArea()) {
            rows.getChildren().add(profileRow("\ud83d\udcca  " + lm.get("dashboard"), "dashboard"));
        }
        rows.getChildren().add(settings);

        Region sep2 = new Region();
        sep2.setPrefHeight(1);
        sep2.setStyle("-fx-background-color: " + (themeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.10)") + ";");

        Button logout = new Button("\u23fb  " + lm.get("logout"));
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setPadding(new Insets(9, 10, 9, 10));
        logout.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12));
        styleDropdownRow(logout);
        logout.setOnAction(e -> {
            closeProfilePopup();
            MainApplication.getInstance().logout();
        });

        VBox bottom = new VBox(8);
        bottom.setPadding(new Insets(8));
        bottom.getChildren().add(logout);

        box.getChildren().addAll(head, sep1, rows, sep2, bottom);
        return box;
    }

    private Button profileRow(String text, String route) {
        Button row = new Button(text);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 10, 9, 10));
        row.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12));
        styleDropdownRow(row);
        row.setOnAction(e -> {
            closeProfilePopup();
            NavigationManager.getInstance().navigateTo(route);
        });
        return row;
    }

    private void showNotificationPopup() {
        if (notificationDropdown == null || notificationAnchor == null) {
            return;
        }
        closeAllTabPopups();
        closeProfilePopup();
        positionNotificationDropdown();
        notificationDropdown.setVisible(true);
        notificationDropdown.setMouseTransparent(false);
        notificationDropdown.toFront();
        HorizonDesignSystem.dropdownIn(notificationDropdown);
    }

    private void positionProfileDropdown() {
        if (profileDropdown == null || profileAnchor == null || root.getScene() == null) {
            return;
        }

        profileDropdown.applyCss();
        profileDropdown.autosize();

        Bounds anchorScreen = profileAnchor.localToScreen(profileAnchor.getBoundsInLocal());
        Bounds rootScreen = root.localToScreen(root.getBoundsInLocal());
        if (anchorScreen == null || rootScreen == null) {
            return;
        }

        double popupW = profileDropdown.prefWidth(-1);
        double popupH = Math.max(profileDropdown.prefHeight(-1), 220);

        double xScreen = anchorScreen.getMaxX() - popupW;
        double yScreen = dropdownTopScreen();

        double minX = rootScreen.getMinX() + 8;
        double maxX = rootScreen.getMaxX() - popupW - 8;
        double minY = rootScreen.getMinY() + 8;
        double maxY = root.getScene().getWindow().getY() + root.getScene().getWindow().getHeight() - popupH - 24;

        if (maxX >= minX) {
            xScreen = Math.max(minX, Math.min(xScreen, maxX));
        }
        yScreen = Math.max(minY, Math.min(yScreen, maxY));

        Point2D local = root.screenToLocal(xScreen, yScreen);
        profileDropdown.relocate(local.getX(), local.getY());
    }

    private void positionNotificationDropdown() {
        if (notificationDropdown == null || notificationAnchor == null || root.getScene() == null) {
            return;
        }

        notificationDropdown.applyCss();
        notificationDropdown.autosize();

        Bounds anchorScreen = notificationAnchor.localToScreen(notificationAnchor.getBoundsInLocal());
        Bounds rootScreen = root.localToScreen(root.getBoundsInLocal());
        if (anchorScreen == null || rootScreen == null) {
            return;
        }

        double popupW = notificationDropdown.prefWidth(-1);
        double popupH = Math.max(notificationDropdown.prefHeight(-1), 220);

        double xScreen = anchorScreen.getMaxX() - popupW;
        double yScreen = dropdownTopScreen();

        double minX = rootScreen.getMinX() + 8;
        double maxX = rootScreen.getMaxX() - popupW - 8;
        double minY = rootScreen.getMinY() + 8;
        double maxY = rootScreen.getMaxY() - popupH - 8;

        if (maxX >= minX) {
            xScreen = Math.max(minX, Math.min(xScreen, maxX));
        }
        if (maxY >= minY) {
            yScreen = Math.max(minY, Math.min(yScreen, maxY));
        }

        Point2D local = root.screenToLocal(xScreen, yScreen);
        notificationDropdown.relocate(local.getX(), local.getY());
    }

    private double dropdownTopScreen() {
        double localTop = root.getPadding().getTop() + 76 + HEADER_DROPDOWN_GAP;
        Point2D screenPoint = root.localToScreen(0, localTop);
        if (screenPoint != null) {
            return screenPoint.getY();
        }
        Bounds rootScreen = root.localToScreen(root.getBoundsInLocal());
        return rootScreen == null ? localTop : rootScreen.getMinY() + localTop;
    }
    private void scheduleCloseNotificationPopup() {
        cancelNotificationCloseDelay();
        closeNotificationDelay = new PauseTransition(Duration.millis(180));
        closeNotificationDelay.setOnFinished(e -> {
            if (notificationDropdown == null) {
                return;
            }
            boolean hoveringAnchor = notificationAnchor != null && notificationAnchor.isHover();
            boolean hoveringContent = notificationDropdown != null && notificationDropdown.isHover();
            if (!hoveringAnchor && !hoveringContent) {
                closeNotificationPopup();
            }
        });
        closeNotificationDelay.play();
    }

    private void cancelNotificationCloseDelay() {
        if (closeNotificationDelay != null) {
            closeNotificationDelay.stop();
        }
    }

    private void showProfilePopup() {
        if (profileAnchor == null) {
            return;
        }

        refreshProfileTriggerAvatar();

        closeAllTabPopups();
        closeNotificationPopup();

        if (profileDropdown == null) {
            profileDropdown = buildProfileDropdown();
            profileDropdown.setVisible(false);
            profileDropdown.setManaged(false);
            profileDropdown.setMouseTransparent(true);
            profileDropdown.setOnMouseEntered(e -> cancelProfileCloseDelay());
            profileDropdown.setOnMouseExited(e -> scheduleCloseProfilePopup());
            root.getChildren().add(profileDropdown);
            StackPane.setAlignment(profileDropdown, Pos.TOP_LEFT);
        }

        positionProfileDropdown();
        profileDropdown.setVisible(true);
        profileDropdown.setMouseTransparent(false);
        profileDropdown.toFront();
        HorizonDesignSystem.dropdownIn(profileDropdown);
    }

    private String profileDropdownCardStyle() {
        return HorizonDesignSystem.webFloatingIsland(30, false);
    }

    private void refreshProfileTriggerAvatar() {
        if (profileTriggerAvatarCircle == null || profileTriggerInitialLabel == null) {
            return;
        }
        boolean hasAvatar = applyAvatarFill(profileTriggerAvatarCircle);
        profileTriggerInitialLabel.setText(currentInitial());
        profileTriggerInitialLabel.setVisible(!hasAvatar);
        profileTriggerInitialLabel.setManaged(!hasAvatar);
    }

    private String currentDisplayName() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            return "Syndicati Member";
        }

        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (first + " " + last).trim();
        return fullName.isBlank() ? "User" : fullName;
    }

    private String currentEmail() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null || user.getEmailUser() == null || user.getEmailUser().isBlank()) {
            return "contact@syndicati.tn";
        }
        return user.getEmailUser();
    }

    private String currentInitial() {
        String name = currentDisplayName();
        if (!name.isBlank()) {
            return name.substring(0, 1).toUpperCase();
        }
        String email = currentEmail();
        return email.isBlank() ? "U" : email.substring(0, 1).toUpperCase();
    }

    private Profile currentProfile() {
        return SessionManager.getInstance().getCurrentProfile();
        // Removed blocking ProfileService lookup from UI thread
    }

    private boolean applyAvatarFill(Circle avatarCircle) {
        Profile profile = currentProfile();
        String avatarPath = profile == null ? null : profile.getAvatar();

        if (avatarPath != null && !avatarPath.isBlank()) {
            Image img = ImageLoaderUtil.loadProfileAvatar(avatarPath, true);
            if (img != null && !img.isError()) {
                if (img.getProgress() < 1.0) {
                    img.progressProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.doubleValue() >= 1.0) {
                            avatarCircle.setFill(new ImagePattern(img));
                        }
                    });
                } else {
                    avatarCircle.setFill(new ImagePattern(img));
                }
                return true;
            }
        }

        // Fallback to accent color
        avatarCircle.setFill(Color.web(themeManager.getAccentHex()));
        return false;
    }

    private void scheduleCloseProfilePopup() {
        cancelProfileCloseDelay();
        closeProfileDelay = new PauseTransition(Duration.millis(620));
        closeProfileDelay.setOnFinished(e -> {
            if (profileDropdown == null) {
                return;
            }
            boolean hoveringAnchor = profileAnchor != null && profileAnchor.isHover();
            boolean hoveringContent = profileDropdown.isHover();
            if (!hoveringAnchor && !hoveringContent) {
                closeProfilePopup();
            }
        });
        closeProfileDelay.play();
    }

    private void cancelProfileCloseDelay() {
        if (closeProfileDelay != null) {
            closeProfileDelay.stop();
        }
    }

    private void closeAllPopups() {
        closeAllTabPopups();
        closeNotificationPopup();
        closeProfilePopup();
    }

    private void closeAllTabPopups() {
        for (VBox content : tabDropdownPopups.values()) {
            closeTabPopup(content);
        }
    }

    private void closeNotificationPopup() {
        cancelNotificationCloseDelay();
        if (notificationDropdown != null && notificationDropdown.isVisible()) {
            notificationDropdown.setMouseTransparent(true);
            HorizonDesignSystem.dropdownOut(notificationDropdown, () -> notificationDropdown.setVisible(false));
        } else if (notificationDropdown != null) {
            notificationDropdown.setMouseTransparent(true);
        }
    }

    private void closeProfilePopup() {
        cancelProfileCloseDelay();
        if (profileDropdown != null && profileDropdown.isVisible()) {
            profileDropdown.setMouseTransparent(true);
            HorizonDesignSystem.dropdownOut(profileDropdown, () -> profileDropdown.setVisible(false));
        } else if (profileDropdown != null) {
            profileDropdown.setMouseTransparent(true);
        }
    }

    private void updateTabsState() {
        for (Map.Entry<String, Button> e : tabButtons.entrySet()) {
            String key = e.getKey();
            Button tab = e.getValue();
            boolean selected = key.equals(activeTab);
            tab.setStyle(navTabTextStyle(selected, false));
            tab.setFont(Font.font(
                MainApplication.getInstance().getLightFontFamily(),
                selected ? FontWeight.SEMI_BOLD : FontWeight.NORMAL,
                13
            ));
            if (selected && !key.equals(lastAnimatedActiveTab)) {
                HorizonDesignSystem.activePulse(tab);
            }
        }
        lastAnimatedActiveTab = activeTab;
        Platform.runLater(() -> moveTabHighlightToActive(true));
    }

    private String navTabTextStyle(boolean selected, boolean hovering) {
        String color = selected
            ? "#ffffff"
            : (hovering ? themeManager.getAccentHex() : (themeManager.isDarkMode() ? "#f5f5f5" : "#374151"));
        String background = selected
            ? themeManager.toRgba(themeManager.getAccentHex(), 0.15)
            : (hovering ? (themeManager.isDarkMode() ? "rgba(255,255,255,0.10)" : "rgba(15,23,42,0.055)") : "transparent");
        return "-fx-background-color: " + background + ";" +
            "-fx-background-radius: 999px;" +
            "-fx-border-color: transparent;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 999px;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-cursor: hand;";
    }

    private String tabHighlightStyle(boolean hover) {
        double alpha = hover ? 0.14 : 0.09;
        return "-fx-background-color: " + themeManager.getEffectiveAccentGradient() + ", " + themeManager.toRgba(themeManager.getAccentHex(), alpha) + ";" +
            "-fx-background-insets: 0, 1.4;" +
            "-fx-background-radius: 999px;" +
            "-fx-border-color: " + themeManager.toRgba(themeManager.getAccentHex(), hover ? 0.36 : 0.22) + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 999px;" +
            "-fx-effect: dropshadow(gaussian, " + themeManager.toRgba(themeManager.getAccentHex(), hover ? 0.24 : 0.14) + ", " + (hover ? 24 : 14) + ", 0.18, 0, 0);";
    }

    private void moveTabHighlightToActive(boolean animated) {
        Button active = tabButtons.get(activeTab);
        if (active != null) {
            moveTabHighlight(active, false, animated);
        }
    }

    private void moveTabHighlight(Button tab, boolean hover, boolean animated) {
        if (tabsStack == null || tabHighlight == null || tab == null || tab.getScene() == null) {
            return;
        }
        tabsStack.applyCss();
        tabsStack.layout();
        Bounds b = tabsStack.sceneToLocal(tab.localToScene(tab.getBoundsInLocal()));
        tabHighlight.setStyle(tabHighlightStyle(hover));
        double w = Math.max(1, b.getWidth());
        double h = Math.max(1, b.getHeight());
        double x = b.getMinX();
        double y = b.getMinY();
        if (!animated) {
            tabHighlight.setPrefSize(w, h);
            tabHighlight.setMinSize(w, h);
            tabHighlight.setMaxSize(w, h);
            tabHighlight.setTranslateX(x);
            tabHighlight.setTranslateY(y);
            tabHighlight.setOpacity(1);
            return;
        }
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(260),
            new KeyValue(tabHighlight.translateXProperty(), x, HorizonDesignSystem.WEB_EASE),
            new KeyValue(tabHighlight.translateYProperty(), y, HorizonDesignSystem.WEB_EASE),
            new KeyValue(tabHighlight.prefWidthProperty(), w, HorizonDesignSystem.WEB_EASE),
            new KeyValue(tabHighlight.prefHeightProperty(), h, HorizonDesignSystem.WEB_EASE),
            new KeyValue(tabHighlight.minWidthProperty(), w, HorizonDesignSystem.WEB_EASE),
            new KeyValue(tabHighlight.minHeightProperty(), h, HorizonDesignSystem.WEB_EASE),
            new KeyValue(tabHighlight.maxWidthProperty(), w, HorizonDesignSystem.WEB_EASE),
            new KeyValue(tabHighlight.maxHeightProperty(), h, HorizonDesignSystem.WEB_EASE),
            new KeyValue(tabHighlight.opacityProperty(), 1, HorizonDesignSystem.WEB_EASE)
        ));
        tl.play();
    }

    private void styleGhostPill(Button button) {
        button.setStyle(HorizonDesignSystem.buttonGhost() + "-fx-background-radius: 999px; -fx-border-radius: 999px; -fx-padding: 8 14 8 14;");
        button.setMinHeight(42);
        HorizonDesignSystem.installButtonMotion(button);
    }

    private void styleDropdownRow(Button row) {
        String text = themeManager.isDarkMode() ? "#e5e7eb" : "#1f2937";
        String base =
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: transparent;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12px;" +
            "-fx-text-fill: " + text + ";" +
            "-fx-cursor: hand;";
        String hover =
            "-fx-background-color: " + themeManager.toRgba(themeManager.getAccentHex(), themeManager.isDarkMode() ? 0.14 : 0.08) + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + themeManager.toRgba(themeManager.getAccentHex(), 0.26) + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 12px;" +
            "-fx-text-fill: " + text + ";" +
            "-fx-cursor: hand;";
        row.setStyle(base);
        row.setOnMouseEntered(e -> row.setStyle(hover));
        row.setOnMouseExited(e -> row.setStyle(base));
        HorizonDesignSystem.installButtonMotion(row);
    }

    private void applyThemeStyling() {
        if (navbarGlow != null) {
            navbarGlow.setStyle(navbarGlowStyle());
        }
        if (tabHighlight != null) {
            tabHighlight.setStyle(tabHighlightStyle(false));
        }
        navbar.setStyle(HorizonDesignSystem.webFloatingIsland(999, false));

        tabsPill.setStyle(
            "-fx-background-color: " + (themeManager.isDarkMode() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.045)") + ";" +
            "-fx-background-radius: 999px;" +
            "-fx-border-color: " + HorizonDesignSystem.border() + ";" +
            "-fx-border-radius: 999px;" +
            "-fx-border-width: 1;"
        );

        for (VBox box : tabDropdownPopups.values()) {
            box.setStyle(HorizonDesignSystem.webFloatingIsland(22, false));
            for (Node n : box.getChildren()) {
                if (n instanceof Button b) {
                    styleDropdownRow(b);
                }
            }
        }

        if (notificationDropdown != null) {
            notificationDropdown.setStyle(HorizonDesignSystem.webFloatingIsland(30, false));
            for (Node n : notificationDropdown.getChildren()) {
                if (n instanceof HBox h) {
                    for (Node child : h.getChildren()) {
                        if (child instanceof Label l) {
                            l.setTextFill(themeManager.isDarkMode() ? Color.web("#f3f4f6") : Color.web("#111827"));
                        }
                    }
                }
            }
        }

        if (profileDropdown != null) {
            profileDropdown.setStyle(profileDropdownCardStyle());
            for (Node n : profileDropdown.getChildren()) {
                if (n instanceof Button b) {
                    styleDropdownRow(b);
                }
            }
        }

        updateTabsState();
    }

    private String navbarGlowStyle() {
        return "-fx-background-color: " + themeManager.getEffectiveAccentGradient() + ";" +
            "-fx-background-radius: 999px;" +
            "-fx-effect: dropshadow(gaussian, " + themeManager.toRgba(themeManager.getAccentHex(), 0.48) + ", 46, 0.30, 0, 0);";
    }

    private void installNavbarIslandHover() {
        navbar.setOnMouseEntered(e -> {
            Timeline tl = new Timeline(new KeyFrame(Duration.millis(320),
                new KeyValue(navbar.translateYProperty(), -2, HorizonDesignSystem.WEB_EASE),
                new KeyValue(navbarGlow.opacityProperty(), 0.72, HorizonDesignSystem.WEB_EASE)
            ));
            tl.play();
            navbar.setStyle(navbarHoverStyle());
        });
        navbar.setOnMouseExited(e -> {
            Timeline tl = new Timeline(new KeyFrame(Duration.millis(320),
                new KeyValue(navbar.translateYProperty(), 0, HorizonDesignSystem.WEB_EASE),
                new KeyValue(navbarGlow.opacityProperty(), 0, HorizonDesignSystem.WEB_EASE)
            ));
            tl.play();
            applyThemeStyling();
        });
    }

    private String navbarHoverStyle() {
        return HorizonDesignSystem.webFloatingIsland(999, true) +
            "-fx-border-color: " + themeManager.toRgba(themeManager.getAccentHex(), 0.48) + ";";
    }

    public void setBackgroundUpdateCallback(Runnable callback) {
        this.backgroundUpdateCallback = callback;
    }

    public void setMainContainer(javafx.scene.layout.Pane mainContainer) {
        // Compatibility method.
    }

    public void refreshTheme() {
        applyThemeStyling();
    }

    public javafx.scene.layout.Pane getRoot() {
        return root;
    }

    public void cleanup() {
        cancelTabCloseDelay();
        cancelProfileCloseDelay();
        cancelNotificationCloseDelay();
        closeAllPopups();
    }
}


