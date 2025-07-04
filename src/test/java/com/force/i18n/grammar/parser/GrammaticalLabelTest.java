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

package com.force.i18n.grammar.parser;

import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.time.Duration;
import java.util.*;

import org.junit.Assert;
import org.junit.Test;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.AbstractLanguageDeclension.PluralNounForm;
import com.force.i18n.grammar.AbstractLanguageDeclension.SimpleModifierForm;
import com.force.i18n.grammar.GrammaticalLabelSet.GrammaticalLabelSetComposite;
import com.force.i18n.grammar.GrammaticalLabelSetFallbackImpl.ImmutableMapUnion;
import com.force.i18n.grammar.GrammaticalTerm.TermType;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
import com.force.i18n.settings.PropertyFileData;
import com.force.i18n.settings.SettingsSectionNotFoundException;
import com.google.common.base.Joiner;
import com.google.common.collect.*;

/**
 * @author stamm
 *
 */
public class GrammaticalLabelTest extends BaseGrammaticalLabelTest {

    public GrammaticalLabelTest(String name) {
        super(name);
    }

    public void testEnglishBackup() {
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(getDescriptor());
        HumanLanguage ENGLISH_CA = LanguageProviderFactory.get().getLanguage(Locale.CANADA);
        GrammaticalLabelSet set = loader.getSet(ENGLISH_CA);

        assertEquals(set.get("Sample", "click_here_to_create_new_account"), set.get(new LabelRef("Sample", "click_here_to_create_new_account")));
        assertEquals(set.getString("Sample", "click_here_to_create_new_account"), set.getString(new LabelRef("Sample", "click_here_to_create_new_account")));
        assertFalse(set.containsParam("Sample", "invalid"));
        assertFalse(set.containsParam("invalid", "invalid"));
        try {
            set.get(new LabelRef("invalid", "invalid", "invalid"));
            fail();
        } catch (SettingsSectionNotFoundException ex) {}
        assertEquals("__MISSING LABEL__ PropertyFile - val invalid not found in section Sample", set.getString(new LabelRef("Sample", "invalid", "invalid")));

    }

    /**
     * Sample test for validating that the "startsWith" value for a noun is correct.
     */
    public void testEnglishStartsWith() throws IOException {
        LanguageDictionary dictionary = loadDictionary(LanguageProviderFactory.get().getLanguage(Locale.US));
        StringBuilder badLabels = new StringBuilder();
        for (String nounName : dictionary.getAllTermNames(TermType.Noun)) {
            Noun noun = dictionary.getNoun(nounName, false);

            switch (Character.toLowerCase(noun.getDefaultString(false).charAt(0))) {
            case 'a':
            case 'e':
            case 'i':
                if (noun.getStartsWith() == LanguageStartsWith.CONSONANT) {
                    badLabels.append(noun.getName()).append("\n");
                }
                break;
            case 'o':
                // If this fails, that might be ok. O sometimes sounds like 'w', but generally, it's 'an opportunity'
                // same with
                if (noun.getStartsWith() == LanguageStartsWith.CONSONANT) {
                    badLabels.append(noun.getName()).append("\n");
                }
                break;
            case 'y':
            case 'u':
                // If this fails, that might be ok. But generally not. 'a usage date'. not 'an usage date'
                if (noun.getStartsWith() == LanguageStartsWith.VOWEL) {
                    badLabels.append(noun.getName()).append("\n");
                }
                break;
            default:
                if (noun.getStartsWith() == LanguageStartsWith.VOWEL) {
                    badLabels.append(noun.getName()).append("\n");
                }
                break;

            }
        }
        assertEquals(badLabels.toString(), 0, badLabels.length());
    }

