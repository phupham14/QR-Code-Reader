package com.example.qrcode.Service;

import com.example.qrcode.Model.Transaction;
import com.example.qrcode.Utils.ConnectionUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LogService {

    public List<Transaction> fetchTransactions(String senderAcc, LocalDate fromDate, LocalDate toDate) {
        List<Transaction> list = new ArrayList<>();

        String query = "SELECT sender_account, receiver_account, amount, transaction_time, message, dorc " +
                "FROM transactions " +
                "WHERE (sender_account = ? OR receiver_account = ?) " +
                "AND transaction_time BETWEEN ? AND ? " +
                "ORDER BY transaction_time DESC";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, senderAcc);
            stmt.setString(2, senderAcc);
            stmt.setTimestamp(3, Timestamp.valueOf(fromDate.atStartOfDay()));
            stmt.setTimestamp(4, Timestamp.valueOf(toDate.plusDays(1).atStartOfDay().minusSeconds(1)));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Transaction(
                        rs.getString("sender_account"),
                        rs.getString("receiver_account"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("transaction_time"),
                        rs.getString("message"),
                        rs.getString("dorc")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String fetchAccountHolder(String accountNumber) {

        String query = "SELECT account_holder FROM accounts WHERE account_number = ?";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("account_holder");
            } else {
                return "Không tìm thấy tài khoản";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Lỗi khi truy vấn";
        }
    }

    public static boolean isValidAccount(String accountNumber) {
        String query = "SELECT 1 FROM accounts WHERE account_number = ?";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)){

            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Có bản ghi -> hợp lệ

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
