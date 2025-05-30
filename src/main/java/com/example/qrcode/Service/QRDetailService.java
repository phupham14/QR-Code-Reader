package com.example.qrcode.Service;

import com.example.qrcode.Model.QRDetail;
import com.example.qrcode.Utils.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class QRDetailService {
    private static Connection connection;

    public QRDetailService(Connection connection) {
        this.connection = connection;
    }

    static {
        // Sử dụng ConnectionUtil để mở kết nối
        connection = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
    }

    public static void saveQRDetail(QRDetail qrDetail) {
        String sql = "INSERT INTO \"QRDetail\" (\"payloadFormatIndicator\", \"pointOfInitiationMethod\", \"globalUniqueIdentifier\", \"acquirerId\",\n" +
                "                \"merchantId\", \"serviceCode\", \"transactionCurrency\", \"transactionAmount\", \"billNumber\", \"storeLabel\", \"terminalLabel\",\n" +
                "                \"mobileNumber\", \"crc\", \"timestamp\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        //int index = 0;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, qrDetail.getPayloadFormatIndicator());
            stmt.setString(2, qrDetail.getPointOfInitiationMethod());
            stmt.setString(3, qrDetail.getGlobalUniqueIdentifier());
            stmt.setString(4, qrDetail.getAcquirerId());
            stmt.setString(5, qrDetail.getMerchantId());
            stmt.setString(6, qrDetail.getServiceCode());
            stmt.setString(7, qrDetail.getTransactionCurrency());
            stmt.setString(8, qrDetail.getTransactionAmount());
            stmt.setString(9, qrDetail.getBillNumber());
            stmt.setString(10, qrDetail.getStoreLabel());
            stmt.setString(11, qrDetail.getTerminalLabel());
            stmt.setString(12, qrDetail.getMobileNumber());
            stmt.setString(13, qrDetail.getCrc());
            stmt.setTimestamp(14, qrDetail.getTimestamp().valueOf(LocalDateTime.now()));

            stmt.executeUpdate();
            System.out.println("Data inserted successfully into QRDetail table.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
