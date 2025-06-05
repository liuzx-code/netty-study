package com.liuzx.netty.rpc.server.handler;

import com.liuzx.netty.asm.server.handler.AbstractRequestMessageHandler;
import com.liuzx.netty.asm.service.HelloService;
import com.liuzx.netty.asm.service.ServicesFactory;
import com.liuzx.netty.rpc.message.RpcRequestMessage;
import com.liuzx.netty.rpc.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;


@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends AbstractRequestMessageHandler<RpcRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message) {
        RpcResponseMessage response = new RpcResponseMessage();
        response.setSequenceId(message.getSequenceId());
        try {
            // 获取真正的实现对象
            HelloService service = (HelloService)
                    ServicesFactory.getService(Class.forName(message.getInterfaceName()));

            // 获取要调用的方法
            Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());

            // 反射调用方法
            Object invoke = method.invoke(service, message.getParameterValue());
            // 调用成功
            response.setReturnValue(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            // 调用异常
            response.setExceptionValue(e);
        }
        // 返回结果
        ctx.writeAndFlush(response);
    }
}
