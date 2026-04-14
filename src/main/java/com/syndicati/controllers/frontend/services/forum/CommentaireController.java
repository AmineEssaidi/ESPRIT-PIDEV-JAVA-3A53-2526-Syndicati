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
     * Adds a new comment and refreshes the list on success.
     */
    public void ajouterCommentaire(int pubId, String content) {
        if (content == null || content.trim().isEmpty()) {
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

        new Thread(() -> {
            int newId = service.addComment(c);
            Platform.runLater(() -> {
                if (newId > 0) {
                    afficher(pubId); // Refresh comments
                    view.showNotification("Comment added!", "success");
                    view.clearCommentForm(); // Need to implement this in view
                } else {
                    view.showNotification("Failed to post comment.", "error");
                }
            });
        }).start();
    }
}
