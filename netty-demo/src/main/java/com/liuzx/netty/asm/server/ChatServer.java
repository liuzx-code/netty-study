package com.liuzx.netty.asm.server;

import com.liuzx.netty.asm.message.LoginRequestMessage;
import com.liuzx.netty.asm.message.LoginResponseMessage;
import com.liuzx.netty.asm.protocol.MessageCodecSharable;
import com.liuzx.netty.asm.protocol.ProcotolFrameDecoder;
import com.liuzx.netty.asm.server.service.UserServiceFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;


/**
 * 聊天--服务端
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

        try {
            final ServerBootstrap bs = new ServerBootstrap();
            bs.channel(NioServerSocketChannel.class);
            bs.group(boss, worker);
            bs.childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    log.debug("{}",ch);

                    ch.pipeline().addLast(new ProcotolFrameDecoder()); // 帧解码器 【与自定义编解码器 MessageCodecSharable一起配置参数】
                    ch.pipeline().addLast(LOGGING_HANDLER);            // 日志
                    ch.pipeline().addLast(MESSAGE_CODEC);              // 出站入站的 自定义编解码器 【 解析消息类型 】
                    ch.pipeline().addLast(new SimpleChannelInboundHandler<LoginRequestMessage>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
                            log.debug("收到登录请求: {}", msg);
                            String username = msg.getUsername();
                            String password = msg.getPassword();
                            boolean login = UserServiceFactory.getUserService().login(username, password);
                            LoginResponseMessage message;
                            if (login) {
                                message = new LoginResponseMessage(true, "登录成功");
                            } else {
                                message = new LoginResponseMessage(true, "登录成功");
                            }
                            ctx.writeAndFlush(message);
                        }
                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            log.error("处理登录请求时发生异常", cause);
                            ctx.close();
                        }
                    });

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
