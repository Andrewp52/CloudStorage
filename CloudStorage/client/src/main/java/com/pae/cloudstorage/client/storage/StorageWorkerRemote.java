package com.pae.cloudstorage.client.storage;

import com.pae.cloudstorage.client.misc.ExchangeBuffer;
import com.pae.cloudstorage.client.network.Connector;
import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.Command;
import com.pae.cloudstorage.common.FSObject;

import java.io.*;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;

import static com.pae.cloudstorage.common.Command.*;

public class StorageWorkerRemote implements StorageWorker{
    Connector connector;

    public StorageWorkerRemote(Connector connector) {
        this.connector = connector;
    }

    @Override
    public Path getLocation() {
        return Path.of((String) connector.requestObjectDirect(LOCATION, null));
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
    public void removeFile(String name) throws DirectoryNotEmptyException {
        Command c = (Command) connector.requestObjectDirect(FILE_REMOVE, name);
        if(c.equals(FILE_DNE)){
            throw new DirectoryNotEmptyException(name);
        }
    }

    @Override
    public void removeDirRecursive(String name, CallBack callBack) {
        connector.requestObjectDirect(FILE_REMOVEREC, name);
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
        OutputStream out = connector.getUploadStream(source);
        long count = 0;
        try (in){
            if (out == null){
                return;
            }
            byte[] bytes = new byte[8 * 1024];
            while (count < source.getSize()){
                int read = in.read(bytes);
                out.write(bytes, 0, read);
                count += read;
                callBack.call((double) count / (double) source.getSize());
            }
            connector.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<FSObject> populateDirectory(FSObject source) {
        return (List<FSObject>) connector.requestObjectDirect(FILE_PATHS, source.getOrigin());
    }

    @Override
    public void pasteExchBuffer(ExchangeBuffer eb) {
        if(!eb.isMove()){
            eb.getList().forEach(f -> copyFile(f));
        } else {
            eb.getList().forEach(f -> moveFile(f));
        }
    }

    private void copyFile(FSObject file){
        StringJoiner args = new StringJoiner(Connector.getDelimiter(), "", "");
        args.add(file.getName()).add(Path.of(file.getOrigin()).toString());;
        Command ans = (Command) connector.requestObjectDirect(FILE_COPY, args.toString());
    }

    private void moveFile(FSObject file){
        StringJoiner args = new StringJoiner(Connector.getDelimiter(), "", "");
        args.add(file.getName()).add(Path.of(file.getOrigin()).toString());
        Command ans = (Command) connector.requestObjectDirect(FILE_MOVE, args.toString());
    }
}
