package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.client.stages.StageProfile;
import com.pae.cloudstorage.common.Command;
import com.pae.cloudstorage.common.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.StringJoiner;

import static com.pae.cloudstorage.common.Command.PROFILE_UPD;
import static com.pae.cloudstorage.common.Command.PROFILE_UPD_Ok;


public class ControllerProfile {
    private StageProfile window;
    private User user;
    private Connector connector;
    @FXML public Button updateButton;
    @FXML public TextField firstName;
    @FXML public TextField lastName;
    @FXML public TextField email;
    @FXML public TextField login;
    @FXML public Label statusLabel;


    public void update() {
        if(!isChanged()){
            close();
        } else {
            if(isAllDataValid()){
                Command ans = (Command) connector.requestObject(PROFILE_UPD, getArgsString());
                if(ans.equals(PROFILE_UPD_Ok)){
                    this.statusLabel.setText("Update succeed");
                    this.updateButton.setText("Close");
                    this.updateButton.setOnAction(event1 -> this.close());
                } else {
                    this.statusLabel.setText("Update failed");
                }
            }
        }
    }

    boolean isAllDataValid(){
        return isLoginValid() && isEmailValid() && isFnameValid() && isLnameValid();
    }

    boolean isFnameValid(){
        return firstName.getText().length() > 0;
    }

    boolean isLnameValid(){
        return lastName.getText().length() > 0;
    }

    private boolean isLoginValid(){
        if(login.getText().length() > 0){
            return true;
        }
        statusLabel.setText("Login is invalid");
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

    private String getArgsString(){
        return new StringJoiner(Connector.getDelimiter(), "", "")
                .add(firstName.getText())
                .add(lastName.getText())
                .add(email.getText())
                .add(login.getText())
                .toString();
    }

    private boolean isChanged(){
        return !user.getEmail().equals(email.getText())
                || !user.getFirstName().equals(firstName.getText())
                || !user.getLastName().equals(lastName.getText())
                || !user.getNick().equals(login.getText());
    }

    private void close() {
        window.close();
    }

    // Setting up vars
    public void setup() {
        window = (StageProfile) login.getScene().getWindow();
        user = window.getUser();
        connector = window.getConnector();
        firstName.setText(user.getFirstName());
        lastName.setText(user.getLastName());
        email.setText(user.getEmail());
        login.setText(user.getNick());
    }
}
