package com.yhy.im.game;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

/**
 * Handles handshakes and messages
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final String WEBSOCKET_PATH = "/websocket";

    private WebSocketServerHandshaker handshaker;

    public static final LongAdder counter = new LongAdder();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        System.out.println(msg);
        counter.add(1);
        //初次建立连接时的参数，和第二次主动发送的参数不一样
        if (msg instanceof FullHttpRequest) {
            System.out.println("http");
            // 处理websocket握手
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            System.out.println("frame");
            // 处理websocket后续的消息
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
        System.out.println(counter);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("channelReadComplete");
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request. //如果http解码失败 则返回http异常 并且判断消息头有没有包含Upgrade字段(协议升级)
        if (!req.decoderResult().isSuccess() || req.method() != GET || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // 构造握手响应返回，升级协议为ws
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, true, 5 * 1024 * 1024);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            // 版本不支持
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
            ctx.fireChannelRead(req.retain()); // 消息处理完后，传递消息至下一个处理器
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // Check for closing frame 关闭
        if (frame instanceof CloseWebSocketFrame) {
            Object userId = ctx.channel().attr(AttributeKey.valueOf("userId")).get();
            TestCenter.removeConnection(userId);
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) { // ping/pong作为心跳
            System.out.println("ping: " + frame);
            ctx.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof TextWebSocketFrame) {
            // Echo the frame
            // TODO 处理具体的数据请求（... 聊天室，推送给其他的用户）
            //发送到客户端websocket
/*            ctx.channel().write(new TextWebSocketFrame(((TextWebSocketFrame) frame).text()
                    + ", 欢迎使用Netty WebSocket服务， 现在时刻:"
                    + new java.util.Date().toString()));*/

            ConcurrentHashMap<String, Channel> userInfos = TestCenter.userInfos;

            userInfos.forEach((k,v)->{
                System.out.println(k);
                System.out.println(v);
                if (!v.equals(ctx.channel())){
                    v.writeAndFlush(new TextWebSocketFrame(((TextWebSocketFrame) frame).text()
                            + "发送给：" + k
                            + new java.util.Date().toString()));
                }

            });
            return;
        }
        // 不处理二进制消息
        if (frame instanceof BinaryWebSocketFrame) {
            // Echo the frame
            ctx.write(frame.retain());
        }
    }

    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private int readIdleTimes = 0;
    private String read = "read";
    private String write = "write";
    private String all = "all";
    private Map<String,Integer> trigger = new HashMap<>();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent)evt;

        Channel channel = ctx.channel();
        String s = channel.toString();
        String eventType = null;
        Integer orDefault = 0;

        switch (event.state()){
            case READER_IDLE:
                s = s + read;
                orDefault = trigger.getOrDefault(s , 0);
                trigger.put(s ,orDefault + 1);
                eventType = "读空闲";
                readIdleTimes ++; // 读空闲的计数加1
                break;
            case WRITER_IDLE:
                eventType = "写空闲";
                s = s + write;
                orDefault = trigger.getOrDefault(s , 0);
                trigger.put(s ,orDefault + 1);
                // 不处理
                break;
            case ALL_IDLE:
                eventType ="读写空闲";
                s = s + all;
                orDefault = trigger.getOrDefault(s , 0);
                trigger.put(s ,orDefault + 1);
                // 不处理
                break;
        }
        System.out.println(trigger);
        //System.out.println(channel.remoteAddress() + "超时事件：" +eventType);
        if(readIdleTimes > 3){
            System.out.println(" [server]读空闲超过3次，关闭连接");
            channel.writeAndFlush(new TextWebSocketFrame("you are out"));
            channel.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效处理,客户端下线或者强制退出等任何情况都触发这个方法
        System.out.println("捕获异常");
        super.channelInactive(ctx);
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HttpHeaderNames.HOST) + WEBSOCKET_PATH;
        return "ws://" + location;
    }
}