module com.example.explorer {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons.lang3;
    requires org.apache.commons.io;


    opens com.example.explorer to javafx.fxml;
    exports com.example.explorer;
    exports controller;
    opens controller to javafx.fxml;
    exports Model;
    opens Model to javafx.fxml;
}