package com.king.learn.collection.jdk8.reference.weak.demo0;

import java.lang.ref.WeakReference;

public class Main {
    public static void main(String[] args) {
        Integer a = new Integer(111);
        String b = new String("222");
        Node wrc = new Node(a, b);
        System.out.println("gc之前: " + wrc.get());
        System.out.println(wrc.value);
        a = null;
        System.gc();
        System.out.println("gc之后: " + wrc.get());
        System.out.println(wrc.value);

    }
}

class Node<K, V> extends WeakReference<Object> {
    V value;

    public Node(K key, V value) {
        super(key);
        this.value = value;
    }
}