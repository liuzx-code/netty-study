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
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 客户端
 */
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
        AtomicBoolean EXIT = new AtomicBoolean(false);

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

                    // 用来判断应用层连接是否是假死，是不是读 空闲时间过长，或写空闲时间过长 (读，写，读写空闲时间限制) 0表示不关心 单位是秒
                    // 如果6秒内没有向服务器发送数据，会触发一个 IdleState#WRITER_IDLE 事件
                    // 写的频率应该大于读的频率 一般的读的1/2
                    ch.pipeline().addLast(new IdleStateHandler(0, 6, 0));
                     /*
                    ################################################################
                    ###### ChannelDuplexHandler 可以同时作为 入站 和 出站处理器  #######
                    #####          没读到数据 触发   IdleState.READER_IDLE     #######
                    #####  6 秒内  写      触发   IdleState.WRITER_IDLE       #######  【写要比服务端读的频率高些】
                    #####       读写     触发   IdleState.ALL_IDLE            #######
                    ################################################################
                     */
                    // ChannelDuplexHandler 既可以做入站处理器也可以做出站处理器
                    ch.pipeline().addLast(new ChannelDuplexHandler(){
                        // 【用来处理 读写之外的 特殊的事件】
                        @Override //-- 触发的用户事件 --
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent event = (IdleStateEvent) evt;
                            // 是否 读超时
                            if (event.state() == IdleState.WRITER_IDLE) {
                                // log.debug(" 6秒 没写数据了 ，发送心跳包 ==================="); // 影响用户客户端控制的输入 注释掉
                                ctx.writeAndFlush(new PingMessage());
                            }
                        }
                    });

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
                                while (true) {
                                    System.out.println("============ 功能菜单 ============");
                                    System.out.println("send [username] [content]");
                                    System.out.println("gsend [group name] [content]");
                                    System.out.println("gcreate [group name] [m1,m2,m3...]");
                                    System.out.println("gmembers [group name]");
                                    System.out.println("gjoin [group name]");
                                    System.out.println("gquit [group name]");
                                    System.out.println("quit");
                                    System.out.println("==================================");

                                    String command = scanner.nextLine();
                                    final String[] s = command.split(" ");
                                    switch (s[0])
                                    {
                                        case "send": // 发送消息
                                            ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                                            break;
                                        case "gsend": // 群里 发送消息
                                            ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                                            break;
                                        case "gcreate": // 创建群
                                            final Set<String> set = new HashSet(Arrays.asList(s[2].split(",")));
                                            set.add(username);
                                            ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                                            break;
                                        case "gmembers": // 查看群列表
                                            ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                            break;
                                        case "gjoin":
                                            ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                                            break;
                                        case "gquit":
                                            ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                                            break;
                                        case "quit":
                                            ctx.channel().close(); // 触发 【channel.closeFuture().sync(); 向下运行】
                                            return;
                                    }
                                }

                            },"system in").start();
                        }

                        // 在连接断开时触发
                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("Client...主动-连接已经断开，按任意键退出..");
                            EXIT.set(true);
                        }

                        // 在出现异常时触发
                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            log.debug("Client...异常-连接已经断开，按任意键退出..{}", cause.getMessage());
                            EXIT.set(true);
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
