package com.example.qrcode.Controller;

import com.example.qrcode.Model.OTPData;
import com.example.qrcode.Service.gauthService;
import com.example.qrcode.Service.otpService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class otpController implements Initializable {
    @FXML
    private TextField otpTextField;

    private static String senderAccount;
    private String receiverAccount;
    private double transferAmount;
    private String transferCurrency;
    private String transferMessage;
    private transferController parentController;

    private static otpService otpService = new otpService(); // Gọi Service để xử lý CSDL

    // Khởi tạo Controller
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //handleVerifyOTP.setOnAction(actionEvent -> handleVerifyOTP());
        System.out.println("OTPController đã được khởi tạo!");
    }

    public void setTransferDetails(String sender, String receiver, double amount, String currency, String message, transferController parent) {
        this.senderAccount = sender;
        this.receiverAccount = receiver;
        this.transferAmount = amount;
        this.transferCurrency = currency;
        this.transferMessage = message;
        this.parentController = parent;

        // Lấy Secret Key từ DB
        String secretKey = otpService.getSecretKeyFromDB(senderAccount);

        // Tạo dữ liệu OTP
        OTPData otpData = gauthService.generateOTPData(senderAccount, secretKey);
    }

    @FXML
    private void handleVerifyOTP() {
        String inputOTP = otpTextField.getText();

        if (inputOTP.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập mã OTP.");
            return;
        }

        String secretKey = otpService.getSecretKeyFromDB(senderAccount); // Lấy Secret Key từ CSDL

        if (gauthService.verifyOTP(secretKey, Integer.parseInt(inputOTP))) {

            // Thực hiện giao dịch
            boolean success = parentController.transferService.processTransaction(
                    senderAccount, receiverAccount, transferAmount, transferCurrency, transferMessage
            );

            if (success) {

                // Gửi thông tin giao dịch về transferController
                String transactionTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                parentController.showTransactionDetails(transactionTime, transferAmount, receiverAccount);

            } else {
                showAlert("Lỗi", "Giao dịch thất bại.");
            }

            ((Stage) otpTextField.getScene().getWindow()).close(); // Đóng cửa sổ OTP
        } else {
            showAlert("Lỗi", "OTP không hợp lệ! Vui lòng thử lại.");
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
