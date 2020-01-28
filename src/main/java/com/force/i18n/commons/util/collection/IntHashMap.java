/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.util.collection;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import com.google.common.annotations.Beta;

/**
 * Hash table based implementation of the <tt>IntMap</tt> interface.  This
 * implementation provides all of the optional map operations, and permits
 * <tt>null</tt> values.  (The <tt>IntHashMap</tt> class is roughly
 * equivalent to <tt>HashMap</tt>, except that it uses <tt>int</tt>s as its keys.
 * This class makes no guarantees as to
 * the order of the map; in particular, it does not guarantee that the order
 * will remain constant over time.<p>
 *
 * This implementation provides constant-time performance for the basic
 * operations (<tt>get</tt> and <tt>put</tt>), assuming the hash function
 * disperses the elements properly among the buckets.  Iteration over
 * collection views requires time proportional to the "capacity" of the
 * <tt>IntHashMap</tt> instance (the number of buckets) plus its size (the number
 * of key-value mappings).  Thus, it's very important not to set the intial
 * capacity too high (or the load factor too low) if iteration performance is
 * important.<p>
 *
 * An instance of <tt>HashMap</tt> has two parameters that affect its
 * performance: <i>initial capacity</i> and <i>load factor</i>.  The
 * <i>capacity</i> is the number of buckets in the hash table, and the initial
 * capacity is simply the capacity at the time the hash table is created.  The
 * <i>load factor</i> is a measure of how full the hash table is allowed to
 * get before its capacity is automatically increased.  When the number of
 * entries in the hash table exceeds the product of the load factor and the
 * current capacity, the capacity is roughly doubled by calling the
 * <tt>rehash</tt> method.<p>
 *
 * As a general rule, the default load factor (.75) offers a good tradeoff
 * between time and space costs.  Higher values decrease the space overhead
 * but increase the lookup cost (reflected in most of the operations of the
 * <tt>IntHashMap</tt> class, including <tt>get</tt> and <tt>put</tt>).  The
 * expected number of entries in the map and its load factor should be taken
 * into account when setting its initial capacity, so as to minimize the
 * number of <tt>rehash</tt> operations.  If the initial capacity is greater
 * than the maximum number of entries divided by the load factor, no
 * <tt>rehash</tt> operations will ever occur.<p>
 *
 * If many mappings are to be stored in a <tt>IntHashMap</tt> instance, creating
 * it with a sufficiently large capacity will allow the mappings to be stored
 * more efficiently than letting it perform automatic rehashing as needed to
 * grow the table.<p>
 *
 * <b>Note that this implementation is not synchronized.</b> If multiple
 * threads access this map concurrently, and at least one of the threads
 * modifies the map structurally, it <i>must</i> be synchronized externally.
 * (A structural modification is any operation that adds or deletes one or
 * more mappings; merely changing the value associated with a key that an
 * instance already contains is not a structural modification.)  This is
 * typically accomplished by synchronizing on some object that naturally
 * encapsulates the map.
 *
 * The iterators returned by all of this class's "collection view methods" are
 * <i>fail-fast</i>: if the map is structurally modified at any time after the
 * iterator is created, in any way except through the iterator's own
 * <tt>remove</tt> or <tt>add</tt> methods, the iterator will throw a
 * <tt>ConcurrentModificationException</tt>.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 * 
 * Beta class. Generally use {@link java.util.HashMap} or {@link java.util.EnumMap} instead.
 *
 * @author  Based on Sun's java.util.HashMap (modified by koliver)
 * @see	    IntMap
 * @see	    java.util.HashMap
 */
@Beta
@SuppressWarnings("rawtypes") // TODO Fix
public class IntHashMap<V> extends AbstractIntMap<V> implements Serializable {
    private static final long serialVersionUID = 0L;

    private static final int DEFAULT_CAPACITY = 101;

    /**
     * The hash table data.
     */
    private transient IEntry<V>[] table;

    /**
     * The total number of mappings in the hash table.
     */
    private transient int count;

    /**
     * The table is rehashed when its size exceeds this threshold.  (The
     * value of this field is (int)(capacity * loadFactor).)
     */
    private int threshold;

    /**
     * The load factor for the hashtable.
     */
    private final float loadFactor;

    /**
     * The number of times this HashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the HashMap or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the HashMap fail-fast.  (See ConcurrentModificationException).
     */
    private transient int modCount = 0;

