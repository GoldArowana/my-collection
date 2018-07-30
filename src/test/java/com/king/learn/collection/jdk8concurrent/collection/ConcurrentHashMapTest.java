package com.king.learn.collection.jdk8concurrent.collection;

public class ConcurrentHashMapTest {
    public static void main(String[] args) {
        MyConcurrentHashMap myConcurrentHashMap = new MyConcurrentHashMap();
        myConcurrentHashMap.put(1, 1);
        System.out.println(myConcurrentHashMap.get(1));
    }
}
