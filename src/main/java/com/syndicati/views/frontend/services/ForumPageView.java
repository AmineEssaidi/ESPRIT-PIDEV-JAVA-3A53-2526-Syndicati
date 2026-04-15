package com.syndicati.views.frontend.services;

import com.syndicati.MainApplication;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.models.entities.Publication;
import com.syndicati.models.entities.PubCommentReaction;
import com.syndicati.utils.theme.ThemeManager;
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
import javafx.scene.shape.Circle;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ComboBox;
import javafx.util.Duration;

/**
 * Forum page UI duplicated from frontend/forum/index.html.twig and forum.css.
 * UI-only for now; behavior is intentionally local and non-persistent.
 */
public class ForumPageView implements ViewInterface {

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();
    private com.syndicati.controllers.frontend.services.forum.PublicationController publicationController;
    private com.syndicati.controllers.frontend.services.forum.CommentaireController commentaireController;
    private com.syndicati.controllers.frontend.services.forum.PubCommentReactionController reactionController;
    private VBox sidebarList;

    private final StackPane faceStack = new StackPane();
    private VBox readFace;
    private VBox createFace;
    private VBox editFace;
    private Publication currentPub;

    private java.util.List<com.syndicati.models.entities.Publication> allPublications = new java.util.ArrayList<>();
    private String currentFilter = "General";
    private Button btnGeneral;
    private Button btnAnnouncements;

    // Creation form fields
    private TextField createTitleField;
    private ComboBox<String> createCategoryCombo;
    private TextArea createDescriptionArea;
    private java.io.File selectedImageFile;
    private Label selectedImageLabel;
    private javafx.scene.image.ImageView createImageView;
    private Label createStatusLabel;

    // Edit form fields
    private Publication currentEditingPub;
    private TextField editTitleField;
    private ComboBox<String> editCategoryCombo;
    private TextArea editDescriptionArea;
    private java.io.File selectedEditImageFile;
    private Label selectedEditImageLabel;
    private Label editStatusLabel;
    private javafx.scene.image.ImageView oldImageView;
    private javafx.scene.image.ImageView newImageView;
    private boolean shouldDeleteImage = false;

    private StackPane detailCategoryPill;
    private Text detailCategory;
    private Text detailDate;
    private Text detailTitle;
    private Text detailAuthor;
    private Text detailDescription;
    private StackPane detailHero;
    private javafx.scene.image.ImageView detailImageView;
    private Button detailEditBtn;
    private Button detailDeleteBtn;

    private VBox commentListContainer;
    private VBox commentFormContainer;
    private VBox commentsCardContainer;
    private TextArea commentTextArea;
    private java.io.File commentImageFile;
    private boolean isCommentAnonymous = false;
    private Label selectedCommentImageLabel;
    private Button anonymousToggleBtn;

    // Social Action Components (Publication)
    private HBox socialActionsBox;
    private Button btnLikePub;
    private Button btnDislikePub;
    private Button btnBookmarkPub;
    private Button btnSignalPub;
    private Label countLikePub;
    private Label countDislikePub;
    private Label countReportPub;

    public ForumPageView() {
        root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(28, 0, 40, 0));
        root.setStyle("-fx-background-color: transparent;");
        root.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(buildHero(), buildSplitContainer());

