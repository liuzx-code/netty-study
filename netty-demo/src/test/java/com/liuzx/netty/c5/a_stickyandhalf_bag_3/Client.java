package com.liuzx.netty.c5.a_stickyandhalf_bag_3;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Random;

/**
 * 粘包、半包 解决方案：
 * 1、短连接的方式、发送一次就断开一次，缺点：只能解决粘包，不是解决半包的问题，效率比较低，每发一次就得建立一次 eg：ctx.channel().close();
 * 2、定长解码器 发送方会出现粘包现象，接收方正常的解析，需要注意要先解码才能给下一个处理器将那些处理 eg：ch.pipeline().addLast(new FixedLengthFrameDecoder(10));
 */
@Slf4j
public class Client {

    public static void main(String[] args) {
       send();
       log.debug("main over");
    }

    private static void send() {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(worker);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    log.debug("connetted...");
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("sending...");
                            ByteBuf buffer = ctx.alloc().buffer();
                            char c = '0';
                            Random random = new Random();
                            for (int i = 0; i < 10; i++) {
                                byte[] bytes = fill10Bytes(c, random.nextInt(10) + 1);
                                c++;
                                buffer.writeBytes(bytes);
                            }
                            ctx.writeAndFlush(buffer);
                        }
                    });
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8080).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }

    public static byte[] fill10Bytes(char c, int len) {
        byte[] bytes = new byte[10];
        Arrays.fill(bytes, (byte) '_');
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) c;
        }
        System.out.println(new String(bytes));
        return bytes;
    }
}
/**
 *
 * 发送方出现粘包的现象
 *
 * 22:41:54 [DEBUG] [nioEventLoopGroup-2-1] c.l.n.c.c.Client - sending...
 * 00000_____
 * 11________
 * 222_______
 * 33333333__
 * 4_________
 * 5_________
 * 6_________
 * 777_______
 * 888_______
 * 999999999_
 * 22:41:54 [DEBUG] [nioEventLoopGroup-2-1] i.n.h.l.LoggingHandler - [id: 0x65325201, L:/127.0.0.1:61800 - R:/127.0.0.1:8080] WRITE: 100B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 30 30 30 30 30 5f 5f 5f 5f 5f 31 31 5f 5f 5f 5f |00000_____11____|
 * |00000010| 5f 5f 5f 5f 32 32 32 5f 5f 5f 5f 5f 5f 5f 33 33 |____222_______33|
 * |00000020| 33 33 33 33 33 33 5f 5f 34 5f 5f 5f 5f 5f 5f 5f |333333__4_______|
 * |00000030| 5f 5f 35 5f 5f 5f 5f 5f 5f 5f 5f 5f 36 5f 5f 5f |__5_________6___|
 * |00000040| 5f 5f 5f 5f 5f 5f 37 37 37 5f 5f 5f 5f 5f 5f 5f |______777_______|
 * |00000050| 38 38 38 5f 5f 5f 5f 5f 5f 5f 39 39 39 39 39 39 |888_______999999|
 * |00000060| 39 39 39 5f                                     |999_            |
 * +--------+-------------------------------------------------+----------------+
 * 22:41:54 [DEBUG] [nioEventLoopGroup-2-1] i.n.h.l.LoggingHandler - [id: 0x65325201, L:/127.0.0.1:61800 - R:/127.0.0.1:8080] FLUSH
 */
