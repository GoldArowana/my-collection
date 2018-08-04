package com.king.learn.collection.mycollection.cache.lfu;

import java.util.ArrayList;

public class NodeListTest {
    public static void main(String[] args) {
        DoublyLinkedList list = new DoublyLinkedList();
        ArrayList<FrequencyNode> alist = new ArrayList<FrequencyNode>();
        alist.add(new FrequencyNode(0));
        alist.add(new FrequencyNode(1));
        alist.add(new FrequencyNode(2));
        alist.add(new FrequencyNode(3));
        alist.add(new FrequencyNode(4));
        alist.add(new FrequencyNode(5));
        alist.add(new FrequencyNode(6));
        list.append(alist.get(0));
        list.append(alist.get(1));
        list.prepend(alist.get(2));
        list.prepend(alist.get(3));
        list.printList();
        list.insertAfter(alist.get(3), alist.get(4));
        list.printList();
        list.insertAfter(alist.get(2), alist.get(5));
        list.printList();
        list.insertAfter(alist.get(1), alist.get(6));
        list.printList();
        list.remove(alist.get(1));
        list.remove(alist.get(6));
        list.remove(alist.get(3));
        list.printList();

        /* Output should be */
    	/*
    	[3] -> [2] -> [0] -> [1] ->
    	[3] -> [4] -> [2] -> [0] -> [1] ->
    	[3] -> [4] -> [2] -> [5] -> [0] -> [1] ->
    	[3] -> [4] -> [2] -> [5] -> [0] -> [1] -> [6] ->
    	[4] -> [2] -> [5] -> [0] ->
    	*/
    }
}
