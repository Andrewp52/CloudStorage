package com.polozov.cloudstorage.lesson02;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.StringJoiner;
import java.util.stream.Stream;

// Class for disk operations calling from CommandHandler
// Needs callback implementation to send results back/
public class DiskWorker {
    static final Path SRVROOT = Path.of("server");
    String nick;
    Path usrRoot;
    Path location;
    CallBack callBack;

    public DiskWorker(String nick, CallBack callBack) {
        this.nick = nick;
        this.usrRoot = SRVROOT.resolve(Path.of(nick));
        this.location = usrRoot;
        this.callBack = callBack;
        if(!Files.exists(usrRoot)){
            try {
                Files.createDirectory(usrRoot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Path getLocation() {
        return location;
    }

    public String getNick() {
        return nick;
    }

    public void changeUserRoot(String newNick) throws IOException {
        Path newRoot = SRVROOT.resolve(Path.of(newNick));
        if(!Files.exists(newRoot)){
            Files.move(usrRoot, newRoot, StandardCopyOption.ATOMIC_MOVE);
            this.location = newRoot;
            this.nick = newNick;
            callBack.callBack("ok\n");
        } else {
            callBack.callBack("nickname is occupied\n");
        }
    }

    void getFilesList() throws IOException {
        StringJoiner sj = new StringJoiner("\n", "", "\n");
        Stream<Path> sp = Files.list(location);
        sp.forEach(path -> sj.add(path.getFileName().toString()));
        sp.close();
        callBack.callBack(sj.toString());
    }

    // Creates new file with given name
    void touchFile(String name) {
        String ans = String.format("%s created\n", name);
        try {
            Files.createFile(this.location.resolve(name));
        } catch (IOException e){
            ans = String.format("Can`t create file %s\n", name);
        }
        callBack.callBack(ans);
    }

    void mkdir(String dir) {
        String ans = String.format("%s created\n", dir);
        try {
            Files.createDirectories(Path.of(this.location.toString(), dir));
        } catch (IOException e){
            ans = String.format("Can`t create path %s\n", dir);
        }
        callBack.callBack(ans);
    }

    // Changes client`s location directory (goes back up to client`s root directory)
    void changeDirectory(String dir){
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
        callBack.callBack("");
    }

    // Removes file or directory (except of not empty directory) returns String - operation result.
    void removeFile(String name) {
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
        callBack.callBack(ans);
    }

    // Reads content of a given file
    void catFile(String name) throws IOException {
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
        callBack.callBack(ans);
    }

    // Copies file or directory (with inner content)
    void copyFile(String name, String dest){
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
        callBack.callBack(ans);
    }

    public Path usrRoot() {
        return usrRoot;
    }
}
