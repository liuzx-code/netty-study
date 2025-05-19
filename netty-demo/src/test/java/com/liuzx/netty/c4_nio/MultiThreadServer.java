package com.liuzx.netty.c4_nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static com.liuzx.netty.c1.ByteBufferUtil.debugRead;

/**
 * 线程模型：Boss线程负责处理accept事件，Worker线程负责处理read/write事件
 */
@Slf4j
public class MultiThreadServer {
    public static void main(String[] args) throws IOException {
        Thread.currentThread().setName("boss");
        ServerSocketChannel boss = ServerSocketChannel.open();
        boss.configureBlocking(false);

        Selector selector = Selector.open();
        SelectionKey bossKey = boss.register(selector, 0, null);
        bossKey.interestOps(SelectionKey.OP_ACCEPT);

        boss.bind(new InetSocketAddress(8080));
        // 1、创建固定数据的 worker 并初始化
//        Worker  worker = new Worker("worker-0");
//        worker.register();
        // 使用多worker
        // Runtime.getRuntime().availableProcessors() 获取CPU核数，但是JDK10之前有bug，如果项目部署在docker 获取到的分配给docker容器的cup核心数，而不是docker宿主机的cpu核数。
        Worker[] workers = new Worker[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }
        // 使用计数器
        AtomicInteger index = new AtomicInteger();
        while(true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while(iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if(key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    log.debug("connected{}", sc.getRemoteAddress());
                    // 2、注册到worker的selector上
                    log.debug("before register {}", sc.getRemoteAddress());
                    /**
                     * 把 register 方法放这里执行，如果执行 worker.register(); 方法，boss 线程快于 worker 线程，那么可读事件就会被注册上，客户端发送消息就会被接收，
                     * 但是如果有多个客户端的情况下，第一次执行完后selector.select();就会处于阻塞状态，可读事件又再一次不能注册上
                     * 解决方法：
                     *  1、使用多线程时候，让worker.register();  和 sc.register(worker.selector,SelectionKey.OP_READ,  null);  在同一个线程上进行处理
                     */
                    // 使用轮训
                    workers[index.getAndIncrement() % workers.length].register(sc);
//                    sc.register(worker.selector,SelectionKey.OP_READ,  null);  // 为什么没有注册上事件？原因是执行 worker.register(); 方法的时候，selector 在 worker 线程中先执行

                    log.debug("after register {}", sc.getRemoteAddress());
                }
            }

        }
    }
    static class Worker  implements Runnable {

        private Thread  thread;
        private Selector selector;
        private String name;
        private volatile boolean start = false; //  是否启动
        private ConcurrentLinkedDeque<Runnable> queue = new ConcurrentLinkedDeque<>();

        public Worker(String name) {
            this.name = name;
        }

        // 初始化线程和 Selector
        public void register(SocketChannel sc) throws IOException {
            if (!start) {
                thread = new Thread(this, name);
                thread.start();
                selector = Selector.open();
                start = true;
            }
            /**
             * 虽然在同一个 register 方法中执行，但是会分别执行到两个线程的 run 方法中
             * 解决方法：
             * 1、让一个队列存起来，一个线程处理一个队列
             * 2、使用wakeup
             */
            // f1
//            queue.add(() -> {
//                try {
//                    sc.register(selector,SelectionKey.OP_READ,  null);
//                } catch (ClosedChannelException e) {
//                    e.printStackTrace();
//                }
//            });
            // f2
            selector.wakeup(); // 唤醒 selector 无selector是先执行还是后执行都是被唤醒的
            sc.register(selector,SelectionKey.OP_READ,  null);

        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select(); // 也可以执行wakeup()结束堵塞状态
//                    Runnable task = queue.poll(); // 如果用队列的话执行任务就会有点晚，需要等待下次事件触发才结束 selector 结束阻塞
//                    if (task != null) {
//                        task.run();
//                    }
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel sc = (SocketChannel) key.channel();
                            sc.read(buffer);
                            buffer.flip();
                            debugRead(buffer);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}


