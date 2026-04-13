package com.syndicati.views.frontend.services;

import com.syndicati.MainApplication;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.models.entities.Participation;
import com.syndicati.models.services.ParticipationService;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.session.SessionManager;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParticipationsPageView implements ViewInterface {

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();
    private final ParticipationService participationService;
    private GridPane participationsGrid;

    public ParticipationsPageView() {
        this.participationService = new ParticipationService();

        root = new VBox(26);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20, 0, 44, 0));
        root.setStyle("-fx-background-color: transparent;");

        refreshContent();
    }

    private void refreshContent() {
        root.getChildren().clear();
        root.getChildren().addAll(
            buildHeaderSection(),
            buildParticipationsSection()
        );
    }

    private StackPane buildHeaderSection() {
        StackPane header = sectionShell(48, new Insets(80, 64, 80, 64), surfaceStrong(), borderSoft());
        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().add(sectionPill("My Journey"));
        Text title = text("My Event Participations", 48, true, tm.getAccentHex());
        Text subtitle = text("Manage your upcoming community experiences and gatherings.", 18, false, textMuted());

        content.getChildren().addAll(title, subtitle);
        header.getChildren().add(content);
        return header;
    }

    private VBox buildParticipationsSection() {
        VBox section = new VBox(24);
        section.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        section.setPadding(new Insets(0, 48, 0, 48));

        int currentUserId = SessionManager.getInstance().getCurrentUser().getIdUser();
        List<Participation> all = participationService.getAllParticipations();
        List<Participation> userParticipations = all.stream()
                .filter(p -> p.getUser().getIdUser().equals(currentUserId))
                .collect(Collectors.toList());

        if (userParticipations.isEmpty()) {
            VBox empty = new VBox(20);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(100));
            empty.getChildren().addAll(
                text("You haven't joined any events yet.", 20, true, textSoft()),
                text("Explore the community and join your first event!", 14, false, textMuted())
            );
            section.getChildren().add(empty);
            return section;
        }

        participationsGrid = new GridPane();
        participationsGrid.setHgap(34);
        participationsGrid.setVgap(34);
        
        rebuildGrid(participationsGrid, userParticipations);

        section.getChildren().add(participationsGrid);
        return section;
    }

    private void rebuildGrid(GridPane grid, List<Participation> items) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();

        int cols = 2; // For participations we stick to 2 to show more details
        for (int i = 0; i < cols; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(100.0 / cols);
            c.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(c);
        }

        for (int i = 0; i < items.size(); i++) {
            VBox card = participationCard(items.get(i));
            grid.add(card, i % cols, i / cols);
        }
    }

    private VBox participationCard(Participation p) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: " + surfaceCard() + ";" +
            "-fx-background-radius: 24px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 24px;"
        );

        HBox top = new HBox(12);
        top.setAlignment(Pos.TOP_LEFT);
        
        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER);
        dateBox.setPadding(new Insets(8, 12, 8, 12));
        dateBox.setStyle("-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.12) + "; -fx-background-radius: 12px;");
        Text day = text(p.getEvenement().getDateEvent().getDayOfMonth() + "", 20, true, tm.getAccentHex());
        Text month = text(p.getEvenement().getDateEvent().getMonth().toString().substring(0, 3), 11, true, tm.getAccentHex());
        dateBox.getChildren().addAll(day, month);

        VBox titleArea = new VBox(4);
        Text title = text(p.getEvenement().getTitreEvent(), 20, true, tm.getTextColor());
        Text lieu = text(p.getEvenement().getLieuEvent(), 13, false, textMuted());
        titleArea.getChildren().addAll(title, lieu);

        top.getChildren().addAll(dateBox, titleArea);

        HBox info = new HBox(20);
        info.getChildren().addAll(
            statItem("Statut", p.getStatutParticipation().toUpperCase(), tm.getAccentHex()),
            statItem("Accompagnants", String.valueOf(p.getNbAccompagnants()), textSoft())
        );

        Text comment = text(p.getCommentaireParticipation() != null && !p.getCommentaireParticipation().isEmpty() 
            ? "\"" + p.getCommentaireParticipation() + "\"" : "No comment added.", 13, false, textMuted());
        comment.setWrappingWidth(400);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button editBtn = iconButton("Edit Details");
        editBtn.setOnAction(e -> showEditForm(p));
        Button cancelBtn = iconButton("Cancel Registration");
        cancelBtn.setStyle(cancelBtn.getStyle() + "-fx-text-fill: #ff4d4d;");
        cancelBtn.setOnAction(e -> handleCancel(p));

        actions.getChildren().addAll(editBtn, cancelBtn);

        card.getChildren().addAll(top, info, comment, spacer, actions);
        addHoverLift(card);
        return card;
    }

    private VBox statItem(String label, String value, String color) {
        VBox v = new VBox(2);
        v.getChildren().addAll(
            text(label, 10, true, textMuted()),
            text(value, 14, true, color)
        );
        return v;
    }

    private void showEditForm(Participation p) {
        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(40));
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-background-radius: 32px;");

        VBox form = new VBox(15);
        form.setMinWidth(450);
        form.setPadding(new Insets(34));
        form.setStyle("-fx-background-color: " + surfaceStrong().split(";")[0] + "; -fx-background-radius: 20px; -fx-border-color: " + borderSoft() + "; -fx-border-radius: 20px;");

        Text titleText = text("Edit Participation", 26, true, tm.getAccentHex());

        TextField companionsField = new TextField(String.valueOf(p.getNbAccompagnants()));
        applyInputStyle(companionsField);
        VBox companionsBox = new VBox(6, text("Number of companions", 11, true, textSoft()), companionsField);

        TextArea commentArea = new TextArea(p.getCommentaireParticipation());
        commentArea.setPrefRowCount(3);
        applyInputStyle(commentArea);
        VBox commentBox = new VBox(6, text("Comment", 11, true, textSoft()), commentArea);

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = iconButton("Back");
        Button confirm = gradientButton("Save Changes", 13, new Insets(10, 20, 10, 20));
        actions.getChildren().addAll(cancel, confirm);

        form.getChildren().addAll(titleText, companionsBox, commentBox, actions);
        overlay.getChildren().add(form);

        StackPane container = (StackPane) root.getParent();
        container.getChildren().add(overlay);

        cancel.setOnAction(ev -> container.getChildren().remove(overlay));
        confirm.setOnAction(ev -> {
            try {
                int nb = Integer.parseInt(companionsField.getText().trim());
                if (nb < 0) throw new NumberFormatException();

                // Note: Complex validation (adjusting seats) could be added here.
                // For now, we update the personal record as per simple CRUD.
                p.setNbAccompagnants(nb);
                p.setCommentaireParticipation(commentArea.getText());
                if (participationService.updateParticipation(p)) {
                    showSuccessAlert("Changes Saved", "Your participation details have been updated.");
                    container.getChildren().remove(overlay);
                    refreshContent();
                } else {
                    showErrorAlert("Update Failed", "Could not save changes. Please try again.");
                }
            } catch (NumberFormatException ex) {
                showErrorAlert("Invalid Input", "Please enter a valid positive number.");
            }
        });
    }

    private void handleCancel(Participation p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Registration");
        alert.setHeaderText("Cancel participation for: " + p.getEvenement().getTitreEvent());
        alert.setContentText("Are you sure? This will restore " + (1 + p.getNbAccompagnants()) + " available seats for this event.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (participationService.cancelParticipation(p.getIdParticipation())) {
                showSuccessAlert("Participation Cancelled", "You have successfully withdrawn from the event.");
                refreshContent();
            } else {
                showErrorAlert("Cancellation Failed", "Could not process your withdrawal. Please contact support.");
            }
        }
    }

    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-background-color: " + surfaceStrong().split(";")[0] + "; -fx-text-fill: white;");
        alert.show();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-background-color: " + surfaceStrong().split(";")[0] + "; -fx-text-fill: white;");
        alert.show();
    }

    // Helper methods (cloned from EvenementPageView for consistency)
    private StackPane sectionShell(double radius, Insets padding, String bg, String border) {
        StackPane pane = new StackPane();
        pane.setPadding(padding);
        pane.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: " + radius + "px; -fx-border-color: " + border + "; -fx-border-width: 1px; -fx-border-radius: " + radius + "px;");
        return pane;
    }

    private StackPane sectionPill(String value) {
        StackPane pill = new StackPane(text(value, 11, true, "#ffffff"));
        pill.setPadding(new Insets(8, 14, 8, 14));
        pill.setMaxWidth(StackPane.USE_PREF_SIZE);
        pill.setStyle("-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.12) + "; -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.25) + "; -fx-border-width: 1px; -fx-background-radius: 100px; -fx-border-radius: 100px;");
        return pill;
    }

    private Text text(String value, int size, boolean bold, String color) {
        Text t = new Text(value);
        t.setFont(Font.font(bold ? MainApplication.getInstance().getBoldFontFamily() : MainApplication.getInstance().getLightFontFamily(), bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        t.setFill(Color.web(color));
        return t;
    }

    private Button gradientButton(String label, int fontSize, Insets padding) {
        Button b = new Button(label);
        b.setStyle("-fx-background-color: " + tm.getEffectiveAccentGradient() + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: " + fontSize + "px; -fx-background-radius: 14px;");
        b.setPadding(padding);
        return b;
    }

    private Button iconButton(String label) {
        Button b = new Button(label);
        b.setStyle("-fx-background-color: " + surfaceSoft() + "; -fx-border-color: " + borderSoft() + "; -fx-border-width: 1px; -fx-background-radius: 10px; -fx-border-radius: 10px; -fx-text-fill: " + textSoft() + "; -fx-font-weight: 700;");
        return b;
    }

    private void applyInputStyle(TextInputControl field) {
        field.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333333; -fx-border-width: 1px; -fx-background-radius: 12px; -fx-border-radius: 12px; -fx-text-fill: white; -fx-prompt-text-fill: #8a8a8a; -fx-padding: 10px 14px;");
    }

    private String surfaceStrong() { return tm.isDarkMode() ? "linear-gradient(from 0% 0% to 100% 100%, #020202 0%, #070707 58%, #0b0b0b 100%)" : "linear-gradient(from 0% 0% to 100% 100%, #ffffff 0%, #f8fafc 100%)"; }
    private String surfaceCard() { return tm.isDarkMode() ? "linear-gradient(from 0% 0% to 100% 100%, rgba(10,10,10,0.93) 0%, rgba(14,14,14,0.93) 62%, " + tm.toRgba(tm.getAccentHex(), 0.10) + " 100%)" : "linear-gradient(from 0% 0% to 100% 100%, rgba(255,255,255,0.98) 0%, rgba(248,250,252,0.96) 100%)"; }
    private String surfaceSoft() { return tm.isDarkMode() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.06)"; }
    private String borderSoft() { return tm.isDarkMode() ? tm.toRgba(tm.getAccentHex(), 0.34) : "rgba(15,23,42,0.16)"; }
    private String textSoft() { return tm.isDarkMode() ? "rgba(255,255,255,0.92)" : "rgba(15,23,42,0.90)"; }
    private String textMuted() { return tm.isDarkMode() ? "rgba(255,255,255,0.78)" : "rgba(30,41,59,0.82)"; }

    private void addHoverLift(Node node) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(220), node);
            st.setToX(1.01); st.setToY(1.01); st.play();
        });
        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(220), node);
            st.setToX(1); st.setToY(1); st.play();
        });
    }

    @Override
    public VBox getRoot() { return root; }
    @Override
    public void cleanup() {}
}
