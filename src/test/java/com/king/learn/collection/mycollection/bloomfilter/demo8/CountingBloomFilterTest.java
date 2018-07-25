package com.king.learn.collection.mycollection.bloomfilter.demo8;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Created by jeff on 14/05/16.
 */
public class CountingBloomFilterTest {

    private CountingBloomFilter<String> filter;

    @Before
    public void before() {
        this.filter = BloomFilterBuilder.get().buildCountingFilter();
    }

    @Test
    public void whenAskedIfContainsDeletedObject_returnsFalse() {
        String string = "somestr";

        filter.add(string);
        filter.remove(string);

        boolean containsString = filter.mightContain(string);
        assertFalse(containsString);
    }

}
