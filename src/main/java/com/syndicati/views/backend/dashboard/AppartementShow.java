package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.backend.residence.DetailAppartementController;
import com.syndicati.models.entities.Appartement;
import com.syndicati.models.services.ServiceAppartement;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class AppartementShow extends BaseDashboardPage {

    private final String[] rowData;
    private Appartement appartement;
    private DetailAppartementController controller;

    public AppartementShow(Stage stage, Scene previousScene, String[] rowData) {
        super(stage, previousScene);
        this.rowData = rowData;
        loadAppartement();
        this.controller = new DetailAppartementController(
                stage,
                previousScene,
                rowData,
                appartement
        );
    }

    private void loadAppartement() {
        try {
            ServiceAppartement serviceAppartement = new ServiceAppartement();
            appartement = serviceAppartement.TrouverAppartementParId(Integer.parseInt(rowData[0]));
        } catch (Exception e) {
            System.out.println("Error loading appartement: " + e.getMessage());
            appartement = null;
        }
    }

    @Override
    protected VBox buildContent() {
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Text title = pageTitle("Appartement Details");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = secondaryButton("← Back");
        backBtn.setOnAction(e -> stage.setScene(previousScene));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        deleteBtn.setPadding(new Insets(10, 20, 10, 20));
        deleteBtn.setStyle(
                "-fx-background-color:rgba(239,68,68,0.18);" +
                        "-fx-border-color:rgba(239,68,68,0.45);" +
                        "-fx-border-width:1;" +
                        "-fx-background-radius:100px;" +
                        "-fx-border-radius:100px;" +
                        "-fx-text-fill:#fecaca;" +
                        "-fx-cursor:hand;"
        );
        if (appartement != null) {
            deleteBtn.setOnAction(e -> controller.supprimerAppartementAction());
        } else {
            deleteBtn.setDisable(true);
        }

        Button editBtn = primaryButton("Edit Appartement");
        if (appartement != null) {
            editBtn.setOnAction(e -> new AppartementUpdate(stage, stage.getScene(), String.valueOf(appartement.getId_app())).show());
        } else {
            editBtn.setDisable(true);
        }

        titleRow.getChildren().addAll(title, spacer, backBtn, deleteBtn, editBtn);

        VBox card = glassCard();

        if (appartement == null) {
            Text notFound = new Text("Appartement not found.");
            notFound.setFont(Font.font(lightFont(), FontWeight.NORMAL, 14));
            notFound.setFill(Color.web("#ef4444"));
            card.getChildren().add(notFound);
        } else {
            card.getChildren().add(detailSection("General Info"));
            card.getChildren().add(detailRow("Résidence", appartement.getResidence()));
            card.getChildren().add(detailRow("Type", appartement.getType_a()));
            card.getChildren().add(detailRow("ID Utilisateur", String.valueOf(appartement.getId_User())));
            card.getChildren().add(divider());

            card.getChildren().add(detailSection("Localisation"));
            card.getChildren().add(detailRow("Bloc", appartement.getBloc()));
            card.getChildren().add(detailRow("Étage", String.valueOf(appartement.getEtage())));
            card.getChildren().add(divider());

            card.getChildren().add(detailSection("Disponibilité"));
            card.getChildren().add(detailRow("Parking", appartement.getParking() == 1 ? "Disponible" : "Non Disponible"));
            card.getChildren().add(detailRow("Disponible", appartement.getDisponible() == 1 ? "Disponible" : "Non Disponible"));
            card.getChildren().add(divider());

            card.getChildren().add(detailSection("Financier"));
            card.getChildren().add(detailRow("Superficie", appartement.getSuperficie() + " m²"));
            card.getChildren().add(detailRow("Prix Location", appartement.getPrix_location() + " TND"));
            card.getChildren().add(divider());

            card.getChildren().add(detailSection("Metadata"));
            card.getChildren().add(detailRow("Image", appartement.getImage_a() != null ? appartement.getImage_a() : "—"));
        }

        VBox form = new VBox(20, titleRow, card);
        form.setFillWidth(true);
        return form;
    }

    private Node detailSection(String label) {
        Text t = new Text(label.toUpperCase());
        t.setFont(Font.font(boldFont(), FontWeight.BOLD, 11));
        t.setFill(isDark() ? Color.web("rgba(255,255,255,0.35)") : Color.web("rgba(15,23,42,0.40)"));
        VBox wrap = new VBox(t);
        wrap.setPadding(new Insets(4, 0, 2, 0));
        return wrap;
    }

    private Node detailRow(String label, String value) {
        HBox row = new HBox();
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle(
                "-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.03)" : "rgba(15,23,42,0.03)") + ";" +
                        "-fx-background-radius:10px;" +
                        "-fx-border-color:" + (isDark() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.08)") + ";" +
                        "-fx-border-width:1;" +
                        "-fx-border-radius:10px;"
        );

        Text lbl = new Text(label);
        lbl.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        lbl.setFill(isDark() ? Color.web("rgba(255,255,255,0.45)") : Color.web("rgba(15,23,42,0.50)"));

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Text val = new Text(value != null ? value : "—");
        val.setFont(Font.font(boldFont(), FontWeight.BOLD, 13));
        val.setFill(isDark() ? Color.web("#f8fafc") : Color.web("#111827"));

        row.getChildren().addAll(lbl, sp, val);
        return row;
    }

    private Node divider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setMaxHeight(1);
        r.setStyle("-fx-background-color:" + (isDark() ? "rgba(255,255,255,0.07)" : "rgba(15,23,42,0.08)") + ";");
        VBox.setMargin(r, new Insets(4, 0, 4, 0));
        return r;
    }
}