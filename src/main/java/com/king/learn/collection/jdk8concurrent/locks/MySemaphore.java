package com.king.learn.collection.jdk8concurrent.locks;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author Doug Lea
 * @since 1.5
 */
public class MySemaphore {
    private final Sync sync;

    /**
     * 默认是非公平的
     */
    public MySemaphore(int permits) {
        sync = new NonfairSync(permits);
    }

    /**
     * true: 公平
     * false: 非公平
     */
    public MySemaphore(int permits, boolean fair) {
        sync = fair ? new FairSync(permits) : new NonfairSync(permits);
    }

    /**
     * 获取锁
     */
    public void acquire() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 释放锁
     */
    public void release() {
        sync.releaseShared(1);
    }

    /**
     * 获取锁, 一次性获取permits个锁
     */
    public void acquire(int permits) throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireSharedInterruptibly(permits);
    }

    /**
     * 尝试着获取锁
     */
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
            throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.tryAcquireSharedNanos(permits, unit.toNanos(timeout));
    }

    /**
     * 释放permits个锁
     */
    public void release(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.releaseShared(permits);
    }

    /**
     * @return 获取计数器的值
     */
    public int availablePermits() {
        return sync.getPermits();
    }

    /**
     * 一次性获取剩下的所有锁
     *
     * @return 获取的锁的个数
     */
    public int drainPermits() {
        return sync.drainPermits();
    }

    /**
     * Shrinks the number of available permits by the indicated
     * reduction. This method can be useful in subclasses that use
     * semaphores to track resources that become unavailable. This
     * method differs from {@code acquire} in that it does not block
     * waiting for permits to become available.
     *
     * @param reduction the number of permits to remove
     * @throws IllegalArgumentException if {@code reduction} is negative
     */
    protected void reducePermits(int reduction) {
        if (reduction < 0) throw new IllegalArgumentException();
        sync.reducePermits(reduction);
    }

    /**
     * @return 是否是公平锁
     */
    public boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * @return 是否有排队的线程
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * @return 排队的线程的个数
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * @return 所有排队的线程
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    public String toString() {
        return super.toString() + "[Permits = " + sync.getPermits() + "]";
    }

    /**
     * 锁的同一实现
     */
    abstract static class Sync extends MyAbstractQueuedSynchronizer {
        /**
         * 计数器
         */
        Sync(int permits) {
            setState(permits);
        }

        /**
         * 就是获取计数器
         */
        final int getPermits() {
            return getState();
        }

        /**
         * 如果锁已经消费完, 返回值就会<0
         * <p>
         * 非公平就在于, 不会先去判断队列里是否有其他锁在排队, 自己来了先试着争抢
         * 没抢过, 再排队.
         */
        final int nonfairTryAcquireShared(int acquires) {
            for (; ; ) {
                int available = getState();
                int remaining = available - acquires;
                // 如果剩余的<0, 说明早就消费完了, 直接返回这个小于0的值
                if (remaining < 0 ||
                        // 如果大于等于0, 那么就cas. cas成功了就直接返回, 没成功就循环抢
                        compareAndSetState(available, remaining))
                    return remaining;
            }
        }

        protected final boolean tryReleaseShared(int releases) {
            for (; ; ) {
                int current = getState();
                int next = current + releases;
                if (next < current) // overflow
                    throw new Error("Maximum permit count exceeded");
                if (compareAndSetState(current, next))
                    return true;
            }
        }

        final void reducePermits(int reductions) {
            for (; ; ) {
                int current = getState();
                int next = current - reductions;
                if (next > current) // underflow
                    throw new Error("Permit count underflow");
                if (compareAndSetState(current, next))
                    return;
            }
        }

        final int drainPermits() {
            for (; ; ) {
                int current = getState();
                if (current == 0 || compareAndSetState(current, 0))
                    return current;
            }
        }
    }

    /**
     * 非公平锁实现
     */
    static final class NonfairSync extends Sync {
        NonfairSync(int permits) {
            super(permits);
        }

        /**
         * 如果锁已经消费完, 返回值就会<0
         * <p>
         * 非公平就在于, 不会先去判断队列里是否有其他锁在排队, 自己来了先试着争抢
         * 没抢过, 再排队.
         */
        protected int tryAcquireShared(int acquires) {
            return nonfairTryAcquireShared(acquires);
        }
    }

    /**
     * 公平锁实现
     */
    static final class FairSync extends Sync {

        FairSync(int permits) {
            super(permits);
        }

        /**
         * 如果队列里有排队的或者锁已经消费完, 返回值就会<0
         */
        protected int tryAcquireShared(int acquires) {
            for (; ; ) {
                // 如果队列里有等待的, 那么就直接返回-1.因为要让排队的先来.
                if (hasQueuedPredecessors()) return -1;
                int available = getState();
                int remaining = available - acquires;
                // 如果剩余的<0, 说明早就消费完了, 直接返回这个小于0的值
                if (remaining < 0 ||
                        // 如果大于等于0, 那么就cas. cas成功了就直接返回, 没成功就循环抢
                        compareAndSetState(available, remaining))
                    return remaining;
            }
        }
    }
}
