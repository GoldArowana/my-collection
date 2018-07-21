package com.king.learn.collection.myconcurrent.sync;

import java.text.DecimalFormat;
import java.util.concurrent.CyclicBarrier;

public class TestMain {
    static final int NUMBER_OF_TEST = 2;
    static final int[] tableSizeArray = {4};
    static final int numThreads = 4;//Runtime.getRuntime().availableProcessors();
    static final int loopCount = 1_000_000;
    static final int S_TIME = 1_000;

    //return executeTime
    public static long testLockAndAtomicTable(Table table) {
        MyRunnable[] runnables = new MyRunnable[numThreads];
        CyclicBarrier barrier = new CyclicBarrier(numThreads + 1);

        for (int i = 0; i < runnables.length; i++) {
            runnables[i] = new MyRunnable(i, table, barrier);
        }

        Thread[] threads = createThreads(numThreads, runnables);

        try {
            barrier.await();
        } catch (Exception e) {
            System.out.println("TestMain/waitForReady : BrokenBarriar or InterruptedException");
            e.printStackTrace();
        }

        long start = System.currentTimeMillis();

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (final InterruptedException xx) {
                throw new RuntimeException("unexpected", xx);
            }
        }
        long end = System.currentTimeMillis();
        for (int i = 0; i < tableSizeArray[0]; i++) {
            System.out.println(table.getData(i));
        }
        System.out.println();
        return (end - start);
    }

    public static Thread[] createThreads(final int numThreads, final Runnable[] runnables) {
        final Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(runnables[i]);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        return threads;
    }

    public static double getVariance(long[] execTimes, double avg) {
        double variance = 0.0d;

        for (int i = 0; i < execTimes.length; i++) {
            variance += (execTimes[i] - avg) * (execTimes[i] - avg);
        }
        variance = variance / execTimes.length;

        return variance;
    }

    public static void printTimeAnalyze(long[] execTime) {
        DecimalFormat decimalFormat = new DecimalFormat(".##");
        long sum = 0;
        long max = 0;
        long min = Long.MAX_VALUE;

        for (int j = 0; j < NUMBER_OF_TEST; j++) {
            max = Math.max(max, execTime[j]);
            min = Math.min(min, execTime[j]);
            sum += execTime[j];

            System.out.print(execTime[j] + " ");
        }
        double avg = ((double) sum / NUMBER_OF_TEST);
        double variance = getVariance(execTime, avg);

        System.out.print("\t\t AVG : " + decimalFormat.format(avg) + "\t\tVariance : " + decimalFormat.format(variance) + "\t\t Max : " + max + "\t\t Min : " + min + "\n");
    }

    public static void main(String[] args) {
        long[] execTime = new long[NUMBER_OF_TEST];

        System.out.println("LOOP COUNT : " + loopCount);
        System.out.println("NUMBER OF THREAD : " + numThreads);
        System.out.println("SPEND_TIME : " + S_TIME);
/*
        testLockAndAtomicTable(new MutexTable(128));
        testLockAndAtomicTable(new MutexTable(128));
        testLockAndAtomicTable(new MutexTable(128));

        for (int i = 0; i < tableSizeArray.length; i++) {
            System.out.println("\nMUTEX\counter");
            for (int itr = 0; itr < NUMBER_OF_TEST; itr++) {
                execTime[itr] = testLockAndAtomicTable(new MutexTable(tableSizeArray[i]));
            }
            printTimeAnalyze(execTime);
        }
*/
/*
        testLockAndAtomicTable(new AtomicTable(4));
        testLockAndAtomicTable(new AtomicTable(4));
        testLockAndAtomicTable(new AtomicTable(4));


        for (int i = 0; i < tableSizeArray.length; i++) {
            System.out.println("\nATOMIC\counter");
            for (int itr = 0; itr < NUMBER_OF_TEST; itr++) {
                execTime[itr] = testLockAndAtomicTable(new AtomicTable(tableSizeArray[i]));
            }
            printTimeAnalyze(execTime);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

*/

        testLockAndAtomicTable(new SyncKeywordTable(4));
        testLockAndAtomicTable(new SyncKeywordTable(4));
        testLockAndAtomicTable(new SyncKeywordTable(4));


        for (int i = 0; i < tableSizeArray.length; i++) {
            System.out.println("\nSYNC_KEYWORD\n");
            for (int itr = 0; itr < NUMBER_OF_TEST; itr++) {
                execTime[itr] = testLockAndAtomicTable(new SyncKeywordTable(tableSizeArray[i]));
            }
            printTimeAnalyze(execTime);
        }

    }

    static class MyRunnable implements Runnable {
        int threadId;
        Table table;
        CyclicBarrier barrier;
        int sum = 0;

        public MyRunnable(int id, Table table, CyclicBarrier barrier) {
            this.threadId = id;
            this.table = table;
            this.barrier = barrier;
        }

        void waitForReady() {
            try {
                barrier.await();
            } catch (Exception e) {
                System.out.println("TestMain/waitForReady : BrokenBarriar or InterruptedException");
                e.printStackTrace();
            }
        }

        public void spendTime() {
            for (int i = 0; i < S_TIME; i++) {
                sum += i;
            }
        }

        @Override
        public void run() {
            waitForReady();
            for (int i = 0; i < loopCount; i++) {
                table.incrementEntry(threadId, 2);
                long start = System.nanoTime();
                spendTime();
                System.out.println(System.nanoTime() - start);
            }
        }
    }
}


