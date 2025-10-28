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

package com.force.i18n.grammar.impl;

import static com.force.i18n.LanguageConstants.*;

import java.util.HashMap;
import java.util.Map;

import com.force.i18n.HumanLanguage;
import com.force.i18n.I18nJavaUtil;
import com.force.i18n.LanguageProviderFactory;
import com.force.i18n.grammar.LanguageDeclension;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
/**
 * For a given language, provide the declension associated with it.
 *
 * This should be the only "public" member in this package
 *
 * @author stamm
 */
public enum LanguageDeclensionFactory {
    INSTANCE;
    public static LanguageDeclensionFactory get() { return INSTANCE; }

    private final Map<HumanLanguage, LanguageDeclension> declensions;
    private final LanguageDeclension defaultDeclension;  // English is asked for more than everything else, make it durn quick
    private final HumanLanguage baseLanguage;

    @SuppressWarnings({"rawtypes","unchecked"})  // Don't require humanLanguage is an enum
    private LanguageDeclensionFactory() {
        baseLanguage = LanguageProviderFactory.get().getProvider().getBaseLanguage();
        Map<HumanLanguage, LanguageDeclension> map;
        if (baseLanguage instanceof Enum) {
            // There's no good way to do this
            Map eMap = Maps.newEnumMap(baseLanguage.getClass().asSubclass(Enum.class));
            map = eMap;
        } else {
            map = new HashMap<>();
        }
        for (HumanLanguage language : LanguageProviderFactory.get().getProvider().getAll()) {
            LanguageDeclension declension = createDeclension(language, map);
            map.computeIfAbsent(language, l -> declension);
        }
        declensions = ImmutableMap.copyOf(map);
        defaultDeclension = declensions.get(baseLanguage);
    }

    /**
     * @return For the given language, return the associated declension
     * @param language the given language
     */
    public LanguageDeclension getDeclension(HumanLanguage language) {
        if (language == baseLanguage) return defaultDeclension;
        return declensions.get(language);
    }

    /**
     * Convenient method to determine if the desclension is forwarding proxy.
     *
     * @param declension the declension to test
     * @return {@code true} if the given declension is forwarding proxy, {@code false} otherwise.
     */
    public boolean isForwardingProxy(LanguageDeclension declension) {
        return ForwardingLanguageDeclension.isForwardingProxy(declension);
    }

    /**
     * Convenient method to determine if the desclension is forwarding proxy.
     *
     * @param language the language to test
     * @return {@code true} if the given declension is forwarding proxy, {@code false} otherwise.
     */
    public boolean isForwardingProxy(HumanLanguage language) {
        return isForwardingProxy(getDeclension(language));
    }

    // set "false" to switch back to the old behavior. giving packge-only scope for test access.
    static final boolean USE_PROXY = true;

