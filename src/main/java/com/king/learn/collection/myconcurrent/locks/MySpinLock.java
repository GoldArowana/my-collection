package com.king.learn.collection.myconcurrent.locks;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 从这里copy而来: https://coderbee.net/index.php/concurrent/20131115/577
 * 自旋锁
 * <p>
 * 一直循环检测锁是否被释放, 直到自己占用了这个锁，而不是进入线程挂起或睡眠状态。
 * <p>
 * 临界区很小的话，锁占用的时间就很短, 这时自旋就比线程挂起唤醒的性能要好
 * <p>
 * 缺点:
 * 1.CAS操作需要硬件的配合；
 * 2.保证各个CPU的缓存（L1、L2、L3、跨CPU Socket、主存）的数据一致性，通讯开销很大，在多处理器系统上更严重；
 * 3.没法保证公平性，不保证等待进程/线程按照FIFO顺序获得锁。
 */
public class MySpinLock {
    private AtomicReference<Thread> owner = new AtomicReference<Thread>();

    public void lock() {
        Thread currentThread = Thread.currentThread();

        // 如果锁未被占用，则设置当前线程为锁的拥有者
        while (!owner.compareAndSet(null, currentThread)) {
        }
    }

    public void unlock() {
        Thread currentThread = Thread.currentThread();

        // 只有锁的拥有者才能释放锁
        owner.compareAndSet(currentThread, null);
    }
}
