package com.king.learn.concurrent.extention.c.edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicBoolean;

public class TAS implements Lock {
    AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {
        while (state.getAndSet(true)) {
        }
    }

    public void unlock() {
        state.set(false);
    }
}
