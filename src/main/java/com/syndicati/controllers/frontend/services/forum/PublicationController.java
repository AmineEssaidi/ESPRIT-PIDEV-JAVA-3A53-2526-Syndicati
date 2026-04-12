package com.syndicati.controllers.frontend.services.forum;

import com.syndicati.models.entities.Publication;
import com.syndicati.models.services.PublicationService;
import com.syndicati.views.frontend.services.ForumPageView;
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
}
