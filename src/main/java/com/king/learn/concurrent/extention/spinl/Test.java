package com.king.learn.concurrent.extention.spinl;

import com.king.learn.concurrent.extention.spinl.bench.TestThread;
import com.king.learn.concurrent.extention.spinl.bench.criticalSection;
import com.king.learn.concurrent.extention.spinl.locks.ALock;
import com.king.learn.concurrent.extention.spinl.locks.Lock;

public class Test {

    private static final String TASLock = "CLHLock";
    public static int THREAD_COUNT;

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (args.length >= 2)
            THREAD_COUNT = Integer.parseInt(args[1]);
        else
            THREAD_COUNT = 8;
        ALock.capacity = THREAD_COUNT;
        String lockClass = (args.length == 0 ? TASLock : args[0]);
        final criticalSection CS = new criticalSection(0, (Lock) Class.forName("locks." + lockClass).newInstance());
        for (int t = 0; t < THREAD_COUNT; t++)
            new TestThread(CS).start();
    }
}
