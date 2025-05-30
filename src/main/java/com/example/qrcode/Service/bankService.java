package com.example.qrcode.Service;

import com.example.qrcode.Model.Bank;
import com.example.qrcode.Utils.ConnectionUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class bankService {

    private static Connection connection;

    // Phương thức khởi tạo kết nối (Gọi từ main)
    public static void setConnection(Connection conn) {
        connection = conn;
    }

    static {
        // Sử dụng ConnectionUtil để mở kết nối
        connection = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
    }

    public void saveBanks(List<Bank> bankList) {
        if (bankList.isEmpty()) {
            System.out.println("Không có dữ liệu để lưu!");
            return;
        }

        String sql = "INSERT INTO \"BankCode\" (\"Bank_Name\", \"Bank_Code\") VALUES (?, ?);\n";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Bank bank : bankList) {
                pstmt.setString(1, bank.getBankName());
                pstmt.setString(2, bank.getBankCode());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Dữ liệu đã được lưu vào cơ sở dữ liệu!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Bank> searchBanks(String keyword) {
        ObservableList<Bank> searchResults = FXCollections.observableArrayList();
        String sql = "SELECT \"Bank_Name\", \"Bank_Code\" FROM \"BankCode\" WHERE LOWER(\"Bank_Name\") LIKE LOWER(?) OR \"Bank_Code\" LIKE ?";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String bankName = rs.getString("Bank_Name");
                String bankCode = rs.getString("Bank_Code");
                searchResults.add(new Bank(bankName, bankCode));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return searchResults;
    }

    public ObservableList<Bank> getAllBanks() {
        ObservableList<Bank> bankList = FXCollections.observableArrayList();

        if (connection == null) {
            System.out.println("Lỗi: Không có kết nối đến cơ sở dữ liệu!");
            return bankList;
        }

        String sql = "SELECT \"Bank_Name\", \"Bank_Code\" FROM \"BankCode\"";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String bankName = rs.getString("Bank_Name");
                String bankCode = rs.getString("Bank_Code");
                bankList.add(new Bank(bankName, bankCode));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return bankList;
    }

    public void deleteAllBanks() {
        String query = "DELETE FROM \"BankCode\""; // Xóa toàn bộ dữ liệu trong bảng bank

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.executeUpdate();  // Thực thi câu lệnh DELETE
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getBankNameByCode(String bankCode) {
        String query = "SELECT \"Bank_Name\" FROM \"BankCode\" WHERE \"Bank_Code\" = ?";
        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, bankCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Bank_Name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bankCode; // Trả về mã nếu không tìm thấy
    }

}
