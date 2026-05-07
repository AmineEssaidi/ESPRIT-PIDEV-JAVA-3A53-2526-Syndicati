package com.syndicati.models.residence;

/**
 * Review entity - represents user reviews/ratings for apartments.
 */
public class Review {

    private Integer idReview;
    private Integer idUser;
    private Integer idApartment;
    private Integer score;  // Rating score (typically 1-5)

    public Review() {}

    public Review(Integer idUser, Integer idApartment, Integer score) {
        this.idUser = idUser;
        this.idApartment = idApartment;
        this.score = score;
    }

    public Review(Integer idUser, Integer idApartment, Integer score, Integer idReview) {
        this(idUser, idApartment, score);
        this.idReview = idReview;
    }

    public Integer getIdReview() {
        return idReview;
    }

    public void setIdReview(Integer idReview) {
        this.idReview = idReview;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public Integer getIdApartment() {
        return idApartment;
    }

    public void setIdApartment(Integer idApartment) {
        this.idApartment = idApartment;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Review{" +
                "idReview=" + idReview +
                ", idUser=" + idUser +
                ", idApartment=" + idApartment +
                ", score=" + score +
                '}';
    }
}
