package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.residence.AjouterResidenceController;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

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
    private Text ImageText;
    private Text ImageErreur;
    private File imageR=null;

    public ResidenceAdd(Stage stage, Scene previousScene) {
        super(stage, previousScene);
    }

    @Override
    protected VBox buildContent() {

        Button imageButton = new Button("Choisir une image");
        ImageText = new Text("Aucune image choisie");
        ImageErreur = new Text("");

        imageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                imageR = selectedFile;
                ImageText.setText(selectedFile.getName());
            }
        });


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
        VBox imageGroup = new VBox(5, imageButton, ImageText, ImageErreur);




        Button submitBtn = primaryButton("Create Residence");
        submitBtn.setOnAction(e -> {
            controller.setImageR(imageR);
            controller.ajouterResidenceAction();
        });

        Button backBtn = secondaryButton("← Back");
        backBtn.setOnAction(e -> stage.setScene(previousScene));

        HBox actions = new HBox(12, backBtn, submitBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox form = new VBox(20, pageTitle("Add Residence"), card, actions);
        form.setFillWidth(true);
        return form;
    }
}