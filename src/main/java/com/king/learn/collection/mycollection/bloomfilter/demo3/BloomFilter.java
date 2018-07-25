package com.king.learn.collection.mycollection.bloomfilter.demo3;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.Collection;

/**
 * Bloom Filter数据结构实现
 */
public class BloomFilter<E> {

    public static final Charset charset = Charset.forName("UTF-8");

    //在大多数情况下MD5准确率较好，也可以选择SHA1
    public static final String hashName = "MD5";
    public static final MessageDigest digestFunction;

    static {
        MessageDigest tmp;
        try {
            tmp = MessageDigest.getInstance(hashName);
        } catch (NoSuchAlgorithmException e) {
            tmp = null;
        }
        digestFunction = tmp;
    }

    protected BitSet bitset;
    protected int bitSetSize; //Bloom Filter的位数
    protected double c; //每个元素的位数
    protected int dataCount;  //Bloom Filter能容纳的元素个数的极限.(最多能存maximuExpectedCapacity个元素)
    protected int counter; // Bloom Filter实际元素的个数
    protected int funtions; // hash函数的个数

    public BloomFilter(double c, int dataCount, int funtions) {
        this.dataCount = dataCount;
        this.funtions = funtions;
        this.c = c;
        this.bitSetSize = (int) Math.ceil(c * dataCount);
        counter = 0;
        this.bitset = new BitSet(bitSetSize);
    }

    public BloomFilter(int bitSetSize, int dataCount, int funtions) {
        this.dataCount = dataCount;
        this.funtions = funtions;
        this.bitSetSize = bitSetSize;
        this.c = 1.0d * bitSetSize / dataCount;
        counter = 0;
        this.bitset = new BitSet(bitSetSize);
    }

    /**
     * 根据bitSetSize和n_max计算funs的最优值
     * 根据论文的推导：funtions = lg2*(bitSetSize/dataCount)
     */
    public BloomFilter(int bitSetSize, int dataCount) {
        this(bitSetSize / (double) dataCount,
                dataCount,
                (int) Math.round((bitSetSize / (double) dataCount) * Math.log(2.0))); //funtions = log2*(bitSetSize/dataCount)
    }


    /**
     * 最常用的构造方法
     */
    public BloomFilter(double fpp, int dataCount) {
        this(Math.ceil(-(Math.log(fpp) / Math.log(2))) / Math.log(2), // c = funtions / ln(2)
                dataCount,
                (int) Math.ceil(-(Math.log(fpp) / Math.log(2)))); // funtions = ceil(-lg_2(fpp))
    }

    /**
     * 根据Hash的个数，生成散列值
     */
    public static int[] createHashes(byte[] data, int hashes) {
        int[] result = new int[hashes];

        int k = 0;
        byte salt = 0;
        while (k < hashes) {
            byte[] digest;
            synchronized (digestFunction) {
                digestFunction.update(salt);
                salt++;
                digest = digestFunction.digest(data);
            }

            for (int i = 0; i < digest.length / 4 && k < hashes; i++) {
                int h = 0;
                for (int j = (i * 4); j < (i * 4) + 4; j++) {
                    h <<= 8;
                    h |= ((int) digest[j]) & 0xFF;
                }
                result[k] = h;
                k++;
            }
        }
        return result;
    }

    /**
     * 计算在插入最大元素的情况下的误判率
     */
    public double maxFpp() {
        return getFpp(dataCount);
    }

    /**
     * 根据当前的元素计算误判率
     */
    public double getFpp(double n) {
        // (1 - e^(-funtions * counter / bitSetSize)) ^ funtions
        return Math.pow((1 - Math.exp(-funtions * (double) n
                / (double) bitSetSize)), funtions);

    }

    /**
     * 计算当前元素个数的误判率
     */
    public double getFpp() {
        return getFpp(counter);
    }


    public int getFuntions() {
        return funtions;
    }

    /**
     * 重置Bloom Filter
     */
    public void clear() {
        bitset.clear();
        counter = 0;
    }

    /**
     * 添加对象到Bloom Filter中，会调用对象的toString()方法作为Hash方法的输入
     */
    public void add(E element) {
        add(element.toString().getBytes(charset));
    }

    /**
     * 添加字节数组到Bloom Filter中
     */
    public void add(byte[] bytes) {
        int[] hashes = createHashes(bytes, funtions);
        for (int hash : hashes)
            bitset.set(Math.abs(hash % bitSetSize), true); //使用K个Hash函数映射到1位
        counter++;//添加了一个元素
    }

    /**
     * 添加一个对象集合到Bloom Filter中
     */
    public void addAll(Collection<? extends E> c) {
        for (E element : c)
            add(element);
    }

    /**
     * 获取某个对象是否已经插入到Bloom Filter中，可以使用getFpp()方法计算结果正确的概率
     */
    public boolean contains(E element) {
        return contains(element.toString().getBytes(charset));
    }

    /**
     * 判定某个字节数组是否已经插入到Bloom Filter中，可以使用getFpp()方法计算结果正确的概率
     */
    public boolean contains(byte[] bytes) {
        int[] hashes = createHashes(bytes, funtions);
        for (int hash : hashes) {
            if (!bitset.get(Math.abs(hash % bitSetSize))) { //如果有一位未设置，则该元素未插入，但是返回true，并不代表这个元素一定插入过，即存在误判率的概念。
                return false;
            }
        }
        return true;
    }

    /**
     * 如果有一个元素未被插入到Bloom Filter中，则返回false
     */
    public boolean containsAll(Collection<? extends E> c) {
        for (E element : c)
            if (!contains(element))
                return false;
        return true;
    }

    /**
     * 获取Bloom Filter中某一位的值
     */
    public boolean getBit(int bit) {
        return bitset.get(bit);
    }

    /**
     * 设置Bloom Filter每一位的值
     *
     * @param value true代表该位已经被设置，false代表未进行设置
     */
    public void setBit(int bit, boolean value) {
        bitset.set(bit, value);
    }

    public BitSet getBitSet() {
        return bitset;
    }

    /**
     * 获取当前的位数
     */
    public int size() {
        return this.bitSetSize;
    }

    /**
     * 获取当前的插入的元素的个数
     */
    public int count() {
        return this.counter;
    }

    /**
     * 获取Bloom Filter可以插入的最大元素
     *
     * @return
     */
    public int getNMax() {
        return dataCount;
    }

    /**
     * 当Bloom Filter满的时候，每个元素占的位数，通过构造方法进行设置
     */
    public double getC() {
        return this.c;
    }

    /**
     * 获取当前情况下，Bloom Filter实际上每个元素占的位数
     */
    public double getBitsPerElement() {
        return this.bitSetSize / (double) counter;
    }
}