        publicationController = new com.syndicati.controllers.frontend.services.forum.PublicationController(this);
        publicationController.afficher();
        commentaireController = new com.syndicati.controllers.frontend.services.forum.CommentaireController(this);
        reactionController = new com.syndicati.controllers.frontend.services.forum.PubCommentReactionController(this);
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
            "-fx-background-color: radial-gradient(center 12% 18%, radius 75%, " + tm.toRgba(tm.getAccentHex(), 0.18) + " 0%, " + tm.toRgba(tm.getAccentHex(), 0.00) + " 58%), " + surfaceStrong() + ";" +
            "-fx-background-radius: 48px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 48px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.48), 32, 0.16, 0, 8);"
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
        subtitle.setFill(Color.web(textMuted()));

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
            "-fx-background-color: " + surfaceCard() + ";" +
            "-fx-background-radius: 32px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 32px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.42), 24, 0.14, 0, 6);"
        );

        VBox main = buildMainPane();
        VBox side = buildSidebarPane();
        side.prefWidthProperty().bind(Bindings.createDoubleBinding(
            () -> split.getWidth() < 1100 ? 250.0 : 350.0,
            split.widthProperty()
        ));
        side.setMinWidth(220);

        HBox.setHgrow(main, Priority.ALWAYS);
        split.getChildren().addAll(main, side);
        return split;
    }

    private VBox buildMainPane() {
        VBox main = new VBox();
        main.setStyle("-fx-border-color: transparent " + borderSoft() + " transparent transparent; -fx-border-width: 0 1px 0 0;");
        main.setMaxWidth(Double.MAX_VALUE);

        readFace = buildReadFace();
        createFace = buildCreateFace();
        editFace = buildEditFace();

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
        
        detailHero = new StackPane();
        detailHero.setMinHeight(250);
        detailHero.setPrefHeight(250);
        detailHero.setMaxHeight(250);
        detailHero.setStyle(
            "-fx-background-color: #14141e;"
        );

        // Add clip to prevent image from leaking outside hero
        javafx.scene.shape.Rectangle heroClip = new javafx.scene.shape.Rectangle();
        heroClip.setArcWidth(0);
        heroClip.setArcHeight(0);
        heroClip.widthProperty().bind(detailHero.widthProperty());
        heroClip.heightProperty().bind(detailHero.heightProperty());
        detailHero.setClip(heroClip);

        detailImageView = new javafx.scene.image.ImageView();
        detailImageView.setPreserveRatio(true);
        detailImageView.setSmooth(true);
        // Bind to width and let preserveRatio handle height (it will be clipped if taller than 400)
        detailImageView.fitWidthProperty().bind(detailHero.widthProperty());
        detailImageView.setOpacity(1.0); 

        // Gradient overlay to ensure text is readable
        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: linear-gradient(to top, rgba(20,20,30,1) 8%, " + tm.toRgba(tm.getAccentHex(), 0.15) + " 100%);");

        VBox heroText = new VBox(8);
        heroText.setPadding(new Insets(24));
        heroText.setAlignment(Pos.BOTTOM_LEFT);

        HBox topMeta = new HBox(10);
        detailCategory = new Text("Discussion General");
        detailCategory.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 11));
        detailCategory.setFill(Color.WHITE);
        detailCategoryPill = new StackPane(detailCategory);
        detailCategoryPill.setPadding(new Insets(5, 12, 5, 12));
        detailCategoryPill.setStyle(categoryStyle("General"));

        detailDate = new Text();
        detailDate.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12));
        detailDate.setFill(Color.web(textSoft()));
        topMeta.getChildren().addAll(detailCategoryPill, detailDate);

        detailTitle = new Text("Welcome to the Forum");
        detailTitle.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 32));
        detailTitle.setFill(Color.WHITE);
        detailTitle.setWrappingWidth(800);

        HBox authorBox = new HBox(8);
        authorBox.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = new StackPane(new Text("H"));
        avatar.setPrefSize(34, 34);
        avatar.setStyle("-fx-background-color: " + surfaceSoft() + "; -fx-background-radius: 17px;");
        detailAuthor = new Text("Horizon Community");
        detailAuthor.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 13));
        detailAuthor.setFill(Color.WHITE);
        authorBox.getChildren().addAll(avatar, detailAuthor);

        heroText.getChildren().addAll(topMeta, detailTitle, authorBox);
        detailHero.getChildren().addAll(detailImageView, overlay, heroText);
        StackPane.setAlignment(heroText, Pos.BOTTOM_LEFT);
        detailHero.setCursor(javafx.scene.Cursor.HAND);
        detailHero.setOnMouseClicked(e -> showImageLightbox(detailImageView.getImage()));

        VBox body = new VBox(22);
        body.setPadding(new Insets(32));

        detailDescription = new Text(
            "Select a discussion from the sidebar to preview the split-view detail layout. " +
            "This mirrors the Twig read face style and visual rhythm, including action bar and comments section containers."
        );
        detailDescription.wrappingWidthProperty().bind(Bindings.max(faceStack.widthProperty().subtract(60), 520));
        detailDescription.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 16));
        detailDescription.setFill(Color.web(textSoft()));

        VBox commentsSection = new VBox(10);
        commentsSection.setPadding(new Insets(16));
        commentsSection.setStyle(
            "-fx-background-color: rgba(255,255,255,0.02);" +
            "-fx-border-color: " + borderSoft() + " transparent transparent transparent;" +
            "-fx-border-width: 1px 0 0 0;" +
            "-fx-background-insets: 12 0 0 0;" +
            "-fx-background-radius: 14px;" +
            "-fx-border-insets: 0 0 0 0;"
        );
        commentsCardContainer = new VBox(20);
        commentsCardContainer.setPadding(new Insets(14));
        commentsCardContainer.setStyle(
            "-fx-background-color: rgba(255,255,255,0.02);" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 14px;" +
            "-fx-border-radius: 14px;"
        );

        commentListContainer = new VBox(20);
        commentListContainer.setPadding(new Insets(10, 0, 10, 0));

        commentFormContainer = buildCommentForm();

        commentsCardContainer.getChildren().addAll(
            sectionTitle("Discussion"),
            commentFormContainer,
            commentListContainer
        );
        commentsSection.getChildren().add(commentsCardContainer);

        detailEditBtn = ghostBtn("Edit", () -> {
            if (currentPub != null) {
                populateEditForm(currentPub);
                switchFace(editFace);
            }
        });

        detailDeleteBtn = ghostBtn("Delete", () -> {
            if (currentPub != null) {
                showDeleteConfirmation("Delete Publication", "Are you sure you want to delete this publication?\nThis action cannot be undone.", () -> {
                    publicationController.deletePublication(currentPub.getId());
                });
            }
        });
        detailDeleteBtn.setStyle(detailDeleteBtn.getStyle() + "-fx-text-fill: #ef4444; -fx-border-color: rgba(239, 68, 68, 0.4);");

        HBox actionBar = new HBox(12);
        actionBar.setPadding(new Insets(14, 20, 14, 20));
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-background-radius: 18px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 18px;"
        );

        // Social Actions HBox
        countLikePub = new Label("0");
        countDislikePub = new Label("0");
        countReportPub = new Label("0");

        btnLikePub = socialBtn("👍", countLikePub, () -> {
            if (currentPub != null) reactionController.handleReaction(currentPub.getId(), null, PubCommentReaction.KIND_LIKE, null, null);
        });
        btnDislikePub = socialBtn("👎", countDislikePub, () -> {
            if (currentPub != null) reactionController.handleReaction(currentPub.getId(), null, PubCommentReaction.KIND_DISLIKE, null, null);
        });
        btnBookmarkPub = socialBtn("🔖", "Bookmark", () -> {
            if (currentPub != null) reactionController.handleReaction(currentPub.getId(), null, PubCommentReaction.KIND_BOOKMARK, null, null);
        });
        btnSignalPub = socialBtn("🚩", countReportPub, () -> {
            if (currentPub != null) {
                showReportDialog(reason -> {
                    reactionController.handleReaction(currentPub.getId(), null, PubCommentReaction.KIND_REPORT, null, reason);
                });
            }
        });

        socialActionsBox = new HBox(8, btnLikePub, btnDislikePub, btnBookmarkPub, btnSignalPub);
        socialActionsBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox ownerActions = new HBox(8, detailEditBtn, detailDeleteBtn);
        ownerActions.setAlignment(Pos.CENTER_RIGHT);

        actionBar.getChildren().addAll(socialActionsBox, spacer, ownerActions);
        actionBar.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 14px;" +
            "-fx-background-radius: 14px;"
        );

        body.getChildren().addAll(actionBar, detailDescription, commentsSection);

        VBox scrollContent = new VBox(0, detailHero, body);
        scrollContent.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scroll = new ScrollPane(scrollContent);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        face.getChildren().addAll(scroll);
        return face;
    }

    private void showImageLightbox(javafx.scene.image.Image img) {
        if (img == null) return;

        StackPane lightbox = new StackPane();
        lightbox.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
        lightbox.setOpacity(0);

        javafx.scene.image.ImageView fullImg = new javafx.scene.image.ImageView(img);
        fullImg.setPreserveRatio(true);
        fullImg.setSmooth(true);
        // Bind to root size but keep some margin
        fullImg.fitWidthProperty().bind(root.widthProperty().multiply(0.9));
        fullImg.fitHeightProperty().bind(root.heightProperty().multiply(0.9));

        Button closeBtn = new Button("×");
        closeBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 40px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new Insets(20));
        closeBtn.setOnAction(e -> hideLightbox(lightbox));

        lightbox.getChildren().addAll(fullImg, closeBtn);
        lightbox.setOnMouseClicked(e -> hideLightbox(lightbox));

        // Add to the faceStack as that's our main container
        faceStack.getChildren().add(lightbox);

        FadeTransition ft = new FadeTransition(Duration.millis(300), lightbox);
        ft.setToValue(1);
        ft.play();
    }

    private void hideLightbox(StackPane lightbox) {
        FadeTransition ft = new FadeTransition(Duration.millis(250), lightbox);
        ft.setToValue(0);
        ft.setOnFinished(e -> faceStack.getChildren().remove(lightbox));
        ft.play();
    }

    public void refreshReactionsFor(Integer pubId, Integer commId) {
        if (pubId != null && (currentPub != null && pubId.equals(currentPub.getId()))) {
            updatePublicationReactions(pubId);
        }
        if (commId != null) {
            // Comments are tricky as they are items in the list.
            // For now, refreshing the whole comment list is easiest to ensure state consistency.
            if (currentPub != null) commentaireController.afficher(currentPub.getId());
        }
    }

    private void updatePublicationReactions(int pubId) {
        javafx.application.Platform.runLater(() -> {
            int likes = reactionController.getCount(pubId, null, PubCommentReaction.KIND_LIKE);
            int dislikes = reactionController.getCount(pubId, null, PubCommentReaction.KIND_DISLIKE);
            int reports = reactionController.getCount(pubId, null, PubCommentReaction.KIND_REPORT);

            countLikePub.setText(String.valueOf(likes));
            countDislikePub.setText(String.valueOf(dislikes));
            countReportPub.setText(String.valueOf(reports));

            // Update button states (active/inactive)
            updateSocialBtnState(btnLikePub, reactionController.hasReacted(pubId, null, PubCommentReaction.KIND_LIKE), "#2ed573");
            updateSocialBtnState(btnDislikePub, reactionController.hasReacted(pubId, null, PubCommentReaction.KIND_DISLIKE), "#ff4757");
            updateSocialBtnState(btnBookmarkPub, reactionController.hasReacted(pubId, null, PubCommentReaction.KIND_BOOKMARK), "#00d2ff");
            updateSocialBtnState(btnSignalPub, reactionController.hasReacted(pubId, null, PubCommentReaction.KIND_REPORT), "#ffa502");
        });
    }

    private void updateSocialBtnState(Button btn, boolean active, String colorHex) {
        if (active) {
            btn.setStyle(btn.getStyle() + "-fx-border-color: " + colorHex + "; -fx-background-color: " + tm.toRgba(colorHex, 0.1) + ";");
            ((Text)((HBox)btn.getGraphic()).getChildren().get(0)).setFill(javafx.scene.paint.Color.web(colorHex));
        } else {
            // Reset to default style (strip the active additions)
            btn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03);" +
                "-fx-border-color: rgba(255,255,255,0.05);" +
                "-fx-border-radius: 10px;" +
                "-fx-background-radius: 10px;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 5 12 5 12;"
            );
            ((Text)((HBox)btn.getGraphic()).getChildren().get(0)).setFill(javafx.scene.paint.Color.web("rgba(255,255,255,0.6)"));
        }
    }

    private void showReportDialog(java.util.function.Consumer<String> onReport) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Report Content");
        dialog.setHeaderText("Why are you reporting this content?");
        dialog.setContentText("Reason:");
        
        dialog.showAndWait().ifPresent(reason -> {
            if (!reason.isBlank()) {
                onReport.accept(reason);
            }
        });
    }

    private void showDeleteConfirmation(String titleText, String msgText, Runnable onConfirm) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        overlay.setOpacity(0);

        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setMaxSize(400, 200);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: " + surfaceCard() + ";" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;" +
            "-fx-background-radius: 20px;"
        );

        Text title = new Text(titleText);
        title.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), javafx.scene.text.FontWeight.BOLD, 20));
        title.setFill(javafx.scene.paint.Color.WHITE);

        Text msg = new Text(msgText);
        msg.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), 14));
        msg.setFill(javafx.scene.paint.Color.web("rgba(255,255,255,0.8)"));
        msg.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Button cancelBtn = ghostBtn("Cancel", () -> hideLightbox(overlay));
        cancelBtn.setPrefWidth(120);
        
        Button deleteBtn = filledBtn("Confirm Delete", () -> {
            onConfirm.run();
            hideLightbox(overlay);
        });
        deleteBtn.setPrefWidth(120);
        deleteBtn.setStyle(deleteBtn.getStyle() + "-fx-background-color: #ef4444;");

        HBox btns = new HBox(12, cancelBtn, deleteBtn);
        btns.setAlignment(Pos.CENTER);

        card.getChildren().addAll(title, msg, btns);
        overlay.getChildren().add(card);

        faceStack.getChildren().add(overlay);

        FadeTransition ft = new FadeTransition(Duration.millis(300), overlay);
        ft.setToValue(1);
        ft.play();
    }

    public void showNotification(String message, String type) {
        javafx.application.Platform.runLater(() -> {
            StackPane overlay = new StackPane();
            overlay.setMouseTransparent(true);
            overlay.setPadding(new Insets(40));
            StackPane.setAlignment(overlay, Pos.BOTTOM_CENTER);

            HBox card = new HBox(12);
            card.setPadding(new Insets(12, 24, 12, 24));
            card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            card.setAlignment(Pos.CENTER);
            
            String bgColor = type.equals("success") ? "rgba(16, 185, 129, 0.9)" : "rgba(239, 68, 68, 0.9)";
            card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-background-radius: 30px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 8);"
            );

            Text txt = new Text(message);
            txt.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), javafx.scene.text.FontWeight.BOLD, 14));
            txt.setFill(javafx.scene.paint.Color.WHITE);

            card.getChildren().add(txt);
            overlay.getChildren().add(card);
            
            faceStack.getChildren().add(overlay);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlay);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            TranslateTransition slideUp = new TranslateTransition(Duration.millis(400), overlay);
            slideUp.setFromY(50);
            slideUp.setToY(0);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), overlay);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.millis(2500));
            fadeOut.setOnFinished(e -> faceStack.getChildren().remove(overlay));
            
            new ParallelTransition(fadeIn, slideUp).play();
            fadeOut.play();
        });
    }

    private VBox buildCreateFace() {
        VBox face = new VBox(14);
        face.setPadding(new Insets(18));
        face.setStyle("-fx-background-color: transparent;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Text t = sectionTitle("Create New Post");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button back = ghostBtn("Back", () -> switchFace(readFace));
        header.getChildren().addAll(t, spacer, back);

        VBox formCard = new VBox(16);
        formCard.setPadding(new Insets(24));
        formCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 20px;" +
            "-fx-border-radius: 20px;"
        );

        createTitleField = createStyledTextField("Post Title (min 5 characters)");
        
        createCategoryCombo = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(com.syndicati.models.entities.Publication.CATEGORIES));
        createCategoryCombo.setPromptText("Select a category");
        createCategoryCombo.setMaxWidth(Double.MAX_VALUE);
        createCategoryCombo.setStyle(comboStyle());

        createDescriptionArea = createStyledTextArea("Detailed description (min 10 characters)");

        HBox imageRow = new HBox(12);
        imageRow.setAlignment(Pos.CENTER_LEFT);
        selectedImageLabel = new Label("No image selected (Optional)");
        selectedImageLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");

        createImageView = new javafx.scene.image.ImageView();
        createImageView.setFitWidth(120);
        createImageView.setFitHeight(120);
        createImageView.setPreserveRatio(true);
        createImageView.setCursor(javafx.scene.Cursor.HAND);
        createImageView.setOnMouseClicked(e -> showImageLightbox(createImageView.getImage()));
        
        StackPane createImgFrame = new StackPane(createImageView);
        createImgFrame.setPrefSize(130, 130);
        createImgFrame.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 12px; -fx-border-color: " + borderSoft() + "; -fx-border-radius: 12px; -fx-border-width: 1px;");

        Button selectImgBtn = ghostBtn("Select Image", this::handleSelectImage);
        imageRow.getChildren().addAll(selectImgBtn, createImgFrame, selectedImageLabel);

        createStatusLabel = new Label();
        createStatusLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 12px;");

        Button submitBtn = filledBtn("Post Discussion", this::handleCreate);
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setPrefHeight(40);

        formCard.getChildren().addAll(
            sectionLabel("TITLE"), createTitleField,
            sectionLabel("CATEGORY"), createCategoryCombo,
            sectionLabel("DESCRIPTION"), createDescriptionArea,
            sectionLabel("IMAGE (OPTIONAL)"), imageRow,
            createStatusLabel,
            submitBtn
        );

        face.getChildren().addAll(header, formCard);
        return face;
    }

    private void handleCreate() {
        String title = createTitleField.getText().trim();
        String category = createCategoryCombo.getValue();
        String description = createDescriptionArea.getText();

        if (title.isEmpty() || !Character.isLetter(title.charAt(0))) {
            createStatusLabel.setText("Title must start with a letter.");
            return;
        }
        if (category == null || category.isEmpty()) {
            createStatusLabel.setText("Please select a category.");
            return;
        }
        if (description.length() < 10) {
            createStatusLabel.setText("Description must be at least 10 characters.");
            return;
        }

        createStatusLabel.setText("");
        
        String imagePath = null;
        if (selectedImageFile != null) {
            imagePath = saveSelectedImage();
        }

        publicationController.addPublication(title, category, description, imagePath);
    }

    private void handleSelectImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Publication Image");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        java.io.File file = fileChooser.showOpenDialog(MainApplication.getInstance().getPrimaryStage());
        if (file != null) {
            selectedImageFile = file;
            selectedImageLabel.setText(file.getName());
            selectedImageLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px;"); // Success color
            try {
                createImageView.setImage(new javafx.scene.image.Image(file.toURI().toString()));
            } catch (Exception e) {
                System.err.println("Failed to load create preview: " + e.getMessage());
            }
        }
    }

    private String saveSelectedImage() {
        try {
            String fileName = "forum_" + System.currentTimeMillis() + "_" + selectedImageFile.getName().replaceAll("\\s+", "_");
            java.nio.file.Path targetDir = java.nio.file.Paths.get("uploads/forum_images");
            if (!java.nio.file.Files.exists(targetDir)) {
                java.nio.file.Files.createDirectories(targetDir);
            }
            java.nio.file.Path targetPath = targetDir.resolve(fileName);
            java.nio.file.Files.copy(selectedImageFile.toPath(), targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            System.err.println("Failed to save image: " + e.getMessage());
            return null;
        }
    }

    public void switchFaceToRead() {
        // Clear fields
        createTitleField.clear();
        createCategoryCombo.setValue(null);
        createDescriptionArea.clear();
        selectedImageFile = null;
        if (createImageView != null) createImageView.setImage(null);
        selectedImageLabel.setText("No image selected (Optional)");
        selectedImageLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");
        createStatusLabel.setText("");
        
        // Reset edit-specific previews too
        if (oldImageView != null) oldImageView.setImage(null);
        if (newImageView != null) newImageView.setImage(null);
        shouldDeleteImage = false;
        
        switchFace(readFace);
    }

    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06); " +
            "-fx-text-fill: white; " +
            "-fx-prompt-text-fill: rgba(255,255,255,0.4); " +
            "-fx-background-radius: 12px; " +
            "-fx-border-color: " + borderSoft() + "; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 12px; " +
            "-fx-padding: 10px;" +
            "-fx-focus-color: " + tm.getAccentHex() + "; " +
            "-fx-faint-focus-color: transparent;"
        );
        return tf;
    }

    private TextArea createStyledTextArea(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setPrefHeight(150);
        ta.setWrapText(true);
        ta.setStyle(
            "-fx-control-inner-background: rgba(255,255,255,0.03); " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: white; " +
            "-fx-prompt-text-fill: rgba(255,255,255,0.4); " +
            "-fx-background-radius: 12px; " +
            "-fx-border-color: " + borderSoft() + "; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 12px; " +
            "-fx-padding: 8px;" +
            "-fx-focus-color: " + tm.getAccentHex() + "; " +
            "-fx-faint-focus-color: transparent;"
        );
        return ta;
    }

    private VBox buildEditFace() {
        VBox face = new VBox(14);
        face.setPadding(new Insets(18));
        face.setStyle("-fx-background-color: transparent;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Text t = sectionTitle("Edit Publication");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button back = ghostBtn("Back", () -> switchFace(readFace));
        header.getChildren().addAll(t, spacer, back);

        VBox formCard = new VBox(16);
        formCard.setPadding(new Insets(24));
        formCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 20px;" +
            "-fx-border-radius: 20px;"
        );

        editTitleField = createStyledTextField("Update title...");
        editCategoryCombo = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(com.syndicati.models.entities.Publication.CATEGORIES));
        editCategoryCombo.setPromptText("Select a category");
        editCategoryCombo.setMaxWidth(Double.MAX_VALUE);
        editCategoryCombo.setStyle(comboStyle());

        editDescriptionArea = createStyledTextArea("Updated description...");
        
        selectedEditImageLabel = new Label("No image selected (Optional)");
        selectedEditImageLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");

        // Dual Image Previews
        HBox imagePreviewContainer = new HBox(20);
        imagePreviewContainer.setAlignment(Pos.CENTER);
        imagePreviewContainer.setPadding(new Insets(10, 0, 10, 0));

        VBox oldImageBlock = new VBox(8);
        oldImageBlock.setAlignment(Pos.CENTER);
        Label oldLabel = new Label("Current Image");
        oldLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 11px;");
        oldImageView = new javafx.scene.image.ImageView();
        oldImageView.setFitWidth(120);
        oldImageView.setFitHeight(120);
        oldImageView.setPreserveRatio(true);
        oldImageView.setCursor(javafx.scene.Cursor.HAND);
        oldImageView.setOnMouseClicked(e -> showImageLightbox(oldImageView.getImage()));
        
        StackPane oldImgFrame = new StackPane(oldImageView);
        oldImgFrame.setPrefSize(130, 130);
        oldImgFrame.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 12px; -fx-border-color: " + borderSoft() + "; -fx-border-radius: 12px; -fx-border-width: 1px;");
        oldImageBlock.getChildren().addAll(oldLabel, oldImgFrame);

        VBox newImageBlock = new VBox(8);
        newImageBlock.setAlignment(Pos.CENTER);
        Label newLabel = new Label("New Image");
        newLabel.setStyle("-fx-text-fill: " + tm.getAccentHex() + "; -fx-font-size: 11px;");
        newImageView = new javafx.scene.image.ImageView();
        newImageView.setFitWidth(120);
        newImageView.setFitHeight(120);
        newImageView.setPreserveRatio(true);
        newImageView.setCursor(javafx.scene.Cursor.HAND);
        newImageView.setOnMouseClicked(e -> showImageLightbox(newImageView.getImage()));
        
        StackPane newImgFrame = new StackPane(newImageView);
        newImgFrame.setPrefSize(130, 130);
        newImgFrame.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 12px; -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.3) + "; -fx-border-radius: 12px; -fx-border-width: 1px;");
        newImageBlock.getChildren().addAll(newLabel, newImgFrame);

        imagePreviewContainer.getChildren().addAll(oldImageBlock, newImageBlock);
        
        Button imageBtn = ghostBtn("Change Image", () -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Select Publication Image");
            fc.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            java.io.File file = fc.showOpenDialog(com.syndicati.MainApplication.getInstance().getPrimaryStage());
            if (file != null) {
                selectedEditImageFile = file;
                shouldDeleteImage = false;
                selectedEditImageLabel.setText("Selected: " + file.getName());
                selectedEditImageLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 11px;");
                try {
                    newImageView.setImage(new javafx.scene.image.Image(file.toURI().toString()));
                } catch (Exception e) {
                    System.err.println("Failed to load new image preview: " + e.getMessage());
                }
            }
        });

        Button deleteImageBtn = ghostBtn("Delete Image", () -> {
            shouldDeleteImage = true;
            selectedEditImageFile = null;
            newImageView.setImage(null);
            selectedEditImageLabel.setText("Image marked for deletion");
            selectedEditImageLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
        });
        deleteImageBtn.setStyle(deleteImageBtn.getStyle() + "-fx-text-fill: #ef4444; -fx-border-color: rgba(239, 68, 68, 0.4);");

        HBox imageControls = new HBox(12, imageBtn, deleteImageBtn);
        imageControls.setAlignment(Pos.CENTER_LEFT);

        editStatusLabel = new Label();
        editStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");

        Button updateBtn = filledAccentBtn("Update Publication", this::handleEdit);
        updateBtn.setMaxWidth(Double.MAX_VALUE);

        formCard.getChildren().addAll(
            sectionLabel("TITLE"), editTitleField,
            sectionLabel("CATEGORY"), editCategoryCombo,
            sectionLabel("DESCRIPTION"), editDescriptionArea,
            sectionLabel("IMAGE PREVIEW"), imagePreviewContainer,
            sectionLabel("IMAGE CONTROLS"), imageControls, selectedEditImageLabel,
            editStatusLabel,
            updateBtn
        );

        face.getChildren().addAll(header, formCard);
        return face;
    }

    private String savePublicationImage(java.io.File file) {
        if (file == null) return null;
        try {
            java.io.File dir = new java.io.File("uploads/forum_images");
            if (!dir.exists()) dir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getName();
            java.io.File dest = new java.io.File(dir, fileName);

            java.nio.file.Files.copy(file.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return fileName; 
        } catch (Exception e) {
            System.err.println("Error saving publication image: " + e.getMessage());
            return null;
        }
    }

    private void populateEditForm(com.syndicati.models.entities.Publication pub) {
        currentEditingPub = pub;
        editTitleField.setText(pub.getTitrePub());
        editCategoryCombo.setValue(pub.getCategoriePub());
        editDescriptionArea.setText(pub.getDescriptionPub());
        selectedEditImageFile = null; // Reset to null unless user picks new one
        newImageView.setImage(null);
        shouldDeleteImage = false;
        
        if (pub.getImagePub() != null && !pub.getImagePub().isBlank()) {
            selectedEditImageLabel.setText("Current: " + pub.getImagePub());
            String imgPath = "uploads/forum_images/" + pub.getImagePub();
            try {
                javafx.scene.image.Image img = com.syndicati.utils.image.ImageLoaderUtil.loadImage(imgPath, false);
                oldImageView.setImage(img);
            } catch (Exception e) {
                oldImageView.setImage(null);
            }
        } else {
            selectedEditImageLabel.setText("No image currently set");
            oldImageView.setImage(null);
        }
        editStatusLabel.setText("");
    }

    private void handleEdit() {
        String title = editTitleField.getText();
        String cat = editCategoryCombo.getValue();
        String desc = editDescriptionArea.getText();

        if (title == null || title.isBlank() || title.length() < 3) {
            editStatusLabel.setText("Title must be at least 3 characters.");
            return;
        }
        if (cat == null) {
            editStatusLabel.setText("Please select a category.");
            return;
        }
        if (desc == null || desc.length() < 10) {
            editStatusLabel.setText("Description must be at least 10 characters.");
            return;
        }
        String imageName = currentEditingPub.getImagePub();
        if (shouldDeleteImage) {
            imageName = null;
        } else if (selectedEditImageFile != null) {
            String newImg = savePublicationImage(selectedEditImageFile);
            if (newImg != null) imageName = newImg;
        }

        publicationController.updatePublication(
            currentEditingPub.getId(),
            title,
            cat,
            desc,
            imageName,
            currentEditingPub.getUserId()
        );
        
        // Manual sync of current view so buttons/labels stay correct before sidebar refresh
        currentEditingPub.setTitrePub(title);
        currentEditingPub.setCategoriePub(cat);
        currentEditingPub.setDescriptionPub(desc);
        currentEditingPub.setImagePub(imageName);
        updateDetailView(currentEditingPub);
    }

    public void updateDetailView(com.syndicati.models.entities.Publication pub) {
        if (pub == null) {
            this.currentPub = null;
            javafx.application.Platform.runLater(() -> {
                detailTitle.setText("Welcome to the Forum");
                detailDescription.setText("Select a discussion from the sidebar to preview the split-view detail layout.");
                detailCategory.setText("Discussion General");
                detailCategoryPill.setStyle(categoryStyle("General"));
                detailDate.setText("");
                detailAuthor.setText("Horizon Community");
                
                // Clear avatar
                try {
                    VBox hText = (VBox) detailHero.getChildren().get(2);
                    HBox aBox = (HBox) hText.getChildren().get(2);
                    StackPane dAvatar = (StackPane) aBox.getChildren().get(0);
                    Text dInitial = new Text("H");
                    dInitial.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), javafx.scene.text.FontWeight.BOLD, 13));
                    dInitial.setFill(javafx.scene.paint.Color.WHITE);
                    dAvatar.getChildren().setAll(dInitial);
                } catch (Exception ignored) {}
                
                detailImageView.setImage(null);
                detailEditBtn.setVisible(false);
                detailEditBtn.setManaged(false);
                detailDeleteBtn.setVisible(false);
                detailDeleteBtn.setManaged(false);
                
                // Clear comments
                if (commentListContainer != null) {
                    commentListContainer.getChildren().clear();
                }
            });
            return;
        }
        this.currentPub = pub;
        
        javafx.application.Platform.runLater(() -> {
            // 1. Basic Text Fields
            detailTitle.setText(pub.getTitrePub());
            detailDescription.setText(pub.getDescriptionPub());
            String cat = pub.getCategoriePub() != null ? pub.getCategoriePub() : "General";
            detailCategory.setText(cat);
            detailCategoryPill.setStyle(categoryStyle(cat));
            
            if (pub.getDateCreationPub() != null) {
                String date = pub.getDateCreationPub().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"));
                detailDate.setText(date + ", " + pub.getDateCreationPub().getYear());
            } else {
                detailDate.setText("Just now");
            }

            // Sync author names from session if missing (e.g. for newly created posts)
            com.syndicati.models.entities.User sessionUser = com.syndicati.utils.session.SessionManager.getInstance().getCurrentUser();
            if (pub.getAuthorFirstName() == null && sessionUser != null && java.util.Objects.equals(pub.getUserId(), sessionUser.getIdUser())) {
                pub.setAuthorFirstName(sessionUser.getFirstName());
                pub.setAuthorLastName(sessionUser.getLastName());
            }
            detailAuthor.setText(pub.getAuthorFullName() != null ? pub.getAuthorFullName() : "Author");
            
            // 2. Author Avatar logic
            try {
                VBox hText = (VBox) detailHero.getChildren().get(2); // heroText
                HBox aBox = (HBox) hText.getChildren().get(2); // authorBox
                StackPane dAvatar = (StackPane) aBox.getChildren().get(0);
                
                // Reset to initials first
                String initial = pub.getAuthorInitials();
                dAvatar.setOnMouseClicked(null);
                dAvatar.setCursor(javafx.scene.Cursor.DEFAULT);
                
                Text dInitial = new Text(initial);
                dInitial.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), javafx.scene.text.FontWeight.BOLD, 13));
                dInitial.setFill(javafx.scene.paint.Color.WHITE);
                dAvatar.getChildren().setAll(dInitial);

                if (pub.getAuthorAvatar() != null && !pub.getAuthorAvatar().isEmpty()) {
                    String avPath = resolveAvatarPath(pub.getAuthorAvatar());
                    javafx.scene.image.Image avImg = com.syndicati.utils.image.ImageLoaderUtil.loadProfileAvatar(avPath, true);
                    if (avImg != null) {
                        javafx.scene.image.ImageView avView = new javafx.scene.image.ImageView(avImg);
                        avView.setFitWidth(34);
                        avView.setFitHeight(34);
                        avView.setPreserveRatio(true);
                        avView.setSmooth(true);
                        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(17, 17, 17);
                        avView.setClip(clip);
                        dAvatar.getChildren().setAll(avView);
                        dAvatar.setCursor(javafx.scene.Cursor.HAND);
                        dAvatar.setOnMouseClicked(ev -> {
                            ev.consume();
                            showImageLightbox(avView.getImage());
                        });
                    }
                }
            } catch (Exception ignored) {}

            // 3. Publication Image Logic
            if (pub.getImagePub() != null && !pub.getImagePub().isEmpty()) {
                String imgPath = pub.getImagePub();
                if (!imgPath.startsWith("uploads/") && !imgPath.startsWith("forum_images/")) {
                    imgPath = "uploads/forum_images/" + imgPath;
                } else if (imgPath.startsWith("forum_images/")) {
                    imgPath = "uploads/" + imgPath;
                }
                
                try {
                    javafx.scene.image.Image img = com.syndicati.utils.image.ImageLoaderUtil.loadImage(imgPath, false);
                    if (img != null) {
                        detailImageView.setImage(img);
                        // Ratio check logic
                        if (img.getHeight() > 0 && img.getWidth() > 0) {
                            double ratio = img.getWidth() / img.getHeight();
                            if (detailHero.getWidth() / ratio < 250) {
                                detailImageView.fitWidthProperty().unbind();
                                detailImageView.setFitHeight(250);
                            } else {
                                detailImageView.fitHeightProperty().unbind();
                                detailImageView.fitWidthProperty().bind(detailHero.widthProperty());
                            }
                        }
                    } else {
                        detailImageView.setImage(null);
                    }
                } catch (Exception ex) {
                    detailImageView.setImage(null);
                }
            } else {
                detailImageView.setImage(null);
            }
            
            // 3.5 Load Comments
            commentaireController.afficher(pub.getId());
            
            // 3.6 Disable comments for Announcements
            if (commentsCardContainer != null) {
                String catLower = pub.getCategoriePub() != null ? pub.getCategoriePub().toLowerCase() : "";
                boolean isAnnouncement = catLower.contains("announcement") || catLower.contains("annonce");
                commentsCardContainer.setVisible(!isAnnouncement);
                commentsCardContainer.setManaged(!isAnnouncement);
            }

            // 3.7 Update Social Actions for Publication
            updatePublicationReactions(pub.getId());
            
            // 4. Ownership check for Edit/Delete buttons
            try {
                com.syndicati.models.entities.User currentUser = com.syndicati.utils.session.SessionManager.getInstance().getCurrentUser();
                Integer pubUserId = pub.getUserId();
                Integer currentUserId = (currentUser != null) ? currentUser.getIdUser() : null;
                
                boolean isOwner = (pubUserId != null && currentUserId != null && java.util.Objects.equals(pubUserId, currentUserId));
                
                detailEditBtn.setVisible(isOwner);
                detailEditBtn.setManaged(isOwner);
                detailDeleteBtn.setVisible(isOwner);
                detailDeleteBtn.setManaged(isOwner);
            } catch (Exception ex) {
                System.err.println("Error in ownership check: " + ex.getMessage());
                // Don't hide buttons if it's just a metadata error, but keep safety
                if (currentPub != null && pub.getId() != null && java.util.Objects.equals(pub.getId(), currentPub.getId())) {
                     // Keep current state if error occurs during update
                } else {
                    detailEditBtn.setVisible(false);
                    detailEditBtn.setManaged(false);
                    detailDeleteBtn.setVisible(false);
                    detailDeleteBtn.setManaged(false);
                }
            }
        });
    }


    private VBox buildSidebarPane() {
        VBox side = new VBox();
        side.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(255,255,255,0.035), rgba(0,0,0,0.12));");

        VBox header = new VBox(10);
        header.setPadding(new Insets(14));
        header.setStyle("-fx-border-color: transparent transparent " + borderSoft() + " transparent; -fx-border-width: 0 0 1px 0;");

        Text title = sectionTitle("Discussions");
        HBox actions = new HBox(8,
            filledBtn("New Post", () -> switchFace(createFace))
        );

        btnGeneral = toggleFilter("General", true);
        btnGeneral.setOnAction(e -> {
            currentFilter = "General";
            applyFilter();
        });

        btnAnnouncements = toggleFilter("Announcements", false);
        btnAnnouncements.setOnAction(e -> {
            currentFilter = "Announcements";
            applyFilter();
        });

        HBox filters = new HBox(8, btnGeneral, btnAnnouncements);

        header.getChildren().addAll(title, actions, filters);

        sidebarList = new VBox(8);
        sidebarList.setPadding(new Insets(14));

        ScrollPane listScroll = new ScrollPane(sidebarList);
        listScroll.setFitToWidth(true);
        listScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        listScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(listScroll, Priority.ALWAYS);

        side.getChildren().addAll(header, listScroll);
        VBox.setVgrow(listScroll, Priority.ALWAYS);
        return side;
    }

    private VBox forumItem(String category, String title, String author, String initials, String date) {
        VBox item = new VBox(6);
        item.setPadding(new Insets(12, 12, 12, 12));
        item.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: " + borderSoft() + ";" +
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
        
        StackPane avatar = new StackPane();
        avatar.setPrefSize(30, 30);
        
        Circle bgCircle = new Circle(15, 15, 15);
        bgCircle.setFill(Color.web("#3b82f6"));
        
        // Use provided initials for fallback
        String fallbackInitials = (initials != null && !initials.isEmpty()) ? initials : "?";
        
        Label initialLabel = new Label(fallbackInitials);
        initialLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
        initialLabel.setAlignment(Pos.CENTER);
        
        avatar.getChildren().addAll(bgCircle, initialLabel);

        Text authorTxt = mutedSmall(author);
        authorRow.getChildren().addAll(avatar, authorTxt);

        item.getChildren().addAll(top, titleTxt, authorRow);

        item.setOnMouseEntered(e -> {
            item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                "-fx-border-color: " + borderSoft() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-background-radius: 14px;" +
                "-fx-border-radius: 14px;"
            );
            item.setTranslateY(-2);
        });
        item.setOnMouseExited(e -> {
            item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03);" +
                "-fx-border-color: " + borderSoft() + ";" +
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
            // Reset detail avatar to initials placeholder
            String dInit = (initials != null && !initials.isEmpty()) ? initials : "?";
            updateDetailAvatarWithInitials(dInit);
        });

        return item;
    }

    private Button reactionBtn(String text) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-text-fill: " + textSoft() + ";" +
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
                "-fx-text-fill: " + textSoft() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: rgba(255,255,255,0.20);" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;"
            );
            b.setTranslateY(0);
        });
        return b;
    }

    private Button filledAccentBtn(String text, Runnable action) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 12 24;"
        );

        // Hover effect for premium feel
        b.setOnMouseEntered(e -> {
            b.setStyle(
                "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 12 24;" +
                "-fx-effect: dropshadow(gaussian, " + tm.toRgba(tm.getAccentHex(), 0.5) + ", 15, 0.4, 0, 0);"
            );
            b.setTranslateY(-1);
        });
        b.setOnMouseExited(e -> {
            b.setStyle(
                "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 12 24;"
            );
            b.setTranslateY(0);
        });

        if (action != null) {
            b.setOnAction(e -> action.run());
        }
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
            "-fx-text-fill: " + textSoft() + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + borderSoft() + ";" +
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
            "-fx-border-color: " + borderSoft() + ";" +
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
            "-fx-border-color: " + borderSoft() + ";" +
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
        t.setFill(Color.web("rgba(255,255,255,0.94)"));
        t.setMouseTransparent(true);
        return t;
    }

    private Text muted(String text) {
        Text t = new Text(text);
        t.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13));
        t.setFill(Color.web("rgba(255,255,255,0.78)"));
        return t;
    }

    private Text mutedSmall(String text) {
        Text t = new Text(text);
        t.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 11));
        t.setFill(Color.web("rgba(255,255,255,0.76)"));
        return t;
    }

    private Button socialBtn(String icon, Label counter, Runnable action) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER);
        Text iconText = new Text(icon);
        iconText.setFill(Color.web("rgba(255,255,255,0.6)"));
        counter.setTextFill(Color.web("rgba(255,255,255,0.5)"));
        counter.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), 12));
        box.getChildren().addAll(iconText, counter);

        Button btn = new Button();
        btn.setGraphic(box);
        btn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: rgba(255,255,255,0.05);" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12 5 12;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle() + "-fx-background-color: rgba(255,255,255,0.08);"));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace("-fx-background-color: rgba(255,255,255,0.08);", "-fx-background-color: rgba(255,255,255,0.03);")));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private Button socialBtn(String icon, String text, Runnable action) {
        Label label = new Label(text);
        return socialBtn(icon, label, action);
    }

    private String surfaceStrong() {
        return tm.isDarkMode()
            ? "linear-gradient(from 0% 0% to 100% 100%, #020202 0%, #070707 55%, #0b0b0b 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, #ffffff 0%, #f8fafc 100%)";
    }

    private String surfaceCard() {
        return tm.isDarkMode()
            ? "linear-gradient(from 0% 0% to 100% 100%, rgba(10,10,10,0.94) 0%, rgba(14,14,14,0.94) 60%, " + tm.toRgba(tm.getAccentHex(), 0.10) + " 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, rgba(255,255,255,0.98) 0%, rgba(248,250,252,0.96) 100%)";
    }

    private String surfaceSoft() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.12)" : "rgba(15,23,42,0.08)";
    }

    private String borderSoft() {
        return tm.isDarkMode() ? tm.toRgba(tm.getAccentHex(), 0.34) : "rgba(15,23,42,0.16)";
    }

    private String textSoft() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.93)" : "rgba(15,23,42,0.90)";
    }

    private String textMuted() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.79)" : "rgba(30,41,59,0.82)";
    }

    public void setPublications(java.util.List<com.syndicati.models.entities.Publication> publications) {
        this.allPublications = new java.util.ArrayList<>(publications);
        applyFilter();
        
        // After reloading the list, check if the current publication we are viewing was updated
        if (currentPub != null) {
            for (com.syndicati.models.entities.Publication p : allPublications) {
                if (java.util.Objects.equals(p.getId(), currentPub.getId())) {
                    currentPub = p;
                    updateDetailView(p);
                    break;
                }
            }
        }
    }

    private void applyFilter() {
        sidebarList.getChildren().clear();
        
        // Update button styles
        updateFilterButtonStyle(btnGeneral, currentFilter.equals("General"));
        updateFilterButtonStyle(btnAnnouncements, currentFilter.equals("Announcements"));

        java.util.List<com.syndicati.models.entities.Publication> filtered;
        if (currentFilter.equals("Announcements")) {
            filtered = allPublications.stream()
                .filter(p -> p.getCategoriePub() != null && p.getCategoriePub().equalsIgnoreCase("Announcement"))
                .collect(java.util.stream.Collectors.toList());
        } else if (currentFilter.equals("General")) {
            // General shows everything that is NOT an Announcement
            filtered = allPublications.stream()
                .filter(p -> p.getCategoriePub() == null || !p.getCategoriePub().equalsIgnoreCase("Announcement"))
                .collect(java.util.stream.Collectors.toList());
        } else {
            // Specific category filter
            filtered = allPublications.stream()
                .filter(p -> p.getCategoriePub() != null && p.getCategoriePub().equalsIgnoreCase(currentFilter))
                .collect(java.util.stream.Collectors.toList());
        }

            if (filtered.isEmpty()) {
                sidebarList.getChildren().add(muted("No results for " + currentFilter));
            } else {
                for (com.syndicati.models.entities.Publication pub : filtered) {
                    try {
                        sidebarList.getChildren().add(forumItemFromEntity(pub));
                    } catch (Exception e) {
                        System.err.println("Error rendering sidebar item: " + e.getMessage());
                    }
                }
            }
    }

    private void updateFilterButtonStyle(Button b, boolean active) {
        if (b == null) return;
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
    }

    private VBox forumItemFromEntity(com.syndicati.models.entities.Publication pub) {
        // Map entity to forumItem method parameters
        String cat = pub.getCategoriePub() != null ? pub.getCategoriePub() : "General";
        String title = pub.getTitrePub();
        String author = pub.getAuthorFullName();
        String initials = pub.getAuthorInitials();
        String date = pub.getDateCreationPub().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"));

        VBox item = forumItem(cat, title, author, initials, date);
        
        // Load real avatar if available for sidebar
        if (pub.getAuthorAvatar() != null && !pub.getAuthorAvatar().isEmpty()) {
            HBox aRow = (HBox) item.getChildren().get(2);
            StackPane av = (StackPane) aRow.getChildren().get(0);
            String avPath = resolveAvatarPath(pub.getAuthorAvatar());
            try {
                javafx.scene.image.Image avImg = com.syndicati.utils.image.ImageLoaderUtil.loadProfileAvatar(avPath, true);
                if (avImg != null) {
                    javafx.scene.image.ImageView avView = new javafx.scene.image.ImageView(avImg);
                    avView.setFitWidth(30);
                    avView.setFitHeight(30);
                    avView.setPreserveRatio(true);
                    avView.setSmooth(true);
                    javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(15, 15, 15);
                    avView.setClip(clip);
                    av.getChildren().setAll(avView);
                    av.setCursor(javafx.scene.Cursor.HAND);
                    av.setOnMouseClicked(ev -> {
                        ev.consume(); // Prevent parent item click from triggering selection again
                        showImageLightbox(avView.getImage());
                    });
                }
            } catch (Exception ignored) {}
        }
        
        // Update click behavior for entity data
        item.setOnMouseClicked(e -> {
            currentPub = pub;
            updateDetailView(pub);
            switchFace(readFace);
        });

        return item;
    }

    private void updateDetailAvatarWithInitials(String initials) {
        try {
            VBox hText = (VBox) detailHero.getChildren().get(2); // heroText
            HBox aBox = (HBox) hText.getChildren().get(2); // authorBox
            StackPane dAvatar = (StackPane) aBox.getChildren().get(0);
            
            Circle dCircle = new Circle(17, 17, 17);
            dCircle.setFill(Color.web("#3b82f6"));
            
            Label dLabel = new Label(initials);
            dLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
            dLabel.setAlignment(Pos.CENTER);
            
            dAvatar.getChildren().setAll(dCircle, dLabel);
        } catch (Exception ignored) {}
    }

    private String resolveAvatarPath(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) return null;
        if (dbValue.startsWith("uploads/")) return dbValue;
        if (dbValue.startsWith("profile_images/") || dbValue.startsWith("uploads/profile_images/")) {
            return dbValue;
        }
        return "uploads/profile_avatars/" + dbValue;
    }

    private String comboStyle() {
        return "-fx-background-color:" + (tm.isDarkMode() ? "rgba(255,255,255,0.04)" : "rgba(0,0,0,0.05)") + ";" +
               "-fx-text-fill:" + (tm.isDarkMode() ? "#ffffff" : "#1a1a2e") + ";" +
               "-fx-border-color:" + tm.toRgba(tm.getAccentHex(), 0.35) + ";" +
               "-fx-border-width:1.5px;-fx-border-radius:12px;" +
               "-fx-background-radius:12px;-fx-padding:8 14 8 14;" +
               "-fx-font-size:13px;-fx-cursor:hand;";
    }

    private String categoryStyle(String category) {
        if (category == null || category.isBlank()) {
            return "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.15) + "; -fx-background-radius: 12px; -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.4) + "; -fx-border-width: 1px; -fx-border-radius: 12px;";
        }
        
        String key = category.toLowerCase().trim();
        
        // Premium Palette (Muted tints from template)
        if (key.contains("announcement")) {
            return "-fx-background-color: rgba(239, 68, 68, 0.15); -fx-background-radius: 12px; -fx-border-color: rgba(239, 68, 68, 0.4); -fx-border-width: 1px; -fx-border-radius: 12px;";
        }
        if (key.contains("suggestion")) {
            return "-fx-background-color: rgba(16, 185, 129, 0.15); -fx-background-radius: 12px; -fx-border-color: rgba(16, 185, 129, 0.4); -fx-border-width: 1px; -fx-border-radius: 12px;";
        }
        if (key.contains("culture")) {
            return "-fx-background-color: rgba(139, 92, 246, 0.15); -fx-background-radius: 12px; -fx-border-color: rgba(139, 92, 246, 0.4); -fx-border-width: 1px; -fx-border-radius: 12px;";
        }
        if (key.contains("sport")) {
            return "-fx-background-color: rgba(245, 158, 11, 0.15); -fx-background-radius: 12px; -fx-border-color: rgba(245, 158, 11, 0.4); -fx-border-width: 1px; -fx-border-radius: 12px;";
        }
        if (key.contains("nouveaut") || key.contains("new")) {
            return "-fx-background-color: rgba(168, 85, 247, 0.15); -fx-background-radius: 12px; -fx-border-color: rgba(168, 85, 247, 0.4); -fx-border-width: 1px; -fx-border-radius: 12px;";
        }
        
        // Default (Accent-tinted)
        return "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.15) + "; -fx-background-radius: 12px; -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.4) + "; -fx-border-width: 1px; -fx-border-radius: 12px;";
    }

    public void setComments(java.util.List<com.syndicati.models.entities.Commentaire> comments) {
        commentListContainer.getChildren().clear();
        if (comments == null || comments.isEmpty()) {
            commentListContainer.getChildren().add(muted("No comments yet. Be the first to start the discussion!"));
            return;
        }

        for (com.syndicati.models.entities.Commentaire c : comments) {
            commentListContainer.getChildren().add(buildCommentItem(c));
        }
    }

    private VBox buildCommentItem(com.syndicati.models.entities.Commentaire c) {
        VBox item = new VBox(10);
        item.setPadding(new Insets(12));
        item.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 0 0 1px 0;"
        );

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        boolean isAnonymous = (c.getVisibility() != null && c.getVisibility() == 0);

        // Avatar
        StackPane avatarPane = new StackPane();
        avatarPane.setPrefSize(28, 28);
        avatarPane.setStyle("-fx-background-color: " + (isAnonymous ? "#2d2d3d" : surfaceSoft()) + "; -fx-background-radius: 14px;");
        
        Text initial = new Text(isAnonymous ? "👻" : c.getAuthorInitials());
        initial.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), javafx.scene.text.FontWeight.BOLD, isAnonymous ? 14 : 10));
        initial.setFill(javafx.scene.paint.Color.WHITE);
        avatarPane.getChildren().add(initial);

        if (!isAnonymous && c.getAuthorAvatar() != null && !c.getAuthorAvatar().isEmpty()) {
             String avPath = resolveAvatarPath(c.getAuthorAvatar());
             javafx.scene.image.Image avImg = com.syndicati.utils.image.ImageLoaderUtil.loadProfileAvatar(avPath, true);
             if (avImg != null) {
                 javafx.scene.image.ImageView avView = new javafx.scene.image.ImageView(avImg);
                 avView.setFitWidth(28);
                 avView.setFitHeight(28);
                 javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(14, 14, 14);
                 avView.setClip(clip);
                 avatarPane.getChildren().setAll(avView);
             }
        }

        VBox meta = new VBox(2);
        Text name = new Text(isAnonymous ? "Anonyme" : c.getAuthorFullName());
        name.setFont(Font.font(com.syndicati.MainApplication.getInstance().getBoldFontFamily(), javafx.scene.text.FontWeight.BOLD, 12));
        name.setFill(isAnonymous ? javafx.scene.paint.Color.web("#94a3b8") : javafx.scene.paint.Color.WHITE);

        Text date = new Text(c.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm")));
        date.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), 10));
        date.setFill(javafx.scene.paint.Color.web(textSoft()));
        
        meta.getChildren().addAll(name, date);
        header.getChildren().addAll(avatarPane, meta);

        // Ownership check for Edit & Delete (Authors can edit even if anonymous)
        com.syndicati.models.entities.User currentUser = com.syndicati.utils.session.SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getIdUser().equals(c.getIdUser())) {
             Button editIcon = new Button("✎");
             editIcon.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand; -fx-padding: 0 0 0 10;");
             editIcon.setOnAction(e -> {
                 for (javafx.scene.Node node : item.getChildren()) {
                     if (node instanceof Text && ((Text)node).getText().equals(c.getDescriptionCommentaire())) {
                         startCommentEdit(item, (Text)node, c);
                         break;
                     }
                 }
             });

             Button deleteIcon = new Button("🗑");
             deleteIcon.setStyle("-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-cursor: hand; -fx-padding: 0 0 0 10; -fx-font-size: 14px;");
             deleteIcon.setOnAction(e -> {
                 showDeleteConfirmation("Delete Comment", "Are you sure you want to delete this comment permanently?", () -> {
                     commentaireController.supprimerCommentaire(c.getIdCommentaire(), c.getIdPub());
                 });
             });

             header.getChildren().addAll(editIcon, deleteIcon);
        }

        Text body = new Text(c.getDescriptionCommentaire());
        body.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), 13));
        body.setFill(javafx.scene.paint.Color.web("#d1d1d6"));
        body.setWrappingWidth(500);

        item.getChildren().addAll(header, body);

        // Comment Image
        if (c.getImageCommentaire() != null && !c.getImageCommentaire().isEmpty()) {
            String cImgPath = "uploads/comment_images/" + c.getImageCommentaire();
            try {
                javafx.scene.image.Image cImg = com.syndicati.utils.image.ImageLoaderUtil.loadImage(cImgPath, false);
                if (cImg != null) {
                    javafx.scene.image.ImageView cImgView = new javafx.scene.image.ImageView(cImg);
                    cImgView.setFitWidth(300);
                    cImgView.setPreserveRatio(true);
                    cImgView.setCursor(javafx.scene.Cursor.HAND);
                    cImgView.setOnMouseClicked(e -> showImageLightbox(cImg));
                    
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
                    clip.setArcWidth(20);
                    clip.setArcHeight(20);
                    clip.widthProperty().bind(cImgView.fitWidthProperty());
                    clip.heightProperty().bind(cImgView.layoutBoundsProperty().map(b -> b.getHeight()));
                    cImgView.setClip(clip);

                    item.getChildren().add(cImgView);
                }
            } catch (Exception ignored) {}
        }

        // Reaction Footer for Comments
        HBox commentFooter = new HBox(10);
        commentFooter.setAlignment(Pos.CENTER_LEFT);
        commentFooter.setPadding(new Insets(4, 0, 0, 0));

        Label cLikeCount = new Label(String.valueOf(reactionController.getCount(null, c.getIdCommentaire(), PubCommentReaction.KIND_LIKE)));
        Label cDislikeCount = new Label(String.valueOf(reactionController.getCount(null, c.getIdCommentaire(), PubCommentReaction.KIND_DISLIKE)));

        Button cLikeBtn = socialBtn("👍", cLikeCount, () -> reactionController.handleReaction(null, c.getIdCommentaire(), PubCommentReaction.KIND_LIKE, null, null));
        Button cDislikeBtn = socialBtn("👎", cDislikeCount, () -> reactionController.handleReaction(null, c.getIdCommentaire(), PubCommentReaction.KIND_DISLIKE, null, null));
        Button cSignalBtn = socialBtn("🚩", "", () -> {
            showReportDialog(reason -> {
                reactionController.handleReaction(null, c.getIdCommentaire(), PubCommentReaction.KIND_REPORT, null, reason);
            });
        });

        // Highlight active states
        if (reactionController.hasReacted(null, c.getIdCommentaire(), PubCommentReaction.KIND_LIKE)) updateSocialBtnState(cLikeBtn, true, "#2ed573");
        if (reactionController.hasReacted(null, c.getIdCommentaire(), PubCommentReaction.KIND_DISLIKE)) updateSocialBtnState(cDislikeBtn, true, "#ff4757");
        if (reactionController.hasReacted(null, c.getIdCommentaire(), PubCommentReaction.KIND_REPORT)) updateSocialBtnState(cSignalBtn, true, "#ffa502");

        commentFooter.getChildren().addAll(cLikeBtn, cDislikeBtn, cSignalBtn);
        item.getChildren().add(commentFooter);

        return item;
    }

    private VBox buildCommentForm() {
        VBox form = new VBox(12);
        
        commentTextArea = new TextArea();
        commentTextArea.setPromptText("Write a comment...");
        commentTextArea.setPrefRowCount(3);
        commentTextArea.setWrapText(true);
        commentTextArea.setStyle(
            "-fx-control-inner-background: #1a1a2e;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: gray;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-radius: 10px;"
        );

        selectedCommentImageLabel = new Label();
        selectedCommentImageLabel.setFont(Font.font(com.syndicati.MainApplication.getInstance().getLightFontFamily(), 11));
        selectedCommentImageLabel.setTextFill(javafx.scene.paint.Color.web(tm.getAccentHex()));

        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER_LEFT);

        Button attachBtn = ghostBtn("Attach Photo", () -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Select Image");
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            java.io.File selected = fc.showOpenDialog(root.getScene().getWindow());
            if (selected != null) {
                commentImageFile = selected;
                selectedCommentImageLabel.setText("Selected: " + selected.getName());
            }
        });
        attachBtn.setStyle(attachBtn.getStyle() + "-fx-font-size: 11px;");

        anonymousToggleBtn = ghostBtn("Comment Anonymously", () -> {
            isCommentAnonymous = !isCommentAnonymous;
            if (isCommentAnonymous) {
                anonymousToggleBtn.setStyle(anonymousToggleBtn.getStyle() + "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.2) + "; -fx-border-color: " + tm.getAccentHex() + ";");
            } else {
                anonymousToggleBtn.setStyle(ghostBtn("Comment Anonymously", null).getStyle() + "-fx-font-size: 11px;");
            }
        });
        anonymousToggleBtn.setStyle(anonymousToggleBtn.getStyle() + "-fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button postBtn = filledBtn("Post Comment", () -> {
            if (currentPub != null) {
                String imgName = saveCommentImage(commentImageFile);
                int visibility = isCommentAnonymous ? 0 : 1;
                commentaireController.ajouterCommentaire(currentPub.getId(), commentTextArea.getText(), imgName, visibility);
            } else {
                showNotification("Please select a publication first.", "error");
            }
        });
        postBtn.setPrefWidth(140);

        controls.getChildren().addAll(attachBtn, selectedCommentImageLabel, anonymousToggleBtn, spacer, postBtn);
        form.getChildren().addAll(commentTextArea, controls);
        return form;
    }

    private String saveCommentImage(java.io.File file) {
        if (file == null) return null;
        try {
            java.io.File dir = new java.io.File("uploads/comment_images");
            if (!dir.exists()) dir.mkdirs();
            String fileName = System.currentTimeMillis() + "_comm_" + file.getName();
            java.io.File dest = new java.io.File(dir, fileName);
            java.nio.file.Files.copy(file.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return fileName; 
        } catch (Exception e) {
            System.err.println("Error saving comment image: " + e.getMessage());
            return null;
        }
    }

    public void clearCommentForm() {
        if (commentTextArea != null) {
            commentTextArea.clear();
        }
        commentImageFile = null;
        selectedCommentImageLabel.setText("");
        isCommentAnonymous = false;
        if (anonymousToggleBtn != null) {
            anonymousToggleBtn.setStyle(ghostBtn("Comment Anonymously", null).getStyle() + "-fx-font-size: 11px;");
        }
    }

    private void startCommentEdit(VBox item, Text body, com.syndicati.models.entities.Commentaire c) {
        int index = item.getChildren().indexOf(body);
        if (index == -1) return;

        // Local state for the edit session
        final java.io.File[] editFile = {null};
        final boolean[] editAnonyme = {c.getVisibility() == 0};
        final String[] currentImgName = {c.getImageCommentaire()};

        TextArea editArea = new TextArea(c.getDescriptionCommentaire());
        editArea.setWrapText(true);
        editArea.setPrefRowCount(3);
        editArea.setStyle("-fx-control-inner-background: #1a1a2e; -fx-text-fill: white; -fx-background-radius: 8px; -fx-font-size: 13px;");

        VBox editControls = new VBox(8);
        editControls.setPadding(new Insets(5, 0, 0, 0));

        HBox toolBar = new HBox(10);
        toolBar.setAlignment(Pos.CENTER_LEFT);

        Label imgStatus = new Label(currentImgName[0] != null ? "Current: " + currentImgName[0] : "No image");
        imgStatus.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        Button chgImgBtn = ghostBtn("📷 Update Photo", () -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
            java.io.File selected = fc.showOpenDialog(root.getScene().getWindow());
            if (selected != null) {
                editFile[0] = selected;
                imgStatus.setText("New: " + selected.getName());
            }
        });
        chgImgBtn.setStyle(chgImgBtn.getStyle() + "-fx-font-size: 11px; -fx-padding: 3 8;");

        Button rmImgBtn = ghostBtn("🗑 Remove", () -> {
            editFile[0] = null;
            currentImgName[0] = null;
            imgStatus.setText("No image");
        });
        rmImgBtn.setStyle(rmImgBtn.getStyle() + "-fx-font-size: 11px; -fx-padding: 3 8; -fx-text-fill: #ef4444;");

        Button anonBtn = ghostBtn(editAnonyme[0] ? "👻 Anonymous: ON" : "👤 Anonymous: OFF", null);
        anonBtn.setOnAction(e -> {
            editAnonyme[0] = !editAnonyme[0];
            anonBtn.setText(editAnonyme[0] ? "👻 Anonymous: ON" : "👤 Anonymous: OFF");
            if (editAnonyme[0]) {
                anonBtn.setStyle(anonBtn.getStyle() + "-fx-background-color: rgba(99, 102, 241, 0.2); -fx-border-color: #6366f1;");
            } else {
                anonBtn.setStyle(ghostBtn("Anonymous", null).getStyle() + "-fx-font-size: 11px; -fx-padding: 3 8;");
            }
        });
        if (editAnonyme[0]) anonBtn.setStyle(anonBtn.getStyle() + "-fx-background-color: rgba(99, 102, 241, 0.2); -fx-border-color: #6366f1;");
        anonBtn.setStyle(anonBtn.getStyle() + "-fx-font-size: 11px; -fx-padding: 3 8;");

        toolBar.getChildren().addAll(chgImgBtn, rmImgBtn, anonBtn, imgStatus);

        HBox editActions = new HBox(10);
        editActions.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveBtn = filledBtn("Save Changes", () -> {
            String finalImg = currentImgName[0];
            if (editFile[0] != null) {
                finalImg = saveCommentImage(editFile[0]);
            }
            int visibility = editAnonyme[0] ? 0 : 1;
            commentaireController.modifierCommentaire(c.getIdCommentaire(), c.getIdPub(), editArea.getText(), finalImg, visibility);
        });
        saveBtn.setStyle(saveBtn.getStyle() + "-fx-font-size: 11px; -fx-padding: 4 12;");
        
        Button cancelBtn = ghostBtn("Cancel", () -> {
            item.getChildren().setAll(item.getChildren().get(0), body); // Keep header, restore body
            // This is a bit brittle if the header moves, better to refresh discussion
            commentaireController.afficher(c.getIdPub());
        });
        cancelBtn.setStyle(cancelBtn.getStyle() + "-fx-font-size: 11px; -fx-padding: 4 12;");

        editActions.getChildren().addAll(cancelBtn, saveBtn);
        
        editControls.getChildren().addAll(toolBar, editActions);
        
        item.getChildren().set(index, editArea);
        item.getChildren().add(index + 1, editControls);
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

