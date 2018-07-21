package com.king.learn.collection.jdk8;

import sun.misc.SharedSecrets;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {

    /**
     * 初始容量. 容量必须是2的指数.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * 最大容量
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * 默认负载因子
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * 如果一个桶中的元素个数超过这个值, 就使用红黑树来替换链表
     */
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * 当执行resize操作时,当桶中bin的数量少于UNTREEIFY_THRESHOLD时使用链表来代替树
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 在转变成树之前，还会有一次判断，只有键值对数量大于 MIN_TREEIFY_CAPACITY 才会发生转换。
     * 这是为了避免在哈希表建立初期，多个键值对恰好被放入了同一个链表中而导致不必要的转化
     */
    static final int MIN_TREEIFY_CAPACITY = 64;

    private static final long serialVersionUID = 362498820763181265L;

    /**
     * 负载因子
     */
    final float loadFactor;

    /**
     * 在第一次使用时被初始化，必要时会调整大小。被分配后，其长度一直是2的次方
     */
    transient Node<K, V>[] table;

    /**
     * Entry集合
     */
    transient Set<Entry<K, V>> entrySet;

    /**
     * map 中 key-value 的个数
     */
    transient int size;

    /**
     * HashMap 的结构被修改的次数
     */
    transient int modCount;

    /**
     * 大小达到临界值,需要重新分配大小 (capacity * load factor).
     */
    int threshold;

    /**
     * 根据传入的 容量大小 和 负载因子 构造的空HashMap
     */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException(
                "Illegal initial capacity: " + initialCapacity);

        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;

        if (loadFactor <= 0 || Float.isNaN(loadFactor)) throw
                new IllegalArgumentException("Illegal load factor: " + loadFactor);

        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }

    /**
     * 根据传入的容量大小构造的HashMap
     * 采用的默认负载因子0.75
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * 默认初始大小(16), 默认负载因子的HashMap(0.75)
     */
    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }

    /**
     * 构造一个默认大小(16), 默认负载因子(0.75)的一个HashMap,
     * 然后把传入的map的所有k-v都赋值进来
     */
    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }

    /**
     * 计算hash
     */
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * 如果x实现了Comparable接口, 那么返回x的class类型
     * 否则返回null
     */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c;
            Type[] ts, as;
            Type t;
            ParameterizedType p;
            if ((c = x.getClass()) == String.class) // bypass checks
                return c;
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                            ((p = (ParameterizedType) t).getRawType() ==
                                    Comparable.class) &&
                            (as = p.getActualTypeArguments()) != null &&
                            as.length == 1 && as[0] == c) // type arg is c
                        return c;
                }
            }
        }
        return null;
    }

    /**
     * Returns funs.compareTo(x) if x matches kc (funs's screened comparable
     * class), else 0.
     */
    @SuppressWarnings({"rawtypes", "unchecked"}) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable) k).compareTo(x));
    }

    /**
     * 返回目标容量.
     * 比如传入10, 首先10-1=9
     * 9的二进制是0000 1001
     * 把第一个1后面的所有0变为1, 就变成了 0000 1111
     * 最后再加1, 变为了0001 0000, 也就是16
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * Implements Map.putAll and Map constructor
     */
    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
            // 判断table是否已经初始化, 如果没有初始化
            if (table == null) { // pre-size
                float ft = ((float) s / loadFactor) + 1.0F;
                int t = ((ft < (float) MAXIMUM_CAPACITY) ?
                        (int) ft : MAXIMUM_CAPACITY);
                // 计算得到的t大于阈值，则初始化阈值
                if (t > threshold)
                    threshold = tableSizeFor(t);

                // table已初始化，但是m元素个数大于阈值，进行扩容处理
            } else if (s > threshold)
                resize();

            // 将m中的所有元素添加至HashMap中
            for (Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }

    /**
     * 返回map中key-value键值对的个数
     */
    public int size() {
        return size;
    }

    /**
     * map是否为空
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 根据key获取value
     */
    public V get(Object key) {
        Node<K, V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * 根据key的hash值和key, 来获取value
     */
    final Node<K, V> getNode(int hash, Object key) {
        Node<K, V>[] tab;
        Node<K, V> first, e;
        int n;
        K k;
        //table已经初始化，长度大于0
        if ((tab = table) != null && (n = tab.length) > 0 &&
                // 根据hash寻找table表中相应桶的第一项也不空
                (first = tab[(n - 1) & hash]) != null) {

            // 总是先判断第一个节点. 第一个节点的hash值是否和传入的hash值一样
            if (first.hash == hash && // always check first node
                    // hash值如果一样, 那么判断key是否相等
                    ((k = first.key) == key || (key != null && key.equals(k))))
                // 如果key相等(同一个对象, 或者equals判断值相等), 那么就返回这个第一个节点
                return first;

            // 如果第一个节点不相等, 那么获取第二个节点. 如果第二个节点存在的话, 就进入这个if
            if ((e = first.next) != null) {
                // map在冲突严重的时候, 会变为红黑树, 所以map可能是红黑树状态
                if (first instanceof TreeNode)
                    // 如果是红黑树, 那么继续到树里进行查找
                    return ((TreeNode<K, V>) first).getTreeNode(hash, key);

                // hash冲突不明显的时候, 一般都是链表状态. 所以如果是链表的话, 会执行下面这段
                do {
                    // 遍历链表, 先判断hash值, 然后hash值一样的时候判断值是否equals
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        // 没有就是返回null
        return null;
    }

    /**
     * 判断map中是否有这个key
     * containsKey 和 getKey, 调用的是同一个方法. 实现原理是一模一样的.
     */
    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }

    /**
     * 向map中插入一对key-value
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * Implements Map.put and related methods
     *
     * @param hash         key的hash值
     * @param key          要插入进来的key
     * @param value        key对应的value
     * @param onlyIfAbsent 如果是true, 不会覆盖现有的值
     * @param evict        这个字段没有用.
     * @return 如果之前存在这个key, 那么就返回之前的value, 如果之前没有, 那么就返回null
     */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
        Node<K, V>[] tab;
        Node<K, V> p;
        int n, i;
        //tab为空则创建
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;

        // 判断对应hash位置的第一个节点存不存在, 没有就直接创建
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            Node<K, V> e;
            K k;
            // 首先判断第一个节点, 如果key相等, 那么覆盖.
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;

                // 判断第一个节点是不是红黑树的节点,
            else if (p instanceof TreeNode)
                // 如果是红黑树, 那么直接使用红黑树的插入操作就可以了
                e = ((TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);

                // 不是红黑树, 那么就是链表了
            else {
                // 遍历链表
                for (int binCount = 0; ; ++binCount) {
                    // 如果遍历到了null还没找到目标key, 那就直接插入.
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        // 插入后还需要判断一下是否有必要转为红黑树. 没准链表超过长度了呢
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        // 插入成功了, break
                        break;
                    }
                    // 如果找到了相应的key, 那么就break
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
            // 上面的for循环中有两个break
            // 如果执行了第一个break而跳出来, 那么e肯定是null, 也就不会执行下面这句话
            // 如果执行了第二个break而跳出来, 那么e肯定不是null, 也就会执行下面这句话
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                // 如果参数onlyIfAbsent==true的话, 就不会进行覆盖
                // 如果参数onlyIfAbsent==false的话,就会进行覆盖
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                // HashMap里这个afterNodeAccess是一个空实现, 里面什么都没有
                // LinkedHashMap里,如果是根据访问顺序来排序的话, 当节点被访问的时候, 就把这个节点放到尾部
                afterNodeAccess(e);
                return oldValue;
            }
        }
        // 如果要插入的这个key, 是map之前不存在的, 那么就会执行到这里

        // modCount用于记录map的被修改的次数, 在这里进行加一操作
        ++modCount;
        // 如果足够大了, 就应该进行扩容了.
        if (++size > threshold)
            resize();

        // HashMap里这个afterNodeInsertion是一个空实现, 里面什么都没有
        // LinkedHashMap里就不是空操作了, 而是在链表尾进行添加.
        afterNodeInsertion(evict);

        // 因为插入这个这个key是直线不存在的,
        // 这个方法return的就是map在插入本次key-value之前的value的值, 当前就是null了
        return null;
    }

    /**
     * 扩容
     */
    final Node<K, V>[] resize() {
        Node<K, V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        // 如果map扩容前table的大小大于0(也就是桶的数量是0)
        if (oldCap > 0) {
            // 如果table数组的大小超过最大限制
            if (oldCap >= MAXIMUM_CAPACITY) {
                // 直接把k-v的存储限制定为int型的最大值
                threshold = Integer.MAX_VALUE;
                // 不需要改变其他的了, 返回原来的table数组
                return oldTab;

                // 桶的数量先进行翻倍操作. 如果桶的数量没到最大限制
            } else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    // 而且扩容前的大小大于默认初始大小
                    oldCap >= DEFAULT_INITIAL_CAPACITY)
                // 那么k-v容量翻倍
                newThr = oldThr << 1; // double threshold

            // 如果桶的数量是0, 但是容量大于0
        } else if (oldThr > 0) { // initial capacity was placed in threshold
            newCap = oldThr;

            // 不然就走默认值了
        } else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }

        // 如果新的容量大小newThr还是0, 那么就执行这里
        if (newThr == 0) {
            float ft = (float) newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ?
                    (int) ft : Integer.MAX_VALUE);
        }

        // 设置容量
        threshold = newThr;

        //创建新的table
        @SuppressWarnings({"rawtypes", "unchecked"})
        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];

        // 让新的table生效
        table = newTab;

        // 如果原先的table不空, 那么应该把元素都迁移到新的table中.
        if (oldTab != null) {
            // 遍历所有桶
            for (int j = 0; j < oldCap; ++j) {
                Node<K, V> e;
                // 如果第j个桶不为空
                if ((e = oldTab[j]) != null) {
                    // 让旧桶指向null
                    oldTab[j] = null;
                    // 如果第一个桶没有下一个节点, 那么表示 这个桶里只有这一个节点
                    if (e.next == null)
                        // 所以把这一个节点移过去就好了
                        newTab[e.hash & (newCap - 1)] = e;
                        // 如果这个桶里还有后续其他的节点, 而且还是红黑树
                    else if (e instanceof TreeNode)
                        // 那么我们去将树上的节点rehash之后根据hash值放到新地方
                        ((TreeNode<K, V>) e).split(this, newTab, j, oldCap);

                        // 如果后续不是红黑树, 而是链表
                    else { // preserve order

                            /*这里的操作就是 (e.hash & oldCap) == 0 这一句，
                            这一句如果是true，表明(e.hash & (newCap - 1))还会和
                            e.hash & (oldCap - 1)一样。因为oldCap和newCap是2的次幂，
                            并且newCap是oldCap的两倍，就相当于oldCap的唯一
                            一个二进制的1向高位移动了一位
                            (e.hash & oldCap) == 0就代表了(e.hash & (newCap - 1))还会和
                            e.hash & (oldCap - 1)一样。

                            比如原来容量是16，那么就相当于e.hash & 0x1111
                            （0x1111就是oldCap - 1 = 16 - 1 = 15），
                            现在容量扩大了一倍，就是32，那么rehash定位就等于
                            e.hash & 0x11111 （0x11111就是newCap - 1 = 32 - 1 = 31）
                            现在(e.hash & oldCap) == 0就表明了
                            e.hash & 0x10000 == 0，这样的话，不就是
                            已知： e.hash &  0x1111 = hash定位值Value
                             并且  e.hash & 0x10000 = 0
                            那么   e.hash & 0x11111 不也就是
                            原来的hash定位值Value吗？

                            那么就只需要根据这一个二进制位就可以判断下次hash定位在
                            哪里了。将hash冲突的元素连在两条链表上放在相应的位置
                            不就行了嘛。*/

                        Node<K, V> loHead = null, loTail = null;
                        Node<K, V> hiHead = null, hiTail = null;
                        Node<K, V> next;
                        do {
                            next = e.next;
                            // 如果e的hash值在扩容前和扩容后在同一个桶中
                            if ((e.hash & oldCap) == 0) {
                                // 如果是这个桶的第一个节点
                                if (loTail == null)
                                    // 那么走这里
                                    loHead = e;
                                    // 如果不是这个桶的第一个节点,
                                else
                                    // 那么就尾插就好了
                                    loTail.next = e;
                                // 更新尾部
                                loTail = e;

                                // 如果e的hash值在扩容前和扩容后不在同一个桶中,
                                // 说明e的位置在[j + oldCap]这个桶中
                            } else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);

                        // 由于上面的循环, 把原先一个桶中的链表(红黑树)分组分成了两个链表(红黑树)
                        // 在这里把两个链表中hash值小的那个, 插入到新的map中
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        // 在这里把两个链表中hash值大的那个, 插入到新的map中
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        // 返回新的HashMap
        return newTab;
    }

    /**
     * 将链表变为红黑树, 如果链表太短, 那就只是扩容, 而不是变为树
     */
    final void treeifyBin(Node<K, V>[] tab, int hash) {
        int n, index;
        Node<K, V> e;
        // 如果table是空, 或者table数组太小(桶的个数太小), 那么就扩容, 而不是转为树
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
            // 否则判断相应hash位置是否为空, 不为空就进行转换
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K, V> hd = null, tl = null;
            // 把单项链表转化为双向链表
            do {
                TreeNode<K, V> p = replacementTreeNode(e, null);
                if (tl == null)
                    hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null)
                // 进行红黑树的转换
                hd.treeify(tab);
        }
    }

    /**
     * 将参数m的所有key-value都插入进来
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m, true);
    }

    /**
     * 根据key删除
     */
    public V remove(Object key) {
        Node<K, V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
                null : e.value;
    }

    /**
     * 删除map中的节点
     *
     * @param hash       key的hash值
     * @param key        要删除的key
     * @param value      如果matchValue打开着, 就需要传这个参数, 否则没用.
     * @param matchValue 如果是true, 那么只有在value 相等(equals)的时候才会删除.
     * @param movable    if false do not move other nodes while removing
     * @return 返回被删除的节点. 如果没有这个节点, 那么就返回null
     */
    final Node<K, V> removeNode(int hash, Object key, Object value,
                                boolean matchValue, boolean movable) {
        Node<K, V>[] tab;
        Node<K, V> p;
        int n, index;
        // table不空
        if ((tab = table) != null
                // table的数组大小大于0(有桶存在, table不空, 但是数组等于0, 还是相当于没有)
                && (n = tab.length) > 0
                // 相应的hash位置有节点存在
                && (p = tab[index = (n - 1) & hash]) != null) {
            Node<K, V> node = null, e;
            K k;
            V v;
            // 先判断第一个节点的key
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;

                // 如果第一个节点不是吗, 那么判断第二个节点
            else if ((e = p.next) != null) {
                // 判断是不是红黑树结构
                if (p instanceof TreeNode)
                    // 如果是红黑树, 那么就直接调用红黑树的方法来寻找
                    node = ((TreeNode<K, V>) p).getTreeNode(hash, key);

                    // 如果不是红黑树, 那么就是链表了, 进行遍历查找
                else {
                    do {
                        if (e.hash == hash &&
                                ((k = e.key) == key ||
                                        (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            // 如果根据key, 在map中找到了相应的节点
            if (node != null
                    // 如果matchValue为false, 那么进入这段if
                    // 或者如果matchValue为true, 而且value 相等, 那么进入这段if
                    && (!matchValue || (v = node.value) == value ||
                    (value != null && value.equals(v)))) {
                // 如果是红黑树结构的, 那么调用红黑树的方法来进行删除
                if (node instanceof TreeNode)
                    ((TreeNode<K, V>) node).removeTreeNode(this, tab, movable);
                    // 如果是链表结构(因为不是红黑树,那就只能是链表了), 而且要删除的是桶里的第一个节点
                else if (node == p)
                    tab[index] = node.next;

                    // 如果是链表结构, 而且要删除的节点还是不是桶里的第一个节点
                else
                    p.next = node.next;

                // 删除也算修改map结构, 所以计数器+1
                ++modCount;

                // map的k-v个数减一
                --size;

                // HashMap的话这里是空实现, 里面什么也没有
                // LinkedHashMap的话就有用了, 就是删除链表尾部
                afterNodeRemoval(node);

                //返回这个被删除的节点
                return node;
            }
        }
        // 如果没找到这个key,
        // 或者matchValue开关 开着的情况下, value不相等, 导致了没删除节点
        // 那么就返回null
        return null;
    }

    /**
     * 将map清空
     */
    public void clear() {
        Node<K, V>[] tab;
        // 算一次map的修改
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            // 遍历所有桶
            for (int i = 0; i < tab.length; ++i)
                // 将每个桶置为null
                tab[i] = null;
        }
    }

    /**
     * 根据value进行查找, 如果有就返回true
     * (可能会有多个key对应着多个相等的value)
     */
    public boolean containsValue(Object value) {
        Node<K, V>[] tab;
        V v;
        if ((tab = table) != null && size > 0) {
            // 遍历桶
            for (int i = 0; i < tab.length; ++i) {
                // 遍历同一个桶里的所有元素
                for (Node<K, V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                            (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * get 整个key的集合
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    /**
     * @return 返回value的集合
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    /**
     * @return 返回k-v的集合
     */
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    /**
     * @return 如果有就get, 没有就返回默认值
     */
    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K, V> e;
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }

    /**
     * 如果没有就put, 然后返回null
     * 如果有, 那么就不put, 返回原来的值
     */
    @Override
    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, true, true);
    }

    /**
     * 根据key 和 value 来删除元素
     */
    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }

    /**
     * 替换key和value来寻找节点, 然后替换这个节点的value
     * 如果替换成功那么返回true, 没替换就返回false
     * <p>
     * 只根据key来查找, 是只判断key
     * 根据key和value来查找 是不仅key要相等, 而且还要判断value是否相等.
     */
    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K, V> e;
        V v;
        if ((e = getNode(hash(key), key)) != null &&
                ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            // HashMap里这个afterNodeAccess是一个空实现, 里面什么都没有
            // LinkedHashMap里,如果是根据访问顺序来排序的话, 当节点被访问的时候, 就把这个节点放到尾部
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    /**
     * 根据key来进行value的替换, 然后返回原来的value
     * 如果根据key没找到相应的元素, 那么当然就无法替换了, 直接返回null
     */
    @Override
    public V replace(K key, V value) {
        Node<K, V> e;
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            // HashMap里这个afterNodeAccess是一个空实现, 里面什么都没有
            // LinkedHashMap里,如果是根据访问顺序来排序的话, 当节点被访问的时候, 就把这个节点放到尾部
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

    /**
     * 如果没有这个key, 就进行计算
     */
    @Override
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        // mappingFunction不能是空
        if (mappingFunction == null)
            throw new NullPointerException();

        // hash值
        int hash = hash(key);
        Node<K, V>[] tab;
        Node<K, V> first;
        int n, i;
        int binCount = 0;
        TreeNode<K, V> t = null;
        Node<K, V> old = null;

        // 需要扩容
        if (size > threshold || (tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;

        // 尝试获取相应hash位置的第一个元素, 如果获取到了(不为null), 那么进入这个if
        if ((first = tab[i = (n - 1) & hash]) != null) {
            // 如果是红黑树,那么用红黑树的get方法获取
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K, V>) first).getTreeNode(hash, key);

                // 不是红黑树, 那么就是链表了
            else {
                Node<K, V> e = first;
                K k;
                // 遍历桶, 判断桶中是否有
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
            V oldValue;
            // 如果原先有数据, 那么返回直接返回oldValue, 不执行mappingFunction
            if (old != null && (oldValue = old.value) != null) {
                // HashMap里这个afterNodeAccess是一个空实现, 里面什么都没有
                // LinkedHashMap里,如果是根据访问顺序来排序的话, 当节点被访问的时候, 就把这个节点放到尾部
                afterNodeAccess(old);
                return oldValue;
            }
        }
        // 执行mappingFunction
        V v = mappingFunction.apply(key);

        if (v == null) {
            return null;

            // 如果mappingFunction 返回的不是null, 而且old不是空(说明原先有这个key对应的value)
        } else if (old != null) {
            // 那么进行替换, 然后进行返回
            old.value = v;
            // HashMap里这个afterNodeAccess是一个空实现, 里面什么都没有
            // LinkedHashMap里,如果是根据访问顺序来排序的话, 当节点被访问的时候, 就把这个节点放到尾部
            afterNodeAccess(old);
            return v;

            // 如果mappingFunction 返回的不是null, 而且old是空, 而且t不为空, 那么将执行这里
            // t表示的是桶里的第一个节点, 而且是TreeNode类型(红黑树节点)
        } else if (t != null)
            // 那么就put进树里
            t.putTreeVal(this, tab, hash, key, v);

            // 到这里说明 没有这个key对应的value, 而且还不是红黑树, (不是红黑树, 那么只能是链表了)
        else {
            // 创建一个新的节点, 然后采用头插法进行链表插入
            tab[i] = newNode(hash, key, v, first);
            // 看看是否有有必要进行树的转换
            if (binCount >= TREEIFY_THRESHOLD - 1)
                treeifyBin(tab, hash);
        }
        // 算一次map的修改
        ++modCount;
        // map插入了一个节点
        ++size;
        // HashMap里这个afterNodeInsertion是一个空实现, 里面什么都没有
        // LinkedHashMap里就不是空操作了, 而是在链表尾进行添加.
        afterNodeInsertion(true);
        return v;
    }

    /**
     * 如果存在, 那么会执行remappingFunction
     */
    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        // remappingFunction不能为空
        if (remappingFunction == null)
            throw new NullPointerException();
        Node<K, V> e;
        V oldValue;
        // 计算hash值
        int hash = hash(key);
        // 如果根据key获取到了节点
        if ((e = getNode(hash, key)) != null &&
                // 如果获取到的这个节点的value不为空
                (oldValue = e.value) != null) {
            // 执行 remappingFunction
            V v = remappingFunction.apply(key, oldValue);
            // 如果remappingFunction返回的不是空
            if (v != null) {
                // 那么就进行替换
                e.value = v;
                // HashMap里这个afterNodeAccess是一个空实现, 里面什么都没有
                // LinkedHashMap里,如果是根据访问顺序来排序的话, 当节点被访问的时候, 就把这个节点放到尾部
                afterNodeAccess(e);
                // 直接返回
                return v;

                // 如果返回的v是空
            } else
                // 那么根据key进行删除节点
                removeNode(hash, key, null, false, true);
        }
        // 根据key没获取到相应的节点, 或者节点的value是空, 那么直接就返回null
        return null;
    }

    /**
     * 前面分析了computeIfPresent 和 computeIfAbsent, 对这个方法不太感兴趣, 以后再说吧
     */
    @Override
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K, V>[] tab;
        Node<K, V> first;
        int n, i;
        int binCount = 0;
        TreeNode<K, V> t = null;
        Node<K, V> old = null;
        if (size > threshold || (tab = table) == null ||
                (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K, V>) first).getTreeNode(hash, key);
            else {
                Node<K, V> e = first;
                K k;
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        V oldValue = (old == null) ? null : old.value;
        V v = remappingFunction.apply(key, oldValue);
        if (old != null) {
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            } else
                removeNode(hash, key, null, false, true);
        } else if (v != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, v);
            else {
                tab[i] = newNode(hash, key, v, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            // HashMap里这个afterNodeInsertion是一个空实现, 里面什么都没有
            // LinkedHashMap里就不是空操作了, 而是在链表尾进行添加.
            afterNodeInsertion(true);
        }
        return v;
    }

    /**
     * 前面分析了computeIfPresent 和 computeIfAbsent, 对这个方法不太感兴趣, 以后再说吧
     */
    @Override
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null)
            throw new NullPointerException();
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K, V>[] tab;
        Node<K, V> first;
        int n, i;
        int binCount = 0;
        TreeNode<K, V> t = null;
        Node<K, V> old = null;
        if (size > threshold || (tab = table) == null ||
                (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K, V>) first).getTreeNode(hash, key);
            else {
                Node<K, V> e = first;
                K k;
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        if (old != null) {
            V v;
            if (old.value != null)
                v = remappingFunction.apply(old.value, value);
            else
                v = value;
            if (v != null) {
                old.value = v;
                // HashMap里这个afterNodeAccess是一个空实现, 里面什么都没有
                // LinkedHashMap里,如果是根据访问顺序来排序的话, 当节点被访问的时候, 就把这个节点放到尾部
                afterNodeAccess(old);
            } else
                removeNode(hash, key, null, false, true);
            return v;
        }
        if (value != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, value);
            else {
                tab[i] = newNode(hash, key, value, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            // HashMap里这个afterNodeInsertion是一个空实现, 里面什么都没有
            // LinkedHashMap里就不是空操作了, 而是在链表尾进行添加.
            afterNodeInsertion(true);
        }
        return value;
    }

    /**
     * @param action 相应的行为
     */
    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K, V>[] tab;
        // 首先action不能是null
        if (action == null)
            throw new NullPointerException();

        // map中要有k-v, 而且table表不为null (好歹是有意义的map吧)
        if (size > 0 && (tab = table) != null) {
            // 在这里记录一下被修改的次数
            int mc = modCount;
            // 遍历桶
            for (int i = 0; i < tab.length; ++i) {
                // 比那里桶中的所有节点
                for (Node<K, V> e = tab[i]; e != null; e = e.next)
                    // 对所有节点(funs-v)进行action的相应操作
                    action.accept(e.key, e.value);
            }
            // 如果在执行for循环的期间中途被修改过, 那么modCount和mc值就会不一样了
            // 意思就是, 在遍历时, 如果被修改过难么就行抛异常
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * 替换全部, 感觉实现跟上面差不多,就不具体分析了.
     *
     * @param function replace具体的策略实现
     */
    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Node<K, V>[] tab;
        if (function == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K, V> e = tab[i]; e != null; e = e.next) {
                    e.value = function.apply(e.key, e.value);
                }
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * 浅克隆, 只是克隆了HashMap这个实例, 而没有克隆所有的key-value键值对
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        HashMap<K, V> result;
        try {
            // 克隆, 然后用result来引用
            result = (HashMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
        // 重新初始化
        result.reinitialize();
        // 把当前map的所有k-v插入进result中
        result.putMapEntries(this, false);
        return result;
    }

    // 返回负载因子
    final float loadFactor() {
        return loadFactor;
    }

    // 返回容量, 也就是table的数组大小, 也就是桶的个数
    // 如果table==null, 那么就返回threshold
    // threshold 表示当HashMap的size大于threshold时会执行resize操作
    final int capacity() {
        return (table != null) ? table.length :
                (threshold > 0) ? threshold :
                        DEFAULT_INITIAL_CAPACITY;
    }

    /**
     * 序列化, 写
     */
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        int buckets = capacity();
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(size);
        // 写所有的k-v
        internalWriteEntries(s);
    }

    /**
     * 从流中读取数据, 构建一个HashMap
     */
    private void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        // Read in the threshold (ignored), loadfactor, and any hidden stuff
        s.defaultReadObject();
        reinitialize();
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new InvalidObjectException("Illegal load factor: " +
                    loadFactor);
        // 读buckets, 也就是table数组的大小
        s.readInt();                // Read and ignore number of buckets
        // 读size, 也就是k-v 的个数
        int mappings = s.readInt(); // Read number of mappings (size)

        // size<0 说明不正常
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " +
                    mappings);

        else if (mappings > 0) { // (如果size==0, 那么直接使用默认值就可以了, 没必要执行下面这段)
            // Size the table using given load factor only if within range of 0.25...4.0
            // 负载因子在0.25和4.0之间的时候
            float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
            float fc = (float) mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                    DEFAULT_INITIAL_CAPACITY :
                    (fc >= MAXIMUM_CAPACITY) ?
                            MAXIMUM_CAPACITY :
                            tableSizeFor((int) fc));
            float ft = (float) cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                    (int) ft : Integer.MAX_VALUE);

            // Check Map.Entry[].class since it's the nearest public type to what we're actually creating.
            SharedSecrets.getJavaOISAccess().checkArray(s, Entry[].class, cap);
            @SuppressWarnings({"rawtypes", "unchecked"})
            Node<K, V>[] tab = (Node<K, V>[]) new Node[cap];
            table = tab;

            // Read the keys and values, and put the mappings in the HashMap
            for (int i = 0; i < mappings; i++) {
                @SuppressWarnings("unchecked")
                K key = (K) s.readObject();
                @SuppressWarnings("unchecked")
                V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }

    /**
     * 创建一个节点, (不是树节点)
     */
    Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
        return new Node<>(hash, key, value, next);
    }

    // 树节点转化为普通的节点
    Node<K, V> replacementNode(Node<K, V> p, Node<K, V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // 创建一个树的二叉节点
    TreeNode<K, V> newTreeNode(int hash, K key, V value, Node<K, V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    // 树形化, 将普通节点替换为红黑树节点
    TreeNode<K, V> replacementTreeNode(Node<K, V> p, Node<K, V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    /**
     * 重新设置初始值.  Called by clone and readObject.
     */
    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    // Callbacks to allow LinkedHashMap post-actions
    void afterNodeAccess(Node<K, V> p) {
    }

    void afterNodeInsertion(boolean evict) {
    }

    void afterNodeRemoval(Node<K, V> p) {
    }

    // 被 writeObject方法调用, to ensure compatible ordering.
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<K, V>[] tab;
        if (size > 0 && (tab = table) != null) {
            // 遍历
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K, V> e = tab[i]; e != null; e = e.next) {
                    // 序列化写出k-v
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }

    /**
     * 基本的hash节点
     * 红黑树的节点是本Node类的子类, LinkedHashMap的Node也是继承了这里
     */
    static class Node<K, V> implements Entry<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final String toString() {
            return key + "=" + value;
        }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        // 设置新value, 并返回旧value
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        //判断本Node实例与其他Entry类型的实例是否相等.
        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Entry<?, ?> e = (Entry<?, ?>) o;
                if (Objects.equals(key, e.getKey()) &&
                        Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }

    /**
     * TODO
     */
    static class HashMapSpliterator<K, V> {
        final HashMap<K, V> map;
        Node<K, V> current;          // current node
        int index;                  // current index, modified on advance/split
        int fence;                  // one past last index
        int est;                    // size estimate
        int expectedModCount;       // for comodification checks

        HashMapSpliterator(HashMap<K, V> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                HashMap<K, V> m = map;
                est = m.size;
                expectedModCount = m.modCount;
                Node<K, V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    /**
     * TODO
     */
    static final class KeySpliterator<K, V>
            extends HashMapSpliterator<K, V>
            implements Spliterator<K> {

        KeySpliterator(HashMap<K, V> m,
                       int origin,
                       int fence,
                       int est,
                       int expectedModCount) {

            super(m, origin, fence, est, expectedModCount);

        }

        public KeySpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K, V> m = map;
            Node<K, V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            } else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K, V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.key);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K, V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        K k = current.key;
                        current = current.next;
                        action.accept(k);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT;
        }
    }

    /**
     * TODO
     */
    static final class ValueSpliterator<K, V>
            extends HashMapSpliterator<K, V>
            implements Spliterator<V> {
        ValueSpliterator(HashMap<K, V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K, V> m = map;
            Node<K, V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            } else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K, V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.value);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K, V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        V v = current.value;
                        current = current.next;
                        action.accept(v);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
        }
    }

    /**
     * TODO
     */
    static final class EntrySpliterator<K, V>
            extends HashMapSpliterator<K, V>
            implements Spliterator<Entry<K, V>> {
        EntrySpliterator(HashMap<K, V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K, V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new EntrySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K, V> m = map;
            Node<K, V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            } else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K, V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K, V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Node<K, V> e = current;
                        current = current.next;
                        action.accept(e);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT;
        }
    }

    /**
     * 红黑树节点
     */
    static final class TreeNode<K, V> extends LinkedHashMap.Entry<K, V> {
        TreeNode<K, V> parent;  // red-black tree links
        TreeNode<K, V> left;
        TreeNode<K, V> right;
        TreeNode<K, V> prev;    // needed to unlink next upon deletion
        boolean red;

        TreeNode(int hash, K key, V val, Node<K, V> next) {
            super(hash, key, val, next);
        }

        /**
         * Ensures that the given root is the first node of its bin.
         */
        static <K, V> void moveRootToFront(Node<K, V>[] tab, TreeNode<K, V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = (n - 1) & root.hash;
                TreeNode<K, V> first = (TreeNode<K, V>) tab[index];
                if (root != first) {
                    Node<K, V> rn;
                    tab[index] = root;
                    TreeNode<K, V> rp = root.prev;
                    if ((rn = root.next) != null)
                        ((TreeNode<K, V>) rn).prev = rp;
                    if (rp != null)
                        rp.next = rn;
                    if (first != null)
                        first.prev = root;
                    root.next = first;
                    root.prev = null;
                }
                assert checkInvariants(root);
            }
        }

        /**
         * Tie-breaking utility for ordering insertions when equal
         * hashCodes and non-comparable. We don't require a total
         * order, just a consistent insertion rule to maintain
         * equivalence across rebalancings. Tie-breaking further than
         * necessary simplifies testing a bit.
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                    (d = a.getClass().getName().
                            compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                        -1 : 1);
            return d;
        }

        static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root,
                                                TreeNode<K, V> p) {
            TreeNode<K, V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root,
                                                 TreeNode<K, V> p) {
            TreeNode<K, V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root,
                                                      TreeNode<K, V> x) {
            x.red = true;
            for (TreeNode<K, V> xp, xpp, xppl, xppr; ; ) {
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                } else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    } else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                } else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    } else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root,
                                                     TreeNode<K, V> x) {
            for (TreeNode<K, V> xp, xpl, xpr; ; ) {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                } else if (x.red) {
                    x.red = false;
                    return root;
                } else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K, V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                                (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        } else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                        null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                } else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K, V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                                (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        } else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                        null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * Recursive invariant check
         */
        static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
            TreeNode<K, V> tp = t.parent, tl = t.left, tr = t.right,
                    tb = t.prev, tn = (TreeNode<K, V>) t.next;
            if (tb != null && tb.next != t)
                return false;
            if (tn != null && tn.prev != t)
                return false;
            if (tp != null && t != tp.left && t != tp.right)
                return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red)
                return false;
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }

        /**
         * Returns root of tree containing this node.
         */
        final TreeNode<K, V> root() {
            for (TreeNode<K, V> r = this, p; ; ) {
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

        /**
         * Finds the node starting at root p with the given hash and key.
         * The kc argument caches comparableClassFor(key) upon first use
         * comparing keys.
         */
        final TreeNode<K, V> find(int h, Object k, Class<?> kc) {
            TreeNode<K, V> p = this;
            do {
                int ph, dir;
                K pk;
                TreeNode<K, V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h)
                    p = pl;
                else if (ph < h)
                    p = pr;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if (pl == null)
                    p = pr;
                else if (pr == null)
                    p = pl;
                else if ((kc != null ||
                        (kc = comparableClassFor(k)) != null) &&
                        (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                else
                    p = pl;
            } while (p != null);
            return null;
        }

        /**
         * Calls find for root node.
         */
        final TreeNode<K, V> getTreeNode(int h, Object k) {
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        final void treeify(Node<K, V>[] tab) {
            TreeNode<K, V> root = null;
            for (TreeNode<K, V> x = this, next; x != null; x = next) {
                next = (TreeNode<K, V>) x.next;
                x.left = x.right = null;
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                } else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K, V> p = root; ; ) {
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc == null &&
                                (kc = comparableClassFor(k)) == null) ||
                                (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);

                        TreeNode<K, V> xp = p;
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            moveRootToFront(tab, root);
        }

        /**
         * Returns a list of non-TreeNodes replacing those linked from
         * this node.
         */
        final Node<K, V> untreeify(HashMap<K, V> map) {
            Node<K, V> hd = null, tl = null;
            for (Node<K, V> q = this; q != null; q = q.next) {
                Node<K, V> p = map.replacementNode(q, null);
                if (tl == null)
                    hd = p;
                else
                    tl.next = p;
                tl = p;
            }
            return hd;
        }

        /**
         * Tree version of putVal.
         */
        final TreeNode<K, V> putTreeVal(HashMap<K, V> map, Node<K, V>[] tab,
                                        int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K, V> root = (parent != null) ? root() : this;
            for (TreeNode<K, V> p = root; ; ) {
                int dir, ph;
                K pk;
                if ((ph = p.hash) > h)
                    dir = -1;
                else if (ph < h)
                    dir = 1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if ((kc == null &&
                        (kc = comparableClassFor(k)) == null) ||
                        (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K, V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                                (q = ch.find(h, k, kc)) != null) ||
                                ((ch = p.right) != null &&
                                        (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K, V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K, V> xpn = xp.next;
                    TreeNode<K, V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0)
                        xp.left = x;
                    else
                        xp.right = x;
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null)
                        ((TreeNode<K, V>) xpn).prev = x;
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
         * Removes the given node, that must be present before this call.
         * This is messier than typical red-black deletion code because we
         * cannot swap the contents of an interior node with a leaf
         * successor that is pinned by "next" pointers that are accessible
         * independently during traversal. So instead we swap the tree
         * linkages. If the current tree appears to have too few nodes,
         * the bin is converted back to a plain bin. (The test triggers
         * somewhere between 2 and 6 nodes, depending on tree structure).
         */
        final void removeTreeNode(HashMap<K, V> map, Node<K, V>[] tab,
                                  boolean movable) {
            int n;
            if (tab == null || (n = tab.length) == 0)
                return;
            int index = (n - 1) & hash;
            TreeNode<K, V> first = (TreeNode<K, V>) tab[index], root = first, rl;
            TreeNode<K, V> succ = (TreeNode<K, V>) next, pred = prev;
            if (pred == null)
                tab[index] = first = succ;
            else
                pred.next = succ;
            if (succ != null)
                succ.prev = pred;
            if (first == null)
                return;
            if (root.parent != null)
                root = root.root();
            if (root == null || root.right == null ||
                    (rl = root.left) == null || rl.left == null) {
                tab[index] = first.untreeify(map);  // too small
                return;
            }
            TreeNode<K, V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<K, V> s = pr, sl;
                while ((sl = s.left) != null) // find successor
                    s = sl;
                boolean c = s.red;
                s.red = p.red;
                p.red = c; // swap colors
                TreeNode<K, V> sr = s.right;
                TreeNode<K, V> pp = p.parent;
                if (s == pr) { // p was s's direct parent
                    p.parent = s;
                    s.right = p;
                } else {
                    TreeNode<K, V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left)
                            sp.left = p;
                        else
                            sp.right = p;
                    }
                    if ((s.right = pr) != null)
                        pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null)
                    sr.parent = p;
                if ((s.left = pl) != null)
                    pl.parent = s;
                if ((s.parent = pp) == null)
                    root = s;
                else if (p == pp.left)
                    pp.left = s;
                else
                    pp.right = s;
                if (sr != null)
                    replacement = sr;
                else
                    replacement = p;
            } else if (pl != null)
                replacement = pl;
            else if (pr != null)
                replacement = pr;
            else
                replacement = p;
            if (replacement != p) {
                TreeNode<K, V> pp = replacement.parent = p.parent;
                if (pp == null)
                    root = replacement;
                else if (p == pp.left)
                    pp.left = replacement;
                else
                    pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<K, V> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) {  // detach
                TreeNode<K, V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left)
                        pp.left = null;
                    else if (p == pp.right)
                        pp.right = null;
                }
            }
            if (movable)
                moveRootToFront(tab, r);
        }

        /**
         * Splits nodes in a tree bin into lower and upper tree bins,
         * or untreeifies if now too small. Called only from resize;
         * see above discussion about split bits and indices.
         *
         * @param map   the map
         * @param tab   the table for recording bin heads
         * @param index the index of the table being split
         * @param bit   the bit of hash to split on
         */
        final void split(HashMap<K, V> map, Node<K, V>[] tab, int index, int bit) {
            TreeNode<K, V> b = this;
            // Relink into lo and hi lists, preserving order
            TreeNode<K, V> loHead = null, loTail = null;
            TreeNode<K, V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<K, V> e = b, next; e != null; e = next) {
                next = (TreeNode<K, V>) e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    if ((e.prev = loTail) == null)
                        loHead = e;
                    else
                        loTail.next = e;
                    loTail = e;
                    ++lc;
                } else {
                    if ((e.prev = hiTail) == null)
                        hiHead = e;
                    else
                        hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            if (loHead != null) {
                if (lc <= UNTREEIFY_THRESHOLD)
                    tab[index] = loHead.untreeify(map);
                else {
                    tab[index] = loHead;
                    if (hiHead != null) // (else is already treeified)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        hiHead.treeify(tab);
                }
            }
        }
    }

    /**
     * Key 集合
     */
    final class KeySet extends AbstractSet<K> {
        public final int size() {
            return size;
        }

        public final void clear() {
            HashMap.this.clear();
        }

        public final Iterator<K> iterator() {
            return new KeyIterator();
        }

        public final boolean contains(Object o) {
            return containsKey(o);
        }

        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }

        public final Spliterator<K> spliterator() {
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super K> action) {
            Node<K, V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K, V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.key);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * values 集合
     */
    final class Values extends AbstractCollection<V> {
        public final int size() {
            return size;
        }

        public final void clear() {
            HashMap.this.clear();
        }

        public final Iterator<V> iterator() {
            return new ValueIterator();
        }

        public final boolean contains(Object o) {
            return containsValue(o);
        }

        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super V> action) {
            Node<K, V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K, V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * funs-v 集合
     */
    final class EntrySet extends AbstractSet<Entry<K, V>> {
        public final int size() {
            return size;
        }

        public final void clear() {
            HashMap.this.clear();
        }

        public final Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Entry<?, ?> e = (Entry<?, ?>) o;
            Object key = e.getKey();
            Node<K, V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }

        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Entry<?, ?> e = (Entry<?, ?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }

        public final Spliterator<Entry<K, V>> spliterator() {
            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super Entry<K, V>> action) {
            Node<K, V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K, V> e = tab[i]; e != null; e = e.next)
                        action.accept(e);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * 迭代器
     * TODO
     */
    abstract class HashIterator {
        Node<K, V> next;        // next entry to return
        Node<K, V> current;     // current entry
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
            expectedModCount = modCount;
            Node<K, V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {
                } while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K, V> nextNode() {
            Node<K, V>[] t;
            Node<K, V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {
                } while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<K, V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }

    /**
     * 迭代器
     * TODO
     */
    final class KeyIterator extends HashIterator
            implements Iterator<K> {
        public final K next() {
            return nextNode().key;
        }
    }

    /**
     * 迭代器
     * TODO
     */
    final class ValueIterator extends HashIterator
            implements Iterator<V> {
        public final V next() {
            return nextNode().value;
        }
    }

    /**
     * 迭代器
     * TODO
     */
    final class EntryIterator extends HashIterator
            implements Iterator<Entry<K, V>> {
        public final Entry<K, V> next() {
            return nextNode();
        }
    }

}
