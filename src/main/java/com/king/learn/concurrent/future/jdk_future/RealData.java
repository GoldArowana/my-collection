package com.king.learn.concurrent.future.jdk_future;

import java.util.concurrent.Callable;

public class RealData implements Callable<Integer> {
    private int data;

    public RealData(int data) {
        this.data = data * 10;
    }

    @Override
    public Integer call() {
        //利用sleep方法来表示真是业务是非常缓慢的
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return data;
    }
}
