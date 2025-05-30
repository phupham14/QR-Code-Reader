package com.example.qrcode.Controller;

import com.example.qrcode.Model.QRDetail;
import com.example.qrcode.Service.*;
import com.google.zxing.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.stage.Stage;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class imageController {
    private cameraController cameraController;
    private VideoCapture capture;
    private boolean isCameraActive = false;
    private QRDetailService qrDetailService;

    @FXML
    private ImageView imageView;

    @FXML
    private TextField txtDataVersion;    // payloadFormatIndicator
    @FXML
    private TextField txtMethod;         // pointOfInitiationMethod
    @FXML
    private TextField txtOrgID;          // globalUniqueIdentifier
    @FXML
    private TextField txtBank;           // acquirerId
    @FXML
    private TextField txtAccount;        // merchantId
    @FXML
    private TextField txtCategoryCode;   // serviceCode
    @FXML
    private TextField txtCurrency;       // transactionCurrency
    @FXML
    private TextField txtAmount;         // transactionAmount
    @FXML
    private TextField txtInvoice;        // billNumber
    @FXML
    private TextField txtMerchantName;   // storeLabel
    @FXML
    private TextField txtMerchantCity;   // terminalLabel
    @FXML
    private TextField txtPhone;          // mobileNumber
    @FXML
    private TextField txtCRC;            // crc

    // Singleton để lưu controller hiện tại
    private static imageController instance;

    public imageController() {
        instance = this;
    }

    public static imageController getInstance() {
        return instance;
    }

    public imageController(Connection connection) {
        this.qrDetailService = new QRDetailService(connection);
    }

    public void displayParsedData(Map<String, String> parsedData) {
        txtDataVersion.setText(parsedData.getOrDefault("Phiên bản dữ liệu", "null"));
        txtMethod.setText(parsedData.getOrDefault("Phương thức khởi tạo", "null"));
        txtOrgID.setText(parsedData.getOrDefault("Định danh tổ chức", "null"));
        txtBank.setText(parsedData.getOrDefault("Ngân hàng thụ hưởng", "null"));
        txtAccount.setText(parsedData.getOrDefault("Tài khoản thụ hưởng", "null"));
        txtCategoryCode.setText(parsedData.getOrDefault("Mã danh mục", "null"));
        txtCurrency.setText(parsedData.getOrDefault("Mã tiền tệ", "null"));
        txtAmount.setText(parsedData.getOrDefault("Số tiền giao dịch", "null"));
        txtMerchantName.setText(parsedData.getOrDefault("Tên DVCNTT", "null"));
        txtMerchantCity.setText(parsedData.getOrDefault("Merchant City", "null"));
        txtInvoice.setText(parsedData.getOrDefault("Số hóa đơn", "null"));
        txtPhone.setText(parsedData.getOrDefault("Số điện thoại di động", "null"));
        txtCRC.setText(parsedData.getOrDefault("CRC", "null"));
    }

        private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Lấy Stage từ MenuItem bằng cách truy xuất Scene
            Stage stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();

            // Tạo cửa sổ (Stage) mới
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            //newStage.setTitle("New Window"); // Đặt tiêu đề cửa sổ mới
            newStage.show();
        }


        @FXML
        private void goToBank(ActionEvent event) throws IOException {
            switchScene(event, "/com/example/qrcode/bank.fxml");
        }

        @FXML
        private void goToCountry(ActionEvent event) throws IOException {
            switchScene(event, "/com/example/qrcode/country.fxml");
        }

        @FXML
        private void goToCurrency(ActionEvent event) throws IOException {
            switchScene(event, "/com/example/qrcode/currency.fxml");
        }

        @FXML
        private void goToTransaction(ActionEvent event) throws IOException {
            switchScene(event, "/com/example/qrcode/transaction.fxml");
        }

        @FXML
        private void goToAccount(ActionEvent event) throws IOException {
            switchScene(event, "/com/example/qrcode/accounts.fxml");
        }

        @FXML
        private void goToExtraction(ActionEvent event) throws IOException {
            switchScene(event, "/com/example/qrcode/log.fxml");
        }

    public void updateImageView(String imagePath) {
        File file = new File(imagePath);
        if (file.exists()) {
            Image qrImage = new Image(file.toURI().toString());
            Platform.runLater(() -> imageView.setImage(qrImage));
        } else {
            System.out.println("Không tìm thấy file: " + imagePath);
        }
    }

    public void processQRCode(String qrContent, String sourceType) {
        if (qrContent != null && !qrContent.isEmpty()) {
            System.out.println("QR Content from " + sourceType + ": " + qrContent);

            // Lưu QR Raw vào database
            QRTextService.saveQRCodeRaw(qrContent, sourceType);

            // Phân tích mã QR
            Map<String, String> parsedData = parseEMVQRCode(qrContent);

            // Hiển thị dữ liệu trong các TextField
            displayParsedData(parsedData);
        } else {
            showAlert("Lỗi", "Không thể đọc mã QR.");
        }
    }

    private String extractQRCodeFromImage(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);

            if (result != null) {
                return result.getText().trim();
            }
        } catch (IOException | NotFoundException e) {
            System.out.println("Lỗi đọc QR từ ảnh: " + e.getMessage());
        }
        return null;
    }


    // Upload ảnh từ trên máy
    @FXML
    public void uploadImage(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

        if (selectedFile != null) {
            String qrContent = extractQRCodeFromImage(selectedFile);
            if (qrContent != null) {
                Image image = new Image(selectedFile.toURI().toString());
                imageView.setImage(image);
                processQRCode(qrContent, "file");
            } else {
                showAlert("Lỗi đọc ảnh", "Không thể đọc ảnh. Vui lòng thử lại.");
            }
        }
    }

    // Sử dụng camera
    @FXML
    public void startCamera() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/qrcode/camera.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Camera");
            stage.setScene(new Scene(root));
            stage.show();

            cameraController cameraCtrl = loader.getController();
            cameraCtrl.setOnQRCodeScanned(qrContent -> processQRCode(qrContent, "camera"));

            cameraCtrl.startCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static final Map<String, String> TAG_NAMES = new HashMap<>();
    private static final Map<String, String> ACCOUNT_INFO_TAGS = new HashMap<>();
    private static final Map<String, String> ACCOUNT_INFO_TAGS2 = new HashMap<>();

    static {
        TAG_NAMES.put("00", "Phiên bản dữ liệu");
        TAG_NAMES.put("01", "Phương thức khởi tạo");
        TAG_NAMES.put("38", "Định danh tổ chức");
        TAG_NAMES.put("52", "Mã danh mục");
        TAG_NAMES.put("58", "Tên quốc gia");
        TAG_NAMES.put("59", "Tên DVCNTT");
        TAG_NAMES.put("60", "Merchant City");
        TAG_NAMES.put("53", "Mã tiền tệ");
        TAG_NAMES.put("54", "Số tiền giao dịch");
        TAG_NAMES.put("50", "Ngân hàng thụ hưởng");
        TAG_NAMES.put("80", "Tài khoản thụ hưởng");
        TAG_NAMES.put("62", "Dữ liệu bổ sung");
        TAG_NAMES.put("63", "CRC");

        ACCOUNT_INFO_TAGS.put("00", "globalUniqueIdentifier");
        ACCOUNT_INFO_TAGS.put("01", "acquirerId");
        ACCOUNT_INFO_TAGS.put("02", "merchantId");
        ACCOUNT_INFO_TAGS.put("03", "serviceCode");

        ACCOUNT_INFO_TAGS2.put("01", "Số hóa đơn");
        ACCOUNT_INFO_TAGS2.put("02", "Số điện thoại di động");
        ACCOUNT_INFO_TAGS2.put("03", "storeLabel");
        ACCOUNT_INFO_TAGS2.put("07", "terminalNumber");
    }

    // Xử lý các tag chính
    public  Map<String, String> parseEMVQRCode(String qrData) {
        Map<String, String> parsedData = new HashMap<>();
        int index = 0;

        while (index < qrData.length()) {
            try {
                if (index + 4 > qrData.length()) break;

                String tag = qrData.substring(index, index + 2);
                index += 2;

                int length = Integer.parseInt(qrData.substring(index, index + 2));
                index += 2;

                if (index + length > qrData.length()) break;

                String value = qrData.substring(index, index + length);
                index += length;

                String fieldName = TAG_NAMES.getOrDefault(tag, "unknownField");

                if (tag.equals("38")) {
                    Map<String, String> accountInfo = parseAccountInformation38(value);
                    parsedData.put("Định danh tổ chức", accountInfo.getOrDefault("globalUniqueIdentifier", "null"));
                    parsedData.put("Ngân hàng thụ hưởng", accountInfo.getOrDefault("acquirerId", "null"));
                    parsedData.put("Tài khoản thụ hưởng", accountInfo.getOrDefault("merchantId", "null"));
                } else if (tag.equals("62")) {
                    Map<String, String> additionalData = parseAccountInformation62(value);
                    parsedData.put("Số hóa đơn", additionalData.getOrDefault("billNumber", "null"));
                    parsedData.put("Số điện thoại di động", additionalData.getOrDefault("mobileNumber", "null"));
                } else {
                    parsedData.put(fieldName, value.isEmpty() ? "null" : value);
                }
            } catch (Exception e) {
                parsedData.put("error", "Lỗi khi phân tích EMV: " + e.getMessage());
                break;
            }
        }

        // Lấy thông tin ngân hàng, loại tiền và quốc gia
        String bankCode = parsedData.getOrDefault("Ngân hàng thụ hưởng", "null");
        String currencyCode = parsedData.getOrDefault("Mã tiền tệ", "null");
        String countryCode = parsedData.getOrDefault("Mã quốc gia", "null");

        String bankName = bankService.getBankNameByCode(bankCode);
        String currencyName = currencyService.getCurrencyByNumber(currencyCode);
        String countryName = countryService.getCountryNameByCode(countryCode);

        parsedData.put("Ngân hàng thụ hưởng", bankName);
        parsedData.put("Mã tiền tệ", currencyName);
        parsedData.put("Mã quốc gia", countryName);

        // Lưu vào database
        saveQRDetail(parsedData);

        // Hiển thị dữ liệu lên bảng
        displayParsedData(parsedData);
        return parsedData;
    }

    // Lưu dữ liệu vào database
    private static void saveQRDetail(Map<String, String> parsedData) {
        QRDetail qrDetail = new QRDetail();
        qrDetail.setPayloadFormatIndicator(parsedData.getOrDefault("Phiên bản dữ liệu", "null"));
        qrDetail.setPointOfInitiationMethod(parsedData.getOrDefault("Phương thức khởi tạo", "null"));
        qrDetail.setGlobalUniqueIdentifier(parsedData.getOrDefault("Định danh tổ chức", "null"));
        qrDetail.setAcquirerId(parsedData.getOrDefault("Ngân hàng thụ hưởng", "null"));
        qrDetail.setMerchantId(parsedData.getOrDefault("Tài khoản thụ hưởng", "null"));
        qrDetail.setTransactionCurrency(parsedData.getOrDefault("Mã tiền tệ", "null"));
        qrDetail.setTransactionAmount(parsedData.getOrDefault("Số tiền giao dịch", "null"));
        qrDetail.setBillNumber(parsedData.getOrDefault("Số hóa đơn", "null"));
        qrDetail.setStoreLabel(parsedData.getOrDefault("Tên DVCNTT", "null"));
        qrDetail.setMobileNumber(parsedData.getOrDefault("Số điện thoại di động", "null"));
        qrDetail.setCrc(parsedData.getOrDefault("CRC", "null"));

        QRDetailService.saveQRDetail(qrDetail);
    }

    // Xử lý subtag cho ID = 38
    private static Map<String, String> parseAccountInformation38(String data) {
        Map<String, String> result = new HashMap<>();
        int index = 0;

        while (index < data.length()) {
            try {
                if (index + 4 > data.length()) break;

                String tag = data.substring(index, index + 2);
                index += 2;

                int length = Integer.parseInt(data.substring(index, index + 2));
                index += 2;

                if (index + length > data.length()) break;

                String value = data.substring(index, index + length);
                index += length;

                // Xử lý subtag cho 38
                switch (tag) {
                    case "00":
                        result.put("globalUniqueIdentifier", value);
                        break;
                    case "01":
                        if (value.length() >= 14) {
                            result.put("acquirerId", value.substring(4, 10)); // Lấy 6 ký tự đầu tiên
                            result.put("merchantId", value.substring(14)); // Phần còn lại là Merchant ID
                        }
                        break;
                    case "02":
                        result.put("serviceCode", value);
                        break;
                    default:
                        result.put("unknownTag_" + tag, value);
                        break;
                }
            } catch (Exception e) {
                result.put("error", "Lỗi khi xử lý ID 38: " + e.getMessage());
                break;
            }
        }

        return result;
    }

    // Xử lý cho ID = 62
    public static Map<String, String> parseAccountInformation62(String data) {
        Map<String, String> result = new HashMap<>();
        int index = 0;

        while (index < data.length()) {
            try {
                if (index + 2 > data.length()) break; // Đảm bảo có đủ ký tự đọc tag

                String tag = data.substring(index, index + 2);
                index += 2;

                // Kiểm tra nếu index tiếp theo là số, nếu không thì lỗi
                if (index >= data.length() || !Character.isDigit(data.charAt(index))) {
                    result.put("error", "Lỗi đọc độ dài tại tag: " + tag);
                    break;
                }

                // Cố gắng lấy 2 ký tự, nhưng nếu chỉ có 1 ký tự thì vẫn chấp nhận
                int lengthEndIndex = (index + 2 <= data.length()) ? index + 2 : index + 1;
                int length = Integer.parseInt(data.substring(index, lengthEndIndex));

                index = lengthEndIndex; // Di chuyển index sau khi đọc xong độ dài

                // Kiểm tra nếu độ dài vượt quá giới hạn dữ liệu
                if (index + length > data.length()) {
                    result.put("error", "Độ dài không hợp lệ tại tag: " + tag);
                    break;
                }

                String value = data.substring(index, index + length);
                index += length;

                // Xử lý các subtag
                switch (tag) {
                    case "01":
                        result.put("billNumber", value);
                        break;
                    case "02":
                        result.put("mobileNumber", value);
                        break;
                    case "03":
                        result.put("storeLabel", value);
                        break;
                    case "04":
                        result.put("terminalNumber", value);
                        break;
                    default:
                        result.put("unknownTag_" + tag, value);
                        break;
                }
            } catch (Exception e) {
                result.put("error", "Lỗi khi xử lý ID 62: " + e.getMessage());
                break;
            }
        }

        return result;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}