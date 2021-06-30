package com.pae.cloudstorage.client.stages;

import com.pae.cloudstorage.client.controllers.ControllerMain;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StageMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Cloud storage");
        primaryStage.setScene(new Scene(root));
        primaryStage.sizeToScene();
        primaryStage.setOnCloseRequest(event -> {
            ControllerMain controller = loader.getController();
            controller.stop();
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
