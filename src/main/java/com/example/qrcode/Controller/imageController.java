package com.example.qrcode.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.scene.control.TextArea;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class imageController {
    @FXML
    private ImageView imageView;

    @FXML
    private TextArea textArea;

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

    private static final Map<String, String> TAG_NAMES = new HashMap<>();
    private static final Map<String, String> ACCOUNT_INFO_TAGS = new HashMap<>();
    private static final Map<String, String> ACCOUNT_INFO_TAGS2 = new HashMap<>();

    static {
        TAG_NAMES.put("00", "payloadFormatIndicator");
        TAG_NAMES.put("01", "pointOfInitiationMethod");
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
        TAG_NAMES.put("38", "accountInformation");

        // Các trường con của "accountInformation" (id = 38)
        ACCOUNT_INFO_TAGS.put("00", "globalUniqueIdentifier");
        ACCOUNT_INFO_TAGS.put("01", "acquirerId");
        ACCOUNT_INFO_TAGS.put("02", "merchantId");
        ACCOUNT_INFO_TAGS.put("03", "serviceCode");

        // Các trường con của "additionalData" (id = 38)
        ACCOUNT_INFO_TAGS2.put("01", "billNumber");
        ACCOUNT_INFO_TAGS2.put("02", "mobileNumber");
        ACCOUNT_INFO_TAGS2.put("03", "storeNumber");
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
                    Map<String, String> accountInfo = parseAccountInformation(value);

                    // Đưa tất cả giá trị từ accountInfo vào parsedData
                    parsedData.put("globalUniqueIdentifier", accountInfo.getOrDefault("globalUniqueIdentifier", "null"));
                    parsedData.put("acquirerId", accountInfo.getOrDefault("acquirerId", "null"));
                    parsedData.put("merchantId", accountInfo.getOrDefault("merchantId", "null"));
                    parsedData.put("serviceCode", accountInfo.getOrDefault("serviceCode", "null"));
                } else if (tag.equals("62")) {
                    Map<String, String> accountInfo = parseAccountInformation(value);
                    parsedData.put("billNumber", accountInfo.getOrDefault("billNumber", "null"));
                    parsedData.put("mobileNumber", accountInfo.getOrDefault("mobileNumber", "null"));
                    parsedData.put("storeNumber", accountInfo.getOrDefault("storeNumber", "null"));
                    parsedData.put("terminalNumber", accountInfo.getOrDefault("terminalNumber", "null"));
                } else {
                    parsedData.put(fieldName, value.isEmpty() ? "null" : value);
                }
            } catch (Exception e) {
                parsedData.put("error", "Lỗi khi phân tích EMV: " + e.getMessage());
                break;
            }
        }

        // Dùng Gson với cấu hình để loại bỏ escape unicode
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        return gson.toJson(parsedData);
    }

    // Xử lý subtag
    private static Map<String, String> parseAccountInformation(String data) {
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

                // Xử lý từng trường con
                switch (tag) {
                    case "00":
                        result.put("globalUniqueIdentifier", value);
                        break;
                    case "01":
                        result.put("acquirerId", value.substring(4, 10)); // Chỉ lấy 6 ký tự đầu tiên
                        result.put("merchantId", value.substring(14)); // Phần còn lại là Merchant ID
                        break;
                    case "02":
                        result.put("serviceCode", value);
                        break;
                    default:
                        result.put("unknownTag_" + tag, value);
                        break;
                }
            } catch (Exception e) {
                result.put("error", "Lỗi khi bóc tách Account Information: " + e.getMessage());
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
