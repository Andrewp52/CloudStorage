package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.filesystem.FSWorker;
import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.client.stages.StageDialog;
import com.pae.cloudstorage.common.FSObject;
import com.pae.cloudstorage.common.User;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;

import static com.pae.cloudstorage.common.Command.*;
import static com.pae.cloudstorage.client.stages.WindowURL.*;
public class ControllerMain implements Initializable {

    private Connector connector;
    private FSWorker fsWorker;
    private User user;

    @FXML public TextField searchFieldLocal;
    @FXML public TextField searchFieldRemote;
    @FXML public VBox container;
    @FXML public HBox loginBox;
    @FXML public VBox navRemote;
    @FXML public VBox navLocal;
    @FXML public ListView<FSObject> remoteListView;
    @FXML public ListView<FSObject> localListView;
    @FXML public Label statusLabel;
    @FXML public PasswordField passwordField;
    @FXML public TextField loginField;

    public void registerNewUser() {
        // TODO: user registration form execution
    }

    // STUB AUTH
    public void authUser() {
        connector = new Connector();
        connector.requestObject(AUTH_REQ, "aaa", (a) -> Platform.runLater(() -> authReceived((String) a[0])));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        localListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remoteListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fsWorker = new FSWorker();

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
        if(enable){
            container.getChildren().remove(loginBox);
        } else {
            container.getChildren().add(1, loginBox);
        }
    }

    // Received message handler
    // When first run and auth is ok, sends initial "ls" to server
    // TODO: think about message handling not here/
    private void authReceived(String msg) {
        if(msg.equals(AUTH_OK.name())){
            user = (User) connector.requestObjectDirect(PROFILE_REQ, null);
            if(user != null){
                switchControls(true);
                updateLocalList(fsWorker.getFilesList());
                connector.requestObject(FILE_LIST, (a) -> Platform.runLater(() ->updateRemoteList(a[0])));
            }
        }
        statusLabel.setText(msg);
    }

    private void updateRemoteList(Object o){
        List<FSObject> rList = (List<FSObject>) o;
        remoteListView.getItems().clear();
        rList.forEach((f) -> remoteListView.getItems().addAll(f));
    }

    private void updateLocalList(List<FSObject> fList){
        localListView.getItems().clear();
        fList.forEach((f) -> localListView.getItems().addAll(f));
    }

    // Calls by ListView onClick handler
    // If is directory selected - sends "cd path" command to local diskWorker or connector
    // depends on particular ListView (local / remote)

    private void handleListItemClicked(ListView<FSObject> lv, Event event){
        FSObject s = lv.getSelectionModel().getSelectedItem();
        if(s == null){
            return;
        }
        if(isLocalPanelAction(event)){
            if(s.isDirectory()){
                if (fsWorker.getLocation() == null){
                    fsWorker.setLocation(Path.of(s.getName()));
                } else {
                    fsWorker.changeDirectory(s.getPath());
                }
                updateLocalList(fsWorker.getFilesList());
            }
        } else {
            if(s.isDirectory()){
                connector.requestObject(FILE_CD, s.getPath(), (a) -> Platform.runLater(() ->updateRemoteList(a[0])));
            }
        }
    }

    public void goToRoot(Event actionEvent) {
        if(isLocalPanelAction(actionEvent)){
            fsWorker.changeDirectory("~");
            updateLocalList(fsWorker.getFilesList());
        } else {
            connector.requestObject(FILE_CD, "~", (a) -> Platform.runLater(() ->updateRemoteList(a[0])));
        }
    }

    public void goBack(Event actionEvent) {
        if(isLocalPanelAction(actionEvent)){
            fsWorker.changeDirectory("..");
            updateLocalList(fsWorker.getFilesList());
        } else {
            connector.requestObject(FILE_CD, "..", (a) -> Platform.runLater(() ->updateRemoteList(a[0])));
        }
    }

    // Creates a new directory (remote / local) depends on event source.
    // Path chain (d1/d2/....) is also works.
    public void createDirectory(Event actionEvent){
        if(isLocalPanelAction(actionEvent)){
            new StageDialog(
                    "Create new directory on local storage",
                    MAKEDIR.url(),
                    args -> {
                        fsWorker.mkdir((String) args[0]);
                        updateLocalList(fsWorker.getFilesList());
                    }
            );
        } else {
            new StageDialog(
                    "Create new directory on remote storage",
                    MAKEDIR.url(),
                    args -> connector.requestObject(FILE_MKDIR, (String) args[0], (a) -> Platform.runLater(() ->updateRemoteList(a[0])))
            );
        }
    }

    public void removeSelected(ActionEvent event) {
        if(isLocalPanelAction(event)){
            new StageDialog(
                    "Remove file(s) from local storage",
                    DELETE.url(),
                    args -> {
                        localListView.getSelectionModel().getSelectedItems().forEach(
                                (s) -> fsWorker.removeFile(s.getName())
                        );
                        updateLocalList(fsWorker.getFilesList());
                    });
        } else {
            new StageDialog(
                    "Remove file(s) from remote storage",
                    DELETE.url(),
                    args -> {
                        List<FSObject> i = remoteListView.getSelectionModel().getSelectedItems();
                        remoteListView.getSelectionModel().getSelectedItems().forEach(
                                (s) ->connector.requestNoCallBack(FILE_REMOVE, s.getName())
                        );
                        connector.requestObject(FILE_LIST, o -> updateRemoteList(o[0]));
                    });
        }
    }

    public void searchFile(ActionEvent event) {
        if(isLocalPanelAction(event)){

        } else {
            connector.requestObject(FILE_SEARCH, searchFieldRemote.getText(), o -> updateRemoteList(o[0]));
        }

    }

    // Checks Event`s parent to find out is it local or remote panel.
    public boolean isLocalPanelAction(Event event){
        if(event instanceof ActionEvent){
            return "navLocal".equals(((Node) event.getSource()).getParent().getParent().getId());
        } else {
            return "navLocal".equals(((Node) event.getSource()).getParent().getId());
        }

    }

    public void logoff(ActionEvent event) {
        stop();
        switchControls(false);
    }

    // Closes connection.
    public void stop(){
        connector.stop();
    }

}