    public void testReferencedCaseFormsExist() throws IOException {
        List<String> errMsgs = new ArrayList<String>();

        for (HumanLanguage language : LanguageProviderFactory.get().getAll()) {
            LanguageDeclension declension = LanguageDeclensionFactory.get().getDeclension(language);
            if (!declension.isInflected()) {
                continue; // Don't bother with simple declensions
            }

            // Get the set of inherited terms that are verboten
            GrammaticalLabelSet ls = getTestLabelSet(language, "", "");

            Set<String> inheritedNouns = ls.getDictionary().getAllInheritedTermNames(TermType.Noun);

            // Unwrap the layers of falling back: NOTE, this only works one level deep which is fine for now.
            GrammaticalLabelSet mainSet = ls;
            if (mainSet instanceof GrammaticalLabelSetComposite) {
                mainSet = ((GrammaticalLabelSetComposite)mainSet).getOverlay();
            }

            // Iterate through the label set
            for (String section : mainSet.sectionNames()) {
                Map<String, Object> sectionMap = mainSet.getSection(section);
                if (sectionMap == null) {
                    continue;
                }

                for (Map.Entry<String, Object> entry : sectionMap.entrySet()) {
                    if (!(entry.getValue() instanceof List)) {
                        continue; // Only care if there's an adjective
                    }
                    for (Object o : (List<?>)entry.getValue()) {
                        if (o instanceof NounRefTag) {
                            NounRefTag nrt = (NounRefTag)o;
                            String name = nrt.getName();
                            if (inheritedNouns.contains(name)) {
                                continue; // We only case about actual values.
                            }
                            NounForm form = nrt.getForm();
                            if (form instanceof LegacyArticledNounForm) {
                                form = ((LegacyArticledNounForm)form).getBaseNounForm(); // Unwrap the base form
                            }
                            if (!declension.getEntityForms().contains(form)) {
                                continue; // Ignore autoderived forms
                            }
                            Noun noun = mainSet.getDictionary().getNoun(name, false);
                            if (!noun.getAllDefinedValues().keySet().contains(form)) {
                                errMsgs.add(language.getLocaleString() + ":" + section + "." + entry.getKey() + ":"
                                        + ls.getLabelSectionToFilename().get(section) + " form "
                                        + LabelUtils.get().getFormDescriptionInEnglish(declension, form) + " for noun "
                                        + name);
                            }
                        }
                    }
                }
            }
        }

        assertEquals(Joiner.on("\n").join(errMsgs), 0, errMsgs.size());
    }

    /**
      * Validate that you can provide a parent loader for a set, and it will resolve aliases between
     * them to the parent set and return the right values.
     */
    public void testLabelSetParents() {
        URL base = GrammaticalLabelFileTest.class.getResource("/labels/labels.xml");
        GrammaticalLabelSetLoader baseLoader = new GrammaticalLabelSetLoader(base, "test1", null);
        URL overrides = GrammaticalLabelFileTest.class.getResource("/override/override.xml");
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(overrides, "test2", baseLoader);

        HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(Locale.US);

        GrammaticalLabelSet set = loader.getSet(ENGLISH);
        assertEquals("Choose a Color", set.getString("Icons", "colorPicker"));
        assertEquals("New", set.getString("Icons", "new"));
        assertEquals("English only", set.getString("Poker", "rule1"));
        assertEquals("Bullets", set.getString("Poker", "AA"));
        assertEquals("Activity", set.getString("Poker", "Nouns"));
        assertEquals("__MISSING LABEL__ PropertyFile - val unknown not found in section Poker", set.getString("Poker", "unknown"));
        assertEquals(ImmutableSet.of("publicsection"), set.getPublicSectionNames());
        assertEquals(ImmutableSet.of("Poker", "publicsection", "Global", "Icons"), set.sectionNames());
        try {
            set.get("invalid", "invalid");
            fail("Should have been invalid");
        } catch (SettingsSectionNotFoundException ex) {

        }

        HumanLanguage ENGLISH_GB = LanguageProviderFactory.get().getLanguage(Locale.UK);
        set = loader.getSet(ENGLISH_GB);
        assertEquals("New", set.getString("Icons", "new"));
        assertEquals("Choose a Colour", set.getString("Icons", "colorPicker"));
        assertEquals("English only", set.getString("Poker", "rule1"));
        assertEquals("Bullets", set.getString("Poker", "AA"));
        assertEquals("Activity", set.getString("Poker", "Nouns"));
        assertEquals("__MISSING LABEL__ PropertyFile - val unknown not found in section Poker", set.getString("Poker", "unknown"));
        assertEquals(ImmutableSet.of("publicsection"), set.getPublicSectionNames());

        HumanLanguage ENGLISH_CA = LanguageProviderFactory.get().getLanguage(new Locale.Builder().setLanguage("en").setRegion("CA").build());
        set = loader.getSet(ENGLISH_CA);
        assertEquals("Choose a Color", set.getString("Icons", "colorPicker"));

        HumanLanguage ENGLISH_IE = LanguageProviderFactory.get().getLanguage(new Locale.Builder().setLanguage("en").setRegion("IE").build());
        set = loader.getSet(ENGLISH_IE);
        assertEquals("Choose a Colour", set.getString("Icons", "colorPicker"));

        HumanLanguage JAPANESE = LanguageProviderFactory.get().getLanguage(Locale.JAPANESE);
        set = loader.getSet(JAPANESE);
        assertEquals("__MISSING LABEL__ PropertyFile - val unknown not found in section Poker", set.getString("Poker", "unknown"));
        assertEquals("色の選択", set.getString("Icons", "colorPicker"));
        assertEquals("新規", set.getString("Icons", "new"));
        assertEquals("English only", set.getString("Poker", "rule1"));
        assertEquals("勝者", set.getString("Poker", "AA"));
        assertEquals("Activity", set.getString("Poker", "Nouns")); // Yeah...
        assertEquals(ImmutableSet.of("publicsection"), set.getPublicSectionNames());
        assertTrue(set.containsSection("publicsection"));
        assertTrue(set.containsParam("publicsection", "file_not_found"));
        assertEquals(ImmutableSet.of("Poker", "publicsection", "Global", "Icons"), set.sectionNames());
        assertEquals(ImmutableSet.of("new", "activity", "aentity", "myactivity"), set.getParams("Global"));
        assertEquals(ImmutableSet.of("backup"), set.getParams("notfound", ImmutableSet.of("backup")));
        try {
            set.getParams("notfound");
            fail();
        } catch (SettingsSectionNotFoundException x) {}
        Assert.assertNotEquals(-1, set.getLastModified());
    }

