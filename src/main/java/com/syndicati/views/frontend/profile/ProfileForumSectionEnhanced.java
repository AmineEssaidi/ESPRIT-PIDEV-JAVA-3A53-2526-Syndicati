package com.syndicati.views.frontend.profile;

import com.syndicati.controllers.forum.CommentaireController;
import com.syndicati.controllers.forum.PublicationController;
import com.syndicati.controllers.forum.ReactionController;
import com.syndicati.controllers.user.profile.ProfileController;
import com.syndicati.models.forum.Commentaire;
import com.syndicati.models.forum.Publication;
import com.syndicati.models.forum.Reaction;
import com.syndicati.models.user.Profile;
import com.syndicati.models.user.User;
import com.syndicati.utils.image.ImageLoaderUtil;
import com.syndicati.utils.session.SessionManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

/**
 * Enhanced Forum section with working filters and details panel.
 */
public class ProfileForumSectionEnhanced {

    private final VBox root = new VBox(16);
    private final PublicationController publicationController;
    private final CommentaireController commentaireController;
    private final ReactionController reactionController;
    private final ProfileController profileController;
    private final User currentUser;

    private final StackPane switcherContainer = new StackPane();
    private final VBox faceListView = new VBox(16);
    private final VBox faceDetailsView = new VBox(16);

    private final VBox publicationList = new VBox(10);
    private final VBox commentList = new VBox(10);
    private final VBox reactionList = new VBox(10);
    private final VBox bookmarkList = new VBox(10);

    private final Label detailsTitle = new Label("Details");
    private final VBox detailsContent = new VBox(12);

    private String currentTab = "publications";
    private String currentCategory = "General";
    private String currentFilter = "my";

    private Button publicationsBtn;
    private Button commentsBtn;
    private Button reactionsBtn;
    private Button bookmarksBtn;

    private final List<Publication> allPublications = new ArrayList<>();
    private final List<Commentaire> allComments = new ArrayList<>();
    private final List<Reaction> allReactions = new ArrayList<>();
    private final Map<Integer, Publication> publicationById = new HashMap<>();
    private final Map<Integer, Profile> profileByUserIdCache = new HashMap<>();

    private List<Publication> filteredPublications = new ArrayList<>();
    private List<Commentaire> filteredComments = new ArrayList<>();
    private List<Reaction> filteredReactions = new ArrayList<>();
    private List<Reaction> filteredBookmarks = new ArrayList<>();

    public ProfileForumSectionEnhanced() {
        this.publicationController = new PublicationController();
        this.commentaireController = new CommentaireController();
        this.reactionController = new ReactionController();
        this.profileController = new ProfileController();
        this.currentUser = SessionManager.getInstance().getCurrentUser();

        if (isAdminUser(currentUser)) {
            currentFilter = "all";
        }

        buildLayout();
        refreshDataAndRender();
    }

    private void buildLayout() {
        root.setPadding(new Insets(16, 0, 0, 0));

        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.03); -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-width: 1; -fx-background-radius: 20; -fx-border-radius: 20;");
        card.setPrefHeight(600);

        switcherContainer.setPrefHeight(500);
        VBox.setVgrow(switcherContainer, Priority.ALWAYS);

        buildListView();
        buildDetailsView();

        switcherContainer.getChildren().addAll(faceListView, faceDetailsView);
        faceDetailsView.setVisible(false);
        faceDetailsView.setManaged(false);

