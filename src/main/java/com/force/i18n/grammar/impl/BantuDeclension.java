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

import static com.force.i18n.grammar.LanguageGender.*;
import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexAdjective;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexAdjectiveForm;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Declension for Bantu languages, with noun classes.
 *
 * For simplicity, we ask for nouns with singular and plural, and then ask for the noun class.
 * So when expressing a translation for "Account", we'll ask for
 * "Akaunti", "Akaunti", N Class (IX/X)
 *

 *
 * @author stamm
 * @since 1.1
 */
abstract class BantuDeclension extends AbstractLanguageDeclension {
    static final List<? extends NounForm> ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(PluralNounForm.class));
    static final Set<? extends NounForm> OTHER_FORMS = EnumSet.of(PluralNounForm.SINGULAR);
    private final List<BantuAdjectiveForm> adjectiveForms;

    public BantuDeclension(HumanLanguage language) {
        super(language);

        ImmutableList.Builder<BantuAdjectiveForm> adjBuilder = ImmutableList.builder();
        int adjOrdinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageGender gender : getRequiredGenders()) {
                adjBuilder.add(new BantuAdjectiveForm(this, number, gender, adjOrdinal++));
            }
        }
        this.adjectiveForms = adjBuilder.build();
    }


    static class BantuAdjectiveForm extends ComplexAdjectiveForm {
        private static final long serialVersionUID = 1L;

        private final LanguageNumber number;
        private final LanguageGender gender;

        public BantuAdjectiveForm(LanguageDeclension declension, LanguageNumber number, LanguageGender gender, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.gender = gender;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO; }
        @Override public LanguageCase getCase() {  return LanguageCase.NOMINATIVE; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguageStartsWith getStartsWith() {  return LanguageStartsWith.CONSONANT; }
        @Override public LanguageGender getGender() {  return this.gender; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }

        @Override
        public String getKey() {
            return getGender().getDbValue() + "-" + getNumber().getDbValue();
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.number, this.gender);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof BantuAdjectiveForm) {
                BantuAdjectiveForm o = this.getClass().cast(other);
                return super.equals(other) && this.number == o.number && this.gender == o.gender;
            }
            return false;
        }
    }

    /**
     * Represents a bantu adjective
     */
    public static class BantuAdjective extends ComplexAdjective<BantuAdjectiveForm> {
        private static final long serialVersionUID = 1L;

        BantuAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }

        @Override
        protected final Class<BantuAdjectiveForm> getFormClass() {
            return BantuAdjectiveForm.class;
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, ImmutableSet.of(getDeclension().getAdjectiveForm(LanguageStartsWith.CONSONANT, LanguageGender.CLASS_I, LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.ZERO, LanguagePossessive.NONE)));
        }
   }

   @Override
   public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
       return new BantuAdjective(this, name, position);
   }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith,
            LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new SimplePluralNounWithGender(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }

    @Override
    public List< ? extends AdjectiveForm> getAdjectiveForms() {
        return this.adjectiveForms;
    }

    @Override
    public List< ? extends NounForm> getAllNounForms() {
        return ALL_FORMS;
    }

    @Override
    public Collection< ? extends NounForm> getEntityForms() {
        return ALL_FORMS;
    }
    @Override
    public Collection< ? extends NounForm> getFieldForms() {
        return OTHER_FORMS;
    }

    @Override
    public Collection< ? extends NounForm> getOtherForms() {
        return OTHER_FORMS;
    }

    @Override
    public boolean hasArticle() {
        return false;
    }

    @Override
    public boolean hasGender() {
        return true;
    }

    @Override
    public LanguageGender getDefaultGender() {
        return LanguageGender.CLASS_I;
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }

    /**
     * Declension for the Kiswahili language
     * */
    /*
     * An example of specifying adjectives would be

 <adjective name="New">
    <value gender="M-wa" plural="n">Mpya</value>
    <value gender="M-wa" plural="y">Wapya</value>
    <value gender="M-mi" plural="n">Mpya</value>
    <value gender="M-mi" plural="y">Mipya</value>
    <value gender="Ma" plural="n">Jipya</value>
    <value gender="Ma" plural="y">Mapya</value>
    <value gender="Ki-vi" plural="n">Kipya</value>
    <value gender="Ki-vi" plural="y">Vipya</value>
    <value gender="N" plural="n">Mpya</value>
    <value gender="N" plural="y">Mpya</value>
    <value gender="U" plural="n">Mpya</value>
    <value gender="U" plural="y">Mpya</value>
    <value gender="Pa" plural="y">Mpya</value>
    <value gender="Ku" plural="y">Mpya</value>
    <value gender="Mu" plural="y">Mpya</value>
 </adjective>

 <adjective name="My">
    <value gender="M-wa" plural="n">Wangu</value>
    <value gender="M-wa" plural="y">Wangu</value>
    <value gender="M-mi" plural="n">Wangu</value>
    <value gender="M-mi" plural="y">Yangu</value>
    <value gender="Ma" plural="n">Langu</value>
    <value gender="Ma" plural="y">Yangu</value>
    <value gender="Ki-vi" plural="n">Changu</value>
    <value gender="Ki-vi" plural="y">Vyangu</value>
    <value gender="N" plural="n">Yangu</value>
    <value gender="N" plural="y">Zangu</value>
    <value gender="U" plural="n">Wangu</value>
    <value gender="U" plural="y">Wangu</value>
    <value gender="Pa" plural="y">Pangu</value>
    <value gender="Ku" plural="n">Kwangu</value>
    <value gender="Mu" plural="y">Mwangu</value>
 </adjective>

     *
     *
     * @author stamm
     * @since 220
     */
    static final class SwahiliDeclension extends BantuDeclension {
        public SwahiliDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return EnumSet.of(CLASS_I, CLASS_III, CLASS_V, CLASS_VII, CLASS_IX, CLASS_XI, CLASS_XVI, CLASS_XVII, CLASS_XVIII);
        }
    }

    /**
     * Declension for the isiZulu language
     *
     */
 /*
     * An example of specifying adjectives would be
 <adjective name="New">
    <value gender="I" plural="n">Omusha</value>
    <value gender="I" plural="y">Abasha</value>
    <value gender="III" plural="n">Omusha</value>
    <value gender="III" plural="y">Emisha</value>
    <value gender="V" plural="n">Elisha</value>
    <value gender="V" plural="y">Amasha</value>
    <value gender="VII" plural="n">Esisha</value>
    <value gender="VII" plural="y">Vipya</value>
    <value gender="IX" plural="n">Entsha</value>
    <value gender="IX" plural="y">Ezintsha</value>
    <value gender="XI" plural="n">Entsha</value>
    <value gender="XI" plural="y">Ezintsha</value>
    <value gender="XIV" plural="y">Obusha</value>
    <value gender="XV" plural="y">Okusha</value>
    <value gender="XVII" plural="y">Okusha</value>
 </adjective>

     *
     * @author stamm
     * @since 220
     */
    static final class ZuluDeclension extends BantuDeclension {
        public ZuluDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return EnumSet.of(CLASS_I, CLASS_III, CLASS_V, CLASS_VII, CLASS_IX, CLASS_XI, CLASS_XIV, CLASS_XV, CLASS_XVII);
        }
    }

    /**
     * Declension for the isiXhosa language.
     *
     * @author stamm
     * @since 220
     */
    static final class XhosaDeclension extends BantuDeclension {
        public XhosaDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageGender> getRequiredGenders() {
            return EnumSet.of(CLASS_I, CLASS_III, CLASS_V, CLASS_VII, CLASS_IX, CLASS_XI, CLASS_XIV, CLASS_XV, CLASS_XVII);
        }
    }
}
