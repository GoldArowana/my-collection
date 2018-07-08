package com.king.learn.collection.jdk8concurrent.locks;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
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
     * 实际同步器的引用
     */
    final Sync sync;

    /**
     * 读锁
     */
    private final MyReentrantReadWriteLock.ReadLock readerLock;

    /**
     * 写锁
     */
    private final MyReentrantReadWriteLock.WriteLock writerLock;

    /**
     * 默认是非公平的
     */
    public MyReentrantReadWriteLock() {
        this(false);
    }

    /**
     * true: 公平
     * false: 非公平
     */
    public MyReentrantReadWriteLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
        readerLock = new ReadLock(this);
        writerLock = new WriteLock(this);
    }

    /**
     * 传入一个线程, 返回该线程的tid
     */
    static final long getThreadId(Thread thread) {
        return UNSAFE.getLongVolatile(thread, TID_OFFSET);
    }

    /**
     * @return 写锁的实例
     */
    public MyReentrantReadWriteLock.WriteLock writeLock() {
        return writerLock;
    }

    /**
     * @return 读锁的实例
     */
    public MyReentrantReadWriteLock.ReadLock readLock() {
        return readerLock;
    }

    /**
     * @return 是否是公平的
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * 获取当前持有锁的线程
     */
    protected Thread getOwner() {
        return sync.getOwner();
    }

    public int getReadLockCount() {
        return sync.getReadLockCount();
    }


    /**
     * 写锁是否被持有
     */
    public boolean isWriteLocked() {
        return sync.isWriteLocked();
    }


    /**
     * 是否被当前线程持有死锁
     */
    public boolean isWriteLockedByCurrentThread() {
        return sync.isHeldExclusively();
    }


    /**
     * @return 写锁的重入次数.(而不是写锁的持有线程数, 因为写锁是独占的)
     */
    public int getWriteHoldCount() {
        return sync.getWriteHoldCount();
    }


    public int getReadHoldCount() {
        return sync.getReadHoldCount();
    }

    /**
     * @return 所有等待写锁的线程
     */
    protected Collection<Thread> getQueuedWriterThreads() {
        return sync.getExclusiveQueuedThreads();
    }

    /**
     * @return 所有等待读锁的线程
     */
    protected Collection<Thread> getQueuedReaderThreads() {
        return sync.getSharedQueuedThreads();
    }

    /**
     * @return 是否还有在队列里等待的线程
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     * 判断该线程是否在队列里等待
     */
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
    }

    /**
     * 等待队列的长度
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     * @return 等待队列里的所有线程
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     * 该condition是否有线程在等待
     */
    public boolean hasWaiters(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((MyAbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * @return condition的wait队列的长度
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitQueueLength((MyAbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /**
     * @return condition的wait队列里的所有线程
     */
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof MyAbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.getWaitingThreads((MyAbstractQueuedSynchronizer.ConditionObject) condition);
    }

    public String toString() {
        int c = sync.getCount();
        int w = Sync.exclusiveCount(c);
        int r = Sync.sharedCount(c);

        return super.toString() +
                "[Write locks = " + w + ", Read locks = " + r + "]";
    }

    /**
     * 自定义aqs
     */
    abstract static class Sync extends MyAbstractQueuedSynchronizer {

        // state 根据二进制长度一分为二，高 16 位用于共享模式，低16位用于独占模式
        static final int SHARED_SHIFT = 16;
        static final int SHARED_UNIT = (1 << SHARED_SHIFT);
        static final int MAX_COUNT = (1 << SHARED_SHIFT) - 1;
        static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;
        /**
         * MyThreadLocal, 用来记录当前线程持有的读锁数量
         */
        private transient ThreadLocalHoldCounter readHolds;

        // 用于缓存，记录"最后一个获取读锁的线程"的读锁重入次数，
        // 这样就不用到 MyThreadLocal 中查询 map 了, 可能会提高性能
        // 通常读锁的获取很快就会伴随着释放，
        // 显然，在 获取->释放 读锁这段时间，如果没有其他线程获取读锁的话，此缓存就能帮助提高性能
        private transient HoldCounter cachedHoldCounter;

        // 当前持有读锁的线程中, 第一个获取到读锁的线程
        private transient Thread firstReader = null;
        // firstReader线程持有的读锁数量
        private transient int firstReaderHoldCount;

        Sync() {
            // 初始化 readHolds 这个 MyThreadLocal 属性
            readHolds = new ThreadLocalHoldCounter();
            // 为了保证 readHolds 的内存可见性.(不太懂)
            setState(getState()); // ensures visibility of readHolds
        }

        // 取 c 的高 16 位值，代表读锁的获取次数(包括重入)
        static int sharedCount(int c) {
            return c >>> SHARED_SHIFT;
        }

        // 取 c 的低 16 位值，代表写锁的重入次数，因为写锁是独占模式
        static int exclusiveCount(int c) {
            return c & EXCLUSIVE_MASK;
        }

        abstract boolean readerShouldBlock();

        abstract boolean writerShouldBlock();

        // 实现很简单，state 减 1 就是了
        protected final boolean tryRelease(int releases) {
            if (!isHeldExclusively()) throw new IllegalMonitorStateException();
            int nextc = getState() - releases;
            boolean free = exclusiveCount(nextc) == 0;
            // 如果个数是0, 那么就把持有线程设置为null, 意味着彻底释放锁
            //(如果不是0, 那就说明刚刚只是退出了一层重入)
            if (free) setExclusiveOwnerThread(null);
            setState(nextc);
            return free;
        }

        /**
         * 尝试着获取锁
         */
        protected final boolean tryAcquire(int acquires) {
            /*
             * 1. 如果读锁的计数器非零, 返回false
             * 2. 如果写锁的计数器非零的时候, 当前的线程不是持有锁的线程,
             *    (表示锁被其他线程占着) 那么false.
             * 3. 如果锁的计数器溢出, 那么false.(这只能在非零的时候发生)
             * 4. Otherwise, this thread is eligible for lock if
             *    it is either a reentrant acquire or
             *    queue policy allows it. If so, update state
             *    and set owner.
             */
            Thread current = Thread.currentThread();
            int c = getState();
            // 获取写锁的重入次数
            int w = exclusiveCount(c);
            // c==0说明, 写锁和读锁都没有.
            if (c != 0) {
                //   c != 0 && w == 0: 写锁可用，但是有线程持有读锁(也可能是自己持有)
                if (w == 0 ||
                        //   c != 0 && w !=0 && current != getExclusiveOwnerThread(): 其他线程持有写锁
                        current != getExclusiveOwnerThread())
                    return false;

                // 超过最大个数, 溢出
                if (w + exclusiveCount(acquires) > MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");

                // 锁重入. 这里不需要 CAS，能到这里的，只可能是写锁重入, 不然上面线程判断中进行return false了
                setState(c + acquires);
                return true;
            }
            // 如果写锁获取不需要 block，那么进行 CAS，成功就代表获取到了写锁
            if (writerShouldBlock() ||
                    !compareAndSetState(c, c + acquires))
                // 如果cas失败了, 就返回false
                return false;
            // 到这里说明cas争抢成功, 把锁的线程引用指向当前线程
            setExclusiveOwnerThread(current);

            // 占锁成功就返回true
            return true;
        }

        /*
         * Note that tryRelease and tryAcquire can be called by
         * Conditions. So it is possible that their arguments contain
         * both read and write holds that are all released during a
         * condition wait and re-established in tryAcquire.
         */
        protected final boolean tryReleaseShared(int unused) {// 参数没用
            // 获取当前线程的引用
            Thread current = Thread.currentThread();

            // 判断当前线程是不是当前读锁中的第一个读线程
            if (firstReader == current) {
                assert firstReaderHoldCount > 0;

                // 如果等于 1，那么这次解锁后就不再持有锁了，把 firstReader 置为 null，给后来的线程用
                if (firstReaderHoldCount == 1)
                    // 为什么不顺便设置 firstReaderHoldCount = 0？因为没必要，其他线程使用的时候自己会设值
                    firstReader = null;
                else
                    firstReaderHoldCount--;
            } else {
                HoldCounter rh = cachedHoldCounter;
                // 判断cachedHoldCounter是不是空, 是空的话, 要到 MyThreadLocal 中取
                if (rh == null
                        // 判断 cachedHoldCounter 是否缓存的是当前线程，不是的话要到 MyThreadLocal 中取
                        || rh.tid != getThreadId(current))
                    rh = readHolds.get();
                int count = rh.count;
                // 如果计数器小于等于1, 说明该释放了.
                if (count <= 1) {
                    // 这一步将 MyThreadLocal remove 掉，防止内存泄漏。因为已经不再持有读锁了
                    readHolds.remove();
                    // 没锁还要释放? 给你抛个异常...
                    if (count <= 0) throw unmatchedUnlockException();
                }
                // count 减 1
                --rh.count;
            }
            for (; ; ) {
                int c = getState();
                // state 的高 16 部分位减 1 , 低16位不动. (高16位是共享模式)
                int nextc = c - SHARED_UNIT;
                // cas 设置 state
                if (compareAndSetState(c, nextc))
                    // 释放读锁, 对读线程们没有什么影响
                    // 但如果是 nextc == 0，那就是 state 全部 32 位都为 0，也就是读锁和写锁都空了
                    // 此时这里返回 true 的话，其实是帮助唤醒后继节点中的获取写锁的线程
                    return nextc == 0;
            }
        }

        // 当前线程没有占有锁, 还想解锁...那就来这个异常.
        private IllegalMonitorStateException unmatchedUnlockException() {
            return new IllegalMonitorStateException(
                    "attempt to unlock read lock, not locked by current thread");
        }

        protected final int tryAcquireShared(int unused) {// 参数没用
            /*
             * 1. 如果写锁被其他线程持有, 那么直接失败
             * 2. 不然说明现在适合读锁, 用cas
             * 3. 如果第2步cas失败, 或者是计数器溢出, 那么就执行fullTryAcquireShared
             */
            // 获取当前线程的引用
            Thread current = Thread.currentThread();
            int c = getState();

            // exclusiveCount(c) 不等于 0，说明有线程持有写锁
            if (exclusiveCount(c) != 0 &&
                    //    而且不是当前线程持有写锁
                    getExclusiveOwnerThread() != current)
                // 那么当前线程获取读锁失败
                return -1;

            // 读锁的获取次数
            int r = sharedCount(c);

            // 读锁获取是否需要被阻塞
            if (!readerShouldBlock() &&
                    // 判断是否会溢出 (2^16-1)
                    r < MAX_COUNT &&
                    // 下面这行 CAS 是将 state 属性的高 16 位加 1，低 16 位不变，如果成功就代表获取到了读锁
                    compareAndSetState(c, c + SHARED_UNIT)) {

                /* ----------------------
                 *  进到这里就是获取到了读锁
                 * ----------------------*/

                // r == 0 说明此线程是第一个获取读锁的，或者说在它前面获取读锁的都走光光了，它也算是第一个吧
                if (r == 0) {
                    // 记录 firstReader 为当前线程
                    firstReader = current;
                    // 持有的读锁数量为1
                    firstReaderHoldCount = 1;

                    // 进来这里，说明是 firstReader 重入获取读锁
                } else if (firstReader == current) {
                    // 重入, 所以 计数器count 加 1
                    firstReaderHoldCount++;

                    // 读锁被其他线程占用
                } else {
                    // cachedHoldCounter 用于缓存最后一个获取读锁的线程
                    HoldCounter rh = cachedHoldCounter;

                    // 如果cachedHoldCounter为空
                    if (rh == null ||
                            //或者 如果 cachedHoldCounter 缓存的不是当前线程
                            rh.tid != getThreadId(current))
                        // 设置为缓存当前线程的 HoldCounter
                        cachedHoldCounter = rh = readHolds.get();

                        // cachedHoldCounter 缓存的是当前线程，但是 count 为 0，
                    else if (rh.count == 0)
                        // 大家可以思考一下：这里为什么要 set MyThreadLocal 呢？(当然，答案肯定不在这块代码中)
                        readHolds.set(rh);
                    // count 加 1
                    rh.count++;
                }
                // return 一个数, 表示获取到了几个锁
                return 1;
            }
            return fullTryAcquireShared(current);
        }

        /**
         * 1. 刚刚我们说了可能是因为 CAS 失败，如果就此返回，那么就要进入到阻塞队列了，
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
            for (; ; ) {
                int c = getState();
                // 如果其他线程持有了写锁，到阻塞队列排队
                if (exclusiveCount(c) != 0) {
                    if (getExclusiveOwnerThread() != current)
                        return -1;

                    // else we hold the exclusive lock
                    // blocking here would cause deadlock.
                    // 是否需要被阻塞
                } else if (readerShouldBlock()) {
                    /*
                     * 进来这里，说明：
                     *  1. exclusiveCount(c) == 0：写锁没有被占用
                     *  2. readerShouldBlock() 为 true，说明阻塞队列中有其他线程在等待
                     *
                     * 既然 should block，那进来这里是干什么的呢？
                     * 答案：是进来处理读锁重入的！
                     */

                    // firstReader 线程重入读锁，直接到下面的 CAS
                    if (firstReader == current) {
                        // 什么都不做
                        //assert firstReaderHoldCount > 0;

                    } else {
                        // rh==null
                        if (rh == null) {
                            // 赋值
                            rh = cachedHoldCounter;
                            // 如果cachedHoldCounter为空
                            if (rh == null ||
                                    //或者 如果 cachedHoldCounter 缓存的不是当前线程
                                    rh.tid != getThreadId(current)) {
                                // 那么到 MyThreadLocal 中获取当前线程的 HoldCounter, 没有就自动初始化
                                rh = readHolds.get();
                                // 如果发现 count == 0，也就是说，纯属上一行代码初始化的，那么执行 remove
                                // 然后往下两三行，乖乖排队去
                                if (rh.count == 0)
                                    readHolds.remove();
                            }
                        }
                        // rh != null , 并且持有的读锁数等于0
                        if (rh.count == 0)
                            // 排队去
                            return -1;
                    }
                    /*
                     * 这块代码我看了蛮久才把握好它是干嘛的，原来只需要知道，它是处理重入的就可以了。
                     * 就是为了确保读锁重入操作能成功，而不是被塞到阻塞队列中等待
                     *
                     * 另一个信息就是，这里对于 MyThreadLocal 变量 readHolds 的处理：
                     *    如果 get() 后发现 count == 0，居然会做 remove() 操作，
                     *    这行代码对于理解其他代码是有帮助的
                     */
                }

                // 如果读锁持有数超过上限, 那么就报异常
                if (sharedCount(c) == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                // cas 读锁+1
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    // 这里 CAS 成功，那么就意味着成功获取读锁了,下面需要做的是设置 firstReader 或 cachedHoldCounter
                    // 如果发现 sharedCount(c) 等于 0, 也就是当前线程是第一个持有读锁的
                    if (sharedCount(c) == 0) {
                        // 将当前线程设置为 firstReader
                        firstReader = current;
                        // 计数器设置为1
                        firstReaderHoldCount = 1;

                        // 如果是重入
                    } else if (firstReader == current) {
                        // 计数器++
                        firstReaderHoldCount++;

                        // 当前线程不是 firstReader,那么将 cachedHoldCounter 设置为当前线程
                    } else {
                        if (rh == null)
                            rh = cachedHoldCounter;
                        // 如果cachedHoldCounter为空
                        if (rh == null ||
                                // 或者如果当前线程不是最后一次获得读锁的线程
                                rh.tid != getThreadId(current))
                            // 那么就只能从threadlocal中 get了
                            rh = readHolds.get();

                            // 如果cachedHoldCounter不是空, 或者cachedHoldCounter是当前线程
                            // 而且计数器等于0
                        else if (rh.count == 0)
                            // 那么设置进threadlocal里
                            readHolds.set(rh);
                        // 计数器+1
                        rh.count++;

                        // 重置cachedHoldCounter, cachedHoldCounter是最后一次操作的缓存(缓存了tid和counter)
                        cachedHoldCounter = rh; // cache for release
                    }
                    // 返回1, 表示获取了1个锁
                    return 1;
                }
            }
        }

        /**
         * 尝试获取写锁
         */
        final boolean tryWriteLock() {
            Thread current = Thread.currentThread();
            int c = getState();
            // c!=0 说明写锁或者读锁被占着呢
            if (c != 0) {
                // 获取写锁计数器
                int w = exclusiveCount(c);
                // c!=0 && w==0 表示r!=0, 读锁被占
                if (w == 0 ||
                        // c!=0, w!=0 ,写锁被其他线程占用
                        current != getExclusiveOwnerThread())
                    return false;
                // 重入超过最大值, 抛异常
                if (w == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
            }
            // 争抢失败, false.
            if (!compareAndSetState(c, c + 1))
                return false;
            // 争抢成功, 设置写锁持有者为当前线程. true
            setExclusiveOwnerThread(current);
            return true;
        }

        /**
         * 尝试获取读锁
         */
        final boolean tryReadLock() {
            // 获取当先线程的引用
            Thread current = Thread.currentThread();
            for (; ; ) {
                int c = getState();
                // 写锁被占
                if (exclusiveCount(c) != 0 &&
                        // 占有写锁的线程不是当前线程
                        getExclusiveOwnerThread() != current)
                    // 那么就直接false失败.
                    return false;
                // 获取读锁计数器
                int r = sharedCount(c);
                // 读锁计数器溢出的话报异常
                if (r == MAX_COUNT)
                    throw new Error("Maximum lock count exceeded");
                // cas设置读锁计数器+1, cas失败了就for循环重新来.
                if (compareAndSetState(c, c + SHARED_UNIT)) {
                    // 读锁计数器等于0, 表示当前线程是第一个获取读锁的线程
                    if (r == 0) {
                        // 设置firstReader为当前线程
                        firstReader = current;
                        // firstReader的计数器设置为1
                        firstReaderHoldCount = 1;

                        // firstReader是当前线程, 说明读锁重入
                    } else if (firstReader == current) {
                        // 计数器+1
                        firstReaderHoldCount++;

                        // 读锁被持有, 而且当前线程还不是第一个持有读锁的
                    } else {
                        HoldCounter rh = cachedHoldCounter;
                        // 如果cachedHoldCounter为空
                        if (rh == null ||
                                // 或者当前线程不是最后一个操作读锁的线程
                                rh.tid != getThreadId(current))
                            cachedHoldCounter = rh = readHolds.get();
                            // 如果 cachedHoldCounter不空, 而且当前线程是最后一个操作读锁的线程
                        else if (rh.count == 0)
                            readHolds.set(rh);
                        rh.count++;
                    }
                    return true;
                }
            }
        }

        /**
         * @return 当前线程是否持有写锁
         */
        protected final boolean isHeldExclusively() {
            // While we must in general read state before owner,
            // we don't need to do so to check if current thread is owner
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        /**
         * @return 新的condition实例
         */
        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        /**
         * @return 持有写锁的线程
         */
        final Thread getOwner() {
            // Must read state before owner to ensure memory consistency
            return ((exclusiveCount(getState()) == 0) ?
                    null :
                    getExclusiveOwnerThread());
        }

        /**
         * @return 获取读锁的重入次数
         */
        final int getReadLockCount() {
            return sharedCount(getState());
        }

        /**
         * @return 写锁是否被占用
         */
        final boolean isWriteLocked() {
            return exclusiveCount(getState()) != 0;
        }

        /**
         * @return 当前线程持有的写锁的重入次数
         */
        final int getWriteHoldCount() {
            return isHeldExclusively() ? exclusiveCount(getState()) : 0;
        }

        /**
         * @return 当前线程持有的读锁的重入次数
         */
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
         * @return 获取state
         */
        final int getCount() {
            return getState();
        }

        /**
         * A counter for per-thread read hold counts.
         * Maintained as a MyThreadLocal; cached in cachedHoldCounter
         * <p>
         * 这个嵌套类的实例用来记录每个线程持有的读锁数量(读锁重入)
         */
        static final class HoldCounter {
            // Use id, not reference, to avoid garbage retention
            //线程id, 并不是引用
            final long tid = getThreadId(Thread.currentThread());

            // 持有的读锁数
            int count = 0;
        }

        /**
         * MyThreadLocal subclass. Easiest to explicitly define for sake
         * of deserialization mechanics.
         * <p>
         * 这时ThreadLocal 的子类
         */
        static final class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter> {
            public HoldCounter initialValue() {
                return new HoldCounter();
            }
        }
    }

    /**
     * 非公平锁
     */
    static final class NonfairSync extends Sync {

        // 如果是非公平模式，那么 lock 的时候就可以直接用 CAS 去抢锁，抢不到再排队
        final boolean writerShouldBlock() {
            return false; // writers can always barge  // barge是闯入的意思
        }

        final boolean readerShouldBlock() {
            /* As a heuristic to avoid indefinite writer starvation,
             * block if the thread that momentarily appears to be head
             * of queue, if one exists, is a waiting writer.  This is
             * only a probabilistic effect since a new reader will not
             * block if there is a waiting writer behind other enabled
             * readers that have not yet drained from the queue.
             *
             * 这个方法判断队列的head.next是否正在等待独占锁（写锁）。
             *
             * 当然这个方法执行的过程中队列的形态可能发生变化。
             *
             * 这个方法的意思是：读锁不应该让写锁始终等待。
             */
            return apparentlyFirstQueuedIsExclusive();
        }
    }

    /**
     * 公平锁
     */
    static final class FairSync extends Sync {
        // 如果是公平模式，那么如果阻塞队列有线程等待的话，就去排队
        final boolean writerShouldBlock() {
            return hasQueuedPredecessors();
        }

        // 如果是公平模式，那么如果阻塞队列有线程等待的话，就去排队
        final boolean readerShouldBlock() {
            return hasQueuedPredecessors();
        }
    }

    /**
     * 读锁
     */
    public static class ReadLock implements Lock {
        private final Sync sync;

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
         * 可抛异常的lock()
         *
         * @throws InterruptedException
         */
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }

        /**
         * 尝试获取读锁
         */
        public boolean tryLock() {
            return sync.tryReadLock();
        }

        /**
         * 等待时间超时的尝试获取锁.
         */
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
         * 读锁没有condition
         */
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            int r = sync.getReadLockCount();
            return super.toString() +
                    "[Read locks = " + r + "]";
        }
    }

    /**
     * 写锁
     */
    public static class WriteLock implements Lock {
        private final Sync sync;

        protected WriteLock(MyReentrantReadWriteLock lock) {
            sync = lock.sync;
        }

        public void lock() {
            sync.acquire(1);
        }

        /**
         * 可抛异常的lock()
         *
         * @throws InterruptedException
         */
        public void lockInterruptibly() throws InterruptedException {
            sync.acquireInterruptibly(1);
        }

        /**
         * 尝试获取锁
         */
        public boolean tryLock() {
            return sync.tryWriteLock();
        }

        /**
         * 带超时机制的获取写锁
         */
        public boolean tryLock(long timeout, TimeUnit unit)
                throws InterruptedException {
            return sync.tryAcquireNanos(1, unit.toNanos(timeout));
        }

        /**
         * 释放写锁
         */
        public void unlock() {
            sync.release(1);
        }

        /**
         * @return 一个新的condition实例
         */
        public Condition newCondition() {
            return sync.newCondition();
        }

        public String toString() {
            Thread o = sync.getOwner();
            return super.toString() + ((o == null) ?
                    "[Unlocked]" :
                    "[Locked by thread " + o.getName() + "]");
        }

        /**
         * @return 当前线程是否持有写锁
         */
        public boolean isHeldByCurrentThread() {
            return sync.isHeldExclusively();
        }

        /**
         * @return 当前线程的重入次数
         */
        public int getHoldCount() {
            return sync.getWriteHoldCount();
        }
    }

}
