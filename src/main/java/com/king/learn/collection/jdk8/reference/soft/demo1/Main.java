package com.king.learn.collection.jdk8.reference.soft.demo1;

/**
 * -Xms2m -Xmx2m
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        SoftCache cache = new SoftCache();

        for (int i = 0; ; i++) {
            cache.put(i, i);
            Thread.sleep(10);
        }
    }
}
