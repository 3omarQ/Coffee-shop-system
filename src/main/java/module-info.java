module com.example.escaperoom2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires com.opencsv;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.example.escaperoom2 to javafx.fxml;
    exports com.example.escaperoom2;
    exports com.example.escaperoom2.model;
    opens com.example.escaperoom2.model to javafx.fxml;
    exports com.example.escaperoom2.controller;
    opens com.example.escaperoom2.controller to javafx.fxml;
}