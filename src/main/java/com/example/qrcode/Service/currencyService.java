package com.example.qrcode.Service;

import com.example.qrcode.Model.Currency;
import com.example.qrcode.Utils.ConnectionUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class currencyService {

    private static Connection connection;

    static {
        connection = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
    }

    public void saveCurrencies(List<Currency> currencies) {
        if (currencies.isEmpty()) {
            System.out.println("Không có dữ liệu để lưu!");
            return;
        }

        String sql = "INSERT INTO \"CurrencyCode\" (\"Country\", \"Currency\", \"Code\", \"Number\") VALUES (?, ?, ?, ?);";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (Currency currency : currencies) {
                pstmt.setString(1, currency.getCountryName());
                pstmt.setString(2, currency.getCurrencyName());
                pstmt.setString(3, currency.getCurrencyCode());
                pstmt.setString(4, currency.getCurrencyNumber());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Dữ liệu đã được lưu vào cơ sở dữ liệu!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Currency> searchCurrencies(String keyword) {
        ObservableList<Currency> currencyResults = FXCollections.observableArrayList();
        String sql = "SELECT \"Country\", \"Currency\", \"Code\", \"Number\" FROM \"CurrencyCode\" WHERE LOWER(\"Currency\") LIKE LOWER(?) OR LOWER(\"Country\") LIKE LOWER(?) OR \"Code\" LIKE ? OR \"Number\" LIKE ?";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            stmt.setString(3, "%" + keyword + "%");
            stmt.setString(4, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                currencyResults.add(new Currency(rs.getString("Country"), rs.getString("Currency"), rs.getString("Code"), rs.getString("Number")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currencyResults;
    }

    public ObservableList<Currency> getAllCurrencies() {
        ObservableList<Currency> currencyList = FXCollections.observableArrayList();

        if (connection == null) {
            System.out.println("Lỗi: Không có kết nối đến cơ sở dữ liệu!");
            return currencyList;
        }

        String sql = "SELECT \"Country\", \"Currency\", \"Code\", \"Number\" FROM \"CurrencyCode\"";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String countryName = rs.getString("Country");
                String currencyName = rs.getString("Currency");
                String currencyCode = rs.getString("Code");
                String currencyNumber = rs.getString("Number");
                currencyList.add(new Currency(countryName, currencyName, currencyCode, currencyNumber));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return currencyList;
    }

    public void deleteAllCurrencies() {
        String query = "DELETE FROM \"CurrencyCode\"";

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.executeUpdate();  // Thực thi câu lệnh DELETE
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrencyByNumber(String currencyNumber) {
        String query = "SELECT \"Currency\" FROM \"CurrencyCode\" WHERE \"Number\" = ?";
        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, currencyNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Currency");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currencyNumber; // Trả về mã nếu không tìm thấy
    }
}
