package com.king.learn.collection.jdk8.reference.soft.demo2;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Vector;

public class SoftCache2 {
    Vector vector = null;
    Thread remover;
    ReferenceQueue clearedRefs;

    public SoftCache2() {
        vector = new Vector();
        clearedRefs = new ReferenceQueue();
        // start thread to delete cleared references from the cache
        remover = new Remover(clearedRefs, vector);
        remover.start();
    }

    public void put(Object o) {
        synchronized (vector) {
            vector.addElement(new SoftReference(o, clearedRefs));
        }
    }

    public Object get() {
        synchronized (vector) {
            if (vector.size() > 0) {
                SoftReference sr = (SoftReference) vector.elementAt(0);
                vector.remove(0);
                return sr.get();
            }
        }
        return null;
    }

    private class Remover extends Thread {
        ReferenceQueue refQ;
        Vector cache;

        public Remover(ReferenceQueue rq, Vector v) {
            super();
            refQ = rq;
            cache = v;
            setDaemon(true);
        }

        public void run() {
            try {
                while (true) {
                    Object o = refQ.remove();
                    synchronized (cache) {
                        cache.removeElement(o);
                        System.out.println("Removing " + o);
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    }
} 