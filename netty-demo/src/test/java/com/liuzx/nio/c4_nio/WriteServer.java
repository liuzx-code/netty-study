package com.liuzx.nio.c4_nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

@Slf4j
public class WriteServer {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        ssc.bind(new InetSocketAddress(8080));

        while(true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while(iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if(key.isAcceptable()) {
                    log.debug("{}", key);
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    SelectionKey sckey = sc.register(selector, SelectionKey.OP_READ);
                    // 1、向客户端发送大量的数据
                    StringBuffer sb = new StringBuffer();
                    for(int i = 0; i < 30000000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());

                    // 2、返回值代表实际写入的字节数
                    int write = sc.write(buffer);
                    log.debug("{}", write);

                    // 3、判断是否有剩余数据
                    if (buffer.hasRemaining()) {
                        log.debug("剩余数据：{}", buffer.remaining());
                        // 4、关注可写事件 （原来的事件 +  可写事件）
                        sckey.interestOps(sckey.interestOps() + SelectionKey.OP_WRITE);
                        // 5、将未写完的数据保存到 sckey 上，便于后续继续写
                        sckey.attach(buffer);
                    }
                } else if (key.isWritable()) {
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel sc = (SocketChannel) key.channel();
                    sc.write(buffer);
                    // 6、清理操作
                    if (!buffer.hasRemaining()){
                        key.attach(null); // 需要清除buffer
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE); // 不需要关注可写事件
                    }
                }
            }

        }
    }
}
