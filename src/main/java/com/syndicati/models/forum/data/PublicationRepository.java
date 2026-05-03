package com.syndicati.models.forum.data;

import com.syndicati.models.forum.Publication;
import com.syndicati.models.user.User;
import com.syndicati.models.user.data.UserRepository;
import com.syndicati.services.DatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC repository for forum publications.
 */
public class PublicationRepository {

    private final DatabaseService databaseService;
    private final UserRepository userRepository;

    public PublicationRepository() {
        this.databaseService = DatabaseService.getInstance();
        this.userRepository = new UserRepository();
    }

    public List<Publication> findAll() {
        return findAllWithLimit(50);
    }

    public List<Publication> findAllWithLimit(int limit) {
        String sql = """
            SELECT p.*, u.first_name, u.last_name, u.email_user, u.role_user 
            FROM publication p 
            LEFT JOIN user u ON p.user_id = u.id_user 
            ORDER BY p.date_creation_pub DESC LIMIT ?
            """;
        List<Publication> publications = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return publications;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Math.max(1, limit));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        publications.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("PublicationRepository.findAll error: " + e.getMessage());
        }

        return publications;
    }

    public Optional<Publication> findById(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        String sql = """
            SELECT p.*, u.first_name, u.last_name, u.email_user, u.role_user 
            FROM publication p 
            LEFT JOIN user u ON p.user_id = u.id_user 
            WHERE p.id = ?
            """;

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return Optional.empty();
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("PublicationRepository.findById error: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<Publication> findByUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            return new ArrayList<>();
        }

        return findByUserIdWithLimit(userId, 30);
    }

    public List<Publication> findByUserIdWithLimit(Integer userId, int limit) {
        if (userId == null || userId <= 0) {
            return new ArrayList<>();
        }

        String sql = """
            SELECT p.*, u.first_name, u.last_name, u.email_user, u.role_user 
            FROM publication p 
            LEFT JOIN user u ON p.user_id = u.id_user 
            WHERE p.user_id = ? 
            ORDER BY p.date_creation_pub DESC LIMIT ?
            """;
        List<Publication> publications = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return publications;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, Math.max(1, limit));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        publications.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("PublicationRepository.findByUserId error: " + e.getMessage());
        }

        return publications;
    }

    public List<Publication> findByCategory(String category) {
        String cacheKey = "forum_cat_" + (category == null ? "all" : category);
        List<Publication> cached = databaseService.getCache(cacheKey);
        if (cached != null) return cached;

        List<Publication> publications = new ArrayList<>();
        String sql;

        if (category == null || category.isBlank()) {
            return findAll();
        }

        if ("General".equals(category)) {
            sql = """
                SELECT p.*, u.first_name, u.last_name, u.email_user, u.role_user 
                FROM publication p 
                LEFT JOIN user u ON p.user_id = u.id_user 
                WHERE p.categorie_pub <> ? 
                ORDER BY p.date_creation_pub DESC
                """;
        } else {
            sql = """
                SELECT p.*, u.first_name, u.last_name, u.email_user, u.role_user 
                FROM publication p 
                LEFT JOIN user u ON p.user_id = u.id_user 
                WHERE p.categorie_pub = ? 
                ORDER BY p.date_creation_pub DESC
                """;
        }

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return publications;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "General".equals(category) ? "Announcement" : category);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        publications.add(mapRow(rs));
                    }
                }
            }
            databaseService.putCache(cacheKey, publications);
        } catch (SQLException e) {
            System.out.println("PublicationRepository.findByCategory error: " + e.getMessage());
        }

        return publications;
    }

    public Integer create(Publication publication) {
        if (publication == null || publication.getUser() == null) {
            return -1;
        }

        String sql = "INSERT INTO publication (titre_pub, description_pub, date_creation_pub, categorie_pub, image_pub, user_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return -1;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, publication.getTitrePub());
                ps.setString(2, publication.getDescriptionPub());
                LocalDateTime now = publication.getDateCreationPub() != null ? publication.getDateCreationPub() : LocalDateTime.now();
                ps.setTimestamp(3, Timestamp.valueOf(now));
                ps.setString(4, publication.getCategoriePub());
                ps.setString(5, publication.getImagePub());
                ps.setInt(6, publication.getUser().getIdUser());

                ps.executeUpdate();

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        publication.setIdPublication(id);
                        publication.setDateCreationPub(now);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("PublicationRepository.create error: " + e.getMessage());
        }

        return -1;
    }

    public boolean update(Publication publication) {
        if (publication == null || publication.getIdPublication() == null || publication.getIdPublication() <= 0) {
            return false;
        }

        String sql = "UPDATE publication SET titre_pub = ?, description_pub = ?, date_creation_pub = ?, categorie_pub = ?, image_pub = ?, user_id = ? WHERE id = ?";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, publication.getTitrePub());
                ps.setString(2, publication.getDescriptionPub());
                LocalDateTime date = publication.getDateCreationPub() != null ? publication.getDateCreationPub() : LocalDateTime.now();
                ps.setTimestamp(3, Timestamp.valueOf(date));
                ps.setString(4, publication.getCategoriePub());
                ps.setString(5, publication.getImagePub());
                ps.setInt(6, publication.getUser() != null ? publication.getUser().getIdUser() : 0);
                ps.setInt(7, publication.getIdPublication());

                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("PublicationRepository.update error: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }

        String sql = "DELETE FROM publication WHERE id = ?";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("PublicationRepository.delete error: " + e.getMessage());
        }

        return false;
    }

    public List<Publication> findAllLatest() {
        return findAll();
    }

    private Publication mapRow(ResultSet rs) throws SQLException {
        Publication publication = new Publication();
        publication.setIdPublication(rs.getInt("id"));
        publication.setTitrePub(rs.getString("titre_pub"));
        publication.setDescriptionPub(rs.getString("description_pub"));
        publication.setCategoriePub(rs.getString("categorie_pub"));
        publication.setImagePub(rs.getString("image_pub"));

        Timestamp creationTs = rs.getTimestamp("date_creation_pub");
        if (creationTs != null) {
            publication.setDateCreationPub(creationTs.toLocalDateTime());
        }

        // Optimized: Check if user data was joined in the query
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            try {
                // If the query included joined user columns, use them directly
                String firstName = rs.getString("first_name");
                if (firstName != null) {
                    User user = new User();
                    user.setIdUser(userId);
                    user.setFirstName(firstName);
                    user.setLastName(rs.getString("last_name"));
                    user.setEmailUser(rs.getString("email_user"));
                    user.setRoleUser(rs.getString("role_user"));
                    publication.setUser(user);
                } else {
                    // Fallback to separate query ONLY if join data is missing
                    userRepository.findById(userId).ifPresent(publication::setUser);
                }
            } catch (SQLException joinMissing) {
                // Fallback to separate query if columns weren't in result set
                userRepository.findById(userId).ifPresent(publication::setUser);
            }
        }

        return publication;
    }
}