package com.syndicati.controllers.backend.residence;

import com.syndicati.models.entities.Residence;
import com.syndicati.models.services.ServiceResidence;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.syndicati.views.backend.dashboard.ResidenceShow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Provider;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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
    private File imageR;

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

    public void setImageR(File imageR) {
        this.imageR = imageR;
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

        ServiceResidence serviceResidence = new ServiceResidence();
        if (VerifierResidence(nom, adresse, nAppartementsStr, dateAjout, nBlocs,
                nomError, adresseError, nAppartementsError,
                dateAjoutError, nBlocsError)) {
            try {
                SauvegarderImage(imageR, residence);
                residence.setNom_r(nom);
                residence.setAdresse(adresse);
                residence.setN_appartements(Integer.parseInt(nAppartementsStr));
                residence.setDate_ajout(dateAjout);
                residence.setN_blocs(nBlocs);
                residence.setN_etages(0);

                serviceResidence.Ajouter(residence);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Résidence Ajoutée");
                alert.setHeaderText("Résidence ajoutée avec succès!");
                alert.showAndWait();

                String[] rowData = {nom, adresse, nAppartementsStr, dateAjout, nBlocs};
                new ResidenceShow(stage, previousScene, rowData).show();

            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            } catch (DateTimeParseException e) {
                dateAjoutError.setText("Format de date invalide (dd/MM/yyyy)");
            }
        }
    }


    private boolean VerifierResidence(String nom, String adresse, String nAppartementsStr, String dateAjout, String nBlocs,
                                      Text nomError, Text adresseError, Text nAppartementsError, Text dateAjoutError, Text nBlocsError)
    {
        boolean test = true;

        nomError.setText("");
        adresseError.setText("");
        nAppartementsError.setText("");
        dateAjoutError.setText("");
        nBlocsError.setText("");

        nomError.setVisible(true); nomError.setManaged(true);
        adresseError.setVisible(true); adresseError.setManaged(true);
        nAppartementsError.setVisible(true); nAppartementsError.setManaged(true);
        dateAjoutError.setVisible(true); dateAjoutError.setManaged(true);
        nBlocsError.setVisible(true); nBlocsError.setManaged(true);

        if (nom == null || nom.trim().isEmpty())
        {
            nomError.setText("Nom de résidence obligatoire!");
            test = false;
        }
        else if (!(nom.matches("^[a-zA-ZÀ-ÿ]+[a-zA-ZÀ-ÿ0-9 ]*$")))
        {
            nomError.setText("Nom de résidence invalide!");
            test = false;
        }

        if (adresse == null || adresse.trim().isEmpty())
        {
            adresseError.setText("L'adresse est obligatoire!");
            test = false;
        }
        else if (adresse.trim().length() < 5)
        {
            adresseError.setText("Adresse trop courte");
            test = false;
        }
        else if (adresse.matches("[0-9]+"))
        {
            adresseError.setText("Adresse invalide");
            test = false;
        }

        if (!(nAppartementsStr.matches("\\d+")))
        {
            nAppartementsError.setText("Nombre invalide!");
            test = false;
        }
        else
        {
            int nAppartements = Integer.parseInt(nAppartementsStr);
            if (nAppartements <= 0 || nAppartements > 10)
            {
                nAppartementsError.setText("Nombre d'appartements doit être entre 1 et 10!");
                test = false;
            }
        }

        if (dateAjout == null || dateAjout.trim().isEmpty())
        {
            dateAjoutError.setText("Date d'ajout obligatoire");
            test = false;
        }
        else if (!(dateAjout.matches("\\d{4}-\\d{2}-\\d{2}")))
        {
            dateAjoutError.setText("Format invalide (YYYY-MM-DD)");
            test = false;
        }



        return test;
    }

    public boolean SauvegarderImage(File imageR, Residence residence) {
        if (imageR != null) {
            String destDir = System.getProperty("user.dir") + "/uploads/residence_images/";
            new File(destDir).mkdirs();
            String destPath = (destDir + imageR.getName()).replace("\\", "/");
            try {
                Files.copy(imageR.toPath(), Path.of(destPath), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            residence.setImage_r(imageR.getName());
        }
        return true;
    }
}