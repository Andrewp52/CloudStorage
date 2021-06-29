package com.polozov.cloudstorage.lesson02;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.net.SocketAddress;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public class CommandHandler extends SimpleChannelInboundHandler<String> {
    private static final String LS_COMMAND = "\tls     view all files from current directory\n";
    private static final String MKDIR_COMMAND = "\tmkdir     view all files from current directory\n";
    private static final String TOUCH_COMMAND = "\ttouch (filename)     creates a new file\n";
    private static final String CD_COMMAND = "\tcd (path | .. | ~ )     changes current directory\n";
    private static final String RM_COMMAND = "\trm (filename)     removes file or directory\n";
    private static final String CAT_COMMAND = "\tcat (filename)     displays file content\n";
    private static final String CHANGENICK_COMMAND = "\tchangenick (new name)     changes user`s nick\n";
    private static final String COPY_COMMAND = "\tcopy (src) (target)     copies file or directory\n";
    private static final String QUIT_COMMAND = "\tquit     end session & close connection\n";


    private Channel channel;
    private static final ConcurrentHashMap<SocketAddress, String> clients = new ConcurrentHashMap<>();
    private DiskWorker worker;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel = ctx.channel();
        System.out.printf("Client %s connected\n", ctx.channel().remoteAddress());
        ctx.writeAndFlush("Hello user!\nSend {auth [nickname]} to start session\n>");
    }
    // Calls when client disconnects
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        clients.remove(ctx.channel().remoteAddress());
        System.out.println("Client disconnected");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String command = s.replace("\r", "").replace("\n", "");
        String nick = clients.get(ctx.channel().remoteAddress());
        if(nick == null){
            auth(ctx.channel().remoteAddress(), command);
        } else {
            handleMessage(command);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.writeAndFlush("Something totally fucked up...");
        ctx.close();
    }


    private void handleMessage(String command) throws Exception{
        String[] tokens;
            if ("--help".equals(command)) {
                channel.writeAndFlush(getHelp() + getPrompt(channel.remoteAddress()));
            } else if ("ls".equals(command)) {
                worker.getFilesList();
            } else if (command.contains("changenick")) {
                tokens = command.split(" ");
                if (tokens.length > 1) {
                    worker.changeUserRoot(tokens[1]);
                    if(!clients.get(channel.remoteAddress()).equals(worker.getNick())){
                        clients.put(channel.remoteAddress(), tokens[1]);
                    }
                }
            } else if (command.contains("mkdir")) {
                tokens = command.split(" ");
                if (tokens.length > 1) {
                    worker.mkdir(tokens[1]);
                }
            } else if (command.contains("cd")) {
                tokens = command.split(" ");
                if (tokens.length > 1) {
                    worker.changeDirectory(tokens[1]);
                }
            } else if (command.contains("rm")) {
                tokens = command.split(" ");
                if (tokens.length > 1) {
                    worker.removeFile(tokens[1]);
                }
            } else if (command.contains("touch")) {
                tokens = command.split(" ");
                if (tokens.length > 1) {
                    worker.touchFile(tokens[1]);
                }
            } else if (command.contains("cat")) {
                tokens = command.split(" ");
                if (tokens.length > 1) {
                    worker.catFile(tokens[1]);
                }
            } else if (command.contains("copy")) {
                tokens = command.split(" ");
                if (tokens.length > 2) {
                    worker.copyFile(tokens[1], tokens[2]);
                }
            }
    }
    // User auth method. Runs first and also creates DiscWorker and it`s callback.
    private boolean auth(SocketAddress client, String command){
        String[] tokens;
        if (command.contains("auth")) {
            tokens = command.split(" ");
            if (tokens.length > 1) {
                String nick = tokens[1];
                clients.put(client, nick);
                worker = new DiskWorker(nick, args -> {
                    channel.write(args[0]);
                    channel.writeAndFlush(getPrompt(channel.remoteAddress()));
                });
                channel.writeAndFlush("Session started\nEnter --help for support info\n" + getPrompt(channel.remoteAddress()));
                return true;
            } else {
                channel.writeAndFlush("Something wrong. Try again\n>");
                return false;
            }
        }
        return false;
    }

    // Generates terminal prompt using client`s name & current location.
    private String getPrompt(SocketAddress client){
        StringJoiner sj = new StringJoiner("]", "[", File.separator + ">");
        sj.add(clients.get(client))
                .add((this.worker.usrRoot().relativize(this.worker.getLocation())).toString());           // ??????
        return (sj.toString());
    }

    private String getHelp(){
        return new StringBuilder().append(LS_COMMAND)
                .append(MKDIR_COMMAND)
                .append(TOUCH_COMMAND)
                .append(CD_COMMAND)
                .append(RM_COMMAND)
                .append(CAT_COMMAND)
                .append(CHANGENICK_COMMAND)
                .append(COPY_COMMAND)
                .append(QUIT_COMMAND)
                .toString();
    }

}
