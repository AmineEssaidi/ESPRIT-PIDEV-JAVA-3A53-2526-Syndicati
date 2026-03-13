package com.pidev.views.services;

import com.pidev.MainApplication;
import com.pidev.interfaces.ViewInterface;
import com.pidev.utils.theme.ThemeManager;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Static Residence page mirroring frontend/residence/index.html.twig layout blocks.
 */
public class ResidencePageView implements ViewInterface {

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();

    public ResidencePageView() {
        root = new VBox(24);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20, 0, 40, 0));
        root.setMaxWidth(Double.MAX_VALUE);
        root.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(
            buildHero(),
            buildSwitcherMock(),
            buildResidencesGrid(),
            buildApartmentListMock(),
            buildApartmentDetailsMock()
        );
    }

    private StackPane buildHero() {
        StackPane hero = new StackPane();
        hero.setMaxWidth(1800);
        hero.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        hero.setMinHeight(600);
        hero.setPadding(new Insets(120, 64, 120, 64));
        hero.setStyle(
            "-fx-background-color: #000000;" +
            "-fx-background-radius: 48px;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 48px;"
        );

        Region glow = new Region();
        glow.setPrefSize(900, 900);
        glow.setStyle(
            "-fx-background-color: radial-gradient(center 50% 50%, radius 60%, " +
            tm.toRgba(tm.getAccentHex(), 0.20) + " 0%, " + tm.toRgba(tm.getAccentHex(), 0.00) + " 70%);"
        );
        glow.setTranslateX(420);
        glow.setTranslateY(-180);

        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER_LEFT);

        Text badgeTxt = new Text("Premium Living");
        badgeTxt.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 12));
        badgeTxt.setFill(Color.WHITE);
        StackPane badge = new StackPane(badgeTxt);
        badge.setPadding(new Insets(8, 16, 8, 16));
        badge.setMaxWidth(Region.USE_PREF_SIZE);
        badge.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.10) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.30) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 999px;" +
            "-fx-border-radius: 999px;"
        );

        Text title = new Text("Luxury Living\nRedefined.");
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BLACK, 72));
        title.setFill(Color.web("#f7f9fc"));

        Text subtitle = new Text("Explore our curated selection of high-end residences and apartments. Experience a new standard of comfort and elegance with Horizon.");
        subtitle.setWrappingWidth(700);
        subtitle.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 22));
        subtitle.setFill(Color.web("rgba(255,255,255,0.52)"));

        Button cta = ghostButton("View Residences");
        cta.setStyle(
            "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 999px;" +
            "-fx-padding: 12 28 12 28;"
        );

        content.getChildren().addAll(badge, title, subtitle, cta);

        hero.getChildren().addAll(glow, content);
        StackPane.setAlignment(content, Pos.CENTER_LEFT);
        return hero;
    }

    private HBox buildSwitcherMock() {
        HBox row = new HBox(14);
        row.setMaxWidth(1600);
        row.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1600));
        row.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().addAll(
            switchFace("Residence List"),
            switchFace("Apartments List"),
            switchFace("Apartment Details")
        );
        return row;
    }

    private VBox switchFace(String title) {
        VBox face = new VBox(8);
        face.setPadding(new Insets(16));
        face.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(face, Priority.ALWAYS);
        face.setMinHeight(120);
        face.setStyle(shell(20, "rgba(255,255,255,0.03)", 1.2));

        Text t = new Text(title);
        t.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 15));
        t.setFill(Color.web(tm.getTextColor()));

        Text s = new Text("UI switcher face placeholder");
        s.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12));
        s.setFill(Color.web(tm.getSecondaryTextColor()));

        face.getChildren().addAll(t, s);
        return face;
    }

    private VBox buildResidencesGrid() {
        VBox section = new VBox(14);
        section.setMaxWidth(1600);
        section.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1600));

        Text title = sectionTitle("Our Residences");
        Text sub = new Text("Discover the perfect space that suits your lifestyle.");
        sub.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 16));
        sub.setFill(Color.web("rgba(255,255,255,0.45)"));

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);

        String[][] cards = {
            {"Azure Residence", "Lake District", "12 Floors", "120 Units", "3 Blocks"},
            {"Palm Heights", "La Marsa", "9 Floors", "88 Units", "2 Blocks"},
            {"Jardin Central", "Mutuelleville", "14 Floors", "160 Units", "4 Blocks"}
        };

        for (int i = 0; i < cards.length; i++) {
            VBox card = residenceCard(cards[i]);
            GridPane.setFillWidth(card, true);
            card.setMaxWidth(Double.MAX_VALUE);
            grid.add(card, i, 0);
        }

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            grid.getColumnConstraints().add(col);
        }

        section.getChildren().addAll(title, sub, grid);
        return section;
    }

    private VBox residenceCard(String[] data) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(0));
        card.setStyle(
            "-fx-background-color: #0a0a0a;" +
            "-fx-background-radius: 32px;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 32px;"
        );

        StackPane media = new StackPane();
        media.setMinHeight(280);
        media.setStyle("-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.22) + ";-fx-background-radius: 32px 32px 0 0;");

        StackPane yearTag = new StackPane(new Text("2026"));
        yearTag.setPadding(new Insets(6, 12, 6, 12));
        yearTag.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-background-radius: 20px; -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1px;");
        StackPane.setAlignment(yearTag, Pos.TOP_RIGHT);
        StackPane.setMargin(yearTag, new Insets(18, 18, 0, 0));
        media.getChildren().add(yearTag);

        VBox body = new VBox(12);
        body.setPadding(new Insets(24, 24, 20, 24));

        Text name = new Text(data[0]);
        name.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.EXTRA_BOLD, 28));
        name.setFill(Color.WHITE);

        Text loc = new Text(data[1]);
        loc.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 15));
        loc.setFill(Color.web("rgba(255,255,255,0.42)"));

        HBox stats = new HBox(18, stat(data[2]), stat(data[3]), stat(data[4]));
        stats.setPadding(new Insets(12, 0, 0, 0));
        stats.setStyle("-fx-border-color: rgba(255,255,255,0.05) transparent transparent transparent; -fx-border-width: 1px 0 0 0;");

        HBox actions = new HBox(8, ghostButton("See Apartments"), ghostButton("Export PDF"));

        Region accentBar = new Region();
        accentBar.setPrefHeight(4);
        accentBar.setMaxWidth(Double.MAX_VALUE);
        accentBar.setStyle("-fx-background-color: " + tm.getEffectiveAccentGradient() + "; -fx-background-radius: 0 0 32px 32px;");
        accentBar.setScaleX(0.0);

        body.getChildren().addAll(name, loc, stats, actions);
        card.getChildren().addAll(media, body, accentBar);

        addCardHover(card, accentBar);
        return card;
    }

    private VBox buildApartmentListMock() {
        VBox box = new VBox(10);
        box.setMaxWidth(1600);
        box.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1600));
        box.setPadding(new Insets(20));
        box.setStyle(shell(24, "rgba(255,255,255,0.03)", 1.2));

        Text title = sectionTitle("Apartments in Selected Residence");

        HBox row = new HBox(12,
            miniCard("Apartment A1", "Bloc A / Floor 1 / Available"),
            miniCard("Apartment B5", "Bloc B / Floor 5 / Rented"),
            miniCard("Apartment C3", "Bloc C / Floor 3 / Available")
        );

        box.getChildren().addAll(title, row);
        return box;
    }

    private VBox buildApartmentDetailsMock() {
        VBox box = new VBox(10);
        box.setMaxWidth(1600);
        box.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1600));
        box.setPadding(new Insets(20));
        box.setStyle(shell(24, "rgba(255,255,255,0.03)", 1.2));

        Text title = sectionTitle("Apartment Details");

        HBox content = new HBox(14);
        StackPane image = new StackPane();
        image.setMinHeight(220);
        image.setMinWidth(360);
        image.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 16px;");

        VBox details = new VBox(8);
        details.getChildren().addAll(
            detailLine("Rent", "1800 TND"),
            detailLine("Area", "126 m2"),
            detailLine("Floor", "5"),
            detailLine("Parking", "Included"),
            detailLine("Owner Contact", "Static placeholder")
        );
        HBox.setHgrow(details, Priority.ALWAYS);

        content.getChildren().addAll(image, details);
        box.getChildren().addAll(title, content);
        return box;
    }

    private Text detailLine(String k, String v) {
        Text t = new Text(k + ": " + v);
        t.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13));
        t.setFill(Color.web(tm.getSecondaryTextColor()));
        return t;
    }

    private VBox miniCard(String title, String desc) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setStyle(shell(16, "rgba(255,255,255,0.02)", 1));
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);

        Text t = new Text(title);
        t.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 15));
        t.setFill(Color.web(tm.getTextColor()));

        Text d = new Text(desc);
        d.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12));
        d.setFill(Color.web(tm.getSecondaryTextColor()));

        card.getChildren().addAll(t, d);
        return card;
    }

    private Text sectionTitle(String text) {
        Text t = new Text(text);
        t.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.EXTRA_BOLD, 48));
        t.setFill(Color.WHITE);
        return t;
    }

    private Button ghostButton(String text) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.15) + ";" +
            "-fx-text-fill: white; -fx-background-radius: 14px; -fx-padding: 8 14 8 14;"
        );
        return b;
    }

    private Region stat(String text) {
        StackPane p = new StackPane();
        p.setPadding(new Insets(6, 10, 6, 10));
        p.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 10px;");
        Text t = new Text(text);
        t.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 11));
        t.setFill(Color.web("rgba(255,255,255,0.70)"));
        p.getChildren().add(t);
        return p;
    }

    private void addCardHover(VBox card, Region accentBar) {
        card.setOnMouseEntered(e -> {
            TranslateTransition lift = new TranslateTransition(Duration.millis(260), card);
            lift.setToY(-15);
            lift.play();

            ScaleTransition scale = new ScaleTransition(Duration.millis(260), card);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();

            ScaleTransition bar = new ScaleTransition(Duration.millis(220), accentBar);
            bar.setToX(1.0);
            bar.play();
        });
        card.setOnMouseExited(e -> {
            TranslateTransition lift = new TranslateTransition(Duration.millis(220), card);
            lift.setToY(0);
            lift.play();

            ScaleTransition scale = new ScaleTransition(Duration.millis(220), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.play();

            ScaleTransition bar = new ScaleTransition(Duration.millis(200), accentBar);
            bar.setToX(0.0);
            bar.play();
        });
    }

    private String shell(double radius, String inner, double inset) {
        double innerRadius = Math.max(0, radius - inset);
        return "-fx-background-color: " + tm.getEffectiveAccentGradient() + ", " + inner + ";" +
            "-fx-background-insets: 0, " + inset + ";" +
            "-fx-background-radius: " + radius + "px, " + innerRadius + "px;" +
            "-fx-border-color: transparent;";
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public void cleanup() {}
}
