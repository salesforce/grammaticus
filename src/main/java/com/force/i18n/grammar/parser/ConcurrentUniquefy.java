/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A version of uniquefy that works in a "concurrent" fashion, so that it's not actually
 * "unique", but close enough to prevent leakage.  Use this for fixed-set caches.
 *
 * @author stamm
 */
public class ConcurrentUniquefy<T> {

    private final ConcurrentMap<T, T> pool;

    /**
     * Create a new string pool.
     */
    public ConcurrentUniquefy() {
        this.pool = new ConcurrentHashMap<T, T>(128, .75f, 2);
    }

    /**
     * Look to see if the given value is already in the given string pool.
     * If so, return the one already in the pool.  Otherwise, add it to the pool.
     */
    public T unique(T value) {
        if (value == null) return null;

        T result = this.pool.get(value);
        if (result != null) return result;

        result = this.pool.putIfAbsent(value, value);
        assert result == null || result.equals(value) : "There's a flaw in the equals logic associated with " + value.getClass();
        return result == null ? value : result;
    }
}
