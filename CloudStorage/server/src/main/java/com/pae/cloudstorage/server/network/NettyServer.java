package com.pae.cloudstorage.server.network;

import com.pae.cloudstorage.server.ConfigReader;
import com.pae.cloudstorage.server.data.DataService;
import com.pae.cloudstorage.server.network.handlers.AuthHandler;
import com.pae.cloudstorage.server.network.handlers.ObjectOutHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * NETTY server base class with standard configuration.
 */
public class NettyServer {
    private int PORT = Integer.parseInt(ConfigReader.readConfFile("/netserver.conf").get("port"));
    Logger logger = LogManager.getLogger(NettyServer.class);
    public NettyServer(DataService ds) throws IOException {
        NioEventLoopGroup authGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bs = new ServerBootstrap();
            bs.group(authGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch){
                            ch.pipeline()
                                    .addLast("STRDEC", new StringDecoder())
                                    .addLast("AUTH", new AuthHandler());
                        }
                    });
            ChannelFuture future = bs.bind(PORT).sync();
            logger.info("Server started. Listening port: " + PORT);
            future.channel().closeFuture().sync();
            logger.info("Server stopped");
        } catch (Exception e) {
            logger.error("Server error: ", e);
        } finally {
            authGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
