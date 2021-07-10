package com.pae.cloudstorage.server.network.handlers;

import com.pae.cloudstorage.common.FSObject;
import com.pae.cloudstorage.common.User;
import com.pae.cloudstorage.server.storage.FSWorker;
import io.netty.channel.*;
import io.netty.handler.stream.ChunkedFile;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

import static com.pae.cloudstorage.common.Command.*;

/**
 * Main command handler.
 * It is adds automatically by AuthHandler if authentication succeed.
 * When added, it removes AuthHandler from pipeline.
 * Serves client`s commands (filesystem navigation & basic actions)
 * When it reads upload / download command it adds FileReceiverHandler or FileSenderHandler
 * and transfers there last command.
 */
public class CommInHandler extends SimpleChannelInboundHandler<String> {
    private static final String DELIM = "%";
    private ChannelHandlerContext context;
    private User user;
    private FSWorker worker;
    private final Logger logger = LogManager.getLogger(CommInHandler.class);

    public CommInHandler(User user) {
        this.user = user;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if(ctx.channel().pipeline().get(AuthHandler.class) != null){
            worker = new FSWorker(this.user.getNick(), (a) -> ctx.channel().writeAndFlush(a[0])); // Callback impl. for FSWorker.
            ctx.channel().pipeline().remove(AuthHandler.class);
            ctx.channel().pipeline().addLast(new ObjectOutHandler());
            ctx.channel().pipeline().get(FileReceiverHandler.class).setStorageWorker(worker);
        }
        this.context = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String command = s.replace("\n", "").replace("\r", "");
        if(AUTH_OUT.name().equals(command)){
            ctx.channel().close().syncUninterruptibly();
            ctx.channel().closeFuture();
        } else if(PROFILE_REQ.name().equals(command)){
            setObjOutHandler(ctx);
            ctx.channel().writeAndFlush(user);
        } else if (command.contains(FILE_DOWNLOAD.name())) {
            String[] tokens = command.split(DELIM, 2);
            setFileOutHandler(ctx);
            ctx.channel().writeAndFlush(new ChunkedFile(worker.getFile(tokens[1])));
        } else {
            setObjOutHandler(ctx);
            workWithCommand(command, ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        logger.error("Command handler error: ", cause);
    }

    public void writeThrough(Object o){
        this.context.channel().writeAndFlush(o);
    }

    private void workWithCommand(String command, ChannelHandlerContext ctx) throws IOException {
        String[] tokens;
        if (command.contains(FILE_LIST.name())) {
            worker.getFilesList();
        } else if (command.contains(FILE_MKDIR.name())) {
            tokens = command.split(DELIM);
            if (tokens.length > 1) {
                worker.mkdir(tokens[1]);
            }
        } else if (command.contains(FILE_CD.name())) {
            tokens = command.split(DELIM);
            if (tokens.length > 1) {
                worker.changeDirectory(tokens[1]);
            }
        } else if (command.contains(FILE_REMOVE.name())) {
            tokens = command.split(DELIM);
            if (tokens.length > 1) {
                worker.removeFile(tokens[1]);
            }
        } else if (command.contains(FILE_SEARCH.name())) {
            tokens = command.split(DELIM, 2);
            if (tokens.length > 1) {
                worker.searchFile(tokens[1]);
            }
        } else if(command.contains(FILE_PATHS.name())){
            tokens = command.split(DELIM, 2);
            worker.getDirectoryPaths(tokens[1]);
        } else if(command.contains(FILE_UPLOAD.name())){
            tokens = command.split(DELIM);
            if(tokens.length == 4){
                FSObject f = new FSObject(tokens[1], tokens[2], Long.parseLong(tokens[3]), false);
                ctx.channel().pipeline().fireUserEventTriggered(f);
                ctx.channel().writeAndFlush(FILE_UPLOAD.name());
            }
        }
    }

    private void setFileOutHandler(ChannelHandlerContext ctx){
        if(ctx.channel().pipeline().get(ChunkedWriteHandler.class) == null){
            ctx.channel().pipeline().remove(ObjectOutHandler.class);
            ctx.channel().pipeline().addLast(new FileSenderHandler());
        }
    }

    private void setObjOutHandler(ChannelHandlerContext ctx){
        if(ctx.channel().pipeline().get(ObjectOutHandler.class) == null){
            ctx.channel().pipeline().remove(FileSenderHandler.class);
            ctx.channel().pipeline().addLast(new ObjectOutHandler());
        }
    }
}
