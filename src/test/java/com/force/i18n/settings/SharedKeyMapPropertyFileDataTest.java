/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.settings;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import com.force.i18n.settings.SharedKeyMapPropertyFileData;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import junit.framework.TestCase;


/**
 * @author koliver
 */
public class SharedKeyMapPropertyFileDataTest extends TestCase {

    private SharedKeyMapPropertyFileData data;
    private SharedKeyMap<String, SharedKeyMap<String, Object>> seed;
    private Set<String> namespaces;

    public SharedKeyMapPropertyFileDataTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.seed = new SharedKeyMap<String, SharedKeyMap<String, Object>>();
        this.namespaces = new HashSet<String>();
        this.data = new SharedKeyMapPropertyFileData(Locale.US, true, seed, namespaces);
    }

    public void testGetSection() throws Exception {
        assertNull(this.data.getSection(null));
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
        assertFalse(this.data.containsSection(null));
        assertFalse(this.data.containsSection("section"));
        this.data.put("section", "param", "value");
        assertTrue(this.data.containsSection("section"));
    }

    public void testContains() throws Exception {
        assertFalse(this.data.contains("section", null));
        assertFalse(this.data.contains(null, null));
        assertFalse(this.data.contains("section", "param"));
        this.data.put("section", "param", "value");
        assertFalse(this.data.contains("section", null));
        assertFalse(this.data.contains(null, null));
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

    /**
     * Test concurrently compacting a SharedKeyMapPropertyFileData and adding sections to a
     * different SharedKeyMapPropertyFileData that shares the first's seed causes a ConcurrentModificationException.
     */
    public void testConcurrentCompaction() throws Exception {
        final SharedKeyMapPropertyFileData data2 = new SharedKeyMapPropertyFileData(new Locale("fr", "BE"), false, this.seed, this.namespaces);
        ThreadFactory tf = new ThreadFactoryBuilder().setDaemon(true).setNameFormat(getName() + "-%s").build();
        ExecutorService executor = Executors.newSingleThreadExecutor(tf);

        try {
            Future<Void> future = executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    for (int i = 0; i < 1000; i++) {
                        data.compact();

                        for (String s : data.getSectionNames()) {
                            if ("jive".equals(s)) {
                                break;
                            }
                        }
                    }
                    return null;
                }
            });

            for (int i = 0; i < 1000; i++) {
                data2.put("section_" + i, "key", "value");
            }

            future.get();
        } finally {
            executor.shutdownNow();
        }
    }
}
