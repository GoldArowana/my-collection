package com.king.learn.collection.myconcurrent.spinl.locks;

import java.util.concurrent.atomic.AtomicBoolean;

public class TASLock implements Lock {
    AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {
        while (state.getAndSet(true)) {
        }
    }

    public void unlock() {
        state.set(false);
    }
}