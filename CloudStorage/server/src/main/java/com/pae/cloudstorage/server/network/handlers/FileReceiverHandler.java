package com.pae.cloudstorage.server.network.handlers;

import com.pae.cloudstorage.common.FSObject;
import com.pae.cloudstorage.server.storage.StorageWorker;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import static com.pae.cloudstorage.common.Command.FILE_UPLOAD;

/**
 * File receiver. Bypassing by default. Activates by User event (FSObject object)
 * Contains link to Command handler for send answers to client using it`s context.
 * This needs for bypass all handlers up to ObjectOutHandler.
 * While file receiving is in-progress, only this handler works (pipeline propagation doesn`t happens).
 * When receiving is complete, handler resets back to bypass mode.
 */
public class FileReceiverHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private FSObject file;
    private StorageWorker worker;
    private RandomAccessFile raf;
    private FileChannel fc;
    private boolean inProgress;

    // Storage worker sets by command handler at ChannelActive event.
    public void setStorageWorker(StorageWorker worker) {
        this.worker = worker;
    }

    // Event that turns on this handler in receive mode
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof FSObject){
            file = (FSObject) evt;
            inProgress = true;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf o) throws Exception {
        if(file != null){
            if(raf == null && fc == null){
                raf = worker.getFileForWrite(file);
                fc = raf.getChannel();
            }
            while (o.readableBytes() > 0){
                o.readBytes(fc, fc.size(), o.readableBytes());
            }
            if(fc.size() == file.getSize()){
                resetAll();
            }
            if(!inProgress){
                ctx.pipeline().get(CommInHandler.class).getContext().fireChannelRead(FILE_UPLOAD.name());
            }
        } else {
            ctx.fireChannelRead(o.retain());
        }
    }

    // Closing file, file channel & Resetting all fields.
    // Settings for bypass
    private void resetAll() throws IOException {
        fc.close();
        raf.close();
        raf = null;
        fc = null;
        file = null;
        inProgress = false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
