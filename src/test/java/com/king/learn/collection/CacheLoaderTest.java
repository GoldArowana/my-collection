package com.king.learn.collection;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2018/6/12 15:33
 * @since JDK 1.8
 */
public class CacheLoaderTest {
    private final static Integer KEY = 1000;
    private final static LinkedBlockingQueue<Integer> QUEUE = new LinkedBlockingQueue<>(1000);
    private LoadingCache<Integer, AtomicLong> loadingCache;

    public static void main(String[] args) throws InterruptedException {
        CacheLoaderTest cacheLoaderTest = new CacheLoaderTest();
        cacheLoaderTest.init();


        while (true) {

            try {
                Integer integer = QUEUE.poll(200, TimeUnit.MILLISECONDS);
                if (null == integer) {
                    break;
                }
                //TimeUnit.SECONDS.sleep(5);
                cacheLoaderTest.checkAlert(integer);
                System.out.printf("job running times=%s", integer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void init() throws InterruptedException {
        loadingCache = CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.SECONDS)

                .build(new CacheLoader<Integer, AtomicLong>() {
                    @Override
                    public AtomicLong load(Integer key) throws Exception {
                        return new AtomicLong(0);
                    }
                });


        for (int i = 10; i < 15; i++) {
            QUEUE.put(i);
        }
    }

    private void checkAlert(Integer integer) {
        try {

            //loadingCache.put(integer,new AtomicLong(integer));

            TimeUnit.SECONDS.sleep(5);


            System.out.printf("当前缓存值=%s,缓存大小=%s\n", loadingCache.get(KEY), loadingCache.size());
            System.out.printf("缓存的所有内容=%s\n", loadingCache.asMap().toString());
            loadingCache.get(KEY).incrementAndGet();

        } catch (ExecutionException e) {
            System.out.println("Exception" + e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}