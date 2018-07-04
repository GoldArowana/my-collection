package com.king.learn.concurrent.jdk.locks;

import java.util.Scanner;

public class Test {
    private static MyReentrantLock lock = new MyReentrantLock(true);
    private static Scanner scan = new Scanner(System.in);

    public static void main(String[] args) {
//        new Thread(() -> {
//            while (scan.hasNext()) {
//                String str = scan.next();
//                if (str.equals("lock")) {
//                    lock.lock();
//                    System.out.println("locked");
//                } else if (str.equals("unlock")) {
//                    lock.lock();
//                    System.out.println("unlocked");
//                } else {
//                    System.out.println("discard");
//                }
//            }
//        }).start();
//
//        lock.lock();
//        lock.unlock();

        MyCountDownLatch countDownLatch = new MyCountDownLatch(2);

        new Thread(() -> {
            countDownLatch.countDown();
        }).start();
        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
            System.out.println("down");
        }).start();
        try {
            Thread.sleep(2000);
            countDownLatch.await();
            System.out.println("adsf");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
