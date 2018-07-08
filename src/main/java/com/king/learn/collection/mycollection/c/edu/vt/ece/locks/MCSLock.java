package com.king.learn.collection.mycollection.c.edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicReference;

public class MCSLock implements Lock {

    AtomicReference<QNode> tail;
    ThreadLocal<QNode> myNode;

    public MCSLock() {
        tail = new AtomicReference<QNode>(null);
        myNode = new ThreadLocal<QNode>() {
            protected QNode initialValue() {
                return new QNode();
            }
        };

    }

    @Override
    public void lock() {
        // TODO Auto-generated method stub
        QNode qnode = myNode.get();
        QNode pred = tail.getAndSet(qnode);
        if (pred != null) {
            qnode.locked = true;
            pred.next = qnode;
            while (qnode.locked) {
            }
        }

    }

    @Override
    public void unlock() {
        // TODO Auto-generated method stub
        QNode qnode = myNode.get();
        if (qnode.next == null) {
            if (tail.compareAndSet(qnode, null)) {
                return;
            }
            while (qnode.next == null) {
            }
        }
        qnode.next.locked = false;
        qnode.next = null;

    }

}
