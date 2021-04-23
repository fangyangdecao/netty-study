package com.yhy.controller;

import com.alibaba.fastjson.JSON;
import com.yhy.entity.MessageDO;
import com.yhy.service.InitNettyClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private InitNettyClient client;


    @PostMapping
    public void sendMsg(@RequestBody MessageDO msg){
        client.sendStringMsg(JSON.toJSONString(msg));
    }
}
