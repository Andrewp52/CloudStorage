package com.pae.cloudstorage.client.network.handlers;

import com.pae.cloudstorage.client.common.CallBack;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MessageHandler extends SimpleChannelInboundHandler<String> {
    CallBack callBack;

    public MessageHandler(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) {
        callBack.call(s);
        ctx.fireChannelReadComplete();
    }
}
