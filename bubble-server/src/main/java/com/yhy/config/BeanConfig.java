package com.yhy.config;


import com.yhy.entity.MessageDO;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/12/23 00:25
 * @since JDK 1.8
 */
@Configuration
public class BeanConfig {
    /**
     * 创建心跳单例
     * @return
     */
    @Bean(value = "heartBeat")
    public MessageDO heartBeat() {
        MessageDO heart =MessageDO.builder()
                .from(100000)
                .msg("pong")
                .type(3)
                .build();
        return heart;
    }
}
