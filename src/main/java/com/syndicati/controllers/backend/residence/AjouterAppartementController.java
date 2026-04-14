package com.syndicati.controllers.backend.residence;

import com.syndicati.models.entities.Appartement;
import com.syndicati.models.entities.Residence;
import com.syndicati.models.services.ServiceAppartement;
import com.syndicati.models.services.ServiceResidence;
import com.syndicati.views.backend.dashboard.AppartementShow;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AjouterAppartementController {

    private final Stage stage;
    private final Scene previousScene;

    private final TextField imageAChamp;
    private final ComboBox<String> residenceChamp;
    private final TextField IDUtilisateurChamp;
    private final ComboBox<String> parkingChamp;
    private final ComboBox<String> disponibleChamp;
    private final ComboBox<String> typeAChamp;
    private final TextField blocChamp;
    private final TextField etageChamp;
    private final TextField superficieChamp;
    private final TextField prixLocationChamp;

    private final Text imageAChampErreur;
    private final Text IDUtilisateurChampErreur;
    private final Text superficieChampErreur;
    private final Text prixLocationChampErreur;

    public AjouterAppartementController(Stage stage, Scene previousScene,
                                        TextField imageAChamp,
                                        ComboBox<String> residenceChamp,
                                        TextField IDUtilisateurChamp,
                                        ComboBox<String> parkingChamp,
                                        ComboBox<String> disponibleChamp,
                                        ComboBox<String> typeAChamp,
                                        TextField blocChamp,
                                        TextField etageChamp,
                                        TextField superficieChamp,
                                        TextField prixLocationChamp,
                                        Text imageAChampErreur,
                                        Text IDUtilisateurChampErreur,
                                        Text superficieChampErreur,
                                        Text prixLocationChampErreur) {
        this.stage = stage;
        this.previousScene = previousScene;
        this.imageAChamp = imageAChamp;
        this.residenceChamp = residenceChamp;
        this.IDUtilisateurChamp = IDUtilisateurChamp;
        this.parkingChamp = parkingChamp;
        this.disponibleChamp = disponibleChamp;
        this.typeAChamp = typeAChamp;
        this.blocChamp = blocChamp;
        this.etageChamp = etageChamp;
        this.superficieChamp = superficieChamp;
        this.prixLocationChamp = prixLocationChamp;
        this.imageAChampErreur = imageAChampErreur;
        this.IDUtilisateurChampErreur = IDUtilisateurChampErreur;
        this.superficieChampErreur = superficieChampErreur;
        this.prixLocationChampErreur = prixLocationChampErreur;
    }

    public void ajouterAppartementAction() {
        imageAChampErreur.setText("");
        IDUtilisateurChampErreur.setText("");
        superficieChampErreur.setText("");
        prixLocationChampErreur.setText("");

        String imageA = imageAChamp.getText();
        String residence = residenceChamp.getValue();
        String idUtilisateurStr = IDUtilisateurChamp.getText();
        String typeA = typeAChamp.getValue();
        String bloc = blocChamp.getText();
        String etageStr = etageChamp.getText();
        String superficieStr = superficieChamp.getText();
        String prixLocationStr = prixLocationChamp.getText();
        boolean parking = "Disponible".equals(parkingChamp.getValue());
        boolean disponible = "Disponible".equals(disponibleChamp.getValue());

        int idUtilisateur = parseIntSafe(idUtilisateurStr, IDUtilisateurChampErreur);
        int etage = parseIntSafe(etageStr, null);
        int superficie = parseIntSafe(superficieStr, superficieChampErreur);
        int prixLocation = parseIntSafe(prixLocationStr, prixLocationChampErreur);

        String appartementInfo = String.format(
                "{\"bloc\": \"%s\", \"floor\": \"%s\", \"number\": \"\", \"parking\": %b, \"disponible\": %b}",
                bloc, etageStr, parking, disponible
        );

        Appartement appartement = new Appartement();
        Residence Residence= new Residence();

        ServiceResidence ServiceResidence = new ServiceResidence();

        Residence=ServiceResidence.TrouverResidenceParNom(residence);


        appartement.setImage_a(imageA);


        appartement.setId_residence(Residence.getId_residence());
        appartement.setId_user(idUtilisateur);
        appartement.setType_a(typeA);
        appartement.setAppartement_info(appartementInfo);
        appartement.setSuperficie(superficie);
        appartement.setPrix_location(prixLocation);
        appartement.setParking(parking ? 1 : 0);
        appartement.setDisponible(disponible ? 1 : 0);

        ServiceAppartement serviceAppartement = new ServiceAppartement();

        try {
            serviceAppartement.Ajouter(appartement);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Appartement Ajouté");
            alert.setHeaderText("Appartement ajouté avec succès!");
            alert.show();

            String[] rowData = {String.valueOf(appartement.getId_app()), residence, typeA, bloc, etageStr};
            new AppartementShow(stage, previousScene, rowData).show();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(e.getMessage());
            alert.show();
        }
    }

    private int parseIntSafe(String value, Text errorField) {
        if (value == null || value.isBlank()) {
            if (errorField != null) errorField.setText("This field must be a number.");
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            if (errorField != null) errorField.setText("Invalid number: \"" + value + "\"");
            return 0;
        }
    }

    private double parseDoubleSafe(String value, Text errorField) {
        if (value == null || value.isBlank()) {
            if (errorField != null) errorField.setText("This field must be a number.");
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            if (errorField != null) errorField.setText("Invalid number: \"" + value + "\"");
            return 0.0;
        }
    }
}