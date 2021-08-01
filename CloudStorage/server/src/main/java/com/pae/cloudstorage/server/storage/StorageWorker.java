package com.pae.cloudstorage.server.storage;

import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.FSObject;
import com.pae.cloudstorage.common.User;
import com.pae.cloudstorage.server.Main;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.pae.cloudstorage.common.Command.*;

// Class for filesystem operations.
// Needs Callback implementation.

public class StorageWorker {
    private static final Logger logger = LogManager.getLogger(StorageWorker.class);
    private static final Path SRVROOT;
    private Path usrRoot;
    private Path location;
    private User user;
    private CallBack callBack;
    static {
        SRVROOT = Path.of(Main.getSrvConfig().get("srvroot"));
    }
    public StorageWorker(User user, CallBack callBack) throws IOException {
        this.user = user;
        this.callBack = callBack;
        if(Files.notExists(SRVROOT)){
            Files.createDirectory(SRVROOT);
        }
        this.usrRoot = SRVROOT.resolve(Path.of(String.valueOf(this.user.getId())));
        if(!Files.exists(usrRoot)){
            Files.createDirectory(SRVROOT.resolve(Path.of(String.valueOf(this.user.getId()))));
        }
        location = usrRoot;
    }

    public void getLocation() {
        callBack.call(usrRoot.relativize(location).toString());
    }

    public void getFilesList(){
        List<FSObject> dirList = new ArrayList<>();
        Stream<Path> sp = null;
        try {
            sp = Files.list(location);
            sp.forEach(path -> dirList.add(new FSObject(path, location, usrRoot)));
            sp.close();
        } catch (IOException e) {
            logger.error("List files error: ", e);
        }
        callBack.call(dirList);
    }

    // Creates new file with given name
    // We need it for zero-sized files upload
    public void touchFile(FSObject file) {
        try {
            Files.createFile(this.location.resolve(file.getPath()));
        } catch (IOException e){
            logger.error("Touch file error: ", e);
        }
        callBack.call(FILE_SKIP);
    }

    // Makes directory(ies) with given path.
    public void mkdir(String dir) {
        if(location != null){
            try {
                Files.createDirectories(Path.of(this.location.toString(), dir));
                getFilesList();
            } catch (IOException e){
                logger.error("Make dir error: ", e);
            }
        }
    }

    // Changes client`s location directory (goes back up to client`s root directory)
    // Calls getFilesList at and for client`s list (remote) update

    public void changeDirectory(String dir){
        Path current = location;
        if("..".equals(dir)){
            if(!current.equals(usrRoot)){
                location = current.getParent();
            }
        } else if("~".equals(dir)){
            location = usrRoot;
        } else {
            Path newLocation = location.resolve(dir);
            if(Files.exists(newLocation)){
                location = newLocation;
            }
        }
        getFilesList();
    }

    // Removes file or directory (except of not empty directory) returns String - operation result.
    public void removeFile(String name) {
        Path p = location.resolve(name);
        try {
            long size = Files.size(p);
            Files.delete(p);
            user.remUsed(size);
            callBack.call(CMD_SUCCESS);
        } catch (DirectoryNotEmptyException e){
            callBack.call(FILE_DNE);
        } catch (IOException e) {
            callBack.call(CMD_FAIL);
            logger.error("Remove file error: ", e);
        }
    }

