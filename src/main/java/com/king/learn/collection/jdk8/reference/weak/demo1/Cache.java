package com.king.learn.collection.jdk8.reference.weak.demo1;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * https://stackoverflow.com/questions/1802809/javas-weakhashmap-and-caching-why-is-it-referencing-the-keys-not-the-values
 */
public class Cache<K, V> {
    ReferenceQueue<V> queue = null;
    Map<K, WeakReference<V>> values = null;
    Map<WeakReference<V>, K> keys = null;
    Thread cleanup = null;

    Cache() {
        queue = new ReferenceQueue<V>();
        keys = Collections.synchronizedMap(new HashMap<WeakReference<V>, K>());
        values = Collections.synchronizedMap(new HashMap<K, WeakReference<V>>());
        cleanup = new Thread() {
            public void run() {
                try {
                    for (; ; ) {
                        @SuppressWarnings("unchecked")
                        WeakReference<V> ref = (WeakReference<V>) queue.remove();
                        K key = keys.get(ref);
                        keys.remove(ref);
                        values.remove(key);
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        cleanup.setDaemon(true);
        cleanup.start();
    }

    static void gc(int loop, int delay) throws Exception {
        for (int n = loop; n > 0; n--) {
            Thread.sleep(delay);
            System.gc(); // <- obstinate donkey
        }
    }

    public static void main(String[] args) throws Exception {
        // Create the cache
        Cache<String, List> c = new Cache<String, List>();

        // Create some values
        List odd = Arrays.asList(new Object[]{1, 3, 5});
        List even = Arrays.asList(new Object[]{2, 4, 6});

        // Save them in the cache
        c.put("odd", odd);
        c.put("even", even);

        // Display the cache contents
        System.out.println(c);

        // Erase one value;
        odd = null;

        // Force garbage collection
        gc(10, 10);

        // Display the cache again
        System.out.println(c);

        // Stop cleanup thread
        c.stop();
    }

    void stop() {
        cleanup.interrupt();
    }

    V get(K key) {
        return values.get(key).get();
    }

    void put(K key, V value) {
        WeakReference<V> ref = new WeakReference<V>(value, queue);
        keys.put(ref, key);
        values.put(key, ref);
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("{");
        boolean first = true;
        for (Map.Entry<K, WeakReference<V>> entry : values.entrySet()) {
            if (first)
                first = false;
            else
                str.append(", ");
            str.append(entry.getKey());
            str.append(": ");
            str.append(entry.getValue().get());
        }
        str.append("}");
        return str.toString();
    }
}