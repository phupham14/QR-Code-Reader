package com.example.qrcode.Controller;

import com.example.qrcode.Model.OTPData;
import com.example.qrcode.Model.account;
import com.example.qrcode.Service.accountService;
import com.example.qrcode.Service.gauthService;
import com.example.qrcode.Service.genOTPService;
import com.example.qrcode.Service.otpService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Duration;

public class genOTPController {

    @FXML
    public Button verifyOTP;

    @FXML
    public Button imageSave;

    @FXML
    private ImageView qrCodeImageView;

    @FXML
    private Label secretKeyLabel;

    @FXML
    private TextField otpTextField;

    @FXML
    private AnchorPane qrPane; // Node chứa QR cần chụp ảnh

    private account acc; // account được truyền vào từ màn khác
    private String newSecretKey;
    private LocalDateTime otpGeneratedTime; // Khai báo biến toàn cục lưu thời gian tạo mã


    accountService accountService = new accountService();
    genOTPService genOTPService = new genOTPService();

    // Một setter trong controller của bạn
    public void setAccount(account acc) {
        this.acc = acc;
    }

    @FXML
    private void initialize() {
        imageSave.setOnAction(event -> saveQRCodeImage());
    }

    public void handleGenerateQR(account acc) {
        this.acc = acc; // Gán vào biến toàn cục
        System.out.println("Đã khởi tạo GenOTPController");

        String senderAccount = acc.getAccount_number();

        try {
            // Luôn tạo mới Secret Key
            GoogleAuthenticatorKey key = new GoogleAuthenticator().createCredentials();
            this.newSecretKey = key.getKey();

            System.out.println("Generated Secret Key: " + newSecretKey);

            if (newSecretKey == null || newSecretKey.isEmpty()) {
                showAlert("Lỗi", "Không thể tạo Secret Key.");
                return;
            }

            // Tạo OTP data
            OTPData otpData = gauthService.generateOTPData(senderAccount, newSecretKey);
            secretKeyLabel.setText("Secret Key: " + newSecretKey);

            Image qrCodeImage = QRCodeGenerator.generateQRCode(otpData.getQrCodeUrl(), 200, 200);
            qrCodeImageView.setImage(qrCodeImage);

            otpGeneratedTime = LocalDateTime.now(); // Ghi nhận thời điểm tạo mã

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Có lỗi xảy ra khi tạo mã QR: " + e.getMessage());
        }
    }


    @FXML
    public void processVerifyOTP(ActionEvent event) {
        String otpStr = otpTextField.getText();

        if (otpStr == null || otpStr.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập mã OTP trước khi xác thực.");
            otpTextField.requestFocus();
            return;
        }

        if (acc == null) {
            showAlert("Lỗi", "Không tìm thấy thông tin tài khoản người gửi.");
            return;
        }

        int enteredOTP;
        try {
            enteredOTP = Integer.parseInt(otpStr);
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Mã OTP phải là số.");
            return;
        }

        String senderAccount = acc.getAccount_number();

        try {
            // Dùng newSecretKey mới tạo
            if (newSecretKey == null || newSecretKey.isEmpty()) {
                showAlert("Lỗi", "Không tìm thấy secret key mới.");
                return;
            }

            boolean isValid = gauthService.verifyOTP(newSecretKey, enteredOTP);

            if (isValid) {
                showAlert("Thành công", "Mã OTP hợp lệ!");

                otpService.saveSecretKeyToDB(senderAccount, newSecretKey); // Lưu vào DB
                genOTPService.saveSecretKeyHistory(senderAccount, newSecretKey); // Lưu lịch sử
                System.out.println("Đã lưu lại secret key vào CSDL");

                // Đóng cửa sổ OTP
                Stage stage = (Stage) otpTextField.getScene().getWindow();
                stage.close();
            } else {
                // Kiểm tra nếu mã OTP vừa nhập nằm trong khoảng thời gian kế tiếp (expired nhưng vừa hết hạn)
                boolean isExpired = checkExpiredOTP(newSecretKey, otpTextField.getText());

                if (isExpired) {
                    showAlert("Hết hạn", "Mã OTP đã hết hạn. Vui lòng lấy mã mới.");
                } else {
                    showAlert("Lỗi", "Mã OTP không hợp lệ.");
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", "Có lỗi xảy ra khi xác thực mã OTP: " + e.getMessage());
        }
    }

    private boolean checkExpiredOTP(String secretKey, String otpString) {
        try {
            int otp = Integer.parseInt(otpString);

            GoogleAuthenticatorConfig expiredConfig = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                    .setTimeStepSizeInMillis(30000)
                    .setWindowSize(2) // Mở rộng windowSize = 2 -> chấp nhận mã vừa mới hết hạn
                    .build();

            GoogleAuthenticator expiredAuth = new GoogleAuthenticator(expiredConfig);

            return expiredAuth.authorize(secretKey, otp);
        } catch (NumberFormatException e) {
            return false;
        }
    }


    @FXML
    public void saveQRCodeImage() {
        try {
            // Chụp ảnh QR từ node qrPane
            WritableImage image = qrPane.snapshot(new SnapshotParameters(), null);

            // Mở FileChooser để chọn nơi lưu
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu hình ảnh QR");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG Image", "*.png")
            );
            fileChooser.setInitialFileName("qr_image.png");
            File file = fileChooser.showSaveDialog(imageSave.getScene().getWindow());

            if (file != null) {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                System.out.println("Lưu ảnh thành công tại: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Lỗi khi lưu ảnh QR: " + e.getMessage());
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
