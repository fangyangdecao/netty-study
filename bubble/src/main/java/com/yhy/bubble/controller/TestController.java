package com.yhy.bubble.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@RestController
@RequestMapping("/test")
public class TestController {
    private static final Object a = "lock";
    private static final ThreadLocal<String> t = new ThreadLocal<>();


    //tetststest
    @GetMapping("/test")
    public  String test(@RequestParam String b) throws InterruptedException {
        t.set(b);
        Thread.sleep(10000);
        System.out.println(1 + t.get());
        return "test";
    }
}
