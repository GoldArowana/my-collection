package com.king.learn.collection.mycollection.bloomfilter.demo3;

/**
 * 可以移除元素的Bloom Filter数据结构实现
 *
 * @author Letcheng on 2016/2/24.
 */
public class CountBloomFilter<E> extends BloomFilter<E> {

    protected int count[];
    protected int c_m = 0; // 当前使用的位数

    public CountBloomFilter(int m, int n_max, int k) {
        super(m, n_max, k);
        this.count = new int[super.bitSetSize];
    }

    public CountBloomFilter(double fpp, int n_max) {
        super(fpp, n_max);
        this.count = new int[super.bitSetSize];
    }

    @Override
    public void add(byte[] bytes) {
        int[] hashes = createHashes(bytes, funtions);
        for (int hash : hashes) {
            bitset.set(Math.abs(hash % bitSetSize), true); //使用K个Hash函数映射到1位
            if (count[Math.abs(hash % bitSetSize)] == 0) {
                c_m++;
            }
            count[Math.abs(hash % bitSetSize)]++;
        }
        counter++;//添加了一个元素
    }

    /**
     * 移除元素
     *
     * @param element
     */
    public void remove(E element) {
        if (element != null) {
            remove(element.toString().getBytes(charset));
        }
    }

    /**
     * 移除字节数组
     *
     * @param bytes
     */
    public void remove(byte[] bytes) {
        int[] hashes = createHashes(bytes, funtions);
        for (int hash : hashes) {
            if (--count[Math.abs(hash % bitSetSize)] == 0) { //如果数据为空，则将标志位也归位
                c_m--;
                bitset.clear(Math.abs(hash % bitSetSize));
            }
        }
        counter--;
    }

    @Override
    public void clear() {
        this.count = new int[super.bitSetSize];
        super.clear();
    }

    public int[] getCount() {
        return this.count;
    }
}
