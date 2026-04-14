package com.syndicati.models.services;

import com.syndicati.models.entities.Commentaire;
import com.syndicati.models.repositories.CommentaireRepository;

import java.util.List;

/**
 * Service for managing publication comments.
 */
public class CommentaireService {

    private final CommentaireRepository repository;

    public CommentaireService() {
        this.repository = new CommentaireRepository();
    }

    public List<Commentaire> getCommentsForPublication(int pubId) {
        return repository.findByPublicationId(pubId);
    }

    public int addComment(Commentaire comment) {
        if (comment.getDescriptionCommentaire() == null || comment.getDescriptionCommentaire().trim().isEmpty()) {
            return -1;
        }
        return repository.create(comment);
    }

    public boolean updateComment(Commentaire comment) {
        if (comment.getIdCommentaire() == null || comment.getDescriptionCommentaire() == null || comment.getDescriptionCommentaire().trim().isEmpty()) {
            return false;
        }
        return repository.update(comment);
    }
}