    public void testGrammaticalTerms() {
        URL base = GrammaticalLabelFileTest.class.getResource("/labels/labels.xml");
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(base, "test1", null);
        HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(Locale.US);
        GrammaticalLabelSet set = loader.getSet(ENGLISH);
        // <Activity/>
        Object dynamic = set.get("Global", "activity");
        assertTrue("this should be a direct tag reference", dynamic instanceof TermRefTag);
        TermRefTag tag = ((TermRefTag)dynamic);
        assertTrue(tag.isNoun());
        assertFalse(tag.isDynamic());
        assertEquals(PluralNounForm.SINGULAR, tag.getForm(set.getDictionary(), false));

        // <My/> <Activity/>
        List<?> list = (List<?>)set.get("Global", "myactivity");
        assertEquals(3, list.size());
        TermRefTag my1 = (TermRefTag)list.get(0);
        assertTrue(my1.isAdjective());
        assertFalse(my1.isDynamic());
        assertEquals("my", my1.getName());
        assertEquals(SimpleModifierForm.SINGULAR, my1.getForm(set.getDictionary(), false));
        TermRefTag noun1 = (TermRefTag)list.get(2);
        assertTrue(noun1.isNoun());
        assertFalse(noun1.isDynamic());
        assertEquals("activity", noun1.getName());
        assertEquals(PluralNounForm.SINGULAR, noun1.getForm(set.getDictionary(), false));

        // <My/> <Entity entity="0"/>
        list = (List<?>)set.get("Global", "aentity");
        assertEquals(3, list.size());
        TermRefTag a1 = (TermRefTag)list.get(0);
        assertTrue(a1.isArticle());
        assertFalse(a1.isDynamic());
        assertEquals("a", a1.getName());
        ArticleForm articleForm = LanguageDeclensionFactory.get().getDeclension(ENGLISH).getApproximateArticleForm(
                LanguageStartsWith.CONSONANT, LanguageGender.FEMININE, LanguageNumber.SINGULAR,
                LanguageCase.NOMINATIVE);
        assertEquals(articleForm, a1.getForm(set.getDictionary(), false));
        TermRefTag noun2 = (TermRefTag)list.get(2);
        assertTrue(noun2.isNoun());
        assertTrue(noun2.isDynamic());
        assertEquals("entity", noun2.getName());
        assertEquals(PluralNounForm.SINGULAR, noun2.getForm(set.getDictionary(), false));

        RenamingProvider curProvider = RenamingProviderFactory.get().getProvider();
        try {
            Renameable pig = makeCustomRenameable("Pig", LanguageStartsWith.CONSONANT, "Pig", "Pigs");
            Renameable emu = makeCustomRenameable("Emu", LanguageStartsWith.VOWEL, "Emu", "Emus");
            MockRenamingProvider newProvider = new MockRenamingProvider(pig.getStandardNoun(ENGLISH), emu.getStandardNoun(ENGLISH));
            RenamingProviderFactory.get().setProvider(newProvider);
            assertEquals("A Pig", set.getString("Global", new Renameable[] {pig}, "aentity"));
            assertEquals("An Emu", set.getString("Global", new Renameable[] {emu}, "aentity"));
        } finally {
            RenamingProviderFactory.get().setProvider(curProvider);
        }
    }

