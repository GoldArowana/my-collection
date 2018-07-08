package com.king.learn.collection.mycollection.spin;

public class SpinLockDemo implements Runnable {

    SpinLock spin = null;
    String name = null;

    SpinLockDemo(SpinLock spin, String tname) {
        this.spin = spin;
        this.name = tname;
    }

    public static void main(String args[]) {

        SpinLock spin = new SpinLock();
        Thread t1 = new Thread(new SpinLockDemo(spin, "T1"));
        Thread t2 = new Thread(new SpinLockDemo(spin, "T2"));

        System.out.println("Name Main id " + Thread.currentThread().getId());
        spin.lock();
        t1.start();
        t2.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        spin.release();
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        System.out.println("Name " + name + " id " + Thread.currentThread().getId());
        spin.lock();
        for (int i = 0; i < 2; i++) {
            System.out.println("i= " + i + " Thread Id = " + Thread.currentThread().getId());
        }
        System.out.println();

        spin.release();
    }

}
