package com.liuzx.netty.c5.e_stickyandhalf_bag;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
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
                            // “\n”分隔符解码器 1024字符后找不到分隔符就报错
                            ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            // 注意顺序要先解码才能给到下一个处理器进行处理
//                            ch.pipeline().addLast(new FixedLengthFrameDecoder(10));
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
/**
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ: 202B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000010| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000020| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000030| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000040| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000050| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000060| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000070| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000080| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000090| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |000000a0| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |000000b0| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |000000c0| 30 30 30 30 30 30 30 30 30 30                   |0000000000      |
 * +--------+-------------------------------------------------+----------------+
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ: 116B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000010| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000020| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000030| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000040| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000050| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000060| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000070| 31 31 31 31                                     |1111            |
 * +--------+-------------------------------------------------+----------------+
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ: 67B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |00000010| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |00000020| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |00000030| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |00000040| 32 32 32                                        |222             |
 * +--------+-------------------------------------------------+----------------+
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ: 180B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000010| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000020| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000030| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000040| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000050| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000060| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000070| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000080| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000090| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |000000a0| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |000000b0| 33 33 33 33                                     |3333            |
 * +--------+-------------------------------------------------+----------------+
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ: 112B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 |4444444444444444|
 * |00000010| 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 |4444444444444444|
 * |00000020| 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 |4444444444444444|
 * |00000030| 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 |4444444444444444|
 * |00000040| 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 |4444444444444444|
 * |00000050| 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 |4444444444444444|
 * |00000060| 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 |4444444444444444|
 * +--------+-------------------------------------------------+----------------+
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ: 172B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |00000010| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |00000020| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |00000030| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |00000040| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |00000050| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |00000060| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |00000070| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |00000080| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |00000090| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |000000a0| 35 35 35 35 35 35 35 35 35 35 35 35             |555555555555    |
 * +--------+-------------------------------------------------+----------------+
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ: 92B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |00000010| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |00000020| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |00000030| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |00000040| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |00000050| 36 36 36 36 36 36 36 36 36 36 36 36             |666666666666    |
 * +--------+-------------------------------------------------+----------------+
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ: 95B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 37 37 37 37 37 37 37 37 37 37 37 37 37 37 37 37 |7777777777777777|
 * |00000010| 37 37 37 37 37 37 37 37 37 37 37 37 37 37 37 37 |7777777777777777|
 * |00000020| 37 37 37 37 37 37 37 37 37 37 37 37 37 37 37 37 |7777777777777777|
 * |00000030| 37 37 37 37 37 37 37 37 37 37 37 37 37 37 37 37 |7777777777777777|
 * |00000040| 37 37 37 37 37 37 37 37 37 37 37 37 37 37 37 37 |7777777777777777|
 * |00000050| 37 37 37 37 37 37 37 37 37 37 37 37 37 37 37    |777777777777777 |
 * +--------+-------------------------------------------------+----------------+
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ: 38B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 |8888888888888888|
 * |00000010| 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 |8888888888888888|
 * |00000020| 38 38 38 38 38 38                               |888888          |
 * +--------+-------------------------------------------------+----------------+
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ: 4B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 39 39 39 39                                     |9999            |
 * +--------+-------------------------------------------------+----------------+
 * 22:58:07 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xef552144, L:/127.0.0.1:8080 - R:/127.0.0.1:62972] READ COMPLETE
 */