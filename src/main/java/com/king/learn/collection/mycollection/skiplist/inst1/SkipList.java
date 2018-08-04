package com.king.learn.collection.mycollection.skiplist.inst1;

import com.king.learn.collection.mycollection.random.RandomNumber;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现跳跃表：能够对递增链表实现logN的查询时间
 * https://www.cnblogs.com/ljdblog/p/7645814.html
 */
public class SkipList<T> {
    private static final RandomNumber random = new RandomNumber(333, 100); // 随机数生成器
    private static final int MAX_LEVEL = 1 << 6;
    private Node<T> top;
    private int level;

    public SkipList() {
        this(4); // 默认初始是4层
    }

    public SkipList(int level) {
        int i = this.level = level;
        for (Node<T> prev = null; i > 0; i--)
            top = prev = new Node<>(null, Double.MIN_VALUE, null, prev);
    }

    /**
     * 产生节点的高度。使用抛硬币
     */
    private int getRandomLevel() {
        int lev = 1;
        while (random.trueOrFalse()) lev++;
        return lev > MAX_LEVEL ? MAX_LEVEL : lev;
    }

    public T get(double score) {
        for (Node<T> t = top; t != null; )
            if (t.score == score) return t.val;
            else if (t.next == null) t = t.down;
            else t = (t.next.score > score ? t.down : t.next);
        return null;
    }

    public void put(double score, T val) {
        List<Node<T>> path = new ArrayList<>(); // 记录每一层当前节点的前驱节点
        Node<T> cur = null; // 若cur不为空，表示当前score值的节点存在
        for (Node<T> t = top; t != null && cur == null; )
            if (t.score == score) cur = t;            //  表示存在该值的点，表示需要更新该节点
            else if (t.next == null || t.next.score > score) {
                path.add(t); // 需要向下查找，先记录该节点
                if ((t = t.down) == null) break;
            } else t = t.next;

        // cur不为空就表示当前score值的节点存在
        if (cur != null) {
            while (cur != null) {
                cur.val = val;
                cur = cur.down;
            }
            return;
        }

        //  cur为空就表示当前表中不存在score值的节点，需要从下到上插入
        int lev = getRandomLevel();
        for (Node<T> prev = top; level < lev; level++)   //  如果level < lev , 那么需要更新top这一列的节点数量，同时需要在path中增加这些新的首节点
            path.add(0, top = prev = new Node<>(null, Double.MIN_VALUE, null, prev)); // 加到path的首部

        Node<T> downTemp = null;
        for (int i = level - 1; i >= level - lev; i--) {    // 从后向前遍历path中的每一个节点，在其后面增加一个新的节点
            Node<T> prev = path.get(i);
            downTemp = prev.next = new Node<>(val, score, prev.next, downTemp);
        }
    }

    public void delete(double score) {
        for (Node<T> t = top; t != null; ) {
            if (t.next == null || (t.next.score == score && t.deleteNext()) || t.next.score > score)
                t = t.down;
            else
                t = t.next;
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Node<T> t = top; t != null; sb.append("\n"), t = t.down)
            for (Node<T> next = t; next != null; next = next.next)
                sb.append(next.score).append(" ");
        return sb.toString();
    }

    @AllArgsConstructor  //  全参数的构造器
    private static class Node<E> {
        E val; // 存储的数据
        double score; // 跳跃表按照这个分数值进行从小到大排序。
        Node<E> next, down; // next指针，指向下一层的指针

        private boolean deleteNext() {
            this.next = this.next.next;
            return true;
        }
    }
}