module client {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.base;
    requires io.netty.all;

    exports com.pae.cloudstorage.client.controllers;
    opens com.pae.cloudstorage.client.stages;
    opens com.pae.cloudstorage.client.controllers;
}