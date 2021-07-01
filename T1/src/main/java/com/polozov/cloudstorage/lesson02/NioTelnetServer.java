package com.polozov.cloudstorage.lesson02;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public class NioTelnetServer {
    public NioTelnetServer() {
        NioEventLoopGroup authGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bs = new ServerBootstrap();
            bs.group(authGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new StringDecoder(StandardCharsets.UTF_8), // in decoder (1-st in the chain)
                                    new StringEncoder(StandardCharsets.UTF_8), // out encoder (last in the output chain. In this case the one)
                                    new CommandHandler()); // in decoder (2 - nd in the input chain)
                        }
                    });
            ChannelFuture future = bs.bind(5679).sync();
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

    public static void main(String[] args) throws Exception {
        new NioTelnetServer();
    }


    // TODO: 21.06.2021
    // touch (filename) - создание файла
    // mkdir (dirname) - создание директории
    // cd (path | ~ | ..) - изменение текущего положения
    // rm (filename / dirname) - удаление файла / директории
    // copy (src) (target) - копирование файлов / директории
    // cat (filename) - вывод содержимого текстового файла
    // changenick (nickname) - изменение имени пользователя

    // добавить имя клиента
}
