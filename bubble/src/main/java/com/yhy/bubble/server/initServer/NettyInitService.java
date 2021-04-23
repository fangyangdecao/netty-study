package com.yhy.bubble.server.initServer;

import com.yhy.bubble.entity.message.RequestMsg;
import com.yhy.bubble.entity.message.PushMessageDTO;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.Objects;

@Component
public class NettyInitService {
    /*fire-im.server.netty-port=9999*/
    private static final Logger log = LoggerFactory.getLogger(NettyInitService.class);

    private static int nettty_port = 9999;

    private EventLoopGroup boss = new NioEventLoopGroup();
    private EventLoopGroup worker = new NioEventLoopGroup();

    @PostConstruct
    public void init(){
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(nettty_port))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ServerInitializer());
        ChannelFuture future;
        try {
            future = bootstrap.bind().sync();
            if (future.isSuccess()){
                System.out.println("成功启动IM server");
            }
        } catch (InterruptedException e) {
            System.out.println("启动绑定失败：" + e.toString());
        }
    }



    @Autowired
    private SessionHolder sessionStore;

    /**
     * 向指定客户端发送消息
     * @param message
     */
    public void pushMessage (PushMessageDTO message) throws Exception {

        NioSocketChannel socketChannel = sessionStore.getSessionChannel(message.getUserId());

        if (Objects.isNull(socketChannel)) {
            log.error("推送消息失败,客户端已下线。token: {}", message.getUserId());
            throw new Exception("client offline !");
        }

        RequestMsg protoBuf = RequestMsg.builder()
                .toUserId(message.getFromUserId())
                .fromUserId(message.getMessage())
                .type(1)
                .message(message.getMessage())
                .build();

        ChannelFuture future = socketChannel.writeAndFlush(protoBuf);

        future.addListener((ChannelFutureListener) listener -> {
            log.info("推送消息成功,消息内容: {}", message.toString());
        });
    }







}
