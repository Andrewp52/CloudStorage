package com.pae.cloudstorage.server.network.handlers;

import com.pae.cloudstorage.common.User;
import com.pae.cloudstorage.server.data.DataService;
import io.netty.buffer.ByteBuf;
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
    DataService ds;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        String command = s.replace("\n", "").replace("\r", "");
        if(AUTH_OUT.name().equals(command)){
            ctx.channel().writeAndFlush(AUTH_OUT.name());
            ctx.channel().close().syncUninterruptibly();
            ctx.channel().closeFuture();
            return;
        } else if(command.contains(AUTH_REQ.name())) {
            User u = auth(null, null);
            if(u != null){
                ctx.channel().pipeline().addAfter("AUTH", "CMD", new CommHandler(u));
                ctx.channel().writeAndFlush(AUTH_OK.name());
            } else {
                ctx.channel().writeAndFlush(AUTH_FAIL.name());
            }
        }
    }

    private User auth(String login, String pass){
        return new User(0, "aaa", "bbb", "ccc", "", "aaa", 200000000);
    }
}
