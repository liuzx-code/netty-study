package com.liuzx.netty.rpc.protocol;


import com.liuzx.netty.utils.GsonMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;


public interface Serializable {

    /**
     * 序列化
     * @param t 对象
     * @param <T> 泛型
     * @return 字节数据
     */
    <T> byte[] serialize(T t);

    /**
     * 反序列化
     * @param clazz 对象类型
     * @param bytes 对象字节数据
     * @param <T> 泛型
     * @return 对象
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    /** 消息序列化算法枚举 **/
    enum Algorithm implements Serializable {
        /** Java序列化方式 **/
        Java {
            @Override
            public <T> byte[] serialize(T t) {
                try (
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(bos)
                ) {
                    oos.writeObject(t);
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("序列化失败", e);
                }
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try (ObjectInputStream oos = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
                    return (T) oos.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("反序列化失败", e);
                }
            }
        },
        /** Json序列化方式 **/
        Json {
            @Override
            public <T> byte[] serialize(T t) {
                return GsonMapper.conv2Json(t).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                return GsonMapper.conv2Bean(clazz, new String(bytes, StandardCharsets.UTF_8));
            }
        }
    }
}
