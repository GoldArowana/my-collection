package com.king.learn.collection.mycollection.queue.array_loop_queue;

public interface Queue<E> {

    int getSize();

    boolean isEmpty();

    void enqueue(E e);

    E dequeue();

    E getFront();
}
