package com.king.learn.collection.myconcurrent.c.edu.vt.ece.bench;

public class Foo_Bar_SeperateBarriers extends Thread {
    public static int THREAD_COUNT;
    static int[] b = new int[THREAD_COUNT];
    static int i;
    private static int ID_GEN = 0;
    private Thread t;
    private int id;
    private barrier1 a;

    public Foo_Bar_SeperateBarriers(barrier1 a) {
        id = ID_GEN++;
        this.a = a;
    }

    public void run() {
        foo();
        a.TTASbarrier();
        while (barrier1.count < THREAD_COUNT) ;
        while (b[THREAD_COUNT - 1] != 2) {
            //System.out.println(i);
            if (Integer.parseInt(t.getName()) == i) {
                b[i] = 1;
                i++;
            }
            if (Integer.parseInt(t.getName()) == THREAD_COUNT - 1) {
                if (b[THREAD_COUNT - 1] == 1) {
                    b[THREAD_COUNT - 1] = 2;
                }
            }

        }

        bar();

    }

    public void foo() {
        System.out.println(id + "entered foo");
    }

    public void bar() {
        System.out.println(id + "entered bar");
    }

    public void start() {
        if (t == null) {
            id = ID_GEN++;
            t = new Thread(this, Integer.toString(id));
            t.start();
        }
    }
}