    /**
     * Smoke test of the ImmutableMapUnion
     */
    public void testImmutableMapUnion() {
        Map<String,String> base = ImmutableMap.of("a", "1", "b", "2");
        Map<String,String> overlay = ImmutableMap.of("b", "3", "c", "4");
        Map<String,String> union = new ImmutableMapUnion<>(overlay, base);
        assertEquals("1", union.get("a"));
        assertEquals("3", union.get("b"));
        assertEquals("4", union.get("c"));
        assertTrue(union.containsKey("a"));
        assertTrue(union.containsKey("b"));
        assertTrue(union.containsKey("c"));
        assertFalse(union.containsKey("d"));
        assertTrue(union.containsValue("1"));
        assertFalse(union.containsValue("2"));
        assertTrue(union.containsValue("3"));
        assertTrue(union.containsValue("4"));
        assertEquals(ImmutableSet.of("a", "b", "c"), union.keySet());
        assertEquals(ImmutableSet.of("1", "3", "4"), new HashSet<>(union.values()));
        assertEquals(ImmutableSet.of(Maps.immutableEntry("a", "1"), Maps.immutableEntry("b", "3"), Maps.immutableEntry("c", "4")), union.entrySet());
    }

    /**
     * Validate that you can override dictionary entries in a child label set without problems.
     * MakeSkinny in German can cause UnsupportedOperationException because it replaces Maps
     * with ImmutableSortableMaps to save on memory
     */
    public void testMultipleOverrides() {
        // Go from samples to labels to override.  Make sure it loads correctly
        URL base = GrammaticalLabelFileTest.class.getResource("/sample/labels.xml");
        GrammaticalLabelSetLoader baseLoader = new GrammaticalLabelSetLoader(base, "sample", null);
        URL labels = GrammaticalLabelFileTest.class.getResource("/labels/labels.xml");
        GrammaticalLabelSetLoader labelsLoader = new GrammaticalLabelSetLoader(labels, "test1a", baseLoader);
        URL overrides = GrammaticalLabelFileTest.class.getResource("/override/override.xml");
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(overrides, "test2a", labelsLoader);

        HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(Locale.US);
        HumanLanguage GERMAN = LanguageProviderFactory.get().getLanguage(Locale.GERMAN);

        GrammaticalLabelSet set = loader.getSet(ENGLISH);
        assertEquals("New", set.getString("Icons", "new"));
        set = loader.getSet(GERMAN);
        assertEquals("Neu", set.getString("Icons", "new"));
    }


