package com.king.learn.concurrent.jdk.locks;

import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

public class ReentrantReadWriteLockTest {
    private static MyReentrantReadWriteLock lock = new MyReentrantReadWriteLock();
    private static Lock writeLock = lock.writeLock();
    private static Lock readLock = lock.readLock();

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readLock.lock();
            System.out.println("r1");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readLock.unlock();
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readLock.lock();
            System.out.println("r2");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readLock.unlock();
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            writeLock.lock();
            System.out.println("w1");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            writeLock.unlock();
        }).start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        writeLock.lock();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        writeLock.unlock();
    }

    @Test
    public void t() {
        new Thread(() -> {
            readLock.lock();

        }).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        readLock.lock();

    }

    @Test
    public void t2() {
        new Thread(() -> {
            LockSupport.park();
            System.out.println("123");
        }).start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
