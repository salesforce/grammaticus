/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.parser;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.junit.Assert;

import com.force.i18n.*;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.GrammaticalLabelSet.GrammaticalLabelSetComposite;
import com.force.i18n.grammar.GrammaticalLabelSetFallbackImpl.ImmutableMapUnion;
import com.force.i18n.grammar.GrammaticalTerm.TermType;
import com.force.i18n.grammar.LanguageDeclension.PluralNounForm;
import com.force.i18n.grammar.LanguageDeclension.SimpleModifierForm;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
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
            Noun noun = dictionary.getNoun(nounName,false);

            switch (Character.toLowerCase(noun.getDefaultString(false).charAt(0))) {
            case 'a':
            case 'e':
            case 'i':
                if (noun.getStartsWith() == LanguageStartsWith.CONSONANT) {
                    badLabels.append(noun.getName()).append("\n");
                }
                break;
            case 'o':
                // If this fails, that might be ok.  O sometimes sounds like 'w', but generally, it's 'an opportunity'
                // same with
                if (noun.getStartsWith() == LanguageStartsWith.CONSONANT) {
                    badLabels.append(noun.getName()).append("\n");
                }
                break;
            case 'y':
            case 'u':
                // If this fails, that might be ok.  But generally not.  'a usage date'.  not 'an usage date'
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
            if (!declension.isInflected())
             {
                continue;  // Don't bother with simple declensions
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

                for (Map.Entry<String,Object> entry : sectionMap.entrySet()) {
                    if (!(entry.getValue() instanceof List))
                     {
                        continue;  // Only care if there's an adjective
                    }
                    for (Object o : (List<?>)entry.getValue()) {
                        if (o instanceof NounRefTag) {
                            NounRefTag nrt = (NounRefTag) o;
                            String name = nrt.getName();
                            if (inheritedNouns.contains(name))
                             {
                                continue;  // We only case about actual values.
                            }
                            NounForm form = nrt.getForm();
                            if (form instanceof LegacyArticledNounForm)
                             {
                                form = ((LegacyArticledNounForm)form).getBaseNounForm();  // Unwrap the base form
                            }
                            if (!declension.getEntityForms().contains(form))
                             {
                                continue;  // Ignore autoderived forms
                            }
                            Noun noun = mainSet.getDictionary().getNoun(name, false);
                            if (!noun.getAllDefinedValues().keySet().contains(form)) {
                                errMsgs.add(language.getLocaleString() + ":" + section + "." + entry.getKey() + ":" + ls.getLabelSectionToFilename().get(section)
                                    + " form " + LabelUtils.get().getFormDescriptionInEnglish(declension, form) + " for noun " + name );
                            }
                        }
                    }
                }
            }
        }

        if (errMsgs.size() > 0) {
            System.err.println(Joiner.on("\n").join(errMsgs));
        }
        //assertEquals(TextUtil.collectionToString(errMsgs, "\n"), 0, errMsgs.size());
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
        HumanLanguage ENGLISH_GB = LanguageProviderFactory.get().getLanguage(Locale.UK);
        HumanLanguage JAPANESE = LanguageProviderFactory.get().getLanguage(Locale.JAPANESE);

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

        set = loader.getSet(ENGLISH_GB);
        assertEquals("New", set.getString("Icons", "new"));
        assertEquals("Choose a Colour", set.getString("Icons", "colorPicker"));
        assertEquals("English only", set.getString("Poker", "rule1"));
        assertEquals("Bullets", set.getString("Poker", "AA"));
        assertEquals("Activity", set.getString("Poker", "Nouns"));
        assertEquals("__MISSING LABEL__ PropertyFile - val unknown not found in section Poker", set.getString("Poker", "unknown"));
        assertEquals(ImmutableSet.of("publicsection"), set.getPublicSectionNames());

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
        TermRefTag tag = ((TermRefTag) dynamic);
        assertTrue(tag.isNoun());
        assertFalse(tag.isDynamic());
        assertEquals(PluralNounForm.SINGULAR, tag.getForm(set.getDictionary(), false));

        // <My/> <Activity/>
        List<?> list = (List<?>) set.get("Global", "myactivity");
        assertEquals(3, list.size());
        TermRefTag my1 = (TermRefTag) list.get(0);
        assertTrue(my1.isAdjective());
        assertFalse(my1.isDynamic());
        assertEquals("my", my1.getName());
        assertEquals(SimpleModifierForm.SINGULAR, my1.getForm(set.getDictionary(), false));
        TermRefTag noun1 = (TermRefTag) list.get(2);
        assertTrue(noun1.isNoun());
        assertFalse(noun1.isDynamic());
        assertEquals("activity", noun1.getName());
        assertEquals(PluralNounForm.SINGULAR, noun1.getForm(set.getDictionary(), false));


        // <My/> <Entity entity="0"/>
        list = (List<?>) set.get("Global", "aentity");
        assertEquals(3, list.size());
        TermRefTag a1 = (TermRefTag) list.get(0);
        assertTrue(a1.isArticle());
        assertFalse(a1.isDynamic());
        assertEquals("a", a1.getName());
        ArticleForm articleForm = LanguageDeclensionFactory.get().getDeclension(ENGLISH).getApproximateArticleForm(LanguageStartsWith.CONSONANT,
                LanguageGender.FEMININE, LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE);
        assertEquals(articleForm, a1.getForm(set.getDictionary(), false));
        TermRefTag noun2 = (TermRefTag) list.get(2);
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
}
