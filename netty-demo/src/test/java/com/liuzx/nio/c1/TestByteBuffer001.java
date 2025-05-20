package com.liuzx.nio.c1;


import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


@Slf4j
public class TestByteBuffer001 {
    public static void main(String[] args) {
        // 可以有哪些生成FileChannel?
        // 1、输入输出流；2、RandomAccessFile
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            // 准备缓冲区存取数据
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true) {
                // 从 channel 读取数据，向 buffer 进行写入
                int read = channel.read(buffer);
                log.debug("读取到的字节数：{}", read);
                if (read == -1) { // -1代表没有内容可以读取了
                    break;
                }
                // 打印 buffer中的内容
                buffer.flip(); // 开启读模式
                // 是否还有剩余未读的数据
                while (buffer.hasRemaining()) {
                    // 读取数据
                    byte b = buffer.get();
                    log.debug("实际字节{}", (char) b);
                }
                buffer.clear(); // 切换成写模式
            }
        } catch (IOException e) {
        }
    }
}
