package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.stages.StagePreview;
import com.pae.cloudstorage.client.storage.StorageWorker;
import com.pae.cloudstorage.common.FSObject;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.*;

/**
 * Preview text & image files
 * directly from stream provided by given storage worker
 */
public class ControllerPreview {
    @FXML public AnchorPane anchor;
    private FSObject file;
    private StorageWorker worker;
    private StagePreview window;

    //
    public void setupAndRun(){
        window = (StagePreview) anchor.getScene().getWindow();
        file = window.getFile();
        worker = window.getStorageWorker();
        showPreview();
    }

    // Determines file type and launch preview
    private void showPreview() {
        if(file.getType() != null && file.getType().contains("image")){
            showImage();
        } else {
            showText();
        }
    }

    // Creates text area, receives file content and places it to anchor.
    private void showText() {
        TextArea ta = new TextArea();
        anchor.getChildren().add(ta);
        getTextFileContent(ta);
        ta.setEditable(false);
        ta.positionCaret(0);
        fitToAnchor(ta);
    }

    // Creates imageView, loads image, fits it to window, and places it to anchor
    private void showImage(){
        Image image = new Image(worker.getStream(file));
        ImageView iv = new ImageView(image);
        iv.setFitWidth(anchor.getPrefWidth());
        iv.setPreserveRatio(true);
        anchor.getChildren().add(iv);
        anchor.setPrefHeight(iv.getFitHeight());
        fitToAnchor(iv);
        window.sizeToScene();
        window.setResizable(false);
    }

    // Retrieves text content from given file and adds it to given TextArea
    private void getTextFileContent(TextArea ta){
        InputStream in = worker.getStream(file);
        try {
            byte[] b = new byte[8 * 1024];
            int count = 0;
            while (count < file.getSize()){
                count += in.read(b);
                ta.appendText(new String(b));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Fits given node to anchor pane
    private void fitToAnchor(Node n){
        AnchorPane.setBottomAnchor(n, 0d);
        AnchorPane.setTopAnchor(n, 0d);
        AnchorPane.setRightAnchor(n, 0d);
        AnchorPane.setLeftAnchor(n, 0d);
    }

}
