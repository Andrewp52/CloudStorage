package com.pae.cloudstorage.client.network;

import com.pae.cloudstorage.common.CallBack;
import com.pae.cloudstorage.client.network.handlers.MessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Connector {
    private Channel chan;
    private final Thread workingThread;

    public Connector(CallBack callBack) {
        this.workingThread = new Thread(() -> {
            NioEventLoopGroup workGroup = new NioEventLoopGroup();
            Bootstrap bs = new Bootstrap();
            bs.group(workGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<>() {
                @Override
                protected void initChannel(Channel channel){
                    channel.pipeline()
                            .addLast(new StringDecoder())
                            .addLast(new StringEncoder())
                            .addLast(new MessageHandler(callBack));
                    chan = channel;
                }
            });
            try {
                ChannelFuture future = bs.connect("localhost", 9999).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workGroup.shutdownGracefully();
            }
        });
    }

    // Starts client connection
    public void start() {
        if(this.chan == null){
            this.workingThread.start();
            while (this.chan == null){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // Closes session
    // TODO: make graceful stop procedure.
    public void stop(){
        if(this.chan != null && this.chan.isActive()){
            this.chan.close();
        }
    }

    public void sendCommand(String command){
        this.chan.writeAndFlush(command);
    }
}
