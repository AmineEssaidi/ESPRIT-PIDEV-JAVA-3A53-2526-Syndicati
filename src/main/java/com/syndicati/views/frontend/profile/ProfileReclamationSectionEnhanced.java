package com.syndicati.views.frontend.profile;

import com.syndicati.controllers.syndicat.ReclamationController;
import com.syndicati.models.syndicat.Reclamation;
import com.syndicati.models.syndicat.Reponse;
import com.syndicati.models.user.User;
import com.syndicati.utils.session.SessionManager;
import javafx.scene.control.Alert;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enhanced Reclamation section with status filters, detail modal, color-coded statuses.
 * Matches Horizon profile 1:1 with interactive filtering and detailed complaint views.
 */
public class ProfileReclamationSectionEnhanced {
    
    private final VBox root = new VBox(16);
    private final ReclamationController reclamationController;
    private final StackPane switcherContainer = new StackPane();
    
    // Switcher faces
    private final VBox faceListView = new VBox(16);
    private final VBox faceDetailsView = new VBox(16);
    
    // State
    private String currentStatusFilter = "all";
    private List<Reclamation> allReclamations = new ArrayList<>();
    private List<Reclamation> filteredReclamations = new ArrayList<>();
    private Reclamation selectedReclamation = null;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 3;
    
    // Content containers
    private final VBox reclamationList = new VBox(10);
    private final HBox paginationContainer = new HBox(8);

    public ProfileReclamationSectionEnhanced() {
        this.reclamationController = new ReclamationController();
        buildLayout();
    }

    private void buildLayout() {
        root.setPadding(new Insets(16, 0, 0, 0));
        
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.03); -fx-border-color: rgba(255, 255, 255, 0.1); -fx-border-width: 1; -fx-background-radius: 20; -fx-border-radius: 20;");
        card.setPrefHeight(600);
        
        // Switcher container
        switcherContainer.setPrefHeight(500);
        VBox.setVgrow(switcherContainer, Priority.ALWAYS);
        
        buildListView();
        buildDetailsView();
        
        // Add details view first (back), list view on top (front)
        switcherContainer.getChildren().addAll(faceDetailsView, faceListView);
        faceDetailsView.setVisible(false);
        faceDetailsView.setManaged(false);
        
        card.getChildren().add(switcherContainer);
        root.getChildren().add(card);
        
