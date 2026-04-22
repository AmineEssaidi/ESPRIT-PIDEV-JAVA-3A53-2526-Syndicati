package com.syndicati.models.log.data;

import com.syndicati.models.log.AppEventLog;
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

public class AppEventLogRepository {

    private final DatabaseService databaseService;
    private final UserRepository userRepository;

    public AppEventLogRepository() {
        this.databaseService = DatabaseService.getInstance();
        this.userRepository = new UserRepository();
    }

    public int create(AppEventLog log) {
        if (log == null || log.getEventType() == null || log.getEventType().isBlank()) {
            return -1;
        }

        String sql = "INSERT INTO app_event_log (user_id, event_type, entity_type, entity_id, metadata, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return -1;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                if (log.getUser() != null && log.getUser().getIdUser() != null) {
                    ps.setInt(1, log.getUser().getIdUser());
                } else {
                    ps.setNull(1, java.sql.Types.INTEGER);
                }
                ps.setString(2, log.getEventType());
                ps.setString(3, log.getEntityType());
                if (log.getEntityId() != null) {
                    ps.setInt(4, log.getEntityId());
                } else {
                    ps.setNull(4, java.sql.Types.INTEGER);
                }
                ps.setString(5, log.getMetadataJson());
                LocalDateTime createdAt = log.getCreatedAt() != null ? log.getCreatedAt() : LocalDateTime.now();
                ps.setTimestamp(6, Timestamp.valueOf(createdAt));

                int affected = ps.executeUpdate();
                if (affected == 0) {
                    return -1;
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        log.setId((long) id);
                        log.setCreatedAt(createdAt);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.create error: " + e.getMessage());
        }

