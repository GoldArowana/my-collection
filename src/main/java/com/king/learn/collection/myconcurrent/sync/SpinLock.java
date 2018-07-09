package com.king.learn.collection.myconcurrent.sync;

import java.util.concurrent.atomic.AtomicInteger;

public class SpinLock {
    private AtomicInteger owner = new AtomicInteger(-1);

    public void lock(int tid) {
        while (!owner.compareAndSet(-1, tid)) {
        }
    }

    public void unlock() {
        owner.set(-1);
    }
}
