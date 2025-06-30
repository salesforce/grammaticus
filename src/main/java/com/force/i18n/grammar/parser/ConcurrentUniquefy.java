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

package com.force.i18n.grammar.parser;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A version of uniquefy that works in a "concurrent" fashion, so that it's not actually
 * "unique", but close enough to prevent leakage.  Use this for fixed-set caches.
 * @author stamm
 */
public class ConcurrentUniquefy<T> implements Serializable {

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
     * @param value the value to make unique
     * @return the value or an equivalent already in the pool.
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
