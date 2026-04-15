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

    public int addPublication(Publication pub) {
        if (pub.isValid()) {
            int id = repository.create(pub);
            if (id > 0) {
                pub.setId(id);
                return id;
            }
        }
        return -1;
    }

    public boolean updatePublication(Publication pub) {
        if (pub.isValid() && pub.getId() != null) {
            return repository.update(pub);
        }
        return false;
    }

    public boolean deletePublication(int id) {
        return repository.delete(id);
    }

    public List<Publication> getBookmarkedByUserId(int userId) {
        return repository.findAllBookmarkedByUserId(userId);
    }
}
