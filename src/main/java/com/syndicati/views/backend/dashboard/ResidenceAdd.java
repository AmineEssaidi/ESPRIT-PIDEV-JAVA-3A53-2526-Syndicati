package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.backend.residence.AjouterResidenceController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class ResidenceAdd extends BaseDashboardPage {

    private TextField nomResidenceField;
    private TextField adresseField;
    private TextField nAppartementsField;
    private TextField dateAjoutField;
    private TextField nBlocsField;

    private Text nomError;
    private Text adresseError;
    private Text nAppartementsError;
    private Text dateAjoutError;
    private Text nBlocsError;

    public ResidenceAdd(Stage stage, Scene previousScene) {
        super(stage, previousScene);
    }

    @Override
    protected VBox buildContent() {
        nomResidenceField = new TextField();
        nomResidenceField.setPromptText("Nom de la résidence");
        nomError = new Text("");

        adresseField = new TextField();
        adresseField.setPromptText("Adresse de la résidence");
        adresseError = new Text("");

        nAppartementsField = new TextField();
        nAppartementsField.setPromptText("e.g. 24");
        nAppartementsError = new Text("");

        dateAjoutField = new TextField();
        dateAjoutField.setPromptText("Format: YYYY-MM-DD");
        dateAjoutError = new Text("");

        nBlocsField = new TextField();
        nBlocsField.setPromptText("Exemple: A,B,C");
        nBlocsError = new Text("");

        AjouterResidenceController controller = new AjouterResidenceController(
                stage, previousScene,
                nomResidenceField, adresseField, nAppartementsField, dateAjoutField, nBlocsField,
                nomError, adresseError, nAppartementsError, dateAjoutError, nBlocsError
        );

        VBox card = glassCard();
        card.getChildren().addAll(
                fieldGroup("Nom de la Résidence", nomResidenceField, nomError),
                fieldGroup("Adresse", adresseField, adresseError),
                fieldGroup("Nombre d'Appartements", nAppartementsField, nAppartementsError),
                fieldGroup("Date d'Ajout", dateAjoutField, dateAjoutError),
                fieldGroup("Nombre de Blocs", nBlocsField, nBlocsError)
        );

        Button submitBtn = primaryButton("Create Residence");
        submitBtn.setOnAction(e -> controller.ajouterResidenceAction());

        Button backBtn = secondaryButton("← Back");
        backBtn.setOnAction(e -> stage.setScene(previousScene));

        HBox actions = new HBox(12, backBtn, submitBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox form = new VBox(20, pageTitle("Add Residence"), card, actions);
        form.setFillWidth(true);
        return form;
    }
}