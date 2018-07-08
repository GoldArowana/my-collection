package com.king.learn.collection.mycollection.future;

public class RealData implements Data {
    private int data;

    public RealData(int num) {
        //这里用sleep来模拟构造一个复杂对象的场景
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.data = num * 10;
    }

    @Override
    public int getResult() {
        return data;
    }
}

