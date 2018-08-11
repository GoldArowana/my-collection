package com.king.learn.collection.jdk8.reference.soft.demo0;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * VM Options: -Xms2m -Xmx2m
 */
public class Main {
    public static void main(String[] args) {
        Dog<String> dog = new Dog<>(new String("1234321"));
        System.out.println("内存溢出之前: " + dog.get()); // 1234321

        try {
            for (List<Object> arrayList = new ArrayList<>(); ; )
                arrayList.add(new Object());
        } catch (Throwable e) {
            System.out.println("内存溢出时:" + dog.get()); // null
        }
    }

    static class Dog<V> extends SoftReference<Object> {
        public Dog(V value) {
            super(value);
        }
    }
}
