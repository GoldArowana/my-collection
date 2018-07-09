package com.king.learn.collection.myconcurrent.c;

import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.Counter1;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.SharedCounter1;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.TestThread;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.locks.Lock;

/**
 * @author Mohamed M. Saad
 */
public class Test {

    private static final int THREAD_COUNT = 4;
    private static final String TAS = "TAS";
    private static final String TTAS = "TTAS";
    private static final String CLH = "CLH";
    private static final String MCS = "MCS";

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String lockClass = (args[0]);
        //TAS.THREAD_COUNT = THREAD_COUNT;
        //TTAS.THREAD_COUNT = THREAD_COUNT;
        //MCS.THREAD_COUNT = THREAD_COUNT;
        //CLH.THREAD_COUNT = THREAD_COUNT;
        final Counter1 counter = new SharedCounter1(0, (Lock) Class.forName("edu.vt.ece.locks." + lockClass).newInstance());
        for (int t = 0; t < THREAD_COUNT; t++) {            //typecast lock
            new TestThread(counter).start();
            //System.out.println("Thread id"+ ((TestThread)Thread.currentThread()).getThreadId()+ "generated");
        }
        //System.out.println("Average time per thread is " + totalTime/THREAD_COUNT + "ms");
    }
}

