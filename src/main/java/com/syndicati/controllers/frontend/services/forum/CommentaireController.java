package com.syndicati.controllers.frontend.services.forum;

import com.syndicati.models.entities.Commentaire;
import com.syndicati.models.entities.User;
import com.syndicati.models.services.CommentaireService;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.views.frontend.services.ForumPageView;
import javafx.application.Platform;

import java.util.List;

/**
 * Controller for managing comments in the view.
 */
public class CommentaireController {

    private final CommentaireService service;
    private final ForumPageView view;

    public CommentaireController(ForumPageView view) {
        this.service = new CommentaireService();
        this.view = view;
    }

    /**
     * Fetches comments for a publication and updates the view.
     */
    public void afficher(int pubId) {
        new Thread(() -> {
            List<Commentaire> comments = service.getCommentsForPublication(pubId);
            Platform.runLater(() -> {
                view.setComments(comments);
            });
        }).start();
    }

    /**
     * Synchronous fetch of a single comment.
     */
    public Commentaire getCommentById(int id) {
        return service.getCommentById(id);
    }

    /**
     * Adds a new comment and refreshes the list on success.
     */
    public void ajouterCommentaire(int pubId, String content, String image, int visibility) {
        if (content == null || content.trim().length() < 10) {
            view.showNotification("Comment must be at least 10 characters long.", "error");
            return;
        }

        // Quick check for start characters
        if (!Character.isLetter(content.trim().charAt(0))) {
            view.showNotification("Comment must start with a letter (no numbers or symbols like . , ? ;)", "error");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            view.showNotification("You must be logged in to comment.", "error");
            return;
        }

        Commentaire c = new Commentaire();
        c.setIdPub(pubId);
        c.setIdUser(currentUser.getIdUser());
        c.setDescriptionCommentaire(content);
        c.setImageCommentaire(image);
        c.setVisibility(visibility);

        if (!c.isValid()) {
            view.showNotification("Invalid comment content. Please avoid starting with symbols.", "error");
            return;
        }

        new Thread(() -> {
            int newId = service.addComment(c);
            Platform.runLater(() -> {
                if (newId > 0) {
                    afficher(pubId); // Refresh comments
                    view.showNotification("Comment added!", "success");
                    view.clearCommentForm(); 
                } else {
                    view.showNotification("Failed to post comment.", "error");
                }
            });
        }).start();
    }

    public void modifierCommentaire(int commentId, int pubId, String newContent, String image, int visibility) {
        if (newContent == null || newContent.trim().isEmpty() || !Character.isLetterOrDigit(newContent.trim().charAt(0))) {
            view.showNotification("Comment must be non-empty and start with a letter/number.", "error");
            return;
        }

        Commentaire c = new Commentaire();
        c.setIdCommentaire(commentId);
        c.setIdPub(pubId);
        c.setDescriptionCommentaire(newContent);
        c.setImageCommentaire(image);
        c.setVisibility(visibility);

        new Thread(() -> {
            boolean success = service.updateComment(c);
            Platform.runLater(() -> {
                if (success) {
                    afficher(pubId);
                    view.showNotification("Comment updated!", "success");
                } else {
                    view.showNotification("Failed to update comment.", "error");
                }
            });
        }).start();
    }

    public void supprimerCommentaire(int commentId, int pubId) {
        new Thread(() -> {
            boolean success = service.deleteComment(commentId);
            Platform.runLater(() -> {
                if (success) {
                    afficher(pubId);
                    view.showNotification("Comment deleted.", "success");
                } else {
                    view.showNotification("Failed to delete comment.", "error");
                }
            });
        }).start();
    }
}