    public void removeDirRecursive(String name){
        Path p = location.resolve(name);
        AtomicLong size = new AtomicLong();
        try{
            Files.walkFileTree(p, new SimpleFileVisitor<Path>(){

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    size.addAndGet(Files.size(file));
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    exc.printStackTrace();
                    return FileVisitResult.SKIP_SUBTREE;
                }
            });
            user.remUsed(size.get());
            callBack.call(CMD_SUCCESS);
        } catch (IOException e){
            logger.error("Remove dir recursive error: ", e);
            callBack.call(CMD_FAIL);
        }
    }

    public void searchFile(String name) {
        List<FSObject> found = new ArrayList<>();
        try {
            Files.walkFileTree(location, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.getFileName().toString().toLowerCase().contains(name.toLowerCase())) {
                        found.add(new FSObject(dir, dir.getParent(), usrRoot));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().toLowerCase().contains(name.toLowerCase())) {
                        found.add(new FSObject(file, file.getParent(), usrRoot));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error("Search file error: ", e);
        }
        callBack.call(found);
    }

    // Copies file or directory (with inner content) from it`s origin to current location
    public void copyFile(String name, String originStr){
        Path origin = Path.of(originStr);
        AtomicLong size = new AtomicLong();
        if(Files.isDirectory(origin)){
            List<FSObject> files = populateDirectory(origin.toString(), true);
            files.forEach(f -> size.getAndAdd(f.getSize()));
            if(isFreeSpaceEnough(size.get())){
                size.set(0);
                files.forEach(f -> {
                    try {
                        if (f.isDirectory()) {
                            Files.createDirectories(location.resolve(f.getPath()));
                        } else {
                            Files.copy(Path.of(f.getOrigin()), location.resolve(f.getPath()));
                            size.getAndAdd(Files.size(location.resolve(f.getPath())));
                        }
                    } catch (IOException e) {
                        logger.error("Copy file error: ", e);
                    }
                });
                user.addUsed(size.get());
            } else {
                callBack.call(SPACE_NOT_ENOUGH);
            }
        } else {
            try {
                if(isFreeSpaceEnough(Files.size(Path.of(originStr)))){
                    Files.copy(origin, location.resolve(name));
                    size.getAndAdd(Files.size(location.resolve(name)));
                    user.addUsed(size.get());
                } else {
                    callBack.call(SPACE_NOT_ENOUGH);
                }
            } catch (IOException e) {
                logger.error("Copy file error: ", e);
            }
        }
        callBack.call(CMD_SUCCESS);
    }

    // Moves file or directory from it`s origin to current location
    public void moveFile(String name, String originStr){
        Path origin = Path.of(originStr);
        if(Files.isDirectory(origin)){
            List<FSObject> files = populateDirectory(originStr, true);
            files.forEach(f -> {
                try {
                    if(f.isReadOnly()){
                        Files.setAttribute(Path.of(f.getOrigin()), "dos:readonly", false);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            try {
                Files.move(origin, location.resolve(name));
            } catch (IOException e) {
                logger.error("Move file error: ", e);
            }
        } else {
            try {
                if((boolean) Files.getAttribute(origin, "dos:readonly")){
                    Files.setAttribute(origin, "dos:readonly", false);
                }
                Files.move(origin, location.resolve(name));
            } catch (IOException e) {
                logger.error("Move file error: ", e);
            }
        }
        callBack.call(CMD_SUCCESS);
    }

    // Renames given file or directory
    public void rename(String oldName, String newName) {
        Path p = location.resolve(oldName);
        File f = new File(p.toString());
        f.renameTo(new File(p.getParent().toString() + File.separator + newName));
        callBack.call(CMD_SUCCESS);
    }

    // Returns file for sending to client
    public File getFile(String origin) {
        return new File(Path.of(origin).toString());
    }

    // Retrieves all files and directories from given directory
    // Depends on origin is present or not
    public List<FSObject> populateDirectory(String token, boolean suppressCallBack) {
        Path src = Path.of(token);
        List<FSObject> list = new ArrayList<>();
        try {
            Files.walkFileTree(src, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    list.add(new FSObject(dir, src.getParent(), usrRoot));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    list.add(new FSObject(file, src.getParent(), usrRoot));
                    return FileVisitResult.CONTINUE;
                }
            });
            if(!suppressCallBack){
                callBack.call(list);
            }
        } catch (IOException e) {
            logger.error("Populate dir error: ", e);
        }
        return list;
    }

    public RandomAccessFile getFileForWrite(FSObject file){
        Path p = location.resolve(file.getPath());
        RandomAccessFile raf;
        try {
            if (!Files.exists(p)) {
                Files.createFile(p);
            }
            raf = new RandomAccessFile(p.toString(), "rw");
            return raf;
        } catch (IOException e) {
            logger.error("Get file for write error: ", e);
        }
        return null;
    }

    private boolean isFreeSpaceEnough(long forWrite){
        return this.user.getFree() >= forWrite;
    }
}
