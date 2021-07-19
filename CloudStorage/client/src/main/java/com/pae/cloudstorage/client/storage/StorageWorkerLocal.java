package com.pae.cloudstorage.client.storage;

import com.pae.cloudstorage.client.misc.ExchangeBuffer;
import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.FSObject;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// Class for storage operations.

public class StorageWorkerLocal implements StorageWorker{
    private Path location;
    private Path root;

    public Path getLocation() {
        return location.toAbsolutePath();
    }

    public void setLocation(Path location) {
        this.location = location;
    }

    @Override
    public List<FSObject> getFilesList() {
        List<FSObject> fList = new ArrayList<>();
        if(location == null){
            FileSystems.getDefault().getRootDirectories().forEach((d) -> fList.add(new FSObject(d, location, root)));
            root = null;
        } else {
            if(root == null){
                root = location;
            }
            try{
                Stream<Path> sp = Files.list(location);
                sp.forEach(path -> fList.add(new FSObject(path, location, root)));
                sp.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return fList;
    }

    @Override
    public List<FSObject> searchFile(String name) {
        List<FSObject> found = new ArrayList<>();
        try {
            Files.walkFileTree(location, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String filename;
                    if(dir.getFileName() == null){
                        filename = dir.getRoot().toString();
                    } else {
                        filename = dir.getFileName().toString();
                    }
                    if (filename.toLowerCase().contains(name.toLowerCase())) {
                        found.add(new FSObject(dir, dir.getParent(), root));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().toLowerCase().contains(name.toLowerCase())) {
                        found.add(new FSObject(file, file.getParent(), root));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        return found;
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
    public void removeFile(String name) throws DirectoryNotEmptyException {
        Path p = location.resolve(name);
        try {
            Files.delete(p);
        } catch (DirectoryNotEmptyException e){
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Removes directory and all it`s inner content
    public void removeDirRecursive(String name, CallBack callBack){
        Path p = location.resolve(name);
        try{
            Files.walkFileTree(p, new SimpleFileVisitor<Path>(){

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    callBack.call(file.getFileName(), 0D);
                    Files.setAttribute(file, "dos:readonly", false);
                    Files.delete(file);
                    callBack.call(file.getFileName(), 1D);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    callBack.call(dir.getFileName(), 0D);
                    Files.setAttribute(dir, "dos:readonly", false);
                    Files.delete(dir);
                    callBack.call(dir.getFileName(), 1D);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    exc.printStackTrace();
                    return FileVisitResult.SKIP_SUBTREE;
                }
            });
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public InputStream getStream(FSObject source) {
        File f = new File(source.getOrigin());
        try {
            FileInputStream fis = new FileInputStream(f);
            return fis;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
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

    // Retrieves all files and directories info from given directory
    public List<FSObject> populateDirectory(FSObject dir) {
        Path loc = Path.of(dir.getOrigin()).getParent();
        List<FSObject> list = new ArrayList<>();
        try {
            Files.walkFileTree(loc.resolve(dir.getPath()), new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    list.add(new FSObject(dir, loc, root));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    list.add(new FSObject(file, loc, root));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Exchange buffer job (Copy Cut Paste)
    @Override
    public void pasteExchBuffer(ExchangeBuffer eb) {
        List<FSObject> files = new ArrayList<>();
        if(!eb.isMove()){
            eb.getList().forEach(fsObject -> files.addAll(populateDirectory(fsObject)));
            files.forEach(this::copyFile);
        } else {
            eb.getList().forEach(this::moveFile);
        }
    }

    // Copies file or directory to the new location.
    private void copyFile(FSObject file){
        try {
            if (file.isDirectory()) {
                Files.createDirectories(location.resolve(file.getPath()));
            } else {
                Files.copy(Path.of(file.getOrigin()), location.resolve(file.getPath()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Moving file or directory to the new location
    // Read only attribute set/reset is necessary for avoid AccessDeniedException.
    private void moveFile(FSObject file){
        try {
            if(file.isReadOnly()){
                Files.setAttribute(Path.of(file.getOrigin()), "dos:readonly", false);
            }
            Files.move(Path.of(file.getOrigin()), location.resolve(file.getPath()));
            if(file.isReadOnly()){
                Files.setAttribute(location.resolve(file.getPath()), "dos:readonly", true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void compress(List<FSObject> files, String name){

    }

    public void deCompress(Path arch){

    }

}
