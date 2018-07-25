/**
 * bloomfilter - Bloom filters for Java
 * Copyright (c) 2014-2015, Sandeep Gupta
 * <p>
 * http://sangupta.com/projects/bloomfilter
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.king.learn.collection.mycollection.bloomfilter.demo6;

import com.king.learn.collection.mycollection.bloomfilter.demo6.decompose.Decomposer;

import java.nio.charset.Charset;
import java.util.Collection;

/**
 * @author sangupta
 */
public abstract class DelegatingBloomFilter<T> implements BloomFilter<T> {

    protected final BloomFilter<T> originalBloomFilter;

    public DelegatingBloomFilter(BloomFilter<T> original) {
        this.originalBloomFilter = original;
    }

    /**
     */
    @Override
    public boolean add(byte[] bytes) {
        return this.originalBloomFilter.add(bytes);
    }

    /**
     */
    @Override
    public boolean add(T value) {
        return this.originalBloomFilter.add(value);
    }

    /**
     */
    @Override
    public boolean addAll(Collection<T> values) {
        return this.originalBloomFilter.addAll(values);
    }

    /**
     */
    @Override
    public boolean contains(byte[] bytes) {
        return this.originalBloomFilter.contains(bytes);
    }

    @Override
    public boolean contains(T value) {
        return this.originalBloomFilter.contains(value);
    }

    @Override
    public boolean containsAll(Collection<T> values) {
        return this.originalBloomFilter.containsAll(values);
    }

    @Override
    public void setCharset(String charsetName) {
        this.originalBloomFilter.setCharset(charsetName);
    }

    @Override
    public void setCharset(Charset charset) {
        this.originalBloomFilter.setCharset(charset);
    }

    @Override
    public Decomposer<T> getObjectDecomposer() {
        return this.originalBloomFilter.getObjectDecomposer();
    }

    @Override
    public int getNumberOfBits() {
        return this.originalBloomFilter.getNumberOfBits();
    }

    @Override
    public double getFalsePositiveProbability(int numInsertedElements) {
        return this.originalBloomFilter.getFalsePositiveProbability(numInsertedElements);
    }

    @Override
    public void close() {
        this.originalBloomFilter.close();
    }
}