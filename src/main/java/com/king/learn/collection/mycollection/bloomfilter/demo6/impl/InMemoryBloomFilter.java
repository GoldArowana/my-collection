package com.king.learn.collection.mycollection.bloomfilter.demo6.impl;

import com.king.learn.collection.mycollection.bloomfilter.demo6.AbstractBloomFilter;
import com.king.learn.collection.mycollection.bloomfilter.demo6.core.BitArray;
import com.king.learn.collection.mycollection.bloomfilter.demo6.core.JavaBitSetArray;

/**
 * An in-memory implementation of the bloom filter. Not suitable for
 * persistence.
 *
 * @param <T> the type of object to be stored in the filter
 * @author sangupta
 * @since 1.0
 */
public class InMemoryBloomFilter<T> extends AbstractBloomFilter<T> {

    /**
     * Constructor
     *
     * @param n   the number of elements expected to be inserted in the bloom
     *            filter
     * @param fpp the expected max false positivity rate
     */
    public InMemoryBloomFilter(int n, double fpp) {
        super(n, fpp);
    }

    /**
     * Used a normal {@link JavaBitSetArray}.
     */
    @Override
    protected BitArray createBitArray(int numBits) {
        return new JavaBitSetArray(numBits);
    }

}