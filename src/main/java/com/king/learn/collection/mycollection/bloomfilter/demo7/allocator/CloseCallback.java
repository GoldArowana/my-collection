package com.king.learn.collection.mycollection.bloomfilter.demo7.allocator;

/**
 * Called by the bloom filter after it is closed. Passes in the byte array underlying the cache.
 * Greplin uses it, internally, as a counter-part to the Allocator.
 */
public interface CloseCallback {
    void close(byte[] cache);
}
