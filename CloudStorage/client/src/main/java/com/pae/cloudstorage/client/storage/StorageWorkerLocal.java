package com.pae.cloudstorage.client.storage;

import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.FSObject;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// Class for disk operations.
// Needs Callback implementation.

public class StorageWorkerLocal implements StorageWorker{
    Path location;

    public Path getLocation() {
        return location;
    }

    public void setLocation(Path location) {
        this.location = location;
    }

    @Override
    public List<FSObject> getFilesList() {
        List<FSObject> fList = new ArrayList<>();
        if(location == null){
            FileSystems.getDefault().getRootDirectories().forEach((d) -> fList.add(new FSObject(d, location)));
        } else {
            try{
                Stream<Path> sp = Files.list(location);
                sp.forEach(path -> fList.add(new FSObject(path, location)));
                sp.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return fList;
    }

    @Override
    public List<FSObject> searchFile(String name) {
        return null;
    }

    // Makes directory(ies) with given path.
    @Override
    public void makeDirectory(String dir) {
        if(location != null){
            try {
                Files.createDirectories(Path.of(this.location.toString(), dir));
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    // Changes client`s location directory (goes back up to client`s root directory)
    // Calls getFilesList at and for client`s list (remote) update
    @Override
    public void changeDirectory(String dir) {
        Path current = location;
        if(location != null){
            if("..".equals(dir)){
                location = current.getParent();
            } else if("~".equals(dir)){
                location = null;
            } else {
                Path newLocation = location.resolve(dir);
                if(Files.exists(newLocation)){
                    location = newLocation;
                }
            }
        } else if(!"..".equals(dir) && !"~".equals(dir)){
            Path newLocation = Path.of(dir);
            if(Files.exists(newLocation)){
                location = newLocation;
            }
        }
    }

    // Removes file or directory (except of not empty directory) returns String - operation result.
    @Override
    public void removeFile(String name) {
        Path p = location.resolve(name);
        try {
            Files.delete(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public InputStream getStream(FSObject source) {
        File f = new File(location.resolve(source.getPath()).toString());
        try {
            FileInputStream fis = new FileInputStream(f);
            return fis;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeFromStream(InputStream in, FSObject source, String path, CallBack callBack){
        File f = new File(path + File.separator + source.getPath());
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

    @Override
    // Retrieves all files and directories info from given directory
    public List<FSObject> getDirectoryPaths(FSObject dir) {
        List<FSObject> list = new ArrayList<>();
        try {
            Files.walkFileTree(location.resolve(dir.getPath()), new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    list.add(new FSObject(dir, location));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    list.add(new FSObject(file, location));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

}
