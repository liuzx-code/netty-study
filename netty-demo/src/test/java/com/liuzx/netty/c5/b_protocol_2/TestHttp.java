package com.liuzx.netty.c5.b_protocol_2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;


/**
 * Http 服务端
 */
@Slf4j
public class TestHttp {

    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                            // Http协议编解码器  解析成两部分 DefaultHttpRequest（包含请求行和请求头） 和 LastHttpContent$1（请求体）
                            ch.pipeline().addLast(new HttpServerCodec());
                            // 只关注某一个类型HttpRequest/HttpContent
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
                                    // 获取请求
                                    log.debug("url {}",request.uri());

                                    // 返回响应
                                    DefaultFullHttpResponse response = new DefaultFullHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);

                                    byte[] bytes = "<h1> Hello World </h1>".getBytes();
                                    // 设置响应头 告诉浏览器响应长度 不然浏览器一直响应
                                    response.headers().setInt(CONTENT_LENGTH, bytes.length);
                                    response.content().writeBytes(bytes);

                                    // 写入响应
                                    ctx.writeAndFlush(response);
                                }
                            });
                            /*ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    log.debug("{}", msg.getClass());

                                    if (msg instanceof HttpRequest) { // 请求行和请求头

                                    }

                                    if (msg instanceof HttpContent) { // 请求体

                                    }
                                }
                            });/// 编解码器后的结果进行处理 自定义*/
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }
}

/**
 * 13:56:13 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0x3541cdb9, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:55797] READ: 944B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 47 45 54 20 2f 69 6e 64 65 78 2e 68 74 6d 6c 20 |GET /index.html |
 * |00000010| 48 54 54 50 2f 31 2e 31 0d 0a 48 6f 73 74 3a 20 |HTTP/1.1..Host: |
 * |00000020| 6c 6f 63 61 6c 68 6f 73 74 3a 38 30 38 30 0d 0a |localhost:8080..|
 * |00000030| 43 6f 6e 6e 65 63 74 69 6f 6e 3a 20 6b 65 65 70 |Connection: keep|
 * |00000040| 2d 61 6c 69 76 65 0d 0a 73 65 63 2d 63 68 2d 75 |-alive..sec-ch-u|
 * |00000050| 61 3a 20 22 47 6f 6f 67 6c 65 20 43 68 72 6f 6d |a: "Google Chrom|
 * |00000060| 65 22 3b 76 3d 22 31 33 37 22 2c 20 22 43 68 72 |e";v="137", "Chr|
 * |00000070| 6f 6d 69 75 6d 22 3b 76 3d 22 31 33 37 22 2c 20 |omium";v="137", |
 * |00000080| 22 4e 6f 74 2f 41 29 42 72 61 6e 64 22 3b 76 3d |"Not/A)Brand";v=|
 * |00000090| 22 32 34 22 0d 0a 73 65 63 2d 63 68 2d 75 61 2d |"24"..sec-ch-ua-|
 * |000000a0| 6d 6f 62 69 6c 65 3a 20 3f 30 0d 0a 73 65 63 2d |mobile: ?0..sec-|
 * |000000b0| 63 68 2d 75 61 2d 70 6c 61 74 66 6f 72 6d 3a 20 |ch-ua-platform: |
 * |000000c0| 22 57 69 6e 64 6f 77 73 22 0d 0a 55 70 67 72 61 |"Windows"..Upgra|
 * |000000d0| 64 65 2d 49 6e 73 65 63 75 72 65 2d 52 65 71 75 |de-Insecure-Requ|
 * |000000e0| 65 73 74 73 3a 20 31 0d 0a 55 73 65 72 2d 41 67 |ests: 1..User-Ag|
 * |000000f0| 65 6e 74 3a 20 4d 6f 7a 69 6c 6c 61 2f 35 2e 30 |ent: Mozilla/5.0|
 * |00000100| 20 28 57 69 6e 64 6f 77 73 20 4e 54 20 31 30 2e | (Windows NT 10.|
 * |00000110| 30 3b 20 57 69 6e 36 34 3b 20 78 36 34 29 20 41 |0; Win64; x64) A|
 * |00000120| 70 70 6c 65 57 65 62 4b 69 74 2f 35 33 37 2e 33 |ppleWebKit/537.3|
 * |00000130| 36 20 28 4b 48 54 4d 4c 2c 20 6c 69 6b 65 20 47 |6 (KHTML, like G|
 * |00000140| 65 63 6b 6f 29 20 43 68 72 6f 6d 65 2f 31 33 37 |ecko) Chrome/137|
 * |00000150| 2e 30 2e 30 2e 30 20 53 61 66 61 72 69 2f 35 33 |.0.0.0 Safari/53|
 * |00000160| 37 2e 33 36 0d 0a 41 63 63 65 70 74 3a 20 74 65 |7.36..Accept: te|
 * |00000170| 78 74 2f 68 74 6d 6c 2c 61 70 70 6c 69 63 61 74 |xt/html,applicat|
 * |00000180| 69 6f 6e 2f 78 68 74 6d 6c 2b 78 6d 6c 2c 61 70 |ion/xhtml+xml,ap|
 * |00000190| 70 6c 69 63 61 74 69 6f 6e 2f 78 6d 6c 3b 71 3d |plication/xml;q=|
 * |000001a0| 30 2e 39 2c 69 6d 61 67 65 2f 61 76 69 66 2c 69 |0.9,image/avif,i|
 * |000001b0| 6d 61 67 65 2f 77 65 62 70 2c 69 6d 61 67 65 2f |mage/webp,image/|
 *+--------+-------------------------------------------------+----------------+
 *13:56:13 [DEBUG] [nioEventLoopGroup-3-1] c.l.n.c.b.TestHttp - url /index.html
 * 13:56:13 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0x3541cdb9, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:55797] WRITE: 61B
 *          +-------------------------------------------------+
 *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 48 54 54 50 2f 31 2e 31 20 32 30 30 20 4f 4b 0d |HTTP/1.1 200 OK.|
 * |00000010| 0a 63 6f 6e 74 65 6e 74 2d 6c 65 6e 67 74 68 3a |.content-length:|
 * |00000020| 20 32 32 0d 0a 0d 0a 3c 68 31 3e 20 48 65 6c 6c | 22....<h1> Hell|
 * |00000030| 6f 20 57 6f 72 6c 64 20 3c 2f 68 31 3e          |o World </h1>   |
 * +--------+-------------------------------------------------+----------------+
 * 13:56:13 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0x3541cdb9, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:55797] FLUSH
 * 13:56:13 [DEBUG] [nioEventLoopGroup-3-1] i.n.h.l.LoggingHandler - [id: 0x3541cdb9, L:/0:0:0:0:0:0:0:1:8080 - R:/0:0:0:0:0:0:0:1:55797] READ COMPLETE
 *
 */
