package com.pae.cloudstorage.server.network.handlers;


import com.pae.cloudstorage.common.DiskWorker;
import com.pae.cloudstorage.common.DiskWorkerRemote;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.pae.cloudstorage.server.data.DataService;
import java.io.IOException;

import static com.pae.common.Command.*;
/**
    General class for handle plain string commands from client.
    Contains Callback implementation for DiscWorker (disk navigation)
*/
 public class CommandHandler extends SimpleChannelInboundHandler<String> {
    private final DataService dataService;
    private DiskWorker worker;
    private Logger logger = LogManager.getLogger(CommandHandler.class);
    private String nick;

    public CommandHandler(DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    // TODO: look at auth todo))
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String command = s.replace("\n", "").replace("\r", "");
        if(this.nick == null){
            if(auth(command)){
                worker = new DiskWorkerRemote(this.nick, o -> ctx.channel().writeAndFlush(o[0]));     // Callback impl. for DiskWorker.
                ctx.channel().writeAndFlush(AUTH_OK.name());
            } else {
                ctx.channel().writeAndFlush(AUTH_FAIL.name());
            }
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
        if ("ls".equals(command)) {
            worker.getFilesList();
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

    /** Auth method
     * Asks DataService for user authentication
     * Retrieves User object for DiskWorker configuration and matching user profile with connection

     TODO: Modify method and caller for database interaction and work with User object.
            Add connection - profile matching.
    */
    private boolean auth(String command){
        String[] tokens;
        if (command.contains("auth")) {
            tokens = command.split(" ");
            if (tokens.length > 1) {
                this.nick = tokens[1];
                return true;
            } else {
                return false;
            }
        }
        return false;
    }


}
