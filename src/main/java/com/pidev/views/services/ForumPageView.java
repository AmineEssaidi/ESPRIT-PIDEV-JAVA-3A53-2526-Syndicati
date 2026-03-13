package com.pidev.views.services;

import com.pidev.MainApplication;
import com.pidev.interfaces.ViewInterface;
import com.pidev.utils.theme.ThemeManager;
import javafx.beans.binding.Bindings;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Forum page UI duplicated from frontend/forum/index.html.twig and forum.css.
 * UI-only for now; behavior is intentionally local and non-persistent.
 */
public class ForumPageView implements ViewInterface {

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();

    private final StackPane faceStack = new StackPane();
    private VBox readFace;
    private VBox createFace;
    private VBox editFace;

    private Text detailCategory;
    private Text detailDate;
    private Text detailTitle;
    private Text detailAuthor;
    private Text detailDescription;

    public ForumPageView() {
        root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(28, 0, 40, 0));
        root.setMaxWidth(Double.MAX_VALUE);
        root.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(buildHero(), buildSplitContainer());
    }

    private StackPane buildHero() {
        StackPane hero = new StackPane();
        hero.setMaxWidth(1800);
        hero.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        hero.setMinHeight(300);
        hero.setPrefHeight(300);
        hero.setMaxHeight(300);
        hero.setPadding(new Insets(18, 36, 18, 36));
        hero.setStyle(
            "-fx-background-color: #000000;" +
            "-fx-background-radius: 48px;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 48px;"
        );

        // Accent glow blob like forum.css .forum-hero-bg-glow
        Region glow = new Region();
        glow.setPrefSize(900, 900);
        glow.setStyle(
            "-fx-background-color: radial-gradient(center 50% 50%, radius 60%, " +
            tm.toRgba(tm.getAccentHex(), 0.15) + " 0%, " + tm.toRgba(tm.getAccentHex(), 0.00) + " 70%);"
        );
        glow.setTranslateX(420);
        glow.setTranslateY(-180);

        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER_LEFT);

        Text badge = new Text("Community Hub");
        badge.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 12));
        badge.setFill(Color.WHITE);
        StackPane badgeWrap = new StackPane(badge);
        badgeWrap.setPadding(new Insets(7, 14, 7, 14));
        badgeWrap.setMaxWidth(Region.USE_PREF_SIZE);
        badgeWrap.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.16) + ";" +
            "-fx-background-radius: 999px;" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.35) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 999px;"
        );

        detailTitle = new Text("Voices of Horizon.");
        detailTitle.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BLACK, 42));
        detailTitle.setFill(Color.web("#f5f6fa"));

        Text subtitle = new Text(
            "Join the conversation. Connect with neighbors, share ideas, and help shape our community together."
        );
        subtitle.setWrappingWidth(560);
        subtitle.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));
        subtitle.setFill(Color.web("rgba(255,255,255,0.55)"));

        content.getChildren().addAll(badgeWrap, detailTitle, subtitle);

        hero.getChildren().addAll(glow, content);
        StackPane.setAlignment(content, Pos.CENTER_LEFT);
        playEntrance(hero, content);
        return hero;
    }

    private HBox buildSplitContainer() {
        HBox split = new HBox();
        split.setMaxWidth(1800);
        split.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.94), 1800));
        split.setMinHeight(650);
        split.setPrefHeight(760);
        split.setStyle(
            "-fx-background-color: #0a0a0c;" +
            "-fx-background-radius: 32px;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 32px;"
        );

        VBox main = buildMainPane();
        VBox side = buildSidebarPane();
        side.setPrefWidth(350);
        side.setMinWidth(300);

        HBox.setHgrow(main, Priority.ALWAYS);
        split.getChildren().addAll(main, side);
        return split;
    }

    private VBox buildMainPane() {
        VBox main = new VBox();
        main.setStyle("-fx-border-color: transparent rgba(255,255,255,0.09) transparent transparent; -fx-border-width: 0 1px 0 0;");
        main.setMaxWidth(Double.MAX_VALUE);

        readFace = buildReadFace();
        createFace = buildEditorFace("Create New Post", "Title", "Category", "Description", "Post Now");
        editFace = buildEditorFace("Edit Post", "Edit title", "Edit category", "Edit description", "Save Changes");

        createFace.setVisible(false);
        editFace.setVisible(false);

        faceStack.getChildren().addAll(readFace, createFace, editFace);
        VBox.setVgrow(faceStack, Priority.ALWAYS);

        main.getChildren().add(faceStack);
        return main;
    }

    private VBox buildReadFace() {
        VBox face = new VBox();
        face.setMaxWidth(Double.MAX_VALUE);

        StackPane hero = new StackPane();
        hero.setMinHeight(400);
        hero.setStyle(
            "-fx-background-color: linear-gradient(to top, rgba(20,20,30,1) 8%, " + tm.toRgba(tm.getAccentHex(), 0.35) + " 100%);"
        );

        VBox heroText = new VBox(8);
        heroText.setPadding(new Insets(24));
        heroText.setAlignment(Pos.BOTTOM_LEFT);

        HBox topMeta = new HBox(10);
        detailCategory = new Text("Discussion General");
        detailCategory.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 11));
        detailCategory.setFill(Color.WHITE);
        StackPane catPill = new StackPane(detailCategory);
        catPill.setPadding(new Insets(5, 10, 5, 10));
        catPill.setStyle("-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.20) + "; -fx-background-radius: 10px;");

        detailDate = new Text("Mar 12, 2026");
        detailDate.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12));
        detailDate.setFill(Color.web("rgba(255,255,255,0.65)"));
        topMeta.getChildren().addAll(catPill, detailDate);

        Text title = new Text("Welcome to the Forum");
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 52));
        title.setFill(Color.WHITE);

        HBox author = new HBox(8);
        author.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = new StackPane(new Text("H"));
        avatar.setPrefSize(34, 34);
        avatar.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 17px;");
        detailAuthor = new Text("Horizon Community");
        detailAuthor.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 13));
        detailAuthor.setFill(Color.WHITE);
        author.getChildren().addAll(avatar, detailAuthor);

        heroText.getChildren().addAll(topMeta, title, author);
        hero.getChildren().add(heroText);
        StackPane.setAlignment(heroText, Pos.BOTTOM_LEFT);

        VBox body = new VBox(16);
        body.setPadding(new Insets(24, 28, 24, 28));

        HBox actionBar = new HBox(8,
            reactionBtn("Like"),
            reactionBtn("Dislike"),
            reactionBtn("Emoji"),
            reactionBtn("Bookmark"),
            reactionBtn("Report")
        );
        actionBar.setPadding(new Insets(8));
        actionBar.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: rgba(255,255,255,0.10);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 14px;" +
            "-fx-background-radius: 14px;"
        );

        detailDescription = new Text(
            "Select a discussion from the sidebar to preview the split-view detail layout. " +
            "This mirrors the Twig read face style and visual rhythm, including action bar and comments section containers."
        );
        detailDescription.wrappingWidthProperty().bind(Bindings.max(faceStack.widthProperty().subtract(120), 520));
        detailDescription.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 15));
        detailDescription.setFill(Color.web("rgba(255,255,255,0.70)"));

        VBox commentsSection = new VBox(10);
        commentsSection.setPadding(new Insets(16));
        commentsSection.setStyle(
            "-fx-background-color: rgba(255,255,255,0.02);" +
            "-fx-border-color: rgba(255,255,255,0.10) transparent transparent transparent;" +
            "-fx-border-width: 1px 0 0 0;" +
            "-fx-background-insets: 12 0 0 0;" +
            "-fx-background-radius: 14px;" +
            "-fx-border-insets: 0 0 0 0;"
        );
        VBox commentsCard = new VBox(10);
        commentsCard.setPadding(new Insets(14));
        commentsCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.02);" +
            "-fx-border-color: rgba(255,255,255,0.10);" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 14px;" +
            "-fx-border-radius: 14px;"
        );
        commentsCard.getChildren().addAll(
            sectionTitle("Discussion"),
            muted("Comment thread placeholder"),
            muted("Comment form placeholder")
        );
        commentsSection.getChildren().add(commentsCard);

        HBox faceButtons = new HBox(8,
            ghostBtn("New Post", () -> switchFace(createFace)),
            ghostBtn("Edit Post", () -> switchFace(editFace)),
            ghostBtn("Back To Discussion", () -> switchFace(readFace))
        );

        body.getChildren().addAll(actionBar, detailDescription, commentsSection, faceButtons);

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        face.getChildren().addAll(hero, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return face;
    }

    private VBox buildEditorFace(String heading, String f1, String f2, String f3, String submitText) {
        VBox face = new VBox(14);
        face.setPadding(new Insets(18));
        face.setStyle("-fx-background-color: transparent;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Text t = sectionTitle(heading);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button back = ghostBtn("Back", () -> switchFace(readFace));
        header.getChildren().addAll(t, spacer, back);

        VBox formCard = new VBox(10);
        formCard.setPadding(new Insets(16));
        formCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: rgba(255,255,255,0.10);" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 16px;" +
            "-fx-border-radius: 16px;"
        );
        formCard.getChildren().addAll(
            inputMock(f1),
            inputMock(f2),
            areaMock(f3),
            ghostBtn(submitText, null)
        );

        face.getChildren().addAll(header, formCard);
        return face;
    }

    private VBox buildSidebarPane() {
        VBox side = new VBox();
        side.setStyle("-fx-background-color: rgba(0,0,0,0.20);");

        VBox header = new VBox(10);
        header.setPadding(new Insets(14));
        header.setStyle("-fx-border-color: transparent transparent rgba(255,255,255,0.10) transparent; -fx-border-width: 0 0 1px 0;");

        Text title = sectionTitle("Discussions");
        HBox actions = new HBox(8,
            filledBtn("New Post", () -> switchFace(createFace)),
            ghostBtn("Announcement", null)
        );

        HBox filters = new HBox(8,
            toggleFilter("General", true),
            toggleFilter("Announcements", false)
        );

        header.getChildren().addAll(title, actions, filters);

        VBox list = new VBox(8,
            forumItem("Discussion General", "Noise in block B", "Amina Trabelsi", "Mar 12"),
            forumItem("Suggestion", "Parking policy update", "Youssef Ben Ali", "Mar 11"),
            forumItem("Culture", "Community cleanup plan", "Sarra Gharbi", "Mar 10"),
            forumItem("Nouveaute", "Rooftop event photos", "Imen Kallel", "Mar 09")
        );
        list.setPadding(new Insets(14));

        ScrollPane listScroll = new ScrollPane(list);
        listScroll.setFitToWidth(true);
        listScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(listScroll, Priority.ALWAYS);

        side.getChildren().addAll(header, listScroll);
        VBox.setVgrow(listScroll, Priority.ALWAYS);
        return side;
    }

    private VBox forumItem(String category, String title, String author, String date) {
        VBox item = new VBox(6);
        item.setPadding(new Insets(12, 12, 12, 12));
        item.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: rgba(255,255,255,0.05);" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 14px;" +
            "-fx-border-radius: 14px;"
        );

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        Text cat = new Text(category);
        cat.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 10));
        cat.setFill(Color.WHITE);
        StackPane catWrap = new StackPane(cat);
        catWrap.setPadding(new Insets(3, 8, 3, 8));
        catWrap.setStyle(categoryStyle(category));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Text dateTxt = mutedSmall(date);
        top.getChildren().addAll(catWrap, spacer, dateTxt);

        Text titleTxt = new Text(title);
        titleTxt.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 15));
        titleTxt.setFill(Color.WHITE);

        HBox authorRow = new HBox(8);
        authorRow.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = new StackPane(new Text(author.substring(0, 1).toUpperCase()));
        avatar.setPrefSize(30, 30);
        avatar.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-background-radius: 15px;");
        Text authorTxt = mutedSmall(author);
        authorRow.getChildren().addAll(avatar, authorTxt);

        item.getChildren().addAll(top, titleTxt, authorRow);

        item.setOnMouseEntered(e -> {
            item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                "-fx-border-color: rgba(255,255,255,0.12);" +
                "-fx-border-width: 1px;" +
                "-fx-background-radius: 14px;" +
                "-fx-border-radius: 14px;"
            );
            item.setTranslateY(-2);
        });
        item.setOnMouseExited(e -> {
            item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03);" +
                "-fx-border-color: rgba(255,255,255,0.05);" +
                "-fx-border-width: 1px;" +
                "-fx-background-radius: 14px;" +
                "-fx-border-radius: 14px;"
            );
            item.setTranslateY(0);
        });

        // Local visual preview update only
        item.setOnMouseClicked(e -> {
            detailCategory.setText(category);
            detailDate.setText(date + ", 2026");
            detailAuthor.setText(author);
            detailTitle.setText("Voices of\nHorizon.");
            detailDescription.setText(
                "Preview from sidebar selection: \"" + title + "\". " +
                "This mirrors the split-view behavior in the Twig forum where selecting a discussion updates the read panel."
            );
            switchFace(readFace);
        });

        return item;
    }

    private Button reactionBtn(String text) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-text-fill: rgba(255,255,255,0.90);" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(255,255,255,0.20);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );
        b.setOnMouseEntered(e -> {
            b.setStyle(
                "-fx-background-color: rgba(255,255,255,0.10);" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: rgba(255,255,255,0.28);" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;"
            );
            b.setTranslateY(-2);
        });
        b.setOnMouseExited(e -> {
            b.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                "-fx-text-fill: rgba(255,255,255,0.90);" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: rgba(255,255,255,0.20);" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;"
            );
            b.setTranslateY(0);
        });
        return b;
    }

    private Button filledBtn(String text, Runnable action) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
            "-fx-text-fill: white; -fx-background-radius: 12px; -fx-font-weight: bold;"
        );
        if (action != null) {
            b.setOnAction(e -> action.run());
        }
        return b;
    }

    private Button ghostBtn(String text, Runnable action) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-text-fill: rgba(255,255,255,0.86);" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(255,255,255,0.15);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );
        if (action != null) {
            b.setOnAction(e -> action.run());
        }
        return b;
    }

    private Button toggleFilter(String text, boolean active) {
        Button b = new Button(text);
        String bg = active ? tm.toRgba(tm.getAccentHex(), 0.20) : "rgba(255,255,255,0.05)";
        String border = active ? tm.toRgba(tm.getAccentHex(), 0.45) : "rgba(255,255,255,0.12)";
        b.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + border + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );
        return b;
    }

    private VBox inputMock(String name) {
        VBox row = new VBox(5);
        Text l = sectionLabel(name);
        StackPane box = new StackPane();
        box.setPadding(new Insets(10));
        box.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(255,255,255,0.14);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(muted("Input placeholder"));
        row.getChildren().addAll(l, box);
        return row;
    }

    private VBox areaMock(String name) {
        VBox row = new VBox(5);
        Text l = sectionLabel(name);
        StackPane box = new StackPane();
        box.setPadding(new Insets(10));
        box.setMinHeight(130);
        box.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(255,255,255,0.14);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );
        box.setAlignment(Pos.TOP_LEFT);
        box.getChildren().add(muted("Textarea placeholder"));
        row.getChildren().addAll(l, box);
        return row;
    }

    private void switchFace(VBox target) {
        readFace.setVisible(false);
        createFace.setVisible(false);
        editFace.setVisible(false);

        target.setOpacity(0);
        target.setVisible(true);

        FadeTransition fade = new FadeTransition(Duration.millis(450), target);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(450), target);
        slide.setFromY(12);
        slide.setToY(0);

        new ParallelTransition(fade, slide).play();
    }

    private void playEntrance(StackPane hero, VBox content) {
        FadeTransition fade = new FadeTransition(Duration.millis(650), hero);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        TranslateTransition rise = new TranslateTransition(Duration.millis(700), content);
        rise.setFromY(24);
        rise.setToY(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(700), hero);
        scale.setFromX(0.985);
        scale.setFromY(0.985);
        scale.setToX(1.0);
        scale.setToY(1.0);

        new ParallelTransition(fade, rise, scale).play();
    }

    private Text sectionTitle(String text) {
        Text t = new Text(text);
        t.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 22));
        t.setFill(Color.WHITE);
        return t;
    }

    private Text sectionLabel(String text) {
        Text t = new Text(text);
        t.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 12));
        t.setFill(Color.web("rgba(255,255,255,0.88)"));
        return t;
    }

    private Text muted(String text) {
        Text t = new Text(text);
        t.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13));
        t.setFill(Color.web("rgba(255,255,255,0.62)"));
        return t;
    }

    private Text mutedSmall(String text) {
        Text t = new Text(text);
        t.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 11));
        t.setFill(Color.web("rgba(255,255,255,0.62)"));
        return t;
    }

    private String categoryStyle(String category) {
        String key = category.toLowerCase();
        if (key.contains("announcement")) {
            return "-fx-background-color: rgba(255,50,50,0.20); -fx-background-radius: 8px; -fx-border-color: rgba(255,50,50,0.40); -fx-border-width: 1px; -fx-border-radius: 8px;";
        }
        if (key.contains("suggestion")) {
            return "-fx-background-color: rgba(50,255,100,0.15); -fx-background-radius: 8px; -fx-border-color: rgba(50,255,100,0.30); -fx-border-width: 1px; -fx-border-radius: 8px;";
        }
        if (key.contains("culture")) {
            return "-fx-background-color: rgba(200,100,255,0.20); -fx-background-radius: 8px; -fx-border-color: rgba(200,100,255,0.40); -fx-border-width: 1px; -fx-border-radius: 8px;";
        }
        if (key.contains("nouveaute")) {
            return "-fx-background-color: rgba(255,200,50,0.20); -fx-background-radius: 8px; -fx-border-color: rgba(255,200,50,0.40); -fx-border-width: 1px; -fx-border-radius: 8px;";
        }
        return "-fx-background-color: rgba(100,200,255,0.15); -fx-background-radius: 8px; -fx-border-color: rgba(100,200,255,0.30); -fx-border-width: 1px; -fx-border-radius: 8px;";
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public void cleanup() {
        // No persistent listeners for this static page yet.
    }
}
