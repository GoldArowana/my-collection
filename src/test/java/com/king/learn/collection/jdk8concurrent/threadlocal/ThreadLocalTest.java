package com.king.learn.collection.jdk8concurrent.threadlocal;

import org.junit.Test;

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

    }
}
