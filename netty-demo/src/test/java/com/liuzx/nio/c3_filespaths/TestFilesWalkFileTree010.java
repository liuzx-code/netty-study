package com.liuzx.nio.c3_filespaths;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class TestFilesWalkFileTree010 {
    public static void main(String[] args) throws IOException {
        try {
            getFilesByPath();
            getJarFilesByPath();
            deleteAllFilesByPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteAllFilesByPath() throws IOException {
        // 删除指定文件夹下所有的文件
        Files.walkFileTree(Paths.get("/Applications/Snipaste.app/Contents"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("进入文件夹前："+dir);
                return super.preVisitDirectory(dir, attrs);
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                Files.delete(file); // 删除文件
                System.out.println("进入文件夹："+file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                Files.delete(dir); // 删除目录
                System.out.println("进入文件夹后："+dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    private static void getJarFilesByPath() throws IOException {
        // 查看指定文件夹下有多少jar包
        AtomicInteger jarCount = new AtomicInteger();
        Files.walkFileTree(Paths.get("/Library/Java/JavaVirtualMachines"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".jar")) {
                    jarCount.incrementAndGet();
                    System.out.println(file);
                }
                return super.visitFile(file, attrs);
            }
        });
        System.out.println("jarCount: " + jarCount);
    }

    private static void getFilesByPath() throws IOException {
        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(Paths.get("/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("===> preVisitDirectory: " + dir);
                dirCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("===> visitFile: " + file);
                fileCount.incrementAndGet();
                return super.visitFile(file, attrs);
            }
        });
        System.out.println("dirCount: " + dirCount);
        System.out.println("fileCount: " + fileCount);
    }
}
