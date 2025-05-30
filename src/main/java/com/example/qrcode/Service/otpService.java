package com.example.qrcode.Service;

import com.example.qrcode.Model.account;
import com.example.qrcode.Utils.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class otpService {
    // Lưu Secret Key vào CSDL
    public static void saveSecretKeyToDB(String accountNumber, String secretKey) {
        String query = "UPDATE accounts SET secretkey = ? WHERE account_number = ?";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, String.valueOf(secretKey));
            stmt.setString(2, String.valueOf(accountNumber));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi lưu Secret Key vào CSDL.");
        }
    }

    public String getAccountNumberByUsername(account account_holder) {
        String query = "SELECT account_number FROM accounts WHERE account_holder = ?";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, String.valueOf(account_holder));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("account_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    // Lấy Secret Key từ CSDL
    public static String getSecretKeyFromDB(String accountNumber) {
        String query = "SELECT secretkey FROM accounts WHERE account_number = ?";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, String.valueOf(accountNumber));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("secretkey");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
