package com.syndicati.models.residence;

public class Appartement extends Residence {

    int id_app, residence_id, id_user, parking, disponible, superficie, prix_location, prix_vente;
    String image_a, type_a, date_construction, appartement_info;
    Maintenance Maintenance;

    public Appartement(){}

    public Appartement(int residence_id, int id_user, int parking, int disponible, String image_a
    , String type_a, String appartement_info, int superficie, int prix_location, int prix_vente, String date_construction)
    {
        this.residence_id=residence_id;
        this.id_user=id_user;
        this.parking=parking;
        this.disponible=disponible;
        this.image_a=image_a;
        this.type_a=type_a;
        this.appartement_info=appartement_info;
        this.superficie=superficie;
        this.prix_location=prix_location;
        this.prix_vente=getPrix_vente();
        this.date_construction=date_construction;
    }
    public Appartement(int residence_id, int id_user, int parking, int disponible, String image_a
            , String type_a, String appartement_info, int superficie, int prix_location, int prix_vente, String date_construction,
                       int id_app)
    {
        this.residence_id=residence_id;
        this.id_user=id_user;
        this.parking=parking;
        this.disponible=disponible;
        this.image_a=image_a;
        this.type_a=type_a;
        this.appartement_info=appartement_info;
        this.superficie=superficie;
        this.prix_location=prix_location;
        this.prix_vente=prix_vente;
        this.date_construction=date_construction;
        this.id_app=id_app;
    }


    public String getAppartement_info() {
        return appartement_info;
    }

    public void setAppartement_info(String appartement_info) {
        this.appartement_info = appartement_info;
    }

    public int getId_app() {
        return id_app;
    }

    public void setId_app(int id_app) {
        this.id_app = id_app;
    }

    public int getResidence_id() {
        return residence_id;
    }

    public void setResidence_id(int residence_id) {
        this.residence_id = residence_id;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public int getParking() {
        return parking;
    }

    public void setParking(int parking) {
        this.parking = parking;
    }

    public int getDisponible() {
        return disponible;
    }

    public void setDisponible(int disponible) {
        this.disponible = disponible;
    }

    public int getSuperficie() {
        return superficie;
    }

    public void setSuperficie(int superficie) {
        this.superficie = superficie;
    }

    public int getPrix_location() {
        return prix_location;
    }

    public void setPrix_location(int prix_location) {
        this.prix_location = prix_location;
    }

    public int getPrix_vente() {
        return prix_vente;
    }

    public void setPrix_vente(int prix_vente) {
        this.prix_vente = prix_vente;
    }

    public String getImage_a() {
        return image_a;
    }

    public void setImage_a(String image_a) {
        this.image_a = image_a;
    }

    public String getType_a() {
        return type_a;
    }

    public void setType_a(String type_a) {
        this.type_a = type_a;
    }

    public String getDate_construction() {
        return date_construction;
    }

    public void setDate_construction(String date_construction) {
        this.date_construction = date_construction;
    }

    @Override
    public String toString()
    {
        return "ID Appartement: "+this.getId_app()+"\n Appartement "+this.getType_a()+" Prix: "+ this.getPrix_location();
    }
}
