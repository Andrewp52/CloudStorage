package com.pae.cloudstorage.client.stages;

import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.common.CallBack;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class StageDialog extends Stage {
    FXMLLoader loader;
    private String dialogName;
    private CallBack callBack;
    private Connector connector;
    public StageDialog(String dialogName, WindowURL wurl, CallBack callBack) {
        this.loader = new FXMLLoader(wurl.url());
        this.dialogName = dialogName;
        this.callBack = callBack;
        init();
    }

    private void init(){
        try {
            Parent root = loader.load();
            setScene(new Scene(root));
            setTitle(this.dialogName);
            sizeToScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public CallBack getCallBack(){
        return this.callBack;
    }

    public Object getController(){
        return loader.getController();
    }
}
