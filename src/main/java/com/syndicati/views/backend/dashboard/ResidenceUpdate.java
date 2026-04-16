package com.syndicati.views.backend.dashboard;

import com.syndicati.controllers.backend.residence.ModifierResidenceController;
import com.syndicati.models.entities.Residence;
import com.syndicati.models.services.ServiceResidence;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ResidenceUpdate extends BaseDashboardPage {

    private TextField nomRMajChamp;
    private TextField adresseMAJChamp;
    private TextField nAppMAJChamp;
    private TextField dateAjoutMAJChamp;
    private TextField nBlocsMAJChamp;

    private Text nomMAJErreur;
    private Text adresseMAJErreur;
    private Text nAppMAJErreur;
    private Text dateAjoutMAJErreur;
    private Text nBlocsMAJErreur;
    private String nomResidence;


    public ResidenceUpdate(Stage stage, Scene previousScene, String nomResidence) {
        super(stage, previousScene);
        this.nomResidence = nomResidence;
    }

    @Override
    protected VBox buildContent() {

        ServiceResidence ServiceResidence= new ServiceResidence();
        Residence Residence= ServiceResidence.TrouverResidenceParNom(nomResidence);
        nomRMajChamp = new TextField();
        nomRMajChamp.setPromptText("Nom de la résidence");
        nomRMajChamp.setText(Residence.getNom_r());
        nomMAJErreur = new Text("");

        adresseMAJChamp = new TextField();
        adresseMAJChamp.setPromptText("Adresse de la résidence");
        adresseMAJChamp.setText(Residence.getAdresse());
        adresseMAJErreur = new Text("");

        nAppMAJChamp = new TextField();
        nAppMAJChamp.setPromptText("e.g. 24");
        nAppMAJChamp.setText(String.valueOf(Residence.getN_appartements()));
        nAppMAJErreur = new Text("");

        dateAjoutMAJChamp = new TextField();
        dateAjoutMAJChamp.setPromptText("Format: YYYY-MM-DD");
        dateAjoutMAJChamp.setText(Residence.getDate_ajout());
        dateAjoutMAJErreur = new Text("");

        nBlocsMAJChamp = new TextField();
        nBlocsMAJChamp.setPromptText("Exemple: A,B,C");
        nBlocsMAJChamp.setText(Residence.getN_blocs());
        nBlocsMAJErreur = new Text("");

        ModifierResidenceController controller = new ModifierResidenceController(
                stage, previousScene,
                nomRMajChamp, adresseMAJChamp,nAppMAJChamp, dateAjoutMAJChamp, nBlocsMAJChamp,
                nomMAJErreur, adresseMAJErreur, nAppMAJErreur, dateAjoutMAJErreur,  nBlocsMAJErreur, nomResidence
        );

        VBox card = glassCard();
        card.getChildren().addAll(
                fieldGroup("Nom de la Résidence", nomRMajChamp, nomMAJErreur),
                fieldGroup("Adresse", adresseMAJChamp, adresseMAJErreur),
                fieldGroup("Nombre d'Appartements", nAppMAJChamp, nAppMAJErreur),
                fieldGroup("Date d'Ajout", dateAjoutMAJChamp, dateAjoutMAJErreur),
                fieldGroup("Nombre de Blocs", nBlocsMAJChamp, nBlocsMAJErreur)
        );

        Button BoutonModifier = primaryButton("Update Residence");
        BoutonModifier.setOnAction(e -> controller.ModifierResidence());

        Button BoutonRetourner = secondaryButton("← Back");
        BoutonRetourner.setOnAction(e -> stage.setScene(previousScene));

        HBox actions = new HBox(12, BoutonRetourner, BoutonModifier);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox form = new VBox(20, pageTitle("Update Residence"), card, actions);
        form.setFillWidth(true);
        return form;
    }
}