    /**
     * Test behavior of {@link GrammaticalLabelSetLoader#setUseTranslatedLanguage(boolean)}
     * @throws Exception
     */
    @Test
    public void testUseTranslatedLanguage() throws Exception {
        final HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(Locale.US);
        final HumanLanguage ENGLISH_GB = LanguageProviderFactory.get().getLanguage(LanguageConstants.ENGLISH_GB);
        final HumanLanguage ENGLISH_AU = LanguageProviderFactory.get().getLanguage(LanguageConstants.ENGLISH_AU);
        final HumanLanguage ENGLISH_CA = LanguageProviderFactory.get().getLanguage(LanguageConstants.ENGLISH_CA);
        final HumanLanguage ROMANIAN = LanguageProviderFactory.get().getLanguage(LanguageConstants.ROMANIAN);
        final HumanLanguage MOLDOVAN = LanguageProviderFactory.get().getLanguage(LanguageConstants.MOLDOVAN);

        // check statically defined flag that impacts dictionary construction. if false, don't test dictionary
        boolean usingProxy = getPrivateField(LanguageDeclensionFactory.class, "USE_PROXY")
                .getBoolean(LanguageDeclensionFactory.get());

        GrammaticalLabelSetLoader loader = getLoader();
        loader.setUseTranslatedLanguage(true);

        GrammaticalLabelSet enSet = loader.getSet(ENGLISH);
        assertTrue(enSet instanceof GrammaticalLabelSetImpl);

        // en_GB fall backs to en_US. dictionary is different, but its fallback set is exactly equal to en-US
        GrammaticalLabelSet gbSet = loader.getSet(ENGLISH_GB);
        assertTrue(gbSet instanceof GrammaticalLabelSetFallbackImpl);
        assertEquals(gbSet.getDictionary().getLanguage(), ENGLISH_GB);
        if (usingProxy) assertFalse(compareDictionaryExceptLanguage(gbSet.getDictionary(), enSet.getDictionary()));
        assertSameFallbackSet((GrammaticalLabelSetFallbackImpl)gbSet, enSet);

        // en_AU fall backs to en_GB and shares dictionary/label data
        GrammaticalLabelSet auSet = loader.getSet(ENGLISH_AU);
        assertEquals(auSet.getDictionary().getLanguage(), ENGLISH_AU);
        if (usingProxy) assertTrue(compareDictionaryExceptLanguage(auSet.getDictionary(), gbSet.getDictionary()));
        assertSameFallbackSet((GrammaticalLabelSetFallbackImpl)auSet, gbSet);

        // en_CA fall backs to en_US and shares dictionary/label data
        GrammaticalLabelSet caSet = loader.getSet(ENGLISH_CA);
        assertEquals(caSet.getDictionary().getLanguage(), ENGLISH_CA);
        if (usingProxy) assertTrue(compareDictionaryExceptLanguage(caSet.getDictionary(), enSet.getDictionary()));
        assertSameFallbackSet((GrammaticalLabelSetFallbackImpl)caSet, enSet);

        GrammaticalLabelSet roSet = loader.getSet(ROMANIAN);
        assertEquals(roSet.getDictionary().getLanguage(), ROMANIAN);
        assertFalse(compareDictionaryExceptLanguage(roSet.getDictionary(), enSet.getDictionary()));

        GrammaticalLabelSet moSet = loader.getSet(MOLDOVAN);
        assertEquals(moSet.getDictionary().getLanguage(), MOLDOVAN);
        if (usingProxy) assertTrue(compareDictionaryExceptLanguage(moSet.getDictionary(), roSet.getDictionary()));
        assertSameFallbackSet((GrammaticalLabelSetFallbackImpl)moSet, roSet);

        // compare every label entries. create another loader w/o 'useTranslatedLanguage', which provides original behavior
        GrammaticalLabelSetLoader originalLoader = getLoader();
        loader.setUseTranslatedLanguage(false); // this is the default behavior, but just in case.
        for (HumanLanguage l : LanguageProviderFactory.get().getAll()) {
            if (l.isTestOnlyLanguage()) continue;
            compareLabelSet(l, originalLoader, loader);
        }
    }

    private boolean compareDictionaryExceptLanguage(LanguageDictionary src, LanguageDictionary expected) throws Exception {
        assertNotSame(src.getLanguage(), expected.getLanguage());

        assertNotSame(src.getDeclension(), expected.getDeclension());
        assertNotSame(src.getDeclension().getLanguage(), expected.getDeclension().getLanguage());
        assertSame(src.getLanguage(), src.getDeclension().getLanguage());

        // gain access to private fields for comparison
        Field nounMap = getPrivateField(LanguageDictionary.class, "nounMap");
        Field nounMapByPluralAlias  = getPrivateField(LanguageDictionary.class, "nounMapByPluralAlias");
        Field adjectiveMap  = getPrivateField(LanguageDictionary.class, "adjectiveMap");
        Field articleMap  = getPrivateField(LanguageDictionary.class, "articleMap");
        Field nounVersionOverrides  = getPrivateField(LanguageDictionary.class, "nounVersionOverrides");
        Field isSkinny  = getPrivateField(LanguageDictionary.class, "isSkinny");

        // not using assertSame here.  see caller; this method is used by negative test too.
        return nounMap.get(src) == nounMap.get(expected)
            && nounMapByPluralAlias.get(src) == nounMapByPluralAlias.get(expected)
            && adjectiveMap.get(src) == adjectiveMap.get(expected)
            && articleMap.get(src) == articleMap.get(expected)
            && nounVersionOverrides.get(src) == nounVersionOverrides.get(expected)
            && isSkinny.get(src) == isSkinny.get(expected);
    }

