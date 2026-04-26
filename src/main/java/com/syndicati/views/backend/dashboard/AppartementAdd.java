package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.residence.AjouterAppartementController;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.models.residence.Residence;
import com.syndicati.models.user.User;
import com.syndicati.models.user.data.UserRepository;
import com.syndicati.services.ServiceAlert;
import com.syndicati.services.residence.ServiceAppartement;
import com.syndicati.services.residence.ServiceMaintenance;
import com.syndicati.services.residence.ServicePredictionPrix;
import com.syndicati.services.residence.ServiceResidence;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    ComboBox<String> userChamp = new ComboBox<>();
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
    private List<User> finalListeUsers = new ArrayList<>();

    public AppartementAdd(Stage stage, Scene previousScene) {
        super(stage, previousScene);
    }

    @Override
    protected VBox buildContent() {
        Button imageButton = new Button("Choisir une image");
        imageButton.setMaxWidth(Double.MAX_VALUE);
        imageButton.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        imageButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: #8b5cf6;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-style: dashed;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-background-radius: 12px;" +
                        "-fx-text-fill:" + (isDark() ? "rgba(255,255,255,0.7)" : "rgba(15,23,42,0.7)") + ";" +
                        "-fx-padding: 18 0 18 0;" +
                        "-fx-cursor: hand;"
        );

        imageText = new Text("Aucune image choisie");
        imageText.setFont(Font.font(lightFont(), FontWeight.NORMAL, 11));
        imageText.setFill(isDark() ? Color.web("rgba(255,255,255,0.35)") : Color.web("rgba(15,23,42,0.4)"));
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
                imageText.setText("✓  " + selectedFile.getName());
                imageText.setFill(Color.web("#8b5cf6"));
                imageButton.setStyle(
                        "-fx-background-color: linear-gradient(to right, rgba(108,92,231,0.10), rgba(6,182,212,0.10));" +
                                "-fx-border-color: #8b5cf6;" +
                                "-fx-border-width: 2;" +
                                "-fx-border-style: dashed;" +
                                "-fx-border-radius: 12px;" +
                                "-fx-background-radius: 12px;" +
                                "-fx-text-fill: #8b5cf6;" +
                                "-fx-padding: 18 0 18 0;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        VBox imageGroup = new VBox(6);
        Text imageLabel = new Text("Image");
        imageLabel.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        imageLabel.setFill(isDark() ? Color.web("rgba(255,255,255,0.55)") : Color.web("rgba(15,23,42,0.64)"));
        imageGroup.getChildren().addAll(imageLabel, imageButton, imageText, imageErreur);

        userChamp.setMaxWidth(Double.MAX_VALUE);
        userChamp.setPromptText("Sélectionner un utilisateur");
        try {
            finalListeUsers = new UserRepository().findAllUserNames();
            finalListeUsers.stream()
                    .map(u -> u.getIdUser() + " - " + u.getFirstName() + " " + u.getLastName())
                    .forEach(userChamp.getItems()::add);
        } catch (Exception e) {
            System.err.println("Failed to load users: " + e.getMessage());
        }
        styleComboBox(userChamp);

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
                if (selectedRes.getN_blocs() != null)
                    blocChamp.getItems().addAll(selectedRes.getN_blocs().split("\\s*,\\s*"));
                etageChamp.getItems().clear();
                for (int i = 0; i <= selectedRes.getN_etages(); i++)
                    etageChamp.getItems().add(String.valueOf(i));
                numeroChamp.getItems().clear();
                for (int i = 1; i <= selectedRes.getN_appartements(); i++)
                    numeroChamp.getItems().add(String.valueOf(i));
            }
        });

        typeAChamp.getItems().addAll("S+0", "S+1", "S+2", "S+3", "S+4", "S+5");
        parkingChamp.getItems().addAll("Disponible", "Non Disponible");
        disponibleChamp.getItems().addAll("Disponible", "Non Disponible");

        ServicePredictionPrix predictionService = new ServicePredictionPrix();
        try { predictionService.trainFromCsv(); } catch (Exception ex) {
            System.err.println("[ML] Training failed: " + ex.getMessage());
        }

        Label aiPriceLabel = new Label("—");
        aiPriceLabel.setFont(Font.font(lightFont(), FontWeight.BOLD, 13));
        aiPriceLabel.setStyle("-fx-text-fill: white;");

        Text aiKey = new Text("💡 Prix Prédiction IA");
        aiKey.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        aiKey.setFill(Color.web("rgba(255,255,255,0.75)"));

        Region aiSpacer = new Region();
        HBox.setHgrow(aiSpacer, Priority.ALWAYS);

        HBox aiRow = new HBox(12, aiKey, aiSpacer, aiPriceLabel);
        aiRow.setPadding(new Insets(10, 14, 10, 14));
        aiRow.setAlignment(Pos.CENTER_LEFT);
        aiRow.setStyle(
                "-fx-background-color: linear-gradient(to right, rgba(108,92,231,0.18), rgba(139,92,246,0.18), rgba(6,182,212,0.18));" +
                        "-fx-background-radius:10px;" +
                        "-fx-border-color: #8b5cf6;" +
                        "-fx-border-width:1;" +
                        "-fx-border-radius:10px;"
        );

        Runnable updatePrediction = () -> {
            try {
                double surface = Double.parseDouble(superficieChamp.getText().strip());
                String typeA = typeAChamp.getValue();
                if (typeA == null) return;
                double prix = predictionService.predict(surface, typeA);
                if (prix > 0) {
                    aiPriceLabel.setText(String.format("%.0f TND", prix));
                    prixLocationChamp.setPromptText(String.format("IA suggère : %.0f TND", prix));
                }
            } catch (NumberFormatException ignored) {
                aiPriceLabel.setText("—");
                prixLocationChamp.setPromptText("En TND");
            }
        };
        superficieChamp.textProperty().addListener((obs, o, n) -> updatePrediction.run());
        typeAChamp.valueProperty().addListener((obs, o, n) -> updatePrediction.run());

        List<String> etats = List.of("bon", "moyen", "mauvais");
        etatAppChamp.getItems().addAll(etats);
        etatPlomberieChamp.getItems().addAll(etats);
        etatElectriciteChamp.getItems().addAll(etats);
        etatChauffageChamp.getItems().addAll(etats);

        dateDerniereMaintenanceChamp.setPromptText("YYYY-MM-DD");
        String textAreaBg = isDark() ? "rgba(255,255,255,0.05)" : "rgba(15,23,42,0.04)";
        String textAreaBorder = isDark() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.14)";
        String textAreaText = isDark() ? "white" : "#111827";
        String textAreaPrompt = isDark() ? "rgba(255,255,255,0.25)" : "rgba(15,23,42,0.35)";

        descriptionMaintenanceChamp.setPrefRowCount(3);
        descriptionMaintenanceChamp.setWrapText(true);
        descriptionMaintenanceChamp.setMaxWidth(Double.MAX_VALUE);
        descriptionMaintenanceChamp.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        descriptionMaintenanceChamp.setStyle(
                "-fx-background-color: " + textAreaBg + ";" +
                        "-fx-control-inner-background: " + (isDark() ? "#1a1a2e" : "white") + ";" +
                        "-fx-border-color: " + textAreaBorder + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-text-fill: " + textAreaText + ";" +
                        "-fx-prompt-text-fill: " + textAreaPrompt + ";" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-padding: 10 12 10 12;"
        );
        descriptionMaintenanceChamp.getStylesheets().add(
                "data:text/css,.text-area .content {" +
                        "-fx-background-color: " + textAreaBg + ";" +
                        "-fx-background-radius: 10px;" +
                        "} " +
                        ".text-area .scroll-pane {" +
                        "-fx-background-color: transparent;" +
                        "} " +
                        ".text-area .scroll-pane .viewport {" +
                        "-fx-background-color: transparent;" +
                        "}"
        );

        Text descLabel = new Text("Description maintenance");
        descLabel.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        descLabel.setFill(isDark() ? Color.web("rgba(255,255,255,0.55)") : Color.web("rgba(15,23,42,0.64)"));
        VBox descGroup = new VBox(6, descLabel, descriptionMaintenanceChamp);

        AjouterAppartementController controller = new AjouterAppartementController(
                stage, previousScene,
                residenceChamp, userChamp, parkingChamp, disponibleChamp,
                typeAChamp, blocChamp, numeroChamp, etageChamp,
                superficieChamp, prixLocationChamp, imageErreur,
                IDUtilisateurChampErreur, superficieChampErreur, prixLocationChampErreur
        );

        GridPane parkingGrid = new GridPane();
        parkingGrid.setHgap(12);
        parkingGrid.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints pkCol1 = new ColumnConstraints(); pkCol1.setPercentWidth(50);
        ColumnConstraints pkCol2 = new ColumnConstraints(); pkCol2.setPercentWidth(50);
        parkingGrid.getColumnConstraints().addAll(pkCol1, pkCol2);
        parkingGrid.add(fieldGroup("Parking", parkingChamp), 0, 0);
        parkingGrid.add(fieldGroup("Disponible", disponibleChamp), 1, 0);

        GridPane infoAppartement = new GridPane();
        infoAppartement.setHgap(10);
        infoAppartement.add(fieldGroup("Type", typeAChamp), 0, 0);
        infoAppartement.add(fieldGroup("Bloc", blocChamp), 1, 0);
        infoAppartement.add(fieldGroup("Numéro", numeroChamp), 2, 0);
        infoAppartement.add(fieldGroup("Étage", etageChamp), 3, 0);

        GridPane maintenanceGrid = new GridPane();
        maintenanceGrid.setHgap(15);
        maintenanceGrid.setVgap(10);
        ColumnConstraints col = new ColumnConstraints(); col.setPercentWidth(50);
        maintenanceGrid.getColumnConstraints().addAll(col, col);
        maintenanceGrid.add(fieldGroup("État appartement",  etatAppChamp),         0, 0);
        maintenanceGrid.add(fieldGroup("État plomberie",    etatPlomberieChamp),   1, 0);
        maintenanceGrid.add(fieldGroup("État électricité",  etatElectriciteChamp), 0, 1);
        maintenanceGrid.add(fieldGroup("État chauffage",    etatChauffageChamp),   1, 1);

        Text maintLabel = new Text("Maintenance");
        maintLabel.setFont(Font.font(lightFont(), FontWeight.NORMAL, 12));
        maintLabel.setFill(isDark() ? Color.web("rgba(255,255,255,0.55)") : Color.web("rgba(15,23,42,0.64)"));

        VBox card = glassCard();
        card.getChildren().addAll(
                imageGroup,
                fieldGroup("Résidence", residenceChamp),
                fieldGroup("Utilisateur", userChamp),
                infoAppartement,
                fieldGroup("Superficie", superficieChamp, superficieChampErreur),
                fieldGroup("Prix Location", prixLocationChamp, prixLocationChampErreur),
                aiRow,
                parkingGrid,
                new VBox(10, maintLabel, maintenanceGrid),
                fieldGroup("Date dernière maintenance", dateDerniereMaintenanceChamp),
                descGroup
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

                } catch (Exception ex) {
                    ex.printStackTrace();
                    ServiceAlert.showError("Erreur", ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName());
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

        if (userChamp.getValue() == null || userChamp.getValue().isBlank()) {
            IDUtilisateurChampErreur.setText("Veuillez sélectionner un utilisateur");
            valid = false;
        }
        if (superficieChamp.getText() == null || !superficieChamp.getText().matches("\\d+") || Integer.parseInt(superficieChamp.getText()) <= 0) {
            superficieChampErreur.setText("Superficie invalide");
            valid = false;
        }
        if (prixLocationChamp.getText() == null || !prixLocationChamp.getText().matches("\\d+") || Integer.parseInt(prixLocationChamp.getText()) <= 0) {
            prixLocationChampErreur.setText("Prix invalide");
            valid = false;
        }
        return valid;
    }

    private void applyStyles() {
        styleComboBox(residenceChamp); styleComboBox(blocChamp); styleComboBox(etageChamp);
        styleComboBox(numeroChamp); styleComboBox(typeAChamp); styleComboBox(parkingChamp);
        styleComboBox(disponibleChamp); styleComboBox(etatAppChamp); styleComboBox(etatPlomberieChamp);
        styleComboBox(etatElectriciteChamp); styleComboBox(etatChauffageChamp); styleComboBox(userChamp);
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