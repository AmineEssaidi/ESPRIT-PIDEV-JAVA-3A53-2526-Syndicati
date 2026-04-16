package com.syndicati.models.forum;

import com.syndicati.models.user.User;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Forum publication entity aligned with Horizon web schema.
 */
public class Publication {

    public static final Set<String> CATEGORIES = new HashSet<>(Arrays.asList(
        "Announcement",
        "Suggestion",
        "Jeux Video",
        "Informatique",
        "Nouveauté",
        "Discussion General",
        "Culture",
        "Sport"
    ));

    private Integer idPublication;
    private String titrePub;
    private String descriptionPub;
    private LocalDateTime dateCreationPub;
    private String categoriePub;
    private String imagePub;
    private User user;

    public Integer getIdPublication() {
        return idPublication;
    }

    public void setIdPublication(Integer idPublication) {
        this.idPublication = idPublication;
    }

    public String getTitrePub() {
        return titrePub;
    }

    public void setTitrePub(String titrePub) {
        this.titrePub = titrePub;
    }

    public String getDescriptionPub() {
        return descriptionPub;
    }

    public void setDescriptionPub(String descriptionPub) {
        this.descriptionPub = descriptionPub;
    }

    public LocalDateTime getDateCreationPub() {
        return dateCreationPub;
    }

    public void setDateCreationPub(LocalDateTime dateCreationPub) {
        this.dateCreationPub = dateCreationPub;
    }

    public String getCategoriePub() {
        return categoriePub;
    }

    public void setCategoriePub(String categoriePub) {
        this.categoriePub = categoriePub;
    }

    public String getImagePub() {
        return imagePub;
    }

    public void setImagePub(String imagePub) {
        this.imagePub = imagePub;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}