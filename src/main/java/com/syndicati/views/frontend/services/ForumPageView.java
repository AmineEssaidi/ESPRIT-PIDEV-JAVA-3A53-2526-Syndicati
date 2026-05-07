package com.syndicati.views.frontend.services;

import com.syndicati.MainApplication;
import com.syndicati.controllers.forum.CommentaireController;
import com.syndicati.controllers.forum.PublicationController;
import com.syndicati.controllers.forum.ReactionController;
import com.syndicati.controllers.user.profile.ProfileController;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.models.forum.Commentaire;
import com.syndicati.models.forum.Publication;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import com.syndicati.services.forum.OpenAIModerationService;
import com.syndicati.services.forum.SentimentAnalysisService;
import com.syndicati.services.forum.SentimentAnalysisService.SentimentResult;
import com.syndicati.services.forum.DiscordWebhookService;
import com.syndicati.services.forum.ReactionService.ReactionActionResult;
import com.syndicati.services.forum.ReactionService.ReactionPayload;
import com.syndicati.services.forum.ReactionService.ReactionStatus;
import com.syndicati.utils.image.ImageLoaderUtil;
import com.syndicati.utils.session.SessionManager;
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
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.scene.Cursor;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Modern Forum Page View with AI Moderation, Sentiment Analysis, and Discord integration.
 */
public class ForumPageView implements ViewInterface {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter SHORT_DATE_FMT = DateTimeFormatter.ofPattern("MMM dd");
    private static final List<String> CATEGORIES = List.of(
            "Announcement", "Suggestion", "Jeux Video", "Informatique", "Nouveauté", "Discussion General", "Culture",
            "Sport");
    private static final List<String> MODERATOR_ROLES = List.of("OWNER", "ADMIN", "SUPERADMIN", "SYNDIC");
    private static final List<String> EMOJIS = List.of("❤️", "😂", "😮", "😢", "😡", "👍", "🔥", "✨");

    private final StackPane root = new StackPane();
    private final VBox mainLayout = new VBox(18);
    private final VBox notificationBox = new VBox(10);
    private final ThemeManager tm = ThemeManager.getInstance();
    private final SessionManager session = SessionManager.getInstance();
    private final PublicationController publications = new PublicationController();
    private final CommentaireController comments = new CommentaireController();
    private final ReactionController reactions = new ReactionController();
    private final ProfileController profiles = new ProfileController();
    private final OpenAIModerationService moderationService = new OpenAIModerationService();
    private final SentimentAnalysisService sentimentService = new SentimentAnalysisService();
    private final DiscordWebhookService discordService = new DiscordWebhookService();

    private final VBox listBox = new VBox(10);
    private final StackPane faceStack = new StackPane();
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
    private Button feelingButton;
    private final Label postLikeCount = new Label("");
    private final Label postDislikeCount = new Label("");
    private final Label postRatioText = new Label("0%");
    private Region postLikesBarRegion;
    private Region postDislikesBarRegion;

    private final VBox commentsBox = new VBox(10);
    private final VBox commentFormContainer = new VBox(10);
    private final Label announcementHint = new Label("Comments are disabled for this announcement.");
    private final VBox publicationReportPanel = new VBox(8);
    private final VBox publicationEmojiPanel = new VBox(8);
    private final VBox publicationSentimentPanel = new VBox(8);
    private final Map<Integer, VBox> commentEditPanels = new HashMap<>();
    private final Map<Integer, VBox> commentReportPanels = new HashMap<>();
    private final Map<Integer, VBox> commentEmojiPanels = new HashMap<>();
    private final Map<Integer, VBox> commentSentimentPanels = new HashMap<>();
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
        mainLayout.setPadding(new Insets(24));
        mainLayout.setStyle("-fx-background-color: transparent;");
        mainLayout.setAlignment(Pos.TOP_CENTER);

        notificationBox.setPickOnBounds(false);
        notificationBox.setAlignment(Pos.TOP_RIGHT);
        notificationBox.setPadding(new Insets(20));
        notificationBox.setSpacing(10);
        notificationBox.setPrefWidth(400);
        notificationBox.setMaxWidth(Region.USE_PREF_SIZE);

        mainLayout.getChildren().addAll(buildHero(), buildSplit());
        root.getChildren().addAll(mainLayout, notificationBox);

