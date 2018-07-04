package com.king.learn.concurrent.extention.c.edu.vt.ece.bench;


//import java.lang.*;

/**
 * @author Mohamed M. Saad
 */

public class TestThread extends Thread implements ThreadId {
    private static final int MAX_COUNT = 250;
    private static int ID_GEN = 0;
    private long time_start = System.nanoTime();
    //private long total_time =0;
    private Counter1 counter;
    private int id;

    //int TOTAL_THREAD;
    public TestThread(Counter1 counter) {
        id = ID_GEN++;
        this.counter = counter;
        //TOTAL_THREAD = TOT_THREADS;
        //System.out.println("Thread id"+ ((TestThread)Thread.currentThread()).getThreadId()+ "generated");
    }

    @Override
    public void run() {
        long time = System.nanoTime();
        for (int i = 0; i < MAX_COUNT; i++) {
            counter.getAndIncrement();
        }
        //	System.out.println("Thread "+id+ " "+"waited " + (System.nanoTime() - time)+  " to enter critical section" );

//		while(System.nanoTime() - time_start <= 2000000000){
//			counter.getAndIncrement();
//		}
//		System.out.println(counter);

        //System.out.println("Thread " + id + " DONE.. <Counter:" + counter + ">");
        //System.out.println(counter.getClass().getName());
        //if(counter.value ==  ){
        //total_time = System.nanoTime() - time_start;

        //System.out.println("Total Execution time is"+ " "+ (System.currentTimeMillis()- time_start));
        //}
    }

    public int getThreadId() {
        return id;
    }
}
