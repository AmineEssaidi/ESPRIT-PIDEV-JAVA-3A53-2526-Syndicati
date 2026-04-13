package com.syndicati;

import com.syndicati.services.DatabaseService;
import java.sql.*;

public class DbInspector {
    public static void main(String[] args) {
        DatabaseService db = DatabaseService.getInstance();
        try (Connection conn = db.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "publication", null);
            System.out.println("Columns in 'publication' table:");
            while (rs.next()) {
                System.out.println("- " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
