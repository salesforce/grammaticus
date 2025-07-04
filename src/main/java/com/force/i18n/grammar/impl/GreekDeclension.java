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

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.commons.text.TrieMatcher;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.google.common.collect.ImmutableMap;
/**
 * Greek is *not* germanic, but it's close enough for salesforce work.
 * TODO: This really should be a separate declension from germanic, because only greek has a starts with that's interesting.
 *
 * @author stamm
 */
class GreekDeclension extends GermanicDeclension {
    private static final Logger logger = Logger.getLogger(GreekDeclension.class.getName());

    private final Map<ArticleForm,String> indefiniteOverrides;
    private final Map<ArticleForm,String> definiteOverrides;

    public GreekDeclension(HumanLanguage language) {
    	super(language);
        // Setup the map for "plosive endings"
        definiteOverrides = ImmutableMap.<ArticleForm, String>of(
            getArticleForm(LanguageStartsWith.SPECIAL, LanguageGender.MASCULINE, LanguageNumber.SINGULAR, LanguageCase.ACCUSATIVE), "\u03c4\u03bf\u03bd", // τον
            getArticleForm(LanguageStartsWith.SPECIAL, LanguageGender.FEMININE, LanguageNumber.SINGULAR, LanguageCase.ACCUSATIVE), "\u03c4\u03b7\u03bd"); // την
        indefiniteOverrides = Collections.singletonMap(
                getArticleForm(LanguageStartsWith.SPECIAL, LanguageGender.MASCULINE, LanguageNumber.SINGULAR, LanguageCase.ACCUSATIVE), "\u03ad\u03bd\u03b1\u03bd");  // έναν
    }

    private final Map<LanguageCase, ImmutableMap<LanguageNumber, ImmutableMap<LanguageGender,String>>> DEFINITE_ARTICLE =
        ImmutableMap.of(
               LanguageCase.NOMINATIVE,
                        ImmutableMap.of(LanguageNumber.SINGULAR, ImmutableMap.of(
                                LanguageGender.NEUTER, "\u03c4\u03bf", // το
                                LanguageGender.FEMININE, "\u03b7",  // η
                                LanguageGender.MASCULINE, "\u03bf"  // ο
                                ), LanguageNumber.PLURAL, ImmutableMap.of(
                                LanguageGender.NEUTER, "\u03c4\u03b1",   // τα
                                LanguageGender.FEMININE, "\u03bf\u03b9",  // οι
                                LanguageGender.MASCULINE, "\u03bf\u03b9"  // οι
                                )),
                LanguageCase.ACCUSATIVE,
                        ImmutableMap.of(LanguageNumber.SINGULAR, ImmutableMap.of(
                                LanguageGender.NEUTER, "\u03c4\u03bf",  // το
                                LanguageGender.FEMININE, "\u03c4\u03b7", // τη(ν)
                                LanguageGender.MASCULINE, "\u03c4\u03bf"  // το(ν)
                                ), LanguageNumber.PLURAL, ImmutableMap.of(
                                LanguageGender.NEUTER, "\u03c4\u03b1",  // τα
                                LanguageGender.FEMININE, "\u03c4\u03b9\u03c2",  // τις
                                LanguageGender.MASCULINE, "\u03c4\u03bf\u03c5\u03c2"  // τους
                                )),
               LanguageCase.GENITIVE,
                        ImmutableMap.of(LanguageNumber.SINGULAR, ImmutableMap.of(
                                LanguageGender.NEUTER, "\u03c4\u03bf\u03c5",   // του
                                LanguageGender.FEMININE, "\u03c4\u03b7\u03c2",  // της
                                LanguageGender.MASCULINE, "\u03c4\u03bf\u03c5"  // του
                                ), LanguageNumber.PLURAL, ImmutableMap.of(
                                LanguageGender.NEUTER, "\u03c4\u03c9\u03bd",   // των
                                LanguageGender.FEMININE, "\u03c4\u03c9\u03bd",  // των
                                LanguageGender.MASCULINE, "\u03c4\u03c9\u03bd"  // των
                                ))
            );

