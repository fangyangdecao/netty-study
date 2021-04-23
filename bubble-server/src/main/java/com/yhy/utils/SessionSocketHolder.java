package com.yhy.utils;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 22/05/2018 18:33
 * @since JDK 1.8
 */
public class SessionSocketHolder {
    private static final Map<Integer, Channel> CHANNEL_MAP = new ConcurrentHashMap<>(16);
    private static final Map<Integer, String> SESSION_MAP = new ConcurrentHashMap<>(16);

    public static void saveSession(Integer userId,String userName){
        SESSION_MAP.put(userId, userName);
    }

    public static void removeSession(Integer userId){
        SESSION_MAP.remove(userId) ;
    }

    /**
     * Save the relationship between the userId and the channel.
     * @param id
     * @param socketChannel
     */
    public static void put(Integer id, Channel socketChannel) {
        CHANNEL_MAP.put(id, socketChannel);
    }

    public static Channel get(Integer id) {
        return CHANNEL_MAP.get(id);
    }

    public static Map<Integer, Channel> getRelationShip() {
        return CHANNEL_MAP;
    }

    public static void remove(Channel nioSocketChannel) {
        CHANNEL_MAP.entrySet().stream().filter(entry -> entry.getValue() == nioSocketChannel).forEach(entry -> CHANNEL_MAP.remove(entry.getKey()));
    }

    /**
     * 获取注册用户信息
     * @param nioSocketChannel
     * @return
     */


}
