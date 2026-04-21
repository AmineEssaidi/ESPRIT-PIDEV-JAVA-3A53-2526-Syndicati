package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.residence.DetailResidenceController;
import com.syndicati.controllers.residence.ModifierResidenceController;
import com.syndicati.models.residence.Residence;
import com.syndicati.services.residence.ServiceResidence;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class ResidenceShow extends BaseDashboardPage {

    private final String[] rowData;
    private Residence residence;
    DetailResidenceController controller = this.controller;
    TextField nomResidenceField = new TextField(residence != null ? residence.getNom_r() : "");
    TextField adresseField = new TextField(residence != null ? residence.getAdresse() : "");
    TextField nAppartementsField = new TextField(residence != null ? String.valueOf(residence.getN_appartements()) : "");
    TextField dateAjoutField = new TextField(residence != null ? residence.getDate_ajout() : "");
    TextField nBlocsField = new TextField(residence != null ? String.valueOf(residence.getN_blocs()) : "");
    Text nomError = new Text();
    Text adresseError = new Text();
    Text nAppartementsError = new Text();
    Text dateAjoutError = new Text();
    Text nBlocsError = new Text();

    ModifierResidenceController modifierController = new ModifierResidenceController(
            stage,
            previousScene,
            nomResidenceField,
            adresseField,
            nAppartementsField,
            dateAjoutField,
            nBlocsField,
            nomError,
            adresseError,
            nAppartementsError,
            dateAjoutError,
            nBlocsError,
            residence != null ? residence.getNom_r() : ""
    );

    public ResidenceShow(Stage stage, Scene previousScene, String[] rowData) {
        super(stage, previousScene);
        this.rowData = rowData;
        loadResidence();
        this.controller = new DetailResidenceController(
                stage,
                previousScene,
                rowData,
                residence != null ? residence.getNom_r() : null
        );
        System.out.println("Controller initialized: " + this.controller);
        System.out.println("Residence Name: " + (residence != null ? residence.getNom_r() : "null"));
    }

    private void loadResidence() {
        try {
            ServiceResidence serviceResidence = new ServiceResidence();
            residence = serviceResidence.TrouverResidenceParNom(rowData[0]);
            System.out.println("Loaded residence: " + residence);
        } catch (Exception e) {
            System.out.println("Error loading residence: " + e.getMessage());
            residence = null;
        }
    }
    @Override
    protected VBox buildContent() {
        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Text title = pageTitle("Residence Details");
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
        deleteBtn.setOnAction(e -> this.controller.supprimerResidenceAction());

        TextField nomRMAJChamp = new TextField(residence != null ? residence.getNom_r() : "");
        TextField adresseMAJChamp = new TextField(residence != null ? residence.getAdresse() : "");
        TextField nAppMAJChamp = new TextField(residence != null ? String.valueOf(residence.getN_appartements()) : "");
        TextField dateAjoutMAJChamp = new TextField(residence != null ? residence.getDate_ajout() : "");
        TextField nBlocsMAJChamp = new TextField(residence != null ? String.valueOf(residence.getN_blocs()) : "");
        Text nomError = new Text();
        Text adresseError = new Text();
        Text nAppartementsError = new Text();
        Text dateAjoutError = new Text();
        Text nBlocsError = new Text();

        ModifierResidenceController modifierController = new ModifierResidenceController(
                stage,
                previousScene,
                nomRMAJChamp,
                adresseMAJChamp,
                nAppMAJChamp,
                dateAjoutMAJChamp,
                nBlocsMAJChamp,
                nomError,
                adresseError,
                nAppartementsError,
                dateAjoutError,
                nBlocsError,
                residence != null ? residence.getNom_r() : ""
        );

        Button editBtn = primaryButton("Edit Residence");
        editBtn.setOnAction(e -> new ResidenceUpdate(stage, stage.getScene(), residence.getNom_r()).show());

        titleRow.getChildren().addAll(title, spacer, backBtn, deleteBtn, editBtn);

        VBox card = glassCard();

        if (residence == null) {
            Text notFound = new Text("Residence not found.");
            notFound.setFont(Font.font(lightFont(), FontWeight.NORMAL, 14));
            notFound.setFill(Color.web("#ef4444"));
            card.getChildren().add(notFound);
        } else {
            card.getChildren().add(detailSection("General Info"));
            card.getChildren().add(detailRow("Nom", residence.getNom_r()));
            card.getChildren().add(detailRow("Adresse", residence.getAdresse()));
            card.getChildren().add(divider());
            card.getChildren().add(detailSection("Properties"));
            card.getChildren().add(detailRow("Appartements", String.valueOf(residence.getN_appartements())));
            card.getChildren().add(detailRow("Blocs", String.valueOf(residence.getN_blocs())));
            card.getChildren().add(detailRow("Étages", String.valueOf(residence.getN_etages())));
            card.getChildren().add(divider());
            card.getChildren().add(detailSection("Metadata"));
            card.getChildren().add(detailRow("Date d'Ajout", residence.getDate_ajout()));
            card.getChildren().add(detailRow("Image", residence.getImage_r() != null ? residence.getImage_r() : "—"));
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