package com.example.qrcode.Model;

import java.sql.Timestamp;

public class QRDetail {
    private String payloadFormatIndicator;
    private String pointOfInitiationMethod;
    private String globalUniqueIdentifier;
    private String acquirerId;
    private String merchantId;
    private String serviceCode;
    private String transactionCurrency;
    private String transactionAmount;
    private String billNumber;
    private String storeLabel;
    private String terminalLabel;
    private String mobileNumber;
    private String crc;
    private Timestamp timestamp;

    public QRDetail() {

    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public QRDetail(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getPayloadFormatIndicator() {
        return payloadFormatIndicator;
    }

    public void setPayloadFormatIndicator(String payloadFormatIndicator) {
        this.payloadFormatIndicator = payloadFormatIndicator;
    }

    public String getPointOfInitiationMethod() {
        return pointOfInitiationMethod;
    }

    public void setPointOfInitiationMethod(String pointOfInitiationMethod) {
        this.pointOfInitiationMethod = pointOfInitiationMethod;
    }

    public String getGlobalUniqueIdentifier() {
        return globalUniqueIdentifier;
    }

    public void setGlobalUniqueIdentifier(String globalUniqueIdentifier) {
        this.globalUniqueIdentifier = globalUniqueIdentifier;
    }

    public String getAcquirerId() {
        return acquirerId;
    }

    public void setAcquirerId(String acquirerId) {
        this.acquirerId = acquirerId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public String getStoreLabel() {
        return storeLabel;
    }

    public void setStoreLabel(String storeLabel) {
        this.storeLabel = storeLabel;
    }

    public String getTerminalLabel() {
        return terminalLabel;
    }

    public void setTerminalLabel(String terminalLabel) {
        this.terminalLabel = terminalLabel;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    public QRDetail(String payloadFormatIndicator, String pointOfInitiationMethod, String globalUniqueIdentifier, String acquirerId, String merchantId, String serviceCode, String transactionCurrency, String transactionAmount, String billNumber, String storeLabel, String terminalLabel, String mobileNumber, String crc) {
        this.payloadFormatIndicator = payloadFormatIndicator;
        this.pointOfInitiationMethod = pointOfInitiationMethod;
        this.globalUniqueIdentifier = globalUniqueIdentifier;
        this.acquirerId = acquirerId;
        this.merchantId = merchantId;
        this.serviceCode = serviceCode;
        this.transactionCurrency = transactionCurrency;
        this.transactionAmount = transactionAmount;
        this.billNumber = billNumber;
        this.storeLabel = storeLabel;
        this.terminalLabel = terminalLabel;
        this.mobileNumber = mobileNumber;
        this.crc = crc;
    }
}
