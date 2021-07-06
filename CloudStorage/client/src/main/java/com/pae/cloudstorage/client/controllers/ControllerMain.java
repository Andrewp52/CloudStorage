package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.FSTableViewPresentation;
import com.pae.cloudstorage.client.filesystem.FSWorker;
import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.client.stages.StageDialog;
import com.pae.cloudstorage.common.Command;
import com.pae.cloudstorage.common.FSObject;
import com.pae.cloudstorage.common.User;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;

import static com.pae.cloudstorage.common.Command.*;
import static com.pae.cloudstorage.client.stages.WindowURL.*;
public class ControllerMain implements Initializable {

    @FXML public TableView remoteFilesTableView;
    @FXML public TableView localFilesTableView;
    @FXML public TextField searchFieldLocal;
    @FXML public TextField searchFieldRemote;
    @FXML public VBox container;
    @FXML public HBox loginBox;
    @FXML public VBox navRemote;
    @FXML public VBox navLocal;
    @FXML public Label statusLabel;
    @FXML public PasswordField passwordField;
    @FXML public TextField loginField;

    private Connector connector = new Connector();
    private FSWorker fsWorker;
    private User user;
    public void registerNewUser() {
        // TODO: user registration form execution
    }

    // STUB AUTH
    public void authUser() {
        connector.start();
        connector.requestObject(AUTH_REQ, "aaa", (a) -> Platform.runLater(() -> authReceived((String) a[0])));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        localFilesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remoteFilesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remoteFilesTableView.setPlaceholder(new Label("This directory is empty."));
        localFilesTableView.setPlaceholder(new Label("This directory is empty."));

        fsWorker = new FSWorker();

        // Remote list mouse click handler
        remoteFilesTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() > 1) {
                    if(remoteFilesTableView.getSelectionModel().getSelectedItem() != null) {
                        handleItemClicked(remoteFilesTableView, event);
                    }
                } else if(event.getButton().equals(MouseButton.BACK)){
                    goBack(event);
                }
            }
        });
        // Local list mouse click handler
        localFilesTableView.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() > 1){
                if(localFilesTableView.getSelectionModel().getSelectedItem() != null){
                    handleItemClicked(localFilesTableView, event);
                }
            } else if(event.getButton().equals(MouseButton.BACK)){
                goBack(event);
            }
        });
    }

    // Calls by TableView onClick handler
    // If is directory selected - changes current location on diskWorker or connector
    // depends on particular side (local / remote)
    private void handleItemClicked(TableView<FSObject> tableView, MouseEvent event) {
        FSObject s = tableView.getSelectionModel().getSelectedItem();
        if(s == null){
            return;
        }
        if(isLocalPanelAction(event)) {
            if (s.isDirectory()) {
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
                connector.requestObject(FILE_LIST, null, (a) -> Platform.runLater(() ->updateRemoteList(a[0])));
            }
        }
        statusLabel.setText(msg);
    }

    private void updateRemoteList(Object o){
        List<FSObject> rList = (List<FSObject>) o;
        FSTableViewPresentation.updateTable(remoteFilesTableView, rList);
    }

    private void updateLocalList(List<FSObject> fList){
        FSTableViewPresentation.updateTable(localFilesTableView, fList);
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
                        localFilesTableView.getSelectionModel().getSelectedItems().forEach(
                                (s) -> fsWorker.removeFile(((FSObject) s).getName())
                        );
                        updateLocalList(fsWorker.getFilesList());
                    });
        } else {
            new StageDialog(
                    "Remove file(s) from remote storage",
                    DELETE.url(),
                    args -> {
                        ObservableList i = remoteFilesTableView.getSelectionModel().getSelectedItems();
                        remoteFilesTableView.getSelectionModel().getSelectedItems().forEach(
                                (s) -> {
                                    Command ans = (Command) connector.requestObjectDirect(FILE_REMOVE, ((FSObject) s).getName());
                                    if (!ans.equals(CMD_SUCCESS)){
                                        System.out.println("ERROR: " + ans.name());
                                    }
                                }
                        );
                        connector.requestObject(FILE_LIST, null, o -> updateRemoteList(o[0]));
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
