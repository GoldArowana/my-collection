package com.king.learn.collection.mycollection.bloomfilter.demo2;

import com.king.learn.collection.mycollection.bloomfilter.demo2.impl.BSBFDeDuplicator;

public class Main {
    public static void main(String[] args) {
        final long NUM_BITS = 8 * 8L * 1024L * 1024L;
        ProbabilisticDeDuplicator deDuplicator = null;

// Creates a BSBFDeDuplicator with 8MB of RAM and false-positive probability at 3%.
//        deDuplicator = RLBSBFDeDuplicator.create(NUM_BITS, 0.03D);

// Creates a BSBFDeDuplicator with 8MB of RAM and 5 hashing functions..
//        deDuplicator = new RLBSBFDeDuplicator(NUM_BITS, 5);
        deDuplicator = new BSBFDeDuplicator(NUM_BITS, 5);

// The number of bits that the ProbabilisticDeDuplicator should use.
// Output: 67108864
        System.out.println(deDuplicator.numBits());

// The number of hash functions that the ProbabilisticDeDuplicator should use.
// Output: 5
        System.out.println(deDuplicator.numHashFunctions());

// Probabilistically classifies whether a given element is a distinct or a duplicate element.
// This operation does record the result into its history.
// Output: true
        System.out.println(deDuplicator.classifyDistinct("Hello".getBytes()));
// Output: false
        System.out.println(deDuplicator.classifyDistinct("Hello".getBytes()));

// Probabilistically peeks whether a given element is a distinct or a duplicate element.
// This operation does not record the result into its history.
// Output: true
        System.out.println(deDuplicator.peekDistinct("World".getBytes()));
// Output: true
        System.out.println(deDuplicator.peekDistinct("World".getBytes()));

// Version 0.1.2+: Calculate the probability that a distinct element of the stream is reported as duplicate.
// Output: Probability between 0 and 1.
//        System.out.println(deDuplicator.estimateFpp(actuallyDistinctProbability));
// Version 0.1.2+: Calculate the probability that a duplicate element of the stream is reported as distinct.
// Output: Probability between 0 and 1.
//        System.out.println(deDuplicator.estimateFnp(actuallyDistinctProbability));

// Reset the history of the ProbabilisticDeDuplicator.
        deDuplicator.reset();
    }
}
