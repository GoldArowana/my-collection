package com.king.learn.collection.mycollection.c.edu.vt.ece.bench;


import com.king.learn.collection.mycollection.c.edu.vt.ece.locks.Lock;

/**
 * @author Mohamed M. Saad
 */
public class SharedCounter1 extends Counter1 {
    static int count;
    private Lock lock;
    //public int MAX_COUNT=1000;

    public SharedCounter1(int c, Lock lock) {
        super(c); // parent counter constructor run
        this.lock = lock;
    }

    @Override
    public int getAndIncrement() {
        //long time = System.nanoTime();
        lock.lock();
        int temp = -1;
        try {
            //System.out.println("Thread id"+" " +((TestThread)Thread.currentThread()).getThreadId()+" "+ "entered Critcal Section");
            //for(int i=0; i<MAX_COUNT;i++){
            //System.out.println((System.nanoTime() - time));
            temp = super.getAndIncrement();
            //}
            //System.out.println(temp);
            //System.out.println("Thread id"+" " +((TestThread)Thread.currentThread()).getThreadId()+" "+ "entered Critcal Section");

        } finally {
            lock.unlock();
        }
        return temp;
    }

}