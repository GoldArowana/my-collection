package com.king.learn.concurrent.jdk.locks;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class MyReentrantReadWriteLock implements ReadWriteLock {
    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long TID_OFFSET;

    static {
        try {
            // 通过反射获得unsafe实例.
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);

            //tid 偏移
            TID_OFFSET = UNSAFE.objectFieldOffset(Thread.class.getDeclaredField("tid"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Performs all synchronization mechanics
     */
    final Sync sync;
    /**
     * Inner class providing readlock
     */
    private final MyReentrantReadWriteLock.ReadLock readerLock;
    /**
     * Inner class providing writelock
     */
    private final MyReentrantReadWriteLock.WriteLock writerLock;

    /**
     * Creates a new {@code MyReentrantReadWriteLock} with
     * default (nonfair) ordering properties.
     */
    public MyReentrantReadWriteLock() {
        this(false);
    }

    /**
     * Creates a new {@code MyReentrantReadWriteLock} with
     * the given fairness policy.
     *
     * @param fair {@code true} if this lock should use a fair ordering policy
     */
    public MyReentrantReadWriteLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
        readerLock = new ReadLock(this);
        writerLock = new WriteLock(this);
    }

    /**
     * Returns the thread id for the given thread.  We must access
     * this directly rather than via method Thread.getId() because
     * getId() is not final, and has been known to be overridden in
     * ways that do not preserve unique mappings.
     */
    static final long getThreadId(Thread thread) {
        return UNSAFE.getLongVolatile(thread, TID_OFFSET);
    }

    public MyReentrantReadWriteLock.WriteLock writeLock() {
        return writerLock;
    }

    public MyReentrantReadWriteLock.ReadLock readLock() {
        return readerLock;
    }

    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    // Instrumentation and status

    protected Thread getOwner() {
        return sync.getOwner();
    }


    public int getReadLockCount() {
        return sync.getReadLockCount();
    }


    public boolean isWriteLocked() {
        return sync.isWriteLocked();
    }


    public boolean isWriteLockedByCurrentThread() {
        return sync.isHeldExclusively();
    }


    public int getWriteHoldCount() {
        return sync.getWriteHoldCount();
    }


    public int getReadHoldCount() {
        return sync.getReadHoldCount();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire the write lock.  Because the actual set of threads may
     * change dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive lock monitoring facilities.
     *
     * @return the collection of threads
     */
    protected Collection<Thread> getQueuedWriterThreads() {
        return sync.getExclusiveQueuedThreads();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire the read lock.  Because the actual set of threads may
     * change dynamically while constructing this result, the returned
     * collection is only a best-effort estimate.  The elements of the
     * returned collection are in no particular order.  This method is
     * designed to facilitate construction of subclasses that provide
     * more extensive lock monitoring facilities.
     *
     * @return the collection of threads
     */
    protected Collection<Thread> getQueuedReaderThreads() {
        return sync.getSharedQueuedThreads();
    }

    /**
     * Queries whether any threads are waiting to acquire the read or
     * write lock. Note that because cancellations may occur at any
     * time, a {@code true} return does not guarantee that any other
     * thread will ever acquire a lock.  This method is designed
     * primarily for use in monitoring of the system state.
     *
     * @return {@code true} if there may be other threads waiting to
     * acquire the lock
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * Queries whether the given thread is waiting to acquire either
     * the read or write lock. Note that because cancellations may
     * occur at any time, a {@code true} return does not guarantee
     * that this thread will ever acquire a lock.  This method is
     * designed primarily for use in monitoring of the system state.
     *
     * @param thread the thread
     * @return {@code true} if the given thread is queued waiting for this lock
     * @throws NullPointerException if the thread is null
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**
     * Returns an estimate of the number of threads waiting to acquire
     * either the read or write lock.  The value is only an estimate
     * because the number of threads may change dynamically while this
     * method traverses internal data structures.  This method is
     * designed for use in monitoring of the system state, not for
     * synchronization control.
     *
     * @return the estimated number of threads waiting for this lock
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire either the read or write lock.  Because the actual set
     * of threads may change dynamically while constructing this
     * result, the returned collection is only a best-effort estimate.
     * The elements of the returned collection are in no particular
     * order.  This method is designed to facilitate construction of
     * subclasses that provide more extensive monitoring facilities.
     *
     * @return the collection of threads
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * Queries whether any threads are waiting on the given condition
     * associated with the write lock. Note that because timeouts and
     * interrupts may occur at any time, a {@code true} return does
     * not guarantee that a future {@code signal} will awaken any
     * threads.  This method is designed primarily for use in
     * monitoring of the system state.
     *
     * @param condition the condition
     * @return {@code true} if there are any waiting threads
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException     if the given condition is
     *                                      not associated with this lock
     * @throws NullPointerException         if the condition is null
     */
    public boolean hasWaiters(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((MyAbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * Returns an estimate of the number of threads waiting on the
     * given condition associated with the write lock. Note that because
     * timeouts and interrupts may occur at any time, the estimate
     * serves only as an upper bound on the actual number of waiters.
     * This method is designed for use in monitoring of the system
     * state, not for synchronization control.
     *
     * @param condition the condition
     * @return the estimated number of waiting threads
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException     if the given condition is
     *                                      not associated with this lock
     * @throws NullPointerException         if the condition is null
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((MyAbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * Returns a collection containing those threads that may be
     * waiting on the given condition associated with the write lock.
     * Because the actual set of threads may change dynamically while
     * constructing this result, the returned collection is only a
     * best-effort estimate. The elements of the returned collection
     * are in no particular order.  This method is designed to
     * facilitate construction of subclasses that provide more
     * extensive condition monitoring facilities.
     *
     * @param condition the condition
     * @return the collection of threads
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException     if the given condition is
     *                                      not associated with this lock
     * @throws NullPointerException         if the condition is null
     */
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitingThreads((MyAbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * Returns a string identifying this lock, as well as its lock state.
     * The state, in brackets, includes the String {@code "Write locks ="}
     * followed by the number of reentrantly held write locks, and the
     * String {@code "Read locks ="} followed by the number of held
     * read locks.
     *
     * @return a string identifying this lock, as well as its lock state
     */
    public String toString() {
        int c = sync.getCount();
        int w = Sync.exclusiveCount(c);
        int r = Sync.sharedCount(c);

        return super.toString() +
                "[Write locks = " + w + ", Read locks = " + r + "]";
    }

    /**
     * Synchronization implementation for MyReentrantReadWriteLock.
     * Subclassed into fair and nonfair versions.
     */
    abstract static class Sync extends MyAbstractQueuedSynchronizer {

        // 下面这块说的就是将 state 一分为二，高 16 位用于共享模式，低16位用于独占模式
        static final int SHARED_SHIFT = 16;

        /*
         * Read vs write count extraction constants and functions.
         * Lock state is logically divided into two unsigned shorts:
         * The lower one representing the exclusive (writer) lock hold count,
         * and the upper the shared (reader) hold count.
         */
        static final int SHARED_UNIT = (1 << SHARED_SHIFT);
        static final int MAX_COUNT = (1 << SHARED_SHIFT) - 1;
        static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;
        private static final long serialVersionUID = 6317671515068378041L;
        /**
         * The number of reentrant read locks held by current thread.
         * Initialized only in constructor and readObject.
         * Removed whenever a thread's read hold count drops to 0.
         */
        /**
         * 组合使用上面两个类，用一个 ThreadLocal 来记录当前线程持有的读锁数量
         */
        private transient ThreadLocalHoldCounter readHolds;
        /**
         * The hold count of the last thread to successfully acquire
         * readLock. This saves ThreadLocal lookup in the common case
         * where the next thread to release is the last one to
         * acquire. This is non-volatile since it is just used
         * as a heuristic, and would be great for threads to cache.
         *
         * <p>Can outlive the Thread for which it is caching the read
         * hold count, but avoids garbage retention by not retaining a
         * reference to the Thread.
         *
         * <p>Accessed via a benign data race; relies on the memory
         * model's final field and out-of-thin-air guarantees.
         */
        // 用于缓存，记录"最后一个获取读锁的线程"的读锁重入次数，
        // 所以不管哪个线程获取到读锁后，就把这个值占为已用，这样就不用到 ThreadLocal 中查询 map 了
        // 算不上理论的依据：通常读锁的获取很快就会伴随着释放，
        //   显然，在 获取->释放 读锁这段时间，如果没有其他线程获取读锁的话，此缓存就能帮助提高性能
        private transient HoldCounter cachedHoldCounter;
        /**
         * firstReader is the first thread to have acquired the read lock.
         * firstReaderHoldCount is firstReader's hold count.
         *
         * <p>More precisely, firstReader is the unique thread that last
         * changed the shared count from 0 to 1, and has not released the
         * read lock since then; null if there is no such thread.
         *
         * <p>Cannot cause garbage retention unless the thread terminated
         * without relinquishing its read locks, since tryReleaseShared
         * sets it to null.
         *
         * <p>Accessed via a benign data race; relies on the memory
         * model's out-of-thin-air guarantees for references.
         *
         * <p>This allows tracking of read holds for uncontended read
         * locks to be very cheap.
         */
        // 第一个获取读锁的线程(并且其未释放读锁)，以及它持有的读锁数量
        private transient Thread firstReader = null;
        private transient int firstReaderHoldCount;

        Sync() {
            // 初始化 readHolds 这个 ThreadLocal 属性
            readHolds = new ThreadLocalHoldCounter();
            // 为了保证 readHolds 的内存可见性
            setState(getState()); // ensures visibility of readHolds
        }

        /**
         * Returns the number of shared holds represented in count
         */
        // 取 c 的高 16 位值，代表读锁的获取次数(包括重入)
        static int sharedCount(int c) {
            return c >>> SHARED_SHIFT;
        }

        /**
         * Returns the number of exclusive holds represented in count
         */
        // 取 c 的低 16 位值，代表写锁的重入次数，因为写锁是独占模式
        static int exclusiveCount(int c) {
            return c & EXCLUSIVE_MASK;
        }

        /**
         * Returns true if the current thread, when trying to acquire
         * the read lock, and otherwise eligible to do so, should block
         * because of policy for overtaking other waiting threads.
         */
        abstract boolean readerShouldBlock();

        /**
         * Returns true if the current thread, when trying to acquire
         * the write lock, and otherwise eligible to do so, should block
         * because of policy for overtaking other waiting threads.
         */
        abstract boolean writerShouldBlock();

        /*
         * Acquires and releases use the same code for fair and
         * nonfair locks, but differ in whether/how they allow barging
         * when queues are non-empty.
         */
        // Sync
        // 释放锁，是线程安全的，因为写锁是独占锁，具有排他性
        // 实现很简单，state 减 1 就是了
        protected final boolean tryRelease(int releases) {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int nextc = getState() - releases;
            boolean free = exclusiveCount(nextc) == 0;
            if (free)
                setExclusiveOwnerThread(null);
            setState(nextc);
            // 如果 exclusiveCount(nextc) == 0，也就是说包括重入的，所有的写锁都释放了，
            // 那么返回 true，这样会进行唤醒后继节点的操作。
            return free;
        }

        protected final boolean tryAcquire(int acquires) {
            /*
             * Walkthrough:
             * 1. If read count nonzero or write count nonzero
             *    and owner is a different thread, fail.
             * 2. If count would saturate, fail. (This can only
             *    happen if count is already nonzero.)
             * 3. Otherwise, this thread is eligible for lock if
             *    it is either a reentrant acquire or
             *    queue policy allows it. If so, update state
             *    and set owner.
             */
            Thread current = Thread.currentThread();
            int c = getState();
            int w = exclusiveCount(c);
            if (c != 0) {
                // (Note: if c != 0 and w == 0 then shared count != 0)
                // 看下这里返回 false 的情况：
                //   c != 0 && w == 0: 写锁可用，但是有线程持有读锁(也可能是自己持有)
                //   c != 0 && w !=0 && current != getExclusiveOwnerThread(): 其他线程持有写锁
                //   也就是说，只要有读锁或写锁被占用，这次就不能获取到写锁
                if (w == 0 || current != getExclusiveOwnerThread())
                    return false;
                if (w + exclusiveCount(acquires) > MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                // Reentrant acquire
                // 这里不需要 CAS，仔细看就知道了，能到这里的，只可能是写锁重入，不然在上面的 if 就拦截了
                setState(c + acquires);
                return true;
            }
            // 如果写锁获取不需要 block，那么进行 CAS，成功就代表获取到了写锁
            if (writerShouldBlock() ||
                    !compareAndSetState(c, c + acquires))
                return false;
            setExclusiveOwnerThread(current);
            return true;
        }

        /*
         * Note that tryRelease and tryAcquire can be called by
         * Conditions. So it is possible that their arguments contain
         * both read and write holds that are all released during a
         * condition wait and re-established in tryAcquire.
         */

        protected final boolean tryReleaseShared(int unused) {
            Thread current = Thread.currentThread();
            if (firstReader == current) {
                // assert firstReaderHoldCount > 0;
                // 如果等于 1，那么这次解锁后就不再持有锁了，把 firstReader 置为 null，给后来的线程用
                // 为什么不顺便设置 firstReaderHoldCount = 0？因为没必要，其他线程使用的时候自己会设值
                if (firstReaderHoldCount == 1)
                    firstReader = null;
                else
                    firstReaderHoldCount--;
            } else {
                // 判断 cachedHoldCounter 是否缓存的是当前线程，不是的话要到 ThreadLocal 中取
                HoldCounter rh = cachedHoldCounter;
                if (rh == null || rh.tid != getThreadId(current))
                    rh = readHolds.get();
                int count = rh.count;
                if (count <= 1) {
                    // 这一步将 ThreadLocal remove 掉，防止内存泄漏。因为已经不再持有读锁了
                    readHolds.remove();
                    if (count <= 0)
                        // 就是那种，lock() 一次，unlock() 好几次的逗比
                        throw unmatchedUnlockException();
                }
                // count 减 1
                --rh.count;
            }
            for (; ; ) {
                int c = getState();
                // nextc 是 state 高 16 位减 1 后的值
                int nextc = c - SHARED_UNIT;
                if (compareAndSetState(c, nextc))
                    // Releasing the read lock has no effect on readers,
                    // but it may allow waiting writers to proceed if
                    // both read and write locks are now free.
                    // 如果 nextc == 0，那就是 state 全部 32 位都为 0，也就是读锁和写锁都空了
                    // 此时这里返回 true 的话，其实是帮助唤醒后继节点中的获取写锁的线程
                    return nextc == 0;
            }
        }

        private IllegalMonitorStateException unmatchedUnlockException() {
            return new IllegalMonitorStateException(
                    "attempt to unlock read lock, not locked by current thread");
        }

        protected final int tryAcquireShared(int unused) {
            /*
             * Walkthrough:
             * 1. If write lock held by another thread, fail.
             * 2. Otherwise, this thread is eligible for
             *    lock wrt state, so ask if it should block
             *    because of queue policy. If not, try
             *    to grant by CASing state and updating count.
             *    Note that step does not check for reentrant
             *    acquires, which is postponed to full version
             *    to avoid having to check hold count in
             *    the more typical non-reentrant case.
             * 3. If step 2 fails either because thread
             *    apparently not eligible or CAS fails or count
             *    saturated, chain to version with full retry loop.
             */
            Thread current = Thread.currentThread();
            int c = getState();

            // exclusiveCount(c) 不等于 0，说明有线程持有写锁，
            //    而且不是当前线程持有写锁，那么当前线程获取读锁失败
            //         （另，如果持有写锁的是当前线程，是可以继续获取读锁的）
            if (exclusiveCount(c) != 0 &&
                    getExclusiveOwnerThread() != current)
                return -1;

            // 读锁的获取次数
            int r = sharedCount(c);

            // 读锁获取是否需要被阻塞，稍后细说。为了进去下面的分支，假设这里不阻塞就好了
            if (!readerShouldBlock() &&
                    // 判断是否会溢出 (2^16-1，没那么容易溢出的)
                    r < MAX_COUNT &&
                    // 下面这行 CAS 是将 state 属性的高 16 位加 1，低 16 位不变，如果成功就代表获取到了读锁
                    compareAndSetState(c, c + SHARED_UNIT)) {
                // =======================
                //   进到这里就是获取到了读锁
                // =======================

                if (r == 0) {
                    // r == 0 说明此线程是第一个获取读锁的，或者说在它前面获取读锁的都走光光了，它也算是第一个吧
                    //  记录 firstReader 为当前线程，及其持有的读锁数量：1
                    firstReader = current;
                    firstReaderHoldCount = 1;
                } else if (firstReader == current) {
                    // 进来这里，说明是 firstReader 重入获取读锁（这非常简单，count 加 1 结束）
                    firstReaderHoldCount++;
                } else {
                    // 前面我们说了 cachedHoldCounter 用于缓存最后一个获取读锁的线程
                    // 如果 cachedHoldCounter 缓存的不是当前线程，设置为缓存当前线程的 HoldCounter
                    HoldCounter rh = cachedHoldCounter;
                    if (rh == null || rh.tid != getThreadId(current))
                        cachedHoldCounter = rh = readHolds.get();
                    else if (rh.count == 0)
                        // 到这里，那么就是 cachedHoldCounter 缓存的是当前线程，但是 count 为 0，
                        // 大家可以思考一下：这里为什么要 set ThreadLocal 呢？(当然，答案肯定不在这块代码中)
                        //   既然 cachedHoldCounter 缓存的是当前线程，
                        //   当前线程肯定调用过 readHolds.get() 进行初始化 ThreadLocal
                        readHolds.set(rh);
                    // count 加 1
                    rh.count++;
                }
                // return 大于 0 的数，代表获取到了共享锁
                return 1;
            }
            return fullTryAcquireShared(current);
        }

        /**
         * Full version of acquire for reads, that handles CAS misses
         * and reentrant reads not dealt with in tryAcquireShared.
         */
        /**
         * 1. 刚刚我们说了可能是因为 CAS 失败，如果就此返回，那么就要进入到阻塞队列了，
         * 想想有点不甘心，因为都已经满足了 !readerShouldBlock()，也就是说本来可以不用到阻塞队列的，
         * 所以进到这个方法其实是增加 CAS 成功的机会
         * 2. 在 NonFairSync 情况下，虽然 head.next 是获取写锁的，我知道它等待很久了，我没想和它抢，
         * 可是如果我是来重入读锁的，那么只能表示对不起了
         */
        final int fullTryAcquireShared(Thread current) {
            /*
             * This code is in part redundant with that in
             * tryAcquireShared but is simpler overall by not
             * complicating tryAcquireShared with interactions between
             * retries and lazily reading hold counts.
             */
            HoldCounter rh = null;
            // 别忘了这外层有个 for 循环
            for (; ; ) {
                int c = getState();
                // 如果其他线程持有了写锁，自然这次是获取不到读锁了，乖乖到阻塞队列排队吧
                if (exclusiveCount(c) != 0) {
                    if (getExclusiveOwnerThread() != current)
                        return -1;
                    // else we hold the exclusive lock; blocking here
                    // would cause deadlock.
                } else if (readerShouldBlock()) {
                    // Make sure we're not acquiring read lock reentrantly
                    /*
                     * 进来这里，说明：
                     *  1. exclusiveCount(c) == 0：写锁没有被占用
                     *  2. readerShouldBlock() 为 true，说明阻塞队列中有其他线程在等待
                     *
                     * 既然 should block，那进来这里是干什么的呢？
                     * 答案：是进来处理读锁重入的！
                     *
                     */
                    // firstReader 线程重入读锁，直接到下面的 CAS
                    if (firstReader == current) {
                        // assert firstReaderHoldCount > 0;
                    } else {
                        if (rh == null) {
                            rh = cachedHoldCounter;
                            if (rh == null || rh.tid != getThreadId(current)) {
                                // cachedHoldCounter 缓存的不是当前线程
                                // 那么到 ThreadLocal 中获取当前线程的 HoldCounter
                                // 如果当前线程从来没有初始化过 ThreadLocal 中的值，get() 会执行初始化
                                rh = readHolds.get();
                                // 如果发现 count == 0，也就是说，纯属上一行代码初始化的，那么执行 remove
                                // 然后往下两三行，乖乖排队去
                                if (rh.count == 0)
                                    readHolds.remove();
                            }
                        }
                        if (rh.count == 0)
                            // 排队去。
                            return -1;
                    }
                    /*
                     * 这块代码我看了蛮久才把握好它是干嘛的，原来只需要知道，它是处理重入的就可以了。
                     * 就是为了确保读锁重入操作能成功，而不是被塞到阻塞队列中等待
                     *
                     * 另一个信息就是，这里对于 ThreadLocal 变量 readHolds 的处理：
                     *    如果 get() 后发现 count == 0，居然会做 remove() 操作，
                     *    这行代码对于理解其他代码是有帮助的
                     */
                }
                if (sharedCount(c) == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    // 这里 CAS 成功，那么就意味着成功获取读锁了
                    // 下面需要做的是设置 firstReader 或 cachedHoldCounter
                    if (sharedCount(c) == 0) {
                        // 如果发现 sharedCount(c) 等于 0，就将当前线程设置为 firstReader
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {
                        firstReaderHoldCount++;
                    } else {
                        // 下面这几行，就是将 cachedHoldCounter 设置为当前线程
                        if (rh == null)
                            rh = cachedHoldCounter;
                        if (rh == null || rh.tid != getThreadId(current))
                            rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                        cachedHoldCounter = rh; // cache for release
                    }
                    // 返回大于 0 的数，代表获取到了读锁
                    return 1;
                }
            }
        }

        /**
         * Performs tryLock for write, enabling barging in both modes.
         * This is identical in effect to tryAcquire except for lack
         * of calls to writerShouldBlock.
         */
        final boolean tryWriteLock() {
            Thread current = Thread.currentThread();
            int c = getState();
            if (c != 0) {
                int w = exclusiveCount(c);
                if (w == 0 || current != getExclusiveOwnerThread())
                    return false;
                if (w == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
            }
            if (!compareAndSetState(c, c + 1))
                return false;
            setExclusiveOwnerThread(current);
            return true;
        }

        /**
         * Performs tryLock for read, enabling barging in both modes.
         * This is identical in effect to tryAcquireShared except for
         * lack of calls to readerShouldBlock.
         */
        final boolean tryReadLock() {
            Thread current = Thread.currentThread();
            for (; ; ) {
                int c = getState();
                if (exclusiveCount(c) != 0 &&
                        getExclusiveOwnerThread() != current)
                    return false;
                int r = sharedCount(c);
                if (r == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    if (r == 0) {
                        firstReader = current;
                        firstReaderHoldCount = 1;
                    } else if (firstReader == current) {
                        firstReaderHoldCount++;
                    } else {
                        HoldCounter rh = cachedHoldCounter;
                        if (rh == null || rh.tid != getThreadId(current))
                            cachedHoldCounter = rh = readHolds.get();
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                    }
                    return true;
                }
            }
        }

        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        final Thread getOwner() {
            // Must read state before owner to ensure memory consistency
            return ((exclusiveCount(getState()) == 0) ?
                    null :
                    getExclusiveOwnerThread());
        }

        // Methods relayed to outer class

        final int getReadLockCount() {
            return sharedCount(getState());
        }

        final boolean isWriteLocked() {
            return exclusiveCount(getState()) != 0;
        }

        final int getWriteHoldCount() {
            return isHeldExclusively() ? exclusiveCount(getState()) : 0;
        }

        final int getReadHoldCount() {
            if (getReadLockCount() == 0)
                return 0;

            Thread current = Thread.currentThread();
            if (firstReader == current)
                return firstReaderHoldCount;

            HoldCounter rh = cachedHoldCounter;
            if (rh != null && rh.tid == getThreadId(current))
                return rh.count;

            int count = readHolds.get().count;
            if (count == 0) readHolds.remove();
            return count;
        }

        /**
         * Reconstitutes the instance from a stream (that is, deserializes it).
         */
        private void readObject(java.io.ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            readHolds = new ThreadLocalHoldCounter();
            setState(0); // reset to unlocked state
        }

        final int getCount() {
            return getState();
        }

        /**
         * A counter for per-thread read hold counts.
         * Maintained as a ThreadLocal; cached in cachedHoldCounter
         */
        // 这个嵌套类的实例用来记录每个线程持有的读锁数量(读锁重入)
        static final class HoldCounter {
            // Use id, not reference, to avoid garbage retention
            // 线程 id
            final long tid = getThreadId(Thread.currentThread());

            // 持有的读锁数
            int count = 0;
        }

        /**
         * ThreadLocal subclass. Easiest to explicitly define for sake
         * of deserialization mechanics.
         */
        // ThreadLocal 的子类
        static final class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter> {
            public HoldCounter initialValue() {
                return new HoldCounter();
            }
        }
    }

    /**
     * Nonfair version of Sync
     */
    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -8159625535654395037L;

        // 如果是非公平模式，那么 lock 的时候就可以直接用 CAS 去抢锁，抢不到再排队
        final boolean writerShouldBlock() {
            return false; // writers can always barge
        }

        final boolean readerShouldBlock() {
            /* As a heuristic to avoid indefinite writer starvation,
             * block if the thread that momentarily appears to be head
             * of queue, if one exists, is a waiting writer.  This is
             * only a probabilistic effect since a new reader will not
             * block if there is a waiting writer behind other enabled
             * readers that have not yet drained from the queue.
             */
            boolean ret = false;
            try {
                Method m = MyAbstractQueuedSynchronizer.class.getDeclaredMethod("apparentlyFirstQueuedIsExclusive");
                m.setAccessible(true);
                ret = (boolean) m.invoke(null);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return ret;
        }
    }

    /**
     * Fair version of Sync
     */
    static final class FairSync extends Sync {
        private static final long serialVersionUID = -2274990926593161451L;

        // 如果是公平模式，那么如果阻塞队列有线程等待的话，就乖乖去排队
        final boolean writerShouldBlock() {
            return hasQueuedPredecessors();
        }

        final boolean readerShouldBlock() {
            return hasQueuedPredecessors();
        }
    }

    /**
     * The lock returned by method {@link MyReentrantReadWriteLock#readLock}.
     */
    public static class ReadLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -5992448646407690164L;
        private final Sync sync;

        /**
         * Constructor for use by subclasses
         *
         * @param lock the outer lock object
         * @throws NullPointerException if the lock is null
         */
        protected ReadLock(MyReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        /**
         * Acquires the read lock.
         *
         * <p>Acquires the read lock if the write lock is not held by
         * another thread and returns immediately.
         *
         * <p>If the write lock is held by another thread then
         * the current thread becomes disabled for thread scheduling
         * purposes and lies dormant until the read lock has been acquired.
         */
        public void lock() {
            sync.acquireShared(1);
        }

        /**
         * Acquires the read lock unless the current thread is
         * {@linkplain Thread#interrupt interrupted}.
         *
         * <p>Acquires the read lock if the write lock is not held
         * by another thread and returns immediately.
         *
         * <p>If the write lock is held by another thread then the
         * current thread becomes disabled for thread scheduling
         * purposes and lies dormant until one of two things happens:
         *
         * <ul>
         *
         * <li>The read lock is acquired by the current thread; or
         *
         * <li>Some other thread {@linkplain Thread#interrupt interrupts}
         * the current thread.
         *
         * </ul>
         *
         * <p>If the current thread:
         *
         * <ul>
         *
         * <li>has its interrupted status set on entry to this method; or
         *
         * <li>is {@linkplain Thread#interrupt interrupted} while
         * acquiring the read lock,
         *
         * </ul>
         * <p>
         * then {@link InterruptedException} is thrown and the current
         * thread's interrupted status is cleared.
         *
         * <p>In this implementation, as this method is an explicit
         * interruption point, preference is given to responding to
         * the interrupt over normal or reentrant acquisition of the
         * lock.
         *
         * @throws InterruptedException if the current thread is interrupted
         */
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }

        public boolean tryLock() {
            return sync.tryReadLock();
        }

        public boolean tryLock(long timeout, TimeUnit unit)
                throws InterruptedException {
            return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
        }

        /**
         * Attempts to release this lock.
         *
         * <p>If the number of readers is now zero then the lock
         * is made available for write lock attempts.
         */
        public void unlock() {
            sync.releaseShared(1);
        }

        /**
         * Throws {@code UnsupportedOperationException} because
         * {@code ReadLocks} do not support conditions.
         *
         * @throws UnsupportedOperationException always
         */
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns a string identifying this lock, as well as its lock state.
         * The state, in brackets, includes the String {@code "Read locks ="}
         * followed by the number of held read locks.
         *
         * @return a string identifying this lock, as well as its lock state
         */
        public String toString() {
            int r = sync.getReadLockCount();
            return super.toString() +
                    "[Read locks = " + r + "]";
        }
    }

    /**
     * The lock returned by method {@link MyReentrantReadWriteLock#writeLock}.
     */
    public static class WriteLock implements Lock, java.io.Serializable {
        private static final long serialVersionUID = -4992448646407690164L;
        private final Sync sync;

        protected WriteLock(MyReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        public void lock() {
            sync.acquire(1);
        }

        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        public boolean tryLock() {
            return sync.tryWriteLock();
        }

        public boolean tryLock(long timeout, TimeUnit unit)
                throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }

        public void unlock() {
            sync.release(1);
        }

        public Condition newCondition() {
            return sync.newCondition();
        }

        /**
         * Returns a string identifying this lock, as well as its lock
         * state.  The state, in brackets includes either the String
         * {@code "Unlocked"} or the String {@code "Locked by"}
         * followed by the {@linkplain Thread#getName name} of the owning thread.
         *
         * @return a string identifying this lock, as well as its lock state
         */
        public String toString() {
            Thread o = sync.getOwner();
            return super.toString() + ((o == null) ?
                    "[Unlocked]" :
                    "[Locked by thread " + o.getName() + "]");
        }

        public boolean isHeldByCurrentThread() {
            return sync.isHeldExclusively();
        }

        public int getHoldCount() {
            return sync.getWriteHoldCount();
        }
    }

}
