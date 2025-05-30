package com.example.qrcode.Service;

import com.example.qrcode.Model.OTPData;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

public class gauthService {
    private static final String ISSUER = "Account Number"; // Tên hiển thị trên Google Authenticator
    private static final GoogleAuthenticator gAuth;

    static {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(30000)  // Chỉ chấp nhận mã trong 30 giây
                .setWindowSize(1)  // Không nhận mã OTP cũ
                .build();
        gAuth = new GoogleAuthenticator(config);
    }

    public static OTPData generateOTPData(String username, String secretKey) {
        GoogleAuthenticatorKey key = gAuth.createCredentials(); // Tạo Key

        //String secretKey = key.getKey();
        //String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(ISSUER, username, key);
        String qrCodeUrl = "otpauth://totp/" + username + "?secret=" + secretKey + "&issuer=" + ISSUER;
        System.out.println("QR Code URL: " + qrCodeUrl);
        return new OTPData(secretKey, qrCodeUrl);
    }

    public static boolean verifyOTP(String secretKey, int otp) {
        return gAuth.authorize(secretKey, otp);
    }
}
