package com.king.learn.concurrent.extention.c.edu.vt.ece.locks;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PriorityCLH implements Lock {
    //final MyThreadLocal<QNode> myPred;
    public static int THREAD_COUNT;
    final ThreadLocal<QNode> myNode;
    //final MyThreadLocal<Integer> mycount;
    //final AtomicReference<QNode> tail;
    final AtomicReference<QNode> tail = new AtomicReference<QNode>(new QNode());
    AtomicBoolean state = new AtomicBoolean(false);
    PriorityBlockingQueue<QNode> queue = new PriorityBlockingQueue<QNode>();
    long time = 0;

    //public static QNode QNode_object;
    public PriorityCLH() {
        //this.tail = new AtomicReference<QNode>();

        this.myNode = new ThreadLocal<QNode>() {
            protected QNode initialValue() {
                return new QNode();
            }
        };


    }

    public void lock() {

        final QNode qnode = this.myNode.get();
        qnode.priority = Thread.currentThread().getPriority();
        //qnode.my_count = qnode.my_count + 1;
        queue.add(qnode);
        //AtomicInteger a =  queue.size();
        //while(queue.size()<THREAD_COUNT);
        while (!(queue.peek() == qnode)) ;
        while (state.getAndSet(true)) ;
        //( queue.peek().getPriority() == qnode.getPriority())); // && Integer.toHexString(System.identityHashCode(queue.peek())).equals(Integer.toHexString(System.identityHashCode(qnode)))  );

        //System.out.println("Thread entered" + qnode.getPriority());
    }

    public void unlock() {
        final QNode qnode1 = this.myNode.get();
        //while(qnode1.getmy_count()<100);
        state.set(false);
        queue.remove(qnode1);

        //System.out.println(THREAD_COUNT);
    }

//    public static class QNode {  
//        private volatile int priority;
//        public int getPriority(){
//        	return priority;
//        }
//    }  
//    public boolean trylock(){
//    	final QNode qnode = this.myNode.get(); 
//        qnode.priority = Thread.currentThread().getPriority();
//        //qnode.my_count = qnode.my_count + 1;
//        queue.add(qnode);
//        //AtomicInteger a =  queue.size();
//        //while(queue.size()<THREAD_COUNT);
//        while((!(queue.peek() == qnode))&& state.getAndSet(true)){
//        	while(System.currentTimeMillis() - time >= 20){
//        		return false;
//        	}
//        	}
//        
//        	return true;
//    }

}

