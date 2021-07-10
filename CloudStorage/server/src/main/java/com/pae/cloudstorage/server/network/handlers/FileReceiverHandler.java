package com.pae.cloudstorage.server.network.handlers;

import com.pae.cloudstorage.common.FSObject;
import com.pae.cloudstorage.server.storage.FSWorker;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import static com.pae.cloudstorage.common.Command.FILE_UPLOAD;

public class FileReceiverHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private FSObject file;
    private FSWorker worker;
    private CommInHandler commInHandler;
    private RandomAccessFile raf;
    private FileChannel fc;
    private boolean inProgress;

    public void setStorageWorker(FSWorker worker) {
        this.worker = worker;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        commInHandler = ctx.pipeline().get(CommInHandler.class);
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
                int read = o.readBytes(fc, fc.size(), o.readableBytes());
                System.out.println(fc.size() + ":::" + read);
            }
            if(fc.size() == file.getSize()){
                resetAll();
            }
            if(!inProgress){
                file = null;
                commInHandler.writeThrough(FILE_UPLOAD.name());
            }
        } else {
            ctx.fireChannelRead(o.retain());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    private void resetAll() throws IOException {
        fc.close();
        raf.close();
        raf = null;
        fc = null;
        inProgress = false;
    }
}
