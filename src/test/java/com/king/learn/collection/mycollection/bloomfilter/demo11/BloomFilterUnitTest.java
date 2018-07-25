package com.king.learn.collection.mycollection.bloomfilter.demo11;

import com.king.learn.collection.mycollection.bloomfilter.demo11.bloomfilter.BloomFilter;
import com.king.learn.collection.mycollection.bloomfilter.demo11.bloomfilter.hasher.Hasher;
import com.king.learn.collection.mycollection.bloomfilter.demo11.bloomfilter.hasher.RandomHasher;
import com.king.learn.collection.mycollection.bloomfilter.demo11.bloomfilter.hasher.RepeatedMurmurHasher;
import com.king.learn.collection.mycollection.bloomfilter.demo11.bloomfilter.hasher.StringHasher;
import com.king.learn.collection.mycollection.bloomfilter.demo11.utils.Base64Utils;
import com.king.learn.collection.mycollection.bloomfilter.demo11.utils.BloomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;

/**
 * @author Vanja Komadinovic
 * @author vanjakom@gmail.com
 */
public class BloomFilterUnitTest {
    protected static int numberOfElements = 100000;
    protected static int bitsetSize = 800000;

    @Test
    public void testWithRandomHasher() {
        testWithHasher(new RandomHasher());
    }

    @Test
    public void testWithRepeatedMurmurHasher() {
        testWithHasher(new RepeatedMurmurHasher());
    }

    @Test
    public void testWithStringHasher() {
        testWithHasher(new StringHasher());
    }

    @Test
    public void testSerialization() {
        BloomFilter filter = new BloomFilter(bitsetSize, numberOfElements, new RandomHasher());

        HashSet<Long> ids = new HashSet<Long>();

        BloomUtils.fillWithRandom(filter, ids);

        byte[] serialized = null;

        try {
            serialized = filter.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
//            Assert.fail("Unable to serialize filter", e);
        }

        filter = new BloomFilter(bitsetSize, numberOfElements, new RandomHasher());

        try {
            filter.setBytes(serialized);
        } catch (Exception e) {
            e.printStackTrace();
//            Assert.fail("Unable to deserialize filter", e);
        }

        try {
            BloomUtils.checkIfExists(filter, ids);
        } catch (Exception e) {
            e.printStackTrace();
//            Assert.fail(e.getMessage(), e);
        }
    }

    @Test
    public void testStringSerialization() {
        BloomFilter filter = new BloomFilter(bitsetSize, numberOfElements, new RandomHasher());

        HashSet<Long> ids = new HashSet<Long>();

        BloomUtils.fillWithRandom(filter, ids);

        byte[] serialized = null;

        try {
            serialized = filter.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
//            Assert.fail("Unable to serialize filter", e);
        }

        String serializedS = Base64Utils.fromBytes(serialized);
        byte[] deserialized = Base64Utils.fromString(serializedS);

        Assert.assertEquals(serialized, deserialized);

        filter = new BloomFilter(bitsetSize, numberOfElements, new RandomHasher());

        try {
            filter.setBytes(deserialized);
        } catch (Exception e) {
            e.printStackTrace();
//            Assert.fail("Unable to deserialize filter", e);
        }

        try {
            BloomUtils.checkIfExists(filter, ids);
        } catch (Exception e) {
            e.printStackTrace();
//            Assert.fail(e.getMessage(), e);
        }
    }

    protected void testWithHasher(Hasher hasher) {
        BloomFilter filter = new BloomFilter(bitsetSize, numberOfElements, hasher);

        HashSet<Long> ids = new HashSet<Long>();

        BloomUtils.fillWithRandom(filter, ids);

        try {
            BloomUtils.checkIfExists(filter, ids);
        } catch (Exception e) {
            e.printStackTrace();
//            Assert.fail(e.getMessage(), e);
        }
    }
}
