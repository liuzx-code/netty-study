package com.liuzx.netty.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;


@Slf4j
public class TestNettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        // 2、提交任务
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                // 获取当前线程名称
                String currentThreadName = Thread.currentThread().getName();
                log.debug("call:{}", currentThreadName);
                log.debug("执行计算");
                Thread.sleep(1000);
                return 2;
            }
        });
        // 3、主线程通过 future 获取结果
        // 方法一：同步的方式获取结果
//        String currentThreadName = Thread.currentThread().getName();
//        log.debug("future.get:{}", currentThreadName);
//        log.debug("等待结果");
//        future.get();
//        log.debug("获取结果：{}",  future.get());
        // 方法二：异步的方式获取结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                String currentThreadName = Thread.currentThread().getName();
                log.debug("operationComplete:{}", currentThreadName);
                // 获取任务结果，非阻塞，还未产生结果时返回 null
                log.debug("获取结果：{}", future.getNow());
            }
        });
    }
}
