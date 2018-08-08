package com.king.learn.collection.jdk8.string;

public class StringJoinerTest2 {
    public static void main(String[] args) {
        StringJoiner joiner = new StringJoiner("--", "[[[_", "_]]]");
        joiner.add("1").add("2").add("3").add("4");

        StringJoiner joiner2 = new StringJoiner("...");
        joiner2.add("a").add("b").add("c");

        joiner.merge(joiner2);
        System.out.println(joiner.toString());
    }
}
