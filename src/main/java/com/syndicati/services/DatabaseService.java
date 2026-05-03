package com.syndicati.services;

import com.syndicati.utils.config.EnvConfig;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Proxy;

/**
 * Database Service - Handles database operations and connectivity with high-performance pooling and caching.
 */
public class DatabaseService {
    
    private static DatabaseService instance;
    private static final int POOL_SIZE = 4; // Balanced pool for Clever Cloud limits
    private static final long CACHE_TTL_MS = 30000; // 30s cache
    
    private final BlockingQueue<Connection> pool;
    private final Map<String, CacheEntry> dataCache = new ConcurrentHashMap<>();

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private int connectionTimeout;

    private static class CacheEntry {
        final Object data;
        final long expiry;
        CacheEntry(Object data) {
            this.data = data;
            this.expiry = System.currentTimeMillis() + CACHE_TTL_MS;
        }
        boolean isExpired() { return System.currentTimeMillis() > expiry; }
    }

    private DatabaseService() {
        // Initialize from DATABASE_URL when available.
        DbConfig cfg = parseDatabaseConfig(EnvConfig.get("DATABASE_URL"));
        this.dbUrl = cfg.jdbcUrl;
        this.dbUser = cfg.username;
        this.dbPassword = cfg.password;
        this.connectionTimeout = 5000;
        this.pool = new LinkedBlockingQueue<>(POOL_SIZE);
        
        // Pre-fill pool in background
        Thread.startVirtualThread(this::initializePool);
    }

    private void initializePool() {
        for (int i = 0; i < POOL_SIZE; i++) {
            boolean success = false;
            for (int retry = 0; retry < 3 && !success; retry++) {
                try {
                    Connection conn = createNewConnection();
                    if (conn != null) {
                        pool.offer(conn);
                        success = true;
                    }
                } catch (Exception e) {
                    if (retry == 2) {
                        System.err.println("Failed to pre-fill pool connection after 3 retries: " + e.getMessage());
                    } else {
                        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    }
                }
            }
            // Add a substantial delay between successful connection creations to avoid triggering rate limits
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
        }
    }

