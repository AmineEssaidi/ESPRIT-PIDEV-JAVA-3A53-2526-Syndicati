package com.syndicati.controllers.residence;

import com.syndicati.models.residence.Appartement;
import com.syndicati.services.residence.ServiceAppartement;
import com.syndicati.services.residence.ServiceMaintenance;
import com.syndicati.views.backend.dashboard.AppartementUpdate;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class DetailAppartementController {
    private final Stage stage;
    private final Scene previousScene;

    Appartement Appartement = new Appartement();
    ServiceMaintenance ServiceMaintenance = new ServiceMaintenance();

    public DetailAppartementController(Stage stage, Scene previousScene, Appartement Appartement) {
        this.stage = stage;
        this.previousScene = previousScene;
        this.Appartement = Appartement;
    }

    public void supprimerAppartementAction() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer l'appartement ?");
        confirm.setContentText("Cette action supprimera définitivement l'appartement et son historique de maintenance.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                ServiceMaintenance serviceMaintenance = new ServiceMaintenance();
                serviceMaintenance.SupprimerParAppartement(Appartement.getId_app());

                ServiceAppartement serviceAppartement = new ServiceAppartement();
                serviceAppartement.Supprimer(Appartement);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Suppression réussie");
                alert.showAndWait();

                stage.setScene(previousScene);

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Échec de la suppression");
                alert.setContentText(e.getMessage());
                alert.show();
            }
        }
    }
    public void modifierAppartementAction() {
        new AppartementUpdate(stage, previousScene, Appartement.getId_app()).show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(message);
        alert.show();
    }

}
