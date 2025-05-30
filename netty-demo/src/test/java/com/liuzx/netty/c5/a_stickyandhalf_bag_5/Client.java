package com.liuzx.netty.c5.a_stickyandhalf_bag_5;

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
 * 3、分隔符，行解码器 效率比较低要一个一个的字节的去比对找到对应的分割符
 * 4、长度+消息内容 LTC解析器
 */
@Slf4j
public class Client {

    public static void main(String[] args) {
       send();
       log.debug("main over");
    }

    public static StringBuilder makeString(char c, int len) {
        StringBuilder sb = new StringBuilder(len + 2);
        for (int i = 0; i < len; i++) {
            sb.append(c);
        }
        sb.append("\n");
        return sb;
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
                                StringBuilder sb = makeString(c, random.nextInt(256) + 1);
                                c++;
                                buffer.writeBytes(sb.toString().getBytes());
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
 * 。代表分隔符只剩打印不出来\n
 * 22:56:29 [DEBUG] [nioEventLoopGroup-2-1] i.n.h.l.LoggingHandler - [id: 0xdd9b23c2, L:/127.0.0.1:62781 - R:/127.0.0.1:8080] WRITE: 1178B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000010| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000020| 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 30 |0000000000000000|
 * |00000030| 30 30 30 30 30 30 30 30 30 30 30 30 0a 31 31 31 |000000000000.111|
 * |00000040| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000050| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000060| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000070| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000080| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000090| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |000000a0| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |000000b0| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |000000c0| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |000000d0| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |000000e0| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |000000f0| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000100| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000110| 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 31 |1111111111111111|
 * |00000120| 31 0a 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |1.22222222222222|
 * |00000130| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |00000140| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |00000150| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |00000160| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |00000170| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |00000180| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |00000190| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |000001a0| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |000001b0| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |000001c0| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |000001d0| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |000001e0| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 32 |2222222222222222|
 * |000001f0| 32 32 32 32 32 32 32 32 32 32 32 32 32 32 0a 33 |22222222222222.3|
 * |00000200| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000210| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000220| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000230| 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 33 |3333333333333333|
 * |00000240| 33 33 33 33 33 33 33 33 33 33 33 33 0a 34 34 34 |333333333333.444|
 * |00000250| 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 |4444444444444444|
 * |00000260| 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 34 |4444444444444444|
 * |00000270| 34 34 34 34 34 34 0a 35 35 35 35 35 35 35 35 35 |444444.555555555|
 * |00000280| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |00000290| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |000002a0| 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 35 |5555555555555555|
 * |000002b0| 35 35 35 0a 36 36 36 36 36 36 36 36 36 36 36 36 |555.666666666666|
 * |000002c0| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |000002d0| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |000002e0| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |000002f0| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |00000300| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |00000310| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |00000320| 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 36 |6666666666666666|
 * |00000330| 36 36 36 36 0a 37 37 37 37 37 37 37 37 37 37 37 |6666.77777777777|
 * |00000340| 37 37 37 37 37 37 37 0a 38 38 38 38 38 38 38 38 |7777777.88888888|
 * |00000350| 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 |8888888888888888|
 * |00000360| 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 |8888888888888888|
 * |00000370| 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 |8888888888888888|
 * |00000380| 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 |8888888888888888|
 * |00000390| 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 |8888888888888888|
 * |000003a0| 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 |8888888888888888|
 * |000003b0| 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 |8888888888888888|
 * |000003c0| 38 38 38 0a 39 39 39 39 39 39 39 39 39 39 39 39 |888.999999999999|
 * |000003d0| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |000003e0| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |000003f0| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |00000400| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |00000410| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |00000420| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |00000430| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |00000440| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |00000450| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |00000460| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |00000470| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |00000480| 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 39 |9999999999999999|
 * |00000490| 39 39 39 39 39 39 39 39 39 0a                   |999999999.      |
 * +--------+-------------------------------------------------+----------------+
 * 22:56:29 [DEBUG] [nioEventLoopGroup-2-1] i.n.h.l.LoggingHandler - [id: 0xdd9b23c2, L:/127.0.0.1:62781 - R:/127.0.0.1:8080] FLUSH
 */