    /**
     * Constructs a new, empty map with the specified initial
     * capacity and the specified load factor.
     *
     * @param      initialCapacity   the initial capacity of the HashMap.
     * @param      loadFactor        the load factor of the HashMap
     * @throws     IllegalArgumentException  if the initial capacity is less
     *               than zero, or if the load factor is nonpositive.
     */
    @SuppressWarnings("unchecked")
    public IntHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Initial Capacity: " + initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal Load factor: " + loadFactor);
        if (initialCapacity == 0)
            initialCapacity = 1;
        this.loadFactor = loadFactor;
        this.table = new IEntry[initialCapacity];
        this.threshold = (int)(initialCapacity * loadFactor);
    }

    /**
     * Constructs a new, empty map with the specified initial capacity
     * and default load factor, which is <tt>0.75</tt>.
     *
     * @param   initialCapacity   the initial capacity of the HashMap.
     * @throws    IllegalArgumentException if the initial capacity is less
     *              than zero.
     */
    public IntHashMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    /**
     * Constructs a new, empty map with a default capacity, <tt>101</tt>, and load
     * factor, which is <tt>0.75</tt>.
     */
    public IntHashMap() {
        this(DEFAULT_CAPACITY, 0.75f);
    }

    /**
     * Constructs a new map with the same mappings as the given map.  The
     * map is created with a capacity of twice the number of mappings in
     * the given map or 101 (whichever is greater), and a default load factor,
     * which is <tt>0.75</tt>.
     *
     * @param t the map whose mappings are to be placed in this map.
     */
    public IntHashMap(IntMap<? extends V> t) {
        this(Math.max(2 * t.size(), DEFAULT_CAPACITY), 0.75f);
        putAll(t);
    }

    /**
     * Returns the number of key-value mappings in this map.
     */
    @Override
    public int size() {
        return count;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     */
	@Override
    public boolean containsValue(Object value) {
        IEntry[] tab = this.table;

        if (value == null) {
            for (int i = tab.length; i-- > 0;)
                for (IEntry e = tab[i]; e != null; e = e.next)
                    if (e.value == null)
                        return true;
        } else {
            for (int i = tab.length; i-- > 0;)
                for (IEntry e = tab[i]; e != null; e = e.next)
                    if (value.equals(e.value))
                        return true;
        }

        return false;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this Map is to be tested.
     */
    @Override
    public boolean containsKey(int key) {
        IEntry<V>[] tab = this.table;

        int index = (key & 0x7FFFFFFF) % tab.length;
        for (IEntry<V> e = tab[index]; e != null; e = e.next) {
            if (e.key == key) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.  A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     *
     * @return the value to which this map maps the specified key.
     * @param key key whose associated value is to be returned.
     */
    @Override
    public V get(int key) {
        IEntry<V>[] tab = this.table;

        int index = (key & 0x7FFFFFFF) % tab.length;
        for (IEntry<V> e = tab[index]; e != null; e = e.next) {
            if (e.key == key) {
                return e.value;
            }
        }

        return null;
    }

    /**
     * Rehashes the contents of this map into a new <tt>HashMap</tt> instance
     * with a larger capacity. This method is called automatically when the
     * number of keys in this map exceeds its capacity and load factor.
     */
    private void rehash() {
        int oldCapacity = this.table.length;
        IEntry<V>[] oldMap = this.table;

        int newCapacity = oldCapacity * 2 + 1;
        @SuppressWarnings("unchecked")
        IEntry<V>[] newMap = new IEntry[newCapacity];

        this.modCount++;
        this.threshold = (int)(newCapacity * loadFactor);
        this.table = newMap;

        for (int i = oldCapacity; i-- > 0;) {
            for (IEntry<V> old = oldMap[i]; old != null;) {
                IEntry<V> e = old;
                old = old.next;

                int index = (e.key & 0x7FFFFFFF) % newCapacity;
                e.next = newMap[index];
                newMap[index] = e;
            }
        }
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the HashMap previously associated
     *	       <tt>null</tt> with the specified key.
     */
    @Override
    public V put(int key, V value) {
        // Makes sure the key is not already in the HashMap.
        IEntry<V>[] tab = table;
        int index = 0;

        index = (key & 0x7FFFFFFF) % tab.length;
        // first look if there was an old value at key
        for (IEntry<V> e = tab[index]; e != null; e = e.next) {
            if (key == e.key) {
                V old = e.value;
                e.value = value;
                return old;
            }
        }

        modCount++;
        if (count >= threshold) {
            // Rehash the table if the threshold is exceeded
            rehash();

            tab = this.table;
            index = (key & 0x7FFFFFFF) % tab.length;
        }

        // Creates the new entry.
        IEntry<V> e = new IEntry<V>(key, value, tab[index]);
        tab[index] = e;
        count++;
        return null;
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the map previously associated <tt>null</tt>
     *	       with the specified key.
     */
    @Override
    public V remove(int key) {
        IEntry<V>[] tab = this.table;

        int index = (key & 0x7FFFFFFF) % tab.length;

        for (IEntry<V> e = tab[index], prev = null; e != null; prev = e, e = e.next) {
            if (key == e.key) {
                modCount++;
                if (prev != null)
                    prev.next = e.next;
                else
                    tab[index] = e.next;

                count--;
                V oldValue = e.getValue();
                e.value = null;
                return oldValue;
            }
        }

        return null;
    }

    Iterator<IntMap.Entry<V>> getEntriesIterator() {
        return new EntriesIterator();
    }

    Iterator<V> getValuesIterator() {
        return new ValuesIterator();
    }

    /**
     * Removes all mappings from this map.
     */
    @Override
    public void clear() {
        IEntry[] tab = this.table;
        modCount++;
        for (int index = tab.length; --index >= 0;)
            tab[index] = null;
        count = 0;
    }

    // Views
    private transient IntSet keySet = null;
    private transient Set<Entry<V>> entrySet = null;
    private transient Collection<V> values = null;

    /**
     * Returns a set view of the keys contained in this map.  The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa.  The set supports element removal, which removes the
     * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return a set view of the keys contained in this map.
     */
    @Override
    public IntSet keySet() {
        if (this.keySet == null) {
            this.keySet = new BackingIntSet();
        }
        return this.keySet;
    }

    /**
     * Returns a collection view of the values contained in this map.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from this map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the values contained in this map.
     */
    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new AbstractCollection<V>() {
                @Override
                public Iterator<V> iterator() {
                    return IntHashMap.this.getValuesIterator();
                }

                @Override
                public int size() {
                    return IntHashMap.this.count;
                }

                @Override
                public boolean contains(Object o) {
                    return IntHashMap.this.containsValue(o);
                }

                @Override
                public void clear() {
                    IntHashMap.this.clear();
                }
            };
        }
        return values;
    }

    /**
     * Returns a collection view of the mappings contained in this map.  Each
     * element in the returned collection is a <tt>IntMap.Entry</tt>.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the mappings contained in this map.
     * @see IntMap.Entry
     */
    @Override
    public Set<Entry<V>> entrySet() {
        if (entrySet == null) {
            entrySet = new AbstractSet<Entry<V>>() {
                @Override
                public Iterator<Entry<V>> iterator() {
                    return IntHashMap.this.getEntriesIterator();
                }

                @Override
                public boolean contains(Object o) {
                    if (!(o instanceof IntMap.Entry))
                        return false;
                    @SuppressWarnings("unchecked")
                    IntMap.Entry<V> entry = (IntMap.Entry<V>)o;
                    int key = entry.getKey();
                    IEntry<V>[] tab = IntHashMap.this.table;

                    int index = (key & 0x7FFFFFFF) % tab.length;

                    for (IEntry e = tab[index]; e != null; e = e.next)
                        if (e.key == key && e.equals(entry))
                            return true;
                    return false;
                }

                @Override
                public boolean remove(Object o) {
                    if (!(o instanceof IntMap.Entry))
                        return false;
                    @SuppressWarnings("unchecked")
                    IntMap.Entry<V> entry = (IntMap.Entry<V>)o;
                    int key = entry.getKey();
                    IEntry<V>[] tab = IntHashMap.this.table;

                    int index = (key & 0x7FFFFFFF) % tab.length;

                    for (IEntry<V> e = tab[index], prev = null; e != null; prev = e, e = e.next) {
                        if (e.key == key && e.equals(entry)) {
                            modCount++;
                            if (prev != null)
                                prev.next = e.next;
                            else
                                tab[index] = e.next;

                            count--;
                            e.value = null;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public int size() {
                    return IntHashMap.this.count;
                }

                @Override
                public void clear() {
                    IntHashMap.this.clear();
                }
            };
        }

        return entrySet;
    }

    private static class IEntry<IV> implements Entry<IV> {
        final int key;
        IV value;
        IEntry<IV> next;

        IEntry(int key, IV value, IEntry<IV> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public int getKey() {
            return key;
        }

        @Override
        public IV getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IntMap.Entry))
                return false;

            @SuppressWarnings("unchecked")
            IntMap.Entry<IV> e = (IntMap.Entry<IV>)o;

            return (this.key == e.getKey() && (value == null ? e.getValue() == null : value.equals(e.getValue())));
        }

        @Override
        public int hashCode() {
            return this.key ^ (value == null ? 0 : value.hashCode());
        }

        @Override
        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    // Types of Iterators

    private abstract class HashIterator<E> implements Iterator<E> {
        IEntry<V>[] itable = IntHashMap.this.table;
        int index = itable.length;
        IEntry<V> entry = null;
        IEntry<V> lastReturned = null;

        /**
         * The modCount value that the iterator believes that the backing
         * List should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        private int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            IEntry<V> e = entry;
            int i = index;
            IEntry<V>[] t = itable;
            /* Use locals for faster loop iteration */
            while (e == null && i > 0)
                e = t[--i];
            entry = e;
            index = i;
            return e != null;
        }

        public IntMap.Entry<V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            IEntry<V> et = entry;
            int i = index;
            IEntry<V>[] t = itable;

            /* Use locals for faster loop iteration */
            while (et == null && i > 0)
                et = t[--i];

            entry = et;
            index = i;
            if (et != null) {
                IEntry<V> e = lastReturned = entry;
                entry = e.next;
                return e;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            IEntry<V>[] tab = IntHashMap.this.table;
            int index = (lastReturned.key & 0x7FFFFFFF) % tab.length;

            for (IEntry<V> e = tab[index], prev = null; e != null; prev = e, e = e.next) {
                if (e == lastReturned) {
                    modCount++;
                    expectedModCount++;
                    if (prev == null)
                        tab[index] = e.next;
                    else
                        prev.next = e.next;
                    count--;
                    lastReturned = null;
                    return;
                }
            }
            throw new ConcurrentModificationException();
        }
    }

    private class EntriesIterator extends HashIterator<Entry<V>> {
        @Override
        public IntMap.Entry<V> next() {
            return nextEntry();
        }
    }

    private class ValuesIterator extends HashIterator<V> {
        @Override
        public V next() {
            return nextEntry().getValue();
        }
    }

    public int capacity() {
        return table.length;
    }

    float loadFactor() {
        return loadFactor;
    }

    private class BackingIntSet extends IntHashSet {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unchecked")
        BackingIntSet() {
            this.map = (IntHashMap<Object>)IntHashMap.this;
        }

        @Override
        public IntIterator iterator() {
            return new IntHashIterator();
        }

        @Override
        public boolean remove(int o) {
            boolean result = this.map.containsKey(o);
            this.map.remove(o);
            return result;
        }
    }

    private class IntHashIterator implements IntIterator {
        IEntry<V>[] itable = IntHashMap.this.table;
        int index = itable.length;
        IEntry entry = null;

        /**
         * The modCount value that the iterator believes that the backing
         * List should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        private int expectedModCount = modCount;

        @Override
        public boolean hasNext() {
            // Use locals for faster loop iteration
            IEntry e = entry;
            int i = index;
            IEntry[] t = itable;

            while (e == null && i > 0)
                e = t[--i];

            this.entry = e;
            this.index = i;
            return e != null;
        }

        @Override
        public int next() {
            if (IntHashMap.this.modCount != this.expectedModCount)
                throw new ConcurrentModificationException();

            // Use locals for faster loop iteration
            IEntry e = entry;
            int i = index;
            IEntry[] t = itable;

            while (e == null && i > 0)
                e = t[--i];

            this.entry = e;
            this.index = i;
            if (e != null) {
                IEntry et = this.entry;
                this.entry = et.next;
                return et.key;
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    // Serialization
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();

        // Write out number of buckets
        s.writeInt(table.length);

        // Write out number of entries
        s.writeInt(size());

        // Write out keys and values (alternating)
        for (Iterator i = entrySet().iterator(); i.hasNext();) {
            IEntry e = (IEntry)i.next();
            s.writeInt(e.getKey());
            s.writeObject(e.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        // Read in the threshold, loadfactor, and any hidden stuff
        s.defaultReadObject();

        // Read in number of buckets and allocate the bucket array;
        int numBuckets = s.readInt();
        table = new IEntry[numBuckets];

        // Read the keys and values, and put the mappings in the HashMap
        int size = s.readInt();
        for (int i = 0; i < size; i++) {
            int key = s.readInt();
            V value = (V)s.readObject();
            put(key, value);
        }
    }
}
