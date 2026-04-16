package com.syndicati.views.backend.dashboard;
import com.syndicati.utils.theme.ThemeManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
public abstract class BaseDashboardPage {
    protected final Stage stage;
    protected final Scene previousScene;


    public BaseDashboardPage(Stage stage, Scene previousScene) {
        this.stage = stage;
        this.previousScene = previousScene;
    }
    public void show() {
        HBox root = new HBox();
        root.setStyle(
                "-fx-background-color: " + (isDark()
                        ? "linear-gradient(to bottom right, #070707, #030303 55%, #000000 100%)"
                        : "linear-gradient(to bottom right, #f7f7f7, #f2f2f2 55%, #ececec 100%)"
                ) + ";"
        );
        root.setPadding(new Insets(40, 0, 0, 0));

        VBox sidebar = buildSidebar();
        sidebar.setPrefWidth(250);
        sidebar.setMinWidth(250);
        sidebar.setMaxWidth(250);

        VBox mainArea = new VBox(0);
        mainArea.setFillWidth(true);
        HBox.setHgrow(mainArea, Priority.ALWAYS);
        mainArea.setStyle("-fx-background-color:" + (isDark() ? "#050505" : "#f3f4f6") + ";");

        VBox contentArea = new VBox(20);
        contentArea.setPadding(new Insets(24));
        contentArea.setFillWidth(true);
        contentArea.getChildren().add(buildContent());

        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        mainArea.getChildren().addAll(buildHeader(), scroll);
        root.getChildren().addAll(sidebar, mainArea);

        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
    }
    protected abstract VBox buildContent();

    // ── Sidebar ───────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sb = new VBox(10);
        sb.setAlignment(Pos.TOP_CENTER);
        sb.setFillWidth(true);
        sb.setPadding(new Insets(12, 10, 12, 10));
        sb.setStyle("-fx-background-color:transparent;");

        VBox topPill = glassPill();
        topPill.setPadding(new Insets(12, 10, 12, 10));
        StackPane logoMark = new StackPane();
        logoMark.setPrefSize(44, 44);
        logoMark.setStyle("-fx-background-color:" + accentGradient() + ";-fx-background-radius:14px;");
        Text sLetter = new Text("S");
        sLetter.setFont(Font.font(boldFont(), FontWeight.BOLD, 26));
        sLetter.setFill(Color.WHITE);
        logoMark.getChildren().add(sLetter);
        Text logoT = new Text("SYNDICATI");
        logoT.setFont(Font.font(boldFont(), FontWeight.BOLD, 15));
        logoT.setFill(isDark() ? Color.WHITE : Color.web("#111827"));
        Text adminT = new Text("Admin Panel");
        adminT.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        adminT.setFill(isDark() ? Color.web("rgba(255,255,255,0.4)") : Color.web("rgba(15,23,42,0.55)"));
        VBox logoText = new VBox(1, logoT, adminT);
        HBox logoRow = new HBox(10, logoMark, logoText);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        topPill.getChildren().add(logoRow);

        VBox navPill = glassPill();
        navPill.setPadding(new Insets(14, 10, 14, 10));
        navPill.setSpacing(4);
        VBox.setVgrow(navPill, Priority.ALWAYS);
        navPill.getChildren().addAll(
                navItem("📊", "Dashboard"),
                navItem("👥", "Users"),
                navItem("💬", "Forum"),
                navItem("🏛️", "Syndicat"),
                navItem("🏢", "Residence"),
                navItem("🎉", "Evenement")
        );

