package com.liuzx.netty.rpc.service;


public class HelloServiceImpl implements HelloService {
    @Override
    public String say(String name) {
        return "你好，" + name + "!!!!!";
    }
}
