/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.util.*;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LanguageConstants;
/**
 * Represents a language that has articles, and provides a generic mechanism for
 * handling article creation
 *
 * @author stamm
 */
public abstract class ArticledDeclension extends LanguageDeclension {
    public ArticledDeclension(HumanLanguage language) {
		super(language);
	}

	@Override
    public final boolean hasArticle() {
        return true;
    }
    /**
     * Represents a noun that in the old grammar engine had an "article" that could always be derived from the noun itself as opposed to the translation.
     * When parsing a label that refers to articles in the old way, this provides backwards compatibility, i.e.
     * in english, when you parse &lt;Account article="a"&gt;, this will return "An account" instead of throwing an exception.
     *
     * See EnglishNounForm for more info
     */
    public abstract static class LegacyArticledNoun extends Noun {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;

		protected LegacyArticledNoun(ArticledDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        public final String getString(NounForm form) {
            if (form instanceof LegacyArticledNounForm) {
                LegacyArticledNounForm lanf = (LegacyArticledNounForm)form;
                if (getNounType() == NounType.ENTITY) {
                    // OK, we have to "append" the old form
                    String baseString = getExactString(lanf.getBaseNounForm());
                    ArticleForm articleForm = getDeclension().getApproximateArticleForm(getStartsWith(), getGender(), form.getNumber(), form.getCase());
                    String articleString = ((ArticledDeclension)getDeclension()).getDefaultArticleString(articleForm, lanf.getArticle());
                    return appendArticleToBase(baseString, articleString, form);
                } else {
                    // Legacy articles aren't/weren't supported on compound nouns....
                    return getExactString(lanf.getBaseNounForm());
                }
            } else {
                return getExactString(form);
            }
        }

        protected String appendArticleToBase(String base, String article, NounForm form) {
            if (article == null) {
                return base;
            }
            return article + getDeclension().formLowercaseNounForm(base, form);
        }

        /**
         * Subclasses implement this class to support auto-appending of the article.
         * @param form the form of the noun for which the exact string is needed (without adding an article)
         * @return the exact string for the form, nor {@code null} if the form isn't specified
         */
        public abstract String getExactString(NounForm form);
    }
    /**
     * @return a language specific implementation for modifiers/adjectives in this language
     */
    @Override
    protected abstract Article createArticle(String name, LanguageArticle articleType);

    /**
     * @return The set of articles allowed in the language.  Some language have no definite article
     */
    @Override
    public Set<LanguageArticle> getAllowedArticleTypes() {
        return EnumSet.of(LanguageArticle.ZERO, LanguageArticle.INDEFINITE, LanguageArticle.DEFINITE);
    }

    /**
     * @return all of the various forms of adjectives available in the dictionary
     */
    @Override
    public abstract List<? extends ArticleForm> getArticleForms();

    /**
     * Return the "default" article strings.
     * TODO: Remove this when it's unnecessary (i.e. when these are all in the sfdcnames.xml file)
     * TODO: This isn't super useful for languages that postpend articles.
     * @param form the article form to return
     * @param articleType the article type you want (indefinite/definite)
     * @return the default article string from java
     */
    protected abstract String getDefaultArticleString(ArticleForm form, LanguageArticle articleType);

    /**
     * Represents a simple adjective with one inflection
     */
    public static class SimpleArticle extends Article {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private static final Logger logger = Logger.getLogger(SimpleArticle.class.getName());
        private String value;
        public SimpleArticle(ArticledDeclension declension, String name, LanguageArticle articleType) {
            super(declension, name, articleType);
        }
        @Override
        public Map<SimpleModifierForm, String> getAllValues() {
            return Collections.singletonMap(SimpleModifierForm.SINGULAR, value);
        }
        @Override
        public String getString(ArticleForm form) {
            return value;
        }
        @Override
        protected void setString(ArticleForm form, String value) {
            this.value = intern(value);
        }
        @Override
        public boolean validate(String name) {
            if (this.value == null) {
                logger.info("###\tError: The article " + name + " has no form");
                return false;
            }
            return true;
        }
    }

    /**
     * Represents a simple noun with a singular and plural form, but that supports articles.
     * Pretty much the same as SimplePluarlNoun, but needing to extend LegacyArticledNoun to support articles
     */
    public static class SimpleArticledPluralNoun extends LegacyArticledNoun {
        /**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private static final Logger logger = Logger.getLogger(SimpleArticledPluralNoun.class.getName());
        private String singular;
        private String plural;

        public SimpleArticledPluralNoun(ArticledDeclension declension, String name, String pluralAlias, NounType type, String entityName, LanguageStartsWith startsWith, LanguageGender gender, String access, boolean isStandardField, boolean isCopiedFromDefault) {
            super(declension, name, pluralAlias, type, entityName, startsWith, gender, access, isStandardField, isCopiedFromDefault);
        }

        @Override
        public Map<? extends NounForm, String> getAllDefinedValues() {
            return enumMapFilterNulls(PluralNounForm.SINGULAR, singular, PluralNounForm.PLURAL, plural);
        }

        @Override
        public String getDefaultString(boolean isPlural) {
            return isPlural && plural != null ? plural : singular;
        }

        @Override
        public String getExactString(NounForm form) {
            assert form instanceof PluralNounForm : "Why are you asking for some random noun form.  Really? " + form.getClass();
            if (getDeclension().getLanguage().getLocale().getLanguage().equals(LanguageConstants.ENGLISH)) {
                // TODO: English has the unexpected behavior of returning a default value
                // even when getExactString() is called
                return getDefaultString(form.getNumber() == LanguageNumber.PLURAL);
            }
            return form.getNumber() == LanguageNumber.PLURAL ? plural : singular;
        }

        @Override
        protected void setString(String value, NounForm form) {
            value = intern(value);
            if (form.getNumber().isPlural()) {
                this.plural = value;
                if (value != null && value.equals(this.singular))
                 {
                    this.singular = value; // Keep one reference for serialization
                }
            } else {
                this.singular = value;
                if (value != null && value.equals(this.plural))
                 {
                    this.plural = value; // Keep one reference for serialization
                }
            }
        }

        @Override
        protected boolean validateValues(String name, LanguageCase _case) {
            if (this.singular == null) {
                logger.info("###\tError: The noun " + name + " has no singular form");
                return false;
            }
            return true;
        }

        @Override
        public Noun clone() {
            SimpleArticledPluralNoun noun = (SimpleArticledPluralNoun) super.clone();
            return noun;
        }

        @Override
        public void makeSkinny() {
        }
    }
}
