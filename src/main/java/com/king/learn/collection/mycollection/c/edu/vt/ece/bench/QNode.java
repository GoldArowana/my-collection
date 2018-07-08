package com.king.learn.collection.mycollection.c.edu.vt.ece.bench;

public class QNode implements Comparable<QNode> {
    public volatile int priority;
    public volatile int my_count;

    public int getPriority() {
        return this.priority;
    }

    public int getmy_count() {
        return this.my_count;
    }

    public boolean equals(QNode other) {
        return this.getPriority() == other.getPriority();
    }

    public int compareTo(QNode other) {
        if (getPriority() == (other.getPriority()))
            return -1;
        else if (getPriority() >= other.getPriority()) {
            return 1;
        } else {
            return -1;
        }
    }
}
