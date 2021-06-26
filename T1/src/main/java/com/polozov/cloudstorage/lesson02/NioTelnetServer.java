package com.polozov.cloudstorage.lesson02;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

public class NioTelnetServer {
    private static final String LS_COMMAND = "\tls     view all files from current directory\n";
    private static final String MKDIR_COMMAND = "\tmkdir     view all files from current directory\n";
    private static final String TOUCH_COMMAND = "\ttouch (filename)     creates a new file\n";
    private static final String CD_COMMAND = "\tcd (path | .. | ~ )     changes current directory\n";
    private static final String RM_COMMAND = "\trm (filename)     removes file or directory\n";
    private static final String CAT_COMMAND = "\tcat (filename)     displays file content\n";
    private static final String CHANGENICK_COMMAND = "\tchangenick (new name)     changes user`s nick\n";
    private static final String COPY_COMMAND = "\tcopy (src) (target)     copies file or directory\n";

    private final ByteBuffer buffer = ByteBuffer.allocate(512);

    private Map<SocketAddress, String> clients = new HashMap<>();
    private Map<SocketAddress, Path> clientLocation = new HashMap<>();
    private final Path srvRoot = Path.of("server");

    public NioTelnetServer() throws Exception {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(5679));
        server.configureBlocking(false);
        Selector selector = Selector.open();

        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started");
        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                } else if (key.isReadable()) {
                    handleRead(key, selector);
                }
                iterator.remove();
            }
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
        channel.configureBlocking(false);
        System.out.println("Client connected. IP:" + channel.getRemoteAddress());
        channel.register(selector, SelectionKey.OP_READ, "skjghksdhg");
        channel.write(ByteBuffer.wrap("Hello user!\n".getBytes(StandardCharsets.UTF_8)));
        channel.write(ByteBuffer.wrap("Send: auth nickname to start session\n".getBytes(StandardCharsets.UTF_8)));
    }


    private void handleRead(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketAddress client = channel.getRemoteAddress();
        int readBytes = channel.read(buffer);

        if (readBytes < 0) {
            channel.close();
            return;
        } else  if (readBytes == 0) {
            return;
        }

        buffer.flip();
        StringBuilder sb = new StringBuilder();
        while (buffer.hasRemaining()) {
            sb.append((char) buffer.get());
        }
        buffer.clear();

        // TODO: 21.06.2021
        // touch (filename) - создание файла
        // mkdir (dirname) - создание директории
        // cd (path | ~ | ..) - изменение текущего положения
        // rm (filename / dirname) - удаление файла / директории
        // copy (src) (target) - копирование файлов / директории
        // cat (filename) - вывод содержимого текстового файла
        // changenick (nickname) - изменение имени пользователя

        // добавить имя клиента

        if (key.isValid()) {
            String command = sb.toString()
                    .replace("\n", "")
                    .replace("\r", "");
            String[] tokens;
            if (clients.containsKey(client)) {
                if ("--help".equals(command)) {
                    sendMessage(LS_COMMAND, selector, client);
                    sendMessage(MKDIR_COMMAND, selector, client);
                    sendMessage(TOUCH_COMMAND, selector, client);
                    sendMessage(CD_COMMAND, selector, client);
                    sendMessage(RM_COMMAND, selector, client);
                    sendMessage(CAT_COMMAND, selector, client);
                    sendMessage(CHANGENICK_COMMAND, selector, client);
                    sendMessage(COPY_COMMAND, selector, client);
                } else if ("ls".equals(command)) {
                    sendMessage(getFilesList(client).concat("\n"), selector, client);
                } else if (command.contains("changenick")) {
                    tokens = command.split(" ");
                    if (tokens.length > 1) {
                        sendMessage(changeNick(client, tokens[1]), selector, client);
                    }
                } else if (command.contains("mkdir")) {
                    tokens = command.split(" ");
                    if (tokens.length > 1) {
                        sendMessage(mkdir(client, tokens[1]), selector,client);
                    }
                } else if (command.contains("cd")) {
                    tokens = command.split(" ");
                    if (tokens.length > 1) {
                        changeDirectory(client, tokens[1]);
                    }
                } else if (command.contains("rm")){
                    tokens = command.split(" ");
                    if (tokens.length > 1) {
                        String res = removeFile(client, tokens[1]);
                        sendMessage(res, selector, client);
                    }
                } else if (command.contains("touch")){
                    tokens = command.split(" ");
                    if (tokens.length > 1) {
                        sendMessage(touchFile(client, tokens[1]), selector, client);
                    }
                } else if (command.contains("cat")){
                    tokens = command.split(" ");
                    if (tokens.length > 1) {
                        sendMessage(catFile(client, tokens[1]) + "\n", selector, client);
                    }
                } else if (command.contains("copy")){
                    tokens = command.split(" ");
                    if (tokens.length > 2) {
                        sendMessage(copyFile(client, tokens[1], tokens[2]), selector, client);
                    }
                }
                sendMessage(getPrompt(client), selector, client);

            } else if (command.contains("auth")) {
                tokens = command.split(" ");
                if (tokens.length > 1) {
                    String nick = tokens[1];
                    clients.put(client, nick);
                    Path usrRoot = srvRoot.resolve(nick);
                    if (!Files.exists(usrRoot)) {
                        Files.createDirectory(usrRoot);
                    }
                    clientLocation.put(client, usrRoot);
                    sendMessage(nick + ": session started\n", selector, client);
                    sendMessage("Enter --help for support info\n",selector, client);
                    sendMessage(getPrompt(client), selector, client);
                }
            }
        }
    }

    private void sendMessage(String message, Selector selector, SocketAddress client) throws IOException {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                if (((SocketChannel) key.channel()).getRemoteAddress().equals(client)) {
                    ((SocketChannel) key.channel()).write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                }
            }
        }
    }

    // List files in current directory
    // To avoid troubles with visited dirs deletion - Files.list STREAM MUST BE CLOSED !!!!
    private String getFilesList(SocketAddress client) throws IOException {
        StringJoiner sj = new StringJoiner("\n", "", "\n");
        Stream<Path> sp = Files.list(clientLocation.get(client));
        sp.forEach(path -> sj.add(path.getFileName().toString()));
        sp.close();
        return sj.toString();
    }

    public static void main(String[] args) throws Exception {
        new NioTelnetServer();
    }
    // Generates terminal prompt using client`s name & current location.
    private String getPrompt(SocketAddress client){
        StringJoiner sj = new StringJoiner("]", "[", File.separator + ">");
        sj.add(clients.get(client))
                .add((srvRoot.resolve(clients.get(client)).relativize(clientLocation.get(client))).toString());
        return sj.toString();
    }

    // Creates new file with given name
    private String touchFile(SocketAddress client, String name) {
        try {
            Files.createFile(clientLocation.get(client).resolve(name));
        } catch (IOException e){
            return String.format("Can`t create file %s\n", name);
        }
        return String.format("%s created\n", name);
    }

    private String mkdir(SocketAddress client, String dir) {
        try {
            Files.createDirectories(Path.of(clientLocation.get(client).toString(), dir));
        } catch (IOException e){
            return String.format("Can`t create path %s\n", dir);
        }
        return String.format("%s created\n", dir);
    }

    // Changes client`s nickname with moving (renaming) it`s root directory
    private String changeNick(SocketAddress client, String newNick) throws IOException {
        Path newRoot = srvRoot.resolve(newNick);
        if(!Files.exists(newRoot)){
            Path usrRoot = srvRoot.resolve(clients.get(client));
            Files.move(usrRoot, newRoot, StandardCopyOption.ATOMIC_MOVE);
            clients.put(client, newNick);
            clientLocation.put(client, srvRoot.resolve(newNick));
            return "ok\n";
        }
        return "nickname is occupied\n";
    }

    // Changes client`s location directory (goes back up to client`s root directory)
    private void changeDirectory(SocketAddress client, String dir){
        Path current = clientLocation.get(client);
        if("..".equals(dir)){
            if(!current.equals(srvRoot.resolve(clients.get(client)))){
                clientLocation.put(client, current.getParent());
            }
        } else if("~".equals(dir)){
            clientLocation.put(client, srvRoot.resolve(clients.get(client)));
        } else {
            Path newLocation = clientLocation.get(client).resolve(dir);
            if(Files.exists(newLocation)){
                clientLocation.put(client, newLocation);
            }
        }
    }

    // Removes file or directory (except of not empty directory) returns String - operation result.
    private String removeFile(SocketAddress client, String name) {
        Path p = clientLocation.get(client).resolve(name);
        try {
            Files.delete(p);
            return "ok\n";
        } catch (NoSuchFileException e) {
            return "Error : no such file - " + name + "\n";
        } catch (DirectoryNotEmptyException e){
            return "Error : directory not empty - " + name + "\n";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error : IO -" + name + "\n";
        }
    }

    // Reads content of a given file
    private String catFile(SocketAddress client, String name) throws IOException {
        Path p = clientLocation.get(client).resolve(name);
        if(!Files.exists(p) || Files.isDirectory(p)){
            return "invalid filename";
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
        return sb.toString();
    }

    // Copies file or directory (with inner content)
    private String copyFile(SocketAddress client, String name, String dest){
        Path src = clientLocation.get(client).resolve(name);
        Path dst = clientLocation.get(client).resolve(dest);
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
                    Files.copy(src, dst.resolve(clientLocation.get(client).relativize(src)));
                } else {
                    Files.copy(src, dst);
                }

            }
            return "ok\n";
        } catch (IOException e){
            return "Copy error: " + e.getMessage() + "\n";
        }
    }
}
