package com.syndicati.models.residence.data;

import com.syndicati.models.residence.Maintenance;
import com.syndicati.services.DatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Maintenance data access layer.
 */
public class MaintenanceRepository {

    private final DatabaseService databaseService;

    public MaintenanceRepository() {
        this.databaseService = DatabaseService.getInstance();
    }

    public List<Maintenance> findAll() {
        String cacheKey = "maintenance_all";
        List<Maintenance> cached = databaseService.getCache(cacheKey);
        if (cached != null) return cached;

        String sql = "SELECT id_maintenance, id_app AS id_apartment, etat_app AS general_condition, " +
            "etat_plomberie AS plumbing_condition, etat_electricite AS electrical_condition, " +
            "etat_chauffage AS heating_condition, date_derniere_maintenance AS last_maintenance_date, " +
            "description_maint AS description, recommendation_ia AS ai_recommendation " +
            "FROM maintenance ORDER BY date_derniere_maintenance DESC";
        List<Maintenance> maintenanceList = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                maintenanceList.add(mapResultSetToMaintenance(rs));
            }
            databaseService.putCache(cacheKey, maintenanceList);
        } catch (SQLException e) {
            System.err.println("Error fetching all maintenance records: " + e.getMessage());
        }

        return maintenanceList;
    }

    public Optional<Maintenance> findById(Integer id) {
        if (id == null || id <= 0) return Optional.empty();

        String sql = "SELECT id_maintenance, id_app AS id_apartment, etat_app AS general_condition, " +
            "etat_plomberie AS plumbing_condition, etat_electricite AS electrical_condition, " +
            "etat_chauffage AS heating_condition, date_derniere_maintenance AS last_maintenance_date, " +
            "description_maint AS description, recommendation_ia AS ai_recommendation " +
            "FROM maintenance WHERE id_maintenance = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMaintenance(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching maintenance by id: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<Maintenance> findByApartmentId(Integer apartmentId) {
        String cacheKey = "maintenance_apartment_" + apartmentId;
        List<Maintenance> cached = databaseService.getCache(cacheKey);
        if (cached != null) return cached;

        String sql = "SELECT id_maintenance, id_app AS id_apartment, etat_app AS general_condition, " +
            "etat_plomberie AS plumbing_condition, etat_electricite AS electrical_condition, " +
            "etat_chauffage AS heating_condition, date_derniere_maintenance AS last_maintenance_date, " +
            "description_maint AS description, recommendation_ia AS ai_recommendation " +
            "FROM maintenance WHERE id_app = ? ORDER BY date_derniere_maintenance DESC";
        List<Maintenance> maintenanceList = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    maintenanceList.add(mapResultSetToMaintenance(rs));
                }
            }
            databaseService.putCache(cacheKey, maintenanceList);
        } catch (SQLException e) {
            System.err.println("Error fetching maintenance by apartment: " + e.getMessage());
        }

        return maintenanceList;
    }

    public Integer insert(Maintenance maintenance) {
        String sql = "INSERT INTO maintenance (id_app, etat_app, etat_plomberie, " +
            "etat_electricite, etat_chauffage, date_derniere_maintenance, description_maint, recommendation_ia) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, maintenance.getIdApartment());
            stmt.setString(2, maintenance.getGeneralCondition());
            stmt.setString(3, maintenance.getPlumbingCondition());
            stmt.setString(4, maintenance.getElectricalCondition());
            stmt.setString(5, maintenance.getHeatingCondition());
            stmt.setString(6, maintenance.getLastMaintenanceDate());
            stmt.setString(7, maintenance.getDescription());
            stmt.setString(8, maintenance.getAiRecommendation());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        databaseService.clearCache("maintenance_all");
                        databaseService.clearCache("maintenance_apartment_" + maintenance.getIdApartment());
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting maintenance record: " + e.getMessage());
        }

        return -1;
    }

    public boolean update(Integer id, String generalCondition, String plumbingCondition, String electricalCondition,
                         String heatingCondition, String lastMaintenanceDate, String description, String aiRecommendation) {
        String sql = "UPDATE maintenance SET etat_app = ?, etat_plomberie = ?, " +
            "etat_electricite = ?, etat_chauffage = ?, date_derniere_maintenance = ?, " +
            "description_maint = ?, recommendation_ia = ? WHERE id_maintenance = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, generalCondition);
            stmt.setString(2, plumbingCondition);
            stmt.setString(3, electricalCondition);
            stmt.setString(4, heatingCondition);
            stmt.setString(5, lastMaintenanceDate);
            stmt.setString(6, description);
            stmt.setString(7, aiRecommendation);
            stmt.setInt(8, id);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                databaseService.clearCache("maintenance_all");
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error updating maintenance record: " + e.getMessage());
        }

        return false;
    }

    public boolean deleteByApartmentId(Integer apartmentId) {
        String sql = "DELETE FROM maintenance WHERE id_app = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apartmentId);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                databaseService.clearCache("maintenance_all");
                databaseService.clearCache("maintenance_apartment_" + apartmentId);
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error deleting maintenance by apartment: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM maintenance WHERE id_maintenance = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                databaseService.clearCache("maintenance_all");
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error deleting maintenance record: " + e.getMessage());
        }

        return false;
    }

    private Maintenance mapResultSetToMaintenance(ResultSet rs) throws SQLException {
        return new Maintenance(
            rs.getInt("id_apartment"),
            rs.getString("general_condition"),
            rs.getString("plumbing_condition"),
            rs.getString("electrical_condition"),
            rs.getString("heating_condition"),
            rs.getString("last_maintenance_date"),
            rs.getString("description"),
            rs.getString("ai_recommendation"),
            rs.getInt("id_maintenance")
        );
    }
}
