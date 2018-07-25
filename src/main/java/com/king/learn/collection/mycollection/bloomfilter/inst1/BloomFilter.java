package com.king.learn.collection.mycollection.bloomfilter.inst1;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.*;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 代码源自这里: (进行了少许改动)
 * https://github.com/jackfo/cfs-shop/blob/3bf72e6fe963a84d17548d7270071c93e86e57f3/cfs-shop-common/src/main/java/com/whpu/util/cache/BloomFileter.java
 */
public class BloomFilter implements Serializable {

    private static final long serialVersionUID = 4422582596265897335L;
    private final int size;
    private final BitSet bits;
    private final int[] seeds;
    private final AtomicInteger counter;
    private final Double autoClearRate;

    // 构造代码块, 无论是执行那个构造器, counter的初始化是相同的.
    {
        counter = new AtomicInteger(0);
    }

    /**
     * @implNote 构造器1
     * @implSpec 需要传入预期处理的数据规模，如预期用于处理1百万数据的查重，这里则填写1000000
     * @implSpec 中等程序的误判率：MisjudgmentRate.MIDDLE
     * @implSpec 不自动清空数据（性能会有少许提升）
     */
    public BloomFilter(int dataCount) {
        this(MisjudgmentRate.MIDDLE, dataCount, null);
    }

    /**
     * @param rate          一个枚举类型的误判率
     * @param dataCount     预期处理的数据规模，如预期用于处理1百万数据的查重，这里则填写1000000
     * @param autoClearRate 自动清空过滤器内部信息的使用比率，传null则表示不会自动清理，
     *                      当过滤器使用率达到100%时，则无论传入什么数据，都会认为在数据已经存在了
     *                      当希望过滤器使用率达到80%时自动清空重新使用，则传入0.8
     * @implNote 构造器2
     */
    public BloomFilter(MisjudgmentRate rate, int dataCount, Double autoClearRate) {
        // hash种子数 * 预期的数据量就是最终的位图大小.
        long bitSize = rate.getSeeds().length * dataCount;

        // 判断是否溢出
        if (bitSize < 0 || bitSize > Integer.MAX_VALUE) {
            throw new RuntimeException("位数太大溢出了，请降低误判率或者降低数据大小");
        }

        // 设置
        seeds = rate.getSeeds();
        size = (int) bitSize;
        bits = new BitSet(size);
        this.autoClearRate = autoClearRate;
    }

    /**
     * @param path 读取的路径
     * @implNote 反序列化
     */
    public static BloomFilter readFilterFromFile(String path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return (BloomFilter) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return 当前位图中的'1'占的比例.
     */
    private double usingRate() {
        return (double) counter.intValue() / size;
    }

    /**
     * @return 总是返回true
     * @implNote 清空位图和计数器
     */
    public boolean clear() {
        this.bits.clear();
        this.counter.set(0);
        return true;
    }

    /**
     * @return 如果执行了清空这一步骤, 那么返回true. 否则返回false.
     * @implNote 如果当前使用量超过了最大限制autoClearRate, 那么就清空.
     */
    private boolean syncClearIfNeed() {
        if (autoClearRate != null && usingRate() >= autoClearRate) {
            synchronized (this) {
                if (usingRate() >= autoClearRate && clear()) return true;
            }
        }
        return false;
    }

    /**
     * @param data 数据
     * @param seed 哈希种子
     * @return 对应的hash值结果.
     */
    private int hash(String data, int seed) {
        // 默认null和空字符串的hash值为0
        if (data == null || data.length() == 0) return 0;

        // 把String变为字节
        byte[] binaryData = data.getBytes();

        // 这个hash变量就是最终要返回的哈希值
        int hash = 0;

        for (int i = 0; i < binaryData.length; i++)
            hash = i * hash + binaryData[i];

        return Math.abs(hash * seed % size);
    }

    /**
     * @param index 索引位置
     * @implNote 将位图中index位置设置为1.然后计数器+1
     */
    private void setTrue(int index) {
        bits.set(index, true);
        counter.incrementAndGet();
    }

    public boolean add(String data) {
        syncClearIfNeed();
        for (int seed : seeds)
            setTrue(hash(data, seed));

        return true;
    }

    public boolean contains(String data) {
        for (int seed : seeds)
            if (!bits.get(hash(data, seed)))
                return false;
        return true;
    }

    public boolean addIfAbsent(String data) {
        return !(!this.contains(data) && this.add(data));
    }

    /**
     * @param path 序列化的路径以及具体的目标名. 如/User/xxx/1.txt
     * @implNote 序列化
     */
    public void saveFilterToFile(String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @implNote 分配的位数越多，误判率越低但是越占内存
     * @implSpec 4个位误判率大概是0.14689159766308
     * @implSpec 8个位误判率大概是0.02157714146322
     * @implSpec 16个位误判率大概是0.00046557303372
     * @implNote 这里要选取质数，能很好的降低错误率
     */
    @AllArgsConstructor
    public enum MisjudgmentRate {

        /**
         * @implNote 每个字符串分配4个位
         */
        SMALL(new int[]{2, 3, 5, 7}),
        /**
         * @implNote 每个字符串分配8个位
         */
        MIDDLE(new int[]{2, 3, 5, 7, 11, 13, 17, 19}),
        /**
         * @implNote 每个字符串分配16个位
         */
        LARGE(new int[]{2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53});

        @Getter
        private int[] seeds;
    }
}
