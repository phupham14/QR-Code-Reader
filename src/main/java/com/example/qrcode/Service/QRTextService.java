package com.example.qrcode.Service;

import com.example.qrcode.Utils.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class QRTextService {

    private static Connection connection;

    // Phương thức khởi tạo kết nối (Gọi từ main)
    public static void setConnection(Connection conn) {
        connection = conn;
    }

    static {
        // Sử dụng ConnectionUtil để mở kết nối
        connection = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
    }

    public static void saveQRCodeRaw(String qrRawContent, String type) {
        if (connection == null) {
            throw new IllegalStateException("Database connection is not initialized.");
        }

        String sql = "INSERT INTO \"QRText\" (\"QR_Code_Raw\", \"date_create\", type) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, qrRawContent.trim());
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now())); // Lưu thời gian hiện tại
            pstmt.setString(3, type);  // Lưu kiểu dữ liệu "filetype" hoặc "cameratype"

            pstmt.executeUpdate();
            System.out.println("QR Code Raw Content saved successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi lưu QRCode Raw: " + e.getMessage());
        }
    }
}
