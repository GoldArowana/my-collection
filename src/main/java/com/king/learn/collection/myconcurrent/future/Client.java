package com.king.learn.collection.myconcurrent.future;

public class Client {
    public Data request(final int num) {
        // 当有请求的时候, 先创建一个虚拟对象.
        final FutureData futureData = new FutureData();

        // 然后开启一个新线程去创建RealData, 当RealData创建完成后, 绑定带FutureData里.
        new Thread(() -> {
            RealData realData = new RealData(num);
            futureData.setRealData(realData);
        }).start();

        // 不管RealData有没有创建完成, 都会直接返回这个FutureData.
        return futureData;
    }
}

