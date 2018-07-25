package com.king.learn.collection.mycollection.bloomfilter.demo8;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by jeff on 14/05/16.
 */
public class BloomFilterTest {

    private BloomFilter<String> filter;

    @Before
    public void before() {
        this.filter = BloomFilterBuilder.get().buildFilter();
    }

    @Test
    public void whenAskedIfContainsAddedObject_returnsTrue() {
        String string = "somestr";

        filter.add(string);
        boolean isContained = filter.mightContain(string);

        assertTrue(isContained);
    }

    // This test is not valid for arbitrary values since Bloom filters can yield false positives.
    // For this special case it does work though.
    @Test
    public void whenAskedIfContainsNotAddedObject_returnsFalse() {
        String string1 = "somestr";
        String string2 = "someotherstr";
        assertNotEquals(string1, string2);

        filter.add(string1);
        boolean isStr2Contained = filter.mightContain(string2);

        assertFalse(isStr2Contained);
    }

}
