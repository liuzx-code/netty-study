package com.liuzx.nio.c5_aio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;

import static com.liuzx.netty.c1.ByteBufferUtil.debugAll;

@Slf4j
public class AioFileChannel {
    public static void main(String[] args) throws IOException, InterruptedException {
//        log.debug("Current directory: {}", System.getProperty("user.dir"));
//        System.out.println(Paths.get("netty-demo/data.txt"));
        CountDownLatch latch = new CountDownLatch(1);
       AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("netty-demo/data.txt"), StandardOpenOption.READ);
            // 参数1：ByteBuffer
            // 参数2：读取的起始位置
            // 参数3：附件
            // 参数4：回调对象 CompletionHandler
            ByteBuffer buffer = ByteBuffer.allocate(16);
            log.debug("read begin...");
            channel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    log.debug("read complete...{}", result);
                    attachment.flip();
                    debugAll(attachment);
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    log.debug("read failed...");
                    exc.printStackTrace();
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }
            });
            log.debug("read end...");
            latch.await(); // 主线程等待异步操作完成后再退出
//        System.in.read(); // 阻塞主线程，防止主线程结束，打印线程（守护线程也跟着提前结束导致无法打印出内容）
    }
}
