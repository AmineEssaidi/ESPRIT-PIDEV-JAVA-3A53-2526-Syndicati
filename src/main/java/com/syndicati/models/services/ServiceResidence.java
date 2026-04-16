package com.syndicati.models.services;

import com.syndicati.models.entities.Residence;
import com.syndicati.services.DatabaseService;
import com.syndicati.models.services.IServiceSyndicati;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceResidence implements IServiceSyndicati<Residence> {
    private Connection connection;
    public ServiceResidence()
    {
    connection= DatabaseService.getInstance().getConnection();
    }

    @Override
    public void Ajouter(Residence Residence) throws SQLDataException
    {
        String req = "INSERT INTO RESIDENCE (nom_r, adresse, image_r, date_ajout, n_appartements, n_etages, n_blocs) VALUES ('"
                + Residence.getNom_r() + "', '"
                + Residence.getAdresse() + "', "
                + (Residence.getImage_r() != null ? "'" + Residence.getImage_r() + "'" : "NULL") + ", '"
                + Residence.getDate_ajout() + " 00:00:00', '"
                + Residence.getN_appartements() + "', '"
                + Residence.getN_etages() + "', '"
                + Residence.getN_blocs() + "')";

        try
        {
            Statement stat=connection.createStatement();
            System.out.println("LA REQUETE SQL "+req);
            stat.executeUpdate(req);
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void Modifier(Residence Residence) throws SQLDataException
    {
        String requete="UPDATE RESIDENCE set nom_r = ? ,adresse= ? , image_r = ? , date_ajout = ?" +
                ", n_appartements = ?, n_etages = ? , n_blocs = ? where id_residence= ? ";
        try
        {
            PreparedStatement ps= connection.prepareStatement(requete);
            ps.setString(1, Residence.getNom_r());
            ps.setString(2, Residence.getAdresse());
            ps.setString(3, Residence.getImage_r());
            ps.setString(4, Residence.getDate_ajout());
            ps.setInt(5, Residence.getN_appartements());
            ps.setInt(6, Residence.getN_etages());
            ps.setString(7, Residence.getN_blocs());
            ps.setInt(8, Residence.getId_residence());
            ps.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void Supprimer(Residence Residence) throws SQLDataException
    {
        String req= "DELETE FROM RESIDENCE WHERE id_residence ="+ Residence.getId_residence();
        try
        {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Residence> Recuperer() throws SQLDataException
    {
        String requete="SELECT * FROM RESIDENCE";
        List<Residence> ResidenceList= null;
        try
        {
            Statement statement= connection.createStatement();
            ResultSet ResidenceSet= statement.executeQuery(requete);
            ResidenceList = new ArrayList<>();
            while(ResidenceSet.next())
            {
                Residence Residence=new Residence();
                Residence.setId_residence(ResidenceSet.getInt(1));
                Residence.setNom_r(ResidenceSet.getString("nom_r"));
                Residence.setAdresse(ResidenceSet.getString("adresse"));
                Residence.setImage_r(ResidenceSet.getString("image_r"));
                Residence.setDate_ajout(ResidenceSet.getString("date_ajout"));
                Residence.setN_appartements(ResidenceSet.getInt("n_appartements"));
                Residence.setN_etages(ResidenceSet.getInt("n_etages"));
                Residence.setN_blocs(ResidenceSet.getString("n_blocs"));
                ResidenceList.add(Residence);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ResidenceList;
    }

    public List <Residence> RecupererTri(String tri_par, String ordre) throws SQLDataException
    {
        String requete="SELECT * FROM RESIDENCE ORDER BY "+ tri_par + " " + ordre;
        List<Residence> ResidenceList= null;
        try
        {
            Statement statement= connection.createStatement();
            ResultSet ResidenceSet= statement.executeQuery(requete);
            ResidenceList = new ArrayList<>();
            while(ResidenceSet.next())
            {
                Residence Residence=new Residence();
                Residence.setId_residence(ResidenceSet.getInt(1));
                Residence.setNom_r(ResidenceSet.getString("nom_r"));
                Residence.setAdresse(ResidenceSet.getString("adresse"));
                Residence.setImage_r(ResidenceSet.getString("image_r"));
                Residence.setDate_ajout(ResidenceSet.getString("date_ajout"));
                Residence.setN_appartements(ResidenceSet.getInt("n_appartements"));
                Residence.setN_etages(ResidenceSet.getInt("n_etages"));
                Residence.setN_blocs(ResidenceSet.getString("n_blocs"));
                ResidenceList.add(Residence);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ResidenceList;

    }

    public Residence TrouverResidenceParId(int id)
    {
        String requete= "SELECT * FROM RESIDENCE WHERE id_residence = ? ";
        Residence Residence=null;
        try
        {
            PreparedStatement ps= connection.prepareStatement(requete);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Residence=new Residence(
                rs.getString("nom_r"),
                rs.getString("adresse"),
                rs.getString("image_r"),
                rs.getString("date_ajout"),
                rs.getInt("n_appartements"),
                rs.getInt("n_etages"),
                rs.getString("n_blocs"),
                rs.getInt("id_residence")
                );

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Residence;
    }

    public Residence TrouverResidenceParNom(String nom)
    {
        String requete= "SELECT * FROM RESIDENCE WHERE nom_r = ? ;";
        Residence Residence=null;
        try
        {
            PreparedStatement ps= connection.prepareStatement(requete);
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Residence=new Residence(
                        rs.getString("nom_r"),
                        rs.getString("adresse"),
                        rs.getString("image_r"),
                        rs.getString("date_ajout"),
                        rs.getInt("n_appartements"),
                        rs.getInt("n_etages"),
                        rs.getString("n_blocs"),
                        rs.getInt("id_residence")

                );

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Residence;
    }

}

