package com.king.learn.collection.jdk8.reference.weak.demo0;

import java.lang.ref.WeakReference;

public class Main {
    public static void main(String[] args) {
        Integer a = new Integer(111);
        String b = new String("222");
        Node wrc = new Node(a, b);
        a = null;
        System.gc();
        int i = 0;
        while (true) {
            if (wrc.get() != null) {
                i++;
                System.out.println("WeakReferenceCar's Car is alive for " + i + ", loop - " + wrc);
            } else {
                System.out.println("WeakReferenceCar's Car has bean collected");
                break;
            }
        }
    }
}

class Node<K, V> extends WeakReference<Object> {
    V value;

    public Node(K key, V value) {
        super(key);
        this.value = value;
    }
}