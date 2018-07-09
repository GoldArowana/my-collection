package com.king.learn.collection.myconcurrent.c;//import edu.vt.ece.bench.Blocking_Priority_Queue;

import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.Blocking_Priority_Queue_TryLock;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.Counter;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.SharedCounter;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.locks.Lock;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.locks.PriorityCLH;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.locks.PriorityCLH_Try;

public class Try_Lock {

    private static final int THREAD_COUNT = 8;

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String lockClass = "PriorityCLH_Try";
        PriorityCLH.THREAD_COUNT = THREAD_COUNT;
        final Counter counter = new SharedCounter(0, (Lock) Class.forName("edu.vt.ece.locks." + lockClass).newInstance());
        for (int t = 0; t < THREAD_COUNT; t++) {
            new Blocking_Priority_Queue_TryLock(counter).start();
        }

        System.out.println("Threads failed " + PriorityCLH_Try.count + " before entering CS");
    }
}


