package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.stages.StageDialog;
import com.pae.cloudstorage.common.CallBack;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class ControllerRemove {
    @FXML public AnchorPane anchorMain;

    public void yesClicked() {
        getCallBack().call("");
        close();
    }

    public void close() {
        ((StageDialog) this.anchorMain.getScene().getWindow()).close();
    }

    // Retrieves callback from parent stage.
    private CallBack getCallBack(){
        return ((StageDialog) this.anchorMain.getScene().getWindow()).getCallBack();
    }


}
