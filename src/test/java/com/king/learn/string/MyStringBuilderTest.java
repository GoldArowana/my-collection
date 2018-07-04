package com.king.learn.string;

import com.king.learn.string.MyStringBuffer;
import com.king.learn.string.MyStringBuilder;
import org.junit.Test;

public class MyStringBuilderTest {
    @Test
    public void test() {
        MyStringBuilder stringBuilder = new MyStringBuilder();
        System.out.println(stringBuilder);


        stringBuilder = new MyStringBuilder(17);
        System.out.println(stringBuilder);


        stringBuilder = new MyStringBuilder("asdf");
        System.out.println(stringBuilder);


        stringBuilder = new MyStringBuilder(stringBuilder);
        System.out.println(stringBuilder);

    }

    @Test
    public void test2() {
        MyStringBuilder stringBuilder = new MyStringBuilder("asdf");
        stringBuilder.append("123");
        System.out.println(stringBuilder);

        stringBuilder = new MyStringBuilder("asdf");
        stringBuilder.append(new MyStringBuffer("111222"));
        System.out.println(stringBuilder);

        stringBuilder = new MyStringBuilder("asdf");
        stringBuilder.append(new MyStringBuilder("444555"));
        System.out.println(stringBuilder);

        stringBuilder = new MyStringBuilder("asdf");
        stringBuilder.append("1234567890", 3, 7);
        System.out.println(stringBuilder);

        stringBuilder = new MyStringBuilder("9090");
        stringBuilder.append(new char[]{'e', 'a', 'b', 'c'});
        System.out.println(stringBuilder);

        stringBuilder = new MyStringBuilder("9090");
        stringBuilder.append(new char[]{'e', 'a', 'b', 'c', 'd', 'f'}, 2, 4);
        System.out.println(stringBuilder);

        stringBuilder = new MyStringBuilder("9090");
        stringBuilder.append(true);
        System.out.println(stringBuilder);

        stringBuilder = new MyStringBuilder("9090");
        stringBuilder.append('a');
        System.out.println(stringBuilder);

        stringBuilder = new MyStringBuilder();
        stringBuilder.append(11.24f);
        System.out.println(stringBuilder);

    }

    @Test
    public void test3() {
        MyStringBuilder stringBuilder = new MyStringBuilder("0123456789");
        stringBuilder.delete(3, 6);
        System.out.println(stringBuilder);

        stringBuilder = new MyStringBuilder("0123456789");
        stringBuilder.deleteCharAt(5);
        System.out.println(stringBuilder);

        stringBuilder = new MyStringBuilder("0123456789");
        stringBuilder.replace(4, 8, "-");
        System.out.println(stringBuilder);
    }

    @Test
    public void test4() {
        MyStringBuilder stringBuilder = new MyStringBuilder("0123456789~+=");
        stringBuilder.insert(2, "abcdefghijklmnopqrs", 2, 5);
        System.out.println(stringBuilder);

        stringBuilder = new MyStringBuilder("0123456789");
        stringBuilder.insert(2, "***");
        System.out.println(stringBuilder);
    }

}