package com.example.qrcode.Controller;

import com.example.qrcode.Model.Currency;
import com.example.qrcode.Service.currencyService;
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

public class currencyController {
    @FXML
    private TableView<Currency> searchCurrency_tableView;
    @FXML
    private TableColumn<Currency, String> searchCurrency_tableViewCurrency;
    @FXML
    private TableColumn<Currency, String> searchCurrency_tableViewCountry;
    @FXML
    private TableColumn<Currency, String> searchCurrency_number;
    @FXML
    private TableColumn<Currency, String> searchCurrency_tableViewCode;

    @FXML
    private TextField searchCurrency_textfield, searchCurrency_name, searchCurrency_numeric1;

    @FXML
    private Button searchCurrency_searchBtn, searchCurrency_uploacsvBtn, searchCurrency_Savetodb, searchCurrency_deleteDB;

    private final ObservableList<Currency> currencyList = FXCollections.observableArrayList();
    private final currencyService currencyService = new currencyService();

    @FXML
    public void initialize() {
        searchCurrency_tableViewCountry.setCellValueFactory(cellData -> cellData.getValue().countryNameProperty());
        searchCurrency_tableViewCurrency.setCellValueFactory(cellData -> cellData.getValue().currencyNameProperty());
        searchCurrency_tableViewCode.setCellValueFactory(cellData -> cellData.getValue().currencyCodeProperty());
        searchCurrency_number.setCellValueFactory(cellData -> cellData.getValue().currencyNumberProperty());

        searchCurrency_tableView.setItems(currencyList);

        searchCurrency_searchBtn.setOnAction(event -> searchFromDatabase());
        searchCurrency_uploacsvBtn.setOnAction(event -> handleUploadFile());
        searchCurrency_Savetodb.setOnAction(event -> saveToDatabase());
        searchCurrency_deleteDB.setOnAction(event -> deleteAllCurrencies());

        // Gọi phương thức loadDataFromDatabase() để hiển thị dữ liệu khi mở ứng dụng
        loadDataFromDatabase();
    }

    @FXML
    private void searchFromDatabase() {
        String keyword = searchCurrency_textfield.getText().trim();
        if (keyword.isEmpty()) {
            showAlert("Thông báo", "Vui lòng nhập từ khóa tìm kiếm!");
            return;
        }
        ObservableList<Currency> searchResults = currencyService.searchCurrencies(keyword);
        currencyList.setAll(searchResults);
    }

    @FXML
    private void handleUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showOpenDialog(searchCurrency_uploacsvBtn.getScene().getWindow());

        if (file != null) {
            readExcel(file);
        } else {
            showAlert("Lỗi", "Vui lòng chọn một file Excel (.xlsx) hợp lệ!");
        }
    }

    private void loadDataFromDatabase() {
        ObservableList<Currency> storedCurrencies = currencyService.getAllCurrencies();
        currencyList.setAll(storedCurrencies);
    }

    private void readExcel(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int firstRowIndex = sheet.getFirstRowNum() + 1;

            for (int i = firstRowIndex; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String countryName = getCellValueAsString(row.getCell(0));
                String currencyName = getCellValueAsString(row.getCell(1));
                String currencyCode = getCellValueAsString(row.getCell(2));
                String currencyNumber = getCellValueAsString(row.getCell(3));

                if (!currencyName.isEmpty() && !countryName.isEmpty() && !currencyCode.isEmpty() && !currencyNumber.isEmpty()) {
                    currencyList.add(new Currency(currencyName, countryName, currencyCode, currencyNumber));
                }
            }
            searchCurrency_tableView.refresh();
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
        currencyService.saveCurrencies(currencyList);
        loadDataFromDatabase(); // Cập nhật lại danh sách sau khi lưu
    }

    @FXML
    private void addCurrency() {
        String countryName = searchCurrency_name.getText().trim();
        String currencyName = searchCurrency_textfield.getText().trim();
        String currencyCode = searchCurrency_tableViewCode.getText().trim();
        String currencyNumber = searchCurrency_numeric1.getText().trim();

        if (currencyName.isEmpty() || countryName.isEmpty() || currencyCode.isEmpty() || currencyNumber.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        Currency newCurrency = new Currency(countryName, currencyName, currencyCode, currencyNumber);
        currencyList.add(newCurrency);
        searchCurrency_tableView.refresh();
    }

    @FXML
    private void deleteAllCurrencies() {
        // Hiển thị hộp thoại xác nhận trước khi xóa
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Bạn có chắc chắn muốn xóa toàn bộ dữ liệu không? Hành động này không thể hoàn tác!");

        if (confirmDialog.showAndWait().get() == ButtonType.OK) {
            currencyService.deleteAllCurrencies();  // Gọi service để xóa dữ liệu
            loadDataFromDatabase();  // Cập nhật lại danh sách sau khi xóa
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
