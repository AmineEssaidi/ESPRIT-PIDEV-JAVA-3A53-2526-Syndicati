package com.syndicati.controllers.residence;

import com.syndicati.models.residence.Appartement;
import com.syndicati.models.residence.Residence;
import com.syndicati.services.residence.ServiceAppartement;
import com.syndicati.services.residence.ServiceMaintenance;
import com.syndicati.services.residence.ServiceResidence;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.syndicati.views.backend.dashboard.ResidenceAdd;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class DetailResidenceController {
    private final Stage stage;
    private final Scene previousScene;
    private final String[] rowData;


    private final String residenceNom;

    public DetailResidenceController(Stage stage, Scene previousScene, String[] rowData, String residenceNom) {
        this.stage = stage;
        this.previousScene = previousScene;
        this.rowData = rowData;
        this.residenceNom = residenceNom;
    }
    public void supprimerResidenceAction() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer Résidence");
        confirm.setHeaderText("Êtes-vous sûr de vouloir supprimer cette résidence?");
        confirm.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        if (residenceNom == null) {
            showError("Résidence introuvable.");
            return;
        }

        ServiceResidence serviceResidence = new ServiceResidence();
        ServiceAppartement serviceAppartement = new ServiceAppartement();
        ServiceMaintenance serviceMaintenance = new ServiceMaintenance();

        try {
            Residence residence = serviceResidence.TrouverResidenceParNom(residenceNom);
            if (residence == null) {
                showError("Résidence introuvable.");
                return;
            }

            List<Appartement> appartements = serviceAppartement.AppartementsParResidence(residence);

            for (Appartement app : appartements) {
                serviceMaintenance.SupprimerParAppartement(app.getId_app());

                serviceAppartement.Supprimer(app);
            }

            serviceResidence.Supprimer(residence);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Supprimée");
            alert.setHeaderText("Résidence et toutes ses données associées supprimées avec succès!");
            alert.showAndWait();

            stage.setScene(previousScene);

        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }


    public void modifierResidenceAction() {
        new ResidenceAdd(stage, previousScene).show(); // swap with EditResidence when ready
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(message);
        alert.show();
    }

}
