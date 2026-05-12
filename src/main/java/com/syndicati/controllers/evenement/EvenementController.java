package com.syndicati.controllers.evenement;

import com.syndicati.models.evenement.Evenement;
import com.syndicati.models.user.User;
import com.syndicati.services.evenement.EvenementService;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller facade for evenements.
 */
public class EvenementController {

    private final EvenementService evenementService;

    public EvenementController() {
        this.evenementService = new EvenementService();
    }

    public List<Evenement> evenements() {
        return evenementService.listAll();
    }

    public Optional<Evenement> evenementById(Integer id) {
        return evenementService.findById(id);
    }

    public List<Evenement> evenementsByUser(User user) {
        return evenementService.listByUser(user);
    }

    public List<Evenement> evenementsByType(String type) {
        return evenementService.listByType(type);
    }

    public List<Evenement> evenementsByStatut(String statut) {
        return evenementService.listByStatut(statut);
    }

    public Integer evenementCreate(String titre, String description, LocalDateTime dateEvent, String lieu, Integer nbPlaces, String type, String image, User user) {
        Integer id = evenementService.create(titre, description, dateEvent, lieu, nbPlaces, type, image, user);
        if (id != null && id > 0) {
            GlobalNotificationPillManager.created("Event", "Event created successfully.");
            
            // Send email notification to creator
            if (user != null && user.getEmailUser() != null && !user.getEmailUser().isBlank()) {
                try {
                    com.syndicati.services.mail.AsyncMailerService mailer = com.syndicati.services.mail.AsyncMailerService.getInstance();
                    String dateStr = dateEvent != null ? dateEvent.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "To be determined";
                    String emailContent = com.syndicati.services.mail.SyndicatiEmailComposer.eventCreationNotification(
                        user.getFirstName(), titre, dateStr, lieu
                    );
                    mailer.sendHtmlAsync(user.getEmailUser(), "Syndicati: Your event is live!", emailContent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return id;
    }

    public boolean evenementUpdate(Integer id, String titre, String description, LocalDateTime dateEvent, String lieu, Integer nbPlaces, String type) {
        boolean success = evenementService.update(id, titre, description, dateEvent, lieu, nbPlaces, type);
        if (success) {
            GlobalNotificationPillManager.updated("Event", "Event updated successfully.");
        }
        return success;
    }

    public boolean evenementUpdateForDashboard(Integer id, String titre, String description, LocalDateTime dateEvent, String lieu, Integer nbPlaces, Integer nbRestants, String type, String image) {
        boolean success = evenementService.updateForDashboard(id, titre, description, dateEvent, lieu, nbPlaces, nbRestants, type, image);
        if (success) {
            GlobalNotificationPillManager.updated("Event", "Event updated successfully.");
        }
        return success;
    }

    public boolean evenementUpdateStatut(Integer id, String statut) {
        boolean success = evenementService.updateStatut(id, statut);
        if (success) {
            GlobalNotificationPillManager.updated("Event", "Event status updated successfully.");
        }
        return success;
    }

    public boolean evenementDelete(Integer id) {
        boolean success = evenementService.delete(id);
        if (success) {
            GlobalNotificationPillManager.deleted("Event", "Event deleted successfully.");
        }
        return success;
    }

    public EvenementService getEvenementService() {
        return evenementService;
    }
}
