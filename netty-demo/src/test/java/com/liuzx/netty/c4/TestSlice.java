package com.liuzx.netty.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static com.liuzx.netty.c4.TestByteBuf.log;

/**
 *
 * NIO的零拷贝：文件channel向socketChannel传输数据的时候可以不进过Java内存直接从文件走到socketChannel（网络设备），减少内存复制提高了性能
 #### slice
 【Netty的零拷贝】的体现之一，
 对原始 ByteBuf 进行切片成多个 ByteBuf，
 切片后的 ByteBuf 并没有发生内存复制，还是使用原始 ByteBuf 的内存，
 切片后的 ByteBuf 维护独立的 read，write 指针

 注意点：1. 切片的 ByteBuf 将已限制最大容量，不得追加
 2. ByteBuf 和 slice 分别维护自己独立的 read，write 指针
 3. 对 ByteBuf release【引用计数-1】操作之后，不得使用 slice切片的数据，除非 先使用 retain【引用计数+1】
 4. 正确用法：对单个切片进行 retain 和 release 成对处理，不会乱
 5. 【--- 无论 对 原buf 还是 slice 得到的 ByteBuf引用计数 都是一样的，都是对物理内存进行引用计数 ---】
 */
public class TestSlice {
    public static void main(String[] args) {
        final ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j'});

        /**
         * read index:0 write index:10 capacity:10
         *          +-------------------------------------------------+
         *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
         * +--------+-------------------------------------------------+----------------+
         * |00000000| 61 62 63 64 65 66 67 68 69 6a                   |abcdefghij      |
         * +--------+-------------------------------------------------+----------------+
         */
        log(buf);
        // 在切片过程中，没有发生数据复制
        // 切片后的 ByteBuf 有最大容量的限制，不允许进行写入，不然会影响其他的切片
        ByteBuf f1 = buf.slice(0, 5);
        f1.retain();
        /**
         * read index:0 write index:5 capacity:5
         *          +-------------------------------------------------+
         *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
         * +--------+-------------------------------------------------+----------------+
         * |00000000| 61 62 63 64 65                                  |abcde           |
         * +--------+-------------------------------------------------+----------------+
         */
        log(f1);
        ByteBuf f2 = buf.slice(5, 5);
        /**
         * read index:0 write index:5 capacity:5
         *          +-------------------------------------------------+
         *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
         * +--------+-------------------------------------------------+----------------+
         * |00000000| 66 67 68 69 6a                                  |fghij           |
         * +--------+-------------------------------------------------+----------------+
         */
        log(f2);
//        f1.writeByte('x'); // 会报错，数据越界Index0ut0fBoundsException
        System.out.println("释放原有 byteBuf 内存");
        /**
         * 会报错，IllegalReferenceCountException: refCnt: 0
         * 如果想继续使用f1、f2怎么解决？
         * 使用retain让引用计数加1
         */
        buf.release();
        log(f1);
        System.out.println("==============================================================");
        // 怎么证明新的切片跟原来的ByteBuf是同一个物理内存？改变切片的ByteBuf看看原始的ByteBuf是否变化
        f1.setByte(0,'b');
        /**
         * read index:0 write index:10 capacity:10
         *          +-------------------------------------------------+
         *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
         * +--------+-------------------------------------------------+----------------+
         * |00000000| 62 62 63 64 65 66 67 68 69 6a                   |bbcdefghij      |
         * +--------+-------------------------------------------------+----------------+
         */
        log(buf);
        /**
         * read index:0 write index:5 capacity:5
         *          +-------------------------------------------------+
         *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
         * +--------+-------------------------------------------------+----------------+
         * |00000000| 62 62 63 64 65                                  |bbcde           |
         * +--------+-------------------------------------------------+----------------+
         */
        log(f1);
    }
}
