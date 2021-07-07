package com.pae.cloudstorage.client.stages;

import com.pae.cloudstorage.client.controllers.ControllerProcessing;
import com.pae.cloudstorage.client.filesystem.FSWorker;
import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.Command;
import com.pae.cloudstorage.common.FSObject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static com.pae.cloudstorage.common.Command.FILE_DOWNLOAD;

/**
 * Base Stage for files processing
 * Executes command proper method at window show moment (depend on command).
 * Window closes automatically and calls callback method when process is finished.
 */

public class StageProcessing extends Stage {

    Command command;
    FSObject source;
    String destPath;
    FSWorker worker;
    Connector connector;
    CallBack callBack;
    public StageProcessing(Command command, FSObject source, String destPath, FSWorker worker, Connector connector, CallBack callBack) {
        this.command = command;
        this.source = source;
        this.destPath = destPath;
        this.worker = worker;
        this.connector = connector;
        this.callBack = callBack;
        init();
    }

    private void init(){
        FXMLLoader loader = new FXMLLoader(WindowURL.PROCESSING.url());
        try {
            Parent root = loader.load();
            setScene(new Scene(root));
            if(FILE_DOWNLOAD.equals(command)){
                setTitle("Download");
            } else {
                setTitle("Upload");
            }
            sizeToScene();

            setOnShowing(event -> {
                ControllerProcessing c = loader.getController();
                c.setParams();
                if(FILE_DOWNLOAD.equals(command)){
                    c.download();
                } else {
                    c.upload();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FSObject getSource() {
        return source;
    }

    public String getDestPath() {
        return destPath;
    }

    public FSWorker getWorker() {
        return worker;
    }

    public Connector getConnector() {
        return connector;
    }

    public CallBack getCallBack() {
        return callBack;
    }
}
