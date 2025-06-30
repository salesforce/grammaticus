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

import java.util.LinkedHashMap;
import java.util.Map;

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
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

    public int getCapacity() {
        return capacity;
    }
}
