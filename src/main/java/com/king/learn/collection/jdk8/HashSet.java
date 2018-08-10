package com.king.learn.collection.jdk8;

import sun.misc.SharedSecrets;

import java.io.*;
import java.util.*;


public class HashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, Serializable {

    static final long serialVersionUID = -5024744406713321676L;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    //HashSet底层是HashMap
    private transient HashMap<E, Object> map;

    /**
     * 构造一个空HashMap
     * 默认桶的大小 capacity 是 16, 默认负载因子 load factor 是0.75
     */
    public HashSet() {
        map = new HashMap<>();
    }

    /**
     * 参考c的大小来初始化一个HashMap, 为什么说`参考`呢?
     * 因为如果除以负载因子后比16小...那还是使用16...做一个权衡
     * <p>
     * 构造完HashMap后, 把c里的所有元素都插入进去
     */
    public HashSet(Collection<? extends E> c) {
        map = new HashMap<>(Math.max((int) (c.size() / .75f) + 1, 16));
        addAll(c);
    }

    /**
     * 定制初始大小, 负载因子 的构造器.
     */
    public HashSet(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * 定时初始大小, 采用默认构造函数的构造器
     */
    public HashSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    /**
     * 被LinkedHashSet使用, 这里面实例化的是LinkedHashMap
     */
    HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        map = new LinkedHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * 迭代器
     */
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * 返回集合里的元素个数
     */
    public int size() {
        return map.size();
    }

    /**
     * 如果集合里面没有元素, 那么返回true
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * 判断集合里是否包含这个元素 o
     */
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * 向集合里添加元素
     *
     * @return 如果集合里之前没有这个元素, 那么返回true
     */
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    /**
     * 从集合里删除元素
     *
     * @return 如果删除成功了, 那么返回true. 如果原先集合里没有, 那么就不算删除, 返回false.
     */
    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }

    /**
     * 清空集合
     */
    public void clear() {
        map.clear();
    }

    /**
     * 浅克隆
     * 因为只是克隆了HashSet这个对象, 但是HashSet里面的元素都没有进行克隆
     * 两个HashSet共用了整个集合里的元素
     */
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            HashSet<E> newSet = (HashSet<E>) super.clone();
            newSet.map = (HashMap<E, Object>) map.clone();
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 序列化
     * TODO 暂时不分析
     */
    private void writeObject(ObjectOutputStream s)
            throws IOException {
        // Write out any hidden serialization magic
        s.defaultWriteObject();

        // Write out HashMap capacity and load factor
        s.writeInt(map.capacity());
        s.writeFloat(map.loadFactor());

        // Write out size
        s.writeInt(map.size());

        // Write out all elements in the proper order.
        for (E e : map.keySet())
            s.writeObject(e);
    }

    /**
     * 反序列化
     * TODO 暂时不分析
     */
    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        // Read in any hidden serialization magic
        s.defaultReadObject();

        // Read capacity and verify non-negative.
        int capacity = s.readInt();
        if (capacity < 0) {
            throw new InvalidObjectException("Illegal capacity: " +
                    capacity);
        }

        // Read load factor and verify positive and non NaN.
        float loadFactor = s.readFloat();
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new InvalidObjectException("Illegal load factor: " +
                    loadFactor);
        }

        // Read size and verify non-negative.
        int size = s.readInt();
        if (size < 0) {
            throw new InvalidObjectException("Illegal size: " +
                    size);
        }
        // Set the capacity according to the size and load factor ensuring that
        // the HashMap is at least 25% full but clamping to maximum capacity.
        capacity = (int) Math.min(size * Math.min(1 / loadFactor, 4.0f),
                HashMap.MAXIMUM_CAPACITY);

        // Constructing the backing map will lazily create an array when the first element is
        // added, so check it before construction. Call HashMap.tableSizeFor to compute the
        // actual allocation size. Check Map.Entry[].class since it's the nearest public type to
        // what is actually created.

        SharedSecrets.getJavaOISAccess()
                .checkArray(s, Map.Entry[].class, HashMap.tableSizeFor(capacity));

        // Create backing HashMap
        map = (((HashSet<?>) this) instanceof LinkedHashSet ?
                new LinkedHashMap<E, Object>(capacity, loadFactor) :
                new HashMap<E, Object>(capacity, loadFactor));

        // Read in all elements in the proper order.
        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked")
            E e = (E) s.readObject();
            map.put(e, PRESENT);
        }
    }

    /**
     * 分片迭代器
     */
    public Spliterator<E> spliterator() {
        return new HashMap.KeySpliterator<E, Object>(map, 0, -1, 0, 0);
    }
}
