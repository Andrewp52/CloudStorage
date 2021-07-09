package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.storage.StorageWorkerLocal;
import com.pae.cloudstorage.client.storage.StorageWorkerRemote;
import com.pae.cloudstorage.client.stages.StageProcessing;
import com.pae.cloudstorage.common.FSObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.LinkedList;
import java.util.List;

public class ControllerProcessing {
    @FXML public Label operationLabel;
    @FXML public Label statusLabel;
    @FXML public ProgressBar progressBar;

    StageProcessing window;
    StorageWorkerLocal localWorker;
    StorageWorkerRemote remoteWorker;

    // Download selected files and directories from remote server (called by StageProcessing)
    public void download(List<FSObject> sources){
        Thread t = new Thread(() -> {
            sources.forEach( fsObject -> {
                if(fsObject.isDirectory()){
                    downloadDirectory(fsObject);
                } else {
                    downloadFile(fsObject);
                }
            });
            Platform.runLater(() -> close());
        });
        t.setDaemon(true);
        t.start();
    }

    private void downloadDirectory(FSObject dir){
        Platform.runLater(() -> operationLabel.setText("Preparing...."));
        List<FSObject> paths = remoteWorker.getDirectoryPaths(dir);
        List<FSObject> files = new LinkedList<>();
        paths.forEach(fsObject ->{
            if (fsObject.isDirectory()){
                localWorker.makeDirectory(fsObject.getPath());
            } else {
                files.add(fsObject);
            }
        });
        files.forEach(this::downloadFile);
        Platform.runLater(() -> close());
    }

    private void downloadFile(FSObject file){
        Platform.runLater(() ->{
            operationLabel.setText("Downloading " + file.getName() + "...");
            progressBar.setProgress(0);
        });
        String locPath = localWorker.getLocation().toAbsolutePath().toString();
        localWorker.writeFromStream(
                remoteWorker.getStream(file),
                file,
                locPath,
                (o) -> Platform.runLater(() -> progressBar.setProgress(((Double)o[0])))
        );
    }
    // Download file from remote server (called by StageProcessing)
    public void upload(List<FSObject> sources){

    }

    // Setting up class fields (called by StageProcessing)
    public void setParams(){
        window = (StageProcessing)this.operationLabel.getParent().getScene().getWindow();
        localWorker = (StorageWorkerLocal) window.getLocalWorker();
        remoteWorker = (StorageWorkerRemote) window.getRemoteWorker();
    }

    // Closes window and calls callback
    private void close(){
        this.window.getCallBack().call("");
        this.window.close();
    }
}
