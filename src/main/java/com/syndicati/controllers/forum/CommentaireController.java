package com.syndicati.controllers.forum;

import com.syndicati.models.forum.Commentaire;
import com.syndicati.models.forum.Publication;
import com.syndicati.models.user.User;
import com.syndicati.services.forum.CommentaireService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller facade for forum comments.
 */
public class CommentaireController {

    private final CommentaireService commentaireService;

    public CommentaireController() {
        this.commentaireService = new CommentaireService();
    }

    public List<Commentaire> commentaires() {
        return commentaireService.listAll();
    }

    public Optional<Commentaire> commentaireById(Integer id) {
        return commentaireService.findById(id);
    }

    public List<Commentaire> commentairesByPublication(Publication publication) {
        return commentaireService.listByPublication(publication);
    }

    public List<Commentaire> commentairesByUser(User user) {
        return commentaireService.listByUser(user);
    }

    public Integer commentaireCreate(String description, String image, boolean visibility, Publication publication, User user) {
        return commentaireService.create(description, image, visibility, publication, user);
    }

    public Integer commentaireCreate(String description, String image, boolean visibility, LocalDateTime createdAt, Publication publication, User user) {
        return commentaireService.create(description, image, visibility, createdAt, publication, user);
    }

    public boolean commentaireUpdate(Integer id, String description, String image, Boolean visibility) {
        return commentaireService.update(id, description, image, visibility);
    }

    public boolean commentaireUpdate(Integer id, String description, String image, Boolean visibility, LocalDateTime createdAt) {
        return commentaireService.update(id, description, image, visibility, createdAt);
    }

    public boolean commentaireDelete(Integer id) {
        return commentaireService.delete(id);
    }

    public CommentaireService getCommentaireService() {
        return commentaireService;
    }
}