    private void assertSameFallbackSet(GrammaticalLabelSetFallbackImpl src, GrammaticalLabelSet expectedParent)
            throws Exception {
        HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(Locale.US);

        GrammaticalLabelSet srcFallback = src.getFallback();
        HumanLanguage srcLanguage = src.getDictionary().getLanguage();
        HumanLanguage srcFallbackLanguage = srcFallback.getDictionary().getLanguage();

        assertNotSame(srcLanguage, srcFallbackLanguage);
        if (srcFallback instanceof GrammaticalLabelSetComposite)
            srcFallback = ((GrammaticalLabelSetComposite)srcFallback).getOverlay();

        GrammaticalLabelSet parentMain = expectedParent;
        if (expectedParent instanceof GrammaticalLabelSetComposite)
            parentMain = ((GrammaticalLabelSetComposite)expectedParent).getOverlay();
        assertSame(srcFallback, parentMain);

        // gain access to non-public inner class/fields
        final String CLASS_NAME = "com.force.i18n.grammar.GrammaticalLabelSetFallbackImpl$CompositePropertyFileDataImpl";
        ClassLoader clsLoader = ClassLoader.getSystemClassLoader();
        Class<?> innerClazz = clsLoader.loadClass(CLASS_NAME);
        Field fOverlay = getPrivateField(innerClazz, "overlay");
        Field fFallback = getPrivateField(innerClazz, "fallback");

        PropertyFileData d1 = src.getPropertyFileData();
        assertEquals(CLASS_NAME, d1.getClass().getName());
        d1 = (PropertyFileData)fFallback.get(d1);
        if (!srcLanguage.isTranslatedLanguage() && srcFallbackLanguage != ENGLISH) {
            assertEquals(CLASS_NAME, d1.getClass().getName());
            d1 = (PropertyFileData)fOverlay.get(d1);
        }

        PropertyFileData d2 = expectedParent.getPropertyFileData();
        if (expectedParent instanceof GrammaticalLabelSetComposite) {
            assertEquals(CLASS_NAME, d2.getClass().getName());
            d2 = (PropertyFileData)fOverlay.get(d2);
        }
        assertSame(d1, d2);
    }

