package com.king.learn.collection.myconcurrent.spin;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SpinLock {

    static AtomicBoolean lock;
    private final AtomicInteger mutex = new AtomicInteger(-1);
    String word = null;

    SpinLock() {
    }

    SpinLock(String str) {
        word = str;
    }

    public void lock() {
        int i = 0;
        while (!tryLock()) {
            if (i++ == 0)
                System.out.println("current Thread = " + Thread.currentThread().getId() + ": Locked by " + mutex.get());
        }
        System.out.println("current Thread = " + Thread.currentThread().getId() + ": New lock by " + mutex.get());
    }

    public boolean tryLock() {

        long id = Thread.currentThread().getId();
        return mutex.compareAndSet(-1, (int) id);
    }

    public void release() {

        if (mutex.get() == Thread.currentThread().getId()) {
            System.out.println("current Thread = " + Thread.currentThread().getId() + ": Release by " + mutex.get());
            mutex.set(-1);
        }
    }

    public boolean hasLock() {
        return mutex.get() == Thread.currentThread().getId();
    }


}
