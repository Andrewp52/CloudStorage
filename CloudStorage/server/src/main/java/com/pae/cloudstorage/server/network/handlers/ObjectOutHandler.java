package com.pae.cloudstorage.server.network.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Outbound Object handler
 * Serializes & sends serializable objects as ByteBuf
 * Buffer contains two int at start bytes:
 * start position - 0 and Object`s byte-array length.
 * Regular Socket read operations compatible.
 */
public class ObjectOutHandler extends ChannelOutboundHandlerAdapter {
    
    Logger logger = LogManager.getLogger(ObjectOutHandler.class);
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf bb = ctx.alloc().heapBuffer();
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream ous = new ObjectOutputStream(bos);
            ous.writeObject(msg);
            bb.writeInt(0).writeInt(bos.size()).writeBytes(bos.toByteArray());
            ctx.write(bb);
            bb.release();
            ctx.flush();
        } catch (IOException e) {
            logger.error("Object sending error: ", e);
        }
    }
}