    Field getPrivateField(Class<?> clazz, String name) throws Exception {
        Field f = clazz.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private void compareLabelSet(HumanLanguage lang, GrammaticalLabelSetProvider expectedLoader, GrammaticalLabelSetProvider testLoader) {
        HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(Locale.US);
        GrammaticalLabelSet enSet = testLoader.getSet(ENGLISH);

        GrammaticalLabelSet src = expectedLoader.getSet(lang);
        GrammaticalLabelSet dst = testLoader.getSet(lang);

        // labels set may have fallback sets that could end up w/ returning merged sections / params from multiple
        // result. wrap 'em w/ TreeSet here to reduce unnecessary duplicates.
        Set<String> srcSec = new TreeSet<String>(src.sectionNames());
        Set<String> dstSec = new TreeSet<String>(dst.sectionNames());

        ArrayList<String> errors = new ArrayList<String>();
        for (String s : srcSec) {
            if (!dstSec.contains(s)) errors.add(s);
        }
        Assert.assertTrue(lang.getLocaleString() + ": sections does not exits in new set: " + errors.toString(),
                errors.isEmpty());

        for (String sec : srcSec) {
            Set<String> srcParams = new TreeSet<String>(src.getParams(sec));
            for (String p : srcParams) {
                if (!dst.containsParam(sec, p)) {
                    // skip if en_US in base labelset doesn't have it
                    if (!enSet.containsParam(sec, p)) continue;

                    Assert.fail(lang.getLocaleString() + ": " + sec + "." + p + " does not exist");
                }
                compareLabel(lang, sec, p, src.get(sec, p, null), dst.get(sec, p, null));
            }
        }
    }

    // compare two label entries exactly matches
    private void compareLabel(HumanLanguage lang, String sec, String param, Object src, Object dst) {
        String errorMsg = lang.getLocaleString() + ": " + sec + "." + param;

        if (src instanceof List<?>) {
            Object[] src_array = ((List<?>)src).toArray();
            Object[] dst_aray = ((List<?>)dst).toArray();

            Assert.assertEquals(errorMsg + " different length.", src_array.length, dst_aray.length);
            for (int i = 0; i < src_array.length; i++) {
                if (src_array[i] instanceof RefTag)
                    Assert.assertEquals(errorMsg, src_array[i].toString(), dst_aray[i].toString());
                else
                    Assert.assertEquals(errorMsg, src_array[i], dst_aray[i]);
            }
        } else {
            if (src instanceof RefTag)
                Assert.assertEquals(errorMsg, src.toString(), dst.toString());
            else
                Assert.assertEquals(errorMsg, src, dst);
        }
    }

    @Test
    public void testLabelSetLoaderConfig() {
        LabelSetLoaderConfig config = new LabelSetLoaderConfig(null, null);

        // expiration.  any number is fine
        config.setCacheExpireAfter(Duration.ofMinutes(1));
        assertEquals(Duration.ofMinutes(1), config.getCacheExpireAfter());

        // max size.  0 or negative becomes 0
        assertEquals(config, config.setCacheMaxSize(0));
        assertEquals(0, config.getCacheMaxSize());

        assertEquals(config, config.setCacheMaxSize(-5));
        assertEquals(0, config.getCacheMaxSize());

        // any other number should be fine
        assertEquals(config, config.setCacheMaxSize(3));
        assertEquals(3, config.getCacheMaxSize());
    }

    @Test
    @SuppressWarnings("removal")
    public void testCacheConfig() throws InterruptedException {
        HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(Locale.US);
        HumanLanguage ENGLISH_GB = LanguageProviderFactory.get().getLanguage(LanguageConstants.ENGLISH_GB);
        HumanLanguage ENGLISH_AU = LanguageProviderFactory.get().getLanguage(LanguageConstants.ENGLISH_AU);
        HumanLanguage FRENCH = LanguageProviderFactory.get().getLanguage(LanguageConstants.FRENCH);
        HumanLanguage GERMAN = LanguageProviderFactory.get().getLanguage(LanguageConstants.GERMAN);

        GrammaticalLabelSetDescriptor desc = getDescriptor();

        LabelSetLoaderConfig config = new LabelSetLoaderConfig(desc, null);
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(config);
        // default is Ceffeine.  comparing with the class name because the class is not public
        assertEquals("CaffeinatedGuavaLoadingCache", loader.getCache().getClass().getSimpleName());

        // try switching to Guava cache.
        config.setCaffeine(false);
        GrammaticalLabelSetLoader guavaLoader = new GrammaticalLabelSetLoader(config);
        assertEquals("LocalLoadingCache", guavaLoader.getCache().getClass().getSimpleName());

        assertThrows(UnsupportedOperationException.class, () -> guavaLoader.getCacheBuilder(config));

        // Test expiration
        // re-construct the loader with Caffeine and set expiration period to 1ms
        config.setCaffeine(true);
        config.setCacheExpireAfter(Duration.ofMillis(1));
        loader = new GrammaticalLabelSetLoader(config);

        loader.getSet(ENGLISH);
        Thread.sleep(1);
        assertNull(loader.getCache().getIfPresent(desc));

        loader.getSet(FRENCH);
        Thread.sleep(1);
        assertNull(loader.getCache().getIfPresent(desc));
        assertNull(loader.getCache().getIfPresent(desc.getForOtherLanguage(FRENCH)));

        // Test max size of entries
        config.setCacheExpireAfter(Duration.ZERO); // no time-based eviction
        config.setCacheMaxSize(3);
        loader = new GrammaticalLabelSetLoader(config);
        assertEquals(0, loader.getCache().size());

        loader.getSet(ENGLISH);
        Thread.sleep(1);
        assertEquals(1, loader.getCache().size());

        loader.getSet(ENGLISH_AU);
        Thread.sleep(1);
        assertEquals(3, loader.getCache().size());
        assertNotNull(loader.getCache().getIfPresent(desc));
        assertNotNull(loader.getCache().getIfPresent(desc.getForOtherLanguage(ENGLISH_GB)));
        assertNotNull(loader.getCache().getIfPresent(desc.getForOtherLanguage(ENGLISH_AU)));

        // Caffeine does not evict prior the threadshold, but after the size crossed
        loader.getSet(FRENCH);
        Thread.sleep(5);
        assertEquals(3, loader.getCache().size());
        assertNotNull(loader.getCache().getIfPresent(desc.getForOtherLanguage(FRENCH)));

        loader.getSet(GERMAN);
        Thread.sleep(5);
        assertEquals(3, loader.getCache().size());
        assertNotNull(loader.getCache().getIfPresent(desc.getForOtherLanguage(GERMAN)));
    }
}
