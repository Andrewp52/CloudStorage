package com.pae.cloudstorage.common;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// Class for disk operations.
// Needs Callback implementation.

public class DiskWorker {
    Path location;
    CallBack callBack;

    public DiskWorker(CallBack callBack) {
        this.callBack = callBack;
    }

    public Path getLocation() {
        return location;
    }

    public void setLocation(Path location) {
        this.location = location;
    }

    public void getFilesList() {
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
        callBack.call(fList);
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

    public void changeDirectory(String dir) {
        Path current = location;
        if(location != null){
            if("..".equals(dir)){
                location = current.getParent();
            } else if("~".equals(dir)){
                location = null;
            } else {
                location = location.resolve(dir);
            }
        }
        getFilesList();
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
}
