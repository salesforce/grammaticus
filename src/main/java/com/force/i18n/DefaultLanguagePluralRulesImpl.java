/*
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n;

import java.util.Set;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.guava.CaffeinatedGuava;
import com.google.common.cache.*;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.text.PluralRules.PluralType;

/**
 * Default implementation of LanguagePluralRules that uses ICU4J.
 *
 * @author stamm
 * @since 1.1
 */
class DefaultLanguagePluralRulesImpl implements LanguagePluralRules {
    private final HumanLanguage language;
    private final PluralRules cardinal;
    private final PluralRules ordinal;

    public DefaultLanguagePluralRulesImpl(HumanLanguage language) {
        this.language = language;
        this.cardinal = PluralRules.forLocale(language.getLocale(), PluralType.CARDINAL);
        this.ordinal = PluralRules.forLocale(language.getLocale(), PluralType.ORDINAL);
    }

    @Override
    public HumanLanguage getHumanLanguage() {
        return this.language;
    }

    private PluralRules getPluralRules(NumberType numberType) {
        return numberType == NumberType.ORDINAL ? this.ordinal : this.cardinal;
    }

    // Convert from ICU4j to Grammaticus category
    private PluralCategory fromString(String string) {
        switch (string) {
        case PluralRules.KEYWORD_ZERO: return PluralCategory.ZERO;
        case PluralRules.KEYWORD_ONE: return PluralCategory.ONE;
        case PluralRules.KEYWORD_TWO: return PluralCategory.TWO;
        case PluralRules.KEYWORD_FEW: return PluralCategory.FEW;
        case PluralRules.KEYWORD_MANY: return PluralCategory.MANY;
        case PluralRules.KEYWORD_OTHER: return PluralCategory.OTHER;
        default:
            return PluralCategory.OTHER;
        }
    }

    @Override
    public PluralCategory getPluralCategory(Number value, NumberType numberType) {
        return fromString(getPluralRules(numberType).select(value != null ? value.doubleValue() : 0));
    }

    @Override
    public Set<PluralCategory> getSupportedCategories(NumberType numberType) {
        return getPluralRules(numberType).getKeywords().stream().map(a -> fromString(a)).collect(Collectors.toSet());
    }

    static LanguagePluralRules forLanguage(HumanLanguage language) {
        return RULES_CACHE.getUnchecked(language);
    }

    // Have a default cache so we don't load it all the time for every language
    private static final LoadingCache<HumanLanguage, LanguagePluralRules> RULES_CACHE =
            CaffeinatedGuava.build(Caffeine.newBuilder(), CacheLoader.from(DefaultLanguagePluralRulesImpl::new));
}
