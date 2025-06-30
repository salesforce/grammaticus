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
