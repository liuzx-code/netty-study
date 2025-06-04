package com.liuzx.netty.asm.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * 设置直接内存：-Dio.netty.noPreferDirect=false
 * 1、为什么指定了使用堆内存最终还是使用直接内存？2、为什么容量（cap）是1024？
 * receive buf UnpooledByteBufAllocator$InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf(ridx: 0, widx: 5, cap: 1024)
 * 网络io读取数据的时候直接内存比堆内存更高
 *
 * <p>
 * 查看源码：
 * 1、因为是读取的数据所以从读的方法进行入手，read:163, AbstractNioByteChannel$NioByteUnsafe (io.netty.channel.nio) </br>
 * 2、进一步了解byteBuf是怎么创建的？byteBuf = allocHandle.allocate(allocator);</br>
 * 3、关注allocHandle分配器，是RecvByteBufAllocator 的内部类，allocate 方法，会创建 byteBuf，负责决定byteBuf的大小和使用直接内存还是堆内存</br>
 * - final RecvByteBufAllocator.Handler allocHandle = recvBufAllocHandle();</br>
 * 4、byteBuf 的分配器，负责是池化还是非池化的</br>
 * - final ByteBufAllocator allocator = config.getAllocator();</br>
 * - 设置非池化: -Dio.netty.allocator.type=unpooled|pooled</br>
 * 5、进一步分析allocHandle.allocate(allocator)，</br>
 * - DefaultMaxMessagesRecvByteBufAllocatol 调用传入的ByteBuf判断是否是io线程,如果的io线程就直接使用的直接内存</br>
 * - alloc.ioBuffer(guess())</br>
 * 6、进一步分析recvBufAllocHandle()查看哪里被调用了</br>
 * public DefaultChannelconfig(channel channel){</br>
 *      this(channel, new AdaptiveRecvByteBufAllocator());</br>
 * }</br>
 *public AdaptiveRecvByteBufAllocator(){</br>
 *  this(DEFAULT_MINIMUM, DEFAULT_INITIAL, DEFAULT_MAXIMUM);</br>
 *}</br>
 * - DEFAULT_MINIMUM 最小容量  64字节</br>
 * - DEFAULT_INITIAL 默认容量 1024字节</br>
 * - DEFAULT_MAXIMUM 最大容量 65536字节</br>
 *
 * </p>
 * @author liuzx
 */
@Slf4j
public class TestByteBuf {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                 log.debug("receive buf {}",msg);
                            }
                        });
                    }
                })
                .bind(8080);
    }
}
