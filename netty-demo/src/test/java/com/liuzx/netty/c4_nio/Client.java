package com.liuzx.netty.c4_nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Client {
    public static void main(String[] args) throws IOException {
        // 1、创建服务器
        SocketChannel sc = SocketChannel.open();

        // 2、连接服务器
        sc.connect(new InetSocketAddress("localhost", 8080));

        System.out.println("waiting ......");

        sc.write(Charset.defaultCharset().encode("0123456789abcdef123456789\n"));

        System.out.println("waited ......");
        // 3、发送数据
//        sc.write(java.nio.ByteBuffer.wrap(new String("hello").getBytes()));

    }
}
