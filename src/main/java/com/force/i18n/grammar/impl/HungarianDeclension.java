/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;
import static com.force.i18n.grammar.LanguageCase.*;

import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.force.i18n.grammar.impl.ComplexGrammaticalForm.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Hungarian is a Uralic language that has possessives, articles, number, and cases (but no gender)
 * <p>
 * In the old system, hungarian didn't have articles specified (they were in the code); hence the Noun isn't a "legacy" noun.
 * <p>
 * Hungarian also didn't have startsWith enabled; however it does have an article that mutates based on whether
 * the next particle starts with a vowel or not.
 *
 * @author stamm
 */
class HungarianDeclension extends ArticledDeclension {
    private static final Logger logger = Logger.getLogger(HungarianDeclension.class.getName());
    private final List<HungarianNounForm> entityForms;
    private final List<HungarianNounForm> fieldForms;
    private final EnumMap<LanguagePossessive, NounFormMap<HungarianNounForm>> nounFormMap;

    @Override
    public EnumSet<LanguageCase> getRequiredCases() {
        // Hungarian uses cases for pretty much everything
        return EnumSet.of(NOMINATIVE, ACCUSATIVE, ILLATIVE, INESSIVE, ELATIVE,
                SUBLATIVE, SUPERESSIVE, DELATIVE, ALLATIVE, ABLATIVE, DATIVE, INSTRUMENTAL, TRANSLATIVE, CAUSALFINAL,
                ESSIVEFORMAL, TERMINATIVE, DISTRIBUTIVE);
    }

    @Override
    public EnumSet<LanguagePossessive> getRequiredPossessive() {
        // For now, we don't care about the possessive suffixes
        return EnumSet.of(LanguagePossessive.NONE, LanguagePossessive.FIRST, LanguagePossessive.SECOND);
    }

    @Override
    public EnumSet<LanguageStartsWith> getRequiredStartsWith() {
        return EnumSet.of(LanguageStartsWith.CONSONANT, LanguageStartsWith.VOWEL);
    }

    @Override
    public boolean hasGender() {
        return false;
    }  // Gender's irrelevant in hungarian

    @Override
    public boolean hasStartsWith() {
        return true;
    }  // Starting with a vowel is important for the definite article

    @Override
    public boolean hasPossessive() {
        return true;
    }

    static final List<? extends ArticleForm> ARTICLE_FORMS = ImmutableList.copyOf(EnumSet.allOf(HungarianArticleForm.class));


    public HungarianDeclension(HumanLanguage language) {
        super(language);
        // Generate the different forms from subclass methods
        ImmutableList.Builder<HungarianNounForm> entityBuilder = ImmutableList.builder();
        ImmutableList.Builder<HungarianNounForm> fieldBuilder = ImmutableList.builder();
        int ordinal = 0;
        for (LanguageNumber number : EnumSet.of(LanguageNumber.SINGULAR, LanguageNumber.PLURAL)) {
            for (LanguageCase caseType : getRequiredCases()) {
                for (LanguagePossessive possessive : getRequiredPossessive()) {
                    HungarianNounForm form = new HungarianNounForm(this, number, caseType, possessive, ordinal++);
                    entityBuilder.add(form);
                    if (caseType == LanguageCase.NOMINATIVE && possessive == LanguagePossessive.NONE) {
                        fieldBuilder.add(form); // Only plurals count for the fields
                    }
                }
            }
        }
        this.entityForms = entityBuilder.build();
        this.fieldForms = fieldBuilder.build();
        this.nounFormMap = NounFormMap.getPossessiveSpecificMap(this.entityForms);
    }

    /**
     * Hungarian nouns are inflected for case, number, and possessive
     */
    static class HungarianNounForm extends ComplexNounForm {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final LanguageCase caseType;
        private final LanguageNumber number;
        private final LanguagePossessive possesive;

        public HungarianNounForm(LanguageDeclension declension, LanguageNumber number, LanguageCase caseType, LanguagePossessive possesive, int ordinal) {
            super(declension, ordinal);
            this.number = number;
            this.caseType = caseType;
            this.possesive = possesive;
        }

        @Override
        public LanguageArticle getArticle() {
            return LanguageArticle.ZERO;
        }

        @Override
        public LanguageCase getCase() {
            return this.caseType;
        }

        @Override
        public LanguageNumber getNumber() {
            return this.number;
        }

        @Override
        public LanguagePossessive getPossessive() {
            return possesive;
        }

        @Override
        public String getKey() {
            return getNumber().getDbValue() + "-" + getCase().getDbValue() + "-" + getPossessive().getDbValue();
        }

        @Override
        public String toString() {
            return "HungarianNF:" + getKey();
        }
    }

    /**
     * The Hungarian articles are distinguished by whether the next noun starts with a vowel
     * sound or not (it only matters for the definite article, "a" vs "az" which is similar to a vs an in english)
     */
    public static enum HungarianArticleForm implements ArticleForm {
        SINGULAR(LanguageNumber.SINGULAR, LanguageStartsWith.CONSONANT),
        SINGULAR_V(LanguageNumber.SINGULAR, LanguageStartsWith.VOWEL),
        PLURAL(LanguageNumber.PLURAL, LanguageStartsWith.CONSONANT),
        PLURAL_V(LanguageNumber.PLURAL, LanguageStartsWith.VOWEL);