    private LanguageDeclension createDeclension(HumanLanguage language, Map<HumanLanguage, LanguageDeclension> map) {
        LanguageDeclension declension = createDeclension(language);

        // skip returning a new declension object if the language can be safely re-mapped to its fallback.
        if (!language.isTranslatedLanguage() && USE_PROXY) {
            HumanLanguage fallback = language.getFallbackLanguage();
            LanguageDeclension fallbackDeclension = map.computeIfAbsent(fallback, l -> createDeclension(l, map));
            assert fallbackDeclension != null;

            // create forwarding proxy only when the fallback declension equals to the given language's.
            if (fallbackDeclension.getClass() == declension.getClass()) {
                // note that this impacts GrammaticalTerm.declension value for non-translated languages.
                //
                // for example, "en_AU" creates a proxy declension w/ en_GB as a delegate. although the
                // declesion.getLanguage() reports the right language -- "en_AU", terms in dictionary claims its
                // declension is "en_GB".
                // e.g.
                //    // assume "declension" is a LanguageDeclension object for "en_AU"
                //    assert declension.getLanguage() == "en_AU";
                //    assert declension.getNoun("account", false).getDeclension().getLanguage() == "en_GB";
                //
                // this is true even if GrammaticalLabelSetLoader.getUseTranslatedLanguage() returns "false". this is
                // because LangaugeDeclension.createNoun/Article/Adjective implementations pass itself as "this", which
                // is NOT a proxy object but proxy.delegate. (note that Proxy is not a superclass of "delegate")
                // See EnglishDeclension.createNoun for example.
                //
                // this should be okay because both en_AU and en_GB are served by the same declension class. also, when
                // GrammaticalLabelSetLoader.getUseTranslatedLanguage() is true, en_AU shares the all data from "en_GB".
                // See also: GrammaticalLabelSetLoader#compute
                return ForwardingLanguageDeclension.newInstance(language, fallbackDeclension);
            }
        }
        return declension;
    }

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
        case BURMESE:
            return new SimpleDeclension.SimpleDeclensionWithClassifiers(language);
        case VIETNAMESE:
            return new SimpleDeclension.VietnameseDeclension(language);
        case HMONG:
            return new SimpleDeclension.HmongDeclension(language);
        case THAI:
        case TAGALOG:
        case AFRIKAANS:
        case KHMER:
            return new SimpleDeclension(language);
        case KOREAN:
            return new KoreanDeclension(language);
        case INDONESIAN:
        case INDONESIAN_ISO:
        case MALAY:
        case MAORI:
        case SAMOAN:
            return new MalayoPolynesianDeclension(language);
        case HAWAIIAN:
            return new MalayoPolynesianDeclension.HawaiianDeclension(language);
        case HUNGARIAN:
            return new HungarianDeclension(language);
        case TURKISH:
            return new TurkicDeclension.TurkishDeclension(language);
        case KAZAKH:
            return new TurkicDeclension.KazakhDeclension(language);
        case FINNISH:
            return new FinnishDeclension(language);
        case ROMANIAN:
            return new RomanianDeclension(language);
        case UKRAINIAN:
            return new SlavicDeclension.UkrainianDeclension(language);
        case HEBREW:
        case HEBREW_ISO:
            return new HebrewDeclension(language);
        case ARABIC:
            return new ArabicDeclension(language);
        case AMHARIC:
            return new AmharicDeclension(language);
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
        case YIDDISH:
        case YIDDISH_ISO:
            return new GermanicDeclension.YiddishDeclension(language);
        case ARMENIAN:
            return new ArmenianDeclension(language);
        case CATALAN:
            return new CatalanDeclension(language);
        case HINDI:
        case URDU:
            return new HindiUrduDeclension(language);
        case BENGALI:
            return new BengaliDeclension(language);
        case SWAHILI:
            return new BantuDeclension.SwahiliDeclension(language);
        case ZULU:
            return new BantuDeclension.ZuluDeclension(language);
        case XHOSA:
            return new BantuDeclension.XhosaDeclension(language);
        case TAMIL:
            return new DravidianDeclension.TamilDeclension(language);
        case TELUGU:
            return new DravidianDeclension.TeluguDeclension(language);
        case KANNADA:
            return new DravidianDeclension.KannadaDeclension(language);
        case MALAYALAM:
            return new DravidianDeclension.MalayalamDeclension(language);
        case GUJARATI:
            return new IndoAryanDeclension.GujaratiDeclension(language);
        case MARATHI:
            return new IndoAryanDeclension.MarathiDeclension(language);
        case PUNJABI:
        case PUNJABI_WESTERN:
            return new IndoAryanDeclension.PunjabiDeclension(language);
        // Languages too complex to support *EVER*.  Included with ans
        case IRISH:    // Lenition
            return new UnsupportedLanguageDeclension.IrishDeclension(language);
        case WELSH:    // Lenition
            return new UnsupportedLanguageDeclension.CelticDeclension(language);
        case BASQUE:
            return new BasqueDeclension(language);
        case MALTESE:  // More complicated than arabic: complicated starts with, dual form, etc.
            return new UnsupportedLanguageDeclension.MalteseDeclension(language);
        case PERSIAN:  // Arabic vs Persian native words have different declensions
            return new UnsupportedLanguageDeclension.PersianDeclension(language);
        case HAITIAN_CREOLE:
            return new UnsupportedLanguageDeclension.HaitianCreoleDeclension(language);
        case GREENLANDIC:
            return new UnsupportedLanguageDeclension.GreenlandicDeclension(language);
        case CHUJ:
            return new SimpleDeclension(language);
        case KICHE:
            return new SimpleDeclension(language);
        case KAQCHIKEL:
            return new SimpleDeclension(language);
        default:
            // just fallthrouh to the default
        }

        if (FAIL_ON_MISSING) {
            throw new UnsupportedOperationException(
                    "Language " + language + " has no defined declension; the build breaker edited UserLanguage");
        } else {
            return new SimpleDeclension(language);
        }
    }

    private final boolean FAIL_ON_MISSING = "true".equals(I18nJavaUtil.getProperty("failOnMissingDeclension"));
}
