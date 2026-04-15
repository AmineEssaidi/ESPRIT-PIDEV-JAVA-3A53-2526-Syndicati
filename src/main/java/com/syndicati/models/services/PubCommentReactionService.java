package com.syndicati.models.services;

import com.syndicati.models.entities.PubCommentReaction;
import com.syndicati.models.repositories.PubCommentReactionRepository;

import java.util.Optional;

public class PubCommentReactionService {
    private final PubCommentReactionRepository repository;

    public PubCommentReactionService() {
        this.repository = new PubCommentReactionRepository();
    }

    public boolean toggleReaction(int userId, Integer pubId, Integer commId, String kind, String emoji, String reason) {
        // 1. Check if this specific reaction already exists
        Optional<PubCommentReaction> existing = repository.findOne(userId, pubId, commId, kind);

        if (existing.isPresent()) {
            // Already reacted with the same kind -> Remove it (Toggle off)
            return repository.delete(userId, pubId, commId, kind);
        } else {
            // 2. Mutual exclusivity logic if needed (Like vs Dislike)
            if (PubCommentReaction.KIND_LIKE.equals(kind)) {
                repository.delete(userId, pubId, commId, PubCommentReaction.KIND_DISLIKE);
            } else if (PubCommentReaction.KIND_DISLIKE.equals(kind)) {
                repository.delete(userId, pubId, commId, PubCommentReaction.KIND_LIKE);
            }

            // 3. Create new reaction
            PubCommentReaction r = new PubCommentReaction();
            r.setUserId(userId);
            r.setPublicationId(pubId);
            r.setCommentaireId(commId);
            r.setKind(kind);
            r.setEmoji(emoji);
            r.setReportReason(reason);
            return repository.create(r) > 0;
        }
    }

    public int getReactionCount(Integer pubId, Integer commId, String kind) {
        return repository.countByTarget(pubId, commId, kind);
    }

    public boolean hasUserReacted(int userId, Integer pubId, Integer commId, String kind) {
        return repository.findOne(userId, pubId, commId, kind).isPresent();
    }
}
