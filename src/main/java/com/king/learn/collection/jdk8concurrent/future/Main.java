package com.king.learn.collection.jdk8concurrent.future;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class Main {
    public static void main(String[] args) throws Exception {
        //线程池
        ExecutorService executor = Executors.newFixedThreadPool(1); //使用线程池

        // 之前自己实现的future模式中的 Data data = client.request(4) 这句相当于下面这两行代码
        //1. Data data
        FutureTask<Integer> futureTask = new FutureTask<>(new RealData(4));
        //2. 这里相当于 client.request(4);
        executor.submit(futureTask);

        //这里可以用一个sleep代替对其他业务逻辑的处理
        Thread.sleep(0);

        // 获取真实数据
        try {
            System.out.println("数据=" + futureTask.get());
        } finally {
            executor.shutdown();
        }
    }
}

