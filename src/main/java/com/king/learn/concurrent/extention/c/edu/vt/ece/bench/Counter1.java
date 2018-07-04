package com.king.learn.concurrent.extention.c.edu.vt.ece.bench;

/**
 * @author Mohamed M. Saad
 */
public class Counter1 {
    public int value;

    public Counter1(int c) {
        value = c;
    }

    public int getAndIncrement() {
        int temp = value;
        value = temp + 1;
        return temp;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}