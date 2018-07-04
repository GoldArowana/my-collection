package com.king.learn.concurrent.extention.c.edu.vt.ece.bench;


import com.king.learn.concurrent.extention.c.edu.vt.ece.locks.Lock;

/**
 * @author Mohamed M. Saad
 */
public class SharedCounter extends Counter {
    static int count;
    public int MAX_COUNT = 1000;
    private Lock lock;

    public SharedCounter(int c, Lock lock) {
        super(c); // parent counter constructor run
        this.lock = lock;
    }

    @Override
    public int getAndIncrement() {
        //	long time = System.currentTimeMillis();
        long time = System.nanoTime();
        lock.lock();
        int temp = -1;
        try {
            //for(int i=0; i<MAX_COUNT;i++){
            temp = super.getAndIncrement();
            //System.out.println((System.nanoTime() - time));
            //}
            //System.out.println(temp);
            //System.out.println("Thread id"+" " +((TestThread)Thread.currentThread()).getThreadId()+" "+ "entered Critcal Section");

        } finally {
            lock.unlock();
        }
        return temp;
    }

}