package com.king.learn.collection.mycollection.sync;

import java.util.concurrent.locks.ReentrantLock;

public class MutexTable extends Table {
    int[] values;
    ReentrantLock lock;

    public MutexTable(int size) {
        values = new int[size];
        lock = new ReentrantLock(); // check fairness
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public void incrementEntry(int entryId, int inc) {
        int index = entryId % values.length;
        lock.lock();
        try {
            values[index] += inc;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getData(int i) {
        return values[i];
    }

    @Override
    public void printTablename() {
        System.out.println("MUTEX TABLE");
    }
}
