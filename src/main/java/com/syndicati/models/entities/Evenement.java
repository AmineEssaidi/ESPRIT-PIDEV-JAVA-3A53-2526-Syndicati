package com.syndicati.models.entities;

import java.time.LocalDateTime;

/**
 * Evenement entity aligned with the web project's evenement table.
 */
public class Evenement {
    private Integer idEvent;
    private String titreEvent;
    private String descriptionEvent;
    private LocalDateTime dateEvent;
    private String lieuEvent;
    private Integer nbPlaces;
    private Integer nbRestants;
    private String statutEvent = "planifie";
    private String imageEvent;
    private String typeEvent;
    private Double latEvent;
    private Double lngEvent;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private User user;

    public Integer getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(Integer idEvent) {
        this.idEvent = idEvent;
    }

    public String getTitreEvent() {
        return titreEvent;
    }

    public void setTitreEvent(String titreEvent) {
        this.titreEvent = titreEvent;
    }

    public String getDescriptionEvent() {
        return descriptionEvent;
    }

    public void setDescriptionEvent(String descriptionEvent) {
        this.descriptionEvent = descriptionEvent;
    }

    public LocalDateTime getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(LocalDateTime dateEvent) {
        this.dateEvent = dateEvent;
    }

    public String getLieuEvent() {
        return lieuEvent;
    }

    public void setLieuEvent(String lieuEvent) {
        this.lieuEvent = lieuEvent;
    }

    public Integer getNbPlaces() {
        return nbPlaces;
    }

    public void setNbPlaces(Integer nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    public Integer getNbRestants() {
        return nbRestants;
    }

    public void setNbRestants(Integer nbRestants) {
        this.nbRestants = nbRestants;
    }

    public String getStatutEvent() {
        return statutEvent;
    }

    public void setStatutEvent(String statutEvent) {
        this.statutEvent = statutEvent;
    }

    public String getImageEvent() {
        return imageEvent;
    }

    public void setImageEvent(String imageEvent) {
        this.imageEvent = imageEvent;
    }

    public String getTypeEvent() {
        return typeEvent;
    }

    public void setTypeEvent(String typeEvent) {
        this.typeEvent = typeEvent;
    }

    public Double getLatEvent() {
        return latEvent;
    }

    public void setLatEvent(Double latEvent) {
        this.latEvent = latEvent;
    }

    public Double getLngEvent() {
        return lngEvent;
    }

    public void setLngEvent(Double lngEvent) {
        this.lngEvent = lngEvent;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
