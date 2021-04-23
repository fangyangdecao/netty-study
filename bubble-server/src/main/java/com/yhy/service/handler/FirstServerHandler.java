package com.yhy.service.handler;

import com.alibaba.fastjson.JSON;
import com.yhy.entity.MessageDO;
import com.yhy.service.InitNettyServer;
import com.yhy.utils.NettyAttrUtil;
import com.yhy.utils.SessionSocketHolder;
import com.yhy.utils.SpringBeanFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;

public class FirstServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(FirstServerHandler.class);

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    //这个方法在接收到客户端发来的数据之后被回调
    //这里的 msg 参数指的就是 Netty 里面数据读写的载体，为什么这里不直接是 ByteBuf，而需要我们强转一下，我们后面会分析到。
    // 这里我们强转之后，然后调用 byteBuf.toString() 就能够拿到我们客户端发过来的字符串数据。
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;

        System.out.println(new Date() + ": 服务端读到数据 -> " + byteBuf.toString(Charset.forName("utf-8")));

        // 回复数据到客户端
        //System.out.println(new Date() + ": 服务端写出数据");
        //ByteBuf out = getByteBuf(ctx);
        //ctx.channel().writeAndFlush(out);

        log.info("received msg=[{}]", msg.toString());
        MessageDO messageDO = JSON.parseObject(byteBuf.toString(Charset.forName("utf-8")), MessageDO.class);
        if (messageDO.getType() == 1) {
            //保存客户端与 Channel 之间的关系
            SessionSocketHolder.put(messageDO.getFrom(), ctx.channel());
            SessionSocketHolder.saveSession(messageDO.getFrom(), messageDO.getMsg());
            log.info("client [{}] online success!!", messageDO.getFrom());
            System.out.println(SessionSocketHolder.getRelationShip());
        }

        //如果=2表示发送消息
        if (messageDO.getType() == 2) {
            Map<Integer, Channel> relationShip = SessionSocketHolder.getRelationShip();
            Channel nioSocketChannel = (Channel)relationShip.get(messageDO.getTo());
            System.out.println(nioSocketChannel.getClass());
            nioSocketChannel.writeAndFlush(byteBuf);
            //ctx.channel().writeAndFlush(byteBuf);
            log.info("client [{}] online success!!", messageDO.getFrom());
/*            channelGroup.forEach(channel -> {
                channel.writeAndFlush(byteBuf);
            });*/

        }

        //心跳更新时间
        if (messageDO.getType() == 3){
            NettyAttrUtil.updateReaderTime(ctx.channel(),System.currentTimeMillis());

            // 回复数据到客户端
            System.out.println(new Date() + ": 服务端写出数据");
            ByteBuf out = getByteBuf(ctx);
            ctx.channel().writeAndFlush(out);
        }
    }


    private ByteBuf getByteBuf(ChannelHandlerContext ctx){

        MessageDO heart =MessageDO.builder()
                .from(100000)
                .msg("pong")
                .type(3)
                .build();
        byte[] bytes = heart.toString().getBytes();

        ByteBuf buffer = ctx.alloc().buffer();

        buffer.writeBytes(bytes);

        return buffer;


    }
}
