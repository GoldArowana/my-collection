package com.king.learn.collection.jdk8.reference.soft.demo2;

/**
 * -Xms2m -Xmx2m
 */
public class Main2 {
    public static void main(String[] args) throws InterruptedException {
        SoftCache2 cache2 = new SoftCache2();
        for (; ; ) {
            cache2.put(new Object());
            Thread.sleep(10);
        }
    }
}
