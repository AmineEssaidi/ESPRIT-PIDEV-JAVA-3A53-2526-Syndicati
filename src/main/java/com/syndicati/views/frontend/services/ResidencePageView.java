package com.syndicati.views.frontend.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.syndicati.MainApplication;
import com.syndicati.controllers.residence.MaintenanceController;
import com.syndicati.controllers.residence.ResidenceController;
import com.syndicati.interfaces.ViewInterface;
import com.syndicati.models.residence.Apartment;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.models.residence.Review;
import com.syndicati.models.residence.Residence;
import com.syndicati.services.DatabaseService;
import com.syndicati.services.events.DataUpdateBus;
import com.syndicati.controllers.user.user.UserController;
import com.syndicati.models.user.User;
import com.syndicati.services.mail.AsyncMailerService;
import com.syndicati.services.mail.SyndicatiEmailComposer;
import com.syndicati.services.residence.SmsService;
import com.syndicati.utils.image.ImageLoaderUtil;
import com.syndicati.utils.image.QRCodeUtil;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.utils.theme.ThemeManager;
import com.syndicati.utils.ui.HorizonDesignSystem;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Residence page mirrored from /templates/frontend/residence with switcher flow:
 * residences list -> apartments list -> apartment details.
 * Now integrated with database backend via ResidenceController.
 */
public class ResidencePageView implements ViewInterface {

    private final VBox root;
    private final ThemeManager tm = ThemeManager.getInstance();
    private final ResidenceController residenceController = new ResidenceController();
    private final MaintenanceController maintenanceController = new MaintenanceController();
    private final UserController userController = new UserController();
    private final AsyncMailerService mailerService = AsyncMailerService.getInstance();
    private final SmsService smsService = new SmsService();
    private final DatabaseService db = DatabaseService.getInstance();
    private final DataUpdateBus updates = DataUpdateBus.getInstance();
    private AutoCloseable updatesSubscription;

    private final StackPane switcher = new StackPane();
    private final VBox residenceFace = new VBox(28);
    private final VBox apartmentsFace = new VBox(20);
    private final VBox detailsFace = new VBox(20);
    private final VBox paginationBox = new VBox(12);
    private ChangeListener<Number> residenceWidthListener;
    private ChangeListener<Number> apartmentsWidthListener;

    private Integer selectedResidenceId = null;  // Use ID instead of index
    private Apartment selectedApartment;

    // Display records
    private record ResidenceDisplay(Integer id, String name, String address, Integer floors, Integer units, Integer blocks, Integer year, String image) {}
    private record ApartmentDisplay(Integer id, String type, String bloc, Integer floor, Boolean available, Double rent, Double area, Boolean parking, String description, String image) {}

