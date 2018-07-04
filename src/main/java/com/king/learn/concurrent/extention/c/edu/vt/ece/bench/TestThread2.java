package com.king.learn.concurrent.extention.c.edu.vt.ece.bench;

/**
 * @author Balaji Arun
 */
public class TestThread2 extends Thread implements ThreadId {
    private static int ID_GEN = 0;

    private Counter counter;
    private int id;
    private long elapsed;
    private int iter;

    public TestThread2(Counter counter, int iter) {
        id = ID_GEN++;
        this.counter = counter;
        this.iter = iter;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        for (int i = 0; i < iter; i++)
            counter.getAndIncrement();
        System.out.println("Thread " + id + " DONE.. <Counter:" + counter + ">");

        long end = System.currentTimeMillis();
        elapsed = end - start;
    }

    public int getThreadId() {
        return id;
    }

    public long getElapsedTime() {
        return elapsed;
    }
}
