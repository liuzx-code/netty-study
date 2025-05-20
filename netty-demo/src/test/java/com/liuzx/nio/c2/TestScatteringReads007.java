package com.liuzx.nio.c2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.liuzx.netty.c1.ByteBufferUtil.debugAll;

public class TestScatteringReads007 {
    public static void main(String[] args) {
        // 分散读取
        try (FileChannel r = new RandomAccessFile("words.txt", "r").getChannel()) { // 获取文件通道 r 读模式
            // 分别读取前3个字节，中间3个字节，后5个字节
            ByteBuffer b1 = ByteBuffer.allocate(3);
            ByteBuffer b2 = ByteBuffer.allocate(3);
            ByteBuffer b3 = ByteBuffer.allocate(5);
            r.read(new ByteBuffer[]{b1, b2, b3});
            b1.flip();
            b2.flip();
            b3.flip();
            debugAll(b1);
            debugAll(b2);
            debugAll(b3);
        } catch (IOException e) {
        }
    }
}
