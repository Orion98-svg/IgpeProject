package com.example.explorer;

import Model.GestoreTag;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;

public class Main extends Application {

    public static HostServices hs;
    public static HashMap mappa = new HashMap<>();

    @Override
    public void start(Stage stage) throws IOException {
        hs = getHostServices();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("View/Explorer.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Explorer");
        stage.getIcons().add(new Image("icona.png"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        GestoreTag.init();
        launch();
    }
}