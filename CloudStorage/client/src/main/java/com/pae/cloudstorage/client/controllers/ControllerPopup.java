package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.stages.StagePopup;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ControllerPopup {
    @FXML public Label label;
    StagePopup window;

    public void setParams(){
        window = (StagePopup) this.label.getParent().getScene().getWindow();
        label.setText(window.getMessage());
    }

    public void close(){
        window.close();
    }
}
