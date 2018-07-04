package com.king.learn.concurrent.extention.c.q1;


import com.king.learn.concurrent.extention.c.edu.vt.ece.locks.Lock;

public class barrier {
    public static volatile int count;
    public static int THREAD_COUNT = Q1_Test.THREAD_COUNT;
    public volatile int[] array_barrier2 = new int[THREAD_COUNT];
    private Lock lock;

    public barrier(int count, Lock lock) {
        this.lock = lock;
        this.count = count;
    }

    public void barrier() {
        lock.lock();
        count = count + 1;
        lock.unlock();
    }
}
