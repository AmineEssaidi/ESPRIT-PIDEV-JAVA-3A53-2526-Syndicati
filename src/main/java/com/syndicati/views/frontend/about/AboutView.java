package com.syndicati.views.frontend.about;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlurType;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.navigation.NavigationManager;
import com.syndicati.utils.ui.HorizonDesignSystem;

/**
 * About View - Main about page with sub-menu options
 */
public class AboutView implements ViewInterface {
    
    private final VBox root;
    
    public AboutView() {
        this.root = new VBox();
        setupLayout();
    }
    
    private void setupLayout() {
        root.setSpacing(34);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(118, 48, 80, 48));
        
        // Apply theme styling
        applyThemeStyling();
        
        ThemeManager tm = ThemeManager.getInstance();

        VBox hero = new VBox(16);
        hero.setAlignment(Pos.CENTER);
        hero.setMaxWidth(1050);

        Text title = new Text("Discover our incredible team");
        title.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), FontWeight.EXTRA_BOLD, 68));
        title.setFill(tm.getAccentGradientPaint());
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        title.setWrappingWidth(1000);

        Text subtitle = new Text("The people, values, and contact paths behind the Syndicati experience.");
        subtitle.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 19));
        subtitle.setFill(Color.web(tm.getSecondaryTextColor()));
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        subtitle.setWrappingWidth(780);
        hero.getChildren().addAll(title, subtitle);

        HBox aboutGrid = new HBox();
        aboutGrid.setSpacing(24);
        aboutGrid.setAlignment(Pos.CENTER);
        aboutGrid.setMaxWidth(1100);
        
        // Option 1: Our Story
        VBox option1 = createAboutCard("ST", "Our Story", "Learn about our journey and mission");
        option1.setOnMouseClicked(e -> {
            NavigationManager.getInstance().awardInteractionXp(1);
            NavigationManager.getInstance().navigateTo("about-detail");
        });
        
        // Option 2: Our Team
        VBox option2 = createAboutCard("TM", "Our Team", "Meet the people behind our success");
        option2.setOnMouseClicked(e -> {
            NavigationManager.getInstance().awardInteractionXp(1);
            NavigationManager.getInstance().navigateTo("about-detail");
        });
        
        // Option 3: Contact Us
        VBox option3 = createAboutCard("CT", "Contact Us", "Get in touch with our team");
        option3.setOnMouseClicked(e -> {
            NavigationManager.getInstance().awardInteractionXp(1);
            NavigationManager.getInstance().navigateTo("about-detail");
        });
        
        aboutGrid.getChildren().addAll(option1, option2, option3);
        
        root.getChildren().addAll(hero, buildStatsStrip(), aboutGrid, buildValuesGrid());
    }
    
    private VBox createAboutCard(String icon, String title, String description) {
        VBox card = new VBox();
        card.setSpacing(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(34, 30, 34, 30));
        card.setMinWidth(260);
        card.setPrefWidth(320);
        card.setMaxWidth(360);
        
        // Apply card styling
        ThemeManager themeManager = ThemeManager.getInstance();
        card.setStyle(cardStyle(themeManager, false));
        
        // Add shadow effect
        HorizonDesignSystem.installWebLift(card);
        
        // Icon
        Text iconText = new Text(icon);
        iconText.setFont(Font.font(52));
        iconText.setFill(Color.web(themeManager.getAccentHex()));
        
        // Title
        Text titleText = new Text(title);
        titleText.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 24));
        titleText.setFill(Color.web(themeManager.getTextColor()));
        
        // Description
        Text descText = new Text(description);
        descText.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        descText.setFill(Color.web(themeManager.getSecondaryTextColor()));
        descText.setWrappingWidth(250);
        descText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        card.getChildren().addAll(iconText, titleText, descText);
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(cardStyle(themeManager, true));
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(cardStyle(themeManager, false));
        });
        
        return card;
    }

    private String cardStyle(ThemeManager tm, boolean hover) {
        return HorizonDesignSystem.webServiceCard(28, hover);
    }

    private HBox buildStatsStrip() {
        HBox stats = new HBox(28);
        stats.setAlignment(Pos.CENTER);
        stats.setMaxWidth(960);
        stats.setPadding(new Insets(28, 36, 28, 36));
        stats.setStyle(HorizonDesignSystem.webSectionCard(36, false));
        HorizonDesignSystem.installWebLift(stats);
        String[][] rows = {
            {"12+", "MEMBERS"},
            {"8", "COUNTRIES"},
            {"50+", "PROJECTS"},
            {"99%", "SATISFACTION"}
        };
        for (String[] row : rows) {
            VBox item = new VBox(6);
            item.setAlignment(Pos.CENTER);
            HBox.setHgrow(item, Priority.ALWAYS);
            Text number = new Text(row[0]);
            number.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), FontWeight.EXTRA_BOLD, 38));
            number.setFill(ThemeManager.getInstance().getAccentGradientPaint());
            Text label = new Text(row[1]);
            label.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 11));
            label.setFill(Color.web(ThemeManager.getInstance().getSecondaryTextColor()));
            item.getChildren().addAll(number, label);
            stats.getChildren().add(item);
        }
        return stats;
    }

    private GridPane buildValuesGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(18);
        grid.setMaxWidth(980);
        String[][] values = {
            {"IN", "Innovation", "We keep improving the resident experience with smoother tools."},
            {"CO", "Collaboration", "Community workflows are designed to feel connected and clear."},
            {"QL", "Quality", "Every interaction should feel polished, fast, and dependable."},
            {"GR", "Growth", "The platform evolves with the people using it every day."}
        };
        for (int i = 0; i < values.length; i++) {
            VBox card = createAboutCard(values[i][0], values[i][1], values[i][2]);
            card.setMinWidth(0);
            card.setPrefWidth(Region.USE_COMPUTED_SIZE);
            card.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(card, Priority.ALWAYS);
            grid.add(card, i % 2, i / 2);
        }
        for (int i = 0; i < 2; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            grid.getColumnConstraints().add(col);
        }
        return grid;
    }
    
    private void applyThemeStyling() {
        // Don't apply background color - let ImageBackground handle it
        root.setStyle("-fx-background-color: transparent;");
    }
    
    @Override
    public Pane getRoot() {
        return root;
    }
    
    public void cleanup() {
        // Cleanup resources if needed
    }
}


