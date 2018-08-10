package com.king.learn.collection;

import java.util.ArrayList;
import java.util.List;

/**
 * -Xms6m -Xmx6m -XX:MaxMetaspaceSize=10m -XX:MetaspaceSize=10m
 */
public class PermOOM {
    public static void main(String[] args) throws InterruptedException {
        List<String> list = new ArrayList<>();
        int i = 0;
        while (true) {
            list.add(String.valueOf(i++).intern());
            Thread.sleep(10);
        }
    }
}