package com.syndicati.views.frontend.services;

import com.syndicati.MainApplication;
import com.syndicati.controllers.forum.CommentaireController;
import com.syndicati.controllers.forum.PublicationController;
import com.syndicati.controllers.forum.ReactionController;
import com.syndicati.controllers.user.profile.ProfileController;
import com.syndicati.interfaces.ViewInterface;
<<<<<<< HEAD
import com.syndicati.models.entities.Publication;
import com.syndicati.models.entities.PubCommentReaction;
=======
import com.syndicati.models.forum.Commentaire;
import com.syndicati.models.forum.Publication;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import com.syndicati.services.forum.ReactionService.ReactionActionResult;
import com.syndicati.services.forum.ReactionService.ReactionPayload;
import com.syndicati.services.forum.ReactionService.ReactionStatus;
import com.syndicati.utils.image.ImageLoaderUtil;
import com.syndicati.utils.session.SessionManager;
>>>>>>> User
import com.syndicati.utils.theme.ThemeManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
<<<<<<< HEAD
import javafx.scene.shape.Circle;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.ComboBox;
import javafx.util.Duration;
=======
import javafx.scene.Cursor;
import javafx.stage.FileChooser;
import javafx.stage.Window;
>>>>>>> User

public class ForumPageView implements ViewInterface {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter SHORT_DATE_FMT = DateTimeFormatter.ofPattern("MMM dd");
    private static final List<String> CATEGORIES = List.of(
        "Announcement", "Suggestion", "Jeux Video", "Informatique", "Nouveauté", "Discussion General", "Culture", "Sport"
    );
    private static final List<String> MODERATOR_ROLES = List.of("OWNER", "ADMIN", "SUPERADMIN", "SYNDIC");
    private static final List<String> EMOJIS = List.of("❤️", "😂", "😮", "😢", "😡", "👍", "🔥", "✨");

    private final VBox root = new VBox(18);
    private final ThemeManager tm = ThemeManager.getInstance();
<<<<<<< HEAD
    private com.syndicati.controllers.frontend.services.forum.PublicationController publicationController;
    private com.syndicati.controllers.frontend.services.forum.CommentaireController commentaireController;
    private com.syndicati.controllers.frontend.services.forum.PubCommentReactionController reactionController;
    private VBox sidebarList;
=======
    private final SessionManager session = SessionManager.getInstance();
    private final PublicationController publications = new PublicationController();
    private final CommentaireController comments = new CommentaireController();
    private final ReactionController reactions = new ReactionController();
    private final ProfileController profiles = new ProfileController();
>>>>>>> User

    private final VBox listBox = new VBox(10);
    private final StackPane faceStack = new StackPane();
<<<<<<< HEAD
    private VBox readFace;
    private VBox createFace;
    private VBox editFace;
    private Publication currentPub;

