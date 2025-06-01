package com.liuzx.netty.asm.server.handler;

import io.netty.channel.SimpleChannelInboundHandler;
import org.reflections.Reflections;

import java.util.List;
import java.util.stream.Collectors;


public abstract class AbstractRequestMessageHandler<T> extends SimpleChannelInboundHandler<T> {

    public static final List<AbstractRequestMessageHandler<?>> SHARABLE_HANDLERS;

    static {
        // 加载所有请求消息处理器，并且是 Sharable 安全的，共享注册
        String pack = AbstractRequestMessageHandler.class.getPackage().getName();
        Reflections reflections = new Reflections(pack);
        SHARABLE_HANDLERS = reflections.getSubTypesOf(AbstractRequestMessageHandler.class)
                .stream().filter(clazz -> clazz.isAnnotationPresent(Sharable.class)).map(clazz -> {
                    try {
                        return (AbstractRequestMessageHandler<?>) clazz.newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
    }

}