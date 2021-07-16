package com.pae.cloudstorage.server.network.handlers;

import com.pae.cloudstorage.common.User;
import com.pae.cloudstorage.server.data.DataService;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import static com.pae.cloudstorage.common.Command.*;

/** Auth handler class
 * Added to pipeline by default at start after StringDecoder.
 * Asks DataService for user authentication.
 * By succeeded authentication adds CommandHandler to the pipeline.
 * Sends auth status to client.
*/

 public class AuthHandler extends SimpleChannelInboundHandler<String> {
    private DataService ds;
    private CommInHandler commctx;

    public AuthHandler(DataService ds) {
        this.ds = ds;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        commctx = ctx.channel().pipeline().get(CommInHandler.class);
        String command = s.replace("\n", "").replace("\r", "");
        if(AUTH_OUT.name().equals(command)) {
            s = AUTH_OUT.name();
            ctx.channel().close().syncUninterruptibly();
            ctx.channel().closeFuture();
            return;
        } else if(command.contains(AUTH_REQ.name())) {
            User u = auth(command);
            if(u != null){
                s = AUTH_OK.name();
                commctx.setup(u);
            } else {
                s = AUTH_FAIL.name();
            }
        } else if(command.contains(REG_REQ.name())){
            s = register(command) ? REG_OK.name() : REG_FAIL.name();
        }
        commctx.getContext().fireChannelRead(s);
        ctx.fireChannelReadComplete();
        ctx.channel().pipeline().remove(this);
    }

    private User auth(String command){
        String[] tokens = command.split(CommInHandler.getDelimiter());
        return ds.authUser(tokens[1], tokens[2]);
    }

    private boolean register(String command){
        String[] tokens = command.split(CommInHandler.getDelimiter());
        if(tokens.length == 6){
            return ds.registerUser(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
        }
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close().syncUninterruptibly();
        ctx.channel().closeFuture();
    }
}
