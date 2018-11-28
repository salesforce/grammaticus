/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.Locale;

import com.force.i18n.grammar.*;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;

import junit.framework.TestCase;

/**
 * Unit tests for LabelUtils
 *
 * @author stamm
 */
public class LabelUtilsUnitTest extends TestCase {
    public LabelUtilsUnitTest(String name) {
        super(name);
    }


    /**
     * Test the code that gets the form description in english.
     */
    public void testGetFormDescriptionInEnglish() {
        LanguageDeclension english = LanguageDeclensionFactory.get().getDeclension(LanguageProviderFactory.get().getLanguage(Locale.US));
        LanguageDeclension japanese = LanguageDeclensionFactory.get().getDeclension(LanguageProviderFactory.get().getLanguage(LanguageConstants.JAPANESE));
        LanguageDeclension swedish = LanguageDeclensionFactory.get().getDeclension(LanguageProviderFactory.get().getLanguage(LanguageConstants.SWEDISH));
        LanguageDeclension turkish = LanguageDeclensionFactory.get().getDeclension(LanguageProviderFactory.get().getLanguage(LanguageConstants.TURKISH));
        LabelUtils utils = LabelUtils.get();
        assertEquals("plural", utils.getFormDescriptionInEnglish(english, english.getNounForm(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE)));
        assertEquals("singular", utils.getFormDescriptionInEnglish(english, english.getNounForm(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE)));
        assertEquals("simple", utils.getFormDescriptionInEnglish(japanese, english.getNounForm(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE)));
        assertEquals("plural", utils.getFormDescriptionInEnglish(swedish, swedish.getNounForm(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE)));
        assertEquals("plural definite", utils.getFormDescriptionInEnglish(swedish,
                swedish.getApproximateNounForm(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE, LanguagePossessive.NONE, LanguageArticle.DEFINITE)));
        assertEquals("plural first person possessive nominative", utils.getFormDescriptionInEnglish(turkish,
                turkish.getApproximateNounForm(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE, LanguagePossessive.FIRST, LanguageArticle.ZERO)));
    }

    public void testSampleLabelFiles() {
        assertEquals(null, LabelUtils.getSampleGrammarFile(null));
        assertEquals(null, LabelUtils.getSampleGrammarFile(""));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><names><test></test></names>", LabelUtils.getSampleGrammarFile("<test></test>"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><names><test></test></names>", LabelUtils.getSampleGrammarFile("<?xml version=\"1.0\" encoding=\"UTF-8\"?><names><test></test></names>"));
        assertEquals("<adjectives><test></test></adjectives>", LabelUtils.getSampleGrammarFile("<adjectives><test></test></adjectives>"));

        final String preamble = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE iniFile SYSTEM \"labels.dtd\">";
        assertEquals(preamble + "<iniFile><section name=\"Test\"><param name=\"Test\" >My New Account</param></section></iniFile>", LabelUtils.getSampleLabelFile("My New Account"));
        assertEquals(preamble + "<iniFile><section name=\"Test\"><param name=\"Test\" alias=\"Foo\"></param></section></iniFile>", LabelUtils.getSampleLabelFile("alias=\"Foo\""));
    }

}
