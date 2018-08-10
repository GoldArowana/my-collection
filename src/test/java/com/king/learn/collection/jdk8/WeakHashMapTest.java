package com.king.learn.collection.jdk8;

import org.junit.Test;

import java.lang.ref.WeakReference;

public class WeakHashMapTest {
    @Test
    public void weakReferenceTest() throws InterruptedException {
        String str = new String("Hello World !");
        WeakReference<String> r = new WeakReference<String>(str);

        // 弱引用对象（str）存在引用
        System.out.println("①Before GC：" + r.get());

        System.gc();
        Thread.sleep(100);
        System.out.println("①After GC：" + r.get());

        // 将 str 置空，表示其不存在引用
        str = null;
        System.out.println("②Before GC：" + r.get());
        System.gc();
        Thread.sleep(100);
        System.out.println("②After GC：" + r.get());
    }

    @Test
    public void weakHashMapTest() throws InterruptedException {
        WeakHashMap<Object, String> wmap = new WeakHashMap<>();
        Object a = new Object();
        wmap.put(a, "a");
        System.gc();
        Thread.sleep(100);
        System.out.println(wmap.size());
        // 输出结果：1
    }

    @Test
    public void weakHashMapTestt() throws InterruptedException {
        WeakHashMap<Object, String> wmap = new WeakHashMap<>();
        Object a = new Object();
        wmap.put(a, "a");
        System.gc();
        Thread.sleep(100);
        System.out.println(wmap.size());
        // 输出结果：0
    }

    @Test
    public void weakHashMapTest2() throws InterruptedException {
        WeakHashMap<Object, String> wmap = new WeakHashMap<>();
        wmap.put(new Object(), "a");
        System.gc();
        Thread.sleep(100);
        System.out.println(wmap.size());
        // 输出结果：0
    }

    @Test
    public void weakHashMapTest3() throws InterruptedException {
        WeakHashMap<Object, Object> wmap = new WeakHashMap<>();
        Object a = new Object();
        wmap.put(new Object(), a);
        System.gc();
        Thread.sleep(100);
        System.out.println(wmap.size());
        // 输出结果：0
    }

    @Test
    public void weakHashMapTest4() throws InterruptedException {
        WeakHashMap<Object, String> wmap = new WeakHashMap<>();
        wmap.put(null, "a");
        System.gc();
        Thread.sleep(100);
        System.out.println(wmap.size());
        // 输出结果：1
    }

    @Test
    public void weakHashMapTest5() throws InterruptedException {
        WeakHashMap<Object, String> wmap = new WeakHashMap<>();
        wmap.put(new Object(), null);
        System.gc();
        Thread.sleep(100);
        System.out.println(wmap.size());
        System.out.println(wmap.get(new Object()));
        // 输出结果：0
    }
}
