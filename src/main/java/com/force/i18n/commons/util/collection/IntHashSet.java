/*
 * Copyright (c) 2025, Salesforce, Inc.
 * SPDX-License-Identifier: Apache-2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.force.i18n.commons.util.collection;

import java.io.Serializable;

import com.google.common.annotations.Beta;

/**
 * This class implements the {@code IntSet} interface, backed by a hash table
 * (actually a {@code IntHashMap} instance).  It makes no guarantees as to the
 * iteration order of the set; in particular, it does not guarantee that the
 * order will remain constant over time. <p>
 *
 * This class offers constant time performance for the basic operations
 * ({@code add}, {@code remove}, {@code contains} and {@code size}),
 * assuming the hash function disperses the elements properly among the
 * buckets.  Iterating over this set requires time proportional to the sum of
 * the {@code IntHashSet} instance's size (the number of elements) plus the
 * "capacity" of the backing {@code IntHashMap} instance (the number of
 * buckets).  Thus, it's very important not to set the intial capacity too
 * high (or the load factor too low) if iteration performance is important.<p>
 *
 * <b>Note that this implementation is not synchronized.</b> If multiple
 * threads access a set concurrently, and at least one of the threads modifies
 * the set, it <i>must</i> be synchronized externally.
 *
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author  Based on Sun's java.util.HashSet (modified by koliver)
 * @see	    IntSet
 * @see	    IntHashMap
 * @see	    IntMap
 * Note: use {@link java.util.HashSet} or {@link java.util.EnumSet} instead.
 */
@Beta
public class IntHashSet extends AbstractIntSet implements Serializable {
    private static final long serialVersionUID = 7500368467424833369L;

    IntHashMap<Object> map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    /**
     * Constructs a new, empty set; the backing {@code HashMap} instance has
     * default capacity and load factor, which is {@code 0.75}.
     */
    public IntHashSet() {
        this.map = new IntHashMap<Object>();
    }

    /**
     * Constructs a new, empty set; the backing {@code HashMap} instance has
     * the specified initial capacity and the specified load factor.
     *
     * @param      initialCapacity   the initial capacity of the hash map.
     * @param      loadFactor        the load factor of the hash map.
     * @throws     IllegalArgumentException if the initial capacity is less
     *             than zero, or if the load factor is nonpositive.
     */
    public IntHashSet(int initialCapacity, float loadFactor) {
        this.map = new IntHashMap<Object>(initialCapacity, loadFactor);
    }

    /**
     * Constructs a new, empty set; the backing {@code HashMap} instance has
     * the specified initial capacity and default load factor, which is
     * {@code 0.75}.
     *
     * @param      initialCapacity   the initial capacity of the hash table.
     * @throws     IllegalArgumentException if the initial capacity is less
     *             than zero.
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
     * Returns {@code true} if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested.
     * @return {@code true} if this set contains the specified element.
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
     * @return {@code true} if the set did not already contain the specified
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
     * @return {@code true} if the set contained the specified element.
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
