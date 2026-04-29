package com.syndicati.models.services;

import com.syndicati.models.entities.Participation;
import com.syndicati.models.repositories.ParticipationRepository;

import java.util.List;
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
                p.setStatutParticipation(Participation.STATUS_CONFIRMED);
                int id = repository.create(p);
                if (id > 0) {
                    p.setIdParticipation(id);
                    if (es.decrementPlaces(event.getIdEvent(), totalNeeded)) {
                        sendConfirmationEmail(p);
                        return true;
                    }
                }
            } else {
                // Not enough places, put on waiting list
                p.setStatutParticipation(Participation.STATUS_WAITING_LIST);
                int id = repository.create(p);
                if (id > 0) {
                    p.setIdParticipation(id);
                    sendWaitingListEmail(p);
                    System.out.println("User " + p.getUser().getIdUser() + " added to waiting list for event " + event.getIdEvent());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean updateParticipation(Participation p) {
        Optional<Participation> oldOpt = repository.findById(p.getIdParticipation());
        if (oldOpt.isPresent()) {
            Participation oldP = oldOpt.get();
            int diff = p.getNbAccompagnants() - oldP.getNbAccompagnants();
            
            if (diff == 0) {
                return repository.update(p);
            }
            
            EvenementService es = new EvenementService();
            if (diff > 0) {
                // User added companions, need to check availability
                Optional<com.syndicati.models.entities.Evenement> eOpt = es.getEventById(p.getEvenement().getIdEvent());
                if (eOpt.isPresent() && eOpt.get().getNbRestants() >= diff) {
                    if (es.decrementPlaces(p.getEvenement().getIdEvent(), diff)) {
                        return repository.update(p);
                    }
                } else {
                    System.err.println("Not enough places for the update: needed " + diff);
                    return false;
                }
            } else {
                // User removed companions, free up seats
                if (es.incrementPlaces(p.getEvenement().getIdEvent(), Math.abs(diff))) {
                    return repository.update(p);
                }
            }
        }
        return false;
    }

    public boolean updateStatus(int id, String status) {
        return repository.updateStatus(id, status);
    }

    public boolean cancelParticipation(int id) {
        Optional<Participation> pOpt = repository.findById(id);
        if (pOpt.isPresent()) {
            Participation p = pOpt.get();
            String oldStatus = p.getStatutParticipation();
            int eventId = p.getEvenement().getIdEvent();
            
            if (repository.delete(id)) {
                if (Participation.STATUS_CONFIRMED.equals(oldStatus)) {
                    EvenementService es = new EvenementService();
                    int totalToRestore = 1 + p.getNbAccompagnants();
                    es.incrementPlaces(eventId, totalToRestore);
                    
                    // Promotion logic
                    processWaitingList(eventId);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the waiting list for an event and promotes users if spots are available.
     */
    private void processWaitingList(int eventId) {
        List<Participation> waitingList = repository.findWaitingListByEventId(eventId);
        if (waitingList.isEmpty()) return;

        EvenementService es = new EvenementService();
        for (Participation p : waitingList) {
            Optional<com.syndicati.models.entities.Evenement> eOpt = es.getEventById(eventId);
            if (eOpt.isPresent()) {
                com.syndicati.models.entities.Evenement event = eOpt.get();
                int totalNeeded = 1 + p.getNbAccompagnants();
                
                if (event.getNbRestants() >= totalNeeded) {
                    if (repository.updateStatus(p.getIdParticipation(), Participation.STATUS_CONFIRMED)) {
                        es.decrementPlaces(eventId, totalNeeded);
                        sendPromotionEmail(p);
                        System.out.println("Promoted user " + p.getUser().getIdUser() + " to confirmed for event " + eventId);
                    }
                }
            }
        }
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
        } catch (Exception e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }

    private void sendWaitingListEmail(Participation p) {
        try {
            com.syndicati.services.mail.AsyncMailerService mailer = com.syndicati.services.mail.AsyncMailerService.getInstance();
            String html = com.syndicati.services.mail.SyndicatiEmailComposer.waitingListAdded(
                p.getUser().getFirstName(),
                p.getEvenement().getTitreEvent()
            );
            mailer.sendHtmlAsync(p.getUser().getEmailUser(), "Waitlist: " + p.getEvenement().getTitreEvent(), html);
        } catch (Exception e) {
            System.err.println("Failed to send waiting list email: " + e.getMessage());
        }
    }

    private void sendPromotionEmail(Participation p) {
        try {
            com.syndicati.services.mail.AsyncMailerService mailer = com.syndicati.services.mail.AsyncMailerService.getInstance();
            String html = com.syndicati.services.mail.SyndicatiEmailComposer.waitingListPromotion(
                p.getUser().getFirstName(),
                p.getEvenement().getTitreEvent()
            );
            mailer.sendHtmlAsync(p.getUser().getEmailUser(), "You're In! " + p.getEvenement().getTitreEvent(), html);
        } catch (Exception e) {
            System.err.println("Failed to send promotion email: " + e.getMessage());
        }
    }
}
