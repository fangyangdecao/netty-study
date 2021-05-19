package com.yhy.im.game;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

// 正常情况是，后台系统通过接口请求，把数据丢到对应的MQ队列，再由推送服务器读取
public class TestCenter {
    // 此处假设一个用户一台设备，否则用户的通道应该是多个。
    // TODO 还应该有一个定时任务，用于检测失效的连接(类似缓存中的LRU算法，长时间不使用，就拿出来检测一下是否断开了)；
    static ConcurrentHashMap<String, Channel> userInfos = new ConcurrentHashMap<String, Channel>();

    // 保存信息
    public static void saveConnection(String userId, Channel channel) {
        userInfos.put(userId, channel);
        System.out.println(userInfos);
    }

    // 退出的时候移除掉
    public static void removeConnection(Object userId) {
        if (userId != null) {
            userInfos.remove(userId.toString());
        }
    }
}