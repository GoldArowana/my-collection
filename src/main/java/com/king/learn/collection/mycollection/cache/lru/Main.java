package com.king.learn.collection.mycollection.cache.lru;

public class Main {
    public static void main(String[] args) {
        LRUHashMap<String, String> map = new LRUHashMap<>(10, 5000);
        for (int i = 0; i < 11; i++) {
            map.put("key" + i, "value" + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