        card.getChildren().add(switcherContainer);
        root.getChildren().add(card);
    }

    private void buildListView() {
        faceListView.setStyle("-fx-background-color: transparent;");
        faceListView.setPadding(new Insets(20));

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("📧 Your Forum Activity");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);

        HBox adminFilter = createAdminFilter();
        HBox.setHgrow(title, Priority.ALWAYS);
        header.getChildren().addAll(title, adminFilter);

        faceListView.getChildren().add(header);
        faceListView.getChildren().add(createCategoryFilters());
        faceListView.getChildren().add(createSubTabs());

        ScrollPane contentScroll = new ScrollPane();
        contentScroll.setStyle("-fx-control-inner-background: transparent; -fx-padding: 0;");
        contentScroll.setFitToWidth(true);
        contentScroll.setPrefHeight(420);

        VBox contentContainer = new VBox(10);
        contentContainer.getChildren().addAll(publicationList, commentList, reactionList, bookmarkList);
        contentScroll.setContent(contentContainer);
        VBox.setVgrow(contentScroll, Priority.ALWAYS);

        faceListView.getChildren().add(contentScroll);
    }

    private void buildDetailsView() {
        faceDetailsView.setStyle("-fx-background-color: rgba(20, 20, 25, 0.98);");
        faceDetailsView.setPadding(new Insets(20));

        HBox backHeader = new HBox(12);
        backHeader.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-padding: 8; -fx-font-size: 14; -fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 50%; -fx-border-radius: 50%;");
        backBtn.setOnAction(e -> showListView());

        detailsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        detailsTitle.setTextFill(Color.WHITE);
        detailsTitle.setText("Details");

        backHeader.getChildren().addAll(backBtn, detailsTitle);
        faceDetailsView.getChildren().add(backHeader);

        ScrollPane detailScroll = new ScrollPane();
        detailScroll.setStyle("-fx-control-inner-background: transparent; -fx-padding: 0;");
        detailScroll.setFitToWidth(true);

        detailsContent.setPadding(new Insets(16, 0, 0, 0));
        Label placeholder = new Label("Select an item to view details");
        placeholder.setTextFill(Color.color(1, 1, 1, 0.5));
        detailsContent.getChildren().add(placeholder);

        detailScroll.setContent(detailsContent);
        VBox.setVgrow(detailScroll, Priority.ALWAYS);
        faceDetailsView.getChildren().add(detailScroll);
    }

    private HBox createAdminFilter() {
        HBox filter = new HBox(8);
        filter.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05); -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-width: 1; -fx-padding: 4; -fx-background-radius: 999; -fx-border-radius: 999;");

        if (isAdminUser(currentUser)) {
            Button myOwnBtn = createFilterButton("My Own", "my".equals(currentFilter));
            Button everyoneBtn = createFilterButton("Everyone", "all".equals(currentFilter));
            myOwnBtn.setOnAction(e -> switchFilter("my", myOwnBtn, everyoneBtn));
            everyoneBtn.setOnAction(e -> switchFilter("all", myOwnBtn, everyoneBtn));
            filter.getChildren().addAll(myOwnBtn, everyoneBtn);
        }

        return filter;
    }

    private Button createFilterButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-padding: 6 12 6 12; " +
            "-fx-background-color: " + (active ? "rgba(99, 102, 241, 0.8);" : "transparent;") +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 999; " +
            "-fx-font-size: 11;"
        );
        return btn;
    }

    private void switchFilter(String filterType, Button myOwnBtn, Button everyoneBtn) {
        currentFilter = filterType;
        boolean isMyOwn = "my".equals(filterType);

        myOwnBtn.setStyle(
            "-fx-padding: 6 12 6 12; " +
            "-fx-background-color: " + (isMyOwn ? "rgba(99, 102, 241, 0.8);" : "transparent;") +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-font-size: 11;"
        );
        everyoneBtn.setStyle(
            "-fx-padding: 6 12 6 12; " +
            "-fx-background-color: " + (!isMyOwn ? "rgba(99, 102, 241, 0.8);" : "transparent;") +
            "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999; -fx-font-size: 11;"
        );

        refreshDataAndRender();
    }

    private HBox createCategoryFilters() {
        HBox filters = new HBox(8);
        filters.setAlignment(Pos.CENTER_LEFT);

        Button announcementsBtn = new Button("📢 Announcements");
        styleCategoryButton(announcementsBtn, "Announcement".equals(currentCategory));
        announcementsBtn.setOnAction(e -> {
            currentCategory = "Announcement";
            styleCategoryButton(announcementsBtn, true);
            styleCategoryButton((Button) filters.getChildren().get(1), false);
            refreshDataAndRender();
        });

        Button generalBtn = new Button("💬 General");
        styleCategoryButton(generalBtn, !"Announcement".equals(currentCategory));
        generalBtn.setOnAction(e -> {
            currentCategory = "General";
            styleCategoryButton(announcementsBtn, false);
            styleCategoryButton(generalBtn, true);
            refreshDataAndRender();
        });

        filters.getChildren().addAll(announcementsBtn, generalBtn);
        return filters;
    }

    private void styleCategoryButton(Button btn, boolean active) {
        btn.setStyle(
            "-fx-padding: 6 12 6 12; " +
            "-fx-background-color: " + (active ? "rgba(99, 102, 241, 0.2);" : "rgba(255,255,255,0.05);") +
            "-fx-text-fill: " + (active ? "white;" : "rgba(255,255,255,0.7);") +
            "-fx-border-color: " + (active ? "rgba(99, 102, 241, 0.3);" : "rgba(255,255,255,0.1);") +
            "-fx-border-width: 1; -fx-background-radius: 999; -fx-border-radius: 999; -fx-font-size: 11;" +
            (active ? "-fx-font-weight: bold;" : "")
        );
    }

    private HBox createSubTabs() {
        HBox tabs = new HBox(8);
        tabs.setPadding(new Insets(6));
        tabs.setStyle("-fx-background-color: rgba(255, 255, 255, 0.04); -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-width: 1; -fx-background-radius: 999; -fx-border-radius: 999;");
        tabs.setAlignment(Pos.CENTER);

        publicationsBtn = createSubTabButton("📝 Publications", true);
        commentsBtn = createSubTabButton("💬 Comments", false);
        reactionsBtn = createSubTabButton("👍 Reactions", false);
        bookmarksBtn = createSubTabButton("🔖 Bookmarks", false);

        publicationsBtn.setOnAction(e -> switchTab("publications"));
        commentsBtn.setOnAction(e -> switchTab("comments"));
        reactionsBtn.setOnAction(e -> switchTab("reactions"));
        bookmarksBtn.setOnAction(e -> switchTab("bookmarks"));

        tabs.getChildren().addAll(publicationsBtn, commentsBtn, reactionsBtn, bookmarksBtn);
        return tabs;
    }

    private Button createSubTabButton(String text, boolean active) {
        Button btn = new Button(text);
        styleSubTabButton(btn, active);
        return btn;
    }

    private void styleSubTabButton(Button btn, boolean active) {
        btn.setStyle(
            "-fx-padding: 8 14 8 14; " +
            "-fx-background-color: " + (active ? "rgba(99, 102, 241, 0.2);" : "transparent;") +
            "-fx-text-fill: " + (active ? "white;" : "rgba(255, 255, 255, 0.6);") +
            "-fx-border-color: " + (active ? "rgba(99, 102, 241, 0.3);" : "transparent;") +
            "-fx-border-width: 1; -fx-background-radius: 8; -fx-border-radius: 8; " +
            (active ? "-fx-font-weight: bold;" : "") +
            "-fx-font-size: 11;"
        );
    }

    private void switchTab(String tabName) {
        currentTab = tabName;

        styleSubTabButton(publicationsBtn, "publications".equals(tabName));
        styleSubTabButton(commentsBtn, "comments".equals(tabName));
        styleSubTabButton(reactionsBtn, "reactions".equals(tabName));
        styleSubTabButton(bookmarksBtn, "bookmarks".equals(tabName));

        publicationList.setVisible("publications".equals(tabName));
        publicationList.setManaged("publications".equals(tabName));

        commentList.setVisible("comments".equals(tabName));
        commentList.setManaged("comments".equals(tabName));

        reactionList.setVisible("reactions".equals(tabName));
        reactionList.setManaged("reactions".equals(tabName));

        bookmarkList.setVisible("bookmarks".equals(tabName));
        bookmarkList.setManaged("bookmarks".equals(tabName));
    }

    private void refreshDataAndRender() {
        allPublications.clear();
        allComments.clear();
        allReactions.clear();
        publicationById.clear();

        List<Publication> pubs = publicationController.publications();
        if (pubs != null) {
            allPublications.addAll(pubs);
        }
        for (Publication pub : allPublications) {
            if (pub.getIdPublication() != null) {
                publicationById.put(pub.getIdPublication(), pub);
            }
        }

        List<Commentaire> comments = commentaireController.commentaires();
        if (comments != null) {
            allComments.addAll(comments);
        }

        List<Reaction> reactions = reactionController.reactions();
        if (reactions != null) {
            allReactions.addAll(reactions);
        }

        filteredPublications = filterPublications(allPublications);
        filteredComments = filterComments(allComments);
        filteredReactions = filterReactions(allReactions, false);
        filteredBookmarks = filterReactions(allReactions, true);

        renderPublications();
        renderComments();
        renderReactions();
        renderBookmarks();
        updateTabLabels();
        switchTab(currentTab);
    }

    private List<Publication> filterPublications(List<Publication> source) {
        List<Publication> out = new ArrayList<>();
        for (Publication pub : source) {
            if (pub == null) {
                continue;
            }
            if (!matchesUserFilter(pub.getUser())) {
                continue;
            }
            if (!matchesCategory(pub.getCategoriePub())) {
                continue;
            }
            out.add(pub);
        }
        return out;
    }

    private List<Commentaire> filterComments(List<Commentaire> source) {
        List<Commentaire> out = new ArrayList<>();
        for (Commentaire comment : source) {
            if (comment == null) {
                continue;
            }
            if (!matchesUserFilter(comment.getUser())) {
                continue;
            }
            Publication publication = resolvePublication(comment.getPublication());
            if (!matchesCategory(publication == null ? null : publication.getCategoriePub())) {
                continue;
            }
            out.add(comment);
        }
        return out;
    }

    private List<Reaction> filterReactions(List<Reaction> source, boolean bookmarksOnly) {
        List<Reaction> out = new ArrayList<>();
        for (Reaction reaction : source) {
            if (reaction == null) {
                continue;
            }
            if (bookmarksOnly && !"Bookmark".equalsIgnoreCase(reaction.getKind())) {
                continue;
            }
            if (!bookmarksOnly && "Bookmark".equalsIgnoreCase(reaction.getKind())) {
                continue;
            }

            if (!matchesUserFilter(reaction.getUser())) {
                continue;
            }

            Publication publication = resolvePublication(reaction.getPublication());
            if (publication == null && reaction.getCommentaire() != null) {
                publication = resolvePublication(reaction.getCommentaire().getPublication());
            }

            if (!matchesCategory(publication == null ? null : publication.getCategoriePub())) {
                continue;
            }
            out.add(reaction);
        }
        return out;
    }

    private boolean matchesUserFilter(User owner) {
        if (!"my".equals(currentFilter)) {
            return true;
        }
        if (currentUser == null || currentUser.getIdUser() == null || owner == null || owner.getIdUser() == null) {
            return false;
        }
        return currentUser.getIdUser().equals(owner.getIdUser());
    }

    private boolean matchesCategory(String category) {
        boolean announcement = "Announcement".equalsIgnoreCase(category);
        if ("Announcement".equals(currentCategory)) {
            return announcement;
        }
        return !announcement;
    }

    private Publication resolvePublication(Publication publication) {
        if (publication == null) {
            return null;
        }
        if (publication.getIdPublication() == null) {
            return publication;
        }
        return publicationById.getOrDefault(publication.getIdPublication(), publication);
    }

    private void renderPublications() {
        publicationList.getChildren().clear();
        if (filteredPublications.isEmpty()) {
            publicationList.getChildren().add(emptyText("No publications found for this filter."));
            return;
        }

        for (Publication pub : filteredPublications) {
            publicationList.getChildren().add(createPublicationItem(pub));
        }
    }

    private void renderComments() {
        commentList.getChildren().clear();
        if (filteredComments.isEmpty()) {
            commentList.getChildren().add(emptyText("No comments found for this filter."));
            return;
        }

        for (Commentaire comment : filteredComments) {
            commentList.getChildren().add(createCommentItem(comment));
        }
    }

    private void renderReactions() {
        reactionList.getChildren().clear();
        if (filteredReactions.isEmpty()) {
            reactionList.getChildren().add(emptyText("No reactions found for this filter."));
            return;
        }

        for (Reaction reaction : filteredReactions) {
            reactionList.getChildren().add(createReactionItem(reaction));
        }
    }

    private void renderBookmarks() {
        bookmarkList.getChildren().clear();
        if (filteredBookmarks.isEmpty()) {
            bookmarkList.getChildren().add(emptyText("No bookmarks found for this filter."));
            return;
        }

        for (Reaction bookmark : filteredBookmarks) {
            bookmarkList.getChildren().add(createBookmarkItem(bookmark));
        }
    }

    private void updateTabLabels() {
        publicationsBtn.setText("📝 Publications (" + filteredPublications.size() + ")");
        commentsBtn.setText("💬 Comments (" + filteredComments.size() + ")");
        reactionsBtn.setText("👍 Reactions (" + filteredReactions.size() + ")");
        bookmarksBtn.setText("🔖 Bookmarks (" + filteredBookmarks.size() + ")");
    }

    private Label emptyText(String message) {
        Label noData = new Label(message);
        noData.setTextFill(Color.color(1, 1, 1, 0.5));
        return noData;
    }

    private VBox createPublicationItem(Publication publication) {
        VBox item = clickableRow(() -> showPublicationDetails(publication));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("📝");
        icon.setFont(Font.font(16));

        VBox titleSection = new VBox(4);
        Label title = new Label(safe(publication.getTitrePub(), "Untitled publication"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        title.setTextFill(Color.WHITE);

        Label date = new Label("📅 " + formatDate(publication.getDateCreationPub()));
        date.setFont(Font.font("Segoe UI", 10));
        date.setTextFill(Color.color(1, 1, 1, 0.5));

        titleSection.getChildren().addAll(title, date);
        HBox.setHgrow(titleSection, Priority.ALWAYS);

        Label category = new Label(safe(publication.getCategoriePub(), "General"));
        category.setFont(Font.font("Segoe UI", 9));
        category.setStyle("-fx-text-fill: rgba(99, 102, 241, 0.9); -fx-background-color: rgba(99, 102, 241, 0.1); -fx-padding: 4 8 4 8; -fx-background-radius: 6;");

        Label chevron = new Label("→");
        chevron.setFont(Font.font(14));
        chevron.setTextFill(Color.color(1, 1, 1, 0.5));

        header.getChildren().addAll(icon, titleSection, category, chevron);
        item.getChildren().add(header);

        return item;
    }

    private VBox createCommentItem(Commentaire comment) {
        VBox item = clickableRow(() -> showCommentDetails(comment));

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("💬");
        Label title = new Label(trimTo(safe(comment.getDescriptionCommentaire(), "No comment text"), 90));
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        HBox.setHgrow(title, Priority.ALWAYS);

        Label date = new Label(formatDate(comment.getCreatedAt()));
        date.setTextFill(Color.color(1, 1, 1, 0.5));
        date.setFont(Font.font("Segoe UI", 10));

        row.getChildren().addAll(icon, title, date);
        item.getChildren().add(row);

        return item;
    }

    private VBox createReactionItem(Reaction reaction) {
        VBox item = clickableRow(() -> showReactionDetails(reaction));

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("👍");
        String kind = safe(reaction.getKind(), "Reaction");
        String target = reaction.getPublication() != null ? "Publication" : "Comment";

        Label title = new Label(kind + " on " + target);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        HBox.setHgrow(title, Priority.ALWAYS);

        Label date = new Label(formatDate(reaction.getUpdatedAt()));
        date.setTextFill(Color.color(1, 1, 1, 0.5));
        date.setFont(Font.font("Segoe UI", 10));

        row.getChildren().addAll(icon, title, date);
        item.getChildren().add(row);

        return item;
    }

    private VBox createBookmarkItem(Reaction bookmark) {
        VBox item = clickableRow(() -> showBookmarkDetails(bookmark));

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("🔖");
        Publication publication = resolvePublication(bookmark.getPublication());
        if (publication == null && bookmark.getCommentaire() != null) {
            publication = resolvePublication(bookmark.getCommentaire().getPublication());
        }

        String titleText = publication == null ? "Bookmarked item" : safe(publication.getTitrePub(), "Bookmarked publication");
        Label title = new Label(trimTo(titleText, 90));
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        HBox.setHgrow(title, Priority.ALWAYS);

        Label date = new Label(formatDate(bookmark.getUpdatedAt()));
        date.setTextFill(Color.color(1, 1, 1, 0.5));
        date.setFont(Font.font("Segoe UI", 10));

        row.getChildren().addAll(icon, title, date);
        item.getChildren().add(row);

        return item;
    }

    private VBox clickableRow(Runnable onClick) {
        VBox item = new VBox(8);
        item.setStyle(
            "-fx-background-color: rgba(99, 102, 241, 0.05); " +
            "-fx-border-color: rgba(99, 102, 241, 0.1); " +
            "-fx-border-width: 1; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-padding: 12; " +
            "-fx-cursor: hand;"
        );
        item.setOnMouseClicked(e -> onClick.run());
        item.setOnMouseEntered(e -> item.setStyle(
            "-fx-background-color: rgba(99, 102, 241, 0.1); " +
            "-fx-border-color: rgba(99, 102, 241, 0.2); " +
            "-fx-border-width: 1; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-padding: 12; " +
            "-fx-cursor: hand;"
        ));
        item.setOnMouseExited(e -> item.setStyle(
            "-fx-background-color: rgba(99, 102, 241, 0.05); " +
            "-fx-border-color: rgba(99, 102, 241, 0.1); " +
            "-fx-border-width: 1; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-padding: 12; " +
            "-fx-cursor: hand;"
        ));
        return item;
    }

    private void showPublicationDetails(Publication publication) {
        detailsTitle.setText("Publication Details");
        detailsContent.getChildren().clear();

        VBox metaCard = detailCard();
        metaCard.getChildren().addAll(
            detailLine("Title", safe(publication.getTitrePub(), "—")),
            detailLine("Category", safe(publication.getCategoriePub(), "—")),
            detailLine("Date", formatDate(publication.getDateCreationPub())),
            detailLine("Description", safe(publication.getDescriptionPub(), "No description"))
        );

        VBox previewCard = createPublicationPreviewCard(publication);
        VBox commentsSection = createPublicationCommentsSection(publication);

        detailsContent.getChildren().addAll(previewCard, metaCard, commentsSection);
        animateSwitcherFace(false);
    }

    private VBox createPublicationPreviewCard(Publication publication) {
        VBox preview = detailCard();

        Label heading = new Label("Preview");
        heading.setTextFill(Color.WHITE);
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        Label title = new Label(safe(publication.getTitrePub(), "Untitled publication"));
        title.setTextFill(Color.WHITE);
        title.setWrapText(true);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Label description = new Label(safe(publication.getDescriptionPub(), "No description"));
        description.setTextFill(Color.color(1, 1, 1, 0.8));
        description.setWrapText(true);
        description.setFont(Font.font("Segoe UI", 12));

        Node imageNode;
        Image image = resolveImage(publication.getImagePub(), "forum_images");
        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(520);
            imageView.setFitHeight(280);
            imageView.setSmooth(true);
            imageView.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-border-color: rgba(255,255,255,0.12); -fx-border-width: 1; -fx-background-radius: 10; -fx-border-radius: 10;");
            imageNode = imageView;
        } else {
            Label noImage = new Label("No image attached.");
            noImage.setTextFill(Color.color(1, 1, 1, 0.55));
            noImage.setFont(Font.font("Segoe UI", 10));
            imageNode = noImage;
        }

        preview.getChildren().addAll(heading, title, description, imageNode);
        return preview;
    }

    private VBox createPublicationCommentsSection(Publication publication) {
        VBox section = detailCard();

        Label heading = new Label("Comments");
        heading.setTextFill(Color.WHITE);
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        VBox commentsBox = new VBox(8);
        List<Commentaire> publicationComments = commentaireController.commentairesByPublication(publication);
        if (publicationComments == null || publicationComments.isEmpty()) {
            commentsBox.getChildren().add(emptyText("No comments yet."));
        } else {
            for (Commentaire comment : publicationComments) {
                commentsBox.getChildren().add(createCommentPreviewItem(comment));
            }
        }

        Label composeLabel = new Label("Add a comment");
        composeLabel.setTextFill(Color.color(1, 1, 1, 0.8));
        composeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        TextArea commentInput = new TextArea();
        commentInput.setPromptText("Share your thoughts...");
        commentInput.setWrapText(true);
        commentInput.setPrefRowCount(3);
        commentInput.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-control-inner-background: rgba(255,255,255,0.04); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.45); -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: rgba(255,255,255,0.12);");

        Label feedback = new Label();
        feedback.setTextFill(Color.color(1, 1, 1, 0.65));
        feedback.setFont(Font.font("Segoe UI", 11));

        Button postBtn = new Button("Post Comment");
        postBtn.setStyle("-fx-padding: 8 14 8 14; -fx-background-color: rgba(99, 102, 241, 0.85); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        postBtn.setOnAction(e -> postPublicationComment(publication, commentInput, feedback));

        section.getChildren().addAll(heading, commentsBox, composeLabel, commentInput, postBtn, feedback);
        return section;
    }

    private VBox createCommentPreviewItem(Commentaire comment) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.TOP_LEFT);
        item.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-padding: 10; -fx-background-radius: 8; -fx-border-radius: 8;");

        StackPane avatar = createCommentAvatarNode(comment);

        VBox bodyWrap = new VBox(4);
        HBox.setHgrow(bodyWrap, Priority.ALWAYS);

        Label content = new Label(safe(comment.getDescriptionCommentaire(), "No comment text"));
        content.setWrapText(true);
        content.setTextFill(Color.WHITE);
        content.setFont(Font.font("Segoe UI", 12));

        String author = comment.isVisibility() ? author(comment.getUser()) : "Anonymous";

        Label meta = new Label(author + " • " + formatDate(comment.getCreatedAt()));
        meta.setTextFill(Color.color(1, 1, 1, 0.55));
        meta.setFont(Font.font("Segoe UI", 10));

        bodyWrap.getChildren().addAll(meta, content);

        VBox wrap = new VBox();
        wrap.getChildren().add(item);
        item.getChildren().addAll(avatar, bodyWrap);
        return wrap;
    }

    private StackPane createCommentAvatarNode(Commentaire comment) {
        boolean visibleIdentity = comment.isVisibility();
        User commentUser = comment.getUser();

        Circle avatarCircle = new Circle(16);
        avatarCircle.setStroke(Color.color(1, 1, 1, 0.2));
        avatarCircle.setStrokeWidth(1);

        Label initialLabel = new Label(visibleIdentity ? initials(commentUser) : "A");
        initialLabel.setTextFill(Color.WHITE);
        initialLabel.setStyle("-fx-font-size: 11; -fx-font-weight: 700;");

        boolean hasImage = false;
        if (visibleIdentity) {
            hasImage = applyUserAvatarFill(commentUser, avatarCircle);
        }
        if (!hasImage) {
            avatarCircle.setFill(Color.color(1, 1, 1, 0.10));
        }

        initialLabel.setVisible(!hasImage);
        initialLabel.setManaged(!hasImage);

        StackPane avatarWrap = new StackPane(avatarCircle, initialLabel);
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

        Profile profile = profileController.profileByUserId(userId).orElse(null);
        profileByUserIdCache.put(userId, profile);
        return profile;
    }

    private void postPublicationComment(Publication publication, TextArea input, Label feedback) {
        if (publication == null || publication.getIdPublication() == null) {
            feedback.setTextFill(Color.web("#fca5a5"));
            feedback.setText("Unable to post comment: invalid publication.");
            return;
        }

        if (currentUser == null || currentUser.getIdUser() == null) {
            feedback.setTextFill(Color.web("#fca5a5"));
            feedback.setText("You must be logged in to comment.");
            return;
        }

        String content = input.getText() == null ? "" : input.getText().trim();
        if (content.isEmpty()) {
            feedback.setTextFill(Color.web("#fca5a5"));
            feedback.setText("Comment cannot be empty.");
            return;
        }

        Integer createdId = commentaireController.commentaireCreate(content, null, true, publication, currentUser);
        if (createdId != null && createdId > 0) {
            input.clear();
            feedback.setTextFill(Color.web("#86efac"));
            feedback.setText("Comment posted.");
            refreshDataAndRender();
            showPublicationDetails(publication);
            return;
        }

        feedback.setTextFill(Color.web("#fca5a5"));
        feedback.setText("Failed to post comment.");
    }

    private void showCommentDetails(Commentaire comment) {
        detailsTitle.setText("Comment Details");
        detailsContent.getChildren().clear();

        Publication publication = resolvePublication(comment.getPublication());

        VBox card = detailCard();
        card.getChildren().addAll(
            detailLine("Comment", safe(comment.getDescriptionCommentaire(), "—")),
            detailLine("Publication", publication == null ? "—" : safe(publication.getTitrePub(), "—")),
            detailLine("Category", publication == null ? "—" : safe(publication.getCategoriePub(), "—")),
            detailLine("Date", formatDate(comment.getCreatedAt()))
        );

        detailsContent.getChildren().add(card);
        animateSwitcherFace(false);
    }

    private void showReactionDetails(Reaction reaction) {
        detailsTitle.setText("Reaction Details");
        detailsContent.getChildren().clear();

        Publication publication = resolvePublication(reaction.getPublication());
        if (publication == null && reaction.getCommentaire() != null) {
            publication = resolvePublication(reaction.getCommentaire().getPublication());
        }

        VBox card = detailCard();
        card.getChildren().addAll(
            detailLine("Type", safe(reaction.getKind(), "—")),
            detailLine("Emoji", safe(reaction.getEmoji(), "—")),
            detailLine("Target publication", publication == null ? "—" : safe(publication.getTitrePub(), "—")),
            detailLine("Date", formatDate(reaction.getUpdatedAt()))
        );

        detailsContent.getChildren().add(card);
        animateSwitcherFace(false);
    }

    private void showBookmarkDetails(Reaction bookmark) {
        detailsTitle.setText("Bookmark Details");
        detailsContent.getChildren().clear();

        Publication publication = resolvePublication(bookmark.getPublication());
        if (publication == null && bookmark.getCommentaire() != null) {
            publication = resolvePublication(bookmark.getCommentaire().getPublication());
        }

        VBox card = detailCard();
        card.getChildren().addAll(
            detailLine("Type", "Bookmark"),
            detailLine("Publication", publication == null ? "—" : safe(publication.getTitrePub(), "—")),
            detailLine("Category", publication == null ? "—" : safe(publication.getCategoriePub(), "—")),
            detailLine("Date", formatDate(bookmark.getUpdatedAt()))
        );

        detailsContent.getChildren().add(card);
        animateSwitcherFace(false);
    }

    private VBox detailCard() {
        VBox detailCard = new VBox(12);
        detailCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.03); -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-width: 1; -fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 16;");
        return detailCard;
    }

    private VBox detailLine(String key, String value) {
        VBox line = new VBox(4);
        Label k = new Label(key);
        k.setTextFill(Color.color(1, 1, 1, 0.55));
        k.setFont(Font.font("Segoe UI", 10));

        Label v = new Label(value);
        v.setTextFill(Color.WHITE);
        v.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        v.setWrapText(true);

        line.getChildren().addAll(k, v);
        return line;
    }

    private void showListView() {
        animateSwitcherFace(true);
    }

    private void animateSwitcherFace(boolean showList) {
        VBox toShow = showList ? faceListView : faceDetailsView;
        VBox toHide = showList ? faceDetailsView : faceListView;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(220), toHide);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            toHide.setVisible(false);
            toHide.setManaged(false);
        });

        toShow.setVisible(true);
        toShow.setManaged(true);
        toShow.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(240), toShow);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        fadeOut.play();
        fadeIn.play();
    }

    private boolean isAdminUser(User user) {
        if (user == null || user.getRoleUser() == null) {
            return false;
        }
        return "ADMIN".equalsIgnoreCase(user.getRoleUser()) || "OWNER".equalsIgnoreCase(user.getRoleUser());
    }

    private String formatDate(LocalDateTime value) {
        if (value == null) {
            return "—";
        }
        return value.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
    }

    private String safe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String trimTo(String value, int max) {
        if (value == null) {
            return "";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 1) + "…";
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

    public VBox getRoot() {
        return root;
    }

    public void cleanup() {
        // Cleanup resources if needed.
    }
}
