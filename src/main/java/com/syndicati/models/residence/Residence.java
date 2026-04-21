package com.syndicati.models.residence;

public class Residence {

    private int id_residence, n_appartements, n_etages;
    private String nom_r, adresse, image_r, date_ajout, n_blocs;

    public Residence(){}

    public Residence(String nom_r, String adresse, String image_r, String date_ajout,
                     int n_appartements, int n_etages, String n_blocs)
    {
        this.nom_r=nom_r;
        this.adresse=adresse;
        this.image_r=image_r;
        this.date_ajout=date_ajout;
        this.n_appartements=n_appartements;
        this.n_etages=n_etages;
        this.n_blocs=n_blocs;
    }
    public Residence(String nom_r, String adresse, String image_r, String date_ajout,
                     int n_appartements, int n_etages, String n_blocs, int id_residence)
    {
        this.nom_r=nom_r;
        this.adresse=adresse;
        this.image_r=image_r;
        this.date_ajout=date_ajout;
        this.n_appartements=n_appartements;
        this.n_etages=n_etages;
        this.n_blocs=n_blocs;
        this.id_residence=id_residence;
    }

    public int getId_residence() {
        return id_residence;
    }

    public void setId_residence(int id_residence) {
        this.id_residence = id_residence;
    }

    public int getN_appartements() {
        return n_appartements;
    }

    public void setN_appartements(int n_appartements) {
        this.n_appartements = n_appartements;
    }

    public int getN_etages() {
        return n_etages;
    }

    public void setN_etages(int n_etages) {
        this.n_etages = n_etages;
    }

    public String getN_blocs() {
        return n_blocs;
    }

    public void setN_blocs(String n_blocs) {
        this.n_blocs = n_blocs;
    }

    public String getNom_r() {
        return nom_r;
    }

    public void setNom_r(String nom_r) {
        this.nom_r = nom_r;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getImage_r() {
        return image_r;
    }

    public void setImage_r(String image_r) {
        this.image_r = image_r;
    }

    public String getDate_ajout() {
        return date_ajout;
    }

    public void setDate_ajout(String date_ajout) {
        this.date_ajout = date_ajout;
    }

    @Override
    public String toString()
    {
        return "\n Nom residence: "+this.nom_r+"\n Adresse: "+this.adresse + "\n Date Ajout: "+this.getDate_ajout()+"\n";
    }
}
