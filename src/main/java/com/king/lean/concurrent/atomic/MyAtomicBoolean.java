package com.king.lean.concurrent.atomic;

import sun.misc.Unsafe;

import java.io.Serializable;

public class MyAtomicBoolean implements Serializable {
    private static final long serialVersionUID = 4654671469794556979L;

    // 用来调用cas
    private static final Unsafe unsafe = Unsafe.getUnsafe();

    // 表示该变量值在内存中的偏移地址，因为Unsafe就是根据内存偏移地址获取数据的
    private static final long valueOffset;

    static {
        try {
            valueOffset = unsafe.objectFieldOffset(MyAtomicBoolean.class.getDeclaredField("value"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    //用volatile修饰，保证多线程之间的内存可见性
    private volatile int value;

    public MyAtomicBoolean(boolean initialValue) {
        value = initialValue ? 1 : 0;
    }

    public MyAtomicBoolean() {
    }

    /**
     * @return 当前的结果
     */
    public final boolean get() {
        return value != 0;
    }

    /**
     * 如果当前的值等于expect, 那么就会把当前的值更新为update,并且返回true.
     * 如果当前的值不等于expect, 那么就会返回false
     */
    public final boolean compareAndSet(boolean expect, boolean update) {
        int e = expect ? 1 : 0;
        int u = update ? 1 : 0;
        return unsafe.compareAndSwapInt(this, valueOffset, e, u);
    }

    /**
     * 无条件地设置value值
     */
    public final void set(boolean newValue) {
        value = newValue ? 1 : 0;
    }

    /**
     * Eventually sets to the given value.
     * <p>
     * https://blog.csdn.net/iter_zc/article/details/40744485
     */
    public final void lazySet(boolean newValue) {
        int v = newValue ? 1 : 0;
        unsafe.putOrderedInt(this, valueOffset, v);
    }

    /**
     * Atomically sets to the given value and returns the previous value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final boolean getAndSet(boolean newValue) {
        boolean prev;
        do {
            prev = get();

            // 设置失败了就一直循环
        } while (!compareAndSet(prev, newValue));

        // 返回设置成功时的prev值.
        return prev;
    }

    public String toString() {
        return Boolean.toString(get());
    }
}

