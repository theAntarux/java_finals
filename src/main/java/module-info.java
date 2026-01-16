module dev.antarux.skuska_projekt {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;


    opens dev.antarux.skuska_projekt to javafx.fxml;
    exports dev.antarux.skuska_projekt;
}