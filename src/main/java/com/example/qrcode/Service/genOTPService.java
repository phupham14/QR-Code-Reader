package com.example.qrcode.Service;

import com.example.qrcode.Utils.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class genOTPService {
    public void saveSecretKeyHistory(String accountNumber, String secretKey) {
        String query = "INSERT INTO secretkey_change (account_number, secretkey) VALUES (?, ?)";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            stmt.setString(2, secretKey);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
