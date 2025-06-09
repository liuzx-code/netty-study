package com.liuzx.netty.rpc.client;

import com.liuzx.netty.asm.protocol.MessageCodecSharable;
import com.liuzx.netty.asm.protocol.ProcotolFrameDecoder;
import com.liuzx.netty.rpc.message.RpcRequestMessage;
import com.liuzx.netty.rpc.protocol.SequenceIdGenerator;
import com.liuzx.netty.rpc.server.handler.RpcResponseMessageHandler;
import com.liuzx.netty.rpc.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

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
            // 双重检查 防止唤醒时候创建多个 channel
            if (channel != null) {
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    /**
     * 优化 创建代理类
     * @param serviceClass
     * @return
     * @param <T>
     */
    public static <T> T getProxyService(Class<T> serviceClass) {
        ClassLoader classLoader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};
        Object o = Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> {
            // 1、将方法调用转化为消息对象
            int sequenceId = SequenceIdGenerator.nextId();
            RpcRequestMessage msg = new RpcRequestMessage(
                    sequenceId,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args
            );
            // 2、发送消息
            getChannel().writeAndFlush(msg);
            // 3、准备一个promise对象，来保存结果                     指定 promise 对象 异步等待接收结果线程
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            RpcResponseMessageHandler.PROMISES.put(sequenceId, promise);

            /*// 异步的话是这样使用的
            promise.addListener(future -> {

            });*/

            // 4、等待 promise 结果 同步等待
            promise.await();
            if (promise.isSuccess()) {
                // 5、如果成功，就返回结果
                return promise.getNow();
            } else {
                // 6、如果失败，就抛出异常
                throw new RuntimeException(promise.cause());
            }
        });
        return (T) o;
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {
        // 方式一：
        getChannel().writeAndFlush(new RpcRequestMessage(
                1,
                "com.liuzx.netty.rpc.service.HelloService",
                "say",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"}
        ));

        // 方式二：优化
        // 2、创建代理对象，代理对象会把方法调用转换成消息的发送
        // 当调用代理对象的任何一个方法时，会进入代理对象的(proxy, method, args) -> {} 方法执行
        HelloService service = RpcClientManager.getProxyService(HelloService.class);
        service.say("张三");
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