    private Connection createNewConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPassword);
        props.setProperty("connectTimeout", "15000"); // 15 seconds for Clever Cloud cold starts
        props.setProperty("socketTimeout", "30000"); // Longer socket timeout for queries
        props.setProperty("autoReconnect", "true");
        props.setProperty("useSSL", "true");
        props.setProperty("requireSSL", "false");
        props.setProperty("enabledTLSProtocols", "TLSv1.2,TLSv1.3");
        props.setProperty("allowPublicKeyRetrieval", "true");
        props.setProperty("serverTimezone", "UTC");
        props.setProperty("zeroDateTimeBehavior", "CONVERT_TO_NULL");
        props.setProperty("useServerPrepStmts", "true");
        props.setProperty("cachePrepStmts", "true");
        props.setProperty("prepStmtCacheSize", "250");
        props.setProperty("prepStmtCacheSqlLimit", "2048");
        return DriverManager.getConnection(dbUrl, props);
    }
    
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
    
    public Connection getConnection() {
        try {
            // Try to get an existing connection from the pool quickly (1s timeout)
            Connection conn = pool.poll(1, TimeUnit.SECONDS);
            if (conn != null) {
                if (!conn.isClosed()) {
                    return createPooledProxy(conn);
                }
                // If closed, create a replacement
                return createNewConnectionFallback();
            }

            // Pool is empty - create an emergency connection if we're under the limit
            System.out.println("[WARN] Connection pool empty. Creating emergency connection.");
            return createNewConnectionFallback();
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to get database connection: " + e.getMessage());
            return null;
        }
    }

    private synchronized Connection createNewConnectionFallback() throws SQLException {
        return createNewConnection();
    }

    private Connection createPooledProxy(final Connection physicalConn) {
        return (Connection) Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> {
                if ("close".equals(method.getName())) {
                    releaseConnection(physicalConn);
                    return null;
                }
                return method.invoke(physicalConn, args);
            }
        );
    }

    public void putCache(String key, Object value) {
        if (value != null) dataCache.put(key, new CacheEntry(value));
    }

    @SuppressWarnings("unchecked")
    public <T> T getCache(String key) {
        CacheEntry entry = dataCache.get(key);
        if (entry != null && !entry.isExpired()) return (T) entry.data;
        dataCache.remove(key);
        return null;
    }

    public void clearCache(String key) { dataCache.remove(key); }
    public void clearAllCache() { dataCache.clear(); }

    public void releaseConnection(Connection conn) {
        if (conn == null) return;
        try {
            if (!conn.isClosed()) {
                if (!pool.offer(conn)) {
                    conn.close(); // Pool full
                }
            } else {
                conn.close();
            }
        } catch (SQLException e) {
            try { conn.close(); } catch (SQLException ignore) {}
        }
    }

    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    public boolean isDatabaseAvailable() {
        return testConnection();
    }
    
    // Getters and setters for configuration
    public String getDbUrl() { return dbUrl; }
    public void setDbUrl(String dbUrl) { this.dbUrl = dbUrl; }
    public String getDbUser() { return dbUser; }
    public void setDbUser(String dbUser) { this.dbUser = dbUser; }
    public String getDbPassword() { return dbPassword; }
    public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; }
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    private static DbConfig parseDatabaseConfig(String databaseUrl) {
        String fallbackJdbc = "jdbc:mysql://127.0.0.1:3306/syndicati?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=CONVERT_TO_NULL";
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return new DbConfig(fallbackJdbc, "root", "");
        }

        try {
            // Use http scheme for parsing to ensure userInfo and host are correctly extracted
            URI uri = URI.create(databaseUrl.replace("mysql://", "http://"));
            String userInfo = uri.getUserInfo();
            String username = "root";
            String password = "";

            if (userInfo != null && !userInfo.isBlank()) {
                String[] parts = userInfo.split(":", 2);
                username = decode(parts[0]);
                if (parts.length > 1) {
                    password = decode(parts[1]);
                }
            }

            String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 3306;
            String path = uri.getPath() == null ? "/syndicati" : uri.getPath();
            String dbName = path.startsWith("/") ? path.substring(1) : path;
            if (dbName.isBlank()) dbName = "syndicati";

            String query = uri.getQuery();
            StringBuilder jdbc = new StringBuilder("jdbc:mysql://")
                .append(host)
                .append(":")
                .append(port)
                .append("/")
                .append(dbName);

            if (query != null && !query.isBlank()) {
                jdbc.append("?").append(query);
                if (!query.contains("zeroDateTimeBehavior=")) {
                    jdbc.append("&zeroDateTimeBehavior=CONVERT_TO_NULL");
                }
            } else {
                jdbc.append("?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=CONVERT_TO_NULL");
            }

            return new DbConfig(jdbc.toString(), username, password);
        } catch (Exception ex) {
            return new DbConfig(fallbackJdbc, "root", "");
        }
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String extractHostPort(String jdbcUrl) {
        String cleaned = jdbcUrl.replace("jdbc:mysql://", "");
        int slashIndex = cleaned.indexOf('/');
        if (slashIndex > 0) {
            return cleaned.substring(0, slashIndex);
        }
        return "127.0.0.1:3306";
    }

    private static class DbConfig {
        private final String jdbcUrl;
        private final String username;
        private final String password;

        private DbConfig(String jdbcUrl, String username, String password) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
        }
    }
    
    public void shutdown() {
        System.out.println("[SHUTDOWN] Closing database connections...");
        java.util.List<Connection> connections = new java.util.ArrayList<>();
        pool.drainTo(connections);
        for (Connection conn : connections) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ignore) {}
        }
    }
}
