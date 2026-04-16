package com.syndicati.models.syndicat;

import com.syndicati.models.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reclamation entity aligned exactly with Horizon database schema.
 * Maps to reclamations table.
 */
public class Reclamation {

    public static final Set<String> STATUTS = new HashSet<>(Arrays.asList(
        "active", "en_attente", "refuse", "termine"
    ));

    private Integer idReclamations;
    private String titreReclamations;
    private String descReclamation;
    private LocalDateTime dateReclamation;
    private String statutReclamation = "en_attente";
    private String imageReclamation;
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Reponse> reponses = new ArrayList<>();

    // Constructors
    public Reclamation() {
    }

    public Reclamation(String titre, String desc, LocalDateTime date, User user) {
        this.titreReclamations = titre;
        this.descReclamation = desc;
        this.dateReclamation = date;
        this.user = user;
        this.statutReclamation = "en_attente";
    }

    // Getters and Setters
    public Integer getIdReclamations() {
        return idReclamations;
    }

    public void setIdReclamations(Integer idReclamations) {
        this.idReclamations = idReclamations;
    }

    public String getTitreReclamations() {
        return titreReclamations;
    }

    public void setTitreReclamations(String titreReclamations) {
        this.titreReclamations = titreReclamations;
    }

    public String getDescReclamation() {
        return descReclamation;
    }

    public void setDescReclamation(String descReclamation) {
        this.descReclamation = descReclamation;
    }

    public LocalDateTime getDateReclamation() {
        return dateReclamation;
    }

    public void setDateReclamation(LocalDateTime dateReclamation) {
        this.dateReclamation = dateReclamation;
    }

    public String getStatutReclamation() {
        return statutReclamation;
    }

    public void setStatutReclamation(String statutReclamation) {
        this.statutReclamation = statutReclamation;
    }

    public String getImageReclamation() {
        return imageReclamation;
    }

    public void setImageReclamation(String imageReclamation) {
        this.imageReclamation = imageReclamation;
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

    public List<Reponse> getReponses() {
        if (reponses == null) {
            reponses = new ArrayList<>();
        }
        return reponses;
    }

    public void setReponses(List<Reponse> reponses) {
        this.reponses = reponses;
    }

    public void addReponse(Reponse reponse) {
        if (reponses == null) {
            reponses = new ArrayList<>();
        }
        reponses.add(reponse);
        reponse.setReclamation(this);
    }
}
