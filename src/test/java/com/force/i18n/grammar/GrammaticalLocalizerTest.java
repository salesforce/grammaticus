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

package com.force.i18n.grammar;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Assert;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.LanguageDeclensionFactory;
import com.force.i18n.grammar.parser.BaseGrammaticalLabelTest;
import com.force.i18n.grammar.parser.GrammaticalLabelFileTest;
import com.force.i18n.grammar.parser.GrammaticalLabelSetLoader;
import com.google.common.collect.ImmutableMap;

/**
 *
 * @author cchen
 */
public class GrammaticalLocalizerTest extends BaseGrammaticalLabelTest {
    private static final Locale chineseLocale = new Locale.Builder().setLanguage("zh").setRegion("CH").build();

    public GrammaticalLocalizerTest(String name) {
        super(name);
    }

    private LocalizerProvider origProvider;

    @SuppressWarnings("deprecation")
    @Override
    protected void setUp() throws Exception {
        origProvider = LocalizerFactory.get();
        URL sampleJarUrl = getLabelURL();
        GrammaticalLabelSetDescriptor desc = new LabelSetDescriptorImpl(sampleJarUrl, LanguageProviderFactory.get().getBaseLanguage(), "sample");
        LocalizerProvider glf = new GrammaticalLocalizerFactory(GrammaticalLocalizerFactory.getLoader(desc, null));
        assertEquals(sampleJarUrl, glf.getLabelsDirectory());
        LocalizerFactory.set(glf);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        LocalizerFactory.set(origProvider);
        super.tearDown();
    }

    // @NotThreadSafe
    public void testGrammaticalLocalizerFactory() {
        GrammaticalLocalizer gl = (GrammaticalLocalizer)LocalizerFactory.get().getLocalizer(Locale.US);
        SharedLabelSet ls = gl.getLabelSet();
        assertEquals(new LabelRef("Sample_Entity", "created_by"), ls.get("Sample", "created_by"));
        assertEquals("Created by...", ls.getString("Sample", "created_by"));
        assertEquals("Created by...", gl.getLabel("Sample", "created_by"));
        assertEquals("Created by...", gl.getLabel("Sample", "created_by"));
        assertEquals("Created by...", gl.getLabel("Sample", "created_by"));

        // Try it in a non-translated language that fallsback to ENGLISH_GB, then ENGLISH
        ls = LocalizerFactory.get().getLocalizer(new Locale.Builder().setLanguage("en").setRegion("IN").build()).getLabelSet();
        // Fallbacks resolve all aliases immediately.
        assertEquals("Created by...", ls.get("Sample", "created_by"));
        assertEquals("Created by...", ls.getString("Sample", "created_by"));
    }

    public void testLabelThrow() {
        GrammaticalLocalizer gl = (GrammaticalLocalizer) LocalizerFactory.get().getLocalizer(Locale.US);

        //Test for valid Label retrieval using LabelThrow
        Object[] arguments = new Object[] { "1","5" };
        assertEquals("Step 1 of 5", gl.getLabelThrow("Sample", "RightHeader", arguments));

        //Test for invalid Label.An exception should be thrown when invalid label is referenced
         try {
             assertEquals("Step 1 of 5", gl.getLabelThrow("Sample", "InvalidLabel", arguments));
             fail("This should not happen!");
         }
         catch (Exception expected) {}
    }


    /**
     * Test the escapeHtml flag on entities in cases where the label will be used unescaped directly.
     */
    public void testSampleLabels() throws Exception {
        RenamingProvider curProvider = RenamingProviderFactory.get().getProvider();
        try {
            GrammaticalLocalizer gl = (GrammaticalLocalizer) LocalizerFactory.get().getLocalizer(Locale.US);
            Renameable account = getStandardRenameable("Account");
            assertEquals("Back to List: Accounts", gl.getLabel("Sample", new Renameable[]{account}, "last_type_list"));
            assertEquals("Back to Account: foo", gl.getLabel("Sample", new Renameable[]{account}, "back_detail", "foo"));
            assertEquals("Click here to create a new account now.", gl.getLabel("Sample", "click_here_to_create_new_account", "Click here"));  // Note "a new", not "an new"
            assertEquals("Open an account", gl.getLabel("Sample", "openAnAccount"));

            MockRenamingProvider newProvider = new MockRenamingProvider(makeEnglishNoun("Account", NounType.ENTITY, LanguageStartsWith.CONSONANT,
                    "Client or Person", "Clients & People"));
            RenamingProviderFactory.get().setProvider(newProvider);
            assertEquals("Back to List: Clients & People", gl.getLabel("Sample", new Renameable[]{account}, "last_type_list"));
            assertEquals("Back to Client or Person: foo", gl.getLabel("Sample", new Renameable[]{account}, "back_detail", "foo"));
            assertEquals("Click here to create a new client or person now.", gl.getLabel("Sample", "click_here_to_create_new_account", "Click here"));
            assertEquals("Open a client or person", gl.getLabel("Sample", "openAnAccount"));

            // Turn off renamed nouns temporarily
            newProvider.setUseRenamedNouns(false);
            assertEquals("Open an account", gl.getLabel("Sample", "openAnAccount"));
        } finally {
            RenamingProviderFactory.get().setProvider(curProvider);
        }
    }



