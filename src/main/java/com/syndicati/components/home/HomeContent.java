package com.syndicati.components.home;

import com.syndicati.MainApplication;
import com.syndicati.services.DatabaseService;
import com.syndicati.utils.concurrent.FxAsync;
import com.syndicati.utils.image.ImageLoaderUtil;
import com.syndicati.utils.navigation.NavigationManager;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.ui.HorizonDesignSystem;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * HomeContent aligned with the real website home under templates/frontend/home.
 */
public class HomeContent {

    private final VBox root;
    private final ThemeManager theme = ThemeManager.getInstance();
    private HomeData homeData;

    public HomeContent() {
        homeData = fallbackHomeData();
        root = new VBox(42);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(24, 22, 56, 22));
        root.setMaxWidth(Double.MAX_VALUE);

        renderSections();
        hydrateHomeDataAsync();
    }

    public VBox getRoot() {
        return root;
    }

    private void renderSections() {
        root.getChildren().setAll(
            buildHero(),
            buildComplexHeader(),
            buildResidencesSection(),
            buildStatsSection(),
            buildEventsSection(),
            buildCommunitySection(),
            buildServicesSection(),
            buildHelpCta()
        );
    }

    private void hydrateHomeDataAsync() {
        FxAsync.supplyIo(this::loadHomeData)
            .thenAccept(data -> FxAsync.onFx(() -> {
                homeData = data;
                renderSections();
            }))
            .exceptionally(error -> {
                System.out.println("HomeContent async load error: " + FxAsync.message(error));
                return null;
            });
    }

    private StackPane buildHero() {
        StackPane wrapper = new StackPane();
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setPadding(new Insets(40, 0, 18, 0));

        VBox card = new VBox(24);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(1180);
        card.setMinHeight(560);
        card.setPadding(new Insets(82, 78, 82, 78));
        card.setStyle(HorizonDesignSystem.webHeroPanel());

        HBox badge = new HBox(8);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(10, 20, 10, 20));
        badge.setStyle(HorizonDesignSystem.webAccentBadge());
        Text badgeText = new Text("RESIDENTIAL EXPERIENCE");
        badgeText.setFont(Font.font(boldFont(), FontWeight.BOLD, 11));
        badgeText.setFill(Color.WHITE);
        badge.getChildren().add(badgeText);

        HBox titleRow = new HBox(2);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Text title = new Text("Syndicati");
        title.setFont(Font.font(boldFont(), FontWeight.EXTRA_BOLD, 92));
        title.setFill(Color.WHITE);
        Text dot = new Text(".");
        dot.setFont(Font.font(boldFont(), FontWeight.EXTRA_BOLD, 92));
        dot.setFill(Color.web(theme.getAccentHex()));
        applyGlow(dot, 18, 0.7);
        titleRow.getChildren().addAll(title, dot);

        Text desc = new Text("A secure, connected living platform for residences, complaints, events, forums, maintenance, and community engagement.");
        desc.setFont(Font.font(lightFont(), FontWeight.NORMAL, 20));
        desc.setFill(Color.web("rgba(255,255,255,0.56)"));
        desc.setWrappingWidth(760);
        desc.setLineSpacing(5);

        Button exploreButton = buildPrimaryButton("Explore the Complex");
        exploreButton.setOnAction(e -> navigate("residence"));
        Button hubButton = buildSecondaryButton("Join the Hub");
        hubButton.setOnAction(e -> navigate("forum"));
        HBox actions = new HBox(16, exploreButton, hubButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(badge, titleRow, desc, actions);
        Region glow = new Region();
        glow.setPrefSize(640, 640);
        glow.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 55%, " + theme.toRgba(theme.getAccentHex(), 0.24) + ", transparent);");
        glow.setTranslateX(260);
        StackPane.setAlignment(glow, Pos.CENTER_RIGHT);

        wrapper.getChildren().addAll(glow, card);
        StackPane.setAlignment(card, Pos.CENTER_LEFT);

        FadeTransition fade = new FadeTransition(Duration.seconds(1.1), card);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        TranslateTransition rise = new TranslateTransition(Duration.seconds(1.1), card);
        rise.setFromY(26);
        rise.setToY(0);
        rise.play();

        return wrapper;
    }

    private VBox buildComplexHeader() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);

        HBox row = new HBox(2);
        row.setAlignment(Pos.CENTER);
        Text title = new Text("Our Complex");
        title.setFont(Font.font(boldFont(), FontWeight.BOLD, 42));
        title.setFill(Color.web(theme.getTextColor()));
        Text dot = new Text(".");
        dot.setFont(Font.font(boldFont(), FontWeight.BOLD, 42));
        dot.setFill(Color.web(theme.getAccentHex()));
        applyGlow(dot, 12, 0.6);
        row.getChildren().addAll(title, dot);

        Text sub = new Text("Discover our prestige residences and high-end services.");
        sub.setFont(Font.font(lightFont(), FontWeight.NORMAL, 15));
        sub.setFill(Color.web(theme.getSecondaryTextColor()));
        sub.setTextAlignment(TextAlignment.CENTER);

        box.getChildren().addAll(row, sub);
        return box;
    }

    private GridPane buildResidencesSection() {
        GridPane grid = new GridPane();
        grid.setHgap(22);
        grid.setVgap(22);
        grid.setAlignment(Pos.CENTER);
        grid.setMaxWidth(Double.MAX_VALUE);

        List<ResidencePreview> items = homeData.residences;

        for (int i = 0; i < items.size(); i++) {
            ResidencePreview item = items.get(i);
            VBox card = buildResidenceCard(item);
            GridPane.setFillWidth(card, false);
            grid.add(card, i % 4, i / 4);
        }

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(0);
            col.setFillWidth(false);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        return grid;
    }

    private VBox buildResidenceCard(ResidencePreview item) {
        VBox card = new VBox(14);
        card.setMinWidth(275);
        card.setPrefWidth(315);
        card.setMaxWidth(340);
        card.setMinHeight(430);
        card.setPadding(new Insets(12));
        card.setStyle(HorizonDesignSystem.webServiceCard(32, false));

        StackPane imageWrap = new StackPane();
        imageWrap.setMinWidth(291);
        imageWrap.setPrefWidth(291);
        imageWrap.setMaxWidth(291);
        imageWrap.setMinHeight(200);
        imageWrap.setPrefHeight(200);
        imageWrap.setMaxHeight(200);
        imageWrap.setStyle(
            "-fx-background-color: #0a0a0a;" +
            "-fx-background-radius: 24px;" +
            "-fx-border-color: rgba(255,255,255,0.06);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 24px;"
        );
        installRoundedClip(imageWrap, 24);
        ImageView cover = buildCoverImage(item.image(), imageWrap, 291, 200);
        if (cover != null) {
            imageWrap.getChildren().add(cover);
        } else {
            Region light = new Region();
            light.setMaxWidth(Double.MAX_VALUE);
            light.setMaxHeight(100);
            light.setStyle(
                "-fx-background-color: " + theme.toRgba(theme.getAccentHex(), 0.18) + ";" +
                "-fx-background-radius: 12px;"
            );
            light.setRotate(-10);
            Text icon = new Text("RES");
            icon.setFont(Font.font(48));
            imageWrap.getChildren().addAll(light, icon);
        }
        StackPane badgeWrap = new StackPane();
        badgeWrap.setPadding(new Insets(8, 12, 8, 12));
        badgeWrap.setStyle(
            "-fx-background-color: " + theme.toRgba(theme.getAccentHex(), 0.9) + ";" +
            "-fx-background-radius: 999px;"
        );
        Text badgeText = new Text("NEW");
        badgeText.setFont(Font.font(boldFont(), FontWeight.BOLD, 10));
        badgeText.setFill(Color.WHITE);
        badgeWrap.getChildren().add(badgeText);
        lockToContentSize(badgeWrap);
        StackPane.setAlignment(badgeWrap, Pos.TOP_LEFT);
        StackPane.setMargin(badgeWrap, new Insets(14, 0, 0, 14));
        imageWrap.getChildren().add(badgeWrap);

        Text titleText = new Text(item.name());
        titleText.setFont(Font.font(boldFont(), FontWeight.EXTRA_BOLD, 24));
        titleText.setFill(Color.web(theme.getTextColor()));
        titleText.setWrappingWidth(280);
        Text locText = new Text(item.address());
        locText.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        locText.setFill(Color.web(theme.getSecondaryTextColor()));
        locText.setWrappingWidth(280);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        Text type = new Text("RESIDENCE");
        type.setFont(Font.font(boldFont(), FontWeight.BOLD, 11));
        type.setFill(Color.web(theme.getAccentHex()));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        StackPane go = new StackPane(new Text("->"));
        go.setPrefSize(34, 34);
        go.setStyle(
            "-fx-background-color: " + theme.toRgba(theme.getAccentHex(), 0.12) + ";" +
            "-fx-background-radius: 17px;"
        );
        footer.getChildren().addAll(type, spacer, go);

        VBox.setMargin(titleText, new Insets(2, 8, 0, 8));
        VBox.setMargin(locText, new Insets(0, 8, 0, 8));
        VBox.setMargin(footer, new Insets(8, 8, 4, 8));
        card.getChildren().addAll(imageWrap, titleText, locText, footer);
        card.setOnMouseEntered(e -> card.setStyle(HorizonDesignSystem.webServiceCard(32, true)));
        card.setOnMouseExited(e -> card.setStyle(HorizonDesignSystem.webServiceCard(32, false)));
        card.setOnMouseClicked(e -> navigate("residence"));
        addHoverLift(card, -15, 1.02);
        return card;
    }

    private HBox buildStatsSection() {
        HBox row = new HBox(30);
        row.setAlignment(Pos.CENTER);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setPadding(new Insets(44));
        row.setStyle(HorizonDesignSystem.webSectionCard(44, false));
        HorizonDesignSystem.installWebLift(row);

        String[][] stats = {
            {String.valueOf(homeData.residenceCount), "RESIDENCES"},
            {String.valueOf(homeData.apartmentCount), "APARTMENTS"},
            {String.valueOf(homeData.residentCount), "RESIDENTS"},
            {String.valueOf(homeData.eventCount), "EVENTS"}
        };

        for (String[] stat : stats) {
            VBox item = new VBox(8);
            item.setAlignment(Pos.CENTER);
            HBox.setHgrow(item, Priority.ALWAYS);
            Text num = new Text(stat[0]);
            num.setFont(Font.font(boldFont(), FontWeight.BOLD, 44));
            num.setFill(theme.getAccentGradientPaint());
            applyGlow(num, 10, 0.45);
            Text label = new Text(stat[1]);
            label.setFont(Font.font(boldFont(), FontWeight.BOLD, 11));
            label.setFill(Color.web(theme.getSecondaryTextColor()));
            item.getChildren().addAll(num, label);
            row.getChildren().add(item);
        }

        return row;
    }

    private VBox buildEventsSection() {
        VBox section = new VBox(28);
        section.setAlignment(Pos.TOP_CENTER);
        section.setMaxWidth(Double.MAX_VALUE);
        section.getChildren().add(sectionHeader("Upcoming Events", "Join the vibrant activities of our community.", "View all", "evenement"));

        GridPane grid = new GridPane();
        grid.setHgap(26);
        grid.setVgap(26);
        grid.setAlignment(Pos.CENTER);
        grid.setMaxWidth(Double.MAX_VALUE);

        List<EventPreview> events = homeData.events;

        for (int i = 0; i < events.size(); i++) {
            EventPreview event = events.get(i);
            VBox card = buildEventCard(event);
            GridPane.setFillWidth(card, false);
            grid.add(card, i % 3, i / 3);
        }

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(0);
            col.setFillWidth(false);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        section.getChildren().add(grid);
        return section;
    }

    private VBox buildEventCard(EventPreview event) {
        VBox card = new VBox();
        card.setMinWidth(360);
        card.setPrefWidth(420);
        card.setMaxWidth(460);
        card.setMinHeight(520);
        card.setStyle(HorizonDesignSystem.webServiceCard(34, false));

        StackPane media = new StackPane();
        media.setMinWidth(390);
        media.setPrefWidth(390);
        media.setMaxWidth(390);
        media.setMinHeight(220);
        media.setPrefHeight(220);
        media.setMaxHeight(220);
        media.setStyle(
            "-fx-background-color: " + theme.toRgba(theme.getAccentHex(), 0.20) + ";" +
            "-fx-background-radius: 28px;"
        );
        installRoundedClip(media, 28);
        ImageView cover = buildCoverImage(event.image(), media, 390, 220);
        if (cover != null) {
            media.getChildren().add(cover);
        } else {
            Text icon = new Text("EVENTS");
            icon.setFont(Font.font(boldFont(), FontWeight.BOLD, 34));
            icon.setFill(Color.web(theme.toRgba(theme.getTextColor(), 0.65)));
            media.getChildren().add(icon);
        }
        VBox.setMargin(media, new Insets(15, 15, 0, 15));
        StackPane tag = new StackPane();
        tag.setPadding(new Insets(8, 12, 8, 12));
        tag.setStyle(
            "-fx-background-color: " + theme.toRgba(theme.getAccentHex(), 0.9) + ";" +
            "-fx-background-radius: 999px;"
        );
        Text tagText = new Text(safe(event.type(), "Event").toUpperCase());
        tagText.setFont(Font.font(boldFont(), FontWeight.BOLD, 10));
        tagText.setFill(Color.WHITE);
        tag.getChildren().add(tagText);
        lockToContentSize(tag);
        StackPane.setAlignment(tag, Pos.TOP_RIGHT);
        StackPane.setMargin(tag, new Insets(16, 16, 0, 0));
        media.getChildren().add(tag);

        VBox body = new VBox(14);
        body.setPadding(new Insets(26, 30, 18, 30));
        VBox.setVgrow(body, Priority.ALWAYS);
        Text titleText = new Text(event.title());
        titleText.setFont(Font.font(boldFont(), FontWeight.EXTRA_BOLD, 26));
        titleText.setFill(Color.web(theme.getTextColor()));
        titleText.setWrappingWidth(360);
        HBox meta = new HBox(18);
        meta.setAlignment(Pos.CENTER_LEFT);
        Text dateText = new Text(event.date());
        dateText.setFont(Font.font(boldFont(), FontWeight.BOLD, 11));
        dateText.setFill(Color.web(theme.getSecondaryTextColor()));
        Text placeText = new Text(event.place());
        placeText.setFont(Font.font(boldFont(), FontWeight.BOLD, 11));
        placeText.setFill(Color.web(theme.getSecondaryTextColor()));
        placeText.setWrappingWidth(160);
        meta.getChildren().addAll(dateText, placeText);
        Label descText = new Label(event.description());
        descText.setWrapText(true);
        descText.setMaxWidth(Double.MAX_VALUE);
        descText.setMinHeight(58);
        descText.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13.5));
        descText.setTextFill(Color.web(theme.getSecondaryTextColor()));
        descText.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20, 30, 24, 30));
        footer.setStyle(
            "-fx-background-color: " + theme.toRgba(theme.getTextColor(), 0.025) + ";" +
            "-fx-border-color: " + theme.toRgba(theme.getTextColor(), 0.06) + " transparent transparent transparent;" +
            "-fx-border-width: 1px 0 0 0;"
        );
        Text more = new Text("Event details ->");
        more.setFont(Font.font(boldFont(), FontWeight.BOLD, 11));
        more.setFill(Color.web(theme.getAccentHex()));
        footer.getChildren().add(more);
        body.getChildren().addAll(titleText, meta, descText);

        card.getChildren().addAll(media, body, footer);
        card.setOnMouseEntered(e -> card.setStyle(HorizonDesignSystem.webServiceCard(34, true)));
        card.setOnMouseExited(e -> card.setStyle(HorizonDesignSystem.webServiceCard(34, false)));
        card.setOnMouseClicked(e -> navigate("evenement"));
        addHoverLift(card, -10, 1.02);
        return card;
    }

    private VBox buildCommunitySection() {
        VBox section = new VBox(28);
        section.setAlignment(Pos.TOP_CENTER);
        section.setMaxWidth(Double.MAX_VALUE);
        section.getChildren().add(sectionHeader("Community Focus", "Discover the latest discussions from your neighbors.", "Open the Forum", "forum"));

        GridPane grid = new GridPane();
        grid.setHgap(26);
        grid.setVgap(26);
        grid.setMaxWidth(Double.MAX_VALUE);

        List<ForumPreview> posts = homeData.forumPosts;

        for (int i = 0; i < posts.size(); i++) {
            ForumPreview post = posts.get(i);
            VBox card = buildCommunityCard(post);
            GridPane.setFillWidth(card, true);
            card.setMaxWidth(Double.MAX_VALUE);
            grid.add(card, i % 3, i / 3);
        }

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(0);
            col.setFillWidth(true);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        section.getChildren().add(grid);
        return section;
    }

    private VBox buildCommunityCard(ForumPreview post) {
        VBox card = new VBox(18);
        card.setPadding(new Insets(28));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(HorizonDesignSystem.webServiceCard(28, false));

        HBox userRow = new HBox(12);
        userRow.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = new StackPane();
        avatar.setMinSize(48, 48);
        avatar.setPrefSize(48, 48);
        avatar.setMaxSize(48, 48);
        avatar.setStyle(
            "-fx-background-color: " + theme.getEffectiveAccentGradient() + ";" +
            "-fx-background-radius: 16px;"
        );
        installRoundedClip(avatar, 16);
        ImageView avatarImage = buildCoverImage(post.avatar(), avatar, 48, 48);
        if (avatarImage != null) {
            avatar.getChildren().add(avatarImage);
        } else {
            Text initials = new Text(getInitials(post.author()));
            initials.setFont(Font.font(boldFont(), FontWeight.BOLD, 12));
            initials.setFill(Color.WHITE);
            avatar.getChildren().add(initials);
        }
        VBox info = new VBox(2);
        Text userText = new Text(post.author());
        userText.setFont(Font.font(boldFont(), FontWeight.BOLD, 15));
        userText.setFill(Color.web(theme.getTextColor()));
        Text dateText = new Text(post.date());
        dateText.setFont(Font.font(lightFont(), FontWeight.NORMAL, 11));
        dateText.setFill(Color.web(theme.getSecondaryTextColor()));
        info.getChildren().addAll(userText, dateText);
        userRow.getChildren().addAll(avatar, info);

        Text titleText = new Text(post.title());
        titleText.setFont(Font.font(boldFont(), FontWeight.BOLD, 24));
        titleText.setFill(Color.web(theme.getTextColor()));
        Label descText = new Label(post.description());
        descText.setWrapText(true);
        descText.setMaxWidth(Double.MAX_VALUE);
        descText.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13.5));
        descText.setTextFill(Color.web(theme.getSecondaryTextColor()));
        descText.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        Text left = new Text("Discussion");
        left.setFont(Font.font(boldFont(), FontWeight.BOLD, 11));
        left.setFill(Color.web(theme.getSecondaryTextColor()));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Text right = new Text(post.category());
        right.setFont(Font.font(boldFont(), FontWeight.BOLD, 11));
        right.setFill(Color.web(theme.getAccentHex()));
        footer.getChildren().addAll(left, spacer, right);

        card.getChildren().addAll(userRow, titleText, descText, footer);
        card.setOnMouseEntered(e -> card.setStyle(HorizonDesignSystem.webServiceCard(28, true)));
        card.setOnMouseExited(e -> card.setStyle(HorizonDesignSystem.webServiceCard(28, false)));
        card.setOnMouseClicked(e -> navigate("forum"));
        addHoverLift(card, -12, 1.01);
        return card;
    }

    private VBox buildServicesSection() {
        VBox section = new VBox(28);
        section.setAlignment(Pos.TOP_CENTER);
        section.setMaxWidth(Double.MAX_VALUE);
        section.getChildren().add(centeredTitle("Our Services", "A complete set of tools for secure, organized residential living."));

        GridPane grid = new GridPane();
        grid.setHgap(22);
        grid.setVgap(22);
        grid.setMaxWidth(Double.MAX_VALUE);

        String[][] services = {
            {"RES", "Residential Management", "Manage residences, apartments, and residents with clarity and ease."},
            {"FIX", "Maintenance & SAV", "Track complaints and schedule technical interventions from one place."},
            {"CHAT", "Communication", "Stay connected with neighbors through the forum and messaging system."},
            {"PAY", "Secure Payments", "Pay residence and syndic fees online through secure payment options."}
        };

        for (int i = 0; i < services.length; i++) {
            VBox card = buildServiceCard(services[i][0], services[i][1], services[i][2]);
            GridPane.setFillWidth(card, true);
            card.setMaxWidth(Double.MAX_VALUE);
            grid.add(card, i % 4, i / 4);
        }

        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(0);
            col.setFillWidth(true);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        section.getChildren().add(grid);
        return section;
    }

    private VBox buildServiceCard(String icon, String title, String desc) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(26));
        card.setMinHeight(220);
        card.setStyle(HorizonDesignSystem.webServiceCard(24, false));

        Text iconText = new Text(icon);
        iconText.setFont(Font.font(34));
        Text titleText = new Text(title);
        titleText.setFont(Font.font(boldFont(), FontWeight.BOLD, 18));
        titleText.setFill(Color.web(theme.getTextColor()));
        titleText.setTextAlignment(TextAlignment.CENTER);
        Text descText = new Text(desc);
        descText.setWrappingWidth(210);
        descText.setTextAlignment(TextAlignment.CENTER);
        descText.setFont(Font.font(lightFont(), FontWeight.NORMAL, 13));
        descText.setFill(Color.web(theme.getSecondaryTextColor()));
        card.getChildren().addAll(iconText, titleText, descText);

        card.setOnMouseEntered(e -> card.setStyle(HorizonDesignSystem.webServiceCard(24, true)));
        card.setOnMouseExited(e -> card.setStyle(HorizonDesignSystem.webServiceCard(24, false)));
        addHoverLift(card, -8, 1.02);
        return card;
    }

    private StackPane buildHelpCta() {
        StackPane cta = new StackPane();
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(860);
        card.setPadding(new Insets(52));
        card.setStyle(HorizonDesignSystem.webSectionCard(40, false));
        HorizonDesignSystem.installWebLift(card);

        HBox row = new HBox(2);
        row.setAlignment(Pos.CENTER);
        Text title = new Text("Need help?");
        title.setFont(Font.font(boldFont(), FontWeight.BOLD, 42));
        title.setFill(Color.web(theme.getTextColor()));
        Text dot = new Text(".");
        dot.setFont(Font.font(boldFont(), FontWeight.BOLD, 42));
        dot.setFill(Color.web(theme.getAccentHex()));
        applyGlow(dot, 12, 0.65);
        row.getChildren().addAll(title, dot);

        Text desc = new Text("Questions about your residence or need technical support? Our team is ready to help.");
        desc.setFont(Font.font(lightFont(), FontWeight.NORMAL, 16));
        desc.setFill(Color.web(theme.getSecondaryTextColor()));
        desc.setTextAlignment(TextAlignment.CENTER);
        desc.setWrappingWidth(560);

        Button contact = buildPrimaryButton("Contact the Syndic");
        contact.setOnAction(e -> navigate("syndicat"));
        Button report = buildSecondaryButton("Report an Issue");
        report.setOnAction(e -> navigate("syndicat"));
        HBox buttons = new HBox(18, contact, report);
        buttons.setAlignment(Pos.CENTER);

        card.getChildren().addAll(row, desc, buttons);
        cta.getChildren().add(card);
        return cta;
    }

    private VBox sectionHeader(String title, String subtitle, String action) {
        return sectionHeader(title, subtitle, action, null);
    }

    private VBox sectionHeader(String title, String subtitle, String action, String route) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);

        HBox row = new HBox(2);
        row.setAlignment(Pos.CENTER);
        Text titleText = new Text(title);
        titleText.setFont(Font.font(boldFont(), FontWeight.BOLD, 36));
        titleText.setFill(Color.web(theme.getTextColor()));
        Text dot = new Text(".");
        dot.setFont(Font.font(boldFont(), FontWeight.BOLD, 36));
        dot.setFill(Color.web(theme.getAccentHex()));
        applyGlow(dot, 10, 0.55);
        row.getChildren().addAll(titleText, dot);

        Text sub = new Text(subtitle);
        sub.setFont(Font.font(lightFont(), FontWeight.NORMAL, 14));
        sub.setFill(Color.web(theme.getSecondaryTextColor()));
        sub.setWrappingWidth(620);
        sub.setTextAlignment(TextAlignment.CENTER);

        Button link = buildGhostLink(action);
        if (route != null && !route.isBlank()) {
            link.setOnAction(e -> navigate(route));
        }
        box.getChildren().addAll(row, sub, link);
        return box;
    }

    private VBox centeredTitle(String title, String subtitle) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        HBox row = new HBox(2);
        row.setAlignment(Pos.CENTER);
        Text t = new Text(title);
        t.setFont(Font.font(boldFont(), FontWeight.BOLD, 36));
        t.setFill(Color.web(theme.getTextColor()));
        Text dot = new Text(".");
        dot.setFont(Font.font(boldFont(), FontWeight.BOLD, 36));
        dot.setFill(Color.web(theme.getAccentHex()));
        row.getChildren().addAll(t, dot);
        Text sub = new Text(subtitle);
        sub.setFont(Font.font(lightFont(), FontWeight.NORMAL, 14));
        sub.setFill(Color.web(theme.getSecondaryTextColor()));
        sub.setWrappingWidth(620);
        sub.setTextAlignment(TextAlignment.CENTER);
        box.getChildren().addAll(row, sub);
        return box;
    }

    private Button buildGhostLink(String label) {
        Button btn = new Button(label + " ->");
        btn.setFont(Font.font(boldFont(), FontWeight.BOLD, 12));
        btn.setFocusTraversable(false);
        btn.setStyle(
            "-fx-background-color: " + theme.toRgba(theme.getAccentHex(), 0.10) + ";" +
            "-fx-text-fill: " + theme.getAccentHex() + ";" +
            "-fx-background-radius: 18px;" +
            "-fx-border-color: " + theme.toRgba(theme.getAccentHex(), 0.15) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 18px;" +
            "-fx-padding: 12 22 12 22;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(160), btn);
            tt.setToX(8);
            tt.play();
        });
        btn.setOnMouseExited(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(160), btn);
            tt.setToX(0);
            tt.play();
        });
        return btn;
    }

    private Button buildPrimaryButton(String label) {
        Button btn = new Button(label);
        btn.setFont(Font.font(boldFont(), FontWeight.BOLD, 14));
        btn.setFocusTraversable(false);
        btn.setStyle(
            "-fx-background-color: " + theme.getEffectiveAccentGradient() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 20px;" +
            "-fx-border-radius: 20px;" +
            "-fx-padding: 16 30 16 30;" +
            "-fx-cursor: hand;"
        );
        addButtonHover(btn);
        return btn;
    }

    private Button buildSecondaryButton(String label) {
        Button btn = new Button(label);
        btn.setFont(Font.font(boldFont(), FontWeight.BOLD, 14));
        btn.setFocusTraversable(false);
        btn.setStyle(
            "-fx-background-color: " + secondaryButtonBg() + ";" +
            "-fx-text-fill: " + theme.getTextColor() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + secondaryButtonBorder() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;" +
            "-fx-padding: 16 30 16 30;" +
            "-fx-cursor: hand;"
        );
        addButtonHover(btn);
        return btn;
    }

    private void addButtonHover(Button btn) {
        HorizonDesignSystem.installLift(btn, 1.025, -3);
        HorizonDesignSystem.installButtonMotion(btn);
    }

    private String shellStyle(double radius, String innerBg, double inset) {
        return HorizonDesignSystem.webSectionCard((int) Math.round(radius), false);
    }

    private String cardSurface() {
        return HorizonDesignSystem.surface();
    }

    private String cardSurfaceSoft() {
        return HorizonDesignSystem.surfaceSoft();
    }

    private String cardBorder() {
        return HorizonDesignSystem.borderStrong();
    }

    private String secondaryButtonBg() {
        return HorizonDesignSystem.surfaceSoft();
    }

    private String secondaryButtonBorder() {
        return HorizonDesignSystem.borderStrong();
    }

    private void addHoverLift(Region node, double translateY, double scale) {
        node.setOnMouseEntered(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), node);
            tt.setToY(translateY);
            tt.setInterpolator(HorizonDesignSystem.WEB_EASE);
            tt.play();
            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(scale);
            st.setToY(scale);
            st.setInterpolator(HorizonDesignSystem.WEB_EASE);
            st.play();
        });
        node.setOnMouseExited(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), node);
            tt.setToY(0);
            tt.setInterpolator(HorizonDesignSystem.WEB_EASE);
            tt.play();
            ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.setInterpolator(HorizonDesignSystem.WEB_EASE);
            st.play();
        });
    }

    private ImageView buildCoverImage(String imagePath, StackPane container, double width, double height) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        Image image = ImageLoaderUtil.loadImage(imagePath, true);
        if (image == null) {
            return null;
        }
        ImageView view = new ImageView(image);
        view.setPreserveRatio(false);
        view.setSmooth(true);
        view.setCache(true);
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setManaged(false);
        StackPane.setAlignment(view, Pos.CENTER);
        return view;
    }

    private void installRoundedClip(StackPane pane, double radius) {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(pane.widthProperty());
        clip.heightProperty().bind(pane.heightProperty());
        clip.setArcWidth(radius);
        clip.setArcHeight(radius);
        pane.setClip(clip);
    }

    private void lockToContentSize(Region region) {
        region.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        region.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        region.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    private void navigate(String route) {
        try {
            NavigationManager.getInstance().navigateTo(route);
        } catch (Exception e) {
            System.out.println("HomeContent.navigate error: " + e.getMessage());
        }
    }

    private String getInitials(String user) {
        if (user == null || user.isBlank()) {
            return "U";
        }
        String[] parts = user.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
            }
            if (sb.length() == 2) {
                break;
            }
        }
        return sb.length() == 0 ? "U" : sb.toString();
    }

    private void addShadow(javafx.scene.Node node, double radius, double opacity) {
        DropShadow ds = new DropShadow();
        ds.setBlurType(BlurType.ONE_PASS_BOX);
        ds.setColor(Color.web(theme.getAccentHex()).deriveColor(0, 1, 1.15, opacity));
        ds.setRadius(radius);
        ds.setOffsetX(0);
        ds.setOffsetY(8);
        node.setEffect(ds);
    }

    private void applyGlow(javafx.scene.Node node, double radius, double opacity) {
        DropShadow glow = new DropShadow();
        glow.setBlurType(BlurType.ONE_PASS_BOX);
        glow.setColor(Color.web(theme.getAccentHex()).deriveColor(0, 1, 1.2, opacity));
        glow.setRadius(radius);
        glow.setOffsetX(0);
        glow.setOffsetY(0);
        node.setEffect(glow);
    }

    private String boldFont() {
        return MainApplication.getInstance().getBoldFontFamily();
    }

    private String lightFont() {
        return MainApplication.getInstance().getLightFontFamily();
    }

    private HomeData fallbackHomeData() {
        HomeData data = new HomeData();
        data.ensureFallbacks();
        return data;
    }

    private HomeData loadHomeData() {
        HomeData data = new HomeData();
        data.residentCount = count("SELECT COUNT(*) FROM user");
        data.residenceCount = count("SELECT COUNT(*) FROM residence");
        data.apartmentCount = count("SELECT COUNT(*) FROM appartement");
        data.eventCount = count("SELECT COUNT(*) FROM evenement");
        data.residences.addAll(loadResidences());
        data.events.addAll(loadEvents());
        data.forumPosts.addAll(loadForumPosts());
        data.ensureFallbacks();
        return data;
    }

    private int count(String sql) {
        try (Connection conn = DatabaseService.getInstance().getConnection()) {
            if (conn == null) return 0;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.out.println("HomeContent.count error: " + e.getMessage());
            return 0;
        }
    }

    private List<ResidencePreview> loadResidences() {
        List<ResidencePreview> out = new ArrayList<>();
        String sql = "SELECT id_residence, nom_r, adresse, image_r FROM residence ORDER BY date_ajout DESC, id_residence DESC LIMIT 4";
        try (Connection conn = DatabaseService.getInstance().getConnection()) {
            if (conn == null) return out;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ResidencePreview(
                        rs.getInt("id_residence"),
                        safe(rs.getString("nom_r"), "Residence"),
                        safe(rs.getString("adresse"), "Syndicati"),
                        rs.getString("image_r")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("HomeContent.loadResidences error: " + e.getMessage());
        }
        return out;
    }

    private List<EventPreview> loadEvents() {
        List<EventPreview> out = new ArrayList<>();
        String sql = "SELECT id_event, titre_event, DATE_FORMAT(date_event, '%d %b %Y') AS event_date, lieu_event, description_event, type_event, image_event " +
            "FROM evenement WHERE date_event >= CURDATE() AND (statut_event IS NULL OR statut_event <> 'annule') " +
            "ORDER BY date_event ASC, edited_at DESC LIMIT 3";
        try (Connection conn = DatabaseService.getInstance().getConnection()) {
            if (conn == null) return out;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new EventPreview(
                        rs.getInt("id_event"),
                        safe(rs.getString("titre_event"), "Syndicati Event"),
                        safe(rs.getString("event_date"), "Upcoming"),
                        safe(rs.getString("lieu_event"), "Syndicati"),
                        limit(safe(rs.getString("description_event"), "Join the activities of our community."), 150),
                        safe(rs.getString("type_event"), "Event"),
                        rs.getString("image_event")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("HomeContent.loadEvents error: " + e.getMessage());
        }
        return out;
    }

    private List<ForumPreview> loadForumPosts() {
        List<ForumPreview> out = new ArrayList<>();
        String sql = "SELECT p.id, p.titre_pub, p.description_pub, p.categorie_pub, DATE_FORMAT(p.date_creation_pub, '%d/%m/%Y') AS pub_date, " +
            "COALESCE(NULLIF(TRIM(CONCAT(u.first_name, ' ', u.last_name)), ''), u.email_user, 'Syndicati Member') AS author, pr.avatar " +
            "FROM publication p LEFT JOIN user u ON u.id_user = p.user_id LEFT JOIN profile pr ON pr.user_id = u.id_user " +
            "ORDER BY p.date_creation_pub DESC LIMIT 3";
        try (Connection conn = DatabaseService.getInstance().getConnection()) {
            if (conn == null) return out;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ForumPreview(
                        rs.getInt("id"),
                        safe(rs.getString("author"), "Syndicati Member"),
                        rs.getString("avatar"),
                        safe(rs.getString("pub_date"), "Recent"),
                        safe(rs.getString("titre_pub"), "Community update"),
                        limit(safe(rs.getString("description_pub"), "A new discussion in your community."), 120),
                        safe(rs.getString("categorie_pub"), "Discussion")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("HomeContent.loadForumPosts error: " + e.getMessage());
        }
        return out;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String limit(String value, int max) {
        if (value == null || value.length() <= max) return value;
        return value.substring(0, Math.max(0, max - 3)).trim() + "...";
    }

    private static final class HomeData {
        int residentCount;
        int residenceCount;
        int apartmentCount;
        int eventCount;
        final List<ResidencePreview> residences = new ArrayList<>();
        final List<EventPreview> events = new ArrayList<>();
        final List<ForumPreview> forumPosts = new ArrayList<>();

        void ensureFallbacks() {
            if (residences.isEmpty()) {
                residences.add(new ResidencePreview(null, "Residence Azure", "Les Berges du Lac", null));
                residences.add(new ResidencePreview(null, "Palm Heights", "La Marsa", null));
                residences.add(new ResidencePreview(null, "Jardin Central", "Mutuelleville", null));
                residences.add(new ResidencePreview(null, "Sana Residence", "Ariana", null));
            }
            if (events.isEmpty()) {
                events.add(new EventPreview(null, "General Assembly", "14 Mar 2026", "Club House", "Annual meeting to discuss important residence decisions.", "Meeting", null));
                events.add(new EventPreview(null, "Clean Community Day", "21 Mar 2026", "Central Garden", "A shared moment to care for common spaces.", "Community", null));
                events.add(new EventPreview(null, "Residents Night", "29 Mar 2026", "Rooftop", "A friendly evening to bring neighbors closer.", "Social", null));
            }
            if (forumPosts.isEmpty()) {
                forumPosts.add(new ForumPreview(null, "Amina Trabelsi", null, "12/03/2026", "Interior garden improvement", "What do you think about adding a new relaxation area near the main entrance?", "Ideas"));
                forumPosts.add(new ForumPreview(null, "Youssef Ben Ali", null, "11/03/2026", "Block B elevator maintenance", "Work starts Friday. Please share any specific needs before tomorrow.", "Maintenance"));
                forumPosts.add(new ForumPreview(null, "Sarra Gharbi", null, "10/03/2026", "Community evening", "Proposal for a friendly gathering in the common area this weekend.", "Community"));
            }
        }
    }

    private record ResidencePreview(Integer id, String name, String address, String image) {}
    private record EventPreview(Integer id, String title, String date, String place, String description, String type, String image) {}
    private record ForumPreview(Integer id, String author, String avatar, String date, String title, String description, String category) {}
}




