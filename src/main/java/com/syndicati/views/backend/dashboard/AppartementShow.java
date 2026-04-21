package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.residence.DetailAppartementController;
import com.syndicati.models.residence.Appartement;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.services.residence.ServiceMaintenance;
import com.syndicati.services.residence.ServiceResidence;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;
import java.sql.SQLDataException;

public class AppartementShow extends BaseDashboardPage {

    private Appartement appartement;
    private DetailAppartementController controller;


    ServiceResidence ServiceResidence= new ServiceResidence();
    ServiceMaintenance ServiceMaintenance= new ServiceMaintenance();

    public AppartementShow(Stage stage, Scene previousScene, Appartement appartement) {
        super(stage, previousScene);
        this.appartement= appartement;
        this.controller = new DetailAppartementController(stage, previousScene, appartement);
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

        deleteBtn.setOnAction(e -> controller.supprimerAppartementAction());

        Button editBtn = primaryButton("Edit Appartement");
        if (appartement != null) {
            editBtn.setOnAction(e -> new AppartementUpdate(stage, stage.getScene(), appartement.getId_app()).show());
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
            if (appartement.getImage_a() != null) {
                ImageView imageView = new ImageView();

                String imagePath = "uploads/residence_images/" + appartement.getImage_a();
                File imageFile = new File(imagePath);

                Image loadedImage = null;
                try {
                    if (imageFile.exists()) {
                        loadedImage = new Image(imageFile.toURI().toString());
                    } else {
                        File fallback = new File(appartement.getImage_a());
                        if (fallback.exists()) {
                            loadedImage = new Image(fallback.toURI().toString());
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Image load failed: " + ex.getMessage());
                }

                if (loadedImage != null) {
                    imageView.setImage(loadedImage);
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(150);
                    imageView.setPreserveRatio(true);

                    Label imageLabel = new Label("Image:");
                    imageLabel.setStyle("-fx-font-weight: bold;");

                    HBox imageRow = new HBox(10, imageLabel, imageView);
                    imageRow.setAlignment(Pos.CENTER_LEFT);
                    card.getChildren().add(imageRow);
                } else {
                    System.err.println("Image not found at: " + imagePath);
                    card.getChildren().add(detailRow("Image", "— (introuvable)"));
                }
            }

            card.getChildren().add(detailRow("Résidence", ServiceResidence.TrouverResidenceParId(appartement.getResidence_id()).getNom_r()));
            card.getChildren().add(detailRow("Type", appartement.getType_a()));
            card.getChildren().add(detailRow("ID Utilisateur", String.valueOf(appartement.getId_user())));
            card.getChildren().add(divider());

            card.getChildren().add(detailRow("Bloc", new JSONObject(appartement.getAppartement_info()).getString("bloc")));
            card.getChildren().add(detailRow("Etage", new JSONObject(appartement.getAppartement_info()).getString("floor")));
            card.getChildren().add(divider());

            card.getChildren().add(detailRow("Parking", appartement.getParking() == 1 ? "Disponible" : "Non Disponible"));
            card.getChildren().add(detailRow("Disponible", appartement.getDisponible() == 1 ? "Disponible" : "Non Disponible"));
            card.getChildren().add(divider());

            card.getChildren().add(detailRow("Superficie", appartement.getSuperficie() + " m²"));
            card.getChildren().add(detailRow("Prix Location", appartement.getPrix_location() + " TND"));
            card.getChildren().add(divider());
            System.out.println("ID DEP"+appartement.getId_app());
            System.out.println(appartement.toString());

            Maintenance maintenance = null;
            try {
                maintenance = ServiceMaintenance.MaintenanceParIdAppartement(appartement.getId_app());
            } catch (SQLDataException e) {
                throw new RuntimeException(e);
            }
            System.out.println(appartement.getId_app());
            card.getChildren().add(detailSection("Etat de l'appartement"));
            card.getChildren().add(detailSection("Maintenance"));

            if (maintenance != null) {
                GridPane maintenanceGrid = new GridPane();
                maintenanceGrid.setHgap(12);
                maintenanceGrid.setVgap(12);
                maintenanceGrid.setMaxWidth(Double.MAX_VALUE);

                ColumnConstraints col1 = new ColumnConstraints();
                col1.setPercentWidth(50);
                ColumnConstraints col2 = new ColumnConstraints();
                col2.setPercentWidth(50);
                maintenanceGrid.getColumnConstraints().addAll(col1, col2);

                maintenanceGrid.add(detailRow("État Générale",    maintenance.getEtat_app()),           0, 0);
                maintenanceGrid.add(detailRow("État Plomberie",   maintenance.getEtat_plomberie()),      1, 0);
                maintenanceGrid.add(detailRow("État Electricité", maintenance.getEtat_electricite()),    0, 1);
                maintenanceGrid.add(detailRow("État Chauffage",   maintenance.getEtat_chauffage()),      1, 1);

                card.getChildren().add(maintenanceGrid);
                card.getChildren().add(divider());

                card.getChildren().add(detailRow("Date de la dernière maintenance", maintenance.getDate_derniere_maintenance()));
                card.getChildren().add(detailRow("Description de la maintenance",   maintenance.getdescription_maint()));
                card.getChildren().add(detailRow("Recommendation IA",               maintenance.getRecommendation_ia()));
                card.getChildren().add(divider());
            } else {
                Text noMaint = new Text("Aucune maintenance enregistrée pour cet appartement.");
                noMaint.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
                noMaint.setFill(Color.web("#94a3b8"));
                card.getChildren().add(noMaint);
                card.getChildren().add(divider());
            }
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