package com.king.learn.collection.jdk8concurrent.locks;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public abstract class MyAbstractQueuedSynchronizer {

    /**
     * 一个时间估计.
     * 小于这个时间的时候, 让线程自旋会比挂起唤醒更好.
     */
    static final long spinForTimeoutThreshold = 1000L;
    /**
     * unsafe和偏移量
     * 用来使用cas
     */
    private static final Unsafe unsafe;
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            // 通过反射获得unsafe实例.
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);

            // 计算偏移量
            stateOffset = unsafe.objectFieldOffset
                    (MyAbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset
                    (MyAbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset
                    (MyAbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset
                    (Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset
                    (Node.class.getDeclaredField("next"));

        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    /**
     * 当前持有锁的线程的引用
     */
    private transient Thread exclusiveOwnerThread;

    /**
     * wait queue 的头结点, 懒初始化.  通过setHead,才会被初始化.
     * Note: If head exists, its waitStatus is guaranteed not to be CANCELLED.
     */
    private transient volatile Node head;
    /**
     * wait queue的尾节点, 懒初始化.
     * 通过enq方法来向队列中添加结点来改变.
     */
    private transient volatile Node tail;

    /**
     * 锁的重入次数
     */
    private volatile int state;

    protected MyAbstractQueuedSynchronizer() {
    }

    /**
     * 当前线程没有抢到锁，是否需要挂起当前线程
     *
     * @param pred 前驱结点
     * @param node 当前结点
     * @return 如果线程需要被阻塞, 那么就返回true
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        // 前驱节点的 waitStatus == -1 ，说明前驱节点状态正常，当前线程需要挂起，直接可以返回true
        if (ws == Node.SIGNAL)
            /*
             * This node has already set status asking a release
             * to signal it, so it can safely park.
             */
            return true;
        // 大于0, 其实就是等于1, Node.CANCELLED 是 1, 因为状态中只有这个状态是大于0的...说明前驱节点取消了排队
        // 所以下面这块代码说的是, 在链表中从prev结点开始, 往前删掉CANCELLED状态的结点.
        // 只有CANCELLED状态值大于0
        if (ws > 0) {
            /*
             * Predecessor was cancelled. Skip over predecessors and
             * indicate retry.
             */
            do {
                node.prev = pred = pred.prev;

                // 删掉之后再往前看看, 看看前面是不是CANCELLED, 如果是, 那还得继续往前删
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /*
             * waitStatus must be 0 or PROPAGATE.  Indicate that we
             * need a signal, but don't park yet.  Caller will need to
             * retry to make sure it cannot acquire before parking.
             */
            // 在前面的两个if语句中排除掉了waitStatus值为-1和1的情况，
            // 只剩下0，-2，-3这三个状态了
            // 然而在我们前面的源码中，都没有看到有设置waitStatus的，
            // 所以只剩下等于0的情况了
            // 下面的操作就是, 如果waitStatus等于0, 那么就用cas将前驱结点的waitStatus设置为-1
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }

    /**
     * 中断当前线程
     */
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    /**
     * CAS 修改Node结点里的waitStatus字段
     */
    private static final boolean compareAndSetWaitStatus(Node node,
                                                         int expect,
                                                         int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                expect, update);
    }

    /**
     * CAS 操作设置Node结点的next结点
     */
    private static final boolean compareAndSetNext(Node node,
                                                   Node expect,
                                                   Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }


    /**
     * getter, 获得当前持有锁的线程引用
     */
    protected final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }

    /**
     * setter, 设置exclusiveOwnerThread字段, 来记录当前哪个线程持有锁
     */
    protected final void setExclusiveOwnerThread(Thread thread) {
        exclusiveOwnerThread = thread;
    }

    /**
     * getter, 获取锁的当前重入次数
     */
    protected final int getState() {
        return state;
    }

    /**
     * setter, state是volatile修饰的
     * 设置锁的重入次数
     */
    protected final void setState(int newState) {
        state = newState;
    }

    /**
     * cas操作设置锁的重入次数
     */
    protected final boolean compareAndSetState(int expect, int update) {
        // See below for intrinsics setup to support this
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    /**
     * 向队列的尾部插入节点, 如果需要的话会进行初始化(也就是这个队列Head节点是null,就得给他初始化了).
     * 采用自旋的方式入队
     * CAS设置tail,直到争抢成功.
     */
    private Node enq(final Node node) {
        for (; ; ) {
            Node t = tail;
            // tail是空, 说明这个队列就不存在.需要初始化head节点.
            if (t == null) { // Must initialize
                if (compareAndSetHead(new Node())) tail = head;
                // 这个时候有了head，tail=head=new Node();
                // 这里只是把队列进行了初始化(设置了head和tail, 原来都是null)
                // 并没有return, 所以继续for循环迭代.以后就不会再进入到这个if了, 都是进入到else中
            } else {
                // 争抢, 没抢到就继续for循环迭代.抢成功了就可以return了,不然一直循环.
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

    /**
     * 将当前节点封装为Node, 然后根据所给的模式, 进行入队操作
     *
     * @param mode 有两种模式 Node.EXCLUSIVE 独占模式, Node.SHARED 共享模式
     * @return 返回新节点, 这个新节点封装了当前线程.
     */
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        // Try the fast path of enq; backup to full enq on failure
        // 以下几行代码想把当前node加到链表的最后面去，也就是进到阻塞队列的最后
        Node pred = tail;
        // 如果tail不是空, 说明有头结点.说明这个队列被初始化了.
        // 因为这个队列是懒初始化(没有线程争抢锁的时候就不需要队列, 就不需要进行初始化了)
        if (pred != null) {
            // node设置自己的前驱为pred
            node.prev = pred;
            // 用CAS把当前节点node设置为队尾, 如果成功后，tail指针就指向了node
            if (compareAndSetTail(pred, node)) {
                // 剩下的就是整理一下链表数据结构的连接问题了
                // pred调整自己的后继为node
                pred.next = node;
                return node;
            }
        }
        // 到这里说明  队列没有被初始化  或者 CAS失败(有线程在竞争入队)
        enq(node);
        return node;
    }

    /**
     * 设置头结点
     */
    private void setHead(Node node) {
        head = node;
        node.thread = null;
        node.prev = null;
    }

    /**
     * 唤醒后继节点
     */
    private void unparkSuccessor(Node node) {
        /*
         * If status is negative (i.e., possibly needing signal) try
         * to clear in anticipation of signalling.  It is OK if this
         * fails or if status is changed by waiting thread.
         */
        int ws = node.waitStatus;
        // 如果节点当前waitStatus<0, 将其修改为0
        if (ws < 0) compareAndSetWaitStatus(node, ws, 0);

        /*
         * Thread to unpark is held in successor, which is normally
         * just the next node.  But if cancelled or apparently null,
         * traverse backwards from tail to find the actual
         * non-cancelled successor.
         */
        // 当前节点的后继
        Node s = node.next;
        //如果这个后继节点是空, 或者取消了排队
        if (s == null || s.waitStatus > 0) {
            s = null;
            // 从队尾往前找，找到waitStatus<=0的所有节点中排在最前面的
            for (Node t = tail; t != null && t != node; t = t.prev)
                // 跳过取消的节点(waitStatus==1)
                if (t.waitStatus <= 0) s = t;
        }
        // 唤醒后继节点
        if (s != null)
            MyLockSupport.unpark(s.thread);
    }

    /**
     * Cancels an ongoing attempt to acquire.
     * 尝试取消正在获取锁的操作
     */
    private void cancelAcquire(Node node) {
        if (node == null) return;

        // 把node里的线程信息置空
        node.thread = null;

        // Skip cancelled predecessors
        // 跳过处于取消状态的前驱节点
        Node pred = node.prev;
        while (pred.waitStatus > 0)
            node.prev = pred = pred.prev;

        // predNext is the apparent node to unsplice. CASes below will
        // fail if not, in which case, we lost race vs another cancel
        // or signal, so no further action is necessary.
        Node predNext = pred.next;

        // Can use unconditional write instead of CAS here.
        // After this atomic step, other Nodes can skip past us.
        // Before, we are free of interference from other threads.
        node.waitStatus = Node.CANCELLED;

        // If we are the tail, remove ourselves.
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            // If successor needs signal, try to set pred's next-link
            // so it will get one. Otherwise wake it up to propagate.
            int ws;
            if (pred != head &&
                    ((ws = pred.waitStatus) == Node.SIGNAL ||
                            (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) &&
                    pred.thread != null) {
                Node next = node.next;
                if (next != null && next.waitStatus <= 0)
                    compareAndSetNext(pred, predNext, next);
            } else {
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
    }

    /**
     * 在这里线程阻塞.
     * 被唤醒的时候会返回, 如果被中断过, 那么就返回true
     *
     * @return {@code true} if interrupted
     */
    private final boolean parkAndCheckInterrupt() {
        // 挂起.
        MyLockSupport.park(this);
        return Thread.interrupted();
    }

    /**
     * Acquires in exclusive uninterruptible mode for thread already in
     * queue. Used by condition wait methods as well as acquire.
     *
     * @param node the node
     * @param arg  the acquire argument
     * @return {@code true} if interrupted while waiting
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (; ; ) {
                final Node p = node.predecessor();
                // 如果是阻塞队列的第一个, 那么可以尝试抢一下锁
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;

                    // 只有这一处return
                    return interrupted;
                }
                // 不是队头, 或者没争抢成功就会执行到这里.

                // 获取锁失败的时候是否该阻塞
                if (shouldParkAfterFailedAcquire(p, node)
                        // 在这里阻塞, 等待唤醒
                        && parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed) cancelAcquire(node);
        }
    }

    /**
     * Acquires in exclusive interruptible mode.
     *
     * @param arg the acquire argument
     */
    private void doAcquireInterruptibly(int arg)
            throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in exclusive timed mode.
     *
     * @param arg          the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                        nanosTimeout > spinForTimeoutThreshold)
                    MyLockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

    public final void acquire(int arg) {
        /* arg==1, 因为调用方都是写死的字面量1
         * 首先尝试tryAcquire(1), 因为try成功了就不用进队列了.
         * 什么时候tryAcquire(1)会成功呢?
         *     1.当前锁空闲, 而且同一时刻没有竞争;
         *     2.这个锁本来就被当前线程持有, 也就是重入
         */
        if (!tryAcquire(arg) &&
                // 如果 tryAcquire 失败，那么进入到阻塞队列等待
                acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }

    /**
     * Attempts to acquire in exclusive mode, aborting if interrupted,
     * and failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquire}, returning on success.  Otherwise, the thread is
     * queued, possibly repeatedly blocking and unblocking, invoking
     * {@link #tryAcquire} until success or the thread is interrupted
     * or the timeout elapses.
     *
     * @param arg          the acquire argument.  This value is conveyed to
     *                     {@link #tryAcquire} but is otherwise uninterpreted and
     *                     can represent anything you like.
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * @return {@code true} if acquired; {@code false} if timed out
     * @throws InterruptedException if the current thread is interrupted
     */
    public final boolean tryAcquireNanos(int arg, long nanosTimeout)
            throws InterruptedException {

        if (Thread.interrupted()) throw new InterruptedException();

        // 先尝试获取锁, 如果没获取到, 那么就带超时的尝试获取.
        return tryAcquire(arg) || doAcquireNanos(arg, nanosTimeout);
    }

    /**
     * 释放锁, 重入计数器-1
     * 如果计数器为0, 那么就彻底释放
     */
    public final boolean release(int arg) {
        // 1. 释放锁
        if (tryRelease(arg)) {
            // 如果锁没有嵌套的了, 可以完全释放了的话, 就会进入到这个if中
            // 2. 如果独占锁释放"完全"，唤醒后继节点
            Node h = head;
            // 唤醒下一个等待的线程
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }


    /**
     * 是否还有在队列里等待的线程
     */
    public final boolean hasQueuedThreads() {
        return head != tail;
    }

    /**
     * 锁是否发生过竞争
     */
    public final boolean hasContended() {
        return head != null;
    }

    /**
     * 返回等待队列中的第一个线程
     */
    public final Thread getFirstQueuedThread() {
        // handle only fast path, else relay
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }

    /**
     * Version of getFirstQueuedThread called when fastpath fails
     * <p>
     * TODO 这里没太看懂
     */
    private Thread fullGetFirstQueuedThread() {
        /*
         * The first node is normally head.next. Try to get its
         * thread field, ensuring consistent reads: If thread
         * field is nulled out or s.prev is no longer head, then
         * some other thread(s) concurrently performed setHead in
         * between some of our reads. We try this twice before
         * resorting to traversal.
         */
        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null &&
                s.prev == head && (st = s.thread) != null) ||
                ((h = head) != null && (s = h.next) != null &&
                        s.prev == head && (st = s.thread) != null))
            return st;

        /*
         * Head's next field might not have been set yet, or may have
         * been unset after setHead. So we must check to see if tail
         * is actually first node. If not, we continue on, safely
         * traversing from tail back to head to find first,
         * guaranteeing termination.
         */

        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null)
                firstThread = tt;
            t = t.prev;
        }
        return firstThread;
    }


    /**
     * Returns true if the given thread is currently queued.
     * 如果传递的参数thread,在队列中, 那么就返回true
     */
    public final boolean isQueued(Thread thread) {
        if (thread == null) throw new NullPointerException();
        for (Node p = tail; p != null; p = p.prev)
            if (p.thread == thread) return true;
        return false;
    }

    /**
     * 是否有线程在等待
     */
    public final boolean hasQueuedPredecessors() {
        // The correctness of this depends on head being initialized
        // before tail and on head.next being accurate if the current
        // thread is first in queue.
        Node t = tail; // Read fields in reverse initialization order
        Node h = head;
        Node s;
        return h != t &&
                ((s = h.next) == null || s.thread != Thread.currentThread());
    }

    /**
     * @return 等待队列的长度
     */
    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null)
                ++n;
        }
        return n;
    }

    /**
     * @return 队列中的所有线程
     */
    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null)
                list.add(t);
        }
        return list;
    }

    public String toString() {
        int s = getState();
        String q = hasQueuedThreads() ? "non" : "";
        return super.toString() +
                "[State = " + s + ", " + q + "empty queue]";
    }

    /**
     * Returns true if a node, always one that was initially placed on
     * a condition queue, is now waiting to reacquire on sync queue.
     * signal 的时候需要将节点从条件队列移到sync queue
     * 这个方法就是判断 node 是否已经移动到sync queue了
     */
    final boolean isOnSyncQueue(Node node) {
        // 当进入Condition队列时，waitStatus肯定为CONDITION，
        // 如果同时别的线程调用signal，Node会从Condition队列中移除，
        // 并且移除时会清除CONDITION状态。
        // 从移除到进入sync queue队列，中间这段时间prev必然为null，所以还是返回false，即被park
        if (node.waitStatus == Node.CONDITION || node.prev == null)
            return false;

        // 当别的线程进入sync queue队列时，会和前一个Node建立前后关系，所以如果next存在，说明一定在release队列中
        if (node.next != null) // If has successor, it must be on queue
            return true;
        /*
         * node.prev can be non-null, but not yet on queue because
         * the CAS to place it on queue can fail. So we have to
         * traverse from tail to make sure it actually made it.  It
         * will always be near the tail in calls to this method, and
         * unless the CAS failed (which is unlikely), it will be
         * there, so we hardly ever traverse much.
         */
        // 这个可以看上篇 AQS 的入队方法，首先设置的是 node.prev 指向 tail，
        // 然后是 CAS 操作将自己设置为新的 tail，可是这次的 CAS 是可能失败的。
        // 所以不能根据node.prev() != null 来推断出 node 在阻塞队列

        // 调用这个方法的时候，往往我们需要的就在队尾的部分，所以一般都不需要完全遍历整个队列的
        // 可能该Node刚刚最后一个进入release队列，所以是tail，其next必然是null，所以需要从队尾向前查找
        return findNodeFromTail(node);
    }

    /**
     * Returns true if node is on sync queue by searching backwards from tail.
     * Called only when needed by isOnSyncQueue.
     * <p>
     * 从同步队列的队尾往前遍历，如果找到，返回 true
     */
    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (; ; ) {
            if (t == node)
                return true;
            if (t == null)
                return false;
            t = t.prev;
        }
    }

    /**
     * Transfers a node from a condition queue onto sync queue.
     * Returns true if successful.
     * 将节点从条件队列转移到阻塞队列
     *
     * @param node the node
     * @return true if successfully transferred (else the node was
     * cancelled before signal)
     * true 代表成功转移
     * false 代表在 signal 之前，节点已经取消了
     */
    final boolean transferForSignal(Node node) {
        /*
         * If cannot change waitStatus, the node has been cancelled.
         *
         * 将 waitStatus 置为 0
         * 如果CAS 失败，说明此 node 的 waitStatus 已不是 Node.CONDITION，说明节点已经取消，
         */
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0))
            return false;

        /*
         * Splice onto queue and try to set waitStatus of predecessor to
         * indicate that thread is (probably) waiting. If cancelled or
         * attempt to set waitStatus fails, wake up to resync (in which
         * case the waitStatus can be transiently and harmlessly wrong).
         *
         * enq(node): 自旋进入阻塞队列的队尾
         * 这里的返回值 p 是 node 在阻塞队列的前驱节点
         */
        Node p = enq(node);
        int ws = p.waitStatus;
        // ws > 0 说明 node 在阻塞队列中的前驱节点取消了等待锁，直接唤醒 node 对应的线程。
        // 如果 ws <= 0, 那么 compareAndSetWaitStatus 将会被调用
        // 因为节点入队后，需要把前驱节点的状态设为 Node.SIGNAL(-1)
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL))
            // 如果前驱节点取消或者 CAS 失败，会进到这里唤醒线程
            MyLockSupport.unpark(node.thread);
        return true;
    }

    /**
     * Transfers node, if necessary, to sync queue after a cancelled wait.
     * Returns true if thread was cancelled before being signalled.
     *
     * @param node the node
     * @return true if cancelled before the node was signalled
     */
    // 只有线程处于中断状态，才会调用此方法
    // 如果需要的话，将这个已经取消等待的节点转移到阻塞队列
    // 返回 true：如果此线程在 signal 之前被取消，
    final boolean transferAfterCancelledWait(Node node) {
        // 用 CAS 将节点状态设置为 0
        // 如果这步 CAS 成功，说明是 signal 方法之前发生的中断，
        // 因为如果 signal 先发生的话，signal 中会将 waitStatus 设置为 0
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            // 将节点放入阻塞队列
            // 这里我们看到，即使中断了，依然会转移到阻塞队列
            enq(node);
            return true;
        }
        /*
         * If we lost out to a signal(), then we can't proceed
         * until it finishes its enq().  Cancelling during an
         * incomplete transfer is both rare and transient, so just
         * spin.
         */
        // 到这里是因为 CAS 失败，肯定是因为 signal 方法已经将 waitStatus 设置为了 0
        // signal 方法会将节点转移到阻塞队列，但是可能还没完成，这边自旋等待其完成
        // 当然，这种事情还是比较少的吧：signal 调用之后，没完成转移之前，发生了中断
        while (!isOnSyncQueue(node))
            Thread.yield();
        return false;
    }

    /**
     * Invokes release with current state value; returns saved state.
     * Cancels node and throws exception on failure.
     *
     * @param node the condition node for this wait
     * @return previous sync state
     */
    // 首先，我们要先观察到返回值 savedState 代表 release 之前的 state 值
    // 对于最简单的操作：先 lock.lock()，然后 condition1.await()。
    //         那么 state 经过这个方法由 1 变为 0，锁释放，此方法返回 1
    //         相应的，如果 lock 重入了 n 次，savedState == n
    // 如果这个方法失败，会将节点设置为"取消"状态，并抛出异常 IllegalMonitorStateException
    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            // 这里使用了当前的 state 作为 release 的参数，也就是完全释放掉锁，将 state 置为 0
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed)
                node.waitStatus = Node.CANCELLED;
        }
    }

    /**
     * condition是否属于当前锁
     */
    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    /**
     * condition中是否有等待的.
     */
    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition)) throw new IllegalArgumentException("Not owner");
        return condition.hasWaiters();
    }

    /**
     * condition的等待队列的长度.
     */
    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition)) throw new IllegalArgumentException("Not owner");
        return condition.getWaitQueueLength();
    }

    /**
     * Attempts to acquire in shared mode, aborting if interrupted, and
     * failing if the given timeout elapses.  Implemented by first
     * checking interrupt status, then invoking at least once {@link
     * #tryAcquireShared}, returning on success.  Otherwise, the
     * thread is queued, possibly repeatedly blocking and unblocking,
     * invoking {@link #tryAcquireShared} until success or the thread
     * is interrupted or the timeout elapses.
     *
     * @param arg          the acquire argument.  This value is conveyed to
     *                     {@link #tryAcquireShared} but is otherwise uninterpreted
     *                     and can represent anything you like.
     * @param nanosTimeout the maximum number of nanoseconds to wait
     * @return {@code true} if acquired; {@code false} if timed out
     * @throws InterruptedException if the current thread is interrupted
     */
    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        return tryAcquireShared(arg) >= 0 ||
                doAcquireSharedNanos(arg, nanosTimeout);
    }

    /**
     * Acquires in shared timed mode.
     *
     * @param arg          the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireSharedNanos(int arg, long nanosTimeout)
            throws InterruptedException {
        if (nanosTimeout <= 0L)
            return false;
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L)
                    return false;
                if (shouldParkAfterFailedAcquire(p, node) &&
                        nanosTimeout > spinForTimeoutThreshold)
                    MyLockSupport.parkNanos(this, nanosTimeout);
                if (Thread.interrupted())
                    throw new InterruptedException();
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * 获得所有等待condition的线程
     */
    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition))
            throw new IllegalArgumentException("Not owner");
        return condition.getWaitingThreads();
    }

    /**
     * CAS 操作 head 字段
     */
    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    /**
     * CAS 操作 tail 字段
     */
    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }


    public final void acquireSharedInterruptibly(int arg)
            throws InterruptedException {

        // 和acquireShared(int arg) 方法相比...就多了一个这句
        if (Thread.interrupted()) throw new InterruptedException();

        // 尝试获取锁失败, 就会执行 doAcquireSharedInterruptibly(1)
        if (tryAcquireShared(arg) < 0) doAcquireSharedInterruptibly(arg);

    }

    /**
     * Sets head of queue, and checks if successor may be waiting
     * in shared mode, if so propagating if either propagate > 0 or
     * PROPAGATE status was set.
     *
     * @param node      the node
     * @param propagate the return value from a tryAcquireShared
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // Record old head for check below
        setHead(node);
        /*
         * Try to signal next queued node if:
         *   Propagation was indicated by caller,
         *     or was recorded (as h.waitStatus either before
         *     or after setHead) by a previous operation
         *     (note: this uses sign-check of waitStatus because
         *      PROPAGATE status may transition to SIGNAL.)
         * and
         *   The next node is waiting in shared mode,
         *     or we don't know, because it appears null
         *
         * The conservatism in both of these checks may cause
         * unnecessary wake-ups, but only when there are multiple
         * racing acquires/releases, so most need signals now or soon
         * anyway.
         */
        if (propagate > 0 || h == null || h.waitStatus < 0 ||
                (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared())
                doReleaseShared();
        }
    }

    /**
     * Releases in shared mode.  Implemented by unblocking one or more
     * threads if {@link #tryReleaseShared} returns true.
     *
     * @param arg the release argument.  This value is conveyed to
     *            {@link #tryReleaseShared} but is otherwise uninterpreted
     *            and can represent anything you like.
     * @return the value returned from {@link #tryReleaseShared}
     */
    public final boolean releaseShared(int arg) {
        // 如果释放完了后state==0, 那就返回true
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }

    /**
     * Release action for shared mode -- signals successor and ensures
     * propagation. (Note: For exclusive mode, release just amounts
     * to calling unparkSuccessor of head if it needs signal.)
     */
    private void doReleaseShared() {
        /*
         * Ensure that a release propagates, even if there are other
         * in-progress acquires/releases.  This proceeds in the usual
         * way of trying to unparkSuccessor of head if it needs
         * signal. But if it does not, status is set to PROPAGATE to
         * ensure that upon release, propagation continues.
         * Additionally, we must loop in case a new node is added
         * while we are doing this. Also, unlike other uses of
         * unparkSuccessor, we need to know if CAS to reset status
         * fails, if so rechecking.
         */
        for (; ; ) {
            Node h = head;
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);
                } else if (ws == 0 &&
                        !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            if (h == head)                   // loop if head changed
                break;
        }
    }

    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Acquires in shared interruptible mode.
     *
     * @param arg the acquire argument
     */
    private void doAcquireSharedInterruptibly(int arg)
            throws InterruptedException {

        // 共享模式将节点添加到队尾
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (; ; ) {
                // 获取前驱
                final Node p = node.predecessor();
                // 如果前驱是头结点, 说明node就是第一个节点
                if (p == head) {
                    // 尝试获取锁
                    int r = tryAcquireShared(arg);
                    // 如果尝试获取锁成功
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return;
                    }
                }
                // 如果不是第一个节点, 那么判断是否需要挂起
                if (shouldParkAfterFailedAcquire(p, node) &&
                        // 挂起
                        parkAndCheckInterrupt())
                    throw new InterruptedException();
            }
        } finally {
            if (failed) cancelAcquire(node);
        }
    }

    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in exclusive mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to an exclusive acquire.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    /**
     * Returns a collection containing threads that may be waiting to
     * acquire in shared mode. This has the same properties
     * as {@link #getQueuedThreads} except that it only returns
     * those threads waiting due to a shared acquire.
     *
     * @return the collection of threads
     */
    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null)
                    list.add(t);
            }
        }
        return list;
    }

    public final void acquireShared(int arg) {
        // 和acquireSharedInterruptibly(int arg) 方法相比,
        // 就少了判断线程中断, 抛异常

        // 尝试获取锁失败, 就会执行 doAcquireSharedInterruptibly(1)
        if (tryAcquireShared(arg) < 0)
            doAcquireShared(arg);
    }

    /**
     * Acquires in shared uninterruptible mode.
     *
     * @param arg the acquire argument
     */
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted)
                            selfInterrupt();
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                        parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }

    /**
     * Acquires in exclusive mode, aborting if interrupted.
     * Implemented by first checking interrupt status, then invoking
     * at least once {@link #tryAcquire}, returning on
     * success.  Otherwise the thread is queued, possibly repeatedly
     * blocking and unblocking, invoking {@link #tryAcquire}
     * until success or the thread is interrupted.  This method can be
     * used to implement method {@link Lock#lockInterruptibly}.
     *
     * @param arg the acquire argument.  This value is conveyed to
     *            {@link #tryAcquire} but is otherwise uninterpreted and
     *            can represent anything you like.
     * @throws InterruptedException if the current thread is interrupted
     */
    public final void acquireInterruptibly(int arg)
            throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        if (!tryAcquire(arg))
            doAcquireInterruptibly(arg);
    }

    // 该方法如果头节点不为空，并头节点的下一个节点不为空，
    // 并且不是共享模式【独占模式，写锁】、并且线程不为空，则返回true。
    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null &&
                (s = h.next) != null &&
                !s.isShared() &&
                s.thread != null;
    }

    static final class Node {
        /**
         * 标记一个节点是不是共享模式
         */
        static final Node SHARED = new Node();
        /**
         * 标记一个节点是不是独占模式
         */
        static final Node EXCLUSIVE = null;

        /**
         * waitStatus value值为1的时候, 表示取消
         */
        static final int CANCELLED = 1;
        /**
         * waitStatus value值为-1的时候, 表示后继节点的线程需要唤醒
         */
        static final int SIGNAL = -1;
        /**
         * waitStatus value值为-2的时候, 表示线程正在等待condition
         */
        static final int CONDITION = -2;
        /**
         * waitStatus value值为-3的时候, 表示处于共享锁的传播模式
         */
        static final int PROPAGATE = -3;

        volatile int waitStatus;

        volatile Node prev;

        volatile Node next;

        volatile Thread thread;

        /**
         * Link to next node waiting on condition, or the special
         * value SHARED.
         * <p>
         * 用于连接condition等待队列的下一个节点.
         * 或者用来标记是否是共享模式.
         */
        Node nextWaiter;

        Node() {    // Used to establish initial head or SHARED marker
        }

        Node(Thread thread, Node mode) {     // Used by addWaiter
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // Used by Condition
            this.waitStatus = waitStatus;
            this.thread = thread;
        }

        /**
         * Returns true if node is waiting in shared mode.
         * <p>
         * 如果当前节点是共享模式, 那么就返回true
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        /**
         * 返回前驱节点
         */
        final Node predecessor() throws NullPointerException {
            final Node p = prev;
            if (p == null) throw new NullPointerException();
            else return p;
        }
    }

    /**
     * Condition
     */
    public class ConditionObject implements Condition {
        /**
         * Mode meaning to reinterrupt on exit from wait
         */
        private static final int REINTERRUPT = 1;
        /**
         * Mode meaning to throw InterruptedException on exit from wait
         */
        private static final int THROW_IE = -1;
        /**
         * condition queue的第一个节点
         */
        private transient Node firstWaiter;

        /**
         * condition queue的最后一个节点
         */
        private transient Node lastWaiter;

        public ConditionObject() {
        }

        /**
         * Adds a new waiter to wait queue.
         * 将当前线程对应的节点入队，插入队尾
         *
         * @return its new wait node
         * 返回新的等待节点.
         */
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            // If lastWaiter is cancelled, clean out.
            // 如果条件队列的最后一个节点取消了，将其清除出去
            if (t != null && t.waitStatus != Node.CONDITION) {
                // 这个方法会遍历整个条件队列，然后会将已取消的所有节点清除出队列
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            // 如果队列为空
            if (t == null) firstWaiter = node;
            else t.nextWaiter = node;
            lastWaiter = node;
            return node;
        }

        /**
         * Removes and transfers nodes until hit non-cancelled one or
         * null. Split out from signal in part to encourage compilers
         * to inline the case of no waiters.
         * <p>
         * 从条condition队头往后遍历，找出第一个需要转移的 node
         * 因为前面我们说过，有些线程会取消排队，但是还在队列中
         *
         * @param first (non-null) the first node on condition queue
         */
        private void doSignal(Node first) {
            do {
                // 将 firstWaiter 指向 first 节点后面的第一个
                // 如果将队头移除后，后面没有节点在等待了，那么需要将 lastWaiter 置为 null
                if ((firstWaiter = first.nextWaiter) == null)
                    lastWaiter = null;
                // 因为 first 马上要被移到阻塞队列了，和条件队列的链接关系在这里断掉
                first.nextWaiter = null;
            } while (!transferForSignal(first) &&
                    (first = firstWaiter) != null);
            // 这里 while 循环，如果 first 转移不成功，那么选择 first 后面的第一个节点进行转移，依此类推
        }

        // public methods

        /**
         * Removes and transfers all nodes.
         *
         * @param first (non-null) the first node on condition queue
         */
        private void doSignalAll(Node first) {
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        /**
         * Unlinks cancelled waiter nodes from condition queue.
         * Called only while holding lock. This is called when
         * cancellation occurred during condition wait, and upon
         * insertion of a new waiter when lastWaiter is seen to have
         * been cancelled. This method is needed to avoid garbage
         * retention in the absence of signals. So even though it may
         * require a full traversal, it comes into play only when
         * timeouts or cancellations occur in the absence of
         * signals. It traverses all nodes rather than stopping at a
         * particular target to unlink all pointers to garbage nodes
         * without requiring many re-traversals during cancellation
         * storms.
         */
        // 等待队列是一个单向链表，遍历链表将已经取消等待的节点清除出去
        // 纯属链表操作，很好理解，看不懂多看几遍就可以了
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                // 如果节点的状态不是 Node.CONDITION 的话，这个节点就是被取消的
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null;
                    if (trail == null)
                        firstWaiter = next;
                    else
                        trail.nextWaiter = next;
                    if (next == null)
                        lastWaiter = trail;
                } else
                    trail = t;
                t = next;
            }
        }

        /**
         * Moves the longest-waiting thread, if one exists, from the
         * wait queue for this condition to the wait queue for the
         * owning lock.
         *
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *                                      returns {@code false}
         */
        // 唤醒等待了最久的线程
        // 其实就是，将这个线程对应的 node 从条件队列转移到阻塞队列
        public final void signal() {
            // 调用 signal 方法的线程必须持有当前的独占锁
            if (!isHeldExclusively()) throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignal(first);
        }

        /*
         * For interruptible waits, we need to track whether to throw
         * InterruptedException, if interrupted while blocked on
         * condition, versus reinterrupt current thread, if
         * interrupted while blocked waiting to re-acquire.
         */

        /**
         * Moves all threads from the wait queue for this condition to
         * the wait queue for the owning lock.
         *
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *                                      returns {@code false}
         */
        public final void signalAll() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            Node first = firstWaiter;
            if (first != null)
                doSignalAll(first);
        }

        /**
         * Implements uninterruptible condition wait.
         * <ol>
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         * throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled.
         * <li> Reacquire by invoking specialized version of
         * {@link #acquire} with saved state as argument.
         * </ol>
         */
        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                MyLockSupport.park(this);
                if (Thread.interrupted())
                    interrupted = true;
            }
            if (acquireQueued(node, savedState) || interrupted)
                selfInterrupt();
        }

        /**
         * Checks for interrupt, returning THROW_IE if interrupted
         * before signalled, REINTERRUPT if after signalled, or
         * 0 if not interrupted.
         */
        // 1. 如果在 signal 之前已经中断，返回 THROW_IE
        // 2. 如果是 signal 之后中断，返回 REINTERRUPT
        // 3. 没有发生中断，返回 0
        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ?
                    (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) :
                    0;
        }

        /**
         * Throws InterruptedException, reinterrupts current thread, or
         * does nothing, depending on mode.
         */
        private void reportInterruptAfterWait(int interruptMode)
                throws InterruptedException {
            if (interruptMode == THROW_IE)
                throw new InterruptedException();
            else if (interruptMode == REINTERRUPT)
                selfInterrupt();
        }

        /**
         * Implements interruptible condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         * throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled or interrupted.
         * <li> Reacquire by invoking specialized version of
         * {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * </ol>
         */
        // 首先，这个方法是可被中断的，不可被中断的是另一个方法 awaitUninterruptibly()
        // 这个方法会阻塞，直到调用 signal 方法（指 signal() 和 signalAll()，下同），或被中断
        public final void await() throws InterruptedException {
            if (Thread.interrupted()) throw new InterruptedException();
            // 添加到 condition 的条件队列中
            Node node = addConditionWaiter();
            // 释放锁，返回值是释放锁之前的 state 值
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            // 这里退出循环有两种情况，之后再仔细分析
            // 1. isOnSyncQueue(node) 返回 true，即当前 node 已经转移到阻塞队列了
            // 2. checkInterruptWhileWaiting(node) != 0 会到 break，然后退出循环，代表的是线程中断
            while (!isOnSyncQueue(node)) {
                // 线程挂起
                MyLockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            // 被唤醒后，将进入阻塞队列，等待获取锁
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null) // clean up if cancelled
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
        }

        /**
         * Implements timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         * throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         * {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * </ol>
         */
        public final long awaitNanos(long nanosTimeout)
                throws InterruptedException {
            if (Thread.interrupted()) throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold)
                    MyLockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return deadline - System.nanoTime();
        }

        /**
         * Implements absolute timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         * throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         * {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * <li> If timed out while blocked in step 4, return false, else true.
         * </ol>
         */
        public final boolean awaitUntil(Date deadline)
                throws InterruptedException {
            long abstime = deadline.getTime();
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > abstime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                MyLockSupport.parkUntil(this, abstime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        /**
         * Implements timed condition wait.
         * <ol>
         * <li> If current thread is interrupted, throw InterruptedException.
         * <li> Save lock state returned by {@link #getState}.
         * <li> Invoke {@link #release} with saved state as argument,
         * throwing IllegalMonitorStateException if it fails.
         * <li> Block until signalled, interrupted, or timed out.
         * <li> Reacquire by invoking specialized version of
         * {@link #acquire} with saved state as argument.
         * <li> If interrupted while blocked in step 4, throw InterruptedException.
         * <li> If timed out while blocked in step 4, return false, else true.
         * </ol>
         */
        public final boolean await(long time, TimeUnit unit)
                throws InterruptedException {
            // 等待这么多纳秒
            long nanosTimeout = unit.toNanos(time);
            if (Thread.interrupted())
                throw new InterruptedException();
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            // 当前时间 + 等待时长 = 过期时间
            final long deadline = System.nanoTime() + nanosTimeout;
            // 用于返回 await 是否超时
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                // 时间到啦
                if (nanosTimeout <= 0L) {
                    // 这里因为要 break 取消等待了。取消等待的话一定要调用 transferAfterCancelledWait(node) 这个方法
                    // 如果这个方法返回 true，在这个方法内，将节点转移到阻塞队列成功
                    // 返回 false 的话，说明 signal 已经发生，signal 方法将节点转移了。也就是说没有超时嘛
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                // spinForTimeoutThreshold 的值是 1000 纳秒，也就是 1 毫秒
                // 也就是说，如果不到 1 毫秒了，那就不要选择 parkNanos 了，自旋的性能反而更好
                if (nanosTimeout >= spinForTimeoutThreshold)
                    MyLockSupport.parkNanos(this, nanosTimeout);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0)
                    break;
                // 得到剩余时间
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE)
                interruptMode = REINTERRUPT;
            if (node.nextWaiter != null)
                unlinkCancelledWaiters();
            if (interruptMode != 0)
                reportInterruptAfterWait(interruptMode);
            return !timedout;
        }

        //  support for instrumentation

        /**
         * Returns true if this condition was created by the given
         * synchronization object.
         *
         * @return {@code true} if owned
         */
        final boolean isOwnedBy(MyAbstractQueuedSynchronizer sync) {
            return sync == MyAbstractQueuedSynchronizer.this;
        }

        /**
         * Queries whether any threads are waiting on this condition.
         * Implements {@link MyAbstractQueuedSynchronizer#hasWaiters(ConditionObject)}.
         *
         * @return {@code true} if there are any waiting threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *                                      returns {@code false}
         */
        protected final boolean hasWaiters() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    return true;
            }
            return false;
        }

        /**
         * Returns an estimate of the number of threads waiting on
         * this condition.
         * Implements {@link MyAbstractQueuedSynchronizer#getWaitQueueLength(ConditionObject)}.
         *
         * @return the estimated number of waiting threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *                                      returns {@code false}
         */
        protected final int getWaitQueueLength() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION)
                    ++n;
            }
            return n;
        }

        /**
         * Returns a collection containing those threads that may be
         * waiting on this Condition.
         * Implements {@link MyAbstractQueuedSynchronizer#getWaitingThreads(ConditionObject)}.
         *
         * @return the collection of threads
         * @throws IllegalMonitorStateException if {@link #isHeldExclusively}
         *                                      returns {@code false}
         */
        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively())
                throw new IllegalMonitorStateException();
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null)
                        list.add(t);
                }
            }
            return list;
        }
    }
}
