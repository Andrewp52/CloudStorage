package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.stages.StageDialog;
import com.pae.cloudstorage.client.storage.StorageWorker;
import com.pae.cloudstorage.client.storage.StorageWorkerLocal;
import com.pae.cloudstorage.client.storage.StorageWorkerRemote;
import com.pae.cloudstorage.client.stages.StageProcessing;
import com.pae.cloudstorage.common.FSObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pae.cloudstorage.client.misc.WindowURL.DELETE;
import static com.pae.cloudstorage.client.misc.WindowURL.DELETENOTEMP;

/**
 * Class-controller for all operations with files & directories
 * such as remove, upload, download.
 * Operation status - label & progress bar updating from here
 */

public class ControllerProcessing {
    private Object mon = new Object();
    @FXML public Label operationLabel;
    @FXML public Label statusLabel;
    @FXML public ProgressBar progressBar;

    StageProcessing window;
    StorageWorker localWorker;
    StorageWorker remoteWorker;

    // Download selected files and directories from remote server (called by StageProcessing)
    public void execDownload(List<FSObject> sources){
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

    // Executes download file from remote server (called by StageProcessing)
    public void execUpload(List<FSObject> sources){
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

    // Shows confirmation dialog
    // Executes remove files
    public void execRemove(List<FSObject> files){
        AtomicInteger ans = new AtomicInteger();
        new StageDialog("Remove file(s)",
                DELETE,
                args -> ans.set((int) args[0])).showAndWait();
        if(ans.get() != 0){
            Thread t = new Thread(() -> {
                files.forEach(f -> {
                    if(f.isDirectory()){
                        removeDirectory(f, ans);
                    } else {
                        removeFile(f, ans);
                    }
                });
                Platform.runLater(() -> close());
            });
            t.setDaemon(true);
            t.start();
        } else {
            Platform.runLater(() -> close());
        }
    }

    // Removes file
    // Warns if directory for deletion is not empty
    // Possible answers: 0 - no, 1 - yes, 2 - yes for all, 3 - no for all
    private void removeFile(FSObject file, AtomicInteger ans){
        StorageWorker sw = remoteWorker == null ? localWorker : remoteWorker;
        Platform.runLater(() -> progressBar.setProgress(0));
        try {
            Platform.runLater(() -> operationLabel.setText("Removing " + file.getName() + "..."));
            sw.removeFile(file.getName());
        } catch (Exception e) {

        }
    }

    // Removes directory
    // Warns if directory for deletion is not empty
    // Possible answers: 0 - no, 1 - yes, 2 - yes for all, 3 - no for all
    private void removeDirectory(FSObject dir, AtomicInteger ans){
        StorageWorker sw = remoteWorker == null ? localWorker : remoteWorker;
        try {
            Platform.runLater(() -> operationLabel.setText("Removing " + dir.getName() + "..."));
            sw.removeFile(dir.getName());
        } catch (DirectoryNotEmptyException e) {
            if (ans.get() != 2 && ans.get() != 3) {
                Platform.runLater(() -> {
                    StageDialog sd = new StageDialog("Directory deletion", DELETENOTEMP, o -> ans.set((int) o[0]));
                    ((ControllerConfirmation) sd.getController()).message.setText("Directory " + dir.getName() + " is not empty. Delete it?");
                    sd.showAndWait();
                    synchronized (ans) {
                        ans.notify();
                    }
                });
                try {
                    synchronized (ans) {
                        ans.wait();
                    }
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }
        }
        if (ans.get() == 1 || ans.get() == 2) {
            sw.removeDirRecursive(dir.getName(), args -> Platform.runLater(() -> {
                operationLabel.setText("Removing " + args[0] + "...");
                progressBar.setProgress((double) args[1]);
            }));
        }
    }

    // Setting up class fields (called by StageProcessing)
    public void setup(){
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
