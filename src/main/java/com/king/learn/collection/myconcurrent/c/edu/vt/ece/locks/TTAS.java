package com.king.learn.collection.myconcurrent.c.edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicBoolean;

public class TTAS implements Lock {
    AtomicBoolean state = new AtomicBoolean(false);

    public void lock() {
        while (true) {
            while (state.get()) {
            }
            ;
            if (!state.getAndSet(true))
                return;
        }
    }

    public void unlock() {
        state.set(false);
    }
}