    /**
     * Test nouns in a more inflected language than english, german.
     */
    public void testSampleLabelsGerman() throws Exception {
        RenamingProvider curProvider = RenamingProviderFactory.get().getProvider();
        try {
            GrammaticalLocalizer gl = (GrammaticalLocalizer) LocalizerFactory.get().getLocalizer(Locale.GERMAN);
            assertEquals("Klicken Sie hier, um jetzt einen neuen Account zu erstellen.", gl.getLabel("Sample", "click_here_to_create_new_account", "Klicken Sie hier"));

            // Account in german is "Account", "Konto", or "Kunde" depending on what you want, like client above.  The accusative is used here.
            LanguageDeclension germanDecl = LanguageDeclensionFactory.get().getDeclension(LanguageProviderFactory.get().getLanguage(Locale.GERMAN));
            Map<NounForm,String> KONTO = ImmutableMap.<NounForm,String>builder()
                    .put(germanDecl.getNounForm(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE), "Konto")
                    .put(germanDecl.getNounForm(LanguageNumber.SINGULAR, LanguageCase.GENITIVE), "Kontos")
                    .put(germanDecl.getNounForm(LanguageNumber.SINGULAR, LanguageCase.DATIVE), "Konto")
                    .put(germanDecl.getNounForm(LanguageNumber.SINGULAR, LanguageCase.ACCUSATIVE), "Konto")
                    .put(germanDecl.getNounForm(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE), "Konten")  // Koni?  Kontos?
                    .put(germanDecl.getNounForm(LanguageNumber.PLURAL, LanguageCase.GENITIVE), "Konten")
                    .put(germanDecl.getNounForm(LanguageNumber.PLURAL, LanguageCase.DATIVE), "Konten")
                    .put(germanDecl.getNounForm(LanguageNumber.PLURAL, LanguageCase.ACCUSATIVE), "Konten")
                    .build();
            Map<NounForm,String> KUNDE = ImmutableMap.<NounForm,String>builder()
                    .put(germanDecl.getNounForm(LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE), "Kunde")
                    .put(germanDecl.getNounForm(LanguageNumber.SINGULAR, LanguageCase.GENITIVE), "Kunden")
                    .put(germanDecl.getNounForm(LanguageNumber.SINGULAR, LanguageCase.DATIVE), "Kunden")
                    .put(germanDecl.getNounForm(LanguageNumber.SINGULAR, LanguageCase.ACCUSATIVE), "Kunden")
                    .put(germanDecl.getNounForm(LanguageNumber.PLURAL, LanguageCase.NOMINATIVE), "Kunde")
                    .put(germanDecl.getNounForm(LanguageNumber.PLURAL, LanguageCase.GENITIVE), "Kunde")
                    .put(germanDecl.getNounForm(LanguageNumber.PLURAL, LanguageCase.DATIVE), "Kunde")
                    .put(germanDecl.getNounForm(LanguageNumber.PLURAL, LanguageCase.ACCUSATIVE), "Kunde")
                    .build();

            Noun konto = gl.getLabelSet().getDictionary().getNoun("account", false).clone(LanguageGender.NEUTER, LanguageStartsWith.CONSONANT, KONTO);

            MockRenamingProvider newProvider = new MockRenamingProvider(konto);
            RenamingProviderFactory.get().setProvider(newProvider);
            assertEquals("Klicken Sie hier, um jetzt ein neues Konto zu erstellen.", gl.getLabel("Sample", "click_here_to_create_new_account", "Klicken Sie hier"));

            Noun kunde = gl.getLabelSet().getDictionary().getNoun("account", false).clone(LanguageGender.MASCULINE, LanguageStartsWith.CONSONANT, KUNDE);
            newProvider = new MockRenamingProvider(kunde);
            RenamingProviderFactory.get().setProvider(newProvider);
            assertEquals("Klicken Sie hier, um jetzt einen neuen Kunden zu erstellen.", gl.getLabel("Sample", "click_here_to_create_new_account", "Klicken Sie hier"));

            newProvider.setUseRenamedNouns(false);
            assertEquals("Klicken Sie hier, um jetzt einen neuen Account zu erstellen.", gl.getLabel("Sample", "click_here_to_create_new_account", "Klicken Sie hier"));
        } finally {
            RenamingProviderFactory.get().setProvider(curProvider);
        }
    }



