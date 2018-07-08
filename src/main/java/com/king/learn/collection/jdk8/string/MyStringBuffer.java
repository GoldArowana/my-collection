package com.king.learn.collection.jdk8.string;

import java.util.Arrays;

public final class MyStringBuffer extends MyAbstractStringBuilder {

    /**
     * A cache of the last value returned by toString. Cleared
     * whenever the StringBuffer is modified.
     */
    private transient char[] toStringCache;

    public MyStringBuffer() {
        super(16);
    }

    public MyStringBuffer(int capacity) {
        super(capacity);
    }

    public MyStringBuffer(String str) {
        super(str.length() + 16);
        append(str);
    }


    public MyStringBuffer(CharSequence seq) {
        this(seq.length() + 16);
        append(seq);
    }

    @Override
    public synchronized int length() {
        return count;
    }

    @Override
    public synchronized int capacity() {
        return value.length;
    }


    @Override
    public synchronized void ensureCapacity(int minimumCapacity) {
        super.ensureCapacity(minimumCapacity);
    }

    @Override
    public synchronized void trimToSize() {
        super.trimToSize();
    }

    @Override
    public synchronized void setLength(int newLength) {
        toStringCache = null;
        super.setLength(newLength);
    }

    @Override
    public synchronized char charAt(int index) {
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        return value[index];
    }

    @Override
    public synchronized void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        super.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    @Override
    public synchronized void setCharAt(int index, char ch) {
        if ((index < 0) || (index >= count)) throw new StringIndexOutOfBoundsException(index);
        toStringCache = null;
        value[index] = ch;
    }

    @Override
    public synchronized MyStringBuffer append(Object obj) {
        toStringCache = null;
        super.append(String.valueOf(obj));
        return this;
    }

    @Override
    public synchronized MyStringBuffer append(String str) {
        toStringCache = null;
        super.append(str);
        return this;
    }

    public synchronized MyStringBuffer append(MyStringBuffer sb) {
        toStringCache = null;
        super.append(sb);
        return this;
    }

    @Override
    synchronized MyStringBuffer append(MyAbstractStringBuilder asb) {
        toStringCache = null;
        super.append(asb);
        return this;
    }

    @Override
    public synchronized MyStringBuffer append(CharSequence s) {
        toStringCache = null;
        super.append(s);
        return this;
    }

    @Override
    public synchronized MyStringBuffer append(CharSequence s, int start, int end) {
        toStringCache = null;
        super.append(s, start, end);
        return this;
    }

    @Override
    public synchronized MyStringBuffer append(char[] str) {
        toStringCache = null;
        super.append(str);
        return this;
    }

    @Override
    public synchronized MyStringBuffer append(char[] str, int offset, int len) {
        toStringCache = null;
        super.append(str, offset, len);
        return this;
    }

    @Override
    public synchronized MyStringBuffer append(boolean b) {
        toStringCache = null;
        super.append(b);
        return this;
    }

    @Override
    public synchronized MyStringBuffer append(char c) {
        toStringCache = null;
        super.append(c);
        return this;
    }

    @Override
    public synchronized MyStringBuffer appendCodePoint(int codePoint) {
        toStringCache = null;
        super.appendCodePoint(codePoint);
        return this;
    }

    @Override
    public synchronized MyStringBuffer delete(int start, int end) {
        toStringCache = null;
        super.delete(start, end);
        return this;
    }

    @Override
    public synchronized MyStringBuffer deleteCharAt(int index) {
        toStringCache = null;
        super.deleteCharAt(index);
        return this;
    }

    @Override
    public synchronized MyStringBuffer replace(int start, int end, String str) {
        toStringCache = null;
        super.replace(start, end, str);
        return this;
    }

    @Override
    public synchronized String substring(int start) {
        return substring(start, count);
    }

    @Override
    public synchronized CharSequence subSequence(int start, int end) {
        return super.substring(start, end);
    }

    @Override
    public synchronized String substring(int start, int end) {
        return super.substring(start, end);
    }

    @Override
    public synchronized MyStringBuffer insert(int index, char[] str, int offset, int len) {
        toStringCache = null;
        super.insert(index, str, offset, len);
        return this;
    }

    @Override
    public synchronized MyStringBuffer insert(int offset, Object obj) {
        toStringCache = null;
        super.insert(offset, String.valueOf(obj));
        return this;
    }

    @Override
    public synchronized MyStringBuffer insert(int offset, String str) {
        toStringCache = null;
        super.insert(offset, str);
        return this;
    }

    @Override
    public synchronized MyStringBuffer insert(int offset, char[] str) {
        toStringCache = null;
        super.insert(offset, str);
        return this;
    }

    @Override
    public MyStringBuffer insert(int dstOffset, CharSequence s) {
        // Note, synchronization achieved via invocations of other StringBuffer methods
        // after narrowing of s to specific type
        // Ditto for toStringCache clearing
        super.insert(dstOffset, s);
        return this;
    }

    @Override
    public synchronized MyStringBuffer insert(int dstOffset, CharSequence s, int start, int end) {
        toStringCache = null;
        super.insert(dstOffset, s, start, end);
        return this;
    }

    @Override
    public MyStringBuffer insert(int offset, boolean b) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of b to String by super class method
        // Ditto for toStringCache clearing
        super.insert(offset, b);
        return this;
    }

    @Override
    public synchronized MyStringBuffer insert(int offset, char c) {
        toStringCache = null;
        super.insert(offset, c);
        return this;
    }

    @Override
    public MyStringBuffer insert(int offset, int i) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of i to String by super class method
        // Ditto for toStringCache clearing
        super.insert(offset, i);
        return this;
    }

    @Override
    public MyStringBuffer insert(int offset, long l) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of l to String by super class method
        // Ditto for toStringCache clearing
        super.insert(offset, l);
        return this;
    }

    @Override
    public MyStringBuffer insert(int offset, float f) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of f to String by super class method
        // Ditto for toStringCache clearing
        super.insert(offset, f);
        return this;
    }

    @Override
    public MyStringBuffer insert(int offset, double d) {
        // Note, synchronization achieved via invocation of StringBuffer insert(int, String)
        // after conversion of d to String by super class method
        // Ditto for toStringCache clearing
        super.insert(offset, d);
        return this;
    }

    @Override
    public int indexOf(String str) {
        // Note, synchronization achieved via invocations of other StringBuffer methods
        return super.indexOf(str);
    }

    @Override
    public synchronized int indexOf(String str, int fromIndex) {
        return super.indexOf(str, fromIndex);
    }

    @Override
    public int lastIndexOf(String str) {
        // Note, synchronization achieved via invocations of other StringBuffer methods
        return lastIndexOf(str, count);
    }

    @Override
    public synchronized int lastIndexOf(String str, int fromIndex) {
        return super.lastIndexOf(str, fromIndex);
    }

    @Override
    public synchronized MyStringBuffer reverse() {
        toStringCache = null;
        super.reverse();
        return this;
    }

    @Override
    public synchronized String toString() {
        if (toStringCache == null) {
            toStringCache = Arrays.copyOfRange(value, 0, count);
        }
        return new String(toStringCache);
    }
}
