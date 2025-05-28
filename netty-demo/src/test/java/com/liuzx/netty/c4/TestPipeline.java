package com.liuzx.netty.c4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * 21:06:10 [DEBUG] [nioEventLoopGroup-2-2] c.l.n.c.TestPipeline - 1
 * 21:06:10 [DEBUG] [nioEventLoopGroup-2-2] c.l.n.c.TestPipeline - 2
 * 21:06:10 [DEBUG] [nioEventLoopGroup-2-2] c.l.n.c.TestPipeline - 3
 * 21:06:10 [DEBUG] [nioEventLoopGroup-2-2] c.l.n.c.TestPipeline - 6
 * 21:06:10 [DEBUG] [nioEventLoopGroup-2-2] c.l.n.c.TestPipeline - 5
 * 21:06:10 [DEBUG] [nioEventLoopGroup-2-2] c.l.n.c.TestPipeline - 4
 */
@Slf4j
public class TestPipeline {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        //  1.通过 channel 拿到 pipeline
                        ChannelPipeline pipeline = ch.pipeline();
                        // 2.添加处理器 head -> h1 -> h2 -> h3 -> h4 -> h5 -> h6 -> tail
                        // 添加 StringDecoder 来解析客户端发来的字符串
//                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast("h1", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("1，msg: {}",msg);
                                ByteBuf buf = (ByteBuf) msg;
                                // 拿到字符串结果
                                String name = buf.toString(Charset.defaultCharset());
                                log.debug("1，name: {}", name);
                                super.channelRead(ctx, name);
                            }
                        });

                        pipeline.addLast("h2", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object name) throws Exception {
                                Student student = new Student(name.toString());
                                log.debug("2，student：{}",student);
                                // 将数据传递给下一个 handler，如果不调用，数据不会向下传递
                                super.channelRead(ctx, student);
                            }
                        });

                        pipeline.addLast("h4-1", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("4-1");
                                super.write(ctx, msg, promise);
                            }
                        });

                        pipeline.addLast("h3", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("3，结果{}，class:{}", msg, msg.getClass());
                                super.channelRead(ctx, msg);
                                // ctx  是 向上/前找处理器
                                ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("hahaha".getBytes()));
//                                ch.writeAndFlush(ctx.alloc().buffer().writeBytes("hahaha".getBytes()));
                            }
                        });

                        pipeline.addLast("h4", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("4");
                                super.write(ctx, msg, promise);
                            }
                        });

                        pipeline.addLast("h5", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("5");
                                super.write(ctx, msg, promise);
                            }
                        });

                        pipeline.addLast("h6", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("6");
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                }).bind(8080);
    }

    @Data
    @AllArgsConstructor
    static class Student {
        private String name;
    }
}