    //*************** POSTIVE TESTCASES **********************

    public void testGetLocaleDateTimeFormat() {
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        //Would like to test convertTo4DigitYear function, but it was private.
        String temp = ((SimpleDateFormat)DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
            chineseLocale)).toPattern();
        Assert.assertTrue(temp.indexOf("yyyy") == -1);

        DateFormat dt = BaseLocalizer.getLocaleDateTimeFormat(chineseLocale, tz);
        String pattern2 = ((SimpleDateFormat)dt).toPattern();

        //Verify that convertToDigitsYear works
        Assert.assertTrue(pattern2.indexOf("yyyy") != -1);
    }

    public void testDoParseDate() throws ParseException {
        DateFormat df = DateFormat.getDateTimeInstance();
        Date today = new Date();

        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy, h:mm:ss a");
        String dateNewFormat = formatter.format(today);

        Date date = BaseLocalizer.doParseDate(dateNewFormat, df);
        Assert.assertTrue(date.toString().equals(today.toString()));
    }

    //*************** NEGATIVE TESTCASES **********************

    public void testDoParseDate_ExceptionNull() {
        try {
            BaseLocalizer.doParseDate(null, null);
        }
        catch (ParseException expected) {}
    }

    public void testDoParseDate_Exception_WrongFormat() {
        DateFormat df = DateFormat.getDateTimeInstance();
        try {
            BaseLocalizer.doParseDate("8 30, 1988 11:25:59 AM", df);
            fail("This should not happen!");
        }
        catch (ParseException expected) {}
    }

    public void testDoParseDate_ExceptionExtraChar() {
        DateFormat df = DateFormat.getDateTimeInstance();
        Date today = new Date();

        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy h:mm:ss a");
        String dateNewFormat = formatter.format(today) + " ";

        try {
            BaseLocalizer.doParseDate(dateNewFormat, df);
            fail("This should not happen!");
        }
        catch (ParseException expected) {}
    }

    public void testDoParseDate_Exception_InvalidDate() {
        DateFormat df = DateFormat.getDateTimeInstance();

        try {
            BaseLocalizer.doParseDate("Aug 30, 19881 11:25:59 AM", df);
            fail("This should not happen!");
        }
        catch (ParseException expected) {}
    }

    public void testDoParseDate_Exception_DateOutOfRange1() {
        DateFormat df = DateFormat.getDateTimeInstance();
        try {
            BaseLocalizer.doParseDate("Aug 30, 4001 11:25:59 AM", df);
            fail("This should not happen!");
        }
        catch (ParseException expected) {}
    }

    public void testDoParseDate_Exception_DateOutOfRange2() {
        DateFormat df = DateFormat.getDateTimeInstance();
        try {
            BaseLocalizer.doParseDate("Aug 30, 1699 11:25:59 AM", df);
            fail("This should not happen!");
        }
        catch (ParseException expected) {}
    }


    public void testallowOtherGrammaticalForms() {
        URL base = GrammaticalLabelFileTest.class.getResource("/sample/labels.xml");
        GrammaticalLabelSetLoader baseLoader = new GrammaticalLabelSetLoader(base, "sample", null);
        URL labels = GrammaticalLabelFileTest.class.getResource("/labels/labels.xml");
        GrammaticalLabelSetLoader labelsLoader = new GrammaticalLabelSetLoader(labels, "test1a", baseLoader);
        URL overrides = GrammaticalLabelFileTest.class.getResource("/override/override.xml");
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(overrides, "test2a", labelsLoader);

        HumanLanguage ENGLISH = LanguageProviderFactory.get().getLanguage(Locale.US);
        HumanLanguage GERMAN = LanguageProviderFactory.get().getLanguage(Locale.GERMAN);
        GrammaticalLabelSetFallbackImpl set = (GrammaticalLabelSetFallbackImpl) loader.getSet(ENGLISH);
        assertFalse(set.allowOtherGrammaticalForms());
        set = (GrammaticalLabelSetFallbackImpl) loader.getSet(GERMAN);
        assertTrue(set.allowOtherGrammaticalForms());

        // Layer again?
        loader = new GrammaticalLabelSetLoader(overrides, "test2a", loader);
        set = (GrammaticalLabelSetFallbackImpl) loader.getSet(GERMAN);
        assertTrue(set.allowOtherGrammaticalForms());
    }

    public void testReloading() {
        GrammaticalLocalizerFactory factory = (GrammaticalLocalizerFactory) LocalizerFactory.get();
        factory.resetLabels();
        factory.initEnglishLabelProvider();
        factory.resetLabels();
        factory.initLabelProvider();
    }
}
