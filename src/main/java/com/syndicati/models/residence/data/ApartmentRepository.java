package com.syndicati.models.residence.data;

import com.syndicati.models.residence.Apartment;
import com.syndicati.services.DatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Apartment data access layer.
 */
public class ApartmentRepository {

    private final DatabaseService databaseService;

    public ApartmentRepository() {
        this.databaseService = DatabaseService.getInstance();
    }

    public List<Apartment> findAll() {
        String cacheKey = "apartments_all";
        List<Apartment> cached = databaseService.getCache(cacheKey);
        if (cached != null) return cached;

        String sql = "SELECT id_app AS id_apartment, residence_id AS id_residence, id_user, parking, " +
            "disponible AS available, image_a AS image_apartment, type_a AS type_apartment, " +
            "appartement_info AS apartment_info, superficie AS area, prix_location AS rental_price, " +
            "prix_vente AS sale_price, date_construction AS date_constructed " +
            "FROM appartement ORDER BY residence_id ASC, type_a ASC";
        List<Apartment> apartments = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                apartments.add(mapResultSetToApartment(rs));
            }
            databaseService.putCache(cacheKey, apartments);
        } catch (SQLException e) {
            System.err.println("Error fetching all apartments: " + e.getMessage());
        }

        return apartments;
    }

    public Optional<Apartment> findById(Integer id) {
        if (id == null || id <= 0) return Optional.empty();

        String sql = "SELECT id_app AS id_apartment, residence_id AS id_residence, id_user, parking, " +
            "disponible AS available, image_a AS image_apartment, type_a AS type_apartment, " +
            "appartement_info AS apartment_info, superficie AS area, prix_location AS rental_price, " +
            "prix_vente AS sale_price, date_construction AS date_constructed " +
            "FROM appartement WHERE id_app = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToApartment(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching apartment by id: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<Apartment> findByResidenceId(Integer residenceId) {
        String cacheKey = "apartments_residence_" + residenceId;
        List<Apartment> cached = databaseService.getCache(cacheKey);
        if (cached != null) return cached;

        String sql = "SELECT id_app AS id_apartment, residence_id AS id_residence, id_user, parking, " +
            "disponible AS available, image_a AS image_apartment, type_a AS type_apartment, " +
            "appartement_info AS apartment_info, superficie AS area, prix_location AS rental_price, " +
            "prix_vente AS sale_price, date_construction AS date_constructed " +
            "FROM appartement WHERE residence_id = ? ORDER BY type_a ASC";
        List<Apartment> apartments = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, residenceId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    apartments.add(mapResultSetToApartment(rs));
                }
            }
            databaseService.putCache(cacheKey, apartments);
        } catch (SQLException e) {
            System.err.println("Error fetching apartments by residence: " + e.getMessage());
        }

        return apartments;
    }

    public List<Apartment> findByAvailable(Integer available) {
        String cacheKey = "apartments_available_" + available;
        List<Apartment> cached = databaseService.getCache(cacheKey);
        if (cached != null) return cached;

        String sql = "SELECT id_app AS id_apartment, residence_id AS id_residence, id_user, parking, " +
            "disponible AS available, image_a AS image_apartment, type_a AS type_apartment, " +
            "appartement_info AS apartment_info, superficie AS area, prix_location AS rental_price, " +
            "prix_vente AS sale_price, date_construction AS date_constructed " +
            "FROM appartement WHERE disponible = ?";
        List<Apartment> apartments = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, available);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    apartments.add(mapResultSetToApartment(rs));
                }
            }
            databaseService.putCache(cacheKey, apartments);
        } catch (SQLException e) {
            System.err.println("Error fetching available apartments: " + e.getMessage());
        }

        return apartments;
    }

    public Integer insert(Apartment apartment) {
        String sql = "INSERT INTO appartement (residence_id, id_user, parking, disponible, image_a, " +
            "type_a, appartement_info, superficie, prix_location, prix_vente, date_construction) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, apartment.getIdResidence());
            stmt.setObject(2, apartment.getIdUser());
            stmt.setInt(3, apartment.getParking());
            stmt.setInt(4, apartment.getAvailable());
            stmt.setString(5, apartment.getImageApartment());
            stmt.setString(6, apartment.getTypeApartment());
            stmt.setString(7, apartment.getApartmentInfo());
            stmt.setObject(8, apartment.getArea());
            stmt.setObject(9, apartment.getRentalPrice());
            stmt.setObject(10, apartment.getSalePrice());
            stmt.setString(11, apartment.getDateConstructed());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        databaseService.clearCache("apartments_all");
                        databaseService.clearCache("apartments_residence_" + apartment.getIdResidence());
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting apartment: " + e.getMessage());
        }

        return -1;
    }

    public boolean update(Integer id, Integer residenceId, Integer userId, Integer parking, Integer available,
                         String image, String type, String info, Integer area, Integer rentalPrice,
                         Integer salePrice, String dateConstructed) {
        String sql = "UPDATE appartement SET residence_id = ?, id_user = ?, parking = ?, disponible = ?, " +
            "image_a = ?, type_a = ?, appartement_info = ?, superficie = ?, prix_location = ?, " +
            "prix_vente = ?, date_construction = ? WHERE id_app = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, residenceId);
            stmt.setObject(2, userId);
            stmt.setInt(3, parking);
            stmt.setInt(4, available);
            stmt.setString(5, image);
            stmt.setString(6, type);
            stmt.setString(7, info);
            stmt.setObject(8, area);
            stmt.setObject(9, rentalPrice);
            stmt.setObject(10, salePrice);
            stmt.setString(11, dateConstructed);
            stmt.setInt(12, id);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                databaseService.clearCache("apartments_all");
                databaseService.clearCache("apartments_residence_" + residenceId);
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error updating apartment: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM appartement WHERE id_app = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                databaseService.clearCache("apartments_all");
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error deleting apartment: " + e.getMessage());
        }

        return false;
    }

    private Apartment mapResultSetToApartment(ResultSet rs) throws SQLException {
        return new Apartment(
            rs.getInt("id_residence"),
            rs.getObject("id_user") != null ? rs.getInt("id_user") : null,
            rs.getInt("parking"),
            rs.getInt("available"),
            rs.getString("image_apartment"),
            rs.getString("type_apartment"),
            rs.getString("apartment_info"),
            rs.getObject("area") != null ? rs.getInt("area") : null,
            rs.getObject("rental_price") != null ? rs.getInt("rental_price") : null,
            rs.getObject("sale_price") != null ? rs.getInt("sale_price") : null,
            rs.getString("date_constructed"),
            rs.getInt("id_apartment")
        );
    }
}
