package com.example.qrcode.Utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionUtil {
    // Thêm instance static
    private static ConnectionUtil instance;
    private Connection conn = null;

    // Constructor private
    private ConnectionUtil() {}

    // Method static để lấy instance
    public static ConnectionUtil getInstance() {
        if (instance == null) {
            instance = new ConnectionUtil();
        }
        return instance;
    }

    // Giữ nguyên method connect_to_db
    public Connection connect_to_db(String dbname, String user, String pass) {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/" + dbname,
                    user,
                    pass
            );
            if (conn != null) {
                System.out.println("Connection Established");
            }
            return conn;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    // Giữ nguyên method closeConnection
    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Connection Closed");
            }
        } catch (Exception e) {
            System.out.println("Error closing: " + e.getMessage());
        }
    }
}