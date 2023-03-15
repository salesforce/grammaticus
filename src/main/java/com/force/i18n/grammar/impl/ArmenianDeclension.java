/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.grammar.LanguageCase.*;

import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexNoun;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.ComplexNounForm;
import com.google.common.collect.ImmutableList;

/**
 * Declension for Armenian, which is unsupported, but is different enough to warrant
 * its own class.  It has 7 cases and no gender.
 *
 * TODO: It has definite article, but the definitiveness is dependent on the endsWith of the current
 * word, along with the startsWith of the next word, and some special rules depending on dialect.
 * File an issue if you need better Armenian support, and we can look into supporting all of this.
 *
 * There is an unchanging indefinite article, մի, so it isn't included in the grammar yet
 *
 * @author stamm
 */
class ArmenianDeclension extends AbstractLanguageDeclension {
    private static final Logger logger = Logger.getLogger(ArmenianDeclension.class.getName());
    private final List<ArmenianNounForm> entityForms;
    private final List<ArmenianNounForm> fieldForms;

    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        return EnumSet.of(NOMINATIVE, ACCUSATIVE, DATIVE, LOCATIVE, GENITIVE, ABLATIVE, INSTRUMENTAL);
    }

    @Override
    public Set<LanguageArticle> getAllowedArticleTypes() {
        return EnumSet.of(LanguageArticle.ZERO, LanguageArticle.DEFINITE);  // No indefinite article in armenian
    }

    @Override
    public boolean hasStartsWith() {
        return false;
    }

    @Override
    public boolean hasArticleInNounForm() {
        return true;
    }

    @Override
    public boolean hasGender() { return false; }  // Gender's irrelevant in armenian


    public ArmenianDeclension(HumanLanguage language) {
    	super(language);

        // Generate the different forms from subclass methods
        ImmutableList.Builder<ArmenianNounForm> entityBuilder = ImmutableList.builder();
        ImmutableList.Builder<ArmenianNounForm> fieldBuilder = ImmutableList.builder();
        int ordinal = 0;
        for (LanguageNumber number : getAllowedNumbers()) {
            for (LanguageCase caseType : getRequiredCases()) {
                for (LanguageArticle articleType : getAllowedArticleTypes()) {
                    ArmenianNounForm form = new ArmenianNounForm(this, number, caseType, articleType, ordinal++);
                    entityBuilder.add(form);
                    if (caseType == LanguageCase.NOMINATIVE && articleType == LanguageArticle.ZERO) {
                        fieldBuilder.add(form); // Only plurals count for the fields
                    }
                }
            }
        }
        this.entityForms = entityBuilder.build();
        this.fieldForms = fieldBuilder.build();
    }

    /**
     * Armenian nouns are inflected for case, and number
     */
    static class ArmenianNounForm extends ComplexNounForm {
        private static final long serialVersionUID = 1L;

        private final LanguageCase caseType;
        private final LanguageNumber number;
        private final LanguageArticle article;

        public ArmenianNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, LanguageArticle article, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
            this.article = article;
        }

        @Override public LanguageArticle getArticle() { return this.article; }
        @Override public LanguageCase getCase() {  return this.caseType; }
        @Override public LanguageNumber getNumber() {  return this.number; }
        @Override public LanguagePossessive getPossessive() { return LanguagePossessive.NONE;}

        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getCase().getDbValue() + "-" + getArticle().getDbValue();
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.caseType, this.number, this.article);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other instanceof ArmenianNounForm) {
                ArmenianNounForm o = this.getClass().cast(other);
                return super.equals(other) && this.caseType == o.caseType && this.number == o.number
                        && this.article == o.article;
            }
            return false;
        }

        @Override
        public String toString() {
            return "ArmenianNF:" + getKey();
        }
    }

    /**
     * Represents an Armenian noun.
     * See ArmenianNounForm for more info
     */
    public static class ArmenianNoun extends ComplexNoun<ArmenianNounForm> {
        private static final long serialVersionUID = 1L;

        ArmenianNoun(ArmenianDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, LanguageGender.NEUTER, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        protected final Class<ArmenianNounForm> getFormClass() {
        	return ArmenianNounForm.class;
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
    public List< ? extends ArticleForm> getArticleForms() {
        return Collections.singletonList(SimpleModifierForm.SINGULAR);
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase case1, LanguageArticle article, LanguagePossessive possessive) {
        // Adjectives in Armenian are invariant.  Ya rly.
        return SimpleModifierForm.SINGULAR;
    }

    @Override
    public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase case1) {
        return null;  // There is no article form.
    }

    @Override
    public Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new ArmenianNoun(this, name, pluralAlias, type, entityName, startsWith, access, isStandardField, isCopied);
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
}
