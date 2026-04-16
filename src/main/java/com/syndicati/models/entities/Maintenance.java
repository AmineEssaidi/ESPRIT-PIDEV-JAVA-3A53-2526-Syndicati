package com.syndicati.models.entities;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLDataException;

public class Maintenance {
    int id_maintenance, id_app;
    String etat_app, etat_plomberie, etat_electricite, etat_chauffage, date_derniere_maintenance, description_maint, recommendation_ia;


    public Maintenance(){}

    public Maintenance(String recommendation_ia, String description_maint, String date_derniere_maintenance,
                       String etat_chauffage, String etat_electricite, String etat_plomberie, String etat_app, int id_app) {
        this.etat_app = etat_app;
        this.etat_plomberie = etat_plomberie;
        this.etat_electricite = etat_electricite;
        this.etat_chauffage = etat_chauffage;
        this.date_derniere_maintenance = date_derniere_maintenance;
        this.description_maint = description_maint;
        this.id_app = id_app;
        this.recommendation_ia = recommendation_ia;

    }

    public Maintenance(String recommendation_ia, String description_maint, String date_derniere_maintenance,
                       String etat_chauffage, String etat_electricite, String etat_plomberie, String etat_app, int id_app, int id_maintenance) {
        this.etat_app = etat_app;
        this.etat_plomberie = etat_plomberie;
        this.etat_electricite = etat_electricite;
        this.etat_chauffage = etat_chauffage;
        this.date_derniere_maintenance = date_derniere_maintenance;
        this.description_maint = description_maint;
        this.id_app = id_app;
        this.recommendation_ia = recommendation_ia;
        this.id_maintenance=id_maintenance;

    }

    public int getId_maintenance() {
        return id_maintenance;
    }

    public void setId_maintenance(int id_maintenance) {
        this.id_maintenance = id_maintenance;
    }

    public int getId_app() {
        return id_app;
    }

    public void setId_app(int id_app) {
        this.id_app = id_app;
    }

    public String getEtat_app() {
        return etat_app;
    }

    public void setEtat_app(String etat_app) {
        this.etat_app = etat_app;
    }

    public String getEtat_plomberie() {
        return etat_plomberie;
    }

    public void setEtat_plomberie(String etat_plomberie) {
        this.etat_plomberie = etat_plomberie;
    }

    public String getEtat_electricite() {
        return etat_electricite;
    }

    public void setEtat_electricite(String etat_electricite) {
        this.etat_electricite = etat_electricite;
    }

    public String getEtat_chauffage() {
        return etat_chauffage;
    }

    public void setEtat_chauffage(String etat_chauffage) {
        this.etat_chauffage = etat_chauffage;
    }

    public String getDate_derniere_maintenance() {
        return date_derniere_maintenance;
    }

    public void setDate_derniere_maintenance(String date_derniere_maintenance) {
        this.date_derniere_maintenance = date_derniere_maintenance;
    }

    public String getdescription_maint() {
        return description_maint;
    }

    public void setdescription_maint(String description_maint) {
        this.description_maint = description_maint;
    }

    public String getRecommendation_ia() {
        return recommendation_ia;
    }

    public void setRecommendation_ia(String recommendation_ia) {
        this.recommendation_ia = recommendation_ia;
    }

    @Override
    public String toString()
    {
        return "Date maintenance: "+ this.getDate_derniere_maintenance() + " Recommendation: " + this.getRecommendation_ia()+"\n";
    }


}
