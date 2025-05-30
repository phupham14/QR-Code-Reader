package com.example.qrcode.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Currency {
    private final StringProperty countryName;
    private final StringProperty currencyName;
    private final StringProperty currencyCode;
    private final StringProperty currencyNumber;

    public Currency(String currencyName, String countryName, String currencyCode, String currencyNumber) {
        this.countryName = new SimpleStringProperty(countryName);
        this.currencyName = new SimpleStringProperty(currencyName);
        this.currencyCode = new SimpleStringProperty(currencyCode);
        this.currencyNumber = new SimpleStringProperty(currencyNumber);
    }

    // Getters
    public String getCurrencyName() {
        return currencyName.get();
    }

    public String getCountryName() {
        return countryName.get();
    }

    public String getCurrencyCode() {
        return currencyCode.get();
    }

    public String getCurrencyNumber() {
        return currencyNumber.get();
    }

    // Setters
    public void setCurrencyName(String value) {
        currencyName.set(value);
    }

    public void setCountryName(String value) {
        countryName.set(value);
    }

    public void setCurrencyCode(String value) {
        currencyCode.set(value);
    }

    public void setCurrencyNumber(String value) {
        currencyNumber.set(value);
    }

    // Property Getters for TableView Binding
    public StringProperty currencyNameProperty() {
        return currencyName;
    }

    public StringProperty countryNameProperty() {
        return countryName;
    }

    public StringProperty currencyCodeProperty() {
        return currencyCode;
    }

    public StringProperty currencyNumberProperty() {
        return currencyNumber;
    }
}
