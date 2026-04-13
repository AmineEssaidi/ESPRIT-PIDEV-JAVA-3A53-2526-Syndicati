package com.syndicati.models.entities;

import java.time.LocalDateTime;

/**
 * Participation entity aligned with the web project's participation table.
 */
public class Participation {
    private Integer idParticipation;
    private Evenement evenement;
    private User user;
    private LocalDateTime dateParticipation;
    private String statutParticipation = "en_attente";
    private Integer nbAccompagnants = 0;
    private String commentaireParticipation;
    private String formulaireData; // Stored as JSON string
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    public Integer getIdParticipation() {
        return idParticipation;
    }

    public void setIdParticipation(Integer idParticipation) {
        this.idParticipation = idParticipation;
    }

    public Evenement getEvenement() {
        return evenement;
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getDateParticipation() {
        return dateParticipation;
    }

    public void setDateParticipation(LocalDateTime dateParticipation) {
        this.dateParticipation = dateParticipation;
    }

    public String getStatutParticipation() {
        return statutParticipation;
    }

    public void setStatutParticipation(String statutParticipation) {
        this.statutParticipation = statutParticipation;
    }

    public Integer getNbAccompagnants() {
        return nbAccompagnants;
    }

    public void setNbAccompagnants(Integer nbAccompagnants) {
        this.nbAccompagnants = nbAccompagnants;
    }

    public String getCommentaireParticipation() {
        return commentaireParticipation;
    }

    public void setCommentaireParticipation(String commentaireParticipation) {
        this.commentaireParticipation = commentaireParticipation;
    }

    public String getFormulaireData() {
        return formulaireData;
    }

    public void setFormulaireData(String formulaireData) {
        this.formulaireData = formulaireData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }
}
