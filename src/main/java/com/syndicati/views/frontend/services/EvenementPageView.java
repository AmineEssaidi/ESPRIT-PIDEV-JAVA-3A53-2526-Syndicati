package com.syndicati.views.frontend.services;

import com.syndicati.MainApplication;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.utils.theme.ThemeManager;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

/**
 * Evenement page mirrored from Horizon twig/css structure.
 */
public class EvenementPageView implements ViewInterface {

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();

    private static final String[][] EVENTS = {
        {"Assemblee generale des coproprietaires", "Mar 22, 2026", "Hall principal", "Meeting", "84", "Review budget, maintenance planning, and resident proposals."},
        {"Atelier securite incendie", "Mar 28, 2026", "Bloc C", "Training", "41", "Practical fire safety workshop with live evacuation drills."},
        {"Marche de printemps", "Apr 02, 2026", "Patio central", "Social", "126", "A full day of local stands, food, and neighborhood activities."},
        {"Onboarding outils digitaux", "Apr 06, 2026", "Cowork lounge", "Workshop", "33", "Learn resident portal workflows and everyday digital tools."},
        {"Matinee fitness", "Apr 12, 2026", "Jardin deck", "Lifestyle", "57", "Community wellness session with breathing and mobility work."},
        {"Soiree networking", "Apr 18, 2026", "Sky lounge", "Networking", "72", "Connect with residents and propose collaborative initiatives."}
    };