        VBox bottomPill = glassPill();
        bottomPill.setPadding(new Insets(10));
        Button back = new Button("⌂  Back to App");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setStyle("-fx-background-color:transparent;-fx-background-radius:12px;-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.5)" : "rgba(15,23,42,0.55)") + ";-fx-cursor:hand;");
        back.setOnAction(e -> stage.setScene(previousScene));
        bottomPill.getChildren().add(back);

        sb.getChildren().addAll(topPill, navPill, bottomPill);
        return sb;
    }private HBox navItem(String icon, String label) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 10, 6, 10));
        row.setStyle("-fx-background-color:transparent;-fx-background-radius:12px;-fx-cursor:hand;");
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(32, 32);
        iconBox.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.06)") + ";-fx-background-radius:10px;");
        Text ic = new Text(icon);
        ic.setFont(Font.font(15));
        iconBox.getChildren().add(ic);
        Text lbl = new Text(label);
        lbl.setFont(Font.font(lightFont(), FontWeight.NORMAL, 15));
        lbl.setFill(isDark() ? Color.web("rgba(229,231,235,0.75)") : Color.web("rgba(15,23,42,0.72)"));
        row.getChildren().addAll(iconBox, lbl);
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color:" + accentRgba(0.12) + ";-fx-background-radius:12px;-fx-cursor:hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color:transparent;-fx-background-radius:12px;-fx-cursor:hand;"));
        return row;
    }

    private HBox buildHeader() {
        HBox h = new HBox(14);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(12, 24, 12, 24));
        h.setMinHeight(58);
        h.setMaxHeight(58);
        h.setStyle(
                "-fx-background-color:" + (isDark() ? "rgba(0,0,0,0.98)" : "rgba(255,255,255,0.98)") + ";" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.14)" : "rgba(15,23,42,0.12)") + ";" +
                        "-fx-border-width:0 0 1 0;"
        );
        HBox searchBar = new HBox();
        searchBar.setPadding(new Insets(7, 14, 7, 14));
        searchBar.setPrefWidth(280);
        searchBar.setStyle(
                "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.04)") + ";" +
                        "-fx-background-radius:10px;" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.14)") + ";" +
                        "-fx-border-width:1;-fx-border-radius:10px;"
        );
        Text sch = new Text(" Search admin...");
        sch.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        sch.setFill(isDark() ? Color.web("rgba(255,255,255,0.60)") : Color.web("rgba(15,23,42,0.62)"));
        searchBar.getChildren().add(sch);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        HBox userPill = new HBox(8);
        userPill.setAlignment(Pos.CENTER);
        userPill.setPadding(new Insets(5, 12, 5, 10));
        userPill.setStyle(
                "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.05)") + ";" +
                        "-fx-background-radius:20px;" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.12)") + ";" +
                        "-fx-border-width:1;-fx-border-radius:20px;"
        );
        Text adminLabel = new Text("Admin");
        adminLabel.setFont(Font.font(boldFont(), FontWeight.BOLD, 12));
        adminLabel.setFill(isDark() ? Color.WHITE : Color.web("#111827"));
        userPill.getChildren().add(adminLabel);
        h.getChildren().addAll(searchBar, sp, userPill);
        return h;
    }

    // ── Shared UI helpers (available to all subclasses) ───────────────
    protected VBox glassCard() {
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setFillWidth(true);
        card.setStyle(
                "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.03)" : "rgba(255,255,255,0.85)") + ";" +
                        "-fx-background-radius:16px;" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.07)" : "rgba(15,23,42,0.10)") + ";" +
                        "-fx-border-width:1;-fx-border-radius:16px;"
        );
        return card;
    }

    protected VBox fieldGroup(String label, TextField input, Text errorText) {
        VBox group = new VBox(6);
        Text lbl = new Text(label);
        lbl.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        lbl.setFill(isDark() ? Color.web("rgba(255,255,255,0.55)") : Color.web("rgba(15,23,42,0.64)"));
        input.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        input.setMaxWidth(Double.MAX_VALUE);
        input.setStyle(
                "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.04)") + ";" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.14)") + ";" +
                        "-fx-border-width:1;" +
                        "-fx-text-fill:" + (isDark() ? "white" : "#111827") + ";" +
                        "-fx-prompt-text-fill:" + (isDark() ? "rgba(255,255,255,0.25)" : "rgba(15,23,42,0.35)") + ";" +
                        "-fx-background-radius:10px;-fx-border-radius:10px;-fx-padding:10 12 10 12;"
        );
        errorText.setFont(Font.font(lightFont(), FontWeight.NORMAL, 11));
        errorText.setFill(Color.web("#ef4444"));
        group.getChildren().addAll(lbl, input, errorText);
        return group;
    }
    protected VBox fieldGroup(String label, ComboBox<String> comboBox, Text errorText) {
        VBox group = new VBox(6);
        Text lbl = new Text(label);
        lbl.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        lbl.setFill(isDark() ? Color.web("rgba(255,255,255,0.55)") : Color.web("rgba(15,23,42,0.64)"));
        //comboBox.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setStyle(
                "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.04)") + ";" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.14)") + ";" +
                        "-fx-border-width:1;" +
                        "-fx-text-fill:" + (isDark() ? "white" : "#111827") + ";" +
                        "-fx-prompt-text-fill:" + (isDark() ? "rgba(255,255,255,0.25)" : "rgba(15,23,42,0.35)") + ";" +
                        "-fx-background-radius:10px;-fx-border-radius:10px;-fx-padding:4 12 4 12;"
        );
        comboBox.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                comboBox.lookup(".list-view").setStyle(
                        "-fx-background-color:" + (isDark() ? "#1e1e2e" : "white") + ";" +
                                "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.14)") + ";" +
                                "-fx-border-width:1;" +
                                "-fx-background-radius:10px;-fx-border-radius:10px;"
                );
            }
        });
        errorText.setFont(Font.font(lightFont(), FontWeight.NORMAL, 11));
        errorText.setFill(Color.web("#ef4444"));
        group.getChildren().addAll(lbl, comboBox, errorText);
        return group;
    }

    protected Button primaryButton(String label) {
        Button b = new Button(label);
        b.setFont(Font.font(boldFont(), FontWeight.BOLD, 13));
        b.setPadding(new Insets(10, 24, 10, 24));
        b.setStyle(
                "-fx-background-color:" + accentRgba(0.24) + ";" +
                        "-fx-border-color:" + accentRgba(0.34) + ";" +
                        "-fx-border-width:1;-fx-background-radius:100px;-fx-border-radius:100px;" +
                        "-fx-text-fill:white;-fx-cursor:hand;"
        );
        return b;
    }

    protected Button secondaryButton(String label) {
        Button b = new Button(label);
        b.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        b.setPadding(new Insets(10, 20, 10, 20));
        b.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.20)") + ";" +
                        "-fx-border-width:1;-fx-background-radius:100px;-fx-border-radius:100px;" +
                        "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.80)" : "rgba(15,23,42,0.86)") + ";-fx-cursor:hand;"
        );
        return b;
    }

    protected Text pageTitle(String text) {
        Text t = new Text(text);
        t.setFont(Font.font(boldFont(), FontWeight.BOLD, 22));
        t.setFill(isDark() ? Color.web("#f8fafc") : Color.web("#111827"));
        return t;
    }

    protected int parseIntSafe(String value, Text errorField) {
        if (value == null || value.isBlank() || value.equals("-")) {
            errorField.setText("This field must be a number.");
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            errorField.setText("Invalid number: \"" + value + "\"");
            return 0;
        }
    }

    // ── Theme helpers ─────────────────────────────────────────────────
    private VBox glassPill() {
        VBox pill = new VBox(0);
        pill.setAlignment(Pos.TOP_CENTER);
        pill.setFillWidth(true);
        String glassFill = isDark()
                ? "linear-gradient(to bottom right, rgba(14,14,14,0.94), rgba(10,10,10,0.96) 54%, rgba(6,6,6,0.98) 100%)"
                : "linear-gradient(to bottom right, rgba(255,255,255,0.98), rgba(248,248,248,0.97) 54%, rgba(242,242,242,0.96) 100%)";
        pill.setStyle(
                "-fx-background-color:" + glassFill + ";" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.14)" : "rgba(15,23,42,0.12)") + ";" +
                        "-fx-border-width:1;-fx-border-radius:24px;-fx-background-radius:24px;" +
                        "-fx-effect:dropshadow(gaussian," + (isDark() ? "rgba(0,0,0,0.28)" : "rgba(15,23,42,0.12)") + ",26,0,0,8);"
        );
        return pill;
    }

    public Alert styledAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        DialogPane dp = alert.getDialogPane();

        dp.setStyle("-fx-background-color: #1e1e2e;");
        dp.lookup(".header-panel").setStyle("-fx-background-color: #2a2a3e;");
        dp.lookup(".header-panel .label").setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        dp.lookup(".content.label").setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 13px;");
        dp.lookup(".button-bar").setStyle("-fx-background-color: #1e1e2e;");
        dp.lookupAll(".button").forEach(btn -> btn.setStyle(
                "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6;"
        ));

        return alert;
    }

    protected void styleComboBox(ComboBox<String> comboBox) {
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setStyle(
                "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.04)") + ";" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.14)") + ";" +
                        "-fx-border-width:1;" +
                        "-fx-text-fill:" + (isDark() ? "white" : "#111827") + ";" +
                        "-fx-prompt-text-fill:" + (isDark() ? "rgba(255,255,255,0.25)" : "rgba(15,23,42,0.35)") + ";" +
                        "-fx-background-radius:10px;-fx-border-radius:10px;-fx-padding:4 12 4 12;"
        );

        String cellBg      = isDark() ? "#1e1e2e"              : "white";
        String cellText    = isDark() ? "white"                : "#111827";
        String cellHoverBg = isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.06)";
        String borderColor = isDark() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.14)";

        String cellCss = String.format("""
        .combo-box-popup .list-view {
            -fx-background-color: %s;
            -fx-border-color: %s;
            -fx-border-width: 1;
            -fx-border-radius: 10px;
            -fx-background-radius: 10px;
        }
        .combo-box-popup .list-cell {
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-padding: 8 12 8 12;
            -fx-font-size: 13px;
        }
        .combo-box-popup .list-cell:hover {
            -fx-background-color: %s;
        }
        .combo-box-popup .list-cell:selected {
            -fx-background-color: %s;
            -fx-text-fill: %s;
        }
    """, cellBg, borderColor, cellBg, cellText, cellHoverBg, cellHoverBg, cellText);

        comboBox.getStylesheets().add(
                "data:text/css," + cellCss.replace("\n", "").replace("  ", " ")
        );

        comboBox.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                Platform.runLater(() -> {
                    Node listView = comboBox.lookup(".list-view");
                    if (listView != null) {
                        listView.setStyle(
                                "-fx-background-color:" + cellBg + ";" +
                                        "-fx-border-color:" + borderColor + ";" +
                                        "-fx-border-width:1;" +
                                        "-fx-background-radius:10px;-fx-border-radius:10px;"
                        );
                    }
                });
            }
        });
    }
    protected ThemeManager theme()             { return ThemeManager.getInstance(); }
    protected boolean isDark()                 { return theme().isDarkMode(); }
    protected String accentHex()              { return theme().getAccentHex(); }
    protected String accentGradient()         { return theme().getEffectiveAccentGradient(); }
    protected String accentRgba(double alpha) { return theme().toRgba(accentHex(), alpha); }
    protected String boldFont()              { return com.syndicati.MainApplication.getInstance().getBoldFontFamily(); }
    protected String lightFont()             { return com.syndicati.MainApplication.getInstance().getLightFontFamily(); }

}
