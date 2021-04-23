package com.yhy.bubble.server.initServer;

import com.google.common.cache.LoadingCache;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Component
public class SessionHolder {

    @Resource(name = "localSessionStore")
    private LoadingCache<String, Session> loadingCache;


    public Session saveSession(String userId) {
        Session session = Session.newSession();
        session.putAttribute("user_id", userId);

        loadingCache.put(userId, session);
        return session;
    }

    public NioSocketChannel getSessionChannel(String userId) throws ExecutionException {
        if (Objects.isNull(loadingCache.get(userId))) {
            return null;
        }
        Session session = loadingCache.get(userId);
        Object obj = session.get("socket_channel");
        if (Objects.isNull(obj)) {
            return null;
        }
        return (NioSocketChannel)obj;
    }
}
