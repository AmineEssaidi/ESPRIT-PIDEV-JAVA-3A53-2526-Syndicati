package com.syndicati.models.services;

import com.syndicati.models.entities.Evenement;
import com.syndicati.models.repositories.EvenementRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Evenement operations.
 */
public class EvenementService {

    private final EvenementRepository repository;

    public EvenementService() {
        this.repository = new EvenementRepository();
    }

    public List<Evenement> getAllEvents() {
        return repository.findAll();
    }

    public Optional<Evenement> getEventById(int id) {
        return repository.findById(id);
    }

    public boolean createEvent(Evenement event) {
        int id = repository.create(event);
        if (id > 0) {
            event.setIdEvent(id);
            return true;
        }
        return false;
    }

    public boolean updateEvent(Evenement event) {
        return repository.update(event);
    }

    public boolean deleteEvent(int id) {
        return repository.deleteById(id);
    }
    public boolean decrementPlaces(int id, int count) {
        return repository.decrementRestants(id, count);
    }

    public boolean incrementPlaces(int id, int count) {
        return repository.incrementRestants(id, count);
    }
}
