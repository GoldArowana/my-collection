package com.king.learn.collection;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * VM Options: -Xms6m -Xmx6m -XX:MaxDirectMemorySize=10m
 */
public class DirectMemoryOOM {
    private static final int _1MB = 1024 * 1014;

    public static void main(String[] args) throws IllegalAccessException, InterruptedException {
        Field unsafeFile = Unsafe.class.getDeclaredFields()[0];
        unsafeFile.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeFile.get(null);
        while (true) {
            unsafe.allocateMemory(_1MB);
            Thread.sleep(10);
        }
    }
}
