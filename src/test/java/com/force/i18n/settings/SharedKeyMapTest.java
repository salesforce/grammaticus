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

package com.force.i18n.settings;

import java.util.*;

import junit.framework.TestCase;

import org.junit.Assert;

/**
 * @author shansma
 */
public class SharedKeyMapTest extends TestCase {

    private static final int MAP_COUNT = 5;
    private static final List<Map<Object, Object>> SAMPLE_MAPS = new ArrayList<Map<Object, Object>>(MAP_COUNT);
    static {
        for (int i = 0; i < MAP_COUNT; i++) {
            Map<Object, Object> map = new HashMap<Object, Object>();
            SAMPLE_MAPS.add(Collections.unmodifiableMap(map));

            // shared keys
            for (char c = 'a'; c < 'f'; c++) {
                map.put(c, "shared " + c + i);
            }

            // unique keys
            map.put(i, "unique " + i);

            // null
            map.put("null", null);

            // different number of keys per map
            for (int j = 0; j <= i; j++) {
                map.put("diff" + j, "diff" + j);
            }
        }
    }

    private List<SharedKeyMap<Object, Object>> sharedKeyMaps; // initialized by setUp to SharedKeyMap versions of SAMPLE_MAPS

    public SharedKeyMapTest(String name) {
        super(name);
    }

