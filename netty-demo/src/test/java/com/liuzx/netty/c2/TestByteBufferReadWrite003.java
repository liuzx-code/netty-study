package com.liuzx.netty.c2;

import java.nio.ByteBuffer;

import static com.liuzx.netty.c1.ByteBufferUtil.debugAll;


public class TestByteBufferReadWrite003 {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(10);
        buffer.put((byte) 0x61); // 写入’a‘
        debugAll(buffer);
        buffer.put(new byte[]{(byte) 0x62, (byte) 0x63, (byte) 0x64}); // b c d e
        debugAll(buffer);
//        System.out.println(buffer.get()); // 当前位置是在索引5的位置，读取到的是0 get一次索引位置的字节，索引位置加1
        debugAll(buffer);
        buffer.flip(); // 切换到读模式
        System.out.println(buffer.get());
        debugAll(buffer);
        buffer.compact(); // 切换到写模式，compact方法将未读完的数据拷贝到起始位置，然后重置position和limit
        debugAll(buffer);
        buffer.put(new byte[]{(byte) 0x65, (byte) 0x6f});
        debugAll(buffer);
    }
}
