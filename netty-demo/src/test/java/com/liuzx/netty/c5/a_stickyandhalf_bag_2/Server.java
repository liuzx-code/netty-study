package com.liuzx.netty.c5.a_stickyandhalf_bag_2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                      // option 设置的是全局的， childOption 针对每个 channel 连接
                      // 调整系统的接收缓冲器(滑动窗口)
//                    .option(ChannelOption.SO_RCVBUF, 10)// 服务器接收缓存区调小一点，模拟半包现象
                     // 调整 Netty 缓冲区（ByteBuf） 默认值是1024 最小就是16再调小就不行了 模拟接受消息的大小 大于 接收缓冲区大小
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16,16,16))
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                // 会在连接建立后触发 channelActive
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    log.debug("connected {}", ctx.channel());
                                    super.channelActive(ctx);
                                }

                                // 会在连接结束后触发 channelInactive
                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    log.debug("disconnect {}", ctx.channel());
                                    super.channelInactive(ctx);
                                }
                            });
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(8080);
            log.debug("{} binding", channelFuture.channel());
            channelFuture.sync();
            log.debug("{} bound", channelFuture.channel());
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            log.debug("stoped");
        }
    }
}
