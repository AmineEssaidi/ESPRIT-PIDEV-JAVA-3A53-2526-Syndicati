package com.syndicati.models.services;

import com.syndicati.models.entities.Participation;
import com.syndicati.models.repositories.ParticipationRepository;

import java.util.List;

/**
 * Service layer for Participation operations.
 */
import java.util.Optional;

/**
 * Service layer for Participation operations.
 */
public class ParticipationService {

    private final ParticipationRepository repository;

    public ParticipationService() {
        this.repository = new ParticipationRepository();
    }

    public Optional<Participation> getParticipationById(int id) {
        return repository.findById(id);
    }

    public List<Participation> getAllParticipations() {
        return repository.findAll();
    }

    public List<Participation> getParticipationsByUser(int userId) {
        return repository.findAllByUserId(userId);
    }

    public boolean registerParticipation(Participation p) {
        EvenementService es = new EvenementService();
        Optional<com.syndicati.models.entities.Evenement> eOpt = es.getEventById(p.getEvenement().getIdEvent());
        
        if (eOpt.isPresent()) {
            com.syndicati.models.entities.Evenement event = eOpt.get();
            int totalNeeded = 1 + p.getNbAccompagnants();
            
            if (event.getNbRestants() >= totalNeeded) {
                int id = repository.create(p);
                if (id > 0) {
                    p.setIdParticipation(id);
                    if (es.decrementPlaces(event.getIdEvent(), totalNeeded)) {
                        // Send confirmation email
                        sendConfirmationEmail(p);
                        return true;
                    }
                }
            } else {
                System.err.println("Not enough places: requested " + totalNeeded + ", available " + event.getNbRestants());
            }
        }
        return false;
    }

    public boolean updateParticipation(Participation p) {
        return repository.update(p);
    }

    public boolean updateStatus(int id, String status) {
        return repository.updateStatus(id, status);
    }

    public boolean cancelParticipation(int id) {
        Optional<Participation> pOpt = repository.findById(id);
        if (pOpt.isPresent()) {
            Participation p = pOpt.get();
            if (repository.delete(id)) {
                EvenementService es = new EvenementService();
                int totalToRestore = 1 + p.getNbAccompagnants();
                return es.incrementPlaces(p.getEvenement().getIdEvent(), totalToRestore);
            }
        }
        return false;
    }

    private void sendConfirmationEmail(Participation p) {
        try {
            com.syndicati.services.mail.AsyncMailerService mailer = com.syndicati.services.mail.AsyncMailerService.getInstance();
            String html = com.syndicati.services.mail.SyndicatiEmailComposer.participationConfirmation(
                p.getUser().getFirstName(),
                p.getEvenement().getTitreEvent(),
                p.getNbAccompagnants(),
                p.getDateParticipation().toString()
            );
            mailer.sendHtmlAsync(p.getUser().getEmailUser(), "Participation Confirmed: " + p.getEvenement().getTitreEvent(), html);
            System.out.println("Queued confirmation email to " + p.getUser().getEmailUser());
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }
}
