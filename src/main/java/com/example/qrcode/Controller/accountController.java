package com.example.qrcode.Controller;

import com.example.qrcode.Model.account;
import com.example.qrcode.Service.accountService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class accountController implements Initializable {
    @FXML
    private TableView<account> accountTable;
    @FXML
    private TableColumn<account, String> accountNumberCol;
    @FXML
    private TableColumn<account, String> accountHolderCol;
    @FXML
    private TableColumn<account, String> balanceCol;
    @FXML
    private TableColumn<account, Void> secretKeyCol;
    @FXML
    private TableColumn<account, Void> regenerateKeyCol;

    @FXML
    private Pagination pagination;

    //private otpController otpController = new otpController();
    private final ObservableList<account> fullAccountList = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 10;

//    public void setOtpController(otpController controller) {
//        this.otpController = controller;
//    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        accountNumberCol.setCellValueFactory(cellData -> cellData.getValue().account_numberProperty());
        accountHolderCol.setCellValueFactory(cellData -> cellData.getValue().account_holderProperty());
        balanceCol.setCellValueFactory(cellData -> cellData.getValue().balanceProperty());

        fullAccountList.addAll(accountService.getAllAccounts());
        //System.out.println("Loaded accounts: " + fullAccountList);

        int pageCount = (int) Math.ceil((double) fullAccountList.size() / ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));

        pagination.setPageFactory(this::createPage);
        addButtonToTable();
    }

    private void addButtonToTable() {
        secretKeyCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Generate");

            {
                btn.setOnAction(event -> {
                    int index = getIndex();
                    ObservableList<account> items = getTableView().getItems();
                    if (index >= 0 && index < items.size()) {
                        account acc = items.get(index);
                        openOtpWindow(acc);  // ← Gọi hàm tạo mã QR
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    account acc = getTableView().getItems().get(getIndex());

                    // Nếu đã có secret key → disable nút Generate
                    boolean hasSecretKey = acc.getSecretkey() != null && !acc.getSecretkey().isEmpty();
                    btn.setDisable(hasSecretKey);
                    btn.setOpacity(hasSecretKey ? 0.5 : 1.0); // Làm mờ nút nếu đã disable
                    setGraphic(btn);
                }
            }
        });


        regenerateKeyCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Regenerate");

            {
                btn.setOnAction(event -> {
                    int index = getIndex();
                    ObservableList<account> items = getTableView().getItems();
                    if (index >= 0 && index < items.size()) {
                        account acc = items.get(index);
                        openOtpWindow(acc);  // ← Gọi hàm mới
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    @FXML
    private void openOtpWindow(account acc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/qrcode/genotp.fxml"));
            Parent root = loader.load();

            genOTPController controller = loader.getController();
            controller.handleGenerateQR(acc);
            controller.setAccount(acc); // Truyền thông tin tài khoản

            Stage stage = new Stage();
            stage.setTitle("OTP Verification");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TableView<account> createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, fullAccountList.size());
        //System.out.println("Page " + pageIndex + ": From " + fromIndex + " to " + toIndex + ", Data: " + fullAccountList.subList(fromIndex, toIndex));
        accountTable.setItems(FXCollections.observableArrayList(fullAccountList.subList(fromIndex, toIndex)));
        return accountTable;
    }
}