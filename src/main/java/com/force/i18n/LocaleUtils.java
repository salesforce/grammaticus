/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * A collection of utilities for dealing with Locales.
 * @author stamm
 */
public enum LocaleUtils {
    INSTANCE;

    public static LocaleUtils get() { return INSTANCE; }

    // TODO: The number of locales in the system is rather small, but we should probably use a ConcurrentLruMap just in case.
    private static final ConcurrentMap<String,Locale> uniqueLocaleMap = new ConcurrentHashMap<String,Locale>(64, .75f, 2);

    /**
     * @return a locale for language-only ("en") or language/country ("en_UK")
     * iso codes
     * @param isoCode the isoCode to search
     */
    public Locale getLocaleByIsoCode(String isoCode) {
        if (isoCode == null) return null;
        Locale oldValue = uniqueLocaleMap.get(isoCode);
        if (oldValue != null) return oldValue;
        Locale newValue=null;
        if (isoCode.length() == 2) {
            newValue = new Locale.Builder().setLanguage(isoCode).build();
        } else if (isoCode.length() == 5) {
            String countryIsoCode = isoCode.substring(3, 5);
            String langIsoCode = isoCode.substring(0, 2);
            newValue = new Locale.Builder().setLanguage(langIsoCode).setRegion(countryIsoCode).build();
        } else {
            List<String> split = Lists.newArrayList(Splitter.on('_').split(isoCode));
            String language = split.get(0);
            String country = split.size() > 1 ? split.get(1) : "";
            String variant = split.size() > 2 ? split.get(2) : "";
            newValue = new Locale.Builder().setLanguage(language).setRegion(country).setVariant(variant).build();
        }
        if (newValue != null) uniqueLocaleMap.put(isoCode, newValue);
        return newValue;
    }

    public Locale getLocaleFromDbString(String value) {
        // Handle special weirdness
        if (value == null || "null".equals(value)) return null;
        return getLocaleByIsoCode(value);
    }

    /**
     * Parses an "Accept-Language" header value as defined in
     * <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-accept-language">RFC 9110</a> and returns the first
     * value.
     *
     * <p>HTTP and Java handle locales differently. For example, HTTP might use {@code "de-de,de;q=0.8"}, while Java expects
     * "de_DE". This method addresses such discrepancies.
     *
     * @param str
     *            The HTTP language input, which should follow the "language-range" format of the "Accept-Language"
     *            header as defined in <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-accept-language">RFC
     *            9110</a>, except for the wildcard '*'. If the wildcard '*' is provided, this method will return
     *            {@code null}.
     * @return The Java locale derived from the HTTP language input. If {@code str} contains multiple languages, only
     *         the first value is considered, and the rest are ignored. Returns {@code null} if an invalid {@code str}
     *         is provided.
     */
    public Locale getLocaleFromHttpInput(String str) {
        if (str == null || "*".equals(str)) return null; // Invalid

        try {
            final List<LanguageRange> ranges = Locale.LanguageRange.parse(str);
            if (ranges != null && !ranges.isEmpty()) {
                return Locale.forLanguageTag(ranges.get(0).getRange());
            }
        } catch (IllegalArgumentException ignore) {
            // do nothing. let below to return null
        }
        return null;
    }
}
