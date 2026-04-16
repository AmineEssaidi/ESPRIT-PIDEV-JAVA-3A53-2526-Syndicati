package com.syndicati.views.frontend.services;

import com.syndicati.MainApplication;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.utils.theme.ThemeManager;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import javafx.application.Platform;
import com.syndicati.controllers.frontend.services.syndicat.ReclamationController;
import javafx.animation.PauseTransition;
import java.io.File;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;

/**
 * Static Syndicat page mirroring frontend/syndicat/index.html.twig structure.
 */
public class SyndicatPageView implements ViewInterface {

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();
    private final ReclamationController reclamationController;
    private VBox popupPreview;
    private Text popupTitle;
    private Text popupMessage;
    private File selectedImageFile = null;

    public SyndicatPageView() {
        reclamationController = new ReclamationController();
        root = new VBox(22);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20, 0, 40, 0));
        root.setMaxWidth(Double.MAX_VALUE);
        root.setStyle("-fx-background-color: transparent;");

        VBox form = buildFormCard();
        VBox.setMargin(form, new Insets(-90, 0, 0, 0));
        popupPreview = buildPopupPreview();
        popupPreview.setVisible(false); // Hide by default
        root.getChildren().addAll(buildHero(), form, popupPreview);
    }

    private StackPane buildHero() {
        StackPane hero = new StackPane();
        hero.setMaxWidth(1800);
        hero.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        hero.setMinHeight(500);
        hero.setPadding(new Insets(92, 64, 92, 64));
        hero.setStyle(
                "-fx-background-color: " + surfaceStrong() + ";" +
                        "-fx-background-radius: 48px;" +
                        "-fx-border-color: " + borderSoft() + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 48px;");

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
                        "-fx-border-radius: 999px;");

        Text title = line("Voice Your\nConcerns.", 72, true, tm.getAccentHex());
        Text subtitle = line(
                "We are here to listen and resolve. Submit your reclamation directly to the syndicat and track its progress in real-time.",
                21, false, textMuted());
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
                "-fx-background-color: " + surfaceCard() + ";" +
                        "-fx-border-color: " + borderSoft() + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-radius: 40px;" +
                        "-fx-border-radius: 40px;");

        Text title = line("Submit a Reclamation", 34, true, tm.getAccentHex());
        Text sub = line("Fill in the details below. We'll get back to you shortly.", 15, false, textMuted());

        TextField subject = input("Subject: What is this regarding?");

        // Add validation for subject
        subject.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean valid = newVal != null && newVal.trim().length() >= 4 && newVal.trim().length() <= 30 &&
                    newVal.trim().length() > 0 && Character.isLetter(newVal.trim().charAt(0));
            subject.setStyle(valid ? "-fx-background-color: " + inputBg() + "; -fx-text-fill: " + tm.getTextColor()
                    + "; -fx-prompt-text-fill: " + textMuted() + "; -fx-background-radius: 16px; -fx-border-color: "
                    + borderSoft() + "; -fx-border-radius: 16px; -fx-padding: 14 16 14 16;"
                    : "-fx-background-color: rgba(239,68,68,0.1); -fx-text-fill: " + tm.getTextColor()
                            + "; -fx-prompt-text-fill: " + textMuted()
                            + "; -fx-background-radius: 16px; -fx-border-color: #ef4444; -fx-border-radius: 16px; -fx-padding: 14 16 14 16;");
        });

        // Add tooltip for subject validation
        Tooltip subjectTooltip = new Tooltip("Subject must start with a letter and be 4-30 characters long");
        Tooltip.install(subject, subjectTooltip);

        TextArea description = new TextArea();
        description.setPromptText("Description");
        description.setPrefRowCount(4);
        styleArea(description);

        // Add validation for description
        description.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean valid = newVal != null && newVal.trim().length() >= 5 && newVal.trim().length() <= 200;
            description.setStyle(valid
                    ? "-fx-control-inner-background: " + textAreaInnerBg() + "; -fx-background-color: " + inputBg()
                            + "; -fx-text-fill: " + tm.getTextColor() + "; -fx-prompt-text-fill: " + textMuted()
                            + "; -fx-background-radius: 16px; -fx-border-color: " + borderSoft()
                            + "; -fx-border-radius: 16px;"
                    : "-fx-control-inner-background: " + textAreaInnerBg()
                            + "; -fx-background-color: rgba(239,68,68,0.1); -fx-text-fill: " + tm.getTextColor()
                            + "; -fx-prompt-text-fill: " + textMuted()
                            + "; -fx-background-radius: 16px; -fx-border-color: #ef4444; -fx-border-radius: 16px;");
        });

        // Add tooltip for description validation
        Tooltip descTooltip = new Tooltip("Description must be 5-200 characters long");
        Tooltip.install(description, descTooltip);

        HBox attachmentZone = new HBox(12);
        attachmentZone.setAlignment(Pos.CENTER_LEFT);
        attachmentZone.setPadding(new Insets(18));
        attachmentZone
                .setStyle("-fx-background-color: " + surfaceSoft() + "; -fx-background-radius: 14px; -fx-border-color: "
                        + borderSoft() + "; -fx-border-radius: 14px; -fx-border-width: 1px;");

        Button chooseImageBtn = new Button("Choose Image");
        chooseImageBtn.setStyle("-fx-background-color: " + tm.getEffectiveAccentGradient()
                + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 8 16 8 16;");
        addButtonPulse(chooseImageBtn);

        Text selectedFileTxt = line("No image selected (optional)", 12, false, tm.getSecondaryTextColor());

        chooseImageBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp"));

            javafx.stage.Stage stage = (javafx.stage.Stage) root.getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);

            if (file != null) {
                selectedImageFile = file;
                selectedFileTxt.setText(file.getName());
                selectedFileTxt.setFill(Color.web(tm.getTextColor()));
            }
        });

        attachmentZone.getChildren().addAll(chooseImageBtn, selectedFileTxt);

        Button submit = new Button("Submit Reclamation");
        submit.setStyle(
                "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: 800;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-padding: 14 24 14 24;" +
                        "-fx-letter-spacing: 1px;");
        submit.setMaxWidth(Double.MAX_VALUE);
        addButtonPulse(submit);

        submit.setOnAction(e -> {
            // Validate fields before submitting
            String subjectText = subject.getText();
            String descText = description.getText();

            boolean subjectValid = subjectText != null && subjectText.trim().length() >= 4
                    && subjectText.trim().length() <= 30 &&
                    subjectText.trim().length() > 0 && Character.isLetter(subjectText.trim().charAt(0));
            boolean descValid = descText != null && descText.trim().length() >= 5 && descText.trim().length() <= 200;

            if (!subjectValid || !descValid) {
                showPopup("Validation Error", "Please correct the highlighted fields before submitting.", false);
                return;
            }

            submit.setDisable(true);
            submit.setText("Submitting...");

            reclamationController.handleSubmit(
                    subject.getText(),
                    description.getText(),
                    selectedImageFile,
                    () -> Platform.runLater(() -> {
                        showPopup("Success", "Your reclamation has been submitted successfully.", true);
                        submit.setDisable(false);
                        submit.setText("Submit Reclamation");
                        subject.clear();
                        description.clear();
                        selectedImageFile = null;
                        selectedFileTxt.setText("No image selected (optional)");
                        selectedFileTxt.setFill(Color.web(tm.getSecondaryTextColor()));
                    }),
                    (errorMsg) -> Platform.runLater(() -> {
                        showPopup("Error", errorMsg, false);
                        submit.setDisable(false);
                        submit.setText("Submit Reclamation");
                    }));
        });

        form.getChildren().addAll(title, sub, label("Subject"), subject, label("Description"), description,
                label("Attachments"), attachmentZone, submit);
        return form;
    }

    private VBox buildPopupPreview() {
        VBox popup = new VBox(8);
        popup.setAlignment(Pos.CENTER);
        popup.setMaxWidth(700);
        popup.setPadding(new Insets(20));
        popup.setStyle(shell(20, "rgba(255,255,255,0.02)", 1.2));

        popupTitle = line("Notification", 18, true, tm.getTextColor());
        popupMessage = line("Message goes here", 14, false, tm.getSecondaryTextColor());
        popupMessage.setWrappingWidth(600);

        popup.getChildren().addAll(popupTitle, popupMessage);
        return popup;
    }

    private void showPopup(String title, String message, boolean isSuccess) {
        popupTitle.setText(title);
        popupMessage.setText(message);

        if (isSuccess) {
            popupTitle.setFill(Color.web("#10b981")); // Success color (Green)
        } else {
            popupTitle.setFill(Color.web("#ef4444")); // Danger color (Red)
        }

        popupPreview.setVisible(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(4));
        delay.setOnFinished(e -> popupPreview.setVisible(false));
        delay.play();
    }

    private TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle("-fx-background-color: " + inputBg() + "; -fx-text-fill: " + tm.getTextColor()
                + "; -fx-prompt-text-fill: " + textMuted() + "; -fx-background-radius: 16px; -fx-border-color: "
                + borderSoft() + "; -fx-border-radius: 16px; -fx-padding: 14 16 14 16;");
        return field;
    }

    private void styleDate(DatePicker d) {
        d.setStyle("-fx-background-color: " + inputBg() + "; -fx-text-fill: " + tm.getTextColor()
                + "; -fx-background-radius: 16px; -fx-border-color: " + borderSoft() + "; -fx-border-radius: 16px;");
    }

    private String textAreaInnerBg() {
        return tm.isDarkMode() ? "#1a1e28" : "#f1f5f9";
    }

    private void styleArea(TextArea a) {
        a.setStyle("-fx-control-inner-background: " + textAreaInnerBg() + "; -fx-background-color: " + inputBg()
                + "; -fx-text-fill: " + tm.getTextColor() + "; -fx-prompt-text-fill: " + textMuted()
                + "; -fx-background-radius: 16px; -fx-border-color: " + borderSoft() + "; -fx-border-radius: 16px;");
    }

    private Text label(String txt) {
        return line(txt, 12, true, tm.getSecondaryTextColor());
    }

    private Text line(String txt, int size, boolean bold, String color) {
        Text t = new Text(txt);
        t.setFont(Font.font(
                bold ? MainApplication.getInstance().getBoldFontFamily()
                        : MainApplication.getInstance().getLightFontFamily(),
                bold ? FontWeight.BOLD : FontWeight.NORMAL,
                size));
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

    private String surfaceStrong() {
        return tm.isDarkMode()
                ? "linear-gradient(from 0% 0% to 100% 100%, #020202 0%, #070707 58%, #0b0b0b 100%)"
                : "linear-gradient(from 0% 0% to 100% 100%, #ffffff 0%, #f8fafc 100%)";
    }

    private String surfaceCard() {
        return tm.isDarkMode()
                ? "linear-gradient(from 0% 0% to 100% 100%, rgba(10,10,10,0.94) 0%, rgba(14,14,14,0.94) 62%, "
                        + tm.toRgba(tm.getAccentHex(), 0.10) + " 100%)"
                : "linear-gradient(from 0% 0% to 100% 100%, rgba(255,255,255,0.98) 0%, rgba(248,250,252,0.96) 100%)";
    }

    private String surfaceSoft() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.06)";
    }

    private String inputBg() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.06)";
    }

    private String borderSoft() {
        return tm.isDarkMode() ? tm.toRgba(tm.getAccentHex(), 0.34) : "rgba(15,23,42,0.16)";
    }

    private String textMuted() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.79)" : "rgba(30,41,59,0.82)";
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public void cleanup() {
    }
}
