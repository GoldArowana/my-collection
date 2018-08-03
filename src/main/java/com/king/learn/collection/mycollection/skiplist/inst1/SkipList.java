package com.king.learn.collection.mycollection.skiplist.inst1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 实现跳跃表：能够对递增链表实现logN的查询时间
 * https://www.cnblogs.com/ljdblog/p/7645814.html
 */
public class SkipList<T> {
    private static final int MAX_LEVEL = 1 << 6;
    //跳跃表数据结构
    private Node<T> top;
    private int level = 0;
    //用于产生随机数的Random对象
    private Random random = new Random();

    public SkipList() {
        //创建默认初始高度的跳跃表
        this(4);
    }

    //跳跃表的初始化
    public SkipList(int level) {
        this.level = level;
        int i = level;
        Node<T> temp = null;
        Node<T> prev = null;
        while (i-- != 0) {
            temp = new Node<T>(null, Double.MIN_VALUE);
            temp.down = prev;
            prev = temp;
        }
        top = temp;//头节点
    }


    /**
     * 产生节点的高度。使用抛硬币
     */
    private int getRandomLevel() {
        int lev = 1;
        while (random.nextInt() % 2 == 0)
            lev += 1;
        return lev > MAX_LEVEL ? MAX_LEVEL : lev;
    }

    /**
     * 查找跳跃表中的一个值
     *
     * @param score
     * @return
     */
    public T get(double score) {
        Node<T> t = top;
        while (t != null) {
            if (t.score == score)
                return t.val;
            if (t.next == null) {
                if (t.down != null) {
                    t = t.down;
                    continue;
                } else
                    return null;
            }
            if (t.next.score > score) {
                t = t.down;
            } else
                t = t.next;
        }
        return null;
    }

    public void put(double score, T val) {
        //1，找到需要插入的位置
        Node<T> t = top, cur = null;//若cur不为空，表示当前score值的节点存在
        List<Node<T>> path = new ArrayList<>();//记录每一层当前节点的前驱节点
        while (t != null) {
            if (t.score == score) {
                cur = t;
                break;//表示存在该值的点，表示需要更新该节点
            }
            if (t.next == null) {
                path.add(t);//需要向下查找，先记录该节点
                if (t.down != null) {
                    t = t.down;
                    continue;
                } else {
                    break;
                }
            }
            if (t.next.score > score) {
                path.add(t);//需要向下查找，先记录该节点
                if (t.down == null) {
                    break;
                }
                t = t.down;
            } else
                t = t.next;
        }
        if (cur != null) {
            while (cur != null) {
                cur.val = val;
                cur = cur.down;
            }
        } else {//当前表中不存在score值的节点，需要从下到上插入
            int lev = getRandomLevel();
            if (lev > level) {//需要更新top这一列的节点数量，同时需要在path中增加这些新的首节点
                Node<T> temp = null;
                Node<T> prev = top;//前驱节点现在是top了
                while (level++ != lev) {
                    temp = new Node<T>(null, Double.MIN_VALUE);
                    path.add(0, temp);//加到path的首部
                    temp.down = prev;
                    prev = temp;
                }
                top = temp;//头节点
                level = lev;//level长度增加到新的长度
            }
            //从后向前遍历path中的每一个节点，在其后面增加一个新的节点
            Node<T> downTemp = null, temp = null, prev = null;
//            System.out.println("当前深度为"+level+",当前path长度为"+path.size());
            for (int i = level - 1; i >= level - lev; i--) {
                temp = new Node<T>(val, score);
                prev = path.get(i);
                temp.next = prev.next;
                prev.next = temp;
                temp.down = downTemp;
                downTemp = temp;
            }
        }
    }

    /**
     * 根据score的值来删除节点。
     *
     * @param score
     */
    public void delete(double score) {
        //1,查找到节点列的第一个节点的前驱
        Node<T> t = top;
        while (t != null) {
            if (t.next == null) {
                t = t.down;
                continue;
            }
            if (t.next.score == score) {
                // 在这里说明找到了该删除的节点
                t.next = t.next.next;
                t = t.down;
                //删除当前节点后，还需要继续查找之后需要删除的节点
                continue;
            }
            if (t.next.score > score)
                t = t.down;
            else
                t = t.next;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node<T> t = top, next = null;
        while (t != null) {
            next = t;
            while (next != null) {
                sb.append(next.score + " ");
                next = next.next;
            }
            sb.append("\n");
            t = t.down;
        }
        return sb.toString();
    }

    /**
     * 跳跃表的节点的构成
     */
    private static class Node<E> {
        E val;//存储的数据
        double score;//跳跃表按照这个分数值进行从小到大排序。
        Node<E> next, down;//next指针，指向下一层的指针

        Node(E val, double score) {
            this.val = val;
            this.score = score;
        }
    }
}