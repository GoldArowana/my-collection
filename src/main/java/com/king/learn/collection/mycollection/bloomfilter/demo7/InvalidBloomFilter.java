package com.king.learn.collection.mycollection.bloomfilter.demo7;

import java.io.IOException;

/**
 * Thrown when we encounter an invalid bloom filter (unrecognized version, truncated, corrupted, etc).
 */
public class InvalidBloomFilter extends IOException {
    public InvalidBloomFilter(String s) {
        super(s);
    }
}
