package com.syndicati.controllers.forum;

import com.syndicati.models.forum.Commentaire;
import com.syndicati.models.forum.Publication;
import com.syndicati.models.forum.Reaction;
import com.syndicati.models.user.User;
import com.syndicati.services.forum.ReactionService;
import com.syndicati.services.forum.ReactionService.ReactionActionResult;
import com.syndicati.services.forum.ReactionService.ReactionStatus;
import java.util.List;

/**
 * Controller facade for forum reactions.
 */
public class ReactionController {

    private final ReactionService reactionService;

    public ReactionController() {
        this.reactionService = new ReactionService();
    }

    public ReactionActionResult publicationToggle(Publication publication, User user, String kind) {
        Integer publicationId = publication == null ? null : publication.getIdPublication();
        return reactionService.togglePublicationReaction(publicationId, user, kind);
    }

    public ReactionActionResult publicationEmoji(Publication publication, User user, String emoji) {
        Integer publicationId = publication == null ? null : publication.getIdPublication();
        return reactionService.reactPublicationEmoji(publicationId, user, emoji);
    }

    public ReactionActionResult publicationReport(Publication publication, User user, String reason) {
        Integer publicationId = publication == null ? null : publication.getIdPublication();
        return reactionService.reportPublication(publicationId, user, reason);
    }

    public ReactionStatus publicationStatus(Publication publication, User user) {
        Integer publicationId = publication == null ? null : publication.getIdPublication();
        return reactionService.publicationStatus(publicationId, user);
    }

    // Aliases for View Synchronization (ID-based)
    public ReactionStatus getPublicationStatus(Integer publicationId, int userId) {
        User u = new User();
        u.setIdUser(userId);
        return reactionService.publicationStatus(publicationId, u);
    }

    public ReactionActionResult togglePublicationReaction(Integer publicationId, Integer userId, String kind) {
        User u = new User();
        u.setIdUser(userId);
        return reactionService.togglePublicationReaction(publicationId, u, kind);
    }

    public ReactionActionResult reactPublicationEmoji(Integer publicationId, Integer userId, String emoji) {
        User u = new User();
        u.setIdUser(userId);
        return reactionService.reactPublicationEmoji(publicationId, u, emoji);
    }

    public ReactionActionResult reportPublication(Integer publicationId, Integer userId, String reason) {
        User u = new User();
        u.setIdUser(userId);
        return reactionService.reportPublication(publicationId, u, reason);
    }

    public ReactionActionResult togglePublicationReaction(Integer publicationId, User user, String kind) {
        return reactionService.togglePublicationReaction(publicationId, user, kind);
    }

    public ReactionActionResult reactPublicationEmoji(Integer publicationId, User user, String emoji) {
        return reactionService.reactPublicationEmoji(publicationId, user, emoji);
    }

    public ReactionActionResult reportPublication(Integer publicationId, User user, String reason) {
        return reactionService.reportPublication(publicationId, user, reason);
    }

    public ReactionStatus publicationStatus(Integer publicationId, User user) {
        return reactionService.publicationStatus(publicationId, user);
    }

    public ReactionActionResult commentToggle(Commentaire commentaire, User user, String kind) {
        Integer commentId = commentaire == null ? null : commentaire.getIdCommentaire();
        return reactionService.toggleCommentReaction(commentId, user, kind);
    }

    public ReactionActionResult commentEmoji(Commentaire commentaire, User user, String emoji) {
        Integer commentId = commentaire == null ? null : commentaire.getIdCommentaire();
        return reactionService.reactCommentEmoji(commentId, user, emoji);
    }

    public ReactionActionResult commentReport(Commentaire commentaire, User user, String reason) {
        Integer commentId = commentaire == null ? null : commentaire.getIdCommentaire();
        return reactionService.reportComment(commentId, user, reason);
    }

    public ReactionStatus commentStatus(Commentaire commentaire, User user) {
        Integer commentId = commentaire == null ? null : commentaire.getIdCommentaire();
        return reactionService.commentStatus(commentId, user);
    }

    // Aliases for View Synchronization (Comment ID-based)
    public ReactionStatus getCommentStatus(Integer commentId, int userId) {
        User u = new User();
        u.setIdUser(userId);
        return reactionService.commentStatus(commentId, u);
    }

    public ReactionActionResult toggleCommentReaction(Integer commentId, Integer userId, String kind) {
        User u = new User();
        u.setIdUser(userId);
        return reactionService.toggleCommentReaction(commentId, u, kind);
    }

    public ReactionActionResult reactCommentEmoji(Integer commentId, Integer userId, String emoji) {
        User u = new User();
        u.setIdUser(userId);
        return reactionService.reactCommentEmoji(commentId, u, emoji);
    }

    public ReactionActionResult reportComment(Integer commentId, Integer userId, String reason) {
        User u = new User();
        u.setIdUser(userId);
        return reactionService.reportComment(commentId, u, reason);
    }

    public ReactionActionResult toggleCommentReaction(Integer commentId, User user, String kind) {
        return reactionService.toggleCommentReaction(commentId, user, kind);
    }

    public ReactionActionResult reactCommentEmoji(Integer commentId, User user, String emoji) {
        return reactionService.reactCommentEmoji(commentId, user, emoji);
    }

    public ReactionActionResult reportComment(Integer commentId, User user, String reason) {
        return reactionService.reportComment(commentId, user, reason);
    }

    public ReactionStatus commentStatus(Integer commentId, User user) {
        return reactionService.commentStatus(commentId, user);
    }

    public List<Reaction> reactions() {
        return reactionService.reactions();
    }

    public ReactionService getReactionService() {
        return reactionService;
    }
}
