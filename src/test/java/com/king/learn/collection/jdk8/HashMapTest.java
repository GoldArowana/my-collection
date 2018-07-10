package com.king.learn.collection.jdk8;

import org.junit.Test;

public class HashMapTest {
    @Test
    public void t() {
        HashMap map = new HashMap();
        map.put(1, 1);
        for (int i = 2; i <= 10; i++) {
            map.put(i, i);
        }
        map.put(11, 11);
        map.put(12, 12);
        map.put(13, 13);
    }
}
