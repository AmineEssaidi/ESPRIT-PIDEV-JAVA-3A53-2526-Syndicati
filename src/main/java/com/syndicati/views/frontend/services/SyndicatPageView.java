package com.syndicati.views.frontend.services;

import com.syndicati.MainApplication;
import com.syndicati.controllers.syndicat.ReclamationController;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.models.syndicat.Reclamation;
import com.syndicati.models.user.User;
import com.syndicati.utils.image.ImageLoaderUtil;
import com.syndicati.utils.image.imagekit.ImageKitConfig;
import com.syndicati.utils.image.imagekit.ImageKitStorageService;
import com.syndicati.utils.image.imagekit.ImageKitUploadResult;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.ui.HorizonDesignSystem;
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
import javafx.scene.control.DatePicker;
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
import javafx.scene.layout.Region;
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
import java.time.format.TextStyle;
import java.util.Locale;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Syndicat Page View with interactive reclamation form and premium UI.
 */
public class SyndicatPageView implements ViewInterface {

    private static final int SUBJECT_MIN_LENGTH = 10;
    private static final int SUBJECT_MAX_LENGTH = 255;

    private static final String IK_FOLDER_RECLAMATION_IMAGES = "/syndicati/reclamation_images";

    private boolean isUrl(String v) {
        return v != null && (v.startsWith("http://") || v.startsWith("https://"));
    }
    private static final int DESCRIPTION_MIN_LENGTH = 10;
    private static final int DESCRIPTION_MAX_LENGTH = 255;

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();
    private final ReclamationController controller = new ReclamationController();
    
    // Form fields
    private TextField subjectField;
    private DatePicker incidentDatePicker;
    private LocalDateTime selectedDateTime;
    private TextArea descriptionField;
    private Label subjectValidationLabel;
    private Label descriptionValidationLabel;
    private Button submitButton;
    private List<File> selectedFiles = new ArrayList<>();
    private Text fileStatusText;
    private FlowPane previewPane;

    // Calendar State
    private LocalDate calendarMonth = LocalDate.now().withDayOfMonth(1);
    private VBox calendarContainer;

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
        hero.setStyle(HorizonDesignSystem.webHeroPanel());

        VBox content = new VBox(12);
        content.setAlignment(Pos.CENTER_LEFT);

