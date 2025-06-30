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

import static com.force.i18n.grammar.LanguageCase.NOMINATIVE;
import static com.force.i18n.grammar.LanguageGender.FEMININE;
import static com.force.i18n.grammar.LanguageGender.MASCULINE;
import static com.force.i18n.grammar.LanguageNumber.SINGULAR;

import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.*;
import com.force.i18n.grammar.impl.SlavicDeclension.SlavicNoun;
import com.force.i18n.grammar.impl.SlavicDeclension.SlavicNounForm;
import com.google.common.collect.*;

/**
 * Abstract declension for the baltic languages (Latvian and Lithuanian).  It's very similar to Slavic
 * declension, except no neuter gender and Latvian has definitiveness in the adjective.
 *
 * Reuses SlavicNoun/NounForm
 *
 * @author stamm
 */
class BalticDeclension extends AbstractLanguageDeclension {
    private final List<SlavicNounForm> nounForms;
    private final Multimap<LanguageCase, SlavicNounForm> nounFormsByCase;
    private final NounFormMap<SlavicNounForm> nounFormMap;
    private final List<BalticAdjectiveForm> adjectiveForms;

    public BalticDeclension(HumanLanguage language) {
    	super(language);
        // Generate the different forms from subclass methods
        ImmutableList.Builder<SlavicNounForm> nounBuilder = ImmutableList.builder();
        ImmutableMultimap.Builder<LanguageCase, SlavicNounForm> byCaseBuilder = ImmutableMultimap.builder();
        int ordinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageCase caseType : getRequiredCases()) {
                SlavicNounForm form = new SlavicNounForm(this, number, caseType, ordinal++);
                byCaseBuilder.put(caseType, form);
                nounBuilder.add(form);
            }
        }
        this.nounForms = nounBuilder.build();
        this.nounFormsByCase = byCaseBuilder.build();
        this.nounFormMap = new NounFormMap<SlavicNounForm>(this.nounForms);

        ImmutableList.Builder<BalticAdjectiveForm> adjBuilder = ImmutableList.builder();
        int adjOrdinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageGender gender : getRequiredGenders()) {
                for (LanguageCase caseType : getRequiredCases()) {
                    adjBuilder.add(new BalticAdjectiveForm(this, number, gender, caseType, adjOrdinal++));
                }
            }
        }
        this.adjectiveForms = adjBuilder.build();
    }


    static class BalticAdjectiveForm extends ComplexAdjectiveForm {
        private static final long serialVersionUID = 1L;

        private final LanguageNumber number;
        private final LanguageCase caseType;
        private final LanguageGender gender;

        public BalticAdjectiveForm(LanguageDeclension declension, LanguageNumber number, LanguageGender gender,LanguageCase caseType, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.gender = gender;
            this.caseType = caseType;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO; }
        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguageStartsWith getStartsWith() {  return LanguageStartsWith.CONSONANT; }
        @Override public LanguageGender getGender() {  return this.gender; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }

        @Override
        public String getKey() {
            return getGender().getDbValue() + "-" + getNumber().getDbValue() + "-" + getCase().getDbValue();
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.caseType, this.number, this.gender);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof BalticAdjectiveForm) {
                BalticAdjectiveForm o = this.getClass().cast(other);
                return super.equals(other) && this.caseType == o.caseType && this.number == o.number
                        && this.gender == o.gender;
            }
            return false;
        }
    }

    /**
     * Represents a baltic adjective
     */
    public static class BalticAdjective extends ComplexAdjective<BalticAdjectiveForm> {
        private static final long serialVersionUID = 1L;

        BalticAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }

        @Override
        protected final Class<BalticAdjectiveForm> getFormClass() {
            return BalticAdjectiveForm.class;
        }

        @Override
        public boolean validate(String name) {
            return defaultValidate(name, ImmutableSet.of(getDeclension().getAdjectiveForm(LanguageStartsWith.CONSONANT, LanguageGender.FEMININE, LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.ZERO, LanguagePossessive.NONE)));
        }
   }


    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new BalticAdjective(this, name, position);
    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith,
            LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new SlavicNoun(this, name, pluralAlias, type, entityName, gender, access, isStandardField, isCopied);
    }


    @Override
    public List< ? extends AdjectiveForm> getAdjectiveForms() {
        return this.adjectiveForms;
    }

    @Override
    public List< ? extends NounForm> getAllNounForms() {
        return this.nounForms;
    }

    @Override
    public Collection< ? extends NounForm> getEntityForms() {
        return this.nounForms;
    }
    @Override
    public Collection< ? extends NounForm> getFieldForms() {
        return this.nounFormsByCase.get(NOMINATIVE);
    }

    @Override
    public Collection< ? extends NounForm> getOtherForms() {
        assert nounForms.get(0).getCase() == NOMINATIVE && nounForms.get(0).getNumber() == SINGULAR : "Invalid case map";
        return Collections.singleton(nounForms.get(0));
    }

    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive,
            LanguageArticle article) {
        if (article != LanguageArticle.ZERO || possessive != LanguagePossessive.NONE) return null;
        return this.nounFormMap.getForm(number, _case);
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
    public EnumSet<LanguageGender> getRequiredGenders() {
        return EnumSet.of(FEMININE, MASCULINE);
    }

    @Override
    public LanguageGender getDefaultGender() {
        return LanguageGender.FEMININE;
    }

    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        return SlavicDeclension.WEST_SLAVIC_CASES_NO_VOC;
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }
}
