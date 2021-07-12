package com.pae.cloudstorage.client.controllers;

import com.pae.cloudstorage.client.FSTableViewPresentation;
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
import java.nio.file.DirectoryNotEmptyException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    @FXML public ProgressBar progressBar;

    private final Connector connector = new Connector();
    private final StorageWorker swLocal = new StorageWorkerLocal();
    private final StorageWorker swRemote = new StorageWorkerRemote(connector);
    private User user;
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
        connector.requestObject(AUTH_REQ, sj.toString(), (a) -> Platform.runLater(() -> authReceived((String) a[0])));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        localFilesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remoteFilesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        remoteFilesTableView.setPlaceholder(new Label("This directory is empty."));
        localFilesTableView.setPlaceholder(new Label("This directory is empty."));
        FSTableViewPresentation.init(localFilesTableView);
        FSTableViewPresentation.init(remoteFilesTableView);

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
        FSTableViewPresentation.updateTable(target, list);
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
    // Warns if directory for deletion is not empty
    // Possible answers: 0 - no, 1 - yes, 2 - yes for all, 3 - no for all
    // TODO: MOVE TO PROCESSING CONTROLLER
    public void removeSelected(ActionEvent event) {
        TableView tw;
        StorageWorker sw;
        AtomicInteger ans = new AtomicInteger();
        new StageDialog("Remove file(s)",
                DELETE,
                args -> ans.set((int) args[0])).showAndWait();

        if(isLocalPanelAction(event)){
            tw = localFilesTableView;
            sw = swLocal;
        } else {
            tw = remoteFilesTableView;
            sw = swRemote;
        }

        if(ans.get() != 0){
            List<FSObject> selList = tw.getSelectionModel().getSelectedItems();
            StorageWorker finalSw = sw;
            selList.forEach(fso -> {
                String name = fso.getName();
                try {
                    finalSw.removeFile(name);
                } catch (DirectoryNotEmptyException e) {
                    if(ans.get() != 2 && ans.get() != 3){
                        StageDialog sd = new StageDialog("Directory deletion", DELETENOTEMP, o -> ans.set((int)o[0]));
                        ((ControllerConfirmation) sd.getController()).message.setText("Directory " + name + "is not empty. Delete it?");
                        sd.showAndWait();
                    }
                    if(ans.get() == 1 || ans.get() == 2){
                        sw.removeDirRecursive(name);
                    }
                }
            });
        }
        updateFilesList(tw, sw.getFilesList());
    }

    // Searches given name (On remote - from user root, local - from current position)
    public void searchFile(ActionEvent event) {
        if(isLocalPanelAction(event)){
            updateFilesList(localFilesTableView, swLocal.searchFile(new String(searchFieldLocal.getText())));
        } else {
            updateFilesList(remoteFilesTableView, swRemote.searchFile(searchFieldRemote.getText()));
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

    // Closes connection.
    public void stop(){
        connector.stop();
    }

    // Logging off from server without closing application
    public void logoff(ActionEvent event) {
        stop();
        switchControls(false);
    }
}
