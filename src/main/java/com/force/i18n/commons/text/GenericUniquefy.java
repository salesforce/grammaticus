/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.text;

import java.util.HashMap;
import java.util.Map;

import com.google.common.annotations.Beta;

/**
 * A version of uniquefy that is generic any object.
 *
 * The purpose is if you are java serializing an object, removing duplicates
 * if they represent the same thing will reduce the output size.
 *
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author stamm
 */
@Beta
public class GenericUniquefy<T> {
    // Limit the size of the stringPool to prevent OOM
    private static final int MAX_POOL_SIZE = 10000;

    /** a Uniquefy that doesn't do anything; useful to avoid if (uniquefy != null) checks in code */
    private static final GenericUniquefy<Object> NO_OP = new GenericUniquefy<Object>() {
        @Override public Object unique(Object value) { return value; }
    };

    @SuppressWarnings("unchecked")
    public static <V> GenericUniquefy<V> noop() {
        return (GenericUniquefy<V>) NO_OP;
    }

    private final int maxPoolSize;
    private final Map<T, T> pool;

    /**
     * Create a new pool.
     */
    public GenericUniquefy() {
        this(MAX_POOL_SIZE);
    }

    public GenericUniquefy(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        this.pool = new HashMap<T, T>();
    }

    /**
     * Look to see if the given value is already in the given pool.
     * If so, return the one already in the pool.  Otherwise, add it to the pool.
     * @param value the value to uniquefy
     * @return the exact same object or the equivalent object the pool
     */
    public T unique(T value) {
        if (value == null) {
            return null;
        }
        T oldValue = this.pool.get(value);
        if (oldValue != null) {
            return oldValue;
        }
        if (this.pool.size() < maxPoolSize) {
            this.pool.put(value, value);
        }
        return value;
    }
}
