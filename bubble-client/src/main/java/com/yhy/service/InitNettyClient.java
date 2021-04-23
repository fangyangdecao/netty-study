package com.yhy.service;

import com.yhy.service.handler.FirstClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class InitNettyClient {
    private static final Logger log = LoggerFactory.getLogger(InitNettyClient.class);

    private SocketChannel channel;

    @PostConstruct
    public  void init() {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(workerGroup)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 指定连接数据读写逻辑
                        ch.pipeline()
                                .addLast(new IdleStateHandler(0,10,0))
                                .addLast(new FirstClientHandler());

                    }
                });
        ChannelFuture future = connect(bootstrap, "127.0.0.1", 8888, MAX_RETRY);
        if (future.isSuccess()) {
            log.info("启动 cim client 成功");
        }
        channel = (SocketChannel) future.channel();
    }

    /**
     * 发送消息字符串
     *
     * @param msg
     */
    public void sendStringMsg(String msg) {
        ByteBuf message = Unpooled.buffer(msg.getBytes().length);
        message.writeBytes(msg.getBytes());
        ChannelFuture future = channel.writeAndFlush(message);
        future.addListener((ChannelFutureListener) channelFuture ->
                log.info("客户端手动发消息成功={}", msg));

    }


    private static int MAX_RETRY = 5;

    private static ChannelFuture connect(Bootstrap bootstrap, String host, int port,int retry) {
        return  bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接成功!");
            } else if (retry == 0){
                System.err.println("重试次数已用完，放弃连接！");
            } else {
                int order = (MAX_RETRY - retry) + 1;
                int delay = 1 << order;
                System.err.println(new Date() + ": 连接失败，第" + order + "次重连……");
                //定时任务是调用 bootstrap.config().group().schedule(),
                // 其中 bootstrap.config() 这个方法返回的是 BootstrapConfig，他是对 Bootstrap 配置参数的抽象，
                // 然后 bootstrap.config().group() 返回的就是我们在一开始的时候配置的线程模型 workerGroup，
                // 调 workerGroup 的 schedule 方法即可实现定时任务逻辑。
                bootstrap.config().group().schedule(()->
                        connect(bootstrap,host,port,retry-1),delay, TimeUnit.SECONDS);
            }
        });
    }



}
