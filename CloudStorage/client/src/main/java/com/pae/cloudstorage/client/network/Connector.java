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
    private final String HOST = "localhost";
    private final int PORT = 9999;
    private ChannelFuture channelFuture;
    private Thread workingThread;
    private CallBack callBack;
    public Connector(CallBack callBack) {
        this.callBack = callBack;
    }

    // Starts Netty client connection
    public void start() {
        if(this.channelFuture == null && workingThread == null){
            startClient();
            while (this.channelFuture == null){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Stops Netty client
    public void stop(){
        if(channelFuture == null){
            return;
        }
        try {
            channelFuture.channel().close().sync();
            workingThread.join();           //  Waiting until workingThread will die
            channelFuture = null;           //  Release ChannelFuture
            workingThread = null;           //  Release Thread
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Starts Netty client thread
    private void startClient(){
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
                }
            });
            try {
                this.channelFuture = bs.connect(HOST, PORT).sync();
                this.channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                workGroup.shutdownGracefully();
            }
        });
        this.workingThread.start();
    }

    // Sends command to remote server
    public void sendCommand(String command){
        this.channelFuture.channel().writeAndFlush(command);
    }
}
