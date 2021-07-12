package com.pae.cloudstorage.server.storage;

import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.common.FSObject;
import com.pae.cloudstorage.server.Main;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.pae.cloudstorage.common.Command.*;

// Class for filesystem operations.
// Needs Callback implementation.

public class StorageWorker {
    private static final Path SRVROOT;
    private Path usrRoot;
    private Path location;
    private CallBack callBack;
    static {
        SRVROOT = Path.of(Main.getSrvConfig().get("srvroot"));
    }
    public StorageWorker(int uid, CallBack callBack) throws IOException {
        this.callBack = callBack;
        this.usrRoot = SRVROOT.resolve(Path.of(String.valueOf(uid)));
        if(!Files.exists(usrRoot)){
            Files.createDirectory(SRVROOT.resolve(Path.of(String.valueOf(uid))));
        }
        location = usrRoot;
    }

    public Path getLocation() {
        return location;
    }

    public void setLocation(Path location) {
        this.location = location;
    }

    public void changeUserRoot(String newNick) throws IOException {
        Path newRoot = SRVROOT.resolve(Path.of(newNick));
        if(!Files.exists(newRoot)){
            Files.move(usrRoot, newRoot, StandardCopyOption.ATOMIC_MOVE);
            this.location = newRoot;
            callBack.call("ok\n");
        } else {
            callBack.call("nickname is occupied\n");
        }
    }

    public void getFilesList(){
        List<FSObject> dirList = new ArrayList<>();
        Stream<Path> sp = null;
        try {
            sp = Files.list(location);
            sp.forEach(path -> dirList.add(new FSObject(path, location)));
            sp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        callBack.call(dirList);
    }

    // Creates new file with given name
    public void touchFile(String name) {
        String ans = String.format("%s created\n", name);
        try {
            Files.createFile(this.location.resolve(name));
        } catch (IOException e){
            ans = String.format("Can`t create file %s\n", name);
        }
        callBack.call(ans);
    }

    // Makes directory(ies) with given path.
    public void mkdir(String dir) {
        if(location != null){
            try {
                Files.createDirectories(Path.of(this.location.toString(), dir));
                getFilesList();
            } catch (IOException e){
                e.printStackTrace();
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
            Files.delete(p);
            callBack.call(CMD_SUCCESS);
        } catch (DirectoryNotEmptyException e){
            callBack.call(FILE_DNE);
        } catch (IOException e) {
            callBack.call(CMD_FAIL);
            e.printStackTrace();
        }
    }

    public void removeDirRecursive(String name){
        Path p = location.resolve(name);
        try{
            Files.walkFileTree(p, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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
            callBack.call(CMD_SUCCESS);
        } catch (IOException e){
            e.printStackTrace();
            callBack.call(CMD_FAIL);
        }
    }

    // Reads content of a given file
    public void catFile(String name) throws IOException {
        Path p = location.resolve(name);
        String ans;
        try {
            if(!Files.exists(p) || Files.isDirectory(p)){
                throw new NoSuchFileException("invalid filename");
            }
            StringBuilder sb = new StringBuilder();
            FileChannel fc = new RandomAccessFile(p.toString(), "r").getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(12);

            while (fc.read(buffer) >=0){
                buffer.flip();
                while (buffer.hasRemaining()){
                    sb.append((char) buffer.get());
                }
                buffer.rewind();
            }
            fc.close();
            ans = sb.toString();
        } catch (NoSuchFileException e){
            ans = "invalid filename";
        }
        callBack.call(ans);
    }

    public void searchFile(String name) {
        List<FSObject> found = new ArrayList<>();
        try {
            Files.walkFileTree(location, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.getFileName().toString().toLowerCase().contains(name.toLowerCase())) {
                        found.add(new FSObject(dir, location));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().toLowerCase().contains(name.toLowerCase())) {
                        found.add(new FSObject(file, location));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        callBack.call(found);
    }

    // Copies file or directory (with inner content)
    public void copyFile(String name, String dest){
        Path src = location.resolve(name);
        Path dst = location.resolve(dest);
        String ans = "ok\n";
        try {
            if(Files.isDirectory(src)){
                Files.walkFileTree(src, new SimpleFileVisitor<Path>(){
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Files.createDirectories(dst.resolve(src.relativize(dir)));
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.copy(file, dst.resolve(src.relativize(file)));
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                if(Files.isDirectory(dst)){
                    Files.copy(src, dst.resolve(location.relativize(src)));
                } else {
                    Files.copy(src, dst);
                }
            }
        } catch (IOException e){
            ans = "Copy error: " + e.getMessage() + "\n";
        }
        callBack.call(ans);
    }

    public File getFile(String name) {
        return new File(location.resolve(Path.of(name)).toString());
    }

    // Retrieves all files and directories from given directory
    public void getDirectoryPaths(String token) {
        List<FSObject> list = new ArrayList<>();
        try {
            Files.walkFileTree(location.resolve(token), new SimpleFileVisitor<Path>(){
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
            callBack.call(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            e.printStackTrace();
        }
        return null;
    }
}
