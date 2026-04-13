package com.syndicati.models.repositories;

import com.syndicati.models.entities.Evenement;
import com.syndicati.models.entities.Participation;
import com.syndicati.models.entities.User;
import com.syndicati.services.DatabaseService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC repository for Participation CRUD.
 */
public class ParticipationRepository {

    private final DatabaseService databaseService;

    public ParticipationRepository() {
        this.databaseService = DatabaseService.getInstance();
    }

    public List<Participation> findAllByEventId(int eventId) {
        String sql = "SELECT p.*, u.first_name, u.last_name, u.email_user FROM participation p " +
                     "JOIN user u ON p.user_id = u.id_user WHERE p.event_id = ?";
        List<Participation> participations = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return participations;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        participations.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("ParticipationRepository.findAllByEventId error: " + e.getMessage());
        }

        return participations;
    }

    public List<Participation> findAllByUserId(int userId) {
        String sql = "SELECT p.*, e.titre_event, e.date_event, e.lieu_event FROM participation p " +
                     "JOIN evenement e ON p.event_id = e.id_event WHERE p.user_id = ?";
        List<Participation> participations = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return participations;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        participations.add(mapRowMinimal(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("ParticipationRepository.findAllByUserId error: " + e.getMessage());
        }

        return participations;
    }

    public int create(Participation p) {
        String sql = "INSERT INTO participation (event_id, user_id, date_participation, statut_participation, nb_accompagnants, commentaire_participation, formulaire_data, created_at, edited_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return -1;

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, p.getEvenement().getIdEvent());
                ps.setInt(2, p.getUser().getIdUser());
                ps.setTimestamp(3, Timestamp.valueOf(p.getDateParticipation() != null ? p.getDateParticipation() : LocalDateTime.now()));
                ps.setString(4, p.getStatutParticipation());
                ps.setInt(5, p.getNbAccompagnants());
                ps.setString(6, p.getCommentaireParticipation());
                ps.setString(7, p.getFormulaireData());
                ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

                int affected = ps.executeUpdate();
                if (affected == 0) return -1;

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("ParticipationRepository.create error: " + e.getMessage());
        }

        return -1;
    }

    public boolean updateStatus(int idParticipation, String status) {
        String sql = "UPDATE participation SET statut_participation = ?, edited_at = ? WHERE id_participation = ?";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return false;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, idParticipation);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("ParticipationRepository.updateStatus error: " + e.getMessage());
        }

        return false;
    }

    public Optional<Participation> findById(int idParticipation) {
        String sql = "SELECT p.*, u.first_name, u.last_name, u.email_user, e.titre_event, e.date_event, e.lieu_event " +
                     "FROM participation p " +
                     "JOIN user u ON p.user_id = u.id_user " +
                     "JOIN evenement e ON p.event_id = e.id_event " +
                     "WHERE p.id_participation = ?";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return Optional.empty();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idParticipation);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRowWithDetails(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("ParticipationRepository.findById error: " + e.getMessage());
        }

        return Optional.empty();
    }

    public List<Participation> findAll() {
        String sql = "SELECT p.*, u.first_name, u.last_name, u.email_user, e.titre_event, e.date_event, e.lieu_event " +
                     "FROM participation p " +
                     "JOIN user u ON p.user_id = u.id_user " +
                     "JOIN evenement e ON p.event_id = e.id_event " +
                     "ORDER BY p.created_at DESC";
        List<Participation> participations = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return participations;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        participations.add(mapRowWithDetails(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("ParticipationRepository.findAll error: " + e.getMessage());
        }

        return participations;
    }

    public boolean update(Participation p) {
        String sql = "UPDATE participation SET nb_accompagnants = ?, commentaire_participation = ?, formulaire_data = ?, edited_at = ? WHERE id_participation = ?";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return false;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, p.getNbAccompagnants());
                ps.setString(2, p.getCommentaireParticipation());
                ps.setString(3, p.getFormulaireData());
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(5, p.getIdParticipation());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("ParticipationRepository.update error: " + e.getMessage());
        }

        return false;
    }

    public boolean delete(int idParticipation) {
        String sql = "DELETE FROM participation WHERE id_participation = ?";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return false;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idParticipation);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("ParticipationRepository.delete error: " + e.getMessage());
        }

        return false;
    }

    private Participation mapRow(ResultSet rs) throws SQLException {
        Participation p = new Participation();
        p.setIdParticipation(rs.getInt("id_participation"));
        p.setStatutParticipation(rs.getString("statut_participation"));
        p.setNbAccompagnants(rs.getInt("nb_accompagnants"));
        p.setCommentaireParticipation(rs.getString("commentaire_participation"));
        p.setFormulaireData(rs.getString("formulaire_data"));
        p.setDateParticipation(rs.getTimestamp("date_participation").toLocalDateTime());
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        p.setEditedAt(rs.getTimestamp("edited_at").toLocalDateTime());

        User u = new User();
        u.setIdUser(rs.getInt("user_id"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setEmailUser(rs.getString("email_user"));
        p.setUser(u);

        Evenement e = new Evenement();
        e.setIdEvent(rs.getInt("event_id"));
        p.setEvenement(e);

        return p;
    }

    private Participation mapRowWithDetails(ResultSet rs) throws SQLException {
        Participation p = mapRow(rs);
        
        Evenement e = p.getEvenement();
        e.setTitreEvent(rs.getString("titre_event"));
        e.setLieuEvent(rs.getString("lieu_event"));
        e.setDateEvent(rs.getTimestamp("date_event").toLocalDateTime());
        
        return p;
    }

    private Participation mapRowMinimal(ResultSet rs) throws SQLException {
        Participation p = new Participation();
        p.setIdParticipation(rs.getInt("id_participation"));
        p.setStatutParticipation(rs.getString("statut_participation"));
        p.setDateParticipation(rs.getTimestamp("date_participation").toLocalDateTime());

        Evenement e = new Evenement();
        e.setIdEvent(rs.getInt("event_id"));
        e.setTitreEvent(rs.getString("titre_event"));
        e.setLieuEvent(rs.getString("lieu_event"));
        e.setDateEvent(rs.getTimestamp("date_event").toLocalDateTime());
        p.setEvenement(e);

        return p;
    }
}
