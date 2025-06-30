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
