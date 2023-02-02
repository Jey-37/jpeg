module com.example.jpeg {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;
    requires java.logging;


    opens com.example.jpeg to javafx.fxml;
    exports com.example.jpeg;
}