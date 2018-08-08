package com.king.learn.collection.jdk8.string;

public class StringJoinerTest {
    public static void main(String[] args) {
        StringJoiner joiner = new StringJoiner("--", "[[[_", "_]]]");
        System.out.println("toString: " + joiner.toString());
        System.out.println("length: " + joiner.length());

        System.out.println("******************(1)********************");

        joiner.add("1");
        joiner.add("2");
        joiner.add("3");
        joiner.add("4");
        System.out.println("toString: " + joiner.toString());
        System.out.println("length: " + joiner.length());

        System.out.println("******************(2)********************");

        StringJoiner joiner2 = new StringJoiner("...");
        System.out.println("toString: " + joiner2.toString());
        System.out.println("length: " + joiner2.length());

        System.out.println("******************(3)********************");

        joiner2.add("a");
        joiner2.add("b");
        joiner2.add("c");
        System.out.println("toString: " + joiner2.toString());
        System.out.println("length: " + joiner2.length());

        System.out.println("******************(4)********************");

        joiner.merge(joiner2);
        System.out.println("toString: " + joiner.toString());

        System.out.println("******************(5)********************");

        StringJoiner joiner3 = new StringJoiner("==", "qianzhui", "houzhui");
        joiner3.add("壹");
        joiner3.add("贰");
        joiner3.add("叁");

        joiner.merge(joiner3);
        System.out.println("toString: " + joiner.toString());
        System.out.println("length: " + joiner.length());

        System.out.println("******************(6)********************");
        joiner.merge(joiner); // joiner.merge(this)
        System.out.println("toString: " + joiner.toString());
        System.out.println("length: " + joiner.length());
    }
}
