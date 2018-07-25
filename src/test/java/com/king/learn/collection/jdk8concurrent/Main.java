package com.king.learn.collection.jdk8concurrent;

public class Main {
    private volatile int[] arr = new int[10];

    public static void main(String[] args) throws Exception {
        new Main().get();
    }

    public void get() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                arr[0]++;
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                arr[0]++;
            }
        });
        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                arr[0]++;
            }
        });
        Thread t4 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                arr[0]++;
            }
        });
        Thread t5 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                arr[0]++;
            }
        });


        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();

        Thread.sleep(4000);
        arr = new int[10];

        System.out.println(arr[0]);
    }
}
