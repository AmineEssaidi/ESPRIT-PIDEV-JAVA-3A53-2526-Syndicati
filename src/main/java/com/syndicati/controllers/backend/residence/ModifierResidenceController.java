package com.syndicati.controllers.backend.residence;

import com.syndicati.models.entities.Residence;
import com.syndicati.models.services.ServiceResidence;
import com.syndicati.views.backend.dashboard.ResidenceShow;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.SQLException;

public class ModifierResidenceController {

    private final Stage stage;
    private final Scene previousScene;

    private final TextField nomRMAJChamp;
    private final TextField adresseMAJChamp;
    private final TextField nAppMAJChamp;
    private final TextField dateAjoutMAJChamp;
    private final TextField nBlocsMAJChamp;

    private final Text nomError;
    private final Text adresseError;
    private final Text nAppartementsError;
    private final Text dateAjoutError;
    private final Text nBlocsError;
    private String nomResidence;
    public ModifierResidenceController(Stage stage, Scene previousScene,
                                       TextField nomRMAJChamp,
                                       TextField adresseMAJChamp,
                                       TextField nAppMAJChamp,
                                       TextField dateAjoutMAJChamp,
                                       TextField nBlocsMAJChamp,
                                       Text nomError,
                                       Text adresseError,
                                       Text nAppartementsError,
                                       Text dateAjoutError,
                                       Text nBlocsError,
                                        String nomResidence) {
        this.stage = stage;
        this.previousScene = previousScene;
        this.nomRMAJChamp = nomRMAJChamp;
        this.adresseMAJChamp = adresseMAJChamp;
        this.nAppMAJChamp = nAppMAJChamp;
        this.dateAjoutMAJChamp = dateAjoutMAJChamp;
        this.nBlocsMAJChamp = nBlocsMAJChamp;
        this.nomError = nomError;
        this.adresseError = adresseError;
        this.nAppartementsError = nAppartementsError;
        this.dateAjoutError = dateAjoutError;
        this.nBlocsError = nBlocsError;
        this.nomResidence= nomResidence;
    }
    public void ModifierResidence() {
        nomError.setText("");
        adresseError.setText("");
        nAppartementsError.setText("");
        dateAjoutError.setText("");
        nBlocsError.setText("");
        Residence residence = new Residence();

        ServiceResidence serviceResidence = new ServiceResidence();
        residence.setId_residence(serviceResidence.TrouverResidenceParNom(nomResidence).getId_residence());

        String nom = nomRMAJChamp.getText();
        String adresse = adresseMAJChamp.getText();
        String nAppartementsStr = nAppMAJChamp.getText();
        String dateAjout = dateAjoutMAJChamp.getText();
        String nBlocs = nBlocsMAJChamp.getText();

        residence.setNom_r(nom);
        residence.setAdresse(adresse);
        residence.setImage_r(null);
        residence.setDate_ajout(dateAjout);
        residence.setN_appartements(parseIntSafe(nAppartementsStr, nAppartementsError));
        residence.setN_etages(2);
        residence.setN_blocs(nBlocs);




        try {
            serviceResidence.Modifier(residence);

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