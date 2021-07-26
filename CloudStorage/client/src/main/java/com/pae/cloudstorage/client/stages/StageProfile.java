package com.pae.cloudstorage.client.stages;

import com.pae.cloudstorage.client.controllers.ControllerProfile;
import com.pae.cloudstorage.client.misc.WindowURL;
import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.common.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StageProfile extends Stage {
    FXMLLoader loader;
    private String dialogName;
    private User user;
    private Connector connector;

    public StageProfile(String dialogName, WindowURL windowURL, User user, Connector connector) {
        this.loader = new FXMLLoader(windowURL.url());
        this.dialogName = dialogName;
        this.user = user;
        this.connector = connector;
        init();
    }

    private void init(){
        try {
            Parent root = loader.load();
            setScene(new Scene(root));
            setTitle(this.dialogName);
            sizeToScene();
            setOnShowing(event -> {
                ControllerProfile c = loader.getController();
                c.setup();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connector getConnector() {
        return connector;
    }

    public User getUser() {
        return user;
    }
}
