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

import java.io.IOException;
import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Represents italian, a romance language which has complex combining rules around articles.
 * @author stamm
 */
class ItalianDeclension extends RomanceDeclension {
    public ItalianDeclension(HumanLanguage language) {
        super(language);
    }

    /**
     * Adjective form for languages that don't care about "starts with"
     */
    public enum ItalianModifierForm implements AdjectiveForm, ArticleForm {
        // TODO: are all of these necessary?
        SINGULAR_MASCULINE(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, LanguageStartsWith.CONSONANT),
        SINGULAR_FEMININE(LanguageNumber.SINGULAR, LanguageGender.FEMININE, LanguageStartsWith.CONSONANT),
        PLURAL_MASCULINE(LanguageNumber.PLURAL, LanguageGender.MASCULINE, LanguageStartsWith.CONSONANT),
        PLURAL_FEMININE(LanguageNumber.PLURAL, LanguageGender.FEMININE, LanguageStartsWith.CONSONANT),
        SINGULAR_MASCULINE_V(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, LanguageStartsWith.VOWEL),
        SINGULAR_FEMININE_V(LanguageNumber.SINGULAR, LanguageGender.FEMININE, LanguageStartsWith.VOWEL),
        PLURAL_MASCULINE_V(LanguageNumber.PLURAL, LanguageGender.MASCULINE, LanguageStartsWith.VOWEL),
        PLURAL_FEMININE_V(LanguageNumber.PLURAL, LanguageGender.FEMININE, LanguageStartsWith.VOWEL),
        SINGULAR_MASCULINE_Z(LanguageNumber.SINGULAR, LanguageGender.MASCULINE, LanguageStartsWith.SPECIAL),
        SINGULAR_FEMININE_Z(LanguageNumber.SINGULAR, LanguageGender.FEMININE, LanguageStartsWith.SPECIAL),
        PLURAL_MASCULINE_Z(LanguageNumber.PLURAL, LanguageGender.MASCULINE, LanguageStartsWith.SPECIAL),
        PLURAL_FEMININE_Z(LanguageNumber.PLURAL, LanguageGender.FEMININE, LanguageStartsWith.SPECIAL),
        ;

        private final LanguageNumber number;
        private final LanguageGender gender;
        private final LanguageStartsWith startsWith;
        private ItalianModifierForm(LanguageNumber number, LanguageGender gender, LanguageStartsWith startsWith) {
            this.number = number;
            this.gender = gender;
            this.startsWith = startsWith;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO;}
        @Override public LanguageCase getCase() { return LanguageCase.NOMINATIVE; }
        @Override public LanguageNumber getNumber() {return this.number;}
        @Override public LanguageGender getGender() {return this.gender;}
        @Override public LanguageStartsWith getStartsWith() { return startsWith; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }
        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getGender().getDbValue() + "-" + getStartsWith().getDbValue();
        }

