package com.pae.cloudstorage.client.stages;

import com.pae.cloudstorage.client.controllers.ControllerProcessing;
import com.pae.cloudstorage.client.misc.WindowURL;
import com.pae.cloudstorage.client.storage.StorageWorker;
import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.Command;
import com.pae.cloudstorage.common.FSObject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/**
 * Base Stage for files processing
 * Executes command proper method at window show moment (depend on command).
 * Window closes automatically and calls callback method when process is finished.
 */

public class StageProcessing extends Stage {
    Command command;
    List<FSObject> sources;
    StorageWorker localWorker;
    StorageWorker remoteWorker;
    CallBack callBack;
    public StageProcessing(Command command, List<FSObject> sources, CallBack callBack) {
        this.command = command;
        this.callBack = callBack;
        this.sources = sources;
        init();
    }

    private void init(){
        FXMLLoader loader = new FXMLLoader(WindowURL.PROCESSING.url());
        try {
            Parent root = loader.load();
            setScene(new Scene(root));
            switch (command){
                case FILE_DOWNLOAD: setTitle("Download");   break;
                case FILE_UPLOAD: setTitle("Upload");       break;
                case FILE_REMOVE: setTitle("Remove");       break;
                case FILE_COPY: setTitle("Copy");           break;
                case FILE_MOVE: setTitle("Move");           break;
            }
            sizeToScene();
            setOnShowing(event -> {
                ControllerProcessing c = loader.getController();
                c.setParams();
                switch (command){
                    case FILE_DOWNLOAD: c.execDownload(sources);   break;
                    case FILE_UPLOAD: c.execUpload(sources);       break;
                    case FILE_REMOVE: c.execRemove(sources);       break;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLocalWorker(StorageWorker localWorker) {
        this.localWorker = localWorker;
    }

    public void setRemoteWorker(StorageWorker remoteWorker) {
        this.remoteWorker = remoteWorker;
    }

    public StorageWorker getLocalWorker() {
        return localWorker;
    }

    public StorageWorker getRemoteWorker() {
        return remoteWorker;
    }

    public CallBack getCallBack() {
        return callBack;
    }
}
