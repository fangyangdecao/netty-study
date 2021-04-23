package com.yhy.im.client;

import com.yhy.im.protocol.command.LoginRequestPacket;
import com.yhy.im.protocol.command.PacketCodeC;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;
import java.util.UUID;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    public void channelActive(ChannelHandlerContext ctx){
        System.out.println(new Date() + ": 客户端开始登录");

        //创建登录对象
        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();
        loginRequestPacket.setUserId(UUID.randomUUID().toString());
        loginRequestPacket.setUsername("flash");
        loginRequestPacket.setPassword("pwd");

        //编码
        ByteBuf buffer = PacketCodeC.INSTANCE.encode(loginRequestPacket);
        //写数据
        ctx.channel().writeAndFlush(buffer);
    }
}
