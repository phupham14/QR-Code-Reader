package com.example.qrcode.Model;

import java.sql.Time;

public class secretKey {
    private String account_number;
    private String secretKey;
    private Time time;

    public secretKey(String account_number, String secretKey, Time time) {
        this.account_number = account_number;
        this.secretKey = secretKey;
        this.time = time;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

}
