package com.king.learn.collection.mycollection.skiplist.inst3;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SkipList<E extends Comparable<E>> extends AbstractSortedSet<E> {
    private static final double PROBABILITY = 0.5;
    private Node<E> head;
    private int maxLevel;
    private int size;

    public SkipList() {
        size = 0;
        maxLevel = 0;
        head = new Node<E>(null);
        head.getNextNodes().add(null);
    }

    public Node getHead() {
        return head;
    }

    // Adds e to the skiplist.
    // Returns false if already in skiplist, true otherwise.
    public boolean add(E e) {
        if (contains(e)) return false;
        size++;
        // random number from 0 to maxLevel+1 (inclusive)
        int level = 0;
        while (Math.random() < PROBABILITY)
            level++;
        while (level > maxLevel) { // should only happen once
            head.getNextNodes().add(null);
            maxLevel++;
        }
        Node newNode = new Node<E>(e);
        Node current = head;
        do {
            current = findNext(e, current, level);
            newNode.getNextNodes().add(0, current.getNextNodes().get(level));
            current.getNextNodes().set(level, newNode);
        } while (level-- > 0);
        return true;
    }

    // Returns the skiplist node with greatest value <= e
    private Node find(E e) {
        return find(e, head, maxLevel);
    }

    // Returns the skiplist node with greatest value <= e
    // Starts at node start and level
    private Node find(E e, Node current, int level) {
        do {
            current = findNext(e, current, level);
        } while (level-- > 0);
        return current;
    }

    // Returns the node at a given level with highest value less than e
    private Node findNext(E e, Node current, int level) {
        Node next = (Node) current.getNextNodes().get(level);
        while (next != null) {
            E value = (E) next.getValue();
            if (lessThan(e, value)) // e < value
                break;
            current = next;
            next = (Node) current.getNextNodes().get(level);
        }
        return current;
    }

    public int size() {
        return size;
    }

    public boolean contains(Object o) {
        E e = (E) o;
        Node node = find(e);
        return node != null && node.getValue() != null && equalTo((E) node.getValue(), e);
    }

    public Iterator<E> iterator() {
        return new SkipListIterator(this);
    }

    private boolean lessThan(E a, E b) {
        return a.compareTo(b) < 0;
    }

    private boolean equalTo(E a, E b) {
        return a.compareTo(b) == 0;
    }

    private boolean greaterThan(E a, E b) {
        return a.compareTo(b) > 0;
    }

    public String toString() {
        StringBuilder s = new StringBuilder("SkipList: ");
        for (Object o : this)
            s.append(o).append(", ");
        return s.substring(0, s.length() - 2);
    }

    @Getter
    @Setter
    class Node<E> {
        public List<Node<E>> nextNodes = new ArrayList<>();
        private E value;

        public Node(E value) {
            this.value = value;
        }

        public int level() {
            return nextNodes.size() - 1;
        }

        public String toString() {
            return "SLN: " + value;
        }
    }

    class SkipListIterator<E extends Comparable<E>> implements Iterator<E> {
        SkipList<E> list;
        Node<E> current;

        public SkipListIterator(SkipList<E> list) {
            this.list = list;
            this.current = list.getHead();
        }

        public boolean hasNext() {
            return current.getNextNodes().get(0) != null;
        }

        public E next() {
            current = current.getNextNodes().get(0);
            return current.getValue();
        }

        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }
}
