/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.List;
import java.util.Locale;
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
            newValue = new Locale(isoCode);
        } else if (isoCode.length() == 5) {
            String countryIsoCode = isoCode.substring(3, 5);
            String langIsoCode = isoCode.substring(0, 2);
            newValue = new Locale(langIsoCode, countryIsoCode);
        } else {
            List<String> split = Lists.newArrayList(Splitter.on('_').split(isoCode));
            String language = split.get(0);
            String country = split.size() > 1 ? split.get(1) : "";
            String variant = split.size() > 2 ? split.get(2) : "";
            newValue = new Locale(language, country, variant);
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
      * HTTP and Java do not use the same locale stuff
      * HTTP would say "de-de", but Java would want "de_DE".  This handles those kinds of
      * weirdness things  (like "de-de;q=0.8")
      * @param str the HTTP language input
      * @return the java locale from the http language input.  
      */
     public Locale getLocaleFromHttpInput(String str) {
         if ("*".equals(str)) return null;  // Invalid
         int semiIndex = str.indexOf(';');
         String locale = str;
         if (semiIndex > 0) {
             // We have a "quality" rating.  Ignore it.  It's useless
             locale = str.substring(0,semiIndex);
         }
         // OK, we should have "de" or "de-de";
         if (locale.length() == 2) {
             return new Locale(locale.toLowerCase());
         } else if (locale.length() == 5) {
             if (locale.charAt(2) != '-') return null;
             return new Locale(locale.substring(0,2).toLowerCase(), locale.substring(3,5).toUpperCase());
         } else {
             return null;
         }
     }
}