        loadReclamations();
    }

    private void buildListView() {
        faceListView.setStyle("-fx-background-color: transparent;");
        faceListView.setPadding(new Insets(20));
        
        // Header
        Label title = new Label("⚠️ Your Reclamations");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        faceListView.getChildren().add(title);
        
        // Status filters
        HBox statusFilters = createStatusFilters();
        faceListView.getChildren().add(statusFilters);
        
        // Reclamation list with scroll
        ScrollPane listScroll = new ScrollPane();
        listScroll.setStyle("-fx-control-inner-background: transparent; -fx-padding: 0;");
        listScroll.setFitToWidth(true);
        listScroll.setPrefHeight(400);
        listScroll.setContent(reclamationList);
        VBox.setVgrow(listScroll, Priority.ALWAYS);
        faceListView.getChildren().add(listScroll);
        
        // Pagination
        paginationContainer.setAlignment(Pos.CENTER);
        paginationContainer.setPadding(new Insets(12, 0, 0, 0));
        faceListView.getChildren().add(paginationContainer);
    }

    private void buildDetailsView() {
        faceDetailsView.setStyle("-fx-background-color: rgba(20, 20, 25, 0.98);");
        faceDetailsView.setPadding(new Insets(20));
        
        // Back button and title
        HBox backHeader = new HBox(12);
        backHeader.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-padding: 8; -fx-font-size: 14; -fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 50%; -fx-border-radius: 50%;");
        backBtn.setOnAction(e -> showListView());
        
        Label detailTitle = new Label("Complaint Details");
        detailTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        detailTitle.setTextFill(Color.WHITE);
        
        backHeader.getChildren().addAll(backBtn, detailTitle);
        faceDetailsView.getChildren().add(backHeader);
        
        // Detail content
        ScrollPane detailScroll = new ScrollPane();
        detailScroll.setStyle("-fx-control-inner-background: transparent; -fx-padding: 0;");
        detailScroll.setFitToWidth(true);
        
        VBox detailContent = new VBox(12);
        detailContent.setPadding(new Insets(16, 0, 0, 0));
        Label placeholder = new Label("Select a complaint to view details");
        placeholder.setTextFill(Color.color(1, 1, 1, 0.5));
        detailContent.getChildren().add(placeholder);
        
        detailScroll.setContent(detailContent);
        VBox.setVgrow(detailScroll, Priority.ALWAYS);
        faceDetailsView.getChildren().add(detailScroll);
    }

    private HBox createStatusFilters() {
        HBox filters = new HBox(8);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(8, 0, 0, 0));
        
        Button allBtn = createStatusButton("All", true);
        Button pendingBtn = createStatusButton("⏳ Pending", false);
        Button confirmedBtn = createStatusButton("✓ Confirmed", false);
        Button refusedBtn = createStatusButton("✗ Refused", false);
        
        allBtn.setOnAction(e -> switchStatus("all", allBtn, pendingBtn, confirmedBtn, refusedBtn));
        pendingBtn.setOnAction(e -> switchStatus("Pending", allBtn, pendingBtn, confirmedBtn, refusedBtn));
        confirmedBtn.setOnAction(e -> switchStatus("Confirmed", allBtn, pendingBtn, confirmedBtn, refusedBtn));
        refusedBtn.setOnAction(e -> switchStatus("Refused", allBtn, pendingBtn, confirmedBtn, refusedBtn));
        
        filters.getChildren().addAll(allBtn, pendingBtn, confirmedBtn, refusedBtn);
        return filters;
    }

    private Button createStatusButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-padding: 6 12 6 12; " +
            "-fx-background-color: " + (active ? "rgba(99, 102, 241, 0.2);" : "rgba(255,255,255,0.05);") +
            "-fx-text-fill: " + (active ? "white;" : "rgba(255,255,255,0.7);") +
            "-fx-border-color: " + (active ? "rgba(99, 102, 241, 0.3);" : "rgba(255,255,255,0.1);") +
            "-fx-border-width: 1; " +
            "-fx-background-radius: 999; " +
            "-fx-border-radius: 999; " +
            "-fx-font-size: 11; " +
            "-fx-font-weight: " + (active ? "bold;" : "normal;")
        );
        return btn;
    }

    private void switchStatus(String status, Button... buttons) {
        currentStatusFilter = status;
        currentPage = 0;
        
        // Update button styles
        updateStatusButtons(status, buttons);
        applyFilters();
        renderReclamationList();
    }

    private void updateStatusButtons(String activeStatus, Button[] buttons) {
        String[] statuses = {"all", "Pending", "Confirmed", "Refused"};
        for (int i = 0; i < buttons.length; i++) {
            boolean isActive = activeStatus.equals(statuses[i]);
            buttons[i].setStyle(
                "-fx-padding: 6 12 6 12; " +
                "-fx-background-color: " + (isActive ? "rgba(99, 102, 241, 0.2);" : "rgba(255,255,255,0.05);") +
                "-fx-text-fill: " + (isActive ? "white;" : "rgba(255,255,255,0.7);") +
                "-fx-border-color: " + (isActive ? "rgba(99, 102, 241, 0.3);" : "rgba(255,255,255,0.1);") +
                "-fx-border-width: 1; " +
                "-fx-background-radius: 999; " +
                "-fx-border-radius: 999; " +
                "-fx-font-size: 11; " +
                "-fx-font-weight: " + (isActive ? "bold;" : "normal;")
            );
        }
    }

    private void loadReclamations() {
        allReclamations = new ArrayList<>(reclamationController.reclamations());
        applyFilters();
        renderReclamationList();
    }

    private void applyFilters() {
        if (currentStatusFilter.equals("all")) {
            filteredReclamations = new ArrayList<>(allReclamations);
        } else {
            filteredReclamations = allReclamations.stream()
                .filter(r -> currentStatusFilter.equalsIgnoreCase(r.getStatutReclamation()))
                .collect(Collectors.toList());
        }
    }

    private void renderReclamationList() {
        reclamationList.getChildren().clear();
        paginationContainer.getChildren().clear();
        
        int maxPage = (int) Math.ceil((double) filteredReclamations.size() / ITEMS_PER_PAGE);
        if (maxPage == 0) maxPage = 1;
        
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, filteredReclamations.size());
        
        if (start >= filteredReclamations.size()) {
            Label noData = new Label("No reclamations found");
            noData.setTextFill(Color.color(1, 1, 1, 0.5));
            reclamationList.getChildren().add(noData);
            return;
        }
        
        // Add items with animation
        for (int i = start; i < end; i++) {
            VBox item = createReclamationItem(filteredReclamations.get(i));
            reclamationList.getChildren().add(item);
            animateItemEntry(item);
        }
        
        // Add pagination buttons
        for (int page = 0; page < maxPage; page++) {
            Button pageBtn = new Button(String.valueOf(page + 1));
            final int pageNum = page;
            boolean isActive = page == currentPage;
            pageBtn.setStyle(
                "-fx-padding: 4 10 4 10; " +
                "-fx-background-color: " + (isActive ? "rgba(99, 102, 241, 0.8);" : "rgba(255,255,255,0.05);") +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 6; " +
                "-fx-font-size: 10;"
            );
            pageBtn.setOnAction(e -> {
                currentPage = pageNum;
                renderReclamationList();
            });
            paginationContainer.getChildren().add(pageBtn);
        }
    }

    private VBox createReclamationItem(Reclamation reclamation) {
        VBox item = new VBox(8);
        item.setStyle(
            "-fx-background-color: rgba(255, 150, 100, 0.05); " +
            "-fx-border-color: rgba(255, 150, 100, 0.1); " +
            "-fx-border-width: 1; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-padding: 12; " +
            "-fx-cursor: hand;"
        );
        item.setOnMouseClicked(e -> showDetailsView(reclamation));
        
        // Hover effect
        item.setOnMouseEntered(e -> item.setStyle(
            "-fx-background-color: rgba(255, 150, 100, 0.1); " +
            "-fx-border-color: rgba(255, 150, 100, 0.2); " +
            "-fx-border-width: 1; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-padding: 12; " +
            "-fx-cursor: hand;"
        ));
        item.setOnMouseExited(e -> item.setStyle(
            "-fx-background-color: rgba(255, 150, 100, 0.05); " +
            "-fx-border-color: rgba(255, 150, 100, 0.1); " +
            "-fx-border-width: 1; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-padding: 12; " +
            "-fx-cursor: hand;"
        ));
        
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label icon = new Label("⚠️");
        icon.setFont(Font.font(16));
        
        VBox titleSection = new VBox(4);
        Label title = new Label(reclamation.getTitreReclamations() != null ? reclamation.getTitreReclamations() : "—");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        title.setTextFill(Color.WHITE);
        
        String dateStr = reclamation.getDateReclamation() != null 
            ? reclamation.getDateReclamation().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            : "—";
        Label date = new Label("📅 " + dateStr);
        date.setFont(Font.font("Segoe UI", 10));
        date.setTextFill(Color.color(1, 1, 1, 0.5));
        
        titleSection.getChildren().addAll(title, date);
        HBox.setHgrow(titleSection, Priority.ALWAYS);
        
        String statusText = getStatusDisplay(reclamation.getStatutReclamation());
        Label status = new Label(statusText);
        status.setFont(Font.font("Segoe UI", 9));
        status.setStyle("-fx-text-fill: white; -fx-background-color: " + getStatusColor(reclamation.getStatutReclamation()) + "; -fx-padding: 4 8 4 8; -fx-background-radius: 6;");
        
        Label chevron = new Label("→");
        chevron.setFont(Font.font(14));
        chevron.setTextFill(Color.color(1, 1, 1, 0.5));
        
        header.getChildren().addAll(icon, titleSection, status, chevron);
        item.getChildren().add(header);
        
        // Description preview
        if (reclamation.getDescReclamation() != null && !reclamation.getDescReclamation().isEmpty()) {
            String preview = reclamation.getDescReclamation().length() > 80 
                ? reclamation.getDescReclamation().substring(0, 80) + "..." 
                : reclamation.getDescReclamation();
            Label desc = new Label(preview);
            desc.setFont(Font.font("Segoe UI", 10));
            desc.setTextFill(Color.color(1, 1, 1, 0.6));
            desc.setWrapText(true);
            item.getChildren().add(desc);
        }
        
        return item;
    }

    private void reloadResponses(VBox responsesBox, Reclamation reclamation) {
        responsesBox.getChildren().clear();
        List<Reponse> responses = reclamationController.reponsesByReclamation(reclamation);

        if (responses == null || responses.isEmpty()) {
            Label empty = new Label("No responses yet.");
            empty.setTextFill(Color.color(1, 1, 1, 0.5));
            responsesBox.getChildren().add(empty);
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        Integer currentUserId = currentUser == null ? null : currentUser.getIdUser();

        for (Reponse response : responses) {
            boolean mine = currentUserId != null
                && response.getUser() != null
                && response.getUser().getIdUser() != null
                && currentUserId.equals(response.getUser().getIdUser());
            responsesBox.getChildren().add(buildResponseBubble(response, mine));
        }
    }

    private VBox buildResponseBubble(Reponse response, boolean mine) {
        VBox bubble = new VBox(4);
        bubble.setPadding(new Insets(8, 10, 8, 10));
        bubble.setMaxWidth(700);
        bubble.setStyle(
            "-fx-background-color: " + (mine ? "rgba(99,102,241,0.22);" : "rgba(255,255,255,0.06);") +
            "-fx-border-color: rgba(255,255,255,0.12);" +
            "-fx-border-width: 1;" +
            "-fx-background-radius: 10; -fx-border-radius: 10;"
        );

        String author = "Unknown";
        if (response.getUser() != null) {
            String first = response.getUser().getFirstName() == null ? "" : response.getUser().getFirstName();
            String last = response.getUser().getLastName() == null ? "" : response.getUser().getLastName();
            String full = (first + " " + last).trim();
            author = full.isBlank() ? (response.getUser().getEmailUser() == null ? "Unknown" : response.getUser().getEmailUser()) : full;
        }

        Label meta = new Label(author + " • " + (response.getCreatedAt() == null
            ? "—"
            : response.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))));
        meta.setTextFill(Color.color(1, 1, 1, 0.55));
        meta.setFont(Font.font("Segoe UI", 10));

        Label msg = new Label(response.getMessageReponse() == null ? "" : response.getMessageReponse());
        msg.setWrapText(true);
        msg.setTextFill(Color.WHITE);
        msg.setFont(Font.font("Segoe UI", 12));

        bubble.getChildren().addAll(meta, msg);

        HBox row = new HBox(bubble);
        row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox wrap = new VBox(row);
        wrap.setFillWidth(true);
        return wrap;
    }

    private void submitResponse(Reclamation reclamation, TextArea responseInput, VBox responsesBox, Label feedback) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || currentUser.getIdUser() == null) {
            feedback.setTextFill(Color.web("#fca5a5"));
            feedback.setText("You must be logged in to respond.");
            return;
        }

        String text = responseInput.getText() == null ? "" : responseInput.getText().trim();
        if (text.isEmpty()) {
            feedback.setTextFill(Color.web("#fca5a5"));
            feedback.setText("Response cannot be empty.");
            return;
        }

        Integer createdId = reclamationController.reponseCreate("Response", text, null, reclamation, currentUser);
        if (createdId != null && createdId > 0) {
            responseInput.clear();
            feedback.setTextFill(Color.web("#86efac"));
            feedback.setText("Response sent.");
            reloadResponses(responsesBox, reclamation);
            return;
        }

        feedback.setTextFill(Color.web("#fca5a5"));
        feedback.setText("Failed to send response.");
    }

    private void exportReclamationPdf(Reclamation reclamation) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Reclamation PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        String safeTitle = reclamation.getTitreReclamations() == null || reclamation.getTitreReclamations().isBlank()
            ? "reclamation"
            : reclamation.getTitreReclamations().replaceAll("[^a-zA-Z0-9-_]", "_");
        chooser.setInitialFileName(safeTitle + "_" + (reclamation.getIdReclamations() == null ? "details" : reclamation.getIdReclamations()) + ".pdf");

        File file = chooser.showSaveDialog(root.getScene() == null ? null : root.getScene().getWindow());
        if (file == null) {
            return;
        }

        List<Reponse> responses = reclamationController.reponsesByReclamation(reclamation);
        try (PDDocument document = new PDDocument()) {
            final float margin = 42f;
            final float pageWidth = PDRectangle.LETTER.getWidth();
            final float pageHeight = PDRectangle.LETTER.getHeight();
            final float contentWidth = pageWidth - (margin * 2f);

            final java.awt.Color bgDark = new java.awt.Color(18, 18, 18);
            final java.awt.Color panelDark = new java.awt.Color(30, 30, 30);
            final java.awt.Color border = new java.awt.Color(51, 51, 51);
            final java.awt.Color accent = new java.awt.Color(255, 75, 92);
            final java.awt.Color textLight = new java.awt.Color(245, 245, 245);
            final java.awt.Color textMuted = new java.awt.Color(160, 160, 160);

            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            PDPageContentStream stream = new PDPageContentStream(document, page);
            paintPdfBackground(stream, page, bgDark);

            float y = pageHeight - margin;

            pdfWriteText(stream, PDType1Font.HELVETICA_BOLD, 13, margin, y, "Syndicate Hub", accent);
            y -= 18;

            String pdfTitle = sanitizePdfText(reclamation.getTitreReclamations() == null ? "Reclamation" : reclamation.getTitreReclamations());
            List<String> titleLines = wrapTextByWidth(pdfTitle, PDType1Font.HELVETICA_BOLD, 21, contentWidth);
            for (String line : titleLines) {
                pdfWriteText(stream, PDType1Font.HELVETICA_BOLD, 21, margin, y, line, textLight);
                y -= 24;
            }

            stream.setStrokingColor(accent);
            stream.setLineWidth(2f);
            stream.moveTo(margin, y + 8);
            stream.lineTo(pageWidth - margin, y + 8);
            stream.stroke();
            y -= 18;

            float metaBoxHeight = 116f;
            stream.setNonStrokingColor(panelDark);
            stream.addRect(margin, y - metaBoxHeight, contentWidth, metaBoxHeight);
            stream.fill();
            stream.setStrokingColor(border);
            stream.addRect(margin, y - metaBoxHeight, contentWidth, metaBoxHeight);
            stream.stroke();

            float rowHeight = metaBoxHeight / 3f;
            float labelColWidth = 160f;
            for (int i = 1; i <= 2; i++) {
                float yLine = y - (rowHeight * i);
                stream.moveTo(margin, yLine);
                stream.lineTo(margin + contentWidth, yLine);
                stream.stroke();
            }
            stream.moveTo(margin + labelColWidth, y);
            stream.lineTo(margin + labelColWidth, y - metaBoxHeight);
            stream.stroke();

            float row1Y = y - 22;
            float row2Y = y - rowHeight - 22;
            float row3Y = y - (rowHeight * 2f) - 22;
            float valueX = margin + labelColWidth + 12;

            pdfWriteText(stream, PDType1Font.HELVETICA_BOLD, 10, margin + 12, row1Y, "DATE SUBMITTED", textMuted);
            pdfWriteText(
                stream,
                PDType1Font.HELVETICA,
                11,
                valueX,
                row1Y,
                sanitizePdfText(reclamation.getDateReclamation() == null
                    ? "-"
                    : reclamation.getDateReclamation().format(DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a"))),
                textLight
            );

            pdfWriteText(stream, PDType1Font.HELVETICA_BOLD, 10, margin + 12, row2Y, "CURRENT STATUS", textMuted);
            String statusPlain = getStatusDisplayPlain(reclamation.getStatutReclamation());
            float badgeX = valueX;
            float badgeY = row2Y - 11;
            float badgeW = Math.max(80f, textWidth(PDType1Font.HELVETICA_BOLD, 10, statusPlain) + 18f);
            float badgeH = 16f;
            stream.setNonStrokingColor(new java.awt.Color(50, 24, 28));
            stream.addRect(badgeX, badgeY, badgeW, badgeH);
            stream.fill();
            stream.setStrokingColor(accent);
            stream.addRect(badgeX, badgeY, badgeW, badgeH);
            stream.stroke();
            pdfWriteText(stream, PDType1Font.HELVETICA_BOLD, 10, badgeX + 8, row2Y - 1, statusPlain, accent);

            pdfWriteText(stream, PDType1Font.HELVETICA_BOLD, 10, margin + 12, row3Y, "SUBMITTED BY", textMuted);
            String submittedBy = "N/A";
            if (reclamation.getUser() != null) {
                String first = reclamation.getUser().getFirstName() == null ? "" : reclamation.getUser().getFirstName();
                String last = reclamation.getUser().getLastName() == null ? "" : reclamation.getUser().getLastName();
                String full = (first + " " + last).trim();
                String email = reclamation.getUser().getEmailUser() == null ? "" : reclamation.getUser().getEmailUser();
                submittedBy = full.isBlank() ? email : (email.isBlank() ? full : full + " (" + email + ")");
            }
            List<String> byLines = wrapTextByWidth(sanitizePdfText(submittedBy), PDType1Font.HELVETICA, 11, contentWidth - labelColWidth - 24f);
            if (!byLines.isEmpty()) {
                pdfWriteText(stream, PDType1Font.HELVETICA, 11, valueX, row3Y, byLines.get(0), textLight);
            }

            y = y - metaBoxHeight - 24;

            pdfWriteText(stream, PDType1Font.HELVETICA_BOLD, 13, margin, y, "CASE DESCRIPTION", accent);
            y -= 14;

            stream.setStrokingColor(border);
            stream.moveTo(margin, y);
            stream.lineTo(pageWidth - margin, y);
            stream.stroke();
            y -= 10;

            List<String> descLines = wrapTextByWidth(
                sanitizePdfText(reclamation.getDescReclamation() == null ? "-" : reclamation.getDescReclamation()),
                PDType1Font.HELVETICA,
                11,
                contentWidth - 24f
            );
            float descHeight = Math.max(64f, 20f + (descLines.size() * 14f));

            if (y - descHeight < 70f) {
                stream.close();
                page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);
                stream = new PDPageContentStream(document, page);
                paintPdfBackground(stream, page, bgDark);
                y = pageHeight - margin;
            }

            stream.setNonStrokingColor(panelDark);
            stream.addRect(margin, y - descHeight, contentWidth, descHeight);
            stream.fill();
            stream.setStrokingColor(border);
            stream.addRect(margin, y - descHeight, contentWidth, descHeight);
            stream.stroke();
            stream.setNonStrokingColor(accent);
            stream.addRect(margin, y - descHeight, 4f, descHeight);
            stream.fill();

            float descTextY = y - 20;
            for (String line : descLines) {
                pdfWriteText(stream, PDType1Font.HELVETICA, 11, margin + 12, descTextY, line, textLight);
                descTextY -= 14;
            }
            y = y - descHeight - 24;

            pdfWriteText(stream, PDType1Font.HELVETICA_BOLD, 13, margin, y, "RESPONSES", accent);
            y -= 14;
            stream.setStrokingColor(border);
            stream.moveTo(margin, y);
            stream.lineTo(pageWidth - margin, y);
            stream.stroke();
            y -= 12;

            if (responses == null || responses.isEmpty()) {
                pdfWriteText(stream, PDType1Font.HELVETICA_OBLIQUE, 11, margin, y, "No responses.", textMuted);
                y -= 20;
            } else {
                for (Reponse response : responses) {
                    String author = "Unknown";
                    if (response.getUser() != null) {
                        String first = response.getUser().getFirstName() == null ? "" : response.getUser().getFirstName();
                        String last = response.getUser().getLastName() == null ? "" : response.getUser().getLastName();
                        String full = (first + " " + last).trim();
                        author = full.isBlank() ? (response.getUser().getEmailUser() == null ? "Unknown" : response.getUser().getEmailUser()) : full;
                    }

                    String when = response.getCreatedAt() == null
                        ? "-"
                        : response.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
                    String header = sanitizePdfText(author + " - " + when);
                    List<String> msgLines = wrapTextByWidth(
                        sanitizePdfText(response.getMessageReponse() == null ? "" : response.getMessageReponse()),
                        PDType1Font.HELVETICA,
                        11,
                        contentWidth - 26f
                    );

                    float bubbleHeight = 28f + (msgLines.size() * 14f);
                    if (y - bubbleHeight < 70f) {
                        stream.close();
                        page = new PDPage(PDRectangle.LETTER);
                        document.addPage(page);
                        stream = new PDPageContentStream(document, page);
                        paintPdfBackground(stream, page, bgDark);
                        y = pageHeight - margin;

                        pdfWriteText(stream, PDType1Font.HELVETICA_BOLD, 13, margin, y, "RESPONSES (CONTINUED)", accent);
                        y -= 14;
                        stream.setStrokingColor(border);
                        stream.moveTo(margin, y);
                        stream.lineTo(pageWidth - margin, y);
                        stream.stroke();
                        y -= 12;
                    }

                    stream.setNonStrokingColor(new java.awt.Color(24, 24, 24));
                    stream.addRect(margin, y - bubbleHeight, contentWidth, bubbleHeight);
                    stream.fill();
                    stream.setStrokingColor(border);
                    stream.addRect(margin, y - bubbleHeight, contentWidth, bubbleHeight);
                    stream.stroke();

                    pdfWriteText(stream, PDType1Font.HELVETICA_BOLD, 10, margin + 10, y - 14, header, textMuted);

                    float msgY = y - 30;
                    for (String line : msgLines) {
                        pdfWriteText(stream, PDType1Font.HELVETICA, 11, margin + 10, msgY, line, textLight);
                        msgY -= 14;
                    }

                    y -= bubbleHeight + 10;
                }
            }

            pdfWriteText(
                stream,
                PDType1Font.HELVETICA,
                8,
                margin,
                26,
                "Generated securely by the Syndicate Central System",
                textMuted
            );

            stream.close();

            document.save(file);
            Alert ok = new Alert(Alert.AlertType.INFORMATION, "PDF exported successfully.");
            ok.setHeaderText(null);
            ok.showAndWait();
        } catch (IOException ex) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Unable to export PDF: " + ex.getMessage());
            err.setHeaderText(null);
            err.showAndWait();
        }
    }

    private List<String> wrapForPdf(String text, int maxChars) {
        List<String> wrapped = new ArrayList<>();
        if (text == null || text.isBlank()) {
            wrapped.add("");
            return wrapped;
        }

        String[] paragraphs = text.split("\\r?\\n");
        for (String paragraph : paragraphs) {
            String p = paragraph == null ? "" : paragraph.trim();
            if (p.isEmpty()) {
                wrapped.add("");
                continue;
            }

            StringBuilder line = new StringBuilder();
            for (String word : p.split("\\s+")) {
                if (line.length() == 0) {
                    line.append(word);
                } else if (line.length() + 1 + word.length() <= maxChars) {
                    line.append(" ").append(word);
                } else {
                    wrapped.add(line.toString());
                    line = new StringBuilder(word);
                }
            }
            if (line.length() > 0) {
                wrapped.add(line.toString());
            }
        }
        return wrapped;
    }

    private void paintPdfBackground(PDPageContentStream stream, PDPage page, java.awt.Color color) throws IOException {
        PDRectangle box = page.getMediaBox();
        stream.setNonStrokingColor(color);
        stream.addRect(0, 0, box.getWidth(), box.getHeight());
        stream.fill();
    }

    private void pdfWriteText(PDPageContentStream stream, PDType1Font font, float size, float x, float y, String text, java.awt.Color color) throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        stream.setNonStrokingColor(color);
        stream.newLineAtOffset(x, y);
        stream.showText(sanitizePdfText(text));
        stream.endText();
    }

    private List<String> wrapTextByWidth(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String safe = sanitizePdfText(text);
        if (safe.isBlank()) {
            lines.add("");
            return lines;
        }

        String[] paragraphs = safe.split("\\r?\\n");
        for (String paragraph : paragraphs) {
            String p = paragraph == null ? "" : paragraph.trim();
            if (p.isEmpty()) {
                lines.add("");
                continue;
            }

            StringBuilder current = new StringBuilder();
            for (String word : p.split("\\s+")) {
                String candidate = current.length() == 0 ? word : current + " " + word;
                if (textWidth(font, fontSize, candidate) <= maxWidth) {
                    current.setLength(0);
                    current.append(candidate);
                } else {
                    if (current.length() > 0) {
                        lines.add(current.toString());
                        current.setLength(0);
                        current.append(word);
                    } else {
                        lines.add(word);
                    }
                }
            }

            if (current.length() > 0) {
                lines.add(current.toString());
            }
        }
        return lines;
    }

    private float textWidth(PDType1Font font, float fontSize, String text) throws IOException {
        String safe = sanitizePdfText(text);
        return font.getStringWidth(safe) / 1000f * fontSize;
    }

    private String sanitizePdfText(String text) {
        if (text == null) {
            return "";
        }
        String sanitized = text
            .replace('\t', ' ')
            .replace('\r', ' ')
            .replace('\n', ' ')
            .trim();

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < sanitized.length(); i++) {
            char c = sanitized.charAt(i);
            if (c >= 32 && c <= 255) {
                out.append(c);
            } else {
                out.append('?');
            }
        }
        return out.toString();
    }

    private String getStatusDisplayPlain(String status) {
        return switch (status) {
            case "Pending", "en_attente" -> "Pending";
            case "Confirmed", "active", "termine" -> "Confirmed";
            case "Refused", "refuse" -> "Refused";
            default -> status == null || status.isBlank() ? "Pending" : status;
        };
    }

    private String getStatusDisplay(String status) {
        return switch (status) {
            case "Pending" -> "⏳ Pending";
            case "en_attente" -> "⏳ Pending";
            case "Confirmed" -> "✓ Confirmed";
            case "active" -> "✓ Confirmed";
            case "termine" -> "✓ Confirmed";
            case "Refused" -> "✗ Refused";
            case "refuse" -> "✗ Refused";
            default -> status;
        };
    }

    private String getStatusColor(String status) {
        return switch (status) {
            case "Pending" -> "rgba(255, 180, 100, 0.2)";
            case "en_attente" -> "rgba(255, 180, 100, 0.2)";
            case "Confirmed" -> "rgba(100, 255, 150, 0.2)";
            case "active" -> "rgba(100, 255, 150, 0.2)";
            case "termine" -> "rgba(100, 255, 150, 0.2)";
            case "Refused" -> "rgba(255, 100, 100, 0.2)";
            case "refuse" -> "rgba(255, 100, 100, 0.2)";
            default -> "rgba(150, 150, 150, 0.2)";
        };
    }

    private void animateItemEntry(VBox item) {
        item.setOpacity(0);
        item.setScaleY(0.9);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), item);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(300), item);
        scale.setFromY(0.9);
        scale.setToY(1);
        
        fade.play();
        scale.play();
    }

    private void showDetailsView(Reclamation reclamation) {
        selectedReclamation = reclamation;
        // Populate detail content
        ScrollPane detailScroll = (ScrollPane) faceDetailsView.getChildren().get(1);
        VBox detailContent = (VBox) detailScroll.getContent();
        detailContent.getChildren().clear();

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(reclamation.getTitreReclamations() != null ? reclamation.getTitreReclamations() : "Reclamation");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);
        HBox.setHgrow(title, Priority.ALWAYS);

        Button exportPdfBtn = new Button("Download PDF");
        exportPdfBtn.setStyle("-fx-padding: 8 12 8 12; -fx-background-color: rgba(99,102,241,0.8); -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
        exportPdfBtn.setOnAction(e -> exportReclamationPdf(reclamation));
        top.getChildren().addAll(title, exportPdfBtn);

        VBox details = new VBox(6);
        details.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 12;");
        Label date = new Label("Date: " + (reclamation.getDateReclamation() != null
            ? reclamation.getDateReclamation().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
            : "—"));
        date.setTextFill(Color.color(1, 1, 1, 0.7));
        Label status = new Label("Status: " + getStatusDisplay(reclamation.getStatutReclamation()));
        status.setTextFill(Color.color(1, 1, 1, 0.8));
        Label desc = new Label(reclamation.getDescReclamation() == null ? "No description" : reclamation.getDescReclamation());
        desc.setWrapText(true);
        desc.setTextFill(Color.color(1, 1, 1, 0.85));
        details.getChildren().addAll(date, status, desc);

        Label threadTitle = new Label("Responses");
        threadTitle.setTextFill(Color.WHITE);
        threadTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        VBox responsesBox = new VBox(8);
        responsesBox.setFillWidth(true);
        reloadResponses(responsesBox, reclamation);

        ScrollPane responsesScroll = new ScrollPane(responsesBox);
        responsesScroll.setFitToWidth(true);
        responsesScroll.setPrefHeight(280);
        responsesScroll.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        VBox.setVgrow(responsesScroll, Priority.ALWAYS);

        Label composeLabel = new Label("Reply");
        composeLabel.setTextFill(Color.color(1, 1, 1, 0.8));
        composeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        TextArea responseInput = new TextArea();
        responseInput.setPromptText("Write a response...");
        responseInput.setPrefRowCount(3);
        responseInput.setWrapText(true);
        responseInput.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-control-inner-background: rgba(255,255,255,0.04); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.45); -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: rgba(255,255,255,0.12);");

        Label feedback = new Label();
        feedback.setTextFill(Color.color(1, 1, 1, 0.65));

        Button sendBtn = new Button("Send Response");
        sendBtn.setStyle("-fx-padding: 8 14 8 14; -fx-background-color: rgba(99,102,241,0.85); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        sendBtn.setOnAction(e -> submitResponse(reclamation, responseInput, responsesBox, feedback));

        detailContent.getChildren().addAll(top, details, threadTitle, responsesScroll, composeLabel, responseInput, sendBtn, feedback);
        
        animateSwitcherFace(false);
    }

    private void showListView() {
        animateSwitcherFace(true);
    }

    private void animateSwitcherFace(boolean showList) {
        VBox toShow = showList ? faceListView : faceDetailsView;
        VBox toHide = showList ? faceDetailsView : faceListView;
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toHide);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            toHide.setVisible(false);
            toHide.setManaged(false);
        });
        
        toShow.setVisible(true);
        toShow.setManaged(true);
        toShow.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toShow);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        fadeOut.play();
        fadeIn.play();
    }

    public VBox getRoot() {
        return root;
    }
}
