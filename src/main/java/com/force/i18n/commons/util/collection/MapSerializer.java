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
import java.util.Map;

import com.google.common.collect.ImmutableSortedMap;

/**
 * Proxy of {@code Map<Object, Object>} that "uniquefy" key and value.
 *
 * @author yoikawa
 */
public abstract class MapSerializer<K extends Serializable & Comparable<?>, V extends Serializable> implements Serializable {

    protected transient Map<K, V> map;

    protected final Object keys;
    protected final Object values;

    /**
     * @param map a map to wrap
     * @throws NullPointerException when {@code map} is {@code null}
     */
    protected MapSerializer(Map<K, V> map) {
        if (map == null) throw new NullPointerException();
        this.map = map;

        // go classic way
        Object[] k = new Object[map.size()];
        Object[] v = new Object[map.size()];

        int i = 0;
        for (Map.Entry<K, V> e : map.entrySet()) {
            k[i] = internKey(e.getKey());
            v[i] = internValue(e.getValue());
            ++i;
        }
        this.keys = k;
        this.values = v;
    }

    protected abstract K internKey(K key);

    // override this if you need "deep" uniquely for values too
    protected V internValue(V value) { return value; }

    @SuppressWarnings("unchecked")
    protected Object readResolve() {
        Object[] k = (Object[])this.keys;
        Object[] v = (Object[])this.values;

        ImmutableSortedMap.Builder<K, V> builder = ImmutableSortedMap.naturalOrder();
        for (int i = 0; i < k.length; i++) {
            builder.put(internKey((K)k[i]), internValue((V)v[i]));
        }
        this.map = builder.build();
        return this;
    }
}
