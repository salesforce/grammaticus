/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.util.collection;

import com.google.common.annotations.Beta;

/**
 * <p>Modeled closely after the <tt>java.util.Set</tt> class, except it
 * uses <tt>int</tt>s instead of <tt>Object</tt>s.</p>
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
     * Gets the ordered array of ints in the IntSet.
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
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements.
     */
    boolean isEmpty();

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested.
     * @return <tt>true</tt> if this set contains the specified element.
     */
    boolean contains(int o);

    /**
     * Returns <tt>true</tt> if this set contains all of the elements in the specified set
     *
     * @param o set that is being tested for containment.
     * @return <tt>true</tt> if this set is a superset of the given set
     */
    boolean containsAll(IntSet set);

    /**
     * Adds the specified element to this set if it is not already
     * present.
     *
     * @param o element to be added to this set.
     * @return <tt>true</tt> if the set did not already contain the specified
     * element.
     */
    boolean add(int o);

    /**
     * Removes the given element from this set if it is present.
     *
     * @param o object to be removed from this set, if present.
     * @return <tt>true</tt> if the set contained the specified element.
     */
    boolean remove(int o);

    /**
     * Removes all of the elements from this set.
     */
    void clear();

}
