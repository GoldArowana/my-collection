package com.king.learn.collection.mycollection.skiplist.inst3;

public class Main {
    public static void main(String[] args) {
        SkipList testList = new SkipList<Integer>();
        System.out.println(testList);
        testList.add(4);
        System.out.println(testList);
        testList.add(1);
        System.out.println(testList);
        testList.add(2);
        System.out.println(testList);
        testList.contains(4);
        testList = new SkipList<String>();
        System.out.println(testList);
        testList.add("hello");
        System.out.println(testList);
        testList.add("beautiful");
        System.out.println(testList);
        testList.add("world");
        System.out.println(testList);
    }
}
