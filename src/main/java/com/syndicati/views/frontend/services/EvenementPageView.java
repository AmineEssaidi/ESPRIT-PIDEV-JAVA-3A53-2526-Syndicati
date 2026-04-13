package com.syndicati.views.frontend.services;

import com.syndicati.MainApplication;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.models.services.EvenementService;
import com.syndicati.models.services.ParticipationService;
import java.util.List;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class EvenementPageView implements ViewInterface {

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();
    private final EvenementService evenementService;
    private final ParticipationService participationService;

    // Form fields for CRUD
    private TextField titreField;
    private TextArea descArea;
    private TextField lieuField;
    private TextField typeField;
    private DatePicker datePicker;
    private TextField nbPlacesField;
    private com.syndicati.models.entities.Evenement editingEvent = null;

    private GridPane eventsGrid;
    private VBox mainFace;
    private VBox extraFace;

    public EvenementPageView() {
        this.evenementService = new EvenementService();
        this.participationService = new ParticipationService();

        root = new VBox(26);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20, 0, 44, 0));
        root.setStyle("-fx-background-color: transparent;");

        refreshContent();
    }

    private void refreshContent() {
        root.getChildren().clear();
        root.getChildren().addAll(
            buildHeroSection(),
            buildDashboardSection(),
            buildEventsSection()
        );
    }

    private StackPane buildHeroSection() {
        StackPane hero = sectionShell(48, new Insets(128, 64, 128, 64), surfaceStrong(), borderSoft());
        hero.paddingProperty().bind(Bindings.createObjectBinding(
            () -> root.getWidth() < 1050 ? new Insets(58, 30, 58, 30) : new Insets(128, 64, 128, 64),
            root.widthProperty()
        ));

        VBox left = new VBox(18);
        left.setAlignment(Pos.CENTER_LEFT);

        left.getChildren().add(sectionPill("Community Experiences"));

        Text title = text("Discover. Connect.\nExperience.", 80, true, tm.getAccentHex());
        title.wrappingWidthProperty().bind(Bindings.max(300, hero.widthProperty().subtract(120)));
        Text subtitle = text(
            "Join exclusive events, workshops, and gatherings designed for our community. Your next great story starts here.",
            21,
            false,
            textMuted()
        );
        subtitle.wrappingWidthProperty().bind(Bindings.max(280, hero.widthProperty().subtract(180)));

        Button explore = gradientButton("Explore Events", 14, new Insets(12, 26, 12, 26));

        left.getChildren().addAll(title, subtitle, explore);
        hero.getChildren().add(left);
        return hero;
    }

    private StackPane buildDashboardSection() {
        StackPane dashboard = new StackPane();
        dashboard.setPadding(new Insets(96, 80, 96, 80));
        dashboard.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        dashboard.paddingProperty().bind(Bindings.createObjectBinding(
            () -> root.getWidth() < 1100 ? new Insets(38, 24, 38, 24) : new Insets(96, 80, 96, 80),
            root.widthProperty()
        ));
        dashboard.setStyle(
            "-fx-background-color: " + surfaceStrong() + ";" +
            "-fx-background-radius: 48px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 48px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.38), 32, 0.16, 0, 8);"
        );

        GridPane content = new GridPane();
        content.setHgap(48);
        content.setVgap(34);

        ColumnConstraints leftCol = new ColumnConstraints();
        leftCol.setPercentWidth(55);
        leftCol.setHgrow(Priority.ALWAYS);
        leftCol.setFillWidth(true);
        ColumnConstraints rightCol = new ColumnConstraints();
        rightCol.setPercentWidth(45);
        rightCol.setHgrow(Priority.ALWAYS);
        rightCol.setFillWidth(true);
        content.getColumnConstraints().addAll(leftCol, rightCol);

        VBox left = new VBox(24);
        left.setAlignment(Pos.CENTER_LEFT);
        left.setMinWidth(0);
        left.setMaxWidth(Double.MAX_VALUE);

        left.getChildren().add(sectionPill("Event Dashboard"));
        int eventCount = evenementService.getAllEvents().size();
        Text dashTitle = text("Join the Action.\n" + eventCount + " Events Upcoming.", 48, true, tm.getAccentHex());
        dashTitle.wrappingWidthProperty().bind(Bindings.max(280, left.widthProperty().subtract(10)));
        left.getChildren().add(dashTitle);

        Text sub = text(
            "The community is buzzing. Browse upcoming events and secure your spot before they fill up.",
            18,
            false,
            textMuted()
        );
        sub.wrappingWidthProperty().bind(Bindings.max(260, left.widthProperty().subtract(12)));
        left.getChildren().add(sub);

        StackPane statPill = new StackPane(text("Join 500+ members in our next gathering", 13, false, textSoft()));
        statPill.setPadding(new Insets(14, 24, 14, 24));
        statPill.setStyle(
            "-fx-background-color: " + surfaceSoft() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;"
        );
        left.getChildren().add(statPill);

        StackPane switcher = new StackPane();
        switcher.setMinWidth(0);
        switcher.setMaxWidth(Double.MAX_VALUE);
        switcher.setStyle(
            "-fx-background-color: " + surfaceCard() + ";" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 32px;" +
            "-fx-border-radius: 32px;" +
            "-fx-padding: 26px;"
        );

        mainFace = new VBox(18);
        mainFace.setAlignment(Pos.TOP_LEFT);
        mainFace.setFillWidth(true);
        extraFace = new VBox(14);
        extraFace.setAlignment(Pos.TOP_LEFT);
        extraFace.setFillWidth(true);
        extraFace.setVisible(false);
        extraFace.setManaged(false);

        Button hostTrigger = buildHostTrigger();
        hostTrigger.setOnAction(e -> {
            clearForm();
            switchFace(mainFace, extraFace);
        });

        mainFace.getChildren().addAll(
            hostTrigger,
            buildGlassCalendar(),
            buildCalendarPreview()
        );

        Button closeForm = iconButton("x");
        closeForm.setOnAction(e -> switchFace(extraFace, mainFace));

        HBox formHead = new HBox();
        formHead.setAlignment(Pos.CENTER_LEFT);
        formHead.getChildren().addAll(
            text("Organize Event", 24, true, tm.getTextColor()),
            spacer(),
            closeForm
        );

        titreField = (TextField) addFormInput(null, "Event Title", "Sunset Rooftop Gathering");
        
        HBox row1 = new HBox(14);
        VBox dateCol = new VBox(6);
        dateCol.getChildren().add(text("Date and Time", 11, true, textSoft()));
        datePicker = new DatePicker();
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setPromptText("2026-04-25 17:30");
        datePicker.setStyle(
            "-fx-background-color: #1a1a1a;" +
            "-fx-border-color: #333333;" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-text-fill: white;"
        );
        dateCol.getChildren().add(datePicker);
        HBox.setHgrow(dateCol, Priority.ALWAYS);
        
        VBox typeCol = new VBox(6);
        typeCol.getChildren().add(text("Event Type", 11, true, textSoft()));
        typeField = new TextField();
        typeField.setPromptText("Social");
        applyInputStyle(typeField);
        typeCol.getChildren().add(typeField);
        HBox.setHgrow(typeCol, Priority.ALWAYS);
        row1.getChildren().addAll(dateCol, typeCol);
        
        lieuField = (TextField) addFormInput(null, "Location", "Rooftop");
        descArea = (TextArea) addFormInput(null, "Description", "A sunset meetup with music and community networking.", true);

        HBox row2 = new HBox(14);
        nbPlacesField = (TextField) addFormInput(null, "Total Places", "80");
        TextField remainingDummy = new TextField("80");
        applyInputStyle(remainingDummy);
        VBox remainingCol = new VBox(6, text("Initial Remaining", 11, true, textSoft()), remainingDummy);
        HBox.setHgrow(nbPlacesField.getParent(), Priority.ALWAYS);
        HBox.setHgrow(remainingCol, Priority.ALWAYS);
        row2.getChildren().addAll(nbPlacesField.getParent(), remainingCol);

        Button saveBtn = gradientButton("Organize Now", 12, new Insets(13, 18, 13, 18));
        saveBtn.setOnAction(e -> handleSaveEvent(mainFace, extraFace));

        extraFace.getChildren().addAll(
            formHead,
            text("Share your vision with the community", 13, false, textMuted()),
            titreField.getParent(),
            row1,
            lieuField.getParent(),
            descArea.getParent(),
            row2,
            saveBtn
        );

        switcher.getChildren().addAll(mainFace, extraFace);

        content.add(left, 0, 0);
        content.add(switcher, 1, 0);
        updateDashboardColumns(content, switcher, dashboard.getWidth());
        dashboard.widthProperty().addListener((obs, oldW, newW) -> updateDashboardColumns(content, switcher, newW.doubleValue()));

        dashboard.getChildren().add(content);
        return dashboard;
    }

    private void updateDashboardColumns(GridPane content, Node switcher, double width) {
        boolean narrow = width < 1200;
        GridPane.setColumnIndex(switcher, narrow ? 0 : 1);
        GridPane.setRowIndex(switcher, narrow ? 1 : 0);
        if (narrow) {
            content.getColumnConstraints().get(0).setPercentWidth(100);
            content.getColumnConstraints().get(1).setPercentWidth(0);
        } else {
            content.getColumnConstraints().get(0).setPercentWidth(55);
            content.getColumnConstraints().get(1).setPercentWidth(45);
        }
    }

    private VBox buildGlassCalendar() {
        VBox wrap = new VBox(14);
        wrap.setPadding(new Insets(4, 0, 0, 0));

        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);
        head.getChildren().addAll(
            text("March 2026", 18, true, tm.getAccentHex()),
            spacer(),
            iconButton("<"),
            iconButton(">")
        );

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(14.285);
            cc.setHgrow(Priority.ALWAYS);
            cc.setFillWidth(true);
            grid.getColumnConstraints().add(cc);
        }

        String[] dayLabels = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
        for (int i = 0; i < dayLabels.length; i++) {
            Text lbl = text(dayLabels[i], 11, true, textMuted());
            StackPane labelCell = new StackPane(lbl);
            labelCell.setMinHeight(20);
            grid.add(labelCell, i, 0);
        }

        int startOffset = 6;
        int day = 1;
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                StackPane cell = new StackPane();
                cell.setMaxWidth(Double.MAX_VALUE);
                cell.setMinHeight(34);
                cell.setStyle(
                    "-fx-background-color: " + surfaceSoft() + ";" +
                    "-fx-background-radius: 10px;" +
                    "-fx-border-color: transparent;" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 10px;"
                );

                if (row == 1 && col < startOffset) {
                    cell.getChildren().add(text("", 12, false, "rgba(255,255,255,0.2)"));
                } else if (day <= 31) {
                    boolean hasEvent = day == 22 || day == 28 || day == 2 || day == 18;
                    boolean active = day == 22;

                    Text d = text(String.valueOf(day), 12, true, tm.getTextColor());
                    cell.getChildren().add(d);

                    if (hasEvent) {
                        cell.setStyle(
                            "-fx-background-color: " + surfaceSoft() + ";" +
                            "-fx-background-radius: 10px;" +
                            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.35) + ";" +
                            "-fx-border-width: 1px;" +
                            "-fx-border-radius: 10px;"
                        );
                        Circle dot = new Circle(3, Color.web(tm.getAccentHex()));
                        StackPane.setAlignment(dot, Pos.BOTTOM_CENTER);
                        StackPane.setMargin(dot, new Insets(0, 0, 4, 0));
                        cell.getChildren().add(dot);
                    }

                    if (active) {
                        cell.setStyle(
                            "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
                            "-fx-background-radius: 10px;" +
                            "-fx-border-color: transparent;" +
                            "-fx-border-width: 1px;" +
                            "-fx-border-radius: 10px;" +
                            "-fx-effect: dropshadow(gaussian, " + tm.toRgba(tm.getAccentHex(), 0.45) + ", 14, 0.3, 0, 3);"
                        );
                    }
                    day++;
                }

                grid.add(cell, col, row);
            }
        }

        HBox tags = new HBox(8,
            tag("#Workshops"),
            tag("#Meetups"),
            tag("#Social"),
            tag("#Sports"),
            tag("+ 6 Events")
        );
        tags.setPadding(new Insets(8, 0, 0, 0));

        wrap.getChildren().addAll(head, grid, tags);
        return wrap;
    }

    private VBox buildCalendarPreview() {
        VBox preview = new VBox(8);
        preview.setPadding(new Insets(14));
        preview.setStyle(
            "-fx-background-color: " + surfaceCard() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;"
        );

        StackPane banner = new StackPane();
        banner.setMinHeight(90);
        banner.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.25) + ";" +
            "-fx-background-radius: 14px;"
        );

        preview.getChildren().addAll(
            banner,
            tag("Meeting"),
            text("Assemblee generale des coproprietaires", 16, true, tm.getTextColor()),
            text("Mar 22, 2026 - 18:30", 12, false, textSoft()),
            gradientButton("View Details", 11, new Insets(8, 10, 8, 10))
        );
        return preview;
    }

    private VBox buildEventsSection() {
        VBox section = new VBox(18);
        section.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        section.setPadding(new Insets(0, 48, 0, 48));
        section.paddingProperty().bind(Bindings.createObjectBinding(
            () -> root.getWidth() < 992 ? new Insets(0, 14, 0, 14) : new Insets(0, 48, 0, 48),
            root.widthProperty()
        ));

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        top.getChildren().addAll(
            sectionPill("Upcoming Events"),
            spacer(),
            text("Showing 1-" + Math.min(3, evenementService.getAllEvents().size()) + " of " + evenementService.getAllEvents().size(), 13, false, textMuted())
        );

        eventsGrid = new GridPane();
        eventsGrid.setHgap(48);
        eventsGrid.setVgap(48);
        rebuildEventsGrid(eventsGrid, section.getWidth());
        section.widthProperty().addListener((obs, oldW, newW) -> rebuildEventsGrid(eventsGrid, newW.doubleValue()));

        HBox pagination = new HBox(8);
        pagination.setAlignment(Pos.CENTER);
        pagination.getChildren().addAll(
            paginationBtn("<", false),
            paginationBtn("1", true),
            paginationBtn("2", false),
            paginationBtn(">", false)
        );

        section.getChildren().addAll(top, eventsGrid, pagination);
        return section;
    }

    private void rebuildEventsGrid(GridPane grid, double width) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();

        List<com.syndicati.models.entities.Evenement> events = evenementService.getAllEvents();

        // Use effective width to avoid early collapse to 1 column at minimum app size.
        double effectiveWidth = Math.max(width, grid.getWidth());
        int cols = effectiveWidth < 820 ? 1 : (effectiveWidth < 1400 ? 2 : 3);
        for (int i = 0; i < cols; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(100.0 / cols);
            c.setHgrow(Priority.ALWAYS);
            c.setFillWidth(true);
            grid.getColumnConstraints().add(c);
        }

        for (int i = 0; i < events.size(); i++) {
            VBox card = eventCard(events.get(i));
            GridPane.setFillWidth(card, true);
            card.setMaxWidth(Double.MAX_VALUE);
            card.setMinWidth(260);
            grid.add(card, i % cols, i / cols);
        }
    }

    private VBox eventCard(com.syndicati.models.entities.Evenement e) {
        VBox card = new VBox();
        card.setMinWidth(280);
        card.setStyle(
            "-fx-background-color: " + surfaceCard() + ";" +
            "-fx-background-radius: 32px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 32px;"
        );

        StackPane switcher = new StackPane();

        VBox mainFace = new VBox(0);
        VBox detailsFace = new VBox(12);
        detailsFace.setVisible(false);
        detailsFace.setManaged(false);
        detailsFace.setPadding(new Insets(18));

        StackPane image = new StackPane();
        image.setMinHeight(220);
        image.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.22) + ";" +
            "-fx-background-radius: 32px 32px 0 0;"
        );
        StackPane.setAlignment(image, Pos.CENTER);

        StackPane typeTag = tag(e.getTypeEvent() != null ? e.getTypeEvent() : "Event");
        StackPane.setAlignment(typeTag, Pos.TOP_RIGHT);
        StackPane.setMargin(typeTag, new Insets(18, 18, 0, 0));
        image.getChildren().add(typeTag);

        VBox body = new VBox(12);
        body.setPadding(new Insets(20));

        HBox meta = new HBox(14,
            text(e.getDateEvent() != null ? e.getDateEvent().toString() : "", 12, false, textMuted()),
            text(e.getLieuEvent(), 12, false, textMuted())
        );
        Text title = text(e.getTitreEvent(), 24, true, tm.getTextColor());
        title.wrappingWidthProperty().bind(card.widthProperty().subtract(52));
        Text desc = text(e.getDescriptionEvent(), 14, false, textSoft());
        desc.wrappingWidthProperty().bind(card.widthProperty().subtract(52));
        Region pushFooter = new Region();
        VBox.setVgrow(pushFooter, Priority.ALWAYS);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(10, 14, 10, 14));
        footer.setStyle(
            "-fx-background-color: " + surfaceSoft() + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 20px;"
        );
        VBox avail = new VBox(2,
            text(String.valueOf(e.getNbRestants()), 18, true, tm.getTextColor()),
            text("Places Left", 11, false, textMuted())
        );

        HBox actions = new HBox(8);
        Button editBtn = iconButton("✎");
        editBtn.setOnAction(ev -> prepareEdit(e));
        Button deleteBtn = iconButton("🗑");
        deleteBtn.setStyle(deleteBtn.getStyle() + "-fx-text-fill: #ff4d4d;");
        deleteBtn.setOnAction(ev -> handleDelete(e));

        Button details = gradientButton("View Details", 11, new Insets(8, 14, 8, 14));
        details.setOnAction(ev -> switchFace(mainFace, detailsFace));
        footer.getChildren().addAll(avail, spacer(), editBtn, deleteBtn, details);

        body.getChildren().addAll(meta, title, desc, pushFooter, footer);
        mainFace.getChildren().addAll(image, body);

        HBox detailsHead = new HBox();
        detailsHead.setAlignment(Pos.CENTER_LEFT);
        Button back = iconButton("<");
        back.setOnAction(ev -> switchFace(detailsFace, mainFace));
        detailsHead.getChildren().addAll(text("Details", 22, true, tm.getTextColor()), spacer(), back);

        StackPane detailImage = new StackPane();
        detailImage.setMinHeight(130);
        detailImage.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.18) + ";" +
            "-fx-background-radius: 16px;"
        );

        Button joinBtn = gradientButton("Join Now", 12, new Insets(10, 14, 10, 14));
        
        // Check if already joined
        com.syndicati.utils.session.SessionManager sm = com.syndicati.utils.session.SessionManager.getInstance();
        if (sm.isLoggedIn()) {
            boolean joined = participationService.getParticipationsByUser(sm.getCurrentUser().getIdUser())
                    .stream().anyMatch(p -> p.getEvenement().getIdEvent() == e.getIdEvent());
            if (joined) {
                joinBtn.setText("Already Joined");
                joinBtn.setDisable(true);
                joinBtn.setStyle(joinBtn.getStyle() + "-fx-opacity: 0.7;");
            }
        }

        joinBtn.setOnAction(ev -> {
            if (!sm.isLoggedIn()) {
                showErrorAlert("Not Logged In", "You must be logged in to join this event.");
                return;
            }

            if (e.getNbRestants() <= 0) {
                showErrorAlert("No Places Left", "This event is currently full. Please check back later.");
                return;
            }

            showParticipationForm(e, joinBtn);
        });

        detailsFace.getChildren().addAll(
            detailsHead,
            detailImage,
            text(e.getTitreEvent(), 20, true, tm.getTextColor()),
            text(e.getDescriptionEvent(), 13, false, textSoft()),
            new HBox(10, 
                text("\uD83D\uDCCD " + e.getLieuEvent(), 12, false, textMuted()),
                text("\uD83D\uDCC5 " + (e.getDateEvent() != null ? e.getDateEvent().toString() : ""), 12, false, textMuted())
            ),
            new HBox(5, 
                text(String.valueOf(e.getNbRestants()), 14, true, tm.getAccentHex()),
                text("places available", 13, false, textMuted())
            ),
            text("Hosted by " + (e.getUser() != null ? e.getUser().getFirstName() + " " + e.getUser().getLastName() : "Community Team"), 12, false, textMuted()),
            joinBtn
        );

        switcher.getChildren().addAll(mainFace, detailsFace);
        card.getChildren().add(switcher);

        addHoverLift(card);
        return card;
    }

    private void switchFace(Node from, Node to) {
        from.setVisible(false);
        from.setManaged(false);
        to.setVisible(true);
        to.setManaged(true);
    }

    private StackPane sectionPill(String value) {
        StackPane pill = new StackPane(text(value, 11, true, "#ffffff"));
        pill.setPadding(new Insets(8, 14, 8, 14));
        pill.setMaxWidth(StackPane.USE_PREF_SIZE);
        pill.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.12) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.25) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 100px;" +
            "-fx-border-radius: 100px;"
        );
        return pill;
    }

    private StackPane tag(String value) {
        StackPane pill = new StackPane(text(value, 10, true, textSoft()));
        pill.setPadding(new Insets(6, 10, 6, 10));
        pill.setMaxWidth(StackPane.USE_PREF_SIZE);
        pill.setStyle(
            "-fx-background-color: " + surfaceSoft() + ";" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-radius: 12px;"
        );
        return pill;
    }

    private Button buildHostTrigger() {
        Button btn = new Button("Host an Event");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.12) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.28) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 20px;" +
            "-fx-border-radius: 20px;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 800;" +
            "-fx-padding: 12 16 12 16;"
        );
        return btn;
    }

    private Button gradientButton(String label, int fontSize, Insets padding) {
        Button b = new Button(label);
        b.setStyle(
            "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 700;" +
            "-fx-font-size: " + fontSize + "px;" +
            "-fx-background-radius: 14px;"
        );
        b.setPadding(padding);
        return b;
    }

    private Button iconButton(String label) {
        Button b = new Button(label);
        b.setStyle(
            "-fx-background-color: " + surfaceSoft() + ";" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 10px;" +
            "-fx-border-radius: 10px;" +
            "-fx-text-fill: " + textSoft() + ";" +
            "-fx-font-weight: 700;"
        );
        return b;
    }

    private Button paginationBtn(String label, boolean active) {
        Button b = new Button(label);
        if (active) {
            b.setStyle(
                "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 10px;" +
                "-fx-font-weight: 700;" +
                "-fx-min-width: 40px; -fx-min-height: 40px;"
            );
        } else {
            b.setStyle(
                "-fx-background-color: " + surfaceSoft() + ";" +
                "-fx-border-color: " + borderSoft() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-text-fill: " + textSoft() + ";" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;" +
                "-fx-font-weight: 700;" +
                "-fx-min-width: 40px; -fx-min-height: 40px;"
            );
        }
        return b;
    }

    private Node addFormInput(VBox container, String label, String placeholder) {
        return addFormInput(container, label, placeholder, false);
    }

    private Node addFormInput(VBox container, String label, String placeholder, boolean area) {
        VBox row = new VBox(4);
        Text l = text(label, 11, true, textSoft());
        TextInputControl field = area ? new TextArea() : new TextField();
        field.setPromptText(placeholder);
        if (area) {
            ((TextArea) field).setPrefRowCount(3);
            ((TextArea) field).setWrapText(true);
        }
        applyInputStyle(field);
        row.getChildren().addAll(l, field);
        if (container != null) container.getChildren().add(row);
        return field;
    }

    private void applyInputStyle(TextInputControl field) {
        field.setStyle(
            "-fx-background-color: #1a1a1a;" +
            "-fx-border-color: #333333;" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-radius: 12px;" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: #8a8a8a;" +
            "-fx-padding: 10px 14px;"
        );
    }

    private void handleSaveEvent(Node from, Node to) {
        try {
            // Basic validation
            if (titreField.getText().trim().isEmpty()) {
                System.err.println("Error: Event title is required.");
                return;
            }
            if (lieuField.getText().trim().isEmpty()) {
                System.err.println("Error: Location is required.");
                return;
            }
            if (datePicker.getValue() == null) {
                System.err.println("Error: Date is required.");
                return;
            }
            if (nbPlacesField.getText().trim().isEmpty()) {
                System.err.println("Error: Number of places is required.");
                return;
            }

            int nbPlaces;
            try {
                nbPlaces = Integer.parseInt(nbPlacesField.getText().trim());
            } catch (NumberFormatException nfe) {
                System.err.println("Error: Number of places must be a valid number.");
                return;
            }

            com.syndicati.utils.session.SessionManager sm = com.syndicati.utils.session.SessionManager.getInstance();
            if (!sm.isLoggedIn()) {
                System.err.println("Error: You must be logged in to host an event.");
                return; // Stop here to avoid NPE
            }

            com.syndicati.models.entities.Evenement e = (editingEvent != null) ? editingEvent : new com.syndicati.models.entities.Evenement();
            e.setTitreEvent(titreField.getText());
            e.setDescriptionEvent(descArea.getText());
            e.setLieuEvent(lieuField.getText());
            e.setTypeEvent(typeField.getText());
            if (datePicker.getValue() != null) {
                e.setDateEvent(datePicker.getValue().atStartOfDay());
            }
            e.setNbPlaces(nbPlaces);
            e.setUser(sm.getCurrentUser()); // Associate current user
            
            if (editingEvent == null) {
                e.setNbRestants(e.getNbPlaces());
                boolean ok = evenementService.createEvent(e);
                if (ok) System.out.println("✅ Event created successfully with ID: " + e.getIdEvent());
            } else {
                boolean ok = evenementService.updateEvent(e);
                if (ok) System.out.println("✅ Event updated successfully.");
            }
            
            clearForm();
            switchFace(from, to);
            refreshContent();
        } catch (Exception ex) {
            System.err.println("❌ Error saving event: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void prepareEdit(com.syndicati.models.entities.Evenement e) {
        editingEvent = e;
        titreField.setText(e.getTitreEvent());
        descArea.setText(e.getDescriptionEvent());
        lieuField.setText(e.getLieuEvent());
        typeField.setText(e.getTypeEvent());
        if (e.getDateEvent() != null) {
            datePicker.setValue(e.getDateEvent().toLocalDate());
        }
        nbPlacesField.setText(String.valueOf(e.getNbPlaces()));
        
        switchFace(mainFace, extraFace);
    }

    private void handleDelete(com.syndicati.models.entities.Evenement e) {
        evenementService.deleteEvent(e.getIdEvent());
        refreshContent();
    }

    private void clearForm() {
        editingEvent = null;
        titreField.clear();
        descArea.clear();
        lieuField.clear();
        typeField.clear();
        datePicker.setValue(null);
        nbPlacesField.clear();
    }

    private String surfaceStrong() {
        return tm.isDarkMode()
            ? "linear-gradient(from 0% 0% to 100% 100%, #020202 0%, #070707 58%, #0b0b0b 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, #ffffff 0%, #f8fafc 100%)";
    }

    private String surfaceCard() {
        return tm.isDarkMode()
            ? "linear-gradient(from 0% 0% to 100% 100%, rgba(10,10,10,0.93) 0%, rgba(14,14,14,0.93) 62%, " + tm.toRgba(tm.getAccentHex(), 0.10) + " 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, rgba(255,255,255,0.98) 0%, rgba(248,250,252,0.96) 100%)";
    }

    private String surfaceSoft() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.06)";
    }

    private String borderSoft() {
        return tm.isDarkMode() ? tm.toRgba(tm.getAccentHex(), 0.34) : "rgba(15,23,42,0.16)";
    }

    private String textSoft() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.92)" : "rgba(15,23,42,0.90)";
    }

    private String textMuted() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.78)" : "rgba(30,41,59,0.82)";
    }

    private HBox twoColRow(VBox left, VBox right) {
        HBox row = new HBox(10, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        left.setMaxWidth(Double.MAX_VALUE);
        right.setMaxWidth(Double.MAX_VALUE);
        return row;
    }

    private Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    private StackPane sectionShell(double radius, Insets padding, String bg, String border) {
        StackPane pane = new StackPane();
        pane.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        pane.setMinWidth(0);
        pane.setPadding(padding);
        pane.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-background-radius: " + radius + "px;" +
            "-fx-border-color: " + border + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: " + radius + "px;"
        );
        return pane;
    }

    private void showParticipationForm(com.syndicati.models.entities.Evenement e, Button joinBtn) {
        // Create full-screen dimmed overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.78);");
        
        // Ensure its size covers the entire window scene
        if (root.getScene() != null) {
            overlay.setPrefSize(root.getScene().getWidth(), root.getScene().getHeight());
        }

        VBox form = new VBox(20);
        form.setMinWidth(480);
        form.setMaxWidth(480);
        form.setPadding(new Insets(40));
        form.setAlignment(Pos.CENTER);
        form.setStyle(
            "-fx-background-color: " + surfaceStrong().split(";")[0] + ";" +
            "-fx-background-radius: 24px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 24px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 30, 0, 0, 10);"
        );

        Text titleText = text("Join Event: " + e.getTitreEvent(), 26, true, tm.getAccentHex());
        Text subText = text("Confirm your participation and add companions if any.", 14, false, textMuted());
        subText.setWrappingWidth(400);
        subText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        TextField companionsField = new TextField("0");
        applyInputStyle(companionsField);
        VBox companionsBox = new VBox(6, text("Number of companions", 11, true, textSoft()), companionsField);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Any special requests or comments?");
        commentArea.setPrefRowCount(3);
        applyInputStyle(commentArea);
        VBox commentBox = new VBox(6, text("Comment", 11, true, textSoft()), commentArea);

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = iconButton("Cancel");
        Button confirm = gradientButton("Confirm Registration", 13, new Insets(10, 24, 10, 24));

        actions.getChildren().addAll(cancel, confirm);

        form.getChildren().addAll(titleText, subText, companionsBox, commentBox, actions);
        overlay.getChildren().add(form);

        // Add overlay to the absolute scene root
        if (root.getScene() != null && root.getScene().getRoot() instanceof Pane) {
            Pane rootPane = (Pane) root.getScene().getRoot();
            rootPane.getChildren().add(overlay);
            overlay.toFront();
        } else {
            root.getChildren().add(overlay);
        }

        cancel.setOnAction(ev -> {
            if (overlay.getParent() instanceof Pane) {
                ((Pane) overlay.getParent()).getChildren().remove(overlay);
            }
        });

        confirm.setOnAction(ev -> {
            try {
                String companionsText = companionsField.getText().trim();
                if (companionsText.isEmpty()) companionsText = "0";
                int nb = Integer.parseInt(companionsText);
                if (nb < 0) throw new NumberFormatException();
                
                int totalNeeded = 1 + nb;
                if (totalNeeded > e.getNbRestants()) {
                    showErrorAlert("Not Enough Places", "Only " + e.getNbRestants() + " places left, but you requested " + totalNeeded + ".");
                    return;
                }

                com.syndicati.models.entities.User user = com.syndicati.utils.session.SessionManager.getInstance().getCurrentUser();
                com.syndicati.models.entities.Participation p = new com.syndicati.models.entities.Participation();
                p.setEvenement(e);
                p.setUser(user);
                p.setNbAccompagnants(nb);
                p.setCommentaireParticipation(commentArea.getText());
                p.setDateParticipation(java.time.LocalDateTime.now());
                p.setStatutParticipation("en_attente");

                // Format JSON data similar to the web project
                String jsonData = String.format(
                    "{\"user_name\":\"%s %s\",\"email\":\"%s\",\"nb_accompagnants\":%d,\"commentaire\":\"%s\",\"event_title\":\"%s\",\"created_at\":\"%s\"}",
                    user.getFirstName(), user.getLastName(), user.getEmailUser(),
                    nb, p.getCommentaireParticipation().replace("\"", "\\\""),
                    e.getTitreEvent().replace("\"", "\\\""),
                    java.time.LocalDateTime.now().toString()
                );
                p.setFormulaireData(jsonData);

                boolean ok = participationService.registerParticipation(p);
                if (ok) {
                    // Immediate visual feedback on the card
                    e.setNbRestants(e.getNbRestants() - totalNeeded);
                    joinBtn.setText("Registered!");
                    joinBtn.setDisable(true);
                    joinBtn.setStyle(joinBtn.getStyle() + "-fx-opacity: 0.7;");
                    
                    showSuccessAlert("Registration Successful", "You have joined '" + e.getTitreEvent() + "'. A confirmation email has been sent.");
                    
                    if (overlay.getParent() instanceof Pane) {
                        ((Pane) overlay.getParent()).getChildren().remove(overlay);
                    }
                    
                    // Refresh after a small delay to let user see the change
                    javafx.application.Platform.runLater(() -> refreshContent());
                } else {
                    showErrorAlert("Registration Failed", "An error occurred while saving your participation. Please try again later.");
                }
            } catch (NumberFormatException nfe) {
                showErrorAlert("Invalid Input", "Please enter a valid positive number for companions.");
            }
        });
    }

    private void showSuccessAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-background-color: " + surfaceStrong().split(";")[0] + "; -fx-text-fill: white;");
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-background-color: " + surfaceStrong().split(";")[0] + "; -fx-text-fill: white;");
        alert.showAndWait();
    }

    private Text text(String value, int size, boolean bold, String color) {
        Text t = new Text(value);
        t.setFont(Font.font(
            bold ? MainApplication.getInstance().getBoldFontFamily() : MainApplication.getInstance().getLightFontFamily(),
            bold ? FontWeight.BOLD : FontWeight.NORMAL,
            size
        ));
        t.setFill(Color.web(color));
        return t;
    }

    private void addHoverLift(VBox card) {
        card.setOnMouseEntered(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(220), card);
            tt.setToY(-8);
            tt.play();
            ScaleTransition st = new ScaleTransition(Duration.millis(220), card);
            st.setToX(1.01);
            st.setToY(1.01);
            st.play();
        });
        card.setOnMouseExited(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(220), card);
            tt.setToY(0);
            tt.play();
            ScaleTransition st = new ScaleTransition(Duration.millis(220), card);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public void cleanup() {}
}
