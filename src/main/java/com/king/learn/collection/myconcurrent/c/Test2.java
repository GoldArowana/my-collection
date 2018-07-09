package com.king.learn.collection.myconcurrent.c;

//import edu.vt.ece.bench.Blocking_Priority_Queue_TryLock;

import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.Blocking_Priority_Queue;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.Counter;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.SharedCounter;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.locks.Lock;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.locks.PriorityCLH;

public class Test2 {
    //private static final int TOTAL_ITERS =32000;
    private static final int THREAD_COUNT = 8;

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        //private static final int TOTAL_ITERS =32000;
        String lockClass = "PriorityCLH";
        PriorityCLH.THREAD_COUNT = THREAD_COUNT;
        final Counter counter = new SharedCounter(0, (Lock) Class.forName("edu.vt.ece.locks." + lockClass).newInstance());
        for (int t = 0; t < THREAD_COUNT; t++) {
            new Blocking_Priority_Queue(counter).start();
        }
//		final TestThread2[] threads = new TestThread2[THREAD_COUNT];
//
//		for(int t=0; t<THREAD_COUNT; t++) {
//			threads[t] = new TestThread2(counter, ITERS);
//		}
//
//		for(int t=0; t<THREAD_COUNT; t++) {
//			threads[t].start();
//		}
//
//		long totalTime = 0;
//		for(int t=0; t<THREAD_COUNT; t++) {
//			threads[t].join();
//			totalTime += threads[t].getElapsedTime();
//		}
//
//		System.out.println("Average time per thread is " + totalTime/THREAD_COUNT + "ms");
        //long start_time = System.currentTimeMillis();

//		
    }
}

