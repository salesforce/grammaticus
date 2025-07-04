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

import java.util.Iterator;

import com.google.common.annotations.Beta;

/**
 * This class provides a skeletal implementation of the {@code IntMap}
 * interface, to minimize the effort required to implement this interface. <p>
 *
 * This borrows heavily from java.util.AbstractMap
 *
 * Beta class. Classes under com.force.i18n.commons package will be moved into a dedicated project.
 *
 * @author stamm
 */
@Beta
public abstract class AbstractIntMap<V> implements IntMap<V> {
    private static final long serialVersionUID = 1L;

    public AbstractIntMap() {
        super();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void putAll(IntMap<? extends V> t) {
        for (IntMap.Entry<? extends V> e : t.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof IntMap))
            return false;
        @SuppressWarnings("unchecked")
        IntMap<V> t = (IntMap<V>)o;
        if (t.size() != size())
            return false;

        try {
            Iterator<Entry<V>> i = entrySet().iterator();
            while (i.hasNext()) {
                Entry<V> e = i.next();
                int key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(t.get(key) == null && t.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(t.get(key)))
                        return false;
                }
            }
        }
        catch (ClassCastException unused) {
            return false;
        }
        catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (Entry<V> entry : entrySet()) {
            h += entry.hashCode();
        }
        return h;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(this.size() * 16);
        buf.append("{");

        Iterator<Entry<V>> i = entrySet().iterator();
        boolean hasNext = i.hasNext();
        while (hasNext) {
            Entry<V> e = i.next();
            int key = e.getKey();
            V value = e.getValue();
            buf.append(key);
            buf.append("=");
            if (value == this)
                buf.append("(this Map)");
            else
                buf.append(value);
            hasNext = i.hasNext();
            if (hasNext)
                buf.append(", ");
        }

        buf.append("}");
        return buf.toString();
    }

}
