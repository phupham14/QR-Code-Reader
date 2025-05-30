package com.example.qrcode.Controller;

import com.example.qrcode.Model.Country;
import com.example.qrcode.Service.countryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class countryController {
    @FXML
    private TableView<Country> searchCountry_tableView;
    @FXML
    private TableColumn<Country, String> searchCountry_Countryname;
    @FXML
    private TableColumn<Country, String> searchCountry_CountryCode;
    @FXML
    private TableColumn<Country, String> searchCountry_Countrynumeric;
    @FXML
    private TextField searchCountry_textfield;
    @FXML
    private Button searchCountry_searchBtn, searchCountry_uploacsvBtn, searchCountry_savetoDB;
    @FXML
    private Button searchCountry_deleteDB;

    private final ObservableList<Country> countryList = FXCollections.observableArrayList();
    private final countryService countryService = new countryService();

    @FXML
    public void initialize() {
        searchCountry_Countryname.setCellValueFactory(cellData -> cellData.getValue().countryNameProperty());
        searchCountry_CountryCode.setCellValueFactory(cellData -> cellData.getValue().countryCodeProperty());
        searchCountry_Countrynumeric.setCellValueFactory(cellData -> cellData.getValue().numericCodeProperty());
        searchCountry_tableView.setItems(countryList);

        searchCountry_searchBtn.setOnAction(event -> searchFromDatabase());
        searchCountry_uploacsvBtn.setOnAction(event -> handleUploadFile());
        searchCountry_savetoDB.setOnAction(event -> saveToDatabase());
        searchCountry_deleteDB.setOnAction(event -> deleteAllCountries());

        // Tải dữ liệu từ CSDL khi mở ứng dụng
        loadDataFromDatabase();
    }

    @FXML
    private void searchFromDatabase() {
        String keyword = searchCountry_textfield.getText().trim();
        if (keyword.isEmpty()) {
            showAlert("Thông báo", "Vui lòng nhập từ khóa tìm kiếm!");
            return;
        }
        ObservableList<Country> searchResults = countryService.searchCountries(keyword);
        countryList.setAll(searchResults);
    }

    @FXML
    private void handleUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(searchCountry_uploacsvBtn.getScene().getWindow());

        if (file != null) {
            readExcel(file);
        } else {
            showAlert("Lỗi", "Vui lòng chọn một file Excel (.xlsx) hợp lệ!");
        }
    }

    @FXML
    private void deleteAllCountries() {
        // Hiển thị hộp thoại xác nhận trước khi xóa
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Bạn có chắc chắn muốn xóa toàn bộ danh sách quốc gia không? Hành động này không thể hoàn tác!");

        if (confirmDialog.showAndWait().get() == ButtonType.OK) {
            countryService.deleteAllCountries();  // Gọi service để xóa dữ liệu
            loadDataFromDatabase();  // Cập nhật lại danh sách sau khi xóa
        }
    }

    private void loadDataFromDatabase() {
        ObservableList<Country> storedCountries = countryService.getAllCountries();
        countryList.setAll(storedCountries);
    }


    private void readExcel(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int firstRowIndex = sheet.getFirstRowNum() + 1; // Bỏ qua dòng tiêu đề

            for (int i = firstRowIndex; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String countryName = getCellValueAsString(row.getCell(0));
                String countryCode = getCellValueAsString(row.getCell(1));
                String numericCode = getCellValueAsString(row.getCell(2));

                if (!countryName.isEmpty() && !countryCode.isEmpty() && !numericCode.isEmpty()) {
                    countryList.add(new Country(countryName, countryCode, numericCode));
                }
            }
            searchCountry_tableView.refresh();
        } catch (IOException e) {
            showAlert("Lỗi đọc file Excel", e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private void saveToDatabase() {
        countryService.saveCountries(countryList);
        loadDataFromDatabase(); // Cập nhật lại danh sách sau khi lưu
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
