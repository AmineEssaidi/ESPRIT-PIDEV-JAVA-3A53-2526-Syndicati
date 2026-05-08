package com.syndicati.controllers.evenement;

import com.syndicati.models.evenement.Participation;
import com.syndicati.models.evenement.Evenement;
import com.syndicati.models.user.User;
import com.syndicati.services.evenement.ParticipationService;
import com.syndicati.utils.notifications.GlobalNotificationPillManager;
import java.util.List;
import java.util.Optional;

/**
 * Controller facade for participations.
 */
public class ParticipationController {

    private final ParticipationService participationService;

    public ParticipationController() {
        this.participationService = new ParticipationService();
    }

    public List<Participation> participations() {
        return participationService.listAll();
    }

    public Optional<Participation> participationById(Integer id) {
        return participationService.findById(id);
    }

    public List<Participation> participationsByEvenement(Evenement event) {
        return participationService.listByEvenement(event);
    }

    public List<Participation> participationsByUser(User user) {
        return participationService.listByUser(user);
    }

    public List<Participation> participationsByStatut(String statut) {
        return participationService.listByStatut(statut);
    }

    public List<Participation> participationsByEvenementAndStatut(Evenement event, String statut) {
        return participationService.listByEvenementAndStatut(event, statut);
    }

    public Integer participationCreate(Evenement event, User user, Integer nbAccompagnants, String commentaire) {
        Integer id = participationService.create(event, user, nbAccompagnants, commentaire);
        if (id != null && id > 0) {
            GlobalNotificationPillManager.created("Participation", "Participation created successfully.");
        }
        return id;
    }

    public boolean participationUpdateStatut(Integer id, String statut) {
        boolean success = participationService.updateStatut(id, statut);
        if (success) {
            GlobalNotificationPillManager.updated("Participation", "Participation updated successfully.");
        }
        return success;
    }

    public boolean participationUpdate(Integer id, Integer nbAccompagnants, String commentaire) {
        boolean success = participationService.update(id, nbAccompagnants, commentaire);
        if (success) {
            GlobalNotificationPillManager.updated("Participation", "Participation updated successfully.");
        }
        return success;
    }

    public boolean participationDelete(Integer id) {
        boolean success = participationService.delete(id);
        if (success) {
            GlobalNotificationPillManager.deleted("Participation", "Participation deleted successfully.");
        }
        return success;
    }

    public ParticipationService getParticipationService() {
        return participationService;
    }
}
