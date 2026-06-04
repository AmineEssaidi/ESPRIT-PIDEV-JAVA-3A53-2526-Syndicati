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
import com.syndicati.services.DatabaseService;
import com.syndicati.services.events.DataUpdateBus;
import com.syndicati.services.forum.OpenAIModerationService;
import com.syndicati.services.forum.SentimentAnalysisService;
import com.syndicati.services.forum.SentimentAnalysisService.SentimentResult;
import com.syndicati.services.forum.DiscordWebhookService;
import com.syndicati.services.forum.ReactionService.ReactionActionResult;
import com.syndicati.services.forum.ReactionService.ReactionPayload;
import com.syndicati.services.forum.ReactionService.ReactionStatus;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;
import com.syndicati.utils.image.ImageLoaderUtil;
import com.syndicati.utils.image.imagekit.ImageKitConfig;
import com.syndicati.utils.image.imagekit.ImageKitStorageService;
import com.syndicati.utils.image.imagekit.ImageKitUploadResult;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.ui.HorizonDesignSystem;
import javafx.application.Platform;
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
import java.util.concurrent.ConcurrentHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            "Announcement", "Suggestion", "Jeux Video", "Informatique", "Nouveaute", "Discussion General", "Culture",
            "Sport");
    private static final List<String> MODERATOR_ROLES = List.of("OWNER", "ADMIN", "SUPERADMIN", "SYNDIC");
    private static final List<String> EMOJIS = List.of(
            "\u2764", "\uD83D\uDE02", "\uD83D\uDE2E", "\uD83D\uDE22",
            "\uD83D\uDE21", "\uD83D\uDC4D", "\uD83D\uDD25", "\u2728");

    private final StackPane root = new StackPane();
    private final VBox mainLayout = new VBox(0);
    private final VBox notificationBox = new VBox(10);
    private final ThemeManager tm = ThemeManager.getInstance();
    private final SessionManager session = SessionManager.getInstance();
    private final DatabaseService db = DatabaseService.getInstance();
    private final DataUpdateBus updates = DataUpdateBus.getInstance();
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
    private final Label heroAuthorLabel = new Label("Horizon Community");
    private final Text heroAuthorInitial = new Text("H");
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

    private static final String IK_FOLDER_FORUM_IMAGES = "/syndicati/forum_images";
    private static final String IK_FOLDER_COMMENT_IMAGES = "/syndicati/commentaire_images";

    private boolean isUrl(String v) {
        return v != null && (v.startsWith("http://") || v.startsWith("https://"));
    }

    private String filenameFromUrl(String url) {
        if (url == null) return null;
        String u = url.trim();
        int q = u.indexOf('?');
        if (q >= 0) u = u.substring(0, q);
        int lastSlash = u.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < u.length() - 1) return u.substring(lastSlash + 1);
        return u;
    }

    private Image loadForumImage(String value, String fallbackFolder) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isBlank() || "-".equals(v)) return null;

        // URL case (ImageKit)
        if (isUrl(v)) {
            Image urlImg = ImageLoaderUtil.loadImage(v);
            if (urlImg != null) return urlImg;

            // ImageKit down: fallback to local by filename.
            String filename = filenameFromUrl(v);
            if (filename != null && !filename.isBlank()) {
                Image local = ImageLoaderUtil.loadImage("uploads/" + filename);
                if (local != null) return local;
                if (fallbackFolder != null && !fallbackFolder.isBlank()) {
                    local = ImageLoaderUtil.loadImage("uploads/" + fallbackFolder + "/" + filename);
                    if (local != null) return local;
                }
            }
            return null;
        }

        // Local case: either "uploads/..." or just filename.
        if (v.startsWith("uploads/") || v.startsWith("uploads\\")) {
            return ImageLoaderUtil.loadImage(v);
        }

        Image img = ImageLoaderUtil.loadImage("uploads/" + v);
        if (img != null) return img;

        if (fallbackFolder != null && !fallbackFolder.isBlank()) {
            img = ImageLoaderUtil.loadImage("uploads/" + fallbackFolder + "/" + v);
            if (img != null) return img;
        }
        return null;
    }

    private final Button filterGeneral = new Button("General");
    private final Button filterAnnouncements = new Button("Announcements");

    private Publication current;
    private String currentFilter = "General";

    private AutoCloseable updatesSubscription;
    private volatile long lastListRefreshAt = 0L;
    private final Map<Integer, CommentReactionSnapshot> commentReactionById = new ConcurrentHashMap<>();
    private final Map<Integer, Button> likeBtnByCommentId = new ConcurrentHashMap<>();
    private final Map<Integer, Button> dislikeBtnByCommentId = new ConcurrentHashMap<>();
    private final Map<Integer, Button> emojiBtnByCommentId = new ConcurrentHashMap<>();
    private final Map<Integer, Region> commentLikesBarById = new ConcurrentHashMap<>();
    private final Map<Integer, Region> commentDislikesBarById = new ConcurrentHashMap<>();
    private final Map<Integer, Label> commentRatioTextById = new ConcurrentHashMap<>();
    private volatile long commentsRenderToken = 0L;
    private javafx.animation.Timeline commentRenderTimeline;

    public ForumPageView() {
        mainLayout.setPadding(new Insets(48, 48, 58, 48));
        mainLayout.setStyle("-fx-background-color: transparent;");
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setFillWidth(true);
        mainLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mainLayout.setSpacing(34);

        notificationBox.setPickOnBounds(false);
        notificationBox.setAlignment(Pos.TOP_RIGHT);
        notificationBox.setPadding(new Insets(20));
        notificationBox.setSpacing(10);
        notificationBox.setPrefWidth(400);
        notificationBox.setMaxWidth(Region.USE_PREF_SIZE);

        StackPane forumHero = buildForumHero();
        forumHero.prefWidthProperty().bind(root.widthProperty().subtract(96));
        forumHero.maxWidthProperty().bind(root.widthProperty().subtract(96));

        Node forumShell = buildSplit();
        if (forumShell instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            region.prefWidthProperty().bind(root.widthProperty().subtract(96));
        }
        VBox.setVgrow(forumShell, Priority.ALWAYS);
        mainLayout.getChildren().addAll(forumHero, forumShell);
        root.getChildren().addAll(mainLayout, notificationBox);

        showFace(readFace);
        loadCategory("General");

        updatesSubscription = updates.subscribe(DataUpdateBus.Topic.FORUM, t -> Platform.runLater(() -> {
            db.clearCache("forum:category:" + currentFilter);
            loadCategory(currentFilter);
            if (current != null) {
                refreshPublicationDetail();
            }
        }));
    }

    private StackPane buildForumHero() {
        StackPane hero = new StackPane();
        hero.setMinHeight(360);
        hero.setPrefHeight(440);
        hero.setMaxHeight(520);
        hero.setStyle(
                "-fx-background-color: #020303;" +
                        "-fx-background-radius: 32px;" +
                        "-fx-border-color: rgba(255,255,255,0.08);" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 32px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.58), 58, 0.22, 0, 18);");

        Region ambient = new Region();
        ambient.setStyle(
                "-fx-background-color: linear-gradient(from 48% 0% to 100% 100%, rgba(16,185,129,0.11), rgba(0,0,0,0.0) 48%, rgba(255,107,107,0.04));");
        ambient.prefWidthProperty().bind(hero.widthProperty());
        ambient.prefHeightProperty().bind(hero.heightProperty());

        Label badge = new Label("COMMUNITY HUB");
        badge.setStyle(
                "-fx-background-color: rgba(255,255,255,0.035);" +
                        "-fx-border-color: rgba(255,255,255,0.08);" +
                        "-fx-border-width: 1px;" +
                        "-fx-background-radius: 999px;" +
                        "-fx-border-radius: 999px;" +
                        "-fx-text-fill: rgba(255,255,255,0.90);" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 900;" +
                        "-fx-padding: 12 22 12 22;");

        Text title = new Text("Voices of\nSyndicati.");
        title.setFill(Color.WHITE);
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BLACK, 66));
        title.setLineSpacing(-8);

        Text subtitle = new Text("Join the conversation. Connect with neighbors, share ideas, and help shape our community together.");
        subtitle.setFill(Color.web("rgba(255,255,255,0.56)"));
        subtitle.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.BOLD, 20));
        subtitle.setWrappingWidth(760);

        VBox copy = new VBox(28, badge, title, subtitle);
        copy.setAlignment(Pos.CENTER_LEFT);
        copy.setMaxWidth(820);

        HBox layout = new HBox(copy);
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.setPadding(new Insets(0, 0, 0, 108));
        layout.prefWidthProperty().bind(hero.widthProperty());
        layout.prefHeightProperty().bind(hero.heightProperty());

        hero.getChildren().addAll(ambient, layout);
        return hero;
    }

    @Override
    public StackPane getRoot() {
        return root;
    }

    @Override
    public void cleanup() {
        // Stop any in-flight batched rendering.
        try {
            commentsRenderToken++;
            if (commentRenderTimeline != null) {
                commentRenderTimeline.stop();
                commentRenderTimeline = null;
            }
        } catch (Exception ignored) {}

        if (updatesSubscription != null) {
            try { updatesSubscription.close(); } catch (Exception ignored) {}
            updatesSubscription = null;
        }

        // Release heavy node trees & image references.
        try {
            current = null;
            currentHeroImage = null;
            detailHeroImage.setImage(null);
            createImagePreview.setImage(null);
            editCurrentImagePreview.setImage(null);
            editNewImagePreview.setImage(null);
        } catch (Exception ignored) {}

        try {
            listBox.getChildren().clear();
            listItemsById.clear();
            commentsBox.getChildren().clear();
            commentEditPanels.clear();
            commentReportPanels.clear();
            commentEmojiPanels.clear();
            commentSentimentPanels.clear();
            profileByUserIdCache.clear();
            commentReactionById.clear();
            likeBtnByCommentId.clear();
            dislikeBtnByCommentId.clear();
            emojiBtnByCommentId.clear();
        } catch (Exception ignored) {}

        // Forum has the highest image churn: drop loader cache to help RAM recover.
        try { ImageLoaderUtil.clearCache(); } catch (Exception ignored) {}
    }

    private Node buildSplit() {
        HBox split = new HBox();
        split.setMinWidth(0);
        split.setMaxWidth(Double.MAX_VALUE);
        split.setMinHeight(720);
        split.setPrefHeight(820);
        split.setStyle(
                "-fx-background-color: #030405;" +
                        "-fx-background-radius: 32px;" +
                        "-fx-border-color: rgba(16,185,129,0.18);" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 32px;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.62), 62, 0.24, 0, 20);");

        VBox main = buildMainPanel();
        VBox side = buildSidePanel();
        side.setPrefWidth(390);
        side.setMaxWidth(410);
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

        detailHero.setPrefHeight(340);
        detailHero.setMinHeight(300);
        detailHero.setMaxHeight(360);
        detailHero.setMinWidth(0);
        detailHero.setMaxWidth(Double.MAX_VALUE);
        detailHero.setClip(detailHeroClip);
        detailHero.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #151827, #0b0d14 55%, #020303);" +
                        "-fx-border-color: rgba(255,255,255,0.08);" +
                        "-fx-border-width: 0 0 1px 0;");

        detailHeroClip.setArcWidth(0);
        detailHeroClip.setArcHeight(0);
        detailHeroClip.widthProperty().bind(detailHero.widthProperty());
        detailHeroClip.heightProperty().bind(detailHero.heightProperty());

        detailHeroImage.setSmooth(true);
        detailHeroImage.setPreserveRatio(false);
        detailHeroImage.fitWidthProperty().bind(detailHero.widthProperty());
        detailHeroImage.fitHeightProperty().bind(detailHero.heightProperty());

        heroTitle.setFill(Color.WHITE);
        heroTitle.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BLACK, 34));
        heroMeta.setFill(Color.web("rgba(255,255,255,0.72)"));
        heroMeta.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12));
        heroCategory.setFill(Color.web("rgba(255,255,255,0.95)"));
        heroBody.setFill(Color.web("rgba(255,255,255,0.90)"));
        heroBody.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 14));

        Label categoryPill = new Label();
        categoryPill.textProperty().bind(heroCategory.textProperty());
        categoryPill.setStyle(categoryStyle(heroCategory.getText()));
        heroCategory.textProperty().addListener((obs, oldVal, newVal) -> categoryPill.setStyle(categoryStyle(newVal)));

        HBox metaRow = new HBox(10, categoryPill, heroMeta);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.setMinWidth(0);

        Circle authorAvatar = new Circle(20);
        authorAvatar.setFill(Color.web("rgba(255,255,255,0.12)"));
        authorAvatar.setStroke(Color.web("rgba(255,255,255,0.22)"));
        authorAvatar.setStrokeWidth(1);
        heroAuthorInitial.setFill(Color.WHITE);
        heroAuthorInitial.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BLACK, 14));
        StackPane authorAvatarWrap = new StackPane(authorAvatar, heroAuthorInitial);
        heroAuthorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: 800;");
        HBox authorRow = new HBox(8, authorAvatarWrap, heroAuthorLabel);
        authorRow.setAlignment(Pos.CENTER_LEFT);

        VBox copy = new VBox(12, metaRow, heroTitle, authorRow);
        copy.setAlignment(Pos.BOTTOM_LEFT);
        copy.setMinWidth(0);
        copy.setMaxWidth(Double.MAX_VALUE);
        copy.setPadding(new Insets(0));
        heroTitle.wrappingWidthProperty().bind(copy.widthProperty().subtract(8));

        Region heroFade = new Region();
        heroFade.setStyle(
                "-fx-background-color: linear-gradient(to top, rgba(3,4,5,0.96) 0%, rgba(3,4,5,0.34) 62%, rgba(3,4,5,0.08) 100%);");
        heroFade.prefWidthProperty().bind(detailHero.widthProperty());
        heroFade.prefHeightProperty().bind(detailHero.heightProperty());

        StackPane overlay = new StackPane(copy);
        overlay.setAlignment(Pos.BOTTOM_LEFT);
        overlay.setPadding(new Insets(0, 40, 40, 40));
        overlay.prefWidthProperty().bind(detailHero.widthProperty());
        overlay.prefHeightProperty().bind(detailHero.heightProperty());

        detailHero.getChildren().setAll(detailHeroImage, heroFade, overlay);

        FlowPane reactionCluster = buildPublicationReactionCluster();

        BorderPane actionsBar = new BorderPane();
        actionsBar.setMinWidth(0);
        actionsBar.setPadding(new Insets(12));
        actionsBar.setPrefWidth(Region.USE_COMPUTED_SIZE);
        actionsBar.setMaxWidth(Double.MAX_VALUE);
        actionsBar.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03);" +
                        "-fx-background-radius: 16px;" +
                        "-fx-border-color: rgba(255,255,255,0.10);" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 16px;");

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

        HBox rightActions = new HBox(8, feelingButton, ownerActions, postReportButton);
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

        VBox publicationBody = new VBox(22, heroBody, actionsBar);
        publicationBody.setPadding(new Insets(0));
        publicationBody.setMinWidth(0);
        publicationBody.setMaxWidth(Double.MAX_VALUE);
        publicationBody.setStyle(
                "-fx-background-color: transparent;");
        heroBody.wrappingWidthProperty().bind(publicationBody.widthProperty().subtract(38));

        buildPublicationReportPanel();
        buildPublicationEmojiPanel();
        VBox inlinePanels = new VBox(8, publicationReportPanel, publicationEmojiPanel, publicationSentimentPanel);
        inlinePanels.setPadding(new Insets(0, 0, 0, 0));
        inlinePanels.setFillWidth(true);
        inlinePanels.setMaxWidth(Double.MAX_VALUE);

        VBox commentsSection = buildCommentsSection();
        VBox.setMargin(commentsSection, new Insets(20, 0, 0, 0));
        commentsSection.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(14, publicationBody, inlinePanels, commentsSection);
        content.setPadding(new Insets(40, 40, 40, 40));
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
        content.prefWidthProperty().bind(scroll.widthProperty().subtract(80));
        content.setMaxWidth(Double.MAX_VALUE);

        readFace.getChildren().addAll(detailHero, scroll);
    }

    private FlowPane buildPublicationReactionCluster() {
        styleReactionButton(postLikeButton);
        styleReactionButton(postDislikeButton);
        styleForumEmojiButton(postEmojiButton, false, false);
        styleReactionButton(postBookmarkButton);
        styleReactionButton(postReportButton);

        postLikeButton.setText("Like");
        postDislikeButton.setText("Dislike");
        postEmojiButton.setText("React");
        postBookmarkButton.setText("Bookmark");
        postReportButton.setText("Report");

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
        card.setStyle(HorizonDesignSystem.webServiceCard(16, false));

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
        styleComboBox(createCategory);
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
        card.setStyle(HorizonDesignSystem.webServiceCard(16, false));

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
        styleComboBox(editCategory);
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
        wrap.setPadding(new Insets(20));
        wrap.setFillWidth(true);
        wrap.setMaxWidth(Double.MAX_VALUE);
        wrap.setStyle(
                "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, rgba(255,255,255,0.04), rgba(255,107,107,0.03));" +
                        "-fx-border-color: rgba(255,255,255,0.12);" +
                        "-fx-border-radius: 18px; -fx-background-radius: 18px;");

        Text heading = new Text("Discussion");
        heading.setFill(Color.WHITE);
        heading.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 18));

        Label helper = new Label("Reply, react, or use Feeling to read the room.");
        helper.setStyle("-fx-text-fill: rgba(255,255,255,0.50); -fx-font-size: 12px;");
        Region headingSpacer = new Region();
        HBox.setHgrow(headingSpacer, Priority.ALWAYS);
        HBox headingRow = new HBox(10, heading, headingSpacer, helper);
        headingRow.setAlignment(Pos.CENTER_LEFT);

        commentInput.setPromptText("Write a thoughtful reply...");
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
        send.setMinHeight(42);

        VBox composerCard = new VBox(10, commentInput, commentValidation, imageRow, commentImagePreviews, toggleIsland, send);
        composerCard.setPadding(new Insets(14));
        composerCard.setStyle(
                "-fx-background-color: rgba(0,0,0,0.20);" +
                        "-fx-background-radius: 16px;" +
                        "-fx-border-color: rgba(255,255,255,0.08);" +
                        "-fx-border-radius: 16px;");

        commentFormContainer.getChildren().setAll(composerCard);
        commentFormContainer.setFillWidth(true);

        announcementHint.setTextFill(Color.web("rgba(255,255,255,0.5)"));
        announcementHint.setStyle("-fx-font-size: 13px; -fx-font-style: italic; -fx-padding: 10 0 0 0;");
        announcementHint.setManaged(false);
        announcementHint.setVisible(false);

        wrap.getChildren().addAll(headingRow, commentsBox, commentFormContainer, announcementHint);
        return wrap;
    }

    private VBox buildSidePanel() {
        VBox side = new VBox(0);
        side.setPadding(new Insets(0));
        side.setStyle(
                "-fx-background-color: rgba(0,0,0,0.36);" +
                        "-fx-background-radius: 0 32px 32px 0;");
        side.setPrefWidth(390);
        side.setMinWidth(360);
        side.setMaxWidth(410);

        Text title = new Text("Discussions");
        title.setFill(Color.WHITE);
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 20));

        HBox topActions = new HBox(8, filterGeneral, filterAnnouncements);
        topActions.setPadding(new Insets(0));
        topActions.setAlignment(Pos.CENTER_LEFT);

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

        Button createSwitcher = primaryButton("+ New Post", () -> openCreateFace(false));
        createSwitcher.setMaxWidth(Double.MAX_VALUE);
        createSwitcher.setMinHeight(44);

        VBox commandDeck = new VBox(18, title, createSwitcher, topActions);
        commandDeck.setPadding(new Insets(18));
        commandDeck.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: rgba(255,255,255,0.10);" +
                        "-fx-border-width: 0 0 1px 0;");

        listBox.setPadding(new Insets(18));
        listBox.setSpacing(14);
        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        side.getChildren().addAll(commandDeck, scroll);
        return side;
    }

    private void styleFilterButton(Button btn, boolean active) {
        btn.setStyle(
                "-fx-background-color: " + (active ? tm.toRgba(tm.getAccentHex(), 0.20) : "rgba(255,255,255,0.055)") + ";" +
                        "-fx-border-color: " + (active ? tm.toRgba(tm.getAccentHex(), 0.45) : "rgba(255,255,255,0.08)") + ";" +
                        "-fx-border-width: 1px;" +
                        "-fx-border-radius: 12px;" +
                        "-fx-text-fill: " + (active ? "white" : "rgba(255,255,255,0.70)") + ";" +
                        "-fx-font-size: 12px; -fx-font-weight: 800; -fx-background-radius: 12px; -fx-padding: 10 16 10 16;");
    }

    private void loadCategory(String category) {
        // 1) Render instantly from warm cache when available.
        String cacheKey = "forum:category:" + category;
        @SuppressWarnings("unchecked")
        List<Publication> cached = db.getCache(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            renderCategoryList(cached);
        } else {
            // Clear UI immediately for responsiveness.
            listBox.getChildren().clear();
            listItemsById.clear();
            listBox.getChildren().add(sideRailState("Loading discussions", "Fetching the latest community posts..."));
        }

        // 2) Refresh in background and update UI + cache.
        Thread.startVirtualThread(() -> {
            try {
                List<Publication> pubs = publications.publicationsByCategory(category);
                db.putCache(cacheKey, pubs);
                javafx.application.Platform.runLater(() -> {
                    // Only apply if still on the same filter.
                    if (category != null && category.equals(currentFilter)) {
                        renderCategoryList(pubs);
                    }
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    listBox.getChildren().clear();
                    listBox.getChildren().add(sideRailState("Could not load posts", "Check your connection and try again."));
                });
            }
        });
    }

    private void renderCategoryList(List<Publication> pubs) {
        listBox.getChildren().clear();
        listItemsById.clear();
        if (pubs == null || pubs.isEmpty()) {
            listBox.getChildren().add(sideRailState("No discussions yet", "Create the first post for this category."));
            return;
        }
        for (Publication p : pubs) {
            if (p == null) continue;
            VBox item = buildListItem(p);
            listBox.getChildren().add(item);
            if (p.getIdPublication() != null) {
                listItemsById.put(p.getIdPublication(), item);
            }
        }
        selectPublication(pubs.get(0));
    }

    private VBox sideRailState(String title, String message) {
        VBox state = new VBox(8);
        state.setAlignment(Pos.CENTER_LEFT);
        state.setPadding(new Insets(18));
        state.setMinHeight(128);
        state.setStyle(
                "-fx-background-color: rgba(255,255,255,0.035);" +
                        "-fx-background-radius: 16px;" +
                        "-fx-border-color: rgba(255,255,255,0.08);" +
                        "-fx-border-radius: 16px;");

        Label icon = new Label("\u2726");
        icon.setStyle("-fx-text-fill: " + tm.getAccentHex() + "; -fx-font-size: 18px; -fx-font-weight: 900;");
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 900;");
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.58); -fx-font-size: 11px;");
        state.getChildren().addAll(icon, titleLabel, messageLabel);
        return state;
    }

    private VBox buildListItem(Publication p) {
        VBox item = new VBox(6);
        item.setPadding(new Insets(16));
        item.setCursor(Cursor.HAND);
        item.setStyle(forumListItemStyle(false, false));

        item.setOnMouseEntered(e -> item.setStyle(forumListItemStyle(true, false)));
        item.setOnMouseExited(e -> {
            if (current != null && current.getIdPublication().equals(p.getIdPublication())) {
                item.setStyle(forumListItemStyle(false, true));
            } else {
                item.setStyle(forumListItemStyle(false, false));
            }
        });

        item.setOnMouseClicked(e -> selectPublication(p));

        Label cat = new Label(p.getCatPublication().toUpperCase());
        cat.setStyle(
                "-fx-font-size: 9px; -fx-font-weight: 800; -fx-text-fill: " + tm.getAccentHex() + "; -fx-padding: 0 0 4 0;");

        Text title = new Text(p.getTitrePublication());
        title.setFill(Color.WHITE);
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 15));
        title.setWrappingWidth(235);

        String authorName = (p.getUser() != null)
                ? (p.getUser().getFirstName() + " " + p.getUser().getLastName())
                : "Anonymous";
        Label meta = new Label("by " + authorName + " - " + p.getDatePublication().format(SHORT_DATE_FMT));
        meta.setTextFill(Color.web("rgba(255,255,255,0.5)"));
        meta.setStyle("-fx-font-size: 11px; -fx-font-weight: 600;");

        Label snippet = new Label(compactText(p.getDescPublication(), 86));
        snippet.setWrapText(true);
        snippet.setMaxWidth(238);
        snippet.setStyle("-fx-text-fill: rgba(255,255,255,0.44); -fx-font-size: 11px; -fx-padding: 2 0 0 0;");

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Circle avatar = createAvatar(20, p.getUser());
        
        VBox textContent = new VBox(2);
        textContent.getChildren().addAll(cat, title, meta, snippet);
        HBox.setHgrow(textContent, Priority.ALWAYS);
        
        row.getChildren().addAll(avatar, textContent);
        item.getChildren().add(row);
        return item;
    }

    private String compactText(String value, int max) {
        String clean = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        if (clean.isBlank()) return "No description provided.";
        if (clean.length() <= max) return clean;
        return clean.substring(0, Math.max(0, max - 1)).trim() + "...";
    }

    private void selectPublication(Publication p) {
        if (current != null && listItemsById.containsKey(current.getIdPublication())) {
            listItemsById.get(current.getIdPublication()).setStyle(forumListItemStyle(false, false));
        }
        current = p;
        if (listItemsById.containsKey(p.getIdPublication())) {
            listItemsById.get(p.getIdPublication()).setStyle(forumListItemStyle(false, true));
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
        heroAuthorLabel.setText(author);
        heroAuthorInitial.setText(author == null || author.isBlank() ? "H" : author.substring(0, 1).toUpperCase());

        String img = current.getImagePublication();
        if (img != null && !img.isBlank() && !"-".equals(img)) {
            currentHeroImage = loadForumImage(img, "forum_images");
            detailHeroImage.setImage(currentHeroImage);
            detailHeroImage.setVisible(currentHeroImage != null);
            
            if (currentHeroImage != null) {
                currentHeroImage.widthProperty().addListener((obs, o, n) -> refreshHeroViewport());
                currentHeroImage.heightProperty().addListener((obs, o, n) -> refreshHeroViewport());
            }
            
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

        double viewW = detailHeroImage.getFitWidth();
        double viewH = detailHeroImage.getFitHeight();
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

        // Update Emoji Button
        String myEmoji = rs.getReactions().stream()
                .filter(r -> "Emoji".equals(r.getKind()))
                .map(r -> r.getEmoji())
                .findFirst()
                .orElse(null);
        updateEmojiButtonStyle(postEmojiButton, myEmoji);
    }

    private void updateReactionButtonStyle(Button btn, boolean active, String colorHex) {
        if (active) {
            btn.setStyle(
                    "-fx-background-color: " + tm.toRgba(colorHex, 0.18) + ";" +
                            "-fx-border-color: " + colorHex + ";" +
                            "-fx-border-width: 1px;" +
                            "-fx-text-fill: #ffffff;" +
                            "-fx-effect: dropshadow(gaussian, " + tm.toRgba(colorHex, 0.24) + ", 16, 0.25, 0, 5);" +
                            "-fx-font-size: 12px; -fx-font-weight: 800; -fx-background-radius: 12px; -fx-border-radius: 12px; -fx-padding: 8 16 8 16;");
        } else {
            btn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.055);" +
                            "-fx-border-color: rgba(255,255,255,0.16);" +
                            "-fx-border-width: 1px;" +
                            "-fx-text-fill: rgba(255,255,255,0.82);" +
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
            if (emoji.equals("\u2764")) hex = "2764";
            
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
        if (current == null || current.getIdPublication() == null) {
            commentsBox.getChildren().clear();
            return;
        }

        commentsBox.getChildren().setAll(skeletonLine("Loading comments..."));
        commentEditPanels.clear();
        commentReportPanels.clear();
        commentEmojiPanels.clear();

        final Integer pubId = current.getIdPublication();
        final String cacheKey = "forum:comments:pub:" + pubId;

        // 1) Render cached comments immediately when available.
        @SuppressWarnings("unchecked")
        List<Commentaire> cached = db.getCache(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            renderCommentsBatched(pubId, cached);
        }

        // 2) Refresh comments in background and re-render (batched).
        Thread.startVirtualThread(() -> {
            try {
                List<Commentaire> list = comments.commentsByPublication(pubId);
                db.putCache(cacheKey, list);
                javafx.application.Platform.runLater(() -> {
                    // Only apply if the same publication is still selected.
                    if (current != null && pubId.equals(current.getIdPublication())) {
                        renderCommentsBatched(pubId, list);
                    }
                });
            } catch (Exception ignored) {}
        });
    }

    private Node skeletonLine(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("rgba(255,255,255,0.6)"));
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-padding: 8 0 8 0;");
        return l;
    }

    private boolean canSeeComment(Commentaire c, User me, Integer myId) {
        if (c == null) return false;
        if (c.getIsPublic()) return true;
        if (me == null || me.getIdUser() == null) return false;
        if (MODERATOR_ROLES.contains(me.getRoleUser())) return true;
        try {
            if (c.getUser() != null && myId != null && myId.equals(c.getUser().getIdUser())) return true;
            if (current != null && current.getUser() != null && myId != null && myId.equals(current.getUser().getIdUser())) return true;
        } catch (Exception ignored) {}
        return false;
    }

    /**
     * Build comment nodes in small batches so large threads don't freeze the UI.
     */
    private void renderCommentsBatched(Integer pubId, List<Commentaire> list) {
        if (pubId == null) return;
        if (current == null || !pubId.equals(current.getIdPublication())) return;

        commentsBox.getChildren().clear();
        commentEditPanels.clear();
        commentReportPanels.clear();
        commentEmojiPanels.clear();
        commentSentimentPanels.clear();
        likeBtnByCommentId.clear();
        dislikeBtnByCommentId.clear();
        emojiBtnByCommentId.clear();
        commentReactionById.clear();

        User me = session.getCurrentUser();
        Integer myId = me != null ? me.getIdUser() : -1;

        // Filter in-memory first (cheap).
        List<Commentaire> visible = new ArrayList<>();
        if (list != null) {
            for (Commentaire c : list) {
                if (canSeeComment(c, me, myId)) {
                    visible.add(c);
                }
            }
        }

        if (visible.isEmpty()) {
            commentsBox.getChildren().add(skeletonLine("No comments yet."));
            return;
        }

        final int batchSize = 12;
        final int[] idx = {0};

        // Update token so any in-flight batch render stops.
        final long token = ++commentsRenderToken;

        // Prefetch reactions for visible comments in background (batch queries).
        prefetchCommentReactionsAsync(visible, token);

        // Render first batch immediately for perceived speed.
        int first = Math.min(batchSize, visible.size());
        for (int i = 0; i < first; i++) {
            commentsBox.getChildren().add(buildCommentItem(visible.get(i)));
        }
        idx[0] = first;

        if (idx[0] >= visible.size()) return;

        final javafx.animation.Timeline[] timelineRef = new javafx.animation.Timeline[1];
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(16), e -> {
                if (current == null || !pubId.equals(current.getIdPublication())) {
                    // Publication changed, stop rendering.
                    if (timelineRef[0] != null) timelineRef[0].stop();
                    return;
                }
                if (token != commentsRenderToken) {
                    if (timelineRef[0] != null) timelineRef[0].stop();
                    return;
                }

                int end = Math.min(idx[0] + batchSize, visible.size());
                for (int i = idx[0]; i < end; i++) {
                    commentsBox.getChildren().add(buildCommentItem(visible.get(i)));
                }
                idx[0] = end;
                if (idx[0] >= visible.size()) {
                    if (timelineRef[0] != null) timelineRef[0].stop();
                }
            })
        );
        timelineRef[0] = tl;
        tl.setCycleCount(javafx.animation.Animation.INDEFINITE);
        tl.play();
        commentRenderTimeline = tl;
    }

    private Node buildCommentItem(Commentaire c) {
        VBox item = new VBox(8);
        item.setPadding(new Insets(16));
        item.setStyle(forumCommentCardStyle(false));
        item.setOnMouseEntered(e -> item.setStyle(forumCommentCardStyle(true)));
        item.setOnMouseExited(e -> item.setStyle(forumCommentCardStyle(false)));

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
        body.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 13));
        body.wrappingWidthProperty().bind(item.widthProperty().subtract(42));

        VBox content = new VBox(8, body);

        if (c.getImageCommentaire() != null && !c.getImageCommentaire().isBlank() && !"-".equals(c.getImageCommentaire())) {
            String[] imgs = c.getImageCommentaire().split(",");
            FlowPane imgGrid = new FlowPane(8, 8);
            for (String imgPath : imgs) {
                if (imgPath.isBlank()) continue;
                Image loaded = loadForumThumb(imgPath, "commentaire_images", 160, 160);
                if (loaded == null) continue;
                ImageView iv = new ImageView(loaded);
                iv.setFitWidth(160);
                iv.setFitHeight(160);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                iv.setStyle("-fx-background-radius: 8px; -fx-border-radius: 8px;");
                imgGrid.getChildren().add(iv);
            }
            content.getChildren().add(imgGrid);
        }

        Integer cid = c.getIdCommentaire();
        CommentReactionSnapshot snap = (cid != null) ? commentReactionById.get(cid) : null;
        int likes = snap != null ? snap.likes : 0;
        int dislikes = snap != null ? snap.dislikes : 0;

        // Inline Panels (Edit/Report/Emoji)
        VBox editPanel = buildCommentEditPanel(c);
        VBox reportPanel = buildCommentReportPanel(c);
        VBox emojiPanel = buildCommentEmojiPanel(c);
        VBox sentimentPanel = new VBox(8);
        sentimentPanel.setVisible(false);
        sentimentPanel.setManaged(false);
        HBox ratioWrap = buildCommentReactionRatioWrap(cid, likes, dislikes);
        ratioWrap.setPadding(new Insets(2, 0, 0, 0));
        
        commentEditPanels.put(c.getIdCommentaire(), editPanel);
        commentReportPanels.put(c.getIdCommentaire(), reportPanel);
        commentEmojiPanels.put(c.getIdCommentaire(), emojiPanel);
        commentSentimentPanels.put(c.getIdCommentaire(), sentimentPanel);

        // Action Row
        HBox actions = buildCommentActionRow(c);

        item.getChildren().addAll(header, content, ratioWrap, editPanel, reportPanel, emojiPanel, sentimentPanel, actions);
        return item;
    }

    private HBox buildCommentActionRow(Commentaire c) {
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(4, 0, 0, 0));

        Integer cid = c.getIdCommentaire();
        CommentReactionSnapshot snap = (cid != null) ? commentReactionById.get(cid) : null;
        int likes = snap != null ? snap.likes : 0;
        int dislikes = snap != null ? snap.dislikes : 0;
        boolean liked = snap != null && snap.liked;
        boolean disliked = snap != null && snap.disliked;

        Button like = commentActionButton("Like (" + likes + ")", liked, "#22c55e");
        Button dislike = commentActionButton("Dislike (" + dislikes + ")", disliked, "#ef4444");
        Button emoji = new Button("\u2728 React");
        styleForumEmojiButton(emoji, false, true);
        if (cid != null) {
            likeBtnByCommentId.put(cid, like);
            dislikeBtnByCommentId.put(cid, dislike);
            emojiBtnByCommentId.put(cid, emoji);
        }

        updateEmojiButtonStyle(emoji, snap != null ? snap.myEmoji : null);
        
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

    private HBox buildCommentReactionRatioWrap(Integer commentId, int likes, int dislikes) {
        HBox ratioBarContainer = new HBox();
        ratioBarContainer.setPrefSize(96, 6);
        ratioBarContainer.setMaxSize(96, 6);
        ratioBarContainer.setAlignment(Pos.CENTER_LEFT);
        ratioBarContainer.setStyle("-fx-background-radius: 100px; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 100px; -fx-border-width: 1;");

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(96, 6);
        clip.setArcWidth(6);
        clip.setArcHeight(6);
        ratioBarContainer.setClip(clip);

        Region likesBar = new Region();
        likesBar.setStyle("-fx-background-color: #22c55e;");
        likesBar.setPrefHeight(6);

        Region dislikesBar = new Region();
        dislikesBar.setStyle("-fx-background-color: #ef4444;");
        dislikesBar.setPrefHeight(6);

        ratioBarContainer.getChildren().addAll(likesBar, dislikesBar);

        Label ratioText = new Label(formatReactionRatio(likes, dislikes));
        ratioText.setTextFill(Color.web("rgba(255,255,255,0.72)"));
        ratioText.setStyle("-fx-font-size: 11px; -fx-font-weight: 700;");

        if (commentId != null) {
            commentLikesBarById.put(commentId, likesBar);
            commentDislikesBarById.put(commentId, dislikesBar);
            commentRatioTextById.put(commentId, ratioText);
        }

        updateCommentReactionRatioVisuals(commentId, likes, dislikes);
        HBox ratioWrap = new HBox(6, ratioBarContainer, ratioText);
        ratioWrap.setAlignment(Pos.CENTER_LEFT);
        return ratioWrap;
    }

    private static final class CommentReactionSnapshot {
        final int likes;
        final int dislikes;
        final boolean liked;
        final boolean disliked;
        final String myEmoji;
        CommentReactionSnapshot(int likes, int dislikes, boolean liked, boolean disliked, String myEmoji) {
            this.likes = likes;
            this.dislikes = dislikes;
            this.liked = liked;
            this.disliked = disliked;
            this.myEmoji = myEmoji;
        }
    }

    private void prefetchCommentReactionsAsync(List<Commentaire> comments, long token) {
        User me = session.getCurrentUser();
        int userId = me != null && me.getIdUser() != null ? me.getIdUser() : -1;
        if (userId <= 0 || comments == null || comments.isEmpty()) return;

        // Only prefetch for a bounded number of comments to keep queries small.
        List<Integer> ids = new ArrayList<>();
        for (Commentaire c : comments) {
            if (c != null && c.getIdCommentaire() != null) ids.add(c.getIdCommentaire());
            if (ids.size() >= 150) break;
        }
        if (ids.isEmpty()) return;

        Thread.startVirtualThread(() -> {
            try {
                Map<Integer, Integer> likeCounts = new HashMap<>();
                Map<Integer, Integer> dislikeCounts = new HashMap<>();
                Map<Integer, String> myKind = new HashMap<>();
                Map<Integer, String> myEmoji = new HashMap<>();

                String in = ids.stream().map(x -> "?").reduce((a, b) -> a + "," + b).orElse("?");

                try (Connection conn = db.getConnection()) {
                    if (conn == null) return;

                    // 1) Counts grouped by comment + kind
                String sqlCounts = "SELECT commentaire_id, kind, COUNT(*) AS cnt FROM reaction " +
                        "WHERE commentaire_id IN (" + in + ") AND kind IN ('Like','Dislike') " +
                        "GROUP BY commentaire_id, kind";
                    try (PreparedStatement ps = conn.prepareStatement(sqlCounts)) {
                        int idx = 1;
                        for (Integer id : ids) ps.setInt(idx++, id);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                int cid = rs.getInt(1);
                                String kind = rs.getString(2);
                                int cnt = rs.getInt(3);
                                if ("Like".equals(kind)) likeCounts.put(cid, cnt);
                                if ("Dislike".equals(kind)) dislikeCounts.put(cid, cnt);
                            }
                        }
                    }

                    // 2) My reactions (Like/Dislike/Emoji) for these comments
                    String sqlMine = "SELECT commentaire_id, kind, emoji FROM reaction " +
                        "WHERE user_id = ? AND commentaire_id IN (" + in + ") AND kind IN ('Like','Dislike','Emoji')";
                    try (PreparedStatement ps = conn.prepareStatement(sqlMine)) {
                        int p = 1;
                        ps.setInt(p++, userId);
                        for (Integer id : ids) ps.setInt(p++, id);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                int cid = rs.getInt(1);
                                String kind = rs.getString(2);
                                myKind.put(cid, kind);
                                if ("Emoji".equals(kind)) {
                                    myEmoji.put(cid, rs.getString(3));
                                }
                            }
                        }
                    }
                }

                if (token != commentsRenderToken) return;

                // Update local cache + UI button labels in one FX pass.
                javafx.application.Platform.runLater(() -> {
                    if (token != commentsRenderToken) return;
                    for (Integer id : ids) {
                        int likes = likeCounts.getOrDefault(id, 0);
                        int dislikes = dislikeCounts.getOrDefault(id, 0);
                        String k = myKind.get(id);
                        boolean liked = "Like".equals(k);
                        boolean disliked = "Dislike".equals(k);
                        String emoji = myEmoji.get(id);
                        commentReactionById.put(id, new CommentReactionSnapshot(likes, dislikes, liked, disliked, emoji));

                        Button lb = likeBtnByCommentId.get(id);
                        Button dbb = dislikeBtnByCommentId.get(id);
                        Button eb = emojiBtnByCommentId.get(id);
                        if (lb != null) lb.setText("Like (" + likes + ")");
                        if (dbb != null) dbb.setText("Dislike (" + dislikes + ")");
                        if (eb != null) updateEmojiButtonStyle(eb, emoji);
                        updateCommentReactionRatioVisuals(id, likes, dislikes);
                    }
                });
            } catch (Exception ignored) {}
        });
    }

    private void updateCommentReactionRatioVisuals(Integer commentId, int likes, int dislikes) {
        if (commentId == null) {
            return;
        }

        Region likesBar = commentLikesBarById.get(commentId);
        Region dislikesBar = commentDislikesBarById.get(commentId);
        Label ratioText = commentRatioTextById.get(commentId);
        int total = likes + dislikes;
        double greenWidth = total == 0 ? 0 : 96.0 * likes / total;

        if (likesBar != null && dislikesBar != null) {
            likesBar.setPrefWidth(greenWidth);
            dislikesBar.setPrefWidth(96.0 - greenWidth);
        }

        if (ratioText != null) {
            ratioText.setText(formatReactionRatio(likes, dislikes));
        }
    }

    private String formatReactionRatio(int likes, int dislikes) {
        int total = likes + dislikes;
        if (total <= 0) {
            return "0%";
        }
        int percent = (int) Math.round((likes * 100.0) / total);
        return percent + "%";
    }

    private Image loadForumThumb(String value, String fallbackFolder, double w, double h) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isBlank() || "-".equals(v)) return null;

        // URL case: load as a decoded thumbnail to avoid multi-MB buffers per comment.
        if (isUrl(v)) {
            try {
                return new Image(v, w, h, true, true, true);
            } catch (Exception ignored) {
                return null;
            }
        }

        // Local case: resolve similarly to loadForumImage, but decode as thumbnail.
        String path = v;
        if (!(path.startsWith("uploads/") || path.startsWith("uploads\\"))) {
            if (fallbackFolder != null && !fallbackFolder.isBlank()) {
                path = "uploads/" + fallbackFolder + "/" + path;
            } else {
                path = "uploads/" + path;
            }
        }
        try {
            java.io.File f = new java.io.File(System.getProperty("user.dir"), path.replace("uploads/", "uploads" + java.io.File.separator));
            if (f.exists()) {
                return new Image(f.toURI().toString(), w, h, true, true, true);
            }
            // fallback: let ImageLoaderUtil try anything else
            Image img = ImageLoaderUtil.loadImage(path);
            if (img == null) return null;
            return new Image(img.getUrl(), w, h, true, true, true);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void updateEmojiButtonStyle(Button btn, String emoji) {
        if (emoji != null && !emoji.isBlank()) {
            String hex = Integer.toHexString(emoji.codePointAt(0));
            if (emoji.equals("\u2764")) hex = "2764";
            String url = "https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/" + hex + ".png";
            
            ImageView iv = new ImageView(new Image(url, true));
            iv.setFitWidth(16);
            iv.setFitHeight(16);
            iv.setPreserveRatio(true);
            
            btn.setGraphic(iv);
            btn.setText("Reacted");
            styleForumEmojiButton(btn, true, isCommentEmojiButton(btn));
        } else {
            btn.setGraphic(null);
            btn.setText("\u2728 React");
            styleForumEmojiButton(btn, false, isCommentEmojiButton(btn));
        }
    }

    private boolean isCommentEmojiButton(Button btn) {
        return btn.getProperties().containsKey("forum-comment-emoji");
    }

    private void styleForumEmojiButton(Button btn, boolean active, boolean compact) {
        btn.getProperties().put("forum-comment-emoji", compact);
        btn.setCursor(Cursor.HAND);
        btn.setMinHeight(compact ? 26 : 38);
        btn.setPrefHeight(compact ? 26 : 38);
        btn.setMinWidth(compact ? 78 : 96);
        btn.setPadding(compact ? new Insets(5, 10, 5, 10) : new Insets(8, 16, 8, 16));
        btn.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, compact ? 11 : 12));
        applyForumEmojiButtonStyle(btn, active, false, compact);
        btn.setOnMouseEntered(e -> applyForumEmojiButtonStyle(btn, active, true, compact));
        btn.setOnMouseExited(e -> applyForumEmojiButtonStyle(btn, active, false, compact));
        HorizonDesignSystem.installButtonMotion(btn);
    }

    private void applyForumEmojiButtonStyle(Button btn, boolean active, boolean hover, boolean compact) {
        String background = active
                ? "linear-gradient(from 0% 0% to 100% 100%, " + tm.toRgba(tm.getAccentHex(), hover ? 0.32 : 0.24) + ", rgba(255,107,215," + (hover ? "0.24" : "0.16") + "))"
                : "linear-gradient(from 0% 0% to 100% 100%, rgba(255,255,255," + (hover ? "0.105" : "0.065") + "), rgba(255,107,107," + (hover ? "0.13" : "0.08") + "))";
        String border = active ? tm.toRgba(tm.getAccentHex(), hover ? 0.72 : 0.55) : "rgba(255,107,107," + (hover ? "0.52" : "0.32") + ")";
        String shadow = hover || active ? "-fx-effect: dropshadow(gaussian, rgba(255,107,107,0.22), 18, 0.32, 0, 5);" : "-fx-effect: none;";
        int radius = compact ? 11 : 14;
        btn.setStyle(
                "-fx-background-color: " + background + ";" +
                        "-fx-text-fill: rgba(255,255,255,0.92);" +
                        "-fx-background-radius: " + radius + "px;" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-radius: " + radius + "px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-padding: " + (compact ? "5 10 5 10" : "8 16 8 16") + ";" +
                        "-fx-cursor: hand;" +
                        shadow);
    }

    private Button commentActionButton(String text, boolean active, String colorHex) {
        Button btn = new Button(text);
        if (active) {
            btn.setStyle(
                    "-fx-background-color: " + tm.toRgba(colorHex, 0.16) + "; -fx-border-color: " + tm.toRgba(colorHex, 0.55)
                            + "; -fx-border-width: 1px; -fx-border-radius: 11px; -fx-background-radius: 11px; -fx-text-fill: #ffffff"
                            + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
        } else {
            btn.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.055); -fx-border-color: rgba(255,255,255,0.14);"
                            + " -fx-border-width: 1px; -fx-border-radius: 11px; -fx-background-radius: 11px;"
                            + " -fx-text-fill: rgba(255,255,255,0.72); -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
        }
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + tm.toRgba(colorHex, 0.16) + "; -fx-border-color: " + tm.toRgba(colorHex, 0.55)
                        + "; -fx-border-width: 1px; -fx-border-radius: 11px; -fx-background-radius: 11px; -fx-text-fill: #ffffff"
                        + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> {
            if (!active)
                btn.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.055); -fx-border-color: rgba(255,255,255,0.14);"
                                + " -fx-border-width: 1px; -fx-border-radius: 11px; -fx-background-radius: 11px;"
                                + " -fx-text-fill: rgba(255,255,255,0.72); -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 12 6 12; -fx-cursor: hand;");
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
            if (emoji.equals("\u2764")) hex = "2764";
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
            showNotification("Validation", "Comment cannot be empty.", "#ef4444");
            commentValidation.setText("Comment cannot be empty");
            commentValidation.setTextFill(Color.web("#ef4444"));
            return;
        }

        // Moderation
        List<String> flaggedCategories = moderationService.checkContent(text);
        if (!flaggedCategories.isEmpty()) {
            GlobalNotificationPillManager.moderationBlocked("Comment", "Your comment contains inappropriate content.", flaggedCategories);
            return;
        }

        // Image upload
        StringBuilder imgPaths = new StringBuilder();
        for (File f : commentImages) {
            try {
                String path = copyFileToUploads(f, IK_FOLDER_COMMENT_IMAGES);
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
        List<String> flaggedCategories = moderationService.checkContent(title + " " + desc);
        if (!flaggedCategories.isEmpty()) {
            GlobalNotificationPillManager.moderationBlocked("Publication", "Inappropriate content detected.", flaggedCategories);
            return;
        }

        String imgPath = "-";
        if (createImage != null) {
            try {
                imgPath = copyFileToUploads(createImage, IK_FOLDER_FORUM_IMAGES);
            } catch (IOException e) {
            }
        }

        Integer id = publications.publicationCreate(title, desc, cat, imgPath, session.getCurrentUser());
        if (id != null && id > 0) {
            // Discord announcement
            if ("Announcement".equalsIgnoreCase(cat)) {
                discordService.sendAnnouncement("NEW ANNOUNCEMENT\n\n**" + title + "**\n" + desc, createImage);
            } else if ("Jeux Video".equalsIgnoreCase(cat)) {
                String author = (session.getCurrentUser() != null)
                        ? (session.getCurrentUser().getFirstName() + " " + session.getCurrentUser().getLastName())
                        : "Anonymous";
                discordService.sendAnnouncement(title, desc, author, (createImage != null) ? createImage.getName() : null, false);
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

        editCurrentImagePreview.setImage(
            (current.getImagePublication() == null || "-".equals(current.getImagePublication()))
                ? null
                : loadForumImage(current.getImagePublication(), "forum_images")
        );
        showFace(editFace);
    }

    private void submitEditPublication() {
        String title = editTitle.getText().trim();
        String desc = editDescription.getText().trim();
        String cat = editCategory.getValue();

        String imgPath = current.getImagePublication();
        if (editImage != null) {
            try {
                imgPath = copyFileToUploads(editImage, IK_FOLDER_FORUM_IMAGES);
            } catch (IOException e) {
            }
        }

        publications.publicationUpdate(current.getIdPublication(), title, desc, cat, imgPath);

        // Discord announcement for Jeux Video updates
        if ("Jeux Video".equalsIgnoreCase(cat)) {
            String author = (session.getCurrentUser() != null)
                    ? (session.getCurrentUser().getFirstName() + " " + session.getCurrentUser().getLastName())
                    : "Anonymous";
            discordService.sendAnnouncement(title, desc, author, (editImage != null) ? editImage.getName() : filenameFromUrl(imgPath), true);
        }

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

        prepareSentimentPanelForLoading(publicationSentimentPanel);

        String textToAnalyze = current.getTitrePublication() + ". " + current.getDescPublication();
        sentimentService.analyzeContent(textToAnalyze).thenAccept(result -> {
            javafx.application.Platform.runLater(() -> {
                if (publicationSentimentPanel == null) return;
                if (!result.success) {
                    Label err = new Label("AI Offline: " + result.explanation);
                    err.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-padding: 20;");
                    publicationSentimentPanel.getChildren().setAll(err);
                    return;
                }
                fillSentimentPanel(publicationSentimentPanel, result);
            });
        });
    }

    private void prepareSentimentPanelForLoading(VBox panel) {
        panel.getChildren().clear();
        panel.setPadding(new Insets(18));
        panel.setVisible(true);
        panel.setManaged(true);
        panel.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, rgba(255,107,107,0.13), rgba(255,255,255,0.035) 44%, rgba(255,107,215,0.09));" +
            "-fx-background-radius: 22px;" +
            "-fx-border-color: rgba(255,255,255,0.13), rgba(255,107,107,0.30);" +
            "-fx-border-radius: 22px;" +
            "-fx-border-width: 1px, 1.4px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.30), 24, 0.28, 0, 12);"
        );

        HBox loadingRow = new HBox(12);
        loadingRow.setAlignment(Pos.CENTER_LEFT);

        StackPane orb = new StackPane();
        orb.setMinSize(48, 48);
        orb.setPrefSize(48, 48);
        Circle glow = new Circle(24, Color.web(tm.toRgba(tm.getAccentHex(), 0.18)));
        Circle core = new Circle(8, Color.web(tm.getAccentHex()));
        orb.getChildren().addAll(glow, core);

        VBox copy = new VBox(4);
        Label title = new Label("Reading the room");
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 15));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label("AI is checking sentiment, confidence, and emotion signals.");
        subtitle.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), FontWeight.NORMAL, 12));
        subtitle.setTextFill(Color.web("rgba(255,255,255,0.62)"));
        copy.getChildren().addAll(title, subtitle);
        HBox.setHgrow(copy, Priority.ALWAYS);

        Label pulse = new Label("\u25CF  \u25CF  \u25CF");
        pulse.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 11));
        pulse.setTextFill(Color.web(tm.getAccentHex()));

        loadingRow.getChildren().addAll(orb, copy, pulse);
        panel.getChildren().add(loadingRow);

        FadeTransition shimmer = new FadeTransition(Duration.millis(760), pulse);
        shimmer.setFromValue(0.35);
        shimmer.setToValue(1);
        shimmer.setAutoReverse(true);
        shimmer.setCycleCount(4);
        shimmer.play();
    }

    private void fillSentimentPanel(VBox panel, SentimentResult result) {
        String baseColor = "#fbbf24"; // Neutral
        String emoji = "\uD83D\uDE10";
        
        String s = result.sentiment.toLowerCase();
        if (s.contains("pos") || s.contains("happy") || s.contains("joy")) {
            baseColor = "#22c55e";
            emoji = "\u2728";
        } else if (s.contains("neg") || s.contains("angry") || s.contains("sad")) {
            baseColor = "#ef4444";
            emoji = "\uD83D\uDCA2";
        }
        
        // Match specific emotions if possible
        String e = result.primaryEmotion.toLowerCase();
        if (e.contains("joy") || e.contains("happy")) emoji = "\uD83D\uDE0A";
        else if (e.contains("angry") || e.contains("rage")) emoji = "\uD83D\uDE21";
        else if (e.contains("sad")) emoji = "\uD83D\uDE22";
        else if (e.contains("surprise")) emoji = "\uD83D\uDE32";
        else if (e.contains("fear")) emoji = "\uD83D\uDE28";

        String visualIcon = sentimentVisualIcon(result.sentiment, result.primaryEmotion);
        int confidence = 0;
        try {
            confidence = Math.max(0, Math.min(100, Integer.parseInt(result.confidence.replace("%", "").trim())));
        } catch (Exception ignored) {
            confidence = 0;
        }

        panel.getChildren().clear();
        panel.setPadding(new Insets(18));
        panel.setStyle(
            "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + tm.toRgba(baseColor, 0.18) + ", rgba(255,255,255,0.035) 48%, rgba(255,107,215,0.08));" +
            "-fx-background-radius: 24px;" +
            "-fx-border-color: rgba(255,255,255,0.12), " + tm.toRgba(baseColor, 0.50) + ";" +
            "-fx-border-radius: 24px;" +
            "-fx-border-width: 1px, 1.5px;" +
            "-fx-effect: dropshadow(gaussian, " + tm.toRgba(baseColor, 0.18) + ", 26, 0.32, 0, 10);"
        );

        HBox layout = new HBox(18);
        layout.setPadding(new Insets(4));
        layout.setAlignment(Pos.CENTER_LEFT);

        // Left Side: Big Emotion Indicator
        StackPane iconStack = new StackPane();
        iconStack.setMinSize(88, 88);
        iconStack.setPrefSize(88, 88);
        Circle glow = new Circle(42);
        glow.setFill(Color.web(baseColor));
        glow.setOpacity(0.16);
        Circle ring = new Circle(31);
        ring.setFill(Color.web(tm.toRgba(baseColor, 0.14)));
        ring.setStroke(Color.web(tm.toRgba(baseColor, 0.55)));
        ring.setStrokeWidth(1.4);
        
        Label emoLabel = new Label(visualIcon);
        emoLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-weight: 900;");
        
        iconStack.getChildren().addAll(glow, ring, emoLabel);

        // Right Side: Details
        VBox details = new VBox(12);
        HBox.setHgrow(details, Priority.ALWAYS);

        HBox eyebrow = new HBox(8);
        eyebrow.setAlignment(Pos.CENTER_LEFT);
        Label aiChip = sentimentChip("AI FEELING", tm.getAccentHex(), "rgba(255,255,255,0.08)");
        Label emotionChip = sentimentChip(result.primaryEmotion.toUpperCase(), baseColor, tm.toRgba(baseColor, 0.13));
        eyebrow.getChildren().addAll(aiChip, emotionChip);

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label sentimentLabel = new Label(result.sentiment.toUpperCase());
        sentimentLabel.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BLACK, 26));
        sentimentLabel.setTextFill(Color.WHITE);
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        Label confidenceBadge = sentimentChip(confidence + "% CONFIDENCE", baseColor, tm.toRgba(baseColor, 0.18));
        
        header.getChildren().addAll(sentimentLabel, headerSpacer, confidenceBadge);

        StackPane meter = new StackPane();
        meter.setMinHeight(10);
        meter.setPrefHeight(10);
        meter.setMaxHeight(10);
        Rectangle track = new Rectangle();
        track.setHeight(10);
        track.setArcWidth(10);
        track.setArcHeight(10);
        track.setFill(Color.web("rgba(255,255,255,0.10)"));
        track.widthProperty().bind(meter.widthProperty());

        Rectangle fill = new Rectangle();
        fill.setHeight(10);
        fill.setArcWidth(10);
        fill.setArcHeight(10);
        fill.setFill(Color.web(baseColor));
        fill.widthProperty().bind(meter.widthProperty().multiply(confidence / 100.0));
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        meter.getChildren().addAll(track, fill);

        Text explanation = new Text(result.explanation);
        explanation.setFill(Color.web("rgba(255,255,255,0.75)"));
        explanation.setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), 13));
        explanation.setWrappingWidth(panel.getWidth() > 0 ? panel.getWidth() - 160 : 560);
        explanation.setLineSpacing(3);

        HBox foot = new HBox(8);
        foot.setAlignment(Pos.CENTER_LEFT);
        foot.getChildren().addAll(
            sentimentChip("SIGNAL " + confidence + "/100", "rgba(255,255,255,0.78)", "rgba(255,255,255,0.055)"),
            sentimentChip("MOOD " + result.primaryEmotion, baseColor, tm.toRgba(baseColor, 0.10))
        );

        details.getChildren().addAll(eyebrow, header, meter, explanation, foot);
        
        layout.getChildren().addAll(iconStack, details);
        panel.getChildren().add(layout);

        // Entrance Animation
        layout.setOpacity(0);
        layout.setTranslateY(10);
        layout.setScaleX(0.985);
        layout.setScaleY(0.985);
        
        FadeTransition ft = new FadeTransition(Duration.millis(360), layout);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(360), layout);
        tt.setToY(0);
        ScaleTransition st = new ScaleTransition(Duration.millis(360), layout);
        st.setToX(1);
        st.setToY(1);
        
        ft.play();
        tt.play();
        st.play();
    }

    private String sentimentVisualIcon(String sentiment, String emotion) {
        String combined = ((sentiment == null ? "" : sentiment) + " " + (emotion == null ? "" : emotion)).toLowerCase();
        if (combined.contains("joy") || combined.contains("happy") || combined.contains("pos")) return "\u2726";
        if (combined.contains("angry") || combined.contains("rage") || combined.contains("neg")) return "\u26A0";
        if (combined.contains("sad")) return "\u25D2";
        if (combined.contains("surprise")) return "\u2737";
        if (combined.contains("fear")) return "\u25C7";
        return "\u25D0";
    }

    private Label sentimentChip(String text, String color, String background) {
        Label chip = new Label(text);
        chip.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BOLD, 10));
        chip.setTextFill(Color.web(color));
        chip.setPadding(new Insets(5, 9, 5, 9));
        chip.setStyle(
            "-fx-background-color: " + background + ";" +
            "-fx-background-radius: 999px;" +
            "-fx-border-color: rgba(255,255,255,0.10);" +
            "-fx-border-radius: 999px;" +
            "-fx-border-width: 1px;"
        );
        return chip;
    }


    private void showCommentSentimentAnalysis(Commentaire comment) {
        VBox panel = commentSentimentPanels.get(comment.getIdCommentaire());
        if (panel == null) return;
        
        if (panel.isVisible()) {
            panel.setVisible(false);
            panel.setManaged(false);
            return;
        }

        prepareSentimentPanelForLoading(panel);

        sentimentService.analyzeContent(comment.getContenuCommentaire()).thenAccept(result -> {
            javafx.application.Platform.runLater(() -> {
                if (panel == null) return;
                if (!result.success) {
                    Label err = new Label("AI Offline: " + result.explanation);
                    err.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-padding: 10;");
                    panel.getChildren().setAll(err);
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
        String normalizedTitle = title == null ? "" : title.toLowerCase();
        String normalizedMessage = message == null ? "" : message.toLowerCase();

        if (normalizedTitle.contains("validation")) {
            GlobalNotificationPillManager.validationIssue(
                title,
                message,
                "Fill every required field.",
                "Use at least 5 characters for titles and 10 for descriptions.",
                "Keep the text clean and retry."
            );
            return;
        }

        if (normalizedTitle.contains("auth required")) {
            GlobalNotificationPillManager.validationIssue(
                title,
                message,
                "Log in to continue.",
                "Then retry the action from the same page."
            );
            return;
        }

        if (normalizedTitle.contains("success") || normalizedMessage.contains("created") || normalizedMessage.contains("updated") || normalizedMessage.contains("posted")) {
            if (normalizedMessage.contains("created") || normalizedMessage.contains("posted")) {
                GlobalNotificationPillManager.created(title, message);
            } else {
                GlobalNotificationPillManager.updated(title, message);
            }
        } else if (normalizedTitle.contains("report") || normalizedTitle.contains("warning") || "#f59e0b".equalsIgnoreCase(colorHex)) {
            GlobalNotificationPillManager.warning(title, message);
        } else if (normalizedTitle.contains("error") || "#ef4444".equalsIgnoreCase(colorHex)) {
            GlobalNotificationPillManager.error(title, message);
        } else {
            GlobalNotificationPillManager.info(title, message);
        }
    }

    private String copyFileToUploads(File source, String imageKitFolder) throws IOException {
        String uploadsPath = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadsPath);
        if (!dir.exists())
            dir.mkdirs();

        String name = System.currentTimeMillis() + "_" + source.getName();
        File dest = new File(dir, name);
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Prefer ImageKit URL for DB, but keep local copy for fallback.
        try {
            ImageKitConfig cfg = ImageKitConfig.fromEnv();
            if (cfg != null && cfg.isEnabled() && cfg.getPrivateKey() != null) {
                ImageKitStorageService svc = new ImageKitStorageService(cfg);
                ImageKitUploadResult res = svc.uploadFile(dest, imageKitFolder);
                if (res != null && res.url() != null && !res.url().isBlank()) {
                    return res.url();
                }
            }
        } catch (Exception e) {
            System.err.println("ImageKit upload failed (forum). Fallback to local: " + e.getMessage());
        }

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
        applyInputStyle(n, false);
        n.focusedProperty().addListener((obs, oldV, newV) -> applyInputStyle(n, newV));
    }

    private void applyInputStyle(Node n, boolean focused) {
        if (n instanceof ComboBox<?> cb) {
            cb.getEditor().setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
        }
        
        String border = focused ? tm.getAccentHex() : "rgba(255,255,255,0.1)";
        n.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                        "-fx-control-inner-background: rgba(255,255,255,0.06);" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: rgba(255,255,255,0.45);" +
                        "-fx-highlight-fill: " + tm.toRgba(tm.getAccentHex(), 0.45) + ";" +
                        "-fx-highlight-text-fill: white;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-color: " + border + ";" +
                        "-fx-border-radius: 10px;");

        // TextArea has nested ".content" that can default to white in some themes.
        if (n instanceof TextArea ta) {
            Runnable styleContent = () -> {
                Node content = ta.lookup(".content");
                if (content != null) {
                    content.setStyle(
                            "-fx-background-color: rgba(255,255,255,0.06);" +
                                    "-fx-background-radius: 9px;"
                    );
                }
            };
            if (ta.getSkin() == null) {
                ta.skinProperty().addListener((o, oldSkin, newSkin) -> {
                    if (newSkin != null) Platform.runLater(styleContent);
                });
            } else {
                Platform.runLater(styleContent);
            }
        }
    }

    private void styleComboBox(ComboBox<String> cb) {
        cb.setButtonCell(new CategoryListCell());
        cb.setCellFactory(lv -> new CategoryListCell());
        
        cb.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: rgba(255,255,255,0.12);" +
            "-fx-border-radius: 10px;" +
            "-fx-padding: 2 6 2 6;"
        );

        cb.focusedProperty().addListener((obs, oldV, newV) -> {
            String border = newV ? tm.getAccentHex() : "rgba(255,255,255,0.12)";
            cb.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                "-fx-background-radius: 10px;" +
                "-fx-border-color: " + border + ";" +
                "-fx-border-radius: 10px;" +
                "-fx-padding: 2 6 2 6;"
            );
        });
    }

    private class CategoryListCell extends javafx.scene.control.ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            // Force the popup background to be dark
            if (getListView() != null) {
                getListView().setStyle(
                    "-fx-background-color: #1a1a24;" +
                    "-fx-background-insets: 0;" +
                    "-fx-padding: 5;" +
                    "-fx-border-color: rgba(255,255,255,0.12);" +
                    "-fx-border-radius: 12px;" +
                    "-fx-background-radius: 12px;"
                );
            }

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                setText(item);
                setTextFill(Color.WHITE);
                setFont(Font.font(MainApplication.getInstance().getLightFontFamily(), 14));
                
                updateCellStyle(isSelected());
                
                selectedProperty().addListener((obs, wasSelected, isSelected) -> updateCellStyle(isSelected));

                setOnMouseEntered(e -> {
                    if (!isSelected()) {
                        setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 8px; -fx-text-fill: white; -fx-padding: 10 14;");
                    }
                });
                setOnMouseExited(e -> {
                    if (!isSelected()) {
                        updateCellStyle(false);
                    }
                });
            }
        }

        private void updateCellStyle(boolean selected) {
            if (selected) {
                setStyle(
                    "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.25) + ";" +
                    "-fx-background-radius: 8px;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 10 14;" +
                    "-fx-font-weight: bold;"
                );
            } else {
                setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: rgba(255,255,255,0.85);" +
                    "-fx-padding: 10 14;" +
                    "-fx-font-weight: normal;"
                );
            }
        }
    }

    private Button primaryButton(String text, Runnable action) {
        Button b = new Button(text);
        b.setStyle(HorizonDesignSystem.buttonPrimary() + "-fx-padding: 10 20 10 20;");
        b.setOnAction(e -> action.run());
        HorizonDesignSystem.installButtonMotion(b);
        return b;
    }

    private String forumListItemStyle(boolean hover, boolean active) {
        if (active) {
            return "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, " + tm.toRgba(tm.getAccentHex(), 0.18) + ", rgba(255,255,255,0.035));" +
                "-fx-background-radius: 18px;" +
                "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.48) + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 18px;" +
                "-fx-effect: dropshadow(gaussian, " + tm.toRgba(tm.getAccentHex(), 0.16) + ", 20, 0.22, 0, 8);";
        }
        return "-fx-background-color: " + (hover ? "rgba(255,255,255,0.075)" : "rgba(255,255,255,0.035)") + ";" +
            "-fx-background-radius: 18px;" +
            "-fx-border-color: " + (hover ? tm.toRgba(tm.getAccentHex(), 0.26) : "rgba(255,255,255,0.07)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 18px;";
    }

    private String forumCommentCardStyle(boolean hover) {
        return "-fx-background-color: " + (hover
                ? "linear-gradient(from 0% 0% to 100% 100%, rgba(255,255,255,0.055), rgba(255,107,107,0.055))"
                : "linear-gradient(from 0% 0% to 100% 100%, rgba(255,255,255,0.028), rgba(255,255,255,0.012))") + ";" +
                "-fx-background-radius: 16px;" +
                "-fx-border-color: " + (hover ? "rgba(255,107,107,0.34)" : "rgba(255,255,255,0.075)") + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 16px;" +
                "-fx-effect: " + (hover
                        ? "dropshadow(gaussian, rgba(255,107,107,0.12), 18, 0.26, 0, 6)"
                        : "none") + ";";
    }

    private Button ghostButton(String text, Runnable action) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: rgba(255,255,255,0.065);" +
                "-fx-border-color: rgba(255,255,255,0.16);" +
                "-fx-border-width: 1px;" +
                "-fx-background-radius: 14px;" +
                "-fx-border-radius: 14px;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: 700;" +
                "-fx-padding: 10 18 10 18;" +
                "-fx-cursor: hand;");
        b.setOnAction(e -> action.run());
        HorizonDesignSystem.installButtonMotion(b);
        return b;
    }

    private void styleReactionButton(Button b) {
        b.setMinHeight(38);
        b.setStyle(
                "-fx-background-color: rgba(255,255,255,0.055);" +
                "-fx-border-color: rgba(255,255,255,0.16);" +
                "-fx-border-width: 1px;" +
                "-fx-background-radius: 12px;" +
                "-fx-border-radius: 12px;" +
                "-fx-text-fill: rgba(255,255,255,0.86);" +
                "-fx-font-size: 12px; -fx-font-weight: 800;" +
                "-fx-padding: 8 16 8 16;");
        HorizonDesignSystem.installButtonMotion(b);
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
        styleValidationLabel(createCategoryValidation, "Category is required", createCategory.getValue() != null);
        styleValidationLabel(createDescriptionValidation, "Description must be at least 10 chars",
                createDescription.getText().length() >= 10);
    }

    private void updateEditValidationState() {
        styleValidationLabel(editTitleValidation, "Title must be at least 5 chars", editTitle.getText().length() >= 5);
        styleValidationLabel(editCategoryValidation, "Category is required", editCategory.getValue() != null);
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
        Button addBtn = ghostButton("Add Image", add);
        Button clearBtn = ghostButton("Clear", clear);
        zone.getChildren().addAll(addBtn, clearBtn, status);
        return zone;
    }

    private HBox buildMultiImageAttachmentZone(Runnable add, Label status, FlowPane previews, List<File> files) {
        HBox zone = new HBox(10);
        zone.setAlignment(Pos.CENTER_LEFT);
        Button addBtn = ghostButton("Attach Images", add);
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

