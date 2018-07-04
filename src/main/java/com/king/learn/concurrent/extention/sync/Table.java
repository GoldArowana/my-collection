package com.king.learn.concurrent.extention.sync;

public abstract class Table {
    public abstract int size();

    public abstract void incrementEntry(int entryId, int inc);

    public abstract int getData(int i);

    public abstract void printTablename();

}
