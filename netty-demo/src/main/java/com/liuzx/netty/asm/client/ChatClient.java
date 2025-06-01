package com.liuzx.netty.asm.client;

import com.liuzx.netty.asm.message.*;
import com.liuzx.netty.asm.protocol.MessageCodecSharable;
import com.liuzx.netty.asm.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {

    public static void main(String[] args) {

        final NioEventLoopGroup group = new NioEventLoopGroup();

        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);

        // 倒计时锁，【主次线程之间 通信】， 初始基数1，减为零才继续往下运行，否则等待
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);

        // 登录状态 初始值 false 【主次线程之间 共享变量】
        AtomicBoolean LOGIN = new AtomicBoolean(false);

        try {
            Bootstrap bs = new Bootstrap();
            bs.channel(NioSocketChannel.class);
            bs.group(group);
            bs.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);

                    /**
                     * 【创建入站处理器 写入内容会触发出站 操作】 【流水线 会向上执行出站Handler,  到 ProcotolFrameDecoder(入站停止)】
                     * 1. 登录操作
                     * 2. 另起线程：菜单里进行 收发消息操作
                     */
                    ch.pipeline().addLast("ChatClient handler", new ChannelInboundHandlerAdapter(){
                        // ###################### [ 3 ] ######################
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception { // 客户端接收消息
                                log.debug("msg: {}", msg);
                            // 1. 处理登录 [登录成功 登录状态=true]
                            if ((msg instanceof LoginResponseMessage)) {
                                LoginResponseMessage responseMessage = (LoginResponseMessage) msg;
                                if(responseMessage.isSuccess()) LOGIN.set(true);
                                // 减一 唤醒 线程：system in
                                WAIT_FOR_LOGIN.countDown();
                            }
                        }
                        // ###################### [ 1 ] ######################
                        @Override // 【 连接建立后触发一次 】
                        public void channelActive(ChannelHandlerContext ctx) throws Exception { // 服务端发送消息
                            // 另起线程(不然会被主线程阻塞) 接受用户输入消息 【登录】
                            new Thread(()->{
                                final Scanner scanner = new Scanner(System.in);
                                System.out.println("请输入用户名");
                                final String username = scanner.nextLine();
                                System.out.println("请输入密码");
                                final String password = scanner.nextLine();
                                // 构造消息对象
                                final LoginRequestMessage message = new LoginRequestMessage(username, password);
                                // 发送消息
                                ctx.writeAndFlush(message);  // 虽然在入站处理器中，但是调用这个方法出发了出站处理器会向上查找处理器

                                // ###################### [ 2 ] ######################
                                log.debug("等待后续操作......");
                                // 为了让代码不马上结束
                                try {
                                    // 执行完countDown会继续向下运行
                                    WAIT_FOR_LOGIN.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                // ###################### [ 4 ] ######################
                                // 登录失败 停止运行
                                if (!LOGIN.get()) {
                                    // 触发 【channel.closeFuture().sync(); 向下运行】
                                    ctx.channel().close();
                                    return;
                                }

                                // 打印菜单
                                while (true)
                                {
                                    System.out.println("============ 功能菜单 ============");
                                    System.out.println("send [username] [content]");
                                    System.out.println("gsend [group name] [content]");
                                    System.out.println("gcreate [group name] [m1,m2,m3...]");
                                    System.out.println("gmembers [group name]");
                                    System.out.println("gjoin [group name]");
                                    System.out.println("gquit [group name]");
                                    System.out.println("quit");
                                    System.out.println("==================================");
                                }

                            },"system in").start();
                        }
                    });

                }
            });
            Channel channel = bs.connect("localhost", 8080).sync().channel();
            // ... 这个位置 ： 连接已经建立好了  【可以写 登录 ， 也可以在 channelActive(连接建立后触发此事件) 里写】
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("Client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
