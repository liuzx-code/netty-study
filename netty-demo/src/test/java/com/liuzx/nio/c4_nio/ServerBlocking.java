package com.liuzx.nio.c4_nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.liuzx.netty.c1.ByteBufferUtil.debugRead;

@Slf4j
public class ServerBlocking {
    public static void main(String[] args) throws IOException {
        // 模拟 nio 单线程 阻塞模式 执行accpet 就不能执行read ，反之也是一样

        //0、创建 ByteBuffer 接收数据
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1、创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();

        // 2、绑定监听端口
        ssc.bind(new java.net.InetSocketAddress(8080));

        // 3、接受客户端连接，阻塞方法，没有客户端连接，线程阻塞\
        List<SocketChannel> channels = new ArrayList<>();
        while  (true) {
            log.debug("connecting...");
            SocketChannel sc = ssc.accept(); // 阻塞方法，没有客户端连接，线程阻塞, 线程停止运行
            log.debug("connected...{}",sc);
            channels.add(sc);
            for (SocketChannel channel : channels) {
                log.debug("before read...{}",channel);
                int read = channel.read(buffer);// 阻塞方法，没有数据，线程阻塞, 线程停止运行
                // 读取数据
                buffer.flip();
                // 打印 buffer中的内容
                debugRead(buffer);
                // 清空 buffer
                buffer.clear();
                log.debug("after read...{}",channel);
            }
        }

    }

}
