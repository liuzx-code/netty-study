package com.liuzx.netty.c3_filespaths;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class TestFileChannelTransferTo009 {
    public static void main(String[] args) {
        try (
                FileChannel from = new FileInputStream("data.txt").getChannel();
                FileChannel to = new FileOutputStream("to.txt").getChannel();
        ) {
            //  transferTo 方法 底层调用的是操作系统的零拷贝 一次传输只能支持2G
            long size = from.size();
            System.out.println(size);
            for (long left = size; left > 0; ) {
                System.out.println("position:" + (size - left) + "left:" + left);
                left -= from.transferTo(size - left, left, to); // count 应该判断是否超过2G
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
