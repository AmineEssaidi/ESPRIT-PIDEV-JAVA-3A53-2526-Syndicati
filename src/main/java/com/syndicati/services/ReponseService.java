package com.syndicati.services;

import com.syndicati.models.entities.Reponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReponseService {
    private static ReponseService instance;
    private final DatabaseService databaseService;

    private ReponseService() {
        this.databaseService = DatabaseService.getInstance();
    }

    public static synchronized ReponseService getInstance() {
        if (instance == null) {
            instance = new ReponseService();
        }
        return instance;
    }

    public boolean createReponse(Reponse reponse) {
        String query = "INSERT INTO reponses (messagereponse, reclamation_id, titrereponse, imagereponse, created_at, updated_at, id_user) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            if (connection == null) return false;

            preparedStatement.setString(1, reponse.getMessagereponse());
            preparedStatement.setInt(2, reponse.getReclamationId());
            preparedStatement.setString(3, reponse.getTitrereponse());
            preparedStatement.setString(4, reponse.getImagereponse());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(reponse.getCreatedAt()));
            preparedStatement.setTimestamp(6, Timestamp.valueOf(reponse.getUpdatedAt()));
            preparedStatement.setInt(7, reponse.getIdUser());

            return preparedStatement.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating response: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Reponse> getAllReponses() {
        List<Reponse> list = new ArrayList<>();
        String query = "SELECT * FROM reponses ORDER BY created_at DESC";
        
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
             
             try (java.sql.ResultSet rs = preparedStatement.executeQuery()) {
                 while (rs.next()) {
                     Reponse rep = new Reponse();
                     rep.setIdreponses(rs.getInt("idreponses"));
                     rep.setMessagereponse(rs.getString("messagereponse"));
                     rep.setReclamationId(rs.getInt("reclamation_id"));
                     rep.setTitrereponse(rs.getString("titrereponse"));
                     rep.setImagereponse(rs.getString("imagereponse"));
                     
                     Timestamp created = rs.getTimestamp("created_at");
                     if (created != null) rep.setCreatedAt(created.toLocalDateTime());
                     
                     Timestamp updated = rs.getTimestamp("updated_at");
                     if (updated != null) rep.setUpdatedAt(updated.toLocalDateTime());
                     
                     rep.setIdUser(rs.getInt("id_user"));
                     
                     list.add(rep);
                 }
             }
        } catch (SQLException e) {
            System.err.println("Error fetching responses: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}
