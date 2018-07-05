package com.king.learn.concurrent.jdk.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * 锁的统一抽象定义
 */
public interface MyLock {

    void lock();

    boolean tryLock();

    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;

    void unlock();

    Condition newCondition();
}
