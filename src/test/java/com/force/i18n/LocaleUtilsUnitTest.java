/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * Unit tests for {@link LocaleUtils}
 * @author jared.pearson
 */
public class LocaleUtilsUnitTest extends TestCase {

    /**
     * Verifies that getLocaleByIsoCode with a valid language code returns a valid locale
     */
    public void testGetLocaleByIsoCodeWithLanguage() throws Exception {
        final String isoCode = "en";
        final Locale actualLocale = LocaleUtils.get().getLocaleByIsoCode(isoCode);
        assertEquals("Expected getLocaleByIsoCode to return the English Locale", new Locale.Builder().setLanguage("en").build(), actualLocale);
    }

    /**
     * Verifies that getLocaleByIsoCode with valid language code and country code returns a valid locale
     */
    public void testGetLocaleByIsoCodeWithLanguageCountry() throws Exception {
        final String isoCode = "en_US";
        final Locale actualLocale = LocaleUtils.get().getLocaleByIsoCode(isoCode);
        assertEquals("Expected getLocaleByIsoCode to return the US English Locale", new Locale.Builder().setLanguage("en").setRegion("US").build(), actualLocale);
    }

    /**
     * Verifies that getLocaleByIsoCode with valid language code, country code and variant returns a valid locale
     */
    public void testGetLocaleByIsoCodeWithLanguageCountryVariant() throws Exception {
        final String isoCode = "ca_ES_PREEURO";
        final Locale actualLocale = LocaleUtils.get().getLocaleByIsoCode(isoCode);
        assertEquals("Expected getLocaleByIsoCode to return the Catalan Spain Locale with Euro variant",
        new Locale.Builder().setLanguage("ca").setRegion("ES").setVariant("PREEURO").build(), actualLocale);
    }

    public void testGetLocaleFromHttpStrig() {
        assertNull(LocaleUtils.get().getLocaleFromHttpInput("*"));
        assertNull(LocaleUtils.get().getLocaleFromHttpInput(""));
        assertNull(LocaleUtils.get().getLocaleFromHttpInput(null));

        assertEquals(Locale.ENGLISH, LocaleUtils.get().getLocaleFromHttpInput("en"));
        assertEquals(Locale.ENGLISH, LocaleUtils.get().getLocaleFromHttpInput("en;q=0.8"));
        assertEquals(Locale.US, LocaleUtils.get().getLocaleFromHttpInput("en-US,en;q=0.8"));
    }
}
