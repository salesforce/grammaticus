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

import java.util.*;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;
import com.google.common.collect.ImmutableList;

/**
 * Contains commonalities between the Semitic languages supported by Salesforce (Hebrew, Arabic, and possibly Maltese)
 *
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
    public Collection< ? extends NounForm> getEntityForms() {
        return getAllNounForms();
    }

    @Override
    public Collection< ? extends NounForm> getFieldForms() {
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
     * @return the definite marker prefix used in the semitic language
     * @param startsWith startsWith parameter (currently unused) in case the form needs to be different based on the next noun.
     */
    protected abstract String getDefiniteArticlePrefix(LanguageStartsWith startsWith);

    @Override
    public Article createArticle(String name, LanguageArticle articleType) {
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