        @Override
        public void appendJsFormReplacement(Appendable a, String termFormVar, String genderVar, String startsWithVar)
                throws IOException {
            a.append(termFormVar+".substr(0,2)+"+genderVar+"+'-'+"+startsWithVar);
        }
    }

    protected static class ItalianAdjective extends Adjective {
        private static final long serialVersionUID = 1L;

        // The "keys" here are StartsWith, Gender, and Plurality
        private final EnumMap<ItalianModifierForm, String> values = new EnumMap<>(ItalianModifierForm.class);
        private final LanguageStartsWith startsWith;

        ItalianAdjective(LanguageDeclension declension, String name, LanguageStartsWith startsWith, LanguagePosition position) {
            super(declension, name, position);
            this.startsWith = startsWith;
        }

        @Override
        public LanguageStartsWith getStartsWith() {
            return this.startsWith;
        }

        @Override
        public Map< ? extends AdjectiveForm, String> getAllValues() {
            return values;
        }

        @Override
        public String getString(AdjectiveForm form) {
            return values.get(form);
        }

        @Override
        protected void setString(AdjectiveForm form, String value) {
            assert form instanceof ItalianModifierForm : "The Italian do not like their language sullied with foreign words";
            values.put((ItalianModifierForm)form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, EnumSet.of(ItalianModifierForm.SINGULAR_FEMININE));
        }

        protected Object readResolve() {
            this.values.replaceAll((k, v) -> intern(v));
            return this;
        }
    }

    protected static class ItalianArticle extends Article {
        private static final long serialVersionUID = 1L;

        // The "keys" here are StartsWith, Gender, and Plurality
        EnumMap<ItalianModifierForm,String> values = new EnumMap<>(ItalianModifierForm.class);

        ItalianArticle(ItalianDeclension declension, String name, LanguageArticle articleType) {
            super(declension, name, articleType);
        }

        @Override
        public Map< ? extends ArticleForm, String> getAllValues() {
            return values;
        }

        @Override
        public String getString(ArticleForm form) {
            return values.get(form);
        }

        @Override
        protected void setString(ArticleForm form, String value) {
            assert form instanceof ItalianModifierForm : "The Italian do not like their language sullied with foreign words";
            values.put((ItalianModifierForm)form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, EnumSet.of(ItalianModifierForm.SINGULAR_FEMININE));
        }

        protected Object readResolve() {
            this.values.replaceAll((k, v) -> intern(v));
            return this;
        }
    }

    @Override
    public boolean hasStartsWith() {
        return true;
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new ItalianAdjective(this, name, startsWith, position);
    }

    private static final List<ItalianModifierForm> ALL_MODIFIER_FORMS = ImmutableList.copyOf(EnumSet.allOf(ItalianModifierForm.class));

    @Override
    public List< ? extends AdjectiveForm> getAdjectiveForms() {
        return ALL_MODIFIER_FORMS;
    }

    @Override
    public List< ? extends ArticleForm> getArticleForms() {
        return ALL_MODIFIER_FORMS;
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        // Optimize for local cases
        if (_case != LanguageCase.NOMINATIVE || article != LanguageArticle.ZERO) return null;
        switch (startsWith) {
        case CONSONANT:
            return gender == LanguageGender.MASCULINE ? (number.isPlural() ? ItalianModifierForm.PLURAL_MASCULINE : ItalianModifierForm.SINGULAR_MASCULINE)
                    : (number.isPlural() ? ItalianModifierForm.PLURAL_FEMININE : ItalianModifierForm.SINGULAR_FEMININE);
        case VOWEL:
            return gender == LanguageGender.MASCULINE ? (number.isPlural() ? ItalianModifierForm.PLURAL_MASCULINE_V : ItalianModifierForm.SINGULAR_MASCULINE_V)
                    : (number.isPlural() ? ItalianModifierForm.PLURAL_FEMININE_V : ItalianModifierForm.SINGULAR_FEMININE_V);
        case SPECIAL:
            return gender == LanguageGender.MASCULINE ? (number.isPlural() ? ItalianModifierForm.PLURAL_MASCULINE_Z : ItalianModifierForm.SINGULAR_MASCULINE_Z)
                    : (number.isPlural() ? ItalianModifierForm.PLURAL_FEMININE_Z : ItalianModifierForm.SINGULAR_FEMININE_Z);
        }
///CLOVER:OFF
        return null;
///CLOVER:ON
    }


    @Override
    public Collection< ? extends NounForm> getEntityForms() {
        return getAllNounForms();  // You need to specify all of them.
    }

    @Override
    public EnumSet<LanguageStartsWith> getRequiredStartsWith() {
        return EnumSet.of(LanguageStartsWith.CONSONANT, LanguageStartsWith.VOWEL, LanguageStartsWith.SPECIAL);
    }

    private static final EnumMap<ItalianModifierForm, String> DEFINITE_ARTICLE =
        new EnumMap<>(ImmutableMap.<ItalianModifierForm,String>builder()
                .put(ItalianModifierForm.SINGULAR_FEMININE, "La ")
                .put(ItalianModifierForm.SINGULAR_MASCULINE, "Il ")
                .put(ItalianModifierForm.PLURAL_FEMININE, "Le ")
                .put(ItalianModifierForm.PLURAL_MASCULINE, "I ")
                .put(ItalianModifierForm.SINGULAR_FEMININE_V, "L'")
                .put(ItalianModifierForm.SINGULAR_MASCULINE_V, "L'")
                .put(ItalianModifierForm.PLURAL_FEMININE_V, "Le ")
                .put(ItalianModifierForm.PLURAL_MASCULINE_V, "Gli ")
                .put(ItalianModifierForm.SINGULAR_FEMININE_Z, "La ")
                .put(ItalianModifierForm.SINGULAR_MASCULINE_Z, "Lo ")
                .put(ItalianModifierForm.PLURAL_FEMININE_Z, "Le ")
                .put(ItalianModifierForm.PLURAL_MASCULINE_Z, "Gli ")
                .build());

    private static final EnumMap<ItalianModifierForm, String> INDEFINITE_ARTICLE =
        new EnumMap<>(ImmutableMap.<ItalianModifierForm,String>builder()
                .put(ItalianModifierForm.SINGULAR_FEMININE, "Una ")
                .put(ItalianModifierForm.SINGULAR_MASCULINE, "Un ")
                .put(ItalianModifierForm.SINGULAR_FEMININE_V, "Un'")
                .put(ItalianModifierForm.SINGULAR_MASCULINE_V, "Un ")
                .put(ItalianModifierForm.SINGULAR_FEMININE_Z, "Una ")
                .put(ItalianModifierForm.SINGULAR_MASCULINE_Z, "Uno ")
                .build());

    @Override
    protected Map< ? extends ArticleForm, String> getDefiniteArticles() {
        return DEFINITE_ARTICLE;
    }

    @Override
    protected Map< ? extends ArticleForm, String> getIndefiniteArticles() {
        return INDEFINITE_ARTICLE;
    }

    @Override
    public Article createArticle(String name, LanguageArticle articleType) {
        return new ItalianArticle(this, name, articleType);
    }
}
