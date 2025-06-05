package com.liuzx.netty.rpc.client;

import com.liuzx.netty.asm.protocol.MessageCodecSharable;
import com.liuzx.netty.asm.protocol.ProcotolFrameDecoder;
import com.liuzx.netty.rpc.message.RpcRequestMessage;
import com.liuzx.netty.rpc.server.handler.RpcResponseMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClientManager {

    private static Channel channel = null;
    private static final Object  LOCK = new Object();

    /**
     * 单例模式 双重锁机制
     * 获取单一的channel
     * @return
     */
    public static Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        synchronized (LOCK) {
            if (channel != null) { // 双重检查 防止唤醒时候创建多个 channel
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {
        getChannel().writeAndFlush(new RpcRequestMessage(
                1,
                "com.liuzx.netty.rpc.service.HelloService",
                "say",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"}
        ));
    }

    /**
     * 初始化channel
     */
    public static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();

        // rpc 响应消息处理器，待实现
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    ch.pipeline().addLast(RPC_HANDLER);
                }
            });
        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().addListener(future -> { // 异步
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}