    private static final Map<LanguageCase, ImmutableMap<LanguageGender, String>> INDEFINITE_ARTICLE =
        ImmutableMap.of(
               LanguageCase.NOMINATIVE,
                        ImmutableMap.of(
                                LanguageGender.NEUTER, "\u03ad\u03bd\u03b1",  // ένα
                                LanguageGender.FEMININE, "\u03bc\u03af\u03b1",  // μία
                                LanguageGender.MASCULINE, "\u03ad\u03bd\u03b1\u03c2"  // ένας
                                ),
                LanguageCase.ACCUSATIVE,
                        ImmutableMap.of(
                                LanguageGender.NEUTER, "\u03ad\u03bd\u03b1",  // ένα
                                LanguageGender.FEMININE, "\u03bc\u03af\u03b1",  // μία
                                LanguageGender.MASCULINE, "\u03ad\u03bd\u03b1"  // ένα(ν)
                                ),
               LanguageCase.GENITIVE,
                        ImmutableMap.of(
                                LanguageGender.NEUTER, "\u03b5\u03bd\u03cc\u03c2",  // ενός
                                LanguageGender.FEMININE, "\u03bc\u03b9\u03b1\u03c2",  // μιας
                                LanguageGender.MASCULINE, "\u03b5\u03bd\u03cc\u03c2"  // ενός
                                )
                               );

    public static class GreekNoun extends GermanicNoun {
        private static final long serialVersionUID = 1L;

        public GreekNoun(GermanicDeclension declension, String name, String pluralAlias, NounType type, String entityName,
                LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
            super(declension, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
        }

        @Override
        public void setString(String value, NounForm form) {
            super.setString(intern(value), form);
            if (form == getDeclension().getAllNounForms().get(0)) {
                setStartsWith(startsWithGreekPlosive(value) ? LanguageStartsWith.SPECIAL : LanguageStartsWith.CONSONANT);
            }
        }
    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new GreekNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }

    public static boolean startsWithGreekPlosive(String value) {
        return PLOSIVE_MATCHER.begins(value);
    }

    @Override
    protected EnumSet<LanguageArticle> getRequiredAdjectiveArticles() {
        return EnumSet.of(LanguageArticle.ZERO);  // Greek adjectives are not inflected for definitiveness
    }

    @Override
    protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        switch (articleType) {
        case DEFINITE:
            String override = definiteOverrides.get(form);
            if (override != null) return override;
            ImmutableMap<LanguageNumber, ImmutableMap<LanguageGender,String>> byCase = DEFINITE_ARTICLE.get(form.getCase());
            if (byCase == null) {
                logger.fine("Trying to retrieve an illegal definite article form in greek");
                return "";
            }
            return byCase.get(form.getNumber()).get(form.getGender());
        case INDEFINITE:
            if (form.getNumber() == LanguageNumber.PLURAL) return null;
            override = indefiniteOverrides.get(form);
            if (override != null) return override;
            return INDEFINITE_ARTICLE.get(form.getCase()).get(form.getGender());
        default:
            return null;
        }
    }

    @Override
    public boolean hasStartsWith() {
        return true;
    }

    // κ, π, τ, μπ, ντ, γκ, τσ, τζ, ξ, ψ
    private static final String[] PLOSIVES = new String[] {"\u03ba","\u03c0","\u03c4", "\u03bc\u03c0", "\u03bd\u03c4", "\u03b3\u03ba", "\u03c4\u03c3", "\u03c4\u03b6", "\u03be", "\u03c8"};

    private static final TrieMatcher PLOSIVE_MATCHER = TrieMatcher.compile(PLOSIVES, PLOSIVES);

    @Override
    public boolean hasAutoDerivedStartsWith() {
        return true;
    }

    @Override
    public EnumSet<LanguageStartsWith> getRequiredStartsWith() {
        return EnumSet.of(LanguageStartsWith.CONSONANT, LanguageStartsWith.SPECIAL);  // Special is plosive in greek.
    }

    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        return EnumSet.of(LanguageCase.NOMINATIVE, LanguageCase.ACCUSATIVE, LanguageCase.GENITIVE, LanguageCase.VOCATIVE);
    }

    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return EnumSet.of(LanguageGender.NEUTER, LanguageGender.FEMININE, LanguageGender.MASCULINE);
    }

    @Override
    public String formLowercaseNounForm(String s, NounForm form) {
        return hasCapitalization() ? (s == null ? null : s.toLowerCase()) : s;
    }
}
