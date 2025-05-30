package com.example.qrcode.Service;

import com.example.qrcode.Utils.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class transferService {

    public double getAccountBalance(String accountNumber) {
        String query = "SELECT balance FROM accounts WHERE account_number = ?";
        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Nếu tài khoản không tồn tại
    }

    // Kiểm tra tài khoản còn tồn tại
    public boolean checkAccountExists(String accountNumber) {
        String query = "SELECT 1 FROM accounts WHERE account_number = ?";
        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xử lý giao dịch
    public boolean processTransaction(String sender, String receiver, double amount, String currency, String message) {
        String updateSender = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
        String updateReceiver = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
        String insertTransaction = "INSERT INTO transactions (sender_account, receiver_account, amount, currency, message) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");) {
            conn.setAutoCommit(false); // Bắt đầu transaction

            try (PreparedStatement stmt = conn.prepareStatement(updateSender)) {
                stmt.setDouble(1, amount);
                stmt.setString(2, sender);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(updateReceiver)) {
                stmt.setDouble(1, amount);
                stmt.setString(2, receiver);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertTransaction)) {
                stmt.setString(1, sender);
                stmt.setString(2, receiver);
                stmt.setDouble(3, amount);
                stmt.setString(4, currency);
                stmt.setString(5, message);
                stmt.executeUpdate();
            }

            conn.commit(); // Xác nhận transaction
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getAccountHolderName(String accountNumber) {
        String query = "SELECT account_holder FROM accounts WHERE account_number = ?";
        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("account_holder");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

}
