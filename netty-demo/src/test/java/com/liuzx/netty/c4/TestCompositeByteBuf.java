package com.liuzx.netty.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import static com.liuzx.netty.c4.TestByteBuf.log;

/**
 * 将多个小的ByteBuf组合成一个大的ByteBuf，不会发生数据的复制，缺点是增加复杂的维护，到新的ByteBuf要重新的计算
 */
public class TestCompositeByteBuf {
    public static void main(String[] args) {
        final ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{1,2,3,4,5});

        final ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf2.writeBytes(new byte[]{6,7,8,9,10});

        System.out.println(buf1.getClass()); // : class io.netty.buffer.PooledUnsafeDirectByteBuf  池化直接内存

        // readableBytes 返回可读取的字节数
        final ByteBuf bufferWriteCopy = ByteBufAllocator.DEFAULT.buffer(buf1.readableBytes()+buf2.readableBytes());
        bufferWriteCopy.writeBytes(buf1).writeBytes(buf2);
             /*
                read index:0 write index:10 capacity:10
                         +-------------------------------------------------+
                         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
                +--------+-------------------------------------------------+----------------+
                |00000000| 01 02 03 04 05 06 07 08 09 0a                   |..........      |
                +--------+-------------------------------------------------+----------------+
         */
        log(bufferWriteCopy);

        final CompositeByteBuf compositeByteBuf1 = ByteBufAllocator.DEFAULT.compositeBuffer();
//        compositeByteBuf1.addComponents(buf1, buf2);// // addComponents/addComponent 不会自动跳转写入指针的位置 怎么解决？
        compositeByteBuf1.addComponents(true,buf1, buf2);
        /*
        compositeByteBuf1.addComponents(buf1, buf2);
        read index:0 write index:0 capacity:0
        compositeByteBuf1.addComponents(true,buf1, buf2);
        read index:0 write index:10 capacity:10
                 +-------------------------------------------------+
                 |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
        +--------+-------------------------------------------------+----------------+
        |00000000| 01 02 03 04 05 06 07 08 09 0a                   |..........      |
        +--------+-------------------------------------------------+----------------+
         */
        log(compositeByteBuf1);


    }
}