        showFace(readFace);
        loadCategory("General");
    }

    @Override
    public StackPane getRoot() {
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
                        "-fx-border-radius: 48px;");

        Label badge = new Label("COMMUNITY HUB");
        badge.setStyle(
                "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.14) + ";" +
                        "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.45) + ";" +
                        "-fx-border-radius: 100px;" +
                        "-fx-background-radius: 100px;" +
                        "-fx-padding: 7 16 7 16;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: 800;" +
                        "-fx-text-fill: white;");

        Text title = new Text("Voices of Horizon");
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BLACK, 66));
        title.setFill(Color.WHITE);

        Text subtitle = new Text(
                "Join the conversation. Connect with neighbors, share ideas, and shape your community.");
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
                        "-fx-border-radius: 32px;");

        VBox main = buildMainPanel();
        VBox side = buildSidePanel();
        side.setPrefWidth(360);
        side.setMaxWidth(360);
        side.setMinWidth(360);

        main.setMinWidth(0);
        main.prefWidthProperty().bind(
                Bindings.max(0, split.widthProperty().subtract(side.widthProperty())));
        HBox.setHgrow(main, Priority.ALWAYS);
        HBox.setHgrow(side, Priority.NEVER);
        split.getChildren().addAll(main, side);
        return split;
    }

    private VBox buildMainPanel() {
        VBox panel = new VBox();
        panel.setMinWidth(0);
        panel.setPadding(new Insets(0));
        panel.setStyle(
                "-fx-border-color: transparent rgba(255,255,255,0.10) transparent transparent; -fx-border-width: 0 1px 0 0;");
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
        detailHero.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #1a1a2e 0%, #16213e 100%); -fx-border-radius: 24px 0px 0px 24px;");

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
        heroOverlay.setStyle(
                "-fx-background-color: linear-gradient(from 0% 100% to 0% 0%, rgba(20,20,30,0.94) 3%, rgba(20,20,30,0.08) 100%);");

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
        actionsBar.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 14px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 14px;");

        Rectangle actionsBarClip = new Rectangle();
        actionsBarClip.widthProperty().bind(actionsBar.widthProperty());
        actionsBarClip.heightProperty().bind(actionsBar.heightProperty());
        actionsBar.setClip(actionsBarClip);

        Button createSwitcher = ghostButton("New Post", () -> openCreateFace(false));
        Button editSwitcher = ghostButton("Edit", this::openEditFace);
        Button deleteSwitcher = ghostButton("Delete", this::deleteCurrentPublication);
        feelingButton = ghostButton("Feeling", this::showSentimentAnalysis);

        ownerActions.getChildren().setAll(editSwitcher, deleteSwitcher);
        ownerActions.setManaged(false);
        ownerActions.setVisible(false);

        VBox reactionWrap = new VBox(reactionCluster);
        reactionWrap.setMinWidth(0);
        reactionWrap.setMaxWidth(Double.MAX_VALUE);
        BorderPane.setAlignment(reactionWrap, Pos.CENTER_LEFT);

        postReportButton.setMinWidth(Region.USE_PREF_SIZE);
        postReportButton.setMaxWidth(Region.USE_PREF_SIZE);

        HBox rightActions = new HBox(8, ownerActions, feelingButton, postReportButton);
        rightActions.setAlignment(Pos.CENTER_RIGHT);
        rightActions.setMinWidth(Region.USE_PREF_SIZE);
        rightActions.setMaxWidth(Region.USE_PREF_SIZE);
        BorderPane.setAlignment(rightActions, Pos.CENTER_RIGHT);

        reactionCluster.setMinWidth(0);
        reactionCluster.prefWrapLengthProperty().bind(
                actionsBar.widthProperty().subtract(rightActions.widthProperty()).subtract(40));

        actionsBar.setLeft(reactionWrap);
        actionsBar.setRight(rightActions);

        feelingButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                        "-fx-border-color: rgba(255,255,255,0.15);" +
                        "-fx-border-radius: 12px; -fx-background-radius: 12px;" +
                        "-fx-text-fill: white; -fx-padding: 8 14 8 14;");
        feelingButton.setOnAction(e -> showSentimentAnalysis());

        VBox publicationBody = new VBox(10, heroBody, actionsBar);
        publicationBody.setPadding(new Insets(22, 0, 10, 0));
        publicationBody.setMinWidth(0);
        publicationBody.setMaxWidth(Double.MAX_VALUE);

        buildPublicationReportPanel();
        buildPublicationEmojiPanel();
        VBox inlinePanels = new VBox(8, publicationReportPanel, publicationEmojiPanel, publicationSentimentPanel);
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

        // Custom Ratio Bar Container (Pill shape)
        HBox ratioBarContainer = new HBox();
        ratioBarContainer.setPrefSize(110, 6);
        ratioBarContainer.setMaxSize(110, 6);
        ratioBarContainer.setAlignment(Pos.CENTER_LEFT);
        ratioBarContainer.setStyle("-fx-background-radius: 100px; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 100px; -fx-border-width: 1; -fx-overflow: hidden;");
        
        // Clip to ensure rounded corners work for internal children
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(110, 6);
        clip.setArcWidth(6);
        clip.setArcHeight(6);
        ratioBarContainer.setClip(clip);

        // Green Part (Likes)
        Region likesBar = new Region();
        likesBar.setStyle("-fx-background-color: #22c55e;");
        likesBar.setPrefHeight(6);
        
        // Red Part (Dislikes)
        Region dislikesBar = new Region();
        dislikesBar.setStyle("-fx-background-color: #ef4444;");
        dislikesBar.setPrefHeight(6);
        
        ratioBarContainer.getChildren().addAll(likesBar, dislikesBar);
        
        // Store references to update later
        this.postLikesBarRegion = likesBar; 
        this.postDislikesBarRegion = dislikesBar;
        
        postRatioText.setTextFill(Color.web("rgba(255,255,255,0.72)"));
        postRatioText.setStyle("-fx-font-size: 11px; -fx-font-weight: 700;");

        HBox ratioWrap = new HBox(8, ratioBarContainer, postRatioText);
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
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 16px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 16px;");

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
                createFileLabel);

        HBox previewRow = new HBox(8, createImagePreview);

        HBox actions = new HBox(8,
                primaryButton("Publish", this::submitPublication),
                ghostButton("Back", () -> showFace(readFace)));

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
                actions);

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
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 16px; -fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 16px;");

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
                editFileLabel);

        VBox currentWrap = new VBox(4, dimLabel("Current image"), editCurrentImagePreview);
        VBox newWrap = new VBox(4, dimLabel("New image"), editNewImagePreview);
        HBox previews = new HBox(18, currentWrap, newWrap);

        HBox actions = new HBox(8,
                primaryButton("Save", this::submitEditPublication),
                ghostButton("Back", () -> showFace(readFace)));

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
                actions);

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
        wrap.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03); -fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 16px; -fx-background-radius: 16px;");

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
                        "-fx-background-radius: 12px;");

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
                        "-fx-background-radius: 10px; -fx-padding: 6 12 6 12;");
        privateBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                        "-fx-border-color: rgba(255,255,255,0.15);" +
                        "-fx-border-radius: 10px; -fx-background-radius: 10px;" +
                        "-fx-text-fill: rgba(255,255,255,0.70); -fx-padding: 6 12 6 12;");

        publicBtn.setOnAction(e -> {
            isCommentPublic = true;
            publicBtn.setStyle(
                    "-fx-background-color: " + tm.getAccentHex() + ";" +
                            "-fx-text-fill: white; -fx-font-weight: 700;" +
                            "-fx-background-radius: 10px; -fx-padding: 6 12 6 12;");
            privateBtn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.06);" +
                            "-fx-border-color: rgba(255,255,255,0.15);" +
                            "-fx-border-radius: 10px; -fx-background-radius: 10px;" +
                            "-fx-text-fill: rgba(255,255,255,0.70); -fx-padding: 6 12 6 12;");
        });

        privateBtn.setOnAction(e -> {
            isCommentPublic = false;
            privateBtn.setStyle(
                    "-fx-background-color: " + tm.getAccentHex() + ";" +
                            "-fx-text-fill: white; -fx-font-weight: 700;" +
                            "-fx-background-radius: 10px; -fx-padding: 6 12 6 12;");
            publicBtn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.06);" +
                            "-fx-border-color: rgba(255,255,255,0.15);" +
                            "-fx-border-radius: 10px; -fx-background-radius: 10px;" +
                            "-fx-text-fill: rgba(255,255,255,0.70); -fx-padding: 6 12 6 12;");
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
                        commentFileLabel.setText(
                                commentImages.size() + " image" + (commentImages.size() > 1 ? "s" : "") + " selected");
                        updateImagePreview(commentImages, commentImagePreviews);
                    }
                },
                commentFileLabel,
                commentImagePreviews,
                commentImages);
        imageRow.setAlignment(Pos.CENTER_LEFT);
        imageRow.setMaxWidth(Double.MAX_VALUE);

        Button send = primaryButton("Post Comment", this::submitComment);

        commentFormContainer.getChildren().setAll(commentInput, commentValidation, imageRow, commentImagePreviews,
                toggleIsland, send);
        commentFormContainer.setFillWidth(true);

        announcementHint.setTextFill(Color.web("rgba(255,255,255,0.5)"));
        announcementHint.setStyle("-fx-font-size: 13px; -fx-font-style: italic; -fx-padding: 10 0 0 0;");
        announcementHint.setManaged(false);
        announcementHint.setVisible(false);

        wrap.getChildren().addAll(heading, commentsBox, commentFormContainer, announcementHint);
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

        HBox topActions = new HBox(8,
                filterGeneral,
                filterAnnouncements);
        topActions.setPadding(new Insets(10, 0, 10, 0));

        styleFilterButton(filterGeneral, true);
        styleFilterButton(filterAnnouncements, false);

        filterGeneral.setOnAction(e -> {
            currentFilter = "General";
            styleFilterButton(filterGeneral, true);
            styleFilterButton(filterAnnouncements, false);
            loadCategory("General");
        });

        filterAnnouncements.setOnAction(e -> {
            currentFilter = "Announcement";
            styleFilterButton(filterGeneral, false);
            styleFilterButton(filterAnnouncements, true);
            loadCategory("Announcement");
        });

        Button createSwitcher = ghostButton("+ New Post", () -> openCreateFace(false));
        createSwitcher.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        side.getChildren().addAll(title, topActions, createSwitcher, scroll);
        return side;
    }

    private void styleFilterButton(Button btn, boolean active) {
        btn.setStyle(
                "-fx-background-color: " + (active ? tm.getAccentHex() : "rgba(255,255,255,0.05)") + ";" +
                        "-fx-text-fill: " + (active ? "white" : "rgba(255,255,255,0.6)") + ";" +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 8px; -fx-padding: 6 12 6 12;");
    }

    private void loadCategory(String category) {
        listBox.getChildren().clear();
        listItemsById.clear();
        List<Publication> pubs = publications.publicationsByCategory(category);
        for (Publication p : pubs) {
            VBox item = buildListItem(p);
            listBox.getChildren().add(item);
            listItemsById.put(p.getIdPublication(), item);
        }
        if (!pubs.isEmpty()) {
            selectPublication(pubs.get(0));
        }
    }

    private VBox buildListItem(Publication p) {
        VBox item = new VBox(6);
        item.setPadding(new Insets(14));
        item.setCursor(Cursor.HAND);
        item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 12px; -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 12px;");

        item.setOnMouseEntered(e -> item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 12px; -fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 12px;"));
        item.setOnMouseExited(e -> {
            if (current != null && current.getIdPublication().equals(p.getIdPublication())) {
                item.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 12px; -fx-border-color: "
                                + tm.getAccentHex() + "; -fx-border-radius: 12px;");
            } else {
                item.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 12px; -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 12px;");
            }
        });

        item.setOnMouseClicked(e -> selectPublication(p));

        Label cat = new Label(p.getCatPublication().toUpperCase());
        cat.setStyle(
                "-fx-font-size: 9px; -fx-font-weight: 800; -fx-text-fill: " + tm.getAccentHex() + "; -fx-padding: 0 0 4 0;");

        Text title = new Text(p.getTitrePublication());
        title.setFill(Color.WHITE);
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 14));
        title.setWrappingWidth(300);

        String authorName = (p.getUser() != null)
                ? (p.getUser().getFirstName() + " " + p.getUser().getLastName())
                : "Anonymous";
        Label meta = new Label("by " + authorName + " • " + p.getDatePublication().format(SHORT_DATE_FMT));
        meta.setTextFill(Color.web("rgba(255,255,255,0.5)"));
        meta.setStyle("-fx-font-size: 11px;");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Circle avatar = createAvatar(18, p.getUser());
        
        VBox textContent = new VBox(2);
        textContent.getChildren().addAll(cat, title, meta);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        
        row.getChildren().addAll(avatar, textContent);
        item.getChildren().add(row);
        return item;
    }

    private void selectPublication(Publication p) {
        if (current != null && listItemsById.containsKey(current.getIdPublication())) {
            listItemsById.get(current.getIdPublication()).setStyle(
                    "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 12px; -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 12px;");
        }
        current = p;
        if (listItemsById.containsKey(p.getIdPublication())) {
            listItemsById.get(p.getIdPublication()).setStyle(
                    "-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 12px; -fx-border-color: "
                            + tm.getAccentHex() + "; -fx-border-radius: 12px;");
        }

        refreshPublicationDetail();
        showFace(readFace);
    }

    private void refreshPublicationDetail() {
        if (current == null)
            return;

        heroTitle.setText(current.getTitrePublication());
        heroCategory.setText(current.getCatPublication());
        heroBody.setText(current.getDescPublication());

        String author = (current.getUser() != null)
                ? (current.getUser().getFirstName() + " " + current.getUser().getLastName())
                : "Anonymous";
        heroMeta.setText("Published on " + current.getDatePublication().format(DATE_FMT) + " by " + author);

        String img = current.getImagePublication();
        if (img != null && !img.isBlank() && !"-".equals(img)) {
            String fullPath = "file:" + System.getProperty("user.dir") + File.separator + "uploads" + File.separator
                    + img;
            currentHeroImage = new Image(fullPath, true);
            detailHeroImage.setImage(currentHeroImage);
            detailHeroImage.setVisible(true);
            refreshHeroViewport();
        } else {
            detailHeroImage.setImage(null);
            detailHeroImage.setVisible(false);
            currentHeroImage = null;
        }

        // Ownership
        User me = session.getCurrentUser();
        boolean isOwner = me != null && current.getUser() != null
                && Objects.equals(me.getIdUser(), current.getUser().getIdUser());
        boolean isMod = me != null && MODERATOR_ROLES.contains(me.getRoleUser());

        ownerActions.setVisible(isOwner || isMod);
        ownerActions.setManaged(isOwner || isMod);

        // Reactions
        refreshPublicationReactions();

        // Comments
        loadComments();
        
        // Announcement handling
        boolean isAnnouncement = "Announcement".equalsIgnoreCase(current.getCatPublication());
        commentFormContainer.setVisible(!isAnnouncement || isMod);
        commentFormContainer.setManaged(!isAnnouncement || isMod);
        announcementHint.setVisible(isAnnouncement && !isMod);
        announcementHint.setManaged(isAnnouncement && !isMod);

        // Clear panels
        publicationReportPanel.setVisible(false);
        publicationReportPanel.setManaged(false);
        publicationEmojiPanel.setVisible(false);
        publicationEmojiPanel.setManaged(false);
    }

    private void refreshHeroViewport() {
        if (currentHeroImage == null || detailHeroImage.getImage() == null)
            return;

        double viewW = detailHero.getWidth();
        double viewH = detailHero.getHeight();
        if (viewW <= 0 || viewH <= 0)
            return;

        double imgW = currentHeroImage.getWidth();
        double imgH = currentHeroImage.getHeight();

        double viewAspect = viewW / viewH;
        double imgAspect = imgW / imgH;

        double sw, sh, sx, sy;
        if (imgAspect > viewAspect) {
            sh = imgH;
            sw = sh * viewAspect;
            sy = 0;
            sx = (imgW - sw) / 2;
        } else {
            sw = imgW;
            sh = sw / viewAspect;
            sx = 0;
            sy = (imgH - sh) / 2;
        }

        detailHeroImage.setViewport(new Rectangle2D(sx, sy, sw, sh));
        detailHeroImage.setFitWidth(viewW);
        detailHeroImage.setFitHeight(viewH);
    }

    private void refreshPublicationReactions() {
        ReactionStatus rs = reactions.getPublicationStatus(current.getIdPublication(),
                session.getCurrentUser() != null ? session.getCurrentUser().getIdUser() : -1);

        postLikeCount.setText(String.valueOf(rs.getLikes()));
        postDislikeCount.setText(String.valueOf(rs.getDislikes()));

        updateReactionButtonStyle(postLikeButton, rs.isLiked(), "#22c55e");
        updateReactionButtonStyle(postDislikeButton, rs.isDisliked(), "#ef4444");
        updateReactionButtonStyle(postBookmarkButton, rs.isBookmarked(), "#f59e0b");

        // Ratio bar update
        int total = rs.getLikes() + rs.getDislikes();
        double ratio = (total == 0) ? 0 : (double) rs.getLikes() / total;
        postRatioText.setText((int) (ratio * 100) + "%");
        
        if (postLikesBarRegion != null && postDislikesBarRegion != null) {
            double greenWidth = 110 * ratio;
            postLikesBarRegion.setPrefWidth(greenWidth);
            postDislikesBarRegion.setPrefWidth(110 - greenWidth);
        }
    }

    private void updateReactionButtonStyle(Button btn, boolean active, String colorHex) {
        if (active) {
            btn.setStyle(
                    "-fx-background-color: " + tm.toRgba(colorHex, 0.2) + ";" +
                            "-fx-border-color: " + colorHex + ";" +
                            "-fx-text-fill: " + colorHex + ";" +
                            "-fx-font-size: 12px; -fx-font-weight: 800; -fx-background-radius: 12px; -fx-border-radius: 12px; -fx-padding: 8 16 8 16;");
        } else {
            btn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.05);" +
                            "-fx-border-color: rgba(255,255,255,0.12);" +
                            "-fx-text-fill: rgba(255,255,255,0.7);" +
                            "-fx-font-size: 12px; -fx-font-weight: 600; -fx-background-radius: 12px; -fx-border-radius: 12px; -fx-padding: 8 16 8 16;");
        }
    }

    private void togglePublicationReaction(String type) {
        if (session.getCurrentUser() == null) {
            showNotification("Auth Required", "Please log in to react.", "#f59e0b");
            return;
        }

        ReactionActionResult res = reactions.togglePublicationReaction(current.getIdPublication(),
                session.getCurrentUser().getIdUser(), type);
        if (res.isSuccess()) {
            refreshPublicationReactions();
        } else {
            showNotification("Error", res.getMessage(), "#ef4444");
        }
    }

    private void togglePublicationEmojiPanel() {
        publicationEmojiPanel.setVisible(!publicationEmojiPanel.isVisible());
        publicationEmojiPanel.setManaged(publicationEmojiPanel.isVisible());
    }

    private void buildPublicationEmojiPanel() {
        publicationEmojiPanel.getChildren().clear();
        publicationEmojiPanel.setVisible(false);
        publicationEmojiPanel.setManaged(false);
        publicationEmojiPanel.setPadding(new Insets(10));
        publicationEmojiPanel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 12px; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 12px;");

        FlowPane emojis = new FlowPane(15, 15);
        for (String emoji : EMOJIS) {
            String hex = Integer.toHexString(emoji.codePointAt(0));
            // Special case for heart
            if (emoji.equals("❤️")) hex = "2764";
            
            String url = "https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/" + hex + ".png";
            
            ImageView iv = new ImageView(new Image(url, true));
            iv.setFitWidth(32);
            iv.setFitHeight(32);
            iv.setPreserveRatio(true);
            
            Button eb = new Button();
            eb.setGraphic(iv);
            eb.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;");
            
            ScaleTransition pulse = new ScaleTransition(Duration.millis(200), eb);
            pulse.setToX(1.3);
            pulse.setToY(1.3);
            
            eb.setOnMouseEntered(e -> {
                eb.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 12px; -fx-cursor: hand;");
                pulse.setRate(1);
                pulse.play();
            });
            eb.setOnMouseExited(e -> {
                eb.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                pulse.setRate(-1);
                pulse.play();
            });
            
            eb.setOnAction(e -> {
                reactions.reactPublicationEmoji(current.getIdPublication(), session.getCurrentUser().getIdUser(),
                        emoji);
                refreshPublicationReactions();
                publicationEmojiPanel.setVisible(false);
                publicationEmojiPanel.setManaged(false);
            });
            emojis.getChildren().add(eb);
        }
        publicationEmojiPanel.getChildren().add(emojis);
    }

    private void togglePublicationReportPanel() {
        publicationReportPanel.setVisible(!publicationReportPanel.isVisible());
        publicationReportPanel.setManaged(publicationReportPanel.isVisible());
    }

    private void buildPublicationReportPanel() {
        publicationReportPanel.getChildren().clear();
        publicationReportPanel.setVisible(false);
        publicationReportPanel.setManaged(false);
        publicationReportPanel.setPadding(new Insets(10));
        publicationReportPanel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 12px; -fx-border-color: #ef444433; -fx-border-radius: 12px;");

        TextArea reason = new TextArea();
        reason.setPromptText("Reason for reporting...");
        reason.setPrefRowCount(2);
        tuneInput(reason);

        HBox btns = new HBox(8,
                primaryButton("Submit Report", () -> {
                    reactions.togglePublicationReaction(current.getIdPublication(),
                            session.getCurrentUser().getIdUser(), "Report:" + reason.getText());
                    showNotification("Report Sent", "Moderators will review this post.", "#ef4444");
                    publicationReportPanel.setVisible(false);
                    publicationReportPanel.setManaged(false);
                    reason.clear();
                }),
                ghostButton("Cancel", () -> {
                    publicationReportPanel.setVisible(false);
                    publicationReportPanel.setManaged(false);
                }));

        publicationReportPanel.getChildren().addAll(new Label("Report Publication"), reason, btns);
    }

    private void loadComments() {
        commentsBox.getChildren().clear();
        commentEditPanels.clear();
        commentReportPanels.clear();
        commentEmojiPanels.clear();

        List<Commentaire> list = comments.commentsByPublication(current.getIdPublication());
        User me = session.getCurrentUser();
        Integer myId = me != null ? me.getIdUser() : -1;

        for (Commentaire c : list) {
            if (!c.getIsPublic() && !MODERATOR_ROLES.contains(me.getRoleUser()) && !c.getUser().getIdUser().equals(myId) && !current.getUser().getIdUser().equals(myId)) {
                continue;
            }
            commentsBox.getChildren().add(buildCommentItem(c));
        }
    }

    private Node buildCommentItem(Commentaire c) {
        VBox item = new VBox(8);
        item.setPadding(new Insets(14));
        item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 12px; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 12px;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = createAvatar(16, c.getUser());

        String authorName = (c.getUser() != null)
                ? (c.getUser().getFirstName() + " " + c.getUser().getLastName())
                : "Anonymous";
        Label author = new Label(authorName);
        author.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 13px;");

        Label date = new Label(c.getDateCommentaire().format(DATE_FMT));
        date.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.4);");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, author, date, spacer);

        if (!c.getIsPublic()) {
            Label privateBadge = new Label("PRIVATE");
            privateBadge.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #f59e0b; -fx-padding: 2 6; -fx-background-color: #f59e0b11; -fx-background-radius: 4px;");
            header.getChildren().add(privateBadge);
        }

        Text body = new Text(c.getContenuCommentaire());
        body.setFill(Color.web("rgba(255,255,255,0.9)"));
        body.setFont(Font.font(13));
        body.setWrappingWidth(700);

        VBox content = new VBox(8, body);

        if (c.getImageCommentaire() != null && !c.getImageCommentaire().isBlank() && !"-".equals(c.getImageCommentaire())) {
            String[] imgs = c.getImageCommentaire().split(",");
            FlowPane imgGrid = new FlowPane(8, 8);
            for (String imgPath : imgs) {
                if (imgPath.isBlank()) continue;
                String fullPath = "file:" + System.getProperty("user.dir") + File.separator + "uploads" + File.separator + imgPath;
                ImageView iv = new ImageView(new Image(fullPath, 160, 160, true, true));
                iv.setStyle("-fx-background-radius: 8px; -fx-border-radius: 8px;");
                imgGrid.getChildren().add(iv);
            }
            content.getChildren().add(imgGrid);
        }

        // Inline Panels (Edit/Report/Emoji)
        VBox editPanel = buildCommentEditPanel(c);
        VBox reportPanel = buildCommentReportPanel(c);
        VBox emojiPanel = buildCommentEmojiPanel(c);
        VBox sentimentPanel = new VBox(8);
        sentimentPanel.setVisible(false);
        sentimentPanel.setManaged(false);
        
        commentEditPanels.put(c.getIdCommentaire(), editPanel);
        commentReportPanels.put(c.getIdCommentaire(), reportPanel);
        commentEmojiPanels.put(c.getIdCommentaire(), emojiPanel);
        commentSentimentPanels.put(c.getIdCommentaire(), sentimentPanel);

        // Action Row
        HBox actions = buildCommentActionRow(c);

        item.getChildren().addAll(header, content, editPanel, reportPanel, emojiPanel, sentimentPanel, actions);
        return item;
    }

    private HBox buildCommentActionRow(Commentaire c) {
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(4, 0, 0, 0));

        ReactionStatus rs = reactions.getCommentStatus(c.getIdCommentaire(),
                session.getCurrentUser() != null ? session.getCurrentUser().getIdUser() : -1);

        Button like = commentActionButton("Like (" + rs.getLikes() + ")", rs.isLiked(), "#22c55e");
        Button dislike = commentActionButton("Dislike (" + rs.getDislikes() + ")", rs.isDisliked(), "#ef4444");
        Button emoji = commentActionButton("Emoji", false, tm.getAccentHex());
        Button report = commentActionButton("Report", false, "#ef4444");
        Button feeling = commentActionButton("Feeling", false, tm.getAccentHex());

        like.setOnAction(e -> {
            reactions.toggleCommentReaction(c.getIdCommentaire(), session.getCurrentUser().getIdUser(), "Like");
            loadComments();
        });
        dislike.setOnAction(e -> {
            reactions.toggleCommentReaction(c.getIdCommentaire(), session.getCurrentUser().getIdUser(), "Dislike");
            loadComments();
        });
        emoji.setOnAction(e -> toggleCommentEmojiPanel(c.getIdCommentaire()));
        report.setOnAction(e -> toggleCommentReportPanel(c.getIdCommentaire()));
        feeling.setOnAction(e -> showCommentSentimentAnalysis(c));

        actions.getChildren().addAll(like, dislike, emoji, report, feeling);

        User me = session.getCurrentUser();
        boolean isOwner = me != null && c.getUser() != null && Objects.equals(me.getIdUser(), c.getUser().getIdUser());
        boolean isMod = me != null && MODERATOR_ROLES.contains(me.getRoleUser());

        if (isOwner || isMod) {
            Button edit = commentActionButton("Edit", false, tm.getAccentHex());
            Button delete = commentActionButton("Delete", false, "#ef4444");
            edit.setOnAction(e -> toggleCommentEditPanel(c.getIdCommentaire()));
            delete.setOnAction(e -> deleteComment(c));
            actions.getChildren().addAll(edit, delete);
        }

        return actions;
    }

    private Button commentActionButton(String text, boolean active, String colorHex) {
        Button btn = new Button(text);
        if (active) {
            btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: " + colorHex
                            + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0; -fx-cursor: hand;");
        } else {
            btn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0; -fx-cursor: hand;");
        }
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: " + colorHex
                        + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> {
            if (!active)
                btn.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0; -fx-cursor: hand;");
        });
        return btn;
    }

    private VBox buildCommentEditPanel(Commentaire c) {
        VBox panel = new VBox(8);
        panel.setVisible(false);
        panel.setManaged(false);
        panel.setPadding(new Insets(10));
        panel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8px; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8px;");

        TextArea input = new TextArea(c.getContenuCommentaire());
        input.setPrefRowCount(2);
        tuneInput(input);

        CheckBox publicToggle = new CheckBox("Public Visibility");
        publicToggle.setSelected(c.getIsPublic());
        publicToggle.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");

        HBox btns = new HBox(8,
                primaryButton("Update", () -> {
                    comments.commentUpdate(c.getIdCommentaire(), input.getText(), c.getImageCommentaire(),
                            publicToggle.isSelected());
                    loadComments();
                }),
                ghostButton("Cancel", () -> toggleCommentEditPanel(c.getIdCommentaire())));

        panel.getChildren().addAll(input, publicToggle, btns);
        return panel;
    }

    private VBox buildCommentReportPanel(Commentaire c) {
        VBox panel = new VBox(8);
        panel.setVisible(false);
        panel.setManaged(false);
        panel.setPadding(new Insets(10));
        panel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8px; -fx-border-color: #ef444433; -fx-border-radius: 8px;");

        TextArea reason = new TextArea();
        reason.setPromptText("Reason for reporting...");
        reason.setPrefRowCount(2);
        tuneInput(reason);

        HBox btns = new HBox(8,
                primaryButton("Submit Report", () -> {
                    reactions.toggleCommentReaction(c.getIdCommentaire(), session.getCurrentUser().getIdUser(),
                            "Report:" + reason.getText());
                    showNotification("Report Sent", "Moderators will review this comment.", "#ef4444");
                    toggleCommentReportPanel(c.getIdCommentaire());
                }),
                ghostButton("Cancel", () -> toggleCommentReportPanel(c.getIdCommentaire())));

        panel.getChildren().addAll(new Label("Report Comment"), reason, btns);
        return panel;
    }

    private VBox buildCommentEmojiPanel(Commentaire c) {
        VBox panel = new VBox(8);
        panel.setVisible(false);
        panel.setManaged(false);
        panel.setPadding(new Insets(10));
        panel.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8px; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8px;");

        FlowPane emojis = new FlowPane(12, 12);
        for (String emoji : EMOJIS) {
            String hex = Integer.toHexString(emoji.codePointAt(0));
            if (emoji.equals("❤️")) hex = "2764";
            String url = "https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/" + hex + ".png";
            
            ImageView iv = new ImageView(new Image(url, true));
            iv.setFitWidth(28);
            iv.setFitHeight(28);
            iv.setPreserveRatio(true);
            
            Button eb = new Button();
            eb.setGraphic(iv);
            eb.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4;");
            
            ScaleTransition pulse = new ScaleTransition(Duration.millis(200), eb);
            pulse.setToX(1.25);
            pulse.setToY(1.25);
            
            eb.setOnMouseEntered(e -> {
                eb.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-background-radius: 10px; -fx-cursor: hand;");
                pulse.setRate(1);
                pulse.play();
            });
            eb.setOnMouseExited(e -> {
                eb.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                pulse.setRate(-1);
                pulse.play();
            });
            
            eb.setOnAction(e -> {
                reactions.reactCommentEmoji(c.getIdCommentaire(), session.getCurrentUser().getIdUser(), emoji);
                loadComments();
            });
            emojis.getChildren().add(eb);
        }
        panel.getChildren().add(emojis);
        return panel;
    }

    private void toggleCommentEditPanel(int id) {
        VBox p = commentEditPanels.get(id);
        p.setVisible(!p.isVisible());
        p.setManaged(p.isVisible());
    }

    private void toggleCommentReportPanel(int id) {
        VBox p = commentReportPanels.get(id);
        p.setVisible(!p.isVisible());
        p.setManaged(p.isVisible());
    }

    private void toggleCommentEmojiPanel(int id) {
        VBox p = commentEmojiPanels.get(id);
        p.setVisible(!p.isVisible());
        p.setManaged(p.isVisible());
    }

    private void deleteComment(Commentaire c) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Comment");
        alert.setHeaderText("Are you sure you want to delete this comment?");
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                comments.commentDelete(c.getIdCommentaire());
                loadComments();
            }
        });
    }

    private void submitComment() {
        if (session.getCurrentUser() == null) {
            showNotification("Auth Required", "Please log in to comment.", "#f59e0b");
            return;
        }

        String text = commentInput.getText().trim();
        if (text.isEmpty()) {
            commentValidation.setText("Comment cannot be empty");
            commentValidation.setTextFill(Color.web("#ef4444"));
            return;
        }

        // Moderation
        if (moderationService.isFlagged(text)) {
            showNotification("Moderation", "Your comment contains inappropriate content.", "#ef4444");
            return;
        }

        // Image upload
        StringBuilder imgPaths = new StringBuilder();
        for (File f : commentImages) {
            try {
                String path = copyFileToUploads(f);
                if (imgPaths.length() > 0)
                    imgPaths.append(",");
                imgPaths.append(path);
            } catch (IOException e) {
            }
        }

        comments.commentCreate(text, imgPaths.toString(), current, session.getCurrentUser(), isCommentPublic);
        commentInput.clear();
        commentImages.clear();
        commentImagePreviews.getChildren().clear();
        loadComments();
        showNotification("Success", "Comment posted.", "#22c55e");
    }

    private void openCreateFace(boolean isAnnouncement) {
        forceAnnouncementCreate = isAnnouncement;
        createTitle.clear();
        createDescription.clear();
        createImage = null;
        createImageField.clear();
        createImagePreview.setImage(null);
        createCategory.setValue(isAnnouncement ? "Announcement" : "Discussion General");
        showFace(createFace);
    }

    private void submitPublication() {
        String title = createTitle.getText().trim();
        String desc = createDescription.getText().trim();
        String cat = createCategory.getValue();

        if (title.length() < 5 || desc.length() < 10) {
            showNotification("Validation", "Check fields length.", "#ef4444");
            return;
        }

        // Moderation
        if (moderationService.isFlagged(title + " " + desc)) {
            showNotification("Moderation", "Inappropriate content detected.", "#ef4444");
            return;
        }

        String imgPath = "-";
        if (createImage != null) {
            try {
                imgPath = copyFileToUploads(createImage);
            } catch (IOException e) {
            }
        }

        Integer id = publications.publicationCreate(title, desc, cat, imgPath, session.getCurrentUser());
        if (id != null && id > 0) {
            // Discord announcement
            if ("Announcement".equalsIgnoreCase(cat)) {
                discordService.sendAnnouncement("📢 **NEW ANNOUNCEMENT**\n\n**" + title + "**\n" + desc, createImage);
            }

            loadCategory(currentFilter);
            showFace(readFace);
            showNotification("Success", "Publication created.", "#22c55e");
        }
    }

    private void openEditFace() {
        if (current == null)
            return;
        editTitle.setText(current.getTitrePublication());
        editDescription.setText(current.getDescPublication());
        editCategory.setValue(current.getCatPublication());
        editImage = null;
        editImageField.clear();
        editNewImagePreview.setImage(null);

        if (current.getImagePublication() != null && !"-".equals(current.getImagePublication())) {
            String path = "file:" + System.getProperty("user.dir") + File.separator + "uploads" + File.separator
                    + current.getImagePublication();
            editCurrentImagePreview.setImage(new Image(path, true));
        } else {
            editCurrentImagePreview.setImage(null);
        }
        showFace(editFace);
    }

    private void submitEditPublication() {
        String title = editTitle.getText().trim();
        String desc = editDescription.getText().trim();
        String cat = editCategory.getValue();

        String imgPath = current.getImagePublication();
        if (editImage != null) {
            try {
                imgPath = copyFileToUploads(editImage);
            } catch (IOException e) {
            }
        }

        publications.publicationUpdate(current.getIdPublication(), title, desc, cat, imgPath);
        loadCategory(currentFilter);
        showFace(readFace);
        showNotification("Success", "Publication updated.", "#22c55e");
    }

    private void deleteCurrentPublication() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Publication");
        alert.setHeaderText("Delete '" + current.getTitrePublication() + "'?");
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                publications.publicationDelete(current.getIdPublication());
                loadCategory(currentFilter);
            }
        });
    }

    private void showSentimentAnalysis() {
        if (current == null) return;
        
        if (publicationSentimentPanel.isVisible()) {
            publicationSentimentPanel.setVisible(false);
            publicationSentimentPanel.setManaged(false);
            return;
        }

        publicationSentimentPanel.getChildren().clear();
        publicationSentimentPanel.setPadding(new Insets(14));
        publicationSentimentPanel.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 12px; -fx-border-color: " + tm.getAccentHex() + "33; -fx-border-radius: 12px;");
        
        Label loading = new Label("Analyzing feelings...");
        loading.setTextFill(Color.web(tm.getAccentHex()));
        publicationSentimentPanel.getChildren().add(loading);
        publicationSentimentPanel.setVisible(true);
        publicationSentimentPanel.setManaged(true);

        String textToAnalyze = current.getTitrePublication() + ". " + current.getDescPublication();
        sentimentService.analyzeContent(textToAnalyze).thenAccept(result -> {
            javafx.application.Platform.runLater(() -> {
                if (publicationSentimentPanel == null) return;
                if (!result.success) {
                    publicationSentimentPanel.getChildren().setAll(new Label("Analysis Failed: " + result.explanation));
                    return;
                }
                fillSentimentPanel(publicationSentimentPanel, result);
            });
        });
    }

    private void fillSentimentPanel(VBox panel, SentimentResult result) {
        String color = result.sentiment.toLowerCase().contains("pos") ? "#4ade80" : 
                       (result.sentiment.toLowerCase().contains("neg") ? "#f87171" : "#fbbf24");
        
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // AI Icon/Indicator
        Label aiIcon = new Label("✨");
        aiIcon.setStyle("-fx-font-size: 18px;");
        
        VBox titleBox = new VBox(2);
        Label titleLabel = new Label("AI SENTIMENT ANALYSIS");
        titleLabel.setStyle("-fx-font-weight: 900; -fx-font-size: 10px; -fx-text-fill: " + color + "; -fx-letter-spacing: 1px;");
        Label sentimentLabel = new Label(result.sentiment.toUpperCase());
        sentimentLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        titleBox.getChildren().addAll(titleLabel, sentimentLabel);
        
        header.getChildren().addAll(aiIcon, titleBox);

        double confidenceVal = 0.5;
        try { confidenceVal = Double.parseDouble(result.confidence.replace("%", "")) / 100.0; } catch(Exception e){}
        
        HBox confidenceBox = new HBox(10);
        confidenceBox.setAlignment(Pos.CENTER_LEFT);
        ProgressBar pb = new ProgressBar(confidenceVal);
        pb.setPrefWidth(150);
        pb.setPrefHeight(6);
        pb.setStyle("-fx-accent: " + color + ";");
        Label confLabel = new Label(result.confidence + " confidence");
        confLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.5);");
        confidenceBox.getChildren().addAll(pb, confLabel);

        Text explanation = new Text(result.explanation);
        explanation.setFill(Color.web("rgba(255,255,255,0.7)"));
        explanation.setFont(Font.font(13));
        explanation.setWrappingWidth(700);

        layout.getChildren().addAll(header, confidenceBox, explanation);
        panel.getChildren().setAll(layout);
    }


    private void showCommentSentimentAnalysis(Commentaire comment) {
        VBox panel = commentSentimentPanels.get(comment.getIdCommentaire());
        if (panel == null) return;
        
        if (panel.isVisible()) {
            panel.setVisible(false);
            panel.setManaged(false);
            return;
        }

        panel.getChildren().clear();
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 10px; -fx-border-color: " + tm.getAccentHex() + "22; -fx-border-radius: 10px;");
        
        Label loading = new Label("Analyzing comment...");
        loading.setTextFill(Color.web(tm.getAccentHex()));
        panel.getChildren().add(loading);
        panel.setVisible(true);
        panel.setManaged(true);

        sentimentService.analyzeContent(comment.getContenuCommentaire()).thenAccept(result -> {
            javafx.application.Platform.runLater(() -> {
                if (panel == null) return;
                if (!result.success) {
                    panel.getChildren().setAll(new Label("Analysis Failed: " + result.explanation));
                    return;
                }
                fillSentimentPanel(panel, result);
            });
        });
    }

    // Helper UI Builders
    private void showFace(VBox face) {
        readFace.setVisible(face == readFace);
        createFace.setVisible(face == createFace);
        editFace.setVisible(face == editFace);

        readFace.setManaged(face == readFace);
        createFace.setManaged(face == createFace);
        editFace.setManaged(face == editFace);
    }

    private void showNotification(String title, String message, String colorHex) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(16));
        box.setPrefWidth(320);
        box.setStyle("-fx-background-color: rgba(20,20,30,0.95); -fx-background-radius: 12px; -fx-border-color: "
                + colorHex + "; -fx-border-radius: 12px;");

        Label t = new Label(title);
        t.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        Label m = new Label(message);
        m.setWrapText(true);
        m.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");

        box.getChildren().addAll(t, m);
        notificationBox.getChildren().add(0, box);

        box.setOpacity(0);
        box.setTranslateX(100);

        FadeTransition fi = new FadeTransition(Duration.millis(300), box);
        fi.setToValue(1);
        TranslateTransition ti = new TranslateTransition(Duration.millis(300), box);
        ti.setToX(0);

        PauseTransition p = new PauseTransition(Duration.seconds(4));

        FadeTransition fo = new FadeTransition(Duration.millis(300), box);
        fo.setToValue(0);
        fo.setOnFinished(e -> notificationBox.getChildren().remove(box));

        new SequentialTransition(new SequentialTransition(fi, ti), p, fo).play();
    }

    private String copyFileToUploads(File source) throws IOException {
        String uploadsPath = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadsPath);
        if (!dir.exists())
            dir.mkdirs();

        String name = System.currentTimeMillis() + "_" + source.getName();
        File dest = new File(dir, name);
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return name;
    }

    private File chooseImage() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        return fc.showOpenDialog(root.getScene().getWindow());
    }

    private void updateImagePreview(List<File> files, FlowPane pane) {
        pane.getChildren().clear();
        for (File f : files) {
            ImageView iv = new ImageView(new Image(f.toURI().toString(), 100, 100, true, true));
            iv.setStyle("-fx-background-radius: 8px; -fx-border-radius: 8px;");
            pane.getChildren().add(iv);
        }
    }

    private void tuneInput(Node n) {
        n.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: rgba(255,255,255,0.4);" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-radius: 10px;");
        n.focusedProperty().addListener((obs, oldV, newV) -> {
            if (newV)
                n.setStyle(n.getStyle() + "-fx-border-color: " + tm.getAccentHex() + ";");
            else
                n.setStyle(n.getStyle() + "-fx-border-color: rgba(255,255,255,0.1);");
        });
    }

    private Button primaryButton(String text, Runnable action) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: " + tm.getAccentHex() + ";" +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-padding: 10 20 10 20; -fx-cursor: hand;");
        b.setOnAction(e -> action.run());
        return b;
    }

    private Button ghostButton(String text, Runnable action) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-padding: 10 20 10 20; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 10px;");
        b.setOnAction(e -> action.run());
        return b;
    }

    private void styleReactionButton(Button b) {
        b.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-border-color: rgba(255,255,255,0.12);" +
                        "-fx-text-fill: rgba(255,255,255,0.7);" +
                        "-fx-font-size: 12px; -fx-font-weight: 600; -fx-background-radius: 12px; -fx-border-radius: 12px; -fx-padding: 8 16 8 16; -fx-cursor: hand;");
    }

    private HBox reactionCountWrap(Button b, Label l) {
        HBox h = new HBox(6, b, l);
        h.setAlignment(Pos.CENTER_LEFT);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: rgba(255,255,255,0.6);");
        return h;
    }

    private String categoryStyle(String cat) {
        String color = "#6366f1";
        if ("Announcement".equals(cat))
            color = "#f59e0b";
        else if ("Suggestion".equals(cat))
            color = "#22c55e";

        return "-fx-background-color: " + tm.toRgba(color, 0.15) + ";" +
                "-fx-text-fill: " + color + ";" +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 10 4 10; -fx-background-radius: 6px;";
    }

    private void styleValidationLabel(Label l, String text, boolean valid) {
        l.setText(text);
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (valid ? "#22c55e" : "#ef4444") + ";");
    }

    private void updateCreateValidationState() {
        styleValidationLabel(createTitleValidation, "Title must be at least 5 chars", createTitle.getText().length() >= 5);
        styleValidationLabel(createDescriptionValidation, "Description must be at least 10 chars",
                createDescription.getText().length() >= 10);
    }

    private void updateEditValidationState() {
        styleValidationLabel(editTitleValidation, "Title must be at least 5 chars", editTitle.getText().length() >= 5);
        styleValidationLabel(editDescriptionValidation, "Description must be at least 10 chars",
                editDescription.getText().length() >= 10);
    }

    private void updateCommentValidationState() {
        if (commentInput.getText().trim().isEmpty()) {
            styleValidationLabel(commentValidation, "Comment is empty", false);
        } else {
            styleValidationLabel(commentValidation, "Looks good", true);
        }
    }

    private void preparePreview(ImageView iv, double w, double h) {
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(true);
        iv.setStyle("-fx-background-radius: 12px; -fx-border-radius: 12px;");
    }

    private Label dimLabel(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.4);");
        return l;
    }

    private HBox buildAttachmentZone(Runnable add, Runnable clear, Label status) {
        HBox zone = new HBox(10);
        zone.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = ghostButton("📎 Add Image", add);
        Button clearBtn = ghostButton("Clear", clear);
        zone.getChildren().addAll(addBtn, clearBtn, status);
        return zone;
    }

    private HBox buildMultiImageAttachmentZone(Runnable add, Label status, FlowPane previews, List<File> files) {
        HBox zone = new HBox(10);
        zone.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = ghostButton("📎 Attach Images", add);
        Button clearBtn = ghostButton("Clear All", () -> {
            files.clear();
            previews.getChildren().clear();
            status.setText("No images selected");
        });
        zone.getChildren().addAll(addBtn, clearBtn, status);
        return zone;
    }

    private Circle createAvatar(double radius, User user) {
        Circle circle = new Circle(radius);
        circle.setFill(Color.web("rgba(255,255,255,0.1)"));
        circle.setStroke(Color.web("rgba(255,255,255,0.05)"));
        circle.setStrokeWidth(1);

        if (user == null) return circle;

        Profile p = profileByUserIdCache.computeIfAbsent(user.getIdUser(), id -> profiles.profileByUserId(id).orElse(null));
        if (p != null && p.getAvatar() != null && !p.getAvatar().isBlank() && !"-".equals(p.getAvatar())) {
            Image img = com.syndicati.utils.image.ImageLoaderUtil.loadProfileAvatar(p.getAvatar());
            if (img != null) {
                if (img.isError()) {
                    // Skip if error
                } else if (img.getProgress() < 1.0) {
                    img.progressProperty().addListener((obs, oldV, newV) -> {
                        if (newV.doubleValue() >= 1.0 && !img.isError()) {
                            javafx.application.Platform.runLater(() -> {
                                try {
                                    circle.setFill(new ImagePattern(img));
                                } catch (Exception ignored) {}
                            });
                        }
                    });
                } else {
                    circle.setFill(new ImagePattern(img));
                }
            }
        }
        return circle;
    }
}
