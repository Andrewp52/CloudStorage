package com.pae.cloudstorage.server.network.handlers;

import com.pae.cloudstorage.common.FSObject;
import com.pae.cloudstorage.common.User;
import com.pae.cloudstorage.server.data.DataService;
import com.pae.cloudstorage.server.storage.StorageWorker;
import io.netty.channel.*;
import io.netty.handler.stream.ChunkedFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.StringJoiner;

import static com.pae.cloudstorage.common.Command.*;

/**
 * Main command handler.
 * It is adds automatically by AuthHandler if authentication succeed.
 * When added, it removes AuthHandler from pipeline.
 * Serves client`s commands (filesystem navigation & actions)
 */
public class CommInHandler extends SimpleChannelInboundHandler<String> {
    private static final String DELIM = "%";
    private ChannelHandlerContext context;
    private DataService dataService;
    private User user;
    private StorageWorker worker;
    private final Logger logger = LogManager.getLogger(CommInHandler.class);

    // Calls by AuthHandler when user-auth is succeed
    // Initializes StorageWorker and FileReceiverHandler
    public void setup(User user, DataService dataService) throws IOException {
        this.user = user;
        this.dataService = dataService;
        worker = new StorageWorker(user, (a) -> context.fireChannelRead(a[0]));
        context.channel().pipeline().get(FileReceiverHandler.class).setStorageWorker(worker);
        context.channel().pipeline().get(FileReceiverHandler.class).setUser(user);
    }

    public static String getDelimiter() {
        return DELIM;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        this.dataService.updateStorageState(user);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {

        String command = s.replace("\n", "").replace("\r", "");
        if(AUTH_OUT.name().equals(command)){
            ctx.channel().close().syncUninterruptibly();
            ctx.channel().closeFuture();
        } else if(PROFILE_REQ.name().equals(command)){
            ctx.fireChannelRead(user);
        }  else {
            workWithCommand(command, ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        logger.error("Command handler error: ", cause);
    }

    ChannelHandlerContext getContext(){
        return context;
    }

    // Calls necessary methods depends on client`s command
    private void workWithCommand(String command, ChannelHandlerContext ctx) throws IOException {
        String[] tokens = command.split(DELIM);
        if(command.contains(PROFILE_UPD.name())){
            if(dataService.updateProfile(user.getId(), tokens[1], tokens[2], tokens[3], tokens[4])){
                ctx.fireChannelRead(PROFILE_UPD_Ok);
                user = dataService.getUserById(user.getId());
            } else {
                ctx.fireChannelRead(PROFILE_UPD_FAIL);
            }
        } else if(command.contains(SPACE.name())){
            ctx.fireChannelRead(user.getUsed());
        } else if (command.contains(FILE_LIST.name())) {
            worker.getFilesList();
        } else if (command.contains(LOCATION.name())) {
            worker.getLocation();
        } else if (command.contains(FILE_MKDIR.name())) {
            if (tokens.length > 1) {
                worker.mkdir(tokens[1]);
            }
        } else if (command.contains(FILE_CD.name())) {
            if (tokens.length > 1) {
                worker.changeDirectory(tokens[1]);
            }
        } else if (command.contains(FILE_REMOVEREC.name())) {
            if (tokens.length > 1) {
                worker.removeDirRecursive(tokens[1]);
            }
        } else if (command.contains(FILE_REMOVE.name())) {
            if (tokens.length > 1) {
                worker.removeFile(tokens[1]);
            }
        } else if (command.contains(FILE_SEARCH.name())) {
            if (tokens.length > 1) {
                worker.searchFile(tokens[1]);
            }
        } else if(command.contains(FILE_PATHS.name())){
            worker.populateDirectory(tokens[1]);
        } else if(command.contains(FILE_COPY.name())){
            worker.copyFile(tokens[1], tokens[2]);
        } else if(command.contains(FILE_MOVE.name())) {
            worker.moveFile(tokens[1], tokens[2]);
        } else if(command.contains(FILE_RENAME.name())){
            worker.rename(tokens[1], tokens[2]);
        } else if(command.contains(FILE_UPLOAD.name())){
            if(tokens.length == 4){
                FSObject f = new FSObject(tokens[1], tokens[2], Long.parseLong(tokens[3]), false);
                if(f.getSize() == 0){
                    worker.touchFile(f);
                } else {
                    ctx.channel().pipeline().fireUserEventTriggered(f);
                    ctx.fireChannelRead(FILE_UPLOAD);
                }
            }
        } else if (command.contains(FILE_DOWNLOAD.name())) {
            ctx.channel().writeAndFlush(new ChunkedFile(worker.getFile(tokens[1])));
        }
    }

}
