package com.example.qrcode.Model;

import javafx.beans.property.*;

import java.sql.Timestamp;

public class Transaction {
    private final StringProperty senderAccount;
    private final StringProperty receiverAccount;
    private final DoubleProperty amount;
    private final ObjectProperty<Timestamp> transactionTime;
    private final StringProperty description;
    private final StringProperty debitOrCredit;

    public Transaction(String senderAccount, String receiverAccount, double amount, Timestamp transactionTime, String description, String debitOrCredit) {
        this.senderAccount = new SimpleStringProperty(senderAccount);
        this.receiverAccount = new SimpleStringProperty(receiverAccount);
        this.amount = new SimpleDoubleProperty(amount);
        this.transactionTime = new SimpleObjectProperty<>(transactionTime);
        this.description = new SimpleStringProperty(description);
        this.debitOrCredit = new SimpleStringProperty(debitOrCredit);
    }

    // Getter cho giá trị
    public String getSenderAccount() {
        return senderAccount.get();
    }

    public String getReceiverAccount() {
        return receiverAccount.get();
    }

    public double getAmount() {
        return amount.get();
    }

    public Timestamp getTransactionTime() {
        return transactionTime.get();
    }

    public String getDescription() {
        return description.get();
    }

    // Property getter (bắt buộc cho TableView)
    public StringProperty senderAccountProperty() {
        return senderAccount;
    }

    public StringProperty receiverAccountProperty() {
        return receiverAccount;
    }

    public DoubleProperty amountProperty() {
        return amount;
    }

    public ObjectProperty<Timestamp> transactionTimeProperty() {
        return transactionTime;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty debitOrCredit() {
        return debitOrCredit;
    }

}
