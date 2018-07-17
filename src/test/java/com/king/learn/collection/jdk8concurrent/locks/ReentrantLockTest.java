package com.king.learn.collection.jdk8concurrent.locks;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class ReentrantLockTest {
    private static final int RANK = 1000;
    private static MyReentrantLock lock = new MyReentrantLock(true);
    private static int num = 0;
    private CountDownLatch counter = new CountDownLatch(RANK);

    public static void quietSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MyReentrantLock lock = new MyReentrantLock(true); //ReentrantLock lock = new ReentrantLock();
        new Thread(lock::lock).start();
        new Thread(lock::lock).start();
    }

    @Test
    public void withLock() {
        for (int i = 0; i < RANK; i++) {
            new Thread(() -> {
                lock.lock();
                try {
                    quietSleep(10);
                    num += 1;
                } finally {
                    lock.unlock();
                    counter.countDown();
                }
            }).start();
        }

        try {
            counter.await();
            System.out.println(num);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void withoutLock() {
        for (int i = 0; i < RANK; i++) {
            new Thread(() -> {
//                lock.lock();
                try {
                    quietSleep(10);
                    num += 1;
                } finally {
//                    lock.unlock();
                    counter.countDown();
                }
            }).start();
        }

        try {
            counter.await();
            System.out.println(num);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
