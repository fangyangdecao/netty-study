package com.yhy.service;

import com.yhy.service.handler.FirstServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    //此处注入 认证，下线通知等一系列逻辑


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                //12秒没有读取到消息就触发自定义操作
                .addLast("inboundHeartBeat",new IdleStateHandler(12,0,0))
                //入站半包处理
                //.addLast("inboundPack", new ProtobufVarint32FrameDecoder())
                //入站protobuf解码器
                //.addLast("inboundProtobufDecoder", new ProtobufDecoder(ImMessage.RequestMessage.getDefaultInstance()))
                //出站在消息头中加入int32标识消息长度
                //.addLast("outboundPackAdd", new ProtobufVarint32LengthFieldPrepender())
                //出站protobuf编码器
                //.addLast("outboundProtobufEncoder", new ProtobufEncoder());
                //入站身份认证
                //入站其他处理
                .addLast(new FirstServerHandler());

    }
















































}
