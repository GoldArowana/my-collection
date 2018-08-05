package com.king.learn.collection.mycollection.random;

import java.util.ArrayList;
import java.util.List;

/**
 * 线性同余法.  rand[n + 1] = (a * rand[n] + b) % length
 */
public class RandomNumber {
    private static final int a = 1664525;
    private static final int b = 1013904223;
    private static final int m = 0x7FFF_FFFF;
    private final int length;
    private int rand;

    public RandomNumber(int seed, int length) {
        this.rand = seed * (this.length = length);
    }

    public static void main(String[] args) {
        RandomNumber rr = new RandomNumber(333, 100);
        // 输出 10 个100以内的随机数.
        for (int i = 0; i < 10; i++) {
            System.out.println(rr.next());
        }

        System.out.println("****************************");

        // 输出10个0 或者 1. 相当于抛硬币.
        for (int i = 0; i < 100; i++) {
            System.out.print(rr.isTrue());
        }
    }

    private int rand() {
        return (this.rand = a * rand + b & m);// 先计算乘法, 再计算加法, 然后计算按位与
    }

    public int next() {
        return rand() % length;
    }

    /**
     * 随机数在 [0,length) 这个区间.
     * 比如 length = 100, 那么:
     * ----- next() 取到 0 ~ 49 的时候, 本方法会返回 false.
     * ----- next()取到 50 ~ 99 的时候, 本方法会返回 true
     */
    public boolean isTrue() {
        return next() >= (length / 2);
    }

    public List<Integer> getMany(int many) {
        return new ArrayList<Integer>(many) {{
            for (int i = 0; i < many; i++) add(next());
        }};
    }
}
