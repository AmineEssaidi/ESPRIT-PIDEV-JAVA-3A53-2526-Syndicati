package com.syndicati.controllers.syndicat;

import com.syndicati.models.syndicat.Reclamation;
import com.syndicati.models.syndicat.Reponse;
import com.syndicati.models.user.User;
import com.syndicati.services.syndicat.ReclamationService;
import com.syndicati.services.syndicat.ReponseService;
import com.syndicati.services.user.user.UserService;
import com.syndicati.services.mail.AsyncMailerService;
import com.syndicati.services.mail.SyndicatiEmailComposer;
import com.syndicati.services.google.GoogleDriveService;
import com.syndicati.services.pdf.PdfExportService;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Reclamation operations.
 * Bridges UI layer with services for CRUD operations aligned with Horizon patterns.
 */
public class ReclamationController {

    private final ReclamationService reclamationService;
    private final ReponseService reponseService;
    private final UserService userService;
    private final AsyncMailerService mailerService;
    private final SyndicatiEmailComposer emailComposer;

    public ReclamationController() {
        this.reclamationService = new ReclamationService();
        this.reponseService = new ReponseService();
        this.userService = new UserService();
        this.mailerService = AsyncMailerService.getInstance();
        this.emailComposer = null; // Static methods used
    }

    // Reclamation CRUD
    public List<Reclamation> reclamations() {
        return reclamationService.listAll();
    }

    public List<Reclamation> reclamationsByUser(User user) {
        return reclamationService.listByUser(user);
    }

    public Optional<Reclamation> reclamationById(Integer id) {
        return reclamationService.findById(id);
    }

    public List<Reclamation> reclamationsByStatut(String statut) {
        return reclamationService.listByStatut(statut);
    }

    public Integer reclamationCreate(String titre, String description, LocalDateTime date, String image, User user) {
        Integer createdId = reclamationService.create(titre, description, date, image, user);
        
        if (createdId != null && createdId > 0) {
            GlobalNotificationPillManager.created("Reclamation", "Reclamation created successfully.");
            uploadReclamationPdfToDrive(reclamationById(createdId).orElse(null));

            // Notify creator (confirmation copy)
            String confirmationHtml = SyndicatiEmailComposer.reclamationConfirmation(
                user.getFirstName(),
                titre,
                description
            );
            mailerService.sendHtmlAsync(
                user.getEmailUser(),
                "Confirmation: We received your request",
                confirmationHtml
            );

            // Notify admins and syndics
            String userName = (user.getFirstName() + " " + user.getLastName()).trim();
            List<User> allUsers = userService.listUsers();
            
            for (User recipient : allUsers) {
                // Don't notify the creator again as an admin
                if (recipient.getIdUser().equals(user.getIdUser())) continue;

                String role = recipient.getRoleUser();
                if ("ADMIN".equalsIgnoreCase(role) || "SYNDIC".equalsIgnoreCase(role) || "SUPERADMIN".equalsIgnoreCase(role)) {
                    String html = SyndicatiEmailComposer.reclamationNotification(
                        recipient.getFirstName(),
                        titre,
                        userName,
                        description
                    );
                    mailerService.sendHtmlAsync(
                        recipient.getEmailUser(),
                        "New Support Request: " + titre,
                        html
                    );
                }
            }
        }
        
        return createdId;
    }

    public boolean reclamationUpdate(Integer id, String titre, String description, LocalDateTime date, String statut) {
        boolean success = reclamationService.update(id, titre, description, date, statut);
        if (success) {
            GlobalNotificationPillManager.updated("Reclamation", "Reclamation updated successfully.");
            reclamationById(id).ifPresent(rec -> {
                // Upload updated PDF to Google Drive (Replacing old)
                uploadReclamationPdfToDrive(rec);

                if (rec.getUser() != null) {
                    String html = SyndicatiEmailComposer.statusChangeNotification(
                        rec.getUser().getFirstName(),
                        rec.getTitreReclamations(),
                        statut
                    );
                    mailerService.sendHtmlAsync(
                        rec.getUser().getEmailUser(),
                        "Update on your reclamation: " + rec.getTitreReclamations(),
                        html
                    );
                }
            });
        }
        return success;
    }

