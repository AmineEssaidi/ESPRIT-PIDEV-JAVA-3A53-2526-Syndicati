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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Static Evenement page mirroring frontend/evenement/index.html.twig major sections.
 */
public class EvenementPageView implements ViewInterface {

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();

    public EvenementPageView() {
        root = new VBox(22);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20, 0, 40, 0));
        root.setMaxWidth(Double.MAX_VALUE);
        root.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(buildHero(), buildDashboardBand(), buildEventsGrid(), note());
    }

    private VBox buildHero() {
        VBox hero = new VBox(10);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setMaxWidth(1800);
        hero.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        hero.setPadding(new Insets(96, 64, 96, 64));
        hero.setStyle(
            "-fx-background-color: #0a0a0f;" +
            "-fx-background-radius: 48px;" +
            "-fx-border-color: rgba(255,255,255,0.05);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 48px;"
        );

        StackPane badge = new StackPane(text("Community Experiences", 12, true, "#ffffff"));
        badge.setPadding(new Insets(7, 14, 7, 14));
        badge.setMaxWidth(StackPane.USE_PREF_SIZE);
        badge.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.12) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.25) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 999px;" +
            "-fx-border-radius: 999px;"
        );

        Text title = text("Discover. Connect.\nExperience.", 74, true, tm.getAccentHex());
        Text sub = text("Join exclusive events, workshops, and gatherings designed for our community. Your next great story starts here.", 21, false, "rgba(255,255,255,0.5)");
        sub.setWrappingWidth(640);

        Button explore = chip("Explore Events");
        explore.setStyle(
            "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
            "-fx-text-fill: white; -fx-background-radius: 14px; -fx-padding: 12 24 12 24; -fx-font-weight: bold;"
        );

        hero.getChildren().addAll(badge, title, sub, explore);
        return hero;
    }

    private HBox buildDashboardBand() {
        HBox row = new HBox(16);
        row.setMaxWidth(1800);
        row.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        row.setPadding(new Insets(86, 64, 86, 64));
        row.setStyle(
            "-fx-background-color: #0a0a0f;" +
            "-fx-background-radius: 48px;" +
            "-fx-border-color: rgba(255,255,255,0.05);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 48px;"
        );

        VBox left = new VBox(10,
            text("Event Dashboard", 24, true, tm.getTextColor()),
            text("Join the action. Upcoming events and hosting controls are represented as static UI.", 13, false, tm.getSecondaryTextColor()),
            text("Join 500+ members in our next gathering", 12, false, tm.getSecondaryTextColor())
        );
        left.setPadding(new Insets(0));
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(10);
        right.setPadding(new Insets(28));
        right.setMinWidth(420);
        right.setStyle(
            "-fx-background-color: rgba(0,0,0,0.4);" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 32px;" +
            "-fx-border-radius: 32px;"
        );
        right.getChildren().addAll(
            chip("Host an Event"),
            calendarMock(),
            text("Host Event Form Face", 13, true, tm.getTextColor()),
            text("Title, Date, Type, Location, Description, Places, Banner", 12, false, tm.getSecondaryTextColor())
        );

        row.getChildren().addAll(left, right);
        return row;
    }

    private VBox calendarMock() {
        VBox cal = new VBox(8);
        cal.setPadding(new Insets(12));
        cal.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 16px;");
        cal.getChildren().addAll(
            text("Calendar", 13, true, tm.getTextColor()),
            text("Mo Tu We Th Fr Sa Su", 12, false, tm.getSecondaryTextColor()),
            text("Tag pills: #Workshops #Meetups #Social #Sports", 11, false, tm.getSecondaryTextColor())
        );
        return cal;
    }

    private VBox buildEventsGrid() {
        VBox section = new VBox(12);
        section.setMaxWidth(1800);
        section.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        section.getChildren().addAll(text("Upcoming Events", 28, true, tm.getTextColor()));

        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(24);

        for (int i = 0; i < 3; i++) {
            VBox card = eventCard("Event " + (i + 1), "Date and time", "Location", "Description preview");
            card.setMaxWidth(Double.MAX_VALUE);
            GridPane.setFillWidth(card, true);
            grid.add(card, i, 0);
        }

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setFillWidth(true);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        section.getChildren().add(grid);
        return section;
    }

    private VBox eventCard(String title, String date, String location, String desc) {
        VBox card = new VBox(0);
        card.setMinHeight(480);
        card.setStyle(
            "-fx-background-color: rgba(0,0,0,0.70);" +
            "-fx-border-color: rgba(255,255,255,0.10);" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 32px;" +
            "-fx-border-radius: 32px;"
        );

        StackPane media = new StackPane();
        media.setMinHeight(220);
        media.setStyle("-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.18) + "; -fx-background-radius: 32px 32px 0 0;");

        StackPane typeTag = new StackPane(text("Meetup", 10, true, "#ffffff"));
        typeTag.setPadding(new Insets(6, 12, 6, 12));
        typeTag.setStyle("-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.20) + "; -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.30) + "; -fx-border-width: 1px; -fx-background-radius: 14px; -fx-border-radius: 14px;");
        StackPane.setAlignment(typeTag, Pos.TOP_RIGHT);
        StackPane.setMargin(typeTag, new Insets(16, 16, 0, 0));
        media.getChildren().add(typeTag);

        VBox body = new VBox(12);
        body.setPadding(new Insets(22));

        Text meta = text(date + " | " + location, 12, false, "rgba(255,255,255,0.45)");
        Text titleText = text(title, 28, true, "#ffffff");
        Text descText = text(desc, 14, false, "rgba(255,255,255,0.45)");

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 16, 12, 16));
        footer.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 20px;" +
            "-fx-border-radius: 20px;"
        );
        footer.getChildren().add(chip("View Details"));

        body.getChildren().addAll(meta, titleText, descText, footer);
        card.getChildren().addAll(media, body);

        addCardHover(card);
        return card;
    }

    private Button chip(String txt) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.16) + "; -fx-text-fill: white; -fx-background-radius: 999px;");
        return b;
    }

    private Text note() {
        return text("UI duplicated from evenement Twig structure. Map/calendar interactivity and CRUD are deferred.", 12, false, tm.getSecondaryTextColor());
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

    private void addCardHover(VBox card) {
        card.setOnMouseEntered(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(220), card);
            tt.setToY(-8);
            tt.play();
            ScaleTransition st = new ScaleTransition(Duration.millis(220), card);
            st.setToX(1.01);
            st.setToY(1.01);
            st.play();
        });
        card.setOnMouseExited(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(220), card);
            tt.setToY(0);
            tt.play();
            ScaleTransition st = new ScaleTransition(Duration.millis(220), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
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
