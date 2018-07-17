package com.king.learn.collection.cache;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author arowana
 */
public class LRUHashMap<K, V> {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 8660712027640838753L;

    private static ScheduledExecutorService expireExecutor = Executors.newSingleThreadScheduledExecutor();

    private AtomicBoolean isCleanerRuning = new AtomicBoolean(false);

    private LRUContainerMap<K, TimestampEntryValue<V>> container;
    private long duration = -1;
    private Runnable expireRunnable = new Runnable() {

        @Override
        public void run() {
            long nextInterval = 1000;
            container.lock();
            try {
                boolean shouldStopCleaner = true;
                if (container.size() > 0) {
                    long now = System.currentTimeMillis();
                    List<K> toBeRemoved = new ArrayList<>();
                    for (Entry<K, TimestampEntryValue<V>> e : container
                            .entrySet()) {
                        K key = e.getKey();
                        TimestampEntryValue<V> tValue = e.getValue();
                        long timeLapsed = now - tValue.timestamp;
                        if (timeLapsed >= duration) {
                            toBeRemoved.add(key);
                        } else {
                            long delta = duration - timeLapsed;
                            if (delta > 1000L) {
                                nextInterval = delta;
                            }
                            break;
                        }
                    }

                    if (toBeRemoved.size() > 0) {
                        for (K key : toBeRemoved) {
                            container.remove(key);
                        }
                    }

                    if (container.size() > 0) {
                        shouldStopCleaner = false;
                    }
                }

                if (shouldStopCleaner) {
                    isCleanerRuning.compareAndSet(true, false);
                } else {
                    expireExecutor.schedule(this, nextInterval, TimeUnit.MILLISECONDS);
                }

            } finally {
                container.unlock();
            }
        }
    };

    public LRUHashMap(int maxSize) {
        this(maxSize, -1L);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public LRUHashMap(int maxSize, long duration) {
        this.duration = duration;
        container = new LRUContainerMap(maxSize);

    }

    public static void main(String[] args) {
        LRUHashMap<String, String> map = new LRUHashMap<>(10, 5000);
        for (int i = 0; i < 11; i++) {
            map.put("key" + i, "value" + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

    public long getDuration() {
        return duration;
    }

    public V put(K key, V value) {
        TimestampEntryValue<V> v = new TimestampEntryValue<>();
        v.timestamp = System.currentTimeMillis();
        v.value = value;
        TimestampEntryValue<V> old = container.put(key, v);

        if (duration > 0) {
            if (isCleanerRuning.compareAndSet(false, true)) {
                expireExecutor.schedule(expireRunnable, duration,
                        TimeUnit.MILLISECONDS);
            }
        }

        return old == null ? null : old.value;
    }

    public V putIfAbsent(K key, V value) {
        TimestampEntryValue<V> v = new TimestampEntryValue<>();
        v.timestamp = System.currentTimeMillis();
        v.value = value;
        TimestampEntryValue<V> old = container.putIfAbsent(key, v);

        if (old == null) {
            if (duration > 0) {
                if (isCleanerRuning.compareAndSet(false, true)) {
                    expireExecutor.schedule(expireRunnable, duration,
                            TimeUnit.MILLISECONDS);
                }
            }
        }

        return old == null ? null : old.value;
    }

    public boolean containsKey(Object key) {
        return container.containsKey(key);
    }

    public V get(Object key) {
        TimestampEntryValue<V> got = container.get(key);
        V ret = null;
        if (got != null) {
            got.timestamp = System.currentTimeMillis();
            ret = got.value;
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
            ret = removed.value;
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

    private static class TimestampEntryValue<V> {
        public V value;
        public long timestamp;
    }

    private static class LRUContainerMap<K, V extends TimestampEntryValue<?>> extends LinkedHashMap<K, V> {
        private static final long serialVersionUID = -2108033306317724707L;
        private static ExecutorService pool = ThreadPoolHelper.getCommonThreadPool();
        private ReentrantLock lock = new ReentrantLock();

        private int maxSize;

        public LRUContainerMap(int maxSize) {
            super(16, 0.75f, true);
            this.maxSize = maxSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }

        @Override
        public V put(K key, V value) {
            lock();
            try {
                return super.put(key, value);
            } finally {
                unlock();
            }
        }

        @Override
        public V putIfAbsent(K key, V value) {
            lock();
            try {
                V result = super.get(key);
                if (result != null) {
                    return result;
                } else {
                    super.put(key, value);
                    return null;
                }
            } finally {
                unlock();
            }
        }

        @Override
        public V get(Object key) {
            lock.lock();
            try {
                return super.get(key);
            } finally {
                lock.unlock();
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public V remove(final Object key) {
            lock();
            try {
                return super.remove(key);
            } finally {
                unlock();
            }
        }

        public V removeUnEvict(final Object key) {
            lock();
            try {
                return super.remove(key);
            } finally {
                unlock();
            }
        }

        @Override
        protected boolean removeEldestEntry(final Entry<K, V> eldest) {
            return size() > maxSize;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<K, Object> clone() {
            Map<K, V> map;
            lock();
            try {
                map = (Map<K, V>) super.clone();
            } finally {
                unlock();
            }
            Iterator<Entry<K, V>> iter = map.entrySet().iterator();
            Map<K, Object> result = new HashMap<>(map.size());
            while (iter.hasNext()) {
                Entry<K, V> entry = iter.next();
                result.put(entry.getKey(), entry.getValue().value);
            }
            return result;
        }
    }
}
