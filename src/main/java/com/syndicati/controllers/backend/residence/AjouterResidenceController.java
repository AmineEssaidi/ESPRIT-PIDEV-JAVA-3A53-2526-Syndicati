package com.syndicati.controllers.backend.residence;

import com.syndicati.models.entities.Residence;
import com.syndicati.models.services.ServiceResidence;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.syndicati.views.backend.dashboard.ResidenceShow;

import java.sql.SQLException;

public class AjouterResidenceController {

    private final Stage stage;
    private final Scene previousScene;

    private final TextField nomResidenceField;
    private final TextField adresseField;
    private final TextField nAppartementsField;
    private final TextField dateAjoutField;
    private final TextField nBlocsField;

    private final Text nomError;
    private final Text adresseError;
    private final Text nAppartementsError;
    private final Text dateAjoutError;
    private final Text nBlocsError;
    public AjouterResidenceController(Stage stage, Scene previousScene,
                                      TextField nomResidenceField,
                                      TextField adresseField,
                                      TextField nAppartementsField,
                                      TextField dateAjoutField,
                                      TextField nBlocsField,
                                      Text nomError,
                                      Text adresseError,
                                      Text nAppartementsError,
                                      Text dateAjoutError,
                                      Text nBlocsError) {
        this.stage = stage;
        this.previousScene = previousScene;
        this.nomResidenceField = nomResidenceField;
        this.adresseField = adresseField;
        this.nAppartementsField = nAppartementsField;
        this.dateAjoutField = dateAjoutField;
        this.nBlocsField = nBlocsField;
        this.nomError = nomError;
        this.adresseError = adresseError;
        this.nAppartementsError = nAppartementsError;
        this.dateAjoutError = dateAjoutError;
        this.nBlocsError = nBlocsError;
    }
    public void ajouterResidenceAction() {
        nomError.setText("");
        adresseError.setText("");
        nAppartementsError.setText("");
        dateAjoutError.setText("");
        nBlocsError.setText("");

        String nom = nomResidenceField.getText();
        String adresse = adresseField.getText();
        String nAppartementsStr = nAppartementsField.getText();
        String dateAjout = dateAjoutField.getText();
        String nBlocs = nBlocsField.getText();

        Residence residence = new Residence();
        residence.setNom_r(nom);
        residence.setAdresse(adresse);
        residence.setImage_r(null);
        residence.setDate_ajout(dateAjout);
        residence.setN_appartements(parseIntSafe(nAppartementsStr, nAppartementsError));
        residence.setN_etages(2);
        residence.setN_blocs(nBlocs);

        ServiceResidence serviceResidence = new ServiceResidence();

        try {
            serviceResidence.Ajouter(residence);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Résidence Ajoutée");
            alert.setHeaderText("Résidence ajoutée avec succès!");
            alert.show();

            String[] rowData = {nom, adresse, nAppartementsStr, dateAjout, nBlocs};
            new ResidenceShow(stage, previousScene, rowData).show();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(e.getMessage());
            alert.show();
        }
    }

    private int parseIntSafe(String value, Text errorField) {
        if (value == null || value.isBlank() || value.equals("-")) {
            errorField.setText("This field must be a number.");
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            errorField.setText("Invalid number: \"" + value + "\"");
            return 0;
        }
    }
}