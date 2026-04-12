package com.syndicati.views.backend.dashboard;

import com.syndicati.models.entities.Residence;
import com.syndicati.models.services.ServiceResidence;
import com.syndicati.utils.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AddResidence {

    private final Stage stage;
    private final Scene previousScene;

    private TextField nomResidenceField;
    private TextField adresseField;
    private TextField nAppartementsField;
    private TextField dateAjoutField;
    private TextField nBlocsField;

    private Text nomError;
    private Text adresseError;
    private Text nAppartementsError;
    private Text dateAjoutError;
    private Text nBlocsError;

    public AddResidence(Stage stage, Scene previousScene) {
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

        // ── Sidebar (reuse DashboardView's sidebar look) ──────────────
        VBox sidebar = buildSidebar();
        sidebar.setPrefWidth(250);
        sidebar.setMinWidth(250);
        sidebar.setMaxWidth(250);

        // ── Main area ─────────────────────────────────────────────────
        VBox mainArea = new VBox(0);
        mainArea.setFillWidth(true);
        HBox.setHgrow(mainArea, Priority.ALWAYS);
        mainArea.setStyle("-fx-background-color:" + (isDark() ? "#050505" : "#f3f4f6") + ";");

        // Header bar
        mainArea.getChildren().add(buildHeader());

        // Content area — put your page content here
        VBox contentArea = new VBox(20);
        contentArea.setPadding(new Insets(24));
        contentArea.setFillWidth(true);

        // ── YOUR CONTENT GOES HERE ─────────────────────────────────────
        Text title = new Text("Add Residence");
        title.setFont(Font.font(boldFont(), FontWeight.BOLD, 22));
        title.setFill(textPrimaryColor());

        Button backBtn = new Button("← Back");
        backBtn.setPadding(new Insets(8, 16, 8, 16));
        backBtn.setStyle(
                "-fx-background-color:" + accentRgba(0.22) + ";" +
                        "-fx-border-color:" + accentRgba(0.34) + ";" +
                        "-fx-border-width:1;" +
                        "-fx-background-radius:100px;" +
                        "-fx-border-radius:100px;" +
                        "-fx-text-fill:white;" +
                        "-fx-cursor:hand;"
        );
        backBtn.setOnAction(e -> stage.setScene(previousScene));
        contentArea.getChildren().add(buildForm());

        contentArea.getChildren().addAll(title, backBtn);
        // ──────────────────────────────────────────────────────────────

        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        mainArea.getChildren().add(scroll);
        root.getChildren().addAll(sidebar, mainArea);

        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        stage.setScene(scene);
    }

    // ── Sidebar ───────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sb = new VBox(10);
        sb.setAlignment(Pos.TOP_CENTER);
        sb.setFillWidth(true);
        sb.setPadding(new Insets(12, 10, 12, 10));
        sb.setStyle("-fx-background-color:transparent;");

        // Logo pill
        VBox topPill = glassPill();
        topPill.setPadding(new Insets(12, 10, 12, 10));

        StackPane logoMark = new StackPane();
        logoMark.setPrefSize(44, 44);
        logoMark.setStyle(
                "-fx-background-color:" + accentGradient() + ";" +
                        "-fx-background-radius:14px;"
        );
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

        // Nav pill
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

        // Bottom pill
        VBox bottomPill = glassPill();
        bottomPill.setPadding(new Insets(10));

        Button back = new Button("⌂  Back to App");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setStyle("-fx-background-color:transparent;-fx-background-radius:12px;-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.5)" : "rgba(15,23,42,0.55)") + ";-fx-cursor:hand;");
        back.setOnAction(e -> stage.setScene(previousScene));
        bottomPill.getChildren().add(back);

        sb.getChildren().addAll(topPill, navPill, bottomPill);
        return sb;
    }

    private HBox navItem(String icon, String label) {
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

    private VBox buildForm() {
        VBox form = new VBox(20);
        form.setFillWidth(true);

        // Title
        Text title = new Text("Add Residence");
        title.setFont(Font.font(boldFont(), FontWeight.BOLD, 22));
        title.setFill(textPrimaryColor());

        // Glass card container
        VBox card = new VBox(18);
        card.setPadding(new Insets(24));
        card.setStyle(
                "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.03)" : "rgba(255,255,255,0.85)") + ";" +
                        "-fx-background-radius:16px;" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.07)" : "rgba(15,23,42,0.10)") + ";" +
                        "-fx-border-width:1;" +
                        "-fx-border-radius:16px;"
        );

        // ── Fields ────────────────────────────────────────────────────────

        nomResidenceField = new TextField();
        nomResidenceField.setPromptText("e.g. Résidence Les Pins");
        nomError = new Text("");
        VBox nomGroup = fieldGroup("Nom de la Résidence", nomResidenceField, nomError);

        adresseField = new TextField();
        adresseField.setPromptText("e.g. 12 Rue de la Paix, Tunis");
        adresseError = new Text("");
        VBox adresseGroup = fieldGroup("Adresse", adresseField, adresseError);

        nAppartementsField = new TextField();
        nAppartementsField.setPromptText("e.g. 24");
        nAppartementsError = new Text("");
        VBox nAppartementsGroup = fieldGroup("Nombre d'Appartements", nAppartementsField, nAppartementsError);

        dateAjoutField = new TextField();
        dateAjoutField.setPromptText("e.g. 2024-01-15");
        dateAjoutError = new Text("");
        VBox dateAjoutGroup = fieldGroup("Date d'Ajout", dateAjoutField, dateAjoutError);

        nBlocsField = new TextField();
        nBlocsField.setPromptText("e.g. 3");
        nBlocsError = new Text("");
        VBox nBlocsGroup = fieldGroup("Nombre de Blocs", nBlocsField, nBlocsError);

        // ── Actions ───────────────────────────────────────────────────────
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(8, 0, 0, 0));

        Button backBtn = new Button("← Back");
        backBtn.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        backBtn.setPadding(new Insets(10, 20, 10, 20));
        backBtn.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.16)" : "rgba(15,23,42,0.20)") + ";" +
                        "-fx-border-width:1;" +
                        "-fx-background-radius:100px;" +
                        "-fx-border-radius:100px;" +
                        "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.80)" : "rgba(15,23,42,0.86)") + ";" +
                        "-fx-cursor:hand;"
        );
        backBtn.setOnAction(e -> stage.setScene(previousScene));

        Button submitBtn = new Button("Create Residence");
        submitBtn.setFont(Font.font(boldFont(), FontWeight.BOLD, 13));
        submitBtn.setPadding(new Insets(10, 24, 10, 24));
        submitBtn.setStyle(
                "-fx-background-color:" + accentRgba(0.24) + ";" +
                        "-fx-border-color:" + accentRgba(0.34) + ";" +
                        "-fx-border-width:1;" +
                        "-fx-background-radius:100px;" +
                        "-fx-border-radius:100px;" +
                        "-fx-text-fill:white;" +
                        "-fx-cursor:hand;"
        );
        submitBtn.setOnAction(e -> ajouterResidence());

        actions.getChildren().addAll(backBtn, submitBtn);
        card.getChildren().addAll(nomGroup, adresseGroup, nAppartementsGroup, dateAjoutGroup, nBlocsGroup, actions);
        form.getChildren().addAll(title, card);
        return form;
    }

    // ── Field builder ─────────────────────────────────────────────────────
    private VBox fieldGroup(String label, TextField input, Text errorText) {
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
                        "-fx-background-radius:10px;" +
                        "-fx-border-radius:10px;" +
                        "-fx-padding:10 12 10 12;"
        );

        errorText.setFont(Font.font(lightFont(), FontWeight.NORMAL, 11));
        errorText.setFill(Color.web("#ef4444"));

        group.getChildren().addAll(lbl, input, errorText);
        return group;
    }
    // ── Top header bar ────────────────────────────────────────────────
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
                        "-fx-border-width:1;" +
                        "-fx-border-radius:10px;"
        );
        Text sch = new Text(" Search admin...");
        sch.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        sch.setFill(isDark() ? Color.web("rgba(255,255,255,0.60)") : Color.web("rgba(15,23,42,0.62)"));
        searchBar.getChildren().add(sch);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // Admin pill
        HBox userPill = new HBox(8);
        userPill.setAlignment(Pos.CENTER);
        userPill.setPadding(new Insets(5, 12, 5, 10));
        userPill.setStyle(
                "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.05)") + ";" +
                        "-fx-background-radius:20px;" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.12)") + ";" +
                        "-fx-border-width:1;" +
                        "-fx-border-radius:20px;"
        );
        Text adminLabel = new Text("Admin");
        adminLabel.setFont(Font.font(boldFont(), FontWeight.BOLD, 12));
        adminLabel.setFill(isDark() ? Color.WHITE : Color.web("#111827"));
        userPill.getChildren().add(adminLabel);

        h.getChildren().addAll(searchBar, sp, userPill);
        return h;
    }

    // ── Theme helpers (mirrors DashboardView) ─────────────────────────
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
                        "-fx-border-width:1;" +
                        "-fx-border-radius:24px;" +
                        "-fx-background-radius:24px;" +
                        "-fx-effect:dropshadow(gaussian," + (isDark() ? "rgba(0,0,0,0.28)" : "rgba(15,23,42,0.12)") + ",26,0,0,8);"
        );
        return pill;
    }

    private ThemeManager theme()              { return ThemeManager.getInstance(); }
    private boolean isDark()                  { return theme().isDarkMode(); }
    private String accentHex()               { return theme().getAccentHex(); }
    private String accentGradient()          { return theme().getEffectiveAccentGradient(); }
    private String accentRgba(double alpha)  { return theme().toRgba(accentHex(), alpha); }
    private Color textPrimaryColor()         { return isDark() ? Color.web("#f8fafc") : Color.web("#111827"); }
    private String boldFont()               { return com.syndicati.MainApplication.getInstance().getBoldFontFamily(); }
    private String lightFont()              { return com.syndicati.MainApplication.getInstance().getLightFontFamily(); }


    private int parseIntSafe(String value, Text errorField) {
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

    private void ajouterResidence() {
        String nom = nomResidenceField.getText();
        String adresse = adresseField.getText();
        String nAppartementsStr = nAppartementsField.getText();
        String dateAjout = dateAjoutField.getText();
        String nBlocs = nBlocsField.getText();

        nomError.setText("");
        adresseError.setText("");
        nAppartementsError.setText("");
        dateAjoutError.setText("");
        nBlocsError.setText("");

        Residence residence = new Residence();
        residence.setNom_r(nom);
        residence.setAdresse(adresse);
        residence.setImage_r(null);
        residence.setDate_ajout(dateAjout);
        residence.setN_appartements(parseIntSafe(nAppartementsStr, nAppartementsError));
        residence.setN_etages(2);
        residence.setN_blocs(nBlocs);

        ServiceResidence serviceResidence = new ServiceResidence();

        try {
            serviceResidence.Ajouter(residence);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Résidence Ajoutée");
            alert.setHeaderText("Résidence ajoutée avec succès!");
            alert.show();

            stage.setScene(previousScene);

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur lors de l'ajout");
            alert.setHeaderText(e.getMessage());
            alert.show();
        }
    }
}