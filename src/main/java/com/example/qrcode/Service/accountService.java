package com.example.qrcode.Service;

import com.example.qrcode.Model.account;
import com.example.qrcode.Utils.ConnectionUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class accountService {
    private static Connection connection;

    // Phương thức khởi tạo kết nối (Gọi từ main)
    public static void setConnection(Connection conn) {
        connection = conn;
    }

    static {
        // Sử dụng ConnectionUtil để mở kết nối
        connection = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
    }

    public static ObservableList<account> getAllAccounts() {
        ObservableList<account> accountList = FXCollections.observableArrayList();

        if (connection == null) {
            System.out.println("Lỗi: Không có kết nối đến cơ sở dữ liệu!");
            return accountList;
        }

        String sql = "SELECT account_number, account_holder, balance, secretkey FROM accounts order by account_id";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                String account_number = rs.getString("account_number");
                String account_holder = rs.getString("account_holder");
                String balance = rs.getString("balance");
                String secretkey = rs.getString("secretkey");
                accountList.add(new account(account_number, account_holder, balance, secretkey));
                count++;
            }
            System.out.println("Số lượng tài khoản lấy được: " + count);

        } catch (SQLException e) {
            System.err.println("Lỗi SQL: " + e.getMessage());
            e.printStackTrace();
        }

        return accountList;
    }

}
