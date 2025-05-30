package com.example.qrcode.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Bank {
    private final StringProperty bankName;
    private final StringProperty bankCode;

    public Bank(String bankName, String bankCode) {
        this.bankName = new SimpleStringProperty(bankName);
        this.bankCode = new SimpleStringProperty(bankCode);
    }

    public String getBankName() {
        return bankName.get();
    }

    public StringProperty bankNameProperty() {
        return bankName;
    }

    public String getBankCode() {
        return bankCode.get();
    }

    public StringProperty bankCodeProperty() {
        return bankCode;
    }
}
