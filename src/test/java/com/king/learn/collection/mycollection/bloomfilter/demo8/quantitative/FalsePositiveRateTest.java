package com.king.learn.collection.mycollection.bloomfilter.demo8.quantitative;

import com.king.learn.collection.mycollection.bloomfilter.demo8.BloomFilter;
import com.king.learn.collection.mycollection.bloomfilter.demo8.BloomFilterBuilder;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeff on 16/05/16.
 */
public class FalsePositiveRateTest {

    private static List<String> randomStrings(String prefix, int count) {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String value = prefix + RandomStringUtils.randomAlphanumeric(7);
            strings.add(value);
        }
        return strings;
    }

    @Test
    public void whenContinuouslyAddingElements_falsePositivesIncrease() {
        BloomFilter<String> filter = BloomFilterBuilder.get()
                .withSize(10000)
                .buildFilter();
        final int batchSize = 1000;
        final int numberOfBatches = 10;

        for (int currentBatchNumber = 0; currentBatchNumber < numberOfBatches; currentBatchNumber++) {
            List<String> containedStrings = randomStrings("a", batchSize);
            List<String> nonContainedStrings = randomStrings("b", batchSize);

            containedStrings.forEach(filter::add);

            long truePositives = containedStrings.stream()
                    .filter(filter::mightContain)
                    .count();
            long trueNegatives = nonContainedStrings.stream()
                    .filter(string -> !filter.mightContain(string))
                    .count();
            double falsePositiveRate = 100.0 * (batchSize - trueNegatives) / batchSize;
            double falseNegativeRate = 100.0 * (batchSize - truePositives) / batchSize;
            double accuracy = 100.0 * (truePositives + trueNegatives) / (2 * batchSize);

            System.err.printf(
                    "N:%6d : FPR:%.2f%% FNR:%.2f%% ACC:%.2f%%\n",
                    (currentBatchNumber * batchSize),
                    falsePositiveRate,
                    falseNegativeRate,
                    accuracy);
        }
    }

}
