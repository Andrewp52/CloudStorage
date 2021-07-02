package com.pae.cloudstorage.server.network;

import com.pae.cloudstorage.server.data.DataService;
import com.pae.cloudstorage.server.network.handlers.AuthHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class NettyServer {
    public NettyServer(int listenPort, DataService ds){
        NioEventLoopGroup authGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bs = new ServerBootstrap();
            bs.group(authGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch){
                            ch.pipeline().addLast("STRDEC", new StringDecoder())
                                    .addLast("AUTH", new AuthHandler());
                        }
                    });
            ChannelFuture future = bs.bind(listenPort).sync();
            System.out.println("Server started");
            future.channel().closeFuture().sync();
            System.out.println("Server stopped");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            authGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
