package com.liuzx.nio.c2;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.liuzx.netty.c1.ByteBufferUtil.debugAll;

public class TestByteBufferString005 {
    public static void main(String[] args) {
        // 1、字符串转ByteBuffer
        ByteBuffer buffer1 = ByteBuffer.allocate(10);
        buffer1.put("abcde".getBytes());
        debugAll(buffer1);

        // 2、Charset
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello"); // 会自动切换成读模式
        debugAll(buffer2);

        //  3、wrap
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());
        debugAll(buffer3);

        // 第2和3个都是切换到读模式，可以用decode模式转换为字符串模式
        buffer1.flip();
        String str1 = StandardCharsets.UTF_8.decode(buffer1).toString();
        System.out.println(str1); //      

        String str2 = StandardCharsets.UTF_8.decode(buffer2).toString();
        System.out.println(str2); //  hello

        String str3 = StandardCharsets.UTF_8.decode(buffer3).toString();
        System.out.println(str3); // hello
    }
}
