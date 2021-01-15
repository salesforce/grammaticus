/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar;

import static com.force.i18n.commons.util.settings.IniFileUtil.intern;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableSet;

/**
 * In an articled declension, this represents the different forms of articles
 * @author stamm
 */
public abstract class Article extends NounModifier {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(Article.class.getName());

    private final LanguageArticle articleType;
    protected Article(ArticledDeclension declension, String name, LanguageArticle articleType) {
        super(declension, name);
        this.articleType = articleType;
    }

    @Override
    public ArticledDeclension getDeclension() {
        return (ArticledDeclension) super.getDeclension();
    }

    public LanguageArticle getArticleType() {
        return this.articleType;
    }


    /**
     * @return all the forms of the adjective
     */
    @Override
    public abstract Map<? extends ArticleForm, String> getAllValues();

    /**
     * @return the value of this adjective for the given form
     * @param form the form to retrieve
     */
    protected abstract String getString(ArticleForm form);

    @Override
    public final String getString(ModifierForm form) {
        return getString((ArticleForm)form);
    }

    @Override
    public String getDefaultValue() {
        return getString(getDeclension().getArticleForm(getDeclension().getDefaultStartsWith(),
               getDeclension().getDefaultGender(), LanguageNumber.SINGULAR,
               getDeclension().getDefaultCase()));
    }

    /**
     * @return the position of this noun modifier WRT the noun.  In this case, it is *always* pre,
     * otherwise for all the languages, so far, the article is attached to the noun form.
     */
    @Override
    public LanguagePosition getPosition() {
        return LanguagePosition.PRE;
    }

    // Convenience methods
    public String getString(LanguageNumber number, LanguageGender gender, LanguageStartsWith startsWith) {
        return getString(getDeclension().getArticleForm(startsWith, gender, number, getDeclension().getDefaultCase()));
    }

    /**
     * Set the value of one of the forms of this adjective.
     * @param form the form to set
     * @param value the value to set
     */
    protected abstract void setString(ArticleForm form, String value);

    @Override
    public boolean validate(String name) {
        // Default validation to only requiring the most "generic" version of the adjective
        return defaultValidate(name, ImmutableSet.of(getDeclension().getArticleForm(LanguageStartsWith.CONSONANT, getDeclension().getDefaultGender(),
                LanguageNumber.SINGULAR, LanguageCase.NOMINATIVE)));
    }

    /**
     * A "simple" method for validating required terms and validating that they exist.
     * This generally depends on the declensions "return all forms"
     * TODO: This should compare the difference between specified forms and
     * choose the most similar (ie. the smallest Hamming distance)
     * @param name the name of the article, just for logging
     * @param requiredForms the set of required forms for articles in this language
     * @return {@code true} if the forms provided are valid.  Also fills in missing forms if necessary
     */
    public boolean defaultValidate(String name, Set<? extends ArticleForm> requiredForms) {
        for (ArticleForm form : getDeclension().getArticleForms()) {
            if (getString(form) == null) {
                if (requiredForms.contains(form)) {
                    logger.fine("###\tError: The article " + name + " is missing required " + form + " form");
                    // TODO: Uncomment the return false below once we actually handle validation
                    // Presently, the return value is simply ignored
                    // return false;
                }

                String s = null;
                // Article form is first to drop
                ArticleForm baseForm = null;
                // Next try starts with
                if (getDeclension().hasStartsWith() && form.getStartsWith() != getDeclension().getDefaultStartsWith()) {
                     baseForm = getDeclension().getArticleForm(getDeclension().getDefaultStartsWith(), form.getGender(), form.getNumber(), form.getCase());
                     s = getString(baseForm);
                }

                // Now gender
                if (s == null && getDeclension().hasGender() && form.getGender() != getDeclension().getDefaultGender()) {
                    baseForm = getDeclension().getArticleForm(form.getStartsWith(), getDeclension().getDefaultGender(), form.getNumber(), form.getCase());
                    s = getString(baseForm);
                }

                // Now case
                if (s == null && getDeclension().hasAllowedCases() && form.getCase() != getDeclension().getDefaultCase()) {
                    baseForm = getDeclension().getArticleForm(form.getStartsWith(), form.getGender(), form.getNumber(), getDeclension().getDefaultCase());
                    s = getString(baseForm);
                }

                // Now number
                if (s == null && form.getNumber() != LanguageNumber.SINGULAR) {
                    baseForm = getDeclension().getArticleForm(form.getStartsWith(), form.getGender(), LanguageNumber.SINGULAR, getDeclension().getDefaultCase());
                    s = getString(baseForm);
                }

                if (s == null) {
                    // There wasn't a specified value with just 1 difference (ie. 1 Hamming distance),
                    // so default to the absolute default value
                    s = getDefaultValue();
                    if (s == null) {
                        logger.info("###\tError: The article " + name + " has no " + form + " form and no default could be found");
                        return false;
                    } else {
                        logger.info("###\tERROR: The article " + name + " has no obvious default for " + form + "form");
                    }
                }
                
                setString(form, intern(s));
            }
        }
        return true;
    }

    @Override protected TermType getTermType() { return TermType.Article; }

    @Override
    public String toString() {
        return "Article-" + getDeclension().getLanguage().getLocale() + "-'" + getAllValues().get(getDeclension().getArticleForms().iterator().next()) + "'";
    }
}
