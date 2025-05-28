package com.liuzx.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CloseFutureClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override // 建立连接后被调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG)); // debug channel 流程 和 状态【注意logback.xml也要配置】
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 5、绑定服务端端口
                .connect(new InetSocketAddress("127.0.0.1", 8080));

        Channel channel = channelFuture.sync().channel();
        log.debug("{}", channel);

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close(); // close 是异步操作，1秒后才关闭
//                    log.debug("处理关闭之后的操作"); // 写在这里的话可能导致还没关闭就执行了
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "input").start();
//        log.debug("处理关闭之后的操作");

        // 获取 closeFuture 对象 1、同步处理关闭事件；2、异步处理关闭事件
        // 1、同步处理关闭事件
        ChannelFuture closedFuture = channel.closeFuture();
//        log.debug("waiting close....");
//        closedFuture.sync();
//        log.debug("处理关闭之后的操作");
        // 2、异步处理关闭事件
        closedFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("处理关闭之后的操作");
                group.shutdownGracefully(); //  关闭线程组 优雅的关闭，有在进行的任务处理完再关闭
            }
        });

    }
}
