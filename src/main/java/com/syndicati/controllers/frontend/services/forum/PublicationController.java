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
            int newId = service.addPublication(pub);
            Platform.runLater(() -> {
                if (newId > 0) {
                    afficher(); // Refresh sidebar list
                    view.updateDetailView(pub); // Show the new post details immediately
                    view.switchFaceToRead();     // Switch to detail view
                    view.showNotification("Publication created successfully!", "success");
                } else {
                    view.showNotification("Failed to create publication.", "error");
                    System.err.println("Failed to create publication.");
                }
            });
        }).start();
    }

    public void updatePublication(int id, String title, String category, String description, String image, Integer userId) {
        Publication pub = new Publication();
        pub.setId(id);
        pub.setTitrePub(title);
        pub.setCategoriePub(category);
        pub.setDescriptionPub(description);
        pub.setImagePub(image);
        pub.setUserId(userId);

        new Thread(() -> {
            boolean success = service.updatePublication(pub);
            Platform.runLater(() -> {
                if (success) {
                    afficher(); // Refresh list
                    view.switchFaceToRead();
                    view.showNotification("Publication updated successfully!", "success");
                } else {
                    view.showNotification("Failed to update publication.", "error");
                    System.err.println("Failed to update publication.");
                }
            });
        }).start();
    }

    public void deletePublication(int id) {
        new Thread(() -> {
            boolean success = service.deletePublication(id);
            Platform.runLater(() -> {
                if (success) {
                    afficher(); // Refresh list
                    view.updateDetailView(null); // Clear the detail view of the deleted post
                    view.switchFaceToRead();
                    view.showNotification("Publication deleted successfully!", "success");
                } else {
                    view.showNotification("Failed to delete publication.", "error");
                    System.err.println("Failed to delete publication.");
                }
            });
        }).start();
    }
}
