package com.king.learn.collection.jdk8concurrent.locks;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

public class ReentrantReadWriteLockTest2 {
    static final Scanner scanner = new Scanner(System.in);
    static volatile String cmd = "";
    private static MyReentrantReadWriteLock lock = new MyReentrantReadWriteLock(true);
    private static MyReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private static MyReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public static void main(String[] args) {
        for (Map.Entry<String, Lock> entry : new HashMap<String, Lock>() {{
            for (int i = 0; i < 10; i++) {
                put("r" + i, readLock);
                put("w" + i, writeLock);
            }
        }}.entrySet()) {
            new Thread(() -> func(entry::getValue, entry.getKey())).start();
        }

        while (scanner.hasNext()) {
            cmd = scanner.nextLine();
        }
    }

    public static void func(Supplier<Lock> myLockSupplier, String name) {
        String en_type = myLockSupplier.get().getClass().getSimpleName().toLowerCase().split("lock")[0];
        String zn_type = (en_type.equals("read") ? "读" : "写");
        blockUntilEquals(() -> cmd, "lock " + en_type + " " + name);
        myLockSupplier.get().lock();
        System.out.println(name + "获取了" + zn_type + "锁");
        blockUntilEquals(() -> cmd, "unlock " + en_type + " " + name);
        myLockSupplier.get().unlock();
        System.out.println(name + "释放了" + zn_type + "锁");
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
