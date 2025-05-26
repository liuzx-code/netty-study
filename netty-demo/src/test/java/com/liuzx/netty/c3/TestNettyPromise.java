package com.liuzx.netty.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class TestNettyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1、准备EventLoop
        EventLoop eventLoop = new NioEventLoopGroup().next();
        // 2、创建一个Promise
        Promise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread(() -> {
            // 3.向任意一个线程执行，执行完向promise设置结果
            log.debug("begin");
            try {
                int  i = 1 / 0;
                Thread.sleep(1000);
                log.debug("setSuccess");
                promise.setSuccess(10);
            } catch (Exception e) {
               e.printStackTrace();
               promise.setFailure(e);
            }
        }).start();
       // 4、获取结果
        log.debug("waiting");
        log.debug("{}", promise.get());
    }
}