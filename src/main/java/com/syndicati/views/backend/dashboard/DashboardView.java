package com.syndicati.views.backend.dashboard;

import com.syndicati.models.user.Onboarding;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import javafx.application.Platform;
import javafx.animation.TranslateTransition;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.utils.navigation.NavigationManager;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.image.ImageLoaderUtil;
import com.syndicati.services.dashboard.DashboardAdminService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

/**
 * Admin Dashboard View - full replica of the Horizon admin panel.
 * Sidebar (220px) + scrollable main area.
 * Call setExitCallback() so "Back to App" can exit dashboard mode.
 */
public class DashboardView implements ViewInterface {

    private final HBox root;
    String activeSection = "general";
    private final Map<String, Button> sectionButtons = new HashMap<>();
    VBox contentArea;
    Runnable exitCallback;
    private final Runnable accentRefreshListener;
    boolean sidebarExpanded = true;
    Popup profilePopup;
    Popup notificationPopup;
    PauseTransition notificationHideDelay;
    private final DashboardAdminService dashboardAdminService;
    private static final DateTimeFormatter DASHBOARD_DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DashboardView() {
        this.root = new HBox();
        this.accentRefreshListener = this::refreshAccentStyling;
        this.dashboardAdminService = new DashboardAdminService();
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
        ThemeManager.getInstance().removeAccentChangeListener(accentRefreshListener);
    }

    private void refreshAccentStyling() {
        String section = activeSection;
        sectionButtons.clear();

        root.getChildren().clear();
        VBox sidebar  = DashboardShell.buildSidebar(this);
        sidebar.setPrefWidth(sidebarWidth()); sidebar.setMinWidth(sidebarWidth()); sidebar.setMaxWidth(sidebarWidth());
        VBox mainArea = DashboardShell.buildMainArea(this);
        HBox.setHgrow(mainArea, Priority.ALWAYS);
        root.getChildren().addAll(sidebar, mainArea);

        activeSection = section;
        contentArea.getChildren().setAll(buildSection(section));
        sectionButtons.forEach((k, b) -> styleSidebarItem(b, k.equals(section)));
    }

    private ThemeManager theme() { return ThemeManager.getInstance(); }
    private String accentHex() { return theme().getAccentHex(); }
    String accentGradient() { return theme().getEffectiveAccentGradient(); }
    String accentRgba(double alpha) { return theme().toRgba(accentHex(), alpha); }
    boolean isDark() { return theme().isDarkMode(); }
    private Color textPrimaryColor() { return isDark() ? Color.web("#f8fafc") : Color.web("#111827"); }
    private Color textSecondaryColor() { return isDark() ? Color.web("rgba(255,255,255,0.78)") : Color.web("rgba(17,24,39,0.82)"); }
    private Color textMutedColor() { return isDark() ? Color.web("rgba(255,255,255,0.55)") : Color.web("rgba(30,41,59,0.64)"); }

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
            return "Administrateur";
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
            "-fx-effect:dropshadow(gaussian," + (isDark() ? "rgba(0,0,0,0.28)" : "rgba(15,23,42,0.12)") + ",26,0,0,8);";
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    private void setupLayout() {
        root.setSpacing(0);
        root.setAlignment(Pos.TOP_LEFT);
        root.setFillHeight(true);
        root.setStyle(
            "-fx-background-color: " + (
                isDark()
                    ? "linear-gradient(to bottom right, #070707, #030303 55%, #000000 100%)"
                    : "linear-gradient(to bottom right, #f7f7f7, #f2f2f2 55%, #ececec 100%)"
            ) + ";"
        );
        root.setPadding(new Insets(40, 0, 0, 0));   // 40px = window-bar height

        VBox sidebar  = DashboardShell.buildSidebar(this);
        sidebar.setPrefWidth(sidebarWidth()); sidebar.setMinWidth(sidebarWidth()); sidebar.setMaxWidth(sidebarWidth());

        VBox mainArea = DashboardShell.buildMainArea(this);
        HBox.setHgrow(mainArea, Priority.ALWAYS);
        root.getChildren().addAll(sidebar, mainArea);
    }

    private double sidebarWidth() {
        return sidebarExpanded ? 250 : 96;
    }

    void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        refreshAccentStyling();
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    // SIDEBAR  Ã¢â‚¬â€ three floating glass pills (matches web admin CSS)
    //   .admin-sidebar-top | .admin-sidebar-nav | .admin-sidebar-bottom
    //   each: backdrop-blur glass, border-radius 24px, transparent gap
    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    VBox createSidebar() {
        return DashboardShell.buildSidebar(this);
    }

    /** Shared glass pill container Ã¢â‚¬â€ three of these make up the sidebar. */
    VBox glassPill(Pos alignment) {
        VBox pill = new VBox(0);
        pill.setAlignment(alignment);
        pill.setFillWidth(true);
        pill.setStyle(sidebarPillStyle());
        return pill;
    }

