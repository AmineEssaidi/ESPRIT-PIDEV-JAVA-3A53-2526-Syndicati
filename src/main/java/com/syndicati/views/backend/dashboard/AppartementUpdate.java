package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.backend.residence.ModifierAppartementController;
import com.syndicati.models.entities.Appartement;
import com.syndicati.models.entities.Maintenance;
import com.syndicati.models.entities.Residence;
import com.syndicati.models.services.ServiceAppartement;
import com.syndicati.models.services.ServiceMaintenance;
import com.syndicati.models.services.ServiceResidence;
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

public class AppartementUpdate extends BaseDashboardPage {

    ServiceAppartement ServiceAppartement = new ServiceAppartement();

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
    private final int idAppartement;

    public AppartementUpdate(Stage stage, Scene previousScene, int idAppartement) {
        super(stage, previousScene);
        this.idAppartement = idAppartement;
    }

    @Override
    protected VBox buildContent() {
        Appartement appartement = ServiceAppartement.TrouverAppartementParId(idAppartement);
        Maintenance maintenance = null;
        try {
            maintenance = new ServiceMaintenance().MaintenanceParIdAppartement(idAppartement);
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        Button imageButton = new Button("Choisir une image");
        imageText = new Text(appartement != null ? appartement.getImage_a() : "Aucune image choisie");
        imageErreur = new Text("");

        imageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                imageA = selectedFile;
                imageText.setText(selectedFile.getName());
            }
        });

        IDUtilisateurChamp.setText(appartement != null ? String.valueOf(appartement.getId_user()) : "");
        superficieChamp.setText(appartement != null ? String.valueOf(appartement.getSuperficie()) : "");
        prixLocationChamp.setText(appartement != null ? String.valueOf(appartement.getPrix_location()) : "");

        residenceChamp.getItems().addAll(ListeNomsResidences());
        residenceChamp.setOnAction(e -> updateResidenceDependentFields());

        if (appartement != null) {
            ServiceResidence sr = new ServiceResidence();
            Residence res = sr.TrouverResidenceParId(appartement.getId_residence());
            if (res != null) {
                residenceChamp.setValue(res.getNom_r());
                updateResidenceDependentFields();

                blocChamp.setValue(extractJsonField(appartement.getAppartement_info(), "bloc"));
                etageChamp.setValue(extractJsonField(appartement.getAppartement_info(), "floor"));
                numeroChamp.setValue(extractJsonField(appartement.getAppartement_info(), "number"));
            }
        }

        typeAChamp.getItems().addAll("S+0", "S+1", "S+2", "S+3", "S+4", "S+5");
        if (appartement != null) typeAChamp.setValue(appartement.getType_a());

        parkingChamp.getItems().addAll("Disponible", "Non Disponible");
        if (appartement != null) parkingChamp.setValue(appartement.getParking() == 1 ? "Disponible" : "Non Disponible");

        disponibleChamp.getItems().addAll("Disponible", "Non Disponible");
        if (appartement != null) disponibleChamp.setValue(appartement.getDisponible() == 1 ? "Disponible" : "Non Disponible");

        List<String> etats = List.of("bon", "moyen", "mauvais");
        etatAppChamp.getItems().addAll(etats);
        etatPlomberieChamp.getItems().addAll(etats);
        etatElectriciteChamp.getItems().addAll(etats);
        etatChauffageChamp.getItems().addAll(etats);

        if (maintenance != null) {
            etatAppChamp.setValue(maintenance.getEtat_app());
            etatPlomberieChamp.setValue(maintenance.getEtat_plomberie());
            etatElectriciteChamp.setValue(maintenance.getEtat_electricite());
            etatChauffageChamp.setValue(maintenance.getEtat_chauffage());
            dateDerniereMaintenanceChamp.setText(maintenance.getDate_derniere_maintenance());
            descriptionMaintenanceChamp.setText(maintenance.getdescription_maint());
        }

        ModifierAppartementController controller = new ModifierAppartementController(
                stage, previousScene,
                residenceChamp, IDUtilisateurChamp, parkingChamp, disponibleChamp, typeAChamp,
                blocChamp, numeroChamp, etageChamp, superficieChamp, prixLocationChamp,
                imageErreur, IDUtilisateurChampErreur, superficieChampErreur, prixLocationChampErreur,
                idAppartement
        );

        VBox imageGroup = new VBox(5, new Label("Image"), imageButton, imageText, imageErreur);
        VBox card = glassCard();
        GridPane infoGrid = new GridPane();
        infoGrid.add(fieldGroup("Type", typeAChamp), 1, 0);
        infoGrid.add(fieldGroup("Bloc", blocChamp), 2, 0);
        infoGrid.add(fieldGroup("Numéro", numeroChamp), 3, 0);
        infoGrid.add(fieldGrid("Étage", etageChamp), 4, 0);

        GridPane maintenanceGrid = new GridPane();
        maintenanceGrid.setHgap(15); maintenanceGrid.setVgap(10);
        maintenanceGrid.add(fieldGroup("État appartement", etatAppChamp), 0, 0);
        maintenanceGrid.add(fieldGroup("État plomberie", etatPlomberieChamp), 1, 0);
        maintenanceGrid.add(fieldGroup("État électricité", etatElectriciteChamp), 0, 1);
        maintenanceGrid.add(fieldGroup("État chauffage", etatChauffageChamp), 1, 1);

        card.getChildren().addAll(
                imageGroup,
                fieldGroup("Résidence", residenceChamp),
                fieldGroup("ID Utilisateur", IDUtilisateurChamp, IDUtilisateurChampErreur),
                infoGrid,
                fieldGroup("Superficie", superficieChamp, superficieChampErreur),
                fieldGroup("Prix Location", prixLocationChamp, prixLocationChampErreur),
                fieldGroup("Parking", parkingChamp),
                fieldGroup("Disponible", disponibleChamp),
                new VBox(10, new Label("Maintenance de l'appartement"), maintenanceGrid),
                fieldGroup("Date dernière maintenance", dateDerniereMaintenanceChamp),
                fieldGroup("Description maintenance", descriptionMaintenanceChamp)
        );

        Button submitBtn = primaryButton("Update Appartement");
        submitBtn.setOnAction(e -> {
            if (VerifierAppartement()) {
                String desc = descriptionMaintenanceChamp.getText() == null ? "" : descriptionMaintenanceChamp.getText().trim();
                String mDate = dateDerniereMaintenanceChamp.getText() == null ? "" : dateDerniereMaintenanceChamp.getText().trim();

                Maintenance m = new Maintenance(
                        "", desc, mDate,
                        etatChauffageChamp.getValue(), etatElectriciteChamp.getValue(),
                        etatPlomberieChamp.getValue(), etatAppChamp.getValue(), idAppartement
                );

                try {
                    new ServiceMaintenance().Modifier(m);
                } catch (SQLDataException ex) {
                    throw new RuntimeException(ex);
                }
                controller.setImageA(imageA);
                controller.modifierAppartementAction();
            }
        });

        Button backBtn = secondaryButton("← Back");
        backBtn.setOnAction(e -> stage.setScene(previousScene));

        HBox actions = new HBox(12, backBtn, submitBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox form = new VBox(20, pageTitle("Update Appartement"), card, actions);
        form.setFillWidth(true);
        applyStyles();
        styleComboBox(residenceChamp);
        styleComboBox(blocChamp);
        styleComboBox(etageChamp);
        styleComboBox(numeroChamp);
        styleComboBox(typeAChamp);
        styleComboBox(parkingChamp);
        styleComboBox(disponibleChamp);
        styleComboBox(etatAppChamp);
        styleComboBox(etatPlomberieChamp);
        styleComboBox(etatElectriciteChamp);
        styleComboBox(etatChauffageChamp);
        return form;

    }
    private void updateResidenceDependentFields() {
        String selected = residenceChamp.getValue();
        if (selected == null || listeResidences == null) return;

        Residence res = listeResidences.stream()
                .filter(r -> r.getNom_r().equals(selected))
                .findFirst()
                .orElse(null);

        if (res != null) {
            if (res.getN_blocs() != null) {
                blocChamp.getItems().setAll(res.getN_blocs().split("\\s*,\\s*"));
            }

            etageChamp.getItems().clear();
            for (int i = 0; i <= res.getN_etages(); i++) {
                etageChamp.getItems().add(String.valueOf(i));
            }

            numeroChamp.getItems().clear();
            for (int i = 1; i <= res.getN_appartements(); i++) {
                numeroChamp.getItems().add(String.valueOf(i));
            }
        }
    }

    public boolean VerifierAppartement() {
        boolean valid = true;
        try { Integer.parseInt(IDUtilisateurChamp.getText()); IDUtilisateurChampErreur.setText(""); }
        catch (Exception e) { IDUtilisateurChampErreur.setText("ID invalide"); valid = false; }
        try { Double.parseDouble(superficieChamp.getText()); superficieChampErreur.setText(""); }
        catch (Exception e) { superficieChampErreur.setText("Superficie invalide"); valid = false; }
        try { Double.parseDouble(prixLocationChamp.getText()); prixLocationChampErreur.setText(""); }
        catch (Exception e) { prixLocationChampErreur.setText("Prix invalide"); valid = false; }
        return valid;
    }

    private String extractJsonField(String json, String field) {
        try {
            String key = "\"" + field + "\": \"";
            int start = json.indexOf(key) + key.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) { return ""; }
    }

    public String[] ListeNomsResidences() {
        try {
            listeResidences = new ServiceResidence().Recuperer();
            return listeResidences.stream().map(Residence::getNom_r).toArray(String[]::new);
        } catch (SQLDataException e) { throw new RuntimeException(e); }
    }

    private void applyStyles() {
        styleComboBox(residenceChamp); styleComboBox(blocChamp); styleComboBox(etageChamp);
        styleComboBox(numeroChamp); styleComboBox(typeAChamp); styleComboBox(parkingChamp);
        styleComboBox(disponibleChamp); styleComboBox(etatAppChamp); styleComboBox(etatPlomberieChamp);
        styleComboBox(etatElectriciteChamp); styleComboBox(etatChauffageChamp);
    }

    protected VBox fieldGroup(String label, Control input) {
        VBox group = new VBox(5, new Label(label), input);
        return group;
    }

    protected VBox fieldGroup(String label, TextField field, Text error) {
        VBox group = new VBox(5, new Label(label), field, error);
        return group;
    }

    private VBox fieldGrid(String label, Control input) {
        return new VBox(5, new Label(label), input);
    }
}