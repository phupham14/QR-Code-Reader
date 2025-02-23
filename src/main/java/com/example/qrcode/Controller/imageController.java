package com.example.qrcode.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

    private VideoCapture capture;
    private boolean isCameraActive = false;

    @FXML
    public void uploadImage(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(((javafx.scene.Node) event.getSource()).getScene().getWindow());

        if (selectedFile != null) {
            String qrContent = String.valueOf(isQRCode(selectedFile));
            if (qrContent != null) {
                Image image = new Image(selectedFile.toURI().toString());
                imageView.setImage(image); // Hiển thị ảnh trên ImageView
                textArea.setText(qrContent); // Hiển thị nội dung QR code
            } else {
                showAlert("Lỗi đọc ảnh", "Không thể đọc ảnh. Vui lòng thử lại.");
            }
        }
    }

    @FXML
    public void startCamera() {
        try {
            // Load camera.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/qrcode/camera.fxml"));
            Parent root = loader.load();

            // Tạo cửa sổ mới hiển thị Camera
            Stage stage = new Stage();
            stage.setTitle("Camera");
            stage.setScene(new Scene(root));
            stage.show();

            // Lấy cameraController và khởi động camera
            cameraController cameraController = loader.getController();
            cameraController.startCamera();

            cameraController cameraCtrl = loader.getController();
            cameraCtrl.setTextArea(textArea); // Truyền textArea từ imageController vào cameraController
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final Map<String, String> TAG_NAMES = new HashMap<>();
    private static final Map<String, String> ACCOUNT_INFO_TAGS = new HashMap<>();
    private static final Map<String, String> ACCOUNT_INFO_TAGS2 = new HashMap<>();

    static {
        TAG_NAMES.put("00", "payloadFormatIndicator");
        TAG_NAMES.put("01", "pointOfInitiationMethod");
        TAG_NAMES.put("38", "accountInformation");
        TAG_NAMES.put("52", "merchantCategoryCode");
        TAG_NAMES.put("58", "countryCode");
        TAG_NAMES.put("59", "merchantName");
        TAG_NAMES.put("60", "merchantCity");
        TAG_NAMES.put("53", "transactionCurrency");
        TAG_NAMES.put("54", "transactionAmount");
        TAG_NAMES.put("50", "merchantId");
        TAG_NAMES.put("80", "acquiringBank");
        TAG_NAMES.put("62", "additionalData");
        TAG_NAMES.put("63", "crc");

        // Các trường con của "accountInformation" (id = 38)
        ACCOUNT_INFO_TAGS.put("00", "globalUniqueIdentifier");
        ACCOUNT_INFO_TAGS.put("01", "acquirerId");
        ACCOUNT_INFO_TAGS.put("02", "merchantId");
        ACCOUNT_INFO_TAGS.put("03", "serviceCode");

        // Các trường con của "additionalData" (id = 62)
        ACCOUNT_INFO_TAGS2.put("01", "billNumber");
        ACCOUNT_INFO_TAGS2.put("02", "mobileNumber");
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

                if (tag.equals("38")) { // Nếu là accountInformation
                    Map<String, String> accountInfo = parseAccountInformation38(value);

                    // Đưa tất cả giá trị từ accountInfo vào parsedData
                    parsedData.put("globalUniqueIdentifier", accountInfo.getOrDefault("globalUniqueIdentifier", "null"));
                    parsedData.put("acquirerId", accountInfo.getOrDefault("acquirerId", "null"));
                    parsedData.put("merchantId", accountInfo.getOrDefault("merchantId", "null"));
                    parsedData.put("serviceCode", accountInfo.getOrDefault("serviceCode", "null"));
                } else if (tag.equals("62")) {
                    Map<String, String> accountInfo = parseAccountInformation62(value);
                    parsedData.put("billNumber", accountInfo.getOrDefault("billNumber", "null"));
                    parsedData.put("mobileNumber", accountInfo.getOrDefault("mobileNumber", "null"));
                    parsedData.put("storeLabel", accountInfo.getOrDefault("storeLabel", "null"));
                    parsedData.put("terminalNumber", accountInfo.getOrDefault("terminalNumber", "null"));
                } else {
                    parsedData.put(fieldName, value.isEmpty() ? "null" : value);
                }
            } catch (Exception e) {
                parsedData.put("error", "Lỗi khi phân tích EMV: " + e.getMessage());
                break;
            }
        }
        // Danh sách thứ tự khóa mong muốn
        List<String> keyOrder = Arrays.asList(
                "payloadFormatIndicator",
                "pointOfInitiationMethod",
                "globalUniqueIdentifier",
                "acquirerId",
                "merchantId",
                "serviceCode",
                "transactionCurrency",
                "transactionAmount",
                "billNumber",
                "storeLabel",
                "terminalLabel",
                "mobileNumber",
                "crc"
        );

        // Sắp xếp lại kết quả theo thứ tự mong muốn
        Map<String, String> sortedData = new LinkedHashMap<>();
        for (String key : keyOrder) {
            sortedData.put(key, parsedData.getOrDefault(key, "null"));
        }

        // Chuyển sang JSON với định dạng đẹp
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        return gson.toJson(sortedData);
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
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            // return result.getText(); // Trả về nội dung QR code (chuỗi EMV thô)

            // Lấy nội dung QR code (chuỗi EMV thô)
            String qrContent = result.getText();
            System.out.println("QR Code Raw Content: " + qrContent); // Debug
            // Gọi hàm parseEMVQRCode để bóc tách dữ liệu
            return parseEMVQRCode(qrContent);
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