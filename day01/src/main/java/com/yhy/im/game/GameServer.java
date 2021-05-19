package com.yhy.im.game;

import com.yhy.im.server.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.TimeUnit;

public class GameServer {
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,1024)
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                //如果要求高实时性，有数据发送时就马上发送，就关闭，如果需要减少发送次数减少网络交互，就开启
                .childOption(ChannelOption.TCP_NODELAY,true)
                //attr()方法可以给服务端的 channel，也就是NioServerSocketChannel指定一些自定义属性
                .attr(AttributeKey.newInstance("serverName"),"nettyServer")
                //handler()用于指定在服务端启动过程中的一些逻辑，通常情况下呢，我们用不着这个方法。
                .handler(new ChannelInitializer<NioServerSocketChannel>() {
                    @Override
                    protected void initChannel(NioServerSocketChannel nioServerSocketChannel) throws Exception {
                        System.out.println("服务端启动中");
                    }
                })
                //childHandler()用于指定处理新连接数据的读写处理逻辑
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 指定连接数据读写逻辑
                        //ch.pipeline().addLast(new FirstServerHandler());
                        ch.pipeline()
                                //HttpServerCodec是netty针对http编解码的处理类，但是这些只能处理像http get的请求,也就是数据带在url问号后面的http请求
                                .addLast(new HttpServerCodec())
                                //HttpObjectAggregator这个netty的处理器就是为了解决这个问题而来的.它把HttpMessage和HttpContent聚合成为一个FullHttpRquest或者FullHttpRsponse
                                .addLast(new HttpObjectAggregator(65536))
                                //心跳处理
                                .addLast(new IdleStateHandler(4,4,4, TimeUnit.SECONDS))
                                .addLast(new WebSocketServerHandler())
                                //针对新接入的连接做处理
                                .addLast(new NewConnectHandler());
                    }
                });

        bind(serverBootstrap,8000);
    }

    /**
     *
     * @param serverBootstrap 引导类
     * @param port 端口
     */
    private static void bind(final ServerBootstrap serverBootstrap,final  int port){
        //添加监听器，监听绑定端口的异步返回结果
        serverBootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()){
                    System.out.println("port bind succeed");
                    System.out.println(port);
                } else {
                    System.out.println("port bind failed");
                    //绑定失败后重新调用自生方法再次绑定
                    bind(serverBootstrap,port + 1);
                }
            }
        });
    }
}
