package com.liuzx.netty.rpc.service;


public interface HelloService {
    /**
     * 给定 name 返回欢迎文字
     * @param name 用户名
     * @return
     */
    String say(String name);
}
