package com.yhy.bubble.controller;

import com.yhy.bubble.entity.message.PushMessageDTO;
import com.yhy.bubble.server.initServer.NettyInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private NettyInitService server;

    @PostMapping(value = "pushMsg")
    public String pushMessage(@RequestBody PushMessageDTO message) {
        try {
            server.pushMessage(message);
        } catch (Exception e) {
            return e.toString();
        }

        return "ok";
    }
}
