/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.HashSet;
import java.util.Locale;
import junit.framework.TestCase;
import com.force.i18n.*;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;

/**
 * Various sanity test for GrammaticalTermMapImpl
 * 
 */
public class GrammaticalTermMapImplTest extends TestCase {
    /**
     * HumanLanguage for testing
     */
    private static final HumanLanguage US = LanguageProviderFactory.get().getLanguage(Locale.US);

    /**
     * Simple get/ put /isEmpty / sets tests
     * 
     */
    public void testSimpleUpdate() throws Exception{
        GrammaticalTermMapImpl<Noun> map = new GrammaticalTermMapImpl<>();
        assertTrue(map.isEmpty());
        Noun n1 = createNoun("n1");
        Noun n2 = createNoun("n2");
        map.put("n1", n1);
        map.put("n2", n2);
        assertFalse(map.isEmpty());
        assertEquals(map.get("n1"), n1);
        assertEquals(map.get("n2"), n2);
        assertEquals(map.keySet().size(), 2);
        assertEquals(map.entrySet().size(), 2);
    }

    /**
     * Test for writeJson
     */

    public void testSimpleWriteJson() throws Exception {
        GrammaticalTermMapImpl<Noun> map = new GrammaticalTermMapImpl<>();
        StringBuilder sb = new StringBuilder();
        map.writeJson(sb, null, new LanguageDictionary(US), null);
        assertEquals(sb.toString(), "{}");
        sb = new StringBuilder();
        Noun n1 = createNoun("n1");
        Noun n2 = createNoun("n2");
        map.put("n1", n1);
        map.put("n2", n2);

        map.writeJson(sb, null, new LanguageDictionary(US), null);
        assertEquals(sb.toString(), "{\"n1\":{\"t\":\"n\",\"l\":\"n1\",\"s\":\"c\",\"v\":{}},\"n2\":{\"t\":\"n\",\"l\":\"n2\",\"s\":\"c\",\"v\":{}}}");
    }

    /**
     * Test for makeSkinny
     */
    public void testMakeSkinny() {
        GrammaticalTermMap<Noun> map = new GrammaticalTermMapImpl<>();
        assertFalse("Initially false", map.isSkinny());
        Noun n1 = createNoun("n1");
        map.put("one", n1);
        map = map.makeSkinny();
        assertTrue("Now it's true.", map.isSkinny());
        try {
            map.put("two", n1);
            fail("no update allowed on skinny map");
        } catch (RuntimeException e) {
            // expected
        }     
    }

    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception {
        GrammaticalTermMapImpl<Noun> map = new GrammaticalTermMapImpl<>();
        assertSerializedEquals(map);
        Noun n1 = createNoun("n1");
        map.put("one", createNoun("one"));
        map.put("two", createNoun("two"));
        assertSerializedEquals(map);
        assertSerializedEquals((GrammaticalTermMapImpl<Noun>)map.makeSkinny());
        map.put("n1", n1);
        map.put("n1_a", n1);
        GrammaticalTermMapImpl<Noun> serialized = getSerialized(map);
        // same noun should be same in serialized map 
        assertTrue(serialized.get("n1") == serialized.get("n1_a"));
    }

    /**
     * Verify map is same after serialization
     * @param orig the original map
     * @throws Exception
     */
    private void assertSerializedEquals(GrammaticalTermMapImpl<Noun> orig) throws Exception {
        GrammaticalTermMapImpl<Noun> serialized = getSerialized(orig);
        assertEquals("isEmpty returns differently", orig.isEmpty(), serialized.isEmpty());
        assertEquals("keySet returns different", orig.keySet().size(), serialized.keySet().size());

        for (String key : orig.keySet()) {
            assertTrue("serialized map doesn't have "+key, serialized.containsKey(key));
            assertEquals("serialized map have different noun", orig.get(key), serialized.get(key));
        }
        assertEquals("The map returns different isSkinny ", orig.isSkinny(), serialized.isSkinny());            
    }

    /**
     * Serialize and deserialize a map.
     * @param input the map to serialize
     * @return the serialized map
     * @throws Exception if there's an error
     */
    @SuppressWarnings("unchecked")
    private GrammaticalTermMapImpl<Noun> getSerialized(GrammaticalTermMapImpl<Noun> input) throws Exception{
        // Serialize
        byte[] array = null;
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
              ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(input);
            array = baos.toByteArray();
        } 
        assertNotNull(array);
        // Deserialize
        try ( ByteArrayInputStream bais = new ByteArrayInputStream(array);
              ObjectInputStream ois = new ObjectInputStream(bais)){
            return (GrammaticalTermMapImpl<Noun>) ois.readObject();
        }
    }
        
    /**
     * Create a noun for testing
     */
    public Noun createNoun(String some) {
        LanguageDeclension declension = LanguageDeclensionFactory.get().getDeclension(US);
        return declension.createNoun(some, some+"alias", NounType.ENTITY, some, LanguageStartsWith.CONSONANT, LanguageGender.NEUTER, "", false, false);
    }
}
