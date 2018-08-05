package com.king.learn.collection.mycollection.skiplist.demo3;

import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

abstract class AbstractSortedSet<E> extends AbstractSet<E> implements SortedSet<E> {

    public E first() {
        return null;
    }

    public E last() {
        return null;
    }

    public Iterator<E> iterator() {
        return null;
    }

    public SortedSet<E> headSet(E toElement) {
        return null;
    }

    public SortedSet<E> tailSet(E fromElement) {
        return null;
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
        return null;
    }

    public Comparator<? super E> comparator() {
        return null;  // uses natural ordering
    }
}