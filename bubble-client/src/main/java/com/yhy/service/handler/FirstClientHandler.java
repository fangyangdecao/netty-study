package com.yhy.service.handler;

import com.alibaba.fastjson.JSON;
import com.yhy.entity.MessageDO;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.nio.charset.Charset;
import java.util.Date;

public class FirstClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    //这个方法会在客户端连接建立成功之后被调用,
    // 在这个方法里面，我们编写向服务端写数据的逻辑
    public void channelActive(ChannelHandlerContext ctx){
        System.out.println(new Date() + ": 客户端写出数据");

        //1.获取数据
        ByteBuf buffer = getByteBuf(ctx);
        // 2. 写数据
        ctx.channel().writeAndFlush(buffer);
    }

    @Override
    //客户端的读取数据的逻辑和服务端读取数据的逻辑一样，同样是覆盖 ChannelRead() 方法
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        //MessageDO messageDO = JSON.parseObject(byteBuf.toString(Charset.forName("utf-8")), MessageDO.class);

        System.out.println(new Date() + ": 客户端读到数据 -> " + byteBuf.toString(Charset.forName("utf-8")));
        //System.out.println(new Date() + ": 客户端读到数据 -> " + messageDO.toString());
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx){
        // 1. 获取netty对二进制数据的抽象 ByteBuf
        ByteBuf buffer = ctx.alloc().buffer();
        // 2. 准备数据，指定字符串的字符集为 utf-8
        MessageDO aDo = MessageDO.builder().from(456).to(0).msg("登陆").type(1).build();
        byte[] bytes = JSON.toJSONString(aDo).getBytes(Charset.forName("utf-8"));
        // 3. 填充数据到 ByteBuf
        buffer.writeBytes(bytes);

        return buffer;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent idleStateEvent =(IdleStateEvent)evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE){
                System.out.println("已经 10 秒没有发送信息！");
                //向服务端发送消息
                //CustomProtocol heartBeat = SpringBeanFactory.getBean("heartBeat", CustomProtocol.class);
                ctx.writeAndFlush("pong").addListener(ChannelFutureListener.CLOSE_ON_FAILURE) ;
            }
        }
        super.userEventTriggered(ctx,evt);
    }
}
