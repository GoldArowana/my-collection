package com.king.learn.collection.myconcurrent.c;

import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.Foo_Bar_SeperateBarriers;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench.barrier1;
import com.king.learn.collection.myconcurrent.c.edu.vt.ece.locks.Lock;

public class Q1 {

    private static final int THREAD_COUNT = 8;

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String lockClass = "TTAS";
        Foo_Bar_SeperateBarriers.THREAD_COUNT = THREAD_COUNT;
        //PriorityCLH.THREAD_COUNT = THREAD_COUNT;
        final barrier1 a = new barrier1(0, (Lock) Class.forName("edu.vt.ece.locks." + lockClass).newInstance());
        for (int t = 0; t < THREAD_COUNT; t++) {
            new Foo_Bar_SeperateBarriers(a).start();
        }
        //
    }
}

