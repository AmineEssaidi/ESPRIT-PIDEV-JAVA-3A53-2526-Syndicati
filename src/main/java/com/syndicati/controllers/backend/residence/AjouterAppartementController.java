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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

public class AjouterAppartementController {

    private  Stage stage;
    private  Scene previousScene;

    private  ComboBox<String> residenceChamp;
    private  TextField IDUtilisateurChamp;
    private  ComboBox<String> parkingChamp;
    private  ComboBox<String> disponibleChamp;
    private  ComboBox<String> typeAChamp;
    private   ComboBox<String> numeroChamp;
    private ComboBox<String> blocChamp;
    private ComboBox<String> etageChamp;
    private  TextField superficieChamp;
    private  TextField prixLocationChamp;

    private  Text imageErreur;
    private  Text IDUtilisateurChampErreur;
    private  Text superficieChampErreur;
    private  Text prixLocationChampErreur;

    private File imageA;

    public AjouterAppartementController(Stage stage, Scene previousScene,
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
                                        Text prixLocationChampErreur) {
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
    }

    public void setImageA(File imageA) {
        this.imageA = imageA;
    }

    public void ajouterAppartementAction() {
        imageErreur.setText("");
        IDUtilisateurChampErreur.setText("");
        superficieChampErreur.setText("");
        prixLocationChampErreur.setText("");

        String residence = residenceChamp.getValue();
        String idUtilisateurStr = IDUtilisateurChamp.getText();
        String typeA = typeAChamp.getValue();
        String bloc = blocChamp.getValue();
        String numero = numeroChamp.getValue();
        String etageStr = etageChamp.getValue();
        String superficieStr = superficieChamp.getText();
        String prixLocationStr = prixLocationChamp.getText();
        boolean parking = "Disponible".equals(parkingChamp.getValue());
        boolean disponible = "Disponible".equals(disponibleChamp.getValue());

        if (!VerifierAppartement(IDUtilisateurChamp, superficieChamp, prixLocationChamp,
                IDUtilisateurChampErreur, superficieChampErreur, prixLocationChampErreur)) {
            return;
        }

        if (imageA == null) {
            imageErreur.setText("Image obligatoire!");
            return;
        }

        int idUtilisateur = Integer.parseInt(idUtilisateurStr);
        int superficie = Integer.parseInt(superficieStr);
        int prixLocation = Integer.parseInt(prixLocationStr);

        String appartementInfo = String.format(
                "{\"bloc\": \"%s\", \"floor\": \"%s\", \"number\": \"%s\", \"parking\": %b, \"disponible\": %b}",
                bloc, etageStr, numero, parking, disponible
        );

        Appartement appartement = new Appartement();

        ServiceResidence serviceResidence = new ServiceResidence();
        Residence residence_obj = serviceResidence.TrouverResidenceParNom(residence);

        SauvegarderImage(imageA, appartement);

        appartement.setResidence_id(residence_obj.getId_residence());
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
            new AppartementShow(stage, previousScene, appartement).show();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(e.getMessage());
            alert.show();
        }
    }

    private boolean VerifierAppartement(TextField IDUtilisateurChamp,
                                        TextField superficieChamp,
                                        TextField prixLocationChamp,
                                        Text IDUtilisateurChampErreur,
                                        Text superficieChampErreur,
                                        Text prixLocationChampErreur) {
        boolean test = true;

        IDUtilisateurChampErreur.setText("");
        superficieChampErreur.setText("");
        prixLocationChampErreur.setText("");

        IDUtilisateurChampErreur.setVisible(true); IDUtilisateurChampErreur.setManaged(true);
        superficieChampErreur.setVisible(true);    superficieChampErreur.setManaged(true);
        prixLocationChampErreur.setVisible(true);  prixLocationChampErreur.setManaged(true);

        String idUtilisateurStr = IDUtilisateurChamp.getText();
        if (!idUtilisateurStr.matches("\\d+")) {
            IDUtilisateurChampErreur.setText("ID utilisateur invalide!");
            test = false;
        } else {
            int idUtilisateur = Integer.parseInt(idUtilisateurStr);
            if (idUtilisateur <= 0) {
                IDUtilisateurChampErreur.setText("ID utilisateur doit être positif!");
                test = false;
            }
        }

        String superficieStr = superficieChamp.getText();
        if (!superficieStr.matches("\\d+")) {
            superficieChampErreur.setText("Superficie invalide!");
            test = false;
        } else {
            int superficie = Integer.parseInt(superficieStr);
            if (superficie <= 0) {
                superficieChampErreur.setText("Superficie doit être positive!");
                test = false;
            }
        }

        String prixLocationStr = prixLocationChamp.getText();
        if (!prixLocationStr.matches("\\d+")) {
            prixLocationChampErreur.setText("Prix de location invalide!");
            test = false;
        } else {
            int prixLocation = Integer.parseInt(prixLocationStr);
            if (prixLocation <= 0) {
                prixLocationChampErreur.setText("Prix de location doit être positif!");
                test = false;
            }
        }

        return test;
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