    public boolean reclamationUpdateStatut(Integer id, String statut) {
        boolean success = reclamationService.updateStatut(id, statut);
        if (success) {
            GlobalNotificationPillManager.updated("Reclamation", "Reclamation status updated successfully.");
            reclamationById(id).ifPresent(rec -> {
                // Upload updated PDF to Google Drive (Replacing old)
                uploadReclamationPdfToDrive(rec);

                if (rec.getUser() != null) {
                    String html = SyndicatiEmailComposer.statusChangeNotification(
                        rec.getUser().getFirstName(),
                        rec.getTitreReclamations(),
                        statut
                    );
                    mailerService.sendHtmlAsync(
                        rec.getUser().getEmailUser(),
                        "Update on your reclamation: " + rec.getTitreReclamations(),
                        html
                    );
                }
            });
        }
        return success;
    }

    public boolean reclamationDelete(Integer id) {
        boolean success = reclamationService.delete(id);
        if (success) {
            GlobalNotificationPillManager.deleted("Reclamation", "Reclamation deleted successfully.");
        }
        return success;
    }

    // Reponse CRUD
    public List<Reponse> reponses() {
        return reponseService.listAll();
    }

    public List<Reponse> reponsesByReclamation(Reclamation reclamation) {
        return reponseService.listByReclamation(reclamation);
    }

    public List<Reponse> reponsesByUser(User user) {
        return reponseService.listByUser(user);
    }

    public Optional<Reponse> reponseById(Integer id) {
        return reponseService.findById(id);
    }

    public Integer reponseCreate(String titre, String message, String image, Reclamation reclamation, User user) {
        Integer createdId = reponseService.create(titre, message, image, reclamation, user);
        
        if (createdId != null && createdId > 0 && reclamation != null && reclamation.getUser() != null) {
            GlobalNotificationPillManager.created("Response", "Reclamation response added successfully.");
            uploadReclamationPdfToDrive(reclamation);

            // Notify the reclamation owner, but only if someone else replied
            if (!user.getIdUser().equals(reclamation.getUser().getIdUser())) {
                String responderName = (user.getFirstName() + " " + user.getLastName()).trim();
                String html = SyndicatiEmailComposer.responseNotification(
                    reclamation.getUser().getFirstName(),
                    reclamation.getTitreReclamations(),
                    responderName,
                    titre,
                    message,
                    image
                );
                
                mailerService.sendHtmlAsync(
                    reclamation.getUser().getEmailUser(),
                    "New reply to your reclamation: " + reclamation.getTitreReclamations(),
                    html
                );
            }
        }
        
        return createdId;
    }

    public boolean reponseUpdate(Integer id, String titre, String message, String image) {
        boolean success = reponseService.update(id, titre, message, image);
        if (success) {
            GlobalNotificationPillManager.updated("Response", "Reclamation response updated successfully.");
        }
        return success;
    }

    public boolean reponseUpdate(Integer id, String titre, String message) {
        return reponseUpdate(id, titre, message, null);
    }

    public boolean reponseDelete(Integer id) {
        boolean success = reponseService.delete(id);
        if (success) {
            GlobalNotificationPillManager.deleted("Response", "Reclamation response deleted successfully.");
        }
        return success;
    }

    public ReclamationService getReclamationService() {
        return reclamationService;
    }

    public ReponseService getReponseService() {
        return reponseService;
    }

    private void uploadReclamationPdfToDrive(Reclamation rec) {
        if (rec == null || rec.getUser() == null) return;
        
        new Thread(() -> {
            try {
                List<Reponse> responses = reponseService.listByReclamation(rec);
                java.io.File pdfFile = PdfExportService.generateReclamationPdf(rec, responses);
                GoogleDriveService.uploadOrUpdateReclamationPdf(rec, pdfFile);
                System.out.println("[GoogleDrive] Successfully uploaded/updated PDF for reclamation: " + rec.getIdReclamations());
            } catch (IOException e) {
                System.err.println("[GoogleDrive] Failed to generate/upload PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
