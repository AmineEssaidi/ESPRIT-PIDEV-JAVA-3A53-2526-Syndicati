package com.pidev.views.profile;

import com.pidev.MainApplication;
import com.pidev.interfaces.ViewInterface;
import com.pidev.utils.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Profile page replica based on templates/frontend/profile/profile.html.twig.
 * This is a UI-only duplication without backend behavior.
 */
public class ProfileView implements ViewInterface {

    private final VBox root;
    private final ThemeManager tm;

    private final Map<String, VBox> mainPages = new LinkedHashMap<>();
    private final Map<String, Button> mainNavButtons = new LinkedHashMap<>();

    private final Map<String, VBox> detailTabs = new LinkedHashMap<>();
    private final Map<String, Button> detailTabButtons = new LinkedHashMap<>();

    public ProfileView() {
        this.tm = ThemeManager.getInstance();
        this.root = new VBox();
        build();
    }

    private void build() {
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
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-border-color: rgba(255,255,255,0.10);" +
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
                "-fx-text-fill: rgba(255,255,255,0.55);" +
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
        card.prefWidthProperty().bind(root.widthProperty().multiply(0.95));
        card.setStyle(shell(34, "rgba(8,8,12,0.92)", 0.09));

        StackPane banner = new StackPane();
        banner.setMinHeight(180);
        banner.setStyle(
            "-fx-background-color: linear-gradient(to right, rgba(43,43,58,0.95), rgba(20,20,28,0.9));" +
            "-fx-background-radius: 34px 34px 0 0;"
        );
        banner.getChildren().add(text("PROFILE", 40, true, "rgba(255,255,255,0.12)"));

        HBox body = new HBox(26);
        body.setPadding(new Insets(24));
        body.setAlignment(Pos.TOP_LEFT);

        StackPane avatarWrap = new StackPane();
        Circle avatar = new Circle(56);
        avatar.setFill(Color.web(tm.toRgba(tm.getAccentHex(), 0.22)));
        avatar.setStroke(Color.web(tm.toRgba(tm.getAccentHex(), 0.35)));
        avatar.setStrokeWidth(2);
        Text avatarText = text("A", 38, true, "#ffffff");
        avatarWrap.getChildren().addAll(avatar, avatarText);

        VBox identity = new VBox(10);
        identity.setAlignment(Pos.TOP_LEFT);
        Text name = text("Amine User", 34, true, "#ffffff");
        Text role = text("Resident", 16, false, "rgba(255,255,255,0.60)");
        Text email = text("amine@example.com", 14, false, "rgba(255,255,255,0.50)");

        HBox stats = new HBox(12,
            statPill("Account created", "12/03/2026"),
            statPill("Status", "Verified")
        );

        HBox quickTop = new HBox();
        quickTop.setAlignment(Pos.CENTER_RIGHT);
        Button quickBtn = new Button("Quick Actions");
        quickBtn.setStyle("-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.18) + "; -fx-text-fill: " + tm.getAccentHex() + "; -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.35) + "; -fx-border-width: 1px; -fx-background-radius: 999px; -fx-border-radius: 999px; -fx-padding: 9 16 9 16; -fx-font-weight: 700;");
        quickTop.getChildren().add(quickBtn);

        GridPane actions = new GridPane();
        actions.setHgap(10);
        actions.setVgap(10);
        ColumnConstraints c = new ColumnConstraints();
        c.setPercentWidth(33.33);
        actions.getColumnConstraints().addAll(c, c, c);
        actions.add(actionTile("Host Spotlight"), 0, 0);
        actions.add(actionTile("Join by Code"), 1, 0);
        actions.add(actionTile("2FA"), 2, 0);
        actions.add(actionTile("Biometrics"), 0, 1);
        actions.add(actionTile("Face ID"), 1, 1);
        actions.add(actionTile("Settings"), 2, 1);

        identity.getChildren().addAll(name, role, email, stats, quickTop, actions);
        HBox.setHgrow(identity, Priority.ALWAYS);

