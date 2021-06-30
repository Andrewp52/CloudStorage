package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.common.DiskWorker;
import com.pae.cloudstorage.client.network.Connector;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class ControllerMain implements Initializable {
    private Connector connector;
    private DiskWorker diskWorker;
    private ObservableList<String> remote = FXCollections.observableArrayList();
    private ObservableList<String> local = FXCollections.observableArrayList();
    @FXML public VBox navRemote;
    @FXML public VBox navLocal;
    @FXML public ListView<String> remoteListView;
    @FXML public ListView<String> localListView;
    @FXML public Label statusLabel;
    @FXML public PasswordField passwordField;
    @FXML public TextField loginField;

    public void registerNewUser() {
        // TODO: user registration form execution
    }

    // STUB AUTH
    public void authUser() {
        connector.start();
        connector.sendCommand("auth aaa");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connector = new Connector((a)-> {
            Platform.runLater(() -> messageReceived((String) a[0]));            // Connector callback
        });
        diskWorker = new DiskWorker((s) -> Platform.runLater(() -> {
            fillFilesList(local, localListView, (String) s[0]);             // Local worker callback
        }));
        // Remote list mouse click handler
        remoteListView.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() > 1){
                if(remoteListView.getSelectionModel().getSelectedItem() != null){
                    handleListItemClicked(remoteListView, event);
                }
            } else if(event.getButton().equals(MouseButton.BACK)){
                goBack(event);
            }
        });
        // Local list mouse click handler
        localListView.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() > 1){
                if(localListView.getSelectionModel().getSelectedItem() != null){
                    handleListItemClicked(localListView, event);
                }
            } else if(event.getButton().equals(MouseButton.BACK)){
                goBack(event);
            }
        });
    }

    private void switchControls(boolean enable){
        navLocal.disableProperty().setValue(!enable);
        navRemote.disableProperty().setValue(!enable);
    }

    // Received message handler
    // When first run and auth is ok, sends initial "ls" to server
    // TODO: think about message handling not here/
    private void messageReceived(String msg) {
        if(msg.equals("AUTH_OK")){
            switchControls(true);
            requestLocalList();
            connector.sendCommand("ls");
        } else if(msg.contains("<DIR_LIST>")){
            fillFilesList(remote, remoteListView, msg);
        }
        statusLabel.setText(msg);
    }

    // Request local DiskWorker for this computer files list
    // TODO: modify local worker and caller for working with Path objects instead of string
    private void requestLocalList() {
        diskWorker.getFilesList();
    }

    // Fills collection for listView (remote storage)
    private void fillFilesList(List<String> listToFill, ListView viewToFill, String msg) {
        listToFill.clear();
        String str = msg.replace("<DIR_LIST>", "");
        Map<String, Boolean> filesMap = new HashMap<>();
        String[] arr = str.split("[*]");
        listToFill.addAll(Arrays.asList(arr));
        viewToFill.getItems().setAll(listToFill);
        viewToFill.refresh();
    }


    // Calls by ListView onClick handler
    // If is directory selected - sends "cd path" command to local diskWorker or connector
    // depends on particular ListView (local / remote)

    private void handleListItemClicked(ListView lv, Event event){
        String s = lv.getSelectionModel().getSelectedItem().toString();
        if(isLocalPanelAction(event)){
            if(s != null){
                if(s.contains("<D>")){
                    if (diskWorker.getLocation() == null){
                        diskWorker.setLocation(Path.of(s.replace("<D>", "")));
                    } else {
                        diskWorker.changeDirectory(s.replace("<D>", ""));
                    }
                    diskWorker.getFilesList();
                }
            }
        } else {
            if(s != null){
                if(s.contains("<D>")){
                    connector.sendCommand("cd " + s.replace("<D>", ""));
                }
            }
        }
    }

    public void goToRoot(Event actionEvent) {
        if(isLocalPanelAction(actionEvent)){
            diskWorker.changeDirectory("~");
        } else {
            connector.sendCommand("cd ~");
        }
    }

    public void goBack(Event actionEvent) {
        if(isLocalPanelAction(actionEvent)){
            diskWorker.changeDirectory("..");
        } else {
            connector.sendCommand("cd ..");
        }
    }

    // Checks Event`s parent to find out is it local or remote panel.
    public boolean isLocalPanelAction(Event event){
        if(event instanceof ActionEvent){
            return "navLocal".equals(((Button) event.getSource()).getParent().getParent().getId());
        } else {
            return "navLocal".equals(((ListView) event.getSource()).getParent().getId());
        }

    }

    public void stop(){
        connector.stop();
    }

}
