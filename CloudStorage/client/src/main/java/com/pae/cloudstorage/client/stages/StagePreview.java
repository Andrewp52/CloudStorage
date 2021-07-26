package com.pae.cloudstorage.client.stages;

import com.pae.cloudstorage.client.controllers.ControllerPreview;
import com.pae.cloudstorage.client.misc.WindowURL;
import com.pae.cloudstorage.client.storage.StorageWorker;
import com.pae.cloudstorage.common.FSObject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Stage for preview
 * Accepts title string, storage worker (local / remote) & fsobject
 */
public class StagePreview extends Stage {
    StorageWorker storageWorker;
    FSObject file;
    public StagePreview(String title, FSObject file, StorageWorker sw) {
        this.file = file;
        this.storageWorker = sw;
        FXMLLoader loader = new FXMLLoader(WindowURL.PREVIEW.url());

        try {
            Parent root = loader.load();
            setScene(new Scene(root));
            setTitle(title);
            setOnShowing(event -> {
                ControllerPreview c = loader.getController();
                c.setupAndRun();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public StorageWorker getStorageWorker() {
        return storageWorker;
    }

    public FSObject getFile() {
        return file;
    }
}
