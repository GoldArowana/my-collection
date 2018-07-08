package com.king.learn.collection.mycollection.c.edu.vt.ece.bench;

import com.king.learn.collection.mycollection.c.edu.vt.ece.locks.Lock;

public class barrier1 {

    static int count;
    private Lock lock;

    public barrier1(int count, Lock lock) {
        this.lock = lock;
        this.count = count;
    }

    public void TTASbarrier() {
        lock.lock();
        try {
            count = count + 1;

        } finally {
            lock.unlock();
        }
    }
}
