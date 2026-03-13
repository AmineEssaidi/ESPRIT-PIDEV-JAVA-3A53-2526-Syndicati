package com.pidev.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database Service - Handles database operations and connectivity
 */
public class DatabaseService {
    
    private static DatabaseService instance;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private int connectionTimeout;
    
    private DatabaseService() {
        // Initialize with pidev database
        this.dbUrl = "jdbc:mysql://localhost:3306/pidev";
        this.dbUser = "root";
        this.dbPassword = ""; // Empty password works for MySQL connection
        this.connectionTimeout = 5000; // 5 seconds - longer timeout for debugging
    }
    
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
    
    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection() {
        System.out.println("=== Database Connection Test ===");
        System.out.println("URL: " + dbUrl);
        System.out.println("User: " + dbUser);
        System.out.println("Password: " + (dbPassword.isEmpty() ? "[empty]" : "[set]"));
        System.out.println("Timeout: " + connectionTimeout + "ms");
        
        // Try multiple connection approaches
        return testConnectionWithProperties() || 
               testConnectionSimple() || 
               testConnectionWithoutDatabase() ||
               testCommonConfigurations();
    }
    
    private boolean testConnectionWithProperties() {
        System.out.println("--- Testing with full properties ---");
        try {
            Properties props = new Properties();
            props.setProperty("user", dbUser);
            props.setProperty("password", dbPassword);
            props.setProperty("connectTimeout", String.valueOf(connectionTimeout));
            props.setProperty("socketTimeout", String.valueOf(connectionTimeout));
            props.setProperty("autoReconnect", "true");
            props.setProperty("useSSL", "false");
            props.setProperty("allowPublicKeyRetrieval", "true");
            props.setProperty("serverTimezone", "UTC");
            
            try (Connection connection = DriverManager.getConnection(dbUrl, props)) {
                if (connection != null && !connection.isClosed()) {
                    System.out.println("✅ Database connection successful to pidev database!");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Connection with properties failed: " + e.getMessage());
            System.out.println("   Error Code: " + e.getErrorCode());
            System.out.println("   SQL State: " + e.getSQLState());
        }
        return false;
    }
    
    private boolean testConnectionSimple() {
        System.out.println("--- Testing with simple connection ---");
        try {
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                if (connection != null && !connection.isClosed()) {
                    System.out.println("✅ Simple database connection successful!");
                    return true;
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Simple connection failed: " + e.getMessage());
        }
        return false;
    }
    
    private boolean testConnectionWithoutDatabase() {
        System.out.println("--- Testing connection to MySQL server (no database) ---");
        try {
            String serverUrl = "jdbc:mysql://localhost:3306/";
            try (Connection connection = DriverManager.getConnection(serverUrl, dbUser, dbPassword)) {
                if (connection != null && !connection.isClosed()) {
                    System.out.println("✅ MySQL server connection successful!");
                    System.out.println("   Server is running, but pidev database might not exist or be accessible");
                    return false; // Still return false since we need the specific database
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ MySQL server connection failed: " + e.getMessage());
            System.out.println("   This suggests MySQL server is not running or not accessible");
        }
        return false;
    }
    
    private boolean testCommonConfigurations() {
        System.out.println("--- Testing common MySQL configurations ---");
        
        // Test different ports and configurations
        String[] testConfigs = {
            "jdbc:mysql://localhost:3306/pidev",
            "jdbc:mysql://127.0.0.1:3306/pidev",
            "jdbc:mysql://localhost:3307/pidev", // Common alternative port
            "jdbc:mysql://localhost:3306/pidev?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            "jdbc:mysql://localhost:3306/pidev?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8"
        };
        
        for (String testUrl : testConfigs) {
            System.out.println("   Testing: " + testUrl);
            try {
                Properties props = new Properties();
                props.setProperty("user", dbUser);
                props.setProperty("password", dbPassword);
                props.setProperty("connectTimeout", "3000");
                props.setProperty("socketTimeout", "3000");
                
                try (Connection connection = DriverManager.getConnection(testUrl, props)) {
                    if (connection != null && !connection.isClosed()) {
                        System.out.println("✅ SUCCESS with URL: " + testUrl);
                        // Update our working URL
                        this.dbUrl = testUrl;
                        return true;
                    }
                }
            } catch (SQLException e) {
                System.out.println("   ❌ Failed: " + e.getMessage());
            }
        }
        
        // Test with different user/password combinations
        System.out.println("--- Testing different authentication ---");
        String[][] authTests = {
            {"root", ""},
            {"root", "root"},
            {"root", "password"},
            {"root", "123456"},
            {"", ""}
        };
        
        for (String[] auth : authTests) {
            System.out.println("   Testing user: '" + auth[0] + "', password: '" + (auth[1].isEmpty() ? "[empty]" : "[set]") + "'");
            try {
                try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pidev", auth[0], auth[1])) {
                    if (connection != null && !connection.isClosed()) {
                        System.out.println("✅ SUCCESS with user: " + auth[0]);
                        this.dbUser = auth[0];
                        this.dbPassword = auth[1];
                        return true;
                    }
                }
            } catch (SQLException e) {
                System.out.println("   ❌ Failed: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Get database connection
     * @return Connection object or null if failed
     */
    public Connection getConnection() {
        try {
            Properties props = new Properties();
            props.setProperty("user", dbUser);
            props.setProperty("password", dbPassword);
            props.setProperty("connectTimeout", String.valueOf(connectionTimeout));
            props.setProperty("socketTimeout", String.valueOf(connectionTimeout));
            props.setProperty("autoReconnect", "true");
            
            return DriverManager.getConnection(dbUrl, props);
        } catch (SQLException e) {
            System.out.println("Failed to get database connection: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if database is available
     * @return true if database is reachable, false otherwise
     */
    public boolean isDatabaseAvailable() {
        return testConnection();
    }
    
    // Getters and setters for configuration
    public String getDbUrl() {
        return dbUrl;
    }
    
    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }
    
    public String getDbUser() {
        return dbUser;
    }
    
    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }
    
    public String getDbPassword() {
        return dbPassword;
    }
    
    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
