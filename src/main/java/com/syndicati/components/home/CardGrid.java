package com.syndicati.components.home;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlurType;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.ui.HorizonDesignSystem;

public class CardGrid {
    private final GridPane root;

    public CardGrid() {
        this.root = new GridPane();
        setup();
    }

    private void setup() {
        root.setHgap(24);
        root.setVgap(24);
        root.setPadding(new Insets(16));

        // 2 rows x 4 cols placeholder cards
        for (int i = 0; i < 8; i++) {
            root.add(createCard("Card " + (i + 1)), i % 4, i / 4);
        }
    }

    private StackPane createCard(String title) {
        ThemeManager theme = ThemeManager.getInstance();
        StackPane card = new StackPane();
        card.setPrefSize(240, 180);
        card.setStyle(HorizonDesignSystem.webSectionCard(28, false));

        Text t = new Text(title);
        t.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 16));
        t.setFill(Color.web(theme.getTextColor()));
        card.getChildren().add(t);
        StackPane.setAlignment(t, Pos.TOP_LEFT);
        StackPane.setMargin(t, new Insets(16));
        card.setOnMouseEntered(e -> card.setStyle(HorizonDesignSystem.webSectionCard(28, true)));
        card.setOnMouseExited(e -> card.setStyle(HorizonDesignSystem.webSectionCard(28, false)));
        HorizonDesignSystem.installWebLift(card);

        return card;
    }

    public GridPane getRoot() { return root; }
}









