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
    public void ajouterCommentaire(int pubId, String content, String image, int visibility) {
        System.out.println("DEBUG: ajouterCommentaire called for pubId: " + pubId);
        
        if (content == null || content.trim().isEmpty()) {
            System.err.println("DEBUG: Content is empty");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.err.println("DEBUG: Current user is NULL");
            view.showNotification("You must be logged in to comment.", "error");
            return;
        }

        System.out.println("DEBUG: Current user ID: " + currentUser.getIdUser());

        Commentaire c = new Commentaire();
        c.setIdPub(pubId);
        c.setIdUser(currentUser.getIdUser());
        c.setDescriptionCommentaire(content);
        c.setImageCommentaire(image);
        c.setVisibility(visibility);

        new Thread(() -> {
            System.out.println("DEBUG: Starting addComment thread...");
            int newId = service.addComment(c);
            System.out.println("DEBUG: Service.addComment returned ID: " + newId);
            
            Platform.runLater(() -> {
                if (newId > 0) {
                    System.out.println("DEBUG: Success! Refreshing comments for pub: " + pubId);
                    afficher(pubId); // Refresh comments
                    view.showNotification("Comment added!", "success");
                    view.clearCommentForm(); 
                } else {
                    System.err.println("DEBUG: Failed to post comment. Repository returned -1");
                    view.showNotification("Failed to post comment. Check server logs.", "error");
                }
            });
        }).start();
    }
}
