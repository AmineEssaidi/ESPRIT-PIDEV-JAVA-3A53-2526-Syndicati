package com.syndicati.services.residence;

import com.syndicati.interfaces.IServiceSyndicati;
import com.syndicati.models.residence.Maintenance;
import com.syndicati.services.DatabaseService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceMaintenance implements IServiceSyndicati<Maintenance> {
    private Connection connection;

    public ServiceMaintenance() {
        connection = DatabaseService.getInstance().getConnection();
    }

    @Override
    public void Ajouter(Maintenance Maintenance) throws SQLDataException {
        String req = "INSERT INTO Maintenance (etat_app, etat_plomberie, etat_electricite, etat_chauffage" +
                ", date_derniere_maintenance, description_maint, id_app, recommendation_ia) VALUES ('"
                + Maintenance.getEtat_app() + "', '"
                + Maintenance.getEtat_plomberie() + "', '"
                + Maintenance.getEtat_electricite() + "', '"
                + Maintenance.getEtat_chauffage() + "', '"
                + Maintenance.getDate_derniere_maintenance() + "', '"
                + Maintenance.getdescription_maint() + "', '"
                + Maintenance.getId_app() + "', '"
                + Maintenance.getRecommendation_ia() + "')";

        try {
            Statement stat = connection.createStatement();
            System.out.println("REQ "+req);
            stat.executeUpdate(req);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void Modifier(Maintenance Maintenance) throws SQLDataException {
        String requete = "UPDATE Maintenance SET etat_app = ?, etat_plomberie = ?, etat_electricite = ?, etat_chauffage = ?" +
                ", date_derniere_maintenance = ?, description_maint = ?, id_app = ?, recommendation_ia = ? where id_maintenance = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(requete);
            ps.setString(1, Maintenance.getEtat_app());
            ps.setString(2, Maintenance.getEtat_plomberie());
            ps.setString(3, Maintenance.getEtat_electricite());
            ps.setString(4, Maintenance.getEtat_chauffage());

            String dateMaint = Maintenance.getDate_derniere_maintenance();
                ps.setString(5, dateMaint);

            ps.setString(6, Maintenance.getdescription_maint());
            ps.setInt(7, Maintenance.getId_app());
            ps.setString(8, Maintenance.getRecommendation_ia());
            ps.setInt(9, Maintenance.getId_maintenance());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void Supprimer(Maintenance Maintenance) throws SQLDataException {
        String req = "DELETE FROM Maintenance WHERE id_maintenance =" + Maintenance.getId_app();
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Maintenance> Recuperer() throws SQLDataException {
        String requete = "SELECT * FROM Maintenance";
        List<Maintenance> MaintenanceList = null;
        try {
            Statement statement = connection.createStatement();
            ResultSet MaintenanceSet = statement.executeQuery(requete);
            MaintenanceList = new ArrayList<>();
            while (MaintenanceSet.next()) {
                Maintenance Maintenance = new Maintenance();
                Maintenance.setEtat_app(MaintenanceSet.getString("etat_app"));
                Maintenance.setEtat_plomberie(MaintenanceSet.getString("etat_plomberie"));
                Maintenance.setEtat_electricite(MaintenanceSet.getString("etat_electricite"));
                Maintenance.setEtat_chauffage(MaintenanceSet.getString("etat_chauffage"));
                Maintenance.setDate_derniere_maintenance(MaintenanceSet.getString("date_derniere_maintenance"));
                Maintenance.setdescription_maint(MaintenanceSet.getString("description_maint"));
                Maintenance.setId_app(MaintenanceSet.getInt("id_app"));
                Maintenance.setId_maintenance(MaintenanceSet.getInt("id_maintenance"));
                Maintenance.setRecommendation_ia(MaintenanceSet.getString("recommendation_ia"));
                MaintenanceList.add(Maintenance);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return MaintenanceList;
    }

    public Maintenance MaintenanceParIdAppartement(int id) throws SQLDataException {
        String requete = "SELECT * FROM MAINTENANCE WHERE id_app = "+id;
        Maintenance maintenance = null;
        try {
            PreparedStatement ps = connection.prepareStatement(requete);
            System.out.println(requete);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                maintenance = new Maintenance(
                        rs.getString("recommendation_ia"),
                        rs.getString("description_maint"),
                        rs.getString("date_derniere_maintenance"),
                        rs.getString("etat_chauffage"),
                        rs.getString("etat_electricite"),
                        rs.getString("etat_plomberie"),
                        rs.getString("etat_app"),
                        rs.getInt("id_app"),
                        rs.getInt("id_maintenance")

                );
                System.out.println("Maintenance created!!");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return maintenance;
    }

    public void SupprimerParAppartement(int idApp) {
        String requete = "DELETE FROM Maintenance WHERE id_app = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(requete);
            ps.setInt(1, idApp);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression maintenance: " + e.getMessage());
        }
    }
}
