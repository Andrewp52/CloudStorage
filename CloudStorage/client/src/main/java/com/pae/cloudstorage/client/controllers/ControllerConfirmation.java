package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.stages.StageDialog;
import com.pae.cloudstorage.common.CallBack;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 * Controller for action confirmation dialogs
 * supports 4 buttons (Yes, No, Yes for all, No for all)
 */
public class ControllerConfirmation {
    @FXML public AnchorPane anchorMain;
    @FXML public Label message;
    public void noClicked(){
        getCallBack().call(0);
        close();
    }

    public void yesClicked() {
        getCallBack().call(1);
        close();
    }

    public void yesForAllClicked(){
        getCallBack().call(2);
        close();
    }

    public void noForAllClicked(){
        getCallBack().call(3);
        close();
    }

    public void close() {
        ((StageDialog) this.anchorMain.getScene().getWindow()).close();
    }

    // Retrieves callback from stage.
    private CallBack getCallBack(){
        return ((StageDialog) this.anchorMain.getScene().getWindow()).getCallBack();
    }

}
