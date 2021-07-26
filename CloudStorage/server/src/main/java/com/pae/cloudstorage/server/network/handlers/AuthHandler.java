package com.pae.cloudstorage.server.network.handlers;

import com.pae.cloudstorage.common.User;
import com.pae.cloudstorage.server.data.DataService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import static com.pae.cloudstorage.common.Command.*;

/** Auth handler class
 * Added to pipeline by default at start after StringDecoder.
 * Asks DataService for user authentication or registration.
 * By succeeded authentication sets up CommandHandler for particular user.
 * Sends auth status to client.
*/

 public class AuthHandler extends SimpleChannelInboundHandler<String> {
     private final Logger logger = LogManager.getLogger(AuthHandler.class);
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
            logger.debug("Auth for " + ctx.channel().remoteAddress().toString());
            User u = auth(command);
            if(u != null){
                logger.debug("User id " + u.getId() + " auth successful");
                s = AUTH_OK.name();
                commctx.setup(u, ds);
            } else {
                logger.debug("auth for " + ctx.channel().remoteAddress().toString() + " failed");
                s = AUTH_FAIL.name();
            }
        } else if(command.contains(REG_REQ.name())){
            logger.debug("Registration for " + ctx.channel().remoteAddress().toString());
            s = register(command) ? REG_OK.name() : REG_FAIL.name();
        }
        commctx.getContext().fireChannelRead(s);
        ctx.fireChannelReadComplete();
        ctx.channel().pipeline().remove(this);
    }

    // Authenticates user using given login & password
    private User auth(String command){
        String[] tokens = command.split(CommInHandler.getDelimiter());
        return ds.authUser(tokens[1], tokens[2]);
    }

    // Tries to register a new user in database
    private boolean register(String command){
        String[] tokens = command.split(CommInHandler.getDelimiter());
        if(tokens.length == 6){
            return ds.registerUser(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
        }
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Auth handler error: ", cause);
        ctx.channel().close().syncUninterruptibly();
        ctx.channel().closeFuture();
    }
}
