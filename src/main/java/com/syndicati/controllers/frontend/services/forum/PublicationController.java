package com.syndicati.controllers.frontend.services.forum;

import com.syndicati.models.entities.Publication;
import com.syndicati.models.services.PublicationService;
import com.syndicati.views.frontend.services.ForumPageView;
import com.syndicati.models.entities.User;
import com.syndicati.utils.session.SessionManager;
import javafx.application.Platform;
import java.util.List;

/**
 * Controller for Forum Publications.
 */
public class PublicationController {

    private final PublicationService service;
    private final ForumPageView view;

    public PublicationController(ForumPageView view) {
        this.service = new PublicationService();
        this.view = view;
    }

    /**
     * Fetches publications from DB and updates the view.
     */
    public void afficher() {
        // Run on a separate thread to avoid blocking UI
        new Thread(() -> {
            List<Publication> publications = service.getAllPublications();
            
            // Update UI on JavaFX Application Thread
            Platform.runLater(() -> {
                view.setPublications(publications);
            });
        }).start();
    }

    /**
     * Adds a new publication and refreshes the view.
     */
    public void addPublication(String title, String category, String description, String image) {
        Publication pub = new Publication();
        pub.setTitrePub(title);
        pub.setCategoriePub(category);
        pub.setDescriptionPub(description);
        pub.setImagePub(image);
        
        // Get current user from session
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            pub.setUserId(currentUser.getIdUser());
        } else {
            // Fallback for safety, though user should be logged in
            pub.setUserId(1); 
        }

        new Thread(() -> {
            boolean success = service.addPublication(pub);
            Platform.runLater(() -> {
                if (success) {
                    afficher(); // Refresh list
                    view.switchFaceToRead();
                } else {
                    // Could show an error alert here
                    System.err.println("Failed to create publication.");
                }
            });
        }).start();
    }
}
