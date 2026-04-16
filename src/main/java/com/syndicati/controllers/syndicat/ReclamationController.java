package com.syndicati.controllers.syndicat;

import com.syndicati.models.syndicat.Reclamation;
import com.syndicati.models.syndicat.Reponse;
import com.syndicati.models.user.User;
import com.syndicati.services.syndicat.ReclamationService;
import com.syndicati.services.syndicat.ReponseService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Reclamation operations.
 * Bridges UI layer with services for CRUD operations aligned with Horizon patterns.
 */
public class ReclamationController {

    private final ReclamationService reclamationService;
    private final ReponseService reponseService;

    public ReclamationController() {
        this.reclamationService = new ReclamationService();
        this.reponseService = new ReponseService();
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
        return reclamationService.create(titre, description, date, image, user);
    }

    public boolean reclamationUpdate(Integer id, String titre, String description, LocalDateTime date, String statut) {
        return reclamationService.update(id, titre, description, date, statut);
    }

    public boolean reclamationUpdateStatut(Integer id, String statut) {
        return reclamationService.updateStatut(id, statut);
    }

    public boolean reclamationDelete(Integer id) {
        return reclamationService.delete(id);
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
        return reponseService.create(titre, message, image, reclamation, user);
    }

    public boolean reponseUpdate(Integer id, String titre, String message) {
        return reponseService.update(id, titre, message);
    }

    public boolean reponseDelete(Integer id) {
        return reponseService.delete(id);
    }

    public ReclamationService getReclamationService() {
        return reclamationService;
    }

    public ReponseService getReponseService() {
        return reponseService;
    }
}
