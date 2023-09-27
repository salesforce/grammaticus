/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

import org.junit.Test;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.parser.BaseGrammaticalLabelTest;
import com.google.common.collect.Multimap;

/**
 * Test various issues around serialization of Dictionaries and NounForms, along with invariant testing
 *
 * @author stamm
 */
public class LanguageDictionarySerializationTest extends BaseGrammaticalLabelTest {
    private static final Logger logger = Logger.getLogger(LanguageDictionarySerializationTest.class.getName());

    public LanguageDictionarySerializationTest(String name) {
        super(name);
    }

    private void _testComplexGrammaticalFormInvariant(List<?> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            if (element instanceof ComplexGrammaticalForm) {
                assertEquals("Invariant for ComplexGrammaticalForm.invariant fails for " + element, i, ((ComplexGrammaticalForm)element).getOrdinal());
            } else {
                assertTrue("All Grammatical forms must be enums or extends ComplexGrammaticalForm", element.getClass().isEnum());
            }
        }
    }

    /**
     * Group of tests that verify that *all* noun forms, if they are Complex,
     * have the ordinal match the index into the list, or they are an enum.
     *
     * These tests are broken up by language type to avoid test timeouts.
     * Standard languages are further divided since they were flapping.
     *
     * Chances are you should run this again in the production environment, that's why there's
     * a test version so you don't have tests that run forever
     */
    @Test
    public void testDeclensionInvariants() throws Exception {
        declensionInvariantTester(LanguageProviderFactory.get().getAll());
    }


    /**
     * Helper method for testDeclensionInvariant* test methods.
     *
     * @param langs - list of languages to test
     * @throws Exception
     */
    private void declensionInvariantTester(List<? extends HumanLanguage> langs) throws Exception {
        for (HumanLanguage lang : langs) {
            loadDictionary(lang);
            LanguageDeclension declension = LanguageDeclensionFactory.get().getDeclension(lang);
            _testComplexGrammaticalFormInvariant(declension.getAllNounForms());
            _testComplexGrammaticalFormInvariant(declension.getAdjectiveForms());
            if (declension.hasArticle()) {
                _testComplexGrammaticalFormInvariant(declension.getArticleForms());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(LanguageDictionary dict, String name) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field f = LanguageDictionary.class.getDeclaredField(name);
        f.setAccessible(true);

        return (T) f.get(dict);
    }

    // ensure noun is shared across internal maps
    private void validateFields(LanguageDictionary dict) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        final String SINGULAR = "account";
        final String PLUARL = "accounts";

        GrammaticalTermMap<Noun> nounMap = dict.getNounMap();
        Multimap<String, Noun> nounsByEntityType = getPrivateField(dict, "nounsByEntityType");

        Noun expected = nounMap.get(SINGULAR);

        Noun actual = null;
        for (Noun n : nounsByEntityType.get(SINGULAR)) {
            if (SINGULAR.equals(n.getName())) {
                actual = n;
                break;
            }
        }
        assertNotNull(actual);
        assertSame(expected, actual);

        if (dict.getDeclension().hasPlural()) {
            GrammaticalTermMap<Noun> nounMapByPluralAlias = dict.getNounByPluralAlias();
            actual = nounMapByPluralAlias.get(PLUARL);
            assertNotNull(actual);
            assertSame(expected, actual);
        }
    }

    @Test
    public void testSerializeDictionary() throws Exception {
    	// TODO: This is slow, so only do the first ten
        for (HumanLanguage language : LanguageProviderFactory.get().getAll().subList(0, 10)) {
            LanguageDictionary dictionary = loadDictionary(language);
            File file = File.createTempFile("testSerialDictionary" + language.getLocale(), "");
            try {
                long start = System.nanoTime();
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                   oos.writeObject(dictionary);
                } catch (NotSerializableException ex) {
                    System.err.println("Failure to serialize " + language);
                    throw ex;
                }

                logger.info("Wrote " + language + " dictionary in " + (System.nanoTime() - start)/1000000 + " msec of size " + file.length());
                Noun noun = dictionary.getNoun("account", false);
                if (noun == null) continue;  // This means the dictionary doesn't exist.  Whatever.
                IdentityHashMap<NounForm,String> identityValues = new IdentityHashMap<NounForm,String>(noun.getAllDefinedValues());

                 start = System.nanoTime();
                 LanguageDictionary copy;

                 try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                     copy = (LanguageDictionary)ois.readObject();
                 }
                 logger.info("Read " + language + " dictionary in " + (System.nanoTime() - start)/1000000 + " msec");
                 validateFields(copy);

                 // Make sure the noun forms are the same
                 Noun copyNoun = copy.getNoun("account", false);
                 IdentityHashMap<NounForm, String> identityCopyValues = new IdentityHashMap<>(noun.getAllDefinedValues());

                 assertEquals("Serialized nouns aren't the same", noun, copyNoun);
                 assertSame("Serialized declensions are being duplicated", noun.getDeclension(), copyNoun.getDeclension());
                 assertEquals("Serialized nouns aren't the same", noun.getAllDefinedValues(), copyNoun.getAllDefinedValues());
                 assertSame("Serialized nouns aren't the same", noun.getDeclension(), copyNoun.getDeclension());
                 assertEquals("Serialized noun forms aren't the same", identityValues, identityCopyValues);
            }
            finally {
                file.delete();
            }
        }
    }

    public void testSerializeDictionarySpeedTest() throws Exception {
        for (HumanLanguage language : LanguageProviderFactory.get().getAll()) {
            LanguageDictionary dictionary = loadDictionary(language);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1000000);
            long start = System.nanoTime();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(dictionary);
            }

            long writeTime = (System.nanoTime() - start)/1000 ;

            byte[] data = baos.toByteArray();
            start = System.nanoTime();
             try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
             }
             logger.info("InMemTest: " + language.getLocale() + " dictionary in " + writeTime + "/" + (System.nanoTime() - start)/1000 + " usec; size= " + data.length);
        }
    }

}
