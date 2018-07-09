package com.king.learn.collection.myconcurrent.c.edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicReference;

public class CLH implements Lock {

    private AtomicReference<QNode> tail;
    private ThreadLocal<QNode> myPred;
    private ThreadLocal<QNode> myNode;

    public CLH() {
        tail = new AtomicReference<QNode>();
        myPred = new ThreadLocal<QNode>() {
            protected QNode initialValue() {
                return new QNode();
            }
        };

        myNode = new ThreadLocal<QNode>() {
            protected QNode initialValue() {
                return null;
            }
        };

    }

    @Override
    public void lock() {
        QNode qnode = myNode.get();
        qnode.locked = true;

        QNode pred = tail.getAndSet(qnode);
        myPred.set(pred);
        while (pred.locked) {
        }
    }

    @Override
    public void unlock() {
        QNode qnode = myNode.get();
        qnode.locked = false;
        myNode.set(myPred.get());
    }

}