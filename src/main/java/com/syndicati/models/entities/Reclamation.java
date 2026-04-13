package com.syndicati.models.entities;

import java.time.LocalDateTime;

public class Reclamation {
    private int idreclamations;
    private String titrereclamations;
    private String descreclamation;
    private LocalDateTime datereclamation;
    private String statutreclamation; // active, en_attente, refuse, termine
    private String imagereclamation;
    private int id_user;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public Reclamation() {
        this.statutreclamation = "en_attente";
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
    }

    public Reclamation(String titrereclamations, String descreclamation, LocalDateTime datereclamation, int id_user) {
        this();
        this.titrereclamations = titrereclamations;
        this.descreclamation = descreclamation;
        this.datereclamation = datereclamation;
        this.id_user = id_user;
    }

    public int getIdreclamations() {
        return idreclamations;
    }

    public void setIdreclamations(int idreclamations) {
        this.idreclamations = idreclamations;
    }

    public String getTitrereclamations() {
        return titrereclamations;
    }

    public void setTitrereclamations(String titrereclamations) {
        this.titrereclamations = titrereclamations;
    }

    public String getDescreclamation() {
        return descreclamation;
    }

    public void setDescreclamation(String descreclamation) {
        this.descreclamation = descreclamation;
    }

    public LocalDateTime getDatereclamation() {
        return datereclamation;
    }

    public void setDatereclamation(LocalDateTime datereclamation) {
        this.datereclamation = datereclamation;
    }

    public String getStatutreclamation() {
        return statutreclamation;
    }

    public void setStatutreclamation(String statutreclamation) {
        this.statutreclamation = statutreclamation;
    }

    public String getImagereclamation() {
        return imagereclamation;
    }

    public void setImagereclamation(String imagereclamation) {
        this.imagereclamation = imagereclamation;
    }

    public int getIdUser() {
        return id_user;
    }

    public void setIdUser(int id_user) {
        this.id_user = id_user;
    }

    public LocalDateTime getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
}
