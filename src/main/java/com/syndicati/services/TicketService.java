package com.syndicati.services;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.syndicati.models.evenement.Participation;
import com.syndicati.models.evenement.Evenement;
import com.syndicati.models.user.User;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Service to generate PDF tickets with QR codes for event participation.
 */
public class TicketService {

    public String generateTicketPDF(Participation p) throws Exception {
        Evenement e = p.getEvenement();
        if (e == null) throw new IllegalArgumentException("Participation must have an associated event.");
        
        String fileName = "Ticket_" + e.getTitreEvent().replaceAll("[^a-zA-Z0-9]", "_") + "_" + p.getIdParticipation() + ".pdf";
        String downloadsPath = System.getProperty("user.home") + File.separator + "Downloads";
        File file = new File(downloadsPath, fileName);

        Document document = new Document(PageSize.A6, 20, 20, 20, 20);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Styles
        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(99, 102, 241));
        Font subtitleFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
        Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);

        // Header
        Paragraph header = new Paragraph("SYNDICATI EVENT TICKET", titleFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);
        
        document.add(new Paragraph(" ")); // Spacer
        document.add(new LineSeparator());
        document.add(new Paragraph(" "));

        // Event Details
        document.add(new Paragraph("Event: " + e.getTitreEvent(), subtitleFont));
        document.add(new Paragraph("Location: " + e.getLieuEvent(), normalFont));
        
        String dateStr = "N/A";
        if (e.getDateEvent() != null) {
            dateStr = e.getDateEvent().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
        document.add(new Paragraph("Date: " + dateStr, normalFont));
        
        if (p.getUser() != null) {
            document.add(new Paragraph("Attendee: " + p.getUser().getFirstName() + " " + p.getUser().getLastName(), normalFont));
        }
        document.add(new Paragraph("Companions: " + (p.getNbAccompagnants() != null ? p.getNbAccompagnants() : 0), normalFont));

        document.add(new Paragraph(" "));

        // Generate QR Code
        String qrData = "SYNDICATI-PART-" + p.getIdParticipation() + "-" + (p.getUser() != null ? p.getUser().getIdUser() : "GUEST");
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 150, 150);
        
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        Image pdfQrImage = Image.getInstance(qrImage, null);
        pdfQrImage.setAlignment(Element.ALIGN_CENTER);
        document.add(pdfQrImage);

        // Footer
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Present this ticket at the entrance.", new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return file.getAbsolutePath();
    }

    public void openTicket(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("[TicketService] File not found: " + path);
                return;
            }
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("win")) {
                // Windows: use cmd /c start — most reliable way to open a file
                new ProcessBuilder("cmd", "/c", "start", "", path)
                    .redirectErrorStream(true)
                    .start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", path).start();
            } else {
                new ProcessBuilder("xdg-open", path).start();
            }
        } catch (Exception e) {
            System.err.println("[TicketService] Could not open PDF: " + e.getMessage());
        }
    }
}
