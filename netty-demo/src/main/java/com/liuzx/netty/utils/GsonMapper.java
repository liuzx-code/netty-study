package com.liuzx.netty.utils;

import com.google.gson.*;

import java.lang.reflect.Type;


public class GsonMapper {

    private static final Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec());
        GSON = builder.create();
    }

    /** 转化为 Bean 对象 */
    public static <T> T conv2Bean(Class<T> clazz, String json) {
        return GSON.fromJson(json, clazz);
    }

    /** 转化为 Json 对象 */
    public static <T> String conv2Json(T t) {
        return GSON.toJson(t);
    }

    /** 解决 Class.class 类型的转换错误 **/
    static class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return Class.forName(json.getAsString());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("类型转换异常", e);
            }
        }

        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }

}
