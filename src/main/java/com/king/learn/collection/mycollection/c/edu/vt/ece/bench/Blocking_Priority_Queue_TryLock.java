package com.king.learn.collection.mycollection.c.edu.vt.ece.bench;


import com.king.learn.collection.mycollection.c.edu.vt.ece.locks.PriorityCLH_Try;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

//import edu.vt.ece.locks.CLH.QNode;

public class Blocking_Priority_Queue_TryLock extends Thread {
    private static int ID_GEN = 0;
    int random_number;
    Random rand = new Random();
    int MAX_COUNT = 2000;
    private int id;
    private Counter counter;

    //PriorityCLH_Try p;
    public Blocking_Priority_Queue_TryLock(Counter counter) {
        id = ID_GEN++;
        this.counter = counter;
    }

    public void run() {
//		Priority_Compare p = new Priority_Compare(getThreadid());
//		QNode each_thread = new QNode(getThreadid());
//		queue.add(each_thread);
        Thread.currentThread().setPriority(ThreadLocalRandom.current().nextInt(1, 6));
        for (int i = 0; i < MAX_COUNT; i++) {
            counter.getAndIncrement();
        }
        //	System.out.println("Thread "+id+" "+ "with priority" +" "+ currentThread().getPriority() + " DONE.. <Counter:" + counter + ">");
        System.out.println("Threads failed " + PriorityCLH_Try.count + " before entering CS");
    }

    public int getThreadid() {
        return id;
    }
}
