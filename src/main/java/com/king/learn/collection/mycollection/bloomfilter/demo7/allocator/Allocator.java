package com.king.learn.collection.mycollection.bloomfilter.demo7.allocator;

/**
 * The interface for a byte array allocator. Used to allocate the byte array the BloomFilter uses in its cache.
 */
public interface Allocator {
    public byte[] apply(int size);
}
