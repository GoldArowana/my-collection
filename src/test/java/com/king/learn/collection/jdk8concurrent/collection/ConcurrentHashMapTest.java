package com.king.learn.collection.jdk8concurrent.collection;

public class ConcurrentHashMapTest {
    public static void main(String[] args) {
        MyConcurrentHashMap myConcurrentHashMap = new MyConcurrentHashMap();
        myConcurrentHashMap.put(1, 1);
        myConcurrentHashMap.put(2, 2);
        myConcurrentHashMap.put(3, 2);
        MyConcurrentHashMap.KeySetView s = myConcurrentHashMap.keySet(1);
        s.add(4);
        myConcurrentHashMap.forEach((k, v) -> {
            System.out.println(k + ":" + v);
        });

//        System.out.println(myConcurrentHashMap.get(1));
    }
}
