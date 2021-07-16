package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.client.stages.StageDialog;
import com.pae.cloudstorage.common.Command;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.StringJoiner;

import static com.pae.cloudstorage.common.Command.REG_OK;


public class ControllerRegister {
    public Button registerButton;
    public TextField firstName;
    public TextField lastName;
    public TextField email;
    public TextField login;
    public PasswordField pass;
    public PasswordField passConfirm;
    public Label statusLabel;

    public void register(ActionEvent event) {
        if(isAllDataValid()){
            Connector connector = getConnector();
            connector.start();
            String ans = (String) getConnector().requestObjectDirect(Command.REG_REQ, getArgsString());
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
        return true;
//        return isLoginValid() && isEmailValid() && isPasswordValid() && isPasswordConfirmed();
    }

    private boolean isLoginValid(){
        statusLabel.setText("Login is invalid");
        return false;
    }

    private boolean isPasswordValid(){
        statusLabel.setText("Password is invalid");
        return false;
    }

    private boolean isPasswordConfirmed(){
        statusLabel.setText("Password does not match.");
        return false;
    }

    private boolean isEmailValid(){
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
