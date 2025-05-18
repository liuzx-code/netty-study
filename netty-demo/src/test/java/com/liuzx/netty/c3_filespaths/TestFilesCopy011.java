package com.liuzx.netty.c3_filespaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestFilesCopy011 {
    public static void main(String[] args) throws IOException {
        // 指定目录进行多级拷贝到目标目录下
        String source = "D:\\workspace\\netty\\src\\test\\resources\\files";
        String target = "D:\\workspace\\netty\\src\\test\\resources\\files_copy";
        Files.walk(Paths.get(source)).forEach(path -> {
            try {
                String targetName = path.toString().replace(source, target);
                // 判断是否是目录
                if (Files.isDirectory(path)) {
                    Files.createDirectory(Paths.get(targetName));
                }
                // 判断是否是文件
                else if (Files.isRegularFile(path)) {
                    // 复制目录
                    Files.copy(path, Paths.get(targetName));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
