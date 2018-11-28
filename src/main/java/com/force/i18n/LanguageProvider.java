/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableList;

/**
 * Interface for the provider of the "set" of HumanLanguage
 *
 * @author stamm
 */
public interface LanguageProvider {
    /**
     * @return the set of all languages supported by this provider
     */
    List<? extends HumanLanguage> getAll();

    /**
     * @return the language that is the "base" for all labels.  This assumes
     * that the entire application is localized into a single language, and then
     * translated into the others.
     * <p>
     * If this language isn't "English", some things might not work.
     * <p>
     * Note: This doesn't apply to "Renameable" things
     */
    HumanLanguage getBaseLanguage();

    /**
     * @return the User Language associated directly with a locale (including country and variant)
     */
    HumanLanguage getLanguage(Locale loc);

    /**
     * @return the User Language associated directly with a locale (including country and variant)
     */
    HumanLanguage getLanguage(String isoCode);

    /**
     * @return <tt>true</tt> if the given locale is a support user language locale
     */
    boolean isSupportedLanguageLocale(Locale loc);

    /**
     * @return the closest HumanLanguage to the given locale string, taking into account various factors
     */
    HumanLanguage getLanguageForLocale(String localeString);

    /**
     * @return the closest HumanLanguage to the given locale, taking into account various factors
     */
    HumanLanguage getLanguageForLocale(Locale loc);

    /**
     * @return a map with an optimized implementation for the given map
     */
    <L extends HumanLanguage, T> Map<L, T> getNewMap();

    /**
     * Helper class for implementing LanguageProvider
     */
    public static class Helper implements LanguageProvider {
        // STATIC INITIALIZERS FOR MAGIC MAPS
        private final HumanLanguage base;
        private final List<? extends HumanLanguage> languages;
        private final Map<Locale, HumanLanguage> langByLoc;
        private final Map<String, HumanLanguage> langByString;
        private final ConcurrentMap<Locale, HumanLanguage> langByFuzzyLocale = new ConcurrentHashMap<>(128, .75f, 2);

        public Helper(HumanLanguage base, List<? extends HumanLanguage> languages) {
            if (base == null) throw new NullPointerException();
            this.base = base;
            this.languages = ImmutableList.copyOf(languages);

            Map<Locale, HumanLanguage> byLocale = new HashMap<Locale, HumanLanguage>(128);
            Map<String, HumanLanguage> byString = new HashMap<String, HumanLanguage>(128);
            for (HumanLanguage language : languages) {
                byLocale.put(language.getLocale(), language);
                byString.put(language.getLocale().toString(), language);
            }
            langByLoc = Collections.unmodifiableMap(byLocale);
            langByString = Collections.unmodifiableMap(byString);

            langByFuzzyLocale.putAll(langByLoc);
            // Add in aliases to magically help with fuzzy matches.
            for (HumanLanguage language : languages) {
                if (language.getOverrideLanguage() != null) {
                    // Handle any language overrides: note that this doesn't do anything for hebrew (since the locale gets mapped to "iw")
                    langByFuzzyLocale.put(new Locale(language.getOverrideLanguage()), language);
                }
            }
        }

        @Override
        public List<? extends HumanLanguage> getAll() {
            return languages;
        }

        @Override
        public HumanLanguage getBaseLanguage() {
            return base;
        }

        @Override
        public HumanLanguage getLanguage(Locale loc) {
            return langByLoc.get(loc);
        }

        @Override
        public HumanLanguage getLanguage(String isoCode) {
            return langByString.get(isoCode);
        }

        @Override
        public boolean isSupportedLanguageLocale(Locale loc) {
            return langByLoc.containsKey(loc);
        }

        @Override
        public HumanLanguage getLanguageForLocale(String localeString) {
            Locale loc = LocaleUtils.get().getLocaleByIsoCode(localeString);
            return getLanguageForLocale(loc);
        }

        @Override
        public HumanLanguage getLanguageForLocale(Locale loc) {
            if (loc == null) return null;

            HumanLanguage result = langByFuzzyLocale.get(loc);
            if (result != null) return result;

            if (loc.getVariant() != null) {
                Locale newLoc = new Locale(loc.getLanguage(), loc.getCountry());
                result = getLanguage(newLoc);
            }
            if (result == null) {
                Locale newLoc = new Locale(loc.getLanguage());
                result = getLanguage(newLoc);
                if (result == null) {
                    // See if there's another one that's close
                    for (HumanLanguage testLang : languages) {
                        if (testLang.getLocale().getLanguage().equals(loc.getLanguage())) {
                            result = testLang;
                            break;
                        }
                    }
                }
            }
            // Use base as the default so we don't return null for real things
            if (result == null) {
                result = base;
            }
            langByFuzzyLocale.put(loc, result);
            return result;
        }


        @Override
        @SuppressWarnings({"rawtypes", "unchecked"}) // Fake enum checks
        public <L extends HumanLanguage, T> Map<L, T> getNewMap() {
            if (base instanceof Enum) {
                return new EnumMap(base.getClass());
            } else {
                return new HashMap<L, T>();
            }

        }

    }
}
