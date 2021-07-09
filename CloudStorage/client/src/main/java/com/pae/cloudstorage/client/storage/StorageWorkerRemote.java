package com.pae.cloudstorage.client.storage;

import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.FSObject;

import java.io.InputStream;
import java.util.List;

import static com.pae.cloudstorage.common.Command.*;

public class StorageWorkerRemote implements StorageWorker{
    Connector connector;

    public StorageWorkerRemote(Connector connector) {
        this.connector = connector;
    }

    @Override
    public List<FSObject> getFilesList() {
        return (List<FSObject>) connector.requestObjectDirect(FILE_LIST, null);
    }

    @Override
    public void makeDirectory(String dir) {
        connector.requestObjectDirect(FILE_MKDIR, dir);
    }

    @Override
    public void changeDirectory(String dir) {
        connector.requestObjectDirect(FILE_CD, dir);
    }

    @Override
    public void removeFile(String name) {
        connector.requestObjectDirect(FILE_REMOVE, name);
    }

    @Override
    public InputStream getStream(FSObject source) {
        return connector.getDownloadStream(source);
    }

    @Override
    public List<FSObject> searchFile(String name) {
        return (List<FSObject>) connector.requestObjectDirect(FILE_SEARCH, name);
    }

    @Override
    public void writeFromStream(InputStream in, FSObject source, String path, CallBack callBack) {

    }

    @Override
    public List<FSObject> getDirectoryPaths(FSObject source) {
        return (List<FSObject>) connector.requestObjectDirect(FILE_PATHS, source.getName());
    }
}
