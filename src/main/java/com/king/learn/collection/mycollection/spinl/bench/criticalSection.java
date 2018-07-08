package com.king.learn.collection.mycollection.spinl.bench;


import com.king.learn.collection.mycollection.spinl.locks.Lock;

public class criticalSection {
    public static int count;
    private Lock lock;

    public criticalSection(int count, Lock lock) {
        this.lock = lock;
    }

    public void emptyCS() {
        lock.lock();
        count = count + 1;            // This line was uncommented while checking for mutual exclusion
        lock.unlock();
    }
}