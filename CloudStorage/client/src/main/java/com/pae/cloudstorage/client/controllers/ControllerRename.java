package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.stages.StageDialog;
import com.pae.cloudstorage.common.CallBack;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ControllerRename {
    @FXML public TextField newName;

    // Calls by Ok-button click and Enter pressed at text field.
    public void rename() {
        String name = this.newName.getText();
        if(isNameValid(name)){
            getCallBack().call(name);
            close();
        }
    }

    public void close() {
        ((StageDialog) this.newName.getScene().getWindow()).close();
    }

    // Name validation
    private boolean isNameValid(String s){
        return s.length() > 0 && s.matches("^[^*&%\\s]+$");
    }

    // Retrieves callback from parent stage.
    private CallBack getCallBack(){
        return ((StageDialog) this.newName.getScene().getWindow()).getCallBack();
    }
}
