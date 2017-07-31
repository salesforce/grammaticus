/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.util.collection;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.google.common.annotations.Beta;

/**
 * A basic implementation of a size-capped cache based on {@link LinkedHashMap}
 * The eviction policy can be access order or insertion order.
 * 
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 * 
 * @author fhossain
 */
@Beta
public class LruCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1;

    private final int capacity;

    /**
     * Uses access order for the eviction policy.
     * 
     * @param capacity max capacity of the cache.
     */
    public LruCache(int capacity) {
        this(capacity, capacity);
    }
    
    public LruCache(int capacity, int initialCapacity) {
        this(capacity, initialCapacity, true);
    }
    
    public LruCache(int capacity, boolean accessOrder) {
        this(capacity, capacity, accessOrder);
    }

    public LruCache(int capacity, int initialCapacity, boolean accessOrder) {
        super(initialCapacity + 1, capacity == initialCapacity ? 1.1f : 0.75f, accessOrder);
        this.capacity = capacity;
    }
    
    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {
        return size() > capacity;
    }
    
    public int getCapacity() {
        return capacity;
    }
}
