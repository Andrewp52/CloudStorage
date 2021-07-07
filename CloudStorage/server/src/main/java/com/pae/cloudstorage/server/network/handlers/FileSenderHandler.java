package com.pae.cloudstorage.server.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FileSenderHandler extends ChunkedWriteHandler {
    private static final Logger logger = LogManager.getLogger(FileSenderHandler.class);
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("File senderHandler error: ", cause);
    }
}
