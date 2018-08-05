package com.king.learn.collection.mycollection.cache.lru;

public class TestSync {
    public static void main(String[] args) {
        MySynchronizedLinkedHashMap map = new MySynchronizedLinkedHashMap();
        for (int i = 0; i < 100; i++) {
            final int j = i;
            new Thread(() -> map.put(j, j)).start();
        }
    }
}