    public SharedKeyMapTest() {}

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.sharedKeyMaps = SharedKeyMap.createSharedKeyMaps(SAMPLE_MAPS);
    }

    private static void assertMapsSame(List<? extends Map<?, ?>> maps1, List<? extends Map<?, ?>> maps2) {
        // test them front and back -- the maps in maps1 get their entrySets tested,
        // and the maps in maps2 get their get() and containsKey() and containsValue() tested.
        inner_assertMapsSame(maps1, maps2);
        inner_assertMapsSame(maps2, maps1);
    }

    private static void inner_assertMapsSame(List<? extends Map<?, ?>> maps1, List<? extends Map<?, ?>> maps2) {
        Assert.assertEquals(maps1.size(), maps2.size());
        for (int i = 0; i < maps1.size(); i++) {
            Map<?, ?> map1 = maps1.get(i);
            Map<?, ?> map2 = maps2.get(i);
            int count = 0;

            for (Map.Entry<?, ?> entry : map1.entrySet()) {
                count++;
                Assert.assertTrue(map2.containsKey(entry.getKey()));
                Assert.assertTrue(map2.containsValue(entry.getValue()));
                Assert.assertEquals(map2.get(entry.getKey()), entry.getValue());
            }
            Assert.assertEquals(map1.size(), map2.size());
            Assert.assertEquals(count, map1.size());
        }
    }

    public void testEntrySet() {
        assertMapsSame(SAMPLE_MAPS, sharedKeyMaps);
    }

    public void testEntrySetRemoval() {
        for (int i = 0; i < MAP_COUNT; i++) {
            Map<?, ?> sharedKeyMap = sharedKeyMaps.get(i);
            Map<?, ?> map = SAMPLE_MAPS.get(i);

            int size = sharedKeyMap.size();
            for (Iterator<?> j = sharedKeyMap.entrySet().iterator(); j.hasNext();) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>)j.next();
                Assert.assertEquals(map.get(entry.getKey()), entry.getValue());
                j.remove();
                Assert.assertEquals(--size, sharedKeyMap.size());
            }
            Assert.assertEquals(0, sharedKeyMap.size());
        }
    }

    public void testEntrySetConcurrentModification() {
        for (int i = 0; i < MAP_COUNT; i++) {
            Map<?, ?> sharedKeyMap = sharedKeyMaps.get(i);

            Iterator<?> j = sharedKeyMap.entrySet().iterator();
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>)j.next();
            sharedKeyMap.remove(entry.getKey());
            try {
                entry = (Map.Entry<?, ?>)j.next();
                // it should have thrown an exception
                Assert.fail();
            }
            catch (ConcurrentModificationException e) {
                // good
            }
        }
    }

    public void testRemove() {
        for (int i = 0; i < MAP_COUNT; i++) {
            Map<?, ?> sharedKeyMap = sharedKeyMaps.get(i);
            Map<?, ?> map = SAMPLE_MAPS.get(i);

            int size = sharedKeyMap.size();
            for (Object name : map.entrySet()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>)name;
                Object oldValue = sharedKeyMap.remove(entry.getKey());
                Assert.assertEquals(entry.getValue(), oldValue);
                Assert.assertEquals(--size, sharedKeyMap.size());
            }
            Assert.assertEquals(0, sharedKeyMap.size());
        }
    }

    public void testAdd() {
        // make HashMap copies of the sample maps and add crap to them and to the sharedKeyMaps
        List<Map<Object, Object>> refMaps = new ArrayList<Map<Object, Object>>(MAP_COUNT);
        for (int i = 0; i < MAP_COUNT; i++) {
            Map<Object, Object> sharedKeyMap = sharedKeyMaps.get(i);
            Map<Object, Object> refMap = new HashMap<Object, Object>(SAMPLE_MAPS.get(i));
            refMaps.add(refMap);

            sharedKeyMap.put("longVal", Long.valueOf(99999999999999999L + i));
            refMap.put("longVal", Long.valueOf(99999999999999999L + i));

            sharedKeyMap.put("overwrite", "ow1" + i);
            sharedKeyMap.put("overwrite", "ow2" + i);
            refMap.put("overwrite", "ow1" + i);
            refMap.put("overwrite", "ow2" + i);

            sharedKeyMap.put("null", null);
            refMap.put("null", null);

            sharedKeyMap.put("different key " + i, "dk" + i);
            refMap.put("different key " + i, "dk" + i);
        }

        // now make sure that the maps are the same
        assertMapsSame(refMaps, sharedKeyMaps);
    }

    public void testKeySet() {
        for (int i = 0; i < sharedKeyMaps.size(); i++) {
            Map<Object, Object> sharedKeyMap = sharedKeyMaps.get(i);
            Map<Object, Object> map = SAMPLE_MAPS.get(i);
            int count = 0;

            for (Iterator<Object> j = sharedKeyMap.keySet().iterator(); j.hasNext(); count++) {
                Object key = j.next();
                Assert.assertTrue("Expected key to be contained in SAMPLE_MAPS: " + key, map.containsKey(key));
            }
            Assert.assertEquals(count, sharedKeyMap.size());
        }
    }

    public void testKeySetRemoval() {
        for (int i = 0; i < MAP_COUNT; i++) {
            Map<Object, Object> sharedKeyMap = sharedKeyMaps.get(i);
            Map<Object, Object> map = SAMPLE_MAPS.get(i);

            int size = sharedKeyMap.size();
            for (Iterator<Map.Entry<Object, Object>> j = sharedKeyMap.entrySet().iterator(); j.hasNext();) {
                Map.Entry<Object, Object> e = j.next();
                Assert.assertEquals(map.get(e.getKey()), e.getValue());
                j.remove();
                Assert.assertEquals(--size, sharedKeyMap.size());
            }
            Assert.assertEquals(0, sharedKeyMap.size());
        }
    }

    public void testKeySetConcurrentModification() {
        for (int i = 0; i < MAP_COUNT; i++) {
            Map<Object, Object> sharedKeyMap = sharedKeyMaps.get(i);

            Iterator<Object> j = sharedKeyMap.keySet().iterator();
            Object key = j.next();
            sharedKeyMap.remove(key);
            try {
                key = j.next();
                // it should have thrown an exception
                Assert.fail();
            }
            catch (ConcurrentModificationException e) {
                // good
            }
        }
    }

    public void testClear() {
        for (int i = 0; i < MAP_COUNT; i++) {
            Map<Object, Object> sharedKeyMap = sharedKeyMaps.get(i);
            sharedKeyMap.clear();
            Assert.assertEquals(0, sharedKeyMap.size());
            Assert.assertEquals(0, sharedKeyMap.entrySet().size());
            Assert.assertEquals(0, sharedKeyMap.keySet().size());
        }
    }

    public void testValues() {
        for (int i = 0; i < MAP_COUNT; i++) {
            Map<Object, Object> sharedKeyMap = sharedKeyMaps.get(i);
            Map<Object, Object> map = SAMPLE_MAPS.get(i);
            Assert.assertTrue(sharedKeyMap.values().containsAll(map.values()));
            Assert.assertTrue(map.values().containsAll(sharedKeyMap.values()));
        }
    }

    public void testNullValuesArray() throws Exception {
        SharedKeyMap<Object, Object> skm = new SharedKeyMap<Object, Object>(-1);

        try {
            skm.put("hi", "bye");
            fail("shouldn't be allowed to modify this SKM");
        } catch (UnsupportedOperationException expected) { }

        try {
            skm.clear();
            fail("shouldn't be allowed to modify this SKM");
        } catch (UnsupportedOperationException expected) { }

        try {
            skm.remove("hi");
            fail("shouldn't be allowed to modify this SKM");
        } catch (UnsupportedOperationException expected) { }
    }

}
