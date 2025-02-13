package com.example.qrcode.Controller;

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

    static {
        TAG_NAMES.put("00", "Payload Format Indicator");
        TAG_NAMES.put("01", "Point of Initiation Method");
        TAG_NAMES.put("15", "Merchant Category Code");
        TAG_NAMES.put("58", "Country Code");
        TAG_NAMES.put("12", "Merchant Name");
        TAG_NAMES.put("15", "Merchant City");
        TAG_NAMES.put("52", "Transaction Currency");
        TAG_NAMES.put("53", "Transaction Amount");
        TAG_NAMES.put("50", "Merchant ID");
        TAG_NAMES.put("70", "Bakong Account ID");
        TAG_NAMES.put("80", "Acquiring Bank");
        TAG_NAMES.put("95", "Mobile Number");
        TAG_NAMES.put("62", "Additional Data");
        TAG_NAMES.put("63", "CRC"); // Checksum
        TAG_NAMES.put("38", "Reserved for Future Use");
        TAG_NAMES.put("64", "Reserved for Future Use");
    }


    private static String parseEMVQRCode(String qrData, int level, boolean isSubtag) {
        StringBuilder parsedData = new StringBuilder();
        int index = 0;

        while (index < qrData.length()) {
            try {
                if (index + 4 > qrData.length()) break; // Đảm bảo không bị out of bounds

                // Lấy Tag (2 ký tự đầu)
                String tag = qrData.substring(index, index + 2);
                index += 2;

                // Lấy độ dài của giá trị (2 ký tự tiếp theo)
                int length = Integer.parseInt(qrData.substring(index, index + 2));
                index += 2;

                // Kiểm tra xem có đủ độ dài hay không
                if (index + length > qrData.length()) break;

                // Lấy giá trị thực tế
                String value = qrData.substring(index, index + length);
                index += length;

                // Kiểm tra xem có phải Subtag không
                boolean isCurrentSubtag = tag.equals("38") || tag.equals("62") || tag.equals("64");
                String tagLabel = isSubtag ? "Subtag" : "Tag";
                String tagName = TAG_NAMES.getOrDefault(tag, "Unknown");

                // Thêm vào chuỗi kết quả
                parsedData.append("  ".repeat(level))
                        .append(String.format("%s: %s (%s) | Length: %d | Value: %s\n", tagLabel, tag, tagName, length, value));

                // Nếu tag là 38, 62 hoặc 64, phân tích tiếp phần dữ liệu này và đánh dấu là subtag
                if (isCurrentSubtag) {
                    parsedData.append(parseEMVQRCode(value, level + 1, true));
                }
            } catch (Exception e) {
                parsedData.append("  ".repeat(level)).append("Lỗi khi phân tích EMV.\n");
                break;
            }
        }

        return parsedData.toString();
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
            return parseEMVQRCode(qrContent, 0, false);
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
