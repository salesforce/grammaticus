/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.commons.util.collection;

import java.util.Iterator;

import com.google.common.annotations.Beta;

/**
 * This class provides a skeletal implementation of the <tt>IntMap</tt>
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

    protected static class SimpleEntry<V> implements Entry<V> {
        int key;
        V value;

        public SimpleEntry(int key, V value) {
            this.key = key;
            this.value = value;
        }

        public SimpleEntry(Entry<V> e) {
            this.key = e.getKey();
            this.value = e.getValue();
        }

        @Override
        public int getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IntMap.Entry))
                return false;
            IntMap.Entry<?> e = (IntMap.Entry<?>)o;
            return key == e.getKey() && eq(value, e.getValue());
        }

        @Override
        public int hashCode() {
            return key ^ ((value == null) ? 0 : value.hashCode());
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

        private static boolean eq(Object o1, Object o2) {
            return (o1 == null ? o2 == null : o1.equals(o2));
        }
    }
}
