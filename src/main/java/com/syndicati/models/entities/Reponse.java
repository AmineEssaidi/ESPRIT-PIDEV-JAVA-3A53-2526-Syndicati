package com.syndicati.models.entities;

import java.time.LocalDateTime;

public class Reponse {
    private int idreponses;
    private String messagereponse;
    private int reclamationId;
    private String titrereponse;
    private String imagereponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int idUser;

    public Reponse() {}

    public Reponse(String messagereponse, int reclamationId, String titrereponse, int idUser, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.messagereponse = messagereponse;
        this.reclamationId = reclamationId;
        this.titrereponse = titrereponse;
        this.idUser = idUser;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getIdreponses() {
        return idreponses;
    }

    public void setIdreponses(int idreponses) {
        this.idreponses = idreponses;
    }

    public String getMessagereponse() {
        return messagereponse;
    }

    public void setMessagereponse(String messagereponse) {
        this.messagereponse = messagereponse;
    }

    public int getReclamationId() {
        return reclamationId;
    }

    public void setReclamationId(int reclamationId) {
        this.reclamationId = reclamationId;
    }

    public String getTitrereponse() {
        return titrereponse;
    }

    public void setTitrereponse(String titrereponse) {
        this.titrereponse = titrereponse;
    }

    public String getImagereponse() {
        return imagereponse;
    }

    public void setImagereponse(String imagereponse) {
        this.imagereponse = imagereponse;
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

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }
}
