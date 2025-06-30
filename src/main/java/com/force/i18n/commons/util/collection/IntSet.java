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

import com.google.common.annotations.Beta;

/**
 * <p>Modeled closely after the {@code java.util.Set} class, except it
 * uses {@code int}s instead of {@code Object}s.</p>
 *
 * <p>The additional stipulation on constructors is, not surprisingly,
 * that all constructors must create a set that contains no duplicate elements
 * (as defined above).</p>
 *
 * Note: Great care must be exercised if mutable objects are used as set
 * elements.  The behavior of a set is not specified if the value of an object
 * is changed in a manner that affects equals comparisons while the object is
 * an element in the set.  A special case of this prohibition is that it is
 * not permissible for a set to contain itself as an element.
 *
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author  Based on Sun's java.util.Set (modified by koliver)
 * @see IntHashSet
 * @see IntMap
 */
@Beta
public interface IntSet {

    /**
     * Create a new empty IntSet with the same impl
     * @return a new empty IntSet
     */
    IntSet makeEmpty();

    /**
     * @return the ordered array of ints in the IntSet.
     */
    int[] toArray();

    /**
     * Returns an iterator over the elements in this set.  The elements are
     * returned in no particular order (unless this set is an instance of some
     * class that provides a guarantee).
     *
     * @return an iterator over the elements in this set.
     */
    IntIterator iterator();

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set (its cardinality).
     */
    int size();

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements.
     */
    boolean isEmpty();

    /**
     * Returns {@code true} if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested.
     * @return {@code true} if this set contains the specified element.
     */
    boolean contains(int o);

    /**
     * Returns {@code true} if this set contains all of the elements in the specified set
     *
     * @param set set that is being tested for containment.
     * @return {@code true} if this set is a superset of the given set
     */
    boolean containsAll(IntSet set);

    /**
     * Adds the specified element to this set if it is not already
     * present.
     *
     * @param o element to be added to this set.
     * @return {@code true} if the set did not already contain the specified
     * element.
     */
    boolean add(int o);

    /**
     * Removes the given element from this set if it is present.
     *
     * @param o object to be removed from this set, if present.
     * @return {@code true} if the set contained the specified element.
     */
    boolean remove(int o);

    /**
     * Removes all of the elements from this set.
     */
    void clear();

}
