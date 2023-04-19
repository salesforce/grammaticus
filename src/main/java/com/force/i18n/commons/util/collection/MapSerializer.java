/**
 * Copyright 2023 Salesforce, Inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
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
