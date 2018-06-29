package com.king.learn.string;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 一个可变的字符串
 */
public abstract class MyAbstractStringBuilder implements Appendable, CharSequence {
    /**
     * 可开辟的最大空间.(虚拟机限制)
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    /**
     * 用于存储字符串
     */
    char[] value;
    /**
     * 数组value中有多少是使用中的
     */
    int count;

    /**
     * 根据传入的大小进行初始化数组.
     */
    MyAbstractStringBuilder(int capacity) {
        value = new char[capacity];
    }

    /**
     * 数组value中有多少是使用中的
     */
    @Override
    public int length() {
        return count;
    }

    /**
     * 当前对象的数组开辟了的空间是多少.
     */
    public int capacity() {
        return value.length;
    }

    /**
     * 确保当前的数组的空间最小是minimumCapacity.
     * 如果不够就去扩容.
     */
    public void ensureCapacity(int minimumCapacity) {
        if (minimumCapacity > 0)
            ensureCapacityInternal(minimumCapacity);
    }

    /**
     * 确保当前的数组的空间最小是minimumCapacity.
     * 如果不够就去扩容.
     */
    private void ensureCapacityInternal(int minimumCapacity) {
        // overflow-conscious code
        if (minimumCapacity - value.length > 0) {
            value = Arrays.copyOf(value,
                    newCapacity(minimumCapacity));
        }
    }

    /**
     * minCapacity合法时, 按(value.length << 1) + 2 扩展
     * 非法时(小于0, 或者超大时), 进行特殊处理.
     */
    private int newCapacity(int minCapacity) {
        // overflow-conscious code
        int newCapacity = (value.length << 1) + 2;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        return (newCapacity <= 0 || MAX_ARRAY_SIZE - newCapacity < 0)
                ? hugeCapacity(minCapacity)
                : newCapacity;
    }

    private int hugeCapacity(int minCapacity) {
        if (Integer.MAX_VALUE - minCapacity < 0) { // overflow
            throw new OutOfMemoryError();
        }
        return (minCapacity > MAX_ARRAY_SIZE)
                ? minCapacity : MAX_ARRAY_SIZE;
    }

    /**
     * 去掉数组中多余的(未使用)的空间.
     */
    public void trimToSize() {
        if (count < value.length) {
            value = Arrays.copyOf(value, count);
        }
    }


    /**
     * 重定义大小
     */
    public void setLength(int newLength) {
        // 小于0, 非法, 抛异常.
        if (newLength < 0) throw new StringIndexOutOfBoundsException(newLength);

        // 确保当前数组最小是newLength大小.
        ensureCapacityInternal(newLength);

        // 如果数组的使用部分的大小小于newLength, 那么用ascii 0 填充
        if (count < newLength) {
            Arrays.fill(value, count, newLength, '\0');
        }

        // 如果数组的使用部分大于newLength, 那么直接把这个大小赋值就好了
        // (有点像把指针往回拉的这样一个动作)
        count = newLength;
    }

