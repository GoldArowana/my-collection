package com.king.learn.collection.mycollection.cache.lfu;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 双向链表
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class DoublyLinkedList {
    private Node head = null;
    private Node tail = null;
    private int length = 0;

    /**
     * 头插法
     */
    public void prepend(Node node) {
        if (head == null) {
            tail = node;
            node.next = null;
        } else {
            node.next = head;
            head.prev = node;
        }
        head = node;
        node.prev = null;
        length++;
    }

    /**
     * 尾插
     */
    public void append(Node node) {
        if (tail == null) prepend(node);
        else {
            tail.next = node;
            node.next = null;
            node.prev = tail;
            tail = node;
            length++;
        }
    }

    /**
     * 在position节点后面插入node节点.
     */
    public void insertAfter(Node position, Node node) {
        if (position == tail) {
            append(node);
        } else {
            node.next = position.next;
            node.prev = position;
            position.next = node;
            node.next.prev = node;
            length++;
        }
    }

    /**
     * 删除节点node
     */
    public void remove(Node node) {
        if (node == tail && node == head) { /* single node in LinkedList */
            head = null;
            tail = null;
        } else if (node == tail) {
            tail = tail.prev;
            tail.next = null;
        } else if (node == head) {
            head = head.next;
            head.prev = null;
        } else {
            node.next.prev = node.prev;
            node.prev.next = node.next;
        }
        node.next = null;
        node.prev = null;
        length--;
    }

    /**
     * 打印链表
     */
    public void printList() {
        for (Node walk = head; walk != null; walk = walk.next)
            System.out.print("[" + walk + "] -> ");
        System.out.println();
    }
}

