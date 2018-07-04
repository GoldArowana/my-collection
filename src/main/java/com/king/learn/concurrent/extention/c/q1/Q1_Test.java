package com.king.learn.concurrent.extention.c.q1;


import com.king.learn.concurrent.extention.c.edu.vt.ece.locks.Lock;

public class Q1_Test {
    private static final String TTAS = "TTAS";
    public static int THREAD_COUNT;

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        THREAD_COUNT = 8;
        final barrier b = new barrier(0, (Lock) Class.forName("edu.vt.ece.locks." + TTAS).newInstance());
        for (int t = 0; t < THREAD_COUNT; t++)
            new thread_run(b).start();

    }


}
 