package com.syndicati.services.residence;

import com.syndicati.models.residence.Revue;
import com.syndicati.interfaces.IServiceSyndicati;
import com.syndicati.services.DatabaseService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ServiceRevue implements IServiceSyndicati <Revue>{
    private Connection connection;
    public ServiceRevue() {
        connection = DatabaseService.getInstance().getConnection();
    }

    @Override
    public void Ajouter(Revue revue) throws SQLDataException {
        String req = "INSERT INTO REVUE (id_utilisateur, id_appartement, score) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, revue.getId_utilisateur());
            ps.setInt(2, revue.getId_appartement());
            ps.setInt(3, revue.getScore());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void Modifier(Revue revue) throws SQLDataException {
        String req = "UPDATE REVUE SET id_utilisateur = ?, id_appartement = ?, score = ? WHERE id_revue = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, revue.getId_utilisateur());
            ps.setInt(2, revue.getId_appartement());
            ps.setInt(3, revue.getScore());
            ps.setInt(4, revue.getId_revue());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void Supprimer(Revue revue) throws SQLDataException {
        String req = "DELETE FROM REVUE WHERE id_revue = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, revue.getId_revue());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Revue> Recuperer() throws SQLDataException {
        List<Revue> revues = new ArrayList<>();
        try {
            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM REVUE");
            while (rs.next()) {
                revues.add(new Revue(
                        rs.getInt("id_utilisateur"),
                        rs.getInt("id_appartement"),
                        rs.getInt("score"),
                        rs.getInt("id_revue")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return revues;
    }

    public List<Revue> RevuesParAppartement(int id_appartement) throws SQLDataException {
        List<Revue> revues = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM REVUE WHERE id_appartement = ?");
            ps.setInt(1, id_appartement);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                revues.add(new Revue(
                        rs.getInt("id_utilisateur"),
                        rs.getInt("id_appartement"),
                        rs.getInt("score"),
                        rs.getInt("id_revue")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return revues;
    }

    public double ScoreMoyen(int id_appartement) throws SQLDataException {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT AVG(score) FROM REVUE WHERE id_appartement = ?");
            ps.setInt(1, id_appartement);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
