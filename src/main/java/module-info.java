module alle.dupuch.tp1 {
    requires javafx.controls;
    requires javafx.fxml;

    opens alle.dupuch.tp1 to javafx.fxml;
    exports alle.dupuch.tp1;
}