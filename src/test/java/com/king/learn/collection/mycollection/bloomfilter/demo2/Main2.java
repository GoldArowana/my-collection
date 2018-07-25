package com.king.learn.collection.mycollection.bloomfilter.demo2;

import com.king.learn.collection.mycollection.bloomfilter.demo2.impl.BSBFDeDuplicator;

public class Main2 {
    private static final long NUM_BITS = 8 * 8L * 1024L * 1024L;

    public static void main(String[] args) {
        ProbabilisticDeDuplicator deDuplicator = new BSBFDeDuplicator(NUM_BITS, 5);
        System.out.println(deDuplicator.numBits());
        System.out.println(deDuplicator.numHashFunctions());
        System.out.println(deDuplicator.classifyDistinct("Hello".getBytes()));
        System.out.println(deDuplicator.classifyDistinct("Hello".getBytes()));
        System.out.println(deDuplicator.peekDistinct("World".getBytes()));
        System.out.println(deDuplicator.peekDistinct("World".getBytes()));
        deDuplicator.reset();
    }
}
