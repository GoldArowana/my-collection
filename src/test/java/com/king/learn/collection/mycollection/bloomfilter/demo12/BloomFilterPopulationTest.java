package com.king.learn.collection.mycollection.bloomfilter.demo12;

import com.king.learn.collection.mycollection.bloomfilter.demo12.helper.Helper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.king.learn.collection.mycollection.bloomfilter.demo12.helper.Helper.createRedisFilter;
import static org.junit.Assert.assertTrue;


@RunWith(Parameterized.class)
public class BloomFilterPopulationTest {
    private static final String name = "concurrencytests";
    private final boolean redisBacked;
    private final boolean counting;
    private int defaultElements = 1_000;
    private double defaultError = 0.01;
    private BloomFilter<String> filter;

    public BloomFilterPopulationTest(String name, boolean redisBacked, boolean counting) {
        this.redisBacked = redisBacked;
        this.counting = counting;
    }

    @Parameterized.Parameters(name = "Bloom Filter test with {0}")
    public static Collection<Object[]> data() throws Exception {
        Object[][] data = {
                {"normal memory", false, false},
                {"counting memory", false, true},
                {"normal redis", true, false},
                {"counting redis", true, true}
        };
        return Arrays.asList(data);
    }

    private BloomFilter<String> createFilter(String name, int n, double p, HashProvider.HashMethod hm) {
        if (!redisBacked) {
            if (counting) {
                return Helper.createCountingFilter(n, p, hm);
            } else {
                return Helper.createFilter(n, p, hm);
            }
        } else {
            if (counting) {
                return Helper.createCountingRedisFilter(name, n, p, hm, true);
            } else {
                return createRedisFilter(name, n, p, hm, true);
            }
        }
    }

    public <T> void createFilter() {
        filter = createFilter("population tests", defaultElements, defaultError, HashProvider.HashMethod.Murmur3);
        filter.clear();
    }

    @Before
    public void testName() throws Exception {
        if (redisBacked)
            Helper.cleanupRedis();
        createFilter();
    }

    @Test
    public void emptyBF() {
        assertTrue(Math.round(filter.getEstimatedPopulation()) == 0);
    }

    @Test
    public void addTest() {
        boolean added = filter.add("SecondTest");

        assertTrue(added);
        assertTrue(Math.round(filter.getEstimatedPopulation()) == 1);
    }

    @Test
    public void testAddMany() throws Exception {
        int n = 200;

//        Randoms.INTS.generate(counter, 1).get(0).stream().map(Randoms::fromBytes).forEach(filter::add);

        assertTrue(n * 1.05 > filter.getEstimatedPopulation());
        assertTrue(n * 0.95 < filter.getEstimatedPopulation());
    }

    @Test
    public void duplicateTest() {
        boolean added = filter.add("FirstTest");

        assertTrue(added);
        assertTrue(Math.round(filter.getEstimatedPopulation()) == 1);

        added = filter.add("FirstTest");
        assertTrue(!added);
        assertTrue(Math.round(filter.getEstimatedPopulation()) == 1);

        added = filter.add("Secondtest");
        assertTrue(added);
        assertTrue(Math.round(filter.getEstimatedPopulation()) == 2);
    }

    @Test
    public void clearTest() {
        filter.add("FirstTest");
        filter.add("FirstTest");
        filter.add("SecondTest");
        filter.add("third");
        filter.add("fourth");
        filter.add("fifth");
        filter.add("sixth");

        filter.clear();
        assertTrue(Math.round(filter.getEstimatedPopulation()) == 0);
    }

    @Test
    public void addAllTestNoPopulation() {
        boolean added = filter.add("FirstTest");

        assertTrue(added);

        List<String> toAdd = new ArrayList<>();
        toAdd.add("SecondTest");
        toAdd.add("ThirdTest");

        filter.addAll(toAdd);

        toAdd.clear();
        toAdd.add("FourthTest");
        toAdd.add("FifthTest");
        List<Boolean> wereAdded = filter.addAll(toAdd);

        for (boolean wasAdded : wereAdded) {
            assertTrue(wasAdded);
        }

        toAdd.clear();
        toAdd.add("FifthTest");
        toAdd.add("SixthTest");

        wereAdded = filter.addAll(toAdd);

        assertTrue(!wereAdded.get(0));
        assertTrue(wereAdded.get(1));

        // do it again, should not change
        wereAdded = filter.addAll(toAdd);

        assertTrue(!wereAdded.get(0));
        assertTrue(!wereAdded.get(1));
    }

    @Test
    public void addAllTestWithPopulation() {
        boolean added = filter.add("FirstTest");

        assertTrue(added);
        assertTrue(Math.round(filter.getEstimatedPopulation()) == 1);

        List<String> toAdd = new ArrayList<>();
        toAdd.add("SecondTest");
        toAdd.add("ThirdTest");

        filter.addAll(toAdd);
        assertTrue(Math.round(filter.getEstimatedPopulation()) == 3);

        toAdd.clear();
        toAdd.add("FourthTest");
        toAdd.add("FifthTest");
        List<Boolean> wereAdded = filter.addAll(toAdd);

        for (boolean wasAdded : wereAdded) {
            assertTrue(wasAdded);
        }
        assertTrue(Math.round(filter.getEstimatedPopulation()) == 5);

        toAdd.clear();
        toAdd.add("FifthTest");
        toAdd.add("SixthTest");

        wereAdded = filter.addAll(toAdd);
        assertTrue(Math.round(filter.getEstimatedPopulation()) == 6);

        assertTrue(!wereAdded.get(0));
        assertTrue(wereAdded.get(1));

        // do it again, should not change
        wereAdded = filter.addAll(toAdd);
        assertTrue(Math.round(filter.getEstimatedPopulation()) == 6);

        assertTrue(!wereAdded.get(0));
        assertTrue(!wereAdded.get(1));
    }


}
