package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.storage.StorageWorker;
import com.pae.cloudstorage.client.storage.StorageWorkerLocal;
import com.pae.cloudstorage.client.storage.StorageWorkerRemote;
import com.pae.cloudstorage.client.stages.StageProcessing;
import com.pae.cloudstorage.common.FSObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Class-controller for all operations with files & directories
 *
 */
public class ControllerProcessing {
    @FXML public Label operationLabel;
    @FXML public Label statusLabel;
    @FXML public ProgressBar progressBar;

    StageProcessing window;
    StorageWorker localWorker;
    StorageWorker remoteWorker;

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

    // Retrieves all directories & files from given source on server
    // Creates all retrieved directories on local storage.
    // Downloads all inner files.
    private void downloadDirectory(FSObject dir){
        Platform.runLater(() -> operationLabel.setText("Preparing...."));
        List<FSObject> paths = remoteWorker.populateDirectory(dir);
        List<FSObject> files = new ArrayList<>();
        paths.forEach(fsObject ->{
            if (fsObject.isDirectory()){
                localWorker.makeDirectory(fsObject.getPath());
            } else {
                files.add(fsObject);
            }
        });
        files.forEach(this::downloadFile);
    }

    // Downloads file from server (may be called from download or downloadDirectory methods)
    private void downloadFile(FSObject file){
        Platform.runLater(() ->{
            operationLabel.setText("Downloading " + file.getName() + "...");
            progressBar.setProgress(0);
        });
        String locPath = ((StorageWorkerLocal)localWorker).getLocation().toAbsolutePath().toString();
        localWorker.writeFromStream(
                remoteWorker.getStream(file),
                file,
                locPath,
                (o) -> Platform.runLater(() -> progressBar.setProgress(((Double)o[0])))
        );
    }

    // Download file from remote server (called by StageProcessing)
    public void upload(List<FSObject> sources){
        Thread t = new Thread(() -> {
            sources.forEach(fsObject -> {
                if(fsObject.isDirectory()){
                    uploadDirectory(fsObject);
                } else {
                    uploadFile(fsObject);
                }
            });
            Platform.runLater(() -> close());
        });
        t.setDaemon(true);
        t.start();
    }

    // Retrieves all necessary directories from given source on local storage,
    // Creates all retrieved directories on server.
    // Uploads all inner files.
    private void uploadDirectory(FSObject dir){
        Platform.runLater(() -> operationLabel.setText("Preparing...."));
        List<FSObject> paths = localWorker.populateDirectory(dir);
        List<FSObject> files = new ArrayList<>();
        paths.forEach(fsObject ->{
            if (fsObject.isDirectory()){
                remoteWorker.makeDirectory(fsObject.getPath());
            } else {
                files.add(fsObject);
            }
        });
        files.forEach(this::uploadFile);
    }

    // Uploads a given file to remote server (may be called from upload or uploadDirectory methods)
    private void uploadFile(FSObject file){
        Platform.runLater(() ->{
            operationLabel.setText("Uploading " + file.getName() + "...");
            progressBar.setProgress(0);
        });
        remoteWorker.writeFromStream(
                localWorker.getStream(file)
                , file
                ,""
                , (o) -> Platform.runLater(() -> progressBar.setProgress(((Double)o[0])))
        );
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
