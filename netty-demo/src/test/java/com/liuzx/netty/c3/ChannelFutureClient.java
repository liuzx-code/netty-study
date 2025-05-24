package com.liuzx.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelFutureClient {
    public static void main(String[] args) throws InterruptedException {
        // 2.带有 Future，Promise 的类型都是和异步方法配套使用，用来处理结果
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override // 建立连接后被调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());

                    }
                })
                // 1、绑定服务端端口
                // 异步非阻塞，main 发起了调用，真正执行 connect 是 nio 线程
                .connect("127.0.0.1", 8080); // 1秒后
        /**
         * 如果sync被注释掉会发生什么？
         * 1、连接建立成功，但是没有发送数据
         */
//        // 2.1 使用 sync 方法同步处理结果
//        channelFuture.sync();// 阻塞住当前线程，直到nio线程连接建立完毕
//        // 无阻塞向下执行获取 channel
//        Channel channel = channelFuture.channel();
//        log.debug("{}", channel);
//        channel.writeAndFlush("Hello World");

          // 2.2 使用 addListener(回调对象)方法异步处理结果
        channelFuture.addListener(new ChannelFutureListener() {
            // 在 nio 线程连接建立好之后，会调用 operationComplete
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel channel = channelFuture.channel();
                log.debug("{}", channel);
                channel.writeAndFlush("Hello world");
            }
        });

    }
}
