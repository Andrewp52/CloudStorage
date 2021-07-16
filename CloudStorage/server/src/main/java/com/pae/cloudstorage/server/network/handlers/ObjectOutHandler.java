package com.pae.cloudstorage.server.network.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Object serialization handler based on Inbound handler.
 * Serializes & sends serializable objects as ByteBuf
 */
public class ObjectOutHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LogManager.getLogger(ObjectOutHandler.class);
    private ChannelHandlerContext context;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf bb = ctx.alloc().heapBuffer();
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream ous = new ObjectOutputStream(bos);
            ous.writeObject(msg);
            bb.writeBytes(bos.toByteArray());
            ctx.pipeline().get(RawOutHandler.class).writeThrough(bb);
        } catch (IOException e) {
            logger.error("Object sending error: ", e);
        }
    }

    ChannelHandlerContext getContext(){
        return this.context;
    }
}
