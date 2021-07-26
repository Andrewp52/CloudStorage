package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.client.stages.StageDialog;
import com.pae.cloudstorage.client.stages.StagePopup;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.StringJoiner;

import static com.pae.cloudstorage.common.Command.*;


public class ControllerRegister {
    @FXML public Button registerButton;
    @FXML public TextField firstName;
    @FXML public TextField lastName;
    @FXML public TextField email;
    @FXML public TextField login;
    @FXML public PasswordField pass;
    @FXML public PasswordField passConfirm;
    @FXML public Label statusLabel;

    public void register() {
        if(isAllDataValid()){
            Connector connector = getConnector();
            try {
                connector.start();
            } catch (IOException e) {
                new StagePopup("ERROR", e.getMessage());
            }
            String ans = (String) getConnector().requestObject(REG_REQ, getArgsString());
            if(ans.equals(REG_OK.name())){
                this.statusLabel.setText("Registration succeed");
                this.registerButton.setText("Close");
                this.registerButton.setOnAction(event1 -> this.close());
            } else {
                this.statusLabel.setText("Registration failed");
            }
            connector.stop();
        }
    }

    private String getArgsString(){
        return new StringJoiner(Connector.getDelimiter(), "", "")
                .add(firstName.getText())
                .add(lastName.getText())
                .add(email.getText())
                .add(login.getText())
                .add(pass.getText())
                .toString();
    }

    boolean isAllDataValid(){
        return isLoginValid() && isEmailValid() && isPasswordValid() && isPasswordConfirmed();
    }

    private boolean isLoginValid(){
        if(login.getText().length() > 0){
            return true;
        }
        statusLabel.setText("Login is invalid");
        return false;
    }

    private boolean isPasswordValid(){
        if(pass.getText().length() > 0){
            return true;
        }
        statusLabel.setText("Password is invalid");
        return false;
    }

    // Checks password confirmation is equal password
    private boolean isPasswordConfirmed(){
        if(pass.getText().equals(passConfirm.getText())){
            return true;
        }
        statusLabel.setText("Password does not match.");
        return false;
    }

    // Validates email address
    private boolean isEmailValid(){
        String regex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
        if(email.getText().length() > 0 && email.getText().matches(regex)){
            return true;
        }
        statusLabel.setText("email is invalid.");
        return false;
    }

    private StageDialog getWindow(){
        return (StageDialog) login.getScene().getWindow();
    }

    private Connector getConnector(){
        return getWindow().getConnector();
    }
    private void close() {
        getWindow().close();
    }
}
