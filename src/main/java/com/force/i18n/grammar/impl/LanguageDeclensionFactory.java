/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.LanguageConstants.*;

import java.util.HashMap;
import java.util.Map;

import com.force.i18n.*;
import com.force.i18n.grammar.LanguageDeclension;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * For a given language, provide the declension associated with it.
 * <p>
 * This should be the only "public" member in this package
 *
 * @author stamm
 */
public enum LanguageDeclensionFactory {
    INSTANCE;

    public static LanguageDeclensionFactory get() {
        return INSTANCE;
    }

    private final Map<HumanLanguage, LanguageDeclension> declensions;
    private final LanguageDeclension defaultDeclension;  // English is asked for more than everything else, make it durn quick
    private final HumanLanguage baseLanguage;

    @SuppressWarnings({"rawtypes", "unchecked"})  // Don't require humanLanguage is an enum
    private LanguageDeclensionFactory() {
        baseLanguage = LanguageProviderFactory.get().getProvider().getBaseLanguage();
        Map<HumanLanguage, LanguageDeclension> map;
        if (baseLanguage instanceof Enum) {
            // There's no good way to do this
            Map eMap = Maps.newEnumMap(baseLanguage.getClass().asSubclass(Enum.class));
            map = (Map<HumanLanguage, LanguageDeclension>) eMap;
        } else {
            map = new HashMap<>();
        }
        for (HumanLanguage language : LanguageProviderFactory.get().getProvider().getAll()) {
            LanguageDeclension declension = createDeclension(language);
            assert declension.getLanguage() == language : "Programmer error, invalid declension";
            map.put(language, declension);
        }
        declensions = ImmutableMap.copyOf(map);
        defaultDeclension = declensions.get(baseLanguage);
    }

    /**
     * @param language the given language
     * @return For the given language, return the associated declension
     */
    public LanguageDeclension getDeclension(HumanLanguage language) {
        if (language == baseLanguage) return defaultDeclension;
        return declensions.get(language);
    }

    /**
     * @param language
     * @return
     */
    private LanguageDeclension createDeclension(HumanLanguage language) {
        switch (language.getLocale().getLanguage()) {
            case ENGLISH:
                return new EnglishDeclension(language);
            case ITALIAN:
                return new ItalianDeclension(language);
            case FRENCH:
                return new FrenchDeclension(language);
            case SPANISH:
                return new RomanceDeclension.SpanishDeclension(language);
            case PORTUGUESE:
                return new RomanceDeclension.PortugueseDeclension(language);
            case GERMAN:
                return new GermanicDeclension.GermanDeclension(language);
            case SWEDISH:
                return new GermanicDeclension.SwedishDeclension(language);
            case DUTCH:
                return new GermanicDeclension.DutchDeclension(language);
            case DANISH:
                return new GermanicDeclension.DanishDeclension(language);
            case CZECH:
                return new SlavicDeclension.CzechDeclension(language);
            case POLISH:
                return new SlavicDeclension.PolishDeclension(language);
            case RUSSIAN:
                return new SlavicDeclension.RussianDeclension(language);
            case CHINESE:
            case JAPANESE:
            case VIETNAMESE:
            case THAI:
            case KOREAN:
            case TAGALOG:
                return new SimpleDeclension(language);
            case INDONESIAN:
            case MALAY:
                return new IndonesianDeclension(language);
            case HUNGARIAN:
                return new HungarianDeclension(language);
            case TURKISH:
                return new TurkishDeclension(language);
            case FINNISH:
                return new FinnishDeclension(language);
            case ROMANIAN:
                return new RomanianDeclension(language);
            case UKRAINIAN:
                return new SlavicDeclension.UkrainianDeclension(language);
            case HEBREW:
                return new HebrewDeclension(language);
            case ARABIC:
                return new ArabicDeclension(language);
            case GREEK:
                return new GreekDeclension(language);
            case BULGARIAN:
            case MACEDONIAN:
                return new BulgarianDeclension(language);
            case SERBIAN_CYRILLIC:
            case SERBIAN_LATIN:
            case BOSNIAN:
            case CROATIAN:
            case MONTENEGRIN:
                return new SlavicDeclension.VariantSerboCroatianDeclension(language);
            case LITHUANIAN:
                return new BalticDeclension(language);
            case LATVIAN:
                return new BalticDeclension(language);
            case SLOVAK:
                return new SlavicDeclension.SlovakianDeclension(language);
            case SLOVENE:
                return new SlavicDeclension.SlovenianDeclension(language);
            case NORWEGIAN:
                return new GermanicDeclension.NorwegianDeclension(language);
            case GEORGIAN:
                return new SlavicDeclension.GeorgianDeclension(language);
            case ESPERANTO:
                return new EsperantoDeclension(language);
            case ICELANDIC:
                return new GermanicDeclension.IcelandicDeclension(language);
            case ESTONIAN:
                return new FinnishDeclension.EstonianDeclension(language);
            case ALBANIAN:
                return new AlbanianDeclension(language);
            case ROMANSH:
                return new FrenchDeclension.RomanshDeclension(language);
            case LUXEMBOURGISH:
                return new GermanicDeclension.LuxembourgishDeclension(language);
            case ARMENIAN:
                return new ArmenianDeclension(language);
            case HINDI:
            case URDU:
                return new HindiUrduDeclension(language);
            case BENGALI:
                return new BengaliDeclension(language);
            case TAMIL:
                return new TamilDeclension(language);
            // Languages too complex to support *EVER*.  Included with ans
            case IRISH:    // Lenition
                return new UnsupportedLanguageDeclension.IrishDeclension(language);
            case WELSH:    // Lenition
                return new UnsupportedLanguageDeclension.CelticDeclension(language);
            case BASQUE:   // Agglutintive in a way that can't really be supported
                return new UnsupportedLanguageDeclension.BasqueDeclension(language);
            case MALTESE:  // More complicated than arabic: complicated starts with, dual form, etc.
                return new UnsupportedLanguageDeclension.MalteseDeclension(language);
            case CATALAN:
                return new CatalanDeclension(language);
        }
        if (FAIL_ON_MISSING) {
            throw new UnsupportedOperationException("Language has no defined declension; the build breaker edited UserLanguage");
        } else {
            return new SimpleDeclension(language);
        }
    }

    private static final boolean FAIL_ON_MISSING = "true".equals(I18nJavaUtil.getProperty("failOnMissingDeclension"));
}
