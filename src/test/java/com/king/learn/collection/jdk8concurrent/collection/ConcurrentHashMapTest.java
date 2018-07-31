package com.king.learn.collection.jdk8concurrent.collection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("all")
public class ConcurrentHashMapTest {
    public static void main(String[] args) {
        MyConcurrentHashMap myConcurrentHashMap = new MyConcurrentHashMap();
        for (int i = 0; i < 20; i++) {
            myConcurrentHashMap.put(new Stu(i, 22222 + (16) * (i % 2)), new Object());
        }
    }
}

@Getter
@Setter
@AllArgsConstructor
class Stu {
    int eq;
    int hash;

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Stu)) {
            return false;
        }
        return eq == ((Stu) obj).eq;
    }
}