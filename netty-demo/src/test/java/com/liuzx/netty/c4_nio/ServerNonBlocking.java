package com.liuzx.netty.c4_nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.liuzx.netty.c1.ByteBufferUtil.debugRead;

@Slf4j
public class ServerNonBlocking {
    public static void main(String[] args) throws IOException {
        // 模拟 nio 单线程 非阻塞模式 执行accpet，非阻塞，没有就返回null  执行read，非阻塞，没有就返回0

        //0、创建 ByteBuffer 接收数据
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1、创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 默认是阻塞模式

        // 2、绑定监听端口
        ssc.bind(new java.net.InetSocketAddress(8080));

        // 3、接受客户端连接，阻塞方法，没有客户端连接，线程阻塞\
        List<SocketChannel> channels = new ArrayList<>();
        while  (true) {
            SocketChannel sc = ssc.accept(); // 非阻塞，线程还会继续执行，如果没有建立连接，sc返回null
            if (sc != null) {
                log.debug("connected...{}",sc);
                sc.configureBlocking(false); // 默认是阻塞模式
                channels.add(sc);
            }
            for (SocketChannel channel : channels) {
                int read = channel.read(buffer);// 非阻塞，线程还会继续执行，如果没有读取到数据，read返回0
                if (read > 0) {
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

}
