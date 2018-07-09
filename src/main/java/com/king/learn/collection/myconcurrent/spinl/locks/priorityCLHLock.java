package com.king.learn.collection.myconcurrent.spinl.locks;

import com.king.learn.collection.myconcurrent.spinl.bench.Qnode2;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class priorityCLHLock implements Lock {
    private final ThreadLocal<Qnode2> mynode;
    public AtomicBoolean locked;
    PriorityBlockingQueue<Qnode2> priorityQueue = new PriorityBlockingQueue<Qnode2>();

    public priorityCLHLock() {
        locked = new AtomicBoolean();
        this.mynode = new ThreadLocal<Qnode2>() {
            protected Qnode2 initialValue() {
                return new Qnode2(5);
            }
        };
    }

    public void lock() {
        final Qnode2 qnode = this.mynode.get();
        qnode.priority = Thread.currentThread().getPriority();
        priorityQueue.add(qnode);
//		while (priorityQueue.peek() != qnode || !locked.compareAndSet(false, true));
        while (true) {
            while (priorityQueue.peek() != qnode) ;
            while (!locked.compareAndSet(false, true)) ;
            if (priorityQueue.peek() == qnode)
                break;
            locked.set(false);
        }
    }

    public void unlock() {
        final Qnode2 qnode = this.mynode.get();
        priorityQueue.remove(qnode);
        locked.set(false);
    }
}

