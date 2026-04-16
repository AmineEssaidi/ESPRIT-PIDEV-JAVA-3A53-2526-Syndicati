package com.syndicati.models.services;

import com.syndicati.models.entities.Appartement;
import com.syndicati.models.entities.Appartement;
import com.syndicati.models.entities.Residence;
import com.syndicati.models.services.IServiceSyndicati;
import com.syndicati.services.DatabaseService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAppartement implements IServiceSyndicati<Appartement> {
    private Connection connection;
    public ServiceAppartement()
    {
        connection= DatabaseService.getInstance().getConnection();
    }

    @Override
    public void Ajouter(Appartement Appartement) throws SQLDataException
    {
        String req = "INSERT INTO APPARTEMENT (residence_id, id_user, parking, disponible, image_a, type_a" +
                ", appartement_info, superficie, prix_location, prix_vente, date_construction) VALUES ('"
                + Appartement.getResidence_id() + "', '"
                + Appartement.getId_user() + "', '"
                + Appartement.getParking() + "', '"
                + Appartement.getDisponible() + "', '"
                + Appartement.getImage_a() + "', '"
                + Appartement.getType_a() + "', '"
                + Appartement.getAppartement_info() + "', '"
                + Appartement.getSuperficie() + "', '"
                + Appartement.getPrix_location() + "', '"
                + Appartement.getPrix_vente() + "', '"
                + "2019-01-01" + "')";
        System.out.println("REQUETE SQL: "+ req);

        try
        {
            Statement stat=connection.createStatement();
            stat.executeUpdate(req);
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void Modifier(Appartement Appartement) throws SQLDataException
    {
        String requete="UPDATE Appartement SET residence_id = ?, id_user = ?, parking =  ?, disponible = ?, image_a=?" +
                ", type_a= ? , appartement_info = ?, superficie = ?, prix_location = ?, prix_vente = ?, date_construction = ? where id_app= ? ";
        try
        {
            PreparedStatement ps= connection.prepareStatement(requete);
            ps.setInt(1, Appartement.getResidence_id());
            ps.setInt(2, Appartement.getId_user());
            ps.setInt(3, Appartement.getParking());
            ps.setInt(4, Appartement.getDisponible());
            ps.setString(5, Appartement.getImage_a());
            ps.setString(6, Appartement.getType_a());
            ps.setString(7, Appartement.getAppartement_info());
            ps.setInt(8, Appartement.getSuperficie());
            ps.setInt(9, Appartement.getPrix_location());
            ps.setInt(10, Appartement.getPrix_vente());
            ps.setString(11, Appartement.getDate_construction());
            ps.setInt(12, Appartement.getId_app());
            ps.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void Supprimer(Appartement Appartement) throws SQLDataException
    {
        String req= "DELETE FROM Appartement WHERE id_app ="+ Appartement.getId_app();
        try
        {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Appartement> Recuperer() throws SQLDataException
    {
        String requete="SELECT * FROM Appartement";
        List<Appartement> AppartementList= null;
        try
        {
            Statement statement= connection.createStatement();
            ResultSet AppartementSet= statement.executeQuery(requete);
            AppartementList = new ArrayList<>();
            while(AppartementSet.next())
            {
                Appartement Appartement=new Appartement();
                Appartement.setId_app(AppartementSet.getInt("id_app"));
                Appartement.setResidence_id(AppartementSet.getInt("residence_id"));
                Appartement.setId_user(AppartementSet.getInt("id_user"));
                Appartement.setParking(AppartementSet.getInt("parking"));
                Appartement.setDisponible(AppartementSet.getInt("disponible"));
                Appartement.setImage_a(AppartementSet.getString("image_a"));
                Appartement.setType_a(AppartementSet.getString("type_a"));
                Appartement.setAppartement_info(AppartementSet.getString("appartement_info"));
                Appartement.setSuperficie(AppartementSet.getInt("superficie"));
                Appartement.setPrix_location(AppartementSet.getInt("prix_location"));
                Appartement.setPrix_vente(AppartementSet.getInt("prix_vente"));
                Appartement.setDate_construction(AppartementSet.getString("date_construction"));
                AppartementList.add(Appartement);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return AppartementList;
    }


    public List<Appartement> RecupererTri(String tri_par, String ordre) throws SQLDataException
    {
        String requete="SELECT * FROM Appartement ORDER BY "+ tri_par + " " + ordre;
        List<Appartement> AppartementList= null;
        try
        {
            Statement statement= connection.createStatement();
            ResultSet AppartementSet= statement.executeQuery(requete);
            AppartementList = new ArrayList<>();
            while(AppartementSet.next())
            {
                Appartement Appartement=new Appartement();
                Appartement.setId_app(AppartementSet.getInt("id_app"));
                Appartement.setResidence_id(AppartementSet.getInt("residence_id"));
                Appartement.setId_user(AppartementSet.getInt("id_user"));
                Appartement.setParking(AppartementSet.getInt("parking"));
                Appartement.setDisponible(AppartementSet.getInt("disponible"));
                Appartement.setImage_a(AppartementSet.getString("image_a"));
                Appartement.setType_a(AppartementSet.getString("type_a"));
                Appartement.setAppartement_info(AppartementSet.getString("appartement_info"));
                Appartement.setSuperficie(AppartementSet.getInt("superficie"));
                Appartement.setPrix_location(AppartementSet.getInt("prix_location"));
                Appartement.setPrix_vente(AppartementSet.getInt("prix_vente"));
                Appartement.setDate_construction(AppartementSet.getString("date_construction"));
                AppartementList.add(Appartement);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return AppartementList;
    }


    public List<Appartement> AppartementsParResidence(Residence Residence) throws SQLDataException
    {
        String requete= "SELECT * FROM APPARTEMENT WHERE residence_id = ?";
        List<Appartement> AppartementList= null;

        try
        {
            PreparedStatement ps = connection.prepareStatement(requete);
            ps.setInt(1, Residence.getId_residence());
            ResultSet AppartementSet= ps.executeQuery();
            AppartementList = new ArrayList<>();
            while(AppartementSet.next())
            {
                Appartement Appartement=new Appartement();
                Appartement.setId_app(AppartementSet.getInt(1));
                Appartement.setResidence_id(AppartementSet.getInt("residence_id"));
                Appartement.setId_user(AppartementSet.getInt("id_user"));
                Appartement.setParking(AppartementSet.getInt("parking"));
                Appartement.setDisponible(AppartementSet.getInt("disponible"));
                Appartement.setImage_a(AppartementSet.getString("image_a"));
                Appartement.setType_a(AppartementSet.getString("type_a"));
                Appartement.setAppartement_info(AppartementSet.getString("appartement_info"));
                Appartement.setSuperficie(AppartementSet.getInt("superficie"));
                Appartement.setPrix_location(AppartementSet.getInt("prix_location"));
                Appartement.setPrix_vente(AppartementSet.getInt("prix_vente"));
                Appartement.setDate_construction(AppartementSet.getString("date_construction"));
                AppartementList.add(Appartement);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return AppartementList;
    }

    public Appartement TrouverAppartementParId(int id) {
        String requete = "SELECT * FROM APPARTEMENT WHERE id_app = ? ";
        Appartement appartement = null;
        try {
            PreparedStatement ps = connection.prepareStatement(requete);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                appartement = new Appartement(
                        rs.getInt("residence_id"),
                        rs.getInt("id_user"),
                        rs.getInt("parking"),
                        rs.getInt("disponible"),
                        rs.getString("image_a"),
                        rs.getString("type_a"),
                        rs.getString("appartement_info"),
                        rs.getInt("superficie"),
                        rs.getInt("prix_location"),
                        rs.getInt("prix_vente"),
                        rs.getString("date_construction"),
                        rs.getInt("id_app")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return appartement;
    }

    public int PlusRecentAppartementID() throws SQLDataException {
        String req = "SELECT MAX(id_app) FROM Appartement";
        try {
            Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery(req);
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }


}
