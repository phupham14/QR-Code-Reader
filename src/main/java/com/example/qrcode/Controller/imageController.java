package com.example.qrcode.Controller;

import com.example.qrcode.Model.QRDetail;
import com.example.qrcode.Service.QRDetailService;
import com.example.qrcode.Service.QRTextService;
import com.google.zxing.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.scene.control.TextArea;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.*;

public class imageController {
    private cameraController cameraController;

    @FXML
    private ImageView imageView;

    @FXML
    private TextArea textArea;

    @FXML private static TextField txtPayloadFormatIndicator;
    @FXML private static TextField txtPointOfInitiationMethod;
    @FXML private static TextField txtGlobalUniqueIdentifier;
    @FXML private static TextField txtAcquirerId;
    @FXML private static TextField txtMerchantId;
    @FXML private static TextField txtTransactionCurrency;
    @FXML private static TextField txtTransactionAmount;
    @FXML private static TextField txtBillNumber;
    @FXML private static TextField txtStoreLabel;
    @FXML private TextField txtMobileNumber;
    @FXML private TextField txtCrc;

    private VideoCapture capture;
    private boolean isCameraActive = false;
    private QRDetailService qrDetailService;

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

    public void updateImageView(String imagePath) {
        File file = new File(imagePath);
        if (file.exists()) {
            Image qrImage = new Image(file.toURI().toString());
            Platform.runLater(() -> imageView.setImage(qrImage));
        } else {
            System.out.println("Không tìm thấy file: " + imagePath);
        }
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
            String qrContent = String.valueOf(isQRCode(selectedFile));
            if (qrContent != null) {
                Image image = new Image(selectedFile.toURI().toString());
                imageView.setImage(image); // Hiển thị ảnh trên ImageView
                textArea.setText(qrContent); // Hiển thị nội dung QR code

                // Lưu vào database với type = 0 (file)
                //QRTextService.saveQRCodeRaw(qrContent, "filetype");
            } else {
                showAlert("Lỗi đọc ảnh", "Không thể đọc ảnh. Vui lòng thử lại.");
            }
        }
    }

    // Khởi tạo Camera
    @FXML
    public void startCamera() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/qrcode/camera.fxml"));
            Parent root = loader.load();

            // Tạo cửa sổ mới hiển thị Camera
            Stage stage = new Stage();
            stage.setTitle("Camera");
            stage.setScene(new Scene(root));
            stage.show();

            // Lấy cameraController và khởi động camera
            cameraController cameraCtrl = loader.getController();
            cameraCtrl.setTextArea(textArea);
            cameraCtrl.setOnQRCodeScanned(qrContent -> {
                if (qrContent != null && !qrContent.isEmpty()) {
                    textArea.setText(qrContent); // Hiển thị QR code trên TextArea

                    // In nội dung QR Code Raw ra console
                    System.out.println("QR Code Raw Content: " + qrContent);

                    // Lưu dữ liệu QR Raw vào database
                    QRTextService.saveQRCodeRaw(qrContent, "cameratype");

                    // Phân tích QR và lưu vào QRDetail
                    parseEMVQRCode(qrContent);
                } else {
                    System.out.println("Lỗi: Không thể đọc mã QR.");
                    showAlert("Lỗi", "Không thể đọc mã QR.");
                }
            });

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
        TAG_NAMES.put("58", "Mã quốc gia");
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
    public static String parseEMVQRCode(String qrData) {
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

        // Chuyển dữ liệu parsedData vào QRDetail
        QRDetail qrDetail = new QRDetail();
        qrDetail.setPayloadFormatIndicator(parsedData.getOrDefault("Phiên bản dữ liệu", "null"));
        qrDetail.setPointOfInitiationMethod(parsedData.getOrDefault("Phương thức khởi tạo", "null"));
        qrDetail.setGlobalUniqueIdentifier(parsedData.getOrDefault("Định danh tổ chức", "null"));
        qrDetail.setAcquirerId(parsedData.getOrDefault("Ngân hàng thụ hưởng", "null"));
        qrDetail.setMerchantId(parsedData.getOrDefault("Tài khoản thụ hưởng", "null"));
        qrDetail.setServiceCode(parsedData.getOrDefault("serviceCode", "null"));
        qrDetail.setTransactionCurrency(parsedData.getOrDefault("Mã tiền tệ", "null"));
        qrDetail.setTransactionAmount(parsedData.getOrDefault("Số tiền giao dịch", "null"));
        qrDetail.setBillNumber(parsedData.getOrDefault("Số hóa đơn", "null"));
        qrDetail.setStoreLabel(parsedData.getOrDefault("Tên DVCNTT", "null"));
        qrDetail.setTerminalLabel(parsedData.getOrDefault("terminalNumber", "null"));
        qrDetail.setMobileNumber(parsedData.getOrDefault("Số điện thoại di động", "null"));
        qrDetail.setCrc(parsedData.getOrDefault("CRC", "null"));

        // Danh sách thứ tự khóa mong muốn
        List<String> keyOrder = Arrays.asList(
                "Phiên bản dữ liệu",
                "Phương thức khởi tạo",
                "Định danh tổ chức",
                "Ngân hàng thụ hưởng",
                "Tài khoản thụ hưởng",
                "Mã danh mục",
                "Mã tiền tệ",
                "Số tiền giao dịch",
                "Mã quốc gia",
                "Tên DVCNTT",
                "Merchant City",
                "Số hóa đơn",
                "Số điện thoại di động",
                "CRC"
        );

        // Sắp xếp lại kết quả theo thứ tự mong muốn
        StringBuilder result = new StringBuilder();
        Map<String, String> sortedData = new LinkedHashMap<>();
        for (String key : keyOrder) {
            sortedData.put(key, parsedData.getOrDefault(key, "null"));
            if (parsedData.containsKey(key)) {
                result.append(key).append(": ").append(parsedData.get(key)).append("\n");
            }
        }

        // Lưu vào bảng QRDetail
        QRDetailService.saveQRDetail(qrDetail);

        return result.toString();
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

                // Debug
                System.out.println("Tag: " + tag + ", Length: " + length + ", Value: " + value);

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

    private String isQRCode(File file) {
        String type = null;
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);

            if (result == null) {
                showAlert("Không phải QR Code", "Ảnh bạn chọn không chứa mã QR hợp lệ.");
                return null;
            }

            // Lấy nội dung QR code raw (chuỗi EMV thô)
            String qrRawContent = result.getText().trim();
            System.out.println("QR Code Raw Content: " + qrRawContent); // Debug

            // Lưu QR Code raw vào PostgreSQL
            QRTextService.saveQRCodeRaw(qrRawContent, "filetype");

            // Trả về dữ liệu đã phân tích
            return parseEMVQRCode(qrRawContent);

        } catch (IOException | NotFoundException e) {
            showAlert("Không phải QR Code", "Ảnh bạn chọn không chứa mã QR hợp lệ.");
        }
        return null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}