package com.king.learn.collection.mycollection.cache.lru;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;


public class MySynchronizedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = -2108033306317724707L;
    protected ReentrantLock lock;

    @Getter
    @Setter
    protected int maxSize;

    // 用于clone() 方法.
    protected Function<Map.Entry, Object> getValue = Map.Entry::getValue;

    public MySynchronizedLinkedHashMap() {
        super(16, 0.75f, true);
        this.maxSize = Integer.MAX_VALUE;
        this.lock = new ReentrantLock();
    }

    public MySynchronizedLinkedHashMap(int maxSize) {
        super(16, 0.75f, true);
        this.maxSize = maxSize;
        this.lock = new ReentrantLock();
    }

    public MySynchronizedLinkedHashMap(int maxSize, ReentrantLock lock) {
        super(16, 0.75f, true);
        this.maxSize = maxSize;
        this.lock = lock;
    }

    @Override
    public V put(K key, V value) {
        return synchronize(() -> super.put(key, value));
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return synchronize(() -> super.putIfAbsent(key, value));
    }

    @Override
    public V get(final Object key) {
        return synchronize(() -> super.get(key));
    }

    private V synchronize(Supplier<V> supplier) {
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(final Object key) {
        return synchronize(() -> super.remove(key));
    }

    public V removeUnEvict(final Object key) {
        return synchronize(() -> super.remove(key));
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<K, Object> clone() {
        Map<K, V> map;
        lock.lock();
        try {
            map = (Map<K, V>) super.clone();
        } finally {
            lock.unlock();
        }
        Iterator<Map.Entry<K, V>> iter = map.entrySet().iterator();
        Map<K, Object> result = new HashMap<>(map.size());
        while (iter.hasNext()) {
            Map.Entry<K, V> entry = iter.next();
            result.put(entry.getKey(), getValue(entry));
        }
        return result;
    }

    private Object getValue(Map.Entry<K, V> entry) {
        return getValue.apply(entry);
    }
}