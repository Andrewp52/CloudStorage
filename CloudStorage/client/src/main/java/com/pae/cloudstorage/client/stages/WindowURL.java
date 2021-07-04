package com.pae.cloudstorage.client.stages;

import java.net.URL;

public enum WindowURL {
    MAIN("/fxml/MainWindow.fxml"),
    MAKEDIR("/fxml/MakeDirDialog.fxml"),
    DELETE("/fxml/DeleteDialog.fxml"),
    PROFILE("/fxml/ProfileDialog.fxml");

    String s;
    WindowURL(String s) {
        this.s = s;
    }

    public URL url(){
        return getClass().getResource(s);
    }
}
