/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.util.collection;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;

import com.google.common.annotations.Beta;

/**
 * An array of Objects that expands as needed to hold new data.<P>
 *
 * How does this differ from ArrayList, you ask? Well, it behaves like an array and not a list
 * (and thus does not implement List). While an ArrayList will shift values right to fit new
 * values, an ExpandableArray will simply overwrite values. And while if you try to set at a value past the
 * end of an array list, you'll get an ArrayIndexOutOfBounds exception, if you do it on an
 * ExpandableArray, it will happily set it for you. This is fairly similar to an IntHashMap,
 * but more lightweight. Examples:<P>
 * <PRE>
 *
 *    arrayList.set(0, two);
 *    arrayList.set(0, one);
 *    arrayList.set(0, zero);
 *    arrayList.get(0) == zero
 *    arrayList.get(1) == one
 *    arrayList.get(2) == two
 *
 * </PRE>
 * but:<P>
 * <PRE>
 *
 *    expandableArray.set(0, two);
 *    expandableArray.set(0, one);
 *    expandableArray.set(0, zero);
 *    expandableArray.get(0) == zero
 *    expandableArray.get(1) == null
 *    expandableArray.get(2) == null
 *
 * </PRE>
 * and:<P>
 * <PRE>
 *
 *    arrayList.set(99, ninetyNine);    // throws ArrayIndexOutOfBounds
 *
 * </PRE>
 * but:<P>
 * <PRE>
 *
 *    expandableArray.set(99, ninetyNine);
 *    expandableArray.get(99) == ninetyNine
 *
 * </PRE>
 * 
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author shansma
 */
@Beta
@SuppressWarnings("rawtypes")  // TODO: Figure this out
public class ExpandableArray<E> implements Serializable, Iterable<E>, Comparable<ExpandableArray> {
    private static final long serialVersionUID = 1;

    private E[] data;
    private int maxNonNullIndex = -1;
    private boolean isMutable = true;
    private final float factor;

    public ExpandableArray() {
        this(16);
    }

    public ExpandableArray(int initialSize) {
        this(initialSize, 3.0f / 2);
    }

    public static <V> ExpandableArray<V> create(int initialSize) {
        return new ExpandableArray<V>(initialSize);
    }

    public ExpandableArray(int initialSize, float factor) {
        setData(new Object[initialSize]);
        this.factor = factor;
    }

    public ExpandableArray(ExpandableArray<? extends E> m) {
        setData(m.data.clone());
        maxNonNullIndex = m.maxNonNullIndex;
        factor = 3.0f / 2;
        // default to being mutable
    }

    public ExpandableArray(E[] m) {
        setData(m.clone());
        for (int i = m.length - 1; i >= 0; i--) {
            if (m[i] != null) {
                this.maxNonNullIndex = i;
                break;
            }
        }
        factor = 3.0f / 2;
        // default to being mutable
    }

    /**
     * Makes the expandable array immutable. After setting this flag, you can't un-set it,
     * because then it wouldn't be very immutable, would it?
     *
     * This is basically because I'm too lazy to make an interface that this
     * implements and then make an UnmodifiableExpandableArray.
     */
    public void makeUnmodifiable() {
        this.isMutable = false;
    }

    public E get(int index) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (index >= this.data.length) {
            return null;
        }
        return this.data[index];
    }

    @SuppressWarnings("unchecked")
    private void setData(Object[] newData) {
        if (!this.isMutable) {
            throw new UnsupportedOperationException();
        }
        this.data = (E[])newData;
    }

    public void set(int index, E value) {
        if (!this.isMutable) {
            throw new UnsupportedOperationException();
        }
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (index >= this.data.length) {
            int newLength = (int)(this.data.length * factor);
            if (newLength < index + 8) {
                newLength = index + 8;
            }
            resize(newLength);
        }
        // update maxNonNullIndex
        if (value != null) {
            if (index > this.maxNonNullIndex) {
                this.maxNonNullIndex = index;
            }
        } else {
            // if this used to be the max non null, we need to find the next lowest non-null index
            if (index == this.maxNonNullIndex) {
                for (this.maxNonNullIndex = index - 1; this.maxNonNullIndex >= 0; this.maxNonNullIndex--) {
                    if (get(this.maxNonNullIndex) != null) {
                        break;
                    }
                }
            }
        }

        this.data[index] = value;
    }

    private void resize(int newLength) {
        if (data.length == newLength) {
            return;
        }
        Object[] newData = new Object[newLength];
        if (newLength > 0 && data.length > 0) {
            System.arraycopy(data, 0, newData, 0, Math.min(newLength, data.length));
        }
        setData(newData);
    }

    /**
     * Trims the capacity of this <tt>ExpandableArray</tt> instance to be the
     * arrays's current size.  An application can use this operation to minimize
     * the storage of an <tt>ExpandableArray</tt> instance.
     */
    public void trimToSize() {
        // no need to check mutability, because this isn't actually modifying the data
        resize(size());
    }

    /**
     * Returns 1 + the index of the highest non-null value. This is useful as an upper bound in a
     * for loop to iterate through the values in this ExpandableArray.
     */
    public int size() {
        return this.maxNonNullIndex + 1;
    }

    public void clear() {
        if (!this.isMutable) {
            throw new UnsupportedOperationException();
        }
        for (int i = 0; i <= this.maxNonNullIndex; i++) {
            this.data[i] = null;
        }
        this.maxNonNullIndex = -1;
    }

    /**
     * Returns an array containing all of the elements in this list
     * in the correct order.
     *
     * @return an array containing all of the elements in this list
     *         in the correct order.
     */
    public Object[] toArray() {
        Object[] result = new Object[size()];
        System.arraycopy(data, 0, result, 0, size());
        return result;
    }

    /**
     * Returns an array containing all of the elements in this list in the
     * correct order; the runtime type of the returned array is that of the
     * specified array.  If the list fits in the specified array, it is
     * returned therein.  Otherwise, a new array is allocated with the runtime
     * type of the specified array and the size of this list.<p>
     *
     * If the list fits in the specified array with room to spare (i.e., the
     * array has more elements than the list), the element in the array
     * immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of the list
     * <i>only</i> if the caller knows that the list does not contain any
     * <tt>null</tt> elements.
     *
     * @param a the array into which the elements of the list are to
     *      be stored, if it is big enough; otherwise, a new array of the
     *      same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list.
     * @throws ArrayStoreException if the runtime type of a is not a supertype
     *         of the runtime type of every element in this list.
     */
    @SuppressWarnings("unchecked")
    // Bad java, no biscuit
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size) {
            a = (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }

        System.arraycopy(data, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    /**
     * Returns an array containing all of the elements in this list
     * in the correct order.
     *
     * @param clazz the class of the array that should be constructed
     * to hold the element.
     * @return an array containing all of the elements in this list
     *         in the correct order.
     */
    @SuppressWarnings("unchecked")
    // Bad java, no biscuit
    public <T> T[] toArray(Class<T> clazz) {
        int size = size();
        T[] a = (T[])Array.newInstance(clazz, size);

        System.arraycopy(data, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(size() * 16);
        result.append('[');
        boolean isFirst = true;
        for (E item : this) {
            if (isFirst) {
                isFirst = false;
            } else {
                result.append(", ");
            }
            result.append(item);
        }
        result.append(']');
        return result.toString();
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        int cursor = 0;

        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public E next() {
            return get(cursor++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Helper to resize any array. If <code>size</code> is exactly the
     * length of <code>array</code>, it returns <code>array</code>.
     * Otherwise, it will return a new array.
     *
     * Returns an array of size <code>size</code> containing the first
     * <code>size</code> elements in <code>array</code>. If <code>size</code> is
     * greater than the length of <code>array</code>, the remaining
     * elements in the resulting array will be null.
     */
    public static <T> T[] resizeArray(T[] array, int size) {
        return subArray(array, 0, size);
    }

    /**
     * Helper to get the sub-array of any array. If <code>start</code> is 0 and
     * <code>end</code> is length of <code>array</code>, it returns <code>array</code>.
     * Otherwise, it will return a new array.
     *
     * Returns an array containing the elements from <code>start</code> inclusive
     * to <code>end</code> exclusive. The resulting array will be of size <code>end - start</code>.
     *
     * If <code>end</code> is greater than the size of the array, it will create a
     * result array with nulls in those spots.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] subArray(T[] array, int start, int end) {
        if (start == 0 && end == array.length) {
            return array;
        }
        if (start < 0) {
            throw new ArrayIndexOutOfBoundsException(start);
        }
        if (start > array.length) {
            throw new ArrayIndexOutOfBoundsException(start);
        }
        if (end < start) {
            throw new IllegalArgumentException("start: " + start + ", end: " + end);
        }

        int size = end - start;
        T[] result = (T[])Array.newInstance(array.getClass().getComponentType(), size);

        end = Math.min(end, array.length);
        size = end - start;
        if (size > 0) {
            System.arraycopy(array, start, result, 0, size);
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ExpandableArray)) {
            return false;
        }
        return compareTo((ExpandableArray) other) == 0;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    @SuppressWarnings("unchecked")  // warning on mine.compareTo(theirs). if i type it, then there will be a warning on the cast to Comparable<T>
    public int compareTo(ExpandableArray other) {
        int len1 = size();
        int len2 = other.size();
        int len = Math.min(len1, len2);

        for (int i = 0; i < len; i++) {
            Comparable mine = (Comparable) get(i);
            Comparable theirs = (Comparable) other.get(i);
            if (mine == null) {
                if (theirs != null) {
                    return -1;
                }
            } else if (theirs == null) {
                return 1;
            } else {
                int result = mine.compareTo(theirs);
                if (result != 0) {
                    return result;
                }
            }
        }

        return len1 - len2;
    }
}
