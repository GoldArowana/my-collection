package com.king.learn.collection.mycollection.bloomfilter.demo11.bloomfilter.hasher;

/**
 * @author Vanja Komadinovic
 * @author vanjakom@gmail.com
 */
public interface Hasher {
    public int[] hash(long key, int numberOfHashes);
}
