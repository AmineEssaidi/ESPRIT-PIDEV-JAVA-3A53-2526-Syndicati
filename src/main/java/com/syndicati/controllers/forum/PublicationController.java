package com.syndicati.controllers.forum;

import com.syndicati.models.forum.Publication;
import com.syndicati.models.user.User;
import com.syndicati.services.forum.PublicationService;
import java.util.List;
import java.util.Optional;

/**
 * Controller facade for forum publications.
 */
public class PublicationController {

    private final PublicationService publicationService;

    public PublicationController() {
        this.publicationService = new PublicationService();
    }

    public List<Publication> publications() {
        return publicationService.listAll();
    }

    public Optional<Publication> publicationById(Integer id) {
        return publicationService.findById(id);
    }

    public List<Publication> publicationsByUser(User user) {
        return publicationService.listByUser(user);
    }

    public List<Publication> publicationsByCategory(String category) {
        return publicationService.listByCategory(category);
    }

    public Integer publicationCreate(String titre, String description, String categorie, String image, User user) {
        return publicationService.create(titre, description, categorie, image, user);
    }

    public boolean publicationUpdate(Integer id, String titre, String description, String categorie, String image) {
        return publicationService.update(id, titre, description, categorie, image);
    }

    public boolean publicationDelete(Integer id) {
        return publicationService.delete(id);
    }

    public PublicationService getPublicationService() {
        return publicationService;
    }
}