package com.king.learn.collection.mycollection.bloomfilter.demo11.bloomfilter.hasher;

import com.king.learn.collection.mycollection.bloomfilter.demo11.RepeatedMurmurHash;

/**
 * @author Vanja Komadinovic
 * @author vanjakom@gmail.com
 */
public class RepeatedMurmurHasher implements Hasher {
    protected RepeatedMurmurHash greplinHasher = null;

    public RepeatedMurmurHasher() {
        greplinHasher = new RepeatedMurmurHash();
    }

    public int[] hash(long key, int numberOfHashes) {
        return greplinHasher.hash(("" + key).getBytes(), numberOfHashes);
    }
}
