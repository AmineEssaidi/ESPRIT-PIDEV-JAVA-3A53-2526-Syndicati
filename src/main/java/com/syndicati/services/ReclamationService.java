package com.syndicati.services;

import com.syndicati.models.entities.Reclamation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ReclamationService {

    private static ReclamationService instance;
    private final DatabaseService databaseService;

    private ReclamationService() {
        this.databaseService = DatabaseService.getInstance();
    }

    public static synchronized ReclamationService getInstance() {
        if (instance == null) {
            instance = new ReclamationService();
        }
        return instance;
    }

    public boolean createReclamation(Reclamation reclamation) {
        String query = "INSERT INTO reclamations (titrereclamations, descreclamation, datereclamation, statutreclamation, imagereclamation, id_user, created_at, updated_at) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            
            if (connection == null) {
                System.err.println("Could not get database connection.");
                return false;
            }

            preparedStatement.setString(1, reclamation.getTitrereclamations());
            preparedStatement.setString(2, reclamation.getDescreclamation());
            
            if (reclamation.getDatereclamation() != null) {
                preparedStatement.setTimestamp(3, Timestamp.valueOf(reclamation.getDatereclamation()));
            } else {
                preparedStatement.setNull(3, java.sql.Types.TIMESTAMP);
            }
            
            preparedStatement.setString(4, reclamation.getStatutreclamation());
            
            if (reclamation.getImagereclamation() != null) {
                preparedStatement.setString(5, reclamation.getImagereclamation());
            } else {
                preparedStatement.setNull(5, java.sql.Types.VARCHAR);
            }
            
            preparedStatement.setInt(6, reclamation.getIdUser());
            preparedStatement.setTimestamp(7, Timestamp.valueOf(reclamation.getCreatedAt()));
            preparedStatement.setTimestamp(8, Timestamp.valueOf(reclamation.getUpdatedAt()));

            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                try (java.sql.ResultSet rs = preparedStatement.getGeneratedKeys()) {
                    if (rs.next()) {
                        System.out.println("DEBUG (ReclamationService): Inserted reclamation with ID " + rs.getInt(1) + " successfully.");
                    } else {
                         System.out.println("DEBUG (ReclamationService): Inserted, but no auto-generated keys returned. Ensure 'idreclamations' is AUTO_INCREMENT.");
                    }
                }
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error creating reclamation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public java.util.List<Reclamation> getReclamationsByUserId(int userId) {
        java.util.List<Reclamation> list = new java.util.ArrayList<>();
        String query = "SELECT * FROM reclamations WHERE id_user = ? ORDER BY created_at DESC";
        
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
             
             preparedStatement.setInt(1, userId);
             
             try (java.sql.ResultSet rs = preparedStatement.executeQuery()) {
                 while (rs.next()) {
                     Reclamation rec = new Reclamation();
                     rec.setIdreclamations(rs.getInt("idreclamations"));
                     rec.setTitrereclamations(rs.getString("titrereclamations"));
                     rec.setDescreclamation(rs.getString("descreclamation"));
                     
                     Timestamp dateRec = rs.getTimestamp("datereclamation");
                     if (dateRec != null) rec.setDatereclamation(dateRec.toLocalDateTime());
                     
                     rec.setStatutreclamation(rs.getString("statutreclamation"));
                     rec.setImagereclamation(rs.getString("imagereclamation"));
                     rec.setIdUser(rs.getInt("id_user"));
                     
                     Timestamp created = rs.getTimestamp("created_at");
                     if (created != null) rec.setCreatedAt(created.toLocalDateTime());
                     
                     Timestamp updated = rs.getTimestamp("updated_at");
                     if (updated != null) rec.setUpdatedAt(updated.toLocalDateTime());
                     
                     list.add(rec);
                 }
             }
        } catch (SQLException e) {
            System.err.println("Error fetching reclamations for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}
