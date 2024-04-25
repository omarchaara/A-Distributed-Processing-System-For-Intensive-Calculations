module org.master {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires java.desktop;
    requires javafx.swing;

    opens masterproject.client to javafx.fxml;
    exports masterproject.client;
    exports masterproject;
    opens masterproject to javafx.fxml;
}
