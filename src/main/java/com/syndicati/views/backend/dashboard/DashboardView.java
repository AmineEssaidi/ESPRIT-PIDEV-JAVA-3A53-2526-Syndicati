package com.syndicati.views.backend.dashboard;

import com.syndicati.components.shared.ImageBackground;
import com.syndicati.controllers.log.AnalyticsController;
import com.syndicati.models.log.AppEventLog;
import com.syndicati.models.log.analytics.AnomalyResult;
import com.syndicati.models.log.analytics.SuspiciousActivity;
import com.syndicati.models.forum.Publication;
import com.syndicati.models.syndicat.Reclamation;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import com.syndicati.models.residence.Residence;
import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.controllers.residence.ResidenceController;
import com.syndicati.controllers.residence.MaintenanceController;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import com.syndicati.utils.image.imagekit.ImageKitConfig;
import com.syndicati.utils.image.imagekit.ImageKitStorageService;
import com.syndicati.utils.image.imagekit.ImageKitUploadResult;
import java.io.File;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.ui.HorizonDesignSystem;
import com.syndicati.utils.image.ImageLoaderUtil;
import com.syndicati.services.DatabaseService;
import com.syndicati.services.dashboard.DashboardAdminService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;

/**
 * Admin Dashboard View - full replica of the Horizon admin panel.
 * Sidebar (220px) + scrollable main area.
 * Call setExitCallback() so "Back to App" can exit dashboard mode.
 */
@SuppressWarnings({"SpellCheckingInspection", "CssInvalidPropertyValue"})
public class DashboardView implements ViewInterface {

    private final HBox root;
    String activeSection = "general";
    private final Map<String, Button> sectionButtons = new HashMap<>();
    private final Map<String, VBox> viewCache = new HashMap<>();
    VBox contentArea;
    private VBox sidebarNode;
    private Region mainAreaNode;
    private ImageBackground dashboardBackground;
    Runnable exitCallback;

    private VBox getCachedView(String key, java.util.function.Supplier<VBox> builder) {
        if (viewCache.containsKey(key)) {
            return viewCache.get(key);
        }
        VBox view = builder.get();
        viewCache.put(key, view);
        return view;
    }
    private final Runnable accentRefreshListener;
    boolean sidebarExpanded = true;
    Popup profilePopup;
    Popup notificationPopup;
    PauseTransition notificationHideDelay;
    private java.time.Instant lastSectionSwitch = java.time.Instant.now();
    private final DashboardAdminService dashboardAdminService;
    private final AnalyticsController analyticsController;
    private final com.syndicati.controllers.log.ActivityLogController activityLogController;
    private static final DateTimeFormatter DASHBOARD_DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DashboardView() {
        this.root = new HBox();
        this.accentRefreshListener = this::refreshAccentStyling;
        this.dashboardAdminService = new DashboardAdminService();
        this.analyticsController = new AnalyticsController();
        this.activityLogController = new com.syndicati.controllers.log.ActivityLogController();
        setupLayout();
        ThemeManager.getInstance().addAccentChangeListener(accentRefreshListener);
    }

    DashboardAdminService dashboardAdminService() {
        return dashboardAdminService;
    }

    public void setExitCallback(Runnable cb) { this.exitCallback = cb; }

    @Override public HBox getRoot()  { return root; }
    @Override public void cleanup()  {
        exitCallback = null;
        if (dashboardBackground != null) {
            dashboardBackground.cleanup();
            dashboardBackground = null;
        }
        ThemeManager.getInstance().removeAccentChangeListener(accentRefreshListener);
    }

    private void refreshAccentStyling() {
        // Optimization: Do NOT clear root. Re-apply styles instead.
        root.setStyle(
            "-fx-background-color: " + (
                isDark()
                    ? "linear-gradient(to bottom right, #070707, #030303 55%, #000000 100%)"
                    : "linear-gradient(to bottom right, #f7f7f7, #f2f2f2 55%, #ececec 100%)"
            ) + ";"
        );
        
        sectionButtons.forEach((k, b) -> styleSidebarItem(b, k.equals(activeSection)));
        
        // Only refresh the content if necessary, but try to preserve state
        if (contentArea != null) {
            contentArea.getChildren().setAll(getCachedView("SECTION_" + activeSection, () -> buildSection(activeSection)));
        }
    }

    private ThemeManager theme() { return ThemeManager.getInstance(); }
    private String accentHex() { return theme().getAccentHex(); }
    String accentGradient() { return theme().getEffectiveAccentGradient(); }
    String accentRgba(double alpha) { return theme().toRgba(accentHex(), alpha); }
    boolean isDark() { return theme().isDarkMode(); }
    Color textPrimaryColor() { return isDark() ? Color.web("#f8fafc") : Color.web("#111827"); }
    Color textSecondaryColor() { return isDark() ? Color.web("rgba(255,255,255,0.78)") : Color.web("rgba(17,24,39,0.82)"); }
    Color textMutedColor() { return isDark() ? Color.web("rgba(255,255,255,0.55)") : Color.web("rgba(30,41,59,0.64)"); }