        Text badgeText = new Text("Syndicat Services");
        badgeText.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 12));
        badgeText.setFill(Color.WHITE);
        StackPane badge = new StackPane(badgeText);
        badge.setPadding(new Insets(7, 14, 7, 14));
        badge.setMaxWidth(StackPane.USE_PREF_SIZE);
        badge.setStyle(HorizonDesignSystem.webAccentBadge());

        Text title = line("Voice Your\nConcerns.", 72, true, "#ffffff");
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
        form.setPadding(new Insets(64, 64, 64, 64));
        form.setStyle(
            "-fx-background-color: rgba(255,255,255,0.02);" +
            "-fx-border-color: rgba(255,255,255,0.05);" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 40px;" +
            "-fx-border-radius: 40px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.50), 60, 0.20, 0, 30);"
        );

        Text title = line("Submit a Reclamation", 34, true, tm.getAccentHex());
        Text sub = line("Fill in the details below. We'll get back to you shortly.", 15, false, "rgba(255,255,255,0.5)");

        // Subject field with label
        VBox subjectSection = new VBox(6);
        subjectSection.getChildren().add(label("Subject"));
        subjectField = input("What is this regarding?");
        subjectValidationLabel = createLiveValidationLabel("Start typing your subject...");
        subjectSection.getChildren().addAll(subjectField, subjectValidationLabel);

        // Date field (Premium Glass Calendar)
        VBox dateSection = new VBox(12);
        dateSection.getChildren().add(label("Date of Incident"));
        
        // Host-like Banner for Syndicat context
        Button reportTrigger = buildReportTrigger();
        
        calendarContainer = buildGlassCalendar();
        dateSection.getChildren().addAll(reportTrigger, calendarContainer);

        // Description field with label
        VBox descSection = new VBox(4);
        descSection.getChildren().add(label("Description"));

        descriptionField = new TextArea();
        descriptionField.setPromptText("Please describe the issue in detail...");
        descriptionField.setPrefRowCount(6);
        descriptionField.setWrapText(true);
        descriptionField.setStyle(
            HorizonDesignSystem.webServiceInput(16, false) +
            "-fx-control-inner-background: rgba(0,0,0,0.20);" +
            "-fx-highlight-fill: " + tm.toRgba(tm.getAccentHex(), 0.45) + ";" +
            "-fx-highlight-text-fill: white;" +
            "-fx-font-size: 13px;"
        );
        descriptionField.setMinHeight(120);
        // TextArea has a nested '.content' region; style it too so it doesn't appear white.
        descriptionField.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                Platform.runLater(() -> {
                    var content = descriptionField.lookup(".content");
                    if (content != null) {
                        content.setStyle("-fx-background-color: rgba(0,0,0,0.20); -fx-background-radius: 14px;");
                    }
                });
            }
        });
        descSection.getChildren().add(descriptionField);
        descriptionValidationLabel = createLiveValidationLabel("Describe your issue with at least 10 characters...");
        descSection.getChildren().add(descriptionValidationLabel);

        // Attachments zone
        VBox attachmentSection = new VBox(6);
        attachmentSection.getChildren().add(label("Attachments"));

        VBox attachmentZone = new VBox(8);
        attachmentZone.setAlignment(Pos.CENTER);
        attachmentZone.setPrefHeight(100);
        attachmentZone.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.40) + "; -fx-border-width: 2; -fx-border-style: dashed; -fx-background-radius: 16; -fx-border-radius: 16; -fx-cursor: hand;");
        
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
        submitButton.setStyle(HorizonDesignSystem.buttonPrimary() + "-fx-font-size: 15px;-fx-font-weight: 900;-fx-background-radius:999px;-fx-border-radius:999px;-fx-padding: 18 34 18 34;");
        HorizonDesignSystem.installWebLift(submitButton);
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
                subjectValidationLabel.setText("Too short (min 10 chars)");
                subjectValidationLabel.setTextFill(Color.web("#ef4444"));
            } else if (!newT.isEmpty() && !Character.isLetter(newT.charAt(0))) {
                subjectValidationLabel.setText("Must start with a letter");
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
            } else if (!newT.isEmpty() && !Character.isLetter(newT.charAt(0))) {
                descriptionValidationLabel.setText("Must start with a letter");
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

        if (subject.length() < SUBJECT_MIN_LENGTH || (!subject.isEmpty() && !Character.isLetter(subject.charAt(0))) ||
            description.length() < DESCRIPTION_MIN_LENGTH || (!description.isEmpty() && !Character.isLetter(description.charAt(0)))) {
            showError("Validation Error", "Please fill in all fields correctly.\n- Both Subject & Description must be min 10 chars and start with a letter.");
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
        if (incidentDatePicker != null) {
            incidentDatePicker.setValue(LocalDate.now());
        }
        selectedDateTime = LocalDate.now().atStartOfDay();
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
        File dest = new File(dir, name);
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Prefer ImageKit URL for DB.
        try {
            ImageKitConfig cfg = ImageKitConfig.fromEnv();
            if (cfg != null && cfg.isEnabled() && cfg.getPrivateKey() != null) {
                ImageKitStorageService svc = new ImageKitStorageService(cfg);
                ImageKitUploadResult res = svc.uploadFile(dest, IK_FOLDER_RECLAMATION_IMAGES);
                if (res != null && res.url() != null && !res.url().isBlank()) {
                    return res.url();
                }
            }
        } catch (Exception e) {
            System.err.println("ImageKit upload failed (reclamation). Fallback to local: " + e.getMessage());
        }

        return "reclamation_images" + File.separator + name;
    }

    private void showError(String title, String msg) {
        if (title != null && title.toLowerCase().contains("validation")) {
            GlobalNotificationPillManager.validationIssue(
                title,
                msg,
                "Both Subject and Description need at least 10 characters.",
                "They must start with a letter.",
                "Avoid banned or offensive language."
            );
            return;
        }

        if (title != null && title.toLowerCase().contains("upload")) {
            GlobalNotificationPillManager.expandedError(
                title,
                msg,
                "Check that the image is still available on disk.",
                "Try a JPG or PNG file again.",
                "If it still fails, reselect the file."
            );
            return;
        }

        if (title != null && title.toLowerCase().contains("error")) {
            GlobalNotificationPillManager.expandedError(
                title,
                msg,
                "Make sure you are logged in.",
                "Verify all fields before retrying."
            );
            return;
        }

        GlobalNotificationPillManager.error(title, msg);
    }

    private void showSuccess(String title, String msg) {
        GlobalNotificationPillManager.success(title, msg);
    }

    private TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(52);
        field.setStyle(HorizonDesignSystem.webServiceInput(16, false));
        field.focusedProperty().addListener((obs, oldVal, focused) -> field.setStyle(HorizonDesignSystem.webServiceInput(16, focused)));
        return field;
    }

    private void applyEventLikeDatePickerStyle(DatePicker picker) {
        // Match Events page look-and-feel (rounded dark field + subtle border).
        picker.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-border-color: rgba(255,255,255,0.10);" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 16px;" +
            "-fx-border-radius: 16px;" +
            "-fx-text-fill: " + tm.getTextColor() + ";" +
            "-fx-padding: 10 12 10 12;"
        );

        Runnable skinPass = () -> {
            var editor = picker.lookup(".text-field");
            if (editor != null) {
                editor.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-control-inner-background: transparent;" +
                    "-fx-text-fill: " + tm.getTextColor() + ";" +
                    "-fx-prompt-text-fill: rgba(255,255,255,0.45);"
                );
            }
            var arrowButton = picker.lookup(".arrow-button");
            if (arrowButton != null) {
                arrowButton.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.08);" +
                    "-fx-background-radius: 0 14 14 0;"
                );
            }
            var arrow = picker.lookup(".arrow");
            if (arrow != null) {
                arrow.setStyle("-fx-background-color: " + tm.getTextColor() + ";");
            }
        };

        if (picker.getSkin() == null) {
            picker.skinProperty().addListener((obs, oldSkin, newSkin) -> {
                if (newSkin != null) Platform.runLater(skinPass);
            });
        } else {
            Platform.runLater(skinPass);
        }
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

    private Button buildReportTrigger() {
        Button btn = new Button();
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(HorizonDesignSystem.webServiceCard(24, false) + "-fx-padding: 20; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(HorizonDesignSystem.webServiceCard(24, true) + "-fx-padding: 20; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(HorizonDesignSystem.webServiceCard(24, false) + "-fx-padding: 20; -fx-cursor: hand;"));
        
        HBox content = new HBox(18);
        content.setAlignment(Pos.CENTER_LEFT);
        
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(54, 54);
        iconCircle.setMaxSize(54, 54);
        iconCircle.setStyle("-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.15) + "; -fx-background-radius: 18;");
        Text icon = line("!", 24, true, tm.getAccentHex());
        iconCircle.getChildren().add(icon);
        
        VBox texts = new VBox(2);
        texts.getChildren().addAll(
            line("Report an Incident", 18, true, "white"),
            line("Select the date of occurrence below", 13, false, "rgba(255,255,255,0.5)")
        );
        
        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        Text arrow = line(">", 18, true, "rgba(255,255,255,0.3)");
        
        content.getChildren().addAll(iconCircle, texts, s, arrow);
        btn.setGraphic(content);
        return btn;
    }

    private VBox buildGlassCalendar() {
        VBox wrap = new VBox(14);
        wrap.setPadding(new Insets(10, 0, 0, 0));

        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);
        
        Button prevBtn = iconButton("<");
        prevBtn.setOnAction(e -> {
            calendarMonth = calendarMonth.minusMonths(1);
            refreshCalendarUI();
        });
        
        Button nextBtn = iconButton(">");
        nextBtn.setOnAction(e -> {
            calendarMonth = calendarMonth.plusMonths(1);
            refreshCalendarUI();
        });
        
        String monthTitle = calendarMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + calendarMonth.getYear();
        Text monthLabel = line(monthTitle, 18, true, tm.getAccentHex());
        
        head.getChildren().addAll(monthLabel, spacer(), prevBtn, nextBtn);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(14.285);
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            grid.getColumnConstraints().add(cc);
        }

        String[] dayLabels = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
        for (int i = 0; i < dayLabels.length; i++) {
            Text lbl = line(dayLabels[i], 11, true, "rgba(255,255,255,0.4)");
            StackPane labelCell = new StackPane(lbl);
            labelCell.setMinHeight(20);
            grid.add(labelCell, i, 0);
        }

        updateCalendarGrid(grid);

        HBox tags = new HBox(8,
            tag("#Maintenance"),
            tag("#Security"),
            tag("#Cleaning"),
            tag("#Emergency")
        );
        tags.setPadding(new Insets(8, 0, 0, 0));

        wrap.getChildren().addAll(head, grid, tags);
        return wrap;
    }

    private void updateCalendarGrid(GridPane grid) {
        // Clear previous day cells (rows 1 to 6)
        grid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        LocalDate firstDay = calendarMonth.withDayOfMonth(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue(); // 1 (Mon) to 7 (Sun)
        int startOffset = dayOfWeek - 1;
        int daysInMonth = calendarMonth.lengthOfMonth();
        
        LocalDate today = LocalDate.now();
        int dayCounter = 1;
        
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 1 && col < startOffset) {
                    continue; // Empty cells before the first day
                }
                if (dayCounter > daysInMonth) {
                    break;
                }

                int currentDay = dayCounter;
                LocalDate date = calendarMonth.withDayOfMonth(currentDay);
                boolean isSelected = selectedDateTime != null && selectedDateTime.toLocalDate().equals(date);
                boolean isToday = date.equals(today);

                StackPane cell = new StackPane();
                cell.setMaxWidth(Double.MAX_VALUE);
                cell.setMinHeight(38);
                cell.setCursor(Cursor.HAND);
                
                String baseStyle = "-fx-background-radius: 12px; -fx-border-radius: 12px; -fx-border-width: 1px; ";
                if (isSelected) {
                    cell.setStyle(baseStyle + "-fx-background-color: " + tm.getEffectiveAccentGradient() + "; -fx-border-color: transparent; -fx-effect: dropshadow(gaussian, " + tm.toRgba(tm.getAccentHex(), 0.45) + ", 10, 0, 0, 2);");
                } else if (isToday) {
                    cell.setStyle(baseStyle + "-fx-background-color: " + surfaceSoft() + "; -fx-border-color: " + tm.getAccentHex() + "88;");
                } else {
                    cell.setStyle(baseStyle + "-fx-background-color: " + surfaceSoft() + "; -fx-border-color: transparent;");
                }

                Text d = line(String.valueOf(currentDay), 13, true, isSelected ? "white" : "rgba(255,255,255,0.8)");
                cell.getChildren().add(d);

                cell.setOnMouseClicked(e -> {
                    selectedDateTime = date.atStartOfDay();
                    refreshCalendarUI();
                });

                // Hover effect
                if (!isSelected) {
                    cell.setOnMouseEntered(e -> cell.setStyle(baseStyle + "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.2) + "; -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.3) + ";"));
                    cell.setOnMouseExited(e -> {
                        if (date.equals(today)) {
                            cell.setStyle(baseStyle + "-fx-background-color: " + surfaceSoft() + "; -fx-border-color: " + tm.getAccentHex() + "88;");
                        } else {
                            cell.setStyle(baseStyle + "-fx-background-color: " + surfaceSoft() + "; -fx-border-color: transparent;");
                        }
                    });
                }

                grid.add(cell, col, row);
                dayCounter++;
            }
            if (dayCounter > daysInMonth) break;
        }
    }

    private void refreshCalendarUI() {
        if (calendarContainer != null) {
            calendarContainer.getChildren().clear();
            VBox newContent = buildGlassCalendar();
            calendarContainer.getChildren().addAll(newContent.getChildren());
        }
    }

    private String surfaceCard() {
        return HorizonDesignSystem.surface();
    }

    private String surfaceSoft() {
        return HorizonDesignSystem.surfaceSoft();
    }

    private String borderSoft() {
        return HorizonDesignSystem.borderStrong();
    }

    private Button iconButton(String icon) {
        Button btn = new Button(icon);
        btn.setCursor(Cursor.HAND);
        btn.setStyle(HorizonDesignSystem.buttonGhost() + "-fx-padding: 5 10;");
        HorizonDesignSystem.installButtonMotion(btn);
        return btn;
    }

    private Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private StackPane tag(String label) {
        Text t = line(label, 10, true, "white");
        StackPane p = new StackPane(t);
        p.setPadding(new Insets(5, 10, 5, 10));
        p.setStyle(HorizonDesignSystem.webAccentBadge() + "-fx-background-radius: 8px; -fx-border-radius: 8px;");
        return p;
    }

    @Override
    public VBox getRoot() {
        return root;
    }

    @Override
    public void cleanup() {
        try {
            if (selectedFiles != null) selectedFiles.clear();
        } catch (Exception ignored) {}
        try {
            ImageLoaderUtil.clearCache();
        } catch (Exception ignored) {}
    }
}
