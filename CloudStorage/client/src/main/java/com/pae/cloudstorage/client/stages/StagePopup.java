package com.pae.cloudstorage.client.stages;

import com.pae.cloudstorage.client.controllers.ControllerPopup;
import com.pae.cloudstorage.client.misc.WindowURL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StagePopup extends Stage {
    private String message;
    public StagePopup(String title, String message) {
        this.message = message;
        FXMLLoader loader = new FXMLLoader(WindowURL.POPUP.url());
        try {
            Parent root = loader.load();
            setScene(new Scene(root));
            setTitle(title);
            setOnShowing(event -> {
                ControllerPopup c = loader.getController();
                c.setParams();
            });
            show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage() {
        return message;
    }
}
