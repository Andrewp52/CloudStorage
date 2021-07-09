package com.pae.cloudstorage.client.storage;

import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.FSObject;

import java.io.InputStream;
import java.util.List;

public interface StorageWorker {
    List<FSObject> getFilesList();
    List<FSObject> searchFile(String name);
    void changeDirectory(String name);
    void makeDirectory(String name);
    void removeFile(String name);
    InputStream getStream(FSObject source);
    void writeFromStream(InputStream in, FSObject source, String path, CallBack callBack);
    List<FSObject> getDirectoryPaths(FSObject source);
}