package com.king.learn.collection.jdk8concurrent.threadpool;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main(String[] args) throws Exception {
        ExecutorCompletionService<String> pool = new ExecutorCompletionService<String>(
                Executors.newFixedThreadPool(5));
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 10; i++)
            pool.submit(new Call(i + ""));
        for (int i = 0; i < 10; i++) {
            Future<String> f = pool.poll(2000, TimeUnit.MILLISECONDS);
            if (null != f) {
                String res = f.get();
                list.add(res);
            } else {
                System.out.println("有一个元素poll超时");
            }
        }
        for (String aList : list) System.out.println(aList);
    }

    static class Call implements Callable<String> {
        String name;

        public Call(String name) {
            this.name = name;
        }

        @Override
        public String call() throws Exception {
            if (name.equals("1"))
                Thread.sleep(5000);
            Thread.sleep(1000);
            System.out.println(name + "任务结束");
            return name;
        }
    }
}