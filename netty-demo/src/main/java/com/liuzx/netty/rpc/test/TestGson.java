package com.liuzx.netty.rpc.test;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * 复现错误：Attempted to serialize java.lang.Class: java.lang.String. Forgot to register a type adapter?
 * <p>
 * 注意事项：
 * JsonPrimitive 只能接受基本类型或字符串作为参数。
 * 如果你传入了不支持的类型（如 Class.class），Gson 会抛出异常，例如：
 */
public class TestGson {
    public static void main(String[] args) {
        // 如何使用转换器
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
//        System.out.println(new Gson().toJson(String.class)); // 报错
        System.out.println(gson.toJson(String.class));


    }
    static class ClassCodec implements JsonSerializer<Class<?>>,  JsonDeserializer<Class<?>> {
        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            // 确定具体的消息类型
            // src.getName() 通常返回一个 String 类型的名称，比如类名、方法名或其他标识符。
            // JsonPrimitive 创建一个 Gson 的 JsonPrimitive 对象 它表示 JSON 中的基本数据类型值（如字符串、数值、布尔值等）
            String name = src.getName();
            System.out.println("name :"+name);
            JsonPrimitive primitive = new JsonPrimitive(name);
            System.out.println("primitive :"+primitive);
            return primitive;
        }

        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            try {
                // jsonElement.getAsString() 返回的Class对象全限定类名字符串
                //  Class.forName  返回Class<?> 对象
                String string = jsonElement.getAsString();
                System.out.println("string :"+string);
                Class<?> aClass = Class.forName(string);
                System.out.println("aClass :"+aClass);
                return aClass;
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("类型转换异常", e);
            }
        }
    }

}
