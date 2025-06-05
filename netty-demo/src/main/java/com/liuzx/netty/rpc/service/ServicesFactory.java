package com.liuzx.netty.rpc.service;


import com.liuzx.netty.asm.config.Config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ServicesFactory {
    private static final Map<Class<?>, Object> SERVICES = new ConcurrentHashMap<>();

    static {
        Map<String, String> properties = Config.getServiceProperties();
        properties.forEach((key, value) -> {
            try {
                SERVICES.put(Class.forName(key), Class.forName(value).newInstance());
            } catch (Exception e) {
                throw new RuntimeException("ServicesFactory初始化异常", e);
            }
        });
    }

    @SuppressWarnings("all")
    public static <T> T getService(Class<T> clazz) {
        return (T) SERVICES.get(clazz);
    }

}
