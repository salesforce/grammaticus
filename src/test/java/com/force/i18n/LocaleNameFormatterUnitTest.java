/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.Collections;
import java.util.Locale;

import junit.framework.TestCase;

import com.force.i18n.grammar.*;

/**
 * Unit tests for {@link LocaleNameFormatter}
 * @author jared.pearson
 */
public class LocaleNameFormatterUnitTest extends TestCase {
    private static RenamingProvider RENAMING_PROVIDER_ALL_TRUE = new MockRenamingProvider(true, true);
    private static LocaleAttributesUtil EMPTY_LOCALE_CONFIG = new SimpleLocaleAttributesUtil(Collections.<LocaleInfo>emptyList());
    private RenamingProvider originalRenamingProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        originalRenamingProvider = RenamingProviderFactory.get().getProvider();
        RenamingProviderFactory.get().setProvider(RENAMING_PROVIDER_ALL_TRUE);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        RenamingProviderFactory.get().setProvider(originalRenamingProvider);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String)} is formatting correctly
     */
    public void testFormatFirstLastName() throws Exception {
        final Locale locale = Locale.US;
        final LocaleNameFormatter formatter = new LocaleNameFormatter(EMPTY_LOCALE_CONFIG);
        final String actualFormattedName = formatter.format(locale, "Matthew", "Murdock");
        assertEquals("Expected the first name to be first followed by the last name", "Matthew Murdock", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String)} is formatting correctly
     */
    public void testFormatSalutationFirstLastName() throws Exception {
        final Locale locale = Locale.US;
        final LocaleNameFormatter formatter = new LocaleNameFormatter(EMPTY_LOCALE_CONFIG);
        final String actualFormattedName = formatter.format(locale, "Sir", "Matthew", "Murdock");
        assertEquals("Expected the salutation then first name followed by the last name", "Sir Matthew Murdock", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, boolean)} is formatting correctly when the
     * casual is enabled.
     */
    public void testFormatSalutationFirstLastNameWithCasual() throws Exception {
        final Locale locale = Locale.US;
        final LocaleNameFormatter formatter = new LocaleNameFormatter(EMPTY_LOCALE_CONFIG);
        final String actualFormattedName = formatter.format(locale, "Sir", "Matthew", "Murdock", true);
        assertEquals("Expected only the first name since \"casual\" is set", "Matthew", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, boolean)} is formatting correctly when the
     * casual is not enabled.
     */
    public void testFormatSalutationFirstLastNameWithNotCasual() throws Exception {
        final Locale locale = Locale.US;
        final LocaleNameFormatter formatter = new LocaleNameFormatter(EMPTY_LOCALE_CONFIG);
        final String actualFormattedName = formatter.format(locale, "Sir", "Matthew", "Murdock", false);
        assertEquals("Expected only the first name since \"casual\" is false", "Sir Matthew Murdock", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, String, String, String)} is formatting correctly
     */
    public void testFormatSalutationFirstLastMiddleInformalNameSuffix() throws Exception {
        final Locale locale = Locale.US;
        final LocaleNameFormatter formatter = new LocaleNameFormatter(EMPTY_LOCALE_CONFIG);
        final String salutation = "Sir";
        final String firstName = "Matthew";
        final String lastName = "Murdock";
        final String middleName = "Middle";
        final String informalName = "Matt";
        final String suffix = "Jr";
        final String actualFormattedName = formatter.format(locale, salutation, firstName, lastName, middleName, informalName, suffix);
        assertEquals("Expected salutation, firstName, middleName, lastName, and suffix. (The informal name is not used)", "Sir Matthew Middle Murdock Jr", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, String, String, String, boolean)} is formatting correctly when the
     * casual is enabled.
     */
    public void testFormatSalutationFirstLastMiddleInformalNameSuffixWithCasual() throws Exception {
        final Locale locale = Locale.US;
        final LocaleNameFormatter formatter = new LocaleNameFormatter(EMPTY_LOCALE_CONFIG);
        final String salutation = "Sir";
        final String firstName = "Matthew";
        final String lastName = "Murdock";
        final String middleName = "Middle";
        final String informalName = "Matt";
        final String suffix = "Jr";
        final boolean casual = true;
        final String actualFormattedName = formatter.format(locale, salutation, firstName, lastName, middleName, informalName, suffix, casual);
        assertEquals("Expected only the informal name to be returned since casual is enabled", "Matt", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, String, String, String, boolean)} is formatting correctly when the
     * casual is not enabled.
     */
    public void testFormatSalutationFirstLastMiddleInformalNameSuffixWithNotCasual() throws Exception {
        final Locale locale = Locale.US;
        final LocaleNameFormatter formatter = new LocaleNameFormatter(EMPTY_LOCALE_CONFIG);
        final String salutation = "Sir";
        final String firstName = "Matthew";
        final String lastName = "Murdock";
        final String middleName = "Middle";
        final String informalName = "Matt";
        final String suffix = "Jr";
        final boolean casual = false;
        final String actualFormattedName = formatter.format(locale, salutation, firstName, lastName, middleName, informalName, suffix, casual);
        assertEquals("Expected salutation, firstName, middleName, lastName, and suffix since casual is not enabled", "Sir Matthew Middle Murdock Jr", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, String, String, String, boolean)} is formatting correctly when the
     * middle name is configured to be off.
     */
    public void testFormatSalutationFirstLastMiddleInformalNameSuffixWithoutMiddleName() throws Exception {
        final Locale locale = Locale.US;

        // configure middle name disabled
        final RenamingProvider renamingProvider = new MockRenamingProvider(false, true);
        RenamingProviderFactory.get().setProvider(renamingProvider);

        final LocaleNameFormatter formatter = new LocaleNameFormatter(EMPTY_LOCALE_CONFIG);
        final String salutation = "Sir";
        final String firstName = "Matthew";
        final String lastName = "Murdock";
        final String middleName = "Middle";
        final String informalName = "Matt";
        final String suffix = "Jr";
        final boolean casual = false;
        final String actualFormattedName = formatter.format(locale, salutation, firstName, lastName, middleName, informalName, suffix, casual);
        assertEquals("Expected salutation, firstName, lastName, and suffix with no middle name since it has been disabled", "Sir Matthew Murdock Jr", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, String, String, String, boolean)} is formatting correctly when the
     * suffix is configured to be off.
     */
    public void testFormatSalutationFirstLastMiddleInformalNameSuffixWithoutSuffix() throws Exception {
        final Locale locale = Locale.US;

        // configure suffix to be disabled
        final RenamingProvider renamingProvider = new MockRenamingProvider(true, false);
        RenamingProviderFactory.get().setProvider(renamingProvider);

        final LocaleNameFormatter formatter = new LocaleNameFormatter(EMPTY_LOCALE_CONFIG);
        final String salutation = "Sir";
        final String firstName = "Matthew";
        final String lastName = "Murdock";
        final String middleName = "Middle";
        final String informalName = "Matt";
        final String suffix = "Jr";
        final boolean casual = false;
        final String actualFormattedName = formatter.format(locale, salutation, firstName, lastName, middleName, informalName, suffix, casual);
        assertEquals("Expected salutation, firstName, middleName, and lastName with no suffix since it has been disabled", "Sir Matthew Middle Murdock", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, String, String, String, boolean)} is formatting correctly when the
     * locale is set to eastern name order.
     */
    public void testFormatSalutationFirstLastMiddleInformalNameSuffixWithEasternNameOrder() throws Exception {
        final Locale locale = Locale.US;

        // setup the English locale with easter name ordering
        final LocaleAttributesUtil localeAttrUtil = new SimpleLocaleAttributesUtil(new LocaleInfoImpl(locale, true, true));

        final LocaleNameFormatter formatter = new LocaleNameFormatter(localeAttrUtil);
        final String salutation = "Sir";
        final String firstName = "Matthew";
        final String lastName = "Murdock";
        final String middleName = "Middle";
        final String informalName = "Matt";
        final String suffix = "Jr";
        final boolean casual = false;
        final String actualFormattedName = formatter.format(locale, salutation, firstName, lastName, middleName, informalName, suffix, casual);
        assertEquals("Expected lastName, firstName, middleName, and suffix", "Murdock Matthew Middle Jr", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, String, String, String, boolean)} is formatting correctly when the
     * locale is formal but casual is enabled. The expectation is that the full name will be displayed, since the locale only allows
     * for formal names.
     */
    public void testFormatSalutationFirstLastMiddleInformalNameSuffixWithFormalLocaleAndCasual() throws Exception {
        final Locale locale = Locale.US;

        // setup the English locale where formal is enabled
        final LocaleAttributesUtil localeAttrUtil = new SimpleLocaleAttributesUtil(new LocaleInfoImpl(locale, false, true));

        final LocaleNameFormatter formatter = new LocaleNameFormatter(localeAttrUtil);
        final String salutation = "Sir";
        final String firstName = "Matthew";
        final String lastName = "Murdock";
        final String middleName = "Middle";
        final String informalName = "Matt";
        final String suffix = "Jr";
        final boolean casual = true; // set to casual
        final String actualFormattedName = formatter.format(locale, salutation, firstName, lastName, middleName, informalName, suffix, casual);
        assertEquals("Expected salutation, firstName, middleName, lastName, and suffix", "Sir Matthew Middle Murdock Jr", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, String, String, String, boolean)} is formatting correctly when the
     * locale is not formal but casual is enabled. The expectation is that informal name is to be used.
     */
    public void testFormatSalutationFirstLastMiddleInformalNameSuffixWithNotFormalLocaleAndCasual() throws Exception {
        final Locale locale = Locale.US;

        // setup the English locale where formal is not enabled
        final SimpleLocaleAttributesUtil localeAttrUtil = new SimpleLocaleAttributesUtil(new LocaleInfoImpl(locale, false, false));

        final LocaleNameFormatter formatter = new LocaleNameFormatter(localeAttrUtil);
        final String salutation = "Sir";
        final String firstName = "Matthew";
        final String lastName = "Murdock";
        final String middleName = "Middle";
        final String informalName = "Matt";
        final String suffix = "Jr";
        final boolean casual = true; // set to casual
        final String actualFormattedName = formatter.format(locale, salutation, firstName, lastName, middleName, informalName, suffix, casual);
        assertEquals("Expected the informal name because the locale is not forcing formal naming", "Matt", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, String, String, String, boolean)} is returns an empty string when parts of the
     * name are whitespace.
     */
    public void testFormatWhenAllWhitespace() throws Exception {
        final Locale locale = Locale.US;

        // setup the English locale where formal is not enabled
        final SimpleLocaleAttributesUtil localeAttrUtil = new SimpleLocaleAttributesUtil(new LocaleInfoImpl(locale, false, false));

        final LocaleNameFormatter formatter = new LocaleNameFormatter(localeAttrUtil);
        final String salutation = " ";
        final String firstName = " ";
        final String lastName = " ";
        final String middleName = " ";
        final String informalName = " ";
        final String suffix = " ";
        final boolean casual = false;
        final String actualFormattedName = formatter.format(locale, salutation, firstName, lastName, middleName, informalName, suffix, casual);
        assertEquals("Expected the name to be an empty string when only whitespace is specified", "", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, String, String, String, boolean)} is returns an empty string when parts of the
     * name are whitespace when the locale is using eastern name ordering.
     */
    public void testFormatWhenAllWhitespaceWithEasternNameOrder() throws Exception {
        final Locale locale = Locale.US;

        // setup the English locale with easter name ordering
        final SimpleLocaleAttributesUtil localeAttrUtil = new SimpleLocaleAttributesUtil(new LocaleInfoImpl(locale, true, false));

        final LocaleNameFormatter formatter = new LocaleNameFormatter(localeAttrUtil);
        final String salutation = " ";
        final String firstName = " ";
        final String lastName = " ";
        final String middleName = " ";
        final String informalName = " ";
        final String suffix = " ";
        final boolean casual = false;
        final String actualFormattedName = formatter.format(locale, salutation, firstName, lastName, middleName, informalName, suffix, casual);
        assertEquals("Expected the name to be an empty string when only whitespace is specified", "", actualFormattedName);
    }

    /**
     * Test that {@link LocaleNameFormatter#format(Locale, String, String, String, String, String, String, boolean)} is returns an empty string when parts of the
     * name are whitespace.
     */
    public void testFormatWhenAllWhitespaceWithCasual() throws Exception {
        final Locale locale = Locale.US;

        // setup the English locale where formal is not enabled
        final SimpleLocaleAttributesUtil localeAttrUtil = new SimpleLocaleAttributesUtil(new LocaleInfoImpl(locale, false, false));

        final LocaleNameFormatter formatter = new LocaleNameFormatter(localeAttrUtil);
        final String salutation = " ";
        final String firstName = " ";
        final String lastName = " ";
        final String middleName = " ";
        final String informalName = " ";
        final String suffix = " ";
        final boolean casual = true; //setting casual to true
        final String actualFormattedName = formatter.format(locale, salutation, firstName, lastName, middleName, informalName, suffix, casual);
        assertEquals("Expected the name to be an empty string when only whitespace is specified", "", actualFormattedName);
    }

    /**
     * Simple RenamingProvider that allows for the displayMiddleName and displaySuffix properties to be
     * set. All other methods throw {@link UnsupportedOperationException} when invoked.
     * @author jared.pearson
     */
    private static class MockRenamingProvider implements RenamingProvider {
        private final boolean displayMiddleName;
        private final boolean displaySuffix;

        public MockRenamingProvider(boolean displayMiddleName, boolean displaySuffix) {
            this.displayMiddleName = displayMiddleName;
            this.displaySuffix = displaySuffix;
        }

        @Override
        public boolean displayMiddleNameInCalculatedPersonName() {
            return this.displayMiddleName;
        }

        @Override
        public boolean displaySuffixInCalculatedPersonName() {
            return this.displaySuffix;
        }

        @Override
        public Noun getRenamedNoun(HumanLanguage language, String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Noun getPackagedNoun(HumanLanguage language, String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Noun getNoun(HumanLanguage language, Renameable key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRenamed(HumanLanguage language, String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCustomKey(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean useRenamedNouns() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean supportOldGrammarEngine() {
            throw new UnsupportedOperationException();
        }
    }
}
