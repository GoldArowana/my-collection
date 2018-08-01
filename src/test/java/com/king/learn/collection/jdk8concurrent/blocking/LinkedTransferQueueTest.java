package com.king.learn.collection.jdk8concurrent.blocking;

public class LinkedTransferQueueTest {
    public static void main(String[] args) throws InterruptedException {
        LinkedTransferQueue queue = new LinkedTransferQueue();
        queue.put(1);
        queue.put(2);

//        new Thread(()->{
//            Object o = null;
//            try {
//                o = queue.take();
//                System.out.println(o);
//
//                o = queue.take();
//                System.out.println(o);
//
//                o = queue.take();
//                System.out.println(o);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();

        queue.transfer(3);

    }
}
