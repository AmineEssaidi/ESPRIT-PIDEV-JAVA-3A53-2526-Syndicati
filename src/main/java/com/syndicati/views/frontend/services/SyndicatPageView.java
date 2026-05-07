package com.syndicati.views.frontend.services;

import com.syndicati.MainApplication;
import com.syndicati.controllers.syndicat.ReclamationController;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.models.syndicat.Reclamation;
import com.syndicati.models.user.User;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.theme.ThemeManager;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.Scene;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Syndicat Page View with interactive reclamation form and premium UI.
 */
public class SyndicatPageView implements ViewInterface {

    private static final int SUBJECT_MIN_LENGTH = 5;
    private static final int SUBJECT_MAX_LENGTH = 255;
    private static final int DESCRIPTION_MIN_LENGTH = 10;
    private static final int DESCRIPTION_MAX_LENGTH = 255;

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();
    private final ReclamationController controller = new ReclamationController();
    
    // Form fields
    private TextField subjectField;
    private LocalDateTime selectedDateTime;
    private TextArea descriptionField;
    private Label subjectValidationLabel;
    private Label descriptionValidationLabel;
    private Button submitButton;
    private List<File> selectedFiles = new ArrayList<>();
    private Text fileStatusText;
    private FlowPane previewPane;

    public SyndicatPageView() {
        root = new VBox(22);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20, 0, 40, 0));
        root.setMaxWidth(Double.MAX_VALUE);
        root.setStyle("-fx-background-color: transparent;");

        VBox form = buildFormCard();
        VBox.setMargin(form, new Insets(-90, 0, 0, 0));
        root.getChildren().addAll(buildHero(), form);
    }

    private StackPane buildHero() {
        StackPane hero = new StackPane();
        hero.setMaxWidth(1800);
        hero.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        hero.setMinHeight(500);
        hero.setPadding(new Insets(92, 64, 92, 64));
        hero.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-background-radius: 48px;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
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
        Text subtitle = line("We are here to listen and resolve. Submit your reclamation directly to the syndicat and track its progress in real-time.", 21, false, "rgba(255,255,255,0.6)");
        subtitle.setWrappingWidth(660);

        content.getChildren().addAll(badge, title, subtitle);
        hero.getChildren().add(content);
        StackPane.setAlignment(content, Pos.CENTER_LEFT);
        return hero;
    }

    private VBox buildFormCard() {
        VBox form = new VBox(16);
        form.setMaxWidth(1000);
        form.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.90), 1000));
        form.setPadding(new Insets(48, 48, 48, 48));
        form.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-border-color: rgba(255,255,255,0.1);" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 40px;" +
            "-fx-border-radius: 40px;"
        );

        Text title = line("Submit a Reclamation", 34, true, tm.getAccentHex());
        Text sub = line("Fill in the details below. We'll get back to you shortly.", 15, false, "rgba(255,255,255,0.5)");

        // Subject field with label
        VBox subjectSection = new VBox(6);
        subjectSection.getChildren().add(label("Subject"));
        subjectField = input("What is this regarding?");
        subjectValidationLabel = createLiveValidationLabel("Start typing your subject...");
        subjectSection.getChildren().addAll(subjectField, subjectValidationLabel);

        // Date field (Simplified to current date for now or a basic selector)
        VBox dateSection = new VBox(6);
        dateSection.getChildren().add(label("Date of Incident"));
        Button dateBtn = new Button("Select Date: " + LocalDate.now());
        dateBtn.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 10;");
        selectedDateTime = LocalDateTime.now();
        dateSection.getChildren().add(dateBtn);

        // Description field with label
        VBox descSection = new VBox(4);
        descSection.getChildren().add(label("Description"));

        descriptionField = new TextArea();
        descriptionField.setPromptText("Please describe the issue in detail...");
        descriptionField.setPrefRowCount(6);
        descriptionField.setWrapText(true);
        descriptionField.setStyle(
            "-fx-control-inner-background: rgba(255,255,255,0.05);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.4);" +
            "-fx-background-radius: 14px;" +
            "-fx-border-color: rgba(255,255,255,0.1);" +
            "-fx-border-radius: 14px;" +
            "-fx-border-width: 1.5px;" +
            "-fx-padding: 12;" +
            "-fx-font-size: 13px;"
        );
        descriptionField.setMinHeight(120);
        descSection.getChildren().add(descriptionField);
        descriptionValidationLabel = createLiveValidationLabel("Describe your issue with at least 10 characters...");
        descSection.getChildren().add(descriptionValidationLabel);

        // Attachments zone
        VBox attachmentSection = new VBox(6);
        attachmentSection.getChildren().add(label("Attachments"));

        VBox attachmentZone = new VBox(8);
        attachmentZone.setAlignment(Pos.CENTER);
        attachmentZone.setPrefHeight(100);
        attachmentZone.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-border-color: " + tm.getAccentHex() + "66; -fx-border-width: 2; -fx-border-style: dashed; -fx-background-radius: 14; -fx-border-radius: 14; -fx-cursor: hand;");
        
        Text uploadIcon = line("📎", 32, false, tm.getAccentHex());
        Text uploadText = line("Click to upload images", 12, false, "rgba(255,255,255,0.6)");
        fileStatusText = line("No files selected", 11, false, "rgba(255,255,255,0.5)");
        
        attachmentZone.getChildren().addAll(uploadIcon, uploadText, fileStatusText);
        attachmentZone.setOnMouseClicked(e -> openFileChooser());

        previewPane = new FlowPane(8, 8);
        previewPane.setPadding(new Insets(8, 0, 0, 0));
        
        attachmentSection.getChildren().addAll(attachmentZone, previewPane);

        // Submit button
        submitButton = new Button("Submit Reclamation");
        submitButton.setCursor(Cursor.HAND);
        submitButton.setStyle(
            "-fx-background-color: " + tm.getAccentHex() + ";"
            + "-fx-text-fill: white;"
            + "-fx-font-size: 14px;"
            + "-fx-font-weight: 800;"
            + "-fx-background-radius: 12px;"
            + "-fx-padding: 14 24 14 24;"
        );
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setOnAction(e -> handleSubmit());

        form.getChildren().addAll(
            title, sub, new Pane(), 
            subjectSection, dateSection, descSection, attachmentSection,
            new Pane(), submitButton
        );
        
        setupLiveValidation();
        return form;
    }

    private void setupLiveValidation() {
        subjectField.textProperty().addListener((obs, oldT, newT) -> {
            if (newT.length() < SUBJECT_MIN_LENGTH) {
                subjectValidationLabel.setText("Too short (min 5 chars)");
                subjectValidationLabel.setTextFill(Color.web("#ef4444"));
            } else {
                subjectValidationLabel.setText("✓ Looks good");
                subjectValidationLabel.setTextFill(Color.web("#22c55e"));
            }
        });

        descriptionField.textProperty().addListener((obs, oldT, newT) -> {
            if (newT.length() < DESCRIPTION_MIN_LENGTH) {
                descriptionValidationLabel.setText("Too short (min 10 chars)");
                descriptionValidationLabel.setTextFill(Color.web("#ef4444"));
            } else {
                descriptionValidationLabel.setText("✓ Looks good");
                descriptionValidationLabel.setTextFill(Color.web("#22c55e"));
            }
        });
    }

    private Label createLiveValidationLabel(String message) {
        Label label = new Label(message);
        label.setTextFill(Color.web("rgba(255,255,255,0.4)"));
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 600;");
        return label;
    }

    private void handleSubmit() {
        String subject = subjectField.getText().trim();
        String description = descriptionField.getText().trim();

        if (subject.length() < SUBJECT_MIN_LENGTH || description.length() < DESCRIPTION_MIN_LENGTH) {
            showError("Validation Error", "Please fill in all fields correctly.");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showError("Error", "You must be logged in to submit a reclamation.");
            return;
        }

        String imagePath = null;
        if (!selectedFiles.isEmpty()) {
            try {
                imagePath = copyFileToUploads(selectedFiles.get(0));
            } catch (IOException e) {
                showError("Upload Error", "Failed to upload image.");
                return;
            }
        }

        Integer id = controller.reclamationCreate(subject, description, selectedDateTime, imagePath, currentUser);
        if (id > 0) {
            showSuccess("Success", "Reclamation submitted successfully.");
            clearForm();
        } else {
            showError("Failure", "Could not submit reclamation.");
        }
    }

    private void clearForm() {
        subjectField.clear();
        descriptionField.clear();
        selectedFiles.clear();
        previewPane.getChildren().clear();
        fileStatusText.setText("No files selected");
    }

    private void openFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Images");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png", "*.jpeg"));
        List<File> files = chooser.showOpenMultipleDialog(root.getScene().getWindow());
        if (files != null) {
            selectedFiles.addAll(files);
            updateFileStatus();
            for (File f : files) {
                ImageView iv = new ImageView(new Image(f.toURI().toString(), 60, 60, true, true));
                previewPane.getChildren().add(iv);
            }
        }
    }

    private void updateFileStatus() {
        fileStatusText.setText(selectedFiles.size() + " files selected");
    }

    private String copyFileToUploads(File source) throws IOException {
        String uploadsPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "reclamation_images";
        File dir = new File(uploadsPath);
        if (!dir.exists()) dir.mkdirs();
        String name = System.currentTimeMillis() + "_" + source.getName();
        Files.copy(source.toPath(), new File(dir, name).toPath(), StandardCopyOption.REPLACE_EXISTING);
        return "reclamation_images" + File.separator + name;
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }

    private void showSuccess(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.show();
    }

    private TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(44);
        field.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.4); -fx-background-radius: 14; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 14; -fx-padding: 12;");
        return field;
    }

    private Text label(String txt) {
        Text t = new Text(txt);
        t.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 13));
        t.setFill(Color.web("rgba(255,255,255,0.6)"));
        return t;
    }

    private Text line(String txt, int size, boolean bold, String color) {
        Text t = new Text(txt);
        t.setFont(Font.font(bold ? MainApplication.getInstance().getBoldFontFamily() : MainApplication.getInstance().getLightFontFamily(), bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        t.setFill(Color.web(color));
        return t;
    }

    @Override
    public VBox getRoot() {
        return root;
    }

    @Override
    public void cleanup() {}
}
