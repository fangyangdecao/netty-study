package com.yhy.bubble.server.initServer;

import lombok.Getter;

import java.util.HashMap;
import java.util.UUID;

@Getter
public class Session<K,V> extends HashMap<K, V> {

    /**
     * session id
     */
    private String sessionId;


    public Session() {
        this.sessionId = UUID.randomUUID().toString();
    }


    public static Session newSession() {
        return new Session();
    }

    /**
     *
     * @param key
     *
     * @param value
     * @return
     */
    public Session putAttribute(K key,V value) {
        super.put(key, value);
        return this;
    }
}
