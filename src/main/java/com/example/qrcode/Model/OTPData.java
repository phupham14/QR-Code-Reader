package com.example.qrcode.Model;

public class OTPData {
    private String secretKey;
    private String qrCodeUrl;

    public OTPData(String secretKey, String qrCodeUrl) {
        this.secretKey = secretKey;
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }
}
