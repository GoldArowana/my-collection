package com.king.learn.collection.jdk8concurrent.locks;

import java.util.Scanner;
import java.util.function.Supplier;

public class ReentrantLockTest3 {
    static final Scanner scanner = new Scanner(System.in);
    static volatile String cmd = "";
    private static MyReentrantLock lock = new MyReentrantLock(true);

    public static void main(String[] args) {
        for (String name : new String[]{"1", "2", "3", "4", "5", "6"})
            new Thread(() -> func(() -> lock, name)).start();

        while (scanner.hasNext()) {
            cmd = scanner.nextLine();
        }
    }

    public static void func(Supplier<MyLock> myLockSupplier, String name) {
        blockUntilEquals(() -> cmd, "lock " + name);
        myLockSupplier.get().lock();
        System.out.println("获取了" + name + "号锁");
        blockUntilEquals(() -> cmd, "unlock " + name);
        myLockSupplier.get().unlock();
        System.out.println("释放了" + name + "号锁");
    }

    private static void blockUntilEquals(Supplier<String> cmdSupplier, final String expect) {
        while (!cmdSupplier.get().equals(expect))
            quietSleep(1000);
    }

    private static void quietSleep(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
