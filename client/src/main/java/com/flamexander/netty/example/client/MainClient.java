package com.flamexander.netty.example.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainClient extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("GreenCloud Client");
        Scene scene = new Scene(root, 600, 550);
        scene.getStylesheets().add(0, "/caspian/caspian.css");
        primaryStage.setScene(scene);

        primaryStage.getIcons().add(new Image("/icon.png"));
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
