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

package com.force.i18n.commons.text;

import java.util.HashMap;
import java.util.Map;

import com.google.common.annotations.Beta;

/**
 * Utility to extract unique copies of strings.
 * This is primarily used to reduce the serialized size of an object.
 * Call unique() when constructing the object, so that we use the
 * same java.lang.String object to represent each unique string value.
 * Thus, when serializing the object, instead of repeating the string values,
 * they become references in the serialized representation.
 *
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author swong
 */
@Beta
public class Uniquefy {

    // Limit the size of the stringPool to prevent OOM
    private static final int MAX_POOL_SIZE = 100000;

    /** a Uniquefy that doesn't do anything; useful to avoid if (uniquefy != null) checks in code */
    public static final Uniquefy NO_OP = new Uniquefy() {
        @Override public String unique(String value) { return value; }
    };

    private final Map<String, String> stringPool;

    /**
     * Create a new string pool.
     */
    public Uniquefy() {
        this.stringPool = new HashMap<String, String>();
    }

    /**
     * Look to see if the given value is already in the given string pool.
     * If so, return the one already in the pool.  Otherwise, add it to the pool.
     * @param value the value to uniquefy
     * @return the exact same string or the equivalent string the pool
     */
    public String unique(String value) {
        if (value == null)
            return null;
        String oldValue = this.stringPool.get(value);
        if (oldValue != null)
            return oldValue;
        if (this.stringPool.size() < MAX_POOL_SIZE)
            this.stringPool.put(value, value);
        return value;
    }

    /**
     * @return unique all of the values in the array
     * @param values the values in the array to uniquefy
     */
    public String[] unique(String[] values) {
        for (int i = 0; i < values.length; i++) {
            values[i] = unique(values[i]);
        }
        return values;
    }

}
