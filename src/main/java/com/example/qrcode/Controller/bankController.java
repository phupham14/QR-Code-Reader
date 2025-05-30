package com.example.qrcode.Controller;

import com.example.qrcode.Model.Bank;
import com.example.qrcode.Service.bankService;
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

public class bankController {
    @FXML
    private TableView<Bank> searchBank_tableView;
    @FXML
    private TableColumn<Bank, String> searchBank_tableViewBankname;
    @FXML
    private TableColumn<Bank, String> searchBank_tableViewCode;
    @FXML
    private Button searchBank_uploacsvBtn;
    @FXML
    private Button searchBank_savetoDB;
    @FXML
    private Button searchBank_searchBtn;
    @FXML
    private Button searchBank_deleteDB;
    @FXML
    private TextField searchBank_textfield;

    private final ObservableList<Bank> bankList = FXCollections.observableArrayList();
    private final bankService bankService = new bankService(); // Gọi BankService

    @FXML
    public void initialize() {
        searchBank_tableViewBankname.setCellValueFactory(cellData -> cellData.getValue().bankNameProperty());
        searchBank_tableViewCode.setCellValueFactory(cellData -> cellData.getValue().bankCodeProperty());
        searchBank_tableView.setItems(bankList);

        searchBank_uploacsvBtn.setOnAction(event -> handleUploadFile());
        searchBank_savetoDB.setOnAction(event -> saveToDatabase());
        searchBank_searchBtn.setOnAction(event -> searchFromDatabase()); // Thêm sự kiện tìm kiếm
        searchBank_deleteDB.setOnAction(event -> deleteAllBanks());

        // Gọi phương thức loadDataFromDatabase() để hiển thị dữ liệu khi mở ứng dụng
        loadDataFromDatabase();
    }

    @FXML
    private void handleUploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File file = fileChooser.showOpenDialog(searchBank_uploacsvBtn.getScene().getWindow());

        if (file != null) {
            readExcel(file);
        } else {
            showAlert("Lỗi", "Vui lòng chọn một file Excel (.xlsx) hợp lệ!");
        }
    }

    @FXML
    private void searchFromDatabase() {
        String keyword = searchBank_textfield.getText().trim();
        if (keyword.isEmpty()) {
            showAlert("Thông báo", "Vui lòng nhập từ khóa tìm kiếm!");
            return;
        }

        ObservableList<Bank> searchResults = bankService.searchBanks(keyword);
        bankList.setAll(searchResults);
    }

    @FXML
    private void deleteAllBanks() {
        // Hiển thị hộp thoại xác nhận trước khi xóa
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Bạn có chắc chắn muốn xóa toàn bộ danh sách ngân hàng không? Hành động này không thể hoàn tác!");

        if (confirmDialog.showAndWait().get() == ButtonType.OK) {
            bankService.deleteAllBanks();  // Gọi service để xóa dữ liệu
            loadDataFromDatabase();  // Cập nhật lại danh sách sau khi xóa
        }
    }

    private void loadDataFromDatabase() {
        ObservableList<Bank> storedBanks = bankService.getAllBanks();
        bankList.setAll(storedBanks);
    }

    private void readExcel(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int firstRowIndex = sheet.getFirstRowNum() + 1; // Bỏ qua dòng tiêu đề

            for (int i = firstRowIndex; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String bankName = getCellValueAsString(row.getCell(0));
                String bankCode = getCellValueAsString(row.getCell(1));

                if (!bankName.isEmpty() && !bankCode.isEmpty()) {
                    bankList.add(new Bank(bankName, bankCode));
                }
            }

            searchBank_tableView.refresh();

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
        bankService.saveBanks(bankList);
        loadDataFromDatabase(); // Cập nhật danh sách sau khi lưu
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