    private java.util.List<com.syndicati.models.entities.Publication> allPublications = new java.util.ArrayList<>();
    private String currentFilter = "General";
    private Button btnGeneral;
    private Button btnAnnouncements;
    private Button btnFavorites;

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
            if (currentPub != null) {
                boolean alreadyBookmarked = reactionController.hasReacted(currentPub.getId(), null, PubCommentReaction.KIND_BOOKMARK);
                String title = alreadyBookmarked ? "Retirer des favoris" : "Ajouter aux favoris";
                String msg = alreadyBookmarked ? "Voulez-vous retirer cette publication de vos favoris ?" : "Voulez-vous ajouter cette publication à vos favoris ?";
                
                showBookmarkConfirmation(title, msg, () -> {
                    reactionController.handleReaction(currentPub.getId(), null, PubCommentReaction.KIND_BOOKMARK, null, null);
                });
            }
        });
        btnSignalPub = socialBtn("🚩", countReportPub, () -> {
            if (currentPub != null) {
                boolean alreadySignaled = reactionController.hasReacted(currentPub.getId(), null, PubCommentReaction.KIND_REPORT);
                String title = alreadySignaled ? "Retirer le signalement" : "Signaler le contenu";
                String msg = alreadySignaled ? "Voulez-vous retirer votre signalement sur cette publication ?" : "Voulez-vous vraiment signaler cette publication ?";
                
                showSignalConfirmation(title, msg, () -> {
                    reactionController.handleReaction(currentPub.getId(), null, PubCommentReaction.KIND_REPORT, null, "Signaled");
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
            refreshSingleComment(commId);
        }
    }

    private void refreshSingleComment(int commId) {
        javafx.application.Platform.runLater(() -> {
            com.syndicati.models.entities.Commentaire updated = commentaireController.getCommentById(commId);
            if (updated == null) return;

            for (int i = 0; i < commentListContainer.getChildren().size(); i++) {
                javafx.scene.Node node = commentListContainer.getChildren().get(i);
                if (node instanceof javafx.scene.layout.VBox && ("comment-" + commId).equals(node.getId())) {
                    javafx.scene.layout.VBox newItem = buildCommentItem(updated);
                    commentListContainer.getChildren().set(i, newItem);
                    break;
                }
            }
        });
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

    private void showBookmarkConfirmation(String titleText, String msgText, Runnable onConfirm) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        overlay.setOpacity(0);

        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setMaxSize(400, 200);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: " + surfaceCard() + ";" +
            "-fx-border-color: #00d2ff;" +
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

        Button cancelBtn = ghostBtn("Annuler", () -> hideLightbox(overlay));
        Button confirmBtn = filledBtn("Confirmer", () -> {
            onConfirm.run();
            hideLightbox(overlay);
        });
        confirmBtn.setStyle(confirmBtn.getStyle() + "-fx-background-color: #00d2ff;");

        HBox btns = new HBox(12, cancelBtn, confirmBtn);
        btns.setAlignment(Pos.CENTER);

        card.getChildren().addAll(title, msg, btns);
        overlay.getChildren().add(card);
        faceStack.getChildren().add(overlay);

        FadeTransition ft = new FadeTransition(Duration.millis(300), overlay);
        ft.setToValue(1);
        ft.play();
    }

    private void showSignalConfirmation(String titleText, String msgText, Runnable onConfirm) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        overlay.setOpacity(0);

        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setMaxSize(400, 200);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: " + surfaceCard() + ";" +
            "-fx-border-color: #ffa502;" +
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

        Button cancelBtn = ghostBtn("Annuler", () -> hideLightbox(overlay));
        Button confirmBtn = filledBtn("Confirmer", () -> {
            onConfirm.run();
            hideLightbox(overlay);
        });
        confirmBtn.setStyle(confirmBtn.getStyle() + "-fx-background-color: #ffa502;");

        HBox btns = new HBox(12, cancelBtn, confirmBtn);
        btns.setAlignment(Pos.CENTER);

        card.getChildren().addAll(title, msg, btns);
        overlay.getChildren().add(card);
        faceStack.getChildren().add(overlay);

        FadeTransition ft = new FadeTransition(Duration.millis(300), overlay);
        ft.setToValue(1);
        ft.play();
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
            publicationController.afficher();
        });

        btnAnnouncements = toggleFilter("Announcements", false);
        btnAnnouncements.setOnAction(e -> {
            currentFilter = "Announcements";
            applyFilter();
        });

        btnFavorites = toggleFilter("Favoris", false);
        btnFavorites.setOnAction(e -> {
            currentFilter = "Favoris";
            publicationController.afficherFavoris();
        });

        HBox filters = new HBox(8, btnGeneral, btnAnnouncements, btnFavorites);

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
        if (btnFavorites != null) updateFilterButtonStyle(btnFavorites, currentFilter.equals("Favoris"));

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
        item.setId("comment-" + c.getIdCommentaire());
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
        Label cReportCount = new Label(String.valueOf(reactionController.getCount(null, c.getIdCommentaire(), PubCommentReaction.KIND_REPORT)));

        Button cLikeBtn = socialBtn("👍", cLikeCount, () -> reactionController.handleReaction(null, c.getIdCommentaire(), PubCommentReaction.KIND_LIKE, null, null));
        Button cDislikeBtn = socialBtn("👎", cDislikeCount, () -> reactionController.handleReaction(null, c.getIdCommentaire(), PubCommentReaction.KIND_DISLIKE, null, null));
        Button cSignalBtn = socialBtn("🚩", cReportCount, () -> {
            boolean alreadySignaled = reactionController.hasReacted(null, c.getIdCommentaire(), PubCommentReaction.KIND_REPORT);
            String title = alreadySignaled ? "Retirer le signalement" : "Signaler le contenu";
            String msg = alreadySignaled ? "Voulez-vous retirer votre signalement sur ce commentaire ?" : "Voulez-vous vraiment signaler ce commentaire ?";
            
            showSignalConfirmation(title, msg, () -> {
                reactionController.handleReaction(null, c.getIdCommentaire(), PubCommentReaction.KIND_REPORT, null, "Signaled");
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
=======
    private final Map<Integer, VBox> listItemsById = new HashMap<>();

    private final VBox readFace = new VBox();
    private final VBox createFace = new VBox();
    private final VBox editFace = new VBox();

    private final StackPane detailHero = new StackPane();
    private final ImageView detailHeroImage = new ImageView();
    private final Rectangle detailHeroClip = new Rectangle();
    private Image currentHeroImage;

    private final Text heroTitle = new Text("Welcome to the Forum");
    private final Text heroMeta = new Text("");
    private final Text heroBody = new Text("Select a publication from the right panel.");
    private final Text heroCategory = new Text("Discussion General");
    private final HBox ownerActions = new HBox(8);

    private final Button postLikeButton = new Button("Like");
    private final Button postDislikeButton = new Button("Dislike");
    private final Button postEmojiButton = new Button("Emoji");
    private final Button postBookmarkButton = new Button("Bookmark");
    private final Button postReportButton = new Button("Report");
    private final Label postLikeCount = new Label("");
    private final Label postDislikeCount = new Label("");
    private final ProgressBar postRatioBar = new ProgressBar(0);
    private final Label postRatioText = new Label("0%");

    private final VBox commentsBox = new VBox(10);
    private final VBox publicationReportPanel = new VBox(8);
    private final VBox publicationEmojiPanel = new VBox(8);
    private final Map<Integer, VBox> commentEditPanels = new HashMap<>();
    private final Map<Integer, VBox> commentReportPanels = new HashMap<>();
    private final Map<Integer, VBox> commentEmojiPanels = new HashMap<>();
    private final Map<Integer, Profile> profileByUserIdCache = new HashMap<>();
    private Commentaire currentCommentForInline;

    private final TextField createTitle = new TextField();
    private final ComboBox<String> createCategory = new ComboBox<>();
    private final TextArea createDescription = new TextArea();
    private final Label createTitleValidation = new Label();
    private final Label createCategoryValidation = new Label();
    private final Label createDescriptionValidation = new Label();
    private final TextField createImageField = new TextField();
    private final ImageView createImagePreview = new ImageView();
    private File createImage;
    private boolean forceAnnouncementCreate;

    private final TextField editTitle = new TextField();
    private final ComboBox<String> editCategory = new ComboBox<>();
    private final TextArea editDescription = new TextArea();
    private final Label editTitleValidation = new Label();
    private final Label editCategoryValidation = new Label();
    private final Label editDescriptionValidation = new Label();
    private final TextField editImageField = new TextField();
    private final ImageView editCurrentImagePreview = new ImageView();
    private final ImageView editNewImagePreview = new ImageView();
    private File editImage;

    private final TextArea commentInput = new TextArea();
    private final Label commentValidation = new Label();
    private boolean isCommentPublic = true;
    private final TextField commentImageField = new TextField();
    private final List<File> commentImages = new ArrayList<>();
    private final FlowPane commentImagePreviews = new FlowPane();

    private final Button filterGeneral = new Button("General");
    private final Button filterAnnouncements = new Button("Announcements");

    private Publication current;
    private String currentFilter = "General";

    public ForumPageView() {
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: transparent;");
        root.setAlignment(Pos.TOP_CENTER);

        root.getChildren().addAll(buildHero(), buildSplit());
        showFace(readFace);
        loadCategory("General");
>>>>>>> User
    }

    @Override
    public VBox getRoot() {
        return root;
    }

    @Override
    public void cleanup() {
    }

    private Node buildHero() {
        VBox hero = new VBox(12);
        hero.setPadding(new Insets(34));
        hero.setMaxWidth(1800);
        hero.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #090909 0%, #131313 100%);" +
            "-fx-background-radius: 48px;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-radius: 48px;"
        );

        Label badge = new Label("COMMUNITY HUB");
        badge.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.14) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.45) + ";" +
            "-fx-border-radius: 100px;" +
            "-fx-background-radius: 100px;" +
            "-fx-padding: 7 16 7 16;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: 800;" +
            "-fx-text-fill: white;"
        );

        Text title = new Text("Voices of Horizon");
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BLACK, 66));
        title.setFill(Color.WHITE);

        Text subtitle = new Text("Join the conversation. Connect with neighbors, share ideas, and shape your community.");
        subtitle.setWrappingWidth(840);
        subtitle.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 17));
        subtitle.setFill(Color.web("rgba(255,255,255,0.68)"));

        hero.getChildren().addAll(badge, title, subtitle);
        return hero;
    }

    private Node buildSplit() {
        HBox split = new HBox();
        split.setMinWidth(0);
        split.setMaxWidth(1800);
        split.setPrefHeight(860);
        split.setStyle(
            "-fx-background-color: #0a0a0c;" +
            "-fx-background-radius: 32px;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-radius: 32px;"
        );

        VBox main = buildMainPanel();
        VBox side = buildSidePanel();
        side.setPrefWidth(360);
        side.setMaxWidth(360);
        side.setMinWidth(360);

        main.setMinWidth(0);
        main.prefWidthProperty().bind(
            Bindings.max(0, split.widthProperty().subtract(side.widthProperty()))
        );
        HBox.setHgrow(main, Priority.ALWAYS);
        HBox.setHgrow(side, Priority.NEVER);
        split.getChildren().addAll(main, side);
        return split;
    }

    private VBox buildMainPanel() {
        VBox panel = new VBox();
        panel.setMinWidth(0);
        panel.setPadding(new Insets(0));
        panel.setStyle("-fx-border-color: transparent rgba(255,255,255,0.10) transparent transparent; -fx-border-width: 0 1px 0 0;");
        panel.setMaxWidth(Double.MAX_VALUE);

        Rectangle panelClip = new Rectangle();
        panelClip.widthProperty().bind(panel.widthProperty());
        panelClip.heightProperty().bind(panel.heightProperty());
        panel.setClip(panelClip);

        faceStack.getChildren().addAll(readFace, createFace, editFace);
        buildReadFace();
        buildCreateFace();
        buildEditFace();

        VBox.setVgrow(faceStack, Priority.ALWAYS);
        panel.getChildren().add(faceStack);
        return panel;
    }

    private void buildReadFace() {
        readFace.getChildren().clear();
        readFace.setMinWidth(0);
        readFace.setMaxWidth(Double.MAX_VALUE);
        readFace.setPrefWidth(Region.USE_COMPUTED_SIZE);

        detailHero.setPrefHeight(320);
        detailHero.setMinHeight(320);
        detailHero.setMaxHeight(320);
        detailHero.setMinWidth(0);
        detailHero.setMaxWidth(Double.MAX_VALUE);
        detailHero.setClip(detailHeroClip);
        detailHero.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a1a2e 0%, #16213e 100%); -fx-border-radius: 24px 0px 0px 24px;");

        detailHeroClip.setArcWidth(24);
        detailHeroClip.setArcHeight(24);
        detailHeroClip.widthProperty().bind(detailHero.widthProperty());
        detailHeroClip.heightProperty().bind(detailHero.heightProperty());

        detailHeroImage.setSmooth(true);
        detailHeroImage.setPreserveRatio(false);
        detailHero.widthProperty().addListener((obs, oldV, newV) -> refreshHeroViewport());
        detailHero.heightProperty().addListener((obs, oldV, newV) -> refreshHeroViewport());

        heroTitle.setFill(Color.WHITE);
        heroTitle.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 42));
        heroMeta.setFill(Color.web("rgba(255,255,255,0.72)"));
        heroCategory.setFill(Color.web("rgba(255,255,255,0.95)"));
        heroBody.setFill(Color.web("rgba(255,255,255,0.90)"));
        heroBody.wrappingWidthProperty().bind(detailHero.widthProperty().subtract(52));

        VBox heroOverlay = new VBox(10);
        heroOverlay.setPadding(new Insets(28));
        heroOverlay.setAlignment(Pos.BOTTOM_LEFT);
        heroOverlay.setStyle("-fx-background-color: linear-gradient(from 0% 100% to 0% 0%, rgba(20,20,30,0.94) 3%, rgba(20,20,30,0.08) 100%);");

        Label categoryPill = new Label();
        categoryPill.textProperty().bind(heroCategory.textProperty());
        categoryPill.setStyle(categoryStyle(heroCategory.getText()));
        heroCategory.textProperty().addListener((obs, oldVal, newVal) -> categoryPill.setStyle(categoryStyle(newVal)));

        heroOverlay.getChildren().addAll(categoryPill, heroMeta, heroTitle);
        detailHero.getChildren().setAll(detailHeroImage, heroOverlay);
        StackPane.setAlignment(heroOverlay, Pos.BOTTOM_LEFT);

        FlowPane reactionCluster = buildPublicationReactionCluster();

        BorderPane actionsBar = new BorderPane();
        actionsBar.setMinWidth(0);
        actionsBar.setPadding(new Insets(12));
        actionsBar.setPrefWidth(Region.USE_COMPUTED_SIZE);
        actionsBar.setMaxWidth(Double.MAX_VALUE);
        actionsBar.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 14px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 14px;");

        Rectangle actionsBarClip = new Rectangle();
        actionsBarClip.widthProperty().bind(actionsBar.widthProperty());
        actionsBarClip.heightProperty().bind(actionsBar.heightProperty());
        actionsBar.setClip(actionsBarClip);

        Button createSwitcher = ghostButton("New Post", () -> openCreateFace(false));
        Button editSwitcher = ghostButton("Edit", this::openEditFace);
        Button deleteSwitcher = ghostButton("Delete", this::deleteCurrentPublication);

        ownerActions.getChildren().setAll(editSwitcher, deleteSwitcher);
        ownerActions.setManaged(false);
        ownerActions.setVisible(false);

        VBox reactionWrap = new VBox(reactionCluster);
        reactionWrap.setMinWidth(0);
        reactionWrap.setMaxWidth(Double.MAX_VALUE);
        BorderPane.setAlignment(reactionWrap, Pos.CENTER_LEFT);

        postReportButton.setMinWidth(Region.USE_PREF_SIZE);
        postReportButton.setMaxWidth(Region.USE_PREF_SIZE);

        HBox rightActions = new HBox(8, ownerActions, postReportButton);
        rightActions.setAlignment(Pos.CENTER_RIGHT);
        rightActions.setMinWidth(Region.USE_PREF_SIZE);
        rightActions.setMaxWidth(Region.USE_PREF_SIZE);
        BorderPane.setAlignment(rightActions, Pos.CENTER_RIGHT);

        reactionCluster.setMinWidth(0);
        reactionCluster.prefWrapLengthProperty().bind(
            Bindings.max(120, actionsBar.widthProperty().subtract(rightActions.widthProperty()).subtract(40))
        );

        actionsBar.setCenter(reactionWrap);
        actionsBar.setRight(rightActions);

        VBox publicationBody = new VBox(10, heroBody, actionsBar);
        publicationBody.setPadding(new Insets(22, 0, 10, 0));
        publicationBody.setMinWidth(0);
        publicationBody.setMaxWidth(Double.MAX_VALUE);

        buildPublicationReportPanel();
        buildPublicationEmojiPanel();
        VBox inlinePanels = new VBox(8, publicationReportPanel, publicationEmojiPanel);
        inlinePanels.setPadding(new Insets(0, 0, 0, 0));
        inlinePanels.setFillWidth(true);
        inlinePanels.setMaxWidth(Double.MAX_VALUE);

        VBox commentsSection = buildCommentsSection();
        VBox.setMargin(commentsSection, new Insets(20, 0, 0, 0));
        commentsSection.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(publicationBody, inlinePanels, commentsSection);
        content.setPadding(new Insets(0, 26, 26, 26));
        content.setMinWidth(0);
        content.setFillWidth(true);
        content.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setMinWidth(0);
        scroll.setMaxWidth(Double.MAX_VALUE);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Hard clip to prevent content from overflowing behind sidebar
        Rectangle scrollClip = new Rectangle();
        scrollClip.widthProperty().bind(scroll.widthProperty());
        scrollClip.heightProperty().bind(scroll.heightProperty());
        scroll.setClip(scrollClip);

        // Force content to fit scroll pane width
        content.prefWidthProperty().bind(scroll.widthProperty().subtract(52));
        content.setMaxWidth(Double.MAX_VALUE);

        readFace.getChildren().addAll(detailHero, scroll);
    }

    private FlowPane buildPublicationReactionCluster() {
        styleReactionButton(postLikeButton);
        styleReactionButton(postDislikeButton);
        styleReactionButton(postEmojiButton);
        styleReactionButton(postBookmarkButton);
        styleReactionButton(postReportButton);

        postLikeButton.setOnAction(e -> togglePublicationReaction("Like"));
        postDislikeButton.setOnAction(e -> togglePublicationReaction("Dislike"));
        postEmojiButton.setOnAction(e -> togglePublicationEmojiPanel());
        postBookmarkButton.setOnAction(e -> togglePublicationReaction("Bookmark"));
        postReportButton.setOnAction(e -> togglePublicationReportPanel());

        HBox likeWrap = reactionCountWrap(postLikeButton, postLikeCount);
        HBox dislikeWrap = reactionCountWrap(postDislikeButton, postDislikeCount);

        postRatioBar.setPrefWidth(110);
        postRatioBar.setPrefHeight(8);
        postRatioBar.setStyle("-fx-accent: #4ade80; -fx-control-inner-background: #f87171;");
        postRatioText.setTextFill(Color.web("rgba(255,255,255,0.72)"));
        postRatioText.setStyle("-fx-font-size: 11px; -fx-font-weight: 700;");

        HBox ratioWrap = new HBox(8, postRatioBar, postRatioText);
        ratioWrap.setAlignment(Pos.CENTER_LEFT);

        FlowPane cluster = new FlowPane();
        cluster.setHgap(8);
        cluster.setVgap(8);
        cluster.getChildren().addAll(likeWrap, dislikeWrap, ratioWrap, postEmojiButton, postBookmarkButton);
        cluster.setAlignment(Pos.CENTER_LEFT);
        return cluster;
    }

    private void buildCreateFace() {
        createFace.getChildren().clear();

        VBox card = new VBox(12);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 16px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 16px;");

        Text heading = new Text("Create Publication");
        heading.setFill(Color.WHITE);
        heading.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 24));

        createTitle.setPromptText("Title");
        createCategory.getItems().setAll(CATEGORIES);
        createCategory.setValue("Discussion General");
        createDescription.setPromptText("Description");
        createDescription.setPrefRowCount(8);
        createImageField.setPromptText("Optional image");
        createImageField.setEditable(false);

        tuneInput(createTitle);
        tuneInput(createDescription);
        tuneInput(createCategory);
        styleValidationLabel(createTitleValidation, "Title must be at least 5 characters", false);
        styleValidationLabel(createCategoryValidation, "Category is required", false);
        styleValidationLabel(createDescriptionValidation, "Description must be at least 10 characters", false);

        createTitle.textProperty().addListener((obs, oldValue, newValue) -> updateCreateValidationState());
        createCategory.valueProperty().addListener((obs, oldValue, newValue) -> updateCreateValidationState());
        createDescription.textProperty().addListener((obs, oldValue, newValue) -> updateCreateValidationState());

        preparePreview(createImagePreview, 130, 130);

        Label createFileLabel = new Label("No image selected");
        createFileLabel.setTextFill(Color.web("rgba(255,255,255,0.65)"));
        createFileLabel.setStyle("-fx-font-size: 10px;");

        HBox imageRow = buildAttachmentZone(
            () -> {
                File file = chooseImage();
                if (file != null) {
                    createImage = file;
                    createImageField.setText(file.getName());
                    createFileLabel.setText(file.getName());
                    createImagePreview.setImage(new Image(file.toURI().toString(), true));
                }
            },
            () -> {
                createImage = null;
                createImageField.clear();
                createFileLabel.setText("No image selected");
                createImagePreview.setImage(null);
            },
            createFileLabel
        );

        HBox previewRow = new HBox(8, createImagePreview);

        HBox actions = new HBox(8,
            primaryButton("Publish", this::submitPublication),
            ghostButton("Back", () -> showFace(readFace))
        );

        card.getChildren().addAll(
            heading,
            createTitle,
            createTitleValidation,
            createCategory,
            createCategoryValidation,
            createDescription,
            createDescriptionValidation,
            imageRow,
            previewRow,
            actions
        );

        updateCreateValidationState();

        ScrollPane scroll = new ScrollPane(card);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        createFace.getChildren().add(scroll);
    }

    private void buildEditFace() {
        editFace.getChildren().clear();

        VBox card = new VBox(12);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 16px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 16px;");

        Text heading = new Text("Edit Publication");
        heading.setFill(Color.WHITE);
        heading.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 24));

        editTitle.setPromptText("Title");
        editCategory.getItems().setAll(CATEGORIES);
        editDescription.setPromptText("Description");
        editDescription.setPrefRowCount(8);
        editImageField.setPromptText("Optional new image");
        editImageField.setEditable(false);

        tuneInput(editTitle);
        tuneInput(editDescription);
        tuneInput(editCategory);
        styleValidationLabel(editTitleValidation, "Title must be at least 5 characters", false);
        styleValidationLabel(editCategoryValidation, "Category is required", false);
        styleValidationLabel(editDescriptionValidation, "Description must be at least 10 characters", false);

        editTitle.textProperty().addListener((obs, oldValue, newValue) -> updateEditValidationState());
        editCategory.valueProperty().addListener((obs, oldValue, newValue) -> updateEditValidationState());
        editDescription.textProperty().addListener((obs, oldValue, newValue) -> updateEditValidationState());

        preparePreview(editCurrentImagePreview, 120, 120);
        preparePreview(editNewImagePreview, 120, 120);

        Label editFileLabel = new Label("No new image selected");
        editFileLabel.setTextFill(Color.web("rgba(255,255,255,0.65)"));
        editFileLabel.setStyle("-fx-font-size: 10px;");

        HBox imageRow = buildAttachmentZone(
            () -> {
                File file = chooseImage();
                if (file != null) {
                    editImage = file;
                    editImageField.setText(file.getName());
                    editFileLabel.setText(file.getName());
                    editNewImagePreview.setImage(new Image(file.toURI().toString(), true));
                }
            },
            () -> {
                editImage = null;
                editImageField.clear();
                editFileLabel.setText("No new image selected");
                editNewImagePreview.setImage(null);
            },
            editFileLabel
        );

        VBox currentWrap = new VBox(4, dimLabel("Current image"), editCurrentImagePreview);
        VBox newWrap = new VBox(4, dimLabel("New image"), editNewImagePreview);
        HBox previews = new HBox(18, currentWrap, newWrap);

        HBox actions = new HBox(8,
            primaryButton("Save", this::submitEditPublication),
            ghostButton("Back", () -> showFace(readFace))
        );

        card.getChildren().addAll(
            heading,
            editTitle,
            editTitleValidation,
            editCategory,
            editCategoryValidation,
            editDescription,
            editDescriptionValidation,
            imageRow,
            previews,
            actions
        );

        updateEditValidationState();

        ScrollPane scroll = new ScrollPane(card);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        editFace.getChildren().add(scroll);
    }

    private VBox buildCommentsSection() {
        VBox wrap = new VBox(10);
        wrap.setPadding(new Insets(18));
        wrap.setFillWidth(true);
        wrap.setMaxWidth(Double.MAX_VALUE);
        wrap.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 16px; -fx-background-radius: 16px;");

        Text heading = new Text("Discussion");
        heading.setFill(Color.WHITE);
        heading.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 18));

        commentInput.setPromptText("Share your thoughts...");
        commentInput.setPrefRowCount(3);
        commentInput.setMaxWidth(Double.MAX_VALUE);
        tuneInput(commentInput);
        commentValidation.setStyle("-fx-font-size: 11px; -fx-font-weight: 600;");
        commentInput.textProperty().addListener((obs, oldValue, newValue) -> updateCommentValidationState());
        updateCommentValidationState();

        // Public/Private toggle island
        VBox toggleIsland = new VBox(6);
        toggleIsland.setPadding(new Insets(12));
        toggleIsland.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.08) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.25) + ";" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;"
        );

        Text toggleLabel = new Text("Comment Visibility");
        toggleLabel.setFill(Color.WHITE);
        toggleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700;");

        Text toggleDesc = new Text("Choose whether others can see your comment");
        toggleDesc.setFill(Color.web("rgba(255,255,255,0.60)"));
        toggleDesc.setStyle("-fx-font-size: 10px;");

        HBox toggleSwitch = new HBox(8);
        toggleSwitch.setAlignment(Pos.CENTER_LEFT);
        toggleSwitch.setPrefHeight(36);

        Button publicBtn = new Button("Public");
        Button privateBtn = new Button("Private");
        publicBtn.setPrefWidth(70);
        privateBtn.setPrefWidth(70);
        
        publicBtn.setStyle(
            "-fx-background-color: " + tm.getAccentHex() + ";" +
            "-fx-text-fill: white; -fx-font-weight: 700;" +
            "-fx-background-radius: 10px; -fx-padding: 6 12 6 12;"
        );
        privateBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-border-color: rgba(255,255,255,0.15);" +
            "-fx-border-radius: 10px; -fx-background-radius: 10px;" +
            "-fx-text-fill: rgba(255,255,255,0.70); -fx-padding: 6 12 6 12;"
        );

        publicBtn.setOnAction(e -> {
            isCommentPublic = true;
            publicBtn.setStyle(
                "-fx-background-color: " + tm.getAccentHex() + ";" +
                "-fx-text-fill: white; -fx-font-weight: 700;" +
                "-fx-background-radius: 10px; -fx-padding: 6 12 6 12;"
            );
            privateBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                "-fx-border-color: rgba(255,255,255,0.15);" +
                "-fx-border-radius: 10px; -fx-background-radius: 10px;" +
                "-fx-text-fill: rgba(255,255,255,0.70); -fx-padding: 6 12 6 12;"
            );
        });

        privateBtn.setOnAction(e -> {
            isCommentPublic = false;
            privateBtn.setStyle(
                "-fx-background-color: " + tm.getAccentHex() + ";" +
                "-fx-text-fill: white; -fx-font-weight: 700;" +
                "-fx-background-radius: 10px; -fx-padding: 6 12 6 12;"
            );
            publicBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                "-fx-border-color: rgba(255,255,255,0.15);" +
                "-fx-border-radius: 10px; -fx-background-radius: 10px;" +
                "-fx-text-fill: rgba(255,255,255,0.70); -fx-padding: 6 12 6 12;"
            );
        });

        toggleSwitch.getChildren().addAll(publicBtn, privateBtn);
        toggleIsland.getChildren().addAll(toggleLabel, toggleDesc, toggleSwitch);

        // Image attachments with preview
        Label commentFileLabel = new Label("No images selected");
        commentFileLabel.setTextFill(Color.web("rgba(255,255,255,0.65)"));
        commentFileLabel.setStyle("-fx-font-size: 10px;");

        commentImagePreviews.setHgap(8);
        commentImagePreviews.setVgap(8);
        commentImagePreviews.setPadding(new Insets(8, 0, 0, 0));
        commentImagePreviews.setStyle("-fx-background-color: transparent;");

        HBox imageRow = buildMultiImageAttachmentZone(
            () -> {
                File file = chooseImage();
                if (file != null && !commentImages.contains(file)) {
                    commentImages.add(file);
                    commentFileLabel.setText(commentImages.size() + " image" + (commentImages.size() > 1 ? "s" : "") + " selected");
                    updateImagePreview(commentImages, commentImagePreviews);
                }
            },
            commentFileLabel,
            commentImagePreviews,
            commentImages
        );
        imageRow.setAlignment(Pos.CENTER_LEFT);
        imageRow.setMaxWidth(Double.MAX_VALUE);

        Button send = primaryButton("Post Comment", this::submitComment);

        commentsBox.getChildren().setAll(emptyLabel("No comments yet."));
        commentsBox.setFillWidth(true);
        commentsBox.setMaxWidth(Double.MAX_VALUE);

        wrap.getChildren().addAll(heading, commentsBox, commentInput, commentValidation, imageRow, commentImagePreviews, toggleIsland, send);
        return wrap;
    }

    private VBox buildSidePanel() {
        VBox side = new VBox(10);
        side.setPadding(new Insets(14));
        side.setStyle("-fx-background-color: rgba(0,0,0,0.2);");
        side.setPrefWidth(360);
        side.setMinWidth(300);
        side.setMaxWidth(360);

        Text title = new Text("Discussions");
        title.setFill(Color.WHITE);
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 20));

        filterGeneral.setOnAction(e -> loadCategory("General"));
        filterAnnouncements.setOnAction(e -> loadCategory("Announcement"));

        filterGeneral.setMaxWidth(Double.MAX_VALUE);
        filterAnnouncements.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(filterGeneral, Priority.ALWAYS);
        HBox.setHgrow(filterAnnouncements, Priority.ALWAYS);
        HBox filters = new HBox(8, filterGeneral, filterAnnouncements);

        Button newPost = primaryButton("New Post", () -> openCreateFace(false));
        newPost.setMaxWidth(Double.MAX_VALUE);

        Button newAnnouncement = ghostButton("Announcement", () -> openCreateFace(true));
        newAnnouncement.setMaxWidth(Double.MAX_VALUE);
        boolean moderator = isModerator(session.getCurrentUser());
        newAnnouncement.setVisible(moderator);
        newAnnouncement.setManaged(moderator);

        HBox createButtons = new HBox(8, newPost, newAnnouncement);
        HBox.setHgrow(newPost, Priority.ALWAYS);
        HBox.setHgrow(newAnnouncement, Priority.ALWAYS);

        listBox.setMaxWidth(Double.MAX_VALUE);
        listBox.setPrefWidth(330);
        listBox.setFillWidth(true);

        ScrollPane listScroll = new ScrollPane(listBox);
        listScroll.setFitToWidth(true);
        listScroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        listScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(listScroll, Priority.ALWAYS);

        side.getChildren().addAll(title, createButtons, filters, listScroll);
        return side;
    }

    private void loadCategory(String category) {
        currentFilter = category;
        updateFilterButtons();

        List<Publication> items = "Announcement".equals(category)
            ? publications.publicationsByCategory("Announcement")
            : publications.publicationsByCategory("General");
        items = new ArrayList<>(items);
        items.sort((a, b) -> compareDates(b.getDateCreationPub(), a.getDateCreationPub()));

        listBox.getChildren().clear();
        listItemsById.clear();

        if (items.isEmpty()) {
            listBox.getChildren().add(emptyLabel("No publications."));
            clearCurrent();
            return;
        }

        for (Publication pub : items) {
            listBox.getChildren().add(publicationItem(pub));
        }

        Publication target = current;
        boolean found = false;
        if (target != null) {
            for (Publication item : items) {
                if (Objects.equals(item.getIdPublication(), target.getIdPublication())) {
                    found = true;
                    break;
                }
            }
        }

        if (target == null || !found) {
            target = items.get(0);
        }
        selectPublication(target);
    }

    private Node publicationItem(Publication pub) {
        VBox item = new VBox(7);
        item.setPadding(new Insets(12));
        item.setStyle(inactiveListItemStyle());
        item.setMinWidth(0);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setPrefWidth(Double.MAX_VALUE);

        HBox top = new HBox(8);
        Label pill = new Label(pub.getCategoriePub() == null ? "General" : pub.getCategoriePub());
        pill.setStyle(categoryStyle(pub.getCategoriePub()));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label dateMini = dimLabel(shortDate(pub.getDateCreationPub()));
        top.getChildren().addAll(pill, spacer, dateMini);

        Text title = new Text(trim(pub.getTitrePub(), 40));
        title.setFill(Color.WHITE);
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 13));
        title.wrappingWidthProperty().bind(item.widthProperty().subtract(26));

        HBox authorRow = new HBox(8);
        Label avatar = new Label(initials(pub.getUser()));
        avatar.setMinSize(32, 32);
        avatar.setAlignment(Pos.CENTER);
        avatar.setStyle(
            "-fx-background-color: rgba(255,255,255,0.10);" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(255,255,255,0.20);" +
            "-fx-border-radius: 16px;" +
            "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 700;"
        );

        Text meta = new Text(author(pub.getUser()));
        meta.setFill(Color.web("rgba(255,255,255,0.65)"));
        meta.wrappingWidthProperty().bind(item.widthProperty().subtract(86));
        authorRow.getChildren().addAll(avatar, meta);

        item.getChildren().addAll(top, title, authorRow);
        item.setOnMouseClicked(e -> {
            selectPublication(pub);
            showFace(readFace);
        });

        if (pub.getIdPublication() != null) {
            listItemsById.put(pub.getIdPublication(), item);
        }
        return item;
    }

    private void selectPublication(Publication publication) {
        current = publication;
        if (publication == null) {
            clearCurrent();
            return;
        }

        heroTitle.setText(publication.getTitrePub() == null ? "Untitled" : publication.getTitrePub());
        heroBody.setText(publication.getDescriptionPub() == null ? "" : publication.getDescriptionPub());
        heroCategory.setText(publication.getCategoriePub() == null ? "Discussion General" : publication.getCategoriePub());
        heroMeta.setText(author(publication.getUser()) + " • " + format(publication.getDateCreationPub()));

        Image image = resolveImage(publication.getImagePub(), "forum_images");
        setHeroImage(image);

        updateOwnerActionsVisibility();
        updateActiveListItem();
        refreshPublicationReactionUI();
        renderComments(publication);
    }

    private void renderComments(Publication publication) {
        List<Commentaire> list = comments.commentairesByPublication(publication);
        commentsBox.getChildren().clear();

        if (list.isEmpty()) {
            commentsBox.getChildren().add(emptyLabel("No comments yet."));
            return;
        }

        for (Commentaire comment : list) {
            commentsBox.getChildren().add(commentCard(comment));
        }
    }

    private Node commentCard(Commentaire comment) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 12px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 12px;");

        HBox authorRow = new HBox(8);
        authorRow.setAlignment(Pos.CENTER_LEFT);

        StackPane avatarNode = commentAvatarNode(comment);
        Text authorText = new Text((comment.isVisibility() ? author(comment.getUser()) : "Anonymous") + " • " + format(comment.getCreatedAt()));
        authorText.setFill(Color.web("rgba(255,255,255,0.70)"));
        authorRow.getChildren().addAll(avatarNode, authorText);

        Text body = new Text(safe(comment.getDescriptionCommentaire()));
        body.setFill(Color.WHITE);
        body.wrappingWidthProperty().bind(card.widthProperty().subtract(26));

        card.getChildren().addAll(authorRow, body);

        Image img = resolveImage(comment.getImageCommentaire(), "commentaire_images");
        if (img != null) {
            ImageView preview = new ImageView(img);
            preview.setPreserveRatio(true);
            preview.setFitWidth(340);
            preview.setFitHeight(220);
            preview.setSmooth(true);
            preview.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 10px;");
            card.getChildren().add(preview);
        }

        card.getChildren().add(buildCommentActionBar(comment));
        
        VBox editPanelContainer = buildCommentEditPanel(comment);
        card.getChildren().add(editPanelContainer);
        
        VBox reportPanelContainer = buildCommentReportPanel(comment);
        card.getChildren().add(reportPanelContainer);
        
        VBox emojiPanelContainer = buildCommentEmojiPanel(comment);
        card.getChildren().add(emojiPanelContainer);
        
        return card;
    }

    private StackPane commentAvatarNode(Commentaire comment) {
        boolean visibleIdentity = comment != null && comment.isVisibility();
        User commentUser = comment == null ? null : comment.getUser();

        Circle avatarCircle = new Circle(16);
        avatarCircle.setStroke(Color.color(1, 1, 1, 0.2));
        avatarCircle.setStrokeWidth(1);

        Label initialsLabel = new Label(visibleIdentity ? initials(commentUser) : "A");
        initialsLabel.setTextFill(Color.WHITE);
        initialsLabel.setStyle("-fx-font-size: 11; -fx-font-weight: 700;");

        boolean hasImage = visibleIdentity && applyUserAvatarFill(commentUser, avatarCircle);
        if (!hasImage) {
            avatarCircle.setFill(Color.color(1, 1, 1, 0.10));
        }

        initialsLabel.setVisible(!hasImage);
        initialsLabel.setManaged(!hasImage);

        StackPane avatarWrap = new StackPane(avatarCircle, initialsLabel);
        avatarWrap.setMinSize(32, 32);
        avatarWrap.setPrefSize(32, 32);
        return avatarWrap;
    }

    private boolean applyUserAvatarFill(User user, Circle avatarCircle) {
        if (user == null || user.getIdUser() == null) {
            return false;
        }

        Profile profile = profileForUser(user.getIdUser());
        String avatarPath = profile == null ? null : profile.getAvatar();
        if (avatarPath == null || avatarPath.isBlank()) {
            return false;
        }

        Image img = ImageLoaderUtil.loadProfileAvatar(avatarPath, false);
        if (img != null && !img.isError()) {
            avatarCircle.setFill(new ImagePattern(img));
            return true;
        }

        return false;
    }

    private Profile profileForUser(Integer userId) {
        if (userId == null) {
            return null;
        }

        if (profileByUserIdCache.containsKey(userId)) {
            return profileByUserIdCache.get(userId);
        }

        Profile profile = profiles.profileByUserId(userId).orElse(null);
        profileByUserIdCache.put(userId, profile);
        return profile;
    }

    private Node buildCommentActionBar(Commentaire comment) {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 0, 0, 0));

        Button like = new Button("Like");
        Button dislike = new Button("Dislike");
        Button emoji = new Button("Emoji");
        Button report = new Button("Report");

        styleReactionButton(like);
        styleReactionButton(dislike);
        styleReactionButton(emoji);
        styleReactionButton(report);

        Label likeCount = new Label("");
        likeCount.setTextFill(Color.web("rgba(255,255,255,0.75)"));
        likeCount.setStyle("-fx-font-size: 11px;");

        Label dislikeCount = new Label("");
        dislikeCount.setTextFill(Color.web("rgba(255,255,255,0.75)"));
        dislikeCount.setStyle("-fx-font-size: 11px;");

        HBox likeWrap = reactionCountWrap(like, likeCount);
        HBox dislikeWrap = reactionCountWrap(dislike, dislikeCount);

        ProgressBar ratioBar = new ProgressBar(0);
        ratioBar.setPrefWidth(70);
        ratioBar.setPrefHeight(6);
        ratioBar.setStyle("-fx-accent: #4ade80; -fx-control-inner-background: #f87171;");
        Label ratioText = new Label("0%");
        ratioText.setTextFill(Color.web("rgba(255,255,255,0.72)"));
        ratioText.setStyle("-fx-font-size: 10px; -fx-font-weight: 700;");

        HBox ratioWrap = new HBox(6, ratioBar, ratioText);
        ratioWrap.setAlignment(Pos.CENTER_LEFT);

        User currentUser = session.getCurrentUser();
        applyCommentReactionUI(reactions.commentStatus(comment, currentUser), like, dislike, emoji, likeCount, dislikeCount, ratioBar, ratioText);

        like.setOnAction(e -> {
            ReactionActionResult result = reactions.commentToggle(comment, session.getCurrentUser(), "Like");
            if (!result.isSuccess()) {
                showInfo("Reaction", result.getMessage());
                return;
            }
            applyCommentReactionUI(result.getStatus(), like, dislike, emoji, likeCount, dislikeCount, ratioBar, ratioText);
        });

        dislike.setOnAction(e -> {
            ReactionActionResult result = reactions.commentToggle(comment, session.getCurrentUser(), "Dislike");
            if (!result.isSuccess()) {
                showInfo("Reaction", result.getMessage());
                return;
            }
            applyCommentReactionUI(result.getStatus(), like, dislike, emoji, likeCount, dislikeCount, ratioBar, ratioText);
        });

        emoji.setOnAction(e -> toggleCommentEmojiPanel(comment));
        report.setOnAction(e -> toggleCommentReportPanel(comment));

        bar.getChildren().addAll(likeWrap, dislikeWrap, ratioWrap, emoji, report);

        if (canManageComment(comment)) {
            Button edit = ghostButton("Edit", () -> toggleCommentEditPanel(comment));
            Button delete = ghostButton("Delete", () -> deleteComment(comment));
            bar.getChildren().addAll(edit, delete);
        }

        return bar;
    }

    private HBox reactionCountWrap(Button button, Label count) {
        HBox wrap = new HBox(4, button, count);
        wrap.setAlignment(Pos.CENTER_LEFT);
        return wrap;
    }

    private void applyCommentReactionUI(
        ReactionStatus status,
        Button like,
        Button dislike,
        Button emoji,
        Label likeCount,
        Label dislikeCount,
        ProgressBar ratioBar,
        Label ratioText
    ) {
        styleReactionButton(like);
        styleReactionButton(dislike);
        styleReactionButton(emoji);

        boolean hasLike = hasKind(status, "Like");
        boolean hasDislike = hasKind(status, "Dislike");
        String emojiValue = emojiFromStatus(status);
        boolean hasEmoji = emojiValue != null;

        if (hasLike) {
            styleReactionButtonActive(like, "#4ade80");
        } else if (hasDislike) {
            styleReactionButtonActive(dislike, "#f87171");
        } else if (hasEmoji) {
            styleReactionButtonActive(emoji, "#fbbf24");
        }

        int likes = count(status, "Like");
        int dislikes = count(status, "Dislike");
        int total = likes + dislikes;

        likeCount.setText(likes > 0 ? String.valueOf(likes) : "");
        dislikeCount.setText(dislikes > 0 ? String.valueOf(dislikes) : "");

        double ratio = total > 0 ? (double) likes / total : 0;
        ratioBar.setProgress(ratio);
        ratioText.setText(Math.round(ratio * 100) + "%");

        if (hasEmoji) {
            emoji.setText(emojiValue);
        } else {
            emoji.setText("Emoji");
        }
    }

    private void openCreateFace(boolean announcement) {
        forceAnnouncementCreate = announcement;
        createTitle.clear();
        createDescription.clear();
        createCategory.setValue(announcement ? "Announcement" : "Discussion General");
        createCategory.setDisable(announcement);
        createImageField.clear();
        createImagePreview.setImage(null);
        createImage = null;
        updateCreateValidationState();
        showFace(createFace);
    }

    private void openEditFace() {
        if (current == null) {
            showInfo("Edit", "Select a publication first.");
            return;
        }
        if (!canEditCurrent()) {
            showInfo("Permission", "You can edit only your own publication (or admin role).");
            return;
        }

        editTitle.setText(current.getTitrePub());
        editDescription.setText(current.getDescriptionPub());
        editCategory.setValue(current.getCategoriePub());
        editImageField.clear();
        editCurrentImagePreview.setImage(resolveImage(current.getImagePub(), "forum_images"));
        editNewImagePreview.setImage(null);
        editImage = null;
        updateEditValidationState();
        showFace(editFace);
    }

    private void submitPublication() {
        User user = session.getCurrentUser();
        if (user == null) {
            showInfo("Error", "You must be logged in to publish.");
            return;
        }

        String title = safe(createTitle.getText());
        String description = safe(createDescription.getText());
        String category = forceAnnouncementCreate ? "Announcement" : createCategory.getValue();

        List<String> createErrors = buildPublicationDraft(title, description, category).validateForCreate();
        if (!createErrors.isEmpty()) {
            showInfo("Validation", createErrors.getFirst());
            return;
        }

        String image = null;
        try {
            if (createImage != null) {
                image = copyUpload(createImage, "forum_images");
            }
        } catch (IOException ex) {
            showInfo("Upload Error", ex.getMessage());
            return;
        }

        Integer id = publications.publicationCreate(title, description, category, image, user);
        if (id != null && id > 0) {
            loadCategory("Announcement".equals(category) ? "Announcement" : "General");
            publications.publicationById(id).ifPresent(this::selectPublication);
            forceAnnouncementCreate = false;
            showFace(readFace);
        } else {
            showInfo("Error", "Unable to create publication.");
        }
    }

    private void submitEditPublication() {
        if (current == null) {
            return;
        }

        String title = safe(editTitle.getText());
        String description = safe(editDescription.getText());
        String category = editCategory.getValue();

        List<String> editErrors = buildPublicationDraft(title, description, category).validateForCreate();
        if (!editErrors.isEmpty()) {
            showInfo("Validation", editErrors.getFirst());
            return;
        }

        String image = current.getImagePub();
        try {
            if (editImage != null) {
                image = copyUpload(editImage, "forum_images");
            }
        } catch (IOException ex) {
            showInfo("Upload Error", ex.getMessage());
            return;
        }

        boolean ok = publications.publicationUpdate(current.getIdPublication(), title, description, category, image);
        if (ok) {
            loadCategory(currentFilter);
            publications.publicationById(current.getIdPublication()).ifPresent(this::selectPublication);
            showFace(readFace);
        } else {
            showInfo("Error", "Unable to update publication.");
        }
    }

    private void deleteCurrentPublication() {
        if (current == null) {
            return;
        }
        if (!canEditCurrent()) {
            showInfo("Permission", "You can delete only your own publication (or admin role).");
            return;
        }

        boolean confirmed = confirmFancy("Delete Publication", "Delete this publication permanently?");
        if (!confirmed) {
            return;
        }

        boolean ok = publications.publicationDelete(current.getIdPublication());
        if (ok) {
            loadCategory(currentFilter);
            showFace(readFace);
        } else {
            showInfo("Error", "Unable to delete publication.");
        }
    }

    private void submitComment() {
        if (current == null) {
            return;
        }

        User user = session.getCurrentUser();
        if (user == null) {
            showInfo("Error", "You must be logged in to comment.");
            return;
        }

        String description = safe(commentInput.getText());
        String commentError = commentDescriptionError(description);
        if (commentError != null) {
            showInfo("Validation", commentError);
            return;
        }

        String image = null;
        try {
            if (!commentImages.isEmpty()) {
                // Upload first image as primary
                image = copyUpload(commentImages.get(0), "commentaire_images");
            }
        } catch (IOException ex) {
            showInfo("Upload Error", ex.getMessage());
            return;
        }

        Integer id = comments.commentaireCreate(description, image, isCommentPublic, current, user);
        if (id != null && id > 0) {
            commentInput.clear();
            updateCommentValidationState();
            commentImageField.clear();
            commentImages.clear();
            commentImagePreviews.getChildren().clear();
            isCommentPublic = true;
            renderComments(current);
        } else {
            showInfo("Error", "Unable to post comment.");
        }
    }

    private void editComment(Commentaire comment) {
        if (comment.getIdCommentaire() == null) {
            return;
        }

        Dialog<ButtonType> dialog = styledDialog("Edit Comment");
        TextArea area = new TextArea(safe(comment.getDescriptionCommentaire()));
        area.setPrefRowCount(4);
        tuneInput(area);

        VBox content = new VBox(10, dimLabel("Update your comment"), area);
        content.setPadding(new Insets(4, 0, 0, 0));
        dialog.getDialogPane().setContent(content);

        ButtonType save = new ButtonType("Save", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result != save) {
                return;
            }

            String text = safe(area.getText());
            String updateError = commentDescriptionError(text);
            if (updateError != null) {
                showInfo("Validation", updateError);
                return;
            }

            boolean ok = comments.commentaireUpdate(comment.getIdCommentaire(), text, comment.getImageCommentaire(), comment.isVisibility());
            if (ok) {
                renderComments(current);
            } else {
                showInfo("Error", "Unable to update comment.");
            }
        });
    }

    private void deleteComment(Commentaire comment) {
        if (comment.getIdCommentaire() == null) {
            return;
        }
        if (!canManageComment(comment)) {
            showInfo("Permission", "You can delete only your own comment (or admin role).");
            return;
        }

        boolean confirmed = confirmFancy("Delete Comment", "Delete this comment permanently?");
        if (!confirmed) {
            return;
        }

        boolean ok = comments.commentaireDelete(comment.getIdCommentaire());
        if (ok) {
            renderComments(current);
        } else {
            showInfo("Error", "Unable to delete comment.");
        }
    }

    private void togglePublicationReportPanel() {
        if (publicationReportPanel.isManaged()) {
            publicationReportPanel.setVisible(false);
            publicationReportPanel.setManaged(false);
        } else {
            publicationReportPanel.setVisible(true);
            publicationReportPanel.setManaged(true);
        }
    }

    private VBox buildPublicationReportPanel() {
        publicationReportPanel.getChildren().clear();
        publicationReportPanel.setVisible(false);
        publicationReportPanel.setManaged(false);
        publicationReportPanel.setMinWidth(0);
        publicationReportPanel.setMaxWidth(Double.MAX_VALUE);
        publicationReportPanel.setPadding(new Insets(10));
        publicationReportPanel.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 10px;");

        TextArea reasonInput = new TextArea();
        reasonInput.setPromptText("Why are you reporting this publication?");
        reasonInput.setPrefRowCount(3);
        reasonInput.setMaxWidth(Double.MAX_VALUE);
        reasonInput.setWrapText(true);
        tuneInput(reasonInput);

        Button submitBtn = primaryButton("Send Report", () -> {
            String reason = safe(reasonInput.getText());
            if (reason.isBlank()) {
                showInfo("Report", "Please provide a reason.");
                return;
            }

            if (current != null && current.getIdPublication() != null) {
                ReactionActionResult result = reactions.publicationReport(current, session.getCurrentUser(), reason);
                if (result.isSuccess()) {
                    showInfo("Report Sent", result.getMessage());
                    reasonInput.clear();
                    togglePublicationReportPanel();
                } else {
                    showInfo("Report", result.getMessage());
                }
            }
        });

        Button closeBtn = ghostButton("Cancel", this::togglePublicationReportPanel);

        HBox actions = new HBox(8, submitBtn, closeBtn);
        actions.setMaxWidth(Double.MAX_VALUE);
        publicationReportPanel.getChildren().addAll(reasonInput, actions);
        return publicationReportPanel;
    }

    private void toggleCommentEditPanel(Commentaire comment) {
        VBox panel = commentEditPanels.get(comment.getIdCommentaire());
        if (panel != null) {
            if (Objects.equals(currentCommentForInline, comment) && panel.isManaged()) {
                panel.setVisible(false);
                panel.setManaged(false);
                currentCommentForInline = null;
            } else {
                currentCommentForInline = comment;
                panel.setVisible(true);
                panel.setManaged(true);
            }
        }
    }

    private void toggleCommentReportPanel(Commentaire comment) {
        VBox panel = commentReportPanels.get(comment.getIdCommentaire());
        if (panel != null) {
            if (Objects.equals(currentCommentForInline, comment) && panel.isManaged()) {
                panel.setVisible(false);
                panel.setManaged(false);
                currentCommentForInline = null;
            } else {
                currentCommentForInline = comment;
                panel.setVisible(true);
                panel.setManaged(true);
            }
        }
    }

    private void toggleCommentEmojiPanel(Commentaire comment) {
        VBox panel = commentEmojiPanels.get(comment.getIdCommentaire());
        if (panel != null) {
            if (Objects.equals(currentCommentForInline, comment) && panel.isManaged()) {
                panel.setVisible(false);
                panel.setManaged(false);
                currentCommentForInline = null;
            } else {
                currentCommentForInline = comment;
                panel.setVisible(true);
                panel.setManaged(true);
            }
        }
    }

    private VBox buildCommentReportPanel(Commentaire comment) {
        VBox panel = new VBox(8);
        commentReportPanels.put(comment.getIdCommentaire(), panel);
        
        panel.setMinWidth(0);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 10px;");
        panel.setVisible(false);
        panel.setManaged(false);

        TextArea reasonInput = new TextArea();
        reasonInput.setPromptText("Why are you reporting this comment?");
        reasonInput.setPrefRowCount(3);
        reasonInput.setMaxWidth(Double.MAX_VALUE);
        reasonInput.setWrapText(true);
        tuneInput(reasonInput);

        Button submitBtn = primaryButton("Send Report", () -> {
            String reason = safe(reasonInput.getText());
            if (reason.isBlank()) {
                showInfo("Report", "Please provide a reason.");
                return;
            }

            ReactionActionResult result = reactions.commentReport(comment, session.getCurrentUser(), reason);
            if (result.isSuccess()) {
                showInfo("Report Sent", result.getMessage());
                reasonInput.clear();
                toggleCommentReportPanel(comment);
            } else {
                showInfo("Report", result.getMessage());
            }
        });

        Button closeBtn = ghostButton("Cancel", () -> toggleCommentReportPanel(comment));

        HBox actions = new HBox(8, submitBtn, closeBtn);
        actions.setMaxWidth(Double.MAX_VALUE);
        panel.getChildren().addAll(reasonInput, actions);
        return panel;
    }

    private void togglePublicationReaction(String kind) {
        if (current == null || current.getIdPublication() == null) {
            return;
        }

        ReactionActionResult result = reactions.publicationToggle(current, session.getCurrentUser(), kind);
        if (!result.isSuccess()) {
            showInfo("Reaction", result.getMessage());
            return;
        }

        applyPublicationReactionUI(result.getStatus());
    }

    private void togglePublicationEmojiPanel() {
        if (publicationEmojiPanel.isManaged()) {
            publicationEmojiPanel.setVisible(false);
            publicationEmojiPanel.setManaged(false);
        } else {
            publicationEmojiPanel.setVisible(true);
            publicationEmojiPanel.setManaged(true);
        }
    }

    private VBox buildPublicationEmojiPanel() {
        publicationEmojiPanel.getChildren().clear();
        publicationEmojiPanel.setVisible(false);
        publicationEmojiPanel.setManaged(false);
        publicationEmojiPanel.setPadding(new Insets(8));
        publicationEmojiPanel.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 10px;");
        publicationEmojiPanel.setSpacing(6);

        HBox emojiRow = new HBox(6);
        emojiRow.setAlignment(Pos.CENTER_LEFT);

        for (String emoji : EMOJIS) {
            Label emojiLabel = new Label(emoji);
            emojiLabel.setFont(new Font(28));
            emojiLabel.setCursor(Cursor.HAND);
            emojiLabel.setPadding(new Insets(4));
            
            String emojiValue = emoji;
            emojiLabel.setOnMouseEntered(e -> emojiLabel.setOpacity(0.7));
            emojiLabel.setOnMouseExited(e -> emojiLabel.setOpacity(1.0));
            emojiLabel.setOnMouseClicked(e -> {
                e.consume();
                if (current != null && current.getIdPublication() != null) {
                    ReactionActionResult result = reactions.publicationEmoji(current, session.getCurrentUser(), emojiValue);
                    if (!result.isSuccess()) {
                        showInfo("Reaction", result.getMessage());
                        return;
                    }
                    applyPublicationReactionUI(result.getStatus());
                    togglePublicationEmojiPanel();
                }
            });
            emojiRow.getChildren().add(emojiLabel);
        }

        Button closeBtn = ghostButton("Close", this::togglePublicationEmojiPanel);
        publicationEmojiPanel.getChildren().addAll(emojiRow, closeBtn);
        return publicationEmojiPanel;
    }

    private VBox buildCommentEmojiPanel(Commentaire comment) {
        VBox panel = new VBox(8);
        commentEmojiPanels.put(comment.getIdCommentaire(), panel);
        
        panel.setVisible(false);
        panel.setManaged(false);
        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 10px;");
        panel.setSpacing(6);

        HBox emojiRow = new HBox(6);
        emojiRow.setAlignment(Pos.CENTER_LEFT);

        for (String emoji : EMOJIS) {
            Label emojiLabel = new Label(emoji);
            emojiLabel.setFont(new Font(28));
            emojiLabel.setCursor(Cursor.HAND);
            emojiLabel.setPadding(new Insets(4));
            
            String emojiValue = emoji;
            emojiLabel.setOnMouseEntered(e -> emojiLabel.setOpacity(0.7));
            emojiLabel.setOnMouseExited(e -> emojiLabel.setOpacity(1.0));
            emojiLabel.setOnMouseClicked(e -> {
                e.consume();
                ReactionActionResult result = reactions.commentEmoji(comment, session.getCurrentUser(), emojiValue);
                if (!result.isSuccess()) {
                    showInfo("Reaction", result.getMessage());
                    return;
                }
                toggleCommentEmojiPanel(comment);
            });
            emojiRow.getChildren().add(emojiLabel);
        }

        Button closeBtn = ghostButton("Close", () -> toggleCommentEmojiPanel(comment));
        panel.getChildren().addAll(emojiRow, closeBtn);
        return panel;
    }

    private VBox buildCommentEditPanel(Commentaire comment) {
        VBox panel = new VBox(8);
        commentEditPanels.put(comment.getIdCommentaire(), panel);
        
        panel.setMinWidth(0);
        panel.setMaxWidth(Double.MAX_VALUE);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 10px;");
        panel.setVisible(false);
        panel.setManaged(false);

        TextArea editInput = new TextArea(safe(comment.getDescriptionCommentaire()));
        editInput.setPromptText("Update your comment...");
        editInput.setPrefRowCount(3);
        editInput.setMaxWidth(Double.MAX_VALUE);
        editInput.setWrapText(true);
        tuneInput(editInput);

        Button saveBtn = primaryButton("Save", () -> {
            String newText = safe(editInput.getText());
            if (newText.isBlank()) {
                showInfo("Edit", "Comment cannot be empty.");
                return;
            }

            boolean success = comments.commentaireUpdate(comment.getIdCommentaire(), newText, comment.getImageCommentaire(), comment.isVisibility());
            if (success) {
                comment.setDescriptionCommentaire(newText);
                showInfo("Comment", "Updated successfully.");
                toggleCommentEditPanel(comment);
                if (current != null) {
                    renderComments(current);
                }
            } else {
                showInfo("Edit", "Unable to update comment.");
            }
        });

        Button closeBtn = ghostButton("Cancel", () -> toggleCommentEditPanel(comment));

        HBox actions = new HBox(8, saveBtn, closeBtn);
        actions.setMaxWidth(Double.MAX_VALUE);
        panel.getChildren().addAll(editInput, actions);
        return panel;
    }

    private void refreshPublicationReactionUI() {
        if (current == null || current.getIdPublication() == null) {
            return;
        }

        applyPublicationReactionUI(reactions.publicationStatus(current, session.getCurrentUser()));
    }

    private void applyPublicationReactionUI(ReactionStatus status) {
        if (status == null) {
            status = ReactionStatus.empty();
        }

        styleReactionButton(postLikeButton);
        styleReactionButton(postDislikeButton);
        styleReactionButton(postEmojiButton);
        styleReactionButton(postBookmarkButton);

        boolean hasLike = hasKind(status, "Like");
        boolean hasDislike = hasKind(status, "Dislike");
        String emojiValue = emojiFromStatus(status);
        boolean hasEmoji = emojiValue != null;

        if (hasLike) {
            styleReactionButtonActive(postLikeButton, "#4ade80");
        } else if (hasDislike) {
            styleReactionButtonActive(postDislikeButton, "#f87171");
        } else if (hasEmoji) {
            styleReactionButtonActive(postEmojiButton, "#fbbf24");
        }

        if (status.isBookmarked()) {
            styleReactionButtonActive(postBookmarkButton, "#93c5fd");
            postBookmarkButton.setText("Bookmarked");
        } else {
            postBookmarkButton.setText("Bookmark");
        }

        int likes = count(status, "Like");
        int dislikes = count(status, "Dislike");
        int total = likes + dislikes;

        postLikeCount.setText(likes > 0 ? String.valueOf(likes) : "");
        postDislikeCount.setText(dislikes > 0 ? String.valueOf(dislikes) : "");

        double ratio = total > 0 ? (double) likes / total : 0;
        postRatioBar.setProgress(ratio);
        postRatioText.setText(Math.round(ratio * 100) + "%");

        if (hasEmoji) {
            postEmojiButton.setText(emojiValue);
        } else {
            postEmojiButton.setText("Emoji");
        }
    }

    private void styleReactionButton(Button button) {
        button.setStyle(
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-border-color: rgba(255,255,255,0.16);" +
            "-fx-background-radius: 12px;" +
            "-fx-border-radius: 12px;" +
            "-fx-text-fill: rgba(255,255,255,0.92);" +
            "-fx-padding: 6 12 6 12;"
        );
    }

    private boolean hasKind(ReactionStatus status, String kind) {
        if (status == null || kind == null) {
            return false;
        }
        for (ReactionPayload payload : status.getReactions()) {
            if (kind.equals(payload.getKind())) {
                return true;
            }
        }
        return false;
    }

    private String emojiFromStatus(ReactionStatus status) {
        if (status == null) {
            return null;
        }
        for (ReactionPayload payload : status.getReactions()) {
            if ("Emoji".equals(payload.getKind()) && payload.getEmoji() != null && !payload.getEmoji().isBlank()) {
                return payload.getEmoji();
            }
        }
        return null;
    }

    private int count(ReactionStatus status, String kind) {
        if (status == null || status.getCounts() == null || kind == null) {
            return 0;
        }
        Integer value = status.getCounts().get(kind);
        return value == null ? 0 : value;
    }

    private void styleReactionButtonActive(Button button, String colorHex) {
        button.setStyle(
            "-fx-background-color: " + rgba(colorHex, 0.18) + ";" +
            "-fx-border-color: " + rgba(colorHex, 0.55) + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-radius: 12px;" +
            "-fx-text-fill: " + colorHex + ";" +
            "-fx-font-weight: 700;" +
            "-fx-padding: 6 12 6 12;"
        );
    }

    private String rgba(String hex, double alpha) {
        if (hex == null || !hex.startsWith("#") || (hex.length() != 7 && hex.length() != 4)) {
            return "rgba(255,255,255," + alpha + ")";
        }

        int r;
        int g;
        int b;
        if (hex.length() == 7) {
            r = Integer.parseInt(hex.substring(1, 3), 16);
            g = Integer.parseInt(hex.substring(3, 5), 16);
            b = Integer.parseInt(hex.substring(5, 7), 16);
        } else {
            r = Integer.parseInt(hex.substring(1, 2) + hex.substring(1, 2), 16);
            g = Integer.parseInt(hex.substring(2, 3) + hex.substring(2, 3), 16);
            b = Integer.parseInt(hex.substring(3, 4) + hex.substring(3, 4), 16);
        }

        return "rgba(" + r + "," + g + "," + b + "," + alpha + ")";
    }

    private void setHeroImage(Image image) {
        currentHeroImage = image;
        detailHeroImage.setImage(image);
        refreshHeroViewport();
    }

    private void refreshHeroViewport() {
        if (currentHeroImage == null) {
            detailHeroImage.setViewport(null);
            return;
        }

        double boxW = detailHero.getWidth();
        double boxH = detailHero.getHeight();
        double imgW = currentHeroImage.getWidth();
        double imgH = currentHeroImage.getHeight();

        if (boxW <= 1 || boxH <= 1 || imgW <= 1 || imgH <= 1) {
            return;
        }

        double boxRatio = boxW / boxH;
        double imgRatio = imgW / imgH;

        double vpX;
        double vpY;
        double vpW;
        double vpH;

        if (imgRatio > boxRatio) {
            vpH = imgH;
            vpW = imgH * boxRatio;
            vpX = (imgW - vpW) / 2.0;
            vpY = 0;
        } else {
            vpW = imgW;
            vpH = imgW / boxRatio;
            vpX = 0;
            vpY = (imgH - vpH) / 2.0;
        }

        detailHeroImage.setViewport(new Rectangle2D(vpX, vpY, vpW, vpH));
        detailHeroImage.setFitWidth(boxW);
        detailHeroImage.setFitHeight(boxH);
    }

    private void showFace(VBox face) {
        readFace.setVisible(false);
        readFace.setManaged(false);
        createFace.setVisible(false);
        createFace.setManaged(false);
        editFace.setVisible(false);
        editFace.setManaged(false);

        face.setVisible(true);
        face.setManaged(true);
    }

    private boolean canEditCurrent() {
        User currentUser = session.getCurrentUser();
        if (currentUser == null || current == null || current.getUser() == null) {
            return false;
        }

        if (Objects.equals(currentUser.getIdUser(), current.getUser().getIdUser())) {
            return true;
        }

        String role = currentUser.getRoleUser();
        return role != null && MODERATOR_ROLES.contains(role);
    }

    private boolean canDeleteCurrent() {
        User currentUser = session.getCurrentUser();
        if (currentUser == null || current == null || current.getUser() == null) {
            return false;
        }

        // Only allow delete if user is the owner AND has admin role
        if (Objects.equals(currentUser.getIdUser(), current.getUser().getIdUser())) {
            String role = currentUser.getRoleUser();
            return role != null && MODERATOR_ROLES.contains(role);
        }

        return false;
    }

    private boolean canManageComment(Commentaire comment) {
        User currentUser = session.getCurrentUser();
        if (currentUser == null || comment == null || comment.getUser() == null) {
            return false;
        }

        if (Objects.equals(currentUser.getIdUser(), comment.getUser().getIdUser())) {
            return true;
        }

        String role = currentUser.getRoleUser();
        return role != null && MODERATOR_ROLES.contains(role);
    }

    private void updateFilterButtons() {
        filterGeneral.setStyle("General".equals(currentFilter) ? activeFilterStyle() : inactiveFilterStyle());
        filterAnnouncements.setStyle("Announcement".equals(currentFilter) ? activeFilterStyle() : inactiveFilterStyle());
    }

    private String activeFilterStyle() {
        return "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.20) + "; -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.70) + "; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-text-fill: white; -fx-font-weight: 700;";
    }

    private String inactiveFilterStyle() {
        return "-fx-background-color: rgba(255,255,255,0.06); -fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-text-fill: white;";
    }

    private Button primaryButton(String label, Runnable action) {
        Button button = new Button(label);
        button.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.80) + ";" +
            "-fx-text-fill: white; -fx-font-weight: 700;" +
            "-fx-background-radius: 12px; -fx-padding: 8 14 8 14;"
        );
        button.setOnAction(e -> action.run());
        return button;
    }

    private HBox buildAttachmentZone(Runnable onBrowse, Runnable onRemove, Label fileLabel) {
        Label uploadIcon = new Label("📎");
        uploadIcon.setStyle("-fx-font-size: 18px;");
        
        VBox attachmentZone = new VBox(6);
        attachmentZone.setAlignment(Pos.CENTER);
        attachmentZone.setPrefHeight(70);
        attachmentZone.setCursor(Cursor.HAND);
        attachmentZone.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.06) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.40) + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-style: dashed;"
        );
        
        Text uploadText = new Text("Click to upload image");
        uploadText.setFill(Color.web("rgba(255,255,255,0.60)"));
        uploadText.setStyle("-fx-font-size: 11px;");
        
        attachmentZone.getChildren().addAll(uploadIcon, uploadText, fileLabel);
        attachmentZone.setOnMouseClicked(e -> onBrowse.run());
        attachmentZone.setOnMouseEntered(e -> attachmentZone.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.12) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.60) + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-style: dashed;"
        ));
        attachmentZone.setOnMouseExited(e -> attachmentZone.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.06) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.40) + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-style: dashed;"
        ));
        
        Button remove = ghostButton("Remove", onRemove);
        
        HBox row = new HBox(8, attachmentZone, remove);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(attachmentZone, Priority.ALWAYS);
        return row;
    }

    private HBox buildMultiImageAttachmentZone(Runnable onBrowse, Label fileLabel, FlowPane previewPane, List<File> images) {
        Label uploadIcon = new Label("📎");
        uploadIcon.setStyle("-fx-font-size: 18px;");
        
        VBox attachmentZone = new VBox(6);
        attachmentZone.setAlignment(Pos.CENTER);
        attachmentZone.setPrefHeight(70);
        attachmentZone.setCursor(Cursor.HAND);
        attachmentZone.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.06) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.40) + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-style: dashed;"
        );
        
        Text uploadText = new Text("Click to add images");
        uploadText.setFill(Color.web("rgba(255,255,255,0.60)"));
        uploadText.setStyle("-fx-font-size: 11px;");
        
        attachmentZone.getChildren().addAll(uploadIcon, uploadText, fileLabel);
        attachmentZone.setOnMouseClicked(e -> onBrowse.run());
        attachmentZone.setOnMouseEntered(e -> attachmentZone.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.12) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.60) + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-style: dashed;"
        ));
        attachmentZone.setOnMouseExited(e -> attachmentZone.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.06) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.40) + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-style: dashed;"
        ));
        
        HBox row = new HBox(8, attachmentZone);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(attachmentZone, Priority.ALWAYS);
        return row;
    }

    private void updateImagePreview(List<File> images, FlowPane previewPane) {
        previewPane.getChildren().clear();
        for (File file : images) {
            VBox imageCard = new VBox(4);
            imageCard.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03);" +
                "-fx-border-color: rgba(255,255,255,0.10);" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 4;"
            );
            
            ImageView img = new ImageView(new Image(file.toURI().toString(), true));
            img.setFitWidth(80);
            img.setFitHeight(80);
            img.setPreserveRatio(true);
            
            Button removeBtn = ghostButton("×", () -> {
                images.remove(file);
                updateImagePreview(images, previewPane);
            });
            removeBtn.setStyle(
                "-fx-background-color: rgba(200,0,0,0.70);" +
                "-fx-text-fill: white; -fx-font-weight: 700;" +
                "-fx-background-radius: 6px; -fx-padding: 2 6 2 6;" +
                "-fx-font-size: 16px;"
            );
            removeBtn.setMaxWidth(Double.MAX_VALUE);
            
            imageCard.getChildren().addAll(img, removeBtn);
            VBox.setVgrow(img, Priority.ALWAYS);
            previewPane.getChildren().add(imageCard);
        }
    }

    private Button ghostButton(String label, Runnable action) {
        Button button = new Button(label);
        button.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-border-color: rgba(255,255,255,0.15);" +
            "-fx-border-radius: 12px; -fx-background-radius: 12px;" +
            "-fx-text-fill: white; -fx-padding: 8 14 8 14;"
        );
        button.setOnAction(e -> action.run());
        return button;
    }

    private String copyUpload(File source, String folder) throws IOException {
        File dir = new File(System.getProperty("user.dir") + File.separator + "uploads" + File.separator + folder);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Unable to create upload folder");
        }

        String name = System.currentTimeMillis() + "_" + source.getName();
        File dest = new File(dir, name);
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return folder + "/" + name;
    }

    private File chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.webp"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Window window = root.getScene() == null ? null : root.getScene().getWindow();
        return chooser.showOpenDialog(window);
    }

    private void clearCurrent() {
        current = null;
        heroTitle.setText("Welcome to the Forum");
        heroMeta.setText("");
        heroBody.setText("Select a publication from the right panel.");
        heroCategory.setText("Discussion General");
        setHeroImage(null);

        ownerActions.setManaged(false);
        ownerActions.setVisible(false);

        postLikeCount.setText("");
        postDislikeCount.setText("");
        postRatioBar.setProgress(0);
        postRatioText.setText("0%");

        updateActiveListItem();
        commentsBox.getChildren().setAll(emptyLabel("No comments yet."));
    }

    private Node emptyLabel(String value) {
        Label label = new Label(value);
        label.setTextFill(Color.web("rgba(255,255,255,0.70)"));
        return label;
    }

    private String author(User user) {
        if (user == null) {
            return "Unknown";
        }

        String first = user.getFirstName() == null ? "" : user.getFirstName();
        String last = user.getLastName() == null ? "" : user.getLastName();
        String full = (first + " " + last).trim();

        if (!full.isBlank()) {
            return full;
        }
        return user.getEmailUser() == null ? "Unknown" : user.getEmailUser();
    }

    private String initials(User user) {
        if (user == null) {
            return "U";
        }

        String first = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String last = user.getLastName() == null ? "" : user.getLastName().trim();

        StringBuilder result = new StringBuilder();
        if (!first.isEmpty()) {
            result.append(Character.toUpperCase(first.charAt(0)));
        }
        if (!last.isEmpty()) {
            result.append(Character.toUpperCase(last.charAt(0)));
        }

        if (result.length() == 0) {
            return "U";
        }
        return result.toString();
    }

    private String trim(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 3) + "...";
    }

    private String format(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(DATE_FMT);
    }

    private String shortDate(LocalDateTime value) {
        if (value == null) {
            return "";
        }
        return value.format(SHORT_DATE_FMT);
    }

    private int compareDates(LocalDateTime left, LocalDateTime right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String inactiveListItemStyle() {
        return "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 16px; -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 16px;";
    }

    private String activeListItemStyle() {
        return "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.17) + "; -fx-background-radius: 16px; -fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.90) + "; -fx-border-radius: 16px;";
    }

    private void updateActiveListItem() {
        for (VBox item : listItemsById.values()) {
            item.setStyle(inactiveListItemStyle());
        }

        if (current != null && current.getIdPublication() != null) {
            VBox active = listItemsById.get(current.getIdPublication());
            if (active != null) {
                active.setStyle(activeListItemStyle());
            }
        }
    }

    private String categoryStyle(String category) {
        String base = "-fx-padding: 4 8 4 8; -fx-font-size: 10px; -fx-font-weight: 800; -fx-background-radius: 8px; -fx-border-radius: 8px;";
        if (category == null) {
            return base + "-fx-text-fill: white; -fx-background-color: rgba(255,255,255,0.1); -fx-border-color: rgba(255,255,255,0.2);";
        }

        switch (category) {
            case "Announcement":
                return base + "-fx-text-fill: #ff8080; -fx-background-color: rgba(255,50,50,0.2); -fx-border-color: rgba(255,50,50,0.4);";
            case "Suggestion":
                return base + "-fx-text-fill: #80ffaa; -fx-background-color: rgba(50,255,100,0.15); -fx-border-color: rgba(50,255,100,0.3);";
            case "Jeux Video":
                return base + "-fx-text-fill: #d080ff; -fx-background-color: rgba(150,50,255,0.2); -fx-border-color: rgba(150,50,255,0.4);";
            case "Informatique":
                return base + "-fx-text-fill: #80c0ff; -fx-background-color: rgba(50,150,255,0.2); -fx-border-color: rgba(50,150,255,0.4);";
            case "Nouveauté":
                return base + "-fx-text-fill: #ffd680; -fx-background-color: rgba(255,200,50,0.2); -fx-border-color: rgba(255,200,50,0.4);";
            case "Culture":
                return base + "-fx-text-fill: #d4a0ff; -fx-background-color: rgba(200,100,255,0.2); -fx-border-color: rgba(200,100,255,0.4);";
            case "Sport":
                return base + "-fx-text-fill: #7fe0c0; -fx-background-color: rgba(50,200,150,0.2); -fx-border-color: rgba(50,200,150,0.4);";
            default:
                return base + "-fx-text-fill: #9cc5ff; -fx-background-color: rgba(100,200,255,0.15); -fx-border-color: rgba(100,200,255,0.3);";
        }
    }

    private Label dimLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("rgba(255,255,255,0.62)"));
        return label;
    }

    private void preparePreview(ImageView preview, double width, double height) {
        preview.setFitWidth(width);
        preview.setFitHeight(height);
        preview.setPreserveRatio(true);
        preview.setSmooth(true);
        preview.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 10px;");
    }

    private void tuneInput(Node node) {
        node.setStyle(
            "-fx-background-color: rgba(0,0,0,0.42);" +
            "-fx-control-inner-background: rgba(0,0,0,0.42);" +
            "-fx-border-color: rgba(255,255,255,0.14);" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.55);" +
            "-fx-text-fill: white;"
        );
    }

    private void styleValidationLabel(Label label, String text, boolean valid) {
        label.setText(text);
        label.setTextFill(valid ? Color.web(tm.getAccentHex()) : Color.web("#ff3b30"));
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 600;");
    }

    private boolean isPublicationFormValid(String title, String description, String category) {
        return buildPublicationDraft(title, description, category).validateForCreate().isEmpty();
    }

    private void updateCreateValidationState() {
        String title = safe(createTitle.getText());
        String description = safe(createDescription.getText());
        String category = forceAnnouncementCreate ? "Announcement" : createCategory.getValue();

        String titleError = publicationFieldError(title, description, category, "Title ");
        String categoryError = publicationFieldError(title, description, category, "Category ");
        String descriptionError = publicationFieldError(title, description, category, "Description ");

        styleValidationLabel(
            createTitleValidation,
            titleError == null ? "✓ Title looks good" : titleError,
            titleError == null
        );
        styleValidationLabel(
            createCategoryValidation,
            categoryError == null ? "✓ Category selected" : categoryError,
            categoryError == null
        );
        styleValidationLabel(
            createDescriptionValidation,
            descriptionError == null ? "✓ Description looks good" : descriptionError,
            descriptionError == null
        );
    }

    private void updateEditValidationState() {
        String title = safe(editTitle.getText());
        String description = safe(editDescription.getText());
        String category = editCategory.getValue();

        String titleError = publicationFieldError(title, description, category, "Title ");
        String categoryError = publicationFieldError(title, description, category, "Category ");
        String descriptionError = publicationFieldError(title, description, category, "Description ");

        styleValidationLabel(
            editTitleValidation,
            titleError == null ? "✓ Title looks good" : titleError,
            titleError == null
        );
        styleValidationLabel(
            editCategoryValidation,
            categoryError == null ? "✓ Category selected" : categoryError,
            categoryError == null
        );
        styleValidationLabel(
            editDescriptionValidation,
            descriptionError == null ? "✓ Description looks good" : descriptionError,
            descriptionError == null
        );
    }

    private void updateCommentValidationState() {
        String description = safe(commentInput.getText());
        if (description.isEmpty()) {
            commentValidation.setText("Type your comment (minimum 5 characters)");
            commentValidation.setTextFill(Color.web("rgba(255,255,255,0.62)"));
            return;
        }

        String error = commentDescriptionError(description);
        if (error != null) {
            commentValidation.setText(error);
            commentValidation.setTextFill(Color.web("#ff3b30"));
            return;
        }

        commentValidation.setText("✓ Comment looks good");
        commentValidation.setTextFill(Color.web(tm.getAccentHex()));
    }

    private Publication buildPublicationDraft(String title, String description, String category) {
        Publication draft = new Publication();
        draft.setTitrePub(title == null ? "" : title.trim());
        draft.setDescriptionPub(description == null ? "" : description.trim());
        draft.setCategoriePub(category);
        draft.setDateCreationPub(LocalDateTime.now());
        draft.setUser(validationUser());
        return draft;
    }

    private String publicationFieldError(String title, String description, String category, String prefix) {
        List<String> errors = buildPublicationDraft(title, description, category).validateForCreate();
        for (String error : errors) {
            if (error.startsWith(prefix)) {
                return error;
            }
        }
        return null;
    }

    private String commentDescriptionError(String description) {
        Commentaire draft = new Commentaire();
        draft.setDescriptionCommentaire(description == null ? "" : description.trim());
        Publication publication = new Publication();
        publication.setIdPublication(current != null && current.getIdPublication() != null ? current.getIdPublication() : 1);
        draft.setPublication(publication);
        draft.setUser(validationUser());
        for (String error : draft.validateForCreate()) {
            if (error.startsWith("Comment text ")) {
                return error;
            }
        }
        return null;
    }

    private User validationUser() {
        User user = session.getCurrentUser();
        if (user != null && user.getIdUser() != null && user.getIdUser() > 0) {
            return user;
        }
        User placeholder = new User();
        placeholder.setIdUser(1);
        return placeholder;
    }

    private void updateOwnerActionsVisibility() {
        boolean can = canDeleteCurrent();
        ownerActions.setVisible(can);
        ownerActions.setManaged(can);
    }

    private boolean isModerator(User user) {
        if (user == null) {
            return false;
        }
        String role = user.getRoleUser();
        return role != null && MODERATOR_ROLES.contains(role);
    }

    private Image resolveImage(String dbValue, String fallbackFolder) {
        if (dbValue == null || dbValue.isBlank()) {
            return null;
        }

        String raw = dbValue.trim().replace("\\", "/");
        try {
            if (raw.startsWith("http://") || raw.startsWith("https://") || raw.startsWith("file:/")) {
                Image web = new Image(raw, true);
                return web.isError() ? null : web;
            }

            File pathAsFile = new File(raw);
            if (pathAsFile.exists()) {
                Image img = new Image(pathAsFile.toURI().toString(), true);
                return img.isError() ? null : img;
            }

            File uploadsRoot = new File(System.getProperty("user.dir") + File.separator + "uploads");
            List<File> candidates = List.of(
                new File(uploadsRoot, raw),
                new File(uploadsRoot, fallbackFolder + File.separator + raw),
                new File(uploadsRoot, "forum_images" + File.separator + raw),
                new File(uploadsRoot, "commentaire_images" + File.separator + raw)
            );

            for (File candidate : candidates) {
                if (candidate.exists()) {
                    Image img = new Image(candidate.toURI().toString(), true);
                    if (!img.isError()) {
                        return img;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private Dialog<ButtonType> styledDialog(String title) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        if (root.getScene() != null && root.getScene().getWindow() != null) {
            dialog.initOwner(root.getScene().getWindow());
        }

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle(
            "-fx-background-color: #12141b;" +
            "-fx-border-color: rgba(255,255,255,0.10);" +
            "-fx-border-width: 1;" +
            "-fx-font-family: 'Segoe UI';"
        );

        return dialog;
    }

    private boolean confirmFancy(String title, String message) {
        Dialog<ButtonType> dialog = styledDialog(title);
        Label content = new Label(message);
        content.setWrapText(true);
        content.setTextFill(Color.web("rgba(255,255,255,0.92)"));
        dialog.getDialogPane().setContent(content);

        ButtonType yes = new ButtonType("Delete", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(yes, ButtonType.CANCEL);

        return dialog.showAndWait().filter(result -> result == yes).isPresent();
    }

    private String promptReason(String title, String placeholder) {
        Dialog<ButtonType> dialog = styledDialog(title);
        TextArea area = new TextArea();
        area.setPrefRowCount(4);
        area.setPromptText(placeholder);
        tuneInput(area);

        VBox content = new VBox(10, dimLabel("Provide a short reason"), area);
        dialog.getDialogPane().setContent(content);

        ButtonType submit = new ButtonType("Submit", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(submit, ButtonType.CANCEL);

        return dialog.showAndWait().filter(result -> result == submit).map(result -> safe(area.getText())).orElse(null);
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);

        if (root.getScene() != null && root.getScene().getWindow() != null) {
            alert.initOwner(root.getScene().getWindow());
        }
        alert.showAndWait();
    }
}
