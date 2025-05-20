package com.liuzx.netty.c4_nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;

import static com.liuzx.netty.c1.ByteBufferUtil.debugAll;
import static com.liuzx.netty.c1.ByteBufferUtil.debugRead;

/**
 * 传统阻塞IO是干等着，NIO是注册了关心的事件，有事件了才去处理
 * NIO使用select调用监听事件，当事件发生时开始进行处理，此时连接可以直接建立，数据也已经准备就绪，处理事件时，可以一次性处理多个事件，并且不需要等待，直接进行处理。
 * 多路复用，并不是一次处理多个事件，而是在秉着一种有事了再找我，你准备好了，再触发事件
 */
@Slf4j
public class ServerSelector {

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
        /**
         * 记录position，切换到写模式，compact方法将未读完的数据拷贝到起始位置，然后重置position和limit
         * position = 下一个可读位置 limit = capacity定义的容量
         */
        source.compact();
    }

    public static void main(String[] args) throws IOException {
        // 1、创建 Selector 管理多个 channel
        Selector selector = Selector.open();
        // 创建服务器连接
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 开启非阻塞模式
        ssc.configureBlocking(false);

        // 2、建立 selector 和 channel 的关联（注册）
        SelectionKey selectionKey = ssc.register(selector, 0, null);
        // 指定 channel 监听的事件类型，key 只关注 accept 事件
        selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("register key:{}", selectionKey);

        ssc.bind(new InetSocketAddress(8080));

        while (true) {
            //  3、selector 监听 channel 的事件，没有事件发生，线程阻塞，有事件发生，线程恢复运行
            // selector 事件未处理时 线程为非阻塞，事件发生后要么处理要么取消，不能置之不理
            selector.select();
            // 4、处理事件，selectedKeys() 包含所有发生的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); // 移除 ==SelectKeys== 中当前 key, 如果不移除，下次进来处理时就会报空指针问题
                log.debug("key:{}", key);
                // 5、 增加类型区分
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    ByteBuffer buffer = ByteBuffer.allocate(4);
                    SelectionKey scKey = sc.register(selector, 0, buffer); // 注册到selector上，并且关联buffer
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("scKey:{}", scKey);
                    log.debug("sc:{}", sc);
                } else if (key.isReadable()) {
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        /**
                         * 1、定义在这里会导致如果发送的字节大于定义的字节就会丢失
                         * 2、不能定义在while (iterator.hasNext()) 外面，因为所有的 channel 会用同一个缓存区
                         *
                         * 解决方案：用附件的方式解决
                         */
//                        ByteBuffer buffer = ByteBuffer.allocate(4);
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int read = channel.read(buffer); // 如果是正常断开，read 的方法的返回值是 -1
                        if (read == -1) {
                            key.cancel();
                        } else {
//                            buffer.flip();
//                            debugRead(buffer);
//                            buffer.clear();
//                            log.debug(Charset.defaultCharset().decode(buffer).toString());
                            spilit(buffer); // 会发现如果超过ByteBuffer的容量，数据会丢失
                            if (buffer.position() == buffer.limit()) {
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                buffer.flip();
                                newBuffer.put(buffer);
                                key.attach(newBuffer); // 替换掉旧的buffer
                            }
                        }
                    } catch (IOException e) { // 读操作可能因为断开连接而失败，异常断开
                        e.printStackTrace();
                        key.cancel(); // 因为客户端断开了,因此需要将 key 取消(从 ==selector== 的 keys 集合中真正删除 key)
                    }
                }


//                key.cancel(); // 取消事件
            }
        }

    }
}
