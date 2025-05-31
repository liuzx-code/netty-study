package com.liuzx.netty.c5.a_stickyandhalf_bag_3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
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
                            // 注意顺序要先解码才能给到下一个处理器进行处理
                            ch.pipeline().addLast(new FixedLengthFrameDecoder(10));
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
 * 接收方这边正常解析了
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ: 10B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 30 30 30 30 30 5f 5f 5f 5f 5f                   |00000_____      |
 * +--------+-------------------------------------------------+----------------+
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ COMPLETE
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ: 10B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 31 31 5f 5f 5f 5f 5f 5f 5f 5f                   |11________      |
 * +--------+-------------------------------------------------+----------------+
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ: 10B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 32 32 32 5f 5f 5f 5f 5f 5f 5f                   |222_______      |
 * +--------+-------------------------------------------------+----------------+
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ COMPLETE
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ: 10B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 33 33 33 33 33 33 33 33 5f 5f                   |33333333__      |
 * +--------+-------------------------------------------------+----------------+
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ COMPLETE
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ: 10B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 34 5f 5f 5f 5f 5f 5f 5f 5f 5f                   |4_________      |
 * +--------+-------------------------------------------------+----------------+
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ: 10B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 35 5f 5f 5f 5f 5f 5f 5f 5f 5f                   |5_________      |
 * +--------+-------------------------------------------------+----------------+
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ COMPLETE
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ: 10B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 36 5f 5f 5f 5f 5f 5f 5f 5f 5f                   |6_________      |
 * +--------+-------------------------------------------------+----------------+
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ: 10B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 37 37 37 5f 5f 5f 5f 5f 5f 5f                   |777_______      |
 * +--------+-------------------------------------------------+----------------+
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ COMPLETE
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ: 10B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 38 38 38 5f 5f 5f 5f 5f 5f 5f                   |888_______      |
 * +--------+-------------------------------------------------+----------------+
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ COMPLETE
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ: 10B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 39 39 39 39 39 39 39 39 39 5f                   |999999999_      |
 * +--------+-------------------------------------------------+----------------+
 * 22:41:54 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0xc0538558, L:/127.0.0.1:8080 - R:/127.0.0.1:61800] READ COMPLETE
 */