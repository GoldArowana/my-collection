package com.king.learn.concurrent.extention.c;

//import edu.vt.ece.locks.BinaryTreePetersonLock;

import com.king.learn.concurrent.extention.c.edu.vt.ece.bench.Counter;
import com.king.learn.concurrent.extention.c.edu.vt.ece.bench.SharedCounter;
import com.king.learn.concurrent.extention.c.edu.vt.ece.bench.TestThread2;
import com.king.learn.concurrent.extention.c.edu.vt.ece.locks.Lock;

/**
 * @author Balaji Arun
 */
public class Average_Calculate {

    private static final int TOTAL_ITERS = 20000000;
    private static final String LOCK_ONE = "LockOne";
    private static final String LOCK_TWO = "LockTwo";
    private static final String PETERSON = "Peterson";
    private static final String FILTER = "Filter";
    private static int THREAD_COUNT;
    private static int ITERS;

    //private static final String LExclusion = "L_Exclusion";
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, InterruptedException {
        if (args.length > 1) {
            if (!(args[1].equals(0))) {
                int thread_user = Integer.parseInt(args[1]);
                System.out.println(thread_user);
                THREAD_COUNT = thread_user;
                ITERS = TOTAL_ITERS / THREAD_COUNT;
            }
        } else {
            THREAD_COUNT = 8;
            ITERS = TOTAL_ITERS / THREAD_COUNT;
        }
//	    BinaryTreePetersonLock.number_of_threads = THREAD_COUNT;
//		//L_Exclusion.THREAD_COUNT = THREAD_COUNT;
//		Filter.THREAD_COUNT = THREAD_COUNT;
//		Peterson.THREAD_COUNT = THREAD_COUNT;
//		LExclusion.THREAD_COUNT = THREAD_COUNT;
//		LExclusion.THREAD_ALLOWED = THREAD_COUNT/2;
        String lockClass = (args.length == 0 ? PETERSON : args[0]);
        final Counter counter;
        //if(lockClass == "BinaryTreePeterson"){
        //	counter = new SharedCounter(0, (Lock)Class.forName("edu.vt.ece.util." + lockClass).newInstance());
        //}
        //else{
        counter = new SharedCounter(0, (Lock) Class.forName("edu.vt.ece.locks." + lockClass).newInstance());
        //}

        final TestThread2[] threads = new TestThread2[THREAD_COUNT];

        for (int t = 0; t < THREAD_COUNT; t++) {
            threads[t] = new TestThread2(counter, ITERS);
        }

        for (int t = 0; t < THREAD_COUNT; t++) {
            threads[t].start();
        }

        long totalTime = 0;
        for (int t = 0; t < THREAD_COUNT; t++) {
            threads[t].join();
            totalTime += threads[t].getElapsedTime();
        }

        System.out.println("Average time per thread is " + (totalTime / THREAD_COUNT) + "ms");

    }
}
