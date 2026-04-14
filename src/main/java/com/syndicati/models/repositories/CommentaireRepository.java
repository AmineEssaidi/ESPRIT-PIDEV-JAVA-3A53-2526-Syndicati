package com.syndicati.models.repositories;

import com.syndicati.models.entities.Commentaire;
import com.syndicati.services.DatabaseService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository for comments.
 */
public class CommentaireRepository {

    private final DatabaseService databaseService;

    public CommentaireRepository() {
        this.databaseService = DatabaseService.getInstance();
    }

    public List<Commentaire> findByPublicationId(int pubId) {
        String sql = "SELECT c.*, " +
                     "u.first_name AS author_fname, " +
                     "u.last_name AS author_lname, " +
                     "pr.avatar AS author_av " +
                     "FROM commentaire c " +
                     "LEFT JOIN user u ON c.id_user = u.id_user " +
                     "LEFT JOIN profile pr ON u.id_user = pr.user_id " +
                     "WHERE c.id_pub = ? AND c.visibility = 1 " +
                     "ORDER BY c.created_at ASC";
        
        List<Commentaire> comments = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return comments;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, pubId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        comments.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("CommentaireRepository.findByPublicationId error: " + e.getMessage());
        }

        return comments;
    }

    public int create(Commentaire comment) {
        String sql = "INSERT INTO commentaire (description_commentaire, image_commentaire, created_at, updated_at, visibility, id_pub, id_user) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return -1;

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, comment.getDescriptionCommentaire());
                ps.setString(2, comment.getImageCommentaire());
                ps.setTimestamp(3, Timestamp.valueOf(comment.getCreatedAt()));
                ps.setTimestamp(4, Timestamp.valueOf(comment.getUpdatedAt()));
                ps.setInt(5, comment.getVisibility());
                ps.setInt(6, comment.getIdPub());
                ps.setInt(7, comment.getIdUser());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            return keys.getInt(1);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("CommentaireRepository.create error: " + e.getMessage());
        }
        return -1;
    }

    private Commentaire mapRow(ResultSet rs) throws SQLException {
        Commentaire c = new Commentaire();
        c.setIdCommentaire(rs.getInt("id_commentaire"));
        c.setDescriptionCommentaire(rs.getString("description_commentaire"));
        c.setImageCommentaire(rs.getString("image_commentaire"));
        c.setCreatedAt(readLocalDateTime(rs, "created_at"));
        c.setUpdatedAt(readLocalDateTime(rs, "updated_at"));
        c.setVisibility(rs.getInt("visibility"));
        c.setIdPub(rs.getInt("id_pub"));
        c.setIdUser(rs.getInt("id_user"));

        // Author meta
        try {
            c.setAuthorFirstName(rs.getString("author_fname"));
            c.setAuthorLastName(rs.getString("author_lname"));
            c.setAuthorAvatar(rs.getString("author_av"));
        } catch (SQLException ignored) {}

        return c;
    }

    private LocalDateTime readLocalDateTime(ResultSet rs, String col) throws SQLException {
        Timestamp ts = rs.getTimestamp(col);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}