    void styleBackButton(Button b, boolean h) {
        b.setStyle(h
            ? "-fx-background-color:" + (isDark() ? accentRgba(0.22) : "rgba(15,23,42,0.12)") + ";-fx-background-radius:12px;-fx-text-fill:" + (isDark() ? "#e5e7eb" : "#111827") + ";-fx-cursor:hand;"
            : "-fx-background-color:transparent;-fx-background-radius:12px;-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.5)" : "rgba(15,23,42,0.55)") + ";-fx-cursor:hand;"
        );
    }

    void styleSignOutButton(Button b, boolean h) {
        b.setStyle(h
            ? "-fx-background-color:#800020;-fx-background-radius:12px;-fx-text-fill:#ffffff;-fx-cursor:hand;"
            : "-fx-background-color:rgba(128,0,32,0.85);-fx-background-radius:12px;-fx-text-fill:#ffe4ea;-fx-cursor:hand;"
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

        // Icon box: 36Ãƒâ€”36 rounded square matching .admin-sidebar-icon sizing
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(32, 32); iconBox.setMinSize(32, 32); iconBox.setMaxSize(32, 32);
        iconBox.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.06)") + ";-fx-background-radius:10px;");
        Text ic = new Text(icon); ic.setFont(Font.font(15));
        iconBox.getChildren().add(ic);

        inner.getChildren().add(iconBox);
        if (sidebarExpanded) {
            Text lbl = t(label, lightFont(), FontWeight.NORMAL, 15);
            lbl.setFill(isDark() ? Color.web("rgba(229,231,235,0.75)") : Color.web("rgba(15,23,42,0.72)"));
            inner.getChildren().add(lbl);
        }

        Button btn = new Button();
        btn.setGraphic(inner);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(sidebarExpanded ? Pos.CENTER_LEFT : Pos.CENTER);
        btn.setPadding(sidebarExpanded ? new Insets(6, 10, 6, 10) : new Insets(6, 6, 6, 6));
        btn.setPrefHeight(44);
        if (!sidebarExpanded) {
            btn.setPrefWidth(44);
            btn.setMinWidth(44);
            btn.setMaxWidth(44);
            Tooltip.install(btn, new Tooltip(label));
        }
        sectionButtons.put(section, btn);
        styleSidebarItem(btn, section.equals(activeSection));

        btn.setOnMouseEntered(e -> {
            if (!section.equals(activeSection)) {
                btn.setStyle("-fx-background-color:" + (isDark() ? accentRgba(0.15) : "rgba(15,23,42,0.08)") + ";-fx-background-radius:12px;-fx-cursor:hand;-fx-border-color:" + (isDark() ? accentRgba(0.22) : "rgba(15,23,42,0.14)") + ";-fx-border-width:1;-fx-border-radius:12px;-fx-effect:dropshadow(gaussian," + (isDark() ? "rgba(0,0,0,0.3)" : "rgba(15,23,42,0.12)") + ",16,0,0,4);");
                iconBox.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.1)" : "rgba(15,23,42,0.10)") + ";-fx-background-radius:10px;");
             }
        });
        btn.setOnMouseExited(e -> {
            styleSidebarItem(btn, section.equals(activeSection));
            if (!section.equals(activeSection)) iconBox.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.06)") + ";-fx-background-radius:10px;");
        });
        btn.setOnAction(e -> switchSection(section));
        return btn;
    }

    private void styleSidebarItem(Button b, boolean active) {
        if (active) {
            b.setStyle(
                "-fx-background-color:" + accentGradient() + ";" +
                "-fx-background-radius:12px;" +
                "-fx-cursor:hand;" +
                "-fx-effect:dropshadow(gaussian," + accentRgba(0.5) + ",20,0.3,0,6);"
            );
            // Re-style icon box inside active button to match web active item
            if (b.getGraphic() instanceof HBox) {
                HBox inner = (HBox) b.getGraphic();
                if (!inner.getChildren().isEmpty() && inner.getChildren().get(0) instanceof StackPane) {
                    StackPane ib = (StackPane) inner.getChildren().get(0);
                    ib.setStyle("-fx-background-color:rgba(255,255,255,0.15);-fx-background-radius:10px;");
                }
                if (inner.getChildren().size() > 1 && inner.getChildren().get(1) instanceof Text) {
                    ((Text) inner.getChildren().get(1)).setFill(Color.WHITE);
                }
            }
        } else {
            b.setStyle("-fx-background-color:transparent;-fx-background-radius:12px;-fx-cursor:hand;");
            if (b.getGraphic() instanceof HBox) {
                HBox inner = (HBox) b.getGraphic();
                if (!inner.getChildren().isEmpty() && inner.getChildren().get(0) instanceof StackPane) {
                    ((StackPane) inner.getChildren().get(0)).setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.06)") + ";-fx-background-radius:10px;");
                }
                if (inner.getChildren().size() > 1 && inner.getChildren().get(1) instanceof Text) {
                    ((Text) inner.getChildren().get(1)).setFill(isDark() ? Color.web("rgba(229,231,235,0.75)") : Color.web("rgba(15,23,42,0.72)"));
                }
            }
        }
    }

    private void switchSection(String section) {
        activeSection = section;
        sectionButtons.forEach((k, b) -> styleSidebarItem(b, k.equals(section)));
        contentArea.getChildren().setAll(buildSection(section));
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    // MAIN AREA
    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    VBox createMainArea() {
        VBox area = new VBox(0);
        area.setFillWidth(true);
        area.setStyle("-fx-background-color:" + (isDark() ? "#050505" : "#f3f4f6") + ";");

        contentArea = new VBox(0);
        contentArea.setFillWidth(true);
        contentArea.getChildren().add(buildSection(activeSection));

        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        area.getChildren().addAll(DashboardShell.buildHeader(this), scroll);
        return area;
    }

    HBox createAdminHeader() {
        return DashboardShell.buildHeader(this);
    }

    private StackPane buildNotificationTrigger() { return DashboardShell.buildNotificationTrigger(this); }

    private VBox buildNotificationDropdownCard() { return DashboardShell.buildNotificationDropdownCard(this); }

    private VBox notificationRow(String title, String body, String time) { return DashboardShell.notificationRow(this, title, body, time); }

    private void scheduleCloseNotificationPopup(StackPane anchor, VBox card) { DashboardShell.scheduleCloseNotificationPopup(this, anchor, card); }

    private void cancelNotificationCloseDelay() { DashboardShell.cancelNotificationCloseDelay(this); }

    private void showNotificationPopupInsideStage(StackPane bell, VBox card) { DashboardShell.showNotificationPopupInsideStage(this, bell, card); }

    private void attachProfileDropdown(HBox profilePill) { DashboardShell.attachProfileDropdown(this, profilePill); }

    private void showProfilePopupInsideStage(HBox profilePill, VBox card) { DashboardShell.showProfilePopupInsideStage(this, profilePill, card); }

    private Button dropdownItem(String icon, String label, Runnable action) { return DashboardShell.dropdownItem(this, icon, label, action); }

    private Button dropdownItemWithBadge(String icon, String label, String badge, Runnable action) { return DashboardShell.dropdownItemWithBadge(this, icon, label, badge, action); }

    private void openFrontendPage(String page) { DashboardShell.openFrontendPage(this, page); }

    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    // SECTION DISPATCHER
    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    VBox buildSection(String section) {
        switch (section) {
            case "users":     return buildUsersSection();
            case "forum":     return buildForumSection();
            case "syndicat":  return buildSyndicatSection();
            case "residence": return buildResidenceSection();
            case "evenement": return buildEvenementSection();
            default:          return buildGeneralSection();
        }
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    // GENERAL section
    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    VBox buildGeneralSection() {
        VBox s = new VBox(20); s.setFillWidth(true); s.setPadding(new Insets(24));
        VBox subContent = new VBox(20); subContent.setFillWidth(true);
        HBox subBar = subTabBar(new String[]{"Overview","Engagement","System"}, "Overview", key -> {
            subContent.getChildren().setAll(
                "Engagement".equals(key) ? buildEngagementContent() :
                "System".equals(key)     ? buildSystemContent()     :
                buildGeneralOverview()
            );
        });
        subContent.getChildren().add(buildGeneralOverview());
        s.getChildren().addAll(mainSwitcher(), subBar, subContent);
        return s;
    }

    private VBox buildGeneralOverview() {
        VBox v = new VBox(20); v.setFillWidth(true);
        HBox stats = new HBox(16); stats.setFillHeight(true);
        addStatCards(stats,
            new String[]{"USR","OK","ACT","PLS"},
            new String[]{"Total Residents","Active Today","Interactions Today","Community Pulse"},
            new String[]{"1,247","84","502","38"},
            new String[]{"#a78bfa","#34d399","#60a5fa","#fbbf24"}
        );
        HBox grid = new HBox(16); grid.setFillHeight(true);
        VBox chart = buildActivityChart(); HBox.setHgrow(chart, Priority.ALWAYS);
        VBox topU  = buildTopUsers(); topU.setPrefWidth(290); topU.setMinWidth(290); topU.setMaxWidth(290);
        grid.getChildren().addAll(chart, topU);
        v.getChildren().addAll(stats, grid);
        return v;
    }

    private VBox buildEngagementContent() {
        VBox v = new VBox(20); v.setFillWidth(true);
        VBox card = glassCard();
        Text title = t("Top Pages - Most Visited Routes", boldFont(), FontWeight.BOLD, 18); title.setFill(textPrimaryColor());
        VBox rows = new VBox(10);
        String[][] pages = {
            {"1","#a78bfa","/frontend/home",      "284 views"},
            {"2","#60a5fa","/frontend/forum",     "211 views"},
            {"3","#34d399","/frontend/profile",   "183 views"},
            {"4","#fbbf24","/frontend/evenement", "124 views"},
            {"5","#f87171","/admin/dashboard",    "98 views"}
        };
        for (String[] p : pages) {
            HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10,14,10,14));
            row.setStyle("-fx-background-color:rgba(255,255,255,0.02);-fx-background-radius:10px;-fx-border-color:rgba(255,255,255,0.05);-fx-border-width:1;-fx-border-radius:10px;");
            StackPane rd = new StackPane(); rd.setPrefSize(28,28);
            rd.setStyle("-fx-background-color:" + p[1] + "33;-fx-background-radius:14;");
            Text rn = t(p[0], boldFont(), FontWeight.BOLD, 11); rn.setFill(Color.web(p[1]));
            rd.getChildren().add(rn);
            Text rt = t(p[2], boldFont(), FontWeight.NORMAL, 13); rt.setFill(textSecondaryColor());
            Region spr = new Region(); HBox.setHgrow(spr, Priority.ALWAYS);
            Text cnt = t(p[3], boldFont(), FontWeight.BOLD, 13); cnt.setFill(Color.web(p[1]));
            row.getChildren().addAll(rd, rt, spr, cnt);
            rows.getChildren().add(row);
        }
        card.getChildren().addAll(title, rows);
        v.getChildren().add(card);
        return v;
    }

    private VBox buildSystemContent() {
        VBox v = new VBox(20); v.setFillWidth(true);
        HBox stats = new HBox(16); stats.setFillHeight(true);
        addStatCards(stats,
            new String[]{"SRV","DB","RT","PRC"},
            new String[]{"Server Status","DB Size","Avg Response","Active Processes"},
            new String[]{"Online","248 MB","124 ms","7"},
            new String[]{"#34d399","#60a5fa","#a78bfa","#fbbf24"}
        );
        VBox logCard = glassCard();
        Text lt = t("Recent System Events", boldFont(), FontWeight.BOLD, 18); lt.setFill(textPrimaryColor());
        VBox logs = new VBox(8);
        for (String[] ev : new String[][]{
            {"OK","Mar 12 09:14","User admin@syndicati.tn logged in"},
            {"WARN","Mar 12 08:52","Scheduled email batch: 58 sent"},
            {"OK","Mar 12 07:30","DB backup completed (248 MB)"},
            {"OK","Mar 11 22:00","Cache cleared successfully"},
            {"WARN","Mar 11 20:18","New user registration: Karim S."}
        }) {
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(7,10,7,10));
            row.setStyle("-fx-background-color:rgba(255,255,255,0.02);-fx-background-radius:8;");
            Text dot = new Text(ev[0]); Text ts = t(ev[1], lightFont(), FontWeight.NORMAL, 13); ts.setFill(textMutedColor());
            Text msg = t(ev[2], lightFont(), FontWeight.NORMAL, 14); msg.setFill(textSecondaryColor());
            row.getChildren().addAll(dot, ts, msg);
            logs.getChildren().add(row);
        }
        logCard.getChildren().addAll(lt, logs);
        v.getChildren().addAll(stats, logCard);
        return v;
    }

    private VBox buildActivityChart() {
        VBox card = glassCard();
        Text title = t("Activity Pulse", boldFont(), FontWeight.BOLD, 15); title.setFill(textPrimaryColor());
        Text sub   = t("Page Views \u25A0  UI Clicks \u25A0  \u2014 Last 7 Days", lightFont(), FontWeight.NORMAL, 13);
        sub.setFill(textMutedColor());

        HBox cw = new HBox(8); cw.setAlignment(Pos.BOTTOM_LEFT);
        cw.setPrefHeight(140); cw.setPadding(new Insets(8,0,0,0));
        String[] days  = {"MON","TUE","WED","THU","FRI","SAT","SUN"};
        int[] views    = {65,82,58,90,74,45,60};
        int[] clicks   = {42,55,38,70,52,30,44};
        for (int i = 0; i < days.length; i++) {
            VBox col = new VBox(4); col.setAlignment(Pos.BOTTOM_CENTER);
            HBox.setHgrow(col, Priority.ALWAYS);
            HBox pair = new HBox(3); pair.setAlignment(Pos.BOTTOM_CENTER);
            pair.getChildren().addAll(barR(Math.max(8, views[i]*120/100), "#a78bfa"), barR(Math.max(8, clicks[i]*120/100), "#34d399"));
            Text d = t(days[i], lightFont(), FontWeight.NORMAL, 11); d.setFill(textMutedColor());
            col.getChildren().addAll(pair, d);
            cw.getChildren().add(col);
        }
        HBox footer = new HBox(20); footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10,0,0,0));
        footer.setStyle("-fx-border-color:rgba(255,255,255,0.06);-fx-border-width:1 0 0 0;");
        footer.getChildren().addAll(
            metric("P","New Posts","+12","#a78bfa"),
            metric("E","New Events","+5","#34d399"),
            metric("G","Engagement","High","#60a5fa")
        );
        card.getChildren().addAll(title, sub, cw, footer);
        return card;
    }

    private VBox buildTopUsers() {
        VBox card = glassCard();
        Text title = t("\u2B50  Top Active Citizens", boldFont(), FontWeight.BOLD, 14); title.setFill(textPrimaryColor());
        VBox list = new VBox(6);
        for (String[] u : new String[][]{
            {"Ahmed B.","SYNDIC","142"}, {"Leila M.","RESIDENT","118"},
            {"Karim S.","ADMIN","95"},   {"Sara A.","RESIDENT","87"},
            {"Omar Z.","SYNDIC","76"}
        }) {
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6,8,6,8));
            row.setStyle("-fx-background-color:rgba(255,255,255,0.025);-fx-background-radius:8;");
            Text ini = t(u[0].substring(0,1), boldFont(), FontWeight.BOLD, 14); ini.setFill(Color.web("#fbbf24"));
            StackPane av = new StackPane(ini); av.setPrefSize(32,32);
            av.setStyle("-fx-background-color:rgba(251,191,36,0.15);-fx-background-radius:16;");
            VBox info = new VBox(1); HBox.setHgrow(info, Priority.ALWAYS);
            Text nm = t(u[0], lightFont(), FontWeight.NORMAL, 14); nm.setFill(textSecondaryColor());
            Text rl = t(u[1], lightFont(), FontWeight.NORMAL, 12); rl.setFill(Color.web("#fbbf24"));
            info.getChildren().addAll(nm, rl);
            Text pts = t(u[2], boldFont(), FontWeight.BOLD, 13); pts.setFill(textPrimaryColor());
            row.getChildren().addAll(av, info, pts);
            list.getChildren().add(row);
        }
        card.getChildren().addAll(title, list);
        return card;
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    // USERS section (Twig: switcher Users/Profile/Onboarding)
    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    VBox buildUsersSection() { return DashboardUsersSection.build(this); }

    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    // FORUM section (Twig: Publications/Commentaires/Reactions)
    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    VBox buildForumSection() { return DashboardForumSection.build(this); }

    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    // SYNDICAT section (Twig: Reclamations/Responses)
    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    VBox buildSyndicatSection() { return DashboardSyndicatSection.build(this); }

    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    // RESIDENCE section (Twig: Residences/Appartements/Maintenance)
    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    VBox buildResidenceSection() { return DashboardResidenceSection.build(this); }

    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    // EVENEMENT section (Twig: Evenements/Participations)
    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    VBox buildEvenementSection() { return DashboardEvenementSection.build(this); }

    String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DASHBOARD_DATE_TIME_FMT);
    }

    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    // SHARED HELPERS
    // Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    VBox moduleShell(String name, String icon) {
        VBox s = new VBox(20); s.setFillWidth(true); s.setPadding(new Insets(24));
        HBox heading = new HBox(10); heading.setAlignment(Pos.CENTER_LEFT);
        Text ic = new Text(icon); ic.setFont(Font.font(20));
        Text nm = t(name+" Overview", boldFont(), FontWeight.BOLD, 21); nm.setFill(textPrimaryColor());
        heading.getChildren().addAll(ic, nm);
        s.getChildren().add(heading);
        return s;
    }

    void addStatCards(HBox row, String[] icons, String[] labels, String[] values, String[] colors) {
        for (int i = 0; i < labels.length; i++) {
            VBox card = statCard(icons[i], labels[i], values[i], colors[i]);
            HBox.setHgrow(card, Priority.ALWAYS);
            row.getChildren().add(card);
        }
    }

    private VBox statCard(String icon, String label, String value, String color) {
        VBox card = new VBox(10); card.setPadding(new Insets(20)); card.setAlignment(Pos.TOP_LEFT);
        String normal = "-fx-background-color:rgba(255,255,255,0.03);-fx-background-radius:16px;-fx-border-color:rgba(255,255,255,0.07);-fx-border-width:1;-fx-border-radius:16px;";
        String hover  = "-fx-background-color:rgba(255,255,255,0.055);-fx-background-radius:16px;-fx-border-color:" + color + "55;-fx-border-width:1;-fx-border-radius:16px;";
        card.setStyle(normal);
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e  -> card.setStyle(normal));
        StackPane ic = new StackPane(); ic.setPrefSize(40,40);
        ic.setStyle("-fx-background-color:" + color + "25;-fx-background-radius:12px;");
        ic.getChildren().add(new Text(icon));
        Text lbl = t(label, lightFont(), FontWeight.NORMAL, 14); lbl.setFill(textMutedColor());
        Text val = t(value, boldFont(),  FontWeight.BOLD,   28); val.setFill(textPrimaryColor());
        Region gl = new Region(); gl.setPrefHeight(2);
        gl.setStyle("-fx-background-color:linear-gradient(to right,"+color+",transparent);-fx-background-radius:2;");
        card.getChildren().addAll(ic, lbl, val, gl);
        return card;
    }

    private VBox glassCard() {
        VBox c = new VBox(14); c.setPadding(new Insets(20)); c.setFillWidth(true);
        c.setStyle("-fx-background-color:rgba(255,255,255,0.03);-fx-background-radius:16px;-fx-border-color:rgba(255,255,255,0.07);-fx-border-width:1;-fx-border-radius:16px;");
        return c;
    }

    private VBox dataTable(String title, String[] cols, String[][] rows) {
        VBox card = glassCard();
        card.setPadding(new Insets(16, 16, 14, 16));

        // Website-like glass-table header
        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        Text tt = t(title, boldFont(), FontWeight.BOLD, 15); tt.setFill(textPrimaryColor());
        Text sub = t("Live module data", lightFont(), FontWeight.NORMAL, 13);
        sub.setFill(textMutedColor());
        VBox titleWrap = new VBox(2, tt, sub);
        head.getChildren().add(titleWrap);

        GridPane tbl = new GridPane(); tbl.setHgap(0); tbl.setVgap(0); tbl.setMaxWidth(Double.MAX_VALUE);
        for (int c = 0; c < cols.length; c++) {
            ColumnConstraints cc = new ColumnConstraints(); cc.setHgrow(Priority.ALWAYS); cc.setFillWidth(true);
            tbl.getColumnConstraints().add(cc);
        }
        for (int c = 0; c < cols.length; c++) {
            Text col = t(cols[c].toUpperCase(), boldFont(), FontWeight.BOLD, 12);
            col.setFill(textMutedColor());
            HBox cell = new HBox(col); cell.setPadding(new Insets(9,12,9,12));
            cell.setStyle("-fx-border-color:rgba(255,255,255,0.06);-fx-border-width:0 0 1 0;");
            tbl.add(cell, c, 0);
        }
        for (int r = 0; r < rows.length; r++) {
            final int ri = r;
            String bg = (r%2==0) ? "transparent" : "rgba(255,255,255,0.01)";
            for (int c = 0; c < rows[r].length; c++) {
                Text tx = t(rows[r][c], lightFont(), FontWeight.NORMAL, 14);
                tx.setFill(c==0 ? textSecondaryColor() : textMutedColor());
                HBox cb = new HBox(tx); cb.setPadding(new Insets(10,12,10,12)); cb.setStyle("-fx-background-color:"+bg+";");
                cb.setOnMouseEntered(e -> cb.setStyle("-fx-background-color:" + accentRgba(0.07) + ";"));
                cb.setOnMouseExited(e  -> cb.setStyle("-fx-background-color:"+bg+";"));
                tbl.add(cb, c, ri+1);
            }
        }

        // Pagination controls matching website pattern
        HBox pager = new HBox(8);
        pager.setAlignment(Pos.CENTER);
        pager.setPadding(new Insets(8, 0, 0, 0));
        pager.getChildren().addAll(
            pagerBtn("<", false),
            pagerBtn("1", true),
            pagerBtn("2", false),
            pagerBtn("3", false),
            pagerBtn(">", false)
        );

        card.getChildren().addAll(head, tbl, pager);
        return card;
    }

    VBox dataTableWithCrud(String title, String entityLabel, String[] cols, String[][] rows, boolean allowAdd, boolean allowEdit) {
        return dataTableWithCrud(title, entityLabel, cols, rows, allowAdd, allowEdit, null);
    }

    VBox dataTableWithCrud(String title, String entityLabel, String[] cols, String[][] rows, boolean allowAdd, boolean allowEdit, Node headerControls) {
        CrudSpec spec = crudSpec(title, entityLabel);
        
        StackPane faceContainer = new StackPane();
        faceContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
        faceContainer.setMaxHeight(Double.MAX_VALUE);
        faceContainer.setStyle("-fx-background-color:transparent;");
        faceContainer.setMaxWidth(Double.MAX_VALUE);
        
        VBox tableCard = new VBox(12);
        tableCard.setPadding(new Insets(12));
        tableCard.setStyle("-fx-background-color:transparent;");
        tableCard.setMaxWidth(Double.MAX_VALUE);
        
        VBox modalFace = new VBox(12);
        modalFace.setPadding(new Insets(12));
        modalFace.setStyle("-fx-background-color:transparent;");
        modalFace.setMaxWidth(Double.MAX_VALUE);
        modalFace.setVisible(false);
        
        VBox card = glassCard();
        card.setPadding(new Insets(16, 16, 14, 16));

        final int pageSize = 10;
        final String[][] sourceRows = rows == null ? new String[0][] : rows;

        final class PagerState {
            int page = 1;
            String searchTerm = "";
            String filterKey = "all";
        }
        final PagerState pagerState = new PagerState();

        final Runnable[] refreshTable = new Runnable[1];

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        Text tt = t(title, boldFont(), FontWeight.BOLD, 18); tt.setFill(textPrimaryColor());
        Text sub = t("Live module data", lightFont(), FontWeight.NORMAL, 13);
        sub.setFill(textMutedColor());
        VBox titleWrap = new VBox(2, tt, sub);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        head.getChildren().add(titleWrap);

        if ("Users Table".equals(title) && headerControls == null) {
            TextField usersSearch = new TextField();
            usersSearch.setPromptText("Search users...");
            usersSearch.setPrefWidth(240);
            usersSearch.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
            usersSearch.setStyle(
                "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.04)") + ";" +
                "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.14)") + ";" +
                "-fx-border-width:1;" +
                "-fx-border-radius:10px;" +
                "-fx-background-radius:10px;" +
                "-fx-text-fill:" + (isDark() ? "white" : "#111827") + ";" +
                "-fx-prompt-text-fill:" + (isDark() ? "rgba(255,255,255,0.45)" : "rgba(15,23,42,0.45)") + ";"
            );

            HBox usersFilters = new HBox(6);
            usersFilters.setAlignment(Pos.CENTER_LEFT);

            String[][] usersFilterDefs = new String[][]{
                {"all", "All"},
                {"verified", "Verified"},
                {"pending", "Pending"},
                {"active", "Active"},
                {"disabled", "Disabled"}
            };

            Map<String, Button> usersFilterButtons = new LinkedHashMap<>();
            for (String[] def : usersFilterDefs) {
                String key = def[0];
                Button b = pillAction(def[1], "all".equals(key));
                b.setOnAction(e -> {
                    pagerState.filterKey = key;
                    pagerState.page = 1;
                    usersFilterButtons.forEach((k, btn) -> {
                        btn.setStyle(k.equals(pagerState.filterKey)
                            ? "-fx-background-color:" + accentRgba(0.24) + ";-fx-border-color:" + accentRgba(0.34) + ";-fx-border-width:1;-fx-background-radius:100px;-fx-border-radius:100px;-fx-text-fill:white;-fx-cursor:hand;"
                            : "-fx-background-color:transparent;-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.20)") + ";-fx-border-width:1;-fx-background-radius:100px;-fx-border-radius:100px;-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.80)" : "rgba(15,23,42,0.86)") + ";-fx-cursor:hand;"
                        );
                    });
                    refreshTable[0].run();
                });
                usersFilterButtons.put(key, b);
                usersFilters.getChildren().add(b);
            }

            usersSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                pagerState.searchTerm = newVal == null ? "" : newVal;
                pagerState.page = 1;
                refreshTable[0].run();
            });

            VBox usersControls = new VBox(6, usersSearch, usersFilters);
            usersControls.setAlignment(Pos.CENTER_LEFT);
            head.getChildren().add(usersControls);
        }

        if (headerControls != null) {
            if (headerControls instanceof Region) {
                ((Region) headerControls).setMinWidth(Region.USE_PREF_SIZE);
            }
            head.getChildren().add(headerControls);
        }

        head.getChildren().add(spacer);

        if (allowAdd) {
            Button addBtn = pillAction(spec.addButtonLabel, true);
            addBtn.setOnAction(e -> switchToModalFace(faceContainer, spec, entityLabel, "add", cols, null));
            head.getChildren().add(addBtn);
        }

        GridPane tbl = new GridPane();
        tbl.setHgap(0);
        tbl.setVgap(0);
        tbl.setMaxWidth(Double.MAX_VALUE);
        VBox tableWrap = new VBox(tbl);
        tableWrap.setFillWidth(true);

        int displayCols = cols.length + 1;
        for (int c = 0; c < displayCols; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            tbl.getColumnConstraints().add(cc);
        }
        HBox pagerInfo = new HBox(8);
        pagerInfo.setAlignment(Pos.CENTER_LEFT);
        Text pageLabel = t("", lightFont(), FontWeight.NORMAL, 12);
        pageLabel.setFill(textMutedColor());
        Button prevBtn = pillAction("<", false);
        Button nextBtn = pillAction(">", false);
        HBox pageNumberBox = new HBox(6);
        pageNumberBox.setAlignment(Pos.CENTER);
        HBox pager = new HBox(8, prevBtn, pageNumberBox, nextBtn, pageLabel);
        pager.setAlignment(Pos.CENTER);
        pager.setPadding(new Insets(8, 0, 0, 0));

        for (int c = 0; c < cols.length; c++) {
            Text col = t(cols[c].toUpperCase(), boldFont(), FontWeight.BOLD, 12);
            col.setFill(textMutedColor());
            HBox cell = new HBox(col);
            cell.setPadding(new Insets(9,12,9,12));
            cell.setStyle("-fx-border-color:rgba(255,255,255,0.06);-fx-border-width:0 0 1 0;");
            tbl.add(cell, c, 0);
        }
        Text actionCol = t("ACTIONS", boldFont(), FontWeight.BOLD, 12);
        actionCol.setFill(textMutedColor());
        HBox actionHeader = new HBox(actionCol);
        actionHeader.setPadding(new Insets(9,12,9,12));
        actionHeader.setStyle("-fx-border-color:rgba(255,255,255,0.06);-fx-border-width:0 0 1 0;");
        tbl.add(actionHeader, cols.length, 0);

        refreshTable[0] = () -> {
            tbl.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

            List<String[]> workingRows = new ArrayList<>();
            for (String[] row : sourceRows) {
                if (row == null) {
                    continue;
                }

                if ("Users Table".equals(title)) {
                    String q = pagerState.searchTerm == null ? "" : pagerState.searchTerm.trim().toLowerCase();
                    if (!q.isEmpty()) {
                        boolean match = false;
                        for (String cell : row) {
                            if (cell != null && cell.toLowerCase().contains(q)) {
                                match = true;
                                break;
                            }
                        }
                        if (!match) {
                            continue;
                        }
                    }

                    String key = pagerState.filterKey == null ? "all" : pagerState.filterKey;
                    if ("verified".equals(key) && (row.length < 4 || !"Yes".equalsIgnoreCase(row[3]))) {
                        continue;
                    }
                    if ("pending".equals(key) && (row.length < 5 || !"Pending".equalsIgnoreCase(row[4]))) {
                        continue;
                    }
                    if ("active".equals(key) && (row.length < 5 || !"Active".equalsIgnoreCase(row[4]))) {
                        continue;
                    }
                    if ("disabled".equals(key) && (row.length < 5 || !"Disabled".equalsIgnoreCase(row[4]))) {
                        continue;
                    }
                }

                workingRows.add(row);
            }

            int totalRows = workingRows.size();
            int totalPages = Math.max(1, (int) Math.ceil(totalRows / (double) pageSize));

            if (totalRows == 0) {
                HBox emptyCell = new HBox(t("No records found", lightFont(), FontWeight.NORMAL, 14));
                emptyCell.setPadding(new Insets(12, 12, 12, 12));
                emptyCell.setStyle("-fx-border-color:rgba(255,255,255,0.06);-fx-border-width:0 0 1 0;");
                tbl.add(emptyCell, 0, 1, cols.length + 1, 1);
                pageLabel.setText("Page 1 / 1 | 0 records");
                prevBtn.setDisable(true);
                nextBtn.setDisable(true);
                prevBtn.setOpacity(0.55);
                nextBtn.setOpacity(0.55);
                return;
            }

            int safePage = Math.max(1, Math.min(pagerState.page, totalPages));
            pagerState.page = safePage;
            int fromIndex = (safePage - 1) * pageSize;
            int toIndex = Math.min(fromIndex + pageSize, totalRows);

            for (int r = fromIndex; r < toIndex; r++) {
                String bg = ((r - fromIndex) % 2 == 0) ? "transparent" : "rgba(255,255,255,0.01)";
                String[] rowData = workingRows.get(r);
                for (int c = 0; c < rowData.length; c++) {
                    Text tx = t(rowData[c], lightFont(), FontWeight.NORMAL, 14);
                    tx.setFill(c == 0 ? textSecondaryColor() : textMutedColor());
                    HBox cb = new HBox(tx);
                    cb.setPadding(new Insets(10,12,10,12));
                    cb.setStyle("-fx-background-color:" + bg + ";");
                    cb.setOnMouseEntered(e -> cb.setStyle("-fx-background-color:" + accentRgba(0.07) + ";"));
                    cb.setOnMouseExited(e -> cb.setStyle("-fx-background-color:" + bg + ";"));
                    tbl.add(cb, c, (r - fromIndex) + 1);
                }

                HBox rowActions = new HBox(6);
                rowActions.setAlignment(Pos.CENTER_LEFT);
                rowActions.setPadding(new Insets(8, 12, 8, 12));
                rowActions.setStyle("-fx-background-color:" + bg + ";");

                Button viewBtn = pillAction("View", false);
                viewBtn.setOnAction(e -> switchToModalFace(faceContainer, spec, entityLabel, "view", cols, rowData));
                rowActions.getChildren().add(viewBtn);

                tbl.add(rowActions, cols.length, (r - fromIndex) + 1);
            }

            pageLabel.setText("Page " + safePage + " / " + totalPages + " | " + totalRows + " records");
            prevBtn.setDisable(safePage <= 1);
            nextBtn.setDisable(safePage >= totalPages);
            prevBtn.setOpacity(prevBtn.isDisable() ? 0.55 : 1.0);
            nextBtn.setOpacity(nextBtn.isDisable() ? 0.55 : 1.0);

            pageNumberBox.getChildren().clear();
            int maxButtons = 3;
            int start = Math.max(1, safePage - 1);
            int end = Math.min(totalPages, start + maxButtons - 1);
            start = Math.max(1, end - maxButtons + 1);

            for (int pageNum = start; pageNum <= end; pageNum++) {
                final int targetPage = pageNum;
                Button pageBtn = pagerBtn(String.valueOf(pageNum), pageNum == safePage);
                pageBtn.setDisable(pageNum == safePage);
                pageBtn.setOpacity(pageNum == safePage ? 1.0 : 0.95);
                pageBtn.setOnAction(e -> {
                    pagerState.page = targetPage;
                    refreshTable[0].run();
                });
                pageNumberBox.getChildren().add(pageBtn);
            }
        };

        prevBtn.setOnAction(e -> {
            pagerState.page--;
            refreshTable[0].run();
        });
        nextBtn.setOnAction(e -> {
            pagerState.page++;
            refreshTable[0].run();
        });

        refreshTable[0].run();

        card.getChildren().addAll(head, tableWrap, pager);
        tableCard.getChildren().add(card);
        
        StackPane.setAlignment(tableCard, Pos.TOP_CENTER);
        StackPane.setAlignment(modalFace, Pos.TOP_CENTER);
        faceContainer.getChildren().addAll(tableCard, modalFace);
        faceContainer.setUserData(modalFace);
        
        VBox wrap = new VBox(faceContainer);
        wrap.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(faceContainer, Priority.ALWAYS);
        return wrap;
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
                s.cancelLabel = "Back to List";
                s.viewDeleteLabel = null;
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
        b.setFont(Font.font(lightFont(), FontWeight.NORMAL, 10));
        b.setPadding(new Insets(4, 10, 4, 10));
        b.setStyle(primary
            ? "-fx-background-color:" + accentRgba(0.24) + ";-fx-border-color:" + accentRgba(0.34) + ";-fx-border-width:1;-fx-background-radius:100px;-fx-border-radius:100px;-fx-text-fill:white;-fx-cursor:hand;"
            : "-fx-background-color:transparent;-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.20)") + ";-fx-border-width:1;-fx-background-radius:100px;-fx-border-radius:100px;-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.80)" : "rgba(15,23,42,0.86)") + ";-fx-cursor:hand;"
        );
        return b;
    }

    private void switchToModalFace(StackPane container, CrudSpec spec, String entityLabel, String mode, String[] cols, String[] rowData) {
        VBox modalFace = (VBox) container.getUserData();
        boolean editable = "edit".equals(mode) || "add".equals(mode);
        String title = "view".equals(mode) ? spec.viewTitle : ("edit".equals(mode) ? spec.editTitle : spec.addTitle);
        String subtitle = "view".equals(mode) ? spec.viewSubtitle : ("edit".equals(mode) ? spec.editSubtitle : spec.addSubtitle);

        modalFace.getChildren().clear();
        
        VBox modalCard = glassCard();
        modalCard.setPadding(new Insets(16, 16, 14, 16));

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        VBox tWrap = new VBox(3);
        Text hTitle = t(title, boldFont(), FontWeight.BOLD, 19); hTitle.setFill(textPrimaryColor());
        Text hSub = t(subtitle, lightFont(), FontWeight.NORMAL, 13); hSub.setFill(textMutedColor());
        tWrap.getChildren().addAll(hTitle, hSub);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button close = new Button("x");
        close.setPadding(new Insets(6, 10, 6, 10));
        close.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.08)") + ";-fx-background-radius:999;-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.85)" : "rgba(15,23,42,0.85)") + ";-fx-cursor:hand;");
        close.setOnAction(e -> switchToTableFace(container));
        head.getChildren().addAll(tWrap, spacer, close);

        VBox fields = buildModalFields(spec, mode, cols, rowData, editable);

        ScrollPane formScroll = new ScrollPane(fields);
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        formScroll.setPrefViewportHeight(300);
        formScroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");
        VBox.setVgrow(formScroll, Priority.ALWAYS);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = pillAction(spec.cancelLabel, false);
        cancel.setOnAction(e -> switchToTableFace(container));
        actions.getChildren().add(cancel);
        
        if ("view".equals(mode)) {
            if (spec.viewDeleteLabel != null) {
                Button del = dangerAction(spec.viewDeleteLabel);
                del.setOnAction(e -> {
                    if (dashboardAdminService.deleteEntity(entityLabel, rowData)) {
                        switchSection(activeSection);
                    } else {
                        switchToTableFace(container);
                    }
                });
                actions.getChildren().add(del);
            }
            Button edit = pillAction("Edit", true);
            edit.setOnAction(e -> switchToModalFace(container, spec, entityLabel, "edit", cols, rowData));
            actions.getChildren().add(edit);
        } else {
            String saveLabel = "add".equals(mode) ? spec.saveAddLabel : spec.saveEditLabel;
            Button save = pillAction(saveLabel, true);
            save.setOnAction(e -> {
                if (dashboardAdminService.saveEntity(entityLabel, mode, rowData, fields)) {
                    switchSection(activeSection);
                } else {
                    switchToTableFace(container);
                }
            });
            actions.getChildren().add(save);
        }

        modalCard.getChildren().addAll(head, formScroll, actions);
        modalFace.getChildren().add(modalCard);

        ObservableList<Node> children = container.getChildren();
        VBox tableCard = (VBox) children.get(0);
        tableCard.setVisible(false);
        modalFace.setVisible(true);
    }

    private void switchToTableFace(StackPane container) {
        ObservableList<Node> children = container.getChildren();
        VBox tableCard = (VBox) children.get(0);
        VBox modalFace = (VBox) children.get(1);
        
        tableCard.setVisible(true);
        modalFace.setVisible(false);
    }

    private VBox buildModalFields(CrudSpec spec, String mode, String[] cols, String[] rowData, boolean editable) {
        VBox fields = new VBox(10);

        if ("view".equals(mode)) {
            fields.getChildren().add(sectionTitle("Overview"));
            fields.getChildren().add(metaStrip(rowData));
        } else if ("edit".equals(mode)) {
            fields.getChildren().add(sectionTitle("Editable Fields"));
        } else {
            fields.getChildren().add(sectionTitle("Create New Record"));
        }

        for (int i = 0; i < cols.length; i++) {
            String val = (rowData != null && i < rowData.length) ? rowData[i] : "";
            fields.getChildren().add(fieldRow(cols[i], val, editable));
        }

        if ("edit".equals(mode) || "add".equals(mode)) {
            fields.getChildren().add(sectionTitle("Flags & Metadata"));
            fields.getChildren().add(infoChipRow(
                "Active", "Verified", "Synced", "Tracked"
            ));
            fields.getChildren().add(notesBox("Internal notes", "Add context for admins (reason, follow-up, priority)."));
        }

        return fields;
    }

    private Text sectionTitle(String text) {
        Text t = t(text, boldFont(), FontWeight.BOLD, 16);
        t.setFill(textSecondaryColor());
        return t;
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

    private HBox infoChipRow(String... labels) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < labels.length; i++) {
            row.getChildren().add(chip(labels[i], i % 2 == 1));
        }
        return row;
    }

    private Region chip(String label, boolean accent) {
        Text tx = t(label, lightFont(), FontWeight.NORMAL, 12);
        tx.setFill(accent ? Color.WHITE : textSecondaryColor());
        HBox box = new HBox(tx);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setStyle(accent
            ? "-fx-background-color:" + accentRgba(0.26) + ";-fx-border-color:" + accentRgba(0.36) + ";-fx-border-width:1;-fx-background-radius:999;-fx-border-radius:999;"
            : "-fx-background-color:rgba(255,255,255,0.05);-fx-border-color:rgba(255,255,255,0.12);-fx-border-width:1;-fx-background-radius:999;-fx-border-radius:999;"
        );
        return box;
    }

    private VBox notesBox(String label, String value) {
        VBox wrap = new VBox(4);
        Text lbl = t(label, lightFont(), FontWeight.NORMAL, 13);
        lbl.setFill(textMutedColor());
        Text val = t(value, lightFont(), FontWeight.NORMAL, 13);
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

    private Node fieldRow(String label, String value, boolean editable) {
        VBox row = new VBox(4);
        Text lbl = t(label, lightFont(), FontWeight.NORMAL, 11);
        lbl.setFill(textMutedColor());

        if (editable) {
            TextField input = new TextField(value);
            input.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
            input.setStyle(
                "-fx-background-color:rgba(255,255,255,0.05);" +
                "-fx-border-color:rgba(255,255,255,0.12);" +
                "-fx-border-width:1;" +
                "-fx-text-fill:" + (isDark() ? "white" : "#111827") + ";" +
                "-fx-background-radius:10px;" +
                "-fx-border-radius:10px;" +
                "-fx-padding:8 10 8 10;"
            );
            row.getChildren().addAll(lbl, input);
        } else {
            Text val = t(value, lightFont(), FontWeight.NORMAL, 14);
            val.setFill(textSecondaryColor());
            VBox box = new VBox(val);
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

    private Button dangerAction(String text) {
        Button b = new Button(text);
        b.setFont(Font.font(lightFont(), FontWeight.NORMAL, 10));
        b.setPadding(new Insets(5, 12, 5, 12));
        b.setStyle(
            "-fx-background-color:rgba(239,68,68,0.18);" +
            "-fx-border-color:rgba(239,68,68,0.45);" +
            "-fx-border-width:1;" +
            "-fx-background-radius:999;" +
            "-fx-border-radius:999;" +
            "-fx-text-fill:#fecaca;" +
            "-fx-cursor:hand;"
        );
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
        b.setStyle(active
            ? "-fx-background-color:" + accentGradient() + ";-fx-background-radius:8px;-fx-text-fill:white;-fx-cursor:hand;"
            : "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.05)") + ";-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.10)" : "rgba(15,23,42,0.14)") + ";-fx-border-width:1;-fx-border-radius:8px;-fx-background-radius:8px;-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.72)" : "rgba(15,23,42,0.78)") + ";-fx-cursor:hand;"
        );
        return b;
    }

    private HBox mainSwitcher() {
        HBox c = new HBox(); c.setAlignment(Pos.CENTER);
        HBox pill = new HBox(0); pill.setAlignment(Pos.CENTER); pill.setPadding(new Insets(4));
        pill.setStyle("-fx-background-color:" + (isDark() ? "rgba(10,10,10,0.65)" : "rgba(248,250,252,0.96)") + ";-fx-background-radius:100px;-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.1)" : "rgba(15,23,42,0.14)") + ";-fx-border-width:1;-fx-border-radius:100px;");
        String[] labels   = {"General","Users","Forum","Syndicat","Residence","Evenement"};
        String[] sections = {"general","users","forum","syndicat","residence","evenement"};
        for (int i = 0; i < labels.length; i++) {
            final String sec = sections[i];
            Button tab = new Button(labels[i]);
            tab.setFont(Font.font(boldFont(), FontWeight.BOLD, 12));
            tab.setPadding(new Insets(8,18,8,18));
            tab.setStyle(sec.equals(activeSection)
                ? "-fx-background-color:" + accentGradient() + ";-fx-background-radius:100px;-fx-text-fill:white;-fx-cursor:hand;"
                : "-fx-background-color:transparent;-fx-background-radius:100px;-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.45)" : "rgba(15,23,42,0.74)") + ";-fx-cursor:hand;"
            );
            tab.setOnAction(e -> switchSection(sec));
            pill.getChildren().add(tab);
        }
        c.getChildren().add(pill);
        return c;
    }

    /** Switcher used inside module pages (Users/Profile/Onboarding, etc.). */
    HBox moduleModeSwitcher(String[] labels, String activeLabel, Consumer<String> onSelect) {
        HBox wrap = new HBox();
        wrap.setAlignment(Pos.CENTER);

        HBox pill = new HBox(6);
        pill.setAlignment(Pos.CENTER);
        pill.setPadding(new Insets(6));
        pill.setStyle(
            "-fx-background-color:" + (isDark() ? "rgba(10,10,10,0.45)" : "rgba(248,250,252,0.95)") + ";" +
            "-fx-background-radius:100px;" +
            "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.1)" : "rgba(15,23,42,0.14)") + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:100px;" +
            "-fx-effect:dropshadow(gaussian," + (isDark() ? "rgba(0,0,0,0.35)" : "rgba(15,23,42,0.10)") + ",20,0,0,6);"
        );

        List<Button> tabButtons = new ArrayList<>();

        for (String label : labels) {
            Button tab = new Button(label);
            tab.setFont(Font.font(boldFont(), FontWeight.BOLD, 12));
            tab.setPadding(new Insets(8, 18, 8, 18));
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

            tab.setOnAction(e -> {
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
                        "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.55)" : "rgba(15,23,42,0.78)") + ";" +
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

    private HBox subTabBar(String[] labels, String activeLabel, Consumer<String> onSelect) {
        HBox c = new HBox(); c.setAlignment(Pos.CENTER);
        HBox pill = new HBox(4); pill.setAlignment(Pos.CENTER); pill.setPadding(new Insets(6));
        pill.setStyle("-fx-background-color:" + (isDark() ? "rgba(15,15,17,0.65)" : "rgba(248,250,252,0.95)") + ";-fx-background-radius:16px;-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.14)") + ";-fx-border-width:1;-fx-border-radius:16px;");
        for (String label : labels) {
            Button btn = new Button(label.toUpperCase());
            btn.setFont(Font.font(boldFont(), FontWeight.BOLD, 11));
            btn.setPadding(new Insets(5,16,5,16));
            styleSubTab(btn, label.equals(activeLabel));
            btn.setOnAction(e -> {
                pill.getChildren().forEach(n -> { if (n instanceof Button) styleSubTab((Button)n, false); });
                styleSubTab(btn, true);
                onSelect.accept(label);
            });
            pill.getChildren().add(btn);
        }
        c.getChildren().add(pill);
        return c;
    }

    private void styleSubTab(Button b, boolean active) {
        b.setStyle(active
            ? "-fx-background-color:" + accentRgba(0.2) + ";-fx-background-radius:12px;-fx-text-fill:white;-fx-border-color:" + accentRgba(0.3) + ";-fx-border-width:1;-fx-border-radius:12px;-fx-cursor:hand;"
            : "-fx-background-color:transparent;-fx-background-radius:12px;-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.4)" : "rgba(15,23,42,0.7)") + ";-fx-border-color:transparent;-fx-border-width:1;-fx-border-radius:12px;-fx-cursor:hand;"
        );
    }

    private HBox metric(String icon, String label, String val, String color) {
        HBox row = new HBox(6); row.setAlignment(Pos.CENTER_LEFT);
        Text ic = new Text(icon); ic.setFont(Font.font(13));
        Text lb = t(label, lightFont(), FontWeight.NORMAL, 13); lb.setFill(textMutedColor());
        Text vl = t(val,   boldFont(),  FontWeight.BOLD,   11); vl.setFill(Color.web(color));
        row.getChildren().addAll(ic, lb, vl);
        return row;
    }

    private Region barR(int h, String color) {
        Region r = new Region(); r.setPrefWidth(12); r.setPrefHeight(h);
        r.setStyle("-fx-background-color:"+color+";-fx-background-radius:4 4 0 0;");
        return r;
    }

    Text t(String s, String family, FontWeight w, double size) {
        Text tx = new Text(s);
        tx.setFont(Font.font(family, w, size));
        tx.setFill(textPrimaryColor());
        return tx;
    }
    String boldFont()  { return com.syndicati.MainApplication.getInstance().getBoldFontFamily();  }
    String lightFont() { return com.syndicati.MainApplication.getInstance().getLightFontFamily(); }
}


