package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.residence.AjouterAppartementController;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.models.residence.Residence;
import com.syndicati.services.residence.ServiceAppartement;
import com.syndicati.services.residence.ServiceMaintenance;
import com.syndicati.services.residence.ServiceResidence;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

public class AppartementAdd extends BaseDashboardPage {

    ServiceAppartement serviceAppartement = new ServiceAppartement();
    Residence res = null;

    ComboBox<String> residenceChamp = new ComboBox<>();
    TextField IDUtilisateurChamp = new TextField();
    ComboBox<String> parkingChamp = new ComboBox<>();
    ComboBox<String> disponibleChamp = new ComboBox<>();
    ComboBox<String> typeAChamp = new ComboBox<>();
    ComboBox<String> numeroChamp = new ComboBox<>();
    ComboBox<String> blocChamp = new ComboBox<>();
    ComboBox<String> etageChamp = new ComboBox<>();
    TextField superficieChamp = new TextField();
    TextField prixLocationChamp = new TextField();

    Text IDUtilisateurChampErreur = new Text();
    Text superficieChampErreur = new Text();
    Text prixLocationChampErreur = new Text();

    ComboBox<String> etatAppChamp = new ComboBox<>();
    ComboBox<String> etatPlomberieChamp = new ComboBox<>();
    ComboBox<String> etatElectriciteChamp = new ComboBox<>();
    ComboBox<String> etatChauffageChamp = new ComboBox<>();
    TextField dateDerniereMaintenanceChamp = new TextField();
    TextArea descriptionMaintenanceChamp = new TextArea();
    private List<Residence> listeResidences = new ArrayList<>();

    private Text imageText;
    private Text imageErreur;
    private File imageA = null;

    public AppartementAdd(Stage stage, Scene previousScene) {
        super(stage, previousScene);
    }