    @Override
    public char charAt(int index) {
        // 检查越界
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        // 返回数组中对应的位置数据.
        return value[index];
    }

    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        if (srcBegin < 0)
            throw new StringIndexOutOfBoundsException(srcBegin);
        if ((srcEnd < 0) || (srcEnd > count))
            throw new StringIndexOutOfBoundsException(srcEnd);
        if (srcBegin > srcEnd)
            throw new StringIndexOutOfBoundsException("srcBegin > srcEnd");
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }

    public void setCharAt(int index, char ch) {
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        value[index] = ch;
    }

    public MyAbstractStringBuilder append(Object obj) {
        return append(String.valueOf(obj));
    }

    public MyAbstractStringBuilder append(String str) {
        if (str == null)
            return appendNull();
        int len = str.length();
        ensureCapacityInternal(count + len);
        str.getChars(0, len, value, count);
        count += len;
        return this;
    }

    public MyAbstractStringBuilder append(MyStringBuffer sb) {
        if (sb == null)
            return appendNull();
        int len = sb.length();
        ensureCapacityInternal(count + len);
        sb.getChars(0, len, value, count);
        count += len;
        return this;
    }

    /**
     * @since 1.8
     */
    MyAbstractStringBuilder append(MyAbstractStringBuilder asb) {
        if (asb == null)
            return appendNull();
        int len = asb.length();
        ensureCapacityInternal(count + len);
        asb.getChars(0, len, value, count);
        count += len;
        return this;
    }

    @Override
    public MyAbstractStringBuilder append(CharSequence s) {
        if (s == null)
            return appendNull();
        if (s instanceof MyAbstractStringBuilder)
            return this.append((MyAbstractStringBuilder) s);

        return this.append(s, 0, s.length());
    }

    private MyAbstractStringBuilder appendNull() {
        int c = count;
        ensureCapacityInternal(c + 4);
        final char[] value = this.value;
        value[c++] = 'n';
        value[c++] = 'u';
        value[c++] = 'l';
        value[c++] = 'l';
        count = c;
        return this;
    }

    @Override
    public MyAbstractStringBuilder append(CharSequence s, int start, int end) {
        if (s == null)
            s = "null";
        if ((start < 0) || (start > end) || (end > s.length()))
            throw new IndexOutOfBoundsException(
                    "start " + start + ", end " + end + ", s.length() "
                            + s.length());
        int len = end - start;
        ensureCapacityInternal(count + len);
        for (int i = start, j = count; i < end; i++, j++)
            value[j] = s.charAt(i);
        count += len;
        return this;
    }

    public MyAbstractStringBuilder append(char[] str) {
        int len = str.length;
        ensureCapacityInternal(count + len);
        System.arraycopy(str, 0, value, count, len);
        count += len;
        return this;
    }

    public MyAbstractStringBuilder append(char str[], int offset, int len) {
        if (len > 0)                // let arraycopy report AIOOBE for len < 0
            ensureCapacityInternal(count + len);
        System.arraycopy(str, offset, value, count, len);
        count += len;
        return this;
    }

    public MyAbstractStringBuilder append(boolean b) {
        if (b) {
            ensureCapacityInternal(count + 4);
            value[count++] = 't';
            value[count++] = 'r';
            value[count++] = 'u';
            value[count++] = 'e';
        } else {
            ensureCapacityInternal(count + 5);
            value[count++] = 'f';
            value[count++] = 'a';
            value[count++] = 'l';
            value[count++] = 's';
            value[count++] = 'e';
        }
        return this;
    }

    @Override
    public MyAbstractStringBuilder append(char c) {
        ensureCapacityInternal(count + 1);
        value[count++] = c;
        return this;
    }

    public MyAbstractStringBuilder delete(int start, int end) {
        if (start < 0)
            throw new StringIndexOutOfBoundsException(start);
        if (end > count)
            end = count;
        if (start > end)
            throw new StringIndexOutOfBoundsException();
        int len = end - start;
        if (len > 0) {
            System.arraycopy(value, start + len, value, start, count - end);
            count -= len;
        }
        return this;
    }

    /**
     * appendCodePoint(0x5b57);    // 0x5b57是“字”的unicode编码
     */
    public MyAbstractStringBuilder appendCodePoint(int codePoint) {
        final int count = this.count;

        if (Character.isBmpCodePoint(codePoint)) {
            ensureCapacityInternal(count + 1);
            value[count] = (char) codePoint;
            this.count = count + 1;
        } else if (Character.isValidCodePoint(codePoint)) {
            ensureCapacityInternal(count + 2);

            try {
                Method m = Character.class.getDeclaredMethod("toSurrogates", int.class, char[].class, int.class);
                m.setAccessible(true);
                m.invoke(null, codePoint, value, count);

//                Character.toSurrogates(codePoint, value, count);

            } catch (Exception e) {
                e.printStackTrace();
            }

            this.count = count + 2;
        } else {
            throw new IllegalArgumentException();
        }
        return this;
    }

    public MyAbstractStringBuilder deleteCharAt(int index) {
        if ((index < 0) || (index >= count))
            throw new StringIndexOutOfBoundsException(index);
        System.arraycopy(value, index + 1, value, index, count - index - 1);
        count--;
        return this;
    }

    /**
     * Replaces the characters in a substring of this sequence
     * with characters in the specified {@code String}. The substring
     * begins at the specified {@code start} and extends to the character
     * at index {@code end - 1} or to the end of the
     * sequence if no such character exists. First the
     * characters in the substring are removed and then the specified
     * {@code String} is inserted at {@code start}. (This
     * sequence will be lengthened to accommodate the
     * specified String if necessary.)
     *
     * @param start The beginning index, inclusive.
     * @param end   The ending index, exclusive.
     * @param str   String that will replace previous contents.
     * @return This object.
     * @throws StringIndexOutOfBoundsException if {@code start}
     *                                         is negative, greater than {@code length()}, or
     *                                         greater than {@code end}.
     */
    public MyAbstractStringBuilder replace(int start, int end, String str) {
        if (start < 0)
            throw new StringIndexOutOfBoundsException(start);
        if (start > count)
            throw new StringIndexOutOfBoundsException("start > length()");
        if (start > end)
            throw new StringIndexOutOfBoundsException("start > end");

        if (end > count)
            end = count;
        int len = str.length();
        int newCount = count + len - (end - start);
        ensureCapacityInternal(newCount);

        System.arraycopy(value, end, value, start + len, count - end);
        try {
            Method m = str.getClass().getDeclaredMethod("getChars", char[].class, int.class);
            m.setAccessible(true);
            m.invoke(str, value, start);

//            str.getChars(value, start);

        } catch (Exception e) {
            e.printStackTrace();
        }
        count = newCount;
        return this;
    }

    public String substring(int start) {
        return substring(start, count);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return substring(start, end);
    }

    public String substring(int start, int end) {
        if (start < 0)
            throw new StringIndexOutOfBoundsException(start);
        if (end > count)
            throw new StringIndexOutOfBoundsException(end);
        if (start > end)
            throw new StringIndexOutOfBoundsException(end - start);
        return new String(value, start, end - start);
    }

    public MyAbstractStringBuilder insert(int index, char[] str, int offset,
                                          int len) {
        if ((index < 0) || (index > length()))
            throw new StringIndexOutOfBoundsException(index);
        if ((offset < 0) || (len < 0) || (offset > str.length - len))
            throw new StringIndexOutOfBoundsException(
                    "offset " + offset + ", len " + len + ", str.length "
                            + str.length);
        ensureCapacityInternal(count + len);
        System.arraycopy(value, index, value, index + len, count - index);
        System.arraycopy(str, offset, value, index, len);
        count += len;
        return this;
    }

    public MyAbstractStringBuilder insert(int offset, Object obj) {
        return insert(offset, String.valueOf(obj));
    }

    public MyAbstractStringBuilder insert(int offset, String str) {
        if ((offset < 0) || (offset > length()))
            throw new StringIndexOutOfBoundsException(offset);
        if (str == null)
            str = "null";
        int len = str.length();
        ensureCapacityInternal(count + len);
        System.arraycopy(value, offset, value, offset + len, count - offset);

        try {
            Method m = str.getClass().getDeclaredMethod("getChars", char[].class, int.class);
            m.setAccessible(true);
            m.invoke(str, value, offset);

//            str.getChars(value, offset);
        } catch (Exception e) {
            e.printStackTrace();
        }

        count += len;
        return this;
    }

    public MyAbstractStringBuilder insert(int offset, char[] str) {
        if ((offset < 0) || (offset > length()))
            throw new StringIndexOutOfBoundsException(offset);
        int len = str.length;
        ensureCapacityInternal(count + len);
        System.arraycopy(value, offset, value, offset + len, count - offset);
        System.arraycopy(str, 0, value, offset, len);
        count += len;
        return this;
    }

    public MyAbstractStringBuilder insert(int dstOffset, CharSequence s) {
        if (s == null)
            s = "null";
        if (s instanceof String)
            return this.insert(dstOffset, (String) s);
        return this.insert(dstOffset, s, 0, s.length());
    }

    public MyAbstractStringBuilder insert(int dstOffset, CharSequence s, int start, int end) {
        if (s == null)
            s = "null";
        if ((dstOffset < 0) || (dstOffset > this.length()))
            throw new IndexOutOfBoundsException("dstOffset " + dstOffset);
        if ((start < 0) || (end < 0) || (start > end) || (end > s.length()))
            throw new IndexOutOfBoundsException(
                    "start " + start + ", end " + end + ", s.length() "
                            + s.length());
        int len = end - start;
        ensureCapacityInternal(count + len);
        System.arraycopy(value, dstOffset, value, dstOffset + len,
                count - dstOffset);
        for (int i = start; i < end; i++)
            value[dstOffset++] = s.charAt(i);
        count += len;
        return this;
    }

    public MyAbstractStringBuilder insert(int offset, boolean b) {
        return insert(offset, String.valueOf(b));
    }

    public MyAbstractStringBuilder insert(int offset, char c) {
        ensureCapacityInternal(count + 1);
        System.arraycopy(value, offset, value, offset + 1, count - offset);
        value[offset] = c;
        count += 1;
        return this;
    }

    public MyAbstractStringBuilder insert(int offset, int i) {
        return insert(offset, String.valueOf(i));
    }

    public MyAbstractStringBuilder insert(int offset, long l) {
        return insert(offset, String.valueOf(l));
    }

    public MyAbstractStringBuilder insert(int offset, float f) {
        return insert(offset, String.valueOf(f));
    }

    public MyAbstractStringBuilder insert(int offset, double d) {
        return insert(offset, String.valueOf(d));
    }

    public int indexOf(String str) {
        return indexOf(str, 0);
    }

    public int indexOf(String str, int fromIndex) {
        try {
            Method m = String.class.getDeclaredMethod("indexOf", char[].class, int.class, int.class, String.class, int.class);
            m.setAccessible(true);
            return (int) m.invoke(null, value, 0, count, str, fromIndex);
//            return String.indexOf(value, 0, count, str, fromIndex);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("反射异常");
        }

    }

    public int lastIndexOf(String str) {
        return lastIndexOf(str, count);
    }

    public int lastIndexOf(String str, int fromIndex) {
        try {
            Method m = String.class.getDeclaredMethod("lastIndexOf", char[].class, int.class, int.class, String.class, int.class);
            m.setAccessible(true);
            return (int) m.invoke(null, value, 0, count, str, fromIndex);
//            return String.lastIndexOf(value, 0, count, str, fromIndex);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("反射异常");
        }
    }

    /**
     * Causes this character sequence to be replaced by the reverse of
     * the sequence. If there are any surrogate pairs included in the
     * sequence, these are treated as single characters for the
     * reverse operation. Thus, the order of the high-low surrogates
     * is never reversed.
     * <p>
     * Let <i>n</i> be the character length of this character sequence
     * (not the length in {@code char} values) just prior to
     * execution of the {@code reverse} method. Then the
     * character at index <i>k</i> in the new character sequence is
     * equal to the character at index <i>n-k-1</i> in the old
     * character sequence.
     *
     * <p>Note that the reverse operation may result in producing
     * surrogate pairs that were unpaired low-surrogates and
     * high-surrogates before the operation. For example, reversing
     * "\u005CuDC00\u005CuD800" produces "\u005CuD800\u005CuDC00" which is
     * a valid surrogate pair.
     *
     * @return a reference to this object.
     */
    public MyAbstractStringBuilder reverse() {
        boolean hasSurrogates = false;
        int n = count - 1;
        for (int j = (n - 1) >> 1; j >= 0; j--) {
            int k = n - j;
            char cj = value[j];
            char ck = value[k];
            value[j] = ck;
            value[k] = cj;
            if (Character.isSurrogate(cj) ||
                    Character.isSurrogate(ck)) {
                hasSurrogates = true;
            }
        }
        if (hasSurrogates) {
            reverseAllValidSurrogatePairs();
        }
        return this;
    }

    /**
     * Outlined helper method for reverse()
     */
    private void reverseAllValidSurrogatePairs() {
        for (int i = 0; i < count - 1; i++) {
            char c2 = value[i];
            if (Character.isLowSurrogate(c2)) {
                char c1 = value[i + 1];
                if (Character.isHighSurrogate(c1)) {
                    value[i++] = c1;
                    value[i] = c2;
                }
            }
        }
    }

    @Override
    public abstract String toString();

}
