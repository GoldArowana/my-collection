package com.king.learn.collection.jdk8.reference.soft.demo1;

import java.lang.ref.SoftReference;
import java.util.HashMap;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 * Date: Apr 26, 2005
 */
public class SoftCache {
    private HashMap map = new HashMap();

    public Object get(Object key) {
        SoftReference softRef = (SoftReference) map.get(key);

        if (softRef == null)
            return null;

        return softRef.get();
    }

    public Object put(Object key, Object value) {
        SoftReference softRef = (SoftReference) map.put(key, new SoftReference(value));

        if (softRef == null)
            return null;

        Object oldValue = softRef.get();
        softRef.clear();

        return oldValue;
    }

    public Object remove(Object key) {
        SoftReference softRef = (SoftReference) map.remove(key);

        if (softRef == null)
            return null;

        Object oldValue = softRef.get();
        softRef.clear();

        return oldValue;
    }
}