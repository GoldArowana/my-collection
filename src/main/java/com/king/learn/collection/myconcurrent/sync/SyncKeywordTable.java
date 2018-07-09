package com.king.learn.collection.myconcurrent.sync;

public class SyncKeywordTable extends Table {
    int values[];
//    volatile int testValue = 0;


    SyncKeywordTable(int size) {
        values = new int[size];
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void incrementEntry(int entryId, int inc) {
        int index = entryId % values.length;
        synchronized (this) {
            values[index] += inc;
        }
//        testValue += 2;
    }

    @Override
    public int getData(int i) {
        return values[i];
    }

    @Override
    public void printTablename() {

    }
}
