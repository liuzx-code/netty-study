package com.liuzx.netty.c3;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class TestJdkFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1、线程池
        ExecutorService executor = Executors.newFixedThreadPool(2);
        // 2、提交任务
        Future<Integer> future = executor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                // 获取当前线程名称
                String currentThreadName = Thread.currentThread().getName();
                log.debug("call:{}", currentThreadName);
                log.debug("执行计算");
                Thread.sleep(1000);
                return 1;
            }
        });
        // 3、主线程通过 future 获取结果
        String currentThreadName = Thread.currentThread().getName();
        log.debug("future.get:{}", currentThreadName);
        log.debug("等待结果");
        future.get();
        log.debug("获取结果：{}",  future.get());
    }
}
