package com.syndicati.services.user.messaging;

import com.syndicati.models.user.Conversation;
import com.syndicati.models.user.Message;
import com.syndicati.models.user.Participant;
import com.syndicati.models.user.User;
import com.syndicati.services.DatabaseService;
import com.syndicati.services.user.user.UserService;
import com.syndicati.services.user.relationship.UserRelationshipService;

import com.syndicati.services.user.messaging.socket.MessagingSocketClient;
import com.syndicati.services.user.messaging.socket.SocketPayload;
import com.syndicati.utils.session.SessionManager;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessagingService {
    private static MessagingService instance;
    private final DatabaseService db = DatabaseService.getInstance();
    private final UserService userService = new UserService(); // Using new instance as it doesn't seem to have a singleton in the listing

    private MessagingService() {
        ensureTablesExist();
    }

    public static MessagingService getInstance() {
        if (instance == null) {
            instance = new MessagingService();
        }
        return instance;
    }

    private void ensureTablesExist() {
        String[] queries = {
            "CREATE TABLE IF NOT EXISTS conversation (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "created_at DATETIME NOT NULL," +
            "name VARCHAR(255)," +
            "is_group TINYINT(1) DEFAULT 0," +
            "creator_id INT," +
            "FOREIGN KEY (creator_id) REFERENCES user(id_user)" +
            ") ENGINE=InnoDB;",

            "CREATE TABLE IF NOT EXISTS conversation_participant (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "conversation_id INT NOT NULL," +
            "user_id INT NOT NULL," +
            "joined_at DATETIME NOT NULL," +
            "is_banned TINYINT(1) DEFAULT 0," +
            "FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE," +
            "FOREIGN KEY (user_id) REFERENCES user(id_user)," +
            "UNIQUE KEY UQ_conv_user (conversation_id, user_id)" +
            ") ENGINE=InnoDB;",

            "CREATE TABLE IF NOT EXISTS message (" +
            "id INT AUTO_INCREMENT PRIMARY KEY," +
            "conversation_id INT NOT NULL," +
            "sender_id INT NOT NULL," +
            "content TEXT NOT NULL," +
            "created_at DATETIME NOT NULL," +
            "is_read TINYINT(1) DEFAULT 0," +
            "FOREIGN KEY (conversation_id) REFERENCES conversation(id) ON DELETE CASCADE," +
            "FOREIGN KEY (sender_id) REFERENCES user(id_user)" +
            ") ENGINE=InnoDB;"
        };

        try (Connection conn = db.getConnection(); Statement stmt = conn.createStatement()) {
            for (String q : queries) {
                stmt.execute(q);
            }
        } catch (SQLException e) {
            System.err.println("Failed to ensure messaging tables exist: " + e.getMessage());
        }
    }

    public List<Conversation> findUserConversations(int userId) {
        List<Conversation> conversations = new ArrayList<>();
        String query = "SELECT c.* FROM conversation c " +
                       "JOIN conversation_participant cp ON c.id = cp.conversation_id " +
                       "WHERE cp.user_id = ? ORDER BY (SELECT MAX(created_at) FROM message WHERE conversation_id = c.id) DESC, c.created_at DESC";
        
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                conversations.add(mapConversation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    public List<Message> findConversationMessages(int conversationId) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM message WHERE conversation_id = ? ORDER BY created_at ASC";
        
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, conversationId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(mapMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public Optional<Message> findLastMessage(int conversationId) {
        String query = "SELECT * FROM message WHERE conversation_id = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, conversationId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public int sendMessage(int senderId, String content, Integer conversationId, Integer recipientId) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            conn.setAutoCommit(false);

            int targetConvId = -1;
            if (conversationId != null) {
                targetConvId = conversationId;
            } else if (recipientId != null) {
                // Find existing 1-to-1 conversation
                targetConvId = findExisting1to1(conn, senderId, recipientId);
                if (targetConvId == -1) {
                    // Create new conversation
                    targetConvId = create1to1Conversation(conn, senderId, recipientId);
                }
            }

            if (targetConvId == -1) throw new SQLException("Could not determine conversation");

            String query = "INSERT INTO message (conversation_id, sender_id, content, created_at, is_read) VALUES (?, ?, ?, ?, 0)";
            try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, targetConvId);
                pstmt.setInt(2, senderId);
                pstmt.setString(3, content);
                pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int msgId = rs.getInt(1);
                    conn.commit();
                    
                    // Broadcast via socket
                    broadcastSocketMessage(senderId, content, targetConvId, recipientId);
                    
                    return targetConvId; // Return conversation ID so UI knows where it went
                }
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return -1;
    }

    private void broadcastSocketMessage(int senderId, String content, int convId, Integer recipientId) {
        try {
            MessagingSocketClient client = MessagingSocketClient.getInstance();
            if (!client.isConnected()) {
                client.connect(senderId);
            }
            
            SocketPayload payload = new SocketPayload(SocketPayload.Type.MESSAGE);
            payload.setSenderId(senderId);
            payload.setConversationId(convId);
            payload.setRecipientId(recipientId);
            payload.setContent(content);
            
            Message m = new Message();
            m.setSenderId(senderId);
            m.setConversationId(convId);
            m.setContent(content);
            m.setCreatedAt(LocalDateTime.now());
            payload.setMessage(m);
            
            client.send(payload);
        } catch (Exception e) {
            System.err.println("[MessagingService] Failed to broadcast via socket: " + e.getMessage());
        }
    }

    private int findExisting1to1(Connection conn, int u1, int u2) throws SQLException {
        String query = "SELECT cp1.conversation_id FROM conversation_participant cp1 " +
                       "JOIN conversation_participant cp2 ON cp1.conversation_id = cp2.conversation_id " +
                       "JOIN conversation c ON c.id = cp1.conversation_id " +
                       "WHERE cp1.user_id = ? AND cp2.user_id = ? AND c.is_group = 0";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, u1);
            pstmt.setInt(2, u2);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    private int create1to1Conversation(Connection conn, int u1, int u2) throws SQLException {
        String qConv = "INSERT INTO conversation (created_at, is_group) VALUES (?, 0)";
        int convId = -1;
        try (PreparedStatement pstmt = conn.prepareStatement(qConv, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) convId = rs.getInt(1);
        }

        if (convId != -1) {
            String qPart = "INSERT INTO conversation_participant (conversation_id, user_id, joined_at) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(qPart)) {
                // Participant 1
                pstmt.setInt(1, convId);
                pstmt.setInt(2, u1);
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.addBatch();
                // Participant 2
                pstmt.setInt(1, convId);
                pstmt.setInt(2, u2);
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.addBatch();
                pstmt.executeBatch();
            }
        }
        return convId;
    }

    public int createGroup(String name, List<Integer> participants, int creatorId) {
        Connection conn = null;
        try {
            conn = db.getConnection();
            conn.setAutoCommit(false);

            String qConv = "INSERT INTO conversation (created_at, name, is_group, creator_id) VALUES (?, ?, 1, ?)";
            int convId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(qConv, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setString(2, name);
                pstmt.setInt(3, creatorId);
                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) convId = rs.getInt(1);
            }

            if (convId != -1) {
                String qPart = "INSERT INTO conversation_participant (conversation_id, user_id, joined_at) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(qPart)) {
                    // Add creator
                    pstmt.setInt(1, convId);
                    pstmt.setInt(2, creatorId);
                    pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    pstmt.addBatch();

                    for (int uid : participants) {
                        if (uid == creatorId) continue;
                        pstmt.setInt(1, convId);
                        pstmt.setInt(2, uid);
                        pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
                conn.commit();
                return convId;
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return -1;
    }

    public List<Participant> findConversationParticipants(int conversationId) {
        List<Participant> participants = new ArrayList<>();
        String query = "SELECT * FROM conversation_participant WHERE conversation_id = ?";
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, conversationId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                participants.add(mapParticipant(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return participants;
    }

    private Conversation mapConversation(ResultSet rs) throws SQLException {
        Conversation c = new Conversation();
        c.setId(rs.getInt("id"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        c.setName(rs.getString("name"));
        c.setGroup(rs.getBoolean("is_group"));
        c.setCreatorId(rs.getInt("creator_id"));
        return c;
    }

    private Message mapMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setConversationId(rs.getInt("conversation_id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setContent(rs.getString("content"));
        m.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        m.setRead(rs.getBoolean("is_read"));
        return m;
    }

    private Participant mapParticipant(ResultSet rs) throws SQLException {
        Participant p = new Participant();
        p.setId(rs.getInt("id"));
        p.setConversationId(rs.getInt("conversation_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setJoinedAt(rs.getTimestamp("joined_at").toLocalDateTime());
        p.setBanned(rs.getBoolean("is_banned"));
        return p;
    }
}
