package com.yhy.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

@Component
public class InitNettyServer {
    /*fire-im.server.netty-port=9999*/
    private static final Logger log = LoggerFactory.getLogger(InitNettyServer.class);

    private static final int netty_port = 8888;

    private EventLoopGroup boss = new NioEventLoopGroup();
    private EventLoopGroup worker = new NioEventLoopGroup();

    @PostConstruct
    public void init(){
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(netty_port))
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


}