        return -1;
    }

    public List<AppEventLog> findLatest(int limit) {
        String sql = "SELECT * FROM app_event_log ORDER BY created_at DESC LIMIT ?";
        List<AppEventLog> logs = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return logs;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Math.max(1, limit));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        logs.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.findLatest error: " + e.getMessage());
        }

        return logs;
    }

    public List<AppEventLog> findLatestByUser(int userId, int limit) {
        String sql = "SELECT * FROM app_event_log WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        List<AppEventLog> logs = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return logs;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setInt(2, Math.max(1, limit));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        logs.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.findLatestByUser error: " + e.getMessage());
        }

        return logs;
    }

    public List<AppEventLog> findByEntityType(String entityType, int entityId) {
        String sql = "SELECT * FROM app_event_log WHERE entity_type = ? AND entity_id = ? ORDER BY created_at DESC";
        List<AppEventLog> logs = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return logs;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, entityType);
                ps.setInt(2, entityId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        logs.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.findByEntityType error: " + e.getMessage());
        }

        return logs;
    }

    public int countSince(LocalDateTime since) {
        String sql = "SELECT COUNT(*) FROM app_event_log WHERE created_at >= ?";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return 0;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(since));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.countSince error: " + e.getMessage());
        }

        return 0;
    }

    public int countByEventTypeSince(String eventType, LocalDateTime since) {
        String sql = "SELECT COUNT(*) FROM app_event_log WHERE event_type = ? AND created_at >= ?";

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return 0;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, eventType);
                ps.setTimestamp(2, Timestamp.valueOf(since));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.countByEventTypeSince error: " + e.getMessage());
        }

        return 0;
    }

    public int countDistinctActiveSince(LocalDateTime since) {
        String sql = """
            SELECT COUNT(DISTINCT COALESCE(CAST(user_id AS CHAR), JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.host'))))
            FROM app_event_log
            WHERE created_at >= ?
            """;

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return 0;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(since));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.countDistinctActiveSince error: " + e.getMessage());
        }

        return 0;
    }

    public List<String[]> fetchInteractionTrends(LocalDateTime since) {
        String sql = """
            SELECT DATE(created_at) AS log_date,
                   SUM(CASE WHEN event_type = 'PAGE_VIEW' THEN 1 ELSE 0 END) AS views,
                   SUM(CASE WHEN event_type = 'UI_CLICK' THEN 1 ELSE 0 END) AS clicks
            FROM app_event_log
            WHERE created_at >= ?
            GROUP BY DATE(created_at)
            ORDER BY log_date ASC
            """;

        List<String[]> rows = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return rows;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(since));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new String[]{
                            String.valueOf(rs.getDate("log_date")),
                            String.valueOf(rs.getInt("views")),
                            String.valueOf(rs.getInt("clicks"))
                        });
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.fetchInteractionTrends error: " + e.getMessage());
        }

        return rows;
    }

    public List<String[]> fetchTopPages(int limit) {
        String sql = """
            SELECT COALESCE(JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.route')), 'Direct/Unknown') AS route,
                   COUNT(*) AS visit_count
            FROM app_event_log
            WHERE event_type = 'PAGE_VIEW'
            GROUP BY route
            ORDER BY visit_count DESC
            LIMIT ?
            """;

        List<String[]> rows = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return rows;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Math.max(1, limit));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new String[]{
                            rs.getString("route"),
                            String.valueOf(rs.getInt("visit_count"))
                        });
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.fetchTopPages error: " + e.getMessage());
        }

        return rows;
    }

    public List<String[]> fetchTopClicks(int limit) {
        String sql = """
            SELECT COALESCE(JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.text')), 'Unknown') AS element_text,
                   COALESCE(JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.target')), 'Unknown') AS target,
                   COUNT(*) AS click_count
            FROM app_event_log
            WHERE event_type = 'UI_CLICK'
            GROUP BY element_text, target
            ORDER BY click_count DESC
            LIMIT ?
            """;

        List<String[]> rows = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return rows;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, Math.max(1, limit));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new String[]{
                            rs.getString("element_text"),
                            rs.getString("target"),
                            String.valueOf(rs.getInt("click_count"))
                        });
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.fetchTopClicks error: " + e.getMessage());
        }

        return rows;
    }

    public List<String[]> fetchTopUsers(LocalDateTime since, int limit) {
        String sql = """
            SELECT u.first_name,
                   u.last_name,
                   u.role_user,
                   COUNT(l.id) AS activity_count
            FROM app_event_log l
            JOIN user u ON l.user_id = u.id_user
            WHERE l.created_at >= ?
            GROUP BY u.id_user, u.first_name, u.last_name, u.role_user
            ORDER BY activity_count DESC
            LIMIT ?
            """;

        List<String[]> rows = new ArrayList<>();

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return rows;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(since));
                ps.setInt(2, Math.max(1, limit));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new String[]{
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("role_user"),
                            String.valueOf(rs.getInt("activity_count"))
                        });
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.fetchTopUsers error: " + e.getMessage());
        }

        return rows;
    }

    public List<String[]> fetchDeviceBreakdown(LocalDateTime since) {
        String sql = """
            SELECT JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.user_agent')) AS ua
            FROM app_event_log
            WHERE created_at >= ? AND JSON_EXTRACT(metadata, '$.user_agent') IS NOT NULL
            """;

        int desktop = 0;
        int mobile = 0;
        int chrome = 0;
        int safari = 0;
        int firefox = 0;
        int edge = 0;
        int other = 0;
        int total = 0;

        try (Connection conn = databaseService.getConnection()) {
            if (conn == null) {
                return new ArrayList<>();
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(since));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String ua = rs.getString("ua");
                        if (ua == null || ua.isBlank()) {
                            continue;
                        }

                        String lower = ua.toLowerCase();
                        total++;

                        if (lower.matches(".*(mobi|android|touch|mini).*")) {
                            mobile++;
                        } else {
                            desktop++;
                        }

                        if (lower.contains("edg/")) {
                            edge++;
                        } else if (lower.contains("chrome") || lower.contains("crios")) {
                            chrome++;
                        } else if (lower.contains("firefox") || lower.contains("fxios")) {
                            firefox++;
                        } else if (lower.contains("safari") && !lower.contains("chrome")) {
                            safari++;
                        } else {
                            other++;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("AppEventLogRepository.fetchDeviceBreakdown error: " + e.getMessage());
        }

        int safeTotal = Math.max(1, total);
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"desktop", String.valueOf(Math.round((desktop / (double) safeTotal) * 100))});
        rows.add(new String[]{"mobile", String.valueOf(Math.round((mobile / (double) safeTotal) * 100))});
        rows.add(new String[]{"Chrome", String.valueOf(Math.round((chrome / (double) safeTotal) * 100))});
        rows.add(new String[]{"Safari", String.valueOf(Math.round((safari / (double) safeTotal) * 100))});
        rows.add(new String[]{"Firefox", String.valueOf(Math.round((firefox / (double) safeTotal) * 100))});
        rows.add(new String[]{"Edge", String.valueOf(Math.round((edge / (double) safeTotal) * 100))});
        rows.add(new String[]{"Other", String.valueOf(Math.round((other / (double) safeTotal) * 100))});
        return rows;
    }

    private AppEventLog mapRow(ResultSet rs) throws SQLException {
        AppEventLog log = new AppEventLog();
        log.setId(rs.getLong("id"));
        log.setEventType(rs.getString("event_type"));
        log.setEntityType(rs.getString("entity_type"));

        int entityId = rs.getInt("entity_id");
        if (!rs.wasNull()) {
            log.setEntityId(entityId);
        }

        String metadata = rs.getString("metadata");
        log.setMetadataJson(metadata);

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            log.setCreatedAt(createdAt.toLocalDateTime());
        }

        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) {
            Optional<User> user = userRepository.findById(userId);
            user.ifPresent(log::setUser);
        }

        return log;
    }
}