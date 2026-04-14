package com.syndicati.scratch;

import com.syndicati.services.DatabaseService;
import java.sql.*;

public class DbInspector {
    public static void main(String[] args) {
        DatabaseService db = DatabaseService.getInstance();
        try (Connection conn = db.getConnection()) {
            if (conn == null) {
                System.out.println("Connection failed!");
                return;
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) {
                    System.out.println("CONNECTED TO DATABASE: " + rs.getString(1));
                }
            }
            DatabaseMetaData dm = conn.getMetaData();
            ResultSet rsTables = dm.getTables(null, null, "commentaire", null);
            if (rsTables.next()) {
                System.out.println("TABLE 'commentaire' EXISTS.");
            } else {
                System.out.println("TABLE 'commentaire' DOES NOT EXIST in this database!");
                
                System.out.println("List of tables:");
                ResultSet allTables = dm.getTables(null, null, "%", new String[]{"TABLE"});
                while (allTables.next()) {
                    System.out.println("- " + allTables.getString("TABLE_NAME"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
