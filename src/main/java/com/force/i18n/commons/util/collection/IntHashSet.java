/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.util.collection;

import java.io.Serializable;

import com.google.common.annotations.Beta;

/**
 * This class implements the <tt>IntSet</tt> interface, backed by a hash table
 * (actually a <tt>IntHashMap</tt> instance).  It makes no guarantees as to the
 * iteration order of the set; in particular, it does not guarantee that the
 * order will remain constant over time. <p>
 * <p>
 * This class offers constant time performance for the basic operations
 * (<tt>add</tt>, <tt>remove</tt>, <tt>contains</tt> and <tt>size</tt>),
 * assuming the hash function disperses the elements properly among the
 * buckets.  Iterating over this set requires time proportional to the sum of
 * the <tt>IntHashSet</tt> instance's size (the number of elements) plus the
 * "capacity" of the backing <tt>IntHashMap</tt> instance (the number of
 * buckets).  Thus, it's very important not to set the initial capacity too
 * high (or the load factor too low) if iteration performance is important.<p>
 *
 * <b>Note that this implementation is not synchronized.</b> If multiple
 * threads access a set concurrently, and at least one of the threads modifies
 * the set, it <i>must</i> be synchronized externally.
 * <p>
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author Based on Sun's java.util.HashSet (modified by koliver)
 * @note use {@link java.util.HashSet} or {@link java.util.EnumSet} instead.
 * @see IntSet
 * @see IntHashMap
 * @see IntMap
 */
@Beta
public class IntHashSet extends AbstractIntSet implements Serializable {
    private static final long serialVersionUID = 7500368467424833369L;

    IntHashMap<Object> map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * default capacity and load factor, which is <tt>0.75</tt>.
     */
    public IntHashSet() {
        this.map = new IntHashMap<Object>();
    }

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * the specified initial capacity and the specified load factor.
     *
     * @param initialCapacity the initial capacity of the hash map.
     * @param loadFactor      the load factor of the hash map.
     * @throws IllegalArgumentException if the initial capacity is less
     *                                  than zero, or if the load factor is nonpositive.
     */
    public IntHashSet(int initialCapacity, float loadFactor) {
        this.map = new IntHashMap<Object>(initialCapacity, loadFactor);
    }

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * the specified initial capacity and default load factor, which is
     * <tt>0.75</tt>.
     *
     * @param initialCapacity the initial capacity of the hash table.
     * @throws IllegalArgumentException if the initial capacity is less
     *                                  than zero.
     */
    public IntHashSet(int initialCapacity) {
        this.map = new IntHashMap<Object>(initialCapacity);
    }

    @Override
    public IntSet makeEmpty() {
        return new IntHashSet(this.map.capacity(), this.map.loadFactor());
    }

    @Override
    public IntIterator iterator() {
        return this.map.keySet().iterator();
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set (its cardinality).
     */
    @Override
    public int size() {
        return this.map.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested.
     * @return <tt>true</tt> if this set contains the specified element.
     */
    @Override
    public boolean contains(int o) {
        return this.map.containsKey(o);
    }

    /**
     * Adds the specified element to this set if it is not already
     * present.
     *
     * @param o element to be added to this set.
     * @return <tt>true</tt> if the set did not already contain the specified
     * element.
     */
    @Override
    public boolean add(int o) {
        return this.map.put(o, PRESENT) == null;
    }

    /**
     * Removes the given element from this set if it is present.
     *
     * @param o object to be removed from this set, if present.
     * @return <tt>true</tt> if the set contained the specified element.
     */
    @Override
    public boolean remove(int o) {
        return this.map.remove(o) == PRESENT;
    }

    /**
     * Removes all of the elements from this set.
     */
    @Override
    public void clear() {
        this.map.clear();
    }
}
