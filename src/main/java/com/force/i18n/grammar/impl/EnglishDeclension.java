/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.force.i18n.grammar.Noun.NounType;
import com.google.common.collect.ImmutableList;
/**
 * An implementation of declension of English nouns.
 *
 * English is fairly simple (
 *
 * @author stamm
 */
class EnglishDeclension extends ArticledDeclension {
	private static final Logger logger = Logger.getLogger(EnglishDeclension.class.getName());

	public EnglishDeclension(HumanLanguage language) {
		super(language);
        assert language.getLocale().getLanguage().equals("en") : "Initializing a language that isn't english";
	}

    /**
     * The english articles are distinguished by whether the next noun starts with a vowel
     * sound or not (although the, unlike a/an is spelled the same).
     */
    public static enum EnglishArticleForm implements ArticleForm {
        SINGULAR(LanguageNumber.SINGULAR, LanguageStartsWith.CONSONANT),
        SINGULAR_V(LanguageNumber.SINGULAR, LanguageStartsWith.VOWEL),
        PLURAL(LanguageNumber.PLURAL, LanguageStartsWith.CONSONANT)
        ;

        private final LanguageNumber number;
        private final LanguageStartsWith startsWith;
        private EnglishArticleForm(LanguageNumber number, LanguageStartsWith startsWith) {
            this.number = number;
            this.startsWith = startsWith;
        }

        @Override public LanguageCase getCase() { return LanguageCase.NOMINATIVE; }
        @Override public LanguageGender getGender() { return LanguageGender.NEUTER; }
        @Override public LanguageNumber getNumber() { return this.number; }
        @Override public LanguageStartsWith getStartsWith() {return this.startsWith; }
        // Helper method to convert from "generic" form to enum
        static EnglishArticleForm getForm(ModifierForm form) {
            return form.getNumber() == LanguageNumber.SINGULAR ?
                    (form.getStartsWith() == LanguageStartsWith.VOWEL ? SINGULAR_V : SINGULAR)
                    : PLURAL;
        }
    }

    /**
     * Represents an english adjective
     */
    public static class EnglishArticle extends Article {
        private String singular; // We only store one value
        private String singularVowel; // We only store one value
        private String plural; // We only store one value
        private static final long serialVersionUID = 597093332610194996L; // javac generated value. Mandatory for javac/eclipse compatibility

        EnglishArticle(EnglishDeclension declension, String name, LanguageArticle articleType) {
            super(declension, name, articleType);
        }

        @Override
        public Map<? extends ArticleForm, String> getAllValues() {
            return enumMapFilterNulls(EnglishArticleForm.SINGULAR, singular,
                    EnglishArticleForm.SINGULAR_V, singularVowel,
                    EnglishArticleForm.PLURAL, plural);
        }

        @Override
        public String getString(ArticleForm form) {
            switch (EnglishArticleForm.getForm(form)) {
            case PLURAL:  return plural;
            case SINGULAR_V: return singularVowel;
            default:
            case SINGULAR:  return singular;
            }
        }

        @Override
        protected void setString(ArticleForm form, String value) {
            switch (EnglishArticleForm.getForm(form)) {
            case PLURAL:  this.plural = intern(value);  break;
            case SINGULAR_V: this.singularVowel = intern(value);  break;
            default:
            case SINGULAR:  this.singular = intern(value);
            }
        }

        @Override
        public boolean validate(String name) {
            if (this.singular == null) {
                logger.info("###\tError: The article " + name + " has no form");
                return false;
            }
            if (this.singularVowel == null) {
                this.singularVowel = this.singular;
            }
            if (this.plural == null) {
                this.plural = this.singular;
            }
            return true;
        }
    }

    // All the forms you can request
    static final List<? extends NounForm> ALL_FORMS = ImmutableList.copyOf(EnumSet.allOf(PluralNounForm.class));
    // All the forms you can set for "other" forms
    static final Set<? extends NounForm> OTHER_FORMS = EnumSet.of(PluralNounForm.SINGULAR);
    // All the forms you can set for "other" forms
    static final List<? extends AdjectiveForm> ADJECTIVE_FORMS = Collections.singletonList(SimpleModifierForm.SINGULAR);
    // All the forms you can set for articles
    static final List<? extends ArticleForm> ARTICLE_FORMS = ImmutableList.copyOf(EnumSet.of(EnglishArticleForm.SINGULAR, EnglishArticleForm.SINGULAR_V, EnglishArticleForm.PLURAL));

    @Override public List<? extends NounForm> getAllNounForms() { return ALL_FORMS;  }

    @Override public Collection<? extends NounForm> getEntityForms() { return getAllNounForms();  }

    @Override public Collection<? extends NounForm> getFieldForms() { return getAllNounForms(); }

    @Override public Collection<? extends NounForm> getOtherForms() { return OTHER_FORMS; }

    @Override public List< ? extends AdjectiveForm> getAdjectiveForms() { return ADJECTIVE_FORMS; }

    @Override public List< ? extends ArticleForm> getArticleForms() { return ARTICLE_FORMS; }


    @Override
    protected Adjective createAdjective(String name, LanguageStartsWith startsWith, LanguagePosition position) {
        return new SimpleAdjectiveWithStartsWith(this, name, startsWith);
    }

    @Override
    protected Article createArticle(String name, LanguageArticle articleType) {
        return new EnglishArticle(this, name, articleType);
    }

    /**
     * Simplify the code to just return what you want without iterating through the forms
     */
    @Override
    public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case) {
        if (number == LanguageNumber.PLURAL) {
            return EnglishArticleForm.PLURAL;
        }
        return startsWith == LanguageStartsWith.VOWEL ? EnglishArticleForm.SINGULAR_V : EnglishArticleForm.SINGULAR;
    }

    @Override
    public AdjectiveForm getAdjectiveForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
            LanguageCase _case, LanguageArticle article, LanguagePossessive possessive) {
        return SimpleModifierForm.SINGULAR;
    }

    @Override
    public boolean hasGender() {
        return false;
    }

    @Override
    public boolean hasStartsWith() {
        return true;
    }

    @Override
    public EnumSet<LanguageStartsWith> getRequiredStartsWith() {
        return EnumSet.of(LanguageStartsWith.CONSONANT, LanguageStartsWith.VOWEL);  // Only generally care about consonant.
    }

    @Override
    protected Noun createNoun(String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopied) {
        return new SimpleArticledPluralNoun(this, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopied);
    }

    @Override
    protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        EnglishArticleForm e = EnglishArticleForm.getForm(form);

        switch (articleType) {
        case INDEFINITE:
            switch (e) {
            case PLURAL:  return null;
            case SINGULAR_V: return "An ";
            case SINGULAR:  return "A ";
            }
            throw new UnsupportedOperationException("Invalid article");
        case DEFINITE:
            return "The ";
        case ZERO:
            return null;
        default:
            throw new UnsupportedOperationException("Invalid article");
        }
    }
}
