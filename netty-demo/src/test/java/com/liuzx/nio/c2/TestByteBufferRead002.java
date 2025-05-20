package com.liuzx.nio.c2;

import java.nio.ByteBuffer;

public class TestByteBufferRead002 {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a','b','c','d'});
        // 开启读模式
        buffer.flip();

        // 有两种读方法
        // get 一直往后读
        // rewind  可以回到起始位置读
        /*buffer.get(new byte[4]);
        debugAll(buffer);
        buffer.rewind();
        debugAll(buffer);
        System.out.println((char) buffer.get());
        debugAll(buffer);*/

        // mark & reset
        //mark 做一个标记，记录 position 位置，reset 是将 position 重置到 mark 的位置
        /*System.out.println((char) buffer.get()); // a
        System.out.println((char) buffer.get()); // b
        buffer.mark(); // 重点标记下一个位置
        System.out.println((char) buffer.get()); //  c
        System.out.println((char) buffer.get()); // d
        buffer.reset(); // 重置到上一个位置
        System.out.println((char) buffer.get()); // c
        System.out.println((char) buffer.get()); // d*/

        // get(i) 根据索引位置读取
        System.out.println((char) buffer.get(3)); // 索引是从0开始


    }
}
