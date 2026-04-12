package com.syndicati.models.services;

import com.syndicati.models.entities.Publication;
import com.syndicati.models.repositories.PublicationRepository;

import java.util.List;

/**
 * Service for publication operations.
 */
public class PublicationService {

    private final PublicationRepository repository;

    public PublicationService() {
        this.repository = new PublicationRepository();
    }

    public List<Publication> getAllPublications() {
        return repository.findAllByDateDesc();
    }

    public boolean addPublication(Publication pub) {
        if (pub.isValid()) {
            return repository.create(pub) > 0;
        }
        return false;
    }
}
