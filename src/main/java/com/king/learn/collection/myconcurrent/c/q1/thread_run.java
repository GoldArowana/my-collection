package com.king.learn.collection.myconcurrent.c.q1;


import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.ThreadId;

public class thread_run extends Thread implements ThreadId {
    public static int THREAD_COUNT = Q1_Test.THREAD_COUNT;
    private static int ID_GEN = 0;
    private barrier b;
    private int id;
    private long start_time;
    private long end_time;

    public thread_run(barrier b) {
        id = ID_GEN++;
        this.b = b;
    }

    @Override
    public void run() {
        foo();
        //start_time = System.currentTimeMillis();
        b.barrier();
        while (barrier.count < THREAD_COUNT) ;

//////////////////Barrier 2 Starts Here//////////////////////////////	
//	if(id ==0){
//		b.array_barrier2[id] =1;
//	}
//	else{
//			while(b.array_barrier2[id-1] ==0);
//			b.array_barrier2[id] =1;
//	}
//	if( id == THREAD_COUNT -1 && b.array_barrier2[THREAD_COUNT-1] ==1)
//		b.array_barrier2[THREAD_COUNT -1]=2;
//	while(b.array_barrier2[THREAD_COUNT -1]!=2);
        ///////////////////////////bARRIER 2 ENDES HERE//////////////////////////
        bar();

        end_time = ((System.currentTimeMillis() - start_time));
    }

    public void foo() {
        System.out.println(id + " Entered foo");
    }

    public void bar() {
        System.out.println(id + " Entered Bar");
    }

    public int getThreadId() {
        return id;
    }

    public long getelapsedTime() {
        return end_time;
    }
}

