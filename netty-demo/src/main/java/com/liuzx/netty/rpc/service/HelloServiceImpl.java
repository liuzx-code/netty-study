package com.liuzx.netty.rpc.service;


public class HelloServiceImpl implements HelloService {
    @Override
    public String say(String name) {
        // 模拟异常
        int i = 1 / 0;
        return "你好，" + name + "!!!!!";
    }
}
