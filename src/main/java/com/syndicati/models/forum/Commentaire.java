package com.syndicati.models.forum;

import com.syndicati.models.user.User;
import java.time.LocalDateTime;

/**
 * Forum comment entity aligned with Horizon web schema.
 */
public class Commentaire {

    private Integer idCommentaire;
    private String descriptionCommentaire;
    private String imageCommentaire;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean visibility = true;
    private Publication publication;
    private User user;

    public Integer getIdCommentaire() {
        return idCommentaire;
    }

    public void setIdCommentaire(Integer idCommentaire) {
        this.idCommentaire = idCommentaire;
    }

    public String getDescriptionCommentaire() {
        return descriptionCommentaire;
    }

    public void setDescriptionCommentaire(String descriptionCommentaire) {
        this.descriptionCommentaire = descriptionCommentaire;
    }

    public String getImageCommentaire() {
        return imageCommentaire;
    }

    public void setImageCommentaire(String imageCommentaire) {
        this.imageCommentaire = imageCommentaire;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}