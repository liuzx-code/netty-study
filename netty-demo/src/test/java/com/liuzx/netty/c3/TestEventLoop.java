package com.liuzx.netty.c3;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.NettyRuntime;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TestEventLoop {
    public static void main(String[] args) {
        // 1、创建事件循环组，如果没有指定线程数量，会默认以系统的线程数量 * 2
        EventLoopGroup group = new NioEventLoopGroup(2); // io、事件、普通任务、定时任务
//        EventLoopGroup group = new DefaultEventLoopGroup(); // 事件、普通任务、定时任务

        System.out.println(NettyRuntime.availableProcessors());

        // 2、获取下一个循环对象
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());
        System.out.println(group.next());

        // 3、执行普通任务
//        group.next().submit(() -> {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            log.debug("OK");
//        });

        // 4、执行定时任务
        /**
         * 参数1：任务
         * 参数2：首次执行时间
         * 参数3：执行频率
         * 参数4：执行频率单位
         */
        group.next().scheduleAtFixedRate(() -> {
            log.debug("OK");
        },0, 1, TimeUnit.SECONDS);

        log.debug("main");
    }
}
