package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.misc.ExchangeBuffer;
import com.pae.cloudstorage.client.misc.StorageAsTableView;
import com.pae.cloudstorage.client.storage.*;
import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.client.stages.*;
import com.pae.cloudstorage.common.*;
import javafx.application.Platform;
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;


import java.net.URL;
import java.util.*;

import static com.pae.cloudstorage.common.Command.*;
import static com.pae.cloudstorage.client.misc.WindowURL.*;

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
    @FXML public ProgressBar progressBar;

    private final Connector connector = new Connector(args -> Platform.runLater(() ->connectorMessage(args)));
    private final StorageWorker swLocal = new StorageWorkerLocal();
    private final StorageWorker swRemote = new StorageWorkerRemote(connector);
    private User user;
    private ExchangeBuffer exBuffer;

    public void registerNewUser() {
        StageDialog sd = new StageDialog("Sign Up", REGISTER, null);
        sd.setConnector(connector);
        sd.show();
    }

    // STUB AUTH
    public void authUser() {
        StringJoiner sj = new StringJoiner(Connector.getDelimiter(), "", "")
                .add(loginField.getText()).add(passwordField.getText());
        connector.start();
        authReceived((String) connector.requestObjectDirect(AUTH_REQ, sj.toString()));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        localFilesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remoteFilesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remoteFilesTableView.setPlaceholder(new Label("This directory is empty."));
        localFilesTableView.setPlaceholder(new Label("This directory is empty."));
        StorageAsTableView.init(localFilesTableView);
        StorageAsTableView.init(remoteFilesTableView);

        // Remote list mouse click handler
        remoteFilesTableView.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() > 1) {
                handleItemClicked(remoteFilesTableView, event);
            } else if (event.getButton().equals(MouseButton.BACK)) {
                goBack(event);
            }
        });
        // Local list mouse click handler
        localFilesTableView.setOnMouseClicked(event -> {
            if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() > 1){
                handleItemClicked(localFilesTableView, event);
            } else if(event.getButton().equals(MouseButton.BACK)){
                goBack(event);
            }
        });
    }

    // Received message handler
    // At first run, if auth is ok, enables controls and updates files lists.
    private void authReceived(String msg) {
        if(msg.equals(AUTH_OK.name())){
            user = (User) connector.requestObjectDirect(PROFILE_REQ, null);
            if(user != null){
                switchControls(true);
                updateFilesList(localFilesTableView, swLocal.getFilesList());
                updateFilesList(remoteFilesTableView, swRemote.getFilesList());
            }
        } else if(msg.equals(AUTH_FAIL.name())){
            new StagePopup("Authentication", "Auth failed...");
        }
        statusLabel.setText(msg);
    }

    // Switches form controls (on / off)
    private void switchControls(boolean enable){
        navLocal.disableProperty().setValue(!enable);
        navRemote.disableProperty().setValue(!enable);
        if(enable){
            container.getChildren().remove(loginBox);
        } else {
            container.getChildren().add(1, loginBox);
        }
    }

    // Updates given TableView wiyh given StorageWorker

    private void updateFilesList(TableView target, List<FSObject> list){
        StorageAsTableView.updateTable(target, list);
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
                swLocal.changeDirectory(s.getName());
                updateFilesList(localFilesTableView, swLocal.getFilesList());
            }
        } else {
            if(s.isDirectory()){
                swRemote.changeDirectory(s.getName());
                updateFilesList(remoteFilesTableView, swRemote.getFilesList());
            }
        }
    }

    public void goToRoot(Event actionEvent) {
        if(isLocalPanelAction(actionEvent)){
            swLocal.changeDirectory("~");
            updateFilesList(localFilesTableView, swLocal.getFilesList());
        } else {
            swRemote.changeDirectory("~");
            updateFilesList(remoteFilesTableView, swRemote.getFilesList());
        }
    }

    public void goBack(Event actionEvent) {
        if(isLocalPanelAction(actionEvent)){
            swLocal.changeDirectory("..");
            updateFilesList(localFilesTableView, swLocal.getFilesList());
        } else {
            swRemote.changeDirectory("..");
            updateFilesList(remoteFilesTableView, swRemote.getFilesList());
        }
    }

    // Creates a new directory (remote / local) depends on event source.
    // Path chain (d1/d2/....) is also works.
    public void createDirectory(Event event){
        TableView tw;
        StorageWorker sw;
        if(isLocalPanelAction(event)){
            tw = localFilesTableView;
            sw = swLocal;
        } else {
            tw = remoteFilesTableView;
            sw = swRemote;
        }
        new StageDialog(
                "Create new directory",
                MAKEDIR,
                args -> {
                    sw.makeDirectory((String) args[0]);
                }
        ).showAndWait();
        updateFilesList(tw, sw.getFilesList());
    }

    // Deletes selected file(s) and directories
    public void removeSelected(ActionEvent event) {
        TableView tw;
        StorageWorker sw;
        boolean local = false;
        if(isLocalPanelAction(event)){
            tw = localFilesTableView;
            sw = swLocal;
            local = true;
        } else {
            tw = remoteFilesTableView;
            sw = swRemote;
        }
        StageProcessing sp = new StageProcessing(
                FILE_REMOVE
                , tw.getSelectionModel().getSelectedItems()
                , a -> updateFilesList(tw, sw.getFilesList())
        );
        sp.setLocalWorker(local ? sw : null);
        sp.setRemoteWorker(local ? null : sw);
        sp.show();
    }

    // Searches given name (On remote - from user root, local - from current position)
    public void searchFile(ActionEvent event) {
        if(isLocalPanelAction(event)){
            updateFilesList(localFilesTableView, swLocal.searchFile(new String(searchFieldLocal.getText())));
        } else {
            updateFilesList(remoteFilesTableView, swRemote.searchFile(searchFieldRemote.getText()));
        }

    }

    // Downloads selected files
    public void downloadSelected() {
        StageProcessing sp = new StageProcessing(
                FILE_DOWNLOAD,
                remoteFilesTableView.getSelectionModel().getSelectedItems(),
                args -> Platform.runLater(() -> updateFilesList(localFilesTableView , swLocal.getFilesList()))
        );
        sp.setLocalWorker(swLocal);
        sp.setRemoteWorker(swRemote);
        sp.show();
    }

    public void downloadFromExBuffer(){
        StageProcessing sp = new StageProcessing(
                FILE_DOWNLOAD,
                exBuffer.getList(),
                args -> Platform.runLater(() -> updateFilesList(localFilesTableView , swLocal.getFilesList()))
        );
        sp.setLocalWorker(swLocal);
        sp.setRemoteWorker(swRemote);
        sp.show();
    }

    public void uploadFromExBuffer(){
        StageProcessing sp = new StageProcessing(
                FILE_UPLOAD,
                exBuffer.getList(),
                args -> Platform.runLater(() -> updateFilesList(remoteFilesTableView , swRemote.getFilesList()))
        );
        sp.setLocalWorker(swLocal);
        sp.setRemoteWorker(swRemote);
        sp.show();
    }
    // Uploads selected files
    public void uploadSelected() {
        StageProcessing sp = new StageProcessing(
                FILE_UPLOAD,
                localFilesTableView.getSelectionModel().getSelectedItems(),
                args -> Platform.runLater(() -> updateFilesList(remoteFilesTableView , swRemote.getFilesList()))
        );
        sp.setLocalWorker(swLocal);
        sp.setRemoteWorker(swRemote);
        sp.show();
    }

    // Writes FSObjects to exchange buffer
    public void copyToBuff(ActionEvent event) {
        TableView tw;
        boolean ebLocal = false;
        if(isLocalPanelAction(event)){
            tw = localFilesTableView;
            ebLocal = true;
        } else {
            tw = remoteFilesTableView;
        }
        exBuffer = new ExchangeBuffer(
                new ArrayList<>(tw.getSelectionModel().getSelectedItems())
                , ebLocal
                ,false
        );
    }

    // Writes FSObject to exchange buffer & sets move flag
    public void moveToBuff(ActionEvent event) {
        copyToBuff(event);
        exBuffer.setMove(true);
    }

    // Executes paste from exchange buffer on StorageWorker
    public void pasteFromBuff(ActionEvent event) {
        StorageWorker src = exBuffer.isLocal() ? swLocal : swRemote;
        StorageWorker dst = isLocalPanelAction(event) ? swLocal : swRemote;
        if (src == dst){
            src.pasteExchBuffer(exBuffer);
            updateFilesList(exBuffer.isLocal() ? localFilesTableView : remoteFilesTableView, dst.getFilesList());
        } else {
            if(exBuffer.isLocal()){
                uploadFromExBuffer();
            } else {
                downloadFromExBuffer();
            }
        }

    }

    // Checks Event`s parent to find out is it local or remote panel.
    // Because of Context menu is not child of Node there is special check here.
    public boolean isLocalPanelAction(Event event){
        if(event instanceof ActionEvent){
            if(event.getSource() instanceof MenuItem) {
                ContextMenu cm = ((MenuItem)event.getSource()).getParentPopup();
                return cm.getId().contains("local");
            } else {
                return "navLocal".equals(((Node) event.getSource()).getParent().getParent().getId());
            }
        } else {
            return "navLocal".equals(((Node) event.getSource()).getParent().getId());
        }
    }

    // Closes connection.
    public void stop(){
        connector.stop();
    }

    // Logging off from server without closing application
    public void logoff(ActionEvent event) {
        stop();
        switchControls(false);
    }

    private void connectorMessage(Object[] args) {
        String type = (String) args[0];
        String message = (String) args[1];
        new StagePopup(type, message);
    }
}
