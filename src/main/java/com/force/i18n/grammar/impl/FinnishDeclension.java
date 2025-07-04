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

import static com.force.i18n.grammar.LanguageCase.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Ahh, finnish.  More forms than you can shake a stick at (66).  No articles or gender though, which is nice
 *
 * see spec at \\moorea\bugs\77879\Finnish Renaming Spec.doc
 * @author yoikawa,stamm
 */
class FinnishDeclension extends AbstractLanguageDeclension {
    private static final Logger logger = Logger.getLogger(FinnishDeclension.class.getName());

    private final List<FinnishNounForm> entityForms;
    private final List<FinnishNounForm> fieldForms;
    private final List<FinnishAdjectiveForm> adjectiveForms;

    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        // Finnish uses cases for pretty much everything
        return EnumSet.of(NOMINATIVE, GENITIVE, INESSIVE, ELATIVE, ILLATIVE,
                ADESSIVE, ABLATIVE, ALLATIVE, ESSIVE, TRANSLATIVE, PARTITIVE );
    }

    @Override
    public EnumSet<LanguagePossessive> getRequiredPossessive() {
        return EnumSet.of(LanguagePossessive.NONE, LanguagePossessive.FIRST, LanguagePossessive.SECOND);
    }
    @Override
    public boolean hasGender() {
        return false;
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }

    @Override
    public boolean hasPossessive() {
        return true;
    }

    public FinnishDeclension(HumanLanguage language) {
    	super(language);
        // Generate the different forms from subclass methods
        ImmutableList.Builder<FinnishNounForm> entityBuilder = ImmutableList.builder();
        ImmutableList.Builder<FinnishNounForm> fieldBuilder = ImmutableList.builder();
        int ordinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageCase caseType : getRequiredCases()) {
                for (LanguagePossessive possessive : getRequiredPossessive()) {
                    FinnishNounForm form = new FinnishNounForm(this, number, caseType, possessive, ordinal++);
                    entityBuilder.add(form);
                    if (caseType == LanguageCase.NOMINATIVE && possessive == LanguagePossessive.NONE) {
                        fieldBuilder.add(form); // Only plurals count for the fields
                    }
                }
            }
        }
        this.entityForms = entityBuilder.build();
        this.fieldForms = fieldBuilder.build();

        ImmutableList.Builder<FinnishAdjectiveForm> adjBuilder = ImmutableList.builder();
        int adjOrdinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageCase caseType : getRequiredCases()) {
                adjBuilder.add(new FinnishAdjectiveForm(this, number, caseType, adjOrdinal++));
            }
        }
        this.adjectiveForms = adjBuilder.build();
    }


    /**
     * Finnish nouns are inflected for case, number, and possessive
     */
    static class FinnishNounForm extends ComplexNounForm {
        private static final long serialVersionUID = 1L;

        private final LanguageCase caseType;
        private final LanguageNumber number;
        private final LanguagePossessive possessive;

        public FinnishNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, LanguagePossessive possessive, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
            this.possessive = possessive;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO; }
        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguagePossessive getPossessive() { return possessive;}

        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getCase().getDbValue() + "-" + getPossessive().getDbValue();
        }

        @Override
        public String toString() {
            return "FinnishNF:"+getKey();
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.caseType, this.number, this.possessive);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof FinnishNounForm) {
                FinnishNounForm o = this.getClass().cast(other);
                return super.equals(other) && this.caseType == o.caseType && this.number == o.number
                        && this.possessive == o.possessive;
            }
            return false;
        }
    }

    /**
     * Finnish nouns are inflected for case and number
     */
    static class FinnishAdjectiveForm extends ComplexAdjectiveForm {
        private static final long serialVersionUID = 1L;

        private final LanguageNumber number;
        private final LanguageCase caseType;

        public FinnishAdjectiveForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO; }
        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguageStartsWith getStartsWith() {  return LanguageStartsWith.CONSONANT; }
        @Override public LanguageGender getGender() {  return LanguageGender.NEUTER; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE; }

        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getCase().getDbValue();
        }

        @Override
        public void appendJsFormReplacement(Appendable a, String termFormVar, String genderVar, String startsWithVar)
                throws IOException {
            a.append(termFormVar);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.caseType, this.number);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof FinnishAdjectiveForm) {
                FinnishAdjectiveForm o = this.getClass().cast(other);
                return super.equals(other) && this.caseType == o.caseType && this.number == o.number;
            }
            return false;
        }
    }

    /**
     * Represents an Finnish noun.
     * See FinnishNounForm for more info
     */
    public static class FinnishNoun extends ComplexNoun<FinnishNounForm> {
        private static final long serialVersionUID = 1L;

        FinnishNoun(FinnishDeclension declension, String name, String pluralAlias, NounType type, String entityName, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, LanguageStartsWith.CONSONANT, LanguageGender.NEUTER, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        protected final Class<FinnishNounForm> getFormClass() {
            return FinnishNounForm.class;
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            return defaultValidate(name, getDeclension().getFieldForms());
        }

        @Override
        protected boolean validateGender(String name) {
            if (getGender() != LanguageGender.NEUTER) {
                logger.info(VALIDATION_WARNING_HEADER + name + " invalid gender");
                setGender(getDeclension().getDefaultGender());
            }
            return true;
        }
    }

    /**
     * Represents a finnish adjective
     */
    public static class FinnishAdjective extends ComplexAdjective<FinnishAdjectiveForm> {
        private static final long serialVersionUID = 1L;

        FinnishAdjective(LanguageDeclension declension, String name, LanguagePosition position) {
            super(declension, name, position);
        }

        @Override
        protected final Class<FinnishAdjectiveForm> getFormClass() {
        	return FinnishAdjectiveForm.class;
        }

        @Override
        public boolean validate(String name) {
            defaultValidate(name, ImmutableSet.of(getDeclension().getAdjectiveForm(LanguageStartsWith.CONSONANT, LanguageGender.NEUTER, LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE, LanguageArticle.ZERO, LanguagePossessive.NONE)));
            return true;
        }
    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new FinnishAdjective(this, name, position);
    }

    /* (non-Javadoc)
     * @see i18n.grammar.LanguageDeclension#createNoun(i18n.grammar.Noun.NounType, i18n.grammar.LanguageStartsWith, i18n.grammar.LanguageGender, java.lang.String, boolean)
     */
    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new FinnishNoun(this, name, pluralAlias, type, entityName, access, isStandardField, isCopied);
    }

    @Override
    public List< ? extends AdjectiveForm> getAdjectiveForms() {
        return this.adjectiveForms;
    }

    @Override
    public List< ? extends NounForm> getAllNounForms() {
        return this.entityForms;
    }

    @Override
    public Collection< ? extends NounForm> getEntityForms() {
        return this.entityForms;
    }

    @Override
    public Collection< ? extends NounForm> getFieldForms() {
        return this.fieldForms;
    }

    @Override
    public Collection< ? extends NounForm> getOtherForms() {
        return Collections.singleton(fieldForms.get(0));  // Only need "singular" for other forms
    }

    static class EstonianDeclension extends FinnishDeclension{
        public EstonianDeclension(HumanLanguage language) {
			super(language);
		}

		@Override
        public EnumSet<LanguagePossessive> getRequiredPossessive() {
            return EnumSet.of(LanguagePossessive.NONE);
        }

        @Override
        public boolean hasPossessive() {
            return false;
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            // Estonian uses more cases than Finnish (but no possessive)
            return EnumSet.of(NOMINATIVE, GENITIVE, PARTITIVE, ILLATIVE, INESSIVE, ELATIVE, ALLATIVE, ADESSIVE, ABLATIVE, TRANSLATIVE,
                    TERMINATIVE, ESSIVE, ABESSIVE, COMITATIVE);
        }
    }
}
