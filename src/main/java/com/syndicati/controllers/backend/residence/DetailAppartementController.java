package com.syndicati.controllers.backend.residence;

import com.syndicati.models.entities.Appartement;
import com.syndicati.models.entities.Residence;
import com.syndicati.models.services.ServiceAppartement;
import com.syndicati.models.services.ServiceResidence;
import com.syndicati.views.backend.dashboard.AppartementAdd;
import com.syndicati.views.backend.dashboard.ResidenceAdd;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class DetailAppartementController {
    private final Stage stage;
    private final Scene previousScene;
    private final String[] rowData;

    Appartement Appartement = new Appartement();

    public DetailAppartementController(Stage stage, Scene previousScene, String[] rowData, Appartement Appartement) {
        this.stage = stage;
        this.previousScene = previousScene;
        this.rowData = rowData;
        this.Appartement = Appartement;
    }
    public void supprimerResidenceAction() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer Résidence");
        confirm.setHeaderText("Êtes-vous sûr de vouloir supprimer cette résidence?");
        confirm.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;


        ServiceAppartement serviceAppartement = new ServiceAppartement();
        try {

            serviceAppartement.Supprimer(Appartement);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Supprimée");
            alert.setHeaderText("Résidence supprimée avec succès!");
            alert.show();

            stage.setScene(previousScene);

        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    public void modifierAppartementAction() {
        new AppartementAdd(stage, previousScene).show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(message);
        alert.show();
    }

}
