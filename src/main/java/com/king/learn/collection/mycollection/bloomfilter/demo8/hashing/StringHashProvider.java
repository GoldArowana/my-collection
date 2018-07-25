package com.king.learn.collection.mycollection.bloomfilter.demo8.hashing;

/**
 * Created by jeff on 16/05/16.
 */
public class StringHashProvider extends AbstractHashProvider<String> {

    @Override
    public byte[] toByteArray(String element) {
        return element.getBytes();
    }

}
