package com.king.learn.collection.jdk8concurrent.locks;

import java.util.Scanner;
import java.util.function.Supplier;

public class Main {
    static final Scanner scanner = new Scanner(System.in);
    static volatile String cmd = "";
    private static MyReentrantReadWriteLock lock = new MyReentrantReadWriteLock(true);
    private static MyReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private static MyReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public static void main(String[] args) {
        new Thread(Main::funcA).start();
        new Thread(Main::funcB).start();

        while (scanner.hasNext()) {
            cmd = scanner.nextLine();
        }
    }

    public static void funcA() {
        blockUntilEquals(() -> cmd, "lock a");
        readLock.lock();
        System.out.println("funcA获取了读锁");
        blockUntilEquals(() -> cmd, "unlock a");
        readLock.unlock();
        System.out.println("funcA释放了读锁");
    }

    public static void funcB() {
        blockUntilEquals(() -> cmd, "lock b");
        readLock.lock();
        System.out.println("funcB获取了读锁");
        blockUntilEquals(() -> cmd, "unlock b");
        readLock.unlock();
        System.out.println("funcB释放了读锁");
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
