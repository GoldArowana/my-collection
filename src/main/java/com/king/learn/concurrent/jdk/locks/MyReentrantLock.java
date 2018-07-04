package com.king.learn.concurrent.jdk.locks;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * @author Doug Lea
 * @since 1.5
 */
public class MyReentrantLock implements MyLock {

    /**
     * 锁的引用.可以是公平的和非公平的
     * 公平和非公平都是本类的内部类
     */
    private final Sync sync;

    /**
     * 默认构造器是非公平锁
     */
    public MyReentrantLock() {
        sync = new NonfairSync();
    }

    /**
     * true: 公平锁
     * false: 非公平锁
     */
    public MyReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }

    /**
     * 本方法用于申请并获得锁.
     * <p>
     * 如果锁没有被其他线程获取, 也没被自己获取. 那么就立即return. 设置锁的计数器为1.
     * <p>
     * 如果当前线程之前已经获得了这个锁, 那么就重入, 同时把计数器+1, 立即return;
     * <p>
     * 如果锁被其他线程占了, 那么当前线程就无法获取到锁, 无法继续执行下去了.
     * 那么就挂起线程, 直到能够获取到锁为止, 获取到锁的时候会把计数器设置为1.
     */
    public void lock() {
        sync.lock();
    }

    /**
     * 它表示用来尝试获取锁，如果获取成功，则返回true，
     * 如果获取失败（即锁已被其他线程获取），则返回false，
     * <p>
     * 这个方法无论如何都会立即返回。在拿不到锁时不会一直在那等待。
     */
    public boolean tryLock() {
        return sync.nonfairTryAcquire(1);
    }

    /**
     * 带超时机制的tryLock
     */
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    /**
     * 释放锁
     */
    public void unlock() {
        sync.release(1);
    }

    /**
     * 返回一个Condition实例ConditionObject
     */
    public Condition newCondition() {
        return sync.newCondition();
    }

    /**
     * 获得当前线程的重入次数.
     */
    public int getHoldCount() {
        return sync.getHoldCount();
    }

    /**
     * 判断锁是否是被当前线程所持有
     */
    public boolean isHeldByCurrentThread() {
        return sync.isHeldExclusively();
    }

    /**
     * 判断当前的锁是否被某个线程持有了.
     */
    public boolean isLocked() {
        return sync.isLocked();
    }

    /**
     * 判断当前的锁是否是公平锁
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * 获取当前占用锁的线程.
     * 如果没有被占,那就是null
     */
    protected Thread getOwner() {
        return sync.getOwner();
    }

    /**
     * 是否还有在队列里等待的线程
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * 作为参数传入的这个线程是否还有在队列中
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**
     * @return 等待队列中的长度
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * @return 等待队列中的所有线程
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * 这个condition, 还有没有在wait状态的.
     */
    public boolean hasWaiters(Condition condition) {
        if (condition == null) throw new NullPointerException();
        if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");

        return sync.hasWaiters((MyAbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * 获取这个condition的waiter的个数.
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null) throw new NullPointerException();
        if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");

        return sync.getWaitQueueLength((MyAbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * 获取condition的所有waiter的线程.
     */
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null) throw new NullPointerException();
        if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");

        return sync.getWaitingThreads((MyAbstractQueuedSynchronizer.ConditionObject) condition);
    }

    public String toString() {
        Thread o = sync.getOwner();
        return super.toString() + ((o == null) ?
                "[Unlocked]" :
                "[Locked by thread " + o.getName() + "]");
    }

    abstract static class Sync extends MyAbstractQueuedSynchronizer {

        abstract void lock();

        /**
         * 不公平地尝试获取锁.
         * 不公平的语义就是: 不用判断队列里是否有其他线程在等待, 直接抢.
         */
        final boolean nonfairTryAcquire(int acquires) {
            // 获取当前线程的引用
            final Thread current = Thread.currentThread();
            // 当前线程的重入次数
            int c = getState();
            // 如果是0, 表示此时此刻锁还被被任何一个线程所占用
            if (c == 0) {
                // cas来争抢, 让重入次数变1.
                // 用cas是因为这个地方会发生并发.
                // 多个抢占当然只有一个成功了
                if (compareAndSetState(0, acquires)) {
                    // 设置锁的拥有者为当前线程.
                    setExclusiveOwnerThread(current);
                    return true;
                }

                // 如果不是0, 说明锁被某一个线程占用了
                // 既然被占用了, 那就有两种情况: 1. 被自己占用; 2. 被别的线程占用
                // 所以先看看是不是自己占用的, 如果是自己占用的, 那就重入.
            } else if (current == getExclusiveOwnerThread()) {
                // 其实就是+1
                int nextc = c + acquires;
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                // 这里不会产生争抢, 不必用cas
                // 因为只有占用锁的这一个线程才能进入到这个else if 里
                // 一个线程不可能发生争抢
                setState(nextc);
                return true;
            }
            // 1. 如果在if里的cas争抢失败
            // 2. 或者是不满足else if的条件
            // 那就会直接返回false
            // 不管是成功还是失败, 都不会有线程的等待阻塞之类的. 都是立即返回.
            return false;
        }

        /**
         * releases == 1
         */
        protected final boolean tryRelease(int releases) {
            // 其实就是重入计数器 -1
            int c = getState() - releases;

            // 判断当前的线程是不是持有锁的线程, 不然抛异常.
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();

            // 是否完全释放锁
            boolean free = false;

            // c等于0, 说明没有重入了, 可以完全释放了.
            if (c == 0) {
                // 释放锁
                free = true;
                setExclusiveOwnerThread(null);
            }
            // 设置重入计数器
            setState(c);

            // 表示是否完全释放.
            return free;
        }

        // 判断是否战友锁
        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            // 判断当前线程是持有锁的线程是不是同一个.
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        // 实例化一个condition
        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        // 获取持有当前锁的线程
        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }

        /**
         * 获取调用者线程的重入次数.
         */
        final int getHoldCount() {
            // 如果锁并不是调用方的线程持有的, 那就直接返回0
            // 如果锁是当前调用方的线程持有的, 那就返回重入次数.
            return isHeldExclusively() ? getState() : 0;
        }

        // 当前锁是否是被持有的(被锁住的)
        final boolean isLocked() {
            return getState() != 0;
        }
    }

    /**
     * 非公平锁的实现
     */
    static final class NonfairSync extends Sync {

        final void lock() {
            // 和公平锁相比，这里会直接先进行一次CAS，成功就返回了
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
            } else {
                // 没成功才acquire
                acquire(1);
            }
        }

        /**
         * 抢一次锁看看, 是不是能抢到.
         * 抢到就true
         * 没抢到就false
         */
        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * 公平锁的实现
     */
    static final class FairSync extends Sync {
        final void lock() {
            acquire(1);
        }

        /**
         * 尝试直接获取锁. true: 获取到锁, false: 未获取到锁
         * 返回true：1.没有线程在等待锁；2.重入锁，线程本来就持有锁，也就可以理所当然可以直接获取
         */
        protected final boolean tryAcquire(int acquires) {
            // 获取当前线程的引用
            final Thread current = Thread.currentThread();

            // 当前锁的计数器.
            int c = getState();

            // state == 0 表示此时没有线程持有锁
            if (c == 0) {
                // 虽然此时此刻锁是可以用的，但是这是公平锁，既然是公平，就得讲究先来后到，
                // 看看有没有别人在队列中等了半天了
                // 和非公平锁相比，这里多了一个判断：是否有线程在等待
                if (!hasQueuedPredecessors() &&
                        // 如果没有线程在等待，那就用CAS尝试一下，成功了就获取到锁了，
                        // 不成功的话，只能说明一个问题，就在刚刚几乎同一时刻有个线程抢先了 =_=
                        // 因为刚刚还没人的，我判断过了
                        compareAndSetState(0, acquires)) {
                    // 到这里就是获取到锁了，标记一下，告诉大家，现在是我占用了锁
                    setExclusiveOwnerThread(current);
                    return true;
                }

                // c!=0 表示锁已经被某个线程持有了. c的数值表示重入的次数.
                // 既然当前锁是被占的状态, 那么就看一下是不是当前线程自己占的这个锁.
                // (人家女生说有喜欢的人, 为什么不问问是不是自己呢 = =.)
                // 会进入这个else if分支，说明是重入了，需要操作：state=state+1
            } else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                // 小于0, 说明int溢出了
                if (nextc < 0) throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            return false;
        }
    }
}