    @Override
    protected VBox buildContent() {
        Button imageButton = new Button("Choisir une image");
        imageText = new Text("Aucune image choisie");
        imageErreur = new Text("");

        imageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                imageA = selectedFile;
                imageText.setText(selectedFile.getName());
            }
        });

        IDUtilisateurChamp.setPromptText("ID de l'utilisateur");
        superficieChamp.setPromptText("En m²");
        prixLocationChamp.setPromptText("En TND");
        residenceChamp.getItems().addAll(ListeNomsResidences());
        residenceChamp.setPromptText("Sélectionner une résidence");

        residenceChamp.setOnAction(e -> {
            String selectedResidence = residenceChamp.getValue();
            if (selectedResidence == null) return;

            Residence selectedRes = listeResidences.stream()
                    .filter(r -> r.getNom_r().equals(selectedResidence))
                    .findFirst().orElse(null);

            if (selectedRes != null) {
                blocChamp.getItems().clear();
                if (selectedRes.getN_blocs() != null) {
                    blocChamp.getItems().addAll(selectedRes.getN_blocs().split("\\s*,\\s*"));
                }

                etageChamp.getItems().clear();
                for (int i = 0; i <= selectedRes.getN_etages(); i++) {
                    etageChamp.getItems().add(String.valueOf(i));
                }

                numeroChamp.getItems().clear();
                for (int i = 1; i <= selectedRes.getN_appartements(); i++) {
                    numeroChamp.getItems().add(String.valueOf(i));
                }
            }
        });

        typeAChamp.getItems().addAll("S+0", "S+1", "S+2", "S+3", "S+4", "S+5");
        parkingChamp.getItems().addAll("Disponible", "Non Disponible");
        disponibleChamp.getItems().addAll("Disponible", "Non Disponible");

        List<String> etats = List.of("bon", "moyen", "mauvais");
        etatAppChamp.getItems().addAll(etats);
        etatPlomberieChamp.getItems().addAll(etats);
        etatElectriciteChamp.getItems().addAll(etats);
        etatChauffageChamp.getItems().addAll(etats);

        dateDerniereMaintenanceChamp.setPromptText("YYYY-MM-DD");
        descriptionMaintenanceChamp.setPrefRowCount(3);
        descriptionMaintenanceChamp.setWrapText(true);

        AjouterAppartementController controller = new AjouterAppartementController(
                stage, previousScene,
                residenceChamp, IDUtilisateurChamp, parkingChamp, disponibleChamp,
                typeAChamp, blocChamp, numeroChamp, etageChamp,
                superficieChamp, prixLocationChamp, imageErreur,
                IDUtilisateurChampErreur, superficieChampErreur, prixLocationChampErreur
        );

        VBox imageGroup = new VBox(5, new Label("Image"), imageButton, imageText, imageErreur);
        VBox card = glassCard();

        GridPane infoAppartement = new GridPane();
        infoAppartement.setHgap(10);
        infoAppartement.add(fieldGroup("Type", typeAChamp), 0, 0);
        infoAppartement.add(fieldGroup("Bloc", blocChamp), 1, 0);
        infoAppartement.add(fieldGroup("Numéro", numeroChamp), 2, 0);
        infoAppartement.add(fieldGroup("Étage", etageChamp), 3, 0);

        GridPane maintenanceGrid = new GridPane();
        maintenanceGrid.setHgap(15);
        maintenanceGrid.setVgap(10);
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        maintenanceGrid.getColumnConstraints().addAll(col, col);

        maintenanceGrid.add(fieldGroup("État appartement", etatAppChamp), 0, 0);
        maintenanceGrid.add(fieldGroup("État plomberie", etatPlomberieChamp), 1, 0);
        maintenanceGrid.add(fieldGroup("État électricité", etatElectriciteChamp), 0, 1);
        maintenanceGrid.add(fieldGroup("État chauffage", etatChauffageChamp), 1, 1);

        card.getChildren().addAll(
                imageGroup,
                fieldGroup("Résidence", residenceChamp),
                fieldGroup("ID Utilisateur", IDUtilisateurChamp, IDUtilisateurChampErreur),
                infoAppartement,
                fieldGroup("Superficie", superficieChamp, superficieChampErreur),
                fieldGroup("Prix Location", prixLocationChamp, prixLocationChampErreur),
                fieldGroup("Parking", parkingChamp),
                fieldGroup("Disponible", disponibleChamp),
                new VBox(10, new Label("Maintenance"), maintenanceGrid),
                fieldGroup("Date dernière maintenance", dateDerniereMaintenanceChamp),
                fieldGroup("Description maintenance", descriptionMaintenanceChamp)
        );

        Button submitBtn = primaryButton("Ajouter Appartement");
        submitBtn.setOnAction(e -> {
            if (VerifierAppartement()) {
                try {
                    controller.setImageA(imageA);
                    controller.ajouterAppartementAction();

                    int idApp = serviceAppartement.PlusRecentAppartementID();

                    String dateMaint = dateDerniereMaintenanceChamp.getText().trim();
                    String descMaint = descriptionMaintenanceChamp.getText().trim();

                    Maintenance m = new Maintenance(
                            "",
                            descMaint.isEmpty() || descMaint.equalsIgnoreCase("null") ? null : descMaint,
                            dateMaint.isEmpty() || dateMaint.equalsIgnoreCase("null") ? null : dateMaint,
                            etatChauffageChamp.getValue(),
                            etatElectriciteChamp.getValue(),
                            etatPlomberieChamp.getValue(),
                            etatAppChamp.getValue(),
                            idApp
                    );

                    new ServiceMaintenance().Ajouter(m);

                    stage.setScene(previousScene);

                } catch (SQLDataException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button backBtn = secondaryButton("← Back");
        backBtn.setOnAction(e -> stage.setScene(previousScene));

        HBox actions = new HBox(12, backBtn, submitBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        applyStyles();
        return new VBox(20, pageTitle("Add Appartement"), card, actions);
    }

    private boolean VerifierAppartement() {
        boolean valid = true;
        IDUtilisateurChampErreur.setText("");
        superficieChampErreur.setText("");
        prixLocationChampErreur.setText("");

        if (IDUtilisateurChamp.getText() == null || !IDUtilisateurChamp.getText().matches("\\d+")) {
            IDUtilisateurChampErreur.setText("ID utilisateur invalide");
            valid = false;
        }
        if (superficieChamp.getText() == null || !superficieChamp.getText().matches("\\d+")) {
            superficieChampErreur.setText("Superficie invalide");
            valid = false;
        }
        if (prixLocationChamp.getText() == null || !prixLocationChamp.getText().matches("\\d+")) {
            prixLocationChampErreur.setText("Prix invalide");
            valid = false;
        }
        return valid;
    }

    private void applyStyles() {
        styleComboBox(residenceChamp); styleComboBox(blocChamp); styleComboBox(etageChamp);
        styleComboBox(numeroChamp); styleComboBox(typeAChamp); styleComboBox(parkingChamp);
        styleComboBox(disponibleChamp); styleComboBox(etatAppChamp); styleComboBox(etatPlomberieChamp);
        styleComboBox(etatElectriciteChamp); styleComboBox(etatChauffageChamp);
    }

    public String[] ListeNomsResidences() {
        try {
            listeResidences = new ServiceResidence().Recuperer();
            return listeResidences.stream().map(Residence::getNom_r).toArray(String[]::new);
        } catch (SQLDataException e) {
            return new String[0];
        }
    }

    protected VBox fieldGroup(String label, Control input) {
        return new VBox(5, new Label(label), input);
    }

    protected VBox fieldGroup(String label, TextField field, Text error) {
        error.setStyle("-fx-fill: #ff4d4d; -fx-font-size: 11px;");
        return new VBox(5, new Label(label), field, error);
    }
}