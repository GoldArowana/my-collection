package com.king.learn.collection.mycollection.bloomfilter.demo9.profiling;

public interface Profilable {
    // marker interface
    public void putValue(Object key, Object t);

    public boolean test(Object t);
}
