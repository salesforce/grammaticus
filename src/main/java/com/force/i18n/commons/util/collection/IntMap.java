/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.util.collection;

import java.util.Collection;
import java.util.Set;

import com.google.common.annotations.Beta;

import java.io.Serializable;

/**
 * A Map that uses {@code int}s as its keys.
 * Mostly borrowed from {@code java.util.HashMap}
 *
 * <p>Also contains the public inner class interface IntMap.Entry
 * that all implementations of IntMap must also complete.
 *
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author  Based on Sun's java.util.Map (modified by koliver)
 * @see IntHashMap
 * @see IntSet
 */
@Beta
public interface IntMap<V> extends Serializable {
    /**
     * @return the number of key-value mappings in this map.
     */
    int size();

    /**
     * @return {@code true} if this map contains no key-value mappings.
     */
    boolean isEmpty();

    /**
     * @return {@code true} if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     */
    boolean containsValue(Object value);

    /**
     * @return  {@code true} if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this Map is to be tested.
     */
    boolean containsKey(int key);

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * {@code null} if the map contains no mapping for this key.  A return
     * value of {@code null} does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to {@code null}.  The {@code containsKey}
     * operation may be used to distinguish these two cases.
     *
     * @return the value to which this map maps the specified key.
     * @param key key whose associated value is to be returned.
     */
    V get(int key);

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or {@code null}
     *	       if there was no mapping for key.  A {@code null} return can
     *	       also indicate that the HashMap previously associated
     *	       {@code null} with the specified key.
     */
    V put(int key, V value);

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or {@code null}
     *	       if there was no mapping for key.  A {@code null} return can
     *	       also indicate that the map previously associated {@code null}
     *	       with the specified key.
     */
    V remove(int key);

    /**
     * Copies all of the mappings from the specified map to this one.
     *
     * These mappings replace any mappings that this map had for any of the
     * keys currently in the specified Map.
     *
     * @param t Mappings to be stored in this map.
     */
    void putAll(IntMap<? extends V> t);

    /**
     * Removes all mappings from this map.
     */
    void clear();

    /**
     * Returns a set view of the keys contained in this map.  The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa.  The set supports element removal, which removes the
     * corresponding mapping from this map, via the {@code Iterator.remove},
     * {@code Set.remove}, {@code removeAll}, {@code retainAll}, and
     * {@code clear} operations.  It does not support the {@code add} or
     * {@code addAll} operations.
     *
     * @return a set view of the keys contained in this map.
     */
    IntSet keySet();

    /**
     * Returns a collection view of the values contained in this map.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from this map, via the
     * {@code Iterator.remove}, {@code Collection.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear} operations.
     * It does not support the {@code add} or {@code addAll} operations.
     *
     * @return a collection view of the values contained in this map.
     */
    Collection<V> values();

    /**
     * Returns a collection view of the mappings contained in this map.  Each
     * element in the returned collection is a {@code IntMap.Entry}.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from the map, via the
     * {@code Iterator.remove}, {@code Collection.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear} operations.
     * It does not support the {@code add} or {@code addAll} operations.
     *
     * @return a collection view of the mappings contained in this map.
     * @see IntMap.Entry
     */
    Set<IntMap.Entry<V>> entrySet();

    public interface Entry<V> {
        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry.
         */
        int getKey();

        /**
         * Returns the value corresponding to this entry.  If the mapping
         * has been removed from the backing map (by the iterator's
         * {@code remove} operation), the results of this call are undefined.
         *
         * @return the value corresponding to this entry.
         */
        V getValue();

        /**
         * Compares the specified object with this entry for equality.
         * Returns {@code true} if the given object is also a map entry and
         * the two entries represent the same mapping.  More formally, two
         * entries {@code e1} and {@code e2} represent the same mapping
         * if<pre>
         *     (e1.getKey()==null ?
         *      e2.getKey()==null : e1.getKey().equals(e2.getKey()))  &amp;&amp;
         *     (e1.getValue()==null ?
         *      e2.getValue()==null : e1.getValue().equals(e2.getValue()))
         * </pre>
         * This ensures that the {@code equals} method works properly across
         * different implementations of the {@code Map.Entry} interface.
         *
         * @param o object to be compared for equality with this map entry.
         * @return {@code true} if the specified object is equal to this map
         *         entry.
         */
        @Override
        boolean equals(Object o); // don't mark this with @Override, it messes up Eclipse -- bfry
        
        /*
        * IMPORTANT: this intentionally doesn't have the remove() operation because
        * it makes implementing an unmodifiable IntMap horrifically difficult (see
        * Collections.unmodifiableMap) and no one was using it. Don't add it.
        */
        
    }
}
