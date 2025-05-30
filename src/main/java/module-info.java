module com.example.qrcode {
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires com.google.gson;
    requires opencv;
    requires java.sql;
    requires java.naming;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.xmlbeans;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.swing;
    requires googleauth;
    requires com.google.common;

    opens com.example.qrcode.Model to javafx.base;
    exports com.example.qrcode;
    exports com.example.qrcode.Controller;
    opens com.example.qrcode.Controller to javafx.fxml;
    exports com.example.qrcode.Utils;
    opens com.example.qrcode.Utils to javafx.fxml;
}