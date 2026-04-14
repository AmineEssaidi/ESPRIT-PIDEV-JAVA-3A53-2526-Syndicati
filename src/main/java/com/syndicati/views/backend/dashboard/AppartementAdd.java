package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.backend.residence.AjouterAppartementController;
import com.syndicati.models.services.ServiceResidence;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AppartementAdd extends BaseDashboardPage {

    TextField imageAChamp = new TextField();
    ComboBox<String> residenceChamp = new ComboBox<>();
    TextField IDUtilisateurChamp = new TextField();
    ComboBox<String> parkingChamp = new ComboBox<>();
    ComboBox<String> disponibleChamp = new ComboBox<>();
    ComboBox<String> typeAChamp = new ComboBox<>();
    TextField blocChamp = new TextField();
    TextField etageChamp = new TextField();
    TextField superficieChamp = new TextField();
    TextField prixLocationChamp = new TextField();

    Text imageAChampErreur = new Text();
    Text IDUtilisateurChampErreur = new Text();
    Text superficieChampErreur = new Text();
    Text prixLocationChampErreur = new Text();

    public AppartementAdd(Stage stage, Scene previousScene) {
        super(stage, previousScene);
    }

    @Override
    protected VBox buildContent() {
        imageAChamp.setPromptText("URL ou chemin de l'image");
        IDUtilisateurChamp.setPromptText("ID de l'utilisateur");
        blocChamp.setPromptText("Ex: A, B, C");
        etageChamp.setPromptText("Ex: 1, 2, 3");
        superficieChamp.setPromptText("En m²");
        prixLocationChamp.setPromptText("En TND");

        residenceChamp.getItems().addAll(loadResidenceNames());
        residenceChamp.setPromptText("Sélectionner une résidence");

        typeAChamp.getItems().addAll("S+0", "S+1", "S+2", "S+3", "S+4", "S+5");
        typeAChamp.setPromptText("Type d'appartement");

        parkingChamp.getItems().addAll("Disponible", "Non Disponible");
        parkingChamp.setPromptText("Parking");

        disponibleChamp.getItems().addAll("Disponible", "Non Disponible");
        disponibleChamp.setPromptText("Disponibilité");

        AjouterAppartementController controller= new AjouterAppartementController(
                stage, previousScene,
                imageAChamp,
                residenceChamp,
                IDUtilisateurChamp,
                parkingChamp,
                disponibleChamp,
                typeAChamp,
                blocChamp,
                etageChamp,
                superficieChamp,
                prixLocationChamp,
                imageAChampErreur,
                IDUtilisateurChampErreur,
                superficieChampErreur,
                prixLocationChampErreur
        );

        VBox card = glassCard();
        card.getChildren().addAll(
                fieldGroup("Image", imageAChamp, imageAChampErreur),
                fieldGroup("Résidence", residenceChamp),
                fieldGroup("ID Utilisateur", IDUtilisateurChamp, IDUtilisateurChampErreur),
                fieldGroup("Type", typeAChamp),
                fieldGroup("Bloc", blocChamp),
                fieldGroup("Étage", etageChamp),
                fieldGroup("Superficie", superficieChamp, superficieChampErreur),
                fieldGroup("Prix Location", prixLocationChamp, prixLocationChampErreur),
                fieldGroup("Parking", parkingChamp),
                fieldGroup("Disponible", disponibleChamp)
        );

        Button submitBtn = primaryButton("Ajouter Appartement");
        submitBtn.setOnAction(e -> controller.ajouterAppartementAction());

        Button backBtn = secondaryButton("← Back");
        backBtn.setOnAction(e -> stage.setScene(previousScene));

        HBox actions = new HBox(12, backBtn, submitBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox form = new VBox(20, pageTitle("Add Appartement"), card, actions);
        form.setFillWidth(true);
        return form;
    }

    private String[] loadResidenceNames() {
        try {
            ServiceResidence sr = new ServiceResidence();
            return sr.getTousLesNoms().toArray(new String[0]);
        } catch (Exception e) {
            System.out.println("Error loading residences: " + e.getMessage());
            return new String[]{};
        }
    }
}