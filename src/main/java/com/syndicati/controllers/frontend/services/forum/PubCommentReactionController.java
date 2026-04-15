package com.syndicati.controllers.frontend.services.forum;

import com.syndicati.models.entities.PubCommentReaction;
import com.syndicati.models.services.PubCommentReactionService;
import com.syndicati.utils.session.SessionManager;
import com.syndicati.views.frontend.services.ForumPageView;
import javafx.application.Platform;

/**
 * Controller for reactions on Publications and Comments.
 */
public class PubCommentReactionController {
    private final PubCommentReactionService service;
    private final ForumPageView view;

    public PubCommentReactionController(ForumPageView view) {
        this.service = new PubCommentReactionService();
        this.view = view;
    }

    public void handleReaction(Integer pubId, Integer commId, String kind, String emoji, String reason) {
        com.syndicati.models.entities.User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            view.showNotification("Please log in to react.", "error");
            return;
        }

        new Thread(() -> {
            boolean success = service.toggleReaction(currentUser.getIdUser(), pubId, commId, kind, emoji, reason);
            Platform.runLater(() -> {
                if (success) {
                    // Refresh counts and states in UI
                    view.refreshReactionsFor(pubId, commId);
                } else {
                    view.showNotification("Reaction failed. Please try again.", "error");
                }
            });
        }).start();
    }

    public int getCount(Integer pubId, Integer commId, String kind) {
        return service.getReactionCount(pubId, commId, kind);
    }

    public boolean hasReacted(Integer pubId, Integer commId, String kind) {
        com.syndicati.models.entities.User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return false;
        return service.hasUserReacted(currentUser.getIdUser(), pubId, commId, kind);
    }
}