    public EvenementPageView() {
        root = new VBox(26);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20, 0, 44, 0));
        root.setStyle("-fx-background-color: transparent;");

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
        Text dashTitle = text("Join the Action.\n6 Events Upcoming.", 48, true, tm.getAccentHex());
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

        VBox mainFace = new VBox(18);
        mainFace.setAlignment(Pos.TOP_LEFT);
        mainFace.setFillWidth(true);
        VBox extraFace = new VBox(14);
        extraFace.setAlignment(Pos.TOP_LEFT);
        extraFace.setFillWidth(true);
        extraFace.setVisible(false);
        extraFace.setManaged(false);

        Button hostTrigger = buildHostTrigger();
        hostTrigger.setOnAction(e -> switchFace(mainFace, extraFace));

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

        extraFace.getChildren().addAll(
            formHead,
            text("Share your vision with the community", 13, false, textMuted()),
            formRow("Event Title", "Sunset Rooftop Gathering"),
            twoColRow(
                formRow("Date and Time", "2026-04-25 17:30"),
                formRow("Event Type", "Social")
            ),
            formRow("Location", "Rooftop"),
            formRow("Description", "A sunset meetup with music and community networking."),
            twoColRow(
                formRow("Total Places", "80"),
                formRow("Initial Remaining", "80")
            ),
            gradientButton("Organize Now", 12, new Insets(13, 18, 13, 18))
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
        Button prevBtn = iconButton("<");
        prevBtn.setCursor(javafx.scene.Cursor.HAND);
        prevBtn.setOnAction(e -> System.out.println("Previous month"));
        
        Button nextBtn = iconButton(">");
        nextBtn.setCursor(javafx.scene.Cursor.HAND);
        nextBtn.setOnAction(e -> System.out.println("Next month"));
        
        head.getChildren().addAll(
            text("March 2026", 18, true, tm.getAccentHex()),
            spacer(),
            prevBtn,
            nextBtn
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
                cell.setCursor(javafx.scene.Cursor.HAND);

                if (row == 1 && col < startOffset) {
                    cell.getChildren().add(text("", 12, false, "rgba(255,255,255,0.2)"));
                } else if (day <= 31) {
                    boolean hasEvent = day == 22 || day == 28 || day == 2 || day == 18;
                    boolean active = day == 22;
                    int dayNum = day;

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

                    cell.setOnMouseClicked(e -> System.out.println("Clicked day: " + dayNum));
                    cell.setOnMouseEntered(e -> {
                        if (!active && !hasEvent) {
                            cell.setStyle(
                                "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.15) + ";" +
                                "-fx-background-radius: 10px;" +
                                "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), 0.25) + ";" +
                                "-fx-border-width: 1px;" +
                                "-fx-border-radius: 10px;"
                            );
                        }
                    });
                    cell.setOnMouseExited(e -> {
                        if (!active && !hasEvent) {
                            cell.setStyle(
                                "-fx-background-color: " + surfaceSoft() + ";" +
                                "-fx-background-radius: 10px;" +
                                "-fx-border-color: transparent;" +
                                "-fx-border-width: 1px;" +
                                "-fx-border-radius: 10px;"
                            );
                        }
                    });
                    
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
            text("Showing 1-3 of 6", 13, false, textMuted())
        );

        GridPane grid = new GridPane();
        grid.setHgap(48);
        grid.setVgap(48);
        rebuildEventsGrid(grid, section.getWidth());
        section.widthProperty().addListener((obs, oldW, newW) -> rebuildEventsGrid(grid, newW.doubleValue()));

        HBox pagination = new HBox(8);
        pagination.setAlignment(Pos.CENTER);
        pagination.getChildren().addAll(
            paginationBtn("<", false),
            paginationBtn("1", true),
            paginationBtn("2", false),
            paginationBtn(">", false)
        );

        section.getChildren().addAll(top, grid, pagination);
        return section;
    }

    private void rebuildEventsGrid(GridPane grid, double width) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();

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

        for (int i = 0; i < EVENTS.length; i++) {
            VBox card = eventCard(EVENTS[i]);
            GridPane.setFillWidth(card, true);
            card.setMaxWidth(Double.MAX_VALUE);
            card.setMinWidth(260);
            grid.add(card, i % cols, i / cols);
        }
    }

    private VBox eventCard(String[] e) {
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

        StackPane typeTag = tag(e[3]);
        StackPane.setAlignment(typeTag, Pos.TOP_RIGHT);
        StackPane.setMargin(typeTag, new Insets(18, 18, 0, 0));
        image.getChildren().add(typeTag);

        VBox body = new VBox(12);
        body.setPadding(new Insets(20));

        HBox meta = new HBox(14,
            text(e[1], 12, false, textMuted()),
            text(e[2], 12, false, textMuted())
        );
        Text title = text(e[0], 24, true, tm.getTextColor());
        title.wrappingWidthProperty().bind(card.widthProperty().subtract(52));
        Text desc = text(e[5], 14, false, textSoft());
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
            text(e[4], 18, true, tm.getTextColor()),
            text("Places Left", 11, false, textMuted())
        );
        Button details = gradientButton("View Details", 11, new Insets(8, 14, 8, 14));
        details.setOnAction(ev -> switchFace(mainFace, detailsFace));
        footer.getChildren().addAll(avail, spacer(), details);

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

        detailsFace.getChildren().addAll(
            detailsHead,
            detailImage,
            text(e[0], 20, true, tm.getTextColor()),
            text(e[5], 13, false, textSoft()),
            text("Hosted by Community Team", 12, false, textMuted()),
            gradientButton("Join Now", 12, new Insets(10, 14, 10, 14))
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
        btn.setCursor(javafx.scene.Cursor.HAND);
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
        b.setCursor(javafx.scene.Cursor.HAND);
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
        b.setCursor(javafx.scene.Cursor.HAND);
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
        b.setCursor(javafx.scene.Cursor.HAND);
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

    private VBox formRow(String label, String value) {
        VBox row = new VBox(4);
        Text l = text(label, 11, true, textSoft());
        StackPane field = new StackPane(text(value, 12, false, tm.getTextColor()));
        field.setAlignment(Pos.CENTER_LEFT);
        field.setPadding(new Insets(10, 12, 10, 12));
        field.setStyle(
            "-fx-background-color: " + surfaceSoft() + ";" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 16px;" +
            "-fx-border-radius: 16px;"
        );
        row.getChildren().addAll(l, field);
        return row;
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

