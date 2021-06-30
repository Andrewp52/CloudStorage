package com.pae.cloudstorage.server.common;

import com.pae.cloudstorage.server.common.CallBack;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.StringJoiner;
import java.util.stream.Stream;


// Class for disk operations.
// Needs Callback implementation.
public class DiskWorker {
    static final Path SRVROOT = Path.of("server");
    String nick;
    Path usrRoot;
    Path location;
    CallBack callBack;

    public DiskWorker(String nick, CallBack callBack) throws IOException {
        this.nick = nick;
        this.usrRoot = SRVROOT.resolve(Path.of(nick));
        this.location = usrRoot;
        this.callBack = callBack;
        if(!Files.exists(usrRoot)){
            Files.createDirectory(SRVROOT.resolve(Path.of(nick)));
        }
    }

    public Path getLocation() {
        return location;
    }

    public String getNick() {
        return nick;
    }

    // Changes root directory when user changes it`s profile data (nickname)
    public void changeUserRoot(String newNick) throws IOException {
        Path newRoot = SRVROOT.resolve(Path.of(newNick));
        if(!Files.exists(newRoot)){
            Files.move(usrRoot, newRoot, StandardCopyOption.ATOMIC_MOVE);
            this.location = newRoot;
            this.nick = newNick;
            callBack.call("ok\n");
        } else {
            callBack.call("nickname is occupied\n");
        }
    }

    public void getFilesList() throws IOException {
        StringJoiner sj = new StringJoiner("*", "<DIR_LIST>", "");
        Stream<Path> sp = Files.list(location);
        sp.forEach(path ->
            sj.add(Files.isDirectory(path) ? "<D>" + path.getFileName().toString() : path.getFileName().toString()
        ));
        sp.close();
        callBack.call(sj.toString());
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
        String ans = String.format("%s created\n", dir);
        try {
            Files.createDirectories(Path.of(this.location.toString(), dir));
        } catch (IOException e){
            ans = String.format("Can`t create path %s\n", dir);
        }
        callBack.call(ans);
    }

    // Changes client`s location directory (goes back up to client`s root directory)
    // Calls getFilesList at and for client`s list (remote) update
    public void changeDirectory(String dir) throws IOException {
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
        String ans = "ok\n";
        try {
            Files.delete(p);
        } catch (NoSuchFileException e) {
            ans = "Error : no such file - " + name + "\n";
        } catch (DirectoryNotEmptyException e){
            ans = "Error : directory not empty - " + name + "\n";
        } catch (IOException e) {
            e.printStackTrace();
            ans = "Error : IO -" + name + "\n";
        }
        callBack.call(ans);
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

    public Path usrRoot() {
        return usrRoot;
    }
}
