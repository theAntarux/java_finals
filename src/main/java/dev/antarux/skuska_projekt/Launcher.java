package dev.antarux.skuska_projekt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class Launcher extends Application {
    @Override public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainApp.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 980, 620);
        stage.setTitle("Final-Project");
        stage.setScene(scene);
        stage.resizableProperty().setValue(false);

        // https://stackoverflow.com/questions/61531317/
        InputStream iconStream = getClass().getResourceAsStream("UKF_Logo.png");
        if (iconStream != null) {
            Image icon = new Image(iconStream);
            stage.getIcons().add(icon);
        }

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
