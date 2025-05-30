package com.example.qrcode.Controller;

import com.example.qrcode.Model.Transaction;
import com.example.qrcode.Service.LogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.LocalDateStringConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class LogController {

    @FXML
    private TableView<Transaction> accountTable;

    @FXML
    private TableColumn<Transaction, String> senderAccountCol;

    @FXML
    private TableColumn<Transaction, String> receiverAccountCol;

    @FXML
    private TableColumn<Transaction, Double> amountCol;

    @FXML
    private TableColumn<Transaction, Timestamp> timeCol;

    @FXML
    private TableColumn<Transaction, String> descriptionCol;

    @FXML
    private TableColumn<Transaction, String> debitOrCreditCol;

    @FXML
    private TextField senderAccount;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Button searchButtom;

    @FXML
    private Button excelButton;

    @FXML
    private Pagination pagination;

    private final ObservableList<Transaction> allTransactions = FXCollections.observableArrayList();
    private final LogService logService = new LogService();
    private static final int ROWS_PER_PAGE = 10;
    private LocalDate currentFromDate;
    private LocalDate currentToDate;

    @FXML
    public void initialize() {
        // Định dạng dd/MM/yyyy
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Cài đặt converter cho các DatePicker
        fromDatePicker.setConverter(new LocalDateStringConverter(formatter, formatter));
        toDatePicker.setConverter(new LocalDateStringConverter(formatter, formatter));

        // Khởi tạo cột TableView
        senderAccountCol.setCellValueFactory(cellData -> cellData.getValue().senderAccountProperty());
        receiverAccountCol.setCellValueFactory(cellData -> cellData.getValue().receiverAccountProperty());
        amountCol.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        timeCol.setCellValueFactory(cellData -> cellData.getValue().transactionTimeProperty());
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        debitOrCreditCol.setCellValueFactory(cellData -> cellData.getValue().debitOrCredit());

        searchButtom.setOnAction(this::handleSearch);
        excelButton.setOnAction(this::handleExportExcel);

        pagination.setPageFactory(this::createPage);
    }

    private void handleSearch(ActionEvent event) {
        String senderAcc = senderAccount.getText();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        // Kiểm tra null và điều kiện ngày
        if (fromDate == null || toDate == null) {
            showAlert("Lỗi nhập liệu", "Vui lòng chọn cả ngày bắt đầu và ngày kết thúc.");
            return;
        }

        if (senderAcc == null || senderAcc.trim().isEmpty()) {
            showAlert("Lỗi nhập liệu", "Số tài khoản không được để trống");
            return;
        }

        // Kiểm tra tài khoản tồn tại
        if (!LogService.isValidAccount(senderAcc)) {
            showAlert("Lỗi dữ liệu", "Số tài khoản không tồn tại trong hệ thống.");
            return;
        }

        if (toDate.isBefore(fromDate)) {
            showAlert("Lỗi nhập liệu", "Ngày kết thúc không được nhỏ hơn ngày bắt đầu.");
            return;
        }

        // Lưu lại cho export sau
        currentFromDate = fromDate;
        currentToDate = toDate;

        allTransactions.clear();
        allTransactions.addAll(logService.fetchTransactions(senderAcc, fromDate, toDate));
        pagination.setPageCount((int) Math.ceil(allTransactions.size() * 1.0 / ROWS_PER_PAGE));
        pagination.setCurrentPageIndex(0);
        accountTable.setItems(FXCollections.observableArrayList(allTransactions.subList(0, Math.min(ROWS_PER_PAGE, allTransactions.size()))));
    }

    private void handleExportExcel(ActionEvent event) {
        String senderAcc = senderAccount.getText(); // Lấy số tài khoản từ giao diện

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        Stage stage = (Stage) excelButton.getScene().getWindow();
        File saveFile = fileChooser.showSaveDialog(stage);

        if (saveFile != null) {
            try (FileInputStream templateInput = new FileInputStream("D:/statement_template.xlsx");
                 Workbook workbook = new XSSFWorkbook(templateInput)) {

                Sheet sheet = workbook.getSheetAt(0);
                int maxTemplateRowIndex = 14; // Vị trí dòng chứa Teller & Supervisor trong template
                int startRow = 11;
                int transactionRowLimit = maxTemplateRowIndex - startRow; // Số dòng trống để ghi giao dịch

                // Truy vấn CSDL để lấy tên người dùng
                String accountHolder = LogService.fetchAccountHolder(senderAcc);

                // Ghi tên người dùng vào ô B3
                Row row3 = sheet.getRow(2); // Dòng 3 -> index 2
                if (row3 == null) row3 = sheet.createRow(2);
                Cell cellB3 = row3.getCell(1); // Cột B -> index 1
                if (cellB3 == null) cellB3 = row3.createCell(1);
                cellB3.setCellValue(accountHolder);

                // Ghi số tài khoản vào ô B4
                Row row4 = sheet.getRow(3); // Dòng 4 -> index 3
                if (row4 == null) row4 = sheet.createRow(3);
                Cell cellB4 = row4.getCell(1); // Cột B -> index 1
                if (cellB4 == null) cellB4 = row4.createCell(1);
                cellB4.setCellValue(senderAcc);

                // Set dd/mm/yy
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                // B5 - From date
                Row row5 = sheet.getRow(4);
                if (row5 == null) row5 = sheet.createRow(4);
                Cell cellB5 = row5.getCell(1);
                if (cellB5 == null) cellB5 = row5.createCell(1);
                cellB5.setCellValue(currentFromDate.format(formatter));

                // B6 - To date
                Row row6 = sheet.getRow(5);
                if (row6 == null) row6 = sheet.createRow(5);
                Cell cellB6 = row6.getCell(1);
                if (cellB6 == null) cellB6 = row6.createCell(1);
                cellB6.setCellValue(currentToDate.format(formatter));

                // Nếu số giao dịch vượt quá vùng trống, thì chèn thêm dòng để đẩy phần footer xuống
                if (allTransactions.size() > transactionRowLimit) {
                    int rowsToInsert = allTransactions.size() - transactionRowLimit + 1; // +1 để cách ra

                    sheet.shiftRows(maxTemplateRowIndex, sheet.getLastRowNum(), rowsToInsert); // Dịch các dòng phía dưới xuống
                    maxTemplateRowIndex += rowsToInsert; // Cập nhật vị trí mới của dòng Teller
                }

                // Lấy dòng mẫu để sao chép định dạng
                Row templateRow = sheet.getRow(11); // dòng 12 trong Excel, index = 11

                // Tạo style viền mặc định (phòng trường hợp không có dòng template)
                CellStyle borderedStyle = workbook.createCellStyle();
                borderedStyle.setBorderTop(BorderStyle.THIN);
                borderedStyle.setBorderBottom(BorderStyle.THIN);
                borderedStyle.setBorderLeft(BorderStyle.THIN);
                borderedStyle.setBorderRight(BorderStyle.THIN);
                borderedStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                // Tạo style định dạng số có phân cách hàng nghìn
                CellStyle moneyStyle = workbook.createCellStyle();
                DataFormat format = workbook.createDataFormat();
                moneyStyle.setDataFormat(format.getFormat("#,##0")); // hoặc "#,##0 \"VNĐ\"" nếu muốn thêm VNĐ
                moneyStyle.setBorderTop(BorderStyle.THIN);
                moneyStyle.setBorderBottom(BorderStyle.THIN);
                moneyStyle.setBorderLeft(BorderStyle.THIN);
                moneyStyle.setBorderRight(BorderStyle.THIN);
                moneyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                moneyStyle.setAlignment(HorizontalAlignment.RIGHT);

                // Style định dạng số + màu đỏ (debit)
                CellStyle debitStyle = workbook.createCellStyle();
                debitStyle.setDataFormat(format.getFormat("#,##0"));
                debitStyle.setBorderTop(BorderStyle.THIN);
                debitStyle.setBorderBottom(BorderStyle.THIN);
                debitStyle.setBorderLeft(BorderStyle.THIN);
                debitStyle.setBorderRight(BorderStyle.THIN);
                debitStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                debitStyle.setAlignment(HorizontalAlignment.RIGHT);
                Font redFont = workbook.createFont();
                redFont.setColor(IndexedColors.RED.getIndex());
                redFont.setBold(true);
                debitStyle.setFont(redFont);

                // Style định dạng số + màu xanh lá (credit)
                CellStyle creditStyle = workbook.createCellStyle();
                creditStyle.setDataFormat(format.getFormat("#,##0"));
                creditStyle.setBorderTop(BorderStyle.THIN);
                creditStyle.setBorderBottom(BorderStyle.THIN);
                creditStyle.setBorderLeft(BorderStyle.THIN);
                creditStyle.setBorderRight(BorderStyle.THIN);
                creditStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                creditStyle.setAlignment(HorizontalAlignment.RIGHT);
                Font greenFont = workbook.createFont();
                greenFont.setColor(IndexedColors.GREEN.getIndex());
                greenFont.setBold(true);
                creditStyle.setFont(greenFont);

                int stt = 1;
                for (Transaction tr : allTransactions) {
                    Row row = sheet.createRow(startRow++);

                    for (int col = 0; col <= 5; col++) {
                        Cell newCell = row.createCell(col);

                        if (templateRow != null && templateRow.getCell(col) != null) {
                            CellStyle originalStyle = templateRow.getCell(col).getCellStyle();
                            CellStyle clonedStyle = workbook.createCellStyle();
                            clonedStyle.cloneStyleFrom(originalStyle);

                            // Ép thêm border
                            clonedStyle.setBorderTop(BorderStyle.THIN);
                            clonedStyle.setBorderBottom(BorderStyle.THIN);
                            clonedStyle.setBorderLeft(BorderStyle.THIN);
                            clonedStyle.setBorderRight(BorderStyle.THIN);
                            clonedStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                            clonedStyle.setAlignment(HorizontalAlignment.CENTER); // tùy chọn nếu bạn muốn căn giữa luôn

                            newCell.setCellStyle(clonedStyle);
                        } else {
                            // Nếu không có style gốc thì dùng style viền mặc định
                            newCell.setCellStyle(borderedStyle);
                        }
                    }

                    // Cột A: STT
                    row.getCell(0).setCellValue(stt++);

                    // Cột B: Ngày tháng
                    Timestamp date = tr.getTransactionTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String formattedDateTime = sdf.format(date);
                    row.getCell(1).setCellValue(formattedDateTime);

                    // Cột C: Số tài khoản
                    row.getCell(2).setCellValue(senderAcc);

                    // Cột D và E: Chuyển / Nhận (áp dụng style định dạng tiền)
                    if (senderAcc.equals(tr.getSenderAccount())) {
                        Cell cellD = row.getCell(3);
                        cellD.setCellValue(tr.getAmount());
                        cellD.setCellStyle(debitStyle);

                        Cell cellE = row.getCell(4);
                        cellE.setCellValue(0);
                        cellE.setCellStyle(creditStyle);
                    } else if (senderAcc.equals(tr.getReceiverAccount())) {
                        Cell cellD = row.getCell(3);
                        cellD.setCellValue(0);
                        cellD.setCellStyle(debitStyle);

                        Cell cellE = row.getCell(4);
                        cellE.setCellValue(tr.getAmount());
                        cellE.setCellStyle(creditStyle);
                    } else {
                        row.getCell(3).setCellValue("");
                        row.getCell(4).setCellValue("");
                    }

                    // Cột F: Mô tả
                    row.getCell(5).setCellValue(tr.getDescription() != null ? tr.getDescription() : "");
                }

                // Auto-fit columns
                for (int i = 0; i <= 5; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream outputStream = new FileOutputStream(saveFile)) {
                    workbook.write(outputStream);
                }

                showAlert("Thành công", "Đã lưu file excel thành công");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private TableView<Transaction> createPage(Integer pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allTransactions.size());
        accountTable.setItems(FXCollections.observableArrayList(allTransactions.subList(fromIndex, toIndex)));
        return accountTable;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
