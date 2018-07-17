package com.king.learn.collection.mycollection.cache;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class LRUHashMap<K, V> {

    private static ScheduledExecutorService expireExecutor = Executors.newSingleThreadScheduledExecutor();

    private AtomicBoolean isCleanerRuning = new AtomicBoolean(false);

    private LRUContainerMap<K, TimestampEntryValue<V>> container;

    @Getter
    private long duration = -1;

    private ReentrantLock lock = new ReentrantLock();

    private Runnable expireRunnable = new Runnable() {
        @Override
        public void run() {
            long nextInterval = 1000;
            lock.lock();
            try {
                boolean shouldStopCleaner = true;
                if (container.size() > 0) {
                    long now = System.currentTimeMillis();
                    List<K> toBeRemoved = new ArrayList<>();
                    for (Entry<K, TimestampEntryValue<V>> e : container.entrySet()) {
                        K key = e.getKey();
                        TimestampEntryValue<V> tValue = e.getValue();
                        // 计算存活时间
                        long timeLapsed = now - tValue.timestamp;
                        // 如果 存活时间 >= 过期时间
                        if (timeLapsed >= duration) {
                            // 那么放进
                            toBeRemoved.add(key);
                        } else {
                            long delta = duration - timeLapsed;
                            if (delta > 1000L) {
                                nextInterval = delta;
                            }
                            System.out.println("break");
                            break;
                        }
                    }

                    if (toBeRemoved.size() > 0) {
                        for (K key : toBeRemoved) {
                            container.remove(key);
                            System.out.println(key);
                        }
                    }
                }

                if (container.size() > 0) {
                    expireExecutor.schedule(this, nextInterval, TimeUnit.MILLISECONDS);
                } else {
                    isCleanerRuning.compareAndSet(true, false);
                }

            } finally {
                lock.unlock();
            }
        }
    };

    /**
     * 不限大小, 没有过期时间
     */
    public LRUHashMap() {
        this(Integer.MAX_VALUE, -1L);
    }

    /**
     * 限制大小, 没有过期时间
     */
    public LRUHashMap(int maxSize) {
        this(maxSize, -1L);
    }

    /**
     * 设置大小, 设置过期时间.
     */
    public LRUHashMap(int maxSize, long duration) {
        this.duration = duration;
        container = new LRUContainerMap<>(maxSize, this.lock);
    }

    public int size() {
        return container.size();
    }

    int getMaxSize() {
        return container.getMaxSize();
    }

    void setMaxSize(int maxSize) {
        container.setMaxSize(maxSize);
    }

    public Set<K> getKeys() {
        return container.keySet();
    }

    public V put(K key, V value) {
        TimestampEntryValue<V> v = new TimestampEntryValue<>();
        v.timestamp = System.currentTimeMillis();
        v.setData(value);
        TimestampEntryValue<V> old = container.put(key, v);

        if (duration > 0) {
            if (isCleanerRuning.compareAndSet(false, true)) {
                expireExecutor.schedule(expireRunnable, duration, TimeUnit.MILLISECONDS);
            }
        }

        return old == null ? null : old.getData();
    }

    public V putIfAbsent(K key, V value) {
        TimestampEntryValue<V> v = new TimestampEntryValue<>();
        v.timestamp = System.currentTimeMillis();
        v.setData(value);
        TimestampEntryValue<V> old = container.putIfAbsent(key, v);

        if (old == null) {
            if (duration > 0) {
                if (isCleanerRuning.compareAndSet(false, true)) {
                    expireExecutor.schedule(expireRunnable, duration, TimeUnit.MILLISECONDS);
                }
            }
        }

        return old == null ? null : old.getData();
    }

    public boolean containsKey(Object key) {
        return container.containsKey(key);
    }

    public V get(Object key) {
        TimestampEntryValue<V> got = container.get(key);
        V ret = null;
        if (got != null) {
            got.timestamp = System.currentTimeMillis();
            ret = got.getData();
        }
        return ret;
    }

    public V remove(Object key, boolean doEvict) {
        TimestampEntryValue<V> removed;
        if (doEvict) {
            removed = container.remove(key);
        } else {
            removed = container.removeUnEvict(key);
        }

        V ret = null;
        if (removed != null) {
            ret = removed.getData();
        }
        return ret;
    }

    public V remove(Object key) {
        return remove(key, true);
    }

    @Override
    public Map<K, Object> clone() {
        return container.clone();
    }

    @Setter
    @Getter
    private static class TimestampEntryValue<V> {
        private V data;
        private long timestamp;
    }

    private static class LRUContainerMap<K, V extends TimestampEntryValue<?>> extends MySynchronizedLinkedHashMap<K, V> {
        private static final long serialVersionUID = -2108033306317724707L;

        // 用于clone() 方法.
        protected Function<V, Object> getValue = (entry) -> entry.getData();

        public LRUContainerMap(int maxSize) {
            super(maxSize);
        }

        public LRUContainerMap(int maxSize, ReentrantLock lock) {
            super(maxSize, lock);
        }
    }
}
