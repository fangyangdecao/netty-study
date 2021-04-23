package com.yhy.bubble.server.initServer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SessionCache {

    /**
     * 本地缓存
     * @return
     */
    @Bean(name = "localSessionStore")
    public LoadingCache<String, Session> loadingCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(10000)
//                .expireAfterAccess()
//                .expireAfterWrite()
                .build(new CacheLoader<String, Session>() {
                    @Override
                    public Session load(String s) throws Exception {
                        return null;
                    }
                });
    }
}
