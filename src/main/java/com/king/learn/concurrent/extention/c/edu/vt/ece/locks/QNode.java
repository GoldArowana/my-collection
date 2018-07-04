package com.king.learn.concurrent.extention.c.edu.vt.ece.locks;


public class QNode {
    public volatile int priority;
    volatile boolean locked = false;
    volatile QNode next = null;

}