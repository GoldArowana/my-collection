package com.king.learn.collection.jdk8;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;

public class LinkedHashSet<E> extends HashSet<E>
        implements Set<E>, Cloneable, Serializable {

    private static final long serialVersionUID = -2851667679971038690L;

    public LinkedHashSet(int initialCapacity, float loadFactor) {
        // 这里面实例化的是LinkedHashMap
        super(initialCapacity, loadFactor, true);
    }

    public LinkedHashSet(int initialCapacity) {
        // 这里面实例化的是LinkedHashMap
        super(initialCapacity, .75f, true);
    }

    public LinkedHashSet() {
        // 这里面实例化的是LinkedHashMap
        super(16, .75f, true);
    }

    public LinkedHashSet(Collection<? extends E> c) {
        // 这里面实例化的是LinkedHashMap
        super(Math.max(2 * c.size(), 11), .75f, true);
        addAll(c);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.ORDERED);
    }
}
