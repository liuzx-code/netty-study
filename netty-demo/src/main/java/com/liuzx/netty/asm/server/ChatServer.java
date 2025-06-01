package com.liuzx.netty.asm.server;

import com.liuzx.netty.asm.message.LoginRequestMessage;
import com.liuzx.netty.asm.message.LoginResponseMessage;
import com.liuzx.netty.asm.protocol.MessageCodecSharable;
import com.liuzx.netty.asm.protocol.ProcotolFrameDecoder;
import com.liuzx.netty.asm.server.handler.*;
import com.liuzx.netty.asm.server.service.UserServiceFactory;
import com.liuzx.netty.asm.server.session.SessionFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * 聊天--服务端
 *
 * @author liuzx
 */
@Slf4j
public class ChatServer {

    public static void main(String[] args) throws InterruptedException {

        final NioEventLoopGroup boss = new NioEventLoopGroup();
        final NioEventLoopGroup worker = new NioEventLoopGroup();
        // 局部变量
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);

        // 登录处理器
        LoginRequestMessageHandler LOGIN_HANDLER = new LoginRequestMessageHandler();
        // 聊天消息处理器
        ChatRequestMessageHandler CHAT_HANDLER = new ChatRequestMessageHandler();
        //--创建群聊---处理器
        GroupCreateRequestMessageHandler GROUP_CREATE_HANDLER = new GroupCreateRequestMessageHandler();
        //--加入群---处理器
        GroupJoinRequestMessageHandler GROUP_JOIN_HANDLER = new GroupJoinRequestMessageHandler();
        //--群成员---处理器
        GroupMembersRequestMessageHandler GROUP_MEMBERS_HANDLER = new GroupMembersRequestMessageHandler();
        //--群聊---处理器
        GroupChatRequestMessageHandler GROUP_CHAT_HANDLER = new GroupChatRequestMessageHandler();
        //--退出群---处理器
        GroupQuitRequestMessageHandler GROUP_QUIT_HANDLER = new GroupQuitRequestMessageHandler();
        //--断开连接---处理器
        QuitHandler QUIT_HANDLER = new QuitHandler();

        try {
            final ServerBootstrap bs = new ServerBootstrap();
            bs.channel(NioServerSocketChannel.class);
            bs.group(boss, worker);
            bs.childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    log.debug("{}", ch);

                    ch.pipeline().addLast(new ProcotolFrameDecoder()); // 帧解码器 【与自定义编解码器 MessageCodecSharable一起配置参数】
                    ch.pipeline().addLast(LOGGING_HANDLER);            // 日志
                    ch.pipeline().addLast(MESSAGE_CODEC);              // 出站入站的 自定义编解码器 【 解析消息类型 】


                    // 用来判断应用层连接是否是假死，是不是读 空闲时间过长，或写空闲时间过长 (读，写，读写空闲时间限制) 0表示不关心 单位是秒
                    // 如果12秒内如果没有收到 channel 的数据，会触发一个 IdleState#READER_IDLE 事件
                    ch.pipeline().addLast(new IdleStateHandler(12, 0, 0));
                    /*
                    ################################################################
                    #####  ChannelDuplexHandler 可以同时作为 入站 和 出站处理器  #######
                    ##### 12 秒内 没读到数据 触发   IdleState.READER_IDLE       #######
                    #####       写         触发   IdleState.WRITER_IDLE       #######
                    #####     读写         触发   IdleState.ALL_IDLE          #######
                    ################################################################
                     */
                    // ChannelDuplexHandler 既可以做入站处理器也可以做出站处理器
                    ch.pipeline().addLast(new ChannelDuplexHandler(){
                        // 【用来处理 读写之外的 特殊的事件】
                        @Override //-- 触发的用户事件 --
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent event = (IdleStateEvent) evt;
                            // 是否 读超时
                            if (event.state() == IdleState.READER_IDLE) {
                                log.debug("==============================已经12秒没读到数据了！====================================");
                                ctx.channel().close(); // 假死，释放资源  有点粗暴，直接关闭用户连接 不推荐这样
                            }
                        }
                    });

                    // 以下都是业务处理器
                    ch.pipeline().addLast(LOGIN_HANDLER); // 登录处理器
                    ch.pipeline().addLast(CHAT_HANDLER);// 聊天消息处理器
                    ch.pipeline().addLast(GROUP_CREATE_HANDLER);
                    ch.pipeline().addLast(GROUP_QUIT_HANDLER);
                    ch.pipeline().addLast(GROUP_JOIN_HANDLER);
                    ch.pipeline().addLast(GROUP_MEMBERS_HANDLER);
                    ch.pipeline().addLast(GROUP_CHAT_HANDLER);


//                    ch.pipeline().addLast(new SimpleChannelInboundHandler<LoginRequestMessage>() {
//                        @Override
//                        protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
//                            /**
//                             * 优化内部类移动到独立的handler进行引用
//                             */
//                            log.debug("收到登录请求: {}", msg);
//                            String username = msg.getUsername();
//                            String password = msg.getPassword();
//                            boolean login = UserServiceFactory.getUserService().login(username, password);
//                            LoginResponseMessage message;
//                            if (login) {
//                                // 用户 、channel 绑定关系
//                                SessionFactory.getSession().bind(ctx.channel(), username);
//                                message = new LoginResponseMessage(true, "登录成功");
//                            } else {
//                                message = new LoginResponseMessage(true, "登录成功");
//                            }
//                            ctx.writeAndFlush(message);
//                        }
//                        @Override
//                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//                            log.error("处理登录请求时发生异常", cause);
//                            ctx.close();
//                        }
//                    });

                }
            });
            ChannelFuture channelFuture = bs.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
