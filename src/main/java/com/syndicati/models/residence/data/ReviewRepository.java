package com.syndicati.models.residence.data;

import com.syndicati.models.residence.Review;
import com.syndicati.services.DatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Review data access layer.
 */
public class ReviewRepository {

    private final DatabaseService databaseService;

    public ReviewRepository() {
        this.databaseService = DatabaseService.getInstance();
    }

    public List<Review> findAll() {
        String cacheKey = "reviews_all";
        List<Review> cached = databaseService.getCache(cacheKey);
        if (cached != null) return cached;

        String sql = "SELECT id_revue AS id_review, id_utilisateur AS id_user, " +
                "id_appartement AS id_apartment, score FROM review ORDER BY id_appartement ASC";
        List<Review> reviews = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }
            databaseService.putCache(cacheKey, reviews);
        } catch (SQLException e) {
            System.err.println("Error fetching all reviews: " + e.getMessage());
        }

        return reviews;
    }

    public Optional<Review> findById(Integer id) {
        if (id == null || id <= 0) return Optional.empty();

        String sql = "SELECT id_revue AS id_review, id_utilisateur AS id_user, " +
                "id_appartement AS id_apartment, score FROM review WHERE id_revue = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReview(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching review by id: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<Review> findByApartmentId(Integer apartmentId) {
        String cacheKey = "reviews_apartment_" + apartmentId;
        List<Review> cached = databaseService.getCache(cacheKey);
        if (cached != null) return cached;

        String sql = "SELECT id_revue AS id_review, id_utilisateur AS id_user, " +
                "id_appartement AS id_apartment, score FROM review WHERE id_appartement = ? " +
                "ORDER BY id_revue DESC";
        List<Review> reviews = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapResultSetToReview(rs));
                }
            }
            databaseService.putCache(cacheKey, reviews);
        } catch (SQLException e) {
            System.err.println("Error fetching reviews by apartment: " + e.getMessage());
        }

        return reviews;
    }

    public List<Review> findByUserId(Integer userId) {
        String cacheKey = "reviews_user_" + userId;
        List<Review> cached = databaseService.getCache(cacheKey);
        if (cached != null) return cached;

        String sql = "SELECT id_revue AS id_review, id_utilisateur AS id_user, " +
                "id_appartement AS id_apartment, score FROM review WHERE id_utilisateur = ? " +
                "ORDER BY id_revue DESC";
        List<Review> reviews = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapResultSetToReview(rs));
                }
            }
            databaseService.putCache(cacheKey, reviews);
        } catch (SQLException e) {
            System.err.println("Error fetching reviews by user: " + e.getMessage());
        }

        return reviews;
    }

    public Optional<Review> findByUserAndApartment(Integer userId, Integer apartmentId) {
        String sql = "SELECT id_revue AS id_review, id_utilisateur AS id_user, " +
                "id_appartement AS id_apartment, score FROM review " +
                "WHERE id_utilisateur = ? AND id_appartement = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReview(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching review by user and apartment: " + e.getMessage());
        }

        return Optional.empty();
    }

    public Integer insert(Review review) {
        String sql = "INSERT INTO review (id_utilisateur, id_appartement, score) VALUES (?, ?, ?)";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, review.getIdUser());
            stmt.setInt(2, review.getIdApartment());
            stmt.setInt(3, review.getScore());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        databaseService.clearCache("reviews_all");
                        databaseService.clearCache("reviews_apartment_" + review.getIdApartment());
                        databaseService.clearCache("reviews_user_" + review.getIdUser());
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting review: " + e.getMessage());
        }

        return -1;
    }

    public boolean update(Integer id, Integer score) {
        String sql = "UPDATE review SET score = ? WHERE id_revue = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, score);
            stmt.setInt(2, id);

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                databaseService.clearCache("reviews_all");
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error updating review: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM review WHERE id_revue = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                databaseService.clearCache("reviews_all");
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error deleting review: " + e.getMessage());
        }

        return false;
    }

    public Double getAverageScoreForApartment(Integer apartmentId) {
        String sql = "SELECT AVG(score) as avg_score FROM review WHERE id_appartement = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, apartmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Object avgScore = rs.getObject("avg_score");
                    if (avgScore != null) {
                        return rs.getDouble("avg_score");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average score: " + e.getMessage());
        }

        return 0.0;
    }

    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        return new Review(
            rs.getInt("id_user"),
            rs.getInt("id_apartment"),
            rs.getInt("score"),
            rs.getInt("id_review")
        );
    }
}
