package com.example.qrcode.Model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class account {
    private SimpleStringProperty account_number;
    private SimpleStringProperty account_holder;
    private SimpleStringProperty balance;
    private SimpleStringProperty secretkey;
    private SimpleIntegerProperty generate_time;

    public account(String account_number, String account_holder, String balance, String secretkey) {
        this.account_number = new SimpleStringProperty(account_number);
        this.account_holder = new SimpleStringProperty(account_holder);
        this.balance = new SimpleStringProperty(balance);
        this.secretkey = new SimpleStringProperty(secretkey);
    }

    public String getAccount_number() {
        return account_number.get();
    }

    public String getAccount_holder() {
        return account_holder.get();
    }

    public String getBalance() {
        return balance.get();
    }

    public String getSecretkey() {
        return secretkey.get();
    }

    public int getGenerate_time() {
        return generate_time.get();
    }

    // Thêm phương thức property nếu muốn binding trực tiếp
    public SimpleStringProperty account_numberProperty() {
        return account_number;
    }

    public SimpleStringProperty account_holderProperty() {
        return account_holder;
    }

    public SimpleStringProperty balanceProperty() {
        return balance;
    }

    public SimpleStringProperty secretkeyProperty() {
        return secretkey;
    }

    public SimpleIntegerProperty generate_timeProperty() {
        return generate_time;
    }
}
