package com.example.qrcode.Controller;

import com.example.qrcode.Service.transferService;
import com.example.qrcode.Utils.AudioMerger;
import com.example.qrcode.Utils.NumberToWordsConverter;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class transferController {

    @FXML
    public Button checkReceiverButton;
    @FXML
    public Button checkSenderButton;
    @FXML
    public Label sender_name;
    @FXML
    public Label receiver_name;
    @FXML
    private Label balanceLabel;
    @FXML
    private TextField accountNumberField;
    @FXML
    private TextField beneficiaryAccountField;
    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<String> currencyBox;
    @FXML
    private TextField messageField;

    final transferService transferService = new transferService();
    private double accountBalance = 0.0; // Số tiền

    @FXML
    private void initialize() {
        accountNumberField.setOnKeyReleased(event -> handleAccountNumberChange());
        beneficiaryAccountField.setOnKeyReleased(event -> handleReceiverAccountChange()); // thêm dòng này

        loadAccountBalance();
    }

    @FXML
    private void handleAccountNumberChange() {
        String accountNumber = accountNumberField.getText();
        if (accountNumber.isEmpty() || !transferService.checkAccountExists(accountNumber)) {
            accountBalance = 0;
            sender_name.setText(""); // clear nếu không tồn tại
            updateBalanceLabel();
        } else {
            accountBalance = transferService.getAccountBalance(accountNumber);
            String name = transferService.getAccountHolderName(accountNumber);
            sender_name.setText(name);
            updateBalanceLabel();
        }
    }

    @FXML
    private void handleReceiverAccountChange() {
        String receiverAccount = beneficiaryAccountField.getText();
        if (receiverAccount.isEmpty() || !transferService.checkAccountExists(receiverAccount)) {
            receiver_name.setText(""); // clear nếu không tồn tại
        } else {
            String name = transferService.getAccountHolderName(receiverAccount);
            receiver_name.setText(name);
        }
    }

    private void loadAccountBalance() {
        String accountNumber = accountNumberField.getText();
        if (accountNumber.isEmpty()) return;

        double balance = transferService.getAccountBalance(accountNumber);
        if (balance >= 0) {
            accountBalance = balance;
            updateBalanceLabel();
        } else {
            showAlert("Lỗi", "Tài khoản không tồn tại.");
        }
    }

    private void updateBalanceLabel() {
        balanceLabel.setText(String.format("%,.0f VND", accountBalance));
    }

    @FXML
    private void handleTransfer() {
        String senderAccount = accountNumberField.getText();
        String receiverAccount = beneficiaryAccountField.getText();
        String amountText = amountField.getText();
        String currency = currencyBox.getValue();
        String message = messageField.getText();

        if (senderAccount.isEmpty() || receiverAccount.isEmpty() || amountText.isEmpty() || currency == null) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        if (senderAccount.equals(receiverAccount)) {
            showAlert("Lỗi", "Số tài khoản thụ hưởng không được phép trùng với số tài khoản người gửi");
            return;
        }

        double money;
        try {
            money = Double.parseDouble(amountText);
            if (money <= 0) {
                showAlert("Lỗi", "Số tiền phải lớn hơn 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số tiền không hợp lệ.");
            return;
        }

        if (money > accountBalance) {
            showAlert("Lỗi", "Số dư không đủ.");
            return;
        }

        if (!transferService.checkAccountExists(receiverAccount)) {
            showAlert("Lỗi", "Tài khoản không hợp lệ.");
            return;
        }

        // Gọi màn hình OTP
        openOTPScreen(senderAccount, receiverAccount, money, currency, message);
    }

    public void showTransactionDetails(String transactionTime, double amount, String receiver) {
        Platform.runLater(() -> {
            new Thread(() -> {
                try {
                    NumberToWordsConverter converter = new NumberToWordsConverter();
                    String text = converter.convert((long) amount);

                    AudioMerger audioMerger = new AudioMerger();
                    audioMerger.playMoneyAmount(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Hiện thông báo giao dịch thành công
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành Công!");
            alert.setHeaderText("Chi tiết giao dịch:");
            alert.setContentText(
                    "Tài khoản thụ hưởng: " + receiver + "\n" +
                            "Số tiền: " + amount + " VND\n" +
                            "Thời gian: " + transactionTime + "\n"
            );
            alert.showAndWait();
            loadAccountBalance();
        });
    }

    private void openOTPScreen(String sender, String receiver, double amount, String currency, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/qrcode/otp.fxml"));
            Parent root = loader.load();

            otpController otpCtrl = loader.getController();
            otpCtrl.setTransferDetails(sender, receiver, amount, currency, message, this);

            Stage stage = new Stage();
            stage.setTitle("Xác thực OTP");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCheckSender() {
        String senderAccount = accountNumberField.getText();
        if (senderAccount.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập số tài khoản người gửi.");
            return;
        }

        if (transferService.checkAccountExists(senderAccount)) {
            double balance = transferService.getAccountBalance(senderAccount);
            accountBalance = balance;
            updateBalanceLabel();
            showAlert("Thông báo", "STK " + senderAccount + " hợp lệ. Số dư: " + String.format("%,.0f VND", balance));
        } else {
            showAlert("Lỗi", "Số tài khoản không tồn tại.");
        }
    }

    @FXML
    private void handleCheckReceiver() {
        String receiverAccount = beneficiaryAccountField.getText();
        if (receiverAccount.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập số tài khoản thụ hưởng.");
            return;
        }

        if (transferService.checkAccountExists(receiverAccount)) {
            String name = transferService.getAccountHolderName(receiverAccount);
            receiver_name.setText(name);
            showAlert("Thông báo", "Số tài khoản thụ hưởng " + receiverAccount + " hợp lệ.");
        } else {
            showAlert("Lỗi", "Số tài khoản thụ hưởng không tồn tại.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
