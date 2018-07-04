package com.king.learn.concurrent.extention.spinl.locks;

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