package com.liuzx.netty.asm.config;


import com.liuzx.netty.asm.protocol.MySerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * 使用配置文件 获取 编解码方法
 */
public abstract class Config {

    static Properties properties;

    static {
        try(InputStream in = Config.class.getResourceAsStream("/application.properties")){

            properties = new Properties();
            properties.load(in);

        } catch (IOException e){

            throw new ExceptionInInitializerError(e);
        }
    }

    public static int getServerPort(){
        final String value = properties.getProperty("server.port");

        if(value == null)
        {
            return 8080;
        }else{
            return Integer.parseInt(value);
        }
    }

    public static MySerializer.Algorithm getMySerializerAlgorithm(){

        final String value = properties.getProperty("serialize.algorithm");
        if(value == null)
        {
            return MySerializer.Algorithm.Java;
        }else{
            // 拼接成  MySerializer.Algorithm.Java 或 MySerializer.Algorithm.Json
            return MySerializer.Algorithm.valueOf(value);
        }

    }

    /** 获取服务层映射 **/
    public static Map<String, String> getServiceProperties() {
        HashMap<String, String> map = new HashMap<>(2);
        properties.stringPropertyNames()
                .stream().filter(name -> name.endsWith("Service"))
                .forEach(name -> map.put(name, properties.getProperty(name)));
        return map;
    }


}
