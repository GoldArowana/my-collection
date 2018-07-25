package com.king.learn.collection.mycollection.bloomfilter.demo12.cachesketch;

import com.king.learn.collection.mycollection.bloomfilter.demo12.BloomFilter;
import com.king.learn.collection.mycollection.bloomfilter.demo12.FilterBuilder;
import com.king.learn.collection.mycollection.bloomfilter.demo12.TimeMap;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

public class ExpiringBloomFilterRedis<T> extends AbstractExpiringBloomFilterRedis<T> {
    private final ExpirationQueue<T> queue;

    public ExpiringBloomFilterRedis(FilterBuilder builder) {
        super(builder);

        // Init expiration queue which removes elements from Bloom filter if entry expires
        this.queue = new ExpirationQueueMemory<>(this::onExpire);
    }

    @Override
    protected void addToQueue(T element, long remaining, TimeUnit timeUnit) {
        queue.addExpiration(element, now() + remaining, timeUnit);
    }

    @Override
    public void clear() {
        try (Jedis jedis = pool.getResource()) {
            // Clear CBF, Bits, and TTLs
            jedis.del(keys.COUNTS_KEY, keys.BITS_KEY, keys.TTL_KEY);
            // During init, ONLY clear CBF
            if (queue == null) {
                return;
            }
            // Clear Queue
            queue.clear();
        }
    }

    @Override
    public BloomFilter<T> getClonedBloomFilter() {
        return toMemoryFilter();
    }

    @Override
    public boolean setExpirationEnabled(boolean enabled) {
        return queue.setEnabled(enabled);
    }

    @Override
    public TimeMap<T> getExpirationMap() {
        return queue.getExpirationMap();
    }

    @Override
    public void setExpirationMap(TimeMap<T> map) {
        queue.setExpirationMap(map);
    }

    /**
     * Handler for the expiration queue, removes entry from Bloom filter
     *
     * @param entry The entry which expired in the ExpirationQueue
     */
    private void onExpire(ExpirationQueue.ExpiringItem<T> entry) {
        this.removeAndEstimateCount(entry.getItem());
    }
}
