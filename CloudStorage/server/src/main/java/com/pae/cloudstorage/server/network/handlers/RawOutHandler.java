package com.pae.cloudstorage.server.network.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

/**
 * Raw outbound handler. Stub handler for data transferring avoiding
 * last working handler (FileSenderHandler)
 * write method is not implemented for passing through by default
 */
public class RawOutHandler extends ChannelOutboundHandlerAdapter {
    private ChannelHandlerContext context;
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        context = ctx;
    }

    void writeThrough(ByteBuf bb){
        context.channel().writeAndFlush(bb, context.voidPromise());
    }

}
