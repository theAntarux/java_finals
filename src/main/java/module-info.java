module dev.antarux.skuska_projekt {
    requires javafx.controls;
    requires javafx.fxml;


    opens dev.antarux.skuska_projekt to javafx.fxml;
    exports dev.antarux.skuska_projekt;
}