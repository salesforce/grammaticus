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
import java.util.Map.Entry;

import junit.framework.TestCase;

/**
 * @author koliver
 */
public class MapPropertyFileDataTest extends TestCase {

    private MapPropertyFileData data;

    public MapPropertyFileDataTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.data = new MapPropertyFileData(Locale.US);
    }

    public void testGetLocaleId() throws Exception {
        assertEquals(Locale.US, this.data.getLocale());

        MapPropertyFileData propData = new MapPropertyFileData();
        try {
            propData.getLocale();
            fail("Not allowed to ask for locale when constructed without one");
        } catch (IllegalStateException expected) { }
    }

    public void testGetSection() throws Exception {
        assertNull(this.data.getSection("section that doesn't exist"));

        this.data.put("section", "param", "value");

        Map<String, Object> section = this.data.getSection("section");
        assertEquals(1, section.size());
        assertEquals("value", section.get("param"));

        try {
            section.put("another param", "some other value");
            fail("we should have an unmodifiable view");
        } catch (UnsupportedOperationException expected) { }
    }

    public void testSectionNamespaces() throws Exception {
        assertEquals(Collections.emptySet(), this.data.getPublicSectionNames());

        this.data.put("section", "param", "value");
        assertEquals(Collections.emptySet(), this.data.getPublicSectionNames());

        this.data.setSectionAsPublic("section");
        assertEquals(Collections.singleton("section"), this.data.getPublicSectionNames());

        try {
            this.data.getPublicSectionNames().add("another section");
            fail("we should have an unmodifiable view");
        } catch (UnsupportedOperationException expected) { }

        try {
            this.data.setSectionAsPublic("FOO");
        } catch (RuntimeException expected) {}
    }

    public void testEntrySet() throws Exception {
        Set<Entry<String, Map<String, Object>>> entrySet = this.data.entrySet();
        assertEquals(0, entrySet.size());

        this.data.put("section", "param", "value");
        entrySet = this.data.entrySet();
        assertEquals(1, entrySet.size());
        for (Iterator<Map.Entry<String, Map<String, Object>>> iter = entrySet.iterator(); iter.hasNext(); ) {
            Map.Entry<String, Map<String, Object>> obj = iter.next();
            assertEquals("section", obj.getKey());
            assertFalse(iter.hasNext());
            try {
                iter.remove();
                fail("we should have an unmodifiable view");
            } catch (UnsupportedOperationException expected) { }
        }
    }

    public void testGetSectionNames() throws Exception {
        assertEquals(0, this.data.getSectionNames().size());

        this.data.put("section", "param", "value");
        Set<String> sectionNames = this.data.getSectionNames();
        assertEquals(1, sectionNames.size());
        assertTrue(sectionNames.contains("section"));

        try {
            sectionNames.add("another section");
            fail("we should have an unmodifiable view");
        } catch (UnsupportedOperationException expected) { }
    }

    public void testContainsSection() throws Exception {
        assertFalse(this.data.containsSection("section"));
        this.data.put("section", "param", "value");
        assertTrue(this.data.containsSection("section"));
    }

    public void testContains() throws Exception {
        assertFalse(this.data.contains("section", "param"));
        this.data.put("section", "param", "value");
        assertTrue(this.data.contains("section", "param"));
        assertFalse(this.data.contains("section", "another param"));
    }

    public void testRemoveSection() throws Exception {
        // should be okay to remove a non-existent section
        this.data.removeSection("doesn't exist");

        this.data.put("section", "param", "value");
        assertNotNull(this.data.getSection("section"));

        this.data.removeSection("section");
        assertNull(this.data.getSection("section"));
    }

    public void testRemove() throws Exception {
         // should be okay to remove a non-existent parameter
        this.data.remove("doesn't exist", "blah");

        this.data.put("section", "param", "value");
        assertEquals("value", this.data.remove("section", "param"));
        assertNull(this.data.remove("section", "param"));
    }

}
