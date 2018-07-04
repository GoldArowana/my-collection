package com.king.learn.concurrent.extention.sync;

public class SpinLockTable extends Table {
    int[] values;
    SpinLock lock;

    public SpinLockTable(int size) {
        values = new int[size];
        lock = new SpinLock();
    }

    @Override
    public int size() {
        return values.length;
    }

    public void incrementEntry(int entryId, int inc) {
        int index = entryId % values.length;
        lock.lock(entryId);
        try {
            values[index] += inc;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getData(int index) {
        return values[index];
    }

    @Override
    public void printTablename() {
        System.out.println("SPIN_LOCK_TABLE");
    }
}
