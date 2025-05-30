package com.example.qrcode.Service;

import com.example.qrcode.Model.Country;
import com.example.qrcode.Utils.ConnectionUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class countryService {

    private static Connection connection;

    // Phương thức khởi tạo kết nối (Gọi từ main)
    public static void setConnection(Connection conn) {
        connection = conn;
    }

    static {
        // Sử dụng ConnectionUtil để mở kết nối
        connection = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
    }

    public void saveCountries(List<Country> countries) {
        if (countries.isEmpty()) {
            System.out.println("Không có dữ liệu để lưu!");
            return;
        }

        String sql = "INSERT INTO \"CountryCode\" (\"Country\", \"Alpha2Code\", \"Numeric\") VALUES (?, ?, ?);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            for (Country country : countries) {
                pstmt.setString(1, country.getCountryName());
                pstmt.setString(2, country.getCountryCode());
                pstmt.setString(3, country.getNumericCode());
                pstmt.addBatch(); // Thêm vào batch
            }

            pstmt.executeBatch(); // Thực thi batch
            System.out.println("Dữ liệu đã được lưu vào cơ sở dữ liệu!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public ObservableList<Country> searchCountries(String keyword) {
        ObservableList<Country> countryResults = FXCollections.observableArrayList();

        if (connection == null) {
            System.out.println("Lỗi: Không có kết nối đến cơ sở dữ liệu!");
            return countryResults;
        }

        String sql = "SELECT \"Country\", \"Alpha2Code\", \"Numeric\" FROM \"CountryCode\" WHERE LOWER(\"Country\") LIKE LOWER(?) OR \"Alpha2Code\" LIKE LOWER(?) OR \"Numeric\" LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            stmt.setString(3, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String countryName = rs.getString("Country");
                String countryCode = rs.getString("Alpha2Code");
                String countryNumeric = rs.getString("Numeric");
                countryResults.add(new Country(countryName, countryCode, countryNumeric));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return countryResults;
    }

    public ObservableList<Country> getAllCountries() {
        ObservableList<Country> countryList = FXCollections.observableArrayList();

        if (connection == null) {
            System.out.println("Lỗi: Không có kết nối đến cơ sở dữ liệu!");
            return countryList;
        }

        String sql = "SELECT \"Country\", \"Alpha2Code\", \"Numeric\" FROM \"CountryCode\"";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String countryName = rs.getString("Country");
                String countryCode = rs.getString("Alpha2Code");
                String countryNumeric = rs.getString("Numeric");
                countryList.add(new Country(countryName, countryCode, countryNumeric));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return countryList;
    }

    public void deleteAllCountries() {
        String query = "DELETE FROM \"CountryCode\""; // Xóa toàn bộ dữ liệu trong bảng country

        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.executeUpdate();  // Thực thi câu lệnh DELETE
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getCountryNameByCode(String countryCode) {
        String query = "SELECT cc.\"Country\" FROM \"QRDetail\" qd " +
                "JOIN \"CountryCode\" cc ON qd.\"transactionCurrency\" = cc.\"Numeric\" " +
                "WHERE qd.\"transactionCurrency\" = ?";
        try (Connection conn = ConnectionUtil.getInstance().connect_to_db("QRReader", "postgres", "admin1405");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, countryCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("Country");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countryCode; // Trả về mã nếu không tìm thấy
    }

}
