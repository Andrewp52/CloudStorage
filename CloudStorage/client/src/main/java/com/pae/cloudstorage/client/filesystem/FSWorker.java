package com.pae.cloudstorage.client.filesystem;

import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.FSObject;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// Class for disk operations.
// Needs Callback implementation.

public class FSWorker {
    Path location;

    public Path getLocation() {
        return location;
    }

    public void setLocation(Path location) {
        this.location = location;
    }

    public List<FSObject> getFilesList() {
        List<FSObject> fList = new ArrayList<>();
        if(location == null){
            FileSystems.getDefault().getRootDirectories().forEach((d) -> fList.add(new FSObject(d)));
        } else {
            try{
                Stream<Path> sp = Files.list(location);
                sp.forEach(path -> fList.add(new FSObject(path)));
                sp.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return fList;
    }

    // Creates new file with given name
//    public void touchFile(String name) {
//        String ans = String.format("%s created\n", name);
//        try {
//            Files.createFile(this.location.resolve(name));
//        } catch (IOException e){
//            ans = String.format("Can`t create file %s\n", name);
//        }
//        callBack.call(ans);
//    }

    // Makes directory(ies) with given path.
    public List<FSObject> mkdir(String dir) {
        if(location != null){
            try {
                Files.createDirectories(Path.of(this.location.toString(), dir));
                return getFilesList();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    // Changes client`s location directory (goes back up to client`s root directory)
    // Calls getFilesList at and for client`s list (remote) update

    public void changeDirectory(String dir) {
        Path current = location;
        if(location != null){
            if("..".equals(dir)){
                location = current.getParent();
            } else if("~".equals(dir)){
                location = null;
            } else {
                Path newLocation = Path.of(dir);
                if(Files.exists(newLocation)){
                    location = newLocation;
                }
            }
        }
    }

    // Removes file or directory (except of not empty directory) returns String - operation result.
    public void removeFile(String name) {
        Path p = location.resolve(name);
        try {
            Files.delete(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFromConnectorStream(DataInputStream in, FSObject source, String path, CallBack callBack){
        File f = new File(path + File.separator + source.getName());
        try (FileOutputStream fos = new FileOutputStream(f, false)){
            f.createNewFile();
            Long size = source.getSize();
            Long totalRead = 0L;
            byte[] buffer = new byte[8 * 1024];

            while (totalRead < size){
                int read = in.read(buffer);
                fos.write(buffer, 0, read);
                totalRead += read;
                callBack.call(totalRead.doubleValue() / size.doubleValue());
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
