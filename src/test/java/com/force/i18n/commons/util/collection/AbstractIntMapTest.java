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

import java.io.*;
import java.util.*;

import com.force.i18n.commons.util.collection.IntMap.Entry;

import junit.framework.TestCase;

/**
 * Base class for testing IntMap of all kinds
 * @author stamm
 * @since 1.1
 */
public abstract class AbstractIntMapTest extends TestCase {
    public AbstractIntMapTest(String name) {
        super(name);
    }

    protected abstract <T> IntMap<T> newInstance() throws Exception;
    protected abstract <T> IntMap<T> newInstance(IntMap<? extends T> toClone) throws Exception;
    protected abstract boolean preservesInsertionOrder(IntMap<?> map) throws Exception;
    protected abstract boolean supportsNegativeIndices();

    public void testEmptyIntMap() throws Exception {
        IntMap<String> map = newInstance();
        assertFalse(map.containsValue("Tree"));
        assertFalse(map.containsKey(0));
        assertNull(map.get(0));
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.remove(0));

        map.put(5, "Tree");
        assertTrue(map.containsValue("Tree"));
        assertFalse(map.containsKey(0));
        assertNull(map.get(0));
        assertEquals("Tree", map.get(5));
        assertFalse(map.isEmpty());
        assertEquals(1, map.size()); // Should the existing size be renamed to length?
        assertNull(map.remove(0));
        assertEquals("Tree", map.remove(5));

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertFalse(map.containsValue("Tree"));
        assertFalse(map.containsKey(0));
        assertNull(map.get(0));
        assertNull(map.remove(0));
    }

    // Test most things about int hash maps (including cloning and the like)
    public void testStandard() throws Exception {
        IntMap<String> map1 = newInstance();
        assertTrue(map1.isEmpty());
        assertFalse(map1.containsKey(1));
        assertEquals(0, map1.size());

        map1.put(1, "foo");

        IntMap<String> map2 = newInstance(map1);

        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());
        assertEquals(map1.entrySet(), map2.entrySet());
        assertEquals(map1.keySet(), map2.keySet());
        // There's no guarantee about value equality

        map2.put(1, "bar");

        assertEquals("foo", map1.get(1));
        assertEquals("bar", map2.get(1));
        assertFalse(map1.equals(map2));
        assertNotSame(map1.hashCode(), map2.hashCode());
        assertFalse(map1.entrySet().equals(map2.entrySet()));
        assertEquals(map1.keySet(), map2.keySet()); // Both are only 1
        assertEquals(map1.size(), map2.size());

        map1.put(2, "baz");
        assertEquals("bar", map2.get(1));
        assertEquals("foo", map1.get(1));
        assertEquals("baz", map1.get(2));
        assertNull(map2.get(2));
        assertFalse(map1.equals(map2));
        assertNotSame(map1.hashCode(), map2.hashCode());
        assertFalse(map1.entrySet().equals(map2.entrySet()));
        assertFalse(map1.keySet().equals(map2.keySet()));
        assertEquals(2, map1.size());
        assertEquals(1, map2.size());

        // First is which number came up first when iterating.
        // This assumes that iteration order is the same with all the iterators
        IntIterator k1 = map1.keySet().iterator();
        assertTrue(k1.hasNext());
        int first = k1.next();
        if (preservesInsertionOrder(map1)) {
            assertEquals(1, first);
        }
        assertTrue(k1.hasNext());
        assertEquals(first == 1 ? 2 : 1, k1.next());
        assertFalse(k1.hasNext());
        try {
            k1.next();
            fail();
        }
        catch (NoSuchElementException ex) {}
        IntIterator k2 = map2.keySet().iterator();
        assertTrue(k2.hasNext());
        assertEquals(1, k2.next());
        assertFalse(k2.hasNext());
        try {
            k2.next();
            fail();
        }
        catch (NoSuchElementException ex) {}

        Iterator<String> v1 = map1.values().iterator();
        assertTrue(v1.hasNext());
        assertEquals(first == 1 ? "foo" : "baz", v1.next());
        assertTrue(v1.hasNext());
        assertEquals(first == 1 ? "baz" : "foo", v1.next());
        assertFalse(v1.hasNext());
        try {
            v1.next();
            fail();
        }
        catch (NoSuchElementException ex) {}
        Iterator<String> v2 = map2.values().iterator();
        assertTrue(v2.hasNext());
        assertEquals("bar", v2.next());
        assertFalse(v2.hasNext());
        try {
            v2.next();
            fail();
        }
        catch (NoSuchElementException ex) {}

        Set<Entry<String>> es1 = map1.entrySet();
        assertEquals(2, es1.size());
        Iterator<Entry<String>> e1 = es1.iterator();
        assertTrue(e1.hasNext());
        Entry<String> ent1 = e1.next();
        assertEquals(first == 1 ? 1 : 2, ent1.getKey());
        assertEquals(first == 1 ? "foo" : "baz", ent1.getValue());
        assertTrue(e1.hasNext());
        ent1 = e1.next();
        assertEquals(first == 1 ? 2 : 1, ent1.getKey());
        assertEquals(first == 1 ? "baz" : "foo", ent1.getValue());
        assertFalse(e1.hasNext());
        try {
            e1.next();
            fail();
        }
        catch (NoSuchElementException ex) {}

        Set<Entry<String>> es2 = map2.entrySet();
        assertEquals(1, es2.size());
        Iterator<Entry<String>> e2 = es2.iterator();
        assertTrue(e2.hasNext());
        Entry<String> ent2 = e2.next();
        assertEquals(1, ent2.getKey());
        assertEquals("bar", ent2.getValue());
        assertFalse(e2.hasNext());
        try {
            e2.next();
            fail();
        }
        catch (NoSuchElementException ex) {}

        // Test various set containment functions
        IntMap<String> map5 = newInstance();
        map5.put(1, "foom");
        assertTrue(map5.keySet().contains(1));
        assertFalse(map5.keySet().remove(2));
        assertTrue(map5.keySet().remove(1));
        assertEquals(0, map5.size());
        map5.put(1, "foom");
        assertTrue(map5.values().contains("foom"));
        assertFalse(map5.values().remove("notfoom"));
        assertTrue(map5.values().remove("foom"));
        assertEquals(0, map5.size());
        map5.put(1, "foom");
        assertTrue(map5.values().contains("foom"));
        assertFalse(map5.values().remove("notfoom"));
        assertTrue(map5.values().remove("foom"));
        assertEquals(0, map5.size());
        map5.put(1, "foom");
        map5.keySet().clear();
        assertEquals(0, map5.size());
        map5.put(1, "foom");
        map5.values().clear();
        assertEquals(0, map5.size());

        map5.put(1, "foom");
        assertEquals(new ImmutableEntry<String>(1, "foom"), map5.entrySet().iterator().next());
        try {
            map5.entrySet().iterator().remove();
            fail("Shouldn't be able to remove before nexting");
        } catch (IllegalStateException ex) {}  // ok
        Iterator<?> i = map5.entrySet().iterator();
        i.next();
        i.remove();
        assertEquals(0, map5.size());
        map5.put(1, "foom");
        map5.entrySet().remove(new ImmutableEntry<String>(1,"foom"));
        assertEquals(0, map5.size());
        map5.put(1, "foom");
        assertEquals(1, map5.entrySet().toArray().length);
        assertEquals(1, map5.entrySet().toArray(new IntMap.Entry<?>[0]).length);
        assertEquals(1, map5.entrySet().toArray(new IntMap.Entry<?>[1]).length);
        assertEquals(new ImmutableEntry<String>(1,"foom"), map5.entrySet().toArray(new IntMap.Entry<?>[1])[0]);
        assertEquals(new ImmutableEntry<String>(1,"foom"), map5.entrySet().toArray(new IntMap.Entry<?>[2])[0]);
        assertEquals(null, map5.entrySet().toArray(new IntMap.Entry<?>[2])[1]);
    }

    public void testEqualsAndHashCodes() throws Exception {
        IntMap<String> map1 = newInstance();
        IntMap<String> map2 = newInstance();

        map1.put(0, "left");
        map1.put(2, "at");
        map1.put(5, "albuquerque's");
        map1.put(7, null);
        map2.put(0, "left");
        map2.put(2, "at");
        map2.put(5, "albuquerque's");
        map2.put(7, null);

        assertEquals(map1, map2);
        assertEquals(map1.isEmpty(), map2.isEmpty());
        assertEquals(map1.hashCode(), map2.hashCode());
        assertEquals(map1.size(), map2.size());
        assertEquals(map1.keySet(), map2.keySet());
        assertEquals(map1.keySet().hashCode(), map2.keySet().hashCode());
        assertEquals(map1.entrySet(), map2.entrySet());
        assertEquals(map1.entrySet().hashCode(), map2.entrySet().hashCode());

        IntIterator ki1 = map1.keySet().iterator();
        IntIterator ki2 = map2.keySet().iterator();
        while (ki1.hasNext()) {
            assertEquals(ki1.next(), ki2.next());
        }
        assertFalse(ki2.hasNext());

        Iterator<String> vi1 = map1.values().iterator();
        Iterator<String> vi2 = map2.values().iterator();
        while (vi1.hasNext()) {
            assertEquals(vi1.next(), vi2.next());
        }
        assertFalse(vi2.hasNext());

        Iterator<Entry<String>> ei1 = map1.entrySet().iterator();
        Iterator<Entry<String>> ei2 = map2.entrySet().iterator();
        while (ei1.hasNext()) {
            Entry<String> e1 = ei1.next();
            Entry<String> e2 = ei2.next();
            assertEquals(e1, e2);
            assertEquals(e1.hashCode(), e2.hashCode());
        }
        assertFalse(ei2.hasNext());

    }

    public void testContainment() throws Exception {
        IntMap<String> map1 = newInstance();
        map1.put(1, "foom");
        map1.put(3, "jive");
        map1.put(5, "turkey");

        IntMap<String> map2 = newInstance();
        map2.put(0, "foom");
        map2.put(2, "jive");
        map2.put(4, "turkey");

        IntMap<String> map3 = newInstance();
        map3.put(1, "turkey");
        map3.put(3, "foom");
        map3.put(5, "jive");

        IntMap<String> map4 = newInstance(); // jiveless
        map4.put(1, "foom");
        map4.put(3, "jive");

        assertEquals(map1.keySet(), map3.keySet());
        assertTrue(map3.keySet().containsAll(map1.keySet()));
        assertTrue(map1.keySet().containsAll(map4.keySet()));
        assertFalse(map4.keySet().containsAll(map1.keySet()));
        if (preservesInsertionOrder(map1) && preservesInsertionOrder(map2)) {
            assertEquals(new ArrayList<String>(map1.values()), new ArrayList<String>(map2.values()));
        } else {
            assertEquals(new HashSet<String>(map1.values()), new HashSet<String>(map2.values()));
        }
        assertTrue(map1.entrySet().containsAll(map4.entrySet()));
        assertFalse(map1.entrySet().containsAll(map3.entrySet()));
        if (preservesInsertionOrder(map1) && preservesInsertionOrder(map3)) {
            assertFalse(new ArrayList<String>(map1.values()).equals(new ArrayList<String>(map3.values())));
        }
        assertTrue(new HashSet<String>(map1.values()).equals(new HashSet<String>(map3.values())));
        if (preservesInsertionOrder(map1) && preservesInsertionOrder(map4)) {
            assertTrue(new ArrayList<String>(map1.values()).containsAll(new ArrayList<String>(map4.values())));
        } else {
            assertTrue(new HashSet<String>(map1.values()).containsAll(new HashSet<String>(map4.values())));
        }
    }

    public void testNegativeIndices() throws Exception {
        IntMap<String> map = newInstance();
        try {
            map.put(-1, "Foom");
            assertTrue(supportsNegativeIndices());
        } catch (IndexOutOfBoundsException ex) {
            assertFalse(supportsNegativeIndices());
        }

        IntHashMap<String> hashMap = new IntHashMap<String>();
        hashMap.put(-1, "Foom");
        try {
            map.putAll(hashMap);
            assertTrue(supportsNegativeIndices());
        } catch (IndexOutOfBoundsException ex) {
            assertFalse(supportsNegativeIndices());
        }

        // shouldn't throw in either case
        map.containsKey(-1);
        map.remove(-1);
    }

    public void testSerialization() throws Exception {
        IntMap<String> map = newInstance();
        map.put(1, "hi");
        map.put(2, null);
        map.put(3, "foo");

        // Ensure that the temporary data created from these methods aren't serialized
        map.keySet();
        map.values();
        map.entrySet();

        ByteArrayOutputStream oos = new ByteArrayOutputStream();

        try (ObjectOutputStream os = new ObjectOutputStream(oos)) {
            os.writeObject(map);
            os.flush();
        }
        ByteArrayInputStream iis = new ByteArrayInputStream(oos.toByteArray());

        ObjectInputStream is = new ObjectInputStream(iis);

        Object o = is.readObject();
        assertEquals(0, is.available());

        assertEquals(map,o);
    }

    public void testToString() throws Exception {
        IntMap<String> map = newInstance();
        map.put(1, "hi");
        assertEquals("{1=hi}", map.toString());
    }

    public static class ImmutableEntry<V> implements IntMap.Entry<V> {
        private int key;
        private V value;
        public ImmutableEntry(int key, V value) {
            this.key = key;
            this.value = value;
        }
        @Override
        public int getKey() {return this.key;}
        @Override
        public V getValue() {return this.value;}
        public V setValue(V value) {throw new UnsupportedOperationException();}
        @Override
        public int hashCode() {
            int result = 31 + this.key;
            result = 31 * result + ((this.value == null) ? 0 : this.value.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || !(obj instanceof IntMap.Entry)) {
                return false;
            }
            final IntMap.Entry<?> other = (IntMap.Entry<?>)obj;
            if (this.key != other.getKey()) {
                return false;
            }
            return this.value == null ? other.getValue() == null : this.value.equals(other.getValue());
        }
    }
}
