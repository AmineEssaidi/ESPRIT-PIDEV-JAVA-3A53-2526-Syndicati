package com.syndicati.models.syndicat;

import com.syndicati.models.user.User;
import java.time.LocalDateTime;

/**
 * Reponse entity aligned exactly with Horizon database schema.
 * Maps to reponses table.
 */
public class Reponse {

    private Integer idReponses;
    private String titreReponse;
    private String messageReponse;
    private String imageReponse;
    private Reclamation reclamation;
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Reponse() {
    }

    public Reponse(String titre, String message, Reclamation reclamation, User user) {
        this.titreReponse = titre;
        this.messageReponse = message;
        this.reclamation = reclamation;
        this.user = user;
    }

    // Getters and Setters
    public Integer getIdReponses() {
        return idReponses;
    }

    public void setIdReponses(Integer idReponses) {
        this.idReponses = idReponses;
    }

    public String getTitreReponse() {
        return titreReponse;
    }

    public void setTitreReponse(String titreReponse) {
        this.titreReponse = titreReponse;
    }

    public String getMessageReponse() {
        return messageReponse;
    }

    public void setMessageReponse(String messageReponse) {
        this.messageReponse = messageReponse;
    }

    public String getImageReponse() {
        return imageReponse;
    }

    public void setImageReponse(String imageReponse) {
        this.imageReponse = imageReponse;
    }

    public Reclamation getReclamation() {
        return reclamation;
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
}
