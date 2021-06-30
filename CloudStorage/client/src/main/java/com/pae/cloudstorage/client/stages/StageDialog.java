package com.pae.cloudstorage.client.stages;

import com.pae.cloudstorage.client.common.CallBack;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class StageDialog extends Stage {
    private String dialogName;
    private URL fileToOpen;
    private CallBack callBack;

    public StageDialog(String dialogName, URL fileToOpen, CallBack callBack) {
        this.dialogName = dialogName;
        this.fileToOpen = fileToOpen;
        this.callBack = callBack;
        init();
        show();
    }

    private void init(){
        FXMLLoader loader = new FXMLLoader(fileToOpen);
        try {
            Parent root = loader.load();
            setScene(new Scene(root));
            setTitle(this.dialogName);
            sizeToScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CallBack getCallBack(){
        return this.callBack;
    }
}
