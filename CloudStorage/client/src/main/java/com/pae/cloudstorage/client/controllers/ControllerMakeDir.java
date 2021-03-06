package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.client.stages.StageDialog;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ControllerMakeDir {
    @FXML public TextField dirNameField;

    // Calls by Ok-button click and Enter pressed at text field.
    public void createDir() {
        String name = dirNameField.getText();
        if(isNameValid(name)){
            getCallBack().call(name);
            close();
        }
    }

    public void close() {
        ((StageDialog) this.dirNameField.getScene().getWindow()).close();
    }

    // Directory name validation
    private boolean isNameValid(String s){
        return s.length() > 0 && s.matches("^([a-zA-Z0-9][^*/><?\\|:]*)$");
    }

    // Retrieves callback from parent stage.
    private CallBack getCallBack(){
        return ((StageDialog) this.dirNameField.getScene().getWindow()).getCallBack();
    }
}
