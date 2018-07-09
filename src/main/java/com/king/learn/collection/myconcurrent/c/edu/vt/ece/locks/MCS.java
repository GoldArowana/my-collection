package com.king.learn.collection.myconcurrent.c.edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicReference;

public class MCS implements Lock {
    AtomicReference<QNode> queue;
    ThreadLocal<QNode> myNode;

    public MCS() {
        queue = new AtomicReference<QNode>(null);
        // initialize thread-local variable
        myNode = new ThreadLocal<QNode>() {
            protected QNode initialValue() {
                return new QNode();
            }
        };
    }

    public void lock() {
        QNode qnode = myNode.get();
        QNode pred = queue.getAndSet(qnode);
        if (pred != null) {
            qnode.locked = true;
            pred.next = qnode;
            while (qnode.locked) ;   // spin
        }
    }

    public void unlock() {
        QNode qnode = myNode.get();
        QNode pred = queue.getAndSet(qnode);
        if (pred != null) {
            qnode.locked = true;
            while (qnode.locked) {
            } // spin
        }
        qnode.next.locked = false;
        qnode.next = null;
    }


    static class QNode {
        volatile QNode next = null;
        volatile boolean locked = false;

        QNode() {
            next = null;
            locked = false;
        }// Queue node inner class

    }
}