    public ResidencePageView() {
        root = new VBox(28);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20, 0, 46, 0));
        root.setMaxWidth(Double.MAX_VALUE);
        root.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(
            buildHero(),
            buildShowcase(),
            buildPagination()
        );

        rebuildResidenceFace();
        switchToFace("main");

        updatesSubscription = updates.subscribe(DataUpdateBus.Topic.RESIDENCE, t -> {
            // Clear cache for residence/apartment lists so next rebuild reflects DB changes.
            db.clearCache("residence:list");
            if (selectedResidenceId != null) {
                db.clearCache("apartments:residence:" + selectedResidenceId);
            }
            javafx.application.Platform.runLater(() -> {
                try {
                    if (residenceFace.isVisible()) {
                        rebuildResidenceFace();
                    } else if (apartmentsFace.isVisible()) {
                        rebuildApartmentsFace();
                    } else if (detailsFace.isVisible()) {
                        rebuildDetailsFace();
                    }
                } catch (Exception ignored) {}
            });
        });
    }

    private StackPane buildHero() {
        StackPane hero = new StackPane();
        hero.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1800));
        hero.setMinHeight(600);
        hero.setPrefHeight(600);
        hero.setMaxHeight(Region.USE_COMPUTED_SIZE);
        hero.setPadding(new Insets(160, 64, 160, 64));
        hero.paddingProperty().bind(Bindings.createObjectBinding(
            () -> root.getWidth() < 980 ? new Insets(76, 28, 76, 28) : new Insets(160, 64, 160, 64),
            root.widthProperty()
        ));
        hero.setStyle(HorizonDesignSystem.webHeroPanel());

        Region glow = new Region();
        glow.setPrefSize(620, 620);
        glow.setStyle(
            "-fx-background-color: radial-gradient(center 50% 50%, radius 60%, " +
            tm.toRgba(tm.getAccentHex(), 0.28) + " 0%, " + tm.toRgba(tm.getAccentHex(), 0.00) + " 70%);"
        );
        glow.setTranslateX(260);
        glow.setTranslateY(-90);

        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER_LEFT);

        StackPane badge = pill("Premium Living", 12, 0.10, 0.30);
        badge.setStyle(HorizonDesignSystem.webAccentBadge());

        Text title = text("Luxury Living\nRedefined.", 88, true, "#ffffff");
        title.setFont(Font.font(MainApplication.getInstance().getBoldFontFamily(), FontWeight.BLACK, 88));
        // Keep hero typography stable during hover/reflow by avoiding live width jitter.
        title.setWrappingWidth(760);

        Text subtitle = text(
            "Explore our curated selection of high-end residences and apartments. Experience a new standard of comfort and elegance with Syndicati.",
            22,
            false,
            "rgba(255,255,255,0.50)"
        );
        subtitle.setWrappingWidth(720);

        Button cta = actionBtn("View Residences");

        content.getChildren().addAll(badge, title, subtitle, cta);
        hero.getChildren().addAll(glow, content);
        StackPane.setAlignment(content, Pos.CENTER_LEFT);
        return hero;
    }

    private StackPane buildShowcase() {
        StackPane wrap = new StackPane();
        wrap.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1600));
        wrap.setPadding(new Insets(40, 0, 0, 0));

        switcher.setMinHeight(720);
        switcher.setStyle("-fx-background-color: transparent;");

        residenceFace.setVisible(true);
        apartmentsFace.setVisible(false);
        detailsFace.setVisible(false);
        residenceFace.setManaged(true);
        apartmentsFace.setManaged(false);
        detailsFace.setManaged(false);

        switcher.getChildren().addAll(residenceFace, apartmentsFace, detailsFace);
        wrap.getChildren().add(switcher);
        return wrap;
    }

    private VBox buildPagination() {
        paginationBox.setAlignment(Pos.CENTER);
        paginationBox.prefWidthProperty().bind(Bindings.min(root.widthProperty().multiply(0.95), 1600));
        paginationBox.setPadding(new Insets(18, 0, 0, 0));

        HBox pages = new HBox(8,
            pageBtn("<", false),
            pageBtn("1", true),
            pageBtn("2", false),
            pageBtn(">", false)
        );
        pages.setAlignment(Pos.CENTER);

        Text info = text("Showing 6 of 6 residences - Page 1 of 2", 13, false, textMuted());
        paginationBox.getChildren().addAll(pages, info);
        return paginationBox;
    }

    private void rebuildResidenceFace() {
        residenceFace.getChildren().clear();

        VBox sectionLabel = new VBox(10);
        sectionLabel.setAlignment(Pos.CENTER);
        Text h = text("Our Residences", 50, true, tm.getAccentHex());
        Text s = text("Discover the perfect space that suits your lifestyle.", 18, false, textMuted());
        sectionLabel.getChildren().addAll(h, s);

        // Load residences (cache-first).
        @SuppressWarnings("unchecked")
        List<Residence> cachedResidences = db.getCache("residence:list");
        List<Residence> dbResidences = (cachedResidences != null) ? cachedResidences : residenceController.residences();
        if (cachedResidences == null) {
            db.putCache("residence:list", dbResidences);
        }
        List<ResidenceDisplay> displayResidences = dbResidences.stream()
            .map(r -> new ResidenceDisplay(
                r.getIdResidence(),
                r.getNameResidence(),
                r.getAddressResidence(),
                r.getNumberFloors(),
                r.getNumberApartments(),
                parseBlocksCount(r.getNumberBlocks()),
                2026,  // Default year
                r.getImageResidence()  // Load image from database
            ))
            .toList();

        GridPane cards = responsiveGrid();
        List<Node> cardNodes = new ArrayList<>();
        for (ResidenceDisplay res : displayResidences) {
            cardNodes.add(residenceCard(res));
        }
        rebuildResponsiveGrid(cards, cardNodes, residenceFace.getWidth(), 3, 2, 1);
        if (residenceWidthListener != null) {
            residenceFace.widthProperty().removeListener(residenceWidthListener);
        }
        residenceWidthListener = (obs, oldW, newW) ->
            rebuildResponsiveGrid(cards, cardNodes, newW.doubleValue(), 3, 2, 1);
        residenceFace.widthProperty().addListener(residenceWidthListener);

        residenceFace.getChildren().addAll(sectionLabel, cards);
    }

    private Integer parseBlocksCount(String blocksValue) {
        if (blocksValue == null || blocksValue.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(blocksValue.trim());
        } catch (NumberFormatException ex) {
            return (int) java.util.Arrays.stream(blocksValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .count();
        }
    }

    private Image resolveUploadsImage(String dbValue, String fallbackFolder) {
        if (dbValue == null || dbValue.isBlank()) {
            return null;
        }

        String path = dbValue.trim();

        // URL case (ImageKit)
        if (path.startsWith("http://") || path.startsWith("https://")) {
            Image urlImg = ImageLoaderUtil.loadImage(path);
            if (urlImg != null) return urlImg;

            // ImageKit down: fallback to local by filename.
            String filename = filenameFromUrl(path);
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

        // Try exact path first (already includes uploads/ or is a URL or absolute path)
        Image img = ImageLoaderUtil.loadImage(path);
        if (img != null) return img;

        // Try with uploads/ prefix (handles old stored values like "residence_images/x.jpg")
        if (!path.startsWith("uploads/") && !path.startsWith("uploads\\") && !path.startsWith("/")) {
            img = ImageLoaderUtil.loadImage("uploads/" + path);
            if (img != null) return img;
        }

        // Try with folder-specific path when DB stores just filename
        if (fallbackFolder != null && !fallbackFolder.isBlank()) {
            img = ImageLoaderUtil.loadImage("uploads/" + fallbackFolder + "/" + path);
            if (img != null) return img;
        }

        return null;
    }

    private String filenameFromUrl(String url) {
        if (url == null || url.isBlank()) return null;
        String u = url.trim();
        int q = u.indexOf('?');
        if (q >= 0) u = u.substring(0, q);
        int lastSlash = u.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < u.length() - 1) return u.substring(lastSlash + 1);
        return u;
    }

    private Rectangle roundedClip(Region target, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(target.widthProperty());
        clip.heightProperty().bind(target.heightProperty());
        return clip;
    }

    /**
     * Create an "object-fit: cover" ImageView that fills the container,
     * crops via viewport to preserve aspect ratio, and clips to rounded corners.
     */
    private ImageView coverImage(Image image, Region container, double radius) {
        ImageView iv = new ImageView(image);
        iv.setSmooth(true);
        iv.setPreserveRatio(false);
        iv.fitWidthProperty().bind(container.widthProperty());
        iv.fitHeightProperty().bind(container.heightProperty());

        // Clip image to rounded container.
        iv.setClip(roundedClip(container, radius));

        Runnable updateViewport = () -> {
            double vw = container.getWidth();
            double vh = container.getHeight();
            if (vw <= 1 || vh <= 1) return;

            double iw = image.getWidth();
            double ih = image.getHeight();
            if (iw <= 1 || ih <= 1) return;

            double viewRatio = vw / vh;
            double imgRatio = iw / ih;

            if (imgRatio > viewRatio) {
                // Image is wider -> crop width
                double newW = ih * viewRatio;
                double x = (iw - newW) / 2.0;
                iv.setViewport(new Rectangle2D(x, 0, newW, ih));
            } else {
                // Image is taller -> crop height
                double newH = iw / viewRatio;
                double y = (ih - newH) / 2.0;
                iv.setViewport(new Rectangle2D(0, y, iw, newH));
            }
        };

        // Update once and whenever container size or image metadata changes.
        container.layoutBoundsProperty().addListener((obs, o, n) -> updateViewport.run());
        image.widthProperty().addListener((obs, o, n) -> updateViewport.run());
        image.heightProperty().addListener((obs, o, n) -> updateViewport.run());
        updateViewport.run();
        return iv;
    }

    private VBox residenceCard(ResidenceDisplay r) {
        VBox card = new VBox();
        card.setStyle(HorizonDesignSystem.webServiceCard(32, false));

        StackPane media = new StackPane();
        media.setMinHeight(280);
        media.setPrefHeight(280);
        media.setMaxHeight(280);
        media.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.22) + ";" +
            "-fx-background-radius: 32px 32px 0 0;"
        );

        Image image = resolveUploadsImage(r.image, "residence_images");
        if (image != null && !image.isError()) {
            media.getChildren().add(coverImage(image, media, 32));
        }

        VBox body = new VBox(14);
        body.setPadding(new Insets(26, 22, 22, 22));

        body.getChildren().addAll(
            miniType("Residence"),
            text(r.name, 30, true, "#ffffff"),
            iconLine("Map", r.address)
        );

        HBox stats = new HBox(12,
            statBox(String.valueOf(r.floors), "Floors"),
            statBox(String.valueOf(r.units), "Units"),
            statBox(String.valueOf(r.blocks), "Blocks")
        );
        stats.setPadding(new Insets(16, 0, 0, 0));
        stats.setStyle("-fx-border-color: " + borderSoft() + " transparent transparent transparent; -fx-border-width: 1px 0 0 0;");

        HBox actions = new HBox(10);
        Button seeApts = mainBtn("See Apartments");
        seeApts.setOnAction(e -> openApartments(r.id));
        Button export = mainBtn("Export PDF");
        export.setDisable(true);
        export.setOpacity(0.7);
        HBox.setHgrow(seeApts, Priority.ALWAYS);
        HBox.setHgrow(export, Priority.ALWAYS);
        seeApts.setMaxWidth(Double.MAX_VALUE);
        export.setMaxWidth(Double.MAX_VALUE);
        actions.getChildren().addAll(seeApts, export);

        Region bar = new Region();
        bar.setPrefHeight(4);
        bar.setStyle("-fx-background-color: " + tm.getEffectiveAccentGradient() + "; -fx-background-radius: 0 0 32px 32px;");
        bar.setScaleX(0);

        body.getChildren().addAll(stats, actions);
        card.getChildren().addAll(media, body, bar);

        addCardHover(card, bar);
        return card;
    }

    private void openApartments(Integer residenceId) {
        selectedResidenceId = residenceId;
        rebuildApartmentsFace();
        switchToFace("apartments");
    }

    private void rebuildApartmentsFace() {
        apartmentsFace.getChildren().clear();
        
        // Fetch residence and apartments (cache-first where possible)
        Residence residence = residenceController.residenceById(selectedResidenceId).orElse(null);
        
        if (residence == null) {
            apartmentsFace.getChildren().add(text("Residence not found", 20, false, textMuted()));
            return;
        }

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(
            backBtn(() -> switchToFace("main")),
            text("Apartments in " + residence.getNameResidence(), 34, true, "#ffffff")
        );

        // Load apartments from database and convert to display records
        @SuppressWarnings("unchecked")
        List<Apartment> cachedApts = db.getCache("apartments:residence:" + selectedResidenceId);
        List<Apartment> apartments = (cachedApts != null) ? cachedApts : residenceController.apartmentsByResidence(selectedResidenceId);
        if (cachedApts == null) {
            db.putCache("apartments:residence:" + selectedResidenceId, apartments);
        }
        List<ApartmentDisplay> displayApartments = apartments.stream()
            .map(a -> new ApartmentDisplay(
                a.getIdApartment(),
                a.getTypeApartment(),
                "Block A",  // Placeholder
                1,  // Placeholder floor
                a.getAvailable() != null && a.getAvailable() == 1,
                a.getRentalPrice() != null ? a.getRentalPrice() : 0.0,
                a.getArea() != null ? a.getArea() : 0.0,
                a.getParking() != null && a.getParking() == 1,
                a.getApartmentInfo(),
                a.getImageApartment()  // Load image from database
            ))
            .toList();
        
        GridPane cards = responsiveGrid();
        List<Node> nodes = new ArrayList<>();
        for (ApartmentDisplay apt : displayApartments) {
            nodes.add(apartmentCard(apt));
        }
        rebuildResponsiveGrid(cards, nodes, apartmentsFace.getWidth(), 3, 2, 1);
        if (apartmentsWidthListener != null) {
            apartmentsFace.widthProperty().removeListener(apartmentsWidthListener);
        }
        apartmentsWidthListener = (obs, oldW, newW) ->
            rebuildResponsiveGrid(cards, nodes, newW.doubleValue(), 3, 2, 1);
        apartmentsFace.widthProperty().addListener(apartmentsWidthListener);

        apartmentsFace.getChildren().addAll(header, cards);
    }

    private VBox apartmentCard(ApartmentDisplay apt) {
        VBox card = new VBox();
        card.setStyle(HorizonDesignSystem.webServiceCard(32, false));

        StackPane media = new StackPane();
        media.setMinHeight(240);
        media.setPrefHeight(240);
        media.setMaxHeight(240);
        media.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.18) + ";" +
            "-fx-background-radius: 32px 32px 0 0;"
        );

        Image image = resolveUploadsImage(apt.image, "appartement_images");
        if (image != null && !image.isError()) {
            media.getChildren().add(coverImage(image, media, 32));
        }

        StackPane availability = pill(apt.available ? "Available" : "Rented", 11, 0.12, 0.25);
        StackPane.setAlignment(availability, Pos.TOP_RIGHT);
        StackPane.setMargin(availability, new Insets(18, 18, 0, 0));
        media.getChildren().add(availability);

        VBox body = new VBox(10);
        body.setPadding(new Insets(20));
        body.getChildren().addAll(
            text("Apartment " + apt.type, 26, true, "#ffffff"),
            infoRow("Block", apt.bloc),
            infoRow("Area", apt.area + " m²")
        );

        HBox features = new HBox(8);
        if (apt.parking) {
            features.getChildren().add(featureTag("Parking"));
        }
        if (apt.available) {
            features.getChildren().add(featureTag(apt.rent + " TND"));
        }

        Button seeDetails = mainBtn("Voir les details");
        seeDetails.setOnAction(e -> {
            selectedApartment = residenceController.apartmentById(apt.id).orElse(null);
            rebuildDetailsFace();
            switchToFace("details");
        });
        seeDetails.setMaxWidth(Double.MAX_VALUE);

        body.getChildren().addAll(features, line(), seeDetails);
        card.getChildren().addAll(media, body);
        addLift(card);
        return card;
    }

    private void rebuildDetailsFace() {
        detailsFace.getChildren().clear();
        
        if (selectedApartment == null) {
            detailsFace.getChildren().add(text("Apartment not found", 20, false, textMuted()));
            return;
        }

        Residence residence = residenceController.residenceById(selectedResidenceId)
            .orElse(null);
        
        if (residence == null) {
            detailsFace.getChildren().add(text("Residence not found", 20, false, textMuted()));
            return;
        }

        Apartment apt = selectedApartment;
        String rating = maintenanceController.formattedApartmentScore(apt.getIdApartment());
        Integer reviewCount = maintenanceController.apartmentReviewCount(apt.getIdApartment());
        boolean needsMaintenance = maintenanceController.apartmentNeedsMaintenance(apt.getIdApartment());
        Maintenance latestMaintenance = maintenanceController.latestMaintenance(apt.getIdApartment()).orElse(null);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(
            backBtn(() -> switchToFace("apartments")),
            text("Details de l'Appartement", 34, true, "#ffffff")
        );

        HBox top = new HBox(16);
        top.setAlignment(Pos.TOP_LEFT);

        StackPane image = new StackPane();
        // Fixed height prevents layout feedback loop (ImageView fitHeight bound to container height).
        image.setMinHeight(400);
        image.setPrefHeight(400);
        image.setMaxHeight(400);
        image.setMinWidth(620);
        image.setPrefWidth(620);
        image.setMaxWidth(620);
        HBox.setHgrow(image, Priority.NEVER);
        image.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.20) + ";" +
            "-fx-background-radius: 24px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 24px;"
        );

        // Load apartment image if available (from details page, get fresh data)
        Apartment fullApt = residenceController.apartmentById(selectedApartment.getIdApartment()).orElse(selectedApartment);
        Image loadedImage = resolveUploadsImage(fullApt.getImageApartment(), "appartement_images");
        if (loadedImage != null && !loadedImage.isError()) {
            image.getChildren().add(coverImage(loadedImage, image, 24));
        }

        VBox info = new VBox(12);
        info.setPadding(new Insets(22));
        info.setStyle(
            "-fx-background-color: rgba(10,12,18,0.90);" +
            "-fx-background-radius: 22px;" +
            "-fx-border-color: rgba(255,255,255,0.14);" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 22px;" +
            "-fx-effect: dropshadow(one-pass-box, rgba(0,0,0,0.30), 18, 0.12, 0, 6);"
        );
        HBox.setHgrow(info, Priority.ALWAYS);

        info.getChildren().addAll(
            pill("Type " + (apt.getTypeApartment() != null ? apt.getTypeApartment() : "Apartment"), 11, 0.10, 0.25),
            text(residence.getNameResidence(), 30, true, "#ffffff"),
            infoRow("Loyer", (apt.getRentalPrice() != null ? apt.getRentalPrice() : 0) + " TND"),
            infoRow("Surface", (apt.getArea() != null ? apt.getArea() : 0) + " m2"),
            infoRow("Type", apt.getTypeApartment() != null ? apt.getTypeApartment() : "N/A"),
            infoRow("Rating", (rating.equals("No ratings") ? rating : (rating + "/10")) + " (" + reviewCount + " reviews)")
        );
        info.getChildren().add(buildApartmentInfoPanel(apt.getApartmentInfo()));
        if (apt.getParking() != null && apt.getParking() == 1) {
            info.getChildren().add(featureTag("Parking inclus"));
        }
        info.getChildren().add(featureTag(needsMaintenance ? "Maintenance required" : "Maintenance up to date"));
        if (latestMaintenance != null) {
            if (latestMaintenance.getLastMaintenanceDate() != null) {
                info.getChildren().add(infoRow("Last Maintenance", latestMaintenance.getLastMaintenanceDate()));
            }
            
            VBox aiSection = new VBox(8);
            aiSection.setPadding(new Insets(10));
            aiSection.setStyle(HorizonDesignSystem.webSectionCard(12, false) + "-fx-padding: 10;");
            
            Text aiTitle = text("AI Maintenance Insights", 14, true, tm.getAccentHex());
            Text aiText = text(
                (latestMaintenance.getAiRecommendation() != null && !latestMaintenance.getAiRecommendation().isBlank()) 
                ? latestMaintenance.getAiRecommendation() 
                : "No AI recommendation yet.", 
                12, false, "#ffffff"
            );
            aiText.setWrappingWidth(340);
            
            Button genBtn = new Button("Generate with AI");
            genBtn.setStyle(HorizonDesignSystem.buttonPrimary() + "-fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 8px; -fx-border-radius: 8px;");
            HorizonDesignSystem.installButtonMotion(genBtn);
            
            final Maintenance finalMaint = latestMaintenance;
            genBtn.setOnAction(e -> {
                genBtn.setDisable(true);
                genBtn.setText("Generating...");
                
                db.runAsync(() -> {
                    String rec = maintenanceController.generateMistralRecommendation(apt, finalMaint);
                    javafx.application.Platform.runLater(() -> {
                        if (maintenanceController.maintenanceUpdate(finalMaint.getIdMaintenance(), 
                                finalMaint.getGeneralCondition(), finalMaint.getPlumbingCondition(),
                                finalMaint.getElectricalCondition(), finalMaint.getHeatingCondition(),
                                finalMaint.getDescription(), rec)) {
                            rebuildDetailsFace();
                        } else {
                            genBtn.setDisable(false);
                            genBtn.setText("Generate with AI (Failed)");
                            aiText.setText(rec);
                        }
                    });
                });
            });
            
            aiSection.getChildren().addAll(aiTitle, aiText, genBtn);
            info.getChildren().add(aiSection);
        }
        info.getChildren().add(buildReviewEditor(apt));

        top.getChildren().addAll(image, info);

        VBox recommendations = new VBox(12);
        recommendations.getChildren().add(text("Appartements Similaires", 28, true, "#ffffff"));
        FlowPane recGrid = new FlowPane();
        recGrid.setHgap(12);
        recGrid.setVgap(12);

        // Get apartments for similar recommendations
        @SuppressWarnings("unchecked")
        List<Apartment> cachedApts = db.getCache("apartments:residence:" + selectedResidenceId);
        List<Apartment> apartments = (cachedApts != null) ? cachedApts : residenceController.apartmentsByResidence(selectedResidenceId);
        if (cachedApts == null) {
            db.putCache("apartments:residence:" + selectedResidenceId, apartments);
        }
        for (Apartment rec : apartments) {
            if (!rec.getIdApartment().equals(apt.getIdApartment())) {
                recGrid.getChildren().add(recommendationCard(rec));
            }
        }

        VBox contact = buildContactSection();

        detailsFace.getChildren().addAll(header, top, recommendations, recGrid, contact);
    }

    private VBox buildReviewEditor(Apartment apartment) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 0, 0, 0));
        box.setStyle("-fx-border-color: " + borderSoft() + " transparent transparent transparent; -fx-border-width: 1px 0 0 0;");

        Text title = text("Your review", 15, true, "#ffffff");
        Text hint = text("Rate this apartment from 0 to 10 stars.", 12, false, textMuted());
        Text selectedScore = text("Selected: 0/10", 12, true, tm.getAccentHex());
        Text status = text("", 12, false, textMuted());

        Integer currentUserId = getCurrentUserId();
        Optional<Review> existing = currentUserId != null
            ? maintenanceController.userReviewForApartment(currentUserId, apartment.getIdApartment())
            : Optional.empty();
        int initialScore = existing.map(Review::getScore).orElse(0);
        final int[] selected = {Math.max(0, Math.min(10, initialScore))};
        selectedScore.setText("Selected: " + selected[0] + "/10");

        HBox stars = new HBox(4);
        List<Button> starButtons = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            final int score = i;
            Button star = new Button("★");
            star.setOnAction(e -> {
                selected[0] = score;
                updateStarButtons(starButtons, selected[0]);
                selectedScore.setText("Selected: " + selected[0] + "/10");
                status.setText("");
            });
            starButtons.add(star);
        }
        stars.getChildren().addAll(starButtons);
        updateStarButtons(starButtons, selected[0]);

        Button clear = new Button("Set 0");
        clear.setStyle(
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-text-fill: " + textSoft() + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;" +
            "-fx-padding: 6 10 6 10;"
        );
        clear.setOnAction(e -> {
            selected[0] = 0;
            updateStarButtons(starButtons, 0);
            selectedScore.setText("Selected: 0/10");
            status.setText("");
        });

        Button submit = mainBtn(existing.isPresent() ? "Update review" : "Submit review");
        submit.setOnAction(e -> {
            Integer userId = getCurrentUserId();
            if (userId == null || userId <= 0) {
                status.setText("Log in required to submit a review.");
                status.setFill(Color.web("#ffb4b4"));
                return;
            }
            boolean ok;
            Optional<Review> current = maintenanceController.userReviewForApartment(userId, apartment.getIdApartment());
            if (current.isPresent()) {
                ok = maintenanceController.reviewUpdate(current.get().getIdReview(), selected[0]);
            } else {
                ok = maintenanceController.reviewCreate(userId, apartment.getIdApartment(), selected[0]) > 0;
            }
            if (ok) {
                status.setText("Review saved: " + selected[0] + "/10");
                status.setFill(Color.web("#9ff0b0"));
                rebuildDetailsFace();
                updates.publish(DataUpdateBus.Topic.RESIDENCE);
            } else {
                status.setText("Could not save review. Please retry.");
                status.setFill(Color.web("#ffb4b4"));
            }
        });
        submit.setMaxWidth(Double.MAX_VALUE);

        HBox actions = new HBox(8, clear, submit);
        HBox.setHgrow(submit, Priority.ALWAYS);
        box.getChildren().addAll(title, hint, stars, selectedScore, actions, status);
        return box;
    }

    private VBox buildApartmentInfoPanel(String rawInfo) {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(10));
        panel.setStyle(
            "-fx-background-color: " + (tm.isDarkMode() ? "rgba(255,255,255,0.08)" : "rgba(15,23,42,0.08)") + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + (tm.isDarkMode() ? "rgba(255,255,255,0.14)" : borderSoft()) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );

        panel.getChildren().add(text("Apartment details", 13, true, tm.isDarkMode() ? "#ffffff" : tm.getAccentHex()));

        Map<String, String> parsed = parseApartmentInfo(rawInfo);
        if (parsed.isEmpty()) {
            panel.getChildren().add(text("No additional details available.", 12, false, textMuted()));
            return panel;
        }

        FlowPane tags = new FlowPane();
        tags.setHgap(8);
        tags.setVgap(8);
        for (Map.Entry<String, String> entry : parsed.entrySet()) {
            tags.getChildren().add(metaTag(entry.getKey(), entry.getValue()));
        }
        panel.getChildren().add(tags);
        return panel;
    }

    private Map<String, String> parseApartmentInfo(String rawInfo) {
        Map<String, String> out = new LinkedHashMap<>();
        if (rawInfo == null || rawInfo.isBlank()) {
            return out;
        }

        String value = rawInfo.trim();
        if (value.startsWith("{") && value.endsWith("}")) {
            value = value.substring(1, value.length() - 1);
        }

        String[] pairs = value.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length < 2) continue;

            String key = kv[0].trim().replace("\"", "");
            String val = kv[1].trim().replace("\"", "");
            if (key.isBlank() || val.isBlank()) continue;

            String label = switch (key.toLowerCase()) {
                case "bloc" -> "Block";
                case "floor" -> "Floor";
                case "number" -> "Unit";
                case "parking" -> "Parking";
                case "disponible", "available" -> "Available";
                default -> Character.toUpperCase(key.charAt(0)) + key.substring(1);
            };

            if ("true".equalsIgnoreCase(val)) val = "Yes";
            if ("false".equalsIgnoreCase(val)) val = "No";
            out.put(label, val);
        }
        return out;
    }

    private StackPane metaTag(String label, String value) {
        Text t = text(label + ": " + value, 12, true, tm.isDarkMode() ? "#ffffff" : "#1f2937");
        StackPane tag = new StackPane(t);
        tag.setPadding(new Insets(6, 10, 6, 10));
        tag.setStyle(
            "-fx-background-color: " + (tm.isDarkMode() ? "rgba(255,255,255,0.10)" : tm.toRgba(tm.getAccentHex(), 0.12)) + ";" +
            "-fx-border-color: " + (tm.isDarkMode() ? "rgba(255,255,255,0.20)" : tm.toRgba(tm.getAccentHex(), 0.26)) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-radius: 8px;"
        );
        return tag;
    }

    private void updateStarButtons(List<Button> buttons, int selectedScore) {
        for (int i = 0; i < buttons.size(); i++) {
            Button b = buttons.get(i);
            boolean active = i < selectedScore;
            b.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + (active ? tm.getAccentHex() : "rgba(255,255,255,0.34)") + ";" +
                "-fx-font-size: 18px;" +
                "-fx-padding: 0 2 0 2;"
            );
        }
    }

    private Integer getCurrentUserId() {
        var user = SessionManager.getInstance().getCurrentUser();
        return user != null ? user.getIdUser() : null;
    }


    private VBox recommendationCard(Apartment apt) {
        VBox card = new VBox(8);
        card.setPrefWidth(240);
        card.setPadding(new Insets(10));
        card.setStyle(
            "-fx-background-color: " + surfaceCard() + ";" +
            "-fx-background-radius: 18px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 18px;"
        );

        StackPane image = new StackPane();
        image.setMinHeight(120);
        image.setPrefHeight(120);
        image.setMaxHeight(120);
        image.setStyle("-fx-background-color: " + tm.toRgba(tm.getAccentHex(), 0.16) + "; -fx-background-radius: 14px;");

        // Load apartment image if available
        if (apt.getImageApartment() != null && !apt.getImageApartment().isBlank()) {
            Image loadedImage = resolveUploadsImage(apt.getImageApartment(), "appartement_images");
            if (loadedImage != null && !loadedImage.isError()) {
                image.getChildren().add(coverImage(loadedImage, image, 14));
            }
        }

        Button view = mainBtn("Consulter");
        view.setStyle(view.getStyle() + "-fx-font-size: 11px; -fx-padding: 8 10 8 10;");
        view.setOnAction(e -> {
            selectedApartment = apt;
            rebuildDetailsFace();
            switchToFace("details");
        });

        card.getChildren().addAll(
            image,
            text((apt.getTypeApartment() != null ? apt.getTypeApartment() : "Apartment") + " - " + 
                 (apt.getRentalPrice() != null ? apt.getRentalPrice() : 0) + " TND", 15, true, "#ffffff"),
            view
        );
        return card;
    }

    private VBox buildContactSection() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(32));
        box.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, rgba(255,255,255,0.05), rgba(255,255,255,0.02));" +
            "-fx-background-radius: 32px;" +
            "-fx-border-color: rgba(255,255,255,0.12);" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 32px;"
        );

        HBox row = new HBox(32);
        row.setAlignment(Pos.TOP_LEFT);

        VBox left = new VBox(16);
        HBox.setHgrow(left, Priority.ALWAYS);
        
        Text title = text("Contacter le propriétaire", 32, true, "#ffffff");
        Text subtitle = text("Envoyez une demande directe à l'hôte pour réserver ou poser des questions.", 15, false, textMuted());
        subtitle.setWrappingWidth(500);

        left.getChildren().addAll(title, subtitle);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        String defaultName = currentUser != null ? (currentUser.getFirstName() + " " + currentUser.getLastName()) : "";
        String defaultEmail = currentUser != null ? currentUser.getEmailUser() : "";

        TextField nameInput = input("Votre nom complet");
        nameInput.setText(defaultName);
        TextField emailInput = input("Votre email");
        emailInput.setText(defaultEmail);
        
        TextArea msgInput = new TextArea();
        msgInput.setPromptText("Écrivez votre message ici...");
        msgInput.setPrefRowCount(4);
        msgInput.setWrapText(true);
        msgInput.setStyle(
            "-fx-control-inner-background: " + surfaceSoft() + ";" +
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + tm.getTextColor() + ";" +
            "-fx-prompt-text-fill: " + textMuted() + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-radius: 12px;" +
            "-fx-padding: 8;"
        );

        Button sendBtn = mainBtn("Envoyer le message");
        sendBtn.setPrefHeight(50);
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        
        sendBtn.setOnAction(e -> {
            String n = nameInput.getText().trim();
            String em = emailInput.getText().trim();
            String m = msgInput.getText().trim();

            if (n.isEmpty() || em.isEmpty() || m.isEmpty()) {
                GlobalNotificationPillManager.error("Contact", "Veuillez remplir tous les champs.");
                return;
            }

            if (selectedApartment != null && selectedApartment.getIdUser() != null) {
                userController.userById(selectedApartment.getIdUser()).ifPresentOrElse(owner -> {
                    // Send to owner
                    String ownerSubject = "Nouvelle demande pour votre appartement";
                    String ownerBody = SyndicatiEmailComposer.genericMessage(
                        owner.getFirstName(),
                        "Vous avez reçu une nouvelle demande de " + n + " (" + em + ") pour votre appartement.\n\nMessage:\n" + m
                    );
                    mailerService.sendHtmlAsync(owner.getEmailUser(), ownerSubject, ownerBody);

                    // Send copy to sender
                    String senderSubject = "Copie de votre demande - Syndicati";
                    String senderBody = SyndicatiEmailComposer.genericMessage(
                        n,
                        "Ceci est une copie de votre message envoyé au propriétaire.\n\nVotre message:\n" + m
                    );
                    mailerService.sendHtmlAsync(em, senderSubject, senderBody);

                    GlobalNotificationPillManager.show("Message Envoyé", "Votre message a été transmis au propriétaire. Une copie vous a été envoyée.", GlobalNotificationPillManager.Kind.SUCCESS);
                    msgInput.clear();
                }, () -> {
                    GlobalNotificationPillManager.error("Contact", "Impossible de trouver les coordonnées du propriétaire.");
                });
            }
        });

        left.getChildren().addAll(nameInput, emailInput, msgInput, sendBtn);

        VBox right = new VBox(20);
        right.setAlignment(Pos.TOP_CENTER);
        right.setPrefWidth(280);
        right.setPadding(new Insets(10, 0, 0, 0));

        StackPane qrFrame = new StackPane();
        qrFrame.setPrefSize(220, 220);
        qrFrame.setMaxSize(220, 220);
        qrFrame.setStyle(
            "-fx-background-color: " + (tm.isDarkMode() ? "#f8fafc" : "white") + ";" +
            "-fx-background-radius: 24px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 30, 0, 0, 10);" +
            "-fx-padding: 20;"
        );

        // Generate QR Code for WhatsApp
        ImageView qrView = new ImageView();
        if (selectedApartment != null && selectedApartment.getIdUser() != null) {
            userController.userById(selectedApartment.getIdUser()).ifPresent(owner -> {
                String phone = owner.getPhone() != null ? owner.getPhone() : "+21600000000";
                String normalizedPhone = phone.replaceAll("[^0-9]", "");
                String message = "Bonjour, je vous contacte à propos de votre appartement sur Syndicati.";
                String whatsappUri = "https://wa.me/" + normalizedPhone + "?text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
                
                Image qrImg = QRCodeUtil.generateQRCode(whatsappUri, 180, 180);
                if (qrImg != null) {
                    qrView.setImage(qrImg);
                }
            });
        }
        qrFrame.getChildren().add(qrView);

        Text qrHint = text("Scannez pour contacter sur WhatsApp", 13, true, "#ffffff");
        Text qrSub = text("Contact rapide via mobile", 11, false, textMuted());

        Button whatsappBtn = new Button("Contacter sur WhatsApp");
        whatsappBtn.setStyle(
            "-fx-background-color: #25D366;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 14px;" +
            "-fx-border-color: rgba(255,255,255,0.15);" +
            "-fx-border-radius: 14px;" +
            "-fx-padding: 10 24 10 24;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        whatsappBtn.setMaxWidth(200);

        whatsappBtn.setOnAction(e -> {
            if (selectedApartment != null && selectedApartment.getIdUser() != null) {
                userController.userById(selectedApartment.getIdUser()).ifPresentOrElse(owner -> {
                    String phone = owner.getPhone() != null ? owner.getPhone() : "";
                    if (phone.isEmpty()) {
                        GlobalNotificationPillManager.error("WhatsApp", "Le propriétaire n'a pas de numéro de téléphone.");
                        return;
                    }
                    String normalizedPhone = phone.replaceAll("[^0-9]", "");
                    String message = "Bonjour " + owner.getFirstName() + ", je suis intéressé par votre appartement sur Syndicati.";
                    String whatsappUrl = "https://wa.me/" + normalizedPhone + "?text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
                    MainApplication.getInstance().getHostServices().showDocument(whatsappUrl);
                }, () -> GlobalNotificationPillManager.error("WhatsApp", "Propriétaire introuvable."));
            }
        });

        right.getChildren().addAll(qrFrame, new VBox(4, qrHint, qrSub) {{ setAlignment(Pos.CENTER); }}, whatsappBtn);

        row.getChildren().addAll(left, right);
        box.getChildren().add(row);
        
        // Simple entrance animation
        box.setOpacity(0);
        box.setTranslateY(20);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(800), box);
        ft.setToValue(1);
        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(Duration.millis(800), box);
        tt.setToY(0);
        new javafx.animation.ParallelTransition(ft, tt).play();

        return box;
    }

    private void switchToFace(String face) {
        boolean main = "main".equals(face);
        boolean apartments = "apartments".equals(face);
        boolean details = "details".equals(face);

        residenceFace.setVisible(main);
        residenceFace.setManaged(main);
        apartmentsFace.setVisible(apartments);
        apartmentsFace.setManaged(apartments);
        detailsFace.setVisible(details);
        detailsFace.setManaged(details);

        paginationBox.setVisible(main);
        paginationBox.setManaged(main);
    }

    private FlowPane responsivePane() {
        FlowPane pane = new FlowPane();
        pane.setHgap(20);
        pane.setVgap(20);
        pane.setAlignment(Pos.TOP_LEFT);
        pane.setMaxWidth(Double.MAX_VALUE);
        pane.prefWrapLengthProperty().bind(root.widthProperty().subtract(120));
        return pane;
    }

    private GridPane responsiveGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setMaxWidth(Double.MAX_VALUE);
        return grid;
    }

    private void rebuildResponsiveGrid(GridPane grid, List<Node> cards, double width, int desktopCols, int tabletCols, int mobileCols) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();

        // Keep 2 columns through minimum app sizes; only drop to 1 on genuinely narrow widths.
        int cols = width < 700 ? mobileCols : (width < 1400 ? tabletCols : desktopCols);
        for (int i = 0; i < cols; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(100.0 / cols);
            c.setHgrow(Priority.ALWAYS);
            c.setFillWidth(true);
            grid.getColumnConstraints().add(c);
        }

        for (int i = 0; i < cards.size(); i++) {
            Node card = cards.get(i);
            if (card instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMinWidth(260);
            }
            GridPane.setFillWidth(card, true);
            grid.add(card, i % cols, i / cols);
        }
    }

    private void bindResponsiveCards(FlowPane pane, List<Node> cards, int desktopCols, int tabletCols, int mobileCols) {
        pane.getChildren().setAll(cards);
        for (Node node : cards) {
            if (node instanceof Region region) {
                region.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> {
                    // Use the largest effective width to avoid accidental early collapse to one column.
                    double available = Math.max(320, Math.max(pane.getWidth(), pane.getPrefWrapLength()));
                    int cols = available >= 1500 ? desktopCols : (available >= 820 ? tabletCols : mobileCols);
                    double totalGap = (cols - 1) * 20;
                    return Math.max(260, (available - totalGap) / cols);
                }, pane.widthProperty(), pane.prefWrapLengthProperty()));
            }
        }
    }

    private HBox iconLine(String iconText, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(
            text(iconText + ":", 13, true, tm.getAccentHex()),
            text(value, 14, false, textMuted())
        );
        return row;
    }

    private HBox infoRow(String label, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle(
            "-fx-background-color: " + (tm.isDarkMode() ? "rgba(255,255,255,0.09)" : "rgba(15,23,42,0.08)") + ";" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: " + (tm.isDarkMode() ? "rgba(255,255,255,0.06)" : "rgba(15,23,42,0.12)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 10px;"
        );
        String labelColor = tm.isDarkMode() ? "rgba(255,230,240,0.96)" : "rgba(30,41,59,0.92)";
        row.getChildren().addAll(
            text(label + ":", 13, true, labelColor),
            text(value, 13, false, textSoft())
        );
        return row;
    }

    private StackPane statBox(String value, String label) {
        VBox v = new VBox(2,
            text(value, 16, true, "#ffffff"),
            text(label, 10, true, textMuted())
        );
        v.setAlignment(Pos.CENTER);
        StackPane box = new StackPane(v);
        box.setPadding(new Insets(8, 10, 8, 10));
        box.setStyle("-fx-background-color: " + surfaceSoft() + "; -fx-background-radius: 10px;");
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private StackPane miniType(String text) {
        StackPane p = new StackPane(text(text, 11, true, textMuted()));
        p.setPadding(new Insets(8, 14, 8, 14));
        p.setMaxWidth(StackPane.USE_PREF_SIZE);
        p.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 100px; -fx-border-color: rgba(255,255,255,0.05); -fx-border-width: 1px; -fx-border-radius: 100px;");
        return p;
    }

    private StackPane featureTag(String value) {
        StackPane tag = new StackPane(text(value, 11, true, "#ffffff"));
        tag.setPadding(new Insets(6, 10, 6, 10));
        tag.setStyle(
            "-fx-background-color: " + (tm.isDarkMode() ? "rgba(255,255,255,0.08)" : tm.toRgba(tm.getAccentHex(), 0.10)) + ";" +
            "-fx-border-color: " + (tm.isDarkMode() ? "rgba(255,255,255,0.16)" : tm.toRgba(tm.getAccentHex(), 0.22)) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );
        return tag;
    }

    private StackPane pill(String label, int size, double bgAlpha, double borderAlpha) {
        StackPane p = new StackPane(text(label, size, true, "#ffffff"));
        p.setPadding(new Insets(7, 12, 7, 12));
        p.setMaxWidth(StackPane.USE_PREF_SIZE);
        p.setStyle(
            "-fx-background-color: " + tm.toRgba(tm.getAccentHex(), bgAlpha) + ";" +
            "-fx-border-color: " + tm.toRgba(tm.getAccentHex(), borderAlpha) + ";" +
            "-fx-border-width: 1px;" +
            "-fx-background-radius: 100px;" +
            "-fx-border-radius: 100px;"
        );
        return p;
    }

    private Button actionBtn(String label) {
        Button b = new Button(label);
        b.setStyle(HorizonDesignSystem.buttonPrimary() + "-fx-font-size: 14px;-fx-padding: 12 28 12 28;-fx-background-radius: 12px;-fx-border-radius: 12px;");
        HorizonDesignSystem.installButtonMotion(b);
        return b;
    }

    private Button mainBtn(String label) {
        Button b = new Button(label);
        b.setStyle(HorizonDesignSystem.buttonPrimary() + "-fx-font-size: 12px;-fx-padding: 10 12 10 12;-fx-background-radius: 12px;-fx-border-radius: 12px;");
        HorizonDesignSystem.installButtonMotion(b);
        return b;
    }

    private Button pageBtn(String label, boolean active) {
        Button b = new Button(label);
        if (active) {
            b.setStyle(
                "-fx-background-color: " + tm.getEffectiveAccentGradient() + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: 700;" +
                "-fx-min-width: 48px; -fx-min-height: 48px;" +
                "-fx-background-radius: 12px;"
            );
        } else {
            b.setStyle(HorizonDesignSystem.buttonGhost() + "-fx-min-width: 48px; -fx-min-height: 48px;-fx-background-radius: 12px; -fx-border-radius: 12px;");
        }
        HorizonDesignSystem.installButtonMotion(b);
        return b;
    }

    private HBox backBtn(Runnable onClick) {
        Button back = new Button("<");
        back.setOnAction(e -> onClick.run());
        back.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-border-color: " + borderSoft() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-text-fill: " + textSoft() + ";" +
            "-fx-font-weight: 700;" +
            "-fx-min-width: 44px; -fx-min-height: 44px;" +
            "-fx-background-radius: 999px; -fx-border-radius: 999px;"
        );
        HBox row = new HBox(12, back);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private TextField input(String placeholder) {
        TextField f = new TextField();
        f.setPromptText(placeholder);
        f.setStyle(HorizonDesignSystem.input());
        return f;
    }

    private Region line() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: " + borderSoft() + ";");
        return r;
    }

    private String surfaceStrong() {
        return tm.isDarkMode() ? "#000000" : "#f8fafc";
    }

    private String surfaceCard() {
        return HorizonDesignSystem.surface();
    }

    private String surfaceSoft() {
        return HorizonDesignSystem.surfaceSoft();
    }

    private String borderSoft() {
        return HorizonDesignSystem.borderStrong();
    }

    private String textSoft() {
        return tm.isDarkMode() ? "rgba(255,255,255,0.93)" : "rgba(15,23,42,0.90)";
    }

    private String textMuted() {
        return HorizonDesignSystem.mutedText();
    }

    private String shell(double radius, String inner, double inset) {
        return HorizonDesignSystem.webSectionCard((int) Math.round(radius), false);
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

    private void addCardHover(VBox card, Region accentBar) {
        card.setOnMouseEntered(e -> {
            card.setStyle(HorizonDesignSystem.webServiceCard(32, true));
            TranslateTransition lift = new TranslateTransition(Duration.millis(260), card);
            lift.setToY(-15);
            lift.setInterpolator(HorizonDesignSystem.WEB_POP);
            lift.play();

            ScaleTransition scale = new ScaleTransition(Duration.millis(260), card);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.setInterpolator(HorizonDesignSystem.WEB_POP);
            scale.play();

            ScaleTransition bar = new ScaleTransition(Duration.millis(220), accentBar);
            bar.setToX(1.0);
            bar.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(HorizonDesignSystem.webServiceCard(32, false));
            TranslateTransition lift = new TranslateTransition(Duration.millis(220), card);
            lift.setToY(0);
            lift.setInterpolator(HorizonDesignSystem.WEB_EASE);
            lift.play();

            ScaleTransition scale = new ScaleTransition(Duration.millis(220), card);
            scale.setToX(1.0);
            scale.setToY(1.0);
            scale.setInterpolator(HorizonDesignSystem.WEB_EASE);
            scale.play();

            ScaleTransition bar = new ScaleTransition(Duration.millis(200), accentBar);
            bar.setToX(0.0);
            bar.play();
        });
    }

    private void addLift(VBox card) {
        card.setOnMouseEntered(e -> {
            TranslateTransition t = new TranslateTransition(Duration.millis(220), card);
            t.setToY(-8);
            t.play();
        });
        card.setOnMouseExited(e -> {
            TranslateTransition t = new TranslateTransition(Duration.millis(220), card);
            t.setToY(0);
            t.play();
        });
    }

    @Override
    public Pane getRoot() {
        return root;
    }

    @Override
    public void cleanup() {
        if (residenceWidthListener != null) {
            residenceFace.widthProperty().removeListener(residenceWidthListener);
            residenceWidthListener = null;
        }
        if (apartmentsWidthListener != null) {
            apartmentsFace.widthProperty().removeListener(apartmentsWidthListener);
            apartmentsWidthListener = null;
        }
        if (updatesSubscription != null) {
            try { updatesSubscription.close(); } catch (Exception ignored) {}
            updatesSubscription = null;
        }
    }
}

