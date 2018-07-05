package com.king.learn.concurrent.jdk.locks;

import org.junit.Test;

public class SemaphoreTest {
    @Test
    public void t() throws InterruptedException {
        MySemaphore semaphore = new MySemaphore(1, true);

        semaphore.acquire();
        System.out.println(1);
    }

    @Test
    public void t2() throws InterruptedException {
        MySemaphore semaphore = new MySemaphore(1, true);
        semaphore.acquire();
        semaphore.acquire();
        System.out.println(1);
    }

    @Test
    public void t3() throws InterruptedException {
        MySemaphore semaphore = new MySemaphore(1, true);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            semaphore.release();
        }).start();

        semaphore.acquire();
        semaphore.acquire();
        System.out.println(1);

    }

    @Test
    public void t4() {
        MySemaphore semaphore = new MySemaphore(1, true);
        semaphore.release(4);
        System.out.println(semaphore.availablePermits());
    }
}
