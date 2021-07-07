package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.filesystem.FSWorker;
import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.client.stages.StageProcessing;
import com.pae.cloudstorage.common.FSObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ControllerProcessing {
    @FXML public Label operationLabel;
    @FXML public Label statusLabel;
    @FXML public ProgressBar progressBar;

    StageProcessing window;
    FSObject source;
    String path;
    FSWorker worker;
    Connector connector;

    // Download file from remote server (called by StageProcessing)
    public void download(){
        operationLabel.setText("Downloading " + source.getName() + "...");
        String locPath = worker.getLocation().toAbsolutePath().toString();
        Thread t = new Thread(() -> {
            worker.writeFromConnectorStream(
                    connector.getDownloadStream(source),
                    source,
                    locPath,
                    (o) -> {
                        Platform.runLater(() -> progressBar.setProgress(((Double)o[0])));
                        if(Double.compare ((Double) o[0], 1D) == 0){
                            Platform.runLater(() -> close());
                        }
                    }
            );
        });
        t.setDaemon(true);
        t.start();
    }

    // Download file from remote server (called by StageProcessing)
    public void upload(){

    }

    // Setting up class fields (called by StageProcessing)
    public void setParams(){
        window = (StageProcessing)this.operationLabel.getParent().getScene().getWindow();
        source = window.getSource();
        path = window.getDestPath();
        worker = window.getWorker();
        connector = window.getConnector();
    }

    // Closes window and calls callback
    private void close(){
        this.window.getCallBack().call("");
        this.window.close();
    }
}
