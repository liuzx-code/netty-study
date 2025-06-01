package com.liuzx.netty.asm.server.handler;

import com.liuzx.netty.asm.message.RpcRequestMessage;
import com.liuzx.netty.asm.message.RpcResponseMessage;
import com.liuzx.netty.asm.service.HelloService;
import com.liuzx.netty.asm.service.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;


@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends AbstractRequestMessageHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        RpcResponseMessage responseMessage = new RpcResponseMessage();
        responseMessage.setSequenceId(msg.getSequenceId());
        try {
            Class<?> aClass = Class.forName(msg.getInterfaceName());
            HelloService service = (HelloService) ServicesFactory.getService(aClass);
            Method method = aClass.getMethod(msg.getMethodName(), msg.getParameterTypes());
            Object re = method.invoke(service, msg.getParameterValue());
            responseMessage.setReturnValue(re);
        } catch (Exception e) {
            responseMessage.setExceptionValue(new Exception("远程调用失败" + e.getCause().getMessage()));
        }
        ctx.writeAndFlush(responseMessage);
    }
}
