package com.liuzx.netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

public class EventLoopClient {
    public static void main(String[] args) throws InterruptedException {
        // 1、客户端启动器
        Channel channel = new Bootstrap()
                // 2、添加 EventLoopGroup
                .group(new NioEventLoopGroup())
                // 3、选择客户端的 SocketChannel 实现
                .channel(NioSocketChannel.class)
                // 4、添加处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override // 建立连接后被调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());

                    }
                })
                // 5、绑定服务端端口
                .connect("127.0.0.1", 8080)
                .sync() // 6、阻塞方法，等待连接成功
                .channel();//  7、代表是连接对象
        System.out.println(channel);
        System.out.println();

    }
}
