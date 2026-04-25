package com.syndicati.models.residence;

public class Revue {


    int Id_utilisateur, Id_appartement, Score, Id_revue;

    public Revue(int id_utilisateur, int id_appartement, int score) {
        Id_utilisateur = id_utilisateur;
        Id_appartement = id_appartement;
        Score = score;
    }

    public Revue(int id_utilisateur, int id_appartement, int score, int id_revue) {
        Id_utilisateur = id_utilisateur;
        Id_appartement = id_appartement;
        Score = score;
        Id_revue = id_revue;
    }

    public int getId_utilisateur() {
        return Id_utilisateur;
    }

    public void setId_utilisateur(int id_utilisateur) {
        Id_utilisateur = id_utilisateur;
    }

    public int getId_appartement() {
        return Id_appartement;
    }

    public void setId_appartement(int id_appartement) {
        Id_appartement = id_appartement;
    }

    public int getScore() {
        return Score;
    }

    public void setScore(int score) {
        Score = score;
    }

    public int getId_revue() {
        return Id_revue;
    }

    public void setId_revue(int id_revue) {
        Id_revue = id_revue;
    }





}
