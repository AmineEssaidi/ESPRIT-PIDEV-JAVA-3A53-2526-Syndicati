package com.syndicati.models.repositories;

import com.syndicati.models.entities.Evenement;
import com.syndicati.models.entities.User;
import com.syndicati.services.DatabaseService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC repository for Evenement CRUD.
 */
public class EvenementRepository {

    private final DatabaseService databaseService;

    public EvenementRepository() {
        this.databaseService = DatabaseService.getInstance();
    }

    public List<Evenement> findAll() {
        String sql = "SELECT e.*, u.first_name, u.last_name, u.email_user FROM evenement e " +
                     "LEFT JOIN user u ON e.user_id = u.id_user ORDER BY e.date_event DESC";
        List<Evenement> events = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return events;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("EvenementRepository.findAll error: " + e.getMessage());
        }

        return events;
    }

    public Optional<Evenement> findById(int idEvent) {
        String sql = "SELECT e.*, u.first_name, u.last_name, u.email_user FROM evenement e " +
                     "LEFT JOIN user u ON e.user_id = u.id_user WHERE e.id_event = ?";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return Optional.empty();

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEvent);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("EvenementRepository.findById error: " + e.getMessage());
        }

        return Optional.empty();
    }

    public int create(Evenement event) {
        String sql = "INSERT INTO evenement (titre_event, description_event, date_event, lieu_event, nb_places, nb_restants, statut_event, image_event, type_event, created_at, edited_at, user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                System.err.println("❌ Database Connection Failed! Cannot save to MySQL.");
                return -1;
            }
            
            System.out.println("--- 🛰️ Database Sync: Connecting to " + databaseService.getDbUrl() + " ---");

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, event.getTitreEvent());
                ps.setString(2, event.getDescriptionEvent());
                
                LocalDateTime evDate = event.getDateEvent() != null ? event.getDateEvent() : LocalDateTime.now();
                ps.setTimestamp(3, Timestamp.valueOf(evDate));
                
                ps.setString(4, (event.getLieuEvent() != null && !event.getLieuEvent().isEmpty()) ? event.getLieuEvent() : "Tunis");
                ps.setObject(5, event.getNbPlaces(), Types.INTEGER);
                ps.setObject(6, event.getNbRestants(), Types.INTEGER);
                ps.setString(7, (event.getStatutEvent() != null) ? event.getStatutEvent() : "planifie");
                ps.setString(8, (event.getImageEvent() != null && !event.getImageEvent().isEmpty()) ? event.getImageEvent() : "default_event.png");
                ps.setString(9, (event.getTypeEvent() != null) ? event.getTypeEvent() : "social");
                ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now())); // created_at
                ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now())); // edited_at
                
                int userId = (event.getUser() != null) ? event.getUser().getIdUser() : 1;
                ps.setInt(12, userId);
                
                int affected = ps.executeUpdate();
                if (affected == 0) return -1;

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int newId = keys.getInt(1);
                        System.out.println("✅ Data inserted into MySQL successfully! ID: " + newId);
                        return newId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Critical Database Error during INSERT: " + e.getMessage());
        }

        return -1;
    }

    public boolean update(Evenement event) {
        String sql = "UPDATE evenement SET titre_event = ?, description_event = ?, date_event = ?, lieu_event = ?, nb_places = ?, nb_restants = ?, statut_event = ?, image_event = ?, type_event = ?, edited_at = ?, user_id = ? " +
                     "WHERE id_event = ?";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return false;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, event.getTitreEvent());
                ps.setString(2, event.getDescriptionEvent());
                ps.setTimestamp(3, Timestamp.valueOf(event.getDateEvent()));
                ps.setString(4, event.getLieuEvent());
                ps.setObject(5, event.getNbPlaces(), Types.INTEGER);
                ps.setObject(6, event.getNbRestants(), Types.INTEGER);
                ps.setString(7, event.getStatutEvent());
                ps.setString(8, event.getImageEvent());
                ps.setString(9, event.getTypeEvent());
                ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now())); // edited_at
                
                int userId = (event.getUser() != null) ? event.getUser().getIdUser() : 1;
                ps.setInt(11, userId);
                ps.setInt(12, event.getIdEvent());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("EvenementRepository.update error: " + e.getMessage());
        }

        return false;
    }

    public boolean deleteById(int idEvent) {
        String sql = "DELETE FROM evenement WHERE id_event = ?";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return false;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idEvent);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("EvenementRepository.deleteById error: " + e.getMessage());
        }

        return false;
    }


    public boolean decrementRestants(int idEvent, int count) {
        String sql = "UPDATE evenement SET nb_restants = nb_restants - ? WHERE id_event = ? AND nb_restants >= ?";
        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, count);
                ps.setInt(2, idEvent);
                ps.setInt(3, count);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("EvenementRepository.decrementRestants error: " + e.getMessage());
        }
        return false;
    }

    public boolean incrementRestants(int idEvent, int count) {
        String sql = "UPDATE evenement SET nb_restants = nb_restants + ? WHERE id_event = ? AND nb_restants + ? <= nb_places";
        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, count);
                ps.setInt(2, idEvent);
                ps.setInt(3, count);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("EvenementRepository.incrementRestants error: " + e.getMessage());
        }
        return false;
    }

    private Evenement mapRow(ResultSet rs) throws SQLException {
        Evenement e = new Evenement();
        e.setIdEvent(rs.getInt("id_event"));
        e.setTitreEvent(rs.getString("titre_event"));
        e.setDescriptionEvent(rs.getString("description_event"));
        e.setDateEvent(rs.getTimestamp("date_event").toLocalDateTime());
        e.setLieuEvent(rs.getString("lieu_event"));
        e.setNbPlaces(rs.getInt("nb_places"));
        e.setNbRestants(rs.getInt("nb_restants"));
        e.setStatutEvent(rs.getString("statut_event"));
        e.setImageEvent(rs.getString("image_event"));
        e.setTypeEvent(rs.getString("type_event"));
        e.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        e.setEditedAt(rs.getTimestamp("edited_at").toLocalDateTime());

        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            User u = new User();
            u.setIdUser(userId);
            u.setFirstName(rs.getString("first_name"));
            u.setLastName(rs.getString("last_name"));
            u.setEmailUser(rs.getString("email_user"));
            e.setUser(u);
        }

        return e;
    }
}
