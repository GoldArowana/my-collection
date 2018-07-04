package com.king.learn.concurrent.extention.sync;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class AtomicTable extends Table {
    AtomicIntegerArray values;

    public AtomicTable(int size) {
        values = new AtomicIntegerArray(size);
    }

    @Override
    public int size() {
        return values.length();
    }

    @Override
    public void incrementEntry(int entryId, int inc) {
        int index = entryId % values.length();
        values.getAndAccumulate(index, inc, (prev, update) -> prev + update);
    }

    @Override
    public int getData(int i) {
        return values.get(i);
    }

    @Override
    public void printTablename() {
        System.out.println("AtomicTable");
    }
}