    String currentDisplayName() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            return "Admin User";
        }

        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? "User" : fullName;
    }

    String currentEmail() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null || user.getEmailUser() == null || user.getEmailUser().isBlank()) {
            return "contact@syndicati.tn";
        }
        return user.getEmailUser();
    }

    String currentRoleLabel() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null || user.getRoleUser() == null || user.getRoleUser().isBlank()) {
            return "Administrateur";
        }
        String role = user.getRoleUser().trim();
        if ("ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            return "Administrator";
        }
        return role;
    }

    String currentInitial() {
        String name = currentDisplayName();
        if (!name.isBlank()) {
            return name.substring(0, 1).toUpperCase();
        }
        String email = currentEmail();
        return email.isBlank() ? "U" : email.substring(0, 1).toUpperCase();
    }

    boolean applyAvatarFill(Circle avatarCircle) {
        Profile profile = SessionManager.getInstance().getCurrentProfile();
        String avatarPath = profile == null ? null : profile.getAvatar();

        if (avatarPath != null && !avatarPath.isBlank()) {
            Image img = ImageLoaderUtil.loadProfileAvatar(avatarPath, false);
            if (img != null && !img.isError()) {
                avatarCircle.setFill(new ImagePattern(img));
                return true;
            }
        }

        // Fallback to accent color
        avatarCircle.setFill(Color.web(accentHex()));
        return false;
    }


    private String sidebarPillStyle() {
        String glassFill = isDark()
            ? "linear-gradient(to bottom right, rgba(14,14,14,0.94), rgba(10,10,10,0.96) 54%, rgba(6,6,6,0.98) 100%)"
            : "linear-gradient(to bottom right, rgba(255,255,255,0.98), rgba(248,248,248,0.97) 54%, rgba(242,242,242,0.96) 100%)";
        return
            "-fx-background-color:" + glassFill + ";" +
            "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.14)" : "rgba(15,23,42,0.12)") + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:24px;" +
            "-fx-background-radius:24px;" +
            "-fx-effect:dropshadow(one-pass-box," + (isDark() ? "rgba(0,0,0,0.28)" : "rgba(15,23,42,0.12)") + ",26,0,0,8);";
    }

    private void setupLayout() {
        root.setSpacing(0);
        root.setAlignment(Pos.TOP_LEFT);
        root.setMinSize(0, 0);
        root.setFillHeight(true);
        root.setStyle(
            "-fx-background-color: " + (
                isDark()
                    ? "linear-gradient(to bottom right, #070707, #030303 55%, #000000 100%)"
                    : "linear-gradient(to bottom right, #f7f7f7, #f2f2f2 55%, #ececec 100%)"
            ) + ";"
        );
        root.setPadding(new Insets(40, 0, 0, 0));   // 40px = window-bar height

        sidebarNode = DashboardShell.buildSidebar(this);
        sidebarNode.setPrefWidth(sidebarWidth()); sidebarNode.setMinWidth(sidebarWidth()); sidebarNode.setMaxWidth(sidebarWidth());

        mainAreaNode = DashboardShell.buildMainArea(this);
        HBox.setHgrow(mainAreaNode, Priority.ALWAYS);
        root.getChildren().addAll(sidebarNode, mainAreaNode);
    }

    private double sidebarWidth() {
        return sidebarExpanded ? 250 : 96;
    }

    void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        rebuildSidebarOnly();
    }

    private void rebuildSidebarOnly() {
        sectionButtons.clear();
        VBox replacement = DashboardShell.buildSidebar(this);
        replacement.setPrefWidth(sidebarWidth());
        replacement.setMinWidth(sidebarWidth());
        replacement.setMaxWidth(sidebarWidth());
        sidebarNode = replacement;

        if (root.getChildren().isEmpty()) {
            root.getChildren().add(sidebarNode);
            if (mainAreaNode != null) {
                root.getChildren().add(mainAreaNode);
            }
            return;
        }

        root.getChildren().set(0, sidebarNode);
    }

    // SIDEBAR - three floating glass pills (matches web admin CSS)
    //   .admin-sidebar-top | .admin-sidebar-nav | .admin-sidebar-bottom
    //   each: backdrop-blur glass, border-radius 24px, transparent gap

    /** Shared glass pill container - three of these make up the sidebar. */
    VBox glassPill(Pos alignment) {
        VBox pill = new VBox(0);
        pill.setAlignment(alignment);
        pill.setFillWidth(true);
        pill.setStyle(HorizonDesignSystem.webFloatingIsland(28, false));
        return pill;
    }

    void styleBackButton(Button b, boolean h) {
        b.setStyle(
            "-fx-background-color:" + (h ? (isDark() ? "rgba(255,255,255,0.075)" : "rgba(15,23,42,0.07)") : "transparent") + ";" +
            "-fx-border-color:" + (h ? (isDark() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.12)") : "transparent") + ";" +
            "-fx-border-width:1;" +
            "-fx-background-radius:999px;" +
            "-fx-border-radius:999px;" +
            "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.76)" : "rgba(15,23,42,0.78)") + ";" +
            "-fx-cursor:hand;"
        );
    }

    void styleSignOutButton(Button b, boolean h) {
        String background = h ? "rgba(239,68,68,0.18)" : "rgba(239,68,68,0.10)";
        String border = h ? "rgba(239,68,68,0.36)" : "rgba(239,68,68,0.22)";
        String textColor = h ? "#ffffff" : "#fecaca";
        b.setStyle(
            "-fx-background-color:" + background + ";" +
            "-fx-border-color:" + border + ";" +
            "-fx-border-width:1;" +
            "-fx-background-radius:999px;" +
            "-fx-border-radius:999px;" +
            "-fx-text-fill:" + textColor + ";" +
            "-fx-cursor:hand;"
        );
    }

    Region pillSep() {
        Region r = new Region(); r.setPrefHeight(1); r.setMaxHeight(1);
        r.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.10)") + ";");
        VBox.setMargin(r, new Insets(6, 0, 6, 0));
        return r;
    }

    Button sidebarItem(String icon, String label, String section) {
        HBox inner = new HBox(10);
        inner.setAlignment(sidebarExpanded ? Pos.CENTER_LEFT : Pos.CENTER);
        inner.setMouseTransparent(true);
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(34, 34); iconBox.setMinSize(34, 34); iconBox.setMaxSize(34, 34);
        iconBox.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.045)" : "rgba(15,23,42,0.055)") + ";-fx-background-radius:12px;");
        Text ic = new Text(icon); ic.setFont(Font.font(16));
        iconBox.getChildren().add(ic);

        inner.getChildren().add(iconBox);
        if (sidebarExpanded) {
            Text lbl = t(label, boldFont(), FontWeight.BOLD, 13);
            lbl.setFill(isDark() ? Color.web("rgba(255,255,255,0.66)") : Color.web("rgba(15,23,42,0.70)"));
            inner.getChildren().add(lbl);
        }

        Button btn = new Button();
        btn.setGraphic(inner);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(sidebarExpanded ? Pos.CENTER_LEFT : Pos.CENTER);
        btn.setPadding(sidebarExpanded ? new Insets(7, 10, 7, 10) : new Insets(7, 6, 7, 6));
        btn.setPrefHeight(48);
        if (!sidebarExpanded) {
            btn.setPrefWidth(44);
            btn.setMinWidth(44);
            btn.setMaxWidth(44);
            Tooltip.install(btn, new Tooltip(label));
        }
        sectionButtons.put(section, btn);
        styleSidebarItem(btn, section.equals(activeSection));

        btn.setOnMouseEntered(_ -> {
            if (!section.equals(activeSection)) {
                btn.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.065)" : "rgba(15,23,42,0.07)") + ";-fx-background-radius:999px;-fx-cursor:hand;-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.11)" : "rgba(15,23,42,0.12)") + ";-fx-border-width:1;-fx-border-radius:999px;-fx-effect:dropshadow(one-pass-box," + (isDark() ? "rgba(0,0,0,0.34)" : "rgba(15,23,42,0.12)") + ",18,0,0,6);");
                iconBox.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.105)" : "rgba(15,23,42,0.10)") + ";-fx-background-radius:12px;");
             }
        });
        btn.setOnMouseExited(_ -> {
            styleSidebarItem(btn, section.equals(activeSection));
            if (!section.equals(activeSection)) iconBox.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.045)" : "rgba(15,23,42,0.055)") + ";-fx-background-radius:12px;");
        });
        btn.setOnAction(_ -> switchSection(section));
        return btn;
    }

    private void styleSidebarItem(Button b, boolean active) {
        if (active) {
            b.setStyle(
                "-fx-background-color:" + accentGradient() + ";" +
                "-fx-background-radius:999px;" +
                "-fx-border-color:" + accentRgba(0.38) + ";" +
                "-fx-border-width:1;" +
                "-fx-border-radius:999px;" +
                "-fx-cursor:hand;" +
                "-fx-effect:dropshadow(one-pass-box," + accentRgba(0.48) + ",24,0.24,0,8);"
            );
            // Re-style icon box inside active button to match web active item
            if (b.getGraphic() instanceof HBox inner) {
                if (!inner.getChildren().isEmpty() && inner.getChildren().getFirst() instanceof StackPane ib) {
                    ib.setStyle("-fx-background-color:rgba(255,255,255,0.18);-fx-background-radius:12px;");
                }
                if (inner.getChildren().size() > 1 && inner.getChildren().get(1) instanceof Text text) {
                    text.setFill(Color.WHITE);
                }
            }
        } else {
            b.setStyle("-fx-background-color:transparent;-fx-background-radius:999px;-fx-cursor:hand;");
            if (b.getGraphic() instanceof HBox inner) {
                if (!inner.getChildren().isEmpty() && inner.getChildren().getFirst() instanceof StackPane ib) {
                    ib.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.045)" : "rgba(15,23,42,0.055)") + ";-fx-background-radius:12px;");
                }
                if (inner.getChildren().size() > 1 && inner.getChildren().get(1) instanceof Text text) {
                    text.setFill(isDark() ? Color.web("rgba(255,255,255,0.66)") : Color.web("rgba(15,23,42,0.70)"));
                }
            }
        }
    }

    private void switchSection(String section) {
        // Detect rapid navigation spamming
        java.time.Instant now = java.time.Instant.now();
        if (java.time.Duration.between(lastSectionSwitch, now).toMillis() < 500) {
            java.time.Duration interval = java.time.Duration.between(lastSectionSwitch, now);
            Thread.startVirtualThread(() -> activityLogController.logSecurityAlert(
                "NAVIGATION_SPAM",
                "MEDIUM",
                "User is cycling sections too rapidly",
                java.util.Map.of("section", section, "interval_ms", interval.toMillis())
            ));
        }
        lastSectionSwitch = now;

        activeSection = section;
        sectionButtons.forEach((k, b) -> styleSidebarItem(b, k.equals(section)));
        contentArea.getChildren().setAll(getCachedView("SECTION_" + section, () -> buildSection(section)));
        
        // Log sensitive access
        if ("users".equals(section)) {
            logPageViewAsync("/admin/users", "User Management [Sensitive]");
        } else if ("syndicat".equals(section)) {
            logPageViewAsync("/admin/syndicat", "Syndicat Management");
        } else if ("security_ai".equals(section)) {
            logPageViewAsync("/admin/security-ai", "Security AI Dashboard [Critical]");
        } else {
            logPageViewAsync("/admin/" + section, section + " dashboard");
        }
    }

    private void logPageViewAsync(String route, String title) {
        Thread.startVirtualThread(() -> {
            try {
                activityLogController.logPageView(route, title);
            } catch (Exception ignored) {
                // Admin navigation should never block or fail because analytics logging is unavailable.
            }
        });
    }

    // MAIN AREA
    StackPane createMainArea() {
        VBox area = new VBox(0);
        area.setFillWidth(true);
        area.setStyle("-fx-background-color:transparent;");

        if (dashboardBackground == null) {
            dashboardBackground = new ImageBackground(true);
        }

        Region dashboardTint = new Region();
        dashboardTint.setMouseTransparent(true);
        dashboardTint.setStyle(
            "-fx-background-color:" + (isDark()
                ? "radial-gradient(center 18% 0%, radius 85%, rgba(124,58,237,0.18), transparent 46%), linear-gradient(to bottom right, rgba(5,5,10,0.90), rgba(0,0,0,0.94) 58%, rgba(0,0,0,0.97) 100%)"
                : "radial-gradient(center 18% 0%, radius 85%, rgba(124,58,237,0.10), transparent 46%), linear-gradient(to bottom right, rgba(245,247,251,0.88), rgba(255,255,255,0.78) 58%, rgba(236,240,247,0.86) 100%)") + ";"
        );

        contentArea = new VBox(0);
        contentArea.setFillWidth(true);
        contentArea.setMinWidth(0);
        contentArea.setMaxWidth(Double.MAX_VALUE);
        contentArea.getChildren().add(getCachedView("SECTION_" + activeSection, () -> buildSection(activeSection)));

        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        HorizonDesignSystem.styleScrollPane(scroll);
        
        StackPane floatingWrap = new StackPane(scroll);
        VBox.setVgrow(floatingWrap, Priority.ALWAYS);
        
        // Attach floating action buttons (Agent/Chatbot & Messaging)
        // Global Agent buttons are now handled in MainApplication

        area.getChildren().addAll(DashboardShell.buildHeader(this), floatingWrap);

        StackPane dashboardSurface = new StackPane(dashboardBackground.getRoot(), dashboardTint, area);
        dashboardSurface.setStyle("-fx-background-color:#000000;");
        dashboardSurface.setMinSize(0, 0);
        dashboardSurface.setMaxWidth(Double.MAX_VALUE);
        dashboardSurface.setMaxHeight(Double.MAX_VALUE);
        return dashboardSurface;
    }

    // SECTION DISPATCHER
    VBox buildSection(String section) {
        return switch (section) {
            case "users"     -> buildUsersSection();
            case "forum"     -> buildForumSection();
            case "syndicat"  -> buildSyndicatSection();
            case "residence" -> buildResidenceSection();
            case "evenement" -> buildEvenementSection();
            default          -> buildGeneralSection();
        };
    }

    // GENERAL section
    VBox buildGeneralSection() {
        VBox s = new VBox(20); s.setFillWidth(true); s.setPadding(new Insets(24));
        VBox moduleContent = new VBox(20); moduleContent.setFillWidth(true);
        moduleContent.setViewOrder(20);

        HBox topStatsBar = mainSwitcher(key -> {
            moduleContent.getChildren().setAll(getCachedView("GEN_MODULE_" + key, () ->
                "Users".equals(key)     ? buildUsersStatsDashboard() :
                "Forum".equals(key)     ? buildForumStatsDashboard() :
                "Syndicat".equals(key)  ? buildSyndicatStatsDashboard() :
                "Residence".equals(key) ? buildResidenceStatsDashboard() :
                "Evenement".equals(key) ? buildEvenementStatsDashboard() :
                buildGeneralStatsModule()
            ));
        });

        moduleContent.getChildren().add(getCachedView("GEN_MODULE_General", this::buildGeneralStatsModule));
        s.getChildren().addAll(topStatsBar, moduleContent);
        return s;
    }

    private VBox buildGeneralStatsModule() {
        VBox wrap = new VBox(20);
        wrap.setFillWidth(true);

        VBox subContent = new VBox(20);
        subContent.setFillWidth(true);
        subContent.setViewOrder(20);

        HBox subBar = subTabBar(new String[]{"Overview", "Engagement", "System", "Activity"}, key -> {
            subContent.getChildren().setAll(getCachedView("STATS_" + key, () ->
                "Engagement".equals(key) ? buildEngagementContent() :
                "System".equals(key)     ? buildSystemContent()     :
                "Activity".equals(key)   ? buildActivityLogContent() :
                buildGeneralOverview()
            ));
        });

        subContent.getChildren().add(getCachedView("STATS_Overview", this::buildGeneralOverview));
        wrap.getChildren().addAll(subBar, subContent);
        return wrap;
    }

    private VBox buildGeneralOverview() {
        VBox v = new VBox(20); v.setFillWidth(true);
        HBox stats = new HBox(16); stats.setFillHeight(true);
        
        // Show loading skeleton
        addStatCards(stats,
            new String[]{"...","...","...","..."},
            new String[]{"Loading...","Loading...","Loading...","Loading..."},
            new String[]{"0","0","0","0"},
            new String[]{"#808080","#808080","#808080","#808080"}
        );

        Thread.startVirtualThread(() -> {
            try {
                Map<String, Integer> heartbeat = dashboardAdminService.activityHeartbeat();
                javafx.application.Platform.runLater(() -> {
                    stats.getChildren().clear();
                    addStatCards(stats,
                        new String[]{"USR","OK","ACT","PLS"},
                        new String[]{"Total Residents","Active Today","Interactions Today","Community Pulse"},
                        new String[]{
                            String.valueOf(heartbeat.getOrDefault("total_users", 0)),
                            String.valueOf(heartbeat.getOrDefault("active_today", 0)),
                            String.valueOf(heartbeat.getOrDefault("interactions_today", 0)),
                            String.valueOf(heartbeat.getOrDefault("active_week", 0))
                        },
                        new String[]{"#a78bfa","#34d399","#60a5fa","#fbbf24"}
                    );
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        HBox grid = new HBox(16); grid.setFillHeight(true);
        VBox chart = buildActivityChart(); HBox.setHgrow(chart, Priority.ALWAYS);
        VBox topU  = buildTopUsers(); topU.setPrefWidth(290); topU.setMinWidth(290); topU.setMaxWidth(290);
        grid.getChildren().addAll(chart, topU);
        v.getChildren().addAll(stats, grid);
        return v;
    }

    private VBox buildEngagementContent() {
        VBox rows = new VBox(16);
        rows.getChildren().add(new Label("Calculating engagement..."));

        Thread.startVirtualThread(() -> {
            try {
                List<String[]> pages = dashboardAdminService.topPages();
                javafx.application.Platform.runLater(() -> {
                    rows.getChildren().clear();
                    List<String[]> finalPages = pages;
                    if (finalPages.isEmpty()) {
                        finalPages = new ArrayList<>();
                        finalPages.add(new String[]{"/frontend/home", "284"});
                        finalPages.add(new String[]{"/frontend/forum", "211"});
                        finalPages.add(new String[]{"/frontend/profile", "183"});
                    }
                    for (int i = 0; i < finalPages.size(); i++) {
                        String[] p = finalPages.get(i);
                        rows.getChildren().add(buildRowCard(i == 0 ? "#a78bfa" : i == 1 ? "#60a5fa" : "#34d399", p[0], p[1] + " views", String.valueOf(i + 1), 10));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        VBox card = sectionCard();
        card.getChildren().addAll(t("Top Pages - Most Visited Routes", boldFont(), FontWeight.BOLD, 18), rows);

        VBox arrivals = sectionCard();
        arrivals.getChildren().add(t("Recent Arrivals", boldFont(), FontWeight.BOLD, 18));
        
        VBox arrivalRows = new VBox(8);
        arrivalRows.getChildren().add(new Label("Fetching recent users..."));
        arrivals.getChildren().add(arrivalRows);

        Thread.startVirtualThread(() -> {
            try {
                List<User> recentUsers = dashboardAdminService.recentSignups(5);
                javafx.application.Platform.runLater(() -> {
                    arrivalRows.getChildren().clear();
                    if (recentUsers.isEmpty()) {
                        arrivalRows.getChildren().add(buildRowCard("#60a5fa", "No recent signups", "-", null, 8));
                    } else {
                        for (User u : recentUsers) {
                            String fullName = (safe(u.getFirstName()) + " " + safe(u.getLastName())).trim();
                            String role = safe(u.getRoleUser());
                            String joined = u.getCreatedAt() == null ? "-" : u.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd"));
                            arrivalRows.getChildren().add(buildRowCard("#60a5fa", fullName + " ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ " + role, joined, null, 8));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return new VBox(16, card, arrivals);
    }

    private VBox buildSystemContent() {
        VBox logs = new VBox(8);
        logs.getChildren().add(new Label("Streaming system logs..."));

        Thread.startVirtualThread(() -> {
            try {
                List<com.syndicati.models.log.AppEventLog> recent = dashboardAdminService.recentActivityLogs(5);
                javafx.application.Platform.runLater(() -> {
                    logs.getChildren().clear();
                    if (recent.isEmpty()) {
                        for (String[] ev : new String[][]{
                            {"#34d399","Mar 12 09:14","User admin logged in"},
                            {"#fbbf24","Mar 12 08:52","Scheduled email batch: 58 sent"},
                            {"#34d399","Mar 12 07:30","DB backup completed (248 MB)"}
                        }) {
                            logs.getChildren().add(buildRowCard(ev[0], ev[2], ev[1], null, 8));
                        }
                    } else {
                        for (com.syndicati.models.log.AppEventLog ev : recent) {
                            String actor = ev.getUser() == null ? "Anonymous" : safe(ev.getUser().getFirstName()) + " " + safe(ev.getUser().getLastName());
                            String label = safe(ev.getEventType()) + " ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ " + safe(ev.getLevel()) + " ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ " + safe(ev.getOutcome()) + " ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ " + safe(ev.getEntityType()) + " ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ " + actor.trim();
                            String when = ev.getCreatedAt() == null ? "-" : ev.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd HH:mm"));
                            logs.getChildren().add(buildRowCard("#34d399", label, when, null, 8));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        VBox logCard = sectionCard();
        logCard.getChildren().addAll(t("Live Navigation Log", boldFont(), FontWeight.BOLD, 18), logs);

        VBox deviceCard = sectionCard();
        deviceCard.getChildren().add(t("System Access (Last 30 Days)", boldFont(), FontWeight.BOLD, 18));
        
        VBox deviceRowsContainer = new VBox(8);
        deviceRowsContainer.getChildren().add(new Label("Analyzing device data..."));
        deviceCard.getChildren().add(deviceRowsContainer);

        Thread.startVirtualThread(() -> {
            try {
                List<String[]> deviceRows = dashboardAdminService.deviceBreakdown();
                javafx.application.Platform.runLater(() -> {
                    deviceRowsContainer.getChildren().clear();
                    if (deviceRows.isEmpty()) {
                        deviceRowsContainer.getChildren().add(buildRowCard("#34d399", "No device data", "-", null, 8));
                    } else {
                        for (String[] row : deviceRows) {
                            deviceRowsContainer.getChildren().add(buildRowCard("#34d399", safe(row[0]), safe(row[1]) + "%", null, 8));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        HBox grid = new HBox(16, logCard, deviceCard);
        HBox.setHgrow(logCard, Priority.ALWAYS);
        HBox.setHgrow(deviceCard, Priority.ALWAYS);
        return new VBox(20, grid);
    }

    private VBox buildActivityLogContent() {
        return moduleModeView(
            "Activity Log",
            "\u23F1",
            new String[]{"Overview", "Timeline", "Signals", "Security AI"},
            key -> switch (key) {
                case "Timeline" -> getCachedView("ACT_Timeline", this::activityTimelinePane);
                case "Signals" -> getCachedView("ACT_Signals", this::activitySignalsPane);
                case "Security AI" -> getCachedView("ACT_Security", this::activitySecurityAiPane);
                default -> getCachedView("ACT_Overview", this::activityOverviewPane);
            }
        );
    }

    private VBox activitySecurityAiPane() {
        VBox wrap = new VBox(16);
        wrap.setFillWidth(true);

        Label loadingLabel = new Label("Loading Security AI insights...");
        loadingLabel.setTextFill(textMutedColor());
        wrap.getChildren().add(loadingLabel);

        Thread.startVirtualThread(() -> {
            try {
                List<AnomalyResult> anomalies = analyticsController.getRecentAnomalies(8);
                List<SuspiciousActivity> suspiciousUsers = analyticsController.getSuspiciousUsers(8);
                List<Map<String, Object>> featureUsage = analyticsController.getFeatureUsage(6);

                javafx.application.Platform.runLater(() -> {
                    wrap.getChildren().clear();

                    int highAnomalies = 0;
                    int criticalUsers = 0;
                    double peakRisk = 0.0;

                    for (AnomalyResult anomaly : anomalies) {
                        if (anomaly.getAnomalyScore() >= 0.80) {
                            highAnomalies++;
                        }
                    }

                    for (SuspiciousActivity suspicious : suspiciousUsers) {
                        if (suspicious.getRiskScore() != null) {
                            double risk = suspicious.getRiskScore().getOverallRiskScore();
                            peakRisk = Math.max(peakRisk, risk);
                            String severity = safeDefault(suspicious.getRiskScore().getSeverity(), "SAFE");
                            if ("CRITICAL".equalsIgnoreCase(severity) || "HIGH".equalsIgnoreCase(severity)) {
                                criticalUsers++;
                            }
                        }
                    }

                    HBox cards = new HBox(16);
                    addStatCards(
                        cards,
                        new String[]{"ANM", "SUS", "RISK", "FEAT"},
                        new String[]{"Recent Anomalies", "High-Risk Users", "Peak Risk Score", "Tracked Features"},
                        new String[]{
                            String.valueOf(anomalies.size()),
                            String.valueOf(criticalUsers),
                            formatScore(peakRisk),
                            String.valueOf(featureUsage.size())
                        },
                        new String[]{"#ef4444", "#f59e0b", "#a78bfa", "#34d399"}
                    );

                    VBox anomaliesCard = sectionCard();
                    anomaliesCard.getChildren().add(t("Recent LogAI Anomalies", boldFont(), FontWeight.BOLD, 18));
                    if (anomalies.isEmpty()) {
                        anomaliesCard.getChildren().add(buildRowCard("#60a5fa", "No anomaly records yet", "-", null, 8));
                    } else {
                        for (AnomalyResult anomaly : anomalies) {
                            String userText = anomaly.getUserDisplayName() == null ? "anonymous" : anomaly.getUserDisplayName();
                            String title = safeDefault(anomaly.getEventType(), "UNKNOWN_EVENT") + " - " + userText;
                            String value = safeDefault(anomaly.getAnomalyLabel(), "ANOMALY") + " ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ score " + formatScore(anomaly.getAnomalyScore());
                            anomaliesCard.getChildren().add(buildRowCard(anomalyColor(anomaly.getAnomalyScore()), title, value, null, 8));
                        }
                    }

                    VBox suspiciousCard = sectionCard();
                    suspiciousCard.getChildren().add(t("Suspicious Users (7 days)", boldFont(), FontWeight.BOLD, 18));
                    if (suspiciousUsers.isEmpty()) {
                        suspiciousCard.getChildren().add(buildRowCard("#34d399", "No suspicious users detected", "-", null, 8));
                    } else {
                        for (SuspiciousActivity suspicious : suspiciousUsers) {
                            String title = "User #" + safe(suspicious.getUserId() == null ? null : String.valueOf(suspicious.getUserId()))
                                + " ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ " + safe(suspicious.getUserEmail());
                            String severity = suspicious.getRiskScore() == null ? "SAFE" : safeDefault(suspicious.getRiskScore().getSeverity(), "SAFE");
                            double riskScore = suspicious.getRiskScore() == null ? 0.0 : suspicious.getRiskScore().getOverallRiskScore();
                            String value = severity + " ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ risk " + formatScore(riskScore) + " ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ failures " + suspicious.getFailureCount();
                            suspiciousCard.getChildren().add(buildRowCard(severityColor(severity), title, value, null, 8));
                        }
                    }

                    VBox featureCard = sectionCard();
                    featureCard.getChildren().add(t("Feature Usage (Top)", boldFont(), FontWeight.BOLD, 18));
                    if (featureUsage.isEmpty()) {
                        featureCard.getChildren().add(buildRowCard("#60a5fa", "No feature usage data", "-", null, 8));
                    } else {
                        int index = 1;
                        for (Map<String, Object> row : featureUsage) {
                            String feature = safe(row.get("feature") == null ? null : String.valueOf(row.get("feature")));
                            String count = safe(row.get("count") == null ? null : String.valueOf(row.get("count"))) + " events";
                            featureCard.getChildren().add(buildRowCard(index <= 2 ? "#a78bfa" : "#34d399", feature, count, String.valueOf(index), 10));
                            index++;
                        }
                    }

                    VBox noteCard = sectionCard();
                    String noteText = "\u2022 High anomalies (>=0.80): " + highAnomalies
                        + "\n\u2022 Data source: AnalyticsController (anomalies, suspicious users, feature usage)"
                        + "\n\u2022 Recommendation: review HIGH/CRITICAL users and correlated timelines.";
                    Text note = t(noteText, lightFont(), FontWeight.NORMAL, 13);
                    note.setFill(textMutedColor());
                    noteCard.getChildren().addAll(t("AI Security Notes", boldFont(), FontWeight.BOLD, 16), note);

                    HBox upper = new HBox(16, anomaliesCard, suspiciousCard);
                    HBox.setHgrow(anomaliesCard, Priority.ALWAYS);
                    HBox.setHgrow(suspiciousCard, Priority.ALWAYS);

                    HBox lower = new HBox(16, featureCard, noteCard);
                    HBox.setHgrow(featureCard, Priority.ALWAYS);
                    HBox.setHgrow(noteCard, Priority.ALWAYS);

                    wrap.getChildren().addAll(cards, upper, lower);

                    // Honeypot: A hidden button that looks like a critical system reset
                    Button honeypot = new Button("System Reset All Logs");
                    honeypot.setOpacity(0.01); // Almost invisible to humans
                    honeypot.setPrefSize(1, 1); // Tiny but clickable by bots
                    honeypot.setOnAction(_ -> activityLogController.logHoneypotClick("security_ai_reset_bait", java.util.Map.of()));
                    wrap.getChildren().add(honeypot);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return wrap;
    }

    private String formatScore(double value) {
        return String.format("%.2f", Math.max(0.0, Math.min(1.0, value)));
    }

    private String anomalyColor(double score) {
        if (score >= 0.80) {
            return "#ef4444";
        }
        if (score >= 0.60) {
            return "#f59e0b";
        }
        if (score >= 0.40) {
            return "#a78bfa";
        }
        return "#34d399";
    }

    private String severityColor(String severity) {
        String normalized = severity == null ? "SAFE" : severity.toUpperCase();
        if ("CRITICAL".equals(normalized)) {
            return "#ef4444";
        }
        if ("HIGH".equals(normalized)) {
            return "#f97316";
        }
        if ("MEDIUM".equals(normalized)) {
            return "#f59e0b";
        }
        if ("LOW".equals(normalized)) {
            return "#60a5fa";
        }
        return "#34d399";
    }

    private VBox activityOverviewPane() {
        VBox wrap = new VBox(16);
        wrap.setFillWidth(true);

        Label loadingLabel = new Label("Loading activity overview...");
        loadingLabel.setTextFill(textMutedColor());
        wrap.getChildren().add(loadingLabel);

        Thread.startVirtualThread(() -> {
            try {
                Map<String, Integer> stats = dashboardAdminService.activityHeartbeat();
                javafx.application.Platform.runLater(() -> {
                    wrap.getChildren().clear();

                    HBox cards = new HBox(16);
                    addStatCards(cards,
                        new String[]{"USR", "ACT", "CLK", "WKY"},
                        new String[]{"Active Today", "Interactions Today", "Page Views", "Weekly Activity"},
                        new String[]{
                            String.valueOf(stats.getOrDefault("active_today", 0)),
                            String.valueOf(stats.getOrDefault("interactions_today", 0)),
                            String.valueOf(stats.getOrDefault("page_views", 0)),
                            String.valueOf(stats.getOrDefault("active_week", 0))
                        },
                        new String[]{"#60a5fa", "#34d399", "#a78bfa", "#fbbf24"}
                    );

                    HBox grid = new HBox(16);
                    VBox chart = buildActivityChart();
                    HBox.setHgrow(chart, Priority.ALWAYS);
                    VBox topUsers = buildTopUsers();
                    topUsers.setPrefWidth(300);
                    topUsers.setMinWidth(300);
                    topUsers.setMaxWidth(300);
                    grid.getChildren().addAll(chart, topUsers);

                    wrap.getChildren().addAll(cards, grid);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return wrap;
    }

    private VBox activityTimelinePane() {
        VBox wrap = new VBox(14);
        wrap.setFillWidth(true);

        Label loadingLabel = new Label("Loading timeline logs...");
        loadingLabel.setTextFill(textMutedColor());
        wrap.getChildren().add(loadingLabel);

        Thread.startVirtualThread(() -> {
            try {
                List<com.syndicati.models.log.AppEventLog> logs = dashboardAdminService.recentActivityLogs(200);

                javafx.application.Platform.runLater(() -> {
                    wrap.getChildren().clear();
                    List<String[]> rows = new ArrayList<>();

                    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM dd HH:mm");
                    for (com.syndicati.models.log.AppEventLog log : logs) {
                        String subject = safe(log.getEntityType());
                        String event = safe(log.getEventType());
                        String level = safeDefault(log.getLevel(), "INFO");
                        String outcome = safeDefault(log.getOutcome(), "UNKNOWN");
                        String actor = log.getUser() == null ? "Anonymous" : (safe(log.getUser().getFirstName()) + " " + safe(log.getUser().getLastName())).trim();
                        String metadata = shortMetadata(log.getMetadataJson());
                        String when = log.getEventTimestamp() != null
                            ? log.getEventTimestamp().format(dateFmt)
                            : (log.getCreatedAt() == null ? "-" : log.getCreatedAt().format(dateFmt));
                        String trace = blankOrDash(log.getTraceId());
                        String session = blankOrDash(log.getSessionId());
                        String risk = log.getRiskScore() == null ? "-" : log.getRiskScore().toPlainString();
                        String duration = log.getDurationMs() == null ? "-" : log.getDurationMs() + " ms";
                        rows.add(new String[]{event, level, outcome, subject, actor, trace, session, risk, duration, metadata, when});
                    }

                    DashboardTableQueryEngine.QueryState state = new DashboardTableQueryEngine.QueryState(12);
                    TextField searchField = activitySearchField("Search logs by event, entity, user or metadata...");
                    Button sortPill = pillAction("Order: A-Z", false);
                    HBox filterRow = new HBox(6);
                    filterRow.setAlignment(Pos.CENTER_LEFT);
                    HBox headerControls = new HBox(8, searchField, sortPill, filterRow);
                    headerControls.setAlignment(Pos.CENTER_LEFT);
                    VBox tableHost = new VBox();

                    String[][] filters = new String[][]{
                        {"PAGE_VIEW", "Page View"},
                        {"UI_CLICK", "UI Click"},
                        {"FAILURE", "Failure"},
                        {"ERROR", "Error"},
                        {"all", "All"}
                    };
                    for (String[] filter : filters) {
                        String key = filter[0];
                        Button button = pillAction(filter[1], false);
                        button.setOnAction(e -> {
                            state.filterKey = key.equals(state.filterKey) ? "all" : key;
                            state.page = 1;
                            renderActivityTable(rows, state, tableHost, headerControls);
                        });
                        filterRow.getChildren().add(button);
                    }

                    searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                        state.searchTerm = newVal == null ? "" : newVal;
                        state.page = 1;
                        renderActivityTable(rows, state, tableHost, headerControls);
                    });

                    sortPill.setOnAction(e -> {
                        state.ascending = !state.ascending;
                        renderActivityTable(rows, state, tableHost, headerControls);
                    });

                    renderActivityTable(rows, state, tableHost, headerControls);

                    wrap.getChildren().addAll(headerControls, tableHost);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return wrap;
    }

    private void renderActivityTable(List<String[]> rows, DashboardTableQueryEngine.QueryState state, VBox tableHost, HBox headerControls) {
        DashboardTableQueryEngine.QueryResult result = DashboardTableQueryEngine.apply(rows, state, (row, filterKey) -> {
            if (filterKey == null || filterKey.isBlank() || "all".equalsIgnoreCase(filterKey)) {
                return true;
            }
            return matchesTimelineFilter(row, filterKey);
        }, 10);

        VBox table = sectionCard();
        table.setFillWidth(true);
        table.setMaxWidth(Double.MAX_VALUE);

        HBox summary = new HBox(10);
        summary.setAlignment(Pos.CENTER_LEFT);
        Text title = t("Activity Timeline", boldFont(), FontWeight.BOLD, 20);
        title.setFill(textPrimaryColor());
        Text count = t(result.totalRows + " records", lightFont(), FontWeight.NORMAL, 12);
        count.setFill(textMutedColor());
        Region summarySpacer = new Region();
        HBox.setHgrow(summarySpacer, Priority.ALWAYS);
        summary.getChildren().addAll(title, count, summarySpacer);

        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(4);
        grid.setMaxWidth(Double.MAX_VALUE);
        String[] columns = {"Event", "Level", "Outcome", "Subject", "Actor", "When"};
        for (int c = 0; c < columns.length; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(0);
            cc.setFillWidth(true);
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(100.0 / columns.length);
            grid.getColumnConstraints().add(cc);

            Text header = t(columns[c].toUpperCase(), boldFont(), FontWeight.BOLD, 11);
            header.setFill(textMutedColor());
            HBox headerBox = new HBox(header);
            headerBox.setPadding(new Insets(10, 12, 10, 12));
            headerBox.setStyle("-fx-background-color:rgba(255,255,255,0.025);-fx-border-color:rgba(255,255,255,0.065);-fx-border-width:0 0 1 0;");
            grid.add(headerBox, c, 0);
        }

        if (result.pageRows.isEmpty()) {
            HBox empty = new HBox(t("No timeline records found", lightFont(), FontWeight.NORMAL, 13));
            empty.setPadding(new Insets(14, 12, 14, 12));
            empty.setStyle(tableCellStyle("rgba(255,255,255,0.02)", true, true));
            grid.add(empty, 0, 1, columns.length, 1);
        } else {
            int rowIndex = 1;
            for (String[] row : result.pageRows) {
                String[] visible = {
                    safeCell(row, 0),
                    safeCell(row, 1),
                    safeCell(row, 2),
                    safeCell(row, 3),
                    safeCell(row, 4),
                    safeCell(row, 10)
                };
                String bg = (rowIndex & 1) == 0 ? "rgba(255,255,255,0.018)" : "rgba(255,255,255,0.032)";
                for (int c = 0; c < visible.length; c++) {
                    Text value = t(visible[c], lightFont(), FontWeight.NORMAL, 13);
                    value.setFill(c == 0 ? textSecondaryColor() : textMutedColor());
                    HBox cell = new HBox(value);
                    cell.setMinWidth(0);
                    cell.setMaxWidth(Double.MAX_VALUE);
                    value.wrappingWidthProperty().bind(cell.widthProperty().subtract(18));
                    cell.setPadding(new Insets(11, 12, 11, 12));
                    cell.setStyle(tableCellStyle(bg, c == 0, c == visible.length - 1));
                    grid.add(cell, c, rowIndex);
                }
                rowIndex++;
            }
        }

        HBox pager = new HBox(8);
        pager.setAlignment(Pos.CENTER);
        Button prev = pillAction("<", false);
        Button next = pillAction(">", false);
        Text page = t("Page " + result.page + " / " + result.totalPages, lightFont(), FontWeight.NORMAL, 12);
        page.setFill(textMutedColor());
        prev.setDisable(result.page <= 1);
        next.setDisable(result.page >= result.totalPages);
        prev.setOpacity(prev.isDisable() ? 0.55 : 1.0);
        next.setOpacity(next.isDisable() ? 0.55 : 1.0);
        prev.setOnAction(e -> { state.page--; renderActivityTable(rows, state, tableHost, headerControls); });
        next.setOnAction(e -> { state.page++; renderActivityTable(rows, state, tableHost, headerControls); });
        pager.getChildren().addAll(prev, next, page);

        table.getChildren().addAll(summary, grid, pager);
        tableHost.getChildren().setAll(table);
    }

    private String safeCell(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null || row[index].isBlank()) {
            return "-";
        }
        return row[index];
    }

    VBox buildUsersSection() {
        return DashboardUsersSection.build(this);
    }

    VBox buildForumSection() {
        return DashboardForumSection.build(this);
    }

    VBox buildSyndicatSection() {
        return DashboardSyndicatSection.build(this);
    }

    VBox buildResidenceSection() {
        return DashboardResidenceSection.build(this);
    }

    VBox buildEvenementSection() {
        return DashboardEvenementSection.build(this);
    }

    VBox buildUsersStatsDashboard() { return DashboardUsersSection.build(this); }
    VBox buildForumStatsDashboard() { return DashboardForumSection.build(this); }
    VBox buildSyndicatStatsDashboard() { return DashboardSyndicatSection.build(this); }
    VBox buildResidenceStatsDashboard() { return DashboardResidenceSection.build(this); }
    VBox buildEvenementStatsDashboard() { return DashboardEvenementSection.build(this); }

    void addStatCards(HBox target, String[] codes, String[] labels, String[] values, String[] colors) {
        target.setSpacing(16);
        target.setFillHeight(true);
        for (int i = 0; i < labels.length; i++) {
            VBox card = new VBox(10);
            card.setMinWidth(0);
            card.setMaxWidth(Double.MAX_VALUE);
            card.setPadding(new Insets(18, 20, 18, 20));
            String color = colors[Math.min(i, colors.length - 1)];
            card.setStyle("-fx-background-color:rgba(0,0,0,0.34);-fx-border-color:" + rgba(color, 0.55) + ";-fx-border-width:1;-fx-background-radius:18;-fx-border-radius:18;");
            Text code = t(codes[Math.min(i, codes.length - 1)], lightFont(), FontWeight.BOLD, 11);
            code.setFill(Color.web(color));
            Text label = t(labels[i], lightFont(), FontWeight.NORMAL, 13);
            label.setFill(textMutedColor());
            Text value = t(values[Math.min(i, values.length - 1)], boldFont(), FontWeight.BOLD, 26);
            value.setFill(textPrimaryColor());
            Region line = new Region();
            line.setPrefHeight(2);
            line.setMaxWidth(Double.MAX_VALUE);
            line.setStyle("-fx-background-color:" + color + ";-fx-background-radius:999;");
            card.getChildren().addAll(code, label, value, line);
            HBox.setHgrow(card, Priority.ALWAYS);
            target.getChildren().add(card);
        }
    }

    private String rgba(String hex, double alpha) {
        Color c = Color.web(hex);
        return String.format(Locale.US, "rgba(%d,%d,%d,%.3f)", (int) Math.round(c.getRed() * 255), (int) Math.round(c.getGreen() * 255), (int) Math.round(c.getBlue() * 255), alpha);
    }

    VBox buildActivityChart() {
        VBox card = sectionCard();
        card.setMinHeight(320);
        Text title = t("Activity Timeline", boldFont(), FontWeight.BOLD, 18);
        title.setFill(textPrimaryColor());
        Text note = t("Java and web events from the shared activity log", lightFont(), FontWeight.NORMAL, 13);
        note.setFill(textMutedColor());

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        VBox copy = new VBox(2, title, note);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox live = new HBox(t("LIVE", boldFont(), FontWeight.BOLD, 11));
        live.setPadding(new Insets(5, 11, 5, 11));
        live.setStyle("-fx-background-color:rgba(52,211,153,0.16);-fx-border-color:rgba(52,211,153,0.42);-fx-border-width:1;-fx-background-radius:999px;-fx-border-radius:999px;");
        ((Text) live.getChildren().get(0)).setFill(Color.web("#34d399"));
        head.getChildren().addAll(copy, spacer, live);

        VBox list = new VBox(9);
        List<AppEventLog> logs = dashboardAdminService.recentActivityLogs(6);
        if (logs == null || logs.isEmpty()) {
            list.getChildren().add(buildEmptyState("No activity events found yet"));
        } else {
            for (AppEventLog log : logs) {
                list.getChildren().add(activityTimelineRow(log));
            }
        }

        Map<String, Integer> stats = dashboardAdminService.activityHeartbeat();
        HBox footer = new HBox(14);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10, 0, 0, 0));
        footer.setStyle("-fx-border-color:rgba(255,255,255,0.065);-fx-border-width:1 0 0 0;");
        footer.getChildren().addAll(
            compactMetric("Active", String.valueOf(stats.getOrDefault("active_today", 0)), "#34d399"),
            compactMetric("Interactions", String.valueOf(stats.getOrDefault("interactions_today", 0)), "#60a5fa"),
            compactMetric("Views", String.valueOf(stats.getOrDefault("page_views", 0)), "#a78bfa"),
            compactMetric("Week", String.valueOf(stats.getOrDefault("active_week", 0)), "#fbbf24")
        );

        card.getChildren().addAll(head, list, footer);
        return card;
    }

    private HBox activityTimelineRow(AppEventLog log) {
        String category = safeDefault(log.getCategory(), "APPLICATION");
        String outcome = safeDefault(log.getOutcome(), "UNKNOWN");
        String level = safeDefault(log.getLevel(), "INFO");
        boolean failure = "FAILURE".equalsIgnoreCase(outcome)
            || "WARN".equalsIgnoreCase(level)
            || "WARNING".equalsIgnoreCase(level)
            || "ERROR".equalsIgnoreCase(level)
            || "CRITICAL".equalsIgnoreCase(level);
        String accent = failure ? "#f87171" : (category.equalsIgnoreCase("AUTH") || category.equalsIgnoreCase("SECURITY") ? "#fbbf24" : "#60a5fa");

        HBox row = new HBox(12);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(11, 12, 11, 12));
        row.setStyle(
            "-fx-background-color:linear-gradient(to right, " + rgba(accent, 0.13) + ", rgba(255,255,255,0.025));" +
            "-fx-background-radius:14px;" +
            "-fx-border-color:" + rgba(accent, 0.55) + " rgba(255,255,255,0.07) rgba(255,255,255,0.07) " + rgba(accent, 0.55) + ";" +
            "-fx-border-width:1 1 1 3;" +
            "-fx-border-radius:14px;"
        );

        StackPane dot = new StackPane();
        dot.setMinSize(42, 42);
        dot.setPrefSize(42, 42);
        dot.setMaxSize(42, 42);
        dot.setStyle("-fx-background-color:rgba(255,255,255,0.045);-fx-border-color:rgba(255,255,255,0.09);-fx-border-width:1;-fx-background-radius:14px;-fx-border-radius:14px;");
        Text icon = t(category.equalsIgnoreCase("AUTH") || category.equalsIgnoreCase("SECURITY") ? "S" : "A", boldFont(), FontWeight.BOLD, 14);
        icon.setFill(Color.web(accent));
        dot.getChildren().add(icon);

        VBox content = new VBox(5);
        HBox.setHgrow(content, Priority.ALWAYS);
        HBox line = new HBox(8);
        line.setAlignment(Pos.CENTER_LEFT);
        Text event = t(safeDefault(log.getEventType(), "EVENT").replace('_', ' '), boldFont(), FontWeight.BOLD, 13);
        event.setFill(textPrimaryColor());
        Region lineSpacer = new Region();
        HBox.setHgrow(lineSpacer, Priority.ALWAYS);
        LocalDateTime when = log.getEventTimestamp() != null ? log.getEventTimestamp() : log.getCreatedAt();
        Text time = t(when == null ? "-" : when.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")), lightFont(), FontWeight.NORMAL, 11);
        time.setFill(textMutedColor());
        line.getChildren().addAll(event, lineSpacer, time);

        Text message = t(safeDefault(log.getMessage(), safeDefault(log.getAction(), safeDefault(log.getEntityType(), "Activity"))), lightFont(), FontWeight.NORMAL, 12);
        message.setFill(textMutedColor());
        message.setWrappingWidth(620);

        HBox meta = new HBox(6);
        meta.setAlignment(Pos.CENTER_LEFT);
        String actor = log.getUser() == null ? "Visitor" : (safe(log.getUser().getFirstName()) + " " + safe(log.getUser().getLastName())).trim();
        meta.getChildren().addAll(
            smallTag(category, "rgba(255,255,255,0.05)", "#9ca3af"),
            smallTag(outcome, rgba(accent, 0.12), accent),
            smallTag(actor.isBlank() ? "Visitor" : actor, "rgba(255,255,255,0.04)", "#9ca3af")
        );

        content.getChildren().addAll(line, message, meta);
        row.getChildren().addAll(dot, content);
        return row;
    }

    private HBox smallTag(String text, String background, String color) {
        Text label = t(safeDefault(text, "-"), boldFont(), FontWeight.BOLD, 10);
        label.setFill(Color.web(color));
        HBox tag = new HBox(label);
        tag.setPadding(new Insets(3, 8, 3, 8));
        tag.setStyle("-fx-background-color:" + background + ";-fx-border-color:rgba(255,255,255,0.08);-fx-border-width:1;-fx-background-radius:999px;-fx-border-radius:999px;");
        return tag;
    }

    private HBox compactMetric(String label, String value, String color) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        Text valueText = t(value, boldFont(), FontWeight.BOLD, 12);
        valueText.setFill(Color.web(color));
        Text labelText = t(label, lightFont(), FontWeight.NORMAL, 12);
        labelText.setFill(textMutedColor());
        row.getChildren().addAll(valueText, labelText);
        return row;
    }

    VBox buildTopUsers() {
        VBox card = sectionCard();
        Text title = t("Top Users", boldFont(), FontWeight.BOLD, 18);
        title.setFill(textPrimaryColor());
        card.getChildren().add(title);
        int index = 1;
        for (User user : dashboardAdminService.users().stream().limit(4).toList()) {
            String name = (safe(user.getFirstName()) + " " + safe(user.getLastName())).trim();
            card.getChildren().add(buildRowCard(index == 1 ? accentHex() : "#60a5fa", name.isBlank() ? safe(user.getEmailUser()) : name, safe(user.getRoleUser()), null, index));
            index++;
        }
        return card;
    }

    HBox buildRowCard(String color, String title, String subtitle, String meta, int index) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setStyle("-fx-background-color:rgba(255,255,255,0.032);-fx-border-color:rgba(255,255,255,0.075);-fx-border-width:1;-fx-background-radius:14;-fx-border-radius:14;");
        Text rank = t(String.valueOf(index), boldFont(), FontWeight.BOLD, 12);
        rank.setFill(Color.web(color));
        VBox copy = new VBox(2);
        Text titleText = t(safeDefault(title, "-"), boldFont(), FontWeight.BOLD, 13);
        titleText.setFill(textPrimaryColor());
        Text subText = t(safeDefault(subtitle, ""), lightFont(), FontWeight.NORMAL, 12);
        subText.setFill(textMutedColor());
        copy.getChildren().addAll(titleText, subText);
        if (meta != null && !meta.isBlank()) {
            Text metaText = t(meta, lightFont(), FontWeight.NORMAL, 11);
            metaText.setFill(textMutedColor());
            copy.getChildren().add(metaText);
        }
        row.getChildren().addAll(rank, copy);
        return row;
    }

    String safe(Object value) {
        if (value == null) {
            return "";
        }
        String out = String.valueOf(value).trim();
        return out.equalsIgnoreCase("null") ? "" : out;
    }

    String safeDefault(Object value, String fallback) {
        String out = safe(value);
        return out.isBlank() ? fallback : out;
    }

    private String blankOrDash(String value) {
        return safeDefault(value, "-");
    }

    private String shortMetadata(String metadata) {
        String value = safe(metadata).replace('\n', ' ').replace('\r', ' ');
        return value.length() <= 90 ? value : value.substring(0, 87) + "...";
    }

    private TextField activitySearchField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(300);
        field.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        field.setStyle(HorizonDesignSystem.webServiceInput(16, false));
        HorizonDesignSystem.installFocusGlow(field);
        return field;
    }

    private boolean matchesTimelineFilter(String[] row, String filterKey) {
        if (row == null || filterKey == null || filterKey.isBlank() || "all".equalsIgnoreCase(filterKey)) {
            return true;
        }
        String normalized = filterKey.toLowerCase(Locale.ROOT);
        for (String cell : row) {
            if (cell != null && cell.toLowerCase(Locale.ROOT).contains(normalized)) {
                return true;
            }
        }
        return false;
    }

    private VBox glassCard() {
        VBox c = new VBox(14);
        c.setPadding(new Insets(20));
        c.setFillWidth(true);
        c.setMaxWidth(Double.MAX_VALUE);
        c.setStyle(HorizonDesignSystem.webDashboardGlass(24, false));
        HorizonDesignSystem.installWebLift(c);
        return c;
    }

    VBox dataTableWithCrud(String title, String entityLabel, String[] cols, String[][] rows, boolean allowAdd) {
        return dataTableWithCrud(title, entityLabel, cols, rows, allowAdd, null);
    }

    VBox dataTableWithCrud(String title, String entityLabel, String[] cols, String[][] rows, boolean allowAdd, Node headerControls) {
        CrudSpec spec = crudSpec(title, entityLabel);
        StackPane faceContainer = new StackPane();
        faceContainer.setMinWidth(0);
        faceContainer.setMaxWidth(Double.MAX_VALUE);

        VBox tableFace = new VBox();
        tableFace.setMinWidth(0);
        tableFace.setMaxWidth(Double.MAX_VALUE);
        tableFace.setMaxHeight(Double.MAX_VALUE);
        tableFace.setFillWidth(true);
        VBox modalFace = new VBox();
        modalFace.setMinWidth(0);
        modalFace.setMaxWidth(Double.MAX_VALUE);
        modalFace.setMaxHeight(Double.MAX_VALUE);
        modalFace.setFillWidth(true);
        modalFace.setVisible(false);
        modalFace.setManaged(false);

        VBox card = glassCard();
        card.setPadding(new Insets(22, 24, 22, 24));

        VBox head = new VBox(14);
        head.setFillWidth(true);
        HBox titleLine = new HBox(10);
        titleLine.setAlignment(Pos.CENTER_LEFT);
        Text heading = t(tableDisplayTitle(title), boldFont(), FontWeight.BOLD, 22);
        heading.setFill(textPrimaryColor());
        Text subtitle = t("Live module data", lightFont(), FontWeight.NORMAL, 13);
        subtitle.setFill(textMutedColor());
        VBox titleCopy = new VBox(2, heading, subtitle);
        titleCopy.setMinWidth(0);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleLine.getChildren().addAll(titleCopy, spacer);
        if (allowAdd) {
            Button add = pillAction("+ " + spec.addButtonLabel, true);
            add.setMinWidth(126);
            add.setMaxWidth(170);
            add.setOnAction(e -> switchToModalFace(faceContainer, spec, entityLabel, "add", cols, null));
            titleLine.getChildren().add(add);
        }
        head.getChildren().add(titleLine);
        if (headerControls != null) {
            if (headerControls instanceof Region region) {
                region.setMinWidth(0);
                region.setMaxWidth(Double.MAX_VALUE);
            }
            head.getChildren().add(headerControls);
        }

        GridPane table = new GridPane();
        table.setHgap(0);
        table.setVgap(4);
        table.setMinWidth(0);
        table.setMaxWidth(Double.MAX_VALUE);
        int colCount = cols.length + 1;
        for (int i = 0; i < colCount; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(0);
            cc.setFillWidth(true);
            if (i == cols.length) {
                cc.setMinWidth(104);
                cc.setPrefWidth(126);
                cc.setMaxWidth(142);
                cc.setHgrow(Priority.NEVER);
            } else {
                cc.setHgrow(Priority.ALWAYS);
                cc.setPercentWidth(100.0 / colCount);
            }
            table.getColumnConstraints().add(cc);
        }

        for (int c = 0; c < cols.length; c++) {
            Text label = t(cols[c].toUpperCase(), boldFont(), FontWeight.BOLD, 12);
            label.setFill(textMutedColor());
            HBox cell = new HBox(label);
            cell.setMinWidth(0);
            cell.setMaxWidth(Double.MAX_VALUE);
            cell.setPadding(new Insets(12));
            cell.setStyle("-fx-background-color:rgba(255,255,255,0.025);-fx-border-color:rgba(255,255,255,0.065);-fx-border-width:0 0 1 0;");
            table.add(cell, c, 0);
        }
        Text actionLabel = t("ACTIONS", boldFont(), FontWeight.BOLD, 12);
        actionLabel.setFill(textMutedColor());
        HBox actionHead = new HBox(actionLabel);
        actionHead.setMinWidth(104);
        actionHead.setMaxWidth(142);
        actionHead.setPadding(new Insets(12));
        actionHead.setStyle("-fx-background-color:rgba(255,255,255,0.025);-fx-border-color:rgba(255,255,255,0.065);-fx-border-width:0 0 1 0;");
        table.add(actionHead, cols.length, 0);

        String[][] sourceRows = rows == null ? new String[0][] : rows;
        int maxRows = Math.min(sourceRows.length, 10);
        if (maxRows == 0) {
            HBox empty = new HBox(t("No records found", lightFont(), FontWeight.NORMAL, 14));
            empty.setPadding(new Insets(14, 12, 14, 12));
            empty.setStyle(tableCellStyle("rgba(255,255,255,0.02)", true, true));
            table.add(empty, 0, 1, colCount, 1);
        }
        for (int r = 0; r < maxRows; r++) {
            String[] row = sourceRows[r];
            boolean hasHiddenId = row != null && row.length > cols.length;
            int offset = hasHiddenId ? 1 : 0;
            String bg = (r & 1) == 0 ? "rgba(255,255,255,0.018)" : "rgba(255,255,255,0.032)";
            for (int c = 0; c < cols.length; c++) {
                String value = row != null && offset + c < row.length ? safe(row[offset + c]) : "";
                Node content;
                if (isImageFieldLabel(cols[c]) && !value.isBlank() && !"-".equals(value)) {
                    content = tableThumbnail(value);
                } else {
                    Text text = t(value, lightFont(), FontWeight.NORMAL, 14);
                    text.setFill(c == 0 ? textSecondaryColor() : textMutedColor());
                    content = text;
                }
                HBox cell = new HBox(content);
                cell.setMinWidth(0);
                cell.setMaxWidth(Double.MAX_VALUE);
                if (content instanceof Text text) {
                    text.wrappingWidthProperty().bind(cell.widthProperty().subtract(18));
                }
                cell.setPadding(new Insets(11, 12, 11, 12));
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.setStyle(tableCellStyle(bg, c == 0, false));
                table.add(cell, c, r + 1);
            }
            HBox actions = new HBox(6);
            actions.setAlignment(Pos.CENTER_LEFT);
            actions.setMinWidth(104);
            actions.setMaxWidth(142);
            actions.setPadding(new Insets(10));
            actions.setStyle(tableCellStyle(bg, false, true));
            Button details = pillAction("Details", true);
            details.setMinWidth(82);
            details.setMaxWidth(112);
            String[] rowRef = row;
            details.setOnAction(e -> switchToModalFace(faceContainer, spec, entityLabel, "view", cols, rowRef));
            actions.getChildren().add(details);
            table.add(actions, cols.length, r + 1);
        }

        HBox pager = new HBox(8);
        pager.setAlignment(Pos.CENTER);
        Text page = t("Page 1 / " + Math.max(1, (int) Math.ceil(sourceRows.length / 10.0)) + " | " + sourceRows.length + " records", lightFont(), FontWeight.NORMAL, 12);
        page.setFill(textMutedColor());
        pager.getChildren().add(page);

        card.getChildren().addAll(head, table, pager);
        tableFace.getChildren().add(card);
        faceContainer.getChildren().addAll(tableFace, modalFace);
        faceContainer.setUserData(modalFace);
        StackPane.setAlignment(tableFace, Pos.TOP_CENTER);
        StackPane.setAlignment(modalFace, Pos.TOP_CENTER);

        VBox wrap = new VBox(faceContainer);
        wrap.setMinWidth(0);
        wrap.setMaxWidth(Double.MAX_VALUE);
        return wrap;
    }
    private String tableCellStyle(String background, boolean leftEdge, boolean rightEdge) {
        String radius = leftEdge && rightEdge ? "12px" : (leftEdge ? "12px 0 0 12px" : (rightEdge ? "0 12px 12px 0" : "0"));
        return "-fx-background-color:" + background + ";" +
            "-fx-background-radius:" + radius + ";" +
            "-fx-border-color:rgba(255,255,255,0.055);" +
            "-fx-border-width:0 0 1 0;";
    }


    private String tableDisplayTitle(String title) {
        return switch (title) {
            case "Users Table" -> "Users";
            case "User Profile Data" -> "Profile";
            case "Onboarding Data" -> "Onboarding";
            case "Forum Publications" -> "Publications";
            case "Forum Comments" -> "Commentaires";
            case "Forum Reactions" -> "Reactions";
            case "Events" -> "Evenements";
            case "Event Participations" -> "Participations";
            case "Syndicat Reclamations" -> "Reclamations";
            case "Syndicat Responses" -> "Responses";
            default -> title;
        };
    }
    private CrudSpec crudSpec(String title, String entityLabel) {
        CrudSpec s = CrudSpec.defaults(entityLabel);

        switch (title) {
            case "Users Table":
                s.viewTitle = "User Details";
                s.viewSubtitle = "View complete user information";
                s.editTitle = "Edit User";
                s.editSubtitle = "Current data is pre-filled. Edit and save.";
                s.addTitle = "Add User";
                s.addSubtitle = "Create a new user. Same fields as sign up, plus role and verified.";
                s.cancelLabel = "Discard";
                s.saveEditLabel = "Update Account";
                s.saveAddLabel = "Create Account";
                s.addButtonLabel = "Add User";
                break;
            case "User Profile Data":
                s.viewTitle = "Profile Details";
                s.viewSubtitle = "Comprehensive profile information with user relationship";
                s.editTitle = "Edit Profile";
                s.editSubtitle = "Modify profile fields (locale, theme, timezone, avatar, bio).";
                s.addTitle = "Add Profile";
                s.addSubtitle = "Create a new profile for a user.";
                s.cancelLabel = "Cancel";
                s.saveEditLabel = "Save Changes";
                s.saveAddLabel = "Create Profile";
                s.addButtonLabel = "Add Profile";
                break;
            case "Onboarding Data":
                s.viewTitle = "Onboarding Details";
                s.viewSubtitle = "Comprehensive onboarding information";
                s.editTitle = "Modify onboarding choices";
                s.editSubtitle = "Update language, theme, preferences and suggestions.";
                s.cancelLabel = "Close Wizard";
                s.saveEditLabel = "Save changes";
                s.viewDeleteLabel = null;
                break;
            case "Forum Publications":
                s.viewTitle = "Publication Details";
                s.viewSubtitle = "View complete publication information";
                s.editTitle = "Edit Publication";
                s.editSubtitle = "Modify the publication and save changes.";
                s.addTitle = "Add Publication";
                s.addSubtitle = "Create a new forum publication.";
                s.saveEditLabel = "Update Publication";
                s.saveAddLabel = "Post Publication";
                s.addButtonLabel = "Add Publication";
                break;
            case "Forum Comments":
                s.viewTitle = "Comment Details";
                s.viewSubtitle = "View comment content and metadata.";
                s.editTitle = "Edit Comment";
                s.editSubtitle = "Modify the comment text and save changes.";
                s.saveEditLabel = "Update Comment";
                break;
            case "Forum Reactions":
                s.viewTitle = "Reaction Details";
                s.viewSubtitle = "View reaction content and metadata.";
                s.editTitle = "Edit Reaction";
                s.editSubtitle = "Modify reaction kind and details.";
                s.saveEditLabel = "Update Reaction";
                break;
            case "Reclamations":
                s.viewTitle = "Reclamation Details";
                s.viewSubtitle = "View complete reclamation information";
                s.editTitle = "Edit Reclamation";
                s.editSubtitle = "Modify reclamation status and details.";
                s.saveEditLabel = "Update Status";
                break;
            case "Responses":
                s.viewTitle = "Response Details";
                s.viewSubtitle = "View response content and metadata.";
                break;
            case "Residences":
                s.viewTitle = "Residence Details";
                s.viewSubtitle = "Full information overview";
                s.editTitle = "Edit Residence";
                s.editSubtitle = "Update residence properties";
                s.addTitle = "Add Residence";
                s.addSubtitle = "Create a new entry";
                s.saveEditLabel = "Update Residence";
                s.saveAddLabel = "Create Residence";
                s.addButtonLabel = "Add Residence";
                break;
            case "Appartements":
                s.viewTitle = "Appartement Details";
                s.viewSubtitle = "Information overview";
                s.editTitle = "Edit Appartement";
                s.editSubtitle = "Update details";
                s.addTitle = "Add Appartement";
                s.addSubtitle = "Add to inventory";
                s.saveEditLabel = "Update Appartement";
                s.saveAddLabel = "Create Appartement";
                s.addButtonLabel = "Add Appartement";
                break;
            case "Maintenance":
                s.viewTitle = "Maintenance Details";
                s.viewSubtitle = "Full analysis & conditions";
                s.editTitle = "Edit Maintenance";
                s.editSubtitle = "Update conditions and recommendations";
                s.addTitle = "Add Maintenance";
                s.addSubtitle = "Create a new maintenance record";
                s.cancelLabel = "Back to List";
                s.saveEditLabel = "Update Maintenance";
                s.saveAddLabel = "Create Maintenance";
                s.addButtonLabel = "Add Maintenance";
                s.viewDeleteLabel = "Delete";
                break;
            case "Evenements":
                s.viewTitle = "Event Details";
                s.viewSubtitle = "Full information overview";
                s.editTitle = "Edit Event";
                s.editSubtitle = "Update event properties";
                s.addTitle = "Add Event";
                s.addSubtitle = "Create a new entry";
                s.saveEditLabel = "Update Event";
                s.saveAddLabel = "Post Event";
                s.addButtonLabel = "Add Event";
                break;
            case "Participations":
                s.viewTitle = "Participation Details";
                s.viewSubtitle = "Information overview";
                s.editTitle = "Edit Participation";
                s.editSubtitle = "Update record";
                s.saveEditLabel = "Update Participation";
                break;
            default:
                break;
        }

        return s;
    }

    Button pillAction(String text, boolean primary) {
        Button b = new Button(text);
        b.setFont(Font.font(boldFont(), FontWeight.BOLD, 12));
        b.setPadding(new Insets(9, 16, 9, 16));
        stylePillActionButton(b, primary, false);
        b.setOnMouseEntered(_ -> stylePillActionButton(b, primary, true));
        b.setOnMouseExited(_ -> stylePillActionButton(b, primary, false));
        HorizonDesignSystem.installButtonMotion(b);
        return b;
    }

    private void stylePillActionButton(Button b, boolean primary, boolean hover) {
        String background = primary
            ? accentGradient()
            : (hover ? (isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.07)") : "transparent");
        String border = primary
            ? accentRgba(hover ? 0.50 : 0.34)
            : (isDark() ? "rgba(255,255,255," + (hover ? "0.16" : "0.10") + ")" : "rgba(15,23,42," + (hover ? "0.18" : "0.12") + ")");
        String textColor = primary ? "white" : (isDark() ? "rgba(255,255,255,0.78)" : "rgba(15,23,42,0.82)");
        b.setStyle(
            "-fx-background-color:" + background + ";" +
            "-fx-border-color:" + border + ";" +
            "-fx-text-fill:" + textColor + ";" +
            "-fx-border-width:1;" +
            "-fx-background-radius:100px;" +
            "-fx-border-radius:100px;" +
            "-fx-cursor:hand;" +
            (hover ? "-fx-effect:dropshadow(one-pass-box," + (primary ? accentRgba(0.34) : "rgba(0,0,0,0.18)") + ",16,0,0,6);" : "")
        );
    }

    private void switchToModalFace(StackPane container, CrudSpec spec, String entityLabel, String mode, String[] cols, String[] rowData) {
        VBox modalFace = (VBox) container.getUserData();
        String title = "view".equals(mode) ? spec.viewTitle : ("edit".equals(mode) ? spec.editTitle : spec.addTitle);
        String subtitle = "view".equals(mode) ? spec.viewSubtitle : ("edit".equals(mode) ? spec.editSubtitle : spec.addSubtitle);

        modalFace.getChildren().clear();
        
        VBox modalCard = glassCard();
        modalCard.setSpacing(24);
        modalCard.setFillWidth(true);
        modalCard.setPadding(new Insets(28, 32, 28, 32));
        modalCard.setMaxWidth(Double.MAX_VALUE);

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        VBox tWrap = new VBox(3);
        Text hTitle = t(title, boldFont(), FontWeight.BOLD, 30); hTitle.setFill(textPrimaryColor());
        Text hSub = t(subtitle, lightFont(), FontWeight.NORMAL, 14); hSub.setFill(textMutedColor());
        tWrap.getChildren().addAll(hTitle, hSub);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = new Button("x");
        close.setMinSize(42, 42);
        close.setPrefSize(42, 42);
        close.setMaxSize(42, 42);
        close.setPadding(Insets.EMPTY);
        close.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.04)" : "rgba(15,23,42,0.06)") + ";-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.10)" : "rgba(15,23,42,0.10)") + ";-fx-border-width:1;-fx-background-radius:999;-fx-border-radius:999;-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.72)" : "rgba(15,23,42,0.72)") + ";-fx-cursor:hand;");
        close.setOnAction(_ -> switchToTableFace(container));
        head.getChildren().addAll(tWrap, spacer, close);

        VBox fields = buildModalFields(entityLabel, mode, cols, rowData);

        ScrollPane formScroll = new ScrollPane(fields);
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setPrefViewportHeight(430);
        formScroll.setMaxHeight(520);
        formScroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        VBox.setVgrow(formScroll, Priority.ALWAYS);

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);
        
        if ("view".equals(mode)) {
            Button edit = pillAction("Modify " + entityLabel, false);
            edit.setOnAction(ignored -> switchToModalFace(container, spec, entityLabel, "edit", cols, rowData));
            actions.getChildren().add(edit);
            if (spec.viewDeleteLabel != null) {
                Button del = dangerAction(spec.viewDeleteLabel);
                del.setOnAction(ignored -> {
                    if (dashboardAdminService.deleteEntity(entityLabel, rowData)) {
                        viewCache.clear();
                        switchSection(activeSection);
                    } else {
                        switchToTableFace(container);
                    }
                });
                actions.getChildren().add(del);
            }
        } else {
            Button cancel = pillAction(spec.cancelLabel, false);
            cancel.setOnAction(_ -> switchToTableFace(container));
            actions.getChildren().add(cancel);
            String saveLabel = "add".equals(mode) ? spec.saveAddLabel : spec.saveEditLabel;
            Button save = pillAction(saveLabel, true);
            save.setOnAction(ignored -> {
                if (dashboardAdminService.saveEntity(entityLabel, mode, rowData, fields)) {
                    viewCache.clear();
                    switchSection(activeSection);
                } else {
                    switchToTableFace(container);
                }
            });
            actions.getChildren().add(save);
        }

        modalCard.getChildren().addAll(head, formScroll, actions);
        modalFace.getChildren().add(modalCard);
        VBox.setVgrow(modalCard, Priority.ALWAYS);

        ObservableList<Node> children = container.getChildren();
        VBox tableCard = (VBox) children.getFirst();
        animateFaceOut(tableCard, () -> {
            tableCard.setVisible(false);
            tableCard.setManaged(false);
            modalFace.setManaged(true);
            modalFace.setVisible(true);
            animateFaceIn(modalFace);
        });
    }

    private void switchToTableFace(StackPane container) {
        ObservableList<Node> children = container.getChildren();
        VBox tableCard = (VBox) children.getFirst();
        VBox modalFace = (VBox) children.get(1);

        animateFaceOut(modalFace, () -> {
            modalFace.setVisible(false);
            modalFace.setManaged(false);
            tableCard.setVisible(true);
            tableCard.setManaged(true);
            animateFaceIn(tableCard);
        });
    }

    private void animateFaceIn(Node node) {
        node.setOpacity(0.0);
        node.setTranslateY(34);
        node.setScaleX(0.985);
        node.setScaleY(0.985);

        FadeTransition fade = new FadeTransition(Duration.millis(420), node);
        fade.setToValue(1.0);
        fade.setInterpolator(HorizonDesignSystem.WEB_POP);
        TranslateTransition slide = new TranslateTransition(Duration.millis(420), node);
        slide.setToY(0);
        slide.setInterpolator(HorizonDesignSystem.WEB_POP);
        ScaleTransition scale = new ScaleTransition(Duration.millis(420), node);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(HorizonDesignSystem.WEB_POP);
        fade.play();
        slide.play();
        scale.play();
    }

    private void animateFaceOut(Node node, Runnable after) {
        FadeTransition fade = new FadeTransition(Duration.millis(140), node);
        fade.setToValue(0.0);
        fade.setInterpolator(HorizonDesignSystem.WEB_EASE);
        fade.setOnFinished(_ -> {
            node.setTranslateY(0);
            node.setScaleX(1.0);
            node.setScaleY(1.0);
            after.run();
        });
        fade.play();
    }

    private VBox buildModalFields(String entityLabel, String mode, String[] cols, String[] rowData) {
        VBox fields = new VBox(10);

        fields.getChildren().add(sectionTitle(
            "view".equals(mode) ? "Overview" :
            "edit".equals(mode) ? "Editable Fields" :
            "Create New Record"
        ));
        if ("view".equals(mode)) {
            fields.getChildren().add(metaStrip(rowData));
            if ("Publication".equalsIgnoreCase(entityLabel)) {
                fields.getChildren().add(publicationInsightCard(rowData));
            }
        }

        boolean hasHiddenId = (rowData != null && cols.length < rowData.length);
        for (int i = 0; i < cols.length; i++) {
            int dataIdx = hasHiddenId ? i + 1 : i;
            String val = (rowData != null && dataIdx < rowData.length) ? rowData[dataIdx] : "";
            boolean editable = "edit".equals(mode) || "add".equals(mode);
            fields.getChildren().add(fieldRow(entityLabel, mode, cols[i], val, editable));
        }

        if ("edit".equals(mode) || "add".equals(mode)) {
            fields.getChildren().add(sectionTitle("Flags & Metadata"));
            fields.getChildren().add(infoChipRow());
            fields.getChildren().add(notesBox());
        }

        if ("Reponse".equalsIgnoreCase(entityLabel) && ("edit".equals(mode) || "add".equals(mode))) {
            setupReponseConditionalFields(fields);
        }

        return fields;
    }

    private void setupReponseConditionalFields(VBox fields) {
        ComboBox<String> userSelect = null;
        ComboBox<String> recSelect = null;

        for (Node rowNode : fields.getChildren()) {
            if (rowNode instanceof VBox row) {
                for (Node child : row.getChildren()) {
                    if (child instanceof ComboBox<?> cb) {
                        if ("reponse-user-select".equals(cb.getId())) userSelect = (ComboBox<String>) cb;
                        if ("reponse-reclamation-select".equals(cb.getId())) recSelect = (ComboBox<String>) cb;
                    }
                }
            }
        }

        if (userSelect != null && recSelect != null) {
            final ComboBox<String> finalRecSelect = recSelect;
            final ComboBox<String> finalUserSelect = userSelect;

            // Initial state
            if (userSelect.getValue() == null || userSelect.getValue().isBlank() || "-".equals(userSelect.getValue())) {
                finalRecSelect.setDisable(true);
            }

            userSelect.valueProperty().addListener((obs, oldVal, newVal) -> {
                String currentRec = finalRecSelect.getValue();
                if (newVal == null || newVal.isBlank() || "-".equals(newVal)) {
                    finalRecSelect.setDisable(true);
                    finalRecSelect.getItems().clear();
                } else {
                    finalRecSelect.setDisable(false);
                    finalRecSelect.getItems().setAll(reponseReclamationOptionsByUser(newVal));
                    if (currentRec != null && finalRecSelect.getItems().contains(currentRec)) {
                        finalRecSelect.setValue(currentRec);
                    }
                }
            });
        }
    }

    private Text sectionTitle(String text) {
        Text t = t(text, boldFont(), FontWeight.BOLD, 16);
        t.setFill(textSecondaryColor());
        return t;
    }

    private VBox publicationInsightCard(String[] rowData) {
        // Since we added an ID at index 0, all data indices are shifted by 1
        String imagePath = rowData != null && rowData.length > 4 ? rowData[4] : "";
        int likes = parseIntCell(rowData, 6);
        int dislikes = parseIntCell(rowData, 7);
        int bookmarks = parseIntCell(rowData, 8);
        int reports = parseIntCell(rowData, 9);
        String emojiSummary = rowData != null && rowData.length > 10 ? rowData[10] : "None";

        VBox wrap = new VBox(14);
        wrap.getChildren().add(sectionTitle("Publication Insights"));

        HBox mediaRow = new HBox(16);
        mediaRow.setAlignment(Pos.CENTER_LEFT);

        VBox thumbnailCard = new VBox();
        thumbnailCard.setPadding(new Insets(10));
        thumbnailCard.setStyle(
            "-fx-background-color:rgba(255,255,255,0.03);" +
            "-fx-border-color:rgba(255,255,255,0.08);" +
            "-fx-border-width:1;" +
            "-fx-background-radius:14px;" +
            "-fx-border-radius:14px;"
        );

        Node thumbnail = publicationThumbnail(imagePath);
        thumbnailCard.getChildren().add(thumbnail);

        VBox mediaText = new VBox(4);
        Text mediaTitle = t("Publication media", lightFont(), FontWeight.NORMAL, 12);
        mediaTitle.setFill(textMutedColor());
        Text mediaValue = t(imagePath == null || imagePath.isBlank() || "-".equals(imagePath) ? "No media attached" : imagePath, lightFont(), FontWeight.NORMAL, 13);
        mediaValue.setFill(textSecondaryColor());
        mediaValue.setWrappingWidth(300);
        mediaText.getChildren().addAll(mediaTitle, mediaValue);

        mediaRow.getChildren().addAll(thumbnailCard, mediaText);

        // Ratio Bar (Mirroring ForumPageView)
        VBox ratioBox = new VBox(6);
        int totalReactions = likes + dislikes;
        double ratio = totalReactions == 0 ? 0 : (double) likes / totalReactions;
        String ratioText = totalReactions == 0 ? "0%" : (int)(ratio * 100) + "%";
        
        HBox ratioHeader = new HBox(4);
        ratioHeader.setAlignment(Pos.CENTER_LEFT);
        Text ratioTitle = t("Reaction Ratio", lightFont(), FontWeight.SEMI_BOLD, 12);
        ratioTitle.setFill(textMutedColor());
        Text ratioVal = t(ratioText, boldFont(), FontWeight.BOLD, 12);
        ratioVal.setFill(Color.web(accentHex()));
        ratioHeader.getChildren().addAll(ratioTitle, ratioVal);

        HBox ratioBarContainer = new HBox();
        ratioBarContainer.setPrefSize(200, 6);
        ratioBarContainer.setMaxSize(200, 6);
        ratioBarContainer.setStyle("-fx-background-color:rgba(255,255,255,0.05); -fx-background-radius:100px; -fx-overflow:hidden;");
        
        Region likesBar = new Region();
        likesBar.setStyle("-fx-background-color:#22c55e; -fx-background-radius:100px 0 0 100px;");
        likesBar.setPrefHeight(6);
        likesBar.setPrefWidth(200 * ratio);
        
        Region dislikesBar = new Region();
        dislikesBar.setStyle("-fx-background-color:#ef4444; -fx-background-radius:" + (ratio >= 1.0 ? "0" : "0 100px 100px 0") + ";");
        dislikesBar.setPrefHeight(6);
        dislikesBar.setPrefWidth(200 * (1.0 - ratio));
        
        ratioBarContainer.getChildren().addAll(likesBar, dislikesBar);
        ratioBox.getChildren().addAll(ratioHeader, ratioBarContainer);

        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER_LEFT);
        chips.getChildren().addAll(
            chip("\uD83D\uDC4D " + likes, true),
            chip("\uD83D\uDC4E " + dislikes, false),
            chip("\uD83D\uDCAF " + bookmarks, false),
            chip("\u26A0\uFE0F " + reports, false)
        );

        VBox emojiSection = new VBox(6);
        Text emojiTitle = t("Emoji reactions", lightFont(), FontWeight.NORMAL, 12);
        emojiTitle.setFill(textMutedColor());
        
        FlowPane emojiFlow = new FlowPane(10, 10);
        emojiFlow.setAlignment(Pos.CENTER_LEFT);
        
        if (emojiSummary == null || emojiSummary.isBlank() || "None".equals(emojiSummary)) {
            Text none = t("No emoji reactions", lightFont(), FontWeight.NORMAL, 13);
            none.setFill(textMutedColor());
            emojiFlow.getChildren().add(none);
        } else {
            String[] parts = emojiSummary.split(", ");
            for (String part : parts) {
                // Format: "?? x1"
                int xIndex = part.lastIndexOf(" x");
                if (xIndex > 0) {
                    String emojiStr = part.substring(0, xIndex).trim();
                    String countStr = part.substring(xIndex + 2).trim();
                    
                    HBox item = new HBox(4);
                    item.setAlignment(Pos.CENTER_LEFT);
                    item.setPadding(new Insets(4, 8, 4, 8));
                    item.setStyle("-fx-background-color:rgba(255,255,255,0.04); -fx-background-radius:8px; -fx-border-color:rgba(255,255,255,0.08); -fx-border-radius:8px;");
                    
                    Node icon = twemojiIcon(emojiStr, 18);
                    Text count = t(countStr, lightFont(), FontWeight.BOLD, 12);
                    count.setFill(textSecondaryColor());
                    
                    item.getChildren().addAll(icon, count);
                    emojiFlow.getChildren().add(item);
                }
            }
        }
        emojiSection.getChildren().addAll(emojiTitle, emojiFlow);

        wrap.getChildren().addAll(mediaRow, ratioBox, chips, emojiSection);
        return wrap;
    }

    private Node twemojiIcon(String emoji, int size) {
        if (emoji == null || emoji.isBlank()) return new Text("");
        try {
            String hex = Integer.toHexString(emoji.codePointAt(0));
            if ("2764".equals(hex) || "ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚ÂÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¤ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¯ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¸ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â".equals(emoji)) hex = "2764"; // Fix for heart variant
            String url = "https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/" + hex + ".png";
            
            Image img = new Image(url, true);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(size);
            iv.setFitHeight(size);
            iv.setSmooth(true);
            return iv;
        } catch (Exception e) {
            return t(emoji, lightFont(), FontWeight.NORMAL, size);
        }
    }

    private Node publicationThumbnail(String imagePath) {
        VBox placeholder = new VBox();
        placeholder.setPrefSize(120, 78);
        placeholder.setMinSize(120, 78);
        placeholder.setMaxSize(120, 78);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setStyle(
            "-fx-background-color:rgba(255,255,255,0.04);" +
            "-fx-background-radius:12px;" +
            "-fx-border-color:rgba(255,255,255,0.08);" +
            "-fx-border-radius:12px;" +
            "-fx-border-width:1;"
        );

        if (imagePath == null || imagePath.isBlank() || "-".equals(imagePath)) {
            Text none = t("No image", lightFont(), FontWeight.NORMAL, 12);
            none.setFill(textMutedColor());
            placeholder.getChildren().add(none);
            return placeholder;
        }

        // Try multiple paths to find the image
        Image image = ImageLoaderUtil.loadImage(imagePath);
        if (image == null || image.isError()) {
            image = ImageLoaderUtil.loadImage("uploads/" + imagePath);
        }
        if (image == null || image.isError()) {
            image = ImageLoaderUtil.loadImage("uploads/forum_images/" + imagePath);
        }
        if (image == null || image.isError()) {
            image = ImageLoaderUtil.loadImage("uploads/profile_images/" + imagePath);
        }
        
        if (image == null || image.isError()) {
            Text none = t("Image unavailable", lightFont(), FontWeight.NORMAL, 12);
            none.setFill(textMutedColor());
            placeholder.getChildren().add(none);
            return placeholder;
        }

        ImageView preview = new ImageView(image);
        preview.setFitWidth(120);
        preview.setFitHeight(78);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);
        preview.setClip(new Rectangle(120, 78));
        return preview;
    }

    private int parseIntCell(String[] rowData, int index) {
        if (rowData == null || index < 0 || index >= rowData.length) {
            return 0;
        }
        try {
            return Integer.parseInt(rowData[index]);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private HBox metaStrip(String[] rowData) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 8, 0));
        String a = rowData != null && rowData.length > 0 ? rowData[0] : "Item";
        String b = rowData != null && rowData.length > 1 ? rowData[1] : "Primary";
        row.getChildren().addAll(
            chip("ID * " + Math.abs(a.hashCode() % 10000), false),
            chip("Ref * " + b, true),
            chip("Audit * Enabled", false)
        );
        return row;
    }

    private HBox infoChipRow() {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        String[] labels = {"Active", "Verified", "Synced", "Tracked"};
        for (int i = 0; i < labels.length; i++) {
            row.getChildren().add(chip(labels[i], (i & 1) != 0));
        }
        return row;
    }

    private Region chip(String label, boolean accent) {
        Text tx = t(label, lightFont(), FontWeight.NORMAL, 12);
        tx.setFill(accent ? Color.WHITE : textSecondaryColor());
        HBox box = new HBox(tx);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5, 10, 5, 10));
        String common = ";-fx-border-width:1;-fx-background-radius:999;-fx-border-radius:999;";
        String chipStyle = accent
            ? "-fx-background-color:" + accentRgba(0.26) + ";-fx-border-color:" + accentRgba(0.36) + common
            : "-fx-background-color:rgba(255,255,255,0.05);-fx-border-color:rgba(255,255,255,0.12)" + common;
        box.setStyle(chipStyle);
        return box;
    }

    private VBox notesBox() {
        VBox wrap = new VBox(4);
        Text lbl = t("Internal notes", lightFont(), FontWeight.NORMAL, 13);
        lbl.setFill(textMutedColor());
        Text val = t("Add context for admins (reason, follow-up, priority).", lightFont(), FontWeight.NORMAL, 13);
        val.setFill(textSecondaryColor());
        VBox box = new VBox(val);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.setStyle(
            "-fx-background-color:rgba(255,255,255,0.03);" +
            "-fx-border-color:rgba(255,255,255,0.08);" +
            "-fx-border-width:1;" +
            "-fx-background-radius:10px;" +
            "-fx-border-radius:10px;"
        );
        wrap.getChildren().addAll(lbl, box);
        return wrap;
    }

    private Node fieldRow(String entityLabel, String mode, String label, String value, boolean editable) {
        VBox row = new VBox(6);
        row.setPadding(new Insets(4, 0, 4, 0));
        Text lbl = t(label, lightFont(), FontWeight.NORMAL, 11);
        lbl.setFill(textMutedColor());
        Label liveHint = modalLiveHintLabel();

        boolean canEditThisField = editable;
        if ("Reclamation".equalsIgnoreCase(entityLabel) && "edit".equals(mode) && !"Status".equalsIgnoreCase(label)) {
            canEditThisField = false;
        }
        if ("Reponse".equalsIgnoreCase(entityLabel) && "edit".equals(mode) && !"Message".equalsIgnoreCase(label)) {
            canEditThisField = false;
        }
        if ("Reponse".equalsIgnoreCase(entityLabel) && "add".equals(mode) && "Date".equalsIgnoreCase(label)) {
            canEditThisField = false;
        }

        if (canEditThisField) {
            if (label.toLowerCase().contains("image")) {
                TextField urlField = new TextField(value != null ? value : "");
                urlField.setEditable(false);
                urlField.setPromptText("Click upload to add media...");
                urlField.setStyle(inputStyle(false, false));
                HBox.setHgrow(urlField, Priority.ALWAYS);

                ImageView preview = new ImageView();
                preview.setFitWidth(80);
                preview.setFitHeight(50);
                preview.setPreserveRatio(true);
                preview.setStyle("-fx-background-radius: 8px;");

                Runnable updatePreview = () -> {
                    String path = urlField.getText();
                    if (path != null && !path.isBlank() && !"-".equals(path)) {
                        Image img = ImageLoaderUtil.loadImage(path);
                        if (img != null && !img.isError()) {
                            preview.setImage(img);
                            preview.setManaged(true);
                            preview.setVisible(true);
                        } else {
                            preview.setManaged(false);
                            preview.setVisible(false);
                        }
                    } else {
                        preview.setManaged(false);
                        preview.setVisible(false);
                    }
                };
                updatePreview.run();
                urlField.textProperty().addListener((obs, oldV, newV) -> updatePreview.run());
                
                Button uploadBtn = pillAction(value != null && !value.isBlank() && !"-".equals(value) ? "Change" : "Upload", true);
                uploadBtn.setPadding(new Insets(8, 16, 8, 16));
                
                Button removeBtn = new Button("ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¢");
                removeBtn.setTooltip(new javafx.scene.control.Tooltip("Remove Image"));
                removeBtn.setStyle("-fx-background-color:rgba(239,68,68,0.12);-fx-border-color:rgba(239,68,68,0.24);-fx-border-width:1;-fx-text-fill:#fecaca;-fx-font-weight:bold;-fx-background-radius:100;-fx-border-radius:100;-fx-min-width:34;-fx-min-height:34;-fx-cursor:hand;");
                HorizonDesignSystem.installButtonMotion(removeBtn);
                removeBtn.setOnAction(e -> {
                    urlField.setText("-");
                    uploadBtn.setText("Upload");
                    liveHint.setText("Image removed");
                    liveHint.setTextFill(Color.web("#ff3b30"));
                });

                uploadBtn.setOnAction(e -> {
                    FileChooser fc = new FileChooser();
                    fc.setTitle("Select " + label);
                    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
                    File selected = fc.showOpenDialog(root.getScene().getWindow());
                    if (selected != null) {
                        uploadBtn.setText("...");
                        uploadBtn.setDisable(true);
                        DatabaseService.getInstance().runAsync(() -> {
                            try {
                                ImageKitConfig cfg = ImageKitConfig.fromEnv();
                                ImageKitStorageService svc = new ImageKitStorageService(cfg);
                                String ikFolder = getImageKitFolderForEntity(entityLabel);
                                ImageKitUploadResult res = svc.uploadFile(selected, ikFolder);
                                javafx.application.Platform.runLater(() -> {
                                    urlField.setText(res.url());
                                    uploadBtn.setText("Change");
                                    uploadBtn.setDisable(false);
                                    liveHint.setText("ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ Uploaded");
                                    liveHint.setTextFill(Color.web(accentHex()));
                                });
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                javafx.application.Platform.runLater(() -> {
                                    uploadBtn.setText("Retry");
                                    uploadBtn.setDisable(false);
                                    liveHint.setText("ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â Failed");
                                    liveHint.setTextFill(Color.web("#ff3b30"));
                                });
                            }
                        });
                    }
                });
                
                HBox uploadBox = new HBox(8, preview, urlField, uploadBtn, removeBtn);
                uploadBox.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().addAll(lbl, uploadBox, liveHint);
                return row;
            }

            if ("User".equalsIgnoreCase(entityLabel) && "Role".equalsIgnoreCase(label)) {
                ComboBox<String> roleSelect = new ComboBox<>();
                roleSelect.getItems().addAll(userRoleOptions(value));
                String selectedRole = normalizeRoleValue(value);
                if (selectedRole.equals("-") && "add".equals(mode)) {
                    selectedRole = "ROLE_RESIDENT";
                }
                if (!selectedRole.equals("-") && !roleSelect.getItems().contains(selectedRole)) {
                    roleSelect.getItems().add(selectedRole);
                }
                if (!selectedRole.equals("-")) {
                    roleSelect.setValue(selectedRole);
                }
                styleSelect(roleSelect, "Select Role", this::formatRoleDisplay);
                installLiveValidation(roleSelect, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, roleSelect, liveHint);
                return row;
            }

            if ("User".equalsIgnoreCase(entityLabel) && "Verified".equalsIgnoreCase(label)) {
                ComboBox<String> verifiedSelect = new ComboBox<>();
                verifiedSelect.getItems().addAll("Yes", "No");
                verifiedSelect.setValue(isTruthyText(value) ? "Yes" : "No");
                styleSelect(verifiedSelect, "Select Value", Function.identity());
                installLiveValidation(verifiedSelect, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, verifiedSelect, liveHint);
                return row;
            }

            if ("Reclamation".equalsIgnoreCase(entityLabel) && "Status".equalsIgnoreCase(label)) {
                ComboBox<String> statusSelect = new ComboBox<>();
                statusSelect.getItems().addAll(reclamationStatusOptions(value));
                String selectedStatus = normalizeReclamationStatusValue(value);
                if (!selectedStatus.equals("-") && !statusSelect.getItems().contains(selectedStatus)) {
                    statusSelect.getItems().add(selectedStatus);
                }
                if (!selectedStatus.equals("-")) {
                    statusSelect.setValue(selectedStatus);
                }
                styleSelect(statusSelect, "Select Status", this::formatReclamationStatusDisplay);
                installLiveValidation(statusSelect, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, statusSelect, liveHint);
                return row;
            }

            if ("Reponse".equalsIgnoreCase(entityLabel) && "User".equalsIgnoreCase(label)) {
                ComboBox<String> userSelect = new ComboBox<>();
                userSelect.getItems().addAll(reponseUserOptions(value));
                styleSelect(userSelect, "Select User", Function.identity());
                if (value != null && !value.isBlank() && !"-".equals(value)) {
                    userSelect.setValue(value);
                }
                userSelect.setId("reponse-user-select");
                installLiveValidation(userSelect, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, userSelect, liveHint);
                return row;
            }

            if ("Appartement".equalsIgnoreCase(entityLabel)) {
                if ("Residence ID".equalsIgnoreCase(label)) {
                    ComboBox<String> resSelect = new ComboBox<>();
                    resSelect.getItems().addAll(residenceOptions(value));
                    styleSelect(resSelect, "Select Residence", this::hideIdFormatter);
                    if (value != null && !value.isBlank() && !"-".equals(value)) {
                        // Find match by ID
                        resSelect.getItems().stream()
                            .filter(opt -> opt.startsWith(value + " -"))
                            .findFirst()
                            .ifPresent(resSelect::setValue);
                    }
                    installLiveValidation(resSelect, liveHint, entityLabel, label);
                    row.getChildren().addAll(lbl, resSelect, liveHint);
                    return row;
                }
                if ("User ID".equalsIgnoreCase(label)) {
                    ComboBox<String> userSelect = new ComboBox<>();
                    userSelect.getItems().addAll(fullUserOptions(value));
                    styleSelect(userSelect, "Select User", this::hideIdFormatter);
                    if (value != null && !value.isBlank() && !"-".equals(value)) {
                        userSelect.getItems().stream()
                            .filter(opt -> opt.startsWith(value + " -"))
                            .findFirst()
                            .ifPresent(userSelect::setValue);
                    }
                    installLiveValidation(userSelect, liveHint, entityLabel, label);
                    row.getChildren().addAll(lbl, userSelect, liveHint);
                    return row;
                }
                if ("Type".equalsIgnoreCase(label)) {
                    ComboBox<String> typeSelect = new ComboBox<>();
                    typeSelect.getItems().addAll(apartmentTypeOptions());
                    styleSelect(typeSelect, "Select Type", Function.identity());
                    if (value != null && !value.isBlank() && !"-".equals(value)) {
                        typeSelect.setValue(value);
                    }
                    installLiveValidation(typeSelect, liveHint, entityLabel, label);
                    row.getChildren().addAll(lbl, typeSelect, liveHint);
                    return row;
                }
            }

            if ("Maintenance Ticket".equalsIgnoreCase(entityLabel) && "Apartment ID".equalsIgnoreCase(label)) {
                ComboBox<String> aptSelect = new ComboBox<>();
                aptSelect.getItems().addAll(apartmentOptions(value));
                styleSelect(aptSelect, "Select Apartment", this::hideIdFormatter);
                if (value != null && !value.isBlank() && !"-".equals(value)) {
                    aptSelect.getItems().stream()
                        .filter(opt -> opt.startsWith(value + " -"))
                        .findFirst()
                        .ifPresent(aptSelect::setValue);
                }
                installLiveValidation(aptSelect, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, aptSelect, liveHint);
                return row;
            }

                if ("Blocks".equalsIgnoreCase(label)) {
                    FlowPane blocksPane = new FlowPane(12, 10);
                    blocksPane.setPadding(new Insets(8, 0, 8, 0));
                    blocksPane.setPrefWrapLength(300);
                    String[] allBlocks = {"A", "B", "C", "D", "E", "F"};
                    java.util.Set<String> selected = new java.util.HashSet<>();
                    if (value != null && !value.isBlank() && !"-".equals(value)) {
                        for (String s : value.split(",")) selected.add(s.trim());
                    }
                    for (String b : allBlocks) {
                        javafx.scene.control.CheckBox cb = new javafx.scene.control.CheckBox(b);
                        cb.setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-cursor:hand;-fx-font-size:12px;");
                        if (selected.contains(b)) cb.setSelected(true);
                        blocksPane.getChildren().add(cb);
                    }
                    installLiveValidation(blocksPane, liveHint, entityLabel, label);
                    row.getChildren().addAll(lbl, blocksPane, liveHint);
                    return row;
                }
                
                if ("Units".equalsIgnoreCase(label) || "Floors".equalsIgnoreCase(label)) {
                    ComboBox<String> enumSelect = new ComboBox<>();
                    if ("Units".equalsIgnoreCase(label)) enumSelect.getItems().addAll(residenceUnitOptions());
                    else enumSelect.getItems().addAll(residenceFloorOptions());
                    
                    styleSelect(enumSelect, "Select Value", Function.identity());
                    if (value != null && !value.isBlank() && !"-".equals(value)) {
                        enumSelect.setValue(value);
                    }
                    installLiveValidation(enumSelect, liveHint, entityLabel, label);
                    row.getChildren().addAll(lbl, enumSelect, liveHint);
                    return row;
                }

            if (("Appartement".equalsIgnoreCase(entityLabel) || "Residence".equalsIgnoreCase(entityLabel)) &&
                ("Available".equalsIgnoreCase(label) || "Parking".equalsIgnoreCase(label))) {
                ComboBox<String> boolSelect = new ComboBox<>();
                boolSelect.getItems().addAll(booleanOptions(label));
                String displayVal = "Available".equalsIgnoreCase(label) 
                    ? (isTruthyText(value) ? "Available" : "Occupied")
                    : (isTruthyText(value) ? "Yes" : "No");
                boolSelect.setValue(displayVal);
                styleSelect(boolSelect, "Select Status", Function.identity());
                installLiveValidation(boolSelect, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, boolSelect, liveHint);
                return row;
            }

            if ("Built Date".equalsIgnoreCase(label)) {
                LocalDate initial = LocalDate.now();
                try {
                    if (value != null && !value.isBlank() && !"-".equals(value)) {
                        initial = LocalDate.parse(value);
                    }
                } catch (Exception ignored) {}
                VBox calendar = buildGlassCalendar(initial, (date) -> updateLiveHint(liveHint, null));
                row.getChildren().addAll(lbl, calendar, liveHint);
                return row;
            }

            if ("Maintenance Ticket".equalsIgnoreCase(entityLabel) && 
                (label.contains("General") || label.contains("Plumbing") || label.contains("Electrical") || label.contains("Heating"))) {
                ComboBox<String> scoreSelect = new ComboBox<>();
                for (int i = 1; i <= 10; i++) scoreSelect.getItems().add(String.valueOf(i));
                styleSelect(scoreSelect, "Select Score", Function.identity());
                if (value != null && !value.isBlank() && !"-".equals(value)) {
                    scoreSelect.setValue(value);
                }
                installLiveValidation(scoreSelect, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, scoreSelect, liveHint);
                return row;
            }

            if ("Maintenance Ticket".equalsIgnoreCase(entityLabel) && "AI Recommendation".equalsIgnoreCase(label)) {
                TextField field = new TextField(value != null ? value : "");
                field.setPromptText("Click 'Generate' to use Mistral AI...");
                field.setStyle("-fx-background-color:rgba(255,255,255,0.05);-fx-text-fill:white;-fx-border-color:rgba(255,255,255,0.1);-fx-border-radius:100;-fx-background-radius:100;-fx-padding:8 12 8 12;");
                HBox.setHgrow(field, Priority.ALWAYS);
                
                Button genBtn = pillAction("Generate", true);
                genBtn.setPadding(new Insets(8, 16, 8, 16));
                
                genBtn.setOnAction(e -> {
                    genBtn.setDisable(true);
                    genBtn.setText("...");
                    
                    // Need to find Apartment ID in the form to generate recommendation
                    VBox parentFields = (VBox) row.getParent();
                    Integer apartmentId = null;
                    if (parentFields != null) {
                        for (Node node : parentFields.getChildren()) {
                            if (node instanceof VBox rowNode) {
                                for (Node child : rowNode.getChildren()) {
                                    if (child instanceof ComboBox<?> cb && "Select Apartment".equals(cb.getPromptText())) {
                                        String selected = (String) cb.getValue();
                                        if (selected != null && selected.contains(" - ")) {
                                            try {
                                                apartmentId = Integer.parseInt(selected.split(" - ")[0]);
                                            } catch (Exception ignored) {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (apartmentId == null) {
                        genBtn.setDisable(false);
                        genBtn.setText("Generate");
                        liveHint.setText("Select Apartment first!");
                        liveHint.setTextFill(Color.web("#ff3b30"));
                        return;
                    }
                    
                    final Integer finalAptId = apartmentId;
                    DatabaseService.getInstance().runAsync(() -> {
                        try {
                            ResidenceController resCtrl = new ResidenceController();
                            MaintenanceController maintCtrl = new MaintenanceController();
                            Apartment aptObj = resCtrl.apartmentById(finalAptId).orElse(null);
                            Maintenance maintObj = maintCtrl.latestMaintenance(finalAptId).orElse(null);
                            
                            if (aptObj != null) {
                                String rec = maintCtrl.generateMistralRecommendation(aptObj, maintObj);
                                javafx.application.Platform.runLater(() -> {
                                    field.setText(rec);
                                    genBtn.setDisable(false);
                                    genBtn.setText("Generate");
                                    liveHint.setText("ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ AI Recommendation generated");
                                    liveHint.setTextFill(Color.web(accentHex()));
                                });
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            javafx.application.Platform.runLater(() -> {
                                genBtn.setDisable(false);
                                genBtn.setText("Retry");
                                liveHint.setText("ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã¢â‚¬Â¦ÃƒÂ¢Ã¢â€šÂ¬Ã…â€œÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â AI generation failed");
                                liveHint.setTextFill(Color.web("#ff3b30"));
                            });
                        }
                    });
                });
                
                HBox hBox = new HBox(8, field, genBtn);
                hBox.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().addAll(lbl, hBox, liveHint);
                return row;
            }

            if ("Reponse".equalsIgnoreCase(entityLabel) && "Reclamation".equalsIgnoreCase(label)) {
                ComboBox<String> reclamationSelect = new ComboBox<>();
                reclamationSelect.getItems().addAll(reponseReclamationOptions(value));
                String selectedReclamation = normalizeReclamationTitle(value);
                if (!selectedReclamation.equals("-") && !reclamationSelect.getItems().contains(selectedReclamation)) {
                    reclamationSelect.getItems().add(selectedReclamation);
                }
                if (!selectedReclamation.equals("-")) {
                    reclamationSelect.setValue(selectedReclamation);
                }
                styleSelect(reclamationSelect, "Select Reclamation", Function.identity());
                reclamationSelect.setId("reponse-reclamation-select");
                installLiveValidation(reclamationSelect, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, reclamationSelect, liveHint);
                return row;
            }

            if ("Reponse".equalsIgnoreCase(entityLabel) && "Date".equalsIgnoreCase(label)) {
                canEditThisField = false;
            }

            if ("Publication".equalsIgnoreCase(entityLabel) && "Category".equalsIgnoreCase(label)) {
                ComboBox<String> categorySelect = new ComboBox<>();
                java.util.List<String> categories = java.util.List.of(
                    "Announcement", "Suggestion", "Jeux Video", "Informatique", "NouveautÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â©", "Discussion General", "Culture", "Sport"
                );
                categorySelect.getItems().addAll(categories);
                if (value != null && !value.isEmpty() && !value.equals("-")) {
                    categorySelect.setValue(value);
                } else {
                    categorySelect.setValue("Discussion General");
                }
                styleSelect(categorySelect, "Select Category", Function.identity());
                installLiveValidation(categorySelect, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, categorySelect, liveHint);
                return row;
            }

            if ("Event".equalsIgnoreCase(entityLabel) && "Type".equalsIgnoreCase(label)) {
                ComboBox<String> typeSelect = new ComboBox<>();
                typeSelect.getItems().addAll(eventTypeOptions(value));
                String selectedType = normalizeEventTypeValue(value);
                if (!selectedType.equals("-") && !typeSelect.getItems().contains(selectedType)) {
                    typeSelect.getItems().add(selectedType);
                }
                if (!selectedType.equals("-")) {
                    typeSelect.setValue(selectedType);
                } else if (!typeSelect.getItems().isEmpty()) {
                    typeSelect.setValue(typeSelect.getItems().getFirst());
                }
                styleSelect(typeSelect, "Select Type", this::formatEventTypeDisplay);
                installLiveValidation(typeSelect, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, typeSelect, liveHint);
                return row;
            }

            if ("Event".equalsIgnoreCase(entityLabel) && "Status".equalsIgnoreCase(label)) {
                ComboBox<String> eventStatusSelect = new ComboBox<>();
                eventStatusSelect.getItems().addAll(eventStatusOptions(value));
                String selectedStatus = normalizeEventStatusValue(value);
                if (!selectedStatus.equals("-") && !eventStatusSelect.getItems().contains(selectedStatus)) {
                    eventStatusSelect.getItems().add(selectedStatus);
                }
                if (!selectedStatus.equals("-")) {
                    eventStatusSelect.setValue(selectedStatus);
                }
                styleSelect(eventStatusSelect, "Select Status", this::formatEventStatusDisplay);
                installLiveValidation(eventStatusSelect, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, eventStatusSelect, liveHint);
                return row;
            }

            if (isDateFieldLabel(label)) {
                DatePicker datePicker = new DatePicker(parseDateForPicker(value));
                styleDatePicker(datePicker);
                installLiveValidation(datePicker, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, datePicker, liveHint);
                return row;
            }

            if (isImageFieldLabel(label)) {
                TextField imageInput = new TextField(value);
                imageInput.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
                imageInput.setPrefHeight(36);
                imageInput.setMinHeight(36);
                imageInput.setStyle(inputStyle(false, false));
                imageInput.setOnMouseEntered(_ -> imageInput.setStyle(inputStyle(true, imageInput.isFocused())));
                imageInput.setOnMouseExited(_ -> imageInput.setStyle(inputStyle(false, imageInput.isFocused())));
                imageInput.focusedProperty().addListener((ignoredObservable, ignoredOldValue, newVal) -> imageInput.setStyle(inputStyle(false, newVal)));

                Button browse = new Button("Browse");
                browse.setFont(Font.font(lightFont(), FontWeight.SEMI_BOLD, 11));
                browse.setPrefHeight(36);
                browse.setMinHeight(36);
                browse.setStyle(
                    "-fx-background-color:" + accentGradient() + ";" +
                    "-fx-text-fill:white;" +
                    "-fx-background-radius:10px;" +
                    "-fx-border-radius:10px;" +
                    "-fx-cursor:hand;"
                );
                browse.setOnAction(_ -> {
                    FileChooser chooser = new FileChooser();
                    chooser.setTitle("Select Image");
                    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
                    Window owner = root.getScene() != null ? root.getScene().getWindow() : null;
                    java.io.File file = chooser.showOpenDialog(owner);
                    if (file != null) {
                        imageInput.setText(file.getAbsolutePath());
                    }
                });

                HBox imageRow = new HBox(8, imageInput, browse);
                HBox.setHgrow(imageInput, Priority.ALWAYS);
                installLiveValidation(imageInput, liveHint, entityLabel, label);
                row.getChildren().addAll(lbl, imageRow, liveHint);
                return row;
            }

            TextField input = new TextField(value);
            input.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
            input.setPrefHeight(36);
            input.setMinHeight(36);
            input.setStyle(inputStyle(false, false));
            input.setOnMouseEntered(_ -> input.setStyle(inputStyle(true, input.isFocused())));
            input.setOnMouseExited(_ -> input.setStyle(inputStyle(false, input.isFocused())));
            input.focusedProperty().addListener((ignoredObservable, ignoredOldValue, newVal) -> input.setStyle(inputStyle(false, newVal)));
            installLiveValidation(input, liveHint, entityLabel, label);
            row.getChildren().addAll(lbl, input, liveHint);
        } else {
            Node displayNode;
            if (isImageFieldLabel(label) && value != null && !value.isBlank() && !"-".equals(value)) {
                VBox imgWrap = new VBox(6);
                imgWrap.getChildren().addAll(
                    publicationThumbnail(value),
                    t(value, lightFont(), FontWeight.NORMAL, 11)
                );
                ((Text)imgWrap.getChildren().get(1)).setFill(textMutedColor());
                displayNode = imgWrap;
            } else {
                Text val = t(value, lightFont(), FontWeight.NORMAL, 14);
                val.setFill(textSecondaryColor());
                displayNode = val;
            }

            VBox box = new VBox(displayNode);
            box.setPadding(new Insets(8, 10, 8, 10));
            box.setStyle(
                "-fx-background-color:rgba(255,255,255,0.03);" +
                "-fx-border-color:rgba(255,255,255,0.08);" +
                "-fx-border-width:1;" +
                "-fx-background-radius:10px;" +
                "-fx-border-radius:10px;"
            );
            row.getChildren().addAll(lbl, box);
        }
        return row;
    }

    private Label modalLiveHintLabel() {
        Label hint = new Label("Start typing...");
        hint.setTextFill(textMutedColor());
        hint.setFont(Font.font(lightFont(), FontWeight.SEMI_BOLD, 10));
        return hint;
    }

    private void installLiveValidation(TextField input, Label hint, String entityLabel, String fieldLabel) {
        Runnable refresh = () -> updateLiveHint(hint, dashboardFieldValidationError(entityLabel, fieldLabel, input.getText()));
        input.textProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        refresh.run();
    }

    private void installLiveValidation(ComboBox<String> input, Label hint, String entityLabel, String fieldLabel) {
        Runnable refresh = () -> updateLiveHint(hint, dashboardFieldValidationError(entityLabel, fieldLabel, input.getValue()));
        input.valueProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        refresh.run();
    }

    private void installLiveValidation(DatePicker input, Label hint, String entityLabel, String fieldLabel) {
        Runnable refresh = () -> {
            String value = input.getValue() != null ? input.getValue().toString() : input.getEditor().getText();
            updateLiveHint(hint, dashboardFieldValidationError(entityLabel, fieldLabel, value));
        };
        input.valueProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        input.getEditor().textProperty().addListener((obs, oldValue, newValue) -> refresh.run());
        refresh.run();
    }

    private void installLiveValidation(javafx.scene.layout.FlowPane input, Label hint, String entityLabel, String fieldLabel) {
        input.getChildren().forEach(node -> {
            if (node instanceof javafx.scene.control.CheckBox cb) {
                cb.selectedProperty().addListener((obs, old, val) -> updateLiveHint(hint, null));
            }
        });
        updateLiveHint(hint, null);
    }

    private void updateLiveHint(Label hint, String error) {
        if (error == null || error.isBlank()) {
            hint.setText("\u2713 Looks good");
            hint.setTextFill(Color.web(accentHex()));
            return;
        }
        hint.setText(error);
        hint.setTextFill(Color.web("#ff3b30"));
    }

    private String dashboardFieldValidationError(String entityLabel, String fieldLabel, String value) {
        String normalizedField = fieldLabel == null ? "" : fieldLabel.trim().toLowerCase();
        String normalizedEntity = entityLabel == null ? "" : entityLabel.trim().toLowerCase();
        String cleaned = value == null ? "" : value.trim();

        if (cleaned.isEmpty()) {
            return normalizedField.contains("image") || normalizedField.contains("avatar") ? null : "This field is required";
        }

        if (normalizedField.contains("email")) {
            return cleaned.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$") ? null : "Invalid email format";
        }

        if (normalizedField.contains("date")) {
            return cleaned.length() >= 8 ? null : "Date value looks incomplete";
        }

        if (normalizedField.contains("place") || normalizedField.contains("number") || normalizedField.contains("accompagnant")) {
            try {
                int parsed = Integer.parseInt(cleaned);
                return parsed >= 0 ? null : "Value must be 0 or higher";
            } catch (NumberFormatException ignored) {
                return "Must be a numeric value";
            }
        }

        if (normalizedField.contains("title")
            || normalizedField.contains("name")
            || normalizedField.contains("subject")
            || normalizedField.contains("message")
            || normalizedField.contains("description")) {
            return cleaned.length() >= 3 ? null : "At least 3 characters required";
        }

        if ("user".equals(normalizedEntity) && normalizedField.contains("phone")) {
            return cleaned.length() >= 8 ? null : "Phone looks too short";
        }

        return cleaned.length() >= 2 ? null : "At least 2 characters required";
    }

    private boolean isImageFieldLabel(String label) {
        String normalized = label == null ? "" : label.trim().toLowerCase();
        return normalized.contains("image") || 
               normalized.contains("avatar") || 
               normalized.contains("thumbnail") || 
               normalized.contains("media") || 
               normalized.contains("photo") || 
               normalized.contains("icon") || 
               normalized.contains("cover");
    }

    private Node tableThumbnail(String imagePath) {
        Image image = ImageLoaderUtil.loadImage(imagePath);
        if (image == null || image.isError()) {
            image = ImageLoaderUtil.loadImage("uploads/" + imagePath);
        }
        if (image == null || image.isError()) {
            image = ImageLoaderUtil.loadImage("uploads/forum_images/" + imagePath);
        }
        if (image == null || image.isError()) {
            image = ImageLoaderUtil.loadImage("uploads/profile_images/" + imagePath);
        }
        
        if (image == null || image.isError()) {
            Text none = t("No Image", lightFont(), FontWeight.NORMAL, 10);
            none.setFill(textMutedColor());
            return none;
        }

        ImageView iv = new ImageView(image);
        iv.setFitWidth(40);
        iv.setFitHeight(30);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        
        Rectangle clip = new Rectangle(40, 30);
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        iv.setClip(clip);
        
        return iv;
    }

    private boolean isDateFieldLabel(String label) {
        String normalized = label == null ? "" : label.trim().toLowerCase();
        return normalized.contains("date") || normalized.contains("created") || normalized.contains("updated");
    }

    private LocalDate parseDateForPicker(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(trimmed + ", " + LocalDate.now().getYear(), DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception ignored) {
        }
        return null;
    }

    private void styleDatePicker(DatePicker picker) {
        picker.setEditable(true);
        picker.setPrefHeight(36);
        picker.setMinHeight(36);
        picker.setMaxWidth(Double.MAX_VALUE);
        picker.setStyle(inputStyle(false, false));
        picker.setOnMouseEntered(_ -> picker.setStyle(inputStyle(true, picker.isFocused())));
        picker.setOnMouseExited(_ -> picker.setStyle(inputStyle(false, picker.isFocused())));
        picker.focusedProperty().addListener((ignoredObservable, ignoredOldValue, newVal) -> picker.setStyle(inputStyle(false, newVal)));
    }

    private List<String> eventStatusOptions(String currentValue) {
        LinkedHashSet<String> statuses = new LinkedHashSet<>();
        statuses.add("planifie");
        statuses.add("en_cours");
        statuses.add("termine");
        statuses.add("annule");

        String current = normalizeEventStatusValue(currentValue);
        if (!current.equals("-")) {
            statuses.add(current);
        }
        return new ArrayList<>(statuses);
    }

    private List<String> eventTypeOptions(String currentValue) {
        LinkedHashSet<String> types = new LinkedHashSet<>();
        types.add("reunion");
        types.add("social");
        types.add("formation");
        types.add("maintenance");
        types.add("culturel");
        types.add("sportif");

        String current = normalizeEventTypeValue(currentValue);
        if (!current.equals("-")) {
            types.add(current);
        }
        return new ArrayList<>(types);
    }

    private String normalizeEventTypeValue(String typeValue) {
        if (typeValue == null || typeValue.isBlank()) {
            return "-";
        }
        String token = typeValue.trim().toLowerCase().replace(' ', '_');
        return switch (token) {
            case "reunion", "social", "formation", "maintenance", "culturel", "sportif" -> token;
            default -> "-";
        };
    }

    private String formatEventTypeDisplay(String typeValue) {
        String type = normalizeEventTypeValue(typeValue);
        return switch (type) {
            case "reunion" -> "Reunion";
            case "social" -> "Social";
            case "formation" -> "Formation";
            case "maintenance" -> "Maintenance";
            case "culturel" -> "Culturel";
            case "sportif" -> "Sportif";
            default -> "Select Type";
        };
    }

    private String normalizeEventStatusValue(String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return "-";
        }
        String token = statusValue.trim().toLowerCase().replace(' ', '_');
        return switch (token) {
            case "planifie", "en_cours", "termine", "annule" -> token;
            case "planned" -> "planifie";
            case "ongoing", "in_progress" -> "en_cours";
            case "completed" -> "termine";
            case "cancelled" -> "annule";
            default -> "-";
        };
    }

    private String formatEventStatusDisplay(String statusValue) {
        String status = normalizeEventStatusValue(statusValue);
        return switch (status) {
            case "planifie" -> "Planned";
            case "en_cours" -> "In Progress";
            case "termine" -> "Completed";
            case "annule" -> "Cancelled";
            default -> "Select Status";
        };
    }

    private List<String> reponseUserOptions(String currentValue) {
        LinkedHashSet<String> users = new LinkedHashSet<>();
        for (User user : dashboardAdminService.users()) {
            String displayName = userDisplayName(user);
            if (!displayName.equals("Unknown")) {
                users.add(displayName);
            }
        }

        String current = normalizeUserDisplayName(currentValue);
        if (!current.equals("-")) {
            users.add(current);
        }
        return new ArrayList<>(users);
    }

    private List<String> reponseReclamationOptions(String currentValue) {
        LinkedHashSet<String> reclamations = new LinkedHashSet<>();
        for (Reclamation reclamation : dashboardAdminService.reclamations()) {
            String title = normalizeReclamationTitle(reclamation != null ? reclamation.getTitreReclamations() : null);
            if (!title.equals("-")) {
                reclamations.add(title);
            }
        }

        String current = normalizeReclamationTitle(currentValue);
        if (!current.equals("-")) {
            reclamations.add(current);
        }
        return new ArrayList<>(reclamations);
    }

    private List<String> reponseReclamationOptionsByUser(String userDisplayName) {
        LinkedHashSet<String> reclamations = new LinkedHashSet<>();
        for (Reclamation reclamation : dashboardAdminService.reclamations()) {
            if (reclamation != null && reclamation.getUser() != null) {
                String displayName = userDisplayName(reclamation.getUser());
                if (displayName.equals(userDisplayName)) {
                    String title = normalizeReclamationTitle(reclamation.getTitreReclamations());
                    if (!title.equals("-")) {
                        reclamations.add(title);
                    }
                }
            }
        }
        return new ArrayList<>(reclamations);
    }

    private String normalizeUserDisplayName(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? "-" : normalized;
    }

    private String normalizeReclamationTitle(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isEmpty() ? "-" : normalized;
    }

    private String userDisplayName(User user) {
        if (user == null) {
            return "Unknown";
        }
        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? "Unknown" : fullName;
    }

    private List<String> reclamationStatusOptions(String currentValue) {
        LinkedHashSet<String> statuses = new LinkedHashSet<>();
        statuses.add("active");
        statuses.add("en_attente");
        statuses.add("refuse");
        statuses.add("termine");

        String current = normalizeReclamationStatusValue(currentValue);
        if (!current.equals("-")) {
            statuses.add(current);
        }
        return new ArrayList<>(statuses);
    }

    private String normalizeReclamationStatusValue(String statusValue) {
        if (statusValue == null || statusValue.isBlank()) {
            return "-";
        }
        String token = statusValue.trim().toLowerCase().replace(' ', '_');
        return switch (token) {
            case "active", "en_attente", "refuse", "termine" -> token;
            case "pending" -> "en_attente";
            case "rejected" -> "refuse";
            case "completed" -> "termine";
            default -> "-";
        };
    }

    private String formatReclamationStatusDisplay(String statusValue) {
        String status = normalizeReclamationStatusValue(statusValue);
        return switch (status) {
            case "active" -> "Active";
            case "en_attente" -> "Pending";
            case "refuse" -> "Rejected";
            case "termine" -> "Completed";
            default -> "Select Status";
        };
    }

    private String hideIdFormatter(String value) {
        if (value == null || !value.contains(" - ")) return value;
        return value.split(" - ", 2)[1];
    }

    private void styleSelect(ComboBox<String> select, String placeholder, Function<String, String> displayFormatter) {
        select.setMaxWidth(Double.MAX_VALUE);
        select.setPrefHeight(36);
        select.setMinHeight(36);
        select.setPromptText(placeholder);
        
        // Enhanced combobox styling with glass morphism
        String baseStyle = "-fx-background-color:rgba(255,255,255,0.06);" +
            "-fx-border-color:rgba(255,255,255,0.15);" +
            "-fx-border-width:1;" +
            "-fx-background-radius:10px;" +
            "-fx-border-radius:10px;" +
            "-fx-text-fill:" + (isDark() ? "#e5e7eb" : "#111827") + ";" +
            "-fx-font-size:12;" +
            "-fx-font-family:'" + lightFont() + "';" +
            "-fx-padding:0 12 0 12;" +
            "-fx-focus-color:transparent;" +
            "-fx-faint-focus-color:transparent;";
        
        select.setStyle(baseStyle);
        
        // Custom button cell for displaying formatted text
        select.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(placeholder);
                } else {
                    setText(displayFormatter.apply(item));
                }
                setStyle("-fx-text-fill:" + (isDark() ? "#e5e7eb" : "#111827") + ";");
            }
        });
        
        // Custom list cell factory for dropdown items
        select.setCellFactory(ignored -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(displayFormatter.apply(item));
                    String cellStyle = "-fx-padding:8 12 8 12;" +
                        "-fx-text-fill:" + (isDark() ? "#e5e7eb" : "#111827") + ";" +
                        "-fx-font-size:12;" +
                        "-fx-font-family:'" + lightFont() + "';";
                    if (isSelected()) {
                        setStyle(cellStyle + "-fx-background-color:" + accentRgba(0.28) + ";");
                    } else {
                        setStyle(cellStyle + "-fx-background-color:transparent;");
                    }
                }
            }
        });
        
        // Custom popup styling
        select.setOnShown(ignored -> {
            if (select.getSkin() != null) {
                try {
                    var popup = (javafx.scene.control.skin.ComboBoxListViewSkin<?>) select.getSkin();
                    var listView = (javafx.scene.control.ListView<?>) popup.getPopupContent();
                    if (listView != null) {
                        listView.setStyle(
                            "-fx-background-color:rgba(20,20,20,0.95);" +
                            "-fx-control-inner-background:rgba(20,20,20,0.95);" +
                            "-fx-padding:0;" +
                            "-fx-border-color:rgba(255,255,255,0.12);" +
                            "-fx-border-width:1;" +
                            "-fx-border-radius:8;" +
                            "-fx-background-radius:8;"
                        );
                    }
                } catch (Exception ex) {
                    // Fallback if skin casting fails
                }
            }
        });
        
        // Hover and focus effects
        select.setOnMouseEntered(_ -> select.setStyle(inputStyle(true, select.isFocused())));
        select.setOnMouseExited(_ -> select.setStyle(inputStyle(false, select.isFocused())));
        select.focusedProperty().addListener((ignoredObservable, ignoredOldValue, newVal) -> select.setStyle(inputStyle(false, newVal)));
    }

    private String inputStyle(boolean hover, boolean focus) {
        String base = HorizonDesignSystem.webServiceInput(14, focus) +
            "-fx-font-size:12;" +
            "-fx-font-family:'" + lightFont() + "';" +
            "-fx-focus-color:transparent;" +
            "-fx-faint-focus-color:transparent;";
        if (hover || focus) {
            base += "-fx-effect:dropshadow(one-pass-box," + (focus ? accentRgba(0.25) : "rgba(0,0,0,0.15)") + "," + (focus ? "12" : "8") + ",0,0," + (focus ? "6" : "4") + ");";
        }
        return base;
    }

    private List<String> userRoleOptions(String currentValue) {
        LinkedHashSet<String> roles = new LinkedHashSet<>();
        roles.add("ROLE_ADMIN");
        roles.add("ROLE_SYNDIC");
        roles.add("ROLE_RESIDENT");

        for (User user : dashboardAdminService.users()) {
            String normalized = normalizeRoleValue(user.getRoleUser());
            if (!normalized.equals("-")) {
                roles.add(normalized);
            }
        }

        String current = normalizeRoleValue(currentValue);
        if (!current.equals("-")) {
            roles.add(current);
        }
        return new ArrayList<>(roles);
    }

    private String normalizeRoleValue(String roleValue) {
        if (roleValue == null || roleValue.isBlank()) {
            return "-";
        }
        String normalized = roleValue.trim().toUpperCase();
        if (normalized.equals("-")) {
            return "-";
        }
        if (normalized.startsWith("ROLE_")) {
            return normalized;
        }
        return "ROLE_" + normalized;
    }

    private boolean isTruthyText(String value) {
        String normalized = value == null ? "" : value.trim();
        return "yes".equalsIgnoreCase(normalized)
            || "true".equalsIgnoreCase(normalized)
            || "1".equalsIgnoreCase(normalized)
            || "verified".equalsIgnoreCase(normalized);
    }

    private String formatRoleDisplay(String roleValue) {
        if (roleValue == null || roleValue.isBlank() || "-".equals(roleValue)) {
            return "Select Role";
        }
        String normalized = roleValue.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return switch (normalized) {
            case "ADMIN"    -> "Administrator";
            case "SYNDIC"   -> "Syndic";
            case "RESIDENT" -> "Resident";
            case "OWNER"    -> "Owner";
            default -> {
                // Format as Title Case: "CUSTOM_ROLE" -> "Custom Role"
                String[] parts = normalized.replace("_", " ").toLowerCase().split("\\s+");
                StringBuilder titleCase = new StringBuilder();
                for (String part : parts) {
                    if (!part.isEmpty()) {
                        titleCase.append(Character.toUpperCase(part.charAt(0)))
                            .append(part.substring(1))
                            .append(" ");
                    }
                }
                yield titleCase.toString().trim();
            }
        };
    }

    private Button dangerAction(String text) {
        Button b = new Button(text);
        b.setFont(Font.font(boldFont(), FontWeight.BOLD, 12));
        b.setPadding(new Insets(9, 16, 9, 16));
        b.setStyle(
            "-fx-background-color:rgba(239,68,68,0.14);" +
            "-fx-border-color:rgba(239,68,68,0.34);" +
            "-fx-border-width:1;" +
            "-fx-background-radius:999;" +
            "-fx-border-radius:999;" +
            "-fx-text-fill:#fecaca;" +
            "-fx-cursor:hand;"
        );
        b.setOnMouseEntered(_ -> b.setStyle(
            "-fx-background-color:rgba(239,68,68,0.22);" +
            "-fx-border-color:rgba(239,68,68,0.48);" +
            "-fx-border-width:1;" +
            "-fx-background-radius:999;" +
            "-fx-border-radius:999;" +
            "-fx-text-fill:white;" +
            "-fx-cursor:hand;" +
            "-fx-effect:dropshadow(one-pass-box,rgba(239,68,68,0.24),16,0,0,6);"
        ));
        b.setOnMouseExited(_ -> b.setStyle(
            "-fx-background-color:rgba(239,68,68,0.14);" +
            "-fx-border-color:rgba(239,68,68,0.34);" +
            "-fx-border-width:1;" +
            "-fx-background-radius:999;" +
            "-fx-border-radius:999;" +
            "-fx-text-fill:#fecaca;" +
            "-fx-cursor:hand;"
        ));
        HorizonDesignSystem.installButtonMotion(b);
        return b;
    }

    private static class CrudSpec {
        String viewTitle;
        String viewSubtitle;
        String editTitle;
        String editSubtitle;
        String addTitle;
        String addSubtitle;
        String cancelLabel;
        String saveEditLabel;
        String saveAddLabel;
        String addButtonLabel;
        String viewDeleteLabel;

        static CrudSpec defaults(String entityLabel) {
            CrudSpec s = new CrudSpec();
            s.viewTitle = entityLabel + " Details";
            s.viewSubtitle = "View complete " + entityLabel.toLowerCase() + " information.";
            s.editTitle = "Edit " + entityLabel;
            s.editSubtitle = "Modify fields and save changes.";
            s.addTitle = "Add " + entityLabel;
            s.addSubtitle = "Create a new entry.";
            s.cancelLabel = "Discard";
            s.saveEditLabel = "Save changes";
            s.saveAddLabel = "Create";
            s.addButtonLabel = "Add " + entityLabel;
            s.viewDeleteLabel = "Delete " + entityLabel;
            return s;
        }
    }

    private Button pagerBtn(String text, boolean active) {
        Button b = new Button(text);
        b.setFont(Font.font(lightFont(), active ? FontWeight.BOLD : FontWeight.NORMAL, 11));
        b.setPadding(new Insets(5, 10, 5, 10));
        b.setMinWidth(30);
        String background = active ? accentGradient() : (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.05)");
        String border = active ? "transparent" : (isDark() ? "rgba(255,255,255,0.10)" : "rgba(15,23,42,0.14)");
        String borderWidth = active ? "0" : "1";
        String textColor = active ? "white" : (isDark() ? "rgba(255,255,255,0.72)" : "rgba(15,23,42,0.78)");
        b.setStyle(
            "-fx-background-color:" + background + ";" +
            "-fx-background-radius:8px;" +
            "-fx-border-color:" + border + ";" +
            "-fx-border-width:" + borderWidth + ";" +
            "-fx-border-radius:8px;" +
            "-fx-text-fill:" + textColor + ";" +
            "-fx-cursor:hand;"
        );
        return b;
    }

    private HBox mainSwitcher(Consumer<String> onSelect) {
        HBox c = new HBox();
        c.setAlignment(Pos.CENTER);
        c.setMinWidth(0);
        c.setMaxWidth(Double.MAX_VALUE);
        protectDashboardTabs(c);
        HBox pill = createPill(
            4,
            new Insets(6),
            isDark() ? "rgba(6,8,16,0.78)" : "rgba(255,255,255,0.95)",
            isDark() ? "rgba(255,255,255,0.11)" : "rgba(15,23,42,0.10)",
            999
        );
        pill.setMinWidth(0);
        HBox.setHgrow(pill, Priority.ALWAYS);
        pill.setMaxWidth(Double.MAX_VALUE);

        String[] labels = {"General", "Users", "Forum", "Syndicat", "Residence", "Evenement"};
        List<Button> tabs = new ArrayList<>();
        final String[] activeLabel = {labels[0]};

        for (String label : labels) {
            Button tab = new Button(label);
            tab.setPickOnBounds(true);
            tab.setMouseTransparent(false);
            tab.setFont(Font.font(boldFont(), FontWeight.BOLD, 12));
            tab.setPadding(new Insets(10,22,10,22));
            boolean active = label.equals(activeLabel[0]);
            String background = active ? accentGradient() : "transparent";
            String textColor = active ? "white" : (isDark() ? "rgba(255,255,255,0.45)" : "rgba(15,23,42,0.74)");
            tab.setStyle(
                "-fx-background-color:" + background + ";" +
                "-fx-background-radius:100px;" +
                "-fx-text-fill:" + textColor + ";" +
                "-fx-cursor:hand;"
            );

            tab.setOnAction(_ -> {
                activeLabel[0] = label;
                for (Button b : tabs) {
                    boolean isActive = b == tab;
                    b.setStyle(
                        "-fx-background-color:" + (isActive ? accentGradient() : "transparent") + ";" +
                        "-fx-background-radius:100px;" +
                        "-fx-text-fill:" + (isActive ? "white" : (isDark() ? "rgba(255,255,255,0.45)" : "rgba(15,23,42,0.74)")) + ";" +
                        "-fx-cursor:hand;"
                    );
                }
                onSelect.accept(label);
            });

            tabs.add(tab);
            pill.getChildren().add(tab);
        }

        c.getChildren().add(pill);
        return c;
    }

    /** Switcher used inside module pages (Users/Profile/Onboarding, etc.). */
    HBox moduleModeSwitcher(String[] labels, Consumer<String> onSelect) {
        HBox wrap = new HBox();
        wrap.setAlignment(Pos.CENTER);
        protectDashboardTabs(wrap);

        HBox pill = createPill(
            6,
            new Insets(6),
            isDark() ? "rgba(6,8,16,0.78)" : "rgba(255,255,255,0.95)",
            isDark() ? "rgba(255,255,255,0.11)" : "rgba(15,23,42,0.10)",
            999
        );
        DropShadow pillShadow = new DropShadow();
        pillShadow.setBlurType(BlurType.ONE_PASS_BOX);
        pillShadow.setColor(Color.web(isDark() ? "rgba(0,0,0,0.35)" : "rgba(15,23,42,0.10)"));
        pillShadow.setRadius(20);
        pillShadow.setOffsetX(0);
        pillShadow.setOffsetY(6);
        pill.setEffect(pillShadow);

        List<Button> tabButtons = new ArrayList<>();

        String activeLabel = labels[0];
        for (String label : labels) {
            Button tab = new Button(label);
            tab.setPickOnBounds(true);
            tab.setMouseTransparent(false);
            tab.setFont(Font.font(boldFont(), FontWeight.BOLD, 12));
            tab.setPadding(new Insets(10, 22, 10, 22));
            tab.setAlignment(Pos.CENTER);
            if (label.equals(activeLabel)) {
                tab.setStyle(
                    "-fx-background-color:" + accentGradient() + ";" +
                    "-fx-background-radius:100px;" +
                    "-fx-text-fill:white;" +
                    "-fx-border-color:" + accentRgba(0.32) + ";" +
                    "-fx-border-width:1;" +
                    "-fx-border-radius:100px;" +
                    "-fx-cursor:hand;"
                );
            } else {
                tab.setStyle(
                    "-fx-background-color:transparent;" +
                    "-fx-background-radius:100px;" +
                    "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.55)" : "rgba(15,23,42,0.78)") + ";" +
                    "-fx-cursor:hand;"
                );
            }

            tab.setOnAction(_ -> {
                for (Button b : tabButtons) {
                    b.setStyle(
                        "-fx-background-color:transparent;" +
                        "-fx-background-radius:100px;" +
                        "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.55)" : "rgba(15,23,42,0.78)") + ";" +
                        "-fx-cursor:hand;"
                    );
                }
                tab.setStyle(
                    "-fx-background-color:" + accentGradient() + ";" +
                    "-fx-background-radius:100px;" +
                    "-fx-text-fill:white;" +
                    "-fx-border-color:" + accentRgba(0.32) + ";" +
                    "-fx-border-width:1;" +
                    "-fx-border-radius:100px;" +
                    "-fx-cursor:hand;"
                );
                onSelect.accept(label);
            });

            tabButtons.add(tab);
            pill.getChildren().add(tab);
        }

        wrap.getChildren().add(pill);
        return wrap;
    }

    private HBox subTabBar(String[] labels, Consumer<String> onSelect) {
        HBox c = new HBox(); c.setAlignment(Pos.CENTER);
        protectDashboardTabs(c);
        HBox pill = createPill(
            4,
            new Insets(6),
            isDark() ? "rgba(15,15,17,0.65)" : "rgba(248,250,252,0.95)",
            isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.14)",
            16
        );
        String activeLabel = labels[0];
        for (String label : labels) {
            Button btn = new Button(label.toUpperCase());
            btn.setPickOnBounds(true);
            btn.setMouseTransparent(false);
            btn.setFont(Font.font(boldFont(), FontWeight.BOLD, 11));
            btn.setPadding(new Insets(5,16,5,16));
            styleSubTab(btn, label.equals(activeLabel));
            btn.setOnAction(_ -> {
                pill.getChildren().forEach(this::resetSubTabIfButton);
                styleSubTab(btn, true);
                onSelect.accept(label);
            });
            pill.getChildren().add(btn);
        }
        c.getChildren().add(pill);
        return c;
    }

    private void styleSubTab(Button b, boolean active) {
        String background = active ? accentGradient() : "transparent";
        String textColor = active ? "white" : (isDark() ? "rgba(255,255,255,0.48)" : "rgba(15,23,42,0.74)");
        String border = active ? accentRgba(0.34) : "transparent";
        b.setStyle(
            "-fx-background-color:" + background + ";" +
            "-fx-background-radius:12px;" +
            "-fx-text-fill:" + textColor + ";" +
            "-fx-border-color:" + border + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:12px;" +
            "-fx-cursor:hand;"
        );
        b.setOnMouseEntered(_ -> {
            if (!active) {
                b.setStyle(
                    "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.055)") + ";" +
                    "-fx-background-radius:999px;" +
                    "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.82)" : "rgba(15,23,42,0.86)") + ";" +
                    "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.09)" : "rgba(15,23,42,0.10)") + ";" +
                    "-fx-border-width:1;" +
                    "-fx-border-radius:999px;" +
                    "-fx-cursor:hand;"
                );
            }
        });
        b.setOnMouseExited(_ -> {
            if (!active) {
                styleSubTab(b, false);
            }
        });
    }

    VBox sectionCard() {
        return glassCard();
    }

    private HBox createPill(double spacing, Insets padding, String background, String border, double radius) {
        HBox pill = new HBox(spacing);
        pill.setAlignment(Pos.CENTER);
        pill.setPadding(padding);
        pill.setPickOnBounds(true);
        pill.setMouseTransparent(false);
        pill.setViewOrder(-1000);
        pill.setStyle(
            "-fx-background-color:" + background + ";" +
            "-fx-background-radius:" + radius + "px;" +
            "-fx-border-color:" + border + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:" + radius + "px;" +
            "-fx-effect:dropshadow(one-pass-box," + (isDark() ? "rgba(0,0,0,0.38)" : "rgba(15,23,42,0.12)") + ",28,0,0,10);"
        );
        return pill;
    }

    private void protectDashboardTabs(Region tabLayer) {
        tabLayer.setViewOrder(-1000);
        tabLayer.setPickOnBounds(true);
        tabLayer.setMouseTransparent(false);
        tabLayer.setMinHeight(Region.USE_PREF_SIZE);
    }

    private void resetSubTabIfButton(Node node) {
        if (node instanceof Button b) {
            styleSubTab(b, false);
        }
    }

    HBox metric(String icon, String label, String val, String color) {
        HBox row = new HBox(6); row.setAlignment(Pos.CENTER_LEFT);
        Text ic = new Text(icon); ic.setFont(Font.font(13));
        Text lb = t(label, lightFont(), FontWeight.NORMAL, 13); lb.setFill(textMutedColor());
        Text vl = t(val,   boldFont(),  FontWeight.BOLD,   11); vl.setFill(Color.web(color));
        row.getChildren().addAll(ic, lb, vl);
        return row;
    }

    Region barR(int h, String color) {
        Region r = new Region(); r.setPrefWidth(12); r.setPrefHeight(h);
        r.setStyle("-fx-background-color:"+color+";-fx-background-radius:4 4 0 0;");
        return r;
    }

    VBox buildEmptyState(String message) {
        VBox box = sectionCard();
        Text text = t(message, lightFont(), FontWeight.NORMAL, 13);
        text.setFill(textMutedColor());
        box.getChildren().add(text);
        return box;
    }

    private int parseInt(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length) {
            return 0;
        }

        try {
            return Integer.parseInt(row[index]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int intStat(Map<String, Object> stats, String key) {
        Object value = stats.get(key);
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return value == null ? 0 : Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private List<String[]> listStat(Map<String, Object> stats, String key) {
        Object value = stats.get(key);
        if (value instanceof List<?>) {
            return (List<String[]>) value;
        }
        return new ArrayList<>();
    }

    private int roleCount(Map<String, Object> stats) {
        Object value = stats.get("roles");
        if (value instanceof Map<?, ?> roles) {
            return roles.size();
        }
        return 0;
    }

    private String cell(String[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null || row[index].isBlank()) {
            return "-";
        }
        return row[index];
    }

    Text t(String s, String family, FontWeight w, double size) {
        Text tx = new Text(s);
        tx.setFont(Font.font(family, w, size));
        tx.setFill(textPrimaryColor());
        return tx;
    }
    String boldFont()  { return com.syndicati.MainApplication.getInstance().getBoldFontFamily();  }
    String lightFont() { return com.syndicati.MainApplication.getInstance().getLightFontFamily(); }

    private List<String> residenceOptions(String current) {
        List<String> list = new ArrayList<>();
        for (Residence r : dashboardAdminService.residences()) {
            list.add(r.getIdResidence() + " - " + r.getNameResidence());
        }
        return list;
    }

    private List<String> fullUserOptions(String current) {
        List<String> list = new ArrayList<>();
        for (User u : dashboardAdminService.users()) {
            list.add(u.getIdUser() + " - " + u.getFirstName() + " " + u.getLastName());
        }
        return list;
    }

    private List<String> apartmentOptions(String current) {
        List<String> list = new ArrayList<>();
        for (Apartment a : dashboardAdminService.apartments()) {
            list.add(a.getIdApartment() + " - " + a.getTypeApartment() + " (Block " + (a.getIdResidence() != null ? a.getIdResidence() : "?") + ")");
        }
        return list;
    }

    private List<String> apartmentTypeOptions() {
        return List.of("STUDIO", "S1", "S2", "S3", "S4", "S5");
    }

    private List<String> residenceUnitOptions() {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) list.add(String.valueOf(i));
        return list;
    }

    private List<String> residenceFloorOptions() {
        return List.of("0", "1", "2", "3", "4", "5");
    }

    private List<String> residenceBlockOptions() {
        return List.of("A", "B", "C", "D", "E");
    }

    private List<String> booleanOptions(String label) {
        if ("Available".equalsIgnoreCase(label)) {
            return List.of("Available", "Occupied");
        }
        return List.of("Yes", "No");
    }

    private VBox activitySignalsPane() {
        VBox card = sectionCard();
        Text title = t("Activity Signals", boldFont(), FontWeight.BOLD, 18);
        title.setFill(textPrimaryColor());
        Text body = t("Security and interaction signals are available in the Activity Log module.", lightFont(), FontWeight.NORMAL, 13);
        body.setFill(textMutedColor());
        card.getChildren().addAll(title, body);
        return card;
    }

    String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    VBox moduleShell(String name, String icon) {
        VBox shell = new VBox(22);
        shell.setFillWidth(true);
        shell.setMinWidth(0);
        shell.setMaxWidth(Double.MAX_VALUE);
        shell.setPadding(new Insets(24, 30, 30, 30));
        shell.setStyle("-fx-background-color:transparent;");
        return shell;
    }
    /** Multi-mode module view template - used to reduce duplication across sections. */
    VBox moduleModeView(String name, String icon, String[] labels, java.util.function.Function<String, Node> contentMapper) {
        VBox shell = moduleShell(name, icon);
        VBox body = new VBox(16);
        body.setFillWidth(true);
        body.setViewOrder(20);

        HBox sub = moduleModeSwitcher(labels, key -> {
            body.getChildren().setAll(contentMapper.apply(key));
        });

        body.getChildren().add(contentMapper.apply(labels[0]));
        shell.getChildren().addAll(sub, body);
        return shell;
    }

    private VBox buildGlassCalendar(LocalDate initialValue, Consumer<LocalDate> onSelect) {
        VBox container = new VBox(10);
        container.setUserData(initialValue);
        container.setMaxWidth(320);
        container.setPadding(new Insets(10));
        container.setStyle(HorizonDesignSystem.webServiceCard(16, false));
        
        refreshGlassCalendar(container, initialValue, onSelect);
        return container;
    }

    private void refreshGlassCalendar(VBox container, LocalDate monthToShow, Consumer<LocalDate> onSelect) {
        container.getChildren().clear();
        LocalDate selected = (LocalDate) container.getUserData();

        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);
        
        Button prev = calendarIconButton("<");
        prev.setOnAction(_ -> refreshGlassCalendar(container, monthToShow.minusMonths(1), onSelect));
        
        Button next = calendarIconButton(">");
        next.setOnAction(_ -> refreshGlassCalendar(container, monthToShow.plusMonths(1), onSelect));
        
        String title = monthToShow.getMonth().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH) + " " + monthToShow.getYear();
        Text monthLabel = t(title, boldFont(), FontWeight.BOLD, 14);
        monthLabel.setFill(Color.web(accentHex()));
        
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        head.getChildren().addAll(monthLabel, s, prev, next);

        GridPane grid = new GridPane();
        grid.setHgap(5); grid.setVgap(5);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(14.28);
            grid.getColumnConstraints().add(cc);
        }

        String[] days = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
        for (int i = 0; i < 7; i++) {
            Text d = t(days[i], lightFont(), FontWeight.NORMAL, 10);
            d.setFill(textMutedColor());
            StackPane cell = new StackPane(d);
            cell.setMinHeight(20);
            grid.add(cell, i, 0);
        }

        LocalDate first = monthToShow.withDayOfMonth(1);
        int startOffset = first.getDayOfWeek().getValue() - 1;
        int daysInMonth = monthToShow.lengthOfMonth();
        int day = 1;

        for (int r = 1; r <= 6; r++) {
            for (int c = 0; c < 7; c++) {
                if (r == 1 && c < startOffset) continue;
                if (day > daysInMonth) break;

                final int currentDay = day;
                LocalDate date = monthToShow.withDayOfMonth(currentDay);
                boolean isSelected = date.equals(selected);
                boolean isToday = date.equals(LocalDate.now());

                StackPane cell = new StackPane();
                cell.setMinHeight(30);
                cell.setCursor(Cursor.HAND);
                
                String baseStyle = "-fx-background-radius: 8px;";
                if (isSelected) {
                    cell.setStyle(baseStyle + "-fx-background-color:" + accentGradient() + ";");
                } else if (isToday) {
                    cell.setStyle(baseStyle + "-fx-background-color:rgba(255,255,255,0.055);-fx-border-color:" + accentHex() + ";-fx-border-width:1;");
                } else {
                    cell.setStyle(baseStyle + "-fx-background-color:rgba(255,255,255,0.025);");
                }

                Text txt = t(String.valueOf(currentDay), boldFont(), FontWeight.BOLD, 11);
                txt.setFill(isSelected ? Color.WHITE : textSecondaryColor());
                cell.getChildren().add(txt);

                cell.setOnMouseClicked(_ -> {
                    container.setUserData(date);
                    onSelect.accept(date);
                    refreshGlassCalendar(container, monthToShow, onSelect);
                });

                if (!isSelected) {
                    cell.setOnMouseEntered(_ -> cell.setStyle(baseStyle + "-fx-background-color:" + accentRgba(0.15) + ";"));
                    cell.setOnMouseExited(_ -> {
                        if (date.equals(LocalDate.now())) {
                            cell.setStyle(baseStyle + "-fx-background-color:rgba(255,255,255,0.055);-fx-border-color:" + accentHex() + ";-fx-border-width:1;");
                        } else {
                            cell.setStyle(baseStyle + "-fx-background-color:rgba(255,255,255,0.025);");
                        }
                    });
                }

                grid.add(cell, c, r);
                day++;
            }
            if (day > daysInMonth) break;
        }

        container.getChildren().addAll(head, grid);
    }

    private Button calendarIconButton(String icon) {
        Button btn = new Button(icon);
        btn.setCursor(Cursor.HAND);
        btn.setStyle("-fx-background-color:rgba(255,255,255,0.055);-fx-border-color:rgba(255,255,255,0.10);-fx-border-width:1;-fx-text-fill:white;-fx-background-radius:8;-fx-border-radius:8;-fx-padding:3 8;-fx-font-size:10;");
        return btn;
    }

    private String getImageKitFolderForEntity(String entityLabel) {
        if (entityLabel == null) return "/syndicati/dashboard_assets";
        String normalized = entityLabel.trim().toLowerCase();
        return switch (normalized) {
            case "publication" -> "/syndicati/forum_images";
            case "comment", "commentaire" -> "/syndicati/commentaire_images";
            case "reclamation", "reponse" -> "/syndicati/reclamation_images";
            case "evenement", "event" -> "/syndicati/event_images";
            case "residence" -> "/syndicati/residence_images";
            case "appartement", "apartment" -> "/syndicati/apartment_images";
            case "user", "profile" -> "/syndicati/profile_images";
            default -> "/syndicati/dashboard_assets";
        };
    }

    private Integer extractId(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) return null;
        if (value.contains(" - ")) {
            try { return Integer.parseInt(value.split(" - ")[0]); } catch (Exception e) { return null; }
        }
        try { return Integer.parseInt(value); } catch (Exception e) { return null; }
    }
}






