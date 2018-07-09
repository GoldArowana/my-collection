package com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

//import edu.vt.ece.locks.CLH.QNode;

public class Blocking_Priority_Queue extends Thread {
    private static int ID_GEN = 0;
    int random_number;
    Random rand = new Random();
    int MAX_COUNT = 30000;
    private int id;
    private Counter counter;

    public Blocking_Priority_Queue(Counter counter) {
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
        System.out.println("Thread " + id + " " + "with priority" + " " + currentThread().getPriority() + " DONE.. <Counter:" + counter + ">");
        //System.out.println("Thread with priority" + currentThread().getPriority()+ "failed "+PriorityCLH_Try.count+ " before entering CS");
    }

    public int getThreadid() {
        return id;
    }
}