        private final LanguageNumber number;
        private final LanguageStartsWith startsWith;

        private HungarianArticleForm(LanguageNumber number, LanguageStartsWith startsWith) {
            this.number = number;
            this.startsWith = startsWith;
        }

        @Override
        public LanguageCase getCase() {
            return LanguageCase.NOMINATIVE;
        }

        @Override
        public LanguageGender getGender() {
            return LanguageGender.NEUTER;
        }

        @Override
        public LanguageNumber getNumber() {
            return this.number;
        }

        @Override
        public LanguageStartsWith getStartsWith() {
            return this.startsWith;
        }

        static HungarianArticleForm getForm(ModifierForm form) {
            return form.getNumber() == LanguageNumber.SINGULAR ?
                    (form.getStartsWith() == LanguageStartsWith.VOWEL ? SINGULAR_V : SINGULAR)
                    : (form.getStartsWith() == LanguageStartsWith.VOWEL ? PLURAL_V : PLURAL);
        }
    }


    /**
     * Represents an Hungarian noun.
     * See HungarianNounForm for more info
     */
    public static class HungarianNoun extends ComplexArticledNoun<HungarianNounForm> {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        HungarianNoun(HungarianDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, LanguageGender.NEUTER, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        protected final Class<HungarianNounForm> getFormClass() {
            return HungarianNounForm.class;
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

    /**
     * Represents a hungarian adjective
     */
    public static class HungarianArticle extends Article {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private Map<HungarianArticleForm, String> values = new HashMap<HungarianArticleForm, String>();

        HungarianArticle(ArticledDeclension declension, String name, LanguageArticle articleType) {
            super(declension, name, articleType);
        }

        @Override
        public Map<? extends ArticleForm, String> getAllValues() {
            return values;
        }

        @Override
        public String getString(ArticleForm form) {
            return values.get(form);
        }

        @Override
        protected void setString(ArticleForm form, String value) {
            values.put((HungarianArticleForm) form, intern(value));
        }

        @Override
        public boolean validate(String name) {
            defaultValidate(name, ImmutableSet.of(getDeclension().getArticleForm(LanguageStartsWith.CONSONANT, LanguageGender.NEUTER, LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE)));
            return true;
        }
    }

    @Override
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SimpleAdjectiveWithStartsWith(this, name, startsWith);
    }

    @Override
    protected Article createArticle(String name, LanguageArticle articleType) {
        return new HungarianArticle(this, name, articleType);
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new HungarianNoun(this, name, pluralAlias, type, entityName, startsWith, access, isStandardField, isCopied);
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
                                          LanguageCase case1, LanguageArticle article, LanguagePossessive possessive) {
        // TODO: Adjectives in hungarian are invariant?  It seems so
        return SimpleModifierForm.SINGULAR;
    }


    /**
     * TODO: Articles don't have cases in hungarian; but the super.getArticleForm doesn't really check for that so
     * the article form gets missed.
     * If there are more languages that have articles that aren't inflected for case (i.e. you get NPEs), then you the
     * logic in LanguageDeclension should get more complicated
     */
    @Override
    public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
                                      LanguageCase case1) {
        switch (number) {
            case PLURAL:
                return startsWith == LanguageStartsWith.VOWEL ? HungarianArticleForm.PLURAL_V : HungarianArticleForm.PLURAL;
            case SINGULAR:
                return startsWith == LanguageStartsWith.VOWEL ? HungarianArticleForm.SINGULAR_V : HungarianArticleForm.SINGULAR;
        }
        throw new AssertionError("Invalid article form");
    }

    @Override
    public NounForm getExactNounForm(LanguageNumber number, LanguageCase _case, LanguagePossessive possessive,
                                     LanguageArticle article) {
        if (article != LanguageArticle.ZERO) return null;
        NounFormMap<? extends NounForm> formMap = this.nounFormMap.get(possessive);
        return formMap == null ? null : formMap.getForm(number, _case);
    }

    @Override
    public List<? extends AdjectiveForm> getAdjectiveForms() {
        return Collections.singletonList(SimpleModifierForm.SINGULAR);
    }

    @Override
    public List<? extends NounForm> getAllNounForms() {
        return this.entityForms;
    }

    @Override
    public Collection<? extends NounForm> getEntityForms() {
        return this.entityForms;
    }

    @Override
    public Collection<? extends NounForm> getFieldForms() {
        return this.fieldForms;
    }

    @Override
    public Collection<? extends NounForm> getOtherForms() {
        return Collections.singleton(fieldForms.get(0));  // Only need "singular" for other forms
    }

    @Override
    public List<? extends ArticleForm> getArticleForms() {
        return ARTICLE_FORMS;
    }

    @Override
    protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        switch (articleType) {
            case INDEFINITE:
                return form.getNumber() == LanguageNumber.SINGULAR ? "Egy " : null;
            case DEFINITE:
                return form.getStartsWith() == LanguageStartsWith.VOWEL ? "Az " : "A ";
///CLOVER:OFF
            case ZERO:
                return null;
            default:
                throw new UnsupportedOperationException("Invalid article");
///CLOVER:ON
        }
    }
}