        body.getChildren().addAll(avatarWrap, identity);
        card.getChildren().addAll(banner, body);
        return card;
    }

    private VBox createStandingCard() {
        VBox card = cardShell();
        card.setPadding(new Insets(22));

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox level = new VBox(0, text("7", 28, true, "#ffffff"), text("LVL", 10, true, tm.getAccentHex()));
        level.setAlignment(Pos.CENTER);
        level.setMinSize(72, 72);
        level.setStyle(shell(16, "rgba(255,255,255,0.05)", 0.09));

        VBox title = new VBox(4, text("RESIDENT STANDING", 18, true, "#ffffff"), text("Your status within the Horizon community", 12, false, "rgba(255,255,255,0.5)"));
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
        Text left = text("XP TOWARDS LEVEL 8", 11, true, "rgba(255,255,255,0.55)");
        Text right = text("20/100", 11, true, "#ffffff");
        HBox.setHgrow(left, Priority.ALWAYS);
        xpTop.getChildren().addAll(left, right);

        StackPane progressTrack = new StackPane();
        progressTrack.setAlignment(Pos.CENTER_LEFT);
        progressTrack.setMinHeight(8);
        progressTrack.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 999px;");
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
        search.setStyle(shell(18, "rgba(255,255,255,0.03)", 0.08));
        search.getChildren().addAll(text("Explore Community", 13, true, "#ffffff"), text("Search residents and send connection requests.", 12, false, "rgba(255,255,255,0.55)"));

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
            : "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.55);")
            + "-fx-font-weight: 800; -fx-background-radius: 999px; -fx-padding: 11 18 11 18;");
    }

    private VBox createAccountTab() {
        VBox tab = new VBox(16);
        tab.setPadding(new Insets(16, 0, 0, 0));

        VBox account = cardShell();
        account.setPadding(new Insets(22));
        account.getChildren().addAll(
            text("Account", 24, true, "#ffffff"),
            infoLine("First name", "Amine"),
            infoLine("Last name", "User"),
            infoLine("Email", "amine@example.com"),
            infoLine("Role", "Resident"),
            infoLine("Verified", "Yes"),
            infoLine("Account created", "12/03/2026"),
            infoLine("Bio", "UI-only duplicate of profile twig account panel."),
            infoLine("Timezone", "Africa/Tunis"),
            infoLine("Phone", "+216 XX XXX XXX")
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
            text("Update language, theme and profile preferences.", 13, false, "rgba(255,255,255,0.60)"),
            prefGrid,
            text("Suggestions: Keep notifications instant for urgent building updates.", 13, false, "rgba(255,255,255,0.55)")
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
        subTabs.setStyle(shell(999, "rgba(255,255,255,0.05)", 0.10));

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
        pagination.setStyle(shell(999, "rgba(255,255,255,0.03)", 0.08));

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
        pagination.setStyle(shell(999, "rgba(255,255,255,0.03)", 0.08));
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
        pagination.setStyle(shell(999, "rgba(255,255,255,0.03)", 0.08));
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
        Text key = text(k, 13, true, "rgba(255,255,255,0.55)");
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
        Text right = text(date, 11, false, "rgba(255,255,255,0.45)");
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
            text(date + " | " + location, 12, false, "rgba(255,255,255,0.50)")
        );
        HBox.setHgrow(titleMeta, Priority.ALWAYS);

        Text statusTag = text(status, 11, true, "#ffffff");
        VBox statusWrap = new VBox(statusTag);
        statusWrap.setPadding(new Insets(6, 10, 6, 10));
        statusWrap.setStyle(shell(999, tm.toRgba(tm.getAccentHex(), 0.18), 0.25));

        top.getChildren().addAll(iconWrap, titleMeta, statusWrap);
        row.getChildren().addAll(top, text("Type: " + type + " | Enrolled: 12/40", 12, false, "rgba(255,255,255,0.55)"));
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

        row.getChildren().addAll(top, text(submitted, 12, false, "rgba(255,255,255,0.50)"));
        return row;
    }

    private VBox residenceItem(String residence, String unit, String relation, String availability, String parking) {
        VBox row = new VBox(8);
        row.setPadding(new Insets(16));
        row.setStyle(shell(16, "rgba(255,255,255,0.03)", 0.08));

        HBox top = new HBox();
        VBox info = new VBox(4,
            text(residence, 15, true, "#ffffff"),
            text(unit, 12, false, "rgba(255,255,255,0.55)")
        );
        HBox.setHgrow(info, Priority.ALWAYS);
        Text relationTag = text(relation, 11, true, tm.getAccentHex());
        top.getChildren().addAll(info, relationTag);

        row.getChildren().addAll(top, text(availability + " | " + parking, 12, false, "rgba(255,255,255,0.52)"));
        return row;
    }

    private VBox pillCard(String label, String value) {
        VBox pill = new VBox(4,
            text(label, 11, true, "rgba(255,255,255,0.55)"),
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
            : "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.60);")
            + "-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-radius: 999px; -fx-padding: 7 12 7 12;"
        );
        return b;
    }

    private Button forumTabPill(String text, boolean active) {
        Button b = new Button(text);
        b.setStyle((active
            ? "-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: #ffffff;"
            : "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.55);")
            + "-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-radius: 999px; -fx-padding: 9 12 9 12;"
        );
        return b;
    }

    private Button pagePill(String text, boolean active) {
        Button b = new Button(text);
        b.setMinSize(34, 34);
        b.setStyle((active
            ? "-fx-background-color: " + tm.getEffectiveAccentGradient() + "; -fx-text-fill: #ffffff;"
            : "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.55);")
            + "-fx-font-size: 11px; -fx-font-weight: 800; -fx-background-radius: 999px;"
        );
        return b;
    }

    private VBox actionTile(String label) {
        VBox tile = new VBox(6);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(12));
        tile.setStyle(shell(12, "rgba(255,255,255,0.03)", 0.08));
        tile.getChildren().addAll(
            text("[]", 18, true, tm.getAccentHex()),
            text(label, 12, true, "rgba(255,255,255,0.9)")
        );
        return tile;
    }

    private VBox standingPoint(String label, boolean active) {
        VBox point = new VBox(6);
        point.setAlignment(Pos.CENTER);
        Circle dot = new Circle(10);
        dot.setFill(active ? Color.web(tm.getAccentHex()) : Color.web("#2f3136"));
        Text txt = text(label, 11, true, active ? "#ffffff" : "rgba(255,255,255,0.35)");
        point.getChildren().addAll(dot, txt);
        return point;
    }

    private VBox friendCard(String name) {
        VBox c = new VBox(8);
        c.setAlignment(Pos.CENTER);
        StackPane avatar = new StackPane();
        avatar.setMinSize(60, 60);
        avatar.setStyle(shell(16, "rgba(255,255,255,0.05)", 0.10));
        avatar.getChildren().add(text("U", 18, true, "rgba(255,255,255,0.75)"));
        c.getChildren().addAll(avatar, text(name, 11, true, "rgba(255,255,255,0.6)"));
        return c;
    }

    private VBox statPill(String label, String value) {
        VBox pill = new VBox(4, text(label, 11, true, "rgba(255,255,255,0.55)"), text(value, 13, true, "#ffffff"));
        pill.setPadding(new Insets(10, 14, 10, 14));
        pill.setStyle(shell(12, "rgba(255,255,255,0.04)", 0.09));
        return pill;
    }

    private Button tabChip(String label, boolean active) {
        Button b = new Button(label);
        b.setStyle((active
            ? "-fx-background-color: " + tm.getEffectiveAccentGradient() + "; -fx-text-fill: white;"
            : "-fx-background-color: rgba(255,255,255,0.04); -fx-text-fill: rgba(255,255,255,0.55);")
            + "-fx-font-weight: 800; -fx-background-radius: 12px; -fx-padding: 8 12 8 12;");
        return b;
    }

    private Text badge(String label) {
        return text(label, 10, true, "#ffffff");
    }

    private VBox cardShell() {
        VBox c = new VBox(16);
        c.setStyle(shell(28, "rgba(8,8,12,0.90)", 0.08));
        return c;
    }

    private String shell(double radius, String bg, double borderOpacity) {
        return "-fx-background-color: " + bg + ";"
            + "-fx-border-color: rgba(255,255,255," + borderOpacity + ");"
            + "-fx-border-width: 1px;"
            + "-fx-background-radius: " + radius + "px;"
            + "-fx-border-radius: " + radius + "px;";
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
}
