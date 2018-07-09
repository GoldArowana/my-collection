package com.king.learn.collection.myconcurrent.spinl.barrier;

import com.king.learn.collection.myconcurrent.spinl.locks.Lock;

public class barriers {
    public static volatile int count;
    public static int THREAD_COUNT;
    public volatile int[] barrier2 = new int[THREAD_COUNT];
    private Lock lock;

    @SuppressWarnings("static-access")
    public barriers(int count, Lock lock) {
        this.lock = lock;
        this.count = count;
    }

    public void barrier() {
        lock.lock();
        count = count + 1;
        lock.unlock();
    }
}