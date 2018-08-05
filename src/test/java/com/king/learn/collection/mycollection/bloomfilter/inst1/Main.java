package com.king.learn.collection.mycollection.bloomfilter.inst1;

public class Main {
    public static void main(String[] args) {
        BloomFilter fileter = new BloomFilter(7);
        System.out.println(fileter.addIfAbsent("1111111111111"));
        System.out.println(fileter.addIfAbsent("2222222222222222"));
        System.out.println(fileter.addIfAbsent("3333333333333333"));
        System.out.println(fileter.addIfAbsent("444444444444444"));
        System.out.println(fileter.addIfAbsent("5555555555555"));
        System.out.println(fileter.addIfAbsent("6666666666666"));
        System.out.println(fileter.addIfAbsent("1111111111111"));
        fileter.saveFilterToFile("/Users/arowana/Desktop/1.txt");
        fileter = fileter.readFilterFromFile("/Users/arowana/Desktop/1.txt");
        System.out.println(fileter.addIfAbsent("1111111111111"));
    }
}
