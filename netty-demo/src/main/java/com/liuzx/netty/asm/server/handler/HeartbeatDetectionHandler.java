package com.liuzx.netty.asm.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;


/**
 * 心跳---处理器
 */
@Slf4j
@ChannelHandler.Sharable
public class HeartbeatDetectionHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent state = (IdleStateEvent) evt;
            if (state.state() == IdleState.READER_IDLE) {
//                log.info("IdleState.READER_IDLE 5s 没有接收到数据");
                ctx.channel().close();
            }
        }
    }
}
