package com.pae.cloudstorage.server.network.handlers;

import com.pae.cloudstorage.common.User;
import com.pae.cloudstorage.server.filesystem.FSWorker;
import io.netty.channel.*;
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
 * TODO: inmplement upload / download command handling.
 */
public class CommInHandler extends SimpleChannelInboundHandler<String> {
    private User user;
    private ChannelHandlerContext context;
    private FSWorker worker;
    Logger logger = LogManager.getLogger(CommInHandler.class);
    public CommInHandler(User user) {
        this.user = user;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if(ctx.channel().pipeline().get(AuthHandler.class) != null){
            ctx.channel().pipeline().remove(AuthHandler.class);
            worker = new FSWorker(this.user.getNick(), (a) -> ctx.channel().writeAndFlush(a[0])); // Callback impl. for FSWorker.
            context = ctx;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String command = s.replace("\n", "").replace("\r", "");
        if(AUTH_OUT.name().equals(command)){
            ctx.channel().close().syncUninterruptibly();
            ctx.channel().closeFuture();
        } else if(PROFILE_REQ.name().equals(command)){
            ctx.channel().writeAndFlush(user);
        } else {
            workWithCommand(command);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        logger.error("Command handler error: ", cause);
        ctx.channel().writeAndFlush(cause);
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
        } else if (command.contains(FILE_REMOVE.name())) {          //TODO: REWORK IT !!!!
            tokens = command.split(" ");
            if (tokens.length > 1) {
                worker.removeFile(tokens[1]);
                context.channel().writeAndFlush(context.alloc().heapBuffer(1).writeByte(0));
            }
        } else if (command.contains(FILE_SEARCH.name())) {
            tokens = command.split(" ", 2);
            if (tokens.length > 1) {
                worker.searchFile(tokens[1]);
            }
        }
    }
}
