module com.example.qrcode {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires com.google.gson;
    requires opencv;
    requires javafx.swing;

    opens com.example.qrcode to javafx.fxml;
    exports com.example.qrcode;
    exports com.example.qrcode.Controller;
    opens com.example.qrcode.Controller to javafx.fxml;
}