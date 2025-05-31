package com.liuzx.netty.c5.a_stickyandhalf_bag_5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 粘包、半包 解决方案：
 * 1、短连接的方式、发送一次就断开一次，缺点：只能解决粘包，不是解决半包的问题，效率比较低，每发一次就得建立一次 eg：ctx.channel().close();
 * 2、定长解码器 发送方会出现粘包现象，接收方正常的解析，需要注意要先解码才能给下一个处理器将那些处理 eg：ch.pipeline().addLast(new FixedLengthFrameDecoder(10));
 * 3、分隔符，行解码器 效率比较低要一个一个的字节的去比对找到对应的分割符
 * 4、长度+消息内容 LTC解析器
 */
@Slf4j
public class TestLengthFieldDecoder {

    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(1024, 0, 4, 1, 4),
                new LoggingHandler(LogLevel.DEBUG)
        );

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        send(buffer, "Hello, world");
        send(buffer, "Hi!");
        channel.writeInbound(buffer);
    }

    private static void send(ByteBuf buffer, String content) {
        byte[] bytes = content.getBytes();
        int length = bytes.length;
        buffer.writeInt(length);
        buffer.writeByte(1);
        buffer.writeBytes(bytes);
    }

}
/**
 * writeAndFlush
 * 23:32:08 [DEBUG] [main] i.n.h.l.LoggingHandler - [id: 0xembedded, L:embedded - R:embedded] WRITE: 23B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 00 00 00 0c 48 65 6c 6c 6f 2c 20 77 6f 72 6c 64 |....Hello, world|
 * |00000010| 00 00 00 03 48 69 21                            |....Hi!         |
 * +--------+-------------------------------------------------+----------------+
 * 23:32:08 [DEBUG] [main] i.n.h.l.LoggingHandler - [id: 0xembedded, L:embedded - R:embedded] FLUSH
 *
 *
 * ==================================================================================================
 * writeInbound
 *
 *
 * 23:33:23 [DEBUG] [main] i.n.h.l.LoggingHandler - [id: 0xembedded, L:embedded - R:embedded] READ: 12B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 48 65 6c 6c 6f 2c 20 77 6f 72 6c 64             |Hello, world    |
 * +--------+-------------------------------------------------+----------------+
 * 23:33:23 [DEBUG] [main] i.n.h.l.LoggingHandler - [id: 0xembedded, L:embedded - R:embedded] READ: 3B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 48 69 21                                        |Hi!             |
 * +--------+-------------------------------------------------+----------------+
 * 23:33:23 [DEBUG] [main] i.n.h.l.LoggingHandler - [id: 0xembedded, L:embedded - R:embedded] READ COMPLETE
 *
 */