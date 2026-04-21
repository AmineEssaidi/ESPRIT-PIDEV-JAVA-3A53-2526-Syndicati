package com.syndicati.controllers.residence;

import com.syndicati.models.residence.Appartement;
import com.syndicati.models.residence.Residence;
import com.syndicati.services.residence.ServiceAppartement;
import com.syndicati.services.residence.ServiceResidence;
import com.syndicati.views.backend.dashboard.AppartementShow;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ModifierAppartementController {

    private final Stage stage;
    private final Scene previousScene;

    private final ComboBox<String> residenceChamp;
    private final TextField IDUtilisateurChamp;
    private final ComboBox<String> parkingChamp;
    private final ComboBox<String> disponibleChamp;
    private final ComboBox<String> typeAChamp;
    private final ComboBox<String> blocChamp;
    private final ComboBox<String> numeroChamp;
    private final ComboBox<String> etageChamp;
    private final TextField superficieChamp;
    private final TextField prixLocationChamp;

    private final Text imageErreur;
    private final Text IDUtilisateurChampErreur;
    private final Text superficieChampErreur;
    private final Text prixLocationChampErreur;

    private final int idAppartement;
    private File imageA;

    public ModifierAppartementController(Stage stage, Scene previousScene,
                                         ComboBox<String> residenceChamp,
                                         TextField IDUtilisateurChamp,
                                         ComboBox<String> parkingChamp,
                                         ComboBox<String> disponibleChamp,
                                         ComboBox<String> typeAChamp,
                                         ComboBox<String> blocChamp,
                                         ComboBox<String> numeroChamp,
                                         ComboBox<String> etageChamp,
                                         TextField superficieChamp,
                                         TextField prixLocationChamp,
                                         Text imageErreur,
                                         Text IDUtilisateurChampErreur,
                                         Text superficieChampErreur,
                                         Text prixLocationChampErreur,
                                         int idAppartement) {
        this.stage = stage;
        this.previousScene = previousScene;
        this.residenceChamp = residenceChamp;
        this.IDUtilisateurChamp = IDUtilisateurChamp;
        this.parkingChamp = parkingChamp;
        this.disponibleChamp = disponibleChamp;
        this.typeAChamp = typeAChamp;
        this.blocChamp = blocChamp;
        this.numeroChamp = numeroChamp;
        this.etageChamp = etageChamp;
        this.superficieChamp = superficieChamp;
        this.prixLocationChamp = prixLocationChamp;
        this.imageErreur = imageErreur;
        this.IDUtilisateurChampErreur = IDUtilisateurChampErreur;
        this.superficieChampErreur = superficieChampErreur;
        this.prixLocationChampErreur = prixLocationChampErreur;
        this.idAppartement = idAppartement;
    }

    public void setImageA(File imageA) {
        this.imageA = imageA;
    }

    public void modifierAppartementAction() {
        // Clear old errors
        IDUtilisateurChampErreur.setText("");
        superficieChampErreur.setText("");
        prixLocationChampErreur.setText("");

        if (!VerifierAppartement()) return;


        ServiceAppartement serviceAppartement = new ServiceAppartement();
        Appartement appartement = serviceAppartement.TrouverAppartementParId(idAppartement);

        if (appartement == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Appartement introuvable (ID: " + idAppartement + ")");
            alert.show();
            return;
        }

        try {
            String residence = residenceChamp.getValue();
            ServiceResidence serviceResidence = new ServiceResidence();
            Residence residence_obj = serviceResidence.TrouverResidenceParNom(residence);

            appartement.setResidence_id(residence_obj.getId_residence());
            appartement.setId_user(Integer.parseInt(IDUtilisateurChamp.getText().trim()));
            appartement.setType_a(typeAChamp.getValue());
            appartement.setSuperficie(Integer.parseInt(superficieChamp.getText().trim()));
            appartement.setPrix_location(Integer.parseInt(prixLocationChamp.getText().trim()));
            appartement.setParking("Disponible".equals(parkingChamp.getValue()) ? 1 : 0);
            appartement.setDisponible("Disponible".equals(disponibleChamp.getValue()) ? 1 : 0);

            String appartementInfo = String.format(
                    "{\"bloc\": \"%s\", \"floor\": \"%s\", \"number\": \"%s\", \"parking\": %b, \"disponible\": %b}",
                    blocChamp.getValue(), etageChamp.getValue(), numeroChamp.getValue(), parkingChamp.getValue(), disponibleChamp.getValue()
            );

            appartement.setAppartement_info(appartementInfo);

            if (imageA != null) {
                SauvegarderImage(imageA, appartement);
            }

            serviceAppartement.Modifier(appartement);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appartement modifié avec succès!");
            alert.show();
            new AppartementShow(stage, previousScene, appartement).show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur de mise à jour: " + e.getMessage());
            alert.show();
        }
    }

    private boolean VerifierAppartement() {
        boolean isValid = true;

        IDUtilisateurChampErreur.setText("");
        superficieChampErreur.setText("");
        prixLocationChampErreur.setText("");

        IDUtilisateurChampErreur.setVisible(true);
        IDUtilisateurChampErreur.setManaged(true);
        superficieChampErreur.setVisible(true);
        superficieChampErreur.setManaged(true);
        prixLocationChampErreur.setVisible(true);
        prixLocationChampErreur.setManaged(true);

        String idUser = IDUtilisateurChamp.getText();
        if (idUser == null || !idUser.matches("\\d+")) {
            IDUtilisateurChampErreur.setText("ID utilisateur invalide");
            isValid = false;
        }

        String superficie = superficieChamp.getText();
        if (superficie == null || !superficie.matches("\\d+")) {
            superficieChampErreur.setText("Superficie invalide!");
            isValid = false;
        }

        String prix = prixLocationChamp.getText();
        if (prix == null || !prix.matches("\\d+")) {
            prixLocationChampErreur.setText("Prix de location invalide!");
            isValid = false;
        }

        return isValid;
    }

    public boolean SauvegarderImage(File imageA, Appartement appartement) {
        if (imageA != null) {
            String destDir = System.getProperty("user.dir") + "/uploads/appartement_images/";
            new File(destDir).mkdirs();
            String destPath = (destDir + imageA.getName()).replace("\\", "/");
            try {
                Files.copy(imageA.toPath(), Path.of(destPath), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            appartement.setImage_a(imageA.getName());
        }
        return true;
    }
}