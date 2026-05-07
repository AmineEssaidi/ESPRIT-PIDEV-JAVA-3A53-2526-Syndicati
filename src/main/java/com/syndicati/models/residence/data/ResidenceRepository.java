package com.syndicati.models.residence.data;

import com.syndicati.models.residence.Residence;
import com.syndicati.services.DatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Residence data access layer.
 */
public class ResidenceRepository {

    private final DatabaseService databaseService;

    public ResidenceRepository() {
        this.databaseService = DatabaseService.getInstance();
    }

    public List<Residence> findAll() {
        String cacheKey = "residences_all";
        List<Residence> cached = databaseService.getCache(cacheKey);
        if (cached != null) return cached;

        String sql = "SELECT id_residence, nom_r AS name_residence, adresse AS address_residence, " +
            "image_r AS image_residence, date_ajout AS date_added, " +
            "n_appartements AS number_apartments, n_etages AS number_floors, n_blocs AS number_blocks " +
            "FROM residence ORDER BY nom_r ASC";
        List<Residence> residences = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                residences.add(mapResultSetToResidence(rs));
            }
            databaseService.putCache(cacheKey, residences);
        } catch (SQLException e) {
            System.err.println("Error fetching all residences: " + e.getMessage());
        }

        return residences;
    }

    public Optional<Residence> findById(Integer id) {
        if (id == null || id <= 0) return Optional.empty();

        String sql = "SELECT id_residence, nom_r AS name_residence, adresse AS address_residence, " +
            "image_r AS image_residence, date_ajout AS date_added, " +
            "n_appartements AS number_apartments, n_etages AS number_floors, n_blocs AS number_blocks " +
            "FROM residence WHERE id_residence = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToResidence(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching residence by id: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<Residence> findByNameContains(String name) {
        String cacheKey = "residences_name_" + name;
        List<Residence> cached = databaseService.getCache(cacheKey);
        if (cached != null) return cached;

        String sql = "SELECT id_residence, nom_r AS name_residence, adresse AS address_residence, " +
            "image_r AS image_residence, date_ajout AS date_added, " +
            "n_appartements AS number_apartments, n_etages AS number_floors, n_blocs AS number_blocks " +
            "FROM residence WHERE nom_r LIKE ? ORDER BY nom_r ASC";
        List<Residence> residences = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    residences.add(mapResultSetToResidence(rs));
                }
            }
            databaseService.putCache(cacheKey, residences);
        } catch (SQLException e) {
            System.err.println("Error searching residences by name: " + e.getMessage());
        }

        return residences;
    }

    public Integer insert(Residence residence) {
        String sql = "INSERT INTO residence (nom_r, adresse, image_r, date_ajout, " +
            "n_appartements, n_etages, n_blocs) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, residence.getNameResidence());
            stmt.setString(2, residence.getAddressResidence());
            stmt.setString(3, residence.getImageResidence());
            stmt.setString(4, residence.getDateAdded());
            stmt.setObject(5, residence.getNumberApartments());
            stmt.setObject(6, residence.getNumberFloors());
            stmt.setString(7, residence.getNumberBlocks());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        databaseService.clearCache("residences_all");
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting residence: " + e.getMessage());
        }

        return -1;
    }

    public boolean update(Integer id, String name, String address, String image, Integer numberApartments,
                         Integer numberFloors, String numberBlocks) {
        String sql = "UPDATE residence SET nom_r = ?, adresse = ?, image_r = ?, " +
            "n_appartements = ?, n_etages = ?, n_blocs = ? WHERE id_residence = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, address);
            stmt.setString(3, image);
            stmt.setObject(4, numberApartments);
            stmt.setObject(5, numberFloors);
            stmt.setString(6, numberBlocks);
            stmt.setInt(7, id);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                databaseService.clearCache("residences_all");
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error updating residence: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM residence WHERE id_residence = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                databaseService.clearCache("residences_all");
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error deleting residence: " + e.getMessage());
        }

        return false;
    }

    private Residence mapResultSetToResidence(ResultSet rs) throws SQLException {
        return new Residence(
            rs.getString("name_residence"),
            rs.getString("address_residence"),
            rs.getString("image_residence"),
            rs.getString("date_added"),
            rs.getInt("number_apartments"),
            rs.getInt("number_floors"),
            rs.getString("number_blocks"),
            rs.getInt("id_residence")
        );
    }
}
