package com.liuzx.netty.c2;

import java.nio.ByteBuffer;

import static com.liuzx.netty.c1.ByteBufferUtil.debugAll;

public class TestByteBufferExam008 {
    public static void main(String[] args) {
        /**
         * 网络上有多条数据发送给服务端，数据之间使用\n 进行分隔
         * 但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为
         * Hello,world\n
         * I'm zhangsan\n
         * How are you?\n
         * 变成了下面的两个 byteBuffer(粘包，半包)
         * Hello,world\nI'm zhangsan\nHo
         * w are you?\n
         * 现在要求你编写程序，将错乱的数据恢复成原始的按 n 分隔的数据
         *
         * 1）. 粘包（Sticky Packets）
         * 定义：发送方发送的多个数据包被接收方在一个缓冲区中读取，导致多个数据包“粘”在一起。
         * 原因：
         * 发送方使用了 Nagle 算法优化小数据包的发送，将多个小数据包合并为一个大的数据包发送。
         * 接收方未能及时读取缓冲区中的数据，导致多个数据包堆积在缓冲区中一起被读取。
         *
         *
         * 2）. 半包（Half Packet）
         * 定义：一个完整的数据包被分成了多个部分接收。
         * 原因：
         * 数据包过大，超过底层协议的传输单元（如 TCP 的 MSS 或 UDP 的 MTU），因此被拆分成多个包传输。
         * 接收方缓冲区大小不足以容纳整个数据包。
         *
         *
         * 3. 解决方案
         * 为了处理粘包和半包问题，通常需要在应用层定义一种数据边界标识或长度字段来区分数据包。以下是几种常见做法：
         * (1) 使用分隔符
         * 在每个数据包末尾添加固定的分隔符（如 \n、\r\n 等）。
         * 接收端按分隔符解析数据。
         * 适用场景：文本协议（如 HTTP、SMTP）。
         * (2) 固定长度消息
         * 每个数据包固定长度，不足的部分用空字符填充。
         * 接收端按照固定长度读取数据。
         * 适用场景：数据格式简单且长度固定的场景。
         * (3) 消息头 + 消息体
         * 每个数据包包含一个消息头（Header），其中记录消息体（Body）的长度。
         * 接收端先读取消息头，获取消息体长度后，再读取完整的消息体。
         *
         */

        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        spilit(source);
        source.put("w are you?\n".getBytes());
        spilit(source);
    }

    private static void spilit(ByteBuffer source) {
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            if (source.get(i) == '\n') {
                // 找到一条完整的数据
                int length = i + 1 - source.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                // 从 source 中读取数据到 target 中
                for (int j = 0; j < length; j++) {
                    target.put(source.get());
                }
                debugAll(target);
            }
        }
        source.compact(); // 记录position，切换到写模式，compact方法将未读完的数据拷贝到起始位置，然后重置position和limit
    }
}
