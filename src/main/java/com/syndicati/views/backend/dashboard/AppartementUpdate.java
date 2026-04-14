package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.backend.residence.ModifierAppartementController;
import com.syndicati.models.entities.Appartement;
import com.syndicati.models.services.ServiceAppartement;
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

public class AppartementUpdate extends BaseDashboardPage {

    private TextField imageAChamp;
    private ComboBox<String> residenceChamp;
    private TextField IDUtilisateurChamp;
    private ComboBox<String> parkingChamp;
    private ComboBox<String> disponibleChamp;
    private ComboBox<String> typeAChamp;
    private TextField blocChamp;
    private TextField etageChamp;
    private TextField superficieChamp;
    private TextField prixLocationChamp;

    private Text imageAChampErreur;
    private Text IDUtilisateurChampErreur;
    private Text superficieChampErreur;
    private Text prixLocationChampErreur;

    private final String idAppartement;

    public AppartementUpdate(Stage stage, Scene previousScene, String idAppartement) {
        super(stage, previousScene);
        this.idAppartement = idAppartement;
    }

    @Override
    protected VBox buildContent() {
        Appartement appartement = null;
        try {
            ServiceAppartement serviceAppartement = new ServiceAppartement();
            appartement = serviceAppartement.TrouverAppartementParId(Integer.parseInt(idAppartement));
        } catch (Exception e) {
            System.out.println("Error loading appartement: " + e.getMessage());
        }

        imageAChamp = new TextField(appartement != null ? appartement.getImage_a() : "");
        imageAChamp.setPromptText("URL ou chemin de l'image");
        imageAChampErreur = new Text("");

        IDUtilisateurChamp = new TextField(appartement != null ? String.valueOf(appartement.getId_user()) : "");
        IDUtilisateurChamp.setPromptText("ID de l'utilisateur");
        IDUtilisateurChampErreur = new Text("");

        blocChamp = new TextField(appartement != null ? extractJsonField(appartement.getAppartement_info(), "bloc"): "");
        blocChamp.setPromptText("Ex: A, B, C");

        etageChamp = new TextField(appartement != null ? extractJsonField(appartement.getAppartement_info(), "number"): "");
        etageChamp.setPromptText("Ex: 1, 2, 3");

        superficieChamp = new TextField(appartement != null ? String.valueOf(appartement.getSuperficie()) : "");
        superficieChamp.setPromptText("En m²");
        superficieChampErreur = new Text("");

        prixLocationChamp = new TextField(appartement != null ? String.valueOf(appartement.getPrix_location()) : "");
        prixLocationChamp.setPromptText("En TND");
        prixLocationChampErreur = new Text("");

        residenceChamp = new ComboBox<>();
        residenceChamp.getItems().addAll(loadResidenceNames());
        residenceChamp.setValue(appartement != null ? appartement.getResidence() : null);
        residenceChamp.setPromptText("Sélectionner une résidence");

        typeAChamp = new ComboBox<>();
        typeAChamp.getItems().addAll("S+0", "S+1", "S+2", "S+3", "S+4", "S+5");
        typeAChamp.setValue(appartement != null ? appartement.getType_a() : null);
        typeAChamp.setPromptText("Type d'appartement");

        parkingChamp = new ComboBox<>();
        parkingChamp.getItems().addAll("Disponible", "Non Disponible");
        parkingChamp.setValue(appartement != null ? (appartement.getParking() == 1 ? "Disponible" : "Non Disponible") : null);
        parkingChamp.setPromptText("Parking");

        disponibleChamp = new ComboBox<>();
        disponibleChamp.getItems().addAll("Disponible", "Non Disponible");
        disponibleChamp.setValue(appartement != null ? (appartement.getDisponible() == 1 ? "Disponible" : "Non Disponible") : null);
        disponibleChamp.setPromptText("Disponibilité");

        ModifierAppartementController controller = new ModifierAppartementController(
                stage,
                previousScene,
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
                prixLocationChampErreur,
                idAppartement
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

        Button boutonModifier = primaryButton("Update Appartement");
        boutonModifier.setOnAction(e -> controller.modifierAppartementAction());

        Button boutonRetourner = secondaryButton("← Back");
        boutonRetourner.setOnAction(e -> stage.setScene(previousScene));

        HBox actions = new HBox(12, boutonRetourner, boutonModifier);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox form = new VBox(20, pageTitle("Update Appartement"), card, actions);
        form.setFillWidth(true);
        return form;
    }

    private String extractJsonField(String json, String field) {
        try {
            String key = "\"" + field + "\": \"";
            int start = json.indexOf(key) + key.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "";
        }
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