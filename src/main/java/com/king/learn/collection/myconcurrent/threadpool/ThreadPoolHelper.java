package com.king.learn.collection.myconcurrent.threadpool;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolHelper {
    public static final String DataPersistence = "dataPersistence";
    public static final String Common = "common";
    private static ConcurrentHashMap<String, ExecutorService> threadMap = new ConcurrentHashMap<>();

    public static ExecutorService getThreadPool(String name, int size) {
        ExecutorService pool = Executors.newFixedThreadPool(size);
        ExecutorService old = threadMap.putIfAbsent(name, pool);
        if (old != null) {
            pool.shutdown();
        }
        return threadMap.get(name);
    }

    public static ExecutorService getThreadPool(String name) {
        return getThreadPool(name, Runtime.getRuntime().availableProcessors());
    }

    public static ExecutorService getCommonThreadPool() {
        return getThreadPool(Common, Runtime.getRuntime().availableProcessors());
    }

    public static ExecutorService getDataPersistenceThreadPool() {
        int size = Runtime.getRuntime().availableProcessors() / 2;
        if (size <= 0) {
            size = 1;
        }
        return getThreadPool(DataPersistence, size);
    }
}
