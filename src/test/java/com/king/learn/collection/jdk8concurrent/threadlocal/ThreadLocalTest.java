package com.king.learn.collection.jdk8concurrent.threadlocal;

import org.junit.Test;

import java.lang.reflect.Field;

public class ThreadLocalTest {

    private final static ThreadLocal<String> RESOURCE_1 = new ThreadLocal<String>();

    private final static ThreadLocal<String> RESOURCE_2 = new ThreadLocal<String>();

    public static void setOne(String value) {
        RESOURCE_1.set(value);
    }

    public static void setTwo(String value) {
        RESOURCE_2.set(value);
    }

    public static void display() {
        System.out.println("我的线程号是:" + Thread.currentThread().getId() + "\t我看到的两个数据结果是" + RESOURCE_1.get()
                + ":" + RESOURCE_2.get());
    }

    public static void main(String[] args) {
        new Thread(() -> {
            setOne("1");
            setTwo("2");

            new Thread(() -> {
                setOne("11");
                setTwo("22");
                display();
            }).start();

            mySleep(2000);

            display();
        }).start();
    }

    public static void mySleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void t() throws Exception {
        class A {
            int a;
            int b;

            public A(int a, int b) {
                this.a = a;
                this.b = b;
            }

            @Override
            public int hashCode() {
                return Integer.hashCode(a);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof A
                        && ((A) obj).a == this.a
                        && ((A) obj).b == this.b) {
                    return true;
                }
                return false;
            }
        }
        ThreadLocal<A> threadLocal = new ThreadLocal();
        threadLocal.set(new A(1, 111));
        Field f = ThreadLocal.class.getDeclaredField("HASH_INCREMENT");
        f.setAccessible(true);
        int inc = (int) f.get(null);
        System.out.println(Integer.toHexString(inc));

        new Thread(() -> {
            threadLocal.set(new A(1, 112));
            Field f2 = null;
            try {
                f2 = ThreadLocal.class.getDeclaredField("HASH_INCREMENT");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            f2.setAccessible(true);
            int inc2 = 0;
            try {
                inc2 = (int) f2.get(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            System.out.println(Integer.toHexString(inc2));
        }).start();

    }
}
