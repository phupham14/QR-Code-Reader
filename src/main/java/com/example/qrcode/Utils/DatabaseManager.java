package com.example.qrcode.Utils;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        // Khởi tạo kết nối trong constructor
        String dbName = "QRReader";
        String user = "postgres";
        String password = "admin1405";

        connection = ConnectionUtil.getInstance().connect_to_db(dbName, user, password);
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Method test kết nối
    public Connection testConnection() {
        if (connection != null) {
            try {
                Statement stmt = connection.createStatement();

                System.out.println("\n=== Thông tin Server ===");
                ResultSet rs = stmt.executeQuery("SELECT version()");
                while(rs.next()) {
                    System.out.println("Version: " + rs.getString(1));
                }

                rs = stmt.executeQuery("SELECT current_database()");
                while(rs.next()) {
                    System.out.println("Database hiện tại: " + rs.getString(1));
                }

                rs = stmt.executeQuery("SELECT current_user");
                while(rs.next()) {
                    System.out.println("User hiện tại: " + rs.getString(1));
                }

                rs = stmt.executeQuery("SELECT inet_server_addr(), inet_server_port()");
                while(rs.next()) {
                    System.out.println("Server Address: " + rs.getString(1));
                    System.out.println("Server Port: " + rs.getString(2));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // Method để test
    public static void main(String[] args) {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        dbManager.testConnection();
        ConnectionUtil.getInstance().closeConnection();
    }
}