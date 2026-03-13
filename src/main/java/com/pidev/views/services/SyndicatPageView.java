package com.pidev.views.services;

import com.pidev.MainApplication;
import com.pidev.interfaces.ViewInterface;
import com.pidev.utils.theme.ThemeManager;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Static Syndicat page mirroring frontend/syndicat/index.html.twig structure.
 */
public class SyndicatPageView implements ViewInterface {

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();

    public SyndicatPageView() {
        root = new VBox(22);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20, 0, 40, 0));
        root.setMaxWidth(Double.MAX_VALUE);
        root.setStyle("-fx-background-color: transparent;");

        VBox form = buildFormCard();
        VBox.setMargin(form, new Insets(-90, 0, 0, 0));
        root.getChildren().addAll(buildHero(), form, buildPopupPreview());
    }

    private StackPane buildHero() {
        StackPane hero = new StackPane();
        hero.setMaxWidth(1800);
        hero.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        hero.setMinHeight(500);
        hero.setPadding(new Insets(92, 64, 92, 64));
        hero.setStyle(
            "-fx-background-color: #0a0a0f;" +
            "-fx-background-radius: 48px;" +
            "-fx-border-color: rgba(255,255,255,0.05);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 48px;"
        );

        VBox content = new VBox(12);
        content.setAlignment(Pos.CENTER_LEFT);

        Text badgeText = new Text("Syndicat Services");
        badgeText.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 12));
        badgeText.setFill(Color.WHITE);
        StackPane badge = new StackPane(badgeText);
        badge.setPadding(new Insets(7, 14, 7, 14));
        badge.setMaxWidth(StackPane.USE_PREF_SIZE);
        badge.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.12) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.25) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 999px;" +
            "-fx-border-radius: 999px;"
        );

        Text title = line("Voice Your\nConcerns.", 72, true, tm.getAccentHex());
        Text subtitle = line("We are here to listen and resolve. Submit your reclamation directly to the syndicat and track its progress in real-time.", 21, false, "rgba(255,255,255,0.5)");
        subtitle.setWrappingWidth(660);

        content.getChildren().addAll(badge, title, subtitle);
        hero.getChildren().add(content);
        StackPane.setAlignment(content, Pos.CENTER_LEFT);
        return hero;
    }

    private VBox buildFormCard() {
        VBox form = new VBox(12);
        form.setMaxWidth(1000);
        form.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.90), 1000));
        form.setPadding(new Insets(56, 56, 56, 56));
        form.setStyle(
            "-fx-background-color: rgba(255,255,255,0.02);" +
            "-fx-border-color: rgba(255,255,255,0.05);" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 40px;" +
            "-fx-border-radius: 40px;"
        );

        Text title = line("Submit a Reclamation", 34, true, tm.getAccentHex());
        Text sub = line("Fill in the details below. We'll get back to you shortly.", 15, false, "rgba(255,255,255,0.5)");

        TextField subject = input("Subject: What is this regarding?");
        DatePicker date = new DatePicker();
        date.setPromptText("Date of Incident");
        styleDate(date);

        TextArea description = new TextArea();
        description.setPromptText("Description");
        description.setPrefRowCount(4);
        styleArea(description);

        StackPane attachmentZone = new StackPane(line("Attachments (optional) preview area", 12, false, tm.getSecondaryTextColor()));
        attachmentZone.setPadding(new Insets(18));
        attachmentZone.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 14px; -fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 14px; -fx-border-width: 1px;");

        Button submit = new Button("Submit Reclamation");
        submit.setStyle(
            "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 800;" +
            "-fx-background-radius: 999px;" +
            "-fx-padding: 14 24 14 24;" +
            "-fx-letter-spacing: 1px;"
        );
        submit.setMaxWidth(Double.MAX_VALUE);
        addButtonPulse(submit);

        form.getChildren().addAll(title, sub, label("Subject"), subject, label("Date of Incident"), date, label("Description"), description, label("Attachments"), attachmentZone, submit);
        return form;
    }

    private VBox buildPopupPreview() {
        VBox popup = new VBox(8);
        popup.setAlignment(Pos.CENTER);
        popup.setMaxWidth(700);
        popup.setPadding(new Insets(20));
        popup.setStyle(shell(20, "rgba(255,255,255,0.02)", 1.2));
        popup.getChildren().addAll(
            line("Cool Popup Preview", 18, true, tm.getTextColor()),
            line("Success/error popup system placeholder duplicated from Twig structure.", 12, false, tm.getSecondaryTextColor())
        );
        return popup;
    }

    private TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.3); -fx-background-radius: 16px; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 16px; -fx-padding: 14 16 14 16;");
        return field;
    }

    private void styleDate(DatePicker d) {
        d.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-text-fill: white; -fx-background-radius: 16px; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 16px;");
    }

    private void styleArea(TextArea a) {
        a.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.3); -fx-background-radius: 16px; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 16px;");
    }

    private Text label(String txt) {
        return line(txt, 12, true, tm.getSecondaryTextColor());
    }

    private Text line(String txt, int size, boolean bold, String color) {
        Text t = new Text(txt);
        t.setFont(Font.font(
            bold ? MainApplication.getInstance().getBoldFontFamily() : MainApplication.getInstance().getLightFontFamily(),
            bold ? FontWeight.BOLD : FontWeight.NORMAL,
            size
        ));
        t.setFill(Color.web(color));
        return t;
    }

    private void addButtonPulse(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), button);
            st.setToX(1.02);
            st.setToY(1.02);
            st.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(180), button);
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
