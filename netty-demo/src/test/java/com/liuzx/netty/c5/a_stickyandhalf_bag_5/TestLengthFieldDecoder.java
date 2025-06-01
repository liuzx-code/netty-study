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

    /**
     * ################################################
     * ######       基于长度字段的 帧解码器       ########
     * ################################################

     * #################### 案例1 ###################
     * 注意：内容读完后，再读到字节就当成 Length 如果不规范会报错
     *
     *  * <pre>
     *  * <b>lengthFieldOffset</b>   = <b>0</b>
     *  * <b>lengthFieldLength</b>   = <b>2</b> (000c 两个字节)
     *  * lengthAdjustment    = 0
     *  * initialBytesToStrip = 0 (= do not strip header)
     *  *
     *  * BEFORE DECODE (14 bytes)         AFTER DECODE (14 bytes)
     *  * +--------+----------------+      +--------+----------------+
     *  * | Length | Actual Content |----->| Length | Actual Content |
     *  * | 0x000C | "HELLO, WORLD" |      | 0x000C | "HELLO, WORLD" |
     *  * +--------+----------------+      +--------+----------------+
     *  服务器：获取内容长度，从0(lengthFieldOffset=0)开始，先读两(lengthFieldLength=2)个字节，000c = 12，知道内容长度是12
     *         再读 12个字节
     *  * </pre>
     * LengthFieldBasedFrameDecoder(
     *          int maxFrameLength,     限制最大长度，超过他没找到分隔符报错
     *          int lengthFieldOffset,  长度字段 偏移量
     *          int lengthFieldLength,  长度字段 本身长度
     *          int lengthAdjustment,   长度字段 为基准，跳过几个字节 才是内容
     *          int initialBytesToStrip 从头玻璃 几个字节，解析后将不出现
     * )
     *
     * #################### 案例2 【从头剥离几个字节】 ############
     *  * <pre>
     *  * lengthFieldOffset   = 0
     *  * lengthFieldLength   = 2
     *  * lengthAdjustment    = 0
     *  * <b>initialBytesToStrip</b> = <b>2</b> (= the length of the Length field) 从头剥离几个字节，剩下的都是内容
     *  *
     *  * BEFORE DECODE (14 bytes)         AFTER DECODE (12 bytes)
     *  * +--------+----------------+      +----------------+
     *  * | Length | Actual Content |----->| Actual Content |
     *  * | 0x000C | "HELLO, WORLD" |      | "HELLO, WORLD" |
     *  * +--------+----------------+      +----------------+
     *  * </pre>
     *
     *  #################### 案例3 【带消息头】 ###################
     *  <h3>3字节长度字段位于5字节头的末尾，不带头</h3>
     *
     *  * <pre>
     *  * <b>lengthFieldOffset</b>   = <b>2</b> (= the length of Header 1) 长度字段偏移量 长度= 2 = 头部长度
     *  * <b>lengthFieldLength</b>   = <b>3</b> 指定长度字段本身 为 3字节
     *  * lengthAdjustment    = 0
     *  * initialBytesToStrip = 0     不剥离字节长度，解析过后还是17字节
     *  *
     *  * BEFORE DECODE (17 bytes)                      AFTER DECODE (17 bytes)
     *  * +-- 2字节 --+-- 3字节 --+---- 12字节 -----+      +----------+----------+----------------+
     *  * | Header 1 |  Length  | Actual Content |----->| Header 1 |  Length  | Actual Content |
     *  * |  0xCAFE  | 0x00000C | "HELLO, WORLD" |      |  0xCAFE  | 0x00000C | "HELLO, WORLD" |
     *  * +----------+----------+----------------+      +----------+----------+----------------+
     *  * </pre>
     *
     *   #################### 案例4 【指定长度和长度调整(头部长度)跳过头部内容】 ###################
     *   <h3>3字节长度字段位于5字节头的开头，不带头</h3>
     *
     *  * <pre>
     *  * lengthFieldOffset   = 0
     *  * lengthFieldLength   = 3
     *  * <b>lengthAdjustment</b>    = <b>2</b> (= the length of Header 1)
     *  * initialBytesToStrip = 0
     *  *
     *  * BEFORE DECODE (17 bytes)                      AFTER DECODE (17 bytes)
     *  * +-- 3字节 --+-- 2字节 --+---- 12字节 ----+       +----------+----------+---------------+
     *  * |  Length  | Header 1 | Actual Content |----->|  Length  | Header 1 | Actual Content |
     *  * | 0x00000C |  0xCAFE  | "HELLO, WORLD" |      | 0x00000C |  0xCAFE  | "HELLO, WORLD" |
     *  * +----------+----------+----------------+      +----------+----------+----------------+
     *  Length = 12 表示内容长度
     *  lengthAdjustment = 2 指跳过两个字节长度的头部内容后 ，有 Length = 12 字节长度的主体消息内容
     *  * </pre>
     *
     *   #################### 案例5 【偏移1后的长度字段 调整1字节后 才是内容字段 在丢掉从头开始数3字节】 ########
     *   <h3>在4字节头部中间偏移1的2字节长度字段，带头头字段和长度字段</h3>
     *
     *  * <pre>
     *  * lengthFieldOffset   = 1 (= the length of HDR1)
     *  * lengthFieldLength   = 2
     *  * <b>lengthAdjustment</b>    = <b>1</b> (= the length of HDR2)
     *  * <b>initialBytesToStrip</b> = <b>3</b> (= the length of HDR1 + LEN)
     *  *
     *  * BEFORE DECODE (16 bytes)                       AFTER DECODE (13 bytes)
     *  * + 1字节 +- 2字节 -+ 1字节 +--- 12字节  ----+      +------+----------------+
     *  * | HDR1 | Length | HDR2 | Actual Content |----->| HDR2 | Actual Content |
     *  * | 0xCA | 0x000C | 0xFE | "HELLO, WORLD" |      | 0xFE | "HELLO, WORLD" |
     *  * +------+--------+------+----------------+      +------+----------------+
     *  lengthFieldOffset = 1 ：HDR1长度是1， 偏移1后才是长度字段Length
     *  lengthFieldLength = 2 : 长度字段Length长2 ，内容长度=12
     *  lengthAdjustment  = 1 : 长度字节Length调整1个字节，过后才是内容字段
     *  initialBytesToStrip = 3 : 要从头开始 剥离的字节长度，头3个字节不想要了
     *  * </pre>
     *
     */
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