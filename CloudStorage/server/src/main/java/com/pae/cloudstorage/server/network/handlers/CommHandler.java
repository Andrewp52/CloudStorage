package com.pae.cloudstorage.server.network.handlers;

import com.pae.cloudstorage.common.DiskWorkerRemote;
import com.pae.cloudstorage.common.User;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import static com.pae.cloudstorage.common.Command.*;

/**
 * Main command handler.
 * Adding automatically by AuthHandler if authentication succeed.
 * When added, it removes AuthHandler from pipeline.
 * Serves client`s commands (filesystem navigation & basic actions)
 * When it reads upload / download command it adds FileReceiverHandler or FileSenderHandler
 * and transfers there last command.
 * TODO: inmplement upload / download command handling.
 */
public class CommHandler extends SimpleChannelInboundHandler<String> {
    User user;
    ChannelHandlerContext context;
    DiskWorkerRemote worker;
    public CommHandler(User user) {
        this.user = user;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if(ctx.channel().pipeline().get(AuthHandler.class) != null){
            ctx.channel().pipeline().remove(AuthHandler.class);
            worker = new DiskWorkerRemote(this.user.getNick(), (a) -> sendObject(a[0], ctx));   // Callback impl. for DiskWorker.
            context = ctx;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String command = s.replace("\n", "").replace("\r", "");
        if(AUTH_OUT.name().equals(command)){
            ctx.channel().close().syncUninterruptibly();
            ctx.channel().closeFuture();
            return;
        }
        workWithCommand(command);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        ctx.channel().writeAndFlush(cause);
        cause.printStackTrace();
    }

    private void workWithCommand(String command) throws IOException {
        String[] tokens;
        if (command.contains(FILE_LIST.name())) {
            worker.getFilesList();
        } else if (command.contains(FILE_MKDIR.name())) {
            tokens = command.split(" ");
            if (tokens.length > 1) {
                worker.mkdir(tokens[1]);
            }
        } else if (command.contains(FILE_CD.name())) {
            tokens = command.split(" ");
            if (tokens.length > 1) {
                worker.changeDirectory(tokens[1]);
            }
        } else if (command.contains(FILE_REMOVE.name())) {
            tokens = command.split(" ");
            if (tokens.length > 1) {
                worker.removeFile(tokens[1]);
                context.channel().writeAndFlush(context.alloc().heapBuffer(1).writeByte(0));
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

//    private void sendString (String s, ChannelHandlerContext ctx){
//        ByteBuf bb = ctx.alloc().heapBuffer();
//        bb.writeInt(0).writeInt(s.length()).writeBytes(s.getBytes());
//        try {
//            ctx.channel().writeAndFlush(bb).sync();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    private void sendObject(Object o, ChannelHandlerContext ctx){
        ByteBuf bb = ctx.alloc().heapBuffer();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream ous = new ObjectOutputStream(bos);
            ous.writeObject(o);
            bb.writeInt(0).writeInt(bos.size()).writeBytes(bos.toByteArray());
            ctx.writeAndFlush(bb);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
