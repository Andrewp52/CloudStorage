package com.pae.cloudstorage.client.misc;

import java.net.URL;

/**
 * All Fxml files locations enum.
 */

public enum WindowURL {
    MAIN("/fxml/MainWindow.fxml"),
    MAKEDIR("/fxml/MakeDirDialog.fxml"),
    DELETE("/fxml/DeleteDialog.fxml"),
    DELETENOTEMP("/fxml/DeleteNonEmptyDialog.fxml"),
    PROFILE("/fxml/ProfileDialog.fxml"),
    PROCESSING("/fxml/ProcessingPopup.fxml"),
    REGISTER("/fxml/RegisterDialog.fxml"),
    PREVIEW("/fxml/PreviewWindow.fxml"),
    RENAME("/fxml/RenameDialog.fxml"),
    POPUP("/fxml/InfoPopup.fxml");
    String s;
    WindowURL(String s) {
        this.s = s;
    }

    public URL url(){
        return getClass().getResource(s);
    }
}
