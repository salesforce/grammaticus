/*
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.impl;

import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.google.common.collect.ImmutableList;

/**
 * Contains commonalities between the Semitic languages supported by Salesforce (Hebrew, Arabic, and possibly Maltese)
 * <p>
 * Two genders, simple uninflected article, definite articles in the noun form, default is feminine, etc.
 *
 * @author stamm
 */
abstract class SemiticDeclension extends ArticledDeclension {

    public SemiticDeclension(HumanLanguage language) {
        super(language);
    }

    // All the forms you can set for articles
    private static EnumSet<LanguageGender> GENDER_TYPES = EnumSet.of(LanguageGender.FEMININE, LanguageGender.MASCULINE);
    private static EnumSet<LanguageArticle> ARTICLE_TYPES = EnumSet.of(LanguageArticle.ZERO, LanguageArticle.DEFINITE);
    static final List<? extends ArticleForm> ARTICLE_FORMS = ImmutableList.of(SimpleModifierForm.SINGULAR); // Simple modifier form

    @Override
    public boolean hasGender() {
        return true;
    }

    @Override
    public boolean hasStartsWith() {
        // This is not technically true for Arabic, but the declensions involved are not orthographically different for Arabic and Hebrew
        // For Maltese, this is not the case, and the need to distinguish solar and lunar consonants will be needed
        return false;
    }

    // The entity and compound nouns can be rendered in all forms
    @Override
    public Collection<? extends NounForm> getEntityForms() {
        return getAllNounForms();
    }

    @Override
    public Collection<? extends NounForm> getFieldForms() {
        return getAllNounForms();
    }

    @Override
    public LanguageGender getDefaultGender() {
        return LanguageGender.FEMININE;
    }

    @Override
    public Set<LanguageArticle> getAllowedArticleTypes() {
        return ARTICLE_TYPES;
    }

    @Override
    public boolean hasArticleInNounForm() {
        return true;
    }

    @Override
    public EnumSet<LanguageGender> getRequiredGenders() {
        return GENDER_TYPES;
    }

    @Override
    public boolean hasSubjectGenderInVerbConjugation() {
        return true;
    }

    @Override
    protected String getDefaultArticleString(ArticleForm form, LanguageArticle articleType) {
        if (articleType == LanguageArticle.DEFINITE) return getDefiniteArticlePrefix(form.getStartsWith());
        assert false : "Don't use the article=\"the\" form in the label files for semitic languages";
        return "";
    }

    /**
     * @param startsWith startsWith parameter (currently unused) in case the form needs to be different based on the next noun.
     * @return the definite marker prefix used in the semitic language
     */
    protected abstract String getDefiniteArticlePrefix(LanguageStartsWith startsWith);

    @Override
    protected Article createArticle(String name, LanguageArticle articleType) {
        return new SimpleArticle(this, name, articleType);
    }

    @Override
    public ArticleForm getArticleForm(LanguageStartsWith startsWith, LanguageGender gender, LanguageNumber number,
                                      LanguageCase case1) {
        return SimpleModifierForm.SINGULAR;
    }

    @Override
    public List<? extends ArticleForm> getArticleForms() {
        return ARTICLE_FORMS;
    }
}
