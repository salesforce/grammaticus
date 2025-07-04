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

import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexArticledNoun;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexNounForm;
import com.google.common.collect.ImmutableList;

/**
 * Turk Turk Turkish!
 *
 * Nouns are very complex
 * Adjectives and articles are dead simple.
 *
 * @author jmooney,stamm
 */
abstract class TurkicDeclension extends ArticledDeclension {
    private static final Logger logger = Logger.getLogger(TurkicDeclension.class.getName());
    private final List<TurkishNounForm> entityForms;
    private final List<TurkishNounForm> fieldForms;

    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        return EnumSet.of(NOMINATIVE, ACCUSATIVE, DATIVE, LOCATIVE, GENITIVE, ABLATIVE);
    }

    @Override
    public Set<LanguageArticle> getAllowedArticleTypes() {
        return EnumSet.of(LanguageArticle.ZERO, LanguageArticle.INDEFINITE);  // No definite article in turkish
    }

    @Override
    public EnumSet<LanguagePossessive> getRequiredPossessive() {
        // For now, we don't care about the possessive suffixes
        return EnumSet.of(LanguagePossessive.NONE, LanguagePossessive.FIRST, LanguagePossessive.SECOND /*, LanguagePossessive.FIRST_PLURAL, LanguagePossessive.SECOND_PLURAL */);
    }

    @Override
    public boolean hasGender() { return false; }  // Gender's irrelevant in Turkish

    @Override
    public boolean hasStartsWith() { return false; }  // Starts with is irrelevant in turkish (ends with is very important, however)
    @Override
    public boolean hasPossessive() {
        return true;
    }

    public TurkicDeclension(HumanLanguage language) {
    	super(language);
        // Generate the different forms from subclass methods
        ImmutableList.Builder<TurkishNounForm> entityBuilder = ImmutableList.builder();
        ImmutableList.Builder<TurkishNounForm> fieldBuilder = ImmutableList.builder();
        int ordinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageCase caseType : getRequiredCases()) {
                for (LanguagePossessive possessive : getRequiredPossessive()) {
                    TurkishNounForm form = new TurkishNounForm(this, number, caseType, possessive, ordinal++);
                    entityBuilder.add(form);
                    if (caseType == LanguageCase.NOMINATIVE && possessive == LanguagePossessive.NONE) {
                        fieldBuilder.add(form); // Only plurals count for the fields
                    }
                }
            }
        }
        this.entityForms = entityBuilder.build();
        this.fieldForms = fieldBuilder.build();
    }

    /**
     * Turkish nouns are inflected for case, number, and possessive
     */
    static class TurkishNounForm extends ComplexNounForm {
        private static final long serialVersionUID = 1L;

        private final LanguageCase caseType;
        private final LanguageNumber number;
        private final LanguagePossessive possessive;

        public TurkishNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, LanguagePossessive possessive, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
            this.possessive = possessive;
        }

        @Override public LanguageArticle getArticle() { return LanguageArticle.ZERO; }
        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguagePossessive getPossessive() { return possessive; }

        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getCase().getDbValue() + "-" + getPossessive().getDbValue();
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.caseType, this.number, this.possessive);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof TurkishNounForm) {
                TurkishNounForm o = this.getClass().cast(other);
                return super.equals(other) && this.caseType == o.caseType && this.number == o.number
                        && this.possessive == o.possessive;
            }
            return false;
        }

        @Override
        public String toString() {
            return "TurkishNF:" + getKey();
        }
    }

    /**
     * Represents an Turkish noun.
     * See TurkishNounForm for more info
     */
    public static class TurkishNoun extends ComplexArticledNoun<TurkishNounForm> {
		private static final long serialVersionUID = 1L;

        TurkishNoun(TurkicDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, LanguageGender.NEUTER, access, isStandardField, isCopiedFromDefault);
        }

        @Override
		protected final Class<TurkishNounForm> getFormClass() {
        	return TurkishNounForm.class;
		}

		@Override
        protected boolean validateValues(String name, LanguageCase _case) {
            return defaultValidate(name, getDeclension().getFieldForms());
        }

        @Override
        protected boolean validateGender(String name) {
            if (getGender() != LanguageGender.NEUTER)
                logger.info(VALIDATION_WARNING_HEADER + name + " must be neuter");
            return super.validateGender(name);  // Let it go
        }

    }

    @Override
    public Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SimpleAdjective(this, name);
    }

    @Override
    public Article createArticle(String name, LanguageArticle articleType) {
        return new SimpleArticle(this, name, articleType);
    }

    @Override
    public List< ? extends ArticleForm> getArticleForms() {
        return Collections.singletonList(SimpleModifierForm.SINGULAR);
    }

    @Override
    public String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        if (articleType == LanguageArticle.INDEFINITE) {
            return "Bir ";  //
        } else {
            return null;
        }
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase case1, LanguageArticle article, LanguagePossessive possessive) {
        // Adjectives in turkish are invariant.  Ya rly.
        return SimpleModifierForm.SINGULAR;
    }

    @Override
    public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase case1) {
        return SimpleModifierForm.SINGULAR;
    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new TurkishNoun(this, name, pluralAlias, type, entityName, startsWith, access, isStandardField, isCopied);
    }

    @Override
    public List< ? extends AdjectiveForm> getAdjectiveForms() {
        return SimpleDeclension.ADJECTIVE_FORMS;
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

    /**
     * Turkish is the default.
     * @author stamm
     */
    static class TurkishDeclension extends TurkicDeclension {
        public TurkishDeclension(HumanLanguage language) {
            super(language);
        }
    }

    /**
     * Kazakh has an instrumental case, and doesn't have articles.
     * However, it does use "one" or "bir" to represent indefiniteness, like turkish does.  Sorta.
     *
     * @author stamm
     */
    static class KazakhDeclension extends TurkicDeclension {
        public KazakhDeclension(HumanLanguage language) {
            super(language);
        }

        @Override
        public EnumSet<LanguageCase> getRequiredCases() {
            return EnumSet.of(NOMINATIVE, ACCUSATIVE, DATIVE, LOCATIVE, GENITIVE, ABLATIVE, INSTRUMENTAL);
        }

        @Override
        public String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
            if (articleType == LanguageArticle.INDEFINITE) {
                return "Бір "; //
            } else {
                return null;
            }
        }
    }
}
