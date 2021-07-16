package com.pae.cloudstorage.server.network;

import com.pae.cloudstorage.server.data.DataService;
import com.pae.cloudstorage.server.network.handlers.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * NETTY server base class with standard configuration.
 */
public class NettyServer {
    private final int PORT;
    private final int MAXFRAMESIZE;
    Logger logger = LogManager.getLogger(NettyServer.class);
    public NettyServer(Map<String, String> config, DataService ds) throws IOException {
        PORT = Integer.parseInt(config.get("port"));
        MAXFRAMESIZE = Integer.parseInt(config.get("maxframesize"));
        NioEventLoopGroup authGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bs = new ServerBootstrap();
            bs.group(authGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch){
                            ch.pipeline()
                                    .addLast("FILEREC", new FileReceiverHandler())
                                    .addLast("FRAMEDEC", new DelimiterBasedFrameDecoder(MAXFRAMESIZE, Unpooled.copiedBuffer("$_".getBytes())))
                                    .addLast("STRDEC", new StringDecoder())
                                    .addLast("AUTH", new AuthHandler(ds))
                                    .addLast("COMMIN", new CommInHandler())
                                    .addLast(new ObjectOutHandler(), new FileSenderHandler(), new RawOutHandler());
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
