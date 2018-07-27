package com.king.learn.collection.jdk8concurrent.locks;

import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.function.Supplier;

public class ConditionTest {
    static final Scanner scanner = new Scanner(System.in);
    static volatile String cmd = "";
    private static MyReentrantLock lock = new MyReentrantLock(true);
    private static Condition condition = lock.newCondition();

    public static void main(String[] args) {
        for (String name : new String[]{"w1", "w2", "w3", "w4", "w5", "w6"})
            new Thread(() -> func(() -> lock, name)).start();
        new Thread(() -> signalOne(() -> lock, "s")).start();

        while (scanner.hasNext()) {
            cmd = scanner.nextLine();
        }
    }

    public static void func(Supplier<MyLock> myLockSupplier, String name) {
        blockUntilEquals(() -> cmd, name);
        myLockSupplier.get().lock();

        System.out.println(name + "阻塞等待...");
        try {
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("释放了" + name);

        myLockSupplier.get().unlock();
    }

    public static void signalOne(Supplier<MyLock> myLockSupplier, String name) {
        while (true) {
            blockUntilEquals(() -> cmd, name);
            myLockSupplier.get().lock();
            condition.signal();
            System.out.println("通知唤醒了一个等待...");
            myLockSupplier.get().unlock();
        }
    }

    private static void blockUntilEquals(Supplier<String> cmdSupplier, final String expect) {
        while (!cmdSupplier.get().equals(expect))
            quietSleep(1000);
        clearCmd();
    }

    private static void quietSleep(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void clearCmd() {
        cmd = "";
    }
}

