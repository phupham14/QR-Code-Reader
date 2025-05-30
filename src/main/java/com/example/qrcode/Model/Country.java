package com.example.qrcode.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Country {
    private final StringProperty countryName;
    private final StringProperty countryCode;
    private final StringProperty numericCode;

    public Country(String countryName, String countryCode, String numericCode) {
        this.countryName = new SimpleStringProperty(countryName);
        this.countryCode = new SimpleStringProperty(countryCode);
        this.numericCode = new SimpleStringProperty(numericCode);
    }

    public String getCountryName() {
        return countryName.get();
    }

    public void setCountryName(String countryName) {
        this.countryName.set(countryName);
    }

    public StringProperty countryNameProperty() {
        return countryName;
    }

    public String getCountryCode() {
        return countryCode.get();
    }

    public void setCountryCode(String countryCode) {
        this.countryCode.set(countryCode);
    }

    public StringProperty countryCodeProperty() {
        return countryCode;
    }

    public String getNumericCode() {
        return numericCode.get();
    }

    public void setNumericCode(String numericCode) {
        this.numericCode.set(numericCode);
    }

    public StringProperty numericCodeProperty() {
        return numericCode;
    }
}
