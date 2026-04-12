package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.backend.users.UserController;
import com.syndicati.models.entities.*;
import com.syndicati.models.services.ServiceAppartement;
import com.syndicati.models.services.ServiceMaintenance;
import com.syndicati.models.services.ServiceResidence;
import javafx.application.Platform;
import javafx.animation.TranslateTransition;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.utils.navigation.NavigationManager;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.image.ImageLoaderUtil;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private String activeSection = "general";
    private final Map<String, Button> sectionButtons = new HashMap<>();
    private VBox contentArea;
    private Runnable exitCallback;
    private final Runnable accentRefreshListener;
    private boolean sidebarExpanded = true;
    private Popup profilePopup;
    private Popup notificationPopup;
    private PauseTransition notificationHideDelay;
    private final UserController userController;
    private static final DateTimeFormatter DASHBOARD_DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    ServiceResidence ServiceResidence = new ServiceResidence();
    ServiceAppartement ServiceAppartement = new ServiceAppartement();
    ServiceMaintenance ServiceMaintenance = new ServiceMaintenance();

    List<Residence> ListeResidences;
    List<Appartement> ListeAppartements;
    List<Maintenance> ListeMaintenances;

    {
        try {
            ListeResidences = ServiceResidence.Recuperer();
            ListeAppartements = ServiceAppartement.Recuperer();
            ListeMaintenances = ServiceMaintenance.Recuperer();

        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }
    }

    public DashboardView() {
        this.root = new HBox();
        this.accentRefreshListener = this::refreshAccentStyling;
        this.userController = new UserController();
        setupLayout();
        ThemeManager.getInstance().addAccentChangeListener(accentRefreshListener);
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
        VBox sidebar  = createSidebar();
        sidebar.setPrefWidth(sidebarWidth()); sidebar.setMinWidth(sidebarWidth()); sidebar.setMaxWidth(sidebarWidth());
        VBox mainArea = createMainArea();
        HBox.setHgrow(mainArea, Priority.ALWAYS);
        root.getChildren().addAll(sidebar, mainArea);

        activeSection = section;
        contentArea.getChildren().setAll(buildSection(section));
        sectionButtons.forEach((k, b) -> styleSidebarItem(b, k.equals(section)));
    }

    private ThemeManager theme() { return ThemeManager.getInstance(); }
    private String accentHex() { return theme().getAccentHex(); }
    private String accentGradient() { return theme().getEffectiveAccentGradient(); }
    private String accentRgba(double alpha) { return theme().toRgba(accentHex(), alpha); }
    private boolean isDark() { return theme().isDarkMode(); }
    private Color textPrimaryColor() { return isDark() ? Color.web("#f8fafc") : Color.web("#111827"); }
    private Color textSecondaryColor() { return isDark() ? Color.web("rgba(255,255,255,0.78)") : Color.web("rgba(17,24,39,0.82)"); }
    private Color textMutedColor() { return isDark() ? Color.web("rgba(255,255,255,0.55)") : Color.web("rgba(30,41,59,0.64)"); }

    private String currentDisplayName() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            return "Admin User";
        }

        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? "User" : fullName;
    }

    private String currentEmail() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null || user.getEmailUser() == null || user.getEmailUser().isBlank()) {
            return "contact@syndicati.tn";
        }
        return user.getEmailUser();
    }

    private String currentRoleLabel() {
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

    private String currentInitial() {
        String name = currentDisplayName();
        if (!name.isBlank()) {
            return name.substring(0, 1).toUpperCase();
        }
        String email = currentEmail();
        return email.isBlank() ? "U" : email.substring(0, 1).toUpperCase();
    }

    private boolean applyAvatarFill(Circle avatarCircle) {
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

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
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

        VBox sidebar  = createSidebar();
        sidebar.setPrefWidth(sidebarWidth()); sidebar.setMinWidth(sidebarWidth()); sidebar.setMaxWidth(sidebarWidth());

        VBox mainArea = createMainArea();
        HBox.setHgrow(mainArea, Priority.ALWAYS);
        root.getChildren().addAll(sidebar, mainArea);
    }

    private double sidebarWidth() {
        return sidebarExpanded ? 250 : 96;
    }

    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        refreshAccentStyling();
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // SIDEBAR  â€” three floating glass pills (matches web admin CSS)
    //   .admin-sidebar-top | .admin-sidebar-nav | .admin-sidebar-bottom
    //   each: backdrop-blur glass, border-radius 24px, transparent gap
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private VBox createSidebar() {
        // Outer wrapper: transparent, no background â€“ only the three pills show
        VBox sb = new VBox(10);
        sb.setAlignment(Pos.TOP_CENTER);
        sb.setFillWidth(true);
        VBox.setVgrow(sb, Priority.ALWAYS);
        sb.setStyle("-fx-background-color:transparent;");
        sb.setPadding(new Insets(12, 10, 12, 10));

        // â”€â”€ TOP PILL: logo â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        VBox topPill = glassPill(Pos.CENTER);
        topPill.setPadding(new Insets(12, 10, 12, 10));
        topPill.setSpacing(8);

        // Logo circle with "S" initial (mimics logo-img)
        StackPane logoMark = new StackPane();
        logoMark.setPrefSize(44, 44);
        logoMark.setStyle(
            "-fx-background-color:" + accentGradient() + ";" +
            "-fx-background-radius:14px;" +
            "-fx-effect:dropshadow(gaussian," + accentRgba(0.45) + ",18,0.4,0,4);"
        );
        Text sLetter = t("S", boldFont(), FontWeight.BOLD, 26); sLetter.setFill(Color.WHITE);
        logoMark.getChildren().add(sLetter);

        VBox logoText = new VBox(1);
        logoText.setAlignment(Pos.CENTER_LEFT);
        Text logoT = t("SYNDICATI", boldFont(), FontWeight.BOLD, 15); logoT.setFill(Color.WHITE);
        Text adminT = t("Admin Panel", lightFont(), FontWeight.NORMAL, 12); adminT.setFill(isDark() ? Color.web("rgba(255,255,255,0.4)") : Color.web("rgba(15,23,42,0.55)"));
        logoT.setFill(isDark() ? Color.WHITE : Color.web("#111827"));
        logoText.getChildren().addAll(logoT, adminT);

        HBox logoRow = new HBox(10); logoRow.setAlignment(Pos.CENTER_LEFT);
        if (sidebarExpanded) {
            logoRow.getChildren().addAll(logoMark, logoText);
        } else {
            logoRow.setAlignment(Pos.CENTER);
            logoRow.getChildren().add(logoMark);
        }

        Button toggle = new Button(sidebarExpanded ? "<" : ">");
        toggle.setFont(Font.font(boldFont(), FontWeight.BOLD, 12));
        toggle.setPadding(new Insets(7, 10, 7, 10));
        toggle.setMaxWidth(sidebarExpanded ? Double.MAX_VALUE : Region.USE_PREF_SIZE);
        toggle.setStyle(
            "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.06)") + ";" +
            "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.14)") + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:10px;" +
            "-fx-background-radius:10px;" +
            "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.8)" : "rgba(15,23,42,0.84)") + ";" +
            "-fx-cursor:hand;"
        );
        toggle.setOnAction(e -> toggleSidebar());
        Tooltip.install(toggle, new Tooltip(sidebarExpanded ? "Collapse sidebar" : "Expand sidebar"));

        topPill.getChildren().addAll(logoRow, toggle);

        // â”€â”€ NAV PILL: main items â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        VBox navPill = glassPill(Pos.TOP_CENTER);
        navPill.setPadding(new Insets(14, sidebarExpanded ? 10 : 6, 14, sidebarExpanded ? 10 : 6));
        navPill.setSpacing(4);
        VBox.setVgrow(navPill, Priority.ALWAYS);

        navPill.getChildren().addAll(
            sidebarItem("\uD83D\uDCCA", "Dashboard",  "general"),
            pillSep(),
            sidebarItem("\uD83D\uDC65", "Users",      "users"),
            sidebarItem("\uD83D\uDCAC", "Forum",      "forum"),
            sidebarItem("\uD83C\uDFDB\uFE0F", "Syndicat",  "syndicat"),
            sidebarItem("\uD83C\uDFE2", "Residence",  "residence"),
            sidebarItem("\uD83C\uDF89", "Evenement",  "evenement")
        );

        // â”€â”€ BOTTOM PILL: back to app + sign out â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        VBox bottomPill = glassPill(Pos.CENTER);
        bottomPill.setPadding(new Insets(10, sidebarExpanded ? 10 : 6, 10, sidebarExpanded ? 10 : 6));
        bottomPill.setSpacing(8);

        Button back = new Button(sidebarExpanded ? "\u2302  Back to App" : "\u2302");
        back.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        back.setMaxWidth(Double.MAX_VALUE);
        back.setPadding(new Insets(10, 14, 10, 14));
        back.setAlignment(sidebarExpanded ? Pos.CENTER_LEFT : Pos.CENTER);
        styleBackButton(back, false);
        back.setOnMouseEntered(e -> styleBackButton(back, true));
        back.setOnMouseExited(e  -> styleBackButton(back, false));
        back.setOnAction(e -> { if (exitCallback != null) exitCallback.run(); });
        if (!sidebarExpanded) Tooltip.install(back, new Tooltip("Back to App"));

        Button signOut = new Button(sidebarExpanded ? "\u23FB  Sign out" : "\u23FB");
        signOut.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        signOut.setMaxWidth(Double.MAX_VALUE);
        signOut.setPadding(new Insets(10, 14, 10, 14));
        signOut.setAlignment(sidebarExpanded ? Pos.CENTER_LEFT : Pos.CENTER);
        styleSignOutButton(signOut, false);
        signOut.setOnMouseEntered(e -> styleSignOutButton(signOut, true));
        signOut.setOnMouseExited(e  -> styleSignOutButton(signOut, false));
        signOut.setOnAction(e -> com.syndicati.MainApplication.getInstance().logout());
        if (!sidebarExpanded) Tooltip.install(signOut, new Tooltip("Sign out"));

        bottomPill.getChildren().addAll(back, signOut);

        sb.getChildren().addAll(topPill, navPill, bottomPill);
        return sb;
    }

    /** Shared glass pill container â€” three of these make up the sidebar. */
    private VBox glassPill(Pos alignment) {
        VBox pill = new VBox(0);
        pill.setAlignment(alignment);
        pill.setFillWidth(true);
        pill.setStyle(sidebarPillStyle());
        return pill;
    }

    private void styleBackButton(Button b, boolean h) {
        b.setStyle(h
            ? "-fx-background-color:" + (isDark() ? accentRgba(0.22) : "rgba(15,23,42,0.12)") + ";-fx-background-radius:12px;-fx-text-fill:" + (isDark() ? "#e5e7eb" : "#111827") + ";-fx-cursor:hand;"
            : "-fx-background-color:transparent;-fx-background-radius:12px;-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.5)" : "rgba(15,23,42,0.55)") + ";-fx-cursor:hand;"
        );
    }

    private void styleSignOutButton(Button b, boolean h) {
        b.setStyle(h
            ? "-fx-background-color:#800020;-fx-background-radius:12px;-fx-text-fill:#ffffff;-fx-cursor:hand;"
            : "-fx-background-color:rgba(128,0,32,0.85);-fx-background-radius:12px;-fx-text-fill:#ffe4ea;-fx-cursor:hand;"
        );
    }

    private Region pillSep() {
        Region r = new Region(); r.setPrefHeight(1); r.setMaxHeight(1);
        r.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.10)") + ";");
        VBox.setMargin(r, new Insets(6, 0, 6, 0));
        return r;
    }

    private Button sidebarItem(String icon, String label, String section) {
        HBox inner = new HBox(10);
        inner.setAlignment(sidebarExpanded ? Pos.CENTER_LEFT : Pos.CENTER);
        inner.setMouseTransparent(true);

        // Icon box: 36Ã—36 rounded square matching .admin-sidebar-icon sizing
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

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // MAIN AREA
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private VBox createMainArea() {
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

        area.getChildren().addAll(createAdminHeader(), scroll);
        return area;
    }

    private HBox createAdminHeader() {
        HBox h = new HBox(14);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(12, 24, 12, 24));
        h.setMinHeight(58); h.setMaxHeight(58);
        h.setStyle("-fx-background-color:" + (isDark() ? "rgba(0,0,0,0.98)" : "rgba(255,255,255,0.98)") + ";-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.14)" : "rgba(15,23,42,0.12)") + ";-fx-border-width:0 0 1 0;");

        HBox searchBar = new HBox(8);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(7, 14, 7, 14));
        searchBar.setPrefWidth(280);
        searchBar.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.04)") + ";-fx-background-radius:10px;-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.14)") + ";-fx-border-width:1;-fx-border-radius:10px;");
        Text sch = new Text(" Search admin..."); sch.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13)); sch.setFill(isDark() ? Color.web("rgba(255,255,255,0.60)") : Color.web("rgba(15,23,42,0.62)"));
        searchBar.getChildren().add(sch);

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);

        StackPane bell = buildNotificationTrigger();

        // User pill
        HBox up = new HBox(8); up.setAlignment(Pos.CENTER); up.setPadding(new Insets(5,12,5,6));
        up.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.05)") + ";-fx-background-radius:20px;-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.12)") + ";-fx-border-width:1;-fx-border-radius:20px;-fx-cursor:hand;");
        Circle av = new Circle(14);
        boolean hasAvatarImage = applyAvatarFill(av);
        Text ini = t(currentInitial(), boldFont(), FontWeight.BOLD, 12); ini.setFill(Color.WHITE);
        ini.setVisible(!hasAvatarImage);
        ini.setManaged(!hasAvatarImage);
        StackPane avStack = new StackPane(av, ini);
        VBox ui = new VBox(1);
        Text nm = t(currentDisplayName(), boldFont(), FontWeight.BOLD, 12); nm.setFill(isDark() ? Color.WHITE : Color.web("#111827"));
        Text rl = t(currentRoleLabel(), lightFont(), FontWeight.NORMAL, 12); rl.setFill(isDark() ? Color.web("rgba(255,255,255,0.4)") : Color.web("rgba(15,23,42,0.52)"));
        ui.getChildren().addAll(nm, rl);
        Text chevron = t("v", boldFont(), FontWeight.BOLD, 13); chevron.setFill(isDark() ? Color.web("rgba(255,255,255,0.65)") : Color.web("rgba(15,23,42,0.65)"));
        up.getChildren().addAll(avStack, ui);
        up.getChildren().add(chevron);

        attachProfileDropdown(up);

        h.getChildren().addAll(searchBar, sp, bell, up);
        return h;
    }

    private StackPane buildNotificationTrigger() {
        StackPane bell = new StackPane();
        bell.setPrefSize(38, 38);
        bell.setMaxSize(38, 38);
        bell.setStyle(
            "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.05)") + ";" +
            "-fx-background-radius:19;" +
            "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.10)" : "rgba(15,23,42,0.12)") + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:19;" +
            "-fx-cursor:hand;"
        );

        Text bellIc = t("\uD83D\uDD14", boldFont(), FontWeight.BOLD, 13);
        bellIc.setFill(isDark() ? Color.web("rgba(255,255,255,0.88)") : Color.web("rgba(15,23,42,0.84)"));

        Circle badgeDot = new Circle(4.5, Color.web("#ff3b30"));
        StackPane.setAlignment(badgeDot, Pos.TOP_RIGHT);
        StackPane.setMargin(badgeDot, new Insets(6, 6, 0, 0));

        VBox dropdownCard = buildNotificationDropdownCard();

        Runnable showPopup = () -> {
            cancelNotificationCloseDelay();
            if (profilePopup != null && profilePopup.isShowing()) {
                profilePopup.hide();
            }
            showNotificationPopupInsideStage(bell, dropdownCard);
        };

        bell.setOnMouseEntered(e -> showPopup.run());
        bell.setOnMouseExited(e -> scheduleCloseNotificationPopup(bell, dropdownCard));

        dropdownCard.setOnMouseEntered(e -> cancelNotificationCloseDelay());
        dropdownCard.setOnMouseExited(e -> {
            if (!bell.isHover()) {
                scheduleCloseNotificationPopup(bell, dropdownCard);
            }
        });

        bell.setOnMouseClicked(e -> {
            if (notificationPopup != null && notificationPopup.isShowing()) {
                notificationPopup.hide();
            } else {
                showPopup.run();
            }
        });

        bell.getChildren().addAll(bellIc, badgeDot);
        return bell;
    }

    private VBox buildNotificationDropdownCard() {
        VBox box = new VBox();
        box.setPadding(new Insets(0));
        box.setSpacing(0);
        box.setPrefWidth(300);
        box.setMinWidth(300);
        box.setMaxWidth(300);
        box.setStyle(
            "-fx-background-color:" + (isDark() ? "rgba(12,12,18,0.94)" : "rgba(255,255,255,0.98)") + ";" +
            "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.12)") + ";" +
            "-fx-border-width:1;" +
            "-fx-background-radius:16px;" +
            "-fx-border-radius:16px;" +
            "-fx-effect:dropshadow(gaussian," + (isDark() ? "rgba(0,0,0,0.45)" : "rgba(15,23,42,0.14)") + ",30,0,0,10);"
        );

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 20, 14, 20));

        Text title = t("Notifications", boldFont(), FontWeight.BOLD, 14);
        title.setFill(isDark() ? Color.web("#f3f4f6") : Color.web("#111827"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("x");
        close.setPadding(new Insets(4, 10, 4, 10));
        close.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        close.setStyle(
            "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.06)") + ";" +
            "-fx-background-radius:999;" +
            "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.14)") + ";" +
            "-fx-border-width:1;" +
            "-fx-border-radius:999;" +
            "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.88)" : "rgba(15,23,42,0.84)") + ";" +
            "-fx-cursor:hand;"
        );
        close.setOnAction(e -> {
            cancelNotificationCloseDelay();
            if (notificationPopup != null) {
                notificationPopup.hide();
            }
        });

        VBox rows = new VBox();
        rows.setPadding(new Insets(8));
        rows.setSpacing(6);
        rows.getChildren().addAll(
            notificationRow("Syndicati", "Your community dashboard is synced", "now"),
            notificationRow("Forum", "A new reply landed in your discussion", "5 min"),
            notificationRow("Residence", "A maintenance update is ready", "1 h")
        );

        header.getChildren().addAll(title, spacer, close);
        box.getChildren().addAll(header, rows);
        return box;
    }

    private VBox notificationRow(String title, String body, String time) {
        VBox row = new VBox();
        row.setPadding(new Insets(10, 12, 10, 12));
        row.setSpacing(2);

        Text tTitle = t(title, boldFont(), FontWeight.BOLD, 12);
        tTitle.setFill(isDark() ? Color.web("#f3f4f6") : Color.web("#111827"));
        Text tBody = t(body, lightFont(), FontWeight.NORMAL, 12);
        tBody.setFill(isDark() ? Color.web("#d4d4d8") : Color.web("#334155"));
        Text tTime = t(time, lightFont(), FontWeight.NORMAL, 11);
        tTime.setFill(isDark() ? Color.web("#a1a1aa") : Color.web("#64748b"));

        row.getChildren().addAll(tTitle, tBody, tTime);
        row.setStyle("-fx-background-color:transparent;-fx-background-radius:14px;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.06)") + ";-fx-background-radius:14px;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color:transparent;-fx-background-radius:14px;"));
        return row;
    }

    private void scheduleCloseNotificationPopup(StackPane anchor, VBox card) {
        cancelNotificationCloseDelay();
        notificationHideDelay = new PauseTransition(Duration.millis(180));
        notificationHideDelay.setOnFinished(e -> {
            if (notificationPopup != null && notificationPopup.isShowing() && !anchor.isHover() && !card.isHover()) {
                notificationPopup.hide();
            }
        });
        notificationHideDelay.playFromStart();
    }

    private void cancelNotificationCloseDelay() {
        if (notificationHideDelay != null) {
            notificationHideDelay.stop();
        }
    }

    private void showNotificationPopupInsideStage(StackPane bell, VBox card) {
        if (notificationPopup == null) {
            notificationPopup = new Popup();
            notificationPopup.setAutoHide(true);
            notificationPopup.setHideOnEscape(true);
            notificationPopup.setAutoFix(false);
            notificationPopup.getContent().setAll(card);
        }

        var bellBounds = bell.localToScreen(bell.getBoundsInLocal());
        if (bellBounds == null) return;

        Window window = bell.getScene() != null ? bell.getScene().getWindow() : null;
        if (window == null) return;

        double width = 300;
        double margin = 8;
        double desiredX = bellBounds.getMaxX() - width;
        double desiredY = bellBounds.getMaxY() + 12;

        if (!notificationPopup.isShowing()) {
            notificationPopup.show(bell, desiredX, desiredY);
        }

        double popupW = notificationPopup.getWidth() > 0 ? notificationPopup.getWidth() : width;
        double popupH = notificationPopup.getHeight() > 0 ? notificationPopup.getHeight() : Math.max(220, card.prefHeight(width));

        double minX = window.getX() + margin;
        double maxX = window.getX() + window.getWidth() - popupW - margin;
        double clampedX = Math.max(minX, Math.min(desiredX, maxX));

        double minY = window.getY() + margin;
        double maxY = window.getY() + window.getHeight() - popupH - margin;

        double y = desiredY;
        if (y > maxY) {
            y = bellBounds.getMinY() - popupH - 10;
        }
        double clampedY = Math.max(minY, Math.min(y, maxY));

        notificationPopup.setX(clampedX);
        notificationPopup.setY(clampedY);
    }

    private void attachProfileDropdown(HBox profilePill) {
        VBox card = new VBox(8);
        card.setPrefWidth(280);
        card.setPadding(new Insets(14, 12, 10, 12));
        card.setStyle(
            "-fx-background-color:" + (isDark() ? "rgba(12,12,18,0.90)" : "rgba(255,255,255,0.98)") + ";" +
            "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.12)") + ";" +
            "-fx-border-width:1;" +
            "-fx-background-radius:16px;" +
            "-fx-border-radius:16px;" +
            "-fx-effect:dropshadow(gaussian," + (isDark() ? "rgba(0,0,0,0.45)" : "rgba(15,23,42,0.14)") + ",30,0,0,10);"
        );

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 2, 8, 2));

        Circle av = new Circle(20);
        boolean hasAvatarImage = applyAvatarFill(av);
        Text init = t(currentInitial(), boldFont(), FontWeight.BOLD, 16); init.setFill(Color.WHITE);
        init.setVisible(!hasAvatarImage);
        init.setManaged(!hasAvatarImage);
        StackPane avatar = new StackPane(av, init);

        VBox info = new VBox(2);
        Text name = t(currentDisplayName(), boldFont(), FontWeight.BOLD, 15); name.setFill(isDark() ? Color.WHITE : Color.web("#111827"));
        Text mail = t(currentEmail(), lightFont(), FontWeight.NORMAL, 13); mail.setFill(isDark() ? Color.web("rgba(255,255,255,0.55)") : Color.web("rgba(15,23,42,0.55)"));
        info.getChildren().addAll(name, mail);
        header.getChildren().addAll(avatar, info);

        Region sep1 = new Region();
        sep1.setPrefHeight(1);
        sep1.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.1)" : "rgba(15,23,42,0.10)") + ";");

        VBox items = new VBox(4);
        items.getChildren().addAll(
            dropdownItem("\uD83D\uDC64", "My Profile", () -> openFrontendPage("profile")),
            dropdownItem("\u2302", "Main Home", () -> { if (exitCallback != null) exitCallback.run(); }),
            dropdownItem("\u2699", "Settings", () -> openFrontendPage("settings")),
            dropdownItemWithBadge("\uD83D\uDCB3", "Billing", "4", () -> openFrontendPage("profile"))
        );

        Region sep2 = new Region();
        sep2.setPrefHeight(1);
        sep2.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.1)" : "rgba(15,23,42,0.10)") + ";");

        Button logout = dropdownItem("\u23FB", "Log Out", () -> com.syndicati.MainApplication.getInstance().logout());
        logout.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-background-radius:8px;" +
            "-fx-text-fill:#ef4444;" +
            "-fx-padding:9 10 9 10;" +
            "-fx-alignment:CENTER_LEFT;" +
            "-fx-cursor:hand;"
        );
        logout.setOnMouseEntered(e -> logout.setStyle(
            "-fx-background-color:rgba(239,68,68,0.12);" +
            "-fx-background-radius:8px;" +
            "-fx-text-fill:#ef4444;" +
            "-fx-padding:9 10 9 10;" +
            "-fx-alignment:CENTER_LEFT;" +
            "-fx-cursor:hand;"
        ));
        logout.setOnMouseExited(e -> logout.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-background-radius:8px;" +
            "-fx-text-fill:#ef4444;" +
            "-fx-padding:9 10 9 10;" +
            "-fx-alignment:CENTER_LEFT;" +
            "-fx-cursor:hand;"
        ));

        card.getChildren().addAll(header, sep1, items, sep2, logout);

        profilePopup = new Popup();
        profilePopup.setAutoHide(true);
        profilePopup.setHideOnEscape(true);
        profilePopup.setAutoFix(false);
        profilePopup.getContent().setAll(card);

        PauseTransition hideDelay = new PauseTransition(Duration.millis(170));
        hideDelay.setOnFinished(e -> {
            if (profilePopup != null && profilePopup.isShowing() && !profilePill.isHover() && !card.isHover()) {
                profilePopup.hide();
            }
        });

        Runnable showPopup = () -> {
            hideDelay.stop();
            if (notificationPopup != null && notificationPopup.isShowing()) {
                notificationPopup.hide();
            }
            showProfilePopupInsideStage(profilePill, card);
        };

        profilePill.setOnMouseEntered(e -> showPopup.run());
        profilePill.setOnMouseExited(e -> hideDelay.playFromStart());

        card.setOnMouseEntered(e -> hideDelay.stop());
        card.setOnMouseExited(e -> {
            if (!profilePill.isHover()) {
                hideDelay.playFromStart();
            }
        });

        // Keep click toggle as a fallback interaction.
        profilePill.setOnMouseClicked(e -> {
            if (profilePopup != null && profilePopup.isShowing()) {
                profilePopup.hide();
            } else {
                showPopup.run();
            }
        });
    }

    private void showProfilePopupInsideStage(HBox profilePill, VBox card) {
        if (profilePopup == null) return;

        var pillBounds = profilePill.localToScreen(profilePill.getBoundsInLocal());
        if (pillBounds == null) return;

        Window window = profilePill.getScene() != null ? profilePill.getScene().getWindow() : null;
        if (window == null) return;

        double margin = 8;
        double desiredX = pillBounds.getMaxX() - 280;
        double desiredY = pillBounds.getMaxY() + 12;

        if (!profilePopup.isShowing()) {
            profilePopup.show(profilePill, desiredX, desiredY);
        }

        double popupW = profilePopup.getWidth() > 0 ? profilePopup.getWidth() : 280;
        double popupH = profilePopup.getHeight() > 0 ? profilePopup.getHeight() : Math.max(260, card.prefHeight(280));

        double minX = window.getX() + margin;
        double maxX = window.getX() + window.getWidth() - popupW - margin;
        double clampedX = Math.max(minX, Math.min(desiredX, maxX));

        double minY = window.getY() + margin;
        double maxY = window.getY() + window.getHeight() - popupH - margin;

        double y = desiredY;
        if (y > maxY) {
            y = pillBounds.getMinY() - popupH - 10;
        }
        double clampedY = Math.max(minY, Math.min(y, maxY));

        profilePopup.setX(clampedX);
        profilePopup.setY(clampedY);
    }

    private Button dropdownItem(String icon, String label, Runnable action) {
        Button b = new Button(icon + "   " + label);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        b.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-background-radius:8px;" +
            "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.9)" : "rgba(15,23,42,0.88)") + ";" +
            "-fx-padding:9 10 9 10;" +
            "-fx-alignment:CENTER_LEFT;" +
            "-fx-cursor:hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(
            "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.07)" : "rgba(15,23,42,0.07)") + ";" +
            "-fx-background-radius:8px;" +
            "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,1)" : "rgba(15,23,42,1)") + ";" +
            "-fx-padding:9 10 9 10;" +
            "-fx-alignment:CENTER_LEFT;" +
            "-fx-cursor:hand;"
        ));
        b.setOnMouseExited(e -> b.setStyle(
            "-fx-background-color:transparent;" +
            "-fx-background-radius:8px;" +
            "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.9)" : "rgba(15,23,42,0.88)") + ";" +
            "-fx-padding:9 10 9 10;" +
            "-fx-alignment:CENTER_LEFT;" +
            "-fx-cursor:hand;"
        ));
        b.setOnAction(e -> {
            if (profilePopup != null) profilePopup.hide();
            if (notificationPopup != null) notificationPopup.hide();
            action.run();
        });
        return b;
    }

    private Button dropdownItemWithBadge(String icon, String label, String badge, Runnable action) {
        Button b = dropdownItem(icon, label, action);
        b.setText(icon + "   " + label + "          " + badge);
        return b;
    }

    private void openFrontendPage(String page) {
        if (exitCallback != null) {
            exitCallback.run();
        }
        Platform.runLater(() -> NavigationManager.getInstance().navigateTo(page));
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // SECTION DISPATCHER
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private VBox buildSection(String section) {
        switch (section) {
            case "users":     return buildUsersSection();
            case "forum":     return buildForumSection();
            case "syndicat":  return buildSyndicatSection();
            case "residence": return buildResidenceSection();
            case "evenement": return buildEvenementSection();
            default:          return buildGeneralSection();
        }
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // GENERAL section
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private VBox buildGeneralSection() {
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

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // USERS section (Twig: switcher Users/Profile/Onboarding)
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private VBox buildUsersSection() {
        VBox s = moduleShell("Users", "\uD83D\uDC65");
        VBox body = new VBox(16);
        HBox sub = moduleModeSwitcher(new String[]{"Users", "Profile", "Onboarding"}, "Users", key -> {
            body.getChildren().setAll(
                "Profile".equals(key) ? usersProfilePane() :
                "Onboarding".equals(key) ? usersOnboardingPane() :
                usersTablePane()
            );
        });
        body.getChildren().add(usersTablePane());
        s.getChildren().addAll(sub, body);
        return s;
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // FORUM section (Twig: Publications/Commentaires/Reactions)
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private VBox buildForumSection() {
        VBox s = moduleShell("Forum", "\uD83D\uDCAC");
        VBox body = new VBox(16);
        HBox sub = moduleModeSwitcher(new String[]{"Publications", "Commentaires", "Reactions"}, "Publications", key -> {
            body.getChildren().setAll(
                "Commentaires".equals(key) ? forumCommentsPane() :
                "Reactions".equals(key) ? forumReactionsPane() :
                forumPublicationsPane()
            );
        });
        body.getChildren().add(forumPublicationsPane());
        s.getChildren().addAll(sub, body);
        return s;
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // SYNDICAT section (Twig: Reclamations/Responses)
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private VBox buildSyndicatSection() {
        VBox s = moduleShell("Syndicat", "\uD83C\uDFDB\uFE0F");
        VBox body = new VBox(16);
        HBox sub = moduleModeSwitcher(new String[]{"Reclamations", "Responses"}, "Reclamations", key -> {
            body.getChildren().setAll("Responses".equals(key) ? syndicatResponsesPane() : syndicatReclamationsPane());
        });
        body.getChildren().add(syndicatReclamationsPane());
        s.getChildren().addAll(sub, body);
        return s;
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // RESIDENCE section (Twig: Residences/Appartements/Maintenance)
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private VBox buildResidenceSection() {
        VBox s = moduleShell("Residence", "\uD83C\uDFE2");
        VBox body = new VBox(16);
        HBox sub = moduleModeSwitcher(new String[]{"Residences", "Appartements", "Maintenance"}, "Residences", key -> {
            body.getChildren().setAll(
                "Appartements".equals(key) ? residenceApartmentsPane() :
                "Maintenance".equals(key) ? residenceMaintenancePane() :
                residenceTablePane()
            );
        });
        body.getChildren().add(residenceTablePane());
        s.getChildren().addAll(sub, body);
        return s;
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // EVENEMENT section (Twig: Evenements/Participations)
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private VBox buildEvenementSection() {
        VBox s = moduleShell("Evenement", "\uD83C\uDF89");
        VBox body = new VBox(16);
        HBox sub = moduleModeSwitcher(new String[]{"Evenements", "Participations"}, "Evenements", key -> {
            body.getChildren().setAll("Participations".equals(key) ? eventParticipationsPane() : eventTablePane());
        });
        body.getChildren().add(eventTablePane());
        s.getChildren().addAll(sub, body);
        return s;
    }

    private VBox usersTablePane() {
        VBox wrap = new VBox(16);
        List<User> users = userController.users();

        int registered = users.size();
        int verified = 0;
        int pending = 0;
        int active7Day = 0;
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        for (User user : users) {
            if (user.isVerified()) {
                verified++;
            } else {
                pending++;
            }

            LocalDateTime updatedAt = user.getUpdatedAt();
            if (updatedAt != null && updatedAt.isAfter(sevenDaysAgo)) {
                active7Day++;
            }
        }

        HBox stats = new HBox(16); stats.setFillHeight(true);
        addStatCards(stats,
            new String[]{"USR", "VER", "PEN", "ACT"},
            new String[]{"Registered", "Verified", "Pending", "7-Day Active"},
            new String[]{String.valueOf(registered), String.valueOf(verified), String.valueOf(pending), String.valueOf(active7Day)},
            new String[]{"#60a5fa", "#34d399", "#fbbf24", "#a78bfa"}
        );

        String[][] userRows;
        if (users.isEmpty()) {
            userRows = new String[][]{{"No users found", "-", "-", "-", "-"}};
        } else {
            userRows = new String[users.size()][5];
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                String fullName = ((u.getFirstName() == null ? "" : u.getFirstName()) + " " + (u.getLastName() == null ? "" : u.getLastName())).trim();
                if (fullName.isEmpty()) {
                    fullName = "User #" + (u.getIdUser() == null ? "-" : u.getIdUser());
                }
                String status = u.isDisabled() ? "Disabled" : (u.isVerified() ? "Active" : "Pending");

                userRows[i][0] = fullName;
                userRows[i][1] = safe(u.getEmailUser());
                userRows[i][2] = safe(u.getRoleUser());
                userRows[i][3] = u.isVerified() ? "Yes" : "No";
                userRows[i][4] = status;
            }
        }

        wrap.getChildren().addAll(stats, dataTableWithCrud("Users Table", "User",
            new String[]{"Name", "Email", "Role", "Verified", "Status"},
            userRows, true, true
        ));
        return wrap;
    }

    private VBox usersProfilePane() {
        VBox wrap = new VBox(16);
        List<Profile> profiles = userController.profiles();
        
        // Statistics cards
        HBox stats = new HBox(16);
        stats.setFillHeight(true);
        int totalProfiles = profiles.size();
        int withAvatar = (int) profiles.stream().filter(p -> p.getAvatar() != null && !p.getAvatar().isEmpty()).count();
        int withDescription = (int) profiles.stream().filter(p -> p.getDescriptionProfile() != null && !p.getDescriptionProfile().isEmpty()).count();
        
        addStatCards(stats,
            new String[]{"PFL", "AVT", "BIO", "LOC"},
            new String[]{"Total Profiles", "With Avatar", "With Bio", "Locales Set"},
            new String[]{String.valueOf(totalProfiles), String.valueOf(withAvatar), String.valueOf(withDescription), "—"},
            new String[]{"#60a5fa", "#34d399", "#a78bfa", "#fbbf24"}
        );
        
        String[][] profileRows;
        if (profiles.isEmpty()) {
            profileRows = new String[][]{{"-", "-", "No profile data", "-", "-", "-"}};
        } else {
            profileRows = new String[profiles.size()][6];
            for (int i = 0; i < profiles.size(); i++) {
                Profile p = profiles.get(i);
                profileRows[i][0] = String.valueOf(p.getIdProfile());
                profileRows[i][1] = String.valueOf(p.getUserId());
                profileRows[i][2] = safe(p.getLocale());
                profileRows[i][3] = p.getTheme() == null ? "-" : String.valueOf(p.getTheme());
                profileRows[i][4] = p.getTimezone() == null ? "-" : String.valueOf(p.getTimezone());
                profileRows[i][5] = safe(p.getAvatar());
            }
        }

        wrap.getChildren().addAll(stats, dataTableWithCrud("User Profile Data", "Profile",
            new String[]{"Profile ID", "User ID", "Locale", "Theme", "Timezone", "Avatar"},
            profileRows, false, true
        ));
        return wrap;
    }

    private VBox usersOnboardingPane() {
        VBox wrap = new VBox(14);
        List<Onboarding> onboardings = userController.onboardings();
        String[][] onboardingRows;

        if (onboardings.isEmpty()) {
            onboardingRows = new String[][]{{"-", "-", "-", "-", "No onboarding data", "-", "-"}};
        } else {
            onboardingRows = new String[onboardings.size()][7];
            for (int i = 0; i < onboardings.size(); i++) {
                Onboarding o = onboardings.get(i);
                onboardingRows[i][0] = String.valueOf(o.getIdOnboarding());
                onboardingRows[i][1] = String.valueOf(o.getUserId());
                onboardingRows[i][2] = String.valueOf(o.getStep());
                onboardingRows[i][3] = o.isCompleted() ? "Yes" : "No";
                onboardingRows[i][4] = safe(o.getSelectedLocale());
                onboardingRows[i][5] = safe(o.getSelectedTheme());
                onboardingRows[i][6] = formatDateTime(o.getUpdatedAt());
            }
        }

        wrap.getChildren().add(dataTableWithCrud("Onboarding Data", "Onboarding",
            new String[]{"Onboarding ID", "User ID", "Step", "Completed", "Locale", "Theme", "Updated"},
            onboardingRows, false, true
        ));
        return wrap;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DASHBOARD_DATE_TIME_FMT);
    }

    private VBox forumPublicationsPane() {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(dataTableWithCrud("Forum Publications", "Publication",
            new String[]{"Title", "Author", "Category", "Comments", "Date"},
            new String[][]{
                {"Reunion copropriete - Batiment A", "Ahmed B.", "Announcements", "12", "Mar 11"},
                {"Question sur les charges communes", "Leila M.", "General", "7", "Mar 10"},
                {"Electricite cage d'escalier", "Karim S.", "Issues", "4", "Mar 9"},
                {"Nouveau reglement interieur", "Sara A.", "Announcements", "18", "Mar 8"}
            }, true, true
        ));
        return wrap;
    }

    private VBox forumCommentsPane() {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(dataTableWithCrud("Forum Comments", "Comment",
            new String[]{"Publication", "Author", "Snippet", "Likes", "Date"},
            new String[][]{
                {"Reunion copropriete", "Leila M.", "Merci pour le compte rendu.", "6", "Mar 11"},
                {"Charges communes", "Omar Z.", "Peut-on avoir le detail ?", "2", "Mar 10"},
                {"Reglement", "Karim S.", "C'est valide pour mon bloc.", "3", "Mar 9"}
            }, false, true
        ));
        return wrap;
    }

    private VBox forumReactionsPane() {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(dataTableWithCrud("Forum Reactions", "Reaction",
            new String[]{"User", "Target", "Type", "Count", "Updated"},
            new String[][]{
                {"Ahmed B.", "Publication #88", "Like", "24", "Mar 11"},
                {"Sara A.", "Comment #143", "Love", "9", "Mar 10"},
                {"Leila M.", "Publication #86", "Like", "17", "Mar 9"}
            }, false, true
        ));
        return wrap;
    }

    private VBox syndicatReclamationsPane() {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(dataTableWithCrud("Reclamations", "Reclamation",
            new String[]{"Reference", "Resident", "Type", "Status", "Date"},
            new String[][]{
                {"REC-2024-0156", "Ahmed B.", "Maintenance", "Pending", "Mar 11"},
                {"REC-2024-0155", "Leila M.", "Noise", "Resolved", "Mar 10"},
                {"REC-2024-0154", "Karim S.", "Elevator", "Urgent", "Mar 9"}
            }, false, true
        ));
        return wrap;
    }

    private VBox syndicatResponsesPane() {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(dataTableWithCrud("Responses", "Response",
            new String[]{"Reclamation", "Agent", "Response", "Sent", "State"},
            new String[][]{
                {"REC-2024-0155", "Syndic Team", "Inspection completed", "Mar 10", "Delivered"},
                {"REC-2024-0152", "Syndic Team", "Technician scheduled", "Mar 8", "Delivered"},
                {"REC-2024-0149", "Support", "Need more details", "Mar 7", "Awaiting Reply"}
            }, false, false
        ));
        return wrap;
    }

    private VBox residenceTablePane() {
        VBox wrap = new VBox(14);
        Object data;
        wrap.getChildren().add(dataTableWithCrud("Residences", "Residence",
                new String[]{"Residence", "Address", "N of Appartments", "Date Built", "Blocks"},
                ListeResidences.stream()
                        .map(r -> new String[]{
                                r.getNom_r(),
                                r.getAdresse(),
                                String.valueOf(r.getN_appartements()),
                                r.getDate_ajout(),
                                String.valueOf(r.getN_blocs())
                        })
                        .toArray(String[][]::new), true, true
        ));
        return wrap;
    }

    private VBox residenceApartmentsPane() {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(dataTableWithCrud("Apartments", "Apartments",
                new String[]{"Residence", "Type", "Rent Price", "Status", "Parking Available?"},
                ListeAppartements.stream()
                        .map(a -> new String[]{
                                ServiceResidence.TrouverResidenceParId(a.getResidence_id()).getNom_r() != null
                                        ? ServiceResidence.TrouverResidenceParId(a.getResidence_id()).getNom_r()
                                        : "Unavailable",
                                a.getType_a(),
                                String.valueOf(a.getPrix_location()),
                                String.valueOf(a.getDisponible()).equals("1") ? "Available" : "Rented",
                                String.valueOf(a.getParking()).equals("1") ? "Available" : "Unavailable"
                        })
                        .toArray(String[][]::new), true, true
        ));
        return wrap;
    }

    private VBox residenceMaintenancePane() {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(dataTableWithCrud("Maintenance", "Maintenance Ticket",
                new String[]{"Maintenance Date", "Apartment Condition", "Plumbing", "Electricity", "Heating", "Description", "AI Recommendation"},
                ListeMaintenances.stream()
                        .map(m -> new String[]{
                                m.getDate_derniere_maintenance(),
                                m.getEtat_app(),
                                String.valueOf(m.getEtat_plomberie()),
                                String.valueOf(m.getEtat_electricite()),
                                String.valueOf(m.getEtat_chauffage()),
                                m.getdescription_maint(),
                                m.getRecommendation_ia()
                        })
                        .toArray(String[][]::new), true, true
        ));
        return wrap;
    }

    private VBox eventTablePane() {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(dataTableWithCrud("Evenements", "Event",
            new String[]{"Title", "Type", "Date", "Enrolled", "Status"},
            new String[][]{
                {"Assemblee Generale 2024", "Meeting", "Mar 20", "47", "Open"},
                {"Fete de quartier", "Social", "Apr 5", "120", "Open"},
                {"Formation securite incendie", "Training", "Apr 12", "30", "Limited"}
            }, true, true
        ));
        return wrap;
    }

    private VBox eventParticipationsPane() {
        VBox wrap = new VBox(14);
        wrap.getChildren().add(dataTableWithCrud("Participations", "Participation",
            new String[]{"Event", "Resident", "Seat", "Checked-in", "Date"},
            new String[][]{
                {"Assemblee Generale 2024", "Ahmed B.", "A12", "No", "Mar 20"},
                {"Fete de quartier", "Leila M.", "B03", "No", "Apr 5"},
                {"Formation securite incendie", "Omar Z.", "C08", "No", "Apr 12"}
            }, false, true
        ));
        return wrap;
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // SHARED HELPERS
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private VBox moduleShell(String name, String icon) {
        VBox s = new VBox(20); s.setFillWidth(true); s.setPadding(new Insets(24));
        HBox heading = new HBox(10); heading.setAlignment(Pos.CENTER_LEFT);
        Text ic = new Text(icon); ic.setFont(Font.font(20));
        Text nm = t(name+" Overview", boldFont(), FontWeight.BOLD, 21); nm.setFill(textPrimaryColor());
        heading.getChildren().addAll(ic, nm);
        s.getChildren().add(heading);
        return s;
    }

    private void addStatCards(HBox row, String[] icons, String[] labels, String[] values, String[] colors) {
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

    private VBox dataTableWithCrud(String title, String entityLabel, String[] cols, String[][] rows, boolean allowAdd, boolean allowEdit) {
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

        HBox head = new HBox(10);
        head.setAlignment(Pos.CENTER_LEFT);
        Text tt = t(title, boldFont(), FontWeight.BOLD, 18); tt.setFill(textPrimaryColor());
        Text sub = t("Live module data", lightFont(), FontWeight.NORMAL, 13);
        sub.setFill(textMutedColor());
        VBox titleWrap = new VBox(2, tt, sub);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        head.getChildren().addAll(titleWrap, spacer);

        if (allowAdd) {
            Button addBtn = pillAction(spec.addButtonLabel, true);
            addBtn.setOnAction(e -> switchToModalFace(faceContainer, spec, entityLabel, "add", cols, null));
            head.getChildren().add(addBtn);
        }

        GridPane tbl = new GridPane();
        tbl.setHgap(0);
        tbl.setVgap(0);
        tbl.setMaxWidth(Double.MAX_VALUE);

        int displayCols = cols.length + 1;
        for (int c = 0; c < displayCols; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            tbl.getColumnConstraints().add(cc);
        }

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

        for (int r = 0; r < rows.length; r++) {
            String bg = (r % 2 == 0) ? "transparent" : "rgba(255,255,255,0.01)";
            for (int c = 0; c < rows[r].length; c++) {
                Text tx = t(rows[r][c], lightFont(), FontWeight.NORMAL, 14);
                tx.setFill(c == 0 ? textSecondaryColor() : textMutedColor());
                HBox cb = new HBox(tx);
                cb.setPadding(new Insets(10,12,10,12));
                cb.setStyle("-fx-background-color:" + bg + ";");
                cb.setOnMouseEntered(e -> cb.setStyle("-fx-background-color:" + accentRgba(0.07) + ";"));
                cb.setOnMouseExited(e -> cb.setStyle("-fx-background-color:" + bg + ";"));
                tbl.add(cb, c, r + 1);
            }

            HBox rowActions = new HBox(6);
            rowActions.setAlignment(Pos.CENTER_LEFT);
            rowActions.setPadding(new Insets(8, 12, 8, 12));
            rowActions.setStyle("-fx-background-color:" + bg + ";");

            String[] rowData = rows[r];
            Button viewBtn = pillAction("View", false);
            viewBtn.setOnAction(e -> switchToModalFace(faceContainer, spec, entityLabel, "view", cols, rowData));
            rowActions.getChildren().add(viewBtn);

            tbl.add(rowActions, cols.length, r + 1);
        }

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
        tableCard.getChildren().add(card);
        
        StackPane.setAlignment(tableCard, Pos.TOP_CENTER);
        StackPane.setAlignment(modalFace, Pos.TOP_CENTER);
        faceContainer.getChildren().addAll(tableCard, modalFace);
        faceContainer.setUserData(modalFace);
        
        VBox wrap = new VBox(faceContainer);
        wrap.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(faceContainer, Priority.ALWAYS);


        Button testBtn = pillAction("testing add button", false);
        testBtn.setOnAction(e -> {
            Stage stage = (Stage) faceContainer.getScene().getWindow();
            Scene previousScene = faceContainer.getScene();
            new AddResidence(stage, previousScene).show();
        });
        head.getChildren().add(testBtn);


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

    private Button pillAction(String text, boolean primary) {
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
                    if (handleDeleteAction(entityLabel, rowData)) {
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
                if (handleSaveAction(entityLabel, mode, cols, rowData, fields)) {
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

    private boolean handleSaveAction(String entityLabel, String mode, String[] cols, String[] originalRowData, VBox fields) {
        if ("User".equalsIgnoreCase(entityLabel)) {
            return handleUserSave(mode, originalRowData, fields);
        } else if ("Profile".equalsIgnoreCase(entityLabel)) {
            return handleProfileSave(mode, originalRowData, fields);
        }
        return false;
    }

    private boolean handleUserSave(String mode, String[] originalRowData, VBox fields) {
        Map<String, String> values = readEditableFieldValues(fields);
        String name = safe(values.get("Name"));
        String email = safe(values.get("Email"));
        String role = safe(values.get("Role")).toUpperCase();
        String verifiedText = safe(values.get("Verified"));
        String statusText = safe(values.get("Status"));

        if ("-".equals(name) || "-".equals(email) || "-".equals(role)) {
            return false;
        }

        String[] splitName = splitName(name);
        boolean verified = isTruthy(verifiedText);
        boolean disabled = "DISABLED".equalsIgnoreCase(statusText);

        if ("add".equals(mode)) {
            User user = new User();
            user.setFirstName(splitName[0]);
            user.setLastName(splitName[1]);
            user.setEmailUser(email);
            user.setRoleUser(role);
            user.setVerified(verified);
            user.setDisabled(disabled);
            user.setPasswordUser("ChangeMe#2026");
            return userController.userAdd(user) > 0;
        }

        if ("edit".equals(mode)) {
            String oldEmail = (originalRowData != null && originalRowData.length > 1) ? originalRowData[1] : email;
            Optional<User> existingOpt = userController.userByEmail(oldEmail);
            if (existingOpt.isEmpty()) {
                existingOpt = userController.userByEmail(email);
            }
            if (existingOpt.isEmpty()) {
                return false;
            }

            User existing = existingOpt.get();
            existing.setFirstName(splitName[0]);
            existing.setLastName(splitName[1]);
            existing.setEmailUser(email);
            existing.setRoleUser(role);
            existing.setVerified(verified);
            existing.setDisabled(disabled);
            if (existing.getPasswordUser() == null || existing.getPasswordUser().isBlank()) {
                existing.setPasswordUser("ChangeMe#2026");
            }
            return userController.userEdit(existing);
        }

        return false;
    }

    private boolean handleProfileSave(String mode, String[] originalRowData, VBox fields) {
        Map<String, String> values = readEditableFieldValues(fields);
        
        if ("add".equals(mode)) {
            // For adding profile, we need at least a User ID
            String userIdStr = safe(values.get("User ID"));
            if ("-".equals(userIdStr)) {
                return false;
            }
            try {
                int userId = Integer.parseInt(userIdStr);
                Profile profile = new Profile();
                profile.setUserId(userId);
                profile.setLocale(safe(values.get("Locale")));
                String themeStr = safe(values.get("Theme"));
                profile.setTheme("-".equals(themeStr) ? null : Integer.parseInt(themeStr));
                String tzStr = safe(values.get("Timezone"));
                profile.setTimezone("-".equals(tzStr) ? null : Integer.parseInt(tzStr));
                profile.setAvatar(null); // Avatar upload handled separately
                profile.setDescriptionProfile(safe(values.get("Bio")));
                
                Optional<Integer> createdIdOpt = userController.profileCreate(profile);
                return createdIdOpt.isPresent();
            } catch (NumberFormatException e) {
                return false;
            }
        } else if ("edit".equals(mode)) {
            String profileIdStr = (originalRowData != null && originalRowData.length > 0) ? originalRowData[0] : "-";
            if ("-".equals(profileIdStr)) {
                return false;
            }
            try {
                int profileId = Integer.parseInt(profileIdStr);
                Optional<Profile> existingOpt = userController.profileById(profileId);
                if (existingOpt.isEmpty()) {
                    return false;
                }
                
                Profile existing = existingOpt.get();
                existing.setLocale(safe(values.get("Locale")));
                String themeStr = safe(values.get("Theme"));
                existing.setTheme("-".equals(themeStr) ? null : Integer.parseInt(themeStr));
                String tzStr = safe(values.get("Timezone"));
                existing.setTimezone("-".equals(tzStr) ? null : Integer.parseInt(tzStr));
                existing.setDescriptionProfile(safe(values.get("Bio")));
                // Avatar update handled separately
                
                return userController.profileUpdate(existing);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        return false;
    }

    private String VerifierResidence(String nom)
    {
        String erreur="";
        if (nom.equals(""))
            erreur= "Le nom ne peut pas être vide";

        return erreur;

    }
    private boolean AjouterResidence(String mode, String[] originalRowData, VBox fields) {
        Map<String, String> values = readEditableFieldValues(fields);
        String nom = safe(values.get("Nom"));
        String adresse = safe(values.get("Adresse"));
        String nAppartements = safe(values.get("N Appartements"));
        String dateAjout = safe(values.get("Date Ajout"));
        String nBlocs = safe(values.get("N Blocs"));
        System.out.println("nom : " +nom);
        System.out.println("Adresse "+adresse);
        System.out.println("N appart"+ nAppartements);
        System.out.println("Date: "+dateAjout);

        if ("add".equals(mode)) {
            Residence residence = new Residence();
            residence.setNom_r(nom);
            residence.setAdresse(adresse);
            residence.setImage_r(null);
            residence.setDate_ajout(dateAjout);
            residence.setN_appartements(Integer.parseInt(nAppartements));
            residence.setN_etages(2);
            residence.setN_blocs(nBlocs);

            try {
                ServiceResidence.Ajouter(residence);
                System.out.println("residence added successfully");
                return true;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        /*
        if ("edit".equals(mode)) {
            String oldNom = (originalRowData != null && originalRowData.length > 0) ? originalRowData[0] : nom;
            Optional<Residence> existingOpt = ServiceResidence.residenceByNom(oldNom);
            if (existingOpt.isEmpty()) {
                existingOpt = ServiceResidence.residenceByNom(nom);
            }
            if (existingOpt.isEmpty()) {
                return false;
            }

            Residence existing = existingOpt.get();
            existing.setNom_r(nom);
            existing.setAdresse(adresse);
            existing.setN_appartements(Integer.parseInt(nAppartements));
            existing.setDate_ajout(dateAjout);
            existing.setN_blocs(nBlocs);

            try {
                return ServiceResidence.Modifier(existing);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        */
        return false;
    }

    private boolean handleDeleteAction(String entityLabel, String[] rowData) {
        if ("User".equalsIgnoreCase(entityLabel)) {
            return handleUserDelete(rowData);
        } else if ("Profile".equalsIgnoreCase(entityLabel)) {
            return handleProfileDelete(rowData);
        }
        return false;
    }

    private boolean handleUserDelete(String[] rowData) {
        if (rowData == null || rowData.length < 2) {
            return false;
        }

        String email = rowData[1];
        Optional<User> existing = userController.userByEmail(email);
        return existing.filter(user -> user.getIdUser() != null)
            .map(user -> userController.userDelete(user.getIdUser()))
            .orElse(false);
    }

    private boolean handleProfileDelete(String[] rowData) {
        if (rowData == null || rowData.length < 1) {
            return false;
        }

        String profileIdStr = rowData[0];
        try {
            int profileId = Integer.parseInt(profileIdStr);
            return userController.profileDelete(profileId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Map<String, String> readEditableFieldValues(VBox fields) {
        Map<String, String> values = new LinkedHashMap<>();
        for (Node node : fields.getChildren()) {
            if (!(node instanceof VBox)) {
                continue;
            }
            VBox row = (VBox) node;
            if (row.getChildren().size() < 2) {
                continue;
            }
            Node labelNode = row.getChildren().get(0);
            Node inputNode = row.getChildren().get(1);
            if (labelNode instanceof Text && inputNode instanceof TextField) {
                String label = ((Text) labelNode).getText();
                String value = ((TextField) inputNode).getText();
                values.put(label, value);
            }
        }
        return values;
    }

    private String[] splitName(String fullName) {
        String normalized = fullName == null ? "" : fullName.trim();
        if (normalized.isEmpty()) {
            return new String[]{"Unknown", "User"};
        }

        String[] parts = normalized.split("\\s+", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], parts[0]};
        }
        return new String[]{parts[0], parts[1]};
    }

    private boolean isTruthy(String value) {
        String normalized = value == null ? "" : value.trim();
        return "yes".equalsIgnoreCase(normalized)
            || "true".equalsIgnoreCase(normalized)
            || "1".equalsIgnoreCase(normalized)
            || "verified".equalsIgnoreCase(normalized);
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
    private HBox moduleModeSwitcher(String[] labels, String activeLabel, Consumer<String> onSelect) {
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

    private void switchToTestPage(StackPane container) {
        Stage stage = (Stage) container.getScene().getWindow();
        Scene previousScene = container.getScene();
        new AddResidence(stage, previousScene).show();
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

    private Text t(String s, String family, FontWeight w, double size) {
        Text tx = new Text(s);
        tx.setFont(Font.font(family, w, size));
        tx.setFill(textPrimaryColor());
        return tx;
    }
    private String boldFont()  { return com.syndicati.MainApplication.getInstance().getBoldFontFamily();  }
    private String lightFont() { return com.syndicati.MainApplication.getInstance().getLightFontFamily(